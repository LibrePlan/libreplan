package org.navalplanner.business.workorders.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.NotNull;

public abstract class TaskWork {
    private long id;

    @NotNull
    private String name;

    private Date initDate;

    private Date endDate;

    private Set<ActivityWork> activityWorks = new HashSet<ActivityWork>();

    public int getWorkHours() {
        int result = 0;
        Set<ActivityWork> a = activityWorks;
        for (ActivityWork activityWork : a) {
            result += activityWork.getWorkingHours();
        }
        return result;
    }

    public void addActivity(ActivityWork activityWork) {
        activityWorks.add(activityWork);
    }

    public List<ActivityWork> getActivities() {
        return new ArrayList<ActivityWork>(activityWorks);
    }

    public long getId() {
        return id;
    }

    /**
     * @return the duration in milliseconds
     */
    public long getDuration() {
        return endDate.getTime() - initDate.getTime();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
