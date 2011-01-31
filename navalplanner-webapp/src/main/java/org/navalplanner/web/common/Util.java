/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zkplus.databind.DataBinder;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.api.Checkbox;

/**
 * Utilities class. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class Util {

    private Util() {
    }

    public static void reloadBindings(Component... toReload) {
        for (Component reload : toReload) {
            DataBinder binder = Util.getBinder(reload);
            if (binder != null) {
                binder.loadComponent(reload);
            }
        }
    }

    public static void saveBindings(Component... toReload) {
        for (Component reload : toReload) {
            DataBinder binder = Util.getBinder(reload);
            if (binder != null) {
                binder.saveComponent(reload);
            }
        }
    }

    public static DataBinder getBinder(Component component) {
        return (DataBinder) component.getVariable("binder", false);
    }

    @SuppressWarnings("unchecked")
    public static void createBindingsFor(org.zkoss.zk.ui.Component result) {
        List<org.zkoss.zk.ui.Component> children = new ArrayList<org.zkoss.zk.ui.Component>(
                result.getChildren());
        for (org.zkoss.zk.ui.Component child : children) {
            createBindingsFor(child);
        }
        setBinderFor(result);
    }

    private static void setBinderFor(org.zkoss.zk.ui.Component result) {
        AnnotateDataBinder binder = new AnnotateDataBinder(result, true);
        result.setVariable("binder", binder, true);
        binder.loadAll();
    }

    /**
     * Generic interface to represent a class with a typical get method.
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     * @param <T>
     *           The type of the variable to be returned.
     */
    public static interface Getter<T> {
        /**
         * Typical get method that returns a variable.
         * @return A variable of type <T>.
         */
        public T get();
    }

    /**
     * Generic interface to represent a class with a typical set method.
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     * @param <T>
     *            The type of the variable to be set.
     */
    public static interface Setter<T> {
        /**
         * Typical set method to store a variable.
         * @param value
         *            A variable of type <T> to be set.
         */
        public void set(T value);
    }

    /**
     * Binds a {@link Textbox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Textbox}.
     * @param textBox
     *            The {@link Textbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Textbox} bound
     */
    public static Textbox bind(Textbox textBox, Getter<String> getter) {
        textBox.setValue(getter.get());
        textBox.setDisabled(true);
        return textBox;
    }

    /**
     * Binds a {@link Textbox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Textbox}.
     * The {@link Setter} will be used to store the value inserted by the user
     * in the {@link Textbox}.
     * @param textBox
     *            The {@link Textbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Textbox} bound
     */
    public static Textbox bind(final Textbox textBox,
            final Getter<String> getter, final Setter<String> setter) {
        textBox.setValue(getter.get());
        textBox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                InputEvent newInput = (InputEvent) event;
                String value = newInput.getValue();
                setter.set(value);
                textBox.setValue(getter.get());
            }
        });
        return textBox;
    }

    /**
     * Binds a {@link Textbox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Textbox}.
     * @param textBox
     *            The {@link Textbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Textbox} bound
     */
    public static Combobox bind(Combobox comboBox, Getter<Comboitem> getter) {
        comboBox.setSelectedItem(getter.get());
        comboBox.setDisabled(true);
        return comboBox;
    }

    /**
     * Binds a {@link Textbox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Textbox}.
     * The {@link Setter} will be used to store the value inserted by the user
     * in the {@link Textbox}.
     * @param textBox
     *            The {@link Textbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Textbox} bound
     */
    public static Combobox bind(final Combobox comboBox,
            final Getter<Comboitem> getter, final Setter<Comboitem> setter) {
        comboBox.setSelectedItem(getter.get());
        comboBox.addEventListener("onSelect", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                setter.set(comboBox.getSelectedItem());
                comboBox.setSelectedItem(getter.get());
            }
        });
        return comboBox;
    }

    /**
     * Binds a {@link Intbox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Intbox}.
     * @param intBox
     *            The {@link Intbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Intbox} bound
     */
    public static Intbox bind(Intbox intBox, Getter<Integer> getter) {
        intBox.setValue(getter.get());
        intBox.setDisabled(true);
        return intBox;
    }

    /**
     * Binds a {@link Intbox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Intbox}.
     * The {@link Setter} will be used to store the value inserted by the user
     * in the {@link Intbox}.
     * @param intBox
     *            The {@link Intbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Intbox} bound
     */
    public static Intbox bind(final Intbox intBox,
            final Getter<Integer> getter, final Setter<Integer> setter) {
        intBox.setValue(getter.get());
        intBox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                InputEvent newInput = (InputEvent) event;
                String value = newInput.getValue().trim();
                if (value.isEmpty()) {
                    value = "0";
                }
                setter.set(Integer.valueOf(value));
                intBox.setValue(getter.get());
            }
        });
        return intBox;
    }

    /**
     * Binds a {@link Datebox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Datebox}.
     * @param dateBox
     *            The {@link Datebox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Datebox} bound
     */
    public static Datebox bind(final Datebox dateBox, final Getter<Date> getter) {
        dateBox.setValue(getter.get());
        dateBox.setDisabled(true);
        return dateBox;
    }

    /**
     * Binds a {@link Datebox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Datebox}.
     * The {@link Setter} will be used to store the value inserted by the user
     * in the {@link Datebox}.
     * @param dateBox
     *            The {@link Datebox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Datebox} bound
     */
    public static Datebox bind(final Datebox dateBox,
            final Getter<Date> getter, final Setter<Date> setter) {
        dateBox.setValue(getter.get());
        dateBox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                setter.set(dateBox.getValue());
                dateBox.setValue(getter.get());
            }
        });
        return dateBox;
    }

    /**
     * Binds a {@link Timebox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Timebox}.
     * @param dateBox
     *            The {@link Timebox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Timebox} bound
     */
    public static Timebox bind(final Timebox timeBox, final Getter<Date> getter) {
        timeBox.setValue(getter.get());
        timeBox.setDisabled(true);
        return timeBox;
    }

    /**
     * Binds a {@link Timebox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Timebox}.
     * The {@link Setter} will be used to store the value inserted by the user
     * in the {@link Timebox}.
     * @param timeBox
     *            The {@link Timebox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Timebox} bound
     */
    public static Timebox bind(final Timebox timeBox,
            final Getter<Date> getter, final Setter<Date> setter) {
        timeBox.setValue(getter.get());
        timeBox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                setter.set(timeBox.getValue());
                timeBox.setValue(getter.get());
            }
        });
        return timeBox;
    }

    /**
     * Binds a {@link Decimalbox} with a {@link Getter}. The {@link Getter} will
     * be used to get the value that is going to be showed in the
     * {@link Decimalbox}.
     * @param decimalBox
     *            The {@link Decimalbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Decimalbox} bound
     */
    public static Decimalbox bind(final Decimalbox decimalBox,
            final Getter<BigDecimal> getter) {
        decimalBox.setValue(getter.get());
        decimalBox.setDisabled(true);
        return decimalBox;
    }

    /**
     * Binds a {@link Decimalbox} with a {@link Getter}. The {@link Getter} will
     * be used to get the value that is going to be showed in the
     * {@link Decimalbox}. The {@link Setter} will be used to store the value
     * inserted by the user in the {@link Decimalbox}.
     * @param decimalBox
     *            The {@link Decimalbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Decimalbox} bound
     */
    public static Decimalbox bind(final Decimalbox decimalBox,
            final Getter<BigDecimal> getter, final Setter<BigDecimal> setter) {
        decimalBox.setValue(getter.get());
        decimalBox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                setter.set(decimalBox.getValue());
                decimalBox.setValue(getter.get());
            }
        });
        return decimalBox;
    }

    /**
     * Binds a {@link Checkbox} with a {@link Getter}. The {@link Getter} will
     * be used to get the value that is going to be showed in the
     * {@link Checkbox}.
     * @param decimalBox
     *            The {@link Checkbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Checkbox} bound
     */
    public static Checkbox bind(final Checkbox checkBox,
            final Getter<Boolean> getter) {
        checkBox.setChecked(getter.get());
        checkBox.setDisabled(true);
        return checkBox;
    }

    /**
     * Binds a {@link Checkbox} with a {@link Getter}. The {@link Getter} will
     * be used to get the value that is going to be showed in the
     * {@link Checkbox}. The {@link Setter} will be used to store the value
     * inserted by the user in the {@link Checkbox}.
     * @param decimalBox
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Checkbox} bound
     */
    public static <C extends Checkbox> C bind(final C checkBox,
            final Getter<Boolean> getter, final Setter<Boolean> setter) {
        checkBox.setChecked(getter.get());
        checkBox.addEventListener(Events.ON_CHECK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                setter.set(checkBox.isChecked());
                checkBox.setChecked(getter.get());
            }
        });
        return checkBox;
    }

    /**
     * Binds a {@link Checkbox} with a {@link Getter}. The {@link Getter} will
     * be used to get the value that is going to be showed in the
     * {@link Checkbox}.
     * @param Radio
     *            The {@link Radio} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Radio} bound
     */
    public static Radio bind(final Radio radio, final Getter<Boolean> getter) {
        radio.setSelected(getter.get());
        radio.setDisabled(true);
        return radio;
    }

    /**
     * Binds a {@link Radio} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Radio}.
     * The {@link Setter} will be used to store the value inserted by the user
     * in the {@link Radio}.
     * @param decimalBox
     *            The {@link Radio} to be bound
     * @param getter
     *            he {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Radio} bound
     */
    public static Radio bind(final Radio radio, final Getter<Boolean> getter,
            final Setter<Boolean> setter) {
        radio.setSelected(getter.get());
        radio.addEventListener(Events.ON_CHECK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                setter.set(radio.isSelected());
                radio.setChecked(getter.get());
            }
        });
        return radio;
    }

    /**
     * Binds a {@link Bandbox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Bandbox}.
     *
     * @param bandBox
     *            The {@link Bandbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @return The {@link Bandbox} bound
     */
    public static Bandbox bind(Bandbox bandBox, Getter<String> getter) {
        bandBox.setValue(getter.get());
        bandBox.setDisabled(true);
        return bandBox;
    }

    /**
     * Binds a {@link Bandbox} with a {@link Getter}. The {@link Getter} will be
     * used to get the value that is going to be showed in the {@link Bandbox}.
     * The {@link Setter} will be used to store the value inserted by the user
     * in the {@link Bandbox}.
     *
     * @param bandBox
     *            The {@link Bandbox} to be bound
     * @param getter
     *            The {@link Getter} interface that will implement a get method.
     * @param setter
     *            The {@link Setter} interface that will implement a set method.
     * @return The {@link Bandbox} bound
     */
    public static Bandbox bind(final Bandbox bandBox,
            final Getter<String> getter, final Setter<String> setter) {
        bandBox.setValue(getter.get());
        bandBox.addEventListener(Events.ON_CHANGE, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                InputEvent newInput = (InputEvent) event;
                String value = newInput.getValue();
                setter.set(value);
                bandBox.setValue(getter.get());
            }
        });
        return bandBox;
    }

    /**
     * Creates an edit button with class and icon already set.
     *
     * @param eventListener
     *            A event listener for {@link Events.ON_CLICK}
     * @return An edit {@link Button}
     */
    public static Button createEditButton(EventListener eventListener) {
        Button result = new Button();
        result.setTooltiptext(_("Edit"));
        result.setSclass("icono");
        result.setImage("/common/img/ico_editar1.png");
        result.setHoverImage("/common/img/ico_editar.png");

        result.addEventListener(Events.ON_CLICK, eventListener);

        return result;
    }

    /**
     * Creates a remove button with class and icon already set.
     *
     * @param eventListener
     *            A event listener for {@link Events.ON_CLICK}
     * @return A remove {@link Button}
     */
    public static Button createRemoveButton(EventListener eventListener) {
        Button result = new Button();
        result.setTooltiptext(_("Remove"));
        result.setSclass("icono");
        result.setImage("/common/img/ico_borrar1.png");
        result.setHoverImage("/common/img/ico_borrar.png");

        result.addEventListener(Events.ON_CLICK, eventListener);

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Component> T findComponentAt(Component container,
            String idOfComponentToBeFound) {
        return (T) container.getFellow(idOfComponentToBeFound);
    }

    public interface ICreation<T extends Component> {
        public T createAt(Component parent);
    }

    public static <T extends Component> T findOrCreate(Component container,
            Class<T> klassOfComponentToFind, ICreation<T> ifNotFound) {
        @SuppressWarnings("unchecked")
        List<T> existent = ComponentsFinder.findComponentsOfType(
                klassOfComponentToFind, container.getChildren());
        if (!existent.isEmpty()) {
            return existent.get(0);
        }
        return ifNotFound.createAt(container);
    }

    /**
     * It removes all listeners registered for eventName and adds the new
     * listener. It's ensured that the only listener left in the component for
     * events of name eventName is uniqueListener
     *
     * @param component
     * @param eventName
     * @param uniqueListener
     */
    public static void ensureUniqueListener(Component component, String eventName,
            EventListener uniqueListener) {
        Iterator<?> listenerIterator = component.getListenerIterator(eventName);
        while (listenerIterator.hasNext()) {
            listenerIterator.next();
            listenerIterator.remove();
        }
        component.addEventListener(eventName, uniqueListener);
    }

}
