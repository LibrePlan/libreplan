package org.navalplanner.web.exceptionDays;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.util.InvalidValueException;

/**
 *
 * @author Diego Pino <dpino@igalia.com>
 *
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/exceptionDays/exceptionDays.zul")
public class CalendarExceptionTypeModel implements ICalendarExceptionTypeModel {

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    private CalendarExceptionType calendarExceptionType;

    @Override
    public void initCreate() {
        calendarExceptionType = CalendarExceptionType.create();
    }

    public void initEdit() {

    }

    @Override
    public CalendarExceptionType getExceptionDayType() {
        return calendarExceptionType;
    }

    @Override
    @Transactional(readOnly=true)
    public List<CalendarExceptionType> getExceptionDayTypes() {
        return calendarExceptionTypeDAO.getAll();
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        calendarExceptionTypeDAO.save(calendarExceptionType);
    }

    @Override
    @Transactional
    public void confirmDelete(CalendarExceptionType exceptionType) throws InstanceNotFoundException, InvalidValueException {
        if (calendarExceptionTypeDAO.hasCalendarExceptions(exceptionType)) {
            throw new InvalidValueException(_("Cannot remove {0}, since it is being used by some Exception Day", exceptionType.getName()));
        }
        if (!exceptionType.isNewObject()) {
            calendarExceptionTypeDAO.remove(exceptionType.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(CalendarExceptionType exceptionType) {
        Validate.notNull(exceptionType);
        this.calendarExceptionType = getFromDB(exceptionType);
    }

    private CalendarExceptionType getFromDB(CalendarExceptionType exceptionType) {
        return getFromDB(exceptionType.getId());
    }

    private CalendarExceptionType getFromDB(Long id) {
        try {
            CalendarExceptionType result = calendarExceptionTypeDAO.find(id);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
