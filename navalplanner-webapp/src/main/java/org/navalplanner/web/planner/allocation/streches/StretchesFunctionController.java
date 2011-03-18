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

package org.navalplanner.web.planner.allocation.streches;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.planner.entities.AssignmentFunction;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Stretch;
import org.navalplanner.business.planner.entities.StretchesFunction;
import org.navalplanner.business.planner.entities.StretchesFunction.Type;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.XYModel;
import org.zkoss.zul.api.Window;

public class StretchesFunctionController extends GenericForwardComposer {

    private static final String EXIT_STATUS = "EXIT_STATUS";

    public interface IGraphicGenerator {

        public boolean areChartsEnabled(IStretchesFunctionModel model);

        XYModel getDedicationChart(
                IStretchesFunctionModel stretchesFunctionModel);

        XYModel getAccumulatedHoursChartData(
                IStretchesFunctionModel stretchesFunctionModel);

    }
    private Window window;

    private IStretchesFunctionModel stretchesFunctionModel;

    private StretchesRenderer stretchesRenderer = new StretchesRenderer();

    private String title;

    private final IGraphicGenerator graphicGenerator;

    public StretchesFunctionController(IGraphicGenerator graphicGenerator) {
        Validate.notNull(graphicGenerator);
        this.graphicGenerator = graphicGenerator;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
    }

    public AssignmentFunction getAssignmentFunction() {
        return stretchesFunctionModel.getStretchesFunction();
    }

    public void setResourceAllocation(ResourceAllocation<?> resourceAllocation,
            Type type) {
        AssignmentFunction assignmentFunction = resourceAllocation
                .getAssignmentFunction();
        stretchesFunctionModel.init((StretchesFunction) assignmentFunction,
                resourceAllocation, type);
        reloadStretchesListAndCharts();
    }

