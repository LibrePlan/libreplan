/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
package org.navalplanner.business.templates.entities;

import org.hibernate.validator.Min;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.InfoComponent;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class OrderElementTemplate extends BaseEntity {

    private InfoComponent infoComponent;

    private Integer startAsDaysFromBeginning;

    private Integer deadlineAsDaysFromBeginning;

    private OrderLineGroupTemplate parent;

    @Valid
    private InfoComponent getInfoComponent() {
        if (infoComponent == null) {
            infoComponent = new InfoComponent();
        }
        return infoComponent;
    }

    @Min(0)
    public Integer getDeadlineAsDaysFromBeginning() {
        return deadlineAsDaysFromBeginning;
    }

    @Min(0)
    public Integer getStartAsDaysFromBeginning() {
        return startAsDaysFromBeginning;
    }

    public String getCode() {
        return getInfoComponent().getCode();
    }

    public String getDescription() {
        return getInfoComponent().getDescription();
    }

    public String getName() {
        return getInfoComponent().getName();
    }
}
