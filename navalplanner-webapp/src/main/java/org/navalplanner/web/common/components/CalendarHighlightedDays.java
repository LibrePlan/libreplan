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
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    public Date getValue() {
        return value;
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
                + "', 'black', 'orange', '" + ownExceptionDays
                + "', 'black', 'red', '" + zeroHoursDays
                + "', 'white', 'lightgrey', 'lightgrey', 'white', '"
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