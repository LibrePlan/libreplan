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

    public class CustomMenuItem {

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
        initializeMenu();
        getLocator().store(this);
    }

    private OnZKDesktopRegistry<IMenuItemsRegister> getLocator() {
        return OnZKDesktopRegistry.getLocatorFor(IMenuItemsRegister.class);
    }

    public void initializeMenu() {
        List<CustomMenuItem> l = new ArrayList<CustomMenuItem>();
        CustomMenuItem ci;

        ci = new CustomMenuItem(_("Planification"),
                "/navalplanner-webapp/planner/main.zul");
        l.add(ci);

        ci = new CustomMenuItem(_("Resources"),
                "/navalplanner-webapp/resources/worker/worker.zul");
        ci.appendChildren(new CustomMenuItem(_("Workers list"),
                "/navalplanner-webapp/resources/worker/worker.zul#list"));
        ci.appendChildren(new CustomMenuItem(_("Manage criterions"),
                "/navalplanner-webapp/resources/criterions/criterions.zul"));
        l.add(ci);

        ci = new CustomMenuItem(_("Orders"),
                "/navalplanner-webapp/orders/orders.zul");
        ci.appendChildren(new CustomMenuItem(_("Orders list"),
                "/navalplanner-webapp/orders/orders.zul"));
        ci.appendChildren(new CustomMenuItem(_("Work activities types"),
                "/navalplanner-webapp/orders/orders.zul"));
        ci.appendChildren(new CustomMenuItem(_("Models"),
                "/navalplanner-webapp/orders/orders.zul"));
        l.add(ci);

        ci = new CustomMenuItem(_("Work reports"),
                "/navalplanner-webapp/workreports/workReportTypes.zul");
        ci.appendChildren(new CustomMenuItem(_("Work report types"),
                "/navalplanner-webapp/workreports/workReportTypes.zul"));
        ci.appendChildren(new CustomMenuItem(_("Work report list"),
                "/navalplanner-webapp/workreports/workReport.zul#list"));
        l.add(ci);

        ci = new CustomMenuItem(_("Quality management"),
                "/navalplanner-webapp/");
        l.add(ci);

        ci = new CustomMenuItem(_("Administration"),
                "/navalplanner-webapp/advance/advanceTypes.zul");
        ci.appendChildren(new CustomMenuItem(_("Manage advances types"),
                "/navalplanner-webapp/advance/advanceTypes.zul"));
        ci.appendChildren(new CustomMenuItem(_("Calendars"),
                "/navalplanner-webapp/calendars/calendars.zul"));
        l.add(ci);

        this.firstLevel = l;
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
