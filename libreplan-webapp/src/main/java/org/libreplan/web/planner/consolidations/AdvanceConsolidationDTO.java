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

package org.libreplan.web.planner.consolidations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.libreplan.business.advance.entities.AdvanceAssignment;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.planner.entities.consolidations.ConsolidatedValue;

/**
 * Controller for {@link Advance} consolidation view.
 * @author Susana Montes Pedreira <smontes@wirelessgailicia.com>
 */

public class AdvanceConsolidationDTO {

    public static Date lastConsolidatedDate;

    public static Date lastConsolidatedAndSavedDate;

    private static boolean allReadOnly = false;

    private AdvanceMeasurement advanceMeasurement;

    private ConsolidatedValue consolidatedValue;

    private Date date;

    private BigDecimal units;

    private BigDecimal percentage;

    private boolean consolidated;

    public static List<AdvanceConsolidationDTO> sortByDate(
            List<AdvanceConsolidationDTO> consolidationDTOs) {
        Collections.sort(consolidationDTOs,
                new Comparator<AdvanceConsolidationDTO>() {

                    @Override
                    public int compare(AdvanceConsolidationDTO o1,
                            AdvanceConsolidationDTO o2) {
                        if (o1.getDate() == null) {
                            return 1;
                        }
                        if (o2.getDate() == null) {
                            return -1;
                        }
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });
        return consolidationDTOs;
    }

    public AdvanceConsolidationDTO(AdvanceMeasurement advanceMeasurement) {
        this(advanceMeasurement, null);
    }

    public AdvanceConsolidationDTO(AdvanceMeasurement advanceMeasurement,
            ConsolidatedValue consolidatedValue) {
        this.setAdvanceMeasurement(advanceMeasurement);
        this.setConsolidatedValue(consolidatedValue);
        initConsolidated();
        initDate();
        initPercentage();
        initUnits();
    }

    private void initConsolidated() {
        this.setConsolidated((getConsolidatedValue() != null));
    }

    private void initDate() {
        if (consolidatedValue != null) {
            this.date = consolidatedValue.getDate().toDateTimeAtStartOfDay()
                    .toDate();
        } else if (advanceMeasurement != null) {
            this.date = advanceMeasurement.getDate().toDateTimeAtStartOfDay()
                    .toDate();
        }
    }

    private void initPercentage() {
        if (consolidatedValue != null) {
            this.percentage = this.consolidatedValue.getValue();
        } else if (advanceMeasurement != null) {
            this.percentage = calculatePercentageAdvanceMeasurement(advanceMeasurement);
        }
    }

    private void initUnits(){
        if (advanceMeasurement != null) {
            this.units = advanceMeasurement.getValue();
        } else {
            this.units = BigDecimal.ZERO;
        }
    }

    public void setAdvanceMeasurement(AdvanceMeasurement advanceMeasurement) {
        this.advanceMeasurement = advanceMeasurement;
    }

    public AdvanceMeasurement getAdvanceMeasurement() {
        return advanceMeasurement;
    }

    public void setConsolidatedValue(ConsolidatedValue consolidatedValue) {
        this.consolidatedValue = consolidatedValue;
    }

    public ConsolidatedValue getConsolidatedValue() {
        return consolidatedValue;
    }

    public void setConsolidated(boolean consolidated) {
        this.consolidated = consolidated;
    }

    public boolean isConsolidated() {
        return consolidated;
    }

    public static void setLastConsolidatedAdvance(Date lastConsolidatedAdvance) {
        AdvanceConsolidationDTO.lastConsolidatedDate = lastConsolidatedAdvance;
    }

    public static Date getLastConsolidatedAdvance() {
        return lastConsolidatedDate;
    }

    public Boolean canNotBeConsolidated() {
        if (isAllReadOnly()) {
            return true;
        }
        if ((isConsolidated()) && (consolidatedValue != null)
                && (!consolidatedValue.isNewObject())) {
            if ((lastConsolidatedDate == null)
                    || (lastConsolidatedDate.equals(date))) {
                return false;
            }
            return true;
        }
        if (lastConsolidatedDate != null) {
            if (date != null) {
                return ((lastConsolidatedDate.compareTo(date)) > 0);
            }
        }
        return false;
    }

    public static Boolean canBeConsolidateAndShow(Date date) {
        if (lastConsolidatedAndSavedDate != null) {
            if (date != null) {
                return ((lastConsolidatedAndSavedDate.compareTo(date)) < 0);
            }
        }
        return true;
    }

    public boolean isSavedConsolidatedValue() {
        return ((consolidatedValue != null) && (!consolidatedValue
                .isNewObject()));
    }

    public Date getDate() {
        return date;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public static void setAllReadOnly(boolean allReadOnly) {
        AdvanceConsolidationDTO.allReadOnly = allReadOnly;
    }

    public static boolean isAllReadOnly() {
        return allReadOnly;
    }

    private BigDecimal calculatePercentageAdvanceMeasurement(
            AdvanceMeasurement advanceMeasurement) {
        AdvanceAssignment assignment = advanceMeasurement
                .getAdvanceAssignment();
        if (assignment == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxValue = ((DirectAdvanceAssignment) assignment)
                .getMaxValue();

        if (maxValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = advanceMeasurement.getValue();
        if (value == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal division = value.divide(maxValue, 2, RoundingMode.DOWN);
        return (division.multiply(new BigDecimal(100))).setScale(0,
                RoundingMode.DOWN);
    }

    public String getLabelPercentage() {
        if (getPercentage() != null) {
            return getPercentage().toString() + " %";
        } else {
            return (BigDecimal.ZERO).toString() + " %";
        }
    }

    public BigDecimal getUnits() {
        return units;
    }

    public String getLabelUnits() {
        if (getUnits() != null) {
            return getUnits().toString();
        } else {
            return (BigDecimal.ZERO).toString();
        }
    }
}
