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

package org.navalplanner.business.common.entities;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.BaseEntity;

/**
 * Application configuration variables.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class Configuration extends BaseEntity {

    public static Configuration create() {
        return create(new Configuration());
    }

    private BaseCalendar defaultCalendar;

    private String companyCode;

    private Boolean generateCodeForCriterion = true;

    private Boolean generateCodeForLabel = true;

    private Boolean generateCodeForWorkReport = true;

    private Boolean generateCodeForResources = true;

    private Boolean generateCodeForTypesOfWorkHours = true;

    private Boolean generateCodeForMaterialCategories = true;

    private Boolean generateCodeForUnitTypes = true;

    private Boolean generateCodeForBaseCalendars = true;

    private Boolean expandCompanyPlanningViewCharts = false;

    private Boolean expandOrderPlanningViewCharts = true;

    private Boolean expandResourceLoadViewCharts = true;

    private Boolean monteCarloMethodTabVisible = false;

    private Boolean generateCodeForWorkReportType = true;

    private Boolean generateCodeForCalendarExceptionType = true;

    private Boolean generateCodeForCostCategory = true;

    private Boolean changedDefaultAdminPassword = false;

    private ProgressType progressType = ProgressType.SPREAD_PROGRESS;

    private String companyLogoURL = "";

    private Boolean scenariosVisible = false;

    public void setDefaultCalendar(BaseCalendar defaultCalendar) {
        this.defaultCalendar = defaultCalendar;
    }

    @NotNull(message = "default calendar not specified")
    public BaseCalendar getDefaultCalendar() {
        return defaultCalendar;
    }

    public void setCompanyCode(String companyCode) {
        if (companyCode != null) {
            companyCode = companyCode.trim();
        }
        this.companyCode = companyCode;
    }

    @NotEmpty(message = "company code not specified")
    public String getCompanyCode() {
        return companyCode;
    }

    @AssertTrue(message = "company code must not contain white spaces")
    public boolean checkConstraintCompanyCodeWithoutWhiteSpaces() {
        if ((companyCode == null) || (companyCode.isEmpty())) {
            return false;
        }

        return !companyCode.contains(" ");
    }

    public void setGenerateCodeForCriterion(Boolean generateCodeForCriterion) {
        this.generateCodeForCriterion = generateCodeForCriterion;
    }

    public Boolean getGenerateCodeForCriterion() {
        return generateCodeForCriterion;
    }

    public void setGenerateCodeForLabel(Boolean generateCodeForLabel) {
        this.generateCodeForLabel = generateCodeForLabel;
    }

    public Boolean getGenerateCodeForLabel() {
        return generateCodeForLabel;
    }

    public void setGenerateCodeForWorkReport(Boolean generateCodeForWorkReport) {
        this.generateCodeForWorkReport = generateCodeForWorkReport;
    }

    public Boolean getGenerateCodeForWorkReport() {
        return generateCodeForWorkReport;
    }

    public void setGenerateCodeForResources(Boolean generateCodeForResources) {
        this.generateCodeForResources = generateCodeForResources;
    }

    public Boolean getGenerateCodeForResources() {
        return generateCodeForResources;
    }

    public void setGenerateCodeForTypesOfWorkHours(
            Boolean generateCodeForTypesOfWorkHours) {
        this.generateCodeForTypesOfWorkHours = generateCodeForTypesOfWorkHours;
    }

    public Boolean getGenerateCodeForTypesOfWorkHours() {
        return generateCodeForTypesOfWorkHours;
    }

    public void setGenerateCodeForMaterialCategories(
            Boolean generateCodeForMaterialCategories) {
        this.generateCodeForMaterialCategories = generateCodeForMaterialCategories;
    }

    public Boolean getGenerateCodeForMaterialCategories() {
        return generateCodeForMaterialCategories;
    }

    public void setGenerateCodeForUnitTypes(Boolean generateCodeForUnitTypes) {
        this.generateCodeForUnitTypes = generateCodeForUnitTypes;
    }

    public Boolean getGenerateCodeForUnitTypes() {
        return generateCodeForUnitTypes;
    }

    public void setExpandCompanyPlanningViewCharts(
            Boolean expandCompanyPlanningViewCharts) {
        this.expandCompanyPlanningViewCharts = expandCompanyPlanningViewCharts;
    }

    public Boolean isExpandCompanyPlanningViewCharts() {
        return expandCompanyPlanningViewCharts;
    }

    public void setExpandOrderPlanningViewCharts(
            Boolean expandOrderPlanningViewCharts) {
        this.expandOrderPlanningViewCharts = expandOrderPlanningViewCharts;
    }

    public Boolean isExpandOrderPlanningViewCharts() {
        return expandOrderPlanningViewCharts;
    }

    public void setExpandResourceLoadViewCharts(
            Boolean expandResourceLoadViewCharts) {
        this.expandResourceLoadViewCharts = expandResourceLoadViewCharts;
    }

    public Boolean isExpandResourceLoadViewCharts() {
        return expandResourceLoadViewCharts;
    }

    public Boolean isMonteCarloMethodTabVisible() {
        return monteCarloMethodTabVisible;
    }

    public void setMonteCarloMethodTabVisible(Boolean monteCarloMethodTabVisible) {
        this.monteCarloMethodTabVisible = monteCarloMethodTabVisible;
    }

    public Boolean isScenariosVisible() {
        return scenariosVisible;
    }

    public void setScenariosVisible(Boolean scenariosVisible) {
        this.scenariosVisible = scenariosVisible;
    }

    public void setGenerateCodeForBaseCalendars(
            Boolean generateCodeForBaseCalendars) {
        this.generateCodeForBaseCalendars = generateCodeForBaseCalendars;
    }

    public Boolean getGenerateCodeForBaseCalendars() {
        return generateCodeForBaseCalendars;
    }

    public void setGenerateCodeForWorkReportType(
            Boolean generateCodeForWorkReportType) {
        this.generateCodeForWorkReportType = generateCodeForWorkReportType;
    }

    public Boolean getGenerateCodeForWorkReportType() {
        return generateCodeForWorkReportType;
    }

    public void setGenerateCodeForCalendarExceptionType(
            Boolean generateCodeForCalendarExceptionType) {
        this.generateCodeForCalendarExceptionType = generateCodeForCalendarExceptionType;
    }

    public Boolean getGenerateCodeForCalendarExceptionType() {
        return this.generateCodeForCalendarExceptionType;
    }

    public void setGenerateCodeForCostCategory(
            Boolean generateCodeForCostCategory) {
        this.generateCodeForCostCategory = generateCodeForCostCategory;
    }

    public Boolean getGenerateCodeForCostCategory() {
        return generateCodeForCostCategory;
    }

    public void setProgressType(ProgressType progressType) {
        this.progressType = progressType;
    }

    public ProgressType getProgressType() {
        return progressType;
    }

    public void setCompanyLogoURL(String companyLogoURL) {
        if (companyLogoURL != null) {
            companyLogoURL = companyLogoURL.trim();
        }
        this.companyLogoURL = companyLogoURL;
    }

    public String getCompanyLogoURL() {
        return companyLogoURL;
    }

    public void setChangedDefaultAdminPassword(
            Boolean changedDefaultAdminPassword) {
        this.changedDefaultAdminPassword = changedDefaultAdminPassword;
    }

    public Boolean getChangedDefaultAdminPassword() {
        return changedDefaultAdminPassword == null ? false
                : changedDefaultAdminPassword;
    }

}