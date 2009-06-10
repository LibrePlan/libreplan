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

    public Integer getWorkHours() {
        int result = 0;
        Set<ActivityWork> a = activityWorks;
        for (ActivityWork activityWork : a) {
            Integer workingHours = activityWork.getWorkingHours();
            if (workingHours != null) {
                result += workingHours;
            }
        }
        return result;
    }

    public void setActivities(List<ActivityWork> activities) {
        this.activityWorks = new HashSet<ActivityWork>(activities);
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

    public abstract boolean isLeaf();

    public abstract List<TaskWork> getChildren();

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public abstract void remove(TaskWork lastAsTask);

    public abstract void replace(TaskWork old, TaskWork newTask);

    public abstract TaskWorkContainer asContainer();

    public void forceLoadActivities() {
        for (ActivityWork activityWork : activityWorks) {
            activityWork.getWorkingHours();
        }
    }
}
