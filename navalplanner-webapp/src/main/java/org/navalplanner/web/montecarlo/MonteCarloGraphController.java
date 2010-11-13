package org.navalplanner.web.montecarlo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.SimpleCategoryModel;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 *         Generates a BarChart 3D with the results of a MonteCarlo computation
 *
 *         The window also shows a set of Datebox controllers that allow the
 *         user to specify a start and end date and calculate the probability
 *         density between both values
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MonteCarloGraphController extends GenericForwardComposer {

    private Chart monteCarloChart;

    private Datebox dateboxStartDateProbability;

    private Datebox dateboxEndDateProbability;

    private Decimalbox dbIntervalProbability;

    private List<LocalDate> dates;

    private Map<LocalDate, BigDecimal> monteCarloValues;

    public MonteCarloGraphController() {

    }

    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        self.setVariable("monteCarloGraphController", this, true);
    }

    public void generateMonteCarloGraph(String orderName, Map<LocalDate, BigDecimal> data, boolean byWeek) {
        CategoryModel xymodel;

        initializeMonteCarloValues(data);

        // Generate MonteCarlo chart
        if (byWeek) {
            xymodel = generateMonteCarloGraphByWeek(orderName, groupByWeek(data));
        } else {
            xymodel = generateMonteCarloGraphByDay(orderName, data);
        }
        monteCarloChart.setModel(xymodel);

        // Initialize dates for calculating probability density
        LocalDate first = getFirstDate();
        LocalDate last = getLastDate();
        dateboxStartDateProbability.setValue(toDate(first));
        dateboxEndDateProbability.setValue(toDate(last));
        dbIntervalProbability.setValue(calculateProbabilityDensity(first, last));
    }

    private void initializeMonteCarloValues(Map<LocalDate, BigDecimal> data) {
        monteCarloValues = data;
        initializeDates(data);
    }

    private void initializeDates(Map<LocalDate, BigDecimal> monteCarloValues) {
        dates = new ArrayList(monteCarloValues.keySet());
        Collections.sort(dates);
    }

    private CategoryModel generateMonteCarloGraphByDay(String orderName, Map<LocalDate, BigDecimal> data) {
        CategoryModel result = new SimpleCategoryModel();

        LocalDate first = getFirstDate();
        LocalDate last = getLastDate();
        for (LocalDate i = first; i.compareTo(last) <= 0; i = i.plusDays(1)) {
            String labelDate = i.toString();
            result.setValue(orderName, labelDate, data.get(i));
        }
        return result;
    }

    private LocalDate getFirstDate() {
        return dates.get(0);
    }

    private LocalDate getLastDate() {
        return dates.get(dates.size() - 1);
    }

    private Date toDate(LocalDate date) {
        return date.toDateTimeAtStartOfDay().toDate();
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

    private BigDecimal calculateProbabilityDensity(LocalDate start, LocalDate end) {
        BigDecimal result = BigDecimal.ZERO;

        for (LocalDate i = start; i.compareTo(end) <= 0; i = i.plusDays(1) ) {
            BigDecimal value = monteCarloValues.get(i);
            if (value == null) {
                continue;
            }
            result = result.add(value);
        }
        return result;
    }

    public void showProbabilityDensity(Datebox startDatebox, Datebox endDatebox) {
        LocalDate start, end;

        start = (startDatebox.getValue() != null) ? new LocalDate(
                startDatebox.getValue()) : getFirstDate();
        end = (endDatebox.getValue() != null) ? new LocalDate(
                endDatebox.getValue()) : getLastDate();
        BigDecimal probabilityDensity = calculateProbabilityDensity(start, end);
        dbIntervalProbability.setValue(probabilityDensity);
    }

    public void onCancel(Event event) {
        cancel();
    }

    public void cancel() {
        self.setVisible(false);
    }

}
