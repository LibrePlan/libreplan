package org.navalplanner.web.calendars;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
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

    private BaseCalendar baseCalendar;

    private boolean editing = false;

    private ClassValidator<BaseCalendar> baseCalendarValidator = new ClassValidator<BaseCalendar>(
            BaseCalendar.class);

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getBaseCalendars() {
        return baseCalendarDAO.getBaseCalendars();
    }

    @Override
    public BaseCalendar getBaseCalendar() {
        return baseCalendar;
    }

    @Override
    public void prepareForCreate() {
        editing = false;
        this.baseCalendar = BaseCalendar.create();
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(BaseCalendar BaseCalendar) {
        editing = true;
        Validate.notNull(BaseCalendar);

        this.baseCalendar = getFromDB(BaseCalendar);
    }

    private BaseCalendar getFromDB(BaseCalendar BaseCalendar) {
        return getFromDB(BaseCalendar.getId());
    }

    @Transactional(readOnly = true)
    private BaseCalendar getFromDB(Long id) {
        try {
            BaseCalendar BaseCalendar = baseCalendarDAO.find(id);
            return BaseCalendar;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForRemove(BaseCalendar BaseCalendar) {
        this.baseCalendar = BaseCalendar;
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = baseCalendarValidator
                .getInvalidValues(baseCalendar);
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }

        baseCalendarDAO.save(baseCalendar);
    }

    @Override
    @Transactional
    public void remove(BaseCalendar BaseCalendar) {
        try {
            baseCalendarDAO.remove(baseCalendar.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEditing() {
        return this.editing;
    }

}
