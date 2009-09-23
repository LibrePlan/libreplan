package org.navalplanner.business.advance.bootstrap;

import java.math.BigDecimal;

import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.Registry;

public enum PredefinedAdvancedTypes {

    CHILDREN("children", new BigDecimal(100), new BigDecimal(0.01), true), PERCENTAGE(
            "percentage", new BigDecimal(100), new BigDecimal(0.01), true), UNITS(
            "units", new BigDecimal(Integer.MAX_VALUE), new BigDecimal(1),
            false);

    private PredefinedAdvancedTypes(String name, BigDecimal defaultMaxValue,
            BigDecimal precision, boolean percentage) {
        this.name = name;
        this.defaultMaxValue = defaultMaxValue.setScale(4,
                BigDecimal.ROUND_HALF_UP);
        this.unitPrecision = precision.setScale(4, BigDecimal.ROUND_HALF_UP);
        this.percentage = percentage;
    }

    private final String name;

    private final BigDecimal defaultMaxValue;

    private final BigDecimal unitPrecision;

    private final boolean percentage;

    public AdvanceType createType() {
        return AdvanceType.create(name, defaultMaxValue, false, unitPrecision,
                true, percentage);
    }

    public String getTypeName() {
        return name;
    }

    public AdvanceType getType() {
        return Registry.getAdvanceTypeDao().findByName(name);
    }

}
