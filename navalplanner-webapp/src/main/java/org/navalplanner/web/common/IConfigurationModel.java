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

package org.navalplanner.web.common;

import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.entities.EntityNameEnum;
import org.navalplanner.business.common.entities.EntitySequence;
import org.navalplanner.business.common.entities.ProgressType;

/**
 * Contract for {@link ConfigurationModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IConfigurationModel {

    /*
     * Non conversational steps
     */
    List<BaseCalendar> getCalendars();

    /*
     * Initial conversation steps
     */
    void init();

    /*
     * Intermediate conversation steps
     */
    BaseCalendar getDefaultCalendar();
    void setDefaultCalendar(BaseCalendar calendar);

    String getCompanyCode();
    void setCompanyCode(String companyCode);

    Boolean getGenerateCodeForWorkReportType();

    void setGenerateCodeForWorkReportType(Boolean generateCodeForWorkReportType);

    Boolean getGenerateCodeForCalendarExceptionType();

    void setGenerateCodeForCostCategory(Boolean generateCodeForCostCategory);

    Boolean getGenerateCodeForCostCategory();

    void setGenerateCodeForCalendarExceptionType(
            Boolean generateCodeForCalendarExceptionType);

    Boolean getGenerateCodeForCriterion();
    void setGenerateCodeForCriterion(Boolean generateCodeForCriterion);

    Boolean getGenerateCodeForLabel();
    void setGenerateCodeForLabel(Boolean generateCodeForLabel);

    Boolean getGenerateCodeForWorkReport();
    void setGenerateCodeForWorkReport(Boolean generateCodeForWorkReport);

    Boolean getGenerateCodeForResources();
    void setGenerateCodeForResources(Boolean generateCodeForResources);

    Boolean getGenerateCodeForTypesOfWorkHours();
    void setGenerateCodeForTypesOfWorkHours(
            Boolean generateCodeForTypesOfWorkHours);

    Boolean getGenerateCodeForMaterialCategories();
    void setGenerateCodeForMaterialCategories(
            Boolean generateCodeForMaterialCategories);

    List<EntitySequence> getEntitySequences(EntityNameEnum entityName);
    void addEntitySequence(EntityNameEnum entityName);
    void removeEntitySequence(EntitySequence entitySequence)
            throws IllegalArgumentException;

    void setExpandCompanyPlanningViewCharts(
            Boolean expandCompanyPlanningViewCharts);

    Boolean isExpandCompanyPlanningViewCharts();

    void setExpandOrderPlanningViewCharts(Boolean expandOrderPlanningViewCharts);

    Boolean isExpandOrderPlanningViewCharts();

    void setExpandResourceLoadViewCharts(Boolean expandResourceLoadViewCharts);

    Boolean isExpandResourceLoadViewCharts();

    Boolean isMonteCarloMethodTabVisible();

    void setMonteCarloMethodTabVisible(Boolean visible);

    /*
     * Final conversation steps
     */
    void confirm();
    void cancel();

    Boolean getGenerateCodeForUnitTypes();

    void setGenerateCodeForUnitTypes(Boolean generateCodeForUnitTypes);

    void setGenerateCodeForBaseCalendars(Boolean generateCodeForBaseCalendars);

    Boolean getGenerateCodeForBaseCalendars();

    boolean checkFrefixFormat(EntitySequence sequence);

    List<ProgressType> getProgresTypes();

    void setProgressType(ProgressType progressType);

    ProgressType getProgressType();

}
