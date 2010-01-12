 package org.navalplanner.web.reports;

import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.MaterialStatusEnum;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.reports.dtos.TimeLineRequiredMaterialDTO;
import org.zkoss.zul.TreeModel;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface ITimeLineRequiredMaterialModel {

    JRDataSource getTimeLineRequiredMaterial(Date startingDate,
            Date endingDate, MaterialStatusEnum lbStatus,
            List<Order> listOrders, List<MaterialCategory> categories,
            List<Material> materials);

    List<Order> getOrders();

    List<TimeLineRequiredMaterialDTO> filterConsult(Date startingDate,
            Date endingDate,
 MaterialStatusEnum status, List<Order> listOrders,
            List<MaterialCategory> categories, List<Material> materials);

    List<TimeLineRequiredMaterialDTO> sort(
            List<TimeLineRequiredMaterialDTO> list);

    TreeModel getAllMaterialCategories();
}
