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

import org.navalplanner.web.I18nHelper;
import org.zkoss.zul.Label;

/**
 * This component is aimed to localize strings that come from business objects. For instance:
 *
 * <rows>
 *    <row self="@{each='criterionType'}" value="@{criterionType}">
 *       <l10n value="@{criterionType.name}"/>
 *    </row>
 * </rows>
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@SuppressWarnings("serial")
public class L10n extends Label {

    public L10n() {

    }

    @Override
    public void setValue(String value) {
        super.setValue(I18nHelper._(value));
    }

}
