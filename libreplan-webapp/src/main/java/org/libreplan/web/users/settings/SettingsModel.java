/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 ComtecSF, S.L.
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

package org.libreplan.web.users.settings;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.Profile;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to user settings
 *
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/myaccount/settings.zul")
public class SettingsModel implements ISettingsModel {

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private ILabelDAO labelsDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    private User user;

    private List<Label> allLabels;

    private List<Criterion> allCriteria;

    @Override
    public Language getApplicationLanguage() {
        return user.getApplicationLanguage();
    }

    @Override
    public void setApplicationLanguage(Language applicationLanguage) {
        this.user.setApplicationLanguage(applicationLanguage);
    }

    private User findByLoginUser(String login) {
        try {
            return user = userDAO.findByLoginName(login);
        } catch (InstanceNotFoundException e) {
               throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void initEditLoggedUser() {
        User user = findByLoginUser(SecurityUtils.getSessionUserLoginName());
        this.user = getFromDB(user);
        loadAllLabels();
        loadAllCriteria();
    }

    @Transactional(readOnly = true)
    private void loadAllLabels() {
        allLabels = labelsDAO.getAll();
        // initialize the labels
        for (Label label : allLabels) {
            label.getType().getName();
        }
    }

    @Transactional(readOnly = true)
    private void loadAllCriteria() {
        allCriteria = criterionDAO.getAll();
        // initialize the criteria
        for (Criterion criterion : allCriteria) {
            criterion.getType().getName();
        }
    }

    @Transactional(readOnly = true)
    private User getFromDB(User user) {
        return getFromDB(user.getId());
    }

    private User getFromDB(Long id) {
        try {
            User result = userDAO.find(id);
            forceLoadEntities(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void forceLoadEntities(User user) {
        user.getLoginName();
        for (UserRole each : user.getRoles()) {
            each.name();
        }
        for (Profile each : user.getProfiles()) {
            each.getProfileName();
        }
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        try {
            // user.getLoginName() has to be validated before encoding password,
            // because it must exist to perform the encoding
            Validate.notEmpty(user.getLoginName());

        } catch (IllegalArgumentException e) {
        }
        user.validate();
        userDAO.save(user);
    }
    @Override
    public boolean isExpandCompanyPlanningViewCharts() {
        return user.isExpandCompanyPlanningViewCharts();
    }

    @Override
    public void setExpandOrderPlanningViewCharts(
            boolean expandOrderPlanningViewCharts) {
        if (user != null) {
            user.setExpandOrderPlanningViewCharts(expandOrderPlanningViewCharts);
        }
    }

    @Override
    public boolean isExpandOrderPlanningViewCharts() {
        return user.isExpandOrderPlanningViewCharts();
    }

    @Override
    public void setExpandResourceLoadViewCharts(
            boolean expandResourceLoadViewCharts) {
        if (user != null) {
            user.setExpandResourceLoadViewCharts(expandResourceLoadViewCharts);
        }
    }

    @Override
    public boolean isExpandResourceLoadViewCharts() {
        return user.isExpandResourceLoadViewCharts();
    }

    @Override
    public void setExpandCompanyPlanningViewCharts(
            boolean expandCompanyPlanningViewCharts) {
        if (user != null) {
            user.setExpandCompanyPlanningViewCharts(expandCompanyPlanningViewCharts);
        }
    }

    @Override
    public String getFirstName() {
        return user.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        if (user != null) {
            user.setFirstName(firstName);
        }
    }

    @Override
    public String getLastName() {
        return user.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        if (user != null) {
            user.setLastName(lastName);
        }
    }

    @Override
	public String getLoginName() {
        return user.getLoginName();
    }

    @Override
	public void setEmail(String email) {
        if (user != null) {
            user.setEmail(email);
        }
    }

    @Override
	public String getEmail() {
        return user.getEmail();
    }

    @Override
    public boolean isBound() {
        if (user != null) {
            return user.isBound();
        }
        return false;
    }

    @Override
    public Integer getProjectsFilterPeriodSince() {
        return user.getProjectsFilterPeriodSince();
    }

    @Override
    public void setProjectsFilterPeriodSince(Integer period) {
        user.setProjectsFilterPeriodSince(period);
    }

    @Override
    public Integer getProjectsFilterPeriodTo() {
        return user.getProjectsFilterPeriodTo();
    }

    @Override
    public void setProjectsFilterPeriodTo(Integer period) {
        user.setProjectsFilterPeriodTo(period);
    }

    @Override
    public Integer getResourcesLoadFilterPeriodSince() {
        return user.getResourcesLoadFilterPeriodSince();
    }

    @Override
    public void setResourcesLoadFilterPeriodSince(Integer period) {
        user.setResourcesLoadFilterPeriodSince(period);
    }

    @Override
    public Integer getResourcesLoadFilterPeriodTo() {
        return user.getResourcesLoadFilterPeriodTo();
    }

    @Override
    public void setResourcesLoadFilterPeriodTo(Integer period) {
        user.setResourcesLoadFilterPeriodTo(period);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Label> getAllLabels() {
        return allLabels;
    }

    @Override
    public Label getProjectsFilterLabel() {
        return user.getProjectsFilterLabel();
    }

    @Override
    public void setProjectsFilterLabel(Label label) {
        user.setProjectsFilterLabel(label);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Criterion> getAllCriteria() {
        return allCriteria;
    }

    @Override
    public Criterion getResourcesLoadFilterCriterion() {
        return user.getResourcesLoadFilterCriterion();
    }

    @Override
    public void setResourcesLoadFilterCriterion(Criterion criterion) {
        user.setResourcesLoadFilterCriterion(criterion);
    }

    @Override
    public boolean isShowResourcesOn() {
        return user.isShowResourcesOn();
    }

    @Override
    public void setShowResourcesOn(boolean showResourcesOn) {
        user.setShowResourcesOn(showResourcesOn);
    }

    @Override
    public boolean isShowAdvancesOn() {
        return user.isShowAdvancesOn();
    }

    @Override
    public void setShowAdvancesOn(boolean showAdvancesOn) {
        user.setShowAdvancesOn(showAdvancesOn);
    }

    @Override
    public boolean isShowReportedHoursOn() {
        return user.isShowReportedHoursOn();
    }

    @Override
    public void setShowReportedHoursOn(boolean showReportedHoursOn) {
        user.setShowReportedHoursOn(showReportedHoursOn);
    }

    @Override
    public boolean isShowLabelsOn() {
        return user.isShowLabelsOn();
    }

    @Override
    public void setShowLabelsOn(boolean showLabelsOn) {
        user.setShowLabelsOn(showLabelsOn);
    }

    @Override
    public boolean isShowMoneyCostBarOn() {
        return user.isShowMoneyCostBarOn();
    }

    @Override
    public void setShowMoneyCostBarOn(boolean showMoneyCostBarOn) {
            user.setShowMoneyCostBarOn(showMoneyCostBarOn);
    }

    @Override
    public boolean isProjectsFilterFinishedOn() {
        return user.isProjectsFilterFinishedOn();
    }

    @Override
    public void setProjectsFilterFinishedOn(boolean projectsFilterFinishedOn) {
        user.setProjectsFilterFinishedOn(projectsFilterFinishedOn);
    }

}
