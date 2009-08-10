package org.navalplanner.web.common;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;

public class ConcurrentModificationController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(ConcurrentModificationController.class);
    private String backURL;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        backURL = getBackURL();
    }

    public static void showException(
            OptimisticLockingFailureException exception, String backURL) {
        LOG
                .error(
                        "an OptimistLockingFailureException caused a disruption to an user",
                        exception);
        Executions.sendRedirect("/common/concurrent_modification.zul?back="
                + backURL);
    }

    private static String getBackURL() {
        return getRequest().getParameter("back");
    }

    private static HttpServletRequest getRequest() {
        return (HttpServletRequest) Executions.getCurrent().getNativeRequest();
    }

    public void onClick$continue() {
        Executions.sendRedirect(backURL);
    }

}
