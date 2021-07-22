
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import it.polimi.tiw.bean.*;
import it.polimi.tiw.dao.ArticleDAO;
import it.polimi.tiw.dao.OrderDAO;
import it.polimi.tiw.dao.SellerDAO;
import it.polimi.tiw.dao.ShipmentPolicyDAO;
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

        // get and check params
        String orderId = null;

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        String userId = user.get().getId();

        try {
            orderId = req.getParameter("order-id");
        } catch (NumberFormatException | NullPointerException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
            return;
        }
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws  IOException {

        Optional<UserBean> user = getUserData(request);
        if (!user.isPresent()) {
            response.sendRedirect(getServletContext().getContextPath() + LOGIN_PAGE_PATH);
            return;
        }

        String userId = user.get().getId();

        // Get and parse all parameters from request
        boolean isBadRequest = false;
        String sellerId = null;
        String articles = null;
        try {
            sellerId = request.getParameter("seller_id");
            articles = request.getParameter("articles");
            //articles = "[{'quantity': '2', 'id': '1'},{'quantity': '3', 'id': '2'}]";

        } catch (NumberFormatException | NullPointerException e) {
            isBadRequest = true;
            e.printStackTrace();
        }
        if (isBadRequest) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }
        OrderBean orderBean = new OrderBean();
        OrderDAO orderDAO = new OrderDAO(connection);
        try {
            addArticlesToOrder(orderBean, articles);
            orderBean.setUserId(userId);
            orderBean.setSellerId(sellerId);
            orderBean.setOrderDate(new Date().toString());
            orderBean.setShipmentDate(new Date().toString());
            orderBean.setPriceArticles(computePriceArticles(sellerId, orderBean.getArticleBeans()));
            orderBean.setPriceShipment(Float.toString(extractShipmentPrice(sellerId, computeTotalArticles(orderBean.getArticleBeans()).toString(), orderBean.getPriceArticles())));
            orderDAO.createOrder(orderBean);
        } catch (SQLException sqlException) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create order");
        }

        writeObject(orderBean, response, HttpServletResponse.SC_OK);
    }

    private List<OrderBean> getOrder(String id) throws SQLException {

        OrderDAO orderDAO = new OrderDAO(connection);
        return orderDAO.findOrderById(id);
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

    protected String computePriceArticles(String sellerId, List<ArticleBean> articleBeanList) {

        float total = 0F;
        for (ArticleBean articleBean : articleBeanList)
            total += Float.parseFloat(articleBean.getQuantity()) * extractArticlePrice(articleBean.getId(), sellerId);

        return Float.toString(total);
    }

    private Float extractArticlePrice(String articleId, String sellerId) {

        ArticleDAO articleDAO = new ArticleDAO(connection);
        try {
            return articleDAO.getArticlePrice(sellerId, articleId);
        } catch (SQLException e) {
            log.error("Something went wrong when extracting price for article {} of seller {}", articleId, sellerId);
        }
        return 0F;
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
