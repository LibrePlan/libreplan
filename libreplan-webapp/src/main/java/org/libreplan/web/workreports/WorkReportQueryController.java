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

package org.libreplan.web.workreports;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.web.UserUtil;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.components.Autocomplete;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.security.SecurityUtils;
import org.libreplan.web.users.dashboard.IPersonalTimesheetController;
import org.zkoss.ganttz.IPredicate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Window;

/**
 * Controller for workReportQuery.zul. A kind of report to filter and look for
 * the work report lines.<br />
 *
 * Previously this was part of {@link WorkReportCRUDController}, however it has
 * been moved to a separate controller.
 *
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@SuppressWarnings("serial")
public class WorkReportQueryController extends GenericForwardComposer {

    private IWorkReportModel workReportModel;

    private Datebox filterStartDateLine;

    private Datebox filterFinishDateLine;

    private BandboxSearch bandboxFilterOrderElement;

    private Combobox filterType;

    private Autocomplete filterResource;

    private Autocomplete filterHoursType;

    private Set<IPredicate> predicates = new HashSet<IPredicate>();

    private List<WorkReportLine> filterWorkReportLines = new ArrayList<WorkReportLine>();

    private Grid gridListQuery;

    private Label gridSummary;

    private Window listQueryWindow;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    @javax.annotation.Resource
    private IWorkReportCRUDControllerEntryPoints workReportCRUD;

    @javax.annotation.Resource
    private IPersonalTimesheetController personalTimesheetController;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public List<OrderElement> getOrderElements() {
        return workReportModel.getOrderElements();
    }

    public Constraint checkConstraintFinishDateLine() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date finishDateLine = (Date) value;
                if ((finishDateLine != null)
                        && (filterStartDateLine.getValue() != null)
                        && (finishDateLine.compareTo(filterStartDateLine
                                .getValue()) < 0)) {
                    filterFinishDateLine.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be after start date"));
                }
            }
        };
    }

    public Constraint checkConstraintStartDateLine() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date startDateLine = (Date) value;
                if ((startDateLine != null)
                        && (filterFinishDateLine.getValue() != null)
                        && (startDateLine.compareTo(filterFinishDateLine
                                .getValue()) > 0)) {
                    filterStartDateLine.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be lower than end date"));
                }
            }
        };
    }

    /**
     * Apply filter on all workReportLines
     *
     * First, all workReportLines are retrieved. Then a series of predicates are
     * created containing the conditions of the filter.
     *
     * In the case that there's no order filtered, the predicate filter contains
     * the order of the retrieved workReportLines so it can apply the rest of
     * the parameters of the filter
     *
     * @param event
     */
    public void onApplyFilterWorkReportLines(Event event) {
        OrderElement selectedOrder = getSelectedOrderElement();
        List<WorkReportLine> workReportLines = workReportModel
                .getAllWorkReportLines();

        if (selectedOrder == null) {
            createPredicateLines(filterOrderElements(workReportLines));
        } else {
            createPredicateLines(Collections.singletonList(selectedOrder));
        }
        filterByPredicateLines();
        updateSummary();
    }

    private void updateSummary() {
        updateSummary(filterWorkReportLines);
    }

    private void updateSummary(List<WorkReportLine> workReportLines) {
        WorkReportLineSummary summary = new WorkReportLineSummary(
                totalTasks(workReportLines), totalHours(workReportLines));

        gridSummary.setValue(summary.toString());
    }

    private Integer totalTasks(List<WorkReportLine> workReportLines) {
        return Integer.valueOf(workReportLines.size());
    }

    private EffortDuration totalHours(List<WorkReportLine> workReportLines) {
        EffortDuration result = EffortDuration.zero();
        for (WorkReportLine each : workReportLines) {
            result = result.plus(each.getEffort());
        }
        return result;
    }

    private void filterByPredicateLines() {
        filterWorkReportLines.clear();
        for (IPredicate each : predicates) {
            filterWorkReportLines.addAll(workReportModel
                    .getFilterWorkReportLines(each));
        }
        gridListQuery.setModel(new SimpleListModel(filterWorkReportLines
                .toArray()));
        gridListQuery.invalidate();
    }

    private Collection<OrderElement> filterOrderElements(
            List<WorkReportLine> workReportLines) {
        Collection<OrderElement> result = new HashSet<OrderElement>();
        for (WorkReportLine each : workReportLines) {
            result.add(each.getOrderElement());
        }
        return result;
    }

    private void createPredicateLines(Collection<OrderElement> orderElements) {
        String type = filterType.getValue();
        Resource resource = getSelectedResource();
        Date startDate = filterStartDateLine.getValue();
        Date finishDate = filterFinishDateLine.getValue();
        TypeOfWorkHours hoursType = getSelectedHoursType();

        predicates.clear();
        for (OrderElement each : orderElements) {
            predicates.addAll(createWRLPredicates(type, resource, startDate,
                    finishDate, each, hoursType));
        }
    }

    private Collection<? extends WorkReportLinePredicate> createWRLPredicates(
            String type, Resource resource, Date startDate, Date finishDate,
            OrderElement orderElement, TypeOfWorkHours hoursType) {

        Set<WorkReportLinePredicate> result = new HashSet<WorkReportLinePredicate>();
        if (type.equals(_("All"))) {
            result.add(new WorkReportLinePredicate(resource, startDate,
                    finishDate, orderElement, hoursType));
            if (orderElement != null) {
                for (OrderElement each : orderElement.getChildren()) {
                    result.add(new WorkReportLinePredicate(resource, startDate,
                            finishDate, each, hoursType));
                }
            }
        } else if (type.equals(_("Direct"))) {
            result.add(new WorkReportLinePredicate(resource, startDate,
                    finishDate, orderElement, hoursType));
        } else if (type.equals(_("Indirect")) && orderElement != null) {
            for (OrderElement each : orderElement.getChildren()) {
                result.add(new WorkReportLinePredicate(resource, startDate,
                        finishDate, each, hoursType));
            }
        }
        return result;
    }

    private Resource getSelectedResource() {
        Comboitem itemSelected = filterResource.getSelectedItem();
        if ((itemSelected != null)
                && (((Resource) itemSelected.getValue()) != null)) {
            return (Resource) itemSelected.getValue();
        }
        return null;
    }

    private OrderElement getSelectedOrderElement() {
        OrderElement orderElement = (OrderElement) this.bandboxFilterOrderElement
                .getSelectedElement();
        if ((orderElement != null)
                && ((orderElement.getCode() != null) && (!orderElement
                        .getCode().isEmpty()))) {
            try {
                return workReportModel.findOrderElement(orderElement.getCode());
            } catch (InstanceNotFoundException e) {
                throw new WrongValueException(bandboxFilterOrderElement,
                        _("Task not found"));
            }
        }
        return null;
    }

    private TypeOfWorkHours getSelectedHoursType() {
        Comboitem itemSelected = filterHoursType.getSelectedItem();
        if ((itemSelected != null)
                && (((TypeOfWorkHours) itemSelected.getValue()) != null)) {
            return (TypeOfWorkHours) itemSelected.getValue();
        }
        return null;
    }

    /**
     * Method to manage the query work report lines
     */
    public List<WorkReportLine> getQueryWorkReportLines() {
        List<WorkReportLine> result = workReportModel.getAllWorkReportLines();
        updateSummary(result);
        return result;
    }

    public void sortQueryWorkReportLines() {
        Column columnDateLine = (Column) listQueryWindow.getFellow("date");
        if (columnDateLine != null) {
            if (columnDateLine.getSortDirection().equals("ascending")) {
                columnDateLine.sort(false, false);
                columnDateLine.setSortDirection("ascending");
            } else if (columnDateLine.getSortDirection().equals("descending")) {
                columnDateLine.sort(true, false);
                columnDateLine.setSortDirection("descending");
            }
        }
    }

    public void goToEditFormQuery(WorkReportLine line) {
        WorkReport workReport = line.getWorkReport();
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_TIMESHEETS)) {
            workReportCRUD.goToEditForm(workReport);
        } else if (SecurityUtils.isUserInRole(UserRole.ROLE_BOUND_USER)
                && workReportModel.isPersonalTimesheet(workReport)
                && belongsToCurrentUser(line)) {
            personalTimesheetController
                    .goToCreateOrEditForm(line.getLocalDate());
        } else {
            messagesForUser.showMessage(Level.WARNING,
                    _("You do not have permissions to edit this timesheet"));
        }
    }

    private boolean belongsToCurrentUser(WorkReportLine line) {
        User user = UserUtil.getUserFromSession();
        return line.getResource().getId().equals(user.getWorker().getId());
    }

    /**
     *
     * @author Diego Pino García <dpino@igalia.com>
     *
     */
    class WorkReportLineSummary {

        private Integer totalTasks;

        private EffortDuration totalHours;

        private WorkReportLineSummary(Integer totalTasks,
                EffortDuration totalHours) {
            this.totalTasks = totalTasks;
            this.totalHours = totalHours;
        }

        public String getTotalTasks() {
            return totalTasks.toString();
        }

        public String getTotalHours() {
            return totalHours.toFormattedString();
        }

        public String toString() {
            return _("Tasks") + " " + getTotalTasks() + ". "
                    + _("Total hours") + " " + getTotalHours() + ".";
        }

    }

}
