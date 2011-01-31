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

import java.util.List;

import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.users.bootstrap.MandatoryUser;

/**
 * Contract for {@link TemplateModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface ITemplateModel {

    List<Scenario> getScenarios();

    String getCompanyLogoURL();

    Scenario getScenarioByName(String name);

    public interface IOnFinished {
        public void onWithoutErrorFinish();

        public void errorHappened(Exception exceptionHappened);
    }

    void setScenario(String loginName, Scenario scenario,
            IOnFinished onFinish);

    boolean isScenariosVisible();

    boolean isChangedDefaultPassword(MandatoryUser user);

    String getIdUser(String login);

}
