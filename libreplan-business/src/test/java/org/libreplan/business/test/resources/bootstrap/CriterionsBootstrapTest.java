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

package org.libreplan.business.test.resources.bootstrap;

import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.resources.bootstrap.ICriterionsBootstrap;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.entities.CategoryCriteria;
import org.libreplan.business.resources.entities.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionsBootstrapTest {

    @Autowired
    private ICriterionsBootstrap criterionsBootstrap;

    @Autowired
    private ICriterionDAO criterionDAO;

    private List<Criterion> somePredefinedCriterions;

    public CriterionsBootstrapTest() {
        somePredefinedCriterions = getSomePredefinedCriterions();
    }

    private List<Criterion> getSomePredefinedCriterions() {
        List<Criterion> result = new ArrayList<Criterion>();
        for (CategoryCriteria category : CategoryCriteria.values()) {
            result.add(category.criterion());
        }
        return result;
    }

    @Test
    public void testBootstrap() {
        givenNoSomePredefinedCriterionExists();
        criterionsBootstrap.loadRequiredData(true);
        thenAllSomePredefinedCriterionsExist();
    }

    private void givenNoSomePredefinedCriterionExists() {
        for (Criterion criterion : somePredefinedCriterions) {
            remove(criterion);
        }
    }

    private void thenAllSomePredefinedCriterionsExist() {
        for (Criterion criterion : somePredefinedCriterions) {
            assertTrue(criterionDAO.existsByNameAndType(criterion));
        }
    }

    private void remove(Criterion criterion) {
        if (criterionDAO.existsByNameAndType(criterion)) {
            criterionDAO.removeByNameAndType(criterion);
        }
    }

}
