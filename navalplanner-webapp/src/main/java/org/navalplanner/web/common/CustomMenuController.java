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
        private final String encodedURL;
        private final String unencodedURL;
        private final List<CustomMenuItem> children;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return encodedURL;
        }

        public List<CustomMenuItem> getChildren() {
            return children;
        }

        public CustomMenuItem(String name, String url) {
            this(name, url, new ArrayList<CustomMenuItem>());
        }

        public CustomMenuItem(String name, String url,
                List<CustomMenuItem> children) {
            this.name = name;
            this.encodedURL = Executions.getCurrent().encodeURL(url);
            this.unencodedURL = url;
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
                "/planner/main.zul");

        topItem(
                _("Resources"),
                "/resources/worker/worker.zul",
                subItem(_("Workers List"),
                        "/resources/worker/worker.zul#list"),
                subItem(_("Manage criterions"),
                        "/resources/criterions/criterions.zul"));

        topItem(_("Orders"),
                "/orders/orders.zul",
                subItem(_("Orders list"),
                        "/orders/orders.zul"),
                subItem(_("Work activities types"),
                        "/orders/orders.zul"),
                subItem(_("Models"),
                        "/orders/orders.zul"));

        topItem( _("Work reports"),
                "/workreports/workReportTypes.zul",
                subItem(_("Work report types"),
                        "/workreports/workReportTypes.zul"),
                subItem(_("Work report list"),
                        "/workreports/workReport.zul#list"));

        topItem(_("Administration"),
                "/advance/advanceTypes.zul",
                subItem(_("Manage advances types"),
                        "/advance/advanceTypes.zul"),
                subItem(_("Calendars"),
                        "/calendars/calendars.zul"));

        topItem(_("Quality management"),
                "/");
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
