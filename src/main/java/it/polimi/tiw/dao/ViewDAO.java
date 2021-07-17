
package it.polimi.tiw.dao;

import java.sql.Connection;

import it.polimi.tiw.utils.QueryExecutor;
import it.polimi.tiw.utils.ViewEntity;

public class ViewDAO {

    private final QueryExecutor queryExecutor;

    public ViewDAO(Connection connection) {

        queryExecutor = new QueryExecutor(connection);
    }

    public void insertView(ViewEntity viewEntity) {

        queryExecutor.insert("user_article", viewEntity);
    }

}
