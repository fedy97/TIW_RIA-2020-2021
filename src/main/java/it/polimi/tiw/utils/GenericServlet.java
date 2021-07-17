
package it.polimi.tiw.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.bean.UserBean;

public class GenericServlet extends HttpServlet {

    protected static final String USER_SESSION_ATTRIBUTE = "user";
    protected static final String LOGIN_PAGE_PATH        = "/login.html";
    protected static final String HOME_PAGE_PATH         = "/home";
    protected static final String CART_PAGE_PATH         = "/cart.html";

    protected static final String CART_CONTROLLER_PATH   = "/cart";
    protected static final String ORDER_CONTROLLER_PATH = "/order";

    protected static final String CART_SESSION_VAR       = "cart";
    protected static final String TMP_ORDERS_SESSION_VAR = "tmp_orders";

    protected static final String CART_CONTEXT_VAR       = "tmp_orders";

    private static final long     serialVersionUID       = 1L;
    protected Connection          connection             = null;
    protected TemplateEngine      templateEngine;

    protected GenericServlet() {

        super();
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        super.init(servletConfig);
        connection = ConnectionHandler.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    @Override
    public void destroy() {

        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected Optional<UserBean> getUserData(HttpServletRequest request) {

        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute(USER_SESSION_ATTRIBUTE) == null) return Optional.empty();
        else
            return Optional.of((UserBean) session.getAttribute(USER_SESSION_ATTRIBUTE));

    }

}
