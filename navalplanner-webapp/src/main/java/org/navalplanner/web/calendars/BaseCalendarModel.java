package org.navalplanner.web.calendars;

import static org.navalplanner.web.I18nHelper._;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.ExceptionDay;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
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
        List<BaseCalendar> baseCalendars = baseCalendarDAO.getBaseCalendars();
        for (BaseCalendar baseCalendar : baseCalendars) {
            forceLoad(baseCalendar);
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

    private void forceLoad(BaseCalendar baseCalendar) {
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
        return this.selectedDate;
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
    @Transactional
    public void confirmSave() throws ValidationException {
        BaseCalendar entity = getBaseCalendar();

        InvalidValue[] invalidValues = baseCalendarValidator
                .getInvalidValues(entity);
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }

        if (baseCalendarDAO.thereIsOtherWithSameName(getBaseCalendar())) {
            InvalidValue[] invalidValues2 = { new InvalidValue(_(
                    "{0} already exists", entity.getName()),
                    BaseCalendar.class, "name", entity.getName(), entity) };
            throw new ValidationException(invalidValues2,
                    _("Could not save new calendar"));
        }

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
