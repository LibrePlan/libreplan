package org.navalplanner.business.workorders.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

/**
 * It represents a project with its related information. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ProjectWork {

    private static Date copy(Date date) {
        return date != null ? new Date(date.getTime()) : date;
    }

    private Long id;

    private Long version;

    @NotEmpty
    private String name;

    @NotNull
    private Date initDate;

    private Date endDate;

    private String description;

    private String responsible;

    // TODO turn into a many to one relationship when Customer entity is defined
    private String customer;

    private Set<TaskWork> taskWorks = new HashSet<TaskWork>();

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getInitDate() {
        return copy(initDate);
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return copy(endDate);
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public boolean isEndDateBeforeStart() {
        return endDate != null && endDate.before(initDate);
    }

    public void add(TaskWork task) {
        taskWorks.add(task);
    }

    public List<TaskWork> getTaskWorks() {
        return new ArrayList<TaskWork>(taskWorks);
    }

}
