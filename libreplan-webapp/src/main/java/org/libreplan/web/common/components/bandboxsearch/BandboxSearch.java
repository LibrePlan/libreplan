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

package org.libreplan.web.common.components.bandboxsearch;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.finders.IBandboxFinder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Listbox;
@SuppressWarnings("serial")
public class BandboxSearch extends HtmlMacroComponent {

    public static BandboxSearch create(String finderClassName,
            List<? extends BaseEntity> model) {
        BandboxSearch bandboxSearch = new BandboxSearch();
        bandboxSearch.setFinder(finderClassName);
        bandboxSearch.afterCompose();
        bandboxSearch.setModel(model);
        return bandboxSearch;
    }

    public static BandboxSearch create(String finderClassName) {
        BandboxSearch bandboxSearch = new BandboxSearch();
        bandboxSearch.setFinder(finderClassName);
        bandboxSearch.afterCompose();
        return bandboxSearch;
    }

    private Listbox listbox;

    private Listhead listhead;

    private Bandbox bandbox;

    private IBandboxFinder finder;

    private String widthBandbox;

    private String widthListbox;

    private List<? extends BaseEntity> model;

    public void afterCompose() {
        super.afterCompose();
        listbox = (Listbox) getFellowIfAny("listbox");
        if (model != null) {
            setModel(new SimpleListModel(model));
        } else {
            listbox.setModel(finder.getModel());
        }
        listbox.setItemRenderer(finder.getItemRenderer());

        listhead = (Listhead) listbox.getFellowIfAny("listhead");
        bandbox = (Bandbox) getFellowIfAny("bandbox");

        /**
         * Search for matching elements while typing on bandbox
         */
        bandbox.addEventListener("onChanging", new EventListener() {

            @Override
            public void onEvent(Event event) {
                clearSelectedElement();
                final String inputText = ((InputEvent) event).getValue();
                listbox.setModel(getSubModel(inputText));
                listbox.invalidate();
            }
        });

        bandbox.setCtrlKeys("#down");
        bandbox.addEventListener(Events.ON_CTRL_KEY, new EventListener() {

            @Override
            public void onEvent(Event event) {
                int selectedItemIndex = listbox.getSelectedIndex();
                if (selectedItemIndex != -1) {
                    listbox.getItemAtIndexApi(selectedItemIndex).setFocus(true);
                } else {
                    List<Listitem> items = listbox.getItems();
                    if (!items.isEmpty()) {
                        listbox.setSelectedIndex(0);
                        pickElementFromList();
                        items.get(0).setFocus(true);
                    }
                }
            }
        });

        /**
         * Pick element from list when selecting
         */
        listbox.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) {
                pickElementFromList();
            }
        });

        // Close bandbox for events onClick and onOK
        listbox.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) {
                close();
            }
        });
        listbox.addEventListener(Events.ON_OK, new EventListener() {

            @Override
            public void onEvent(Event event) {
                pickElementFromList();
                close();
            }
        });

        addHeaders();
        updateWidth();
    }

    public void pickElementFromList() {
        final Object object = getSelectedItem().getValue();
        bandbox.setValue(finder.objectToString(object));
        setSelectedElement(object);
    }

    private void clearSelectedElement() {
        setSelectedElement(null);
    }

    public void setSelectedElement(Object obj) {
        bandbox.setVariable("selectedElement", obj, true);
        if (obj != null) {
            bandbox.setValue(finder.objectToString(obj));
        } else {
            bandbox.setValue("");
        }
    }

    public Object getSelectedElement() {
        return bandbox.getVariable("selectedElement", true);
    }

    /**
     * Find {@link Label} which name or type start with prefix
     *
     * @param inputText
     */
    @SuppressWarnings("unchecked")
    private ListModel getSubModel(String inputText) {
        List result = new ArrayList();

        final ListModel finderModel = finder.getModel();
        for (int i = 0; i < finderModel.getSize(); i++) {
            Object obj = finderModel.getElementAt(i);
            if (finder.entryMatchesText(obj, inputText)) {
                result.add(obj);
            }
        }
        return new SimpleListModel(result);
    }

    /**
     * Append headers to listbox header list
     */
    @SuppressWarnings("unchecked")
    public void addHeaders() {
        clearHeaderIfNecessary();
        final String[] headers = finder.getHeaders();
        for (int i = 0; i < headers.length; i++) {
            listhead.getChildren().add(new Listheader(_(headers[i])));
        }
    }

    private void clearHeaderIfNecessary() {
        if (listhead.getChildren() != null) {
            listhead.getChildren().clear();
        }
    }

    public Listitem getSelectedItem() {
        return (listbox == null || listbox.getSelectedItems().isEmpty()) ? null
                : (Listitem) listbox.getSelectedItems().iterator().next();
    }

    public String getFinder() {
        return finder.getClass().toString();
    }

    public void setFinder(String classname) {
        finder = (IBandboxFinder) getBean(StringUtils.uncapitalize(classname));
        finder.resetModel();
    }

    public List<? extends BaseEntity> getModel() {
        return model;
    }

    public void setModel(List<? extends BaseEntity> model) {
        this.model = model;
        setModel(new SimpleListModel(model));
    }

    private void setModel(ListModel model) {
        finder.setModel(model);
        listbox.setModel(model);
    }

    public void setDisabled(boolean disabled) {
        bandbox.setDisabled(disabled);
    }

    private Object getBean(String classname) {
        HttpServletRequest servletRequest = (HttpServletRequest) Executions
                .getCurrent().getNativeRequest();
        ServletContext servletContext = servletRequest.getSession()
                .getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContext);
        return webApplicationContext.getBean(classname);
    }

    /**
     * Clears {@link Bandbox}
     *
     * Fills bandbox list model, clear bandbox textbox, and set selected label
     * to null
     *
     * @param bandbox
     */
    public void clear() {
        listbox.setModel(finder.getModel());
        bandbox.setValue("");
        clearSelectedElement();
    }

    /**
     * Adds a new element to list of elements
     *
     * @param obj
     */
    public void addElement(Object obj) {
        List<Object> list = asList(finder.getModel());
        list.add(obj);
        setModel(new SimpleListModel(list));
        Util.reloadBindings(listbox);
    }

    public List<Object> asList(ListModel model) {
        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < model.getSize(); i++) {
            result.add(model.getElementAt(i));
        }
        return result;
    }

    public void setListboxEventListener(String event,
            EventListener listener) {
        listbox.addEventListener(event, listener);
    }


    public void setBandboxEventListener(String event, EventListener listener) {
        bandbox.addEventListener(event, listener);
    }

    public void foucusOnInput() {
        bandbox.setFocus(true);
    }

    public String getWidthBandbox() {
        return widthBandbox;
    }

    public void setWidthBandbox(String widthBandbox) {
        this.widthBandbox = widthBandbox;
    }

    public String getWidthListbox() {
        return widthListbox;
    }

    public void setWidthListbox(String widthListbox) {
        this.widthListbox = widthListbox;
    }

    private void updateWidth() {
        if ((widthBandbox != null) && (!widthBandbox.isEmpty())) {
            this.bandbox.setWidth(widthBandbox);
            this.listbox.setWidth(widthListbox);
        }
    }

    public void setListboxWidth(String width) {
        if (listbox != null) {
            listbox.setWidth(width);
        }
    }

    public void close() {
        if (bandbox != null) {
            bandbox.close();
        }
    }

    public void setBandboxConstraint(String constr) {
        bandbox.setConstraint(constr);
    }

    public void setBandboxConstraint(Constraint constr) {
        bandbox.setConstraint(constr);
    }
}
