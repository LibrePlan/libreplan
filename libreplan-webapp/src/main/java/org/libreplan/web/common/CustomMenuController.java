/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.web.common;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.libreplan.business.common.Registry;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.entrypoints.EntryPointsHandler;
import org.libreplan.web.common.entrypoints.EntryPointsHandler.ICapture;
import org.libreplan.web.planner.tabs.IGlobalViewEntryPoints;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.ganttz.util.IMenuItemsRegister;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Vbox;

/**
 * Controller for customMenu <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class CustomMenuController extends Div implements IMenuItemsRegister {

    public static class CustomMenuItem {

        private final String name;
        private final String unencodedURL;
        private final String encodedURL;
        private final List<CustomMenuItem> children;
        private boolean activeParent;
        private String helpLink;
        private boolean disabled;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return unencodedURL;
        }

        public String getEncodedUrl() {
            return encodedURL;
        }

        public List<CustomMenuItem> getChildren() {
            return children;
        }

        public boolean getHasChildren() {
            return !children.isEmpty();
        }

        public boolean getHasNotChildren() {
            return children.isEmpty();
        }

        public CustomMenuItem(String name, String url) {
            this(name, url, new ArrayList<CustomMenuItem>());
        }

        public CustomMenuItem(String name, String url, String helpLink) {
            this(name, url, new ArrayList<CustomMenuItem>());
            this.helpLink = helpLink;
        }

        public CustomMenuItem(String name, String url, boolean disabled) {
            this(name, url, new ArrayList<CustomMenuItem>());
            this.disabled = disabled;
        }

        public CustomMenuItem(String name, String url,
                List<CustomMenuItem> children) {
            this.name = name;
            this.unencodedURL = url;
            this.encodedURL = Executions.getCurrent().encodeURL(url);
            this.children = children;
            this.disabled = false;
            this.helpLink = "";
        }

        public void appendChildren(CustomMenuItem newChildren) {
            this.children.add(newChildren);
        }

        public boolean isActiveParent() {
            return activeParent;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public boolean contains(String requestPath) {
            for (CustomMenuItem item : thisAndChildren()) {
                if (requestContains(requestPath, item.unencodedURL)) {
                    return true;
                }
            }
            return false;
        }

        private List<CustomMenuItem> thisAndChildren() {
            List<CustomMenuItem> items = new ArrayList<CustomMenuItem>();
            items.add(this);
            items.addAll(children);
            for (CustomMenuItem child : children) {
                items.addAll(child.children);
            }
            return items;
        }

        private static boolean requestContains(String requestPath, String url) {
            return requestPath.startsWith(url);
        }

        public void setActive(boolean activeParent) {
            this.activeParent = activeParent;
        }

        public void setHelpLink(String helpLink) {
            this.helpLink = helpLink;
        }

    }

    private static IGlobalViewEntryPoints findGlobalViewEntryPoints() {
        return (IGlobalViewEntryPoints) getSpringContext().getBean(
                "globalView", IGlobalViewEntryPoints.class);
    }

    private static WebApplicationContext getSpringContext() {
        Execution current = Executions.getCurrent();
        HttpServletRequest request = (HttpServletRequest) current
                .getNativeRequest();
        ServletContext context = request.getSession().getServletContext();

        return WebApplicationContextUtils.getWebApplicationContext(context);
    }

    private List<CustomMenuItem> firstLevel;

    private IGlobalViewEntryPoints globalView;

    public CustomMenuController() {
        this.firstLevel = new ArrayList<CustomMenuItem>();
        this.globalView = findGlobalViewEntryPoints();
        initializeMenu();
        activateCurrentOne();
        getLocator().store(this);
    }

    private void activateCurrentOne() {
        String requestPath = Executions.getCurrent().getDesktop()
                .getRequestPath();
        for (CustomMenuItem ci : this.firstLevel) {
            if (ci.contains(requestPath)) {
                ci.setActive(true);
                for (CustomMenuItem child : ci.children) {
                    if (child.contains(requestPath)) {
                        child.setActive(true);
                        for (CustomMenuItem c : child.children) {
                            if (c.contains(requestPath)) {
                                c.setActive(true);
                                return;
                            }
                        }
                        return;
                    }
                }
                return;
            }
        }

        if (requestPath.isEmpty()) {
            CustomMenuItem item = this.firstLevel.get(0);
            item.setActive(true);
            item.children.get(0).setActive(true);
        }
    }

    private OnZKDesktopRegistry<IMenuItemsRegister> getLocator() {
        return OnZKDesktopRegistry.getLocatorFor(IMenuItemsRegister.class);
    }

    private CustomMenuController topItem(String name, String url,
            String helpUri, Collection<? extends CustomMenuItem> items) {
        return topItem(name, url, helpUri,
                items.toArray(new CustomMenuItem[items.size()]));
    }

    private CustomMenuController topItem(String name, String url,
            String helpUri,
            CustomMenuItem... items) {
        return topItem(name, url, helpUri, false, items);
    }

    private CustomMenuController topItem(String name, String url,
            String helpLink,
            boolean disabled, CustomMenuItem... items) {
        CustomMenuItem parent = new CustomMenuItem(name, url, disabled);
        parent.setHelpLink(helpLink);
        this.firstLevel.add(parent);
        for (CustomMenuItem child : items) {
            parent.appendChildren(child);
        }
        return this;
    }

    private CustomMenuItem subItem(String name, String url, String helpLink) {
        return new CustomMenuItem(name, url, helpLink);
    }

    private CustomMenuItem subItem(String name, ICapture urlCapture,
            String helpLink) {
        return new CustomMenuItem(name, EntryPointsHandler.capturePath(urlCapture),
                helpLink);
    }

    public void initializeMenu() {
        List<CustomMenuItem> planningItems = new ArrayList<CustomMenuItem>();
        if (SecurityUtils.isSuperuserOrRolePlanningOrHasAnyAuthorization()) {
            planningItems.add(subItem(_("Company view"), new ICapture() {
                @Override
                public void capture() {
                    globalView.goToCompanyScheduling();
                }
            }, "01-introducion.html"));
            planningItems.add(subItem(_("Projects"), new ICapture() {
                @Override
                public void capture() {
                    globalView.goToOrdersList();
                }
            }, "01-introducion.html#id2"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_PLANNING)) {
            planningItems.add(subItem(_("Resource Load"), new ICapture() {
                @Override
                public void capture() {
                    globalView.goToCompanyLoad();
                }
            }, "01-introducion.html#id1"));
            planningItems.add(subItem(_("Limiting Resources"), new ICapture() {
                @Override
                public void capture() {
                    globalView.goToLimitingResources();
                }
            }, "01-introducion.html"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_TEMPLATES)) {
            planningItems.add(subItem(_("Templates"),
                    "/templates/templates.zul", ""));
        }
        if (!planningItems.isEmpty()) {
            topItem(_("Planning"), "/planner/index.zul", "", planningItems);
        }

        List<CustomMenuItem> resourcesItems = new ArrayList<CustomMenuItem>();
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_WORKERS)) {
            resourcesItems.add(subItem(_("Workers"),
                    "/resources/worker/worker.zul",
                    "05-recursos.html#xesti-n-de-traballadores"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_MACHINES)) {
            resourcesItems.add(subItem(_("Machines"),
                    "/resources/machine/machines.zul",
                    "05-recursos.html#xesti-n-de-m-quinas"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_VIRTUAL_WORKERS)) {
            resourcesItems.add(subItem(_("Virtual Workers"),
                    "/resources/worker/virtualWorkers.zul",
                    "05-recursos.html#xesti-n-de-traballadores"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_CALENDARS)) {
            resourcesItems.add(subItem(_("Calendars"),
                    "/calendars/calendars.zul", "03-calendarios.html"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_CALENDAR_EXCEPTION_DAYS)) {
            resourcesItems.add(subItem(_("Calendar Exception Days"),
                    "/excetiondays/exceptionDays.zul", ""));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_CRITERIA)) {
            resourcesItems.add(subItem(_("Criteria"),
                    "/resources/criterions/criterions.zul",
                    "02-criterios.html#id1"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_PROGRESS_TYPES)) {
            resourcesItems.add(subItem(_("Progress Types"),
                    "/advance/advanceTypes.zul", "04-avances.html#id1"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_MATERIALS)) {
            resourcesItems.add(subItem(_("Labels"), "/labels/labelTypes.zul",
                    "10-etiquetas.html"));
            resourcesItems.add(subItem(_("Materials"),
                    "/materials/materials.zul",
                    "11-materiales.html#administraci-n-de-materiais"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_MATERIAL_UNITS)) {
            resourcesItems.add(subItem(_("Material Units"),
                    "/unittypes/unitTypes.zul",
                    "11-materiales.html#administraci-n-de-materiais"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_QUALITY_FORMS)) {
            resourcesItems
                    .add(subItem(_("Quality Forms"),
                            "/qualityforms/qualityForms.zul",
                            "12-formularios-calidad.html#administraci-n-de-formularios-de-calidade"));
        }
        if (!resourcesItems.isEmpty()) {
            topItem(_("Resources"), "/resources/worker/worker.zul", "",
                    resourcesItems);
        }

        List<CustomMenuItem> costItems = new ArrayList<CustomMenuItem>();
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_TIMESHEETS)) {
            costItems.add(subItem(_("Timesheets"),
                    "/workreports/workReport.zul", "09-partes.html#id3"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_TIMESHEETS_TEMPLATES)) {
            costItems.add(subItem(_("Timesheets Templates"),
                    "/workreports/workReportTypes.zul", "09-partes.html#id2"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_EXPENSES)) {
            costItems.add(subItem(_("Expenses"),
                    "/expensesheet/expenseSheet.zul", ""));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_COST_CATEGORIES)) {
            costItems.add(subItem(_("Cost Categories"),
                    "/costcategories/costCategory.zul",
                    "14-custos.html#categor-as-de-custo"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_HOURS_TYPES)) {
            costItems.add(subItem(_("Hours Types"),
                    "/typeofworkhours/typeOfWorkHours.zul",
                    "14-custos.html#administraci-n-de-horas-traballadas"));
        }
        if (!costItems.isEmpty()) {
            topItem(_("Cost"), "/workreports/workReport.zul", "", costItems);
        }

        List<CustomMenuItem> configurationItems = new ArrayList<CustomMenuItem>();
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_MAIN_SETTINGS)) {
            configurationItems
                    .add(subItem(_("Main Settings"),
                            "/common/configuration.zul",
                            "16-ldap-authentication.html"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_USER_ACCOUNTS)) {
            configurationItems.add(subItem(_("User Accounts"),
                    "/users/users.zul",
                    "13-usuarios.html#administraci-n-de-usuarios"));
        }
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_PROFILES)) {
            configurationItems.add(subItem(_("Profiles"),
                    "/profiles/profiles.zul",
                    "13-usuarios.html#administraci-n-de-perfiles"));
        }
        if (!configurationItems.isEmpty()) {
            topItem(_("Configuration"), "/common/configuration.zul", "",
                    configurationItems);
        }

        List<CustomMenuItem> communicationsItems = new ArrayList<CustomMenuItem>();
        if (SecurityUtils.isSuperuserOrUserInRoles(UserRole.ROLE_COMPANIES)) {
            communicationsItems.add(subItem(_("Companies"),
                    "/externalcompanies/externalcompanies.zul", ""));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_SEND_TO_SUBCONTRACTORS)) {
            communicationsItems.add(subItem(_("Send To Subcontractors"),
                    "/subcontract/subcontractedTasks.zul", ""));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_RECEIVED_FROM_SUBCONTRACTORS)) {
            communicationsItems.add(subItem(_("Received From Subcontractors"),
                    "/subcontract/subcontractorCommunications.zul", ""));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_SEND_TO_CUSTOMERS)) {
            communicationsItems.add(subItem(_("Send To Customers"),
                    "/subcontract/reportAdvances.zul", ""));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_RECEIVED_FROM_CUSTOMERS)) {
            communicationsItems.add(subItem(_("Received From Customers"),
                    "/subcontract/customerCommunications.zul", ""));
        }
        if (!communicationsItems.isEmpty()) {
            topItem(_("Communications"),
                    "/externalcompanies/externalcompanies.zul", "",
                    communicationsItems);
        }

        List<CustomMenuItem> reportsItems = new ArrayList<CustomMenuItem>();
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_TIMESHEET_LINES_LIST_REPORT)) {
            reportsItems.add(subItem(_("Timesheet Lines List"),
                    "/workreports/workReportQuery.zul", "09-partes.html#id4"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_HOURS_WORKED_PER_RESOURCE_REPORT)) {
            reportsItems.add(subItem(_("Hours Worked Per Resource"),
                    "/reports/hoursWorkedPerWorkerReport.zul",
                    "15-1-report-hours-worked-by-resource.html"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_TOTAL_WORKED_HOURS_BY_RESOURCE_IN_A_MONTH_REPORT)) {
            reportsItems.add(subItem(
                    _("Total Worked Hours By Resource In A Month"),
                    "/reports/hoursWorkedPerWorkerInAMonthReport.zul",
                    "15-2-total-hours-by-resource-month.html"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_WORK_AND_PROGRESS_PER_PROJECT_REPORT)) {
            reportsItems.add(subItem(_("Work And Progress Per Project"),
                    "/reports/schedulingProgressPerOrderReport.zul",
                    "15-3-work-progress-per-project.html"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_WORK_AND_PROGRESS_PER_TASK_REPORT)) {
            reportsItems.add(subItem(_("Work And Progress Per Task"),
                    "/reports/workingProgressPerTaskReport.zul",
                    "15-informes.html"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_ESTIMATED_PLANNED_HOURS_PER_TASK_REPORT)) {
            reportsItems.add(subItem(_("Estimated/Planned Hours Per Task"),
                    "/reports/completedEstimatedHoursPerTask.zul",
                    "15-informes.html"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_PROJECT_COSTS_REPORT)) {
            reportsItems.add(subItem(_("Project Costs"),
                    "/reports/orderCostsPerResource.zul", "15-informes.html"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_TASK_SCHEDULING_STATUS_IN_PROJECT_REPORT)) {
            reportsItems.add(subItem(_("Task Scheduling Status In Project"),
                    "/reports/workingArrangementsPerOrderReport.zul",
                    "15-informes.html"));
        }
        if (SecurityUtils
                .isSuperuserOrUserInRoles(UserRole.ROLE_MATERIALS_NEED_AT_DATE_REPORT)) {
            reportsItems.add(subItem(_("Materials Needs At Date"),
                    "/reports/timeLineMaterialReport.zul", "15-informes.html"));
        }
        if (!reportsItems.isEmpty()) {
            topItem(_("Reports"), "/reports/hoursWorkedPerWorkerReport.zul",
                    "", reportsItems);
        }

        List<CustomMenuItem> personalAreaItems = new ArrayList<CustomMenuItem>();
        if (SecurityUtils.isUserInRole(UserRole.ROLE_BOUND_USER)) {
            personalAreaItems.add(subItem(_("Home"),
                    "/myaccount/userDashboard.zul", ""));
        }
        personalAreaItems.add(subItem(_("Preferences"),
                "/myaccount/settings.zul", ""));
        personalAreaItems.add(subItem(_("Change Password"),
                "/myaccount/changePassword.zul", ""));
        topItem(_("Personal area"), "/myaccount/userDashboard.zul", "",
                personalAreaItems);
    }

    private Vbox getRegisteredItemsInsertionPoint() {
        return (Vbox) getPage().getFellow("registeredItemsInsertionPoint");
    }

    public List<CustomMenuItem> getCustomMenuItems() {
        return this.firstLevel;
    }

    public List<CustomMenuItem> getBreadcrumbsPath() {
        List<CustomMenuItem> breadcrumbsPath = new ArrayList<CustomMenuItem>();
        for (CustomMenuItem ci : this.firstLevel) {
            if (ci.isActiveParent()) {
                if ((ci.name != null) && (ci.name != _("Planning"))) {
                    breadcrumbsPath.add(ci);
                    for (CustomMenuItem child : ci.children) {
                        if (child.isActiveParent()) {
                            breadcrumbsPath.add(child);
                            for (CustomMenuItem c : child.children) {
                                if (c.isActiveParent()) {
                                    breadcrumbsPath.add(c);
                                }
                            }
                        }
                    }
                }
            }
        }
        return breadcrumbsPath;
    }

    public String getHelpLink() {
        String helpLink = "index.html";
        for (CustomMenuItem ci : this.firstLevel) {
            if (ci.isActiveParent()) {
                if ((ci.name != null)) {
                    for (CustomMenuItem child : ci.children) {
                        if (child.isActiveParent()
                                && !child.helpLink.equals("")) {
                            helpLink = child.helpLink;
                        }
                    }
                }
            }
        }
        return helpLink;
    }

    public List<CustomMenuItem> getCustomMenuSecondaryItems() {
        for (CustomMenuItem ci : this.firstLevel) {
            if (ci.isActiveParent()) {
                return ci.getChildren();
            }
        }
        return Collections.<CustomMenuItem> emptyList();

    }

    private Button currentOne = null;

    @Override
    public Object addMenuItem(String name, String cssClass,
            org.zkoss.zk.ui.event.EventListener eventListener) {
        Vbox insertionPoint = getRegisteredItemsInsertionPoint();
        Button button = new Button();
        button.setLabel(_(name));
        if (cssClass != null) {
            toggleDomainCssClass(cssClass, button);
        }
        setDeselectedClass(button);
        button.addEventListener(Events.ON_CLICK, doNotCallTwice(button,
                eventListener));
        button.setMold("trendy");
        insertionPoint.appendChild(button);
        insertionPoint.appendChild(separator());
        return button;
    }

    @Override
    public void activateMenuItem(Object key) {
        switchCurrentButtonTo((Button) key);
    }

    @Override
    public void renameMenuItem(Object key, String name, String cssClass) {
        Button button = (Button) key;
        button.setLabel(name);
        if (cssClass != null) {
            toggleDomainCssClass(cssClass, button);
        }
    }

    private void toggleDomainCssClass(String cssClass, Button button) {
        Matcher matcher = perspectiveCssClass
                .matcher(button.getSclass() == null ? "" : button.getSclass());
        String previousPerspectiveClass;
        if (matcher.find()) {
            previousPerspectiveClass = matcher.group();
        } else {
            previousPerspectiveClass = "";
        }
        button.setSclass(previousPerspectiveClass + " " + cssClass);
    }

    @Override
    public void toggleVisibilityTo(Object key, boolean visible) {
        Button button = (Button) key;
        button.setVisible(visible);
        button.getNextSibling().setVisible(visible);
    }

    private void setSelectClass(final Button button) {
        togglePerspectiveClassTo(button, "perspective-active");
    }

    private void setDeselectedClass(Button button) {
        togglePerspectiveClassTo(button, "perspective");
    }

    private static final Pattern perspectiveCssClass = Pattern
    .compile("\\bperspective(-\\w+)?\\b");

    private void togglePerspectiveClassTo(final Button button,
            String newPerspectiveClass) {
        button
                .setSclass(togglePerspectiveCssClass(newPerspectiveClass,
                        button));
    }

    private String togglePerspectiveCssClass(String newPerspectiveClass,
            Button button) {
        String sclass = button.getSclass();
        if (!perspectiveCssClass.matcher(sclass).find()) {
            return newPerspectiveClass + " " + sclass;
        } else {
            Matcher matcher = perspectiveCssClass.matcher(sclass);
            return matcher.replaceAll(newPerspectiveClass);
        }
    }

    private EventListener doNotCallTwice(final Button button,
            final org.zkoss.zk.ui.event.EventListener originalListener) {
        return new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                if (currentOne == button) {
                    return;
                }
                switchCurrentButtonTo(button);
                originalListener.onEvent(event);
            }
        };
    }

    private Component separator() {
        Div div = new Div();
        div.setSclass("vertical-separator");
        return div;
    }

    public String getContextPath() {
        return Executions.getCurrent().getContextPath();
    }

    private void switchCurrentButtonTo(final Button button) {
        if (currentOne == button) {
            return;
        }
        if (currentOne != null) {
            setDeselectedClass(currentOne);
        }
        setSelectClass(button);
        currentOne = button;
    }

    public boolean isScenariosVisible() {
        return Registry.getConfigurationDAO()
                .getConfigurationWithReadOnlyTransaction()
                .isScenariosVisible();
    }

}