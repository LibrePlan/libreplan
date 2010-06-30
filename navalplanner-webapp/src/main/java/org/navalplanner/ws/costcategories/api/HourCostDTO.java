package org.navalplanner.ws.costcategories.api;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.datatype.XMLGregorianCalendar;

import org.navalplanner.business.costcategories.entities.HourCost;
import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link HourCost} entity.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class HourCostDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "hour-cost";

    @XmlAttribute
    public BigDecimal priceCost;

    @XmlAttribute
    public XMLGregorianCalendar initDate;

    @XmlAttribute
    public XMLGregorianCalendar endDate;

    @XmlAttribute(name = "work-hours-type")
    public String type;

    public HourCostDTO() {
    }

    public HourCostDTO(String code, BigDecimal priceCost,
            XMLGregorianCalendar initDate,
 XMLGregorianCalendar endDate,
            String type) {

        super(code);
        this.initDate = initDate;
        this.endDate = endDate;
        this.priceCost = priceCost;
        this.type = type;
    }

    /**
     * This constructor automatically generates a unique code. It is intended to
     * facilitate the implementation of test cases that add new instances (such
     * instances will have a unique code).
     */
    public HourCostDTO(BigDecimal priceCost, XMLGregorianCalendar initDate,
            XMLGregorianCalendar endDate,
            String type) {

        this(generateCode(), priceCost, initDate, endDate, type);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
