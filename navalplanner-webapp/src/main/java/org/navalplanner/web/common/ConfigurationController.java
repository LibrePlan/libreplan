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
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.api.Bandbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Configuration} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ConfigurationController extends GenericForwardComposer {

    private Window configurationWindow;
    private Bandbox defaultCalendarBandbox;

    private IConfigurationModel configurationModel;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("configurationController", this, true);
        configurationModel.init();
    }

    public BaseCalendar getDefaultCalendar() {
        return configurationModel.getDefaultCalendar();
    }

    public List<BaseCalendar> getCalendars() {
        return configurationModel.getCalendars();
    }

    public void setDefaultCalendar(Listitem item) {
        BaseCalendar calendar = (BaseCalendar) item.getValue();
        configurationModel.setDefaultCalendar(calendar);

        Util.reloadBindings(defaultCalendarBandbox);
        defaultCalendarBandbox.closeDropdown();
    }

    public void save() throws InterruptedException {
        configurationModel.confirm();
        Messagebox.show(_("Changes saved"), _("Information"), Messagebox.OK,
                Messagebox.INFORMATION);
    }

    public void cancel() throws InterruptedException {
        configurationModel.cancel();
        Messagebox.show(_("Changes has been canceled"), _("Information"),
                Messagebox.OK, Messagebox.INFORMATION);
        reloadWindow();
    }

    private void reloadWindow() {
        Util.reloadBindings(configurationWindow);
    }

}