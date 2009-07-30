package org.navalplanner.business.advance.entities;

import java.math.BigDecimal;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class AdvanceType {
    private Long id;

    @SuppressWarnings("unused")
    private long version;

    private String unitName;

    private BigDecimal defaultMaxValue;

    private boolean updatable;

    public AdvanceType(String unitName, BigDecimal defaultMaxValue,boolean updatable) {
        this.unitName = unitName;
        this.defaultMaxValue = defaultMaxValue;
        this.defaultMaxValue.setScale(2);
        this.updatable = updatable;
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

    public void doPropagateAdvaceToParent(OrderElement orderElement) {
    }
}
