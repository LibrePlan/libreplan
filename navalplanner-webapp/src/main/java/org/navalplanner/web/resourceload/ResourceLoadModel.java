package org.navalplanner.web.resourceload;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.ganttz.util.Interval;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceLoadModel implements IResourceLoadModel {

    @Override
    public void initGlobalView() {
    }

    @Override
    public List<LoadTimelinesGroup> getLoadTimeLines() {
        return new ArrayList<LoadTimelinesGroup>();
    }

    @Override
    public Interval getViewInterval() {
        return new Interval(toDate(new LocalDate(2008, 6, 10)),
                toDate(new LocalDate(
                2011, 6, 10)));
    }

    private Date toDate(LocalDate localDate) {
        return localDate.toDateTimeAtStartOfDay().toDate();
    }

}
