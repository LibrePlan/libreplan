/*
 * This file is part of NavalPlan
 *
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
package org.navalplanner.web.exceptionDays;

import static org.navalplanner.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.List;

import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.calendars.entities.CalendarExceptionTypeColor;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.calendars.entities.PredefinedCalendarExceptionTypes;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.web.common.BaseCRUDController;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.Util.Getter;
import org.navalplanner.web.common.Util.Setter;
import org.navalplanner.web.common.components.CapacityPicker;
import org.navalplanner.web.common.components.EffortDurationPicker;
import org.navalplanner.web.common.components.NewDataSortableGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.InvalidValueException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

/**
 *
 * @author Diego Pino <dpino@igalia.com>
 *
 */
public class CalendarExceptionTypeCRUDController extends
        BaseCRUDController<CalendarExceptionType> {

    @Autowired
    private ICalendarExceptionTypeModel calendarExceptionTypeModel;

    private Textbox tbName;

    private Checkbox overAssignable;

    private EffortDurationPicker standardEffort;

    private EffortDurationPicker extraEffort;

    private static ListitemRenderer calendarExceptionTypeColorRenderer = new ListitemRenderer() {
        @Override
        public void render(Listitem item, Object data) throws Exception {
            CalendarExceptionTypeColor color = (CalendarExceptionTypeColor) data;
            item.setValue(color);
            item.appendChild(new Listcell(_(color.getName())));
        }
    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initializeEditWindowComponents();
    }

    private void initializeCapacityPicker() {
        final CalendarExceptionType exceptionType = getExceptionDayType();
        CapacityPicker.workWith(overAssignable, standardEffort, extraEffort,
                new Getter<Capacity>() {

                    @Override
                    public Capacity get() {
                        return exceptionType.getCapacity();
                    }
                }, new Setter<Capacity>() {

                    @Override
                    public void set(Capacity value) {
                        exceptionType.setCapacity(value);
                    }
                });
    }

    private void initializeEditWindowComponents() {
        tbName = (Textbox) editWindow.getFellowIfAny("tbName");
        overAssignable = Util.findComponentAt(editWindow, "overAssignable");
        standardEffort = Util.findComponentAt(editWindow, "standardEffort");
        extraEffort = Util.findComponentAt(editWindow, "extraEffort");
    }

    @Override
    protected void initCreate() {
        calendarExceptionTypeModel.initCreate();
        initializeCapacityPicker();
    }

    @Override
    protected void initEdit(CalendarExceptionType calendarExceptionType) {
        calendarExceptionTypeModel.initEdit(calendarExceptionType);
        initializeCapacityPicker();
    }

    public CalendarExceptionType getExceptionDayType() {
        return calendarExceptionTypeModel.getExceptionDayType();
    }

    public List<CalendarExceptionType> getExceptionDayTypes() {
        return calendarExceptionTypeModel.getExceptionDayTypes();
    }

    @Override
    protected void cancel() {
        clearFields();
    }

    private void clearFields() {
        tbName.setRawValue("");
    }

    @Override
    protected void save() throws ValidationException {
        calendarExceptionTypeModel.confirmSave();
        clearFields();
    }

    @Override
    protected boolean beforeDeleting(CalendarExceptionType calendarExceptionType) {
        if (PredefinedCalendarExceptionTypes.contains(calendarExceptionType)) {
            messagesForUser
                    .showMessage(
                            Level.ERROR,
                            _("Cannot remove the predefined Exception Day Type \"{0}\"",
                                    calendarExceptionType.getHumanId()));
            return false;
        }
        return true;
    }

    @Override
    protected void delete(CalendarExceptionType calendarExceptionType) {
        try {
            calendarExceptionTypeModel.confirmDelete(calendarExceptionType);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvalidValueException e) {
            NewDataSortableGrid listExceptionDayTypes = (NewDataSortableGrid) listWindow
                    .getFellowIfAny("listExceptionDayTypes");
            Row row = findRowByValue(listExceptionDayTypes,
                    calendarExceptionType);
            throw new WrongValueException(row, e.getMessage());
        }
    }

    private Row findRowByValue(Grid grid, Object value) {
        final List<Row> rows = grid.getRows().getChildren();
        for (Row row: rows) {
            if (row.getValue().equals(value)) {
                return row;
            }
        }
        return null;
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            // we have to auto-generate the code for new objects
            try {
                calendarExceptionTypeModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
        }
        Util.reloadBindings(editWindow);
    }

    @Override
    protected String getEntityType() {
        return "Exception Day Type";
    }

    @Override
    protected String getPluralEntityType() {
        return "Exception Day Types";
    }

    @Override
    protected CalendarExceptionType getEntityBeingEdited() {
        return calendarExceptionTypeModel.getExceptionDayType();
    }

    public CalendarExceptionTypeColor[] getColors() {
        return CalendarExceptionTypeColor.values();
    }

    public ListitemRenderer getColorsRenderer() {
        return calendarExceptionTypeColorRenderer;
    }

}
