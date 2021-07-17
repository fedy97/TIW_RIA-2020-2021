
package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import it.polimi.tiw.bean.ShippingPolicyBean;
import it.polimi.tiw.utils.QueryExecutor;

public class ShipmentPolicyDAO {

    private final QueryExecutor queryExecutor;

    public ShipmentPolicyDAO(Connection connection) {

        queryExecutor = new QueryExecutor(connection);
    }

    public Optional<ShippingPolicyBean> findPolicyByQty(String sellerId, Integer qty) throws SQLException {

        String query = "SELECT * FROM shipping_policy WHERE seller_id=:seller_id AND :qty >= min_item AND :qty < coalesce(max_item, 99999999999)";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("seller_id", sellerId);
        queryParam.put("qty", qty);
        List<ShippingPolicyBean> shippingPolicies = queryExecutor.select(query, queryParam, ShippingPolicyBean.class);
        if (shippingPolicies.size() == 1) return Optional.of(shippingPolicies.get(0));
        else
            return Optional.empty();
    }

    public List<ShippingPolicyBean> findPoliciesBySellerId(String sellerId) throws SQLException {

        String query = "SELECT * FROM shipping_policy WHERE seller_id=:seller_id";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("seller_id", sellerId);
        return queryExecutor.select(query, queryParam, ShippingPolicyBean.class);
    }
}
