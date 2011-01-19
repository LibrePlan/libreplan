/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.common.components;

import java.util.Date;

import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Calendar;

/**
 * ZK macro component that uses the {@link Calendar} component adding the
 * possibility to highlight some days in the calendar.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarHighlightedDays extends HtmlMacroComponent {

    private Date value;

    private String ancestorExceptionDays;
    private String ownExceptionDays;
    private String zeroHoursDays;

    private String calendarUuid;

    public void setInternalValue(Date value) {
        this.value = value;

        Util.saveBindings(this);
        highlightDays();
    }

    public Date getInternalValue() {
        return value != null ? new Date(value.getTime()) : null;
    }

    public void setValue(Date value) {
        this.value = value != null ? new Date(value.getTime()) : null;
    }

    public Date getValue() {
        return value != null ? new Date(value.getTime()) : null;
    }

    public void setAncestorExceptionDays(String ancestorExceptionDays) {
        this.ancestorExceptionDays = ancestorExceptionDays;
    }

    public String getAncestorExceptionDays() {
        return ancestorExceptionDays;
    }

    public void setOwnExceptionDays(String ownExceptionDays) {
        this.ownExceptionDays = ownExceptionDays;
    }

    public String getOwnExceptionDays() {
        return ownExceptionDays;
    }

    public void setZeroHoursDays(String zeroHoursDays) {
        this.zeroHoursDays = zeroHoursDays;
    }

    public String getZeroHoursDays() {
        return zeroHoursDays;
    }

    public void highlightDays() {
        String javascript = "highlightDays('" + ancestorExceptionDays
                + "', 'white', 'orange', '" + ownExceptionDays
                + "', 'white', 'red', '" + zeroHoursDays
                + "', 'red', 'white', 'lightgrey', 'white', '"
                + getCalendarUuid() + "');";

        Clients.evalJavaScript(javascript);
    }

    public String getCalendarUuid() {
        if (calendarUuid == null) {
            Calendar calendar = (Calendar) getLastChild();
            calendarUuid = calendar.getUuid();
        }
        return calendarUuid;
    }

}