package org.navalplanner.web.resourceload;

import java.util.List;

import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.ganttz.util.Interval;

public interface IResourceLoadModel {

    void initGlobalView();

    List<LoadTimelinesGroup> getLoadTimeLines();

    Interval getViewInterval();

}
