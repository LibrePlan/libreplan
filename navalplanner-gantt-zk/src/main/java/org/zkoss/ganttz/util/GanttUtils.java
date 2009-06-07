/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zkoss.ganttz.util;

/**
 *
 * @author Francisco Javier Moran Rúa
 *
 */
public class GanttUtils {

    public static int getIntFromStylePosition(String position) throws Exception {

        String[] tokens = position.split("px");

        if (tokens.length != 1) {
            throw new Exception("Bad formatting for input parameter");
        }

        return Integer.parseInt(tokens[0]);
    }

}
