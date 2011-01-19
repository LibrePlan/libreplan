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

package org.navalplanner.business.i18n;

import java.util.Locale;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class I18nHelper {

    private I18nHelper() {

    }

    public static I18n getI18n() {
        return I18nFactory.getI18n(I18nHelper.class, Locale.getDefault(),
                org.xnap.commons.i18n.I18nFactory.FALLBACK);
    }

    public static String _(String text) {
        return getI18n().tr(text);
    }

    public static String _(String text, Object o1) {
        return getI18n().tr(text, o1);
    }

    public static String _(String text, Object o1, Object o2) {
        return getI18n().tr(text, o1, o2);
    }

    public static String _(String text, Object o1, Object o2, Object o3) {
        return getI18n().tr(text, o1, o2, o3);
    }

    public static String _(String text, Object o1, Object o2, Object o3,
            Object o4) {
        return getI18n().tr(text, o1, o2, o3, o4);
    }

    public static String _(String text, Object[] objects) {
        return getI18n().tr(text, objects);
    }
}
