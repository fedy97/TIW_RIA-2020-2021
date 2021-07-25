
package it.polimi.tiw.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.tiw.bean.ShippingPolicyBean;
import it.polimi.tiw.bean.UserBean;
import it.polimi.tiw.dao.ShipmentPolicyDAO;
import it.polimi.tiw.utils.GenericServlet;

@WebServlet("/shipping")
public class ShippingPolicyController extends GenericServlet {

    private static final Logger log                 = LoggerFactory
            .getLogger(ShippingPolicyController.class.getSimpleName());

    private static final String QTY_ATTRIBUTE       = "qty";
    private static final String SELLER_ID_ATTRIBUTE = "seller_id";

    private static final long   serialVersionUID    = 1L;

    public ShippingPolicyController() {

        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Optional<UserBean> user = getUserData(req);
        if (!user.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String qty;
        String sellerId;
        try {
            qty = escapeSQL(req.getParameter(QTY_ATTRIBUTE));
            sellerId = escapeSQL(req.getParameter(SELLER_ID_ATTRIBUTE));
        } catch (Exception e) {
            log.error("Something went wrong when extracting parameters. Cause is {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed or missing parameters");
            return;
        }

        try {
            Optional<ShippingPolicyBean> shippingPolicyBean = new ShipmentPolicyDAO(connection)
                    .findPolicyByQty(sellerId, Integer.parseInt(qty));
            if (shippingPolicyBean.isPresent()) writeObject(shippingPolicyBean.get(), resp, HttpServletResponse.SC_OK);
            else
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            log.error("Something went wrong when extracting the price of article with id {}. Cause is {}", qty,
                    ExceptionUtils.getStackTrace(e));
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something went wrong when searching the specified articles");
        }
    }
}
