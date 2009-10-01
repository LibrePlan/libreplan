package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;

public class OrderLine extends OrderElement {

    public static OrderLine create() {
        OrderLine result = new OrderLine();
        result.setNewObject(true);
        return result;
    }

    public static OrderLine createOrderLineWithUnfixedPercentage(int hours) {
        OrderLine result = create();
        HoursGroup hoursGroup = HoursGroup.create(result);
        result.addHoursGroup(hoursGroup);
        hoursGroup.setFixedPercentage(false);
        hoursGroup.setPercentage(new BigDecimal(1));
        hoursGroup.setWorkingHours(hours);
        return result;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public OrderLine() {

    }

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
    public OrderLine toLeaf() {
        return this;
    }

    @Override
    public OrderLineGroup toContainer() {
        OrderLineGroup result = OrderLineGroup.create();
        result.setName(getName());
        result.setCode(getCode());
        result.setInitDate(getInitDate());
        result.setEndDate(getEndDate());

        this.setName(getName() + " (copy)");
        this.setCode(getCode() + " (copy)");
        result.add(this);

        return result;
    }

    @Override
    public List<HoursGroup> getHoursGroups() {
        return new ArrayList<HoursGroup>(hoursGroups);
    }

    public void addHoursGroup(HoursGroup hoursGroup) {
        hoursGroup.setParentOrderLine(this);
        hoursGroups.add(hoursGroup);
        recalculateHoursGroups();
    }

    public void deleteHoursGroup(HoursGroup hoursGroup) {
        hoursGroups.remove(hoursGroup);
        recalculateHoursGroups();
    }

    /**
     * Set the total working hours of the {@link OrderLine} taking into account
     * the {@link HoursGroup} policies.
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
            HoursGroup hoursGroup = HoursGroup.create(this);
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
     * the desired value of working hours. This method takes into account the
     * different {@link HoursGroup} policies: If policy is FIXED_PERCENTAGE the
     * new value is calculated for each {@link HoursGroup} with this policy.
     * Using round down in order to avoid problems. Hours are proportionally
     * distributed when there're {@link HoursGroup} with NO_FIXED policy.
     * Finally, it creates new {@link HoursGroup} if the're some remaining hours
     * (it could happen because of the round down used for operations).
     * @param workHours
     *            The value to set as total working hours
     */
    private void updateHoursGroups(Integer workHours) {

        Set<HoursGroup> newHoursGroups = new HashSet<HoursGroup>();

        // Divide HourGroup depending on policy
        Set<HoursGroup> fixedPercentageGroups = new HashSet<HoursGroup>();
        Set<HoursGroup> noFixedGroups = new HashSet<HoursGroup>();

        for (HoursGroup hoursGroup : hoursGroups) {
            if (hoursGroup.isFixedPercentage()) {
                fixedPercentageGroups.add(hoursGroup);
            } else {
                noFixedGroups.add(hoursGroup);
            }
        }

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
                Integer newHours;
                if (oldNoFixed == 0) {
                    newHours = (int) ((float) newNoFixed / hoursGroups.size());
                } else {
                    newHours = (int) ((float) hoursGroup.getWorkingHours()
                            / oldNoFixed * newNoFixed);
                }
                hoursGroup.setWorkingHours(newHours);

                newHoursGroups.add(hoursGroup);
            }
        }

        // If there're remaining hours
        newTotal = calculateTotalHours(newHoursGroups);
        if (newTotal.compareTo(workHours) < 0) {
            // Add a new HourGroup with the remaining hours
            HoursGroup hoursGroup = HoursGroup.create(this);
            hoursGroup.setWorkingHours(workHours - newTotal);

            newHoursGroups.add(hoursGroup);
        }

        // Set the attribute with the new hours group calculated
        hoursGroups = newHoursGroups;

        // Re-calculate percentages
        recalculateHoursGroups();
    }

    /**
     * Checks if the desired total number of hours is valid taking into account
     * {@link HoursGroup} policy restrictions.
     * @param total
     *            The desired value
     * @return true if the value is valid
     */
    public boolean isTotalHoursValid(Integer total) {

        Integer newTotal = 0;

        for (HoursGroup hoursGroup : hoursGroups) {
            if (hoursGroup.isFixedPercentage()) {
                newTotal += hoursGroup.getPercentage().multiply(
                        new BigDecimal(total).setScale(2)).toBigInteger()
                        .intValue();
            }
        }

        if (newTotal.compareTo(total) > 0) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the percentage is or not valid. That means, if the pertentage
     * of all {@link HoursGroup} with FIXED_PERCENTAGE isn't more than 100%.
     * This method is called from setPercentage at {@link HoursGroup} class.
     * @return true if the percentage is valid
     */
    public boolean isPercentageValid() {

        BigDecimal newPercentage = new BigDecimal(0).setScale(2);

        for (HoursGroup hoursGroup : hoursGroups) {
            if (hoursGroup.isFixedPercentage()) {
                newPercentage = newPercentage.add(hoursGroup.getPercentage());
            }
        }

        if (newPercentage.compareTo(new BigDecimal(1).setScale(2)) > 0) {
            return false;
        }

        return true;
    }

    /**
     * Calculates the total number of working hours in a set of
     * {@link HoursGroup}.
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
     * @param hoursGroups
     *            A {@link HoursGroup} set
     * @return The sum of NO_FIXED {@link HoursGroup}
     */
    private Integer calculateTotalHoursNoFixed(Set<HoursGroup> hoursGroups) {
        Integer result = 0;
        for (HoursGroup hoursGroup : hoursGroups) {
            if (!hoursGroup.isFixedPercentage()) {
                result += hoursGroup.getWorkingHours();
            }
        }
        return result;
    }

    /**
     * Re-calculates the working hours and percentages in the {@link HoursGroup}
     * set of the current {@link OrderLine}, taking into account the policy of
     * each {@link HoursGroup}.
     */
    public void recalculateHoursGroups() {
        Integer total = calculateTotalHours(hoursGroups);
        BigDecimal totalBigDecimal = new BigDecimal(total).setScale(2);

        // For each HoursGroup with FIXED_PERCENTAGE, the workingHours are
        // calculated
        for (HoursGroup hoursGroup : hoursGroups) {
            if (hoursGroup.isFixedPercentage()) {
                Integer workingHours = hoursGroup.getPercentage().multiply(
                        totalBigDecimal).toBigInteger().intValue();

                hoursGroup.setWorkingHours(workingHours);
            }
        }

        Integer newTotal = calculateTotalHours(hoursGroups);
        // If the total was modified
        if (!newTotal.equals(total)) {
            Integer totalNoFixed = calculateTotalHoursNoFixed(hoursGroups);

            // For each HoursGroup without FIXED_PERCENTAGE, the hours are
            // proportionally shared
            for (HoursGroup hoursGroup : hoursGroups) {
                if (!hoursGroup.isFixedPercentage()) {
                    Integer hours = hoursGroup.getWorkingHours();
                    Integer newHours = (int) (((float) hours / totalNoFixed) * (total - (newTotal - totalNoFixed)));
                    hoursGroup.setWorkingHours(newHours);
                }
            }
        }

        newTotal = calculateTotalHours(hoursGroups);
        // If there's still some remaining hours
        if (newTotal.compareTo(total) < 0) {
            // Add a new HourGroup with the remaining hours
            HoursGroup hoursGroup = HoursGroup.create(this);
            hoursGroup.setWorkingHours(total - newTotal);
            hoursGroups.add(hoursGroup);
        }

        // Then the percentages for the HoursGroup without FIXED_PERCENTAGE are
        // recalculated.
        for (HoursGroup hoursGroup : hoursGroups) {
            if (!hoursGroup.isFixedPercentage()) {
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

    @Override
    public BigDecimal getAdvancePercentage(LocalDate date) {
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                if (date == null) {
                    return directAdvanceAssignment.getAdvancePercentage();
                }
                return directAdvanceAssignment.getAdvancePercentage(date);
            }
        }

        return BigDecimal.ZERO;
    }

    protected Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments(
            AdvanceType advanceType) {
        Set<DirectAdvanceAssignment> result = new HashSet<DirectAdvanceAssignment>();

        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getAdvanceType().getUnitName().equals(
                    advanceType.getUnitName())) {
                result.add(directAdvanceAssignment);
                return result;
            }
        }

        return result;
    }

    @Override
    protected Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments() {
        return getDirectAdvanceAssignments();
    }

    @Override
    protected Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignmentsReportGlobal() {
        Set<DirectAdvanceAssignment> result = new HashSet<DirectAdvanceAssignment>();

        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                result.add(directAdvanceAssignment);
                return result;
            }
        }

        return result;
    }

}
