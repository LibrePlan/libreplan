/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.common.components.bandboxsearch;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.common.components.finders.IMultipleFiltersFinder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Listbox;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@SuppressWarnings("serial")
public class BandboxMultipleSearch extends HtmlMacroComponent {

    private Listbox listbox;

    private Listhead listhead;

    private Bandbox bandbox;

    private String widthBandbox;

    private String widthListbox;

    private IMultipleFiltersFinder multipleFiltersFinder;

    private List selectedFilters = new ArrayList();

    private String selectedFiltersText = new String("");

    public void afterCompose() {
        super.afterCompose();
        listbox = (Listbox) getFellowIfAny("listbox");
        listhead = (Listhead) listbox.getFellowIfAny("listhead");
        bandbox = (Bandbox) getFellowIfAny("bandbox");

        if (multipleFiltersFinder != null) {
            listbox.setModel(getSubModel());
            listbox.setItemRenderer(multipleFiltersFinder.getItemRenderer());
            addHeaders();

            /**
             * Search for matching elements while typing on bandbox
             */
            bandbox.addEventListener("onChanging", new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    final String inputText = ((InputEvent) event).getValue();
                    if ((inputText == null) || (inputText.isEmpty())) {
                        clear();
                        searchMultipleFilters();
                    } else {
                        searchMultipleFilters(inputText);
                    }
                }
            });

            bandbox.setCtrlKeys("#down");
            bandbox.addEventListener(Events.ON_CTRL_KEY, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    int selectedItemIndex = listbox.getSelectedIndex();
                    List<Listitem> items = listbox.getItems();
                    if (!items.isEmpty()) {
                        listbox.setSelectedIndex(0);
                        items.get(0).setFocus(true);
                    }
                }
            });

            // Close bandbox for events onClick and onOK
            listbox.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    pickElementFromListAndCloseBandbox();
                }
            });
            listbox.addEventListener(Events.ON_OK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    pickElementFromListAndCloseBandbox();
                }
            });
        }

        updateWidth();
    }

    private void pickElementFromListAndCloseBandbox() {
        final Object object = getSelectedItem().getValue();
        if (multipleFiltersFinder.isValidNewFilter(object)) {
            addSelectedElement(object);
            clearListbox();
            listbox.setModel(getSubModel());
        }
        bandbox.close();
    }

    private void searchMultipleFilters() {
        listbox.setModel(getSubModel());
        listbox.invalidate();
    }

    private void searchMultipleFilters(String inputText) {
        String newFilterText = multipleFiltersFinder
                .getNewFilterText(inputText);
        selectedFiltersText = inputText.replace(newFilterText, "");
        if ((newFilterText != null) && (!newFilterText.isEmpty())) {
            listbox.setModel(getSubModel(newFilterText));
            listbox.invalidate();
        } else {
            searchMultipleFilters();
        }
    }

    private void clearSelectedElement() {
        bandbox.setValue("");
        selectedFiltersText = "";
        selectedFilters.clear();
    }

    public void addSelectedElement(Object obj) {
        if (obj != null) {
            selectedFiltersText = selectedFiltersText
                    .concat(multipleFiltersFinder.objectToString(obj));
            bandbox.setValue(selectedFiltersText);

            addFilter(obj);
            selectedFilters = multipleFiltersFinder.updateDeletedFilters(
                    selectedFilters, selectedFiltersText);
        }
    }

    private void addFilter(Object obj) {
        FilterPair newFilter = (FilterPair) obj;
        for (FilterPair filter : (List<FilterPair>) selectedFilters) {
            if ((filter.getType().equals(newFilter.getType()))
                    && (filter.getPattern().equals(newFilter.getPattern()))) {
                throw new WrongValueException(bandbox,
                        _("filter already exists"));
            }
        }
        selectedFilters.add(obj);
    }

    public List getSelectedElements() {
        if (this.multipleFiltersFinder != null) {
            if (!multipleFiltersFinder.isValidFormatText(selectedFilters,
                    bandbox.getValue())) {
                throw new WrongValueException(bandbox,
                        _("format filters are not valid"));
            }
        }
        return selectedFilters;
    }

    /**
     * Find the first ten filters
     */
    @SuppressWarnings("unchecked")
    private ListModel getSubModel() {
        List result = multipleFiltersFinder.getFirstTenFilters();
        return new SimpleListModel(result);
    }

    /**
     * Find filter which contains the expression
     * @param inputText
     */
    @SuppressWarnings("unchecked")
    private ListModel getSubModel(String inputText) {
        List result = multipleFiltersFinder.getMatching(inputText);
        return new SimpleListModel(result);
    }

    /**
     * Append headers to listbox header list
     */
    @SuppressWarnings("unchecked")
    public void addHeaders() {
        clearHeaderIfNecessary();
        final String[] headers = multipleFiltersFinder.getHeaders();
        for (int i = 0; i < headers.length; i++) {
            listhead.getChildren().add(new Listheader(headers[i]));
        }
    }

    private void clearHeaderIfNecessary() {
        if (listhead.getChildren() != null) {
            listhead.getChildren().clear();
        }
    }

    private Listitem getSelectedItem() {
        return (Listitem) listbox.getSelectedItems().iterator().next();
    }

    public void setDisabled(boolean disabled) {
        bandbox.setDisabled(disabled);
    }

    private Object getBean(String beanName) {
        HttpServletRequest servletRequest = (HttpServletRequest) Executions
                .getCurrent().getNativeRequest();
        ServletContext servletContext = servletRequest.getSession()
                .getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContext);
        return webApplicationContext.getBean(beanName);
    }

    public String getFinder() {
        return multipleFiltersFinder.getClass().toString();
    }

    public void setFinder(String classname) {
        multipleFiltersFinder = (IMultipleFiltersFinder) getBean(StringUtils
                .uncapitalize(classname));
    }

    /**
     * Clears {@link Bandbox} Fills bandbox list model, clear bandbox textbox,
     * and set selected label to null
     * @param bandbox
     */
    public void clear() {
        clearSelectedElement();
    }

    private void clearListbox() {
        List<Object> list = new ArrayList<Object>();
        listbox.setModel(new SimpleListModel(list));
        listbox.invalidate();
    }

    public List<Object> asList(ListModel model) {
        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < model.getSize(); i++) {
            result.add(model.getElementAt(i));
        }
        return result;
    }

    public void setListboxEventListener(String event, EventListener listener) {
        listbox.addEventListener(event, listener);
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
}
