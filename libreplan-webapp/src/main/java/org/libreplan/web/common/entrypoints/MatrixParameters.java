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

package org.libreplan.web.common.entrypoints;

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

    private MatrixParameters() {
    }

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
