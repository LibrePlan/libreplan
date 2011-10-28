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

package org.libreplan.business.qualityforms.daos;

import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.QualityFormType;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public interface IQualityFormDAO extends IGenericDAO<QualityForm, Long> {

    List<QualityForm> getAll();

    boolean isUnique(QualityForm qualityForm);

    QualityForm findByNameAndType(String name, QualityFormType type);

    List<QualityForm> getAllByType(QualityFormType type);

    QualityForm findUniqueByName(String name)
            throws InstanceNotFoundException, NonUniqueResultException;

    QualityForm findUniqueByName(QualityForm qualityForm)
            throws InstanceNotFoundException;

    boolean existsOtherWorkReportTypeByName(QualityForm qualityForm);

    boolean existsByNameAnotherTransaction(QualityForm qualityForm);

    void checkHasTasks(QualityForm qualityForm) throws ValidationException;
}
