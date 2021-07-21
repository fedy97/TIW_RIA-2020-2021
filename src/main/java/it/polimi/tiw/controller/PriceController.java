
package it.polimi.tiw.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.ArticleDAO;
import it.polimi.tiw.model.PriceModel;
import it.polimi.tiw.utils.GenericServlet;

@WebServlet("/price")
public class PriceController extends GenericServlet {

    private static final Logger log                  = LoggerFactory.getLogger(PriceController.class.getSimpleName());

    private static final String ARTICLE_ID_ATTRIBUTE = "article_id";
    private static final String SELLER_ID_ATTRIBUTE  = "seller_id";

    private static final long   serialVersionUID     = 1L;

    public PriceController() {

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
        String sellerId;
        try {
            articleId = StringEscapeUtils.escapeJava(req.getParameter(ARTICLE_ID_ATTRIBUTE));
            sellerId = StringEscapeUtils.escapeJava(req.getParameter(SELLER_ID_ATTRIBUTE));
        } catch (Exception e) {
            log.error("Something went wrong when extracting parameters. Cause is {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed or missing parameters");
            return;
        }

        try {
            Float price = new ArticleDAO(connection).getArticlePrice(sellerId, articleId);
            writeObject(new PriceModel(price), resp, HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.error("Something went wrong when extracting the price of article with id {}. Cause is {}", articleId,
                    ExceptionUtils.getStackTrace(e));
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something went wrong when searching the specified articles");
        }

    }

}
