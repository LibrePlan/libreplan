package org.navalplanner.web.montecarlo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void generateMonteCarloGraph(String orderName, Map<LocalDate, BigDecimal> data, boolean byWeek) {
        CategoryModel xymodel;
        if (byWeek) {
            xymodel = generateMonteCarloGraphByWeek(orderName, groupByWeek(data));
        } else {
            xymodel = generateMonteCarloGraphByDay(orderName, data);
        }
        monteCarloChart.setModel(xymodel);
    }

    private CategoryModel generateMonteCarloGraphByDay(String orderName, Map<LocalDate, BigDecimal> data) {
        CategoryModel result = new SimpleCategoryModel();

        List<LocalDate> dates = new ArrayList(data.keySet());
        Collections.sort(dates);
        LocalDate first = dates.get(0);
        LocalDate last = dates.get(dates.size() - 1);
        for (LocalDate i = first; i.isBefore(last) || i.isEqual(last); i = i
                .plusDays(1)) {
            String labelDate = i.toString();
            result.setValue(orderName, labelDate, data.get(i));
        }
        return result;
    }

    private CategoryModel generateMonteCarloGraphByWeek(String orderName, Map<Integer, BigDecimal> data) {
        CategoryModel result = new SimpleCategoryModel();

        List<Integer> weeks = new ArrayList(data.keySet());
        Collections.sort(weeks);
        Integer first = weeks.get(0);
        Integer last = weeks.get(weeks.size() - 1);
        for (Integer i = first; i <= last; i = i + 1) {
            plotWeekValue(result, orderName, "W" + i, data.get(i));
        }
        return result;
    }

    private void plotWeekValue(CategoryModel model, String orderName, String date, BigDecimal probability) {
        if (probability == null) {
            probability = BigDecimal.ZERO;
        }
        model.setValue(orderName, date, probability);
    }

    private Map<Integer, BigDecimal> groupByWeek(Map<LocalDate, BigDecimal> data) {
        Map<Integer, BigDecimal> result = new HashMap<Integer, BigDecimal>();

        // Group values of each date by week
        for (LocalDate date: data.keySet()) {
            Integer week = Integer.valueOf(date.getWeekOfWeekyear());
            BigDecimal value = result.get(week);
            value = (value != null) ? value.add(data.get(date)) : data.get(date);
            result.put(week, value);
        }
        return result;
    }

}
