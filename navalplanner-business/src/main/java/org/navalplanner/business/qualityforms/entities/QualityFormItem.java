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

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
package org.navalplanner.business.qualityforms.entities;

import java.math.BigDecimal;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.INewObject;

public class QualityFormItem implements INewObject {

    public static QualityFormItem create() {
        QualityFormItem qualityFormItem = new QualityFormItem();
        qualityFormItem.setNewObject(true);
        return qualityFormItem;
    }

    public static QualityFormItem create(String name, Integer position,
            BigDecimal percentage) {
        QualityFormItem qualityFormItem = new QualityFormItem(name, position,
                percentage);
        qualityFormItem.setNewObject(true);
        return qualityFormItem;
    }

    public QualityFormItem() {

    }

    private QualityFormItem(String name, Integer position, BigDecimal percentage) {
        this.name = name;
        this.position = position;
        this.percentage = percentage;
    }

    private boolean newObject = false;

    private String name;

    private Integer position;

    private BigDecimal percentage;

    @NotEmpty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer newPosition) {
        this.position = newPosition;
    }

    @NotNull
    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public boolean isNewObject() {
        return newObject;
    }

    private void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }

}
