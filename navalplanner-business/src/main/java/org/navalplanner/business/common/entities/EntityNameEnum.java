/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.common.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.materials.entities.UnitType;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Worker;

/**
 * It represents the entities which use code generation
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public enum EntityNameEnum {
    ORDER(_("Order")), CRITERION(_("Criterion")), LABEL(_("Label")), MACHINE(
            _("Machine")), WORKER(_("Worker")), UNIT_TYPE(_("Unit type")), CALENDAR(
            _("Calendar"));

    private String description;

    private EntityNameEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public IIntegrationEntityDAO<? extends IntegrationEntity> getIntegrationEntityDAO() {
        switch (this) {
        case ORDER:
            return (IIntegrationEntityDAO<Order>) Registry.getOrderDAO();
        case CRITERION:
            return (IIntegrationEntityDAO<CriterionType>) Registry
                    .getCriterionTypeDAO();
        case LABEL:
            return (IIntegrationEntityDAO<LabelType>) Registry
                    .getLabelTypeDAO();
        case MACHINE:
            return (IIntegrationEntityDAO<Machine>) Registry.getMachineDAO();
        case WORKER:
            return (IIntegrationEntityDAO<Worker>) Registry.getWorkerDAO();
        case UNIT_TYPE:
            return (IIntegrationEntityDAO<UnitType>) Registry.getUnitTypeDAO();
        case CALENDAR:
            return (IIntegrationEntityDAO<CalendarData>) Registry
                    .getCalendarDataDAO();
        default:
            throw new RuntimeException("can't handle the code sequence of the "
                    + description);
        }
    }

}
