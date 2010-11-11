package org.navalplanner.web.montecarlo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.Chart;
import org.zkoss.zul.SimpleCategoryModel;


/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MonteCarloGraphController extends GenericForwardComposer {

    private Chart monteCarloChart;

    public MonteCarloGraphController() {

    }

    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        self.setVariable("monteCarloGraphController", this, true);
    }

    public void onCancel(Event event) {
        cancel();
    }

    public void cancel() {
        self.setVisible(false);
    }

    public void generateMonteCarloGraph(String orderName, Map<LocalDate, BigDecimal> data) {
        CategoryModel xymodel = new SimpleCategoryModel();

        List<LocalDate> dates = new ArrayList(data.keySet());
        Collections.sort(dates);
        for (LocalDate each: dates) {
            String labelDate = each.toString();
            xymodel.setValue(orderName, labelDate, data.get(each));
        }
        monteCarloChart.setModel(xymodel);
    }

}
