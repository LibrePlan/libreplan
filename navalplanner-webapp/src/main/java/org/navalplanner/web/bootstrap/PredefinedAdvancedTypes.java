package org.navalplanner.web.bootstrap;

import java.math.BigDecimal;

import org.navalplanner.business.advance.entities.AdvanceType;

public enum PredefinedAdvancedTypes {
    PERCENTAGE("porcentaxe", new BigDecimal(100), new BigDecimal(0.01)), UNITS(
            "unidades", new BigDecimal(Integer.MAX_VALUE), new BigDecimal(1));

    private PredefinedAdvancedTypes(String name, BigDecimal defaultMaxValue,
            BigDecimal precision) {
        this.name = name;
        this.defaultMaxValue = defaultMaxValue.setScale(4,
                BigDecimal.ROUND_HALF_UP);
        this.precision = precision.setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private final String name;

    private final BigDecimal defaultMaxValue;

    private final BigDecimal precision;

    public AdvanceType createType() {
        return new AdvanceType(name, defaultMaxValue, false, precision, true);
    }

    public String getTypeName() {
        return name;
    }

}
