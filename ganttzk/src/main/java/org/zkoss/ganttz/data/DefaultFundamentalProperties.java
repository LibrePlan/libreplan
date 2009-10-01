package org.zkoss.ganttz.data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DefaultFundamentalProperties implements ITaskFundamentalProperties {

    private String name;

    private Date beginDate = null;

    private long lengthMilliseconds = 0;

    private String notes;

    private BigDecimal hoursAdvancePercentage;

    private BigDecimal advancePercentage;

    public DefaultFundamentalProperties() {
    }

    public DefaultFundamentalProperties(String name, Date beginDate,
            long lengthMilliseconds, String notes,
            BigDecimal hoursAdvancePercentage, BigDecimal advancePercentage) {
        this.name = name;
        this.beginDate = beginDate;
        this.lengthMilliseconds = lengthMilliseconds;
        this.notes = notes;
        this.hoursAdvancePercentage = hoursAdvancePercentage;
        this.advancePercentage = advancePercentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public long getLengthMilliseconds() {
        return lengthMilliseconds;
    }

    public void setLengthMilliseconds(long lengthMilliseconds) {
        if (lengthMilliseconds < 0)
            throw new IllegalArgumentException(
                    "a task must not have a negative length. Received value: "
                            + lengthMilliseconds);
        this.lengthMilliseconds = lengthMilliseconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public BigDecimal getHoursAdvancePercentage() {
        return hoursAdvancePercentage;
    }

    @Override
    public BigDecimal getAdvancePercentage() {
        return advancePercentage;
    }

}
