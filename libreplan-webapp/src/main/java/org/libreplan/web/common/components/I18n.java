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

package org.libreplan.web.common.components;

import org.libreplan.web.I18nHelper;
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

    private String value;

    private String arg0;

    private String arg1;

    private String arg2;

    private String arg3;

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
