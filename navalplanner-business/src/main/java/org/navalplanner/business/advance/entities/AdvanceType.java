package org.navalplanner.business.advance.entities;

import java.math.BigDecimal;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class AdvanceType {
    private Long id;

    @SuppressWarnings("unused")
    private long version;

    @NotEmpty
    private String unitName;

    @NotNull
    private BigDecimal defaultMaxValue;

    @NotNull
    private boolean updatable;

    @NotNull
    private BigDecimal precision;

    @NotNull
    private boolean active;

    public AdvanceType() {
        this.updatable = true;
        this.active = true;
    }

    public AdvanceType(String unitName, BigDecimal defaultMaxValue,
            boolean updatable, BigDecimal precision, boolean active) {
        this.unitName = unitName;
        this.defaultMaxValue = defaultMaxValue;
        this.defaultMaxValue.setScale(2);
        this.updatable = updatable;
        this.precision = precision;
        this.precision.setScale(4);
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitName() {
        return this.unitName;
    }

    public void setDefaultMaxValue(BigDecimal defaultMaxValue) {
        this.defaultMaxValue = defaultMaxValue;
        this.defaultMaxValue.setScale(2);
    }

    public BigDecimal getDefaultMaxValue() {
        return this.defaultMaxValue;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public boolean getUpdatable() {
        return this.updatable;
    }

    public void setPrecision(BigDecimal precision) {
        this.precision = precision;
        this.precision.setScale(4);
    }

    public BigDecimal getPrecision() {
        return this.precision;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return this.active;
    }

    public String getType() {
        if (getUpdatable())
            return "De Usuario";
        return "Predefinido";
    }

    public void doPropagateAdvaceToParent(OrderElement orderElement) {
    }

    public boolean isPrecisionValid(BigDecimal precision) {
        if ((this.defaultMaxValue == null) || (precision == null))
            return true;
        if (this.defaultMaxValue.compareTo(precision) < 0)
            return false;
        return true;

    }

    public boolean isDefaultMaxValueValid(BigDecimal defaultMaxValue) {
        if ((this.precision == null) || (defaultMaxValue == null))
            return true;
        if (this.precision.compareTo(defaultMaxValue) > 0)
            return false;
        return true;
    }

}
