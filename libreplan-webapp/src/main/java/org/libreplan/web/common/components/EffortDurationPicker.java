/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
package org.libreplan.web.common.components;

import static org.libreplan.web.I18nHelper._;

import java.util.EnumMap;

import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.Granularity;
import org.libreplan.web.common.Util.Getter;
import org.libreplan.web.common.Util.Setter;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.SimpleSpinnerConstraint;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Vbox;

/**
 * @author Óscar González Fernández
 *
 */
public class EffortDurationPicker extends Hbox {

    private Spinner hours;
    private Spinner minutes;
    private Spinner seconds;
    private boolean withseconds = false;

    public EffortDurationPicker() {
        this(false);
    }

    public EffortDurationPicker(boolean withseconds) {
        hours = new Spinner();
        hours.setCols(2);
        setMinFor(hours, 0);
        minutes = new Spinner();
        minutes.setCols(2);
        setRangeFor(minutes, 0, 59);
        appendWithTooltipText(hours, _("Hours"));
        appendWithTooltipText(minutes, _("Minutes"));

        if (withseconds) {
            seconds = new Spinner();
            seconds.setCols(2);
            setRangeFor(seconds, 0, 59);
            appendWithTooltipText(seconds, _("Seconds"));
        }
    }

    private void appendWithTooltipText(Spinner spinner, String label) {
        spinner.setTooltiptext(label);
        appendChild(spinner);
    }

    private void appendWithLabel(Spinner spinner, String label) {
        Vbox column = new Vbox();
        column.appendChild(new Label(label));
        column.appendChild(spinner);
        appendChild(column);
    }

    private void setRangeFor(Spinner spinner, int min, int max) {
        SimpleSpinnerConstraint spinnerConstraint = new SimpleSpinnerConstraint();
        spinnerConstraint.setMin(min);
        spinnerConstraint.setMax(max);
        spinner.setConstraint(spinnerConstraint);
    }

    public void initializeFor24HoursAnd0Minutes() {
        minutes.addEventListener(Events.ON_CHANGE, new EventListener() {
            public void onEvent(Event event) throws Exception {
                setRangeFor24HoursAnd0Minutes();
            }
        });
        hours.addEventListener(Events.ON_CHANGE, new EventListener() {
            public void onEvent(Event event) throws Exception {
                setRangeFor24HoursAnd0Minutes();
            }
        });
    }

    private void setRangeFor24HoursAnd0Minutes() {
        SimpleSpinnerConstraint spinnerConstraint = new SimpleSpinnerConstraint();
        spinnerConstraint.setMin(0);
        if (minutes.getValue() == null || minutes.getValue().intValue() == 0)
            spinnerConstraint.setMax(24);
        else {
            SimpleSpinnerConstraint spinnerConstraintMinutes = new SimpleSpinnerConstraint();
            spinnerConstraintMinutes.setMin(0);
            spinnerConstraintMinutes.setMax(59);
            minutes.setConstraint(spinnerConstraintMinutes);
            spinnerConstraint.setMax(23);
        }
        hours.setConstraint(spinnerConstraint);
    }

    private void setMinFor(Spinner spinner, int min) {
        SimpleSpinnerConstraint spinnerConstraint = new SimpleSpinnerConstraint();
        spinnerConstraint.setMin(min);
        spinner.setConstraint(spinnerConstraint);
    }

    public void setDisabled(boolean disabled) {
        hours.setDisabled(disabled);
        minutes.setDisabled(disabled);
        if (withseconds) {
            seconds.setDisabled(disabled);
        }
    }

    public boolean isDisabled() {
        return hours.isDisabled();
    }

    public void bind(Getter<EffortDuration> getter) {
        updateUIWithValuesFrom(getter.get());
    }

    private void updateUIWithValuesFrom(EffortDuration duration) {
        EnumMap<Granularity, Integer> values = duration.decompose();
        hours.setValue(values.get(Granularity.HOURS));
        hours.invalidate();
        minutes.setValue(values.get(Granularity.MINUTES));
        minutes.invalidate();
        if (withseconds) {
            seconds.setValue(values.get(Granularity.SECONDS));
            seconds.invalidate();
        }
    }

    public void bind(Getter<EffortDuration> getter,
            Setter<EffortDuration> setter) {
        bind(getter);
        if (withseconds) {
            listenChanges(setter, hours, minutes, seconds);
        } else {
            listenChanges(setter, hours, minutes);
        }
    }

    private void listenChanges(final Setter<EffortDuration> setter,
            Spinner... spinners) {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                notifySetterOfChange(setter);
            }
        };

        for (Spinner each : spinners) {
            each.addEventListener(Events.ON_CHANGE, listener);
        }
    }

    private void notifySetterOfChange(Setter<EffortDuration> setter) {
        EffortDuration newValue = createDurationFromUIValues();
        setter.set(newValue);
        updateUIWithValuesFrom(newValue);
    }

    private EffortDuration createDurationFromUIValues() {
        Integer hoursValue = hours.getValue();
        Integer minutesValue = minutes.getValue();
        Integer secondsValue = 0;
        if (withseconds) {
            secondsValue = seconds.getValue();
        }
        EffortDuration newValue = EffortDuration.hours(
                hoursValue != null ? hoursValue : 0).and(
                minutesValue != null ? minutesValue : 0, Granularity.MINUTES)
                .and(secondsValue != null ? secondsValue : 0,
                        Granularity.SECONDS);
        return newValue;
    }

    public void setValue(EffortDuration effortDuration) {
        if (effortDuration == null) {
            effortDuration = EffortDuration.zero();
        }
        updateUIWithValuesFrom(effortDuration);
    }

    public EffortDuration getValue() {
        return createDurationFromUIValues();
    }
}
