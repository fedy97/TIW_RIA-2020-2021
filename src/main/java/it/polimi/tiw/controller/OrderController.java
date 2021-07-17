
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.bean.OrderBean;
import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.OrderDAO;
import it.polimi.tiw.utils.GenericServlet;

@WebServlet("/order")
public class OrderController extends GenericServlet {

    private static final Logger log                = LoggerFactory.getLogger(OrderController.class.getSimpleName());

    private static final String RESULTS_PAGE_PATH  = "/orders.html";
    private static final String ORDERS_CONTEXT_VAR = "foundOrders";

    public OrderController() {

        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // get and check params
        String orderId = null;

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.sendRedirect(getServletContext().getContextPath() + LOGIN_PAGE_PATH);
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

                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
                ctx.setVariable(ORDERS_CONTEXT_VAR, foundOrder.get(0));
                templateEngine.process(RESULTS_PAGE_PATH, ctx, resp.getWriter());
            } else {
                // get orders
                List<OrderBean> foundOrders = getOrders(userId);
                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
                ctx.setVariable(ORDERS_CONTEXT_VAR, foundOrders);
                templateEngine.process(RESULTS_PAGE_PATH, ctx, resp.getWriter());
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Optional<UserBean> user = getUserData(request);
        if (!user.isPresent()) {
            response.sendRedirect(getServletContext().getContextPath() + LOGIN_PAGE_PATH);
            return;
        }

        String userId = user.get().getId();

        // Get and parse all parameters from request
        boolean isBadRequest = false;
        String sellerId = null;
        try {
            sellerId = request.getParameter("seller_id");

        } catch (NumberFormatException | NullPointerException e) {
            isBadRequest = true;
            e.printStackTrace();
        }
        if (isBadRequest) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }

        // Create mission in DB
        OrderDAO orderDAO = new OrderDAO(connection);
        Map<String, OrderBean> orders;
        try {
            orders = (Map<String, OrderBean>) request.getSession().getAttribute(TMP_ORDERS_SESSION_VAR);
            OrderBean orderBean = orders.get(sellerId);
            orderBean.setUserId(userId);
            orderBean.setSellerId(sellerId);
            orderBean.setOrderDate(new Date().toString());
            orderBean.setShipmentDate(new Date().toString());
            orderDAO.createOrder(orderBean);
        } catch (SQLException sqlException) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create order");
            return;
        }
        // remove orders from session
        orders.remove(sellerId);
        request.getSession().setAttribute(TMP_ORDERS_SESSION_VAR, orders);
        removeOrderFromCart(request.getSession(), sellerId);

        // return the user to the right view
        response.sendRedirect(getServletContext().getContextPath() + ORDER_CONTROLLER_PATH);

    }

    private List<OrderBean> getOrder(String id) throws SQLException {

        OrderDAO orderDAO = new OrderDAO(connection);
        return orderDAO.findOrderById(id);
    }

    private List<OrderBean> getOrders(String userId) throws SQLException {

        OrderDAO orderDAO = new OrderDAO(connection);
        return orderDAO.findOrders(userId);
    }

    private void removeOrderFromCart(HttpSession session, String sellerId) {

        Map<String, List<ArticleBean>> savedArticles = (Map<String, List<ArticleBean>>) session
                .getAttribute(CART_SESSION_VAR);
        savedArticles.remove(sellerId);
        session.setAttribute(CART_SESSION_VAR, savedArticles);
    }
}
