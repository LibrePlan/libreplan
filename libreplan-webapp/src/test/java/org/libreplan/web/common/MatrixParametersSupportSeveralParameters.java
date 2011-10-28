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

package org.libreplan.web.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.libreplan.web.common.entrypoints.MatrixParameters;

/**
 * Tests that {@link MatrixParameters} supports extracting several
 * matrix parameters <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(Theories.class)
public class MatrixParametersSupportSeveralParameters {

    @DataPoint
    public static String TWO_PARAMETERS_IN_THE_END = "/prueba/prueba.php;color=red;size=2";

    @DataPoint
    public static String TWO_PARAMETERS_IN_THE_MIDDLE = "/prueba;color=red;size=2/prueba.php";

    @DataPoint
    public static String TWO_PARAMETERS_IN_DIFFERENT_LOCATIONS = "/prueba;color=red/prueba.php;size=2";

    @Theory
    public void oneParameterEqualToRed(String parameter) {
        Map<String, String> params = MatrixParameters
                .extract(parameter);
        assertThat(params.size(), equalTo(2));
        assertThat(params.get("color"), equalTo("red"));
        assertThat(params.get("size"), equalTo("2"));

    }

}
