/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 ComtecSF, S.L.
 * Copyright (C) 2013 Igalia.
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

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.web.common.FilterUtils;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Intbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for user settings.
 *
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public class SettingsController extends GenericForwardComposer {

    private IMessagesForUser messages;

    private Component messagesContainer;

    private ISettingsModel settingsModel;

    private BandboxSearch projectsFilterLabelBandboxSearch;

    private BandboxSearch resourcesLoadFilterCriterionBandboxSearch;

    private static ListitemRenderer languagesRenderer = (item, data, i) -> {
        Language language = (Language) data;
        String displayName = language.getDisplayName();

        if (language.equals(Language.BROWSER_LANGUAGE)) {
            displayName = _(language.getDisplayName());
        }

        item.setLabel(displayName);
    };

    public SettingsController() {
        settingsModel = (ISettingsModel) SpringUtil.getBean("settingsModel");
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("settingsController", this, true);
        messages = new MessagesForUser(messagesContainer);
        settingsModel.initEditLoggedUser();

        projectsFilterLabelBandboxSearch.setListboxEventListener(
                Events.ON_SELECT, event -> {
                    Listitem selectedItem = (Listitem) ((SelectEvent) event).getSelectedItems().iterator().next();
                    setProjectsFilterLabel(selectedItem.getValue());
                });

        resourcesLoadFilterCriterionBandboxSearch.setListboxEventListener(
                Events.ON_SELECT, event -> {
                    Listitem selectedItem = (Listitem) ((SelectEvent) event).getSelectedItems().iterator().next();
                    setResourcesLoadFilterCriterion(selectedItem.getValue());
                });
    }

    public List<Language> getLanguages() {
        List<Language> languages = Arrays.asList(Language.values());
        Collections.sort(languages, (o1, o2) -> {
            if (o1.equals(Language.BROWSER_LANGUAGE)) {
                return -1;
            }

            if (o2.equals(Language.BROWSER_LANGUAGE)) {
                return 1;
            }

            return o1.getDisplayName().compareTo(o2.getDisplayName());
        });

        return languages;
    }

    public boolean save() {
        try {
            if ( monthsValuesAreValid() ) {
                checkEmptyBandboxes();
                clearSessionVariables();
                settingsModel.confirmSave();
                messages.showMessage(Level.INFO, _("Settings saved"));

                return true;
            }

        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }

        return false;
    }

    private void clearSessionVariables() {
        FilterUtils.clearBandboxes();
        FilterUtils.clearSessionDates();
    }

    private void checkEmptyBandboxes() {
        if (projectsFilterLabelBandboxSearch.getSelectedElement() == null) {
            settingsModel.setProjectsFilterLabel(null);
        }

        if (resourcesLoadFilterCriterionBandboxSearch.getSelectedElement() == null) {
            settingsModel.setResourcesLoadFilterCriterion(null);
        }
    }

    /**
     * Should be public!
     */
    public Constraint checkMonthsMaxValue() {
        return (comp, value) -> {
            if ( value != null && (Integer) value > 999 ) {
                ((Intbox) comp).setValue(null);
                throw new WrongValueException(comp, _("Max value = 999"));
            }
        };
    }

    private boolean monthsValuesAreValid() {
        Integer projectsSince = settingsModel.getProjectsFilterPeriodSince();
        Integer projectsTo = settingsModel.getProjectsFilterPeriodTo();

        if ( projectsTo != null && projectsSince != null && projectsSince > projectsTo ) {
            messages.clearMessages();
            messages.showMessage(Level.WARNING, _("Project since should be lower than project to"));

            return false;
        }

        Integer resourcesSince = settingsModel.getResourcesLoadFilterPeriodSince();
        Integer resourcesTo = settingsModel.getResourcesLoadFilterPeriodTo();

        if ( resourcesTo != null && resourcesSince != null && resourcesSince > resourcesTo ) {
            messages.clearMessages();
            messages.showMessage(Level.WARNING, _("Resources load since should be lower than resources load to"));

            return false;
        }

        return true;
    }

    public void setSelectedLanguage(Language language) {
        settingsModel.setApplicationLanguage(language);
    }

    public Language getSelectedLanguage() {
        return settingsModel.getApplicationLanguage();
    }

    public static ListitemRenderer getLanguagesRenderer() {
        return languagesRenderer;
    }

    public void setExpandCompanyPlanningViewCharts(boolean expandCompanyPlanningViewCharts) {
        checkEmptyBandboxes();
        settingsModel.setExpandCompanyPlanningViewCharts(expandCompanyPlanningViewCharts);
    }

    public boolean isExpandCompanyPlanningViewCharts() {
        return settingsModel.isExpandCompanyPlanningViewCharts();
    }

    public void setExpandOrderPlanningViewCharts(boolean expandOrderPlanningViewCharts) {
        checkEmptyBandboxes();
        settingsModel.setExpandOrderPlanningViewCharts(expandOrderPlanningViewCharts);
    }

    public boolean isExpandOrderPlanningViewCharts() {
        return settingsModel.isExpandOrderPlanningViewCharts();
    }

    public void setExpandResourceLoadViewCharts(boolean expandResourceLoadViewCharts) {
        checkEmptyBandboxes();
        settingsModel.setExpandResourceLoadViewCharts(expandResourceLoadViewCharts);
    }

    public boolean isExpandResourceLoadViewCharts() {
        return settingsModel.isExpandResourceLoadViewCharts();
    }

    public String getFirstName() {
        return settingsModel.getFirstName();
    }

    public void setFirstName(String firstName) {
        settingsModel.setFirstName(firstName);
    }

    public String getLastName() {
        return settingsModel.getLastName();
    }

    public void setLastName(String lastName) {
        settingsModel.setLastName(lastName);
    }

    public String getLoginName() {
        return settingsModel.getLoginName();
    }

    public void setEmail(String email) {
        settingsModel.setEmail(email);
    }

    public String getEmail() {
        return settingsModel.getEmail();
    }

    public boolean isBound() {
        return settingsModel.isBound();
    }

    public Integer getProjectsFilterPeriodSince() {
        return settingsModel.getProjectsFilterPeriodSince();
    }

    public void setProjectsFilterPeriodSince(Integer period) {
        settingsModel.setProjectsFilterPeriodSince(period);
    }

    public Integer getProjectsFilterPeriodTo() {
        return settingsModel.getProjectsFilterPeriodTo();
    }

    public void setProjectsFilterPeriodTo(Integer period) {
        settingsModel.setProjectsFilterPeriodTo(period);
    }

    public Integer getResourcesLoadFilterPeriodSince() {
        return settingsModel.getResourcesLoadFilterPeriodSince();
    }

    public void setResourcesLoadFilterPeriodSince(Integer period) {
        settingsModel.setResourcesLoadFilterPeriodSince(period);
    }

    public Integer getResourcesLoadFilterPeriodTo() {
        return settingsModel.getResourcesLoadFilterPeriodTo();
    }

    public void setResourcesLoadFilterPeriodTo(Integer period) {
        settingsModel.setResourcesLoadFilterPeriodTo(period);
    }

    public List<Label> getAllLabels() {
        return settingsModel.getAllLabels();
    }

    public Label getProjectsFilterLabel() {
        return settingsModel.getProjectsFilterLabel();
    }

    private void setProjectsFilterLabel(Label label) {
        settingsModel.setProjectsFilterLabel(label);
    }

    public List<Criterion> getAllCriteria() {
        return settingsModel.getAllCriteria();
    }

    public Criterion getResourcesLoadFilterCriterion() {
        return settingsModel.getResourcesLoadFilterCriterion();
    }

    private void setResourcesLoadFilterCriterion(Criterion criterion) {
        settingsModel.setResourcesLoadFilterCriterion(criterion);
    }

    public boolean isShowResourcesOn() {
        return settingsModel.isShowResourcesOn();
    }

    public void setShowResourcesOn(boolean showResourcesOn) {
        settingsModel.setShowResourcesOn(showResourcesOn);
    }

    public boolean isShowAdvancesOn() {
        return settingsModel.isShowAdvancesOn();
    }

    public void setShowAdvancesOn(boolean showAdvancesOn) {
        settingsModel.setShowAdvancesOn(showAdvancesOn);
    }

    public boolean isShowReportedHoursOn() {
        return settingsModel.isShowReportedHoursOn();
    }

    public void setShowReportedHoursOn(boolean showReportedHoursOn) {
        settingsModel.setShowReportedHoursOn(showReportedHoursOn);
    }

    public boolean isShowLabelsOn() {
        return settingsModel.isShowLabelsOn();
    }

    public void setShowLabelsOn(boolean showLabelsOn) {
        settingsModel.setShowLabelsOn(showLabelsOn);
    }

    public boolean isShowMoneyCostBarOn() {
        return settingsModel.isShowMoneyCostBarOn();
    }

    public void setShowMoneyCostBarOn(boolean showMoneyCostBarOn) {
        settingsModel.setShowMoneyCostBarOn(showMoneyCostBarOn);
    }

    public boolean isProjectsFilterFinishedOn() {
        return settingsModel.isProjectsFilterFinishedOn();
    }

    public void setProjectsFilterFinishedOn(boolean projectsFilterFinishedOn) {
        settingsModel.setProjectsFilterFinishedOn(projectsFilterFinishedOn);
    }

}
