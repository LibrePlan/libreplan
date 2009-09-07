package org.navalplanner.web.orders;
import java.util.Comparator;
/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AdvanceMeasurementDTOComparator implements Comparator<AdvanceMeasurementDTO> {

    public AdvanceMeasurementDTOComparator(){
    }

    @Override
    public int compare(AdvanceMeasurementDTO arg0, AdvanceMeasurementDTO arg1) {
        if((arg1.getDate() == null) && (arg0.getDate() == null))
            return 0;
        if(arg1.getDate() == null) return 1;
        if(arg0.getDate() == null) return -1;
        return arg1.getDate().compareTo(arg0.getDate());
    }
}
