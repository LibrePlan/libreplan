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

package org.libreplan.web.test.ws.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;

/**
 * Utilities class related with web service tests.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class Util {

    public static Matcher<String> mustEnd(final String property) {
        return new BaseMatcher<String>() {

            @Override
            public boolean matches(Object object) {
                if (object instanceof String) {
                    String s = (String) object;
                    return s.endsWith(property);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("must end with " + property);
            }
        };
    }

    public static String getUniqueName() {
        return UUID.randomUUID().toString();
    }

    public static void assertNoConstraintViolations(
        InstanceConstraintViolationsListDTO
        instanceConstraintViolationsListDTO) {

        assertNotNull(instanceConstraintViolationsListDTO.
            instanceConstraintViolationsList);
        assertTrue(
            instanceConstraintViolationsListDTO.
            instanceConstraintViolationsList.toString(),
            instanceConstraintViolationsListDTO.
            instanceConstraintViolationsList.size() == 0);

    }

    public static void assertOneConstraintViolation(
        InstanceConstraintViolationsListDTO
        instanceConstraintViolationsListDTO) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            instanceConstraintViolationsListDTO.
                instanceConstraintViolationsList;

        assertNotNull(instanceConstraintViolationsListDTO.
            instanceConstraintViolationsList);
        assertTrue(
            instanceConstraintViolationsList.toString(),
            instanceConstraintViolationsList.size() == 1);
        assertNoRecoverableError(instanceConstraintViolationsList.get(0));
        assertNoInternalError(instanceConstraintViolationsList.get(0));
        assertNotNull(instanceConstraintViolationsList.get(0).
            constraintViolations);
        assertTrue(
            instanceConstraintViolationsList.get(0).
            constraintViolations.toString(),
            instanceConstraintViolationsList.get(0).
            constraintViolations.size() == 1);

    }

    public static void assertOneConstraintViolationPerInstance(
        InstanceConstraintViolationsListDTO
        instanceConstraintViolationsListDTO, int numberOfInstances) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            instanceConstraintViolationsListDTO.
                instanceConstraintViolationsList;

         assertNotNull(instanceConstraintViolationsList);
         assertTrue(
             instanceConstraintViolationsList.toString(),
             instanceConstraintViolationsList.size() == numberOfInstances);

         for (InstanceConstraintViolationsDTO i :
             instanceConstraintViolationsList) {
             assertNoRecoverableError(i);
             assertNoInternalError(i);
             assertNotNull(i.constraintViolations);
             assertTrue(
                 i.constraintViolations.toString(),
                 i.constraintViolations.size() == 1);
         }

    }

    public static void assertOneRecoverableError(
        InstanceConstraintViolationsListDTO
        instanceConstraintViolationsListDTO) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            instanceConstraintViolationsListDTO.
                instanceConstraintViolationsList;

        assertNotNull(instanceConstraintViolationsListDTO.
            instanceConstraintViolationsList);
        assertTrue(
            instanceConstraintViolationsList.toString(),
            instanceConstraintViolationsList.size() == 1);
        assertNoConstraintViolations(instanceConstraintViolationsList.get(0));
        assertNoInternalError(instanceConstraintViolationsList.get(0));
        assertNotNull(instanceConstraintViolationsList.get(0).recoverableError);

    }

    private static void assertNoConstraintViolations(
        InstanceConstraintViolationsDTO i) {

        assertNull(i.toString(), i.constraintViolations);

    }

    private static void assertNoRecoverableError(
        InstanceConstraintViolationsDTO i) {

        assertNull(i.toString(), i.recoverableError);

    }

    private static void assertNoInternalError(
        InstanceConstraintViolationsDTO i) {

        assertNull(i.toString(), i.internalError);

    }

}
