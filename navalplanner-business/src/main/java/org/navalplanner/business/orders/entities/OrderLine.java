package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.entities.HoursGroup.HoursPolicies;

public class OrderLine extends OrderElement {

    private Set<HoursGroup> hoursGroups = new HashSet<HoursGroup>();

    @Override
    public Integer getWorkHours() {
        return calculateTotalHours(hoursGroups);
    }

    @Override
    public List<OrderElement> getChildren() {
        return new ArrayList<OrderElement>();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public OrderLineGroup asContainer() {
        OrderLineGroup result = new OrderLineGroup();
        result.setName(getName());
        result.setInitDate(getInitDate());
        result.setEndDate(getEndDate());
        // FIXME
        // result.setHoursGroups(getHoursGroups());
        return result;
    }

    @Override
    public List<HoursGroup> getHoursGroups() {
        return new ArrayList<HoursGroup>(hoursGroups);
    }

    public void addHoursGroup(HoursGroup hoursGroup) {
        hoursGroups.add(hoursGroup);
        recalculatePercentages(hoursGroups);
    }

    public void deleteHoursGroup(HoursGroup hoursGroup) {
        hoursGroups.remove(hoursGroup);
        recalculatePercentages(hoursGroups);
    }

    /**
     * Set the total working hours of the {@link OrderLine} taking into account
     * the {@link HoursGroup} policies.
     *
     * @param workHours
     *            The desired value to set as total working hours
     * @throws IllegalArgumentException
     *             If parameter is less than 0 or if it's not possible to set
     *             this value taking into account {@link HoursGroup} policies.
     */
    public void setWorkHours(Integer workHours) throws IllegalArgumentException {

        if (workHours < 0) {
            throw new IllegalArgumentException(
                    "workHours should be greater or equals to 0");
        }

        if (hoursGroups.isEmpty()) {
            HoursGroup hoursGroup = new HoursGroup();
            hoursGroup.setWorkingHours(workHours);
            hoursGroup.setPercentage((new BigDecimal(1).setScale(2)));

            hoursGroups.add(hoursGroup);
        } else {

            if (!isTotalHoursValid(workHours)) {
                throw new IllegalArgumentException(
                        "\"workHours\" value is not valid, taking into "
                                + "account the current list of HoursGroup");
            }

            updateHoursGroups(workHours);
        }
    }

    /**
     * Makes the needed modifications in hoursGroups attribute in order to set
     * the desired value of working hours.
     *
     * This method takes into account the different {@link HoursGroup} policies:
     *
     * {@link HoursGroup} with FIXED_HOURS policy don't change.
     *
     * If policy is FIXED_PERCENTAGE the new value is calculated for each
     * {@link HoursGroup} with this policy. Using round down in order to avoid
     * problems.
     *
     * Hours are proportionally distributed when there're {@link HoursGroup}
     * with NO_FIXED policy.
     *
     * Finally, it creates new {@link HoursGroup} if the're some remaining hours
     * (it could happen because of the round down used for operations).
     *
     * @param workHours
     *            The value to set as total working hours
     */
    private void updateHoursGroups(Integer workHours) {

        Set<HoursGroup> newHoursGroups = new HashSet<HoursGroup>();

        // Divide HourGroup depending on policy
        Set<HoursGroup> fixedHoursGroups = new HashSet<HoursGroup>();
        Set<HoursGroup> fixedPercentageGroups = new HashSet<HoursGroup>();
        Set<HoursGroup> noFixedGroups = new HashSet<HoursGroup>();

        for (HoursGroup hoursGroup : hoursGroups) {
            switch (hoursGroup.getHoursPolicy()) {
            case FIXED_HOURS:
                fixedHoursGroups.add(hoursGroup);
                break;
            case FIXED_PERCENTAGE:
                fixedPercentageGroups.add(hoursGroup);
                break;
            case NO_FIXED:
            default:
                noFixedGroups.add(hoursGroup);
                break;
            }
        }

        // All the HourGroup with FIXED_HOURS will be kept without changes
        newHoursGroups.addAll(fixedHoursGroups);

        // For every HourGroup with FIXED_PERCENTAGE, workingHours will be
        // calculated
        for (HoursGroup hoursGroup : fixedPercentageGroups) {
            Integer hours = hoursGroup.getPercentage().multiply(
                    new BigDecimal(workHours).setScale(2)).toBigInteger()
                    .intValue();
            hoursGroup.setWorkingHours(hours);
            newHoursGroups.add(hoursGroup);
        }

        Integer newTotal = calculateTotalHours(newHoursGroups);

        if (newTotal.compareTo(workHours) > 0) {
            throw new RuntimeException("Unreachable code");
        } else if (newTotal.compareTo(workHours) == 0) {
            for (HoursGroup hoursGroup : noFixedGroups) {
                hoursGroup.setWorkingHours(0);
                newHoursGroups.add(hoursGroup);
            }
        } else if (newTotal.compareTo(workHours) < 0) {
            // Proportional sharing
            Integer oldNoFixed = calculateTotalHoursNoFixed(hoursGroups);
            Integer newNoFixed = workHours - newTotal;

            for (HoursGroup hoursGroup : noFixedGroups) {
                Integer newHours = (int) ((float) hoursGroup.getWorkingHours()
                        / oldNoFixed * newNoFixed);
                hoursGroup.setWorkingHours(newHours);

                newHoursGroups.add(hoursGroup);
            }
        }

        // If there're remaining hours
        newTotal = calculateTotalHours(newHoursGroups);
        if (newTotal.compareTo(workHours) < 0) {
            // Add a new HourGroup with the remaining hours
            HoursGroup hoursGroup = new HoursGroup();
            hoursGroup.setWorkingHours(workHours - newTotal);
            newHoursGroups.add(hoursGroup);
        }

        // Re-calculate percentages
        recalculatePercentages(newHoursGroups);

        // Set the attribute with the new hours group calculated
        hoursGroups = newHoursGroups;
    }

    /**
     * Check if the desired total number of hours is valid taking into account
     * {@link HoursGroup} policy restrictions.
     *
     * @param total
     *            The desired value
     * @return true if the value is valid
     */
    public boolean isTotalHoursValid(Integer total) {

        Integer newTotal = 0;

        for (HoursGroup hoursGroup : hoursGroups) {
            switch (hoursGroup.getHoursPolicy()) {
            case FIXED_HOURS:
                newTotal += hoursGroup.getWorkingHours();
                break;
            case FIXED_PERCENTAGE:
                newTotal += hoursGroup.getPercentage().multiply(
                        new BigDecimal(total).setScale(2)).toBigInteger()
                        .intValue();
                break;
            case NO_FIXED:
            default:
                break;
            }
        }

        if (newTotal.compareTo(total) > 0) {
            return false;
        }

        return true;
    }

    /**
     * Calculates the total number of working hours in a set of
     * {@link HoursGroup}.
     *
     * @param hoursGroups
     *            A {@link HoursGroup} set
     * @return The sum of working hours
     */
    private Integer calculateTotalHours(Set<HoursGroup> hoursGroups) {
        Integer result = 0;
        for (HoursGroup hoursGroup : hoursGroups) {
            result += hoursGroup.getWorkingHours();
        }
        return result;
    }

    /**
     * Calculates the total number of working hours in a set of
     * {@link HoursGroup} taking into account just {@link HoursGroup} with
     * NO_FIXED as policy.
     *
     * @param hoursGroups
     *            A {@link HoursGroup} set
     * @return The sum of NO_FIXED {@link HoursGroup}
     */
    private Integer calculateTotalHoursNoFixed(Set<HoursGroup> hoursGroups) {
        Integer result = 0;
        for (HoursGroup hoursGroup : hoursGroups) {
            if (hoursGroup.getHoursPolicy() == HoursPolicies.NO_FIXED) {
                result += hoursGroup.getWorkingHours();
            }
        }
        return result;
    }

    /**
     * Re-calculates the percentages in a {@link HoursGroup} set, without modify
     * the {@link HoursGroup} with policy FIXED_PERCENTAGE.
     *
     * @param hoursGroups
     *            A {@link HoursGroup} set
     */
    private void recalculatePercentages(Set<HoursGroup> hoursGroups) {
        Integer total = calculateTotalHours(hoursGroups);
        BigDecimal totalBigDecimal = new BigDecimal(total).setScale(2);

        for (HoursGroup hoursGroup : hoursGroups) {
            if (hoursGroup.getHoursPolicy() != HoursPolicies.FIXED_PERCENTAGE) {
                if (totalBigDecimal.equals(new BigDecimal(0).setScale(2))) {
                    hoursGroup.setPercentage(new BigDecimal(0).setScale(2));
                } else {
                    BigDecimal hoursBigDecimal = new BigDecimal(hoursGroup
                            .getWorkingHours()).setScale(2);
                    BigDecimal percentage = hoursBigDecimal.divide(
                            totalBigDecimal, BigDecimal.ROUND_DOWN);
                    hoursGroup.setPercentage(percentage);
                }
            }
        }
    }

    /**
     * Re-calculates the percentages in the {@link HoursGroup} set of the
     * current {@link OrderLine}, without modify the {@link HoursGroup} with
     * policy FIXED_PERCENTAGE.
     *
     */
    public void recalculatePercentages() {
        recalculatePercentages(hoursGroups);
    }

    @Override
    public void forceLoadHourGroups() {
        for (HoursGroup hoursGroup : hoursGroups) {
            hoursGroup.getWorkingHours();
        }
    }

    @Override
    public void forceLoadHourGroupsCriterions() {
        for (HoursGroup hoursGroup : hoursGroups) {
            hoursGroup.forceLoadCriterions();
        }
    }

}
