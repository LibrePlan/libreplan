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

package org.navalplanner.ws.common.impl;


/**
 * Configuration for methods of {@link OrderElementConverter}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ConfigurationOrderElementConverter {

    public static ConfigurationOrderElementConverter create(boolean labels,
            boolean materialAssignments, boolean advanceMeasurements,
            boolean hoursGroups, boolean criterionRequirements) {
        return new ConfigurationOrderElementConverter(labels,
                materialAssignments, advanceMeasurements, hoursGroups,
                criterionRequirements);
    }

    public static ConfigurationOrderElementConverter all() {
        return new ConfigurationOrderElementConverter(true, true, true, true,
                true);
    }

    public static ConfigurationOrderElementConverter none() {
        return new ConfigurationOrderElementConverter(false, false, false,
                false, false);
    }

    public static ConfigurationOrderElementConverter noAdvanceMeasurements() {
        return new ConfigurationOrderElementConverter(true, true, false, true,
                true);
    }

    private boolean labels;
    private boolean materialAssignments;
    private boolean advanceMeasurements;
    private boolean hoursGroups;
    private boolean criterionRequirements;

    private ConfigurationOrderElementConverter(boolean labels,
            boolean materialAssignments, boolean advanceMeasurements,
            boolean hoursGroups, boolean criterionRequirements) {
        this.labels = labels;
        this.materialAssignments = materialAssignments;
        this.advanceMeasurements = advanceMeasurements;
        this.hoursGroups = hoursGroups;
        this.criterionRequirements = criterionRequirements;
    }

    public boolean isLabels() {
        return labels;
    }

    public boolean isMaterialAssignments() {
        return materialAssignments;
    }

    public boolean isAdvanceMeasurements() {
        return advanceMeasurements;
    }

    public boolean isHoursGroups() {
        return hoursGroups;
    }

    public boolean isCriterionRequirements() {
        return criterionRequirements;
    }

}
