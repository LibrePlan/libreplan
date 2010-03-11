package org.navalplanner.ws.costcategories.api;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link CostCategory} entity.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class CostCategoryDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "cost-category";

    @XmlAttribute
    public String name;

    @XmlAttribute
    public Boolean enabled;

    @XmlElementWrapper(name = "hour-cost-list")
    @XmlElement(name = "hour-cost")
    public Set<HourCostDTO> hourCostDTOs = new HashSet<HourCostDTO>();

    public CostCategoryDTO() {
    }

    public CostCategoryDTO(String code, String name, Boolean enabled,
            Set<HourCostDTO> hourCostDTOs) {

        super(code);
        this.name = name;
        this.enabled = enabled;
        this.hourCostDTOs = hourCostDTOs;
    }

    /**
     * This constructor automatically generates a unique code. It is intended to
     * facilitate the implementation of test cases that add new instances (such
     * instances will have a unique code).
     */
    public CostCategoryDTO(String name, Boolean enabled,
            Set<HourCostDTO> hourCostDTOs) {

        this(generateCode(), name, enabled, hourCostDTOs);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}