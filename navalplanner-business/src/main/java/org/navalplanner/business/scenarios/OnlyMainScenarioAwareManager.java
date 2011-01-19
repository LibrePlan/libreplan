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
package org.navalplanner.business.scenarios;

import org.navalplanner.business.scenarios.bootstrap.IScenariosBootstrap;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class OnlyMainScenarioAwareManager implements IScenarioManager {

    @Autowired
    private IScenariosBootstrap scenariosBootstrap;

    @Override
    public Scenario getCurrent() {
        return scenariosBootstrap.getMain();
    }

}
