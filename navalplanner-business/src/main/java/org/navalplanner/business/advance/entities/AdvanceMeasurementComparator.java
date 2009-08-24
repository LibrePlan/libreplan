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
        return arg0.getDate().compareTo(arg1.getDate());
    }
}
