package org.navalplanner.web.calendars;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ExceptionDay;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.BaseCalendar.Days;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class BaseCalendarModel implements IBaseCalendarModel {

    /**
     * Conversation state
     */
    private BaseCalendar baseCalendar;

    private Date selectedDate;

    private boolean editing = false;

    private ClassValidator<BaseCalendar> baseCalendarValidator = new ClassValidator<BaseCalendar>(
            BaseCalendar.class);

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;


    /*
     * Non conversational steps
     */

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getBaseCalendars() {
        return baseCalendarDAO.findLastVersions();
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

        this.baseCalendar = getFromDB(baseCalendar).newVersion();
        forceLoadHoursPerDayAndExceptionDays(this.baseCalendar);
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreateDerived(BaseCalendar baseCalendar) {
        editing = false;
        Validate.notNull(baseCalendar);

        this.baseCalendar = getFromDB(baseCalendar).newDerivedCalendar();
        forceLoadHoursPerDayAndExceptionDays(this.baseCalendar);
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreateCopy(BaseCalendar baseCalendar) {
        editing = false;
        Validate.notNull(baseCalendar);

        this.baseCalendar = getFromDB(baseCalendar).newCopy();
        forceLoadHoursPerDayAndExceptionDays(this.baseCalendar);
    }

    @Override
    public void initRemove(BaseCalendar baseCalendar) {
        this.baseCalendar = baseCalendar;
    }

    private void forceLoadHoursPerDayAndExceptionDays(BaseCalendar baseCalendar) {
        forceLoadHoursPerDayAndExceptionDaysBasic(baseCalendar);
        forceLoadHoursPerDayAndExceptionDaysPrevious(baseCalendar);
        forceLoadHoursPerDayAndExceptionDaysNext(baseCalendar);
    }

    private void forceLoadHoursPerDayAndExceptionDaysBasic(BaseCalendar baseCalendar) {
        baseCalendar.getHoursPerDay().size();
        baseCalendar.getExceptions().size();

        if (baseCalendar.getParent() != null) {
            forceLoadHoursPerDayAndExceptionDaysBasic(baseCalendar.getParent());
        }
    }

    private void forceLoadHoursPerDayAndExceptionDaysPrevious(
            BaseCalendar baseCalendar) {
        if (baseCalendar.getPreviousCalendar() != null) {
            forceLoadHoursPerDayAndExceptionDaysBasic(baseCalendar.getPreviousCalendar());
            forceLoadHoursPerDayAndExceptionDaysPrevious(baseCalendar
                    .getPreviousCalendar());
        }
    }

    private void forceLoadHoursPerDayAndExceptionDaysNext(
            BaseCalendar baseCalendar) {
        if (baseCalendar.getNextCalendar() != null) {
            forceLoadHoursPerDayAndExceptionDaysBasic(baseCalendar.getNextCalendar());
            forceLoadHoursPerDayAndExceptionDaysNext(baseCalendar
                    .getNextCalendar());
        }
    }

    @Transactional(readOnly = true)
    private BaseCalendar getFromDB(Long id) {
        try {
            BaseCalendar baseCalendar = baseCalendarDAO.find(id);
            return baseCalendar;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private BaseCalendar getFromDB(BaseCalendar baseCalendar) {
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

        if (baseCalendar != null) {
            for (BaseCalendar calendar : baseCalendars) {
                if (isEditing()) {
                    if (calendar.getId().equals(
                            baseCalendar.getPreviousCalendar().getId())) {
                        baseCalendars.remove(calendar);
                        break;
                    }
                } else {
                    if (calendar.getId().equals(baseCalendar.getId())) {
                        baseCalendars.remove(calendar);
                        break;
                    }
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
        return this.selectedDate;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getHoursOfDay() {
        if (baseCalendar == null) {
            return null;
        }

        return baseCalendar.getWorkableHours(selectedDate);
    }

    @Override
    @Transactional(readOnly = true)
    public DayType getTypeOfDay() {
        if (baseCalendar == null) {
            return null;
        }

        return baseCalendar.getType(selectedDate);
    }

    @Override
    @Transactional(readOnly = true)
    public void createException(Integer hours) {
        if (getTypeOfDay().equals(DayType.OWN_EXCEPTION)) {
            baseCalendar.updateExceptionDay(selectedDate, hours);
        } else {
            ExceptionDay day = ExceptionDay.create(selectedDate, hours);
            baseCalendar.addExceptionDay(day);
        }
    }

    @Override
    public Integer getHours(Days day) {
        if (baseCalendar == null) {
            return null;
        }

        return baseCalendar.getHours(day);
    }

    @Override
    public Boolean isDefault(Days day) {
        if (baseCalendar == null) {
            return false;
        }

        return baseCalendar.isDefault(day);
    }

    @Override
    public void unsetDefault(Days day) {
        if (baseCalendar != null) {
            baseCalendar.setHours(day, 0);
        }
    }

    @Override
    public void setDefault(Days day) {
        if (baseCalendar != null) {
            baseCalendar.setDefault(day);
        }
    }

    @Override
    public void setHours(Days day, Integer hours) {
        if (baseCalendar != null) {
            baseCalendar.setHours(day, hours);
        }
    }

    @Override
    public boolean isExceptional() {
        if (baseCalendar == null) {
            return false;
        }

        ExceptionDay day = baseCalendar.getOwnExceptionDay(selectedDate);
        return (day != null);
    }

    @Override
    public void removeException() {
        baseCalendar.removeExceptionDay(selectedDate);
    }

    @Override
    public boolean isDerived() {
        if (baseCalendar == null) {
            return false;
        }

        return baseCalendar.isDerived();
    }

    @Override
    public BaseCalendar getParent() {
        if (baseCalendar == null) {
            return null;
        }

        return baseCalendar.getParent();
    }

    @Override
    @Transactional(readOnly = true)
    public void setParent(BaseCalendar parent) {
        try {
            parent = baseCalendarDAO.find(parent.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        forceLoadHoursPerDayAndExceptionDays(parent);

        if (baseCalendar != null) {
            baseCalendar.setParent(parent);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isParent() {
        if (baseCalendar == null) {
            return false;
        }

        return !baseCalendarDAO.findByParent(baseCalendar).isEmpty();
    }

    @Override
    public Date getExpiringDate() {
        if ((baseCalendar != null)
                && (baseCalendar.getPreviousCalendar() != null)) {
            LocalDate expiringDate = baseCalendar.getPreviousCalendar()
                    .getExpiringDate();
            return expiringDate.toDateTimeAtStartOfDay().toDate();
        }

        return null;
    }

    @Override
    public void setExpiringDate(Date date) {
        if ((baseCalendar != null)
                && (baseCalendar.getPreviousCalendar() != null)) {
            baseCalendar.getPreviousCalendar().setExpiringDate(date);
        }
    }

    /*
     * Final conversation steps
     */

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        InvalidValue[] invalidValues = baseCalendarValidator
                .getInvalidValues(baseCalendar);
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }

        baseCalendar.checkValid();
        baseCalendarDAO.save(baseCalendar);
    }

    @Override
    @Transactional
    public void confirmRemove() {
        try {
            baseCalendarDAO.remove(baseCalendar.getId());
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

}
