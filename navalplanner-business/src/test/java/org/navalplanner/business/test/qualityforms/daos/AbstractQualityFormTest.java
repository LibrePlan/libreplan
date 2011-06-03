/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.test.qualityforms.daos;

import java.math.BigDecimal;
import java.util.UUID;

import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.QualityFormItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test for {@QualityDAO}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public abstract class AbstractQualityFormTest {

    @Autowired
    IQualityFormDAO qualityFormDAO;

    public QualityForm createValidQualityForm() {
        QualityForm qualityForm = QualityForm.create(UUID.randomUUID()
                .toString(), UUID.randomUUID().toString());
        return qualityForm;
    }

    public QualityFormItem createValidQualityFormItem() {
        QualityFormItem qualityFormItem = QualityFormItem.create(UUID
                .randomUUID().toString(), new Integer(0), new BigDecimal(1));
        return qualityFormItem;
    }

}
