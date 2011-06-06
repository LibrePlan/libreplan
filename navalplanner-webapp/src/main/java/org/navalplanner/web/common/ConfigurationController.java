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

package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.entities.Configuration;
import org.navalplanner.business.common.entities.EntityNameEnum;
import org.navalplanner.business.common.entities.EntitySequence;
import org.navalplanner.business.common.entities.ProgressType;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.ArrayGroupsModel;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Group;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;


/**
 * Controller for {@link Configuration} entity.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ConfigurationController extends GenericForwardComposer {

    private final ProgressTypeRenderer progressTypeRenderer = new ProgressTypeRenderer();

    private Window configurationWindow;

    private BandboxSearch defaultCalendarBandboxSearch;

    private Listbox lbTypeProgress;

    private IConfigurationModel configurationModel;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Grid entitySequencesGrid;

    private Combobox entityCombo;

    private Intbox numDigitBox;

    private Textbox prefixBox;

    private Checkbox scenariosVisible;

    private Map<EntityNameEnum, Boolean> mapOpenedGroups = new HashMap<EntityNameEnum, Boolean>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("configurationController", this, true);
        configurationModel.init();

        defaultCalendarBandboxSearch.setListboxEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        Listitem selectedItem = (Listitem) ((SelectEvent) event)
                                .getSelectedItems().iterator().next();
                        setDefaultCalendar((BaseCalendar) selectedItem
                                .getValue());
                    }
                });
        initializeProgressTypeList();
        messages = new MessagesForUser(messagesContainer);
        initOpenedGroup();
        reloadEntitySequences();
        if (moreScenariosThanMasterCreated()) {
            scenariosVisible.setChecked(true);
            scenariosVisible.setDisabled(true);
            scenariosVisible
                    .setTooltiptext(_("Scenarios must be enabled as more elements than master exist"));
        }
    }

    private void initializeProgressTypeList() {
        lbTypeProgress.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) {
                Listitem selectedItem = getSelectedItem((SelectEvent) event);
                if (selectedItem != null) {
                    ProgressType progressType = (ProgressType) selectedItem.getValue();
                    configurationModel.setProgressType(progressType);
                }
            }

            private Listitem getSelectedItem(SelectEvent event) {
                final Set<Listitem> selectedItems = event.getSelectedItems();
                return selectedItems.iterator().next();
            }

        });
    }

    public List<ProgressType> getProgressTypes() {
        return configurationModel.getProgresTypes();
    }

    public ProgressType getSelectedProgressType() {
        return configurationModel.getProgressType();
    }

    public void setSelectedProgressType(ProgressType progressType) {
        configurationModel.setProgressType(progressType);
    }

    private void initOpenedGroup() {
        for (final EntityNameEnum entityName : EntityNameEnum.values()) {
            this.mapOpenedGroups.put(entityName, false);
        }
    }

    private boolean isOpenedGroup(EntityNameEnum entityName) {
        return mapOpenedGroups.get(entityName);
    }

    public void onOpenGroup(EntityNameEnum entityName, boolean open) {
        mapOpenedGroups.put(entityName, open);
    }

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
        if (ConstraintChecker.isValid(configurationWindow)
                && checkValidEntitySequenceRows()) {
            try {
                configurationModel.confirm();
                configurationModel.init();
                messages.showMessage(Level.INFO, _("Changes saved"));
                reloadWindow();
                initOpenedGroup();
                reloadEntitySequences();
            } catch (ValidationException e) {
                messages.showInvalidValues(e);
            } catch (ConcurrentModificationException e) {
                messages.showMessage(Level.ERROR, e.getMessage());
                configurationModel.init();
                reloadWindow();
                reloadEntitySequences();
            }
        }
    }

    public void cancel() throws InterruptedException {
        configurationModel.cancel();
        messages.showMessage(Level.INFO, _("Changes have been canceled"));
        reloadWindow();
        initOpenedGroup();
        reloadEntitySequences();
    }

    private boolean checkValidEntitySequenceRows() {
        Rows rows = entitySequencesGrid.getRows();
        for (Row row : (List<Row>) rows.getChildren()) {
            if (!(row instanceof Group)) {
                EntitySequence seq = (EntitySequence) row.getValue();
                if (seq != null) {
                    Textbox prefixBox = (Textbox) row.getChildren().get(1);
                    if (!seq.isAlreadyInUse()) {
                        String errorMessage = this.validPrefix(seq, prefixBox
                                .getValue());
                        if (errorMessage != null) {
                            throw new WrongValueException(prefixBox,
                                    errorMessage);
                        }
                    }

                    Intbox digitsBox = (Intbox) row.getChildren().get(2);
                    try {
                        if (!seq.isAlreadyInUse()) {
                            seq.setNumberOfDigits(digitsBox.getValue());
                        }
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(digitsBox, e.getMessage());
                    }
                }
            }
        }
        return true;
    }

    private void reloadWindow() {
        Util.reloadBindings(configurationWindow);
    }

    private void reloadEntitySequences() {
        entitySequencesGrid.setModel(getEntitySequenceModel());
        entitySequencesGrid.invalidate();
    }

    public String getCompanyCode() {
        return configurationModel.getCompanyCode();
    }

    public void setCompanyCode(String companyCode) {
        configurationModel.setCompanyCode(companyCode);
    }

    public String getCompanyLogoURL() {
        return configurationModel.getCompanyLogoURL();
    }

    public void setCompanyLogoURL(String companyLogoURL) {
        configurationModel.setCompanyLogoURL(companyLogoURL);
    }

    public Boolean getGenerateCodeForCriterion() {
        return configurationModel.getGenerateCodeForCriterion();
    }

    public void setGenerateCodeForCriterion(Boolean generateCodeForCriterion) {
        configurationModel.setGenerateCodeForCriterion(generateCodeForCriterion);
    }

    public Boolean getGenerateCodeForWorkReportType() {
        return configurationModel.getGenerateCodeForWorkReportType();
    }

    public void setGenerateCodeForWorkReportType(
            Boolean generateCodeForWorkReportType) {
        configurationModel
                .setGenerateCodeForWorkReportType(generateCodeForWorkReportType);
    }

    public Boolean getGenerateCodeForCalendarExceptionType() {
        return configurationModel.getGenerateCodeForCalendarExceptionType();
    }

    public void setGenerateCodeForCalendarExceptionType(
            Boolean generateCodeForCalendarExceptionType) {
        configurationModel
                .setGenerateCodeForCalendarExceptionType(generateCodeForCalendarExceptionType);
    }

    public Boolean getGenerateCodeForCostCategory() {
        return configurationModel.getGenerateCodeForCostCategory();
    }

    public void setGenerateCodeForCostCategory(
            Boolean generateCodeForCostCategory) {
        configurationModel
                .setGenerateCodeForCostCategory(generateCodeForCostCategory);
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

    public Boolean isAutocompleteLogin() {
        return configurationModel.isAutocompleteLogin();
    }

    public void setAutocompleteLogin(Boolean autocompleteLogin) {
        configurationModel.setAutocompleteLogin(autocompleteLogin);
    }

    public void removeEntitySequence(EntitySequence entitySequence) {
        try {
            configurationModel.removeEntitySequence(entitySequence);
        } catch (IllegalArgumentException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
        }
        onOpenGroup(entitySequence.getEntityName(), true);
        reloadEntitySequences();
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

    public void setScenariosVisible(Boolean scenariosVisible) {
        configurationModel.setScenariosVisible(scenariosVisible);
    }

    public Boolean isScenariosVisible() {
        return configurationModel.isScenariosVisible();
    }

    public ProgressTypeRenderer getProgressTypeRenderer() {
        return progressTypeRenderer;
    }

    private static class ProgressTypeRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) {
            ProgressType progressType = (ProgressType) data;
            item.setLabel(_(progressType.getValue()));
        }

    }

    public class EntitySequenceGroupRenderer implements RowRenderer {
        @Override
        public void render(Row row, Object data) {

            EntitySequence entitySequence = (EntitySequence) data;
            final EntityNameEnum entityName = entitySequence.getEntityName();

            if (row instanceof Group) {
                final Group group = ((Group) row);
                if (!isOpenedGroup(entityName)) {
                    group.setOpen(false);
                }
                group.setValue(entityName);
                group
                        .appendChild(new Label(_(entityName
                                .getSequenceLiteral())));
                group.addEventListener(Events.ON_OPEN, new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        onOpenGroup(entityName, group.isOpen());
                    }
                });

            } else {

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
                            updateOtherSequences(entitySequence);
                            entitySequence.setActive(value);
                            Util.reloadBindings(entitySequencesGrid);
                            reloadEntitySequences();
                        }
                    });

            row.appendChild(radiobox);
        }

        private void updateOtherSequences(final EntitySequence activeSequence) {
            for (EntitySequence sequence : getEntitySequences(activeSequence
                    .getEntityName())) {
                    sequence.setActive(false);
            }
        }

        private void appendPrefixTextbox(Row row,
                final EntitySequence entitySequence) {
            final Textbox tempTextbox = new Textbox();
            tempTextbox.setWidth("200px");
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
            textbox.setConstraint(checkConstraintFormatPrefix());

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
            intbox.setConstraint(checkConstraintNumberOfDigits());

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

        private void appendOperations(final Row row,
                final EntitySequence entitySequence) {
            final Button removeButton = Util
                    .createRemoveButton(new EventListener() {

                        @Override
                        public void onEvent(Event event) {
                            if (isLastOne(entitySequence)) {
                                showMessageNotDelete();
                            } else {
                                removeEntitySequence(entitySequence);
                            }
                        }
                    });

            if (entitySequence.isAlreadyInUse()) {
                removeButton.setDisabled(true);
            }

            row.appendChild(removeButton);
        }

    }

    public Constraint checkConstraintFormatPrefix() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {

                Row row = (Row) comp.getParent();
                EntitySequence sequence = (EntitySequence) row.getValue();
                if (!sequence.isAlreadyInUse()) {
                    String errorMessage = validPrefix(sequence, (String) value);
                    if (errorMessage != null) {
                        throw new WrongValueException(comp, errorMessage);
                    }
                }
            }
        };
    }

    private String validPrefix(EntitySequence sequence, String prefixValue) {
        sequence.setPrefix(prefixValue);
        if (!configurationModel.checkFrefixFormat(sequence)) {
            String message = _("format prefix invalid. It cannot be empty or contain '_' or whitespaces.");
            if (sequence.getEntityName().canContainLowBar()) {
                message = _("format prefix invalid. It cannot be empty or contain whitespaces.");
            }
            return message;
        }
        return null;
    }

    public Constraint checkConstraintNumberOfDigits(){
        return new Constraint(){

            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Row row = (Row) comp.getParent();
                EntitySequence sequence = (EntitySequence) row.getValue();
                if (!sequence.isAlreadyInUse()) {
                    Integer numberOfDigits = (Integer) value;
                    try {
                    sequence.setNumberOfDigits(numberOfDigits);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(comp, e.getMessage());
                    }
                }
            }
        };
    }

    public void addEntitySequence(EntityNameEnum entityName, String prefix,
            Integer digits) {
        configurationModel.addEntitySequence(entityName, prefix, digits);
        onOpenGroup(entityName, true);
        reloadEntitySequences();
    }

    public List<EntitySequence> getEntitySequences(EntityNameEnum entityName) {
        return configurationModel.getEntitySequences(entityName);
    }

    private boolean isLastOne(EntitySequence sequence) {
        return (getEntitySequences(sequence.getEntityName()).size() == 1);
    }

    private void showMessageNotDelete() {
        try {
            Messagebox
                    .show(
                            _("It can not be deleted. At least one sequence is necessary."),
                            _("Deleting sequence"), Messagebox.OK,
                            Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
        }
    }

    public static class EntitySequenceComparator implements
            Comparator<EntitySequence> {

        @Override
        public int compare(EntitySequence seq1, EntitySequence seq2) {
            return seq1.getEntityName().compareTo(seq2.getEntityName());
        }
    }

    public ArrayGroupsModel getEntitySequenceModel() {
        return new ArrayGroupsModel(getAllEntitySequences().toArray(),
                new EntitySequenceComparator());
    }

    public EntitySequenceGroupRenderer getEntitySequenceGroupRenderer() {
        return new EntitySequenceGroupRenderer();
    }

    private List<EntitySequence> getAllEntitySequences() {
        List<EntitySequence> allSequences = new ArrayList<EntitySequence>();
        for (final EntityNameEnum entityName : EntityNameEnum.values()) {
            allSequences.addAll(this.getEntitySequences(entityName));
        }
        return allSequences;
    }

    public void addNewEntitySequence() {
        if (entityCombo != null && numDigitBox != null) {
            if (entityCombo.getSelectedItem() == null) {
                throw new WrongValueException(entityCombo,
                        _("Select entity, please"));
            }

            if (prefixBox.getValue() == null || prefixBox.getValue().isEmpty()) {
                throw new WrongValueException(prefixBox,
                        _("cannot be null or empty"));
            }

            try {
                addEntitySequence((EntityNameEnum) entityCombo
                        .getSelectedItem().getValue(), prefixBox.getValue(),
                        numDigitBox.getValue());
            } catch (IllegalArgumentException e) {
                throw new WrongValueException(numDigitBox, e.getMessage());
            }
        }
    }

    public EntityNameEnum[] getEntityNames() {
        return EntityNameEnum.values();
    }

    public boolean moreScenariosThanMasterCreated() {
        return configurationModel.moreScenariosThanMasterCreated();
    }

    public boolean isChangedDefaultPasswdAdmin() {
        return configurationModel.isChangedDefaultPasswdAdmin();
    }

}