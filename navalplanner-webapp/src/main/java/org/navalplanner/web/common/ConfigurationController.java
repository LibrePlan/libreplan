/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.entities.Configuration;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.api.Listitem;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Configuration} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ConfigurationController extends GenericForwardComposer {

    private Window configurationWindow;
    private BandboxSearch defaultCalendarBandboxSearch;

    private IConfigurationModel configurationModel;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("configurationController", this, true);
        configurationModel.init();

        defaultCalendarBandboxSearch.setListboxEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        Listitem selectedItem = (Listitem) ((SelectEvent) event)
                                .getSelectedItems()
                                .iterator().next();
                        setDefaultCalendar((BaseCalendar) selectedItem
                                .getValue());
                    }
                });
    }

    public List<BaseCalendar> getCalendars() {
        return configurationModel.getCalendars();
    }

    public BaseCalendar getDefaultCalendar() {
        return configurationModel.getDefaultCalendar();
    }

    public void setDefaultCalendar(BaseCalendar calendar) {
        configurationModel.setDefaultCalendar(calendar);
    }

    public void save() throws InterruptedException {
        configurationModel.confirm();
        Messagebox.show(_("Changes saved"), _("Information"), Messagebox.OK,
                Messagebox.INFORMATION);
    }

    public void cancel() throws InterruptedException {
        configurationModel.cancel();
        Messagebox.show(_("Changes have been canceled"), _("Information"),
                Messagebox.OK, Messagebox.INFORMATION);
        reloadWindow();
    }

    private void reloadWindow() {
        Util.reloadBindings(configurationWindow);
    }

}