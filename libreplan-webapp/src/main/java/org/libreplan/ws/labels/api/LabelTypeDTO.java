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

package org.libreplan.ws.labels.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link LabelType} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "label-type")
public class LabelTypeDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "label-type";

    @XmlAttribute
    public String name;

    @XmlElementWrapper(name = "label-list")
    @XmlElement(name = "label")
    public List<LabelDTO> labels = new ArrayList<LabelDTO>();

    public LabelTypeDTO() {
    }

    public LabelTypeDTO(String code, String name, List<LabelDTO> labels) {
        super(code);
        this.name = name;
        this.labels = labels;
    }

    public LabelTypeDTO(String name, List<LabelDTO> labels) {
        this(generateCode(), name, labels);
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
