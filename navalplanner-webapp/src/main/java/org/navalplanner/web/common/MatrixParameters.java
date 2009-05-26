package org.navalplanner.web.common;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Extracts <a href="http://www.w3.org/DesignIssues/MatrixURIs.html">matrix
 * parameters</a>. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class MatrixParameters {

    private static Pattern matrixParamPattern = Pattern
            .compile(";([^/=;]+)=?([^/;]+)?");

    public static Map<String, String> extract(HttpServletRequest request) {
        return extract(request.getRequestURI());
    }

    public static Map<String, String> extract(String string) {
        Map<String, String> result = new HashMap<String, String>();
        Matcher matcher = matrixParamPattern.matcher(string);
        while (matcher.find()) {
            result.put(matcher.group(1), matcher.group(2));
        }
        return result;
    }

}
