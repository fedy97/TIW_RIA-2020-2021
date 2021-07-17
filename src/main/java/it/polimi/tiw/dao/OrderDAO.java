
package it.polimi.tiw.dao;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.bean.OrderBean;
import it.polimi.tiw.utils.QueryExecutor;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class OrderDAO {

    private final QueryExecutor queryExecutor;
    private final Connection connection;

    public OrderDAO(Connection connection) {
        this.connection = connection;
        queryExecutor = new QueryExecutor(connection);
    }

    public List<OrderBean> findOrderById(String orderId) throws SQLException {

        String query = "SELECT O.id, O.seller_id, O.price_articles, O.price_shipment, O.shipment_date, O.order_date, O.user_id, S.seller_name, S.seller_rating, U.name, U.surname, U.email, U.shipment_addr "
                + "FROM ecommerce.order O LEFT JOIN ecommerce.seller S " + "on O.seller_id = S.id "
                + "LEFT JOIN ecommerce.user U " + "on O.user_id = U.id " + "where O.id = :orderId";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("orderId", orderId);
        List<OrderBean> result = queryExecutor.select(query, queryParam, OrderBean.class);
        if (result.isEmpty()) return new ArrayList<>();
        result.get(0).setArticleBeans(findArticlesByOrderId(orderId, result.get(0).getSellerId()));
        return result;
    }

    public List<OrderBean> findOrders(String userId) throws SQLException {

        String query = "SELECT O.id, O.seller_id, O.price_articles, O.price_shipment, O.shipment_date, O.order_date, O.user_id, S.seller_name, S.seller_rating, U.name, U.surname, U.email, U.shipment_addr "
                + "FROM ecommerce.order O LEFT JOIN ecommerce.seller S " + "on O.seller_id = S.id "
                + "LEFT JOIN ecommerce.user U " + "on O.user_id = U.id " + "where O.user_id = :userId "
                + "ORDER BY O.order_date DESC";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("userId", userId);
        List<OrderBean> result = queryExecutor.select(query, queryParam, OrderBean.class);
        for (OrderBean orderBean : result) {
            orderBean.setArticleBeans(findArticlesByOrderId(orderBean.getId(), orderBean.getSellerId()));
            float total = Float.parseFloat(orderBean.getPriceShipment()) + Float.parseFloat(orderBean.getPriceArticles());
            orderBean.setPriceTotal(Float.toString(total));
        }
        return result;
    }

    public void createOrder(OrderBean orderBean)
            throws SQLException {
        try {
            connection.setAutoCommit(false);

            String query = "INSERT into ecommerce.order (seller_id, user_id, price_articles, price_shipment) VALUES (?, ?, ?, ?)";
            int orderId;
            try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
                pstatement.setInt(1, Integer.parseInt(orderBean.getSellerId()));
                pstatement.setInt(2, Integer.parseInt(orderBean.getUserId()));
                pstatement.setFloat(3, Float.parseFloat(orderBean.getPriceArticles()));
                pstatement.setFloat(4, Float.parseFloat(orderBean.getPriceShipment()));
                int affectedRows = pstatement.executeUpdate();

                if (affectedRows == 0)
                    throw new SQLException("Creating order failed, no rows affected.");
                // get order id just created by sql auto increment function
                try (ResultSet generatedKeys = pstatement.getGeneratedKeys()) {
                    if (generatedKeys.next())
                        orderId = (int) generatedKeys.getLong(1);
                    else
                        throw new SQLException("Creating order failed, no ID obtained.");
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            for (ArticleBean articleBean : orderBean.getArticleBeans()) {
                String query2 = "INSERT into ecommerce.order_article (order_id, article_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement pstatement = connection.prepareStatement(query2);) {
                    pstatement.setInt(1, orderId);
                    pstatement.setInt(2, Integer.parseInt(articleBean.getId()));
                    pstatement.setInt(3, Integer.parseInt(articleBean.getQuantity()));
                    pstatement.executeUpdate();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ArticleBean> findArticlesByOrderId(String orderId, String sellerId) throws SQLException {

        String query = "SELECT A.id, A.name, A.description, A.category, A.photo, A_O.quantity, S_A.price "
                + "FROM ecommerce.article A LEFT JOIN ecommerce.order_article A_O " + "on A.id = A_O.article_id "
                + "LEFT JOIN ecommerce.seller_article S_A " + "on A.id = S_A.article_id "
                + "where A_O.order_id = :orderId AND S_A.seller_id = :sellerId";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("sellerId", sellerId);
        queryParam.put("orderId", orderId);
        return queryExecutor.select(query, queryParam, ArticleBean.class);
    }
}
