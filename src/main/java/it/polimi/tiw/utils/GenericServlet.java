
package it.polimi.tiw.utils;

import com.google.gson.Gson;
import it.polimi.tiw.bean.UserBean;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;


public class GenericServlet extends HttpServlet {

    protected static final String USER_SESSION_ATTRIBUTE = "user";
    protected static final String LOGIN_PAGE_PATH = "/login.html";
    protected static final String HOME_PAGE_PATH = "/home";
    protected static final String CART_PAGE_PATH = "/cart.html";

    protected static final String CART_CONTROLLER_PATH = "/cart";
    protected static final String ORDER_CONTROLLER_PATH = "/order";

    protected static final String CART_SESSION_VAR = "cart";
    protected static final String TMP_ORDERS_SESSION_VAR = "tmp_orders";

    protected static final String CART_CONTEXT_VAR = "tmp_orders";

    private static final long serialVersionUID = 1L;
    protected Connection connection = null;

    protected GenericServlet() {

        super();
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        super.init(servletConfig);
        connection = ConnectionHandler.getConnection(getServletContext());

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

    protected void sendVoidResponse(HttpServletResponse response) {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    protected void writeObject(Object o, HttpServletResponse response, int status) throws IOException {
        String json = new Gson().toJson(o);
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(json);
    }

    protected String escapeSQL(String sql) {

        return sql == null ? null : sql.replace("'", "\"").replace(";", "");
    }

}
