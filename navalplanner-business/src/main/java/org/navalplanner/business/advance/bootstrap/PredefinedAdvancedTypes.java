package org.navalplanner.business.advance.bootstrap;

import java.math.BigDecimal;

import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.Registry;

public enum PredefinedAdvancedTypes {
    PERCENTAGE("percentage", new BigDecimal(100), new BigDecimal(0.01)), UNITS(
            "units", new BigDecimal(Integer.MAX_VALUE), new BigDecimal(1));

    private PredefinedAdvancedTypes(String name, BigDecimal defaultMaxValue,
            BigDecimal precision) {
        this.name = name;
        this.defaultMaxValue = defaultMaxValue.setScale(4,
                BigDecimal.ROUND_HALF_UP);
        this.unitPrecision = precision.setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private final String name;

    private final BigDecimal defaultMaxValue;

    private final BigDecimal unitPrecision;

    public AdvanceType createType() {
        return new AdvanceType(name, defaultMaxValue, false, unitPrecision,
                true);
    }

    public String getTypeName() {
        return name;
    }

    public AdvanceType getType() {
        return Registry.getAdvanceTypeDao().findByName(name);
    }

}
