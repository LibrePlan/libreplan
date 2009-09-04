package org.navalplanner.web.calendars;

import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Model for UI operations related to {@link ResourceCalendar}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Qualifier("subclass")
public class ResourceCalendarModel extends BaseCalendarModel implements
        IBaseCalendarModel {

    @Override
    public void initCreate() {
        editing = false;
        this.baseCalendar = ResourceCalendar.create();
    }

}
