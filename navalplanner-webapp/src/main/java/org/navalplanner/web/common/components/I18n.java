package org.navalplanner.web.common.components;

import org.navalplanner.web.I18nHelper;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Label;

/**
 * ZK macro component for translating texts created dinamically, that means,
 * those which need one or more parameters
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@SuppressWarnings("serial")
public class I18n extends HtmlMacroComponent {

    public String value;

    public String arg0;

    public String arg1;

    public String arg2;

    public String arg3;

    public I18n() {

    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getArg0() {
        return arg0;
    }

    public void setArg0(String arg0) {
        this.arg0 = arg0;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public String getArg3() {
        return arg3;
    }

    public void setArg3(String arg3) {
        this.arg3 = arg3;
    }

    public String getI18n() {
        if (arg0 != null && arg1 != null && arg2 != null && arg3 != null) {
            return I18nHelper._(value, arg0, arg1, arg2, arg3);
        }
        if (arg0 != null && arg1 != null && arg2 != null) {
            return I18nHelper._(value, arg0, arg1, arg2);
        }
        if (arg0 != null && arg1 != null) {
            return I18nHelper._(value, arg0, arg1);
        }
        if (arg0 != null) {
            return I18nHelper._(value, arg0);
        }

        return I18nHelper._(value);
    }

    public void forceLoad() {
        Label label = (Label) getFellow("i18nlabel");
        label.setValue(getI18n());
    }
}
