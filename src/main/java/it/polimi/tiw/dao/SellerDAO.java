
package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.bean.SellerBean;
import it.polimi.tiw.bean.SellerOfferBean;
import it.polimi.tiw.utils.QueryExecutor;

public class SellerDAO {

    private final QueryExecutor queryExecutor;

    public SellerDAO(Connection connection) {

        queryExecutor = new QueryExecutor(connection);
    }

    public Optional<SellerBean> getSellerFromId(String id) throws SQLException {
        String query = "SELECT * FROM seller WHERE id=:id";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("id", id);
        List<SellerBean> sellers = queryExecutor.select(query, queryParam, SellerBean.class);
        if (sellers.size() == 1) return Optional.of(sellers.get(0));
        else
            return Optional.empty();
    }

    public List<SellerOfferBean> findSellerByArticleId(String id) throws SQLException {

        String query = "SELECT * FROM seller INNER JOIN seller_article ON seller_article.seller_id=seller.id WHERE article_id=:id";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("id", id);
        return queryExecutor.select(query, queryParam, SellerOfferBean.class);
    }

}
