package org.navalplanner.web.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.navalplanner.web.common.entrypoints.MatrixParameters;

/**
 * Tests that {@link MatrixParameters} supports matrix parameters
 * without value <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(Theories.class)
public class MatrixParametersSupportsParametersWithoutValue {

    @DataPoint
    public static String ONE_PARAMETER_WITHOUT_VALUE_AT_THE_END = "/blabalba/eo/prueba;create";

    @DataPoint
    public static String ONE_PARAMETER_WITHOUT_VALUE_IN_THE_MIDDLE = "/blabalba;create/eo/prueba";

    @Theory
    public void testTheory(String parameter) {
        Map<String, String> map = MatrixParameters.extract(parameter);
        assertThat(map.size(), equalTo(1));
        assertTrue(map.containsKey("create"));
        assertNull(map.get("create"));
    }

}
