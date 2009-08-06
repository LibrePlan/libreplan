package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Window;

/**
 * Controller for show the asigned hours of the selected order element<br />
 * @author Susana Montes Pedreria <smontes@wirelessgalicia.com>
 */
public class AsignedHoursToOrderElementController extends
        GenericForwardComposer {

    private Window window;

    IAsignedHoursToOrderElementModel asignedHoursToOrderElementModel;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp.getFellow("listOrderElementHours"));
        comp.setVariable("asignedHoursToOrderElementController", this, true);
        window = (Window) comp;
    }

    public List<WorkReportLine> getWorkReportLines() {
        return asignedHoursToOrderElementModel.getWorkReportLines();
    }

    public int getTotalAsignedDirectHours() {
        return asignedHoursToOrderElementModel.getAsignedDirectHours();
    }

    public int getTotalAsignedHours() {
        return asignedHoursToOrderElementModel.getTotalAsignedHours();
    }

    public void back() {
        window.setVisible(false);
        Util.reloadBindings(window.getParent());
    }

    public int getHoursChildren() {
        return asignedHoursToOrderElementModel.getAsignedDirectHoursChildren();
    }

    public int getEstimatedHours() {
        return asignedHoursToOrderElementModel.getEstimatedHours();
    }

    public int getProgressWork() {
        return asignedHoursToOrderElementModel.getProgressWork();
    }

    public void openWindow(IOrderElementModel orderElementModel) {
        this.asignedHoursToOrderElementModel.initOrderElement(orderElementModel
                .getOrderElement());
        viewPercentage();
    }

    /**
     * This method shows the percentage of the imputed hours with respect to the
     * estimated hours.If the hours imputed is greater that the hours estimated
     * then show the exceed percentage of hours.
     */
    private void viewPercentage() {

        Progressmeter progressBar = (Progressmeter) window
                .getFellow("HoursProgressBar");
        Progressmeter exceedHoursProgressBar = (Progressmeter) window
                .getFellow("exceedHoursProgressBar");
        if (this.getProgressWork() > 100) {
            progressBar.setValue(100);
            exceedHoursProgressBar.setVisible(true);
            exceedHoursProgressBar.setValue(0);
            String exceedValue = String.valueOf(getProgressWork() - 100);
            exceedHoursProgressBar.setWidth(exceedValue + "px");
            exceedHoursProgressBar.setLeft("left");
            exceedHoursProgressBar
                    .setStyle("background:red ; border:1px solid red");
        } else {
            progressBar.setValue(getProgressWork());
            exceedHoursProgressBar.setVisible(false);
        }
    }

}
