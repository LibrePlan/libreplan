package org.libreplan.web.dashboard;

import org.libreplan.business.orders.entities.Order;

import org.libreplan.web.orders.IOrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Row;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 20.11.15.
 */

@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DashboardControllerGlobal extends GenericForwardComposer {

    @Autowired
    private IOrderModel orderModel;

    private Grid pipelineGrid;
    private Checkbox storedColumnVisible;

    private List<Order> preSalesOrders = new ArrayList<Order>();
    private List<Order> offeredOrders = new ArrayList<Order>();
    private List<Order> outsourcedOrders = new ArrayList<Order>();
    private List<Order> acceptedOrders = new ArrayList<Order>();
    private List<Order> startedOrders = new ArrayList<Order>();
    private List<Order> onHoldOrders = new ArrayList<Order>();
    private List<Order> finishedOrders = new ArrayList<Order>();
    private List<Order> cancelledOrders = new ArrayList<Order>();
    private List<Order> storedOrders = new ArrayList<Order>();

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
        component.setVariable("dashboardControllerGlobal", this, true);
        fillOrderLists();
        setupPipelineGrid();
        showStoredColumn();
    }

    public List<Order> getOrders(){
        return orderModel.getOrders();
    }

    private void fillOrderLists() {
        for (Order orderItem : getOrders()){
            switch (orderItem.getState()){
                case PRE_SALES: {
                    preSalesOrders.add(orderItem);
                    break;
                }
                case OFFERED: {
                    offeredOrders.add(orderItem);
                    break;
                }
                case OUTSOURCED: {
                    outsourcedOrders.add(orderItem);
                    break;
                }
                case ACCEPTED: {
                    acceptedOrders.add(orderItem);
                    break;
                }
                case STARTED: {
                    startedOrders.add(orderItem);
                    break;
                }
                case ON_HOLD: {
                    onHoldOrders.add(orderItem);
                    break;
                }
                case FINISHED: {
                    finishedOrders.add(orderItem);
                    break;
                }
                case CANCELLED: {
                    cancelledOrders.add(orderItem);
                    break;
                }
                case STORED: {
                    storedOrders.add(orderItem);
                    break;
                }
            }
        }
    }

    private void setupPipelineGrid() throws ParseException {
        int rowsCount = findMaxList(preSalesOrders.size(), offeredOrders.size(), outsourcedOrders.size(), acceptedOrders.size(),
                startedOrders.size(), onHoldOrders.size(), finishedOrders.size(), cancelledOrders.size(), storedOrders.size());

        Rows rows = new Rows();
        for (int i = 0; i < rowsCount; i++){
            Row row = new Row();
            for (int columns = 0; columns < 9; columns++) row.appendChild(new Label());
            rows.appendChild(row);
        }

        pipelineGrid.appendChild(rows);

        // Fill data into first column and so on with other columns divided by Enter in code

        if ( preSalesOrders.size() > 0 )
            for (int i = 0; i < preSalesOrders.size(); i++){
                String outputInit = getOrderInitDate(preSalesOrders.get(i));
                String outputDeadline = getOrderDeadline(preSalesOrders.get(i));

                ( (Label) pipelineGrid.getCell(i, 0) ).setValue(preSalesOrders.get(i).getName());
                String tooltipText = "Start date: " + outputInit +
                        "\n" + "End date: " + outputDeadline +
                        "\n" + "Progress: " + preSalesOrders.get(i).getAdvancePercentage() + " %";
                ( (Label) pipelineGrid.getCell(i, 0) ).setTooltiptext(tooltipText);
                ( (Label) pipelineGrid.getCell(i, 0) ).setClass("label-highlight");
            }


        if ( offeredOrders.size() > 0 )
            for (int i = 0; i < offeredOrders.size(); i++){
                String outputInit = getOrderInitDate(offeredOrders.get(i));
                String outputDeadline = getOrderDeadline(offeredOrders.get(i));

                ( (Label) pipelineGrid.getCell(i, 1) ).setValue(offeredOrders.get(i).getName());
                String tooltipText = "Start date: " + outputInit +
                        "\n" + "End date: " + outputDeadline +
                        "\n" + "Progress: " + offeredOrders.get(i).getAdvancePercentage() + " %";
                ( (Label) pipelineGrid.getCell(i, 1) ).setTooltiptext(tooltipText);
                ( (Label) pipelineGrid.getCell(i, 1) ).setClass("label-highlight");
            }


        if ( outsourcedOrders.size() > 0 )
            for (int i = 0; i < outsourcedOrders.size(); i++){
                String outputInit = getOrderInitDate(outsourcedOrders.get(i));
                String outputDeadline = getOrderDeadline(outsourcedOrders.get(i));

                ( (Label) pipelineGrid.getCell(i, 2) ).setValue(outsourcedOrders.get(i).getName());
                String tooltipText = "Start date: " + outputInit +
                        "\n" + "End date: " + outputDeadline +
                        "\n" + "Progress: " + outsourcedOrders.get(i).getAdvancePercentage() + " %";
                ( (Label) pipelineGrid.getCell(i, 2) ).setTooltiptext(tooltipText);
                ( (Label) pipelineGrid.getCell(i, 2) ).setClass("label-highlight");
            }


        if ( acceptedOrders.size() > 0 )
            for (int i = 0; i < acceptedOrders.size(); i++){
                String outputInit = getOrderInitDate(acceptedOrders.get(i));
                String outputDeadline = getOrderDeadline(acceptedOrders.get(i));

                ( (Label) pipelineGrid.getCell(i, 3) ).setValue(acceptedOrders.get(i).getName());
                String tooltipText = "Start date: " + outputInit +
                        "\n" + "End date: " + outputDeadline +
                        "\n" + "Progress: " + acceptedOrders.get(i).getAdvancePercentage() + " %";
                ( (Label) pipelineGrid.getCell(i, 3) ).setTooltiptext(tooltipText);
                ( (Label) pipelineGrid.getCell(i, 3) ).setClass("label-highlight");
            }


        if ( startedOrders.size() > 0 )
            for (int i = 0; i < startedOrders.size(); i++){
                String outputInit = getOrderInitDate(startedOrders.get(i));
                String outputDeadline = getOrderDeadline(startedOrders.get(i));

                ( (Label) pipelineGrid.getCell(i, 4) ).setValue(startedOrders.get(i).getName());
                String tooltipText = "Start date: " + outputInit +
                        "\n" + "End date: " + outputDeadline +
                        "\n" + "Progress: " + startedOrders.get(i).getAdvancePercentage() + " %";
                ( (Label) pipelineGrid.getCell(i, 4) ).setTooltiptext(tooltipText);
                ( (Label) pipelineGrid.getCell(i, 4) ).setClass("label-highlight");
            }


        if ( onHoldOrders.size() > 0 )
            for (int i = 0; i < onHoldOrders.size(); i++){
                String outputInit = getOrderInitDate(onHoldOrders.get(i));
                String outputDeadline = getOrderDeadline(onHoldOrders.get(i));

                ( (Label) pipelineGrid.getCell(i, 5) ).setValue(onHoldOrders.get(i).getName());
                String tooltipText = "Start date: " + outputInit +
                        "\n" + "End date: " + outputDeadline +
                        "\n" + "Progress: " + onHoldOrders.get(i).getAdvancePercentage() + " %";
                ( (Label) pipelineGrid.getCell(i, 5) ).setTooltiptext(tooltipText);
                ( (Label) pipelineGrid.getCell(i, 5) ).setClass("label-highlight");
            }


        if ( finishedOrders.size() > 0 )
            for (int i = 0; i < finishedOrders.size(); i++){
                String outputInit = getOrderInitDate(finishedOrders.get(i));
                String outputDeadline = getOrderDeadline(finishedOrders.get(i));

                ( (Label) pipelineGrid.getCell(i, 6) ).setValue(finishedOrders.get(i).getName());
                String tooltipText = "Start date: " + outputInit +
                        "\n" + "End date: " + outputDeadline +
                        "\n" + "Progress: " + finishedOrders.get(i).getAdvancePercentage() + " %";
                ( (Label) pipelineGrid.getCell(i, 6) ).setTooltiptext(tooltipText);
                ( (Label) pipelineGrid.getCell(i, 6) ).setClass("label-highlight");
            }


        if ( cancelledOrders.size() > 0 )
            for (int i = 0; i < cancelledOrders.size(); i++){
                String outputInit = getOrderInitDate(cancelledOrders.get(i));
                String outputDeadline = getOrderDeadline(cancelledOrders.get(i));


                ( (Label) pipelineGrid.getCell(i, 7) ).setValue(cancelledOrders.get(i).getName());
                String tooltipText = "Start date: " + outputInit +
                        "\n" + "End date: " + outputDeadline +
                        "\n" + "Progress: " + cancelledOrders.get(i).getAdvancePercentage() + " %";
                ( (Label) pipelineGrid.getCell(i, 7) ).setTooltiptext(tooltipText);
                ( (Label) pipelineGrid.getCell(i, 7) ).setClass("label-highlight");
            }
    }

    // Remove time, timezone from full-date string
    private String getOrderInitDate(Order order) throws ParseException {
        return new SimpleDateFormat("EEE MMM dd yyyy").format(order.getInitDate());
    }
    private String getOrderDeadline(Order order) throws ParseException {
        return new SimpleDateFormat("EEE MMM dd yyyy").format(order.getDeadline());
    }

    public void showStoredColumn() throws ParseException {
        if ( storedColumnVisible.isChecked() ){
            if ( storedOrders.size() > 0 ){
                for (int i = 0; i < storedOrders.size(); i++){
                    String outputInit = getOrderInitDate(storedOrders.get(i));
                    String outputDeadline = getOrderDeadline(storedOrders.get(i));

                    pipelineGrid.getCell(i, 8).setVisible(true);

                    ( (Label) pipelineGrid.getCell(i, 8) ).setValue(storedOrders.get(i).getName());
                    String tooltipText = "Start date: " + outputInit +
                            "\n" + "End date: " + outputDeadline +
                            "\n" + "Progress: " + storedOrders.get(i).getAdvancePercentage() + " %";
                    ( (Label) pipelineGrid.getCell(i, 8) ).setTooltiptext(tooltipText);
                    ( (Label) pipelineGrid.getCell(i, 8) ).setClass("label-highlight");
                    }
                }
            }
            else if ( !storedColumnVisible.isChecked() ){
                for (int i = 0; i < storedOrders.size(); i++)
                    pipelineGrid.getCell(i, 8).setVisible(false);
            }
    }

    private int findMaxList(int preSales, int offered, int outsourced, int accepted, int started, int onHold, int finished,
                            int cancelled, int stored){

        int[] sizes = {preSales, offered, outsourced, accepted, started, onHold, finished, cancelled, stored};
        int max = sizes[0];

        for (int i = 1; i < sizes.length; i++)
            if ( sizes[i] > max ) max = sizes[i];

        return max;
    }
}
