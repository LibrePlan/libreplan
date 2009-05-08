package org.navalplanner.web.error;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;

public class PageForErrorOnEvent extends GenericForwardComposer {

	private static final Log LOG = LogFactory.getLog(PageForErrorOnEvent.class);

	private Component modalWindow;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		logError();
		modalWindow = comp;

	}

	private void logError() {
		Throwable exception = (Throwable) Executions.getCurrent().getAttribute(
				"javax.servlet.error.exception");
		String errorMessage = (String) Executions.getCurrent().getAttribute(
				"javax.servlet.error.message");
		LOG.error(errorMessage, exception);
	}

	public void onClick$continueWorking() {
		modalWindow.detach();
	}

	public void onClick$reload() {
		Executions.sendRedirect(null);
	}

	public void onClick$quitSession() {
		HttpServletRequest nativeRequest = (HttpServletRequest) Executions
				.getCurrent().getNativeRequest();
		nativeRequest.getSession().invalidate();
		Executions.sendRedirect("/");
	}

}
