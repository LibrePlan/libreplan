/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 ComtecSF, S.L.
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

package org.libreplan.business.settings.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.util.Locale;

/**
 * Available languages.
 *
 * @author Cristina Alavarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public enum Language {

    BROWSER_LANGUAGE(_("Use browser language configuration"), null),
    GALICIAN_LANGUAGE("Galego", new Locale("gl")),
    SPANISH_LANGUAGE("Español", new Locale("es")),
    ENGLISH_LANGUAGE("English", Locale.ENGLISH),
    RUSSIAN_LANGUAGE("Pусский", new Locale("ru")),
    PORTUGUESE_LANGUAGE("Português", new Locale("pt")),
    ITALIAN_LANGUAGE("Italiano", new Locale("it")),
    FRENCH_LANGUAGE("Français", new Locale("fr")),
    DUTCH_LANGUAGE("Nederlands", new Locale("nl"));

    private final String displayName;

    private Locale locale;

    private Language(String displayName, Locale locale) {
        this.displayName = displayName;
        this.locale = locale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Locale getLocale() {
        return locale;
    }

}
