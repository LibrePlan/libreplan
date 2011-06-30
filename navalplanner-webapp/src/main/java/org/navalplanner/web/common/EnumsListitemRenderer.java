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

package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * {@link Enum} types needs to be translated in the webapp module, because of it
 * is not possible to known the user language in the business layer.
 *
 * This class provides a basic renderer that just call to the translation method
 * and could be useful to translate {@link Enum} that are showed into
 * {@link Listbox}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class EnumsListitemRenderer implements ListitemRenderer {

    @Override
    public void render(Listitem item, Object data) {
        item.setValue(data);
        item.appendChild(new Listcell(_(data.toString())));
    }

}
