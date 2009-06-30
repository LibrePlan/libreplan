package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;

public class HoursGroup implements Cloneable {

    private Long id;

    private Long version;

    public Long getId() {
        return id;
    }

    @NotNull
    private Integer workingHours = 0;

    private BigDecimal percentage;

    public enum HoursPolicies {
        NO_FIXED, FIXED_HOURS, FIXED_PERCENTAGE
    };

    private HoursPolicies hoursPolicy = HoursPolicies.NO_FIXED;

    private Set<Criterion> criterions = new HashSet<Criterion>();

    public void setWorkingHours(Integer workingHours) {
        this.workingHours = workingHours;
    }

    public Integer getWorkingHours() {
        return workingHours;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setHoursPolicy(HoursPolicies hoursPolicy) {
        this.hoursPolicy = hoursPolicy;
    }

    public HoursPolicies getHoursPolicy() {
        return hoursPolicy;
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public void addCriterion(Criterion criterion) {
        Criterion oldCriterion = getCriterionByType(criterion.getType());
        if (oldCriterion != null) {
            removeCriterion(oldCriterion);
        }

        criterions.add(criterion);
    }

    public void removeCriterion(Criterion criterion) {
        criterions.remove(criterion);
    }

    public Criterion getCriterionByType(ICriterionType<?> type) {
        for (Criterion criterion : criterions) {
            if (criterion.getType().equals(type.getName())) {
                return criterion;
            }
        }

        return null;
    }

    public Criterion getCriterionByType(String type) {
        for (Criterion criterion : criterions) {
            if (criterion.getType().equals(type)) {
                return criterion;
            }
        }

        return null;
    }

    public void removeCriterionByType(ICriterionType<?> type) {
        Criterion criterion = getCriterionByType(type);
        if (criterion != null) {
            removeCriterion(criterion);
        }
    }

    public void removeCriterionByType(String type) {
        Criterion criterion = getCriterionByType(type);
        if (criterion != null) {
            removeCriterion(criterion);
        }
    }

    public void forceLoadCriterions() {
        criterions.size();
    }

    public void makeTransientAgain() {
        // FIXME Review reattachment
        id = null;
        version = null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
