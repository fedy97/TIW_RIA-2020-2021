
package it.polimi.tiw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.GenericServlet;

@WebServlet("/login")
public class LoginController extends GenericServlet {

    private static final Logger log              = LoggerFactory.getLogger(LoginController.class.getSimpleName());

    private static final String USER_PARAM       = "username";
    private static final String PWD_PARAM        = "pwd";

    private static final long   serialVersionUID = 1L;

    public LoginController() {

        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String usrn;
        String pwd;
        try {
            usrn = StringEscapeUtils.escapeJava(request.getParameter(USER_PARAM));
            pwd = StringEscapeUtils.escapeJava(request.getParameter(PWD_PARAM));

            if (StringUtils.isBlank(usrn) || StringUtils.isBlank(pwd)) {
                throw new Exception("Missing or empty credential value");
            }
            String regex = "^(.+)@(.+)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(usrn);
            if (!matcher.matches())
                throw new Exception("Not a valid email");

        } catch (Exception e) {
            log.error("Something went wrong when extracting login form parameters. Cause is {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        try {
            Optional<UserBean> user = getUserEntity(usrn, pwd);
            if (!user.isPresent()) {
                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
                ctx.setVariable("errorMsg", "Incorrect username or password");
                templateEngine.process(LOGIN_PAGE_PATH, ctx, response.getWriter());
            } else {
                request.getSession().setAttribute(USER_SESSION_ATTRIBUTE, user.get());
                response.sendRedirect(getServletContext().getContextPath() + HOME_PAGE_PATH);
            }
        } catch (SQLException e) {
            log.error("Something went wrong when extracting user data. Cause is {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
        }
    }

    private Optional<UserBean> getUserEntity(String usrn, String pwd) throws SQLException {

        UserDAO userDao = new UserDAO(connection);
        return userDao.checkCredentials(usrn, pwd);
    }

}
