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

package org.libreplan.web.users.dashboard;

import static org.libreplan.web.I18nHelper._;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.libreplan.business.common.Registry;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller for user dashboard window.<br />
 *
 * At this moment it's only used to show a message to user after saving a
 * personal timesheet.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@SuppressWarnings("serial")
public class UserDashboardController extends GenericForwardComposer {

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        messagesForUser = new MessagesForUser(messagesContainer);

        String timesheetSave = Executions.getCurrent().getParameter(
                "timesheet_saved");
        if (!StringUtils.isBlank(timesheetSave)) {
            String personalTimesheet = PersonalTimesheetDTO.toString(Registry
                    .getConfigurationDAO()
                    .getConfigurationWithReadOnlyTransaction()
                    .getPersonalTimesheetsPeriodicity(), new LocalDate(
                    timesheetSave));
            messagesForUser.showMessage(Level.INFO,
                    _("Personal timesheet \"{0}\" saved", personalTimesheet));
        }

        String expenseSheetSaved = Executions.getCurrent().getParameter(
                "expense_sheet_saved");
        if (!StringUtils.isBlank(expenseSheetSaved)) {
            messagesForUser.showMessage(Level.INFO,
                    _("Expense sheet \"{0}\" saved", expenseSheetSaved));
        }
    }

}
