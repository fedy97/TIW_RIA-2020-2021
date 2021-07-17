
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.ArticleDAO;
import it.polimi.tiw.dao.SellerArticleDAO;
import it.polimi.tiw.utils.GenericServlet;
import it.polimi.tiw.utils.SellerArticleEntity;

@WebServlet("/add")
public class SaveArticleController extends GenericServlet {

    private static final Logger log                   = LoggerFactory
            .getLogger(SaveArticleController.class.getSimpleName());

    private static final String ARTICLE_ID_FORM_DATA  = "article_id";
    private static final String SELLER_ID_FORM_DATA   = "seller_id";
    private static final String ARTICLE_QTY_FORM_DATA = "article_qty";

    private static final long   serialVersionUID      = 1L;

    public SaveArticleController() {

        super();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.sendRedirect(getServletContext().getContextPath() + LOGIN_PAGE_PATH);
            return;
        }

        String articleId;
        String sellerId;
        Integer qty;
        try {
            articleId = StringEscapeUtils.escapeJava(req.getParameter(ARTICLE_ID_FORM_DATA));
            sellerId = StringEscapeUtils.escapeJava(req.getParameter(SELLER_ID_FORM_DATA));
            qty = Integer.parseInt(req.getParameter(ARTICLE_QTY_FORM_DATA));
            if (StringUtils.isBlank(articleId)) {
                throw new Exception("Undefined article id or qty");
            }

        } catch (Exception e) {
            log.error("Something went wrong when extracting article id or qty parameter. Cause is {}",
                    ExceptionUtils.getStackTrace(e));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty required parameters");
            return;
        }

        try {
            log.debug("Article id {}, qty {}", articleId, qty);
            ArticleBean article = getExistingArticle(articleId);
            article.setQuantity(qty.toString());
            article.setPrice(getArticlePrice(articleId, sellerId));
            Map<String, List<ArticleBean>> savedArticles = getExistingArticles(req.getSession());
            addSellerArticle(sellerId, article, savedArticles);
            log.debug("Articles --> {}", savedArticles);
            log.debug("Articles size --> {}", savedArticles.size());
            req.getSession().setAttribute(CART_SESSION_VAR, savedArticles);
            resp.sendRedirect(getServletContext().getContextPath() + CART_CONTROLLER_PATH);
        } catch (Exception e) {
            log.error("Something went wrong when saving article number {}. Cause is {}", articleId, e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something went wrong when trying to add the article to the cart");
        }

    }

    private void addSellerArticle(String sellerId, ArticleBean article, Map<String, List<ArticleBean>> savedArticles) {

        savedArticles.computeIfAbsent(sellerId, k -> new ArrayList<>());

        Optional<ArticleBean> existingEntry = savedArticles.get(sellerId).stream().findFirst();
        if (existingEntry.isPresent()) {
            existingEntry.get().setQuantity(Integer.toString(
                    Integer.parseInt(existingEntry.get().getQuantity()) + Integer.parseInt(article.getQuantity())));
        } else
            savedArticles.get(sellerId).add(article);
    }

    private String getArticlePrice(String articleId, String sellerId) throws SQLException {

        Optional<SellerArticleEntity> sellerArticleEntry = new SellerArticleDAO(connection).findEntry(articleId,
                sellerId);
        if (sellerArticleEntry.isPresent()) return sellerArticleEntry.get().getPrice();
        return "";
    }

    private ArticleBean getExistingArticle(String articleId) throws Exception {

        ArticleDAO articleDAO = new ArticleDAO(connection);
        Optional<ArticleBean> articleBean = articleDAO.findArticleById(articleId);
        if (!articleBean.isPresent()) throw new Exception("Article not found!");
        else
            return articleBean.get();
    }

    private Map<String, List<ArticleBean>> getExistingArticles(HttpSession session) {

        Object savedArticles = session.getAttribute(CART_SESSION_VAR);
        if (savedArticles != null) return (Map<String, List<ArticleBean>>) savedArticles;
        else
            return new HashMap<>();
    }

}
