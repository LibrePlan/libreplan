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

import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import java.util.List;

/**
 * Controller for "Personal timesheets" area in the user dashboard window.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enteprise.com>
 */
@SuppressWarnings("serial")
public class PersonalTimesheetsAreaController extends GenericForwardComposer {

    private IPersonalTimesheetsAreaModel personalTimesheetsAreaModel;

    private IPersonalTimesheetController personalTimesheetController;

    private RowRenderer personalTimesheetsRenderer = new RowRenderer() {

        @Override
        public void render(Row row, Object data, int i) throws Exception {
            final PersonalTimesheetDTO personalTimesheet = (PersonalTimesheetDTO) data;
            row.setValue(personalTimesheet);

            Util.appendLabel(
                    row, personalTimesheet.toString(personalTimesheetsAreaModel.getPersonalTimesheetsPeriodicity()));

            Util.appendLabel(row, personalTimesheet.getResourceCapacity().toFormattedString());
            Util.appendLabel(row, personalTimesheet.getTotalHours().toFormattedString());
            Util.appendLabel(row, Integer.toString(personalTimesheet.getTasksNumber()));

            Util.appendOperationsAndOnClickEvent(
                    row, event -> personalTimesheetController.goToCreateOrEditForm(personalTimesheet.getDate()), null);
        }

    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        injectObjects();
    }

    private void injectObjects() {
        if ( personalTimesheetsAreaModel == null ) {

            personalTimesheetsAreaModel =
                    (IPersonalTimesheetsAreaModel) SpringUtil.getBean("personalTimesheetsAreaModel");
        }

        if ( personalTimesheetController == null ) {

            personalTimesheetController =
                    (IPersonalTimesheetController) SpringUtil.getBean("personalTimesheetController");
        }
    }

    public List<PersonalTimesheetDTO> getPersonalTimesheets() {
        return personalTimesheetsAreaModel.getPersonalTimesheets();
    }

    public RowRenderer getPersonalTimesheetsRenderer() {
        return personalTimesheetsRenderer;
    }

}
