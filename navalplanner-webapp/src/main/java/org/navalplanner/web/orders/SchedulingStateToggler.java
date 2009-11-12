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
package org.navalplanner.web.orders;
import static org.navalplanner.web.I18nHelper._;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.entities.SchedulingState;
import org.navalplanner.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.navalplanner.business.orders.entities.SchedulingState.Type;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SchedulingStateToggler extends HtmlMacroComponent {

    private final SchedulingState state;

    public SchedulingStateToggler(SchedulingState state) {
        Validate.notNull(state);
        this.state = state;
        this.state.addTypeChangeListener(new ITypeChangedListener() {

            @Override
            public void typeChanged(Type newType) {
                recreate();
            }
        });
    }

    public boolean isScheduleButtonVisible() {
        return state.canBeScheduled();
    }

    public boolean isUnscheduleButtonVisible() {
        return state.canBeUnscheduled();
    }

    public void schedule() {
        state.schedule();
    }

    public void unschedule() {
        state.unschedule();
    }

    public String getButtonLabel() {
        if (state.isCompletelyScheduled()) {
            return _("completely scheduled");
        } else if (state.isPartiallyScheduled()) {
            return _("partially scheduled");
        } else {
            return _("no scheduled");
        }
    }

}
