
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.bean.SellerOfferBean;
import it.polimi.tiw.bean.ShippingPolicyBean;
import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.ArticleDAO;
import it.polimi.tiw.dao.SellerDAO;
import it.polimi.tiw.dao.ShipmentPolicyDAO;
import it.polimi.tiw.dao.ViewDAO;
import it.polimi.tiw.utils.GenericServlet;
import it.polimi.tiw.utils.ViewEntity;

@WebServlet("/article")
public class ArticleController extends GenericServlet {

    private static final Logger log                  = LoggerFactory.getLogger(ArticleController.class.getSimpleName());

    private static final String ARTICLE_ID_ATTRIBUTE = "article_id";

    private static final long   serialVersionUID     = 1L;

    public ArticleController() {

        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String articleId;
        try {
            articleId = StringEscapeUtils.escapeJava(req.getParameter(ARTICLE_ID_ATTRIBUTE));
        } catch (Exception e) {
            log.error("Something went wrong when extracting search hint parameters. Cause is {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed or missing parameters");
            return;
        }

        try {
            saveView(req.getSession(), articleId);
            Optional<ArticleBean> article = getArticle(articleId);
            if (article.isPresent()) {

                addArticleDetails(article.get());
                writeObject(article.get(), resp, HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            log.error("Something went wrong when extracting article with id {}. Cause is {}", articleId,
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

    private Optional<ArticleBean> getArticle(String articleId) throws SQLException {

        ArticleDAO articleDAO = new ArticleDAO(connection);
        return articleDAO.findArticleById(articleId);
    }

    private void addArticleDetails(ArticleBean articleBean) throws SQLException {

        SellerDAO sellerDAO = new SellerDAO(connection);
        List<SellerOfferBean> sellersOffers = sellerDAO.findSellerByArticleId(articleBean.getId());

        sellersOffers.forEach(sellerOfferBean -> {
            try {
                sellerOfferBean.setShippingPolicies(getSellerShippingPolicy(sellerOfferBean.getSellerId()));
            } catch (SQLException e) {
                log.error("Error when extracting shipping policies of seller {}. Cause is {}",
                        sellerOfferBean.getSellerId(), ExceptionUtils.getStackTrace(e));
            }
        });
        articleBean.setSellers(sellersOffers);
    }

    private List<ShippingPolicyBean> getSellerShippingPolicy(String sellerId) throws SQLException {

        ShipmentPolicyDAO shipmentPolicyDAO = new ShipmentPolicyDAO(connection);
        List<ShippingPolicyBean> shippingPolicyBeans = shipmentPolicyDAO.findPoliciesBySellerId(sellerId);

        return shippingPolicyBeans;
    }

    // private Pair<Integer, Float> getExistingArticlesOfSeller(String sellerId, HttpSession session) {
    //
    // Pair<Integer, Float> existingAmount = null;
    // Map<String, List<ArticleBean>> existingSellerArticles = (Map<String, List<ArticleBean>>) session
    // .getAttribute(CART_SESSION_VAR);
    // if (existingSellerArticles != null && existingSellerArticles.get(sellerId) != null) {
    // AtomicReference<Float> totalPrice = new AtomicReference<>((float) 0);
    // AtomicInteger articleCounter = new AtomicInteger();
    // existingSellerArticles.get(sellerId).forEach(articleBean -> {
    // totalPrice.set(totalPrice.get() + Float.parseFloat(articleBean.getPrice()));
    // articleCounter.getAndIncrement();
    // });
    // existingAmount = new Pair<>(articleCounter.get(), totalPrice.get());
    // }
    // return existingAmount;
    // }

}
