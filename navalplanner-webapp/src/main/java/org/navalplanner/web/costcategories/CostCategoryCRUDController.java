/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.costcategories;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.business.costcategories.entities.HourCost;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.Autocomplete;
import org.navalplanner.web.workreports.WorkReportCRUDController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link CostCategory}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class CostCategoryCRUDController extends GenericForwardComposer
        implements ICostCategoryCRUDController {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(WorkReportCRUDController.class);

    private Window listWindow;

    private Window createWindow;

    private ICostCategoryModel costCategoryModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Grid listHourCosts;

    private HourCostListRenderer hourCostListRenderer = new HourCostListRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        listHourCosts = (Grid) createWindow.getFellowIfAny("listHourCosts");
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        getVisibility().showOnly(listWindow);
    }

    @Override
    public void goToCreateForm() {
        costCategoryModel.initCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    @Override
    public void goToEditForm(CostCategory costCategory) {
        costCategoryModel.initEdit(costCategory);
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    @Override
    public void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    public void cancel() {
        goToList();
    }


    public void saveAndExit() {
        if (save()) {
            goToList();
        }
    }

    public void saveAndContinue() {
        if (save()) {
            goToEditForm(getCostCategory());
        }
    }

    public boolean save() {
        if(!ConstraintChecker.isValid(createWindow)) {
            return false;
        }
        try {
            costCategoryModel.confirmSave();
            messagesForUser.showMessage(Level.INFO,
                    _("Cost category saved"));
            return true;
        } catch (ValidationException e) {
            String message = _("The following errors were found: ");
            for(InvalidValue each: e.getInvalidValues()) {
                message += each.getMessage();
            }
            messagesForUser.showMessage(Level.ERROR, message);
        }
        return false;
    }

    public CostCategory getCostCategory() {
        return costCategoryModel.getCostCategory();
    }

    public List<CostCategory> getCostCategories() {
        return costCategoryModel.getCostCategories();
    }

    public Set<HourCost> getHourCosts() {
        return costCategoryModel.getHourCosts();
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
    }

    private TypeOfWorkHours getTypeOfWorkHours(Row listitem) {
        HourCost hourCost = (HourCost) listitem.getValue();
        return hourCost.getType();
    }

    /**
     * Append a Autocomplete @{link TypeOfWorkHours} to row
     *
     * @param row
     */
    private void appendAutocompleteType(final Row row) {
        final Autocomplete autocomplete = new Autocomplete();
        autocomplete.setAutodrop(true);
        autocomplete.applyProperties();
        autocomplete.setFinder("TypeOfWorkHoursFinder");
        autocomplete.setConstraint("no empty:" + _("A type must be selected"));

        // Getter, show type selected
        if (getTypeOfWorkHours(row) != null) {
            autocomplete.setSelectedItem(getTypeOfWorkHours(row));
        }

        // Setter, set type selected to HourCost.type
        autocomplete.addEventListener("onSelect", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                final Comboitem comboitem = autocomplete.getSelectedItem();

                if(comboitem != null) {
                    // Update hourCost
                    HourCost hourCost = (HourCost) row.getValue();
                    hourCost.setType((TypeOfWorkHours) comboitem.getValue());
                    row.setValue(hourCost);

                    // Update the hourPrice in the hourCost
                    hourCost.setPriceCost(((TypeOfWorkHours) comboitem.getValue()).getDefaultPrice());
                    Util.reloadBindings(listHourCosts);
                }
            }
        });
        row.appendChild(autocomplete);
    }

    /**
     * Append a delete {@link Button} to {@link Row}
     *
     * @param row
     */
    private void appendDeleteButton(final Row row) {
        Button delete = new Button("", "/common/img/ico_borrar1.png");
        delete.setHoverImage("/common/img/ico_borrar.png");
        delete.setSclass("icono");
        delete.setTooltiptext(_("Delete"));
        delete.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                confirmRemove((HourCost) row.getValue());
            }
        });
        row.appendChild(delete);
    }

    /**
     * Append a Textbox "hour cost" to row
     *
     * @param row
     */
    private void appendTextboxCost(Row row) {
        Textbox txtCost = new Textbox();
        bindTextboxCost(txtCost, (HourCost) row.getValue());
        txtCost.setConstraint("no empty:" + _("cannot be null or empty"));
        row.appendChild(txtCost);
    }

    /**
     * Binds Textbox "hour cost" to the corresponding attribute of a {@link HourCost}
     *
     * @param txtCost
     * @param hourCost
     */
    private void bindTextboxCost(final Textbox txtCost,
            final HourCost hourCost) {
        Util.bind(txtCost, new Util.Getter<String>() {

            @Override
            public String get() {
                if (hourCost.getPriceCost() != null) {
                    return hourCost.getPriceCost().toString();
                }
                return "";
            }

        }, new Util.Setter<String>() {

            @Override
            public void set(String value) {
                hourCost.setPriceCost(new BigDecimal(value));
            }
        });
    }

    /**
     * Append a Datebox "init date" to row
     *
     * @param row
     */
    private void appendDateboxInitDate(final Row row) {
        Datebox initDateBox = new Datebox();
        bindDateboxInitDate(initDateBox, (HourCost) row.getValue());
        initDateBox.setConstraint("no empty:" + _("The init date cannot be empty"));
        row.appendChild(initDateBox);

        initDateBox.addEventListener("onChange", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                // Updates the constraint of the endDate box with the new date
                LocalDate initDate = ((HourCost)row.getValue()).getInitDate();
                Datebox endDateBox = (Datebox) row.getChildren().get(3);
                endDateBox.setConstraint("after " + initDate.getYear() +
                        initDate.getMonthOfYear() + initDate.getDayOfMonth());
            }
        });
    }

    /**
     * Binds Datebox "init date" to the corresponding attribute of a {@link HourCost}
     *
     * @param dateBoxInitDate
     * @param hourCost
     */
    private void bindDateboxInitDate(final Datebox dateBoxInitDate,
            final HourCost hourCost) {
        Util.bind(dateBoxInitDate, new Util.Getter<Date>() {

            @Override
            public Date get() {
                LocalDate dateTime = hourCost.getInitDate();
                if (dateTime != null) {
                    return new Date(dateTime.getYear()-1900,
                            dateTime.getMonthOfYear()-1,dateTime.getDayOfMonth());
                }
                return null;
            }

        }, new Util.Setter<Date>() {

            @Override
            public void set(Date value) {
                if (value != null) {
                    hourCost.setInitDate(new LocalDate(value.getYear()+1900,
                            value.getMonth()+1,value.getDate()));
                }
                else {
                    hourCost.setInitDate(null);
                }
            }
        });
    }

    /**
     * Append a Datebox "end date" to row
     *
     * @param row
     */
    private void appendDateboxEndDate(Row row) {
        Datebox endDateBox = new Datebox();
        bindDateboxEndDate(endDateBox, (HourCost) row.getValue());
        LocalDate initDate = ((HourCost)row.getValue()).getInitDate();
        if (initDate != null) {
            endDateBox.setConstraint("after " + initDate.getYear() +
                    initDate.getMonthOfYear() + initDate.getDayOfMonth());
        }
        row.appendChild(endDateBox);
    }

    /**
     * Binds Datebox "init date" to the corresponding attribute of a {@link HourCost}
     *
     * @param dateBoxInitDate
     * @param hourCost
     */
    private void bindDateboxEndDate(final Datebox dateBoxEndDate,
            final HourCost hourCost) {
        Util.bind(dateBoxEndDate, new Util.Getter<Date>() {

            @Override
            public Date get() {
                LocalDate dateTime = hourCost.getEndDate();
                if (dateTime != null) {
                    return new Date(dateTime.getYear()-1900,
                            dateTime.getMonthOfYear()-1,dateTime.getDayOfMonth());
                }
                return null;
            }

        }, new Util.Setter<Date>() {

            @Override
            public void set(Date value) {
                if (value != null) {
                    hourCost.setEndDate(new LocalDate(value.getYear()+1900,
                            value.getMonth()+1,value.getDate()));
                }
                else {
                    hourCost.setEndDate(null);
                }
            }
        });
    }

    public void confirmRemove(HourCost hourCost) {
        try {
            int status = Messagebox.show(_("Confirm deleting this hour cost. Are you sure?"), _("Delete"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                removeHourCost(hourCost);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(
                    Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ", hourCost.getId()), e);
        }
    }

    public HourCostListRenderer getRenderer() {
        return hourCostListRenderer;
    }

    /**
     * Adds a new {@link HourCost} to the list of rows
     *
     * @param rows
     */
    public void addHourCost() {
        costCategoryModel.addHourCost();
        Util.reloadBindings(listHourCosts);
    }

    public void removeHourCost(HourCost hourCost) {
        costCategoryModel.removeHourCost(hourCost);
        Util.reloadBindings(listHourCosts);
    }

    /**
     * RowRenderer for a @{HourCost} element
     *
     * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
     *
     */
    public class HourCostListRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            HourCost hourCost = (HourCost) data;

            row.setValue(hourCost);

            // Create boxes
            appendAutocompleteType(row);
            appendTextboxCost(row);
            appendDateboxInitDate(row);
            appendDateboxEndDate(row);

            appendDeleteButton(row);
        }
    }
}
