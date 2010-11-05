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

import static org.navalplanner.web.I18nHelper._;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.daos.IOrderSequenceDAO;
import org.navalplanner.business.common.entities.Configuration;
import org.navalplanner.business.common.entities.OrderSequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.i18n.I18nHelper;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/common/configuration.zul")
public class ConfigurationModel implements IConfigurationModel {

    /**
     * Conversation state
     */
    private Configuration configuration;

    private List<OrderSequence> orderSequences;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private IOrderSequenceDAO orderSequenceDAO;

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getCalendars() {
        return baseCalendarDAO.getBaseCalendars();
    }

    @Override
    public BaseCalendar getDefaultCalendar() {
        if (configuration == null) {
            return null;
        }
        return configuration.getDefaultCalendar();
    }

    @Override
    @Transactional(readOnly = true)
    public void init() {
        this.configuration = getCurrentConfiguration();
        this.orderSequences = orderSequenceDAO.getAll();
    }

    private Configuration getCurrentConfiguration() {
        Configuration configuration = configurationDAO.getConfiguration();
        if (configuration == null) {
            configuration = Configuration.create();
        }
        forceLoad(configuration);
        return configuration;
    }

    private void forceLoad(Configuration configuration) {
        forceLoad(configuration.getDefaultCalendar());
    }

    private void forceLoad(BaseCalendar calendar) {
        if (calendar != null) {
            calendar.getName();
        }
    }

    @Override
    public void setDefaultCalendar(BaseCalendar calendar) {
        if (configuration != null) {
            configuration.setDefaultCalendar(calendar);
        }
    }

    @Override
    @Transactional
    public void confirm() {
        if (orderSequences.isEmpty()) {
            throw new ValidationException(
                    _("At least one order sequence is needed"));
        }

        if (!checkConstraintJustOneOrderSequenceActive()) {
            throw new ValidationException(
                    _("Just one order sequence must be active"));
        }

        if (!checkConstraintPrefixNotRepeated()) {
            throw new ValidationException(
                    _("Order sequence prefixes can not be repeated"));
        }

        try {
            configurationDAO.save(configuration);
            storeAndRemoveOrderSequences();
        } catch (HibernateOptimisticLockingFailureException e) {
            throw new ConcurrentModificationException(
                    _("Some order was created during the configuration process, it is impossible to update order sequence table. Please, try again later"));
        }

    }

    private boolean checkConstraintPrefixNotRepeated() {
        Set<String> prefixes = new HashSet<String>();
        for (OrderSequence orderSequence : orderSequences) {
            String prefix = orderSequence.getPrefix();
            if (prefixes.contains(prefix)) {
                return false;
            }
            prefixes.add(prefix);
        }
        return true;
    }

