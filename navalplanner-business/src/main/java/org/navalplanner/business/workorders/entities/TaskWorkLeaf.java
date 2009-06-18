package org.navalplanner.business.workorders.entities;

import java.util.ArrayList;
import java.util.List;

public class TaskWorkLeaf extends TaskWork {

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

    public void remove(TaskWork taskWork) {

    }

    @Override
    public TaskWorkContainer asContainer() {
        TaskWorkContainer result = new TaskWorkContainer();
        result.setName(getName());
        result.setInitDate(getInitDate());
        result.setEndDate(getEndDate());
        result.setActivities(getActivities());
        return result;
    }

    @Override
    public void replace(TaskWork old, TaskWork newTask) {
        throw new UnsupportedOperationException();
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
}
