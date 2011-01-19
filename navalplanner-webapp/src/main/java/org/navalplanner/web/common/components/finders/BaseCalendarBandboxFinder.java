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

package org.navalplanner.web.common.components.finders;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Bandbox finder for {@link BaseCalendar}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
public class BaseCalendarBandboxFinder extends BandboxFinder implements IBandboxFinder {

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    private final String headers[] = { _("Name") };

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getAll() {
        List<BaseCalendar> baseCalendars = baseCalendarDAO.getBaseCalendars();
        forLoadCalendars(baseCalendars);
        return baseCalendars;
    }

    private void forLoadCalendars(List<BaseCalendar> baseCalendars) {
        for (BaseCalendar baseCalendar : baseCalendars) {
            baseCalendar.getName();
        }
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        final BaseCalendar calendar = (BaseCalendar) obj;
        text = text.trim().toLowerCase();
        return calendar.getName().toLowerCase().contains(text);
    }

    @Override
    public String objectToString(Object obj) {
        return ((BaseCalendar) obj).getName();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return baseCalendarRenderer;
    }

    private final ListitemRenderer baseCalendarRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            BaseCalendar baseCalendar = (BaseCalendar) data;
            item.setValue(baseCalendar);

            final Listcell baseCalendarName = new Listcell();
            baseCalendarName.setLabel(baseCalendar.getName());
            baseCalendarName.setParent(item);
        }
    };

}
