package org.navalplanner.business.workorders.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskWorkLeaf extends TaskWork {

    private Boolean fixedHours = false;

    private Set<ActivityWork> activityWorks = new HashSet<ActivityWork>();

    @Override
    public Integer getWorkHours() {
        int result = 0;
        List<ActivityWork> a = getActivities();
        for (ActivityWork activityWork : a) {
            Integer workingHours = activityWork.getWorkingHours();
            if (workingHours != null) {
                result += workingHours;
            }
        }
        return result;
    }

    @Override
    public List<TaskWork> getChildren() {
        return new ArrayList<TaskWork>();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public TaskWorkContainer asContainer() {
        TaskWorkContainer result = new TaskWorkContainer();
        result.setName(getName());
        result.setInitDate(getInitDate());
        result.setEndDate(getEndDate());
        // FIXME
        // result.setActivities(getActivities());
        return result;
    }

    public void setWorkHours(Integer workingHours) {
        List<ActivityWork> activities = getActivities();

        // FIXME For the moment we have just one activity for each TaksWorkLeaf
        if (activities.isEmpty()) {
            ActivityWork activity = new ActivityWork();
            activity.setWorkingHours(workingHours);

            activities.add(activity);
        } else {
            ActivityWork activity = activities.get(0);
            activity.setWorkingHours(workingHours);
        }

        setActivities(activities);
    }

    public void setActivities(List<ActivityWork> activities) {
        this.activityWorks = new HashSet<ActivityWork>(activities);
    }

    public void addActivity(ActivityWork activityWork) {
        activityWorks.add(activityWork);
    }

    public void deleteActivity(ActivityWork value) {
        activityWorks.remove(value);
    }

    @Override
    public List<ActivityWork> getActivities() {
        return new ArrayList<ActivityWork>(activityWorks);
    }

    @Override
    public void forceLoadActivities() {
        for (ActivityWork activityWork : activityWorks) {
            activityWork.getWorkingHours();
        }
    }

    public void setFixedHours(Boolean fixedHours) {
        this.fixedHours = fixedHours;
    }

    public Boolean isFixedHours() {
        return fixedHours;
    }

}
