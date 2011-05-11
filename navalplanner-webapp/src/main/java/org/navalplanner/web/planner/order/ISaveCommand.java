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

package org.navalplanner.web.planner.order;

import org.navalplanner.business.planner.entities.TaskElement;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.extensions.ICommand;

/**
 * Contract for {@link SaveCommand} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ISaveCommand extends ICommand<TaskElement> {

    public interface IAfterSaveListener {
        void onAfterSave();
    }

    public void setState(PlanningState planningState);

    public void setConfiguration(PlannerConfiguration<TaskElement> configuration);

    public void addListener(IAfterSaveListener listener);

    public void removeListener(IAfterSaveListener listener);

    public String getImage();


}
