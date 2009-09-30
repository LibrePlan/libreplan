/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.business.advance.entities;

import java.util.Comparator;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AdvanceMeasurementComparator implements Comparator<AdvanceMeasurement> {

    public AdvanceMeasurementComparator(){
    }

    @Override
    public int compare(AdvanceMeasurement arg0, AdvanceMeasurement arg1) {
        if (arg0.getDate() == arg1.getDate()) {
            return 0;
        }
        if (arg0.getDate() == null) {
            return -1;
        }
        if (arg1.getDate() == null) {
            return 1;
        }
        return arg1.getDate().compareTo(arg0.getDate());
    }
}
