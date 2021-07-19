
package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.utils.QueryExecutor;

public class ArticleDAO {

    private final QueryExecutor queryExecutor;

    public ArticleDAO(Connection connection) {

        queryExecutor = new QueryExecutor(connection);
    }

    public Optional<ArticleBean> findArticleById(String id) throws SQLException {

        String query = "SELECT * FROM article WHERE id=:id";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("id", id);
        List<ArticleBean> articles = queryExecutor.select(query, queryParam, ArticleBean.class);
        if (articles.size() == 1) return Optional.of(articles.get(0));
        else
            return Optional.empty();
    }

    public List<ArticleBean> findArticleByKeyword(String keyword) throws SQLException {

        String query = "SELECT article.*, MIN(price) as price FROM article INNER JOIN seller_article ON (id = article_id) WHERE name like :keyword or description like :keyword " +
                "GROUP BY article.id, article.name, article.description, article.category, article.photo, article.insr_ts ORDER BY price";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("keyword", "%" + keyword + "%");
        return queryExecutor.select(query, queryParam, ArticleBean.class);
    }

    public List<ArticleBean> findArticleByViews(String userId) throws SQLException {

        String query = "SELECT DISTINCT article.*, MIN(price) as price FROM article INNER JOIN seller_article ON article.id = seller_article.article_id  LEFT OUTER JOIN user_article ON article.id=user_article.article_id  WHERE user_article.user_id = :userid " +
                "GROUP BY article.id, article.name, article.description, article.category, article.photo, article.insr_ts ORDER BY user_article.view_ts DESC LIMIT 5";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("userid", userId);
        return queryExecutor.select(query, queryParam, ArticleBean.class);
    }

    public List<ArticleBean> findLastArticles(Integer articlesNumber) throws SQLException {

        String query = "SELECT DISTINCT article.*, MIN(price) as price FROM article a INNER JOIN seller_article ON article.id = seller_article.article_id WHERE a.category='Accessori auto' GROUP BY a.id, a.name, a.description, a.category, a.photo, a.insr_ts LIMIT " + articlesNumber;
        return queryExecutor.select(query, new HashMap<>(), ArticleBean.class);
    }

    public Float getArticlePrice(String sellerId, String articleId) throws SQLException {

        String query = "SELECT * FROM article INNER JOIN seller_article ON (id = article_id) WHERE article_id=:article_id AND seller_id=:seller_id";
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("seller_id", sellerId);
        queryParam.put("article_id", articleId);
        List<ArticleBean> articles = queryExecutor.select(query, queryParam, ArticleBean.class);
        if (articles.size() == 1) return Float.parseFloat(articles.get(0).getPrice());
        else
            return 0F;
    }

}
