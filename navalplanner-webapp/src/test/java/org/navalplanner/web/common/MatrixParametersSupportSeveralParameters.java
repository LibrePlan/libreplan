package org.navalplanner.web.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

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
