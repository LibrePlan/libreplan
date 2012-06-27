/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.orders;

import static org.libreplan.web.I18nHelper._;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.libreplan.web.common.Util;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Textbox;

/**
 * Textbox component which is transformed into a Datebox picker on demand <br />
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
public class DynamicDatebox extends GenericForwardComposer {

    final Getter<Date> getter;

    final Setter<Date> setter;

    private Textbox dateTextBox;

    private Datebox dateBox;

    private DateFormat dateFormat;

    private boolean disabled = false;

    public DynamicDatebox(Getter<Date> getter,
            Setter<Date> setter) {
        this.setter = setter;
        this.getter = getter;
        this.dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locales
                .getCurrent());
    }

    public Datebox createDateBox() {
        dateBox = new Datebox();
        dateBox.setFormat("short");
        dateBox.setValue(getter.get());
        registerOnEnterOpenDateBox(dateBox);
        registerBlurListener(dateBox);
        registerOnChange(dateBox);
        dateTextBox.getParent().appendChild(dateBox);
        return dateBox;
    }

    public Datebox getDateBox() {
        return dateBox;
    }

    /**
     * When a text box associated to a datebox is requested to show the datebox,
     * the corresponding datebox is shown
     * @param component
     *            the component that has received focus
     */
    public void userWantsDateBox(Component component) {
        if (component == dateTextBox) {
            showDateBox(dateTextBox);
        }
    }

    private void showDateBox(Textbox associatedTextBox) {
        associatedTextBox.setVisible(false);
        getDateBox();
        createDateBox();
        dateBox.setFocus(true);
        dateBox.setOpen(true);
    }

    /**
     * When the dateBox loses focus the corresponding textbox is shown instead.
     * @param dateBox
     *            the component that has lost focus
     */
    public void dateBoxHasLostFocus(Datebox currentDateBox) {
        if (currentDateBox == dateBox) {
            hideDateBox(dateTextBox);
        }
    }

    private void hideDateBox(Textbox associatedTextBox) {
        dateBox.detach();
        associatedTextBox.setVisible(true);
    }


    public void doAfterCompose(Component component) throws Exception {
        findComponents((Hbox) component);
        registerListeners();
        updateComponents();
        applyDisabledToElements(disabled);
    }

    private void registerListeners() {
        registerOnEnterListener(dateTextBox);

        Util.bind(dateTextBox, new Util.Getter<String>() {
            @Override
            public String get() {
                return asString(getter.get());
            }
        }, new Util.Setter<String>() {
            @Override
            public void set(String string) {
                try {
                    setter.set(fromString(string));
                } catch (ParseException e) {
                    throw new WrongValueException(
                            dateTextBox,
                            _("date format is wrong, please use the right format (for example for today the format would be: {0})",
                                    asString(new Date())));
                }
            }
        });

    }

    private void findComponents(Hbox hbox) {
        List<Object> children = hbox.getChildren();
        assert children.size() == 1;

        dateTextBox = findTextBoxOfCell(children);
        // dateBox = findDateBoxOfCell(children);
    }

    private static Textbox findTextBoxOfCell(List<Object> children) {
        return ComponentsFinder.findComponentsOfType(Textbox.class, children)
                .get(0);
    }

    private void registerOnChange(Component component) {
        component.addEventListener("onChange", new EventListener() {

            @Override
            public void onEvent(Event event) {
                updateBean();
                updateComponents();
            }
        });
    }

    private void registerOnEnterListener(final Textbox textBox) {
        textBox.addEventListener("onOK", new EventListener() {

            @Override
            public void onEvent(Event event) {
                userWantsDateBox(textBox);
            }
        });
    }

    private void registerOnEnterOpenDateBox(final Datebox currentDatebox) {
        currentDatebox.addEventListener("onOK", new EventListener() {

            @Override
            public void onEvent(Event event) {
                currentDatebox.setOpen(true);
            }
        });
    }

    private void registerBlurListener(final Datebox currentDatebox) {
        currentDatebox.addEventListener("onBlur", new EventListener() {

            @Override
            public void onEvent(Event event) {
                dateBoxHasLostFocus(currentDatebox);
            }
        });
    }

    public static interface Getter<Date> {
        /**
         * Typical get method that returns a variable.
         * @return A variable of type Date.
         */
        public Date get();
    }

    public static interface Setter<Date> {
        /**
         * Typical set method to store a variable.
         * @param value
         *            A variable of type Date to be set.
         */
        public void set(Date value);
    }

    public void updateBean() {
        Date date = getDateBox().getValue();
        setter.set(date);
    }

    private void updateComponents() {
        getDateTextBox().setValue(asString(getter.get()));
    }

    private String asString(Date date) {
        if (date == null) {
            return "";
        }
        return dateFormat.format(date);
    }

    private Date fromString(String string) throws ParseException {
        if (StringUtils.isBlank(string)) {
            return null;
        }
        return dateFormat.parse(StringUtils.trim(string));
    }

    public Textbox getDateTextBox() {
        return dateTextBox;
    }

    public void setDateTextBox(Textbox dateTextBox) {
        this.dateTextBox = dateTextBox;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        applyDisabledToElements(disabled);
    }

    private void applyDisabledToElements(boolean disabled) {
        if(dateTextBox != null) {
            dateTextBox.setDisabled(disabled);
        }
    }
}
