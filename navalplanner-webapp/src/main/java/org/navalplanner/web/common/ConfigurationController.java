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

package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.entities.Configuration;
import org.navalplanner.business.common.entities.OrderSequence;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Listitem;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Configuration} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ConfigurationController extends GenericForwardComposer {

    private Window configurationWindow;
    private BandboxSearch defaultCalendarBandboxSearch;

    private IConfigurationModel configurationModel;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private OrderSequenceRowRenderer orderSequenceRowRenderer = new OrderSequenceRowRenderer();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("configurationController", this, true);
        configurationModel.init();

        defaultCalendarBandboxSearch.setListboxEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        Listitem selectedItem = (Listitem) ((SelectEvent) event)
                                .getSelectedItems()
                                .iterator().next();
                        setDefaultCalendar((BaseCalendar) selectedItem
                                .getValue());
                    }
                });

        messages = new MessagesForUser(messagesContainer);
    }

    public List<BaseCalendar> getCalendars() {
        return configurationModel.getCalendars();
    }

    public BaseCalendar getDefaultCalendar() {
        return configurationModel.getDefaultCalendar();
    }

    public void setDefaultCalendar(BaseCalendar calendar) {
        configurationModel.setDefaultCalendar(calendar);
    }

    public void save() throws InterruptedException {
        if (ConstraintChecker.isValid(configurationWindow)) {
            try {
                configurationModel.confirm();
                messages.showMessage(Level.INFO, _("Changes saved"));
                reloadWindow();
            } catch (ValidationException e) {
                messages.showInvalidValues(e);
            } catch (ConcurrentModificationException e) {
                messages.showMessage(Level.ERROR, e.getMessage());
                configurationModel.init();
                reloadWindow();
            }
        }
    }

    public void cancel() throws InterruptedException {
        configurationModel.cancel();
        messages.showMessage(Level.INFO, _("Changes have been canceled"));
        reloadWindow();
    }

    private void reloadWindow() {
        Util.reloadBindings(configurationWindow);
    }

    public String getCompanyCode() {
        return configurationModel.getCompanyCode();
    }

    public void setCompanyCode(String companyCode) {
        configurationModel.setCompanyCode(companyCode);
    }

    public Boolean getGenerateCodeForCriterion() {
        return configurationModel.getGenerateCodeForCriterion();
    }

    public void setGenerateCodeForCriterion(Boolean generateCodeForCriterion) {
        configurationModel.setGenerateCodeForCriterion(generateCodeForCriterion);
    }

    public Boolean getGenerateCodeForLabel() {
        return configurationModel.getGenerateCodeForLabel();
    }

    public void setGenerateCodeForLabel(Boolean generateCodeForLabel) {
        configurationModel.setGenerateCodeForLabel(generateCodeForLabel);
    }

    public Boolean getGenerateCodeForWorkReport() {
        return configurationModel.getGenerateCodeForWorkReport();
    }

    public void setGenerateCodeForWorkReport(Boolean generateCodeForWorkReport) {
        configurationModel.setGenerateCodeForWorkReport(generateCodeForWorkReport);
    }

    public Boolean getGenerateCodeForResources() {
        return configurationModel.getGenerateCodeForResources();
    }

    public void setGenerateCodeForResources(Boolean generateCodeForResources) {
        configurationModel.setGenerateCodeForResources(generateCodeForResources);
    }

    public Boolean getGenerateCodeForTypesOfWorkHours() {
        return configurationModel.getGenerateCodeForTypesOfWorkHours();
    }

    public void setGenerateCodeForTypesOfWorkHours(
            Boolean generateCodeForTypesOfWorkHours) {
        configurationModel.setGenerateCodeForTypesOfWorkHours(
                generateCodeForTypesOfWorkHours);
    }

    public Boolean getGenerateCodeForMaterialCategories() {
        return configurationModel.getGenerateCodeForMaterialCategories();
    }

    public void setGenerateCodeForMaterialCategories(
            Boolean generateCodeForMaterialCategories) {
        configurationModel.setGenerateCodeForMaterialCategories(
                generateCodeForMaterialCategories);
    }

    public Boolean getGenerateCodeForUnitTypes() {
        return configurationModel.getGenerateCodeForUnitTypes();
    }

    public void setGenerateCodeForUnitTypes(
            Boolean generateCodeForUnitTypes) {
        configurationModel.setGenerateCodeForUnitTypes(
                generateCodeForUnitTypes);
    }

    public List<OrderSequence> getOrderSequences() {
        return configurationModel.getOrderSequences();
    }

    public void addOrderSequence() {
        configurationModel.addOrderSequence();
        reloadOrderSequencesList();
    }

    public void removeOrderSequence(OrderSequence orderSequence) {
        try {
            configurationModel.removeOrderSequence(orderSequence);
        } catch (IllegalArgumentException e) {
            messages.showMessage(Level.ERROR, e.getMessage());
        }
        reloadOrderSequencesList();
    }

    private void reloadOrderSequencesList() {
        Util
                .reloadBindings(configurationWindow
                        .getFellow("orderSequencesList"));
    }

    public OrderSequenceRowRenderer getOrderSequenceRowRenderer() {
        return orderSequenceRowRenderer;
    }

    private class OrderSequenceRowRenderer implements RowRenderer {

        @Override
        public void render(Row row, Object data) throws Exception {
            OrderSequence orderSequence = (OrderSequence) data;
            row.setValue(orderSequence);

            appendActiveCheckbox(row, orderSequence);
            appendPrefixTextbox(row, orderSequence);
            appendNumberOfDigitsInbox(row, orderSequence);
            appendLastValueInbox(row, orderSequence);
            appendOperations(row, orderSequence);
        }

        private void appendActiveCheckbox(Row row,
                final OrderSequence orderSequence) {
            Checkbox checkbox = Util.bind(new Checkbox(),
                    new Util.Getter<Boolean>() {

                        @Override
                        public Boolean get() {
                            return orderSequence.isActive();
                        }
                    }, new Util.Setter<Boolean>() {

                        @Override
                        public void set(Boolean value) {
                            orderSequence.setActive(value);
                        }
                    });

            row.appendChild(checkbox);
        }

        private void appendPrefixTextbox(Row row,
                final OrderSequence orderSequence) {
            final Textbox tempTextbox = new Textbox();
            Textbox textbox = Util.bind(tempTextbox,
                    new Util.Getter<String>() {

                @Override
                public String get() {
                    return orderSequence.getPrefix();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    try {
                        orderSequence.setPrefix(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(tempTextbox, e
                                .getMessage());
                    }
                }
            });
            textbox.setConstraint("no empty:" + _("cannot be null or empty"));

            if (orderSequence.isAlreadyInUse()) {
                textbox.setDisabled(true);
            }

            row.appendChild(textbox);
        }

        private void appendNumberOfDigitsInbox(Row row,
                final OrderSequence orderSequence) {
            final Intbox tempIntbox = new Intbox();
            Intbox intbox = Util.bind(tempIntbox, new Util.Getter<Integer>() {

                @Override
                public Integer get() {
                    return orderSequence.getNumberOfDigits();
                }
            }, new Util.Setter<Integer>() {

                @Override
                public void set(Integer value) {
                    try {
                        orderSequence.setNumberOfDigits(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(tempIntbox, e.getMessage());
                    }
                }
            });
            intbox.setConstraint("no empty:" + _("cannot be null or empty"));

            if (orderSequence.isAlreadyInUse()) {
                intbox.setDisabled(true);
            }

            row.appendChild(intbox);
        }

        private void appendLastValueInbox(Row row,
                final OrderSequence orderSequence) {
            Textbox textbox = Util.bind(new Textbox(),
                    new Util.Getter<String>() {

                @Override
                public String get() {
                            return OrderSequence.formatValue(orderSequence
                                    .getNumberOfDigits(), orderSequence
                                    .getLastValue());
                }
            });

            row.appendChild(textbox);
        }

        private void appendOperations(Row row, final OrderSequence orderSequence) {
            final Button removeButton = Util
                    .createRemoveButton(new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    removeOrderSequence(orderSequence);
                }
            });

            row.appendChild(removeButton);
        }

    }

    public void setExpandCompanyPlanningViewCharts(
            Boolean expandCompanyPlanningViewCharts) {
        configurationModel
                .setExpandCompanyPlanningViewCharts(expandCompanyPlanningViewCharts);
    }

    public Boolean isExpandCompanyPlanningViewCharts() {
        return configurationModel.isExpandCompanyPlanningViewCharts();
    }

    public void setExpandOrderPlanningViewCharts(
            Boolean expandOrderPlanningViewCharts) {
        configurationModel
                .setExpandOrderPlanningViewCharts(expandOrderPlanningViewCharts);
    }

    public Boolean isExpandOrderPlanningViewCharts() {
        return configurationModel.isExpandOrderPlanningViewCharts();
    }

    public void setExpandResourceLoadViewCharts(
            Boolean expandResourceLoadViewCharts) {
        configurationModel
                .setExpandResourceLoadViewCharts(expandResourceLoadViewCharts);
    }

    public Boolean isExpandResourceLoadViewCharts() {
        return configurationModel.isExpandResourceLoadViewCharts();
    }

    public void setMonteCarloMethodTabVisible(
            Boolean expandResourceLoadViewCharts) {
        configurationModel
                .setMonteCarloMethodTabVisible(expandResourceLoadViewCharts);
    }

    public Boolean isMonteCarloMethodTabVisible() {
        return configurationModel.isMonteCarloMethodTabVisible();
    }

}