/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.orders.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public abstract class HoursGroupHandler<T> implements IHoursGroupHandler<T> {

    public boolean isTotalHoursValid(Integer total, final Set<HoursGroup> hoursGroups) {

        if (total == null) {
            return false;
        }

        int newTotal = 0;

        for (HoursGroup hoursGroup : hoursGroups) {
            if (hoursGroup.isFixedPercentage()) {
                newTotal += hoursGroup.getPercentage().multiply(
                        new BigDecimal(total).setScale(2)).toBigInteger()
                        .intValue();
            }
        }

        if (newTotal > total) {
            return false;
        }

        return true;
    }

    public boolean isPercentageValid(final Set<HoursGroup> hoursGroups) {

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

    public Integer calculateTotalHours(Set<HoursGroup> hoursGroups) {
        Integer result = 0;
        for (HoursGroup hoursGroup : hoursGroups) {
            Integer workingHours = hoursGroup.getWorkingHours();
            if (workingHours != null) {
                result += workingHours;
            }
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

    public void recalculateHoursGroups(T orderLine) {
        Set<HoursGroup> hoursGroups = getHoursGroup(orderLine);
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
            HoursGroup hoursGroup = createHoursGroup(orderLine);
            hoursGroup.updateMyCriterionRequirements();
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

    protected abstract Set<HoursGroup> getHoursGroup(T orderLine);

    protected abstract HoursGroup createHoursGroup(T orderLine);

    private void updateHoursGroups(T orderLine, Integer workHours) {
        final Set<HoursGroup> hoursGroups = getHoursGroup(orderLine);
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
            HoursGroup hoursGroup = createHoursGroup(orderLine);
            hoursGroup.setWorkingHours(workHours - newTotal);

            newHoursGroups.add(hoursGroup);
        }

        // Set the attribute with the new hours group calculated
        setHoursGroups(orderLine, newHoursGroups);

        // Re-calculate percentages
        recalculateHoursGroups(orderLine);
    }

    protected abstract void setHoursGroups(T orderLine, Set<HoursGroup> hoursGroups);

    /**
     * Set the total working hours of the {@link OrderLine} taking into account
     * the {@link HoursGroup} policies.
     * @param workHours
     *            The desired value to set as total working hours
     * @throws IllegalArgumentException
     *             If parameter is less than 0 or if it's not possible to set
     *             this value taking into account {@link HoursGroup} policies.
     */
    @Override
    public void setWorkHours(T orderLine, Integer workHours) throws IllegalArgumentException {

        if (workHours == null) {
            workHours = 0;
        }

        if (workHours < 0) {
            throw new IllegalArgumentException(
                    _("workHours should be greater or equals to 0"));
        }

        if (hoursGroupsIsEmpty(orderLine)) {
            HoursGroup hoursGroup = createHoursGroup(orderLine);
            hoursGroup.setWorkingHours(workHours);
            hoursGroup.setPercentage((new BigDecimal(1).setScale(2)));
            addHoursGroup(orderLine, hoursGroup);
        } else {

            if (!isTotalHoursValid(workHours, getHoursGroup(orderLine))) {
                throw new IllegalArgumentException(
                        _("\"workHours\" value is not valid, taking into "
                                + "account the current list of HoursGroup"));
            }

            updateHoursGroups(orderLine, workHours);
        }
    }

    protected abstract boolean hoursGroupsIsEmpty(T orderLine);

    protected abstract void addHoursGroup(T orderLine, HoursGroup hoursGroup);

}
