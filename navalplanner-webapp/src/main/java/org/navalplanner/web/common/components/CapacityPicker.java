/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.navalplanner.web.common.components;

import org.apache.commons.lang.BooleanUtils;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.Util.Getter;
import org.navalplanner.web.common.Util.Setter;
import org.zkoss.zul.api.Checkbox;

/**
 * It configures some ZK components to work together and edit a Capacity object
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CapacityPicker {

    public static CapacityPicker workWith(Checkbox checkbox,
            EffortDurationPicker standardEffortPicker,
            EffortDurationPicker extraHoursPicker, Getter<Capacity> getter,
            Setter<Capacity> setter) {
        return new CapacityPicker(checkbox, standardEffortPicker,
                extraHoursPicker, getter.get(), setter);
    }

    public static CapacityPicker workWith(Checkbox checkbox,
            EffortDurationPicker standardEffortPicker,
            EffortDurationPicker extraHoursPicker,
            final Capacity initialCapacity) {
        return new CapacityPicker(checkbox, standardEffortPicker,
                extraHoursPicker, initialCapacity, null);
    }

    private Capacity currentCapacity;

    private final Setter<Capacity> setter;

    private final Checkbox overAssignableWithoutLimitCheckbox;

    private final EffortDurationPicker standardEffortPicker;

    private final EffortDurationPicker extraEffortPicker;

    private CapacityPicker(Checkbox overAssignableWithoutLimitCheckbox,
            EffortDurationPicker standardEffortPicker,
            final EffortDurationPicker extraEffortPicker,
            Capacity initialCapacity,
            Setter<Capacity> setter) {
        this.overAssignableWithoutLimitCheckbox = overAssignableWithoutLimitCheckbox;
        this.standardEffortPicker = standardEffortPicker;
        this.extraEffortPicker = extraEffortPicker;
        this.currentCapacity = initialCapacity;
        this.setter = setter;

        standardEffortPicker.bind(new Getter<EffortDuration>() {

            @Override
            public EffortDuration get() {
                return currentCapacity.getStandardEffort();
            }
        }, new Setter<EffortDuration>() {

            @Override
            public void set(EffortDuration value) {
                updateCapacity(currentCapacity.withStandardEffort(value));
            }
        });
        extraEffortPicker.bind(new Getter<EffortDuration>() {

            @Override
            public EffortDuration get() {
                if (currentCapacity.getAllowedExtraEffort() == null) {
                    return EffortDuration.zero();
                }
                return currentCapacity.getAllowedExtraEffort();
            }
        }, new Setter<EffortDuration>() {

            @Override
            public void set(EffortDuration value) {
                updateCapacity(currentCapacity.extraEffort(value));
            }
        });
        Util.bind(overAssignableWithoutLimitCheckbox, new Getter<Boolean>() {

            @Override
            public Boolean get() {
                return currentCapacity.isOverAssignableWithoutLimit();
            }
        }, new Setter<Boolean>() {

            @Override
            public void set(Boolean value) {
                updateCapacity(currentCapacity.overAssignableWithoutLimit(BooleanUtils
                        .isTrue(value)));
                updateExtraEffortDisability(extraEffortPicker);
            }
        });
        updateExtraEffortDisability(extraEffortPicker);

    }

    private void updateExtraEffortDisability(
            EffortDurationPicker extraHoursPicker) {
        extraHoursPicker.setDisabled(currentCapacity.isOverAssignableWithoutLimit());
    }

    private void updateCapacity(Capacity newCapacity) {
        this.currentCapacity = newCapacity;
        if (this.setter != null) {
            this.setter.set(currentCapacity);
        }
    }

    public Capacity getValue() {
        return currentCapacity;
    }

    public void setValue(Capacity capacity) {
        overAssignableWithoutLimitCheckbox.setChecked(capacity
                .isOverAssignableWithoutLimit());
        standardEffortPicker.setValue(capacity.getStandardEffort());
        extraEffortPicker.setValue(capacity.getAllowedExtraEffort());
        currentCapacity = capacity;
        updateExtraEffortDisability(extraEffortPicker);
    }

}
