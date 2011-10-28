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
 * Test that {@link MatrixParameters} support one parameter<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(Theories.class)
public class MatrixParametersSupportsOneParameterTheory {

    @DataPoint
    public static String ONE_PARAMETER_IN_THE_END = "/prueba/prueba.php;color=red";

    @DataPoint
    public static String ONE_PARAMETER_IN_THE_MIDDLE = "/prueba;color=red/prueba.php";

    @DataPoint
    public static String ONE_PARAMETER_IN_THE_MIDDLE_WITH_SEMICOLON_AFTER = "/prueba;color=red;/prueba.php";

    @Theory
    public void oneParameterEqualToRed(String parameter) {
        Map<String, String> params = MatrixParameters
                .extract(parameter);
        assertThat(params.size(), equalTo(1));
        assertThat(params.keySet().iterator().next(), equalTo("color"));
        assertThat(params.get("color"), equalTo("red"));
    }

}
