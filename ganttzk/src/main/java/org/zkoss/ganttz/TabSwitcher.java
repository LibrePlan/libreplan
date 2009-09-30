package org.zkoss.ganttz;

import org.zkoss.ganttz.adapters.TabsConfiguration;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.util.IMenuItemsRegister;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class TabSwitcher extends HtmlMacroComponent {

    private TabsConfiguration configuration;

    private TabsRegistry tabsRegistry;
    private Component container = null;

    public TabSwitcher() {
    }

    public TabSwitcher(TabsConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setConfiguration(TabsConfiguration configuration) {
        this.configuration = configuration;
        addTabsFromComfiguration(configuration);
    }

    private void addTabsFromComfiguration(TabsConfiguration configuration) {
        container.getChildren().clear();
        tabsRegistry = new TabsRegistry(container);
        for (ITab tab : configuration.getTabs()) {
            tabsRegistry.add(tab);
        }
        tabsRegistry.registerAtMenu(getMenuItemsRegisterLocator().retrieve());
        tabsRegistry.showFirst();
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        container = getFellow("container");
        if (configuration != null) {
            addTabsFromComfiguration(configuration);
        }
    }

    private OnZKDesktopRegistry<IMenuItemsRegister> getMenuItemsRegisterLocator() {
        return OnZKDesktopRegistry.getLocatorFor(IMenuItemsRegister.class);
    }

}
