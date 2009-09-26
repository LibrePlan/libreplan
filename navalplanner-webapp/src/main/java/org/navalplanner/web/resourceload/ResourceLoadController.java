package org.navalplanner.web.resourceload;

import java.util.ArrayList;
import java.util.Date;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller for global resourceload view
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceLoadController extends GenericForwardComposer {

    public ResourceLoadController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        ResourcesLoadPanel resourcesLoadPanel = buildResourcesLoadPanel();
        comp.appendChild(resourcesLoadPanel);
        resourcesLoadPanel.afterCompose();
    }

    private ResourcesLoadPanel buildResourcesLoadPanel() {
        return new ResourcesLoadPanel(new ArrayList<LoadTimelinesGroup>(),
                getDummyTimeTracker());
    }

    private TimeTracker getDummyTimeTracker() {
        Interval interval = new Interval(toDate(new LocalDate(2008, 6, 10)),
                toDate(new LocalDate(2011, 6, 10)));
        return new TimeTracker(interval);
    }

    private Date toDate(LocalDate localDate) {
        return localDate.toDateTimeAtStartOfDay().toDate();
    }

}
