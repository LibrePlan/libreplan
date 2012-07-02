/*
 * This file is part of LibrePlan
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

package org.libreplan.web.workreports;

import static org.libreplan.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.workreports.entities.HoursManagementEnum;
import org.libreplan.business.workreports.entities.PositionInWorkReportEnum;
import org.libreplan.business.workreports.entities.WorkReportLabelTypeAssigment;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.EnumsListitemRenderer;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.Autocomplete;
import org.libreplan.web.common.components.NewDataSortableGrid;
import org.libreplan.web.common.entrypoints.EntryPointsHandler;
import org.libreplan.web.common.entrypoints.IURLHandlerRegistry;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link WorkReportType}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class WorkReportTypeCRUDController extends BaseCRUDController<WorkReportType>
        implements IWorkReportTypeCRUDControllerEntryPoints {

     private static final org.apache.commons.logging.Log LOG = LogFactory
     .getLog(WorkReportTypeCRUDController.class);

    private NewDataSortableGrid listDescriptionFields;

    private NewDataSortableGrid listWorkReportLabelTypeAssigments;

    private Textbox name;

    private Textbox code;

    private Vbox orderedListFieldsAndLabels;

    private Tab tabSortedLabelsAndFields;

    private Tab tabReportStructure;

    private Component containerMessageSortedLabelsAndFields;

    private IMessagesForUser messagesForUserSortedLabelsAndFields;

    private IWorkReportTypeModel workReportTypeModel;

    private IWorkReportCRUDControllerEntryPoints workReportCRUD;

    private IURLHandlerRegistry URLHandlerRegistry;

    private DescriptionFieldRowRenderer descriptionFieldRowRenderer = new DescriptionFieldRowRenderer();

    private WorkReportLabelTypeAssigmentRowRenderer workReportLabelTypeAssigmentRowRenderer = new WorkReportLabelTypeAssigmentRowRenderer();

    private OrderedFieldsAndLabelsRowRenderer orderedFieldsAndLabesRowRenderer = new OrderedFieldsAndLabelsRowRenderer();

    public List<WorkReportType> getWorkReportTypes() {
        return workReportTypeModel.getWorkReportTypesExceptMonthlyTimeSheets();
    }

    public WorkReportType getWorkReportType() {
        return workReportTypeModel.getWorkReportType();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        final EntryPointsHandler<IWorkReportTypeCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkReportTypeCRUDControllerEntryPoints.class);
        handler.register(this, page);
    }

    @Override
    protected void save() throws ValidationException {
        workReportTypeModel.save();
    }

    @Override
    protected void beforeSaving() throws ValidationException {
        isAllValid();
    }

    private boolean thereAreWorkReportsFor(WorkReportType workReportType) {
        return workReportTypeModel.thereAreWorkReportsFor(workReportType);
    }

    public boolean isReadOnly() {
        return thereAreWorkReportsFor(getWorkReportType());
    }

    public boolean isEditable() {
        return (!thereAreWorkReportsFor(getWorkReportType()));
    }

    private void loadComponents(Window window) {
        if (!window.equals(listWindow)) {
            name = (Textbox) window.getFellow("name");
            code = (Textbox) window.getFellow("code");
            listDescriptionFields = (NewDataSortableGrid) window
                    .getFellow("listDescriptionFields");
            listWorkReportLabelTypeAssigments = (NewDataSortableGrid) window
                    .getFellow("listWorkReportLabelTypeAssigments");
            orderedListFieldsAndLabels = (Vbox) window
                    .getFellow("orderedListFieldsAndLabels");
            containerMessageSortedLabelsAndFields = (Vbox) window
                    .getFellow("containerMessageSortedLabelsAndFields");
            messagesForUserSortedLabelsAndFields = new MessagesForUser(
                    containerMessageSortedLabelsAndFields);
            tabSortedLabelsAndFields = (Tab) window
                    .getFellow("tabSortedLabelsAndFields");
            tabReportStructure = (Tab) window.getFellow("tabReportStructure");
        }
    }

    public void goToEditNewWorkReportForm(WorkReportType workReportType) {
        workReportCRUD.goToCreateForm(workReportType);
    }

    /* Operations to manage the description fiels of the edited workReportType */

    public List<DescriptionField> getDescriptionFields() {
        return workReportTypeModel.getDescriptionFields();
    }

    public void addNewDescriptionField() {
        workReportTypeModel.addNewDescriptionField();
        Util.reloadBindings(listDescriptionFields);
    }

    private void removeDescriptionField(DescriptionField descriptionField) {
        workReportTypeModel.removeDescriptionField(descriptionField);
        Util.reloadBindings(listDescriptionFields);
    }

    public DescriptionFieldRowRenderer getDescriptionFieldsRowRender() {
        return descriptionFieldRowRenderer;
    }

    public class DescriptionFieldRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) {

            final DescriptionField descriptionField = (DescriptionField) data;
            row.setValue(descriptionField);

            if (isReadOnly()) {
                appendLabelNameDescriptionField(row);
                appendLabelLengthDescriptionField(row);
                appendLabelPositionDescriptionField(row);
            } else {
                appendTextboBoxNameDescriptionField(row);
                appendIntBoxLengthDescriptionField(row);
                appendListboxPositionDescriptionField(row);
                appendRemoveButtonDescriptionField(row);
            }
        }
    }

    private void appendLabelNameDescriptionField(Row row) {
        org.zkoss.zul.Label labelName = new org.zkoss.zul.Label();
        labelName.setValue(((DescriptionField) row.getValue()).getFieldName());
        labelName.setParent(row);
    }

    private void appendLabelLengthDescriptionField(Row row) {
        org.zkoss.zul.Label labelLength = new org.zkoss.zul.Label();
        labelLength.setValue(((DescriptionField) row.getValue()).getLength()
                .toString());
        labelLength.setParent(row);
    }

    private void appendLabelPositionDescriptionField(Row row) {
        org.zkoss.zul.Label labelPosition = new org.zkoss.zul.Label();
        labelPosition.setParent(row);

        if (workReportTypeModel
                .isHeadingDescriptionField((DescriptionField) row.getValue())) {
            labelPosition.setValue(_(PositionInWorkReportEnum.HEADING
                    .toString()));
        } else {
            labelPosition.setValue(_(PositionInWorkReportEnum.LINE.toString()));
        }
    }

    private void appendTextboBoxNameDescriptionField(final Row row) {
        Textbox boxName = new Textbox();
        boxName.setHflex("1");
        boxName.setParent(row);
        boxName
                .setConstraint(validateIfExistTheSameFieldName((DescriptionField) row
                        .getValue()));

        Util.bind(boxName, new Util.Getter<String>() {
            @Override
            public String get() {
                return ((DescriptionField) row.getValue()).getFieldName();
            }
        }, new Util.Setter<String>() {

            @Override
            public void set(String value) {
                ((DescriptionField) row.getValue()).setFieldName(value);
            }
        });
    }

    private void appendIntBoxLengthDescriptionField(final Row row) {
        Intbox boxLength = new Intbox();
        boxLength.setHflex("1");
        boxLength.setReadonly(isReadOnly());
        boxLength.setParent(row);
        boxLength.setConstraint("no negative, no zero");

        Util.bind(boxLength, new Util.Getter<Integer>() {
            @Override
            public Integer get() {
                return ((DescriptionField) row.getValue()).getLength();
            }
        }, new Util.Setter<Integer>() {

            @Override
            public void set(Integer value) {
                ((DescriptionField) row.getValue()).setLength(value);
            }
        });
    }

    private void appendListboxPositionDescriptionField(final Row row) {
        final DescriptionField descriptionField = (DescriptionField) row
                .getValue();
        final Listbox listPosition = createListPosition();
        listPosition.setParent(row);

        if (workReportTypeModel.isHeadingDescriptionField(descriptionField)) {
            listPosition.setSelectedItem(listPosition.getItemAtIndex(0));
        } else {
            listPosition.setSelectedItem(listPosition.getItemAtIndex(1));
        }

        listPosition.addEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) {
                changePositionDescriptionField(listPosition
                            .getSelectedItem(), descriptionField);
                Util.reloadBindings(listDescriptionFields);
            }
        });
    }

    private void appendRemoveButtonDescriptionField(final Row row) {
        final DescriptionField descriptionField = (DescriptionField) row
                .getValue();
        final Button removeButton = createRemoveButton();
        removeButton.setParent(row);

        removeButton.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) {
                removeDescriptionField(descriptionField);
            }
        });
    }

    private Listbox createListPosition() {
        final Listbox listPosition = new Listbox();
        listPosition.setMold("select");

        listPosition.setModel(new SimpleListModel(
                getPositionInWorkReportEnums()));
        listPosition.setItemRenderer(new EnumsListitemRenderer());
        return listPosition;
    }

    private Button createRemoveButton() {
        Button removeButton = new Button();
        removeButton.setSclass("icono");
        removeButton.setImage("/common/img/ico_borrar1.png");
        removeButton.setHoverImage("/common/img/ico_borrar.png");
        removeButton.setTooltiptext(_("Delete"));
        return removeButton;
    }

    private void changePositionDescriptionField(Listitem selectedItem,
            DescriptionField descriptionField) {
        PositionInWorkReportEnum newPosition = (PositionInWorkReportEnum) selectedItem
                .getValue();
        workReportTypeModel.changePositionDescriptionField(newPosition,
                descriptionField);
    }

    /* Operations to manage the label of the edited workReportType */

    private Map<LabelType, List<Label>> getMapLabelTypes() {
        return workReportTypeModel.getMapLabelTypes();
    }

    public Set<WorkReportLabelTypeAssigment> getWorkReportLabelTypeAssigments() {
        return workReportTypeModel.getWorkReportLabelTypeAssigments();
    }

    public void addNewWorkReportLabelTypeAssigment() {
        workReportTypeModel.addNewWorkReportLabelTypeAssigment();
        Util.reloadBindings(listWorkReportLabelTypeAssigments);
    }

    private void removeWorkReportLabelTypeAssigment(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        workReportTypeModel
                .removeWorkReportLabelTypeAssigment(workReportLabelTypeAssigment);
        Util.reloadBindings(listWorkReportLabelTypeAssigments);
    }

    public WorkReportLabelTypeAssigmentRowRenderer getWorkReportLabelTypeAssigmentRowRender() {
        return workReportLabelTypeAssigmentRowRenderer;
    }

    public class WorkReportLabelTypeAssigmentRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) {

            final WorkReportLabelTypeAssigment workReportLabelTypeAssigment = (WorkReportLabelTypeAssigment) data;
            row.setValue(workReportLabelTypeAssigment);

            if (isReadOnly()) {
                appendLabelType(row);
                appendLabelPosition(row);
                appendLabel(row);
            } else {
                appendComboboxLabelTypes(row);
                appendComboboxPositionLabel(row);
                appendComboboxLabels(row);
                appendRemoveButtonWorkReportLabelTypeAssigment(row);
            }
        }
    }

    private void appendLabelType(Row row) {
        org.zkoss.zul.Label labelType = new org.zkoss.zul.Label();
        labelType.setParent(row);
        labelType.setValue(((WorkReportLabelTypeAssigment) row.getValue())
                .getLabelType().getName());
    }

    private void appendLabelPosition(Row row) {
        org.zkoss.zul.Label labelPosition = new org.zkoss.zul.Label();
        labelPosition.setParent(row);
        labelPosition.setValue(_(workReportTypeModel.getLabelAssigmentPosition(
                (WorkReportLabelTypeAssigment) row.getValue()).toString()));
    }

    private void appendLabel(Row row) {
        org.zkoss.zul.Label label = new org.zkoss.zul.Label();
        label.setParent(row);
        label.setValue(((WorkReportLabelTypeAssigment) row.getValue())
                .getDefaultLabel().getName());
    }

    private void appendComboboxLabelTypes(final Row row) {
        final WorkReportLabelTypeAssigment workReportLabelTypeAssigment = (WorkReportLabelTypeAssigment) row
                .getValue();

        final Combobox comboLabelTypes = createComboboxLabelTypes(workReportLabelTypeAssigment);
        comboLabelTypes.setParent(row);

        comboLabelTypes.addEventListener(Events.ON_CHANGE, new EventListener() {
            @Override
            public void onEvent(Event event) {
                changeLabelType(comboLabelTypes.getSelectedItem(),
                        workReportLabelTypeAssigment);
                validateIfExistTheSameLabelType(comboLabelTypes,
                        workReportLabelTypeAssigment);
                Util.reloadBindings(listWorkReportLabelTypeAssigments);
            }
        });

    }

    private void appendComboboxLabels(final Row row) {
        final WorkReportLabelTypeAssigment workReportLabelTypeAssigment = (WorkReportLabelTypeAssigment) row
                .getValue();
        Comboitem selectedItemType = ((Autocomplete) row.getFirstChild())
                .getSelectedItem();

        LabelType selectedLabelType = null;
        if (selectedItemType != null) {
            selectedLabelType = (LabelType) selectedItemType.getValue();
        }

        final Combobox comboLabels = createComboboxLabels(selectedLabelType,
                workReportLabelTypeAssigment);
        comboLabels.setParent(row);

        comboLabels.addEventListener(Events.ON_CHANGE, new EventListener() {
            @Override
            public void onEvent(Event event) {
                changeLabel(comboLabels.getSelectedItem(),
                        workReportLabelTypeAssigment);
                Util.reloadBindings(listWorkReportLabelTypeAssigments);
            }
        });
    }

    private void appendComboboxPositionLabel(final Row row) {
        final WorkReportLabelTypeAssigment workReportLabelTypeAssigment = (WorkReportLabelTypeAssigment) row
                .getValue();

        final Listbox listPosition = this.createListPosition();
        listPosition.setParent(row);

        if (workReportLabelTypeAssigment.getLabelsSharedByLines()) {
            listPosition.setSelectedItem(listPosition.getItemAtIndex(0));
        } else {
            listPosition.setSelectedItem(listPosition.getItemAtIndex(1));
        }

        listPosition.addEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) {
                changePositionLabel(listPosition.getSelectedItem(),
                            workReportLabelTypeAssigment);
                Util.reloadBindings(listWorkReportLabelTypeAssigments);
            }
        });

    }

    private void appendRemoveButtonWorkReportLabelTypeAssigment(final Row row) {
        final WorkReportLabelTypeAssigment workReportLabelTypeAssigment = (WorkReportLabelTypeAssigment) row
                .getValue();
        final Button removeButton = createRemoveButton();
        removeButton.setParent(row);

        removeButton.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) {
                removeWorkReportLabelTypeAssigment(workReportLabelTypeAssigment);
            }
        });
    }

    private Autocomplete createComboboxLabelTypes(
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        Autocomplete comboLabelTypes = new Autocomplete();
        comboLabelTypes.setButtonVisible(true);

        final Set<LabelType> listLabelType = getMapLabelTypes().keySet();
        for (LabelType labelType : listLabelType) {
            Comboitem comboItem = new Comboitem();
            comboItem.setValue(labelType);
            comboItem.setLabel(labelType.getName());
            comboItem.setParent(comboLabelTypes);

            if ((workReportLabelTypeAssigment.getLabelType() != null)
                    && (workReportLabelTypeAssigment.getLabelType()
                            .equals(labelType))) {
                comboLabelTypes.setSelectedItem(comboItem);
            }
        }
        return comboLabelTypes;
    }

    private Autocomplete createComboboxLabels(LabelType labelType,
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        Autocomplete comboLabels = new Autocomplete();
        comboLabels.setButtonVisible(true);

        if (labelType != null) {
            final List<Label> listLabel = this.getMapLabelTypes()
                    .get(labelType);
            for (Label label : listLabel) {
                Comboitem comboItem = new Comboitem();
                comboItem.setValue(label);
                comboItem.setLabel(label.getName());
                comboItem.setParent(comboLabels);

                if ((workReportLabelTypeAssigment.getDefaultLabel() != null)
                        && (workReportLabelTypeAssigment.getDefaultLabel()
                                .equals(label))) {
                    comboLabels.setSelectedItem(comboItem);
                }
            }
        }
        return comboLabels;
    }

    private void changePositionLabel(Listitem selectedItem,
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        PositionInWorkReportEnum newPosition = (PositionInWorkReportEnum) selectedItem
                .getValue();
        workReportTypeModel.setLabelAssigmentPosition(
                workReportLabelTypeAssigment, newPosition);
    }

    private void changeLabelType(Comboitem selectedItem,
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        LabelType labelType = null;
        if (selectedItem != null) {
            labelType = (LabelType) selectedItem.getValue();
        }
        workReportLabelTypeAssigment.setLabelType(labelType);
        workReportLabelTypeAssigment.setDefaultLabel(null);
    }

    private void changeLabel(Comboitem selectedItem,
            WorkReportLabelTypeAssigment workReportLabelTypeAssigment) {
        Label defaultLabel = null;
        if (selectedItem != null) {
            defaultLabel = (Label) selectedItem.getValue();
        }
        workReportLabelTypeAssigment.setDefaultLabel(defaultLabel);
    }

    /* Operations to manage the requiremts fields */

    public HoursManagementEnum[] getHoursManagementEnums() {
        return HoursManagementEnum.values();
    }

    public PositionInWorkReportEnum[] getPositionInWorkReportEnums() {
        return PositionInWorkReportEnum.values();
    }

    public PositionInWorkReportEnum getDatePosition() {
        return workReportTypeModel.getDatePosition();
    }

    public void setDatePosition(PositionInWorkReportEnum position) {
        workReportTypeModel.setDatePosition(position);
    }

    public PositionInWorkReportEnum getResourcePosition() {
        return workReportTypeModel.getResourcePosition();
    }

    public void setResourcePosition(PositionInWorkReportEnum position) {
        workReportTypeModel.setResourcePosition(position);
    }

    public PositionInWorkReportEnum getOrderElementPosition() {
        return workReportTypeModel.getOrderElementPosition();
     }

    public void setOrderElementPosition(PositionInWorkReportEnum position) {
        workReportTypeModel.setOrderElementPosition(position);
    }

    /* Operations to the data validations */

    public Constraint validateWorkReportTypeName() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                try {
                    workReportTypeModel
                            .validateWorkReportTypeName((String) value);
                } catch (IllegalArgumentException e) {
                    throw new WrongValueException(comp, _(e.getMessage()));
                }
            }
        };
    }

    public Constraint validateWorkReportTypeCode() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                try {
                    workReportTypeModel
                            .validateWorkReportTypeCode((String) value);
                } catch (IllegalArgumentException e) {
                    throw new WrongValueException(comp, _(e.getMessage()));
                }
            }
        };
    }

    public void validateIfExistTheSameLabelType(final Combobox comboLabelTypes,
            final WorkReportLabelTypeAssigment workReportLabelTypeAssigment)
            throws WrongValueException {
        if ((getWorkReportType() != null)
                && (getWorkReportType()
                        .existRepeatedLabelType(workReportLabelTypeAssigment))) {
            workReportLabelTypeAssigment.setLabelType(null);
            throw new WrongValueException(
                    comboLabelTypes,
                    _("Label type already assigned"));
        }
    }

    public Constraint validateIfExistTheSameFieldName(
            final DescriptionField descriptionField) {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                descriptionField.setFieldName((String) value);
                if ((getWorkReportType() != null)
                        && (getWorkReportType()
                                .existSameFieldName(descriptionField))) {
                    descriptionField.setFieldName(null);
                    throw new WrongValueException(
                            comp,
                            _("A description field with the same name already exists."));
                }
            }
        };
    }

    private boolean isAllValid() {
        // validate workReportType name
        if (!name.isValid()) {
            selectTab(tabReportStructure);
            showInvalidWorkReportTypeName();
            return false;
        }

        if (!code.isValid()) {
            selectTab(tabReportStructure);
            showInvalidWorkReportTypeCode();
            return false;
        }

        // validate the descriptionFields and the WorkReportLabelTypeAssigments
        if (!((validateDescriptionFields()) && (validateWorkReportLabelTypeAssigments()))) {
            return false;
        }
        return validateIndexLabelsAndFields();
    }

    private boolean validateDescriptionFields() {
        DescriptionField descriptionField = workReportTypeModel
                .validateFieldNameLineFields();
        if (descriptionField != null) {
            selectTab(tabReportStructure);
            showInvalidDescriptionFieldName(descriptionField);
            return false;
        }

        descriptionField = workReportTypeModel.validateLengthLineFields();
        if (descriptionField != null) {
            selectTab(tabReportStructure);
            showInvalidDescriptionFieldLength(descriptionField);
            return false;
        }
        return true;
    }

    private boolean validateWorkReportLabelTypeAssigments() {
        WorkReportLabelTypeAssigment labelTypeAssigment = workReportTypeModel
                .validateLabelTypes();
        if (labelTypeAssigment != null) {
            selectTab(tabReportStructure);
            String errorMessage = "The label type must unique and not null.";
            showInvalidWorkReportLabelTypeAssigment(0, errorMessage,
                    labelTypeAssigment);
            return false;
        }

        WorkReportLabelTypeAssigment labelAssigment = workReportTypeModel
                .validateLabels();
        if (labelAssigment != null) {
            selectTab(tabReportStructure);
            String errorMessage = "The label must not null.";
            showInvalidWorkReportLabelTypeAssigment(2, errorMessage,
                    labelAssigment);
            return false;
        }
        return true;
    }

    private boolean validateIndexLabelsAndFields() {
        if (!workReportTypeModel.validateTheIndexFieldsAndLabels()) {
            selectTab(tabSortedLabelsAndFields);
            showMessageSortedLabelsAndFields();
            return false;
        }
        return true;
    }

    private void showInvalidWorkReportTypeName() {
        try {
            workReportTypeModel.validateWorkReportTypeName(name.getValue());
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(name, _(e.getMessage()));
        }
    }

    private void showInvalidWorkReportTypeCode() {
        try {
            workReportTypeModel.validateWorkReportTypeCode(code.getValue());
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(code, _(e.getMessage()));
        }
    }

    private void showInvalidDescriptionFieldName(DescriptionField field) {
        // Find which row contains the description field inside grid
        Row row = findRowByValue(listDescriptionFields.getRows(), field);
            Textbox fieldName = (Textbox) row.getFirstChild();
        throw new WrongValueException(fieldName,
                _("The field name must be unique and not empty"));
    }

    private void showInvalidDescriptionFieldLength(DescriptionField field) {
        // Find which row contains the description field inside grid
        Row row = findRowByValue(listDescriptionFields.getRows(), field);
        Intbox fieldName = (Intbox) row.getChildren().get(1);
        throw new WrongValueException(fieldName,
                _("The length must be greater than 0 and not empty"));
    }

    private void showInvalidWorkReportLabelTypeAssigment(int combo,
            String message, WorkReportLabelTypeAssigment labelType) {
        Row row = findRowByValue(listWorkReportLabelTypeAssigments.getRows(),
                labelType);
        Combobox comboLabelType = (Combobox) row.getChildren().get(combo);
        throw new WrongValueException(comboLabelType, _(message));
    }

    private Row findRowByValue(Rows rows, Object value) {
        List<Row> listRows = (List<Row>) rows.getChildren();
        for (Row row : listRows) {
            if (value.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

    /* Operations to manage the ordered list of fields and labels */

    public void reloadOrderedListFieldsAndLabels() {
        Util.reloadBindings(orderedListFieldsAndLabels);
    }

    public List<Object> headingFieldsAndLabels(){
        return workReportTypeModel.getOrderedListHeading();
    }

    public List<Object> linesFieldsAndLabels() {
        return workReportTypeModel.getOrderedListLines();
    }

    public OrderedFieldsAndLabelsRowRenderer getOrderedFieldsAndLabelsRowRenderer() {
        return orderedFieldsAndLabesRowRenderer;
    }

    public class OrderedFieldsAndLabelsRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) {
            row.setValue(data);

            String name = _("Unallocated name");
            String type;
            if (data instanceof DescriptionField) {
                if ((((DescriptionField) data).getFieldName() != null)
                        || (!((DescriptionField) data).getFieldName().isEmpty())) {
                    name = ((DescriptionField) data).getFieldName();
                }
                type = _("Text field");
            } else {
                if((((WorkReportLabelTypeAssigment) data)
                        .getLabelType() != null)
                        && (((WorkReportLabelTypeAssigment) data)
                                .getDefaultLabel() != null)) {
                    String labelType = ((WorkReportLabelTypeAssigment) data)
                        .getLabelType().getName();
                    String label = ((WorkReportLabelTypeAssigment) data)
                        .getDefaultLabel().getName();
                    name = labelType + " :: " + label;
                }
                type = _("Label");
            }

            appendNewLabel(row, name);
            appendNewLabel(row, type);
            appendOperationsFieldOrLabel(row);
        }
    }

    private void appendNewLabel(Row row, String label) {
        org.zkoss.zul.Label labelName = new org.zkoss.zul.Label();
        labelName.setParent(row);
        labelName.setValue(label);
    }

    private void appendOperationsFieldOrLabel(final Row row) {
        Hbox hbox = new Hbox();
        hbox.setParent(row);

        Button downbutton = new Button("", "/common/img/ico_subir1.png");
        downbutton.setHoverImage("/common/img/ico_subir.png");
        downbutton.setParent(hbox);
        downbutton.setSclass("icono");
        downbutton.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) {
                boolean intoHeading = intoHeading(row.getGrid());
                workReportTypeModel.upFieldOrLabel(row.getValue(), intoHeading);
                Util.reloadBindings(row.getGrid());
            }
        });

        Button upbutton = new Button("", "/common/img/ico_bajar1.png");
        upbutton.setHoverImage("/common/img/ico_bajar.png");
        upbutton.setParent(hbox);
        upbutton.setSclass("icono");
        upbutton.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) {
                boolean intoHeading = intoHeading(row.getGrid());
                workReportTypeModel.downFieldOrLabel(row.getValue(),
                        intoHeading);
                Util.reloadBindings(row.getGrid());
            }
        });

    }

    private boolean intoHeading(Grid grid) {
        if (grid.getId().equals("linesFieldsAndLabels")) {
            return false;
        } else {
            return true;
        }
    }

    private void showMessageSortedLabelsAndFields() {
        reloadOrderedListFieldsAndLabels();
        if (messagesForUserSortedLabelsAndFields != null) {
            messagesForUserSortedLabelsAndFields
                    .showMessage(
                            Level.ERROR,
                            _("Index fields and labels must be unique and consecutive"));
        }
    }

    private void selectTab(Tab tab) {
        if (tab != null) {
            tab.setSelected(true);
        }
    }

    public void sortWorkReportTypes() {
        Column columnName = (Column) listWindow
                .getFellow("workReportTypeName");
        if (columnName != null) {
            if (columnName.getSortDirection().equals("ascending")) {
                columnName.sort(false, false);
                columnName.setSortDirection("ascending");
            } else if (columnName.getSortDirection().equals("descending")) {
                columnName.sort(true, false);
                columnName.setSortDirection("descending");
            }
        }
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            // we have to auto-generate the code for new objects
            try {
                workReportTypeModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
        }
        Util.reloadBindings(code);
    }

    @Override
    protected String getEntityType() {
        return _("Timesheets Template");
    }

    @Override
    protected String getPluralEntityType() {
        return _("Timesheets Templates");
    }

    @Override
    protected void initCreate() {
        workReportTypeModel.prepareForCreate();
        loadComponents(editWindow);
    }

    @Override
    protected void initEdit(WorkReportType workReportType) {
        workReportTypeModel.initEdit(workReportType);
        loadComponents(editWindow);
    }

    @Override
    protected WorkReportType getEntityBeingEdited() {
        return workReportTypeModel.getWorkReportType();
    }

    @Override
    protected boolean beforeDeleting(WorkReportType workReportType) {
        if (thereAreWorkReportsFor(workReportType)) {
            try {
                Messagebox
                        .show(_("Cannot delete timesheet template. There are some timesheets bound to it."),
                                _("Warning"), Messagebox.OK, Messagebox.EXCLAMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        return true;
    }

    @Override
    protected void delete(WorkReportType workReportType)
            throws InstanceNotFoundException {
        workReportTypeModel.confirmRemove(workReportType);
        final Grid workReportTypes = (Grid) listWindow.getFellowIfAny("listing");
        if (workReportTypes != null) {
            Util.reloadBindings(workReportTypes);
        }
    }
}
