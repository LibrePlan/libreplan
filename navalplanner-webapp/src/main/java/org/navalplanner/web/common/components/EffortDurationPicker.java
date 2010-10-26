/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import static org.navalplanner.web.I18nHelper._;

import java.util.EnumMap;

import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.Granularity;
import org.navalplanner.web.common.Util.Getter;
import org.navalplanner.web.common.Util.Setter;
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

    public EffortDurationPicker() {
        hours = new Spinner();
        hours.setCols(2);
        setMinFor(hours, 0);
        minutes = new Spinner();
        minutes.setCols(2);
        setRangeFor(minutes, 0, 59);
        seconds = new Spinner();
        seconds.setCols(2);
        setRangeFor(seconds, 0, 59);
        appendWithLabel(hours, _("Hours"));
        appendWithLabel(minutes, _("Minutes"));
        appendWithLabel(seconds, _("Seconds"));
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

    private void setMinFor(Spinner spinner, int min) {
        SimpleSpinnerConstraint spinnerConstraint = new SimpleSpinnerConstraint();
        spinnerConstraint.setMin(min);
        spinner.setConstraint(spinnerConstraint);
    }

    public void setDisabled(boolean disabled) {
        hours.setDisabled(disabled);
        minutes.setDisabled(disabled);
        seconds.setDisabled(disabled);
    }

    public void bind(Getter<EffortDuration> getter) {
        updateUIWithValuesFrom(getter.get());
    }

    private void updateUIWithValuesFrom(EffortDuration duration) {
        EnumMap<Granularity, Integer> values = duration.decompose();
        hours.setValue(values.get(Granularity.HOURS));
        minutes.setValue(values.get(Granularity.MINUTES));
        seconds.setValue(values.get(Granularity.SECONDS));
    }

    public void bind(Getter<EffortDuration> getter,
            Setter<EffortDuration> setter) {
        bind(getter);
        listenChanges(setter, hours, minutes, seconds);
    }

    private void listenChanges(final Setter<EffortDuration> setter,
            Spinner... spinners) {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
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
        Integer secondsValue = seconds.getValue();
        EffortDuration newValue = EffortDuration.hours(hoursValue)
                .and(minutesValue, Granularity.MINUTES)
                .and(secondsValue, Granularity.SECONDS);
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
