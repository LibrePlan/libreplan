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

import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.UnitType;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReport;

/**
 * It represents the entities which use code generation
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public enum EntityNameEnum {

    ORDER("Order"), CRITERION("Criterion"), LABEL("Label"), MACHINE("Machine"), WORKER(
            "Worker"), UNIT_TYPE("Unit type"), CALENDAR("Calendar"), WORK_HOURS_TYPE(
			"Type of work hours"), MATERIAL_CATEGORY("Material category"), WORK_REPORT(
            "Work report"), RESOURCE_CALENDAR("Resource calendar");

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
        case RESOURCE_CALENDAR:
            return (IIntegrationEntityDAO<CalendarData>) Registry
                    .getCalendarDataDAO();
        case WORK_HOURS_TYPE:
            return (IIntegrationEntityDAO<TypeOfWorkHours>) Registry
                    .getTypeOfWorkHoursDAO();
        case MATERIAL_CATEGORY:
            return (IIntegrationEntityDAO<MaterialCategory>) Registry
                    .getMaterialCategoryDAO();
        case WORK_REPORT:
            return (IIntegrationEntityDAO<WorkReport>) Registry
                    .getWorkReportDAO();
        default:
            throw new RuntimeException("can't handle the code sequence of the "
                    + description);
        }
    }

    public String getSequenceLiteral() {
        return getDescription() + " sequences";
    }

}
