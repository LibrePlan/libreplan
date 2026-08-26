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

package org.libreplan.web.costcategories;

import static org.libreplan.web.I18nHelper._t;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.HourCost;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;

/**
 * Controller for CRUD actions over a {@link CostCategory}.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@SuppressWarnings("serial")
public class CostCategoryCRUDController extends BaseCRUDController<CostCategory> {

    private ICostCategoryModel costCategoryModel;

    private Grid listHourCosts;

    private Grid listCostCategories;

    private List<TypeOfWorkHours> allHoursType;

    private HourCostListRenderer hourCostListRenderer = new HourCostListRenderer();

    public CostCategoryCRUDController() {
        if ( costCategoryModel == null ) {
            costCategoryModel = (ICostCategoryModel) SpringUtil.getBean("costCategoryModel");
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        listHourCosts = (Grid) editWindow.getFellowIfAny("listHourCosts");
        listCostCategories = (Grid) listWindow.getFellowIfAny("listing");

        // Renders grid and disables delete button in case it cannot be removed
        listCostCategories.addEventListener("onInitRender", new EventListener() {

            @Override
            public void onEvent(Event event) {
                listCostCategories.renderAll();

                final Rows rows = listCostCategories.getRows();
                for (Iterator i = rows.getChildren().iterator(); i.hasNext(); ) {
                    final Row row = (Row) i.next();
                    final CostCategory category = row.getValue();
                    Button btnDelete = (Button) row.getChildren().get(2).getChildren().get(1);
                    if (!canRemoveCostCategory(category)) {
                        btnDelete.setDisabled(true);
                        btnDelete.setImage("/common/img/ico_borrar_out.png");
                        btnDelete.setHoverImage("/common/img/ico_borrar_out.png");
                    }
                }
            }
        });
        initializeHoursType();

        // BaseCRUDController.doAfterCompose() already calls showListWindow() (which reloads
        // listWindow's bindings), but that happens before AnnotateBinderInit's own later
        // page-level pass has created any binder at all, so it's a no-op. Redo it here, now that
        // the binder exists. Only listWindow is reloaded - editWindow's bindings reference
        // controller.costCategory, which is still null at this point (only set up by
        // initCreate()/initEdit(), called later); showEditWindow() reloads editWindow's own
        // bindings once that has happened.
        Util.createBindingsFor(comp);
        Util.reloadBindings(listWindow);
    }

    private void initializeHoursType() {
        allHoursType = costCategoryModel.getAllHoursType();
    }

    /**
     * IntegrationEntity.isCodeAutogenerated() returns a boxed Boolean - ZK's BeanELResolver only
     * recognizes the "is"-prefix getter convention for properties declared as primitive boolean,
     * so binding directly to it throws PropertyNotFoundException. This thin wrapper (with matching
     * setter for @save) exposes a primitive boolean instead.
     */
    public boolean isCostCategoryCodeAutogenerated() {
        return getCostCategory().isCodeAutogenerated();
    }

    public void setCostCategoryCodeAutogenerated(boolean codeAutogenerated) {
        getCostCategory().setCodeAutogenerated(codeAutogenerated);
    }

    @Override
    protected void save() throws ValidationException{
        costCategoryModel.confirmSave();
    }

    @Override
    protected void beforeSaving() throws ValidationException {
        costCategoryModel.validateHourCostsOverlap();
    }

    private void showInvalidValues(ValidationException e) {
        Object value = e.getInvalidValue().getRootBean();
        if (value instanceof HourCost) {
            showInvalidValue((HourCost) value);
        }
        messagesForUser.showInvalidValues(e);
    }

    private void showInvalidValue(HourCost hourCost) {
        Row row = ComponentsFinder.findRowByValue(listHourCosts, hourCost);
        if (row != null) {
            if (hourCost.getType() == null) {
                Listbox workHoursType = getWorkHoursType(row);

                String message = workHoursType.getItems().isEmpty()
                        ? _t("Hours types are empty. Please, create some hours types before proceeding")
                        : _t("cannot be empty");

                throw new WrongValueException(getWorkHoursType(row), message);
            }
            if (hourCost.getPriceCost() == null) {
                throw new WrongValueException(getPricePerHour(row), _t("cannot be empty"));
            }
        }
    }

    private Listbox getWorkHoursType(Row row) {
        return (Listbox) row.getChildren().get(1);
    }

    private Component getPricePerHour(Row row) {
        return row.getChildren().get(2);
    }

    public CostCategory getCostCategory() {
        return costCategoryModel.getCostCategory();
    }

    public List<CostCategory> getCostCategories() {
        // The grid's "model" property is declared as org.zkoss.zul.ListModel - AnnotateBinder has
        // no automatic List->ListModel coercion, so returning a plain List here throws a
        // ClassCastException when the binder tries to load it.
        return new org.zkoss.zul.ListModelList<>(costCategoryModel.getCostCategories());
    }

    /**
     * The list's rows used to be declared with a ZUML "each" template (self="@{each=...}") - under
     * this app's AnnotateBinder/ZK 10 stack that only ever clones the row's FIRST child for each
     * iteration. Building rows programmatically via RowRenderer sidesteps that "each" bug entirely.
     * Child order (Label, Checkbox, Hbox[edit,delete]) is kept the same as before so the
     * canRemoveCostCategory disable-button logic in doAfterCompose's onInitRender listener
     * (which indexes into row children) keeps working unchanged.
     */
    public RowRenderer getCostCategoryRenderer() {
        return (row, data, index) -> {
            final CostCategory costCategory = (CostCategory) data;
            row.setValue(costCategory);
            row.addEventListener(Events.ON_CLICK, event -> goToEditForm(costCategory));

            row.appendChild(new org.zkoss.zul.Label(costCategory.getName()));

            org.zkoss.zul.Checkbox enabled = new org.zkoss.zul.Checkbox();
            enabled.setChecked(costCategory.getEnabled());
            enabled.setDisabled(true);
            row.appendChild(enabled);

            org.zkoss.zul.Hbox hbox = new org.zkoss.zul.Hbox();
            hbox.appendChild(Util.createEditButton(event -> goToEditForm(costCategory)));
            hbox.appendChild(Util.createRemoveButton(event -> confirmDelete(costCategory)));
            row.appendChild(hbox);
        };
    }

    public List<HourCost> getHourCosts() {
        // The grid's "model" property is declared as org.zkoss.zul.ListModel - AnnotateBinder has
        // no automatic coercion from Set (or List) to ListModel, so wrap it explicitly.
        return new ListModelList<>(costCategoryModel.getHourCosts());
    }


    private TypeOfWorkHours getTypeOfWorkHours(Row listitem) {
        HourCost hourCost = listitem.getValue();
        return hourCost.getType();
    }

    /**
     * Append a textbox code to row.
     *
     * @param row
     */
    private void appendTextboxCode(final Row row) {
        final HourCost hourCost = row.getValue();
        final Textbox txtCode = new Textbox();
        txtCode.setWidth("200px");
        if (hourCost != null) {
            CostCategory costCategory = hourCost.getCategory();
            txtCode.setDisabled(costCategory.isCodeAutogenerated());

            Util.bind(
                    txtCode,
                    new Util.Getter<String>() {
                        @Override
                        public String get() {
                            return hourCost.getCode();
                        }
                    },
                    new Util.Setter<String>() {
                        @Override
                        public void set(String value) {
                            hourCost.setCode(value);
                        }
                    });

            if (!hourCost.getCategory().isCodeAutogenerated()) {
                txtCode.setConstraint("no empty:"
                        + _t("cannot be empty"));
            } else {
                txtCode.setConstraint("");
            }
        }
        row.appendChild(txtCode);
    }

    /**
     * Append Selectbox of {@link TypeOfWorkHours} to row.
     *
     * @param row
     */
    private void appendHoursType(final Row row) {
        final HourCost hourCost = row.getValue();
        final Listbox lbHoursType = new Listbox();
        lbHoursType.setMold("select");

        if ( allHoursType.isEmpty() ) {
            lbHoursType.setModel(new SimpleListModel<>(allHoursType));
            lbHoursType.renderAll();
            lbHoursType.applyProperties();
            row.appendChild(lbHoursType);

            return;
        }

        // Pick which TypeOfWorkHours should be pre-selected: the existing value if the line
        // already has one, otherwise (for a brand new row) the first available option.
        // Compared by name (like ComponentsFinder.findItemByValue below does), not equals():
        // TypeOfWorkHours has no equals()/hashCode() override, and `type` here almost never is
        // the exact same object instance as its counterpart in the shared `allHoursType` list
        // (different query/association), so reference-equality would never match.
        TypeOfWorkHours type = hourCost.getType();
        boolean isNewSelection = hourCost.isNewObject() && type == null;
        final String nameToSelect = isNewSelection ? allHoursType.get(0).getName() : type.toString();

        // A fresh ListModel per row is required here, not a single shared instance across every
        // row's Listbox: org.zkoss.zul.AbstractListModel (the base of SimpleListModel)
        // implements Selectable and stores the current selection INSIDE the model instance
        // itself. Sharing one SimpleListModel across every row's Listbox means selecting an item
        // in any row mutates that one shared model's selection, which every other row's Listbox
        // (bound to the same instance) then reflects too - see WorkReportCRUDController's
        // near-identical appendHoursType() for the same bug, found and fixed first.
        //
        // Selection is set via an ItemRenderer (item.setSelected(...) called per-item, as each
        // Listitem is actually created) rather than via a post-hoc
        // Listbox.selectItem()/Listitem.setSelected() call on an already fully-built but
        // still-unattached component tree: that approach leaves the Java-side flag set but the
        // rendered <select> shows no <option selected> - the "select" mold only reflects
        // whichever item the renderer itself marked selected while building each option, not a
        // flag flipped on an existing Listitem afterwards.
        lbHoursType.setModel(new SimpleListModel<>(allHoursType));
        lbHoursType.setItemRenderer((item, data, index) -> {
            TypeOfWorkHours hoursType = (TypeOfWorkHours) data;
            item.setLabel(hoursType.toString());
            item.setValue(hoursType);
            item.setSelected(hoursType.toString().equals(nameToSelect));
        });
        lbHoursType.renderAll();
        lbHoursType.applyProperties();

        if ( isNewSelection ) {
            setHoursType(hourCost, ComponentsFinder.findItemByValue(lbHoursType, allHoursType.get(0)));
        }

        lbHoursType.addEventListener(Events.ON_SELECT, new EventListener() {
            @Override
            public void onEvent(Event event) {
                Listitem item = lbHoursType.getSelectedItem();
                if (item != null) {
                    setHoursType(row.getValue(), item);
                }
            }

        });

        row.appendChild(lbHoursType);
    }

    private void setHoursType(HourCost hourCost, Listitem item) {
        TypeOfWorkHours value = item != null ? (TypeOfWorkHours) item.getValue() : null;
        hourCost.setType(value);
        if (value != null) {
            final BigDecimal defaultPrice = value.getDefaultPrice();
            final Decimalbox dbPricePerHour = (Decimalbox) item.getParent().getNextSibling();
            hourCost.setPriceCost(defaultPrice);
            if (dbPricePerHour != null) {
                dbPricePerHour.setValue(defaultPrice);
            }
        } else if (item != null) {
            hourCost.setPriceCost(BigDecimal.ZERO);
            throw new WrongValueException(item.getParent(), _t("Please, select an item"));
        }
    }

    /**
     * Append a delete {@link Button} to {@link Row}.
     *
     * @param row
     */
    private void appendDeleteButton(final Row row) {
        Button delete = new Button("", "/common/img/ico_borrar1.png");
        delete.setHoverImage("/common/img/ico_borrar.png");
        delete.setSclass("icono");
        delete.setTooltiptext(_t("Delete"));

        delete.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) {
                confirmRemove((HourCost) row.getValue());
            }
        });

        row.appendChild(delete);
    }

    /**
     * Append a Textbox "hour cost" to row.
     *
     * @param row
     */
    private void appendDecimalboxCost(Row row) {
        Decimalbox boxCost = new Decimalbox();
        bindDecimalboxCost(boxCost, row.getValue());
        boxCost.setConstraint("no empty:" + _t("cannot be empty"));
        boxCost.setFormat(Util.getMoneyFormat());
        row.appendChild(boxCost);
    }

    /**
     * Binds Decimalbox "hour cost" to the corresponding attribute of a {@link HourCost}.
     *
     * @param boxCost
     * @param hourCost
     */
    private void bindDecimalboxCost(final Decimalbox boxCost, final HourCost hourCost) {
        Util.bind(
                boxCost,
                new Util.Getter<BigDecimal>() {
                    @Override
                    public BigDecimal get() {
                        if (hourCost.getPriceCost() != null) {
                            return hourCost.getPriceCost();
                        }
                        return new BigDecimal(0);
                    }
                },
                new Util.Setter<BigDecimal>() {
                    @Override
                    public void set(BigDecimal value) {
                        hourCost.setPriceCost(value);
                    }
                });
    }

    /**
     * Append a Datebox "init date" to row.
     *
     * @param row
     */
    private void appendDateboxInitDate(final Row row) {
        Datebox initDateBox = new Datebox();
        bindDateboxInitDate(initDateBox, row.getValue());
        initDateBox.setConstraint("no empty:" + _t("Start date cannot be empty"));
        row.appendChild(initDateBox);

        initDateBox.addEventListener("onChange", new EventListener() {
            @Override
            public void onEvent(Event event) {
                // Updates the constraint of the endDate box with the new date
                LocalDate initDate = ((HourCost)row.getValue()).getInitDate();
                Datebox endDateBox = (Datebox) row.getChildren().get(4);
                endDateBox.setConstraint("after " +
                        String.format("%04d", initDate.getYear()) +
                        String.format("%02d", initDate.getMonthOfYear()) +
                        String.format("%02d", initDate.getDayOfMonth()));
            }
        });
    }

    /**
     * Binds Datebox "init date" to the corresponding attribute of a {@link HourCost}.
     *
     * @param dateBoxInitDate
     * @param hourCost
     */
    private void bindDateboxInitDate(final Datebox dateBoxInitDate, final HourCost hourCost) {
        Util.bind(
                dateBoxInitDate,
                new Util.Getter<Date>() {
                    @Override
                    public Date get() {
                        LocalDate dateTime = hourCost.getInitDate();
                        if (dateTime != null) {
                            /* TODO resolve deprecated */
                            return new Date(dateTime.getYear() - 1900, dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());
                        } else {
                            Date now = new Date();
                            hourCost.setInitDate(new LocalDate(now));
                            return now;
                        }
                    }
                },
                new Util.Setter<Date>() {
                    @Override
                    public void set(Date value) {
                        if (value != null) {
                            /* TODO resolve deprecated */
                            hourCost.setInitDate(new LocalDate(value.getYear() + 1900, value.getMonth() + 1, value.getDate()));
                        }
                        else {
                            hourCost.setInitDate(null);
                        }
                    }
                });
    }

    /**
     * Append a Datebox "end date" to row.
     *
     * @param row
     */
    private void appendDateboxEndDate(Row row) {
        Datebox endDateBox = new Datebox();
        bindDateboxEndDate(endDateBox, row.getValue());
        LocalDate initDate = ((HourCost)row.getValue()).getInitDate();
        if (initDate != null) {
            endDateBox.setConstraint("after " +
                    String.format("%04d", initDate.getYear()) +
                    String.format("%02d", initDate.getMonthOfYear()) +
                    String.format("%02d", initDate.getDayOfMonth()));
        }
        row.appendChild(endDateBox);
    }

    /**
     * Binds Datebox "init date" to the corresponding attribute of a {@link HourCost}.
     *
     * @param dateBoxEndDate
     * @param hourCost
     */
    private void bindDateboxEndDate(final Datebox dateBoxEndDate, final HourCost hourCost) {
        Util.bind(
                dateBoxEndDate,
                new Util.Getter<Date>() {
                    @Override
                    public Date get() {
                        LocalDate dateTime = hourCost.getEndDate();
                        if (dateTime != null) {
                            /* TODO resolve deprecated */
                            return new Date(
                                    dateTime.getYear() - 1900, dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());
                        }
                        return null;
                    }
                },
                new Util.Setter<Date>() {
                    @Override
                    public void set(Date value) {
                        if (value != null) {
                            /* TODO resolve deprecated */
                            hourCost.setEndDate(
                                    new LocalDate(value.getYear() + 1900, value.getMonth() + 1, value.getDate()));
                        }
                        else {
                            hourCost.setEndDate(null);
                        }
                    }
                });
    }

    public void confirmRemove(HourCost hourCost) {
        int status = Messagebox.show(
                _t("Confirm deleting this hour cost. Are you sure?"), _t("Delete"),
                Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);

        if (Messagebox.OK == status) {
            removeHourCost(hourCost);
        }
    }

    public HourCostListRenderer getRenderer() {
        return hourCostListRenderer;
    }

    /**
     * Adds a new {@link HourCost} to the list of rows.
     */
    public void addHourCost() {
        costCategoryModel.addHourCost();
        Util.reloadBindings(listHourCosts);
    }

    private void removeHourCost(HourCost hourCost) {
        costCategoryModel.removeHourCost(hourCost);
        Util.reloadBindings(listHourCosts);
    }

    /**
     * RowRenderer for a @{HourCost} element.
     */
    private class HourCostListRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object o, int i) throws Exception {
            HourCost hourCost = (HourCost) o;

            row.setValue(hourCost);

            // Create boxes
            appendTextboxCode(row);
            appendHoursType(row);
            appendDecimalboxCost(row);
            appendDateboxInitDate(row);
            appendDateboxEndDate(row);

            appendDeleteButton(row);
        }
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            // We have to auto-generate the code for new objects
            try {
                costCategoryModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
        }
        Util.reloadBindings(listCostCategories);
        Util.reloadBindings(listHourCosts);
    }

    @Override
    protected String getEntityType() {
        return _t("Cost Category");
    }

    @Override
    protected String getPluralEntityType() {
        return _t("Cost Categories");
    }

    @Override
    protected void initCreate() {
        costCategoryModel.initCreate();
    }

    @Override
    protected void initEdit(CostCategory costCategory) {
        costCategoryModel.initEdit(costCategory);
    }

    @Override
    protected CostCategory getEntityBeingEdited() {
        return costCategoryModel.getCostCategory();
    }

    @Override
    protected void delete(CostCategory category) throws InstanceNotFoundException{
        costCategoryModel.confirmRemoveCostCategory(category);
        Util.reloadBindings(listCostCategories);
    }

    protected boolean beforeDeleting(CostCategory category) {
        return costCategoryModel.canRemoveCostCategory(category);
    }

    private boolean canRemoveCostCategory(CostCategory category) {
        return costCategoryModel.canRemoveCostCategory(category);
    }

}