    private void storeAndRemoveOrderSequences() {
        for (OrderSequence orderSequence : orderSequences) {
            orderSequenceDAO.save(orderSequence);
        }

        List<OrderSequence> toRemove = orderSequenceDAO
                .findOrderSquencesNotIn(orderSequences);
        for (OrderSequence orderSequence : toRemove) {
            try {
                orderSequenceDAO.remove(orderSequence);
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean checkConstraintJustOneOrderSequenceActive() {
        boolean someoneActive = false;
        for (OrderSequence orderSequence : orderSequences) {
            if (orderSequence.isActive()) {
                if (someoneActive) {
                    return false;
                }
                someoneActive = true;
            }
        }
        return someoneActive;
    }

    @Override
    @Transactional(readOnly = true)
    public void cancel() {
        init();
    }

    @Override
    public String getCompanyCode() {
        if (configuration == null) {
            return null;
        }
        return configuration.getCompanyCode();
    }

    @Override
    public void setCompanyCode(String companyCode) {
        if (configuration != null) {
            configuration.setCompanyCode(companyCode);
        }
    }

    @Override
    public Boolean getGenerateCodeForCriterion() {
        if (configuration == null) {
            return null;
        }
        return configuration.getGenerateCodeForCriterion();
    }

    @Override
    public void setGenerateCodeForCriterion(Boolean generateCodeForCriterion) {
        if (configuration != null) {
            configuration.setGenerateCodeForCriterion(generateCodeForCriterion);
        }
    }

    @Override
    public Boolean getGenerateCodeForLabel() {
        if (configuration == null) {
            return null;
        }
        return configuration.getGenerateCodeForLabel();
    }

    @Override
    public void setGenerateCodeForLabel(Boolean generateCodeForLabel) {
        if (configuration != null) {
            configuration.setGenerateCodeForLabel(generateCodeForLabel);
        }
    }

    @Override
    public Boolean getGenerateCodeForWorkReport() {
        if (configuration == null) {
            return null;
        }
        return configuration.getGenerateCodeForWorkReport();
    }

    @Override
    public void setGenerateCodeForWorkReport(Boolean generateCodeForWorkReport) {
        if (configuration != null) {
            configuration.setGenerateCodeForWorkReport(generateCodeForWorkReport);
        }
    }

    @Override
    public Boolean getGenerateCodeForResources() {
        if (configuration == null) {
            return null;
        }
        return configuration.getGenerateCodeForResources();
    }

    @Override
    public void setGenerateCodeForResources(Boolean generateCodeForResources) {
        if (configuration != null) {
            configuration.setGenerateCodeForResources(generateCodeForResources);
        }
    }

    @Override
    public Boolean getGenerateCodeForTypesOfWorkHours() {
        if (configuration == null) {
            return null;
        }
        return configuration.getGenerateCodeForTypesOfWorkHours();
    }

    @Override
    public void setGenerateCodeForTypesOfWorkHours(
            Boolean generateCodeForTypesOfWorkHours) {
        if (configuration != null) {
            configuration.setGenerateCodeForTypesOfWorkHours(
                    generateCodeForTypesOfWorkHours);
        }
    }

    @Override
    public Boolean getGenerateCodeForMaterialCategories() {
        if (configuration == null) {
            return null;
        }
        return configuration.getGenerateCodeForMaterialCategories();
    }

    @Override
    public void setGenerateCodeForMaterialCategories(
            Boolean generateCodeForMaterialCategories) {
        if (configuration != null) {
            configuration.setGenerateCodeForMaterialCategories(
                    generateCodeForMaterialCategories);
        }
    }

    @Override
    public Boolean getGenerateCodeForUnitTypes() {
        if (configuration == null) {
            return null;
        }
        return configuration.getGenerateCodeForUnitTypes();
    }

    @Override
    public void setGenerateCodeForUnitTypes(Boolean generateCodeForUnitTypes) {
            if (configuration != null) {
                configuration.setGenerateCodeForUnitTypes(
                        generateCodeForUnitTypes);
            }
    }

    @Override
    public List<OrderSequence> getOrderSequences() {
        return Collections.unmodifiableList(orderSequences);
    }

    @Override
    public void addOrderSequence() {
        orderSequences.add(OrderSequence.create(""));
    }

    @Override
    public void removeOrderSequence(OrderSequence orderSequence)
            throws IllegalArgumentException {
        if (orderSequence.getLastValue() > 0) {
            throw new IllegalArgumentException(
                    I18nHelper
                            ._("You can not remove this order sequence, it is already in use"));
        }
        orderSequences.remove(orderSequence);
    }

    @Override
    public void setExpandCompanyPlanningViewCharts(
            Boolean expandCompanyPlanningViewCharts) {
        if (configuration != null) {
            configuration
                    .setExpandCompanyPlanningViewCharts(expandCompanyPlanningViewCharts);
        }
    }

    @Override
    public Boolean isMonteCarloMethodTabVisible() {
        if (configuration == null) {
            return null;
        }
        return configuration.isMonteCarloMethodTabVisible();
    }

    @Override
    public void setMonteCarloMethodTabVisible(
            Boolean visible) {
        if (configuration != null) {
            configuration
                    .setMonteCarloMethodTabVisible(visible);
        }
    }

    @Override
    public Boolean isExpandCompanyPlanningViewCharts() {
        if (configuration == null) {
            return null;
        }
        return configuration.isExpandCompanyPlanningViewCharts();
    }

    @Override
    public void setExpandOrderPlanningViewCharts(
            Boolean expandOrderPlanningViewCharts) {
        if (configuration != null) {
            configuration
                .setExpandOrderPlanningViewCharts(expandOrderPlanningViewCharts);
        }
    }

    @Override
    public Boolean isExpandOrderPlanningViewCharts() {
        if (configuration == null) {
            return null;
        }
        return configuration.isExpandOrderPlanningViewCharts();
    }


    @Override
    public void setExpandResourceLoadViewCharts(
            Boolean expandResourceLoadViewCharts) {
        if (configuration != null) {
            configuration
                    .setExpandResourceLoadViewCharts(expandResourceLoadViewCharts);
        }
    }

    @Override
    public Boolean isExpandResourceLoadViewCharts() {
        if (configuration == null) {
            return null;
        }
        return configuration.isExpandResourceLoadViewCharts();
    }

}