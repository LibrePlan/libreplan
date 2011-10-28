/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.web.common.concurrentdetection;

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
                .info(
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
