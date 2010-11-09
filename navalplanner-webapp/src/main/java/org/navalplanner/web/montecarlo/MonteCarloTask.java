package org.navalplanner.web.montecarlo;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.workingday.EffortDuration;

/**
 *
 * @author Diego Pino Garcia<dpino@igalia.com>
 *
 */
public class MonteCarloTask {

    public static MonteCarloTask create(Task task) {
        return new MonteCarloTask(task);
    }

    public static MonteCarloTask copy(MonteCarloTask task) {
        return new MonteCarloTask(task);
    }

    public static BigDecimal calculateRealDurationFor(MonteCarloTask task, BigDecimal daysDuration) {
        LocalDate start = new LocalDate(task.getStartDate());
        Validate.notNull(start);
        LocalDate end = calculateEndDateFor(task, daysDuration);
        Days daysBetween = Days.daysBetween(start, end);
        return BigDecimal.valueOf(daysBetween.getDays());
    }

    private static LocalDate calculateEndDateFor(MonteCarloTask task, BigDecimal daysDuration) {
        BaseCalendar calendar = task.getCalendar();
        LocalDate start = new LocalDate(task.getStartDate());
        LocalDate day = start;

        int duration = daysDuration.intValue();
        for (int i = 0; i < duration;) {
            EffortDuration workableTime = calendar.getWorkableTimeAt(day);
            if (!EffortDuration.zero().equals(workableTime)) {
                i++;
            }
            day = day.plusDays(1);
        }
        return day;
    }

    private Task task;

    private BigDecimal duration;

    private BigDecimal pessimisticDuration;

    private Integer pessimisticDurationPercentage;

    private BigDecimal normalDuration;

    private Integer normalDurationPercentage;

    private BigDecimal optimisticDuration;

    private Integer optimisticDurationPercentage;

    private MonteCarloTask(Task task) {
        this.task = task;
        duration = BigDecimal.valueOf(task.getDaysBetweenDates());
        pessimisticDuration = duration.multiply(BigDecimal.valueOf(1.50));
        pessimisticDurationPercentage = 30;
        normalDuration = duration;
        normalDurationPercentage = 50;
        optimisticDuration = duration.multiply(BigDecimal.valueOf(0.50));
        optimisticDurationPercentage = 20;
    }

    private MonteCarloTask(MonteCarloTask task) {
        this.task = task.getTask();
        this.duration = task.getDuration();
        this.pessimisticDuration = task.getPessimisticDuration();
        this.pessimisticDurationPercentage = task.getPessimisticDurationPercentage();
        this.normalDuration = task.getNormalDuration();
        this.normalDurationPercentage = task.getNormalDurationPercentage();
        this.optimisticDuration = task.getOptimisticDuration();
        this.optimisticDurationPercentage = task.getOptimisticDurationPercentage();
    }

    public Task getTask() {
        return task;
    }

    private Date getStartDate() {
        return task.getStartDate();
    }

    private BaseCalendar getCalendar() {
        return task.getCalendar();
    }

    public String getTaskName() {
        return task.getName();
    }

    public BigDecimal getDuration() {
        return duration;
    }

    public BigDecimal getPessimisticDuration() {
        return pessimisticDuration;
    }

    public Integer getPessimisticDurationPercentage() {
        return pessimisticDurationPercentage;
    }

    public BigDecimal getNormalDuration() {
        return normalDuration;
    }

    public Integer getNormalDurationPercentage() {
        return normalDurationPercentage;
    }

    public BigDecimal getOptimisticDuration() {
        return optimisticDuration;
    }

    public Integer getOptimisticDurationPercentage() {
        return optimisticDurationPercentage;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
    }

    public void setPessimisticDuration(BigDecimal pessimisticDuration) {
        this.pessimisticDuration = pessimisticDuration;
    }

    public void setPessimisticDurationPercentage(
            Integer pessimisticDurationPercentage) {
        this.pessimisticDurationPercentage = pessimisticDurationPercentage;
    }

    public void setNormalDuration(BigDecimal normalDuration) {
        this.normalDuration = normalDuration;
    }

    public void setNormalDurationPercentage(Integer normalDurationPercentage) {
        this.normalDurationPercentage = normalDurationPercentage;
    }

    public void setOptimisticDuration(BigDecimal optimisticDuration) {
        this.optimisticDuration = optimisticDuration;
    }

    public void setOptimisticDurationPercentage(
            Integer optimisticDurationPercentage) {
        this.optimisticDurationPercentage = optimisticDurationPercentage;
    }

    public String toString() {
        return String.format("%s:%f:(%f,%f):(%f,%f):(%f,%f)", task.getName(),
                duration, pessimisticDuration, pessimisticDurationPercentage,
                normalDuration, normalDurationPercentage, optimisticDuration,
                optimisticDurationPercentage);
    }

    public String getOrderName() {
        return task.getOrderElement().getOrder().getName();
    }

    public BigDecimal getPessimisticDurationPercentageLowerLimit() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getPessimisticDurationPercentageUpperLimit() {
        return BigDecimal.valueOf(pessimisticDurationPercentage).divide(
                BigDecimal.valueOf(100));
    }

    public BigDecimal getNormalDurationPercentageLowerLimit() {
        return getPessimisticDurationPercentageUpperLimit();
    }

    public BigDecimal getNormalDurationPercentageUpperLimit() {
        BigDecimal result = BigDecimal.valueOf(pessimisticDurationPercentage
                + normalDurationPercentage);
        return result.divide(BigDecimal.valueOf(100));
    }

    public BigDecimal getOptimisticDurationPercentageLowerLimit() {
        return getNormalDurationPercentageUpperLimit();
    }

    public BigDecimal getOptimisticDurationPercentageUpperLimit() {
        return BigDecimal.ONE;
    }

}
