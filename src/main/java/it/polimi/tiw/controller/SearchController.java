
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.ArticleDAO;
import it.polimi.tiw.utils.GenericServlet;

@WebServlet("/search")
public class SearchController extends GenericServlet {

    private static final Logger log              = LoggerFactory.getLogger(SearchController.class.getSimpleName());

    private static final String HINT_ATTRIBUTE   = "hint";

    private static final long   serialVersionUID = 1L;

    public SearchController() {

        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String keyword;
        try {
            keyword = escapeSQL(req.getParameter(HINT_ATTRIBUTE));

            if (StringUtils.isBlank(keyword)) throw new Exception("Missing search keyword");
        } catch (Exception e) {
            log.error("Something went wrong when extracting search hint parameters. Cause is {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed or missing parameters");
            return;
        }

        try {
            log.debug("Search keyword {}", keyword);
            List<ArticleBean> foundArticles = getSearchedArticles(keyword);
            writeObject(foundArticles, resp, HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.error("Something went wrong when extracting article by keyword {}. Cause is {}", keyword,
                    ExceptionUtils.getStackTrace(e));
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something went wrong when searching the specified articles");
        }

    }

    private List<ArticleBean> getSearchedArticles(String keyword) throws SQLException {

        ArticleDAO articleDAO = new ArticleDAO(connection);
        return articleDAO.findArticleByKeyword(keyword);
    }

}