    public int showWindow() {
        try {
            window.doModal();
            return (Integer) window.getVariable("EXIT_STATUS", true);
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void confirm() throws InterruptedException {
        try {
            stretchesFunctionModel.confirm();
            exit();
        } catch (ValidationException e) {
            Messagebox.show(e.getMessage(), _("Error"), Messagebox.OK,
                    Messagebox.ERROR);
        }
    }

    public void cancel() throws InterruptedException {
        int status = Messagebox.show(
                _("You will lose the changes. Are you sure?"),
                _("Confirm cancel"), Messagebox.YES | Messagebox.NO,
                Messagebox.QUESTION);
        if (Messagebox.YES == status) {
            stretchesFunctionModel.cancel();
            close();
        }
    }

    private void close() {
        window.setVariable(EXIT_STATUS, Messagebox.CANCEL, true);
        window.setVisible(false);
    }

    private void exit() {
        window.setVariable(EXIT_STATUS, Messagebox.OK, true);
        window.setVisible(false);
    }

    public void onClose(Event event) {
        close();
    }

    public List<Stretch> getStretches() {
        return stretchesFunctionModel.getStretches();
    }

    public StretchesRenderer getStretchesRenderer() {
        return stretchesRenderer;
    }

    private interface IFocusApplycability {
        public abstract boolean focusIfApplycableOnLength(Stretch strech,
                Decimalbox lenghtPercentage);

        public abstract boolean focusIfApplycableOnAmountWork(Stretch strech,
                Decimalbox amountWork);
    }

    private static class FocusState implements IFocusApplycability {

        private static final NoFocus NO_FOCUS = new NoFocus();

        private IFocusApplycability currentFocus;

        private FocusState(IFocusApplycability currentFocus) {
            this.currentFocus = currentFocus;
        }

        public static FocusState noFocus() {
            return new FocusState(NO_FOCUS);
        }

        @Override
        public boolean focusIfApplycableOnAmountWork(Stretch strech,
                Decimalbox amountWork) {
            boolean result = currentFocus.focusIfApplycableOnAmountWork(strech,
                    amountWork);
            if (result) {
                currentFocus = NO_FOCUS;
            }
            return result;
        }

        @Override
        public boolean focusIfApplycableOnLength(Stretch strech,
                Decimalbox lenghtPercentage) {
            boolean result = currentFocus.focusIfApplycableOnLength(strech,
                    lenghtPercentage);
            if (result) {
                currentFocus = NO_FOCUS;
            }
            return result;
        }

        public void focusOn(Stretch stretch, Field field) {
            this.currentFocus = new FocusOnStrech(stretch, field);
        }

    }

    private static class NoFocus implements IFocusApplycability {

        @Override
        public boolean focusIfApplycableOnAmountWork(Stretch strech,
                Decimalbox amountWork) {
            return false;
        }

        @Override
        public boolean focusIfApplycableOnLength(Stretch strech,
                Decimalbox lenghtPercentage) {
            return false;
        }
    }

    public enum Field {
        AMOUNT_WORK, LENGTH
    }

    private static class FocusOnStrech implements IFocusApplycability {

        private final Stretch stretch;

        private final Field field;

        public FocusOnStrech(Stretch stretch, Field field) {
            this.stretch = stretch;
            this.field = field;
        }

        @Override
        public boolean focusIfApplycableOnAmountWork(Stretch stretch,
                Decimalbox amountWork) {
            if (field == Field.AMOUNT_WORK && this.stretch.equals(stretch)) {
                amountWork.focus();
                return true;
            }
            return false;
        }

        @Override
        public boolean focusIfApplycableOnLength(Stretch stretch,
                Decimalbox lenghtPercentage) {
            if (field == Field.LENGTH && this.stretch.equals(stretch)) {
                lenghtPercentage.focus();
                return true;
            }
            return false;
        }

    }

    private FocusState focusState = FocusState.noFocus();

    /**
     * Renders a {@link Stretch}.
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     */
    private class StretchesRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            Stretch stretch = (Stretch) data;
            item.setValue(stretch);

            appendDate(item, stretch);
            appendLengthPercentage(item, stretch);
            appendAmountWorkPercentage(item, stretch);
            appendOperations(item, stretch);
        }

        private void appendChild(Listitem item, Component component) {
            Listcell listcell = new Listcell();
            listcell.appendChild(component);
            item.appendChild(listcell);
        }

        private void appendDate(Listitem item, final Stretch stretch) {
            final Datebox tempDatebox = new Datebox();
            Datebox datebox = Util.bind(tempDatebox, new Util.Getter<Date>() {
                @Override
                public Date get() {
                    return stretch.getDate().toDateTimeAtStartOfDay().toDate();
                }
            }, new Util.Setter<Date>() {
                @Override
                public void set(Date value) {
                    try {
                        if (value == null) {
                            value = new Date();
                        }
                        stretchesFunctionModel.setStretchDate(stretch, value);
                        reloadStretchesListAndCharts();
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(tempDatebox, e
                                .getMessage());
                    }
                }
            });
            appendChild(item, datebox);
        }

        private void appendLengthPercentage(Listitem item, final Stretch stretch) {
            final Decimalbox tempDecimalbox = new Decimalbox();
            final Decimalbox decimalbox = Util.bind(tempDecimalbox,
                    new Util.Getter<BigDecimal>() {
                        @Override
                        public BigDecimal get() {
                            return stretch.getLengthPercentage().multiply(
                                    new BigDecimal(100));
                        }
                    }, new Util.Setter<BigDecimal>() {
                        @Override
                        public void set(BigDecimal value) {
                            if (value == null) {
                                value = BigDecimal.ZERO;
                            }
                            if (value.toBigInteger().intValue() > 100
                                    || value.toBigInteger().intValue() < 0) {
                                throw new WrongValueException(
                                        tempDecimalbox,
                                        _("Length percentage should be between 0 and 100"));
                            }
                            value = value.setScale(2).divide(
                                    new BigDecimal(100), RoundingMode.DOWN);
                            stretchesFunctionModel.setStretchLengthPercentage(
                                    stretch, value);
                            focusState.focusOn(stretch, Field.LENGTH);
                            reloadStretchesListAndCharts();
                        }
                    });
            appendChild(item, decimalbox);
            focusState.focusIfApplycableOnLength(stretch, decimalbox);
        }

        private void appendAmountWorkPercentage(Listitem item,
                final Stretch stretch) {
            final Decimalbox decimalBox = new Decimalbox();
            Util.bind(decimalBox,
                    new Util.Getter<BigDecimal>() {
                        @Override
                        public BigDecimal get() {
                            return stretch.getAmountWorkPercentage().multiply(
                                    new BigDecimal(100));
                        }
                    }, new Util.Setter<BigDecimal>() {
                        @Override
                        public void set(BigDecimal value) {
                            if(value==null){
                                value = BigDecimal.ZERO;
                            }
                            value = value.setScale(2).divide(
                                    new BigDecimal(100), RoundingMode.DOWN);
                            try {
                                stretch.setAmountWorkPercentage(value);
                                focusState.focusOn(stretch, Field.AMOUNT_WORK);
                                reloadStretchesListAndCharts();
                            } catch (IllegalArgumentException e) {
                                throw new WrongValueException(
                                        decimalBox,
                                        _("Amount work percentage should be between 0 and 100"));
                            }
                        }
                    });
            appendChild(item, decimalBox);
            focusState.focusIfApplycableOnAmountWork(stretch, decimalBox);
        }

        private void appendOperations(Listitem item, final Stretch stretch) {
            Button button = new Button("", "/common/img/ico_borrar1.png");
            button.setHoverImage("/common/img/ico_borrar.png");
            button.setSclass("icono");
            button.setTooltiptext(_("Delete"));

            button.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    stretchesFunctionModel.removeStretch(stretch);
                    reloadStretchesListAndCharts();
                }
            });

            appendChild(item, button);
        }

    }

    public void addStretch() {
        stretchesFunctionModel.addStretch();
        reloadStretchesListAndCharts();
    }

    private void reloadStretchesListAndCharts() {
        Util.reloadBindings(window.getFellow("stretchesList"));
        Util.reloadBindings(window.getFellow("charts"));
    }

    public XYModel getDedicationChartData() {
        return graphicGenerator.getDedicationChart(stretchesFunctionModel);
    }

    public XYModel getAccumulatedHoursChartData() {
        return graphicGenerator
                .getAccumulatedHoursChartData(stretchesFunctionModel);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChartsEnabled() {
        return graphicGenerator.areChartsEnabled(stretchesFunctionModel);
    }

    public boolean isChartsDisabled() {
        return !isChartsEnabled();
    }

}
