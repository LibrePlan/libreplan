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

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.entities.Configuration;
import org.navalplanner.business.common.entities.EntityNameEnum;
import org.navalplanner.business.common.entities.EntitySequence;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Listitem;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Configuration} entity.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ConfigurationController extends GenericForwardComposer {

    private Window configurationWindow;
    private BandboxSearch defaultCalendarBandboxSearch;

    private IConfigurationModel configurationModel;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Tabpanel panelConfiguration;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("configurationController", this, true);
        configurationModel.init();

        defaultCalendarBandboxSearch.setListboxEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        Listitem selectedItem = (Listitem) ((SelectEvent) event)
                                .getSelectedItems().iterator().next();
                        setDefaultCalendar((BaseCalendar) selectedItem
                                .getValue());
                    }
                });

        messages = new MessagesForUser(messagesContainer);

        createPanelEntityComponents();

    }

    private void createPanelEntityComponents() {
        for (final EntityNameEnum entityName : EntityNameEnum.values()) {
            Component entitySequenceComponent = Executions.createComponents(
                    "components/panelEntitySequence.zul", panelConfiguration
                            .getFellow("panelEntitySequence"),
                    new HashMap<String, String>());
            initLabelInPanelSequence(entitySequenceComponent, entityName);
            initButtonInPanelSequence(entitySequenceComponent, entityName);
            initGridInPanelSequence(entitySequenceComponent, entityName);
            reloadEntitySequenceList(entityName);
        }
    }

    private void initLabelInPanelSequence(Component component,
            final EntityNameEnum entityName) {
        try {
            Label label = (Label) component.getFirstChild().getFirstChild();
            label.setValue(_(entityName.getSequenceLiteral()));
            label.invalidate();
        } catch (ClassCastException e) {
        }
    }

    private void initButtonInPanelSequence(Component component,
            final EntityNameEnum entityName) {
        try {
            Button button = (Button) component.getFirstChild().getLastChild();
            button.setLabel(_("New"));
            button.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    addEntitySequence(entityName);
                }
            });
        } catch (ClassCastException e) {
        }
    }

    private void initGridInPanelSequence(Component component,
            EntityNameEnum entityName) {
        String name = entityName.getDescription();
        String id = name + "SequenceList";
        try {
            Grid grid = (Grid) component.getLastChild().getFirstChild();
            grid.setId(id);
        } catch (ClassCastException e) {
        }
    }

    // private void addSeparatorComponent(Component component){
    // Separator separator = new Separator();
    // separator.setHeight("10px");
    // separator.setBar(true);
    // component.appendChild(separator);
    // }

    public List<BaseCalendar> getCalendars() {
        return configurationModel.getCalendars();
    }

    public BaseCalendar getDefaultCalendar() {
        return configurationModel.getDefaultCalendar();
    }

    public void setDefaultCalendar(BaseCalendar calendar) {
        configurationModel.setDefaultCalendar(calendar);
    }

    public void save() throws InterruptedException {
        if (ConstraintChecker.isValid(configurationWindow)) {
            try {
                configurationModel.confirm();
                configurationModel.init();
                messages.showMessage(Level.INFO, _("Changes saved"));
                reloadWindow();
            } catch (ValidationException e) {
                messages.showInvalidValues(e);
            } catch (ConcurrentModificationException e) {
                messages.showMessage(Level.ERROR, e.getMessage());
                configurationModel.init();
                reloadWindow();
            }
        }
    }

    public void cancel() throws InterruptedException {
        configurationModel.cancel();
        messages.showMessage(Level.INFO, _("Changes have been canceled"));
        reloadWindow();
    }

    private void reloadWindow() {
        Util.reloadBindings(configurationWindow);
        for (EntityNameEnum sequence : EntityNameEnum.values()) {
            reloadEntitySequenceList(sequence);
        }
    }

    public String getCompanyCode() {
        return configurationModel.getCompanyCode();
    }

    public void setCompanyCode(String companyCode) {
        configurationModel.setCompanyCode(companyCode);
    }

    public Boolean getGenerateCodeForCriterion() {
        return configurationModel.getGenerateCodeForCriterion();
    }

    public void setGenerateCodeForCriterion(Boolean generateCodeForCriterion) {
        configurationModel.setGenerateCodeForCriterion(generateCodeForCriterion);
    }

    public Boolean getGenerateCodeForLabel() {
        return configurationModel.getGenerateCodeForLabel();
    }

    public void setGenerateCodeForLabel(Boolean generateCodeForLabel) {
        configurationModel.setGenerateCodeForLabel(generateCodeForLabel);
    }

    public Boolean getGenerateCodeForWorkReport() {
        return configurationModel.getGenerateCodeForWorkReport();
    }

    public void setGenerateCodeForWorkReport(Boolean generateCodeForWorkReport) {
        configurationModel.setGenerateCodeForWorkReport(generateCodeForWorkReport);
    }

    public Boolean getGenerateCodeForResources() {
        return configurationModel.getGenerateCodeForResources();
    }

    public void setGenerateCodeForResources(Boolean generateCodeForResources) {
        configurationModel.setGenerateCodeForResources(generateCodeForResources);
    }

    public Boolean getGenerateCodeForTypesOfWorkHours() {
        return configurationModel.getGenerateCodeForTypesOfWorkHours();
    }

    public void setGenerateCodeForTypesOfWorkHours(
            Boolean generateCodeForTypesOfWorkHours) {
        configurationModel.setGenerateCodeForTypesOfWorkHours(
                generateCodeForTypesOfWorkHours);
    }

    public Boolean getGenerateCodeForMaterialCategories() {
        return configurationModel.getGenerateCodeForMaterialCategories();
    }

    public void setGenerateCodeForMaterialCategories(
            Boolean generateCodeForMaterialCategories) {
        configurationModel.setGenerateCodeForMaterialCategories(
                generateCodeForMaterialCategories);
    }

    public void reloadGeneralConfiguration() {
        reloadWindow();
    }

    public Boolean getGenerateCodeForUnitTypes() {
        return configurationModel.getGenerateCodeForUnitTypes();
    }

    public void setGenerateCodeForUnitTypes(Boolean generateCodeForUnitTypes) {
        configurationModel
                .setGenerateCodeForUnitTypes(generateCodeForUnitTypes);
    }

    public Boolean getGenerateCodeForBaseCalendars() {
        return configurationModel.getGenerateCodeForBaseCalendars();
    }

    public void setGenerateCodeForBaseCalendars(
            Boolean generateCodeForBaseCalendars) {
        configurationModel
                .setGenerateCodeForBaseCalendars(generateCodeForBaseCalendars);
    }

    public void removeEntitySequence(EntitySequence entitySequence) {
        try {
            configurationModel.removeEntitySequence(entitySequence);
        } catch (IllegalArgumentException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
        }
        reloadEntitySequenceList(entitySequence.getEntityName());
    }

    public void setExpandCompanyPlanningViewCharts(
            Boolean expandCompanyPlanningViewCharts) {
        configurationModel
                .setExpandCompanyPlanningViewCharts(expandCompanyPlanningViewCharts);
    }

    public Boolean isExpandCompanyPlanningViewCharts() {
        return configurationModel.isExpandCompanyPlanningViewCharts();
    }

    public void setExpandOrderPlanningViewCharts(
            Boolean expandOrderPlanningViewCharts) {
        configurationModel
                .setExpandOrderPlanningViewCharts(expandOrderPlanningViewCharts);
    }

    public Boolean isExpandOrderPlanningViewCharts() {
        return configurationModel.isExpandOrderPlanningViewCharts();
    }

    public void setExpandResourceLoadViewCharts(
            Boolean expandResourceLoadViewCharts) {
        configurationModel
                .setExpandResourceLoadViewCharts(expandResourceLoadViewCharts);
    }

    public Boolean isExpandResourceLoadViewCharts() {
        return configurationModel.isExpandResourceLoadViewCharts();
    }

    public void setMonteCarloMethodTabVisible(
            Boolean expandResourceLoadViewCharts) {
        configurationModel
                .setMonteCarloMethodTabVisible(expandResourceLoadViewCharts);
    }

    public Boolean isMonteCarloMethodTabVisible() {
        return configurationModel.isMonteCarloMethodTabVisible();
    }

    private void reloadEntitySequenceList(EntityNameEnum entityNameEnum) {
        String nameList = entityNameEnum.getDescription();
        Grid grid = (Grid) configurationWindow.getFellow(nameList
                + "SequenceList");
        grid.setModel(new SimpleListModel(getEntitySequences(entityNameEnum)));
        grid.invalidate();
    }

    public EntitySequenceRowRenderer getEntitySequenceRowRenderer() {
        return new EntitySequenceRowRenderer();
    }

    private class EntitySequenceRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            EntitySequence entitySequence = (EntitySequence) data;
            row.setValue(entitySequence);

            appendActiveRadiobox(row, entitySequence);
            appendPrefixTextbox(row, entitySequence);
            appendNumberOfDigitsInbox(row, entitySequence);
            appendLastValueInbox(row, entitySequence);
            appendOperations(row, entitySequence);

            if (entitySequence.isAlreadyInUse()) {
                row
                        .setTooltiptext(_("The code sequence is already in use and it can not be updated."));
            }
        }

        private void appendActiveRadiobox(final Row row,
                final EntitySequence entitySequence) {
            final Radio radiobox = Util.bind(new Radio(),
                    new Util.Getter<Boolean>() {

                        @Override
                        public Boolean get() {
                            return entitySequence.isActive();
                        }
                    }, new Util.Setter<Boolean>() {

                        @Override
                        public void set(Boolean value) {
                            entitySequence.setActive(value);
                            updateOtherSequences(entitySequence);
                        }
                    });

            row.appendChild(radiobox);

            if (entitySequence.isActive()) {
                radiobox.getRadiogroup().setSelectedItem(radiobox);
                radiobox.getRadiogroup().invalidate();
            }
        }

        private void updateOtherSequences(final EntitySequence activeSequence) {
            for (EntitySequence sequence : getEntitySequences(activeSequence
                    .getEntityName())) {
                if (sequence.getId() != activeSequence.getId()) {
                    sequence.setActive(false);
                }
            }
        }

        private void appendPrefixTextbox(Row row,
                final EntitySequence entitySequence) {
            final Textbox tempTextbox = new Textbox();
            Textbox textbox = Util.bind(tempTextbox, new Util.Getter<String>() {

                @Override
                public String get() {
                    return entitySequence.getPrefix();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    try {
                        entitySequence.setPrefix(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(tempTextbox, e
                                .getMessage());
                    }
                }
            });
            textbox.setConstraint("no empty:" + _("cannot be null or empty"));

            if (entitySequence.isAlreadyInUse()) {
                textbox.setDisabled(true);
            }

            row.appendChild(textbox);
        }

        private void appendNumberOfDigitsInbox(Row row,
                final EntitySequence entitySequence) {
            final Intbox tempIntbox = new Intbox();
            Intbox intbox = Util.bind(tempIntbox, new Util.Getter<Integer>() {

                @Override
                public Integer get() {
                    return entitySequence.getNumberOfDigits();
                }
            }, new Util.Setter<Integer>() {

                @Override
                public void set(Integer value) {
                    try {
                        entitySequence.setNumberOfDigits(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(tempIntbox, e
                                .getMessage());
                    }
                }
            });
            intbox.setConstraint("no empty:" + _("cannot be null or empty"));

            if (entitySequence.isAlreadyInUse()) {
                intbox.setDisabled(true);
            }

            row.appendChild(intbox);
        }

        private void appendLastValueInbox(Row row,
                final EntitySequence entitySequence) {
            Textbox textbox = Util.bind(new Textbox(),
                    new Util.Getter<String>() {

                        @Override
                        public String get() {
                            return EntitySequence.formatValue(entitySequence
                                    .getNumberOfDigits(), entitySequence
                                    .getLastValue());
                        }
                    });

            row.appendChild(textbox);
        }

        private void appendOperations(Row row,
                final EntitySequence entitySequence) {
            final Button removeButton = Util
                    .createRemoveButton(new EventListener() {

                        @Override
                        public void onEvent(Event event) throws Exception {
                            removeEntitySequence(entitySequence);
                        }
                    });

            if (entitySequence.isAlreadyInUse()) {
                removeButton.setDisabled(true);
            }

            row.appendChild(removeButton);
        }

    }

    public void addEntitySequence(EntityNameEnum entityName) {
        configurationModel.addEntitySequence(entityName);
        reloadEntitySequenceList(entityName);
    }

    public List<EntitySequence> getEntitySequences(EntityNameEnum entityName) {
        return configurationModel.getEntitySequences(entityName);
    }

}