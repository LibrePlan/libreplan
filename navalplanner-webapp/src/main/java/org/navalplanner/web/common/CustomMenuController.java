package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.ganttz.util.IMenuItemsRegister;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
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
        private boolean activeParent;

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

        public boolean isActiveParent() {
            return activeParent;
        }

        public boolean contains(String requestPath) {
            for (CustomMenuItem item : thisAndChildren()) {
                if (requestContains(requestPath, item.unencodedURL))
                    return true;
            }
            return false;
        }

        private List<CustomMenuItem> thisAndChildren() {
            List<CustomMenuItem> items = new ArrayList<CustomMenuItem>();
            items.add(this);
            items.addAll(children);
            return items;
        }

        private static boolean requestContains(String requestPath, String url) {
            return requestPath.startsWith(url);
        }

        public void setActive(boolean activeParent) {
            this.activeParent = activeParent;
        }

    }

    public CustomMenuController() {
        this.firstLevel = new ArrayList<CustomMenuItem>();
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
                break;
            }
        }
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

        topItem(_("Resources"), "/resources/worker/worker.zul",
                subItem(
                        _("Workers List"),
                        "/resources/worker/worker.zul#list"),
                subItem(
                        _("Manage criterions"),
                        "/resources/criterions/criterions.zul"));

        topItem(_("Orders"),
                "/orders/orders.zul",
                subItem(_("Orders list"),
                        "/orders/orders.zul"),
                subItem(_("Work activities types"),
                        "/orders/orders.zul"),
                subItem(_("Models"),
                        "/orders/orders.zul"));

        topItem(_("Work reports"), "/workreports/workReportTypes.zul", subItem(
                _("Work report types"), "/workreports/workReportTypes.zul"),
                subItem(_("Work report list"),
                        "/workreports/workReport.zul#list"));

        topItem(_("Administration"),
                "/advance/advanceTypes.zul",
                subItem(_("Manage advances types"),
                        "/advance/advanceTypes.zul"),
                subItem(_("Calendars"),
                        "/calendars/calendars.zul"),
                subItem(_("Label types"),
                        "/labels/labelTypes.zul"));

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
        for (CustomMenuItem ci : this.firstLevel) {
            if (ci.isActiveParent())
                return ci.getChildren();
        }
        return Collections.<CustomMenuItem> emptyList();
    }

    private Button currentOne = null;

    @Override
    public void addMenuItem(String name,
            org.zkoss.zk.ui.event.EventListener eventListener) {
        Hbox insertionPoint = getRegisteredItemsInsertionPoint();
        Button button = new Button();
        button.setLabel(_(name));
        setDeselectedClass(button);
        button.addEventListener(Events.ON_CLICK, doNotCallTwice(button,
                eventListener));
        insertionPoint.appendChild(button);
        insertionPoint.appendChild(separator());

    }

    private void setSelectClass(final Button button) {
        button.setSclass("sub_menu_active");
    }

    private void setDeselectedClass(Button button) {
        button.setSclass("sub_menu");
    }

    private EventListener doNotCallTwice(final Button button,
            final org.zkoss.zk.ui.event.EventListener originalListener) {
        return new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                if (currentOne == button) {
                    return;
                }
                if (currentOne != null) {
                    currentOne.setSclass("sub_menu");
                    setDeselectedClass(currentOne);
                }
                setSelectClass(button);
                currentOne = button;
                originalListener.onEvent(event);
            }
        };
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
