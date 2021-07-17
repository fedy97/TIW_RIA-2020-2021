
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.bean.ArticleBean;
import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.ArticleDAO;
import it.polimi.tiw.utils.GenericServlet;

@WebServlet("/home")
public class HomeController extends GenericServlet {

    private static final Logger  log                = LoggerFactory.getLogger(HomeController.class.getSimpleName());

    private static final String  RESULT_CONTEXT_VAR = "lastArticles";

    private static final Integer MAX_PAGE_ARTICLES  = 5;

    private static final long    serialVersionUID   = 1L;

    public HomeController() {

        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.sendRedirect(getServletContext().getContextPath() + LOGIN_PAGE_PATH);
            return;
        }

        String userId = user.get().getId();
        try {
            log.debug("Searching for last viewed articles of user {}", userId);
            List<ArticleBean> lastViewedArticles = getLastViewedArticles(userId);

            if (lastViewedArticles.size() < MAX_PAGE_ARTICLES) {
                List<ArticleBean> lastArticles = getLastArticles(MAX_PAGE_ARTICLES - lastViewedArticles.size());
                filterDuplicates(lastViewedArticles, lastArticles);
                lastViewedArticles.addAll(lastArticles);
            }

            ServletContext servletContext = getServletContext();
            final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
            ctx.setVariable(RESULT_CONTEXT_VAR, lastViewedArticles);
            templateEngine.process(HOME_PAGE_PATH, ctx, resp.getWriter());
        } catch (Exception e) {
            log.error("Something went wrong when extracting last articles. Cause is {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something went wrong when searching the last articles");
        }

    }

    private List<ArticleBean> getLastViewedArticles(String userId) throws SQLException {

        ArticleDAO articleDAO = new ArticleDAO(connection);
        return articleDAO.findArticleByViews(userId);
    }

    private List<ArticleBean> getLastArticles(Integer articleNumbers) throws SQLException {

        ArticleDAO articleDAO = new ArticleDAO(connection);
        return articleDAO.findLastArticles(articleNumbers);
    }

    private void filterDuplicates(List<ArticleBean> viewedArticles, List<ArticleBean> lastArticles) {

        viewedArticles.forEach(viewedArticle -> lastArticles.removeIf(el -> el.getId().equals(viewedArticle.getId())));
    }

}
