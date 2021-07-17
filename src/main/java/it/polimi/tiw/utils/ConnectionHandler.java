
package it.polimi.tiw.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

public class ConnectionHandler {

    private static final String DB_DRIVER_PARAM_NAME = "dbDriver";
    private static final String DB_URL_PARAM_NAME    = "dbUrl";
    private static final String DB_USER_PARAM_NAME   = "dbUser";
    private static final String DB_PPP_PARAM_NAME    = "dbPassword";

    public static Connection getConnection(ServletContext context) throws UnavailableException {

        Connection connection = null;
        try {

            String driver = context.getInitParameter(DB_DRIVER_PARAM_NAME);
            String url = context.getInitParameter(DB_URL_PARAM_NAME);
            String user = context.getInitParameter(DB_USER_PARAM_NAME);
            String password = context.getInitParameter(DB_PPP_PARAM_NAME);
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            throw new UnavailableException("Couldn't get db connection");
        }
        return connection;
    }

    public static void closeConnection(Connection connection) throws SQLException {

        if (connection != null) {
            connection.close();
        }
    }

}
