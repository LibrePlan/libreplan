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

import java.util.List;

import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Bandbox finder for {@link Scenario}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
public class ScenarioBandboxFinder extends BandboxFinder implements IBandboxFinder {

    @Autowired
    private IScenarioDAO scenarioDAO;

    private final String headers[] = { _("Name") };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Scenario> getAll() {
        List<Scenario> scenarios = scenarioDAO.getAll();
        forceLoad(scenarios);
        return scenarios;
    }

    private void forceLoad(List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            scenario.getName();
        }
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        final Scenario scenario = (Scenario) obj;
        text = text.trim().toLowerCase();
        return scenario.getName().toLowerCase().contains(text);
    }

    @Override
    public String objectToString(Object obj) {
        return ((Scenario) obj).getName();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return scenarioRenderer;
    }

    private final ListitemRenderer scenarioRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) {
            Scenario scenario = (Scenario) data;
            item.setValue(scenario);

            final Listcell baseCalendarName = new Listcell();
            baseCalendarName.setLabel(scenario.getName());
            baseCalendarName.setParent(item);
        }
    };

}
