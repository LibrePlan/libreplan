package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.ganttz.util.IMenuItemsRegister;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;

/**
 * Controller for customMenu <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class CustomMenuController extends Div implements IMenuItemsRegister {

    private List<CustomMenuItem> firstLevel;

    public static class CustomMenuItem {

        private final String name;
        private final String url;
        private final List<CustomMenuItem> children;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public List<CustomMenuItem> getChildren() {
            return children;
        }

        public CustomMenuItem(String name, String url) {
            this.name = name;
            this.url = url;
            this.children = new ArrayList<CustomMenuItem>();
        }

        public CustomMenuItem(String name, String url,
                List<CustomMenuItem> children) {
            this.name = name;
            this.url = url;
            this.children = children;
        }

        public void appendChildren(CustomMenuItem newChildren) {
            this.children.add(newChildren);
        }

        public boolean getActiveParent() {
            String requestPath = Executions.getCurrent().getDesktop()
                    .getRequestPath();
            if (requestPath.contains(url) || url.contains(requestPath)) {
                return true;
            }
            return false;
        }

    }

    public CustomMenuController() {
        this.firstLevel = new ArrayList<CustomMenuItem>();
        initializeMenu();
        getLocator().store(this);
    }

    private OnZKDesktopRegistry<IMenuItemsRegister> getLocator() {
        return OnZKDesktopRegistry.getLocatorFor(IMenuItemsRegister.class);
    }

    private CustomMenuController topItem(String name, String url,
            CustomMenuItem... items) {
        CustomMenuItem parent = new CustomMenuItem(name, url);
        this.firstLevel.add(parent);
        for (CustomMenuItem child : items) {
            parent.appendChildren(child);
        }
        return this;
    }

    private CustomMenuItem subItem(String name, String url) {
        return new CustomMenuItem(name, url);
    }

    public void initializeMenu() {
        topItem(
                _("Planification"),
                "/navalplanner-webapp/planner/main.zul");
        topItem(
                _("Resources"),
                "/navalplanner-webapp/resources/worker/worker.zul",
                subItem(_("Workers List"),
                        "/navalplanner-webapp/resources/worker/worker.zul#list"),
                subItem(_("Manage criterions"),
                        "/navalplanner-webapp/resources/criterions/criterions.zul"));

        topItem(_("Orders"),
                "/navalplanner-webapp/orders/orders.zul",
                subItem(_("Orders list"),
                        "/navalplanner-webapp/orders/orders.zul"),
                subItem(_("Work activities types"),
                        "/navalplanner-webapp/orders/orders.zul"),
                subItem(_("Models"),
                        "/navalplanner-webapp/orders/orders.zul"));

        topItem( _("Work reports"),
                "/navalplanner-webapp/workreports/workReportTypes.zul",
                subItem(_("Work report types"),
                        "/navalplanner-webapp/workreports/workReportTypes.zul"),
                subItem(_("Work report list"),
                        "/navalplanner-webapp/workreports/workReport.zul#list"));

        topItem(_("Administration"),
                "/navalplanner-webapp/advance/advanceTypes.zul",
                subItem(_("Manage advances types"),
                        "/navalplanner-webapp/advance/advanceTypes.zul"),
                subItem(_("Calendars"),
                        "/navalplanner-webapp/calendars/calendars.zul"));

        topItem(_("Quality management"),
                "/navalplanner-webapp/");
    }

    private Hbox getRegisteredItemsInsertionPoint() {
        return (Hbox) getFellow("registeredItemsInsertionPoint");
    }

    public List<CustomMenuItem> getCustomMenuItems() {
        return this.firstLevel;
    }

    public List<CustomMenuItem> getCustomMenuSecondaryItems() {
        String requestPath = Executions.getCurrent().getDesktop()
                .getRequestPath();
        for (CustomMenuItem ci : this.firstLevel) {
            if (requestPath.contains(ci.url) || ci.url.contains(requestPath)) {
                return ci.getChildren();
            }
        }
        return this.firstLevel.get(0).getChildren();
    }

    @Override
    public void addMenuItem(String name,
            org.zkoss.zk.ui.event.EventListener eventListener) {
        Hbox insertionPoint = getRegisteredItemsInsertionPoint();
        Button button = new Button();
        button.setLabel(_(name));
        button.setSclass(true ? "sub_menu" : "sub_menu");
        button.addEventListener(Events.ON_CLICK, eventListener);
        insertionPoint.appendChild(button);
        insertionPoint.appendChild(separator());
    }

    private Component separator() {
        Div div = new Div();
        div.setSclass("vertical_separator");
        return div;
    }

    public String getContextPath() {
        return Executions.getCurrent().getContextPath();
    }

}
