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

package org.libreplan.business.i18n;

/**
 * This class provides a function to mark strings to be translated.
 * Real translation have to be done in webapp module depending on user language and not done here depending on server language.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class I18nHelper {

    private I18nHelper() {
    }

    /**
     * Internationalization marker method for translatable strings.
     * This method serves as a marker for strings that need translation.
     * Real translation is handled in the webapp module based on user language.
     *
     * @param text the text to be marked for translation
     * @return the input text (translation happens at runtime in web layer)
     */
    public static String _t(String text) {
        return text;
    }

}
