
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.bean.SellerOfferBean;
import it.polimi.tiw.bean.ShippingPolicyBean;
import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.ArticleDAO;
import it.polimi.tiw.dao.SellerDAO;
import it.polimi.tiw.dao.ShipmentPolicyDAO;
import it.polimi.tiw.dao.ViewDAO;
import it.polimi.tiw.utils.GenericServlet;
import it.polimi.tiw.utils.Pair;
import it.polimi.tiw.utils.ViewEntity;

@WebServlet("/search")
public class SearchController extends GenericServlet {

    private static final Logger log                              = LoggerFactory
            .getLogger(SearchController.class.getSimpleName());

    private static final String HINT_ATTRIBUTE                   = "hint";
    private static final String ARTICLE_ID_ATTRIBUTE             = "article_id";

    private static final String SELLER_OFFERS_CONTEXT_VAR        = "seller_offers";
    private static final String ARTICLE_DETAILS_CONTEXT_VAR      = "article";
    private static final String SHOW_ARTICLE_DETAILS_CONTEXT_VAR = "show_article_details";
    private static final String RESULT_CONTEXT_VAR               = "searched_articles";
    private static final String HINT_CONTEXT_VAR                 = "hint";

    private static final String RESULTS_PAGE_PATH                = "/results.html";

    private static final long   serialVersionUID                 = 1L;

    public SearchController() {

        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.sendRedirect(getServletContext().getContextPath() + LOGIN_PAGE_PATH);
            return;
        }

        String keyword;
        String articleId;
        try {
            keyword = StringEscapeUtils.escapeJava(req.getParameter(HINT_ATTRIBUTE));
            articleId = req.getParameter(ARTICLE_ID_ATTRIBUTE);

        } catch (Exception e) {
            log.error("Something went wrong when extracting search hint parameters. Cause is {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed parameters");
            return;
        }

        try {
            log.debug("Search keyword {}", keyword);
            List<ArticleBean> foundArticles = getSearchedArticles(keyword, articleId);
            ServletContext servletContext = getServletContext();
            final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());

            if (StringUtils.isNotBlank(articleId)) {
                saveView(req.getSession(), articleId);
                ctx.setVariable(SHOW_ARTICLE_DETAILS_CONTEXT_VAR, true);
                getArticleDetails(articleId)
                        .ifPresent(articleBean -> ctx.setVariable(ARTICLE_DETAILS_CONTEXT_VAR, articleBean));
                addArticleDetails(articleId, ctx, req.getSession());
            }

            ctx.setVariable(RESULT_CONTEXT_VAR, foundArticles);
            ctx.setVariable(HINT_CONTEXT_VAR, keyword);

            templateEngine.process(RESULTS_PAGE_PATH, ctx, resp.getWriter());
        } catch (Exception e) {
            log.error("Something went wrong when extracting article by keyword {}. Cause is {}", keyword,
                    ExceptionUtils.getStackTrace(e));
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something went wrong when searching the specified articles");
        }

    }

    private void saveView(HttpSession session, String articleId) {

        ViewDAO viewDAO = new ViewDAO(connection);
        UserBean userBean = (UserBean) session.getAttribute(USER_SESSION_ATTRIBUTE);
        viewDAO.insertView(new ViewEntity(userBean.getId(), articleId));
    }

    private Optional<ArticleBean> getArticleDetails(String articleId) throws SQLException {

        ArticleDAO articleDAO = new ArticleDAO(connection);
        return articleDAO.findArticleById(articleId);
    }

    private void addArticleDetails(String articleId, WebContext ctx, HttpSession session) throws SQLException {

        SellerDAO sellerDAO = new SellerDAO(connection);
        List<SellerOfferBean> sellersOffers = sellerDAO.findSellerByArticleId(articleId);

        sellersOffers.forEach(sellerOfferBean -> {
            try {
                sellerOfferBean.setShippingPolicies(getSellerShippingPolicy(sellerOfferBean.getSellerId()));
            } catch (SQLException e) {
                log.error("Error when extracting shipping policies of seller {}. Cause is {}",
                        sellerOfferBean.getSellerId(), ExceptionUtils.getStackTrace(e));
            }
            sellerOfferBean.setExistingArticles(getExistingArticlesOfSeller(sellerOfferBean.getSellerId(), session));
        });
        ctx.setVariable(SELLER_OFFERS_CONTEXT_VAR, sellersOffers);
    }

    private List<ShippingPolicyBean> getSellerShippingPolicy(String sellerId) throws SQLException {

        ShipmentPolicyDAO shipmentPolicyDAO = new ShipmentPolicyDAO(connection);
        List<ShippingPolicyBean> shippingPolicyBeans = shipmentPolicyDAO.findPoliciesBySellerId(sellerId);

        log.debug(shippingPolicyBeans.toString());
        return shippingPolicyBeans;
    }

    private Pair<Integer, Float> getExistingArticlesOfSeller(String sellerId, HttpSession session) {

        Pair<Integer, Float> existingAmount = null;
        Map<String, List<ArticleBean>> existingSellerArticles = (Map<String, List<ArticleBean>>) session
                .getAttribute(CART_SESSION_VAR);
        if (existingSellerArticles != null && existingSellerArticles.get(sellerId) != null) {
            AtomicReference<Float> totalPrice = new AtomicReference<>((float) 0);
            AtomicInteger articleCounter = new AtomicInteger();
            existingSellerArticles.get(sellerId).forEach(articleBean -> {
                totalPrice.set(totalPrice.get() + Float.parseFloat(articleBean.getPrice()));
                articleCounter.getAndIncrement();
            });
            existingAmount = new Pair<>(articleCounter.get(), totalPrice.get());
        }
        return existingAmount;
    }

    private List<ArticleBean> getSearchedArticles(String keyword, String articleId) throws SQLException {

        ArticleDAO articleDAO = new ArticleDAO(connection);
        List<ArticleBean> articleBeanList = articleDAO.findArticleByKeyword(keyword);
        return StringUtils.isNotBlank(articleId)
                ? articleBeanList.stream().filter(el -> !articleId.equals(el.getId())).collect(Collectors.toList())
                : articleBeanList;
    }

}
