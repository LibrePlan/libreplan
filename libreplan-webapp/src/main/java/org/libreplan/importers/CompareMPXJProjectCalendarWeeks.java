package org.libreplan.importers;

import java.util.Comparator;

import net.sf.mpxj.ProjectCalendarWeek;

public class CompareMPXJProjectCalendarWeeks implements
        Comparator<ProjectCalendarWeek> {

    @Override
    public int compare(ProjectCalendarWeek o1, ProjectCalendarWeek o2) {

        return o1.getDateRange().getStart()
                .compareTo(o2.getDateRange().getStart());
    }

}
