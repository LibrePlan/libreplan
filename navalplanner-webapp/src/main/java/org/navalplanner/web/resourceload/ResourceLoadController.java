package org.navalplanner.web.resourceload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller for global resourceload view
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceLoadController extends GenericForwardComposer {

    @Autowired
    private IResourceLoadModel resourceLoadModel;

    public ResourceLoadController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        resourceLoadModel.initGlobalView();
        ResourcesLoadPanel resourcesLoadPanel = buildResourcesLoadPanel();
        comp.appendChild(resourcesLoadPanel);
        resourcesLoadPanel.afterCompose();
    }

    private ResourcesLoadPanel buildResourcesLoadPanel() {
        return new ResourcesLoadPanel(resourceLoadModel.getLoadTimeLines(),
                new TimeTracker(resourceLoadModel.getViewInterval()));
    }
}
