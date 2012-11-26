/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.web.common;

import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.IBackGroundOperation;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdate;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdatesEmitter;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.util.Clients;

/**
 * Utilities class to manage confirm close dialogue.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class ConfirmCloseUtil {

    private static final int WARNING_ON_EXIT_MS = 30000; // 30 seconds

    public static void resetConfirmClose() {
        Clients.confirmClose(null);
    }

    public static void setConfirmClose(Desktop desktop, final String message) {
        LongOperationFeedback
                .progressive(
                        desktop,
                        new IBackGroundOperation<LongOperationFeedback.IDesktopUpdate>() {

                            @Override
                            public void doOperation(
                                    IDesktopUpdatesEmitter<IDesktopUpdate> desktopUpdateEmitter) {
                                try {
                                    Thread.sleep(WARNING_ON_EXIT_MS);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                desktopUpdateEmitter
                                        .doUpdate(new IDesktopUpdate() {

                                            @Override
                                            public void doUpdate() {
                                                resetConfirmClose();
                                                Clients.confirmClose(message);
                                            }
                                        });
                            }
                        });
    }

}
