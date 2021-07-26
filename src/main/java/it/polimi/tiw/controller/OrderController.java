
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import it.polimi.tiw.bean.*;
import it.polimi.tiw.dao.ArticleDAO;
import it.polimi.tiw.dao.OrderDAO;
import it.polimi.tiw.dao.SellerDAO;
import it.polimi.tiw.dao.ShipmentPolicyDAO;
import it.polimi.tiw.utils.Exception400;
import it.polimi.tiw.utils.GenericServlet;

@WebServlet("/order")
@MultipartConfig
public class OrderController extends GenericServlet {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class.getSimpleName());

    public OrderController() {

        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String userId = user.get().getId();

        String orderId = escapeSQL(req.getParameter("order-id"));

        try {
            if (orderId != null) {
                // get order by id
                List<OrderBean> foundOrder = getOrder(orderId);
                // check if the order exists
                if (foundOrder.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                    return;
                }
                // check if the order belongs to the logged user
                if (!foundOrder.get(0).getUserId().equals(userId)) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot view this order");
                    return;
                }
                writeObject(foundOrder, resp, HttpServletResponse.SC_OK);

            } else {
                // get orders
                List<OrderBean> foundOrders = getOrders(userId);
                writeObject(foundOrders, resp, HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Optional<UserBean> user = getUserData(request);
        if (!user.isPresent()) {
            response.sendRedirect(getServletContext().getContextPath() + LOGIN_PAGE_PATH);
            return;
        }

        String userId = user.get().getId();

        String sellerId = request.getParameter("seller_id");
        String articles = request.getParameter("articles");

        if (StringUtils.isBlank(sellerId) || StringUtils.isBlank(articles)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }
        OrderBean orderBean = new OrderBean();
        OrderDAO orderDAO = new OrderDAO(connection);
        try {
            addArticlesToOrder(orderBean, articles);
            orderBean.setUserId(userId);

            if (!isValidSeller(sellerId)) throw new Exception400("Invalid sellerid");

            orderBean.setSellerId(sellerId);
            orderBean.setOrderDate(new Date().toString());
            orderBean.setShipmentDate(new Date().toString());
            orderBean.setPriceArticles(computePriceArticles(sellerId, orderBean.getArticleBeans()));
            orderBean.setPriceShipment(Float.toString(extractShipmentPrice(sellerId,
                    computeTotalArticles(orderBean.getArticleBeans()).toString(), orderBean.getPriceArticles())));
            orderDAO.createOrder(orderBean);
        } catch (SQLException sqlException) {
            log.error(ExceptionUtils.getStackTrace(sqlException));
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create order");
            return;
        } catch (Exception400 e) {
            log.error(ExceptionUtils.getStackTrace(e));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid input parameters");
            return;
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            return;
        }

        writeObject(orderBean, response, HttpServletResponse.SC_OK);
    }

    private List<OrderBean> getOrder(String id) throws SQLException {

        OrderDAO orderDAO = new OrderDAO(connection);
        return orderDAO.findOrderById(id);
    }

    private boolean isValidSeller(String sellerId) throws SQLException {

        SellerDAO sellerDAO = new SellerDAO(connection);
        return sellerDAO.getSellerFromId(sellerId).isPresent();
    }

    private List<OrderBean> getOrders(String userId) throws SQLException {

        OrderDAO orderDAO = new OrderDAO(connection);
        return orderDAO.findOrders(userId);
    }

    private void addArticlesToOrder(OrderBean orderBean, String articles) {

        Gson gson = new Gson();
        List<ArticleBean> articleBeans = Arrays.asList(gson.fromJson(articles, ArticleBean[].class));
        orderBean.setArticleBeans(articleBeans);
    }

    protected String computePriceArticles(String sellerId, List<ArticleBean> articleBeanList) throws Exception {

        float total = 0F;
        for (ArticleBean articleBean : articleBeanList) {
            try {
                total += Float.parseFloat(articleBean.getQuantity()) * extractArticlePrice(articleBean.getId(), sellerId);
                if (Float.parseFloat(articleBean.getQuantity()) < 1) throw new Exception400("Invalid article quantity, you have to insert a positive quantity");
            } catch (NumberFormatException e) {
                throw new Exception400("Invalid article quantity, you have to insert a number");
            }
        }

        return Float.toString(total);
    }

    private Float extractArticlePrice(String articleId, String sellerId) throws Exception {

        ArticleDAO articleDAO = new ArticleDAO(connection);

        return articleDAO.getArticlePrice(sellerId, articleId);
    }

    protected Integer computeTotalArticles(List<ArticleBean> articleBeanList) {

        AtomicInteger counter = new AtomicInteger();
        articleBeanList.forEach(articleBean -> counter.getAndAdd(Integer.parseInt(articleBean.getQuantity())));
        return counter.get();
    }

    protected Float extractShipmentPrice(String sellerId, String articleQty, String priceArticles) {

        ShipmentPolicyDAO shipmentPolicyDAO = new ShipmentPolicyDAO(connection);
        SellerDAO sellerDAO = new SellerDAO(connection);

        try {
            Optional<SellerBean> sellerBean = sellerDAO.getSellerFromId(sellerId);
            if (sellerBean.isPresent() && Float.parseFloat(sellerBean.get().getPriceThreshold()) != 0.0f
                    && Float.parseFloat(priceArticles) >= Float.parseFloat(sellerBean.get().getPriceThreshold()))
                return 0F;
            Optional<ShippingPolicyBean> shippingPolicy = shipmentPolicyDAO.findPolicyByQty(sellerId,
                    Integer.parseInt(articleQty));

            return shippingPolicy.map(shippingPolicyBean -> Float.parseFloat(shippingPolicyBean.getShipCost()))
                    .orElse(0F);
        } catch (SQLException e) {
            log.error("Something went wrong when extracting shippin policy for seller {}", sellerId);
        }
        return 0F;
    }
}
