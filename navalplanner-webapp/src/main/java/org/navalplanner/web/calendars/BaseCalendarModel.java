package org.navalplanner.web.calendars;

import java.util.ArrayList;
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

        this.baseCalendar = getFromDB(baseCalendar);
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

        if (getBaseCalendar() != null) {
            for (BaseCalendar calendar : baseCalendars) {
                if (isEditing()) {
                    if ((getBaseCalendar().getPreviousCalendar() != null)
                            && (calendar.getId().equals(getBaseCalendar()
                                    .getPreviousCalendar().getId()))) {
                        baseCalendars.remove(calendar);
                        break;
                    }
                } else {
                    if (calendar.getId().equals(getBaseCalendar().getId())) {
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
    @Transactional(readOnly = true)
    public void setSelectedDay(Date date) {
        this.selectedDate = date;

        BaseCalendar validCalendar = baseCalendar.getCalendarVersion(date);
        if (!validCalendar.equals(baseCalendar)) {
            baseCalendar = validCalendar;
            forceLoadHoursPerDayAndExceptionDays(baseCalendar);
        }
    }

    @Override
    public Date getSelectedDay() {
        return this.selectedDate;
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

        return getBaseCalendar().getHours(day);
    }

    @Override
    public Boolean isDefault(Days day) {
        if (getBaseCalendar() == null) {
            return false;
        }

        return getBaseCalendar().isDefault(day);
    }

    @Override
    public void unsetDefault(Days day) {
        if (getBaseCalendar() != null) {
            getBaseCalendar().setHours(day, 0);
        }
    }

    @Override
    public void setDefault(Days day) {
        if (getBaseCalendar() != null) {
            getBaseCalendar().setDefault(day);
        }
    }

    @Override
    public void setHours(Days day, Integer hours) {
        if (getBaseCalendar() != null) {
            getBaseCalendar().setHours(day, hours);
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

        return getBaseCalendar().isDerived();
    }

    @Override
    public BaseCalendar getParent() {
        if (getBaseCalendar() == null) {
            return null;
        }

        return getBaseCalendar().getParent();
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

        if (getBaseCalendar() != null) {
            getBaseCalendar().setParent(parent);
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
                && (getBaseCalendar().getExpiringDate() != null)) {
            return getBaseCalendar().getExpiringDate().toDateTimeAtStartOfDay()
                    .toDate();
        }

        return null;
    }

    @Override
    public Date getDateValidFrom() {
        if ((getBaseCalendar() != null)
                && (getBaseCalendar().getPreviousCalendar() != null)) {
            LocalDate expiringDate = getBaseCalendar().getPreviousCalendar()
                    .getExpiringDate();
            return expiringDate.toDateTimeAtStartOfDay().toDate();
        }

        return null;
    }

    @Override
    public void setDateValidFrom(Date date) {
        if ((getBaseCalendar() != null)
                && (getBaseCalendar().getPreviousCalendar() != null)) {
            getBaseCalendar().getPreviousCalendar().setExpiringDate(date);
        }
    }

    @Override
    public List<BaseCalendar> getHistoryVersions() {
        if (getBaseCalendar() == null) {
            return null;
        }

        List<BaseCalendar> history = new ArrayList<BaseCalendar>();

        BaseCalendar current = getBaseCalendar().getPreviousCalendar();
        while (current != null) {
            history.add(current);
            current = current.getPreviousCalendar();
        }

        return history;
    }

    /*
     * Final conversation steps
     */

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        InvalidValue[] invalidValues = baseCalendarValidator
                .getInvalidValues(getBaseCalendar());
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }

        getBaseCalendar().checkValid();
        baseCalendarDAO.save(getBaseCalendar());
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

}
