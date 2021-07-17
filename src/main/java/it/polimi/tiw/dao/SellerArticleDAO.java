
package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import it.polimi.tiw.utils.QueryExecutor;
import it.polimi.tiw.utils.SellerArticleEntity;

public class SellerArticleDAO {

    private final QueryExecutor queryExecutor;

    public SellerArticleDAO(Connection connection) {

        queryExecutor = new QueryExecutor(connection);
    }

    public Optional<SellerArticleEntity> findEntry(String articleId, String sellerId) throws SQLException {

        String query = "SELECT * FROM seller_article  WHERE article_id=:article_id and seller_id=:seller_id";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("article_id", articleId);
        queryParam.put("seller_id", sellerId);
        List<SellerArticleEntity> sellerArticleEntries = queryExecutor.select(query, queryParam,
                SellerArticleEntity.class);

        if (sellerArticleEntries.size() == 1) return Optional.of(sellerArticleEntries.get(0));
        else
            return Optional.empty();
    }

}
