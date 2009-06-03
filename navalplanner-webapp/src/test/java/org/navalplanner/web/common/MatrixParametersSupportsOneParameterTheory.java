package org.navalplanner.web.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.navalplanner.web.common.entrypoints.MatrixParameters;

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
