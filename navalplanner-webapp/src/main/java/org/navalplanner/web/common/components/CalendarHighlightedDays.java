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

    private String days;

    public void setInternalValue(Date value) {
        this.value = value;

        Util.saveBindings(this);
        highlightDays();
    }

    public Date getInternalValue() {
        highlightDays();
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    public Date getValue() {
        return value;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getDays() {
        return days;
    }

    private void highlightDays() {
        Clients
                .evalJavaScript("highlightDays('" + days
                        + "', 'black', 'red');");
    }

}
