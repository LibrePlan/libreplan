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

package org.navalplanner.web.calendars;

import static org.navalplanner.web.I18nHelper._;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.InvalidValue;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.ExceptionDay;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Model for UI operations related to {@link BaseCalendar}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Qualifier("main")
public class BaseCalendarModel implements IBaseCalendarModel {

    /**
     * Conversation state
     */
    protected BaseCalendar baseCalendar;

    private Date selectedDate;

    protected boolean editing = false;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;


    /*
     * Non conversational steps
     */

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getBaseCalendars() {
        List<BaseCalendar> baseCalendars = baseCalendarDAO.getBaseCalendars();
        for (BaseCalendar each : baseCalendars) {
            forceLoad(each);
        }
        return baseCalendars;
    }

    /*
     * Initial conversation steps
     */

    @Override
    public void initCreate() {
        editing = false;
        this.baseCalendar = BaseCalendar.create();
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(BaseCalendar baseCalendar) {
        editing = true;
        Validate.notNull(baseCalendar);

        this.baseCalendar = getFromDB(baseCalendar);
        forceLoad(this.baseCalendar);
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreateDerived(BaseCalendar baseCalendar) {
        editing = false;
        Validate.notNull(baseCalendar);

        this.baseCalendar = getFromDB(baseCalendar).newDerivedCalendar();
        forceLoad(this.baseCalendar);
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreateCopy(BaseCalendar baseCalendar) {
        editing = false;
        Validate.notNull(baseCalendar);

        this.baseCalendar = getFromDB(baseCalendar).newCopy();
        forceLoad(this.baseCalendar);
    }

    @Override
    public void initRemove(BaseCalendar baseCalendar) {
        this.baseCalendar = baseCalendar;
    }

    protected void forceLoad(BaseCalendar baseCalendar) {
        for (CalendarData calendarData : baseCalendar.getCalendarDataVersions()) {
            calendarData.getHoursPerDay().size();
            if (calendarData.getParent() != null) {
                forceLoad(calendarData.getParent());
            }
        }
        baseCalendar.getExceptions().size();
    }

    @Transactional(readOnly = true)
    private BaseCalendar getFromDB(Long id) {
        try {
            BaseCalendar result = baseCalendarDAO.find(id);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected BaseCalendar getFromDB(BaseCalendar baseCalendar) {
        return getFromDB(baseCalendar.getId());
    }

    /*
     * Intermediate conversation steps
     */
    @Override
    public BaseCalendar getBaseCalendar() {
        return baseCalendar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getPossibleParentCalendars() {
        List<BaseCalendar> baseCalendars = getBaseCalendars();

        if (getBaseCalendar() != null) {
            for (BaseCalendar calendar : baseCalendars) {
                if (calendar.getId().equals(getBaseCalendar().getId())) {
                    baseCalendars.remove(calendar);
                    break;
                }
            }
        }

        return baseCalendars;
    }

    @Override
    public boolean isEditing() {
        return this.editing;
    }

    @Override
    public void setSelectedDay(Date date) {
        this.selectedDate = date;
    }

    @Override
    public Date getSelectedDay() {
        return selectedDate != null ? new Date(this.selectedDate.getTime())
                : null;
    }

    @Override
    public Integer getHoursOfDay() {
        if (getBaseCalendar() == null) {
            return null;
        }

        return getBaseCalendar().getWorkableHours(selectedDate);
    }

    @Override
    public DayType getTypeOfDay() {
        if (getBaseCalendar() == null) {
            return null;
        }

        return getBaseCalendar().getType(selectedDate);
    }

    @Override
    public DayType getTypeOfDay(LocalDate date) {
        if (getBaseCalendar() == null) {
            return null;
        }

        return getBaseCalendar().getType(date);
    }

    @Override
    public void createException(Integer hours) {
        if (getTypeOfDay().equals(DayType.OWN_EXCEPTION)) {
            getBaseCalendar().updateExceptionDay(selectedDate, hours);
        } else {
            ExceptionDay day = ExceptionDay.create(selectedDate, hours);
            getBaseCalendar().addExceptionDay(day);
        }
    }

    @Override
    public Integer getHours(Days day) {
        if (getBaseCalendar() == null) {
            return null;
        }

        return getBaseCalendar().getHours(selectedDate, day);
    }

    @Override
    public Boolean isDefault(Days day) {
        if (getBaseCalendar() == null) {
            return false;
        }

        return getBaseCalendar().isDefault(day, selectedDate);
    }

    @Override
    public void unsetDefault(Days day) {
        if (getBaseCalendar() != null) {
            getBaseCalendar().setHours(day, 0, selectedDate);
        }
    }

    @Override
    public void setDefault(Days day) {
        if (getBaseCalendar() != null) {
            getBaseCalendar().setDefault(day, selectedDate);
        }
    }

    @Override
    public void setHours(Days day, Integer hours) {
        if (getBaseCalendar() != null) {
            getBaseCalendar().setHours(day, hours, selectedDate);
        }
    }

    @Override
    public boolean isExceptional() {
        if (getBaseCalendar() == null) {
            return false;
        }

        ExceptionDay day = getBaseCalendar().getOwnExceptionDay(selectedDate);
        return (day != null);
    }

    @Override
    public void removeException() {
        getBaseCalendar().removeExceptionDay(selectedDate);
    }

    @Override
    public boolean isDerived() {
        if (getBaseCalendar() == null) {
            return false;
        }

        return getBaseCalendar().isDerived(selectedDate);
    }

    @Override
    public BaseCalendar getParent() {
        if (getBaseCalendar() == null) {
            return null;
        }

        return getBaseCalendar().getParent(selectedDate);
    }

    @Override
    @Transactional(readOnly = true)
    public void setParent(BaseCalendar parent) {
        try {
            parent = baseCalendarDAO.find(parent.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        forceLoad(parent);

        if (getBaseCalendar() != null) {
            getBaseCalendar().setParent(parent, selectedDate);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isParent() {
        if (getBaseCalendar() == null) {
            return false;
        }

        return !baseCalendarDAO.findByParent(getBaseCalendar()).isEmpty();
    }

    @Override
    public Date getExpiringDate() {
        if ((getBaseCalendar() != null)
                && (getBaseCalendar().getExpiringDate(selectedDate) != null)) {
            return getBaseCalendar().getExpiringDate(selectedDate).minusDays(1)
                    .toDateTimeAtStartOfDay()
                    .toDate();
        }

        return null;
    }

    @Override
    public void setExpiringDate(Date date) {
        if ((getBaseCalendar() != null)
                && (getBaseCalendar().getExpiringDate(selectedDate) != null)) {
            getBaseCalendar()
                    .setExpiringDate(date, selectedDate);
        }
    }

    @Override
    public Date getDateValidFrom() {
        if (getBaseCalendar() != null) {
            LocalDate validFromDate = getBaseCalendar().getValidFrom(
                    selectedDate);
            if (validFromDate != null) {
                return validFromDate.toDateTimeAtStartOfDay().toDate();
            }
        }

        return null;
    }

    @Override
    public void setDateValidFrom(Date date) {
        if (getBaseCalendar() != null) {
            getBaseCalendar().setValidFrom(date, selectedDate);
        }
    }

    @Override
    public List<CalendarData> getHistoryVersions() {
        if (getBaseCalendar() == null) {
            return null;
        }

        return getBaseCalendar().getCalendarDataVersions();
    }

    @Override
    public void createNewVersion(Date date) {
        if (getBaseCalendar() != null) {
            getBaseCalendar().newVersion(date);
        }
    }

    @Override
    public boolean isLastVersion() {
        if (getBaseCalendar() != null) {
            return getBaseCalendar().isLastVersion(selectedDate);
        }
        return false;
    }

    @Override
    public String getName() {
        if (getBaseCalendar() != null) {
            return getBaseCalendar().getName();
        }
        return null;
    }

    @Override
    public LocalDate getValidFrom(CalendarData calendarData) {
        if (getBaseCalendar() != null) {
            List<CalendarData> calendarDataVersions = getBaseCalendar()
                    .getCalendarDataVersions();
            Integer index = calendarDataVersions.indexOf(calendarData);
            if (index > 0) {
                return calendarDataVersions.get(index - 1).getExpiringDate();
            }
        }

        return null;
    }

    /*
     * Final conversation steps
     */

    @Override
    @Transactional(rollbackFor = ValidationException.class)
    public void confirmSave() throws ValidationException {
        checkInvalidValuesCalendar(getBaseCalendar());
        baseCalendarDAO.save(getBaseCalendar());
    }

    @Override
    public void checkInvalidValuesCalendar(BaseCalendar entity)
            throws ValidationException {
        if (baseCalendarDAO.thereIsOtherWithSameName(entity)) {
            InvalidValue[] invalidValues2 = { new InvalidValue(_(
                    "{0} already exists", entity.getName()),
                    BaseCalendar.class, "name", entity.getName(), entity) };
            throw new ValidationException(invalidValues2,
                    _("Could not save new calendar"));
        }
    }

    @Override
    @Transactional
    public void confirmRemove() {
        try {
            baseCalendarDAO.remove(getBaseCalendar().getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancel() {
        resetState();
    }

    private void resetState() {
        baseCalendar = null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDefaultCalendar(BaseCalendar baseCalendar) {
        return baseCalendar.getId().equals(
                configurationDAO.getConfiguration().getDefaultCalendar()
                        .getId());
    }

}
