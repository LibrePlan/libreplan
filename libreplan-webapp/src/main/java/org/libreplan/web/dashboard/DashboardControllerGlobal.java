package org.libreplan.web.dashboard;

import org.libreplan.business.orders.entities.Order;

import org.libreplan.web.orders.IOrderModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Row;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller to manage Dashboard tab on index page.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DashboardControllerGlobal extends GenericForwardComposer {

    // TODO enumns instead of numbers ( 0, 1, 2, 3, ... )
    // TODO 1 list instead of 8
    // TODO insert Label only in needed Cell

    private IOrderModel orderModel;

    private Grid pipelineGrid;

    private Checkbox storedColumnVisible;

    private List<Order> preSalesOrders = new ArrayList<>();

    private List<Order> offeredOrders = new ArrayList<>();

    private List<Order> outsourcedOrders = new ArrayList<>();

    private List<Order> acceptedOrders = new ArrayList<>();

    private List<Order> startedOrders = new ArrayList<>();

    private List<Order> onHoldOrders = new ArrayList<>();

    private List<Order> finishedOrders = new ArrayList<>();

    private List<Order> cancelledOrders = new ArrayList<>();

    private List<Order> storedOrders = new ArrayList<>();

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
        component.setAttribute("dashboardControllerGlobal", this, true);
        orderModel = (IOrderModel) SpringUtil.getBean("orderModel");
        fillOrderLists();
        setupPipelineGrid();
        showStoredColumn();
    }

    public List<Order> getOrders() {
        return orderModel.getOrders();
    }

    private void fillOrderLists() {
        for (Order orderItem : getOrders()) {

            switch ( orderItem.getState() ) {

                case PRE_SALES:
                    preSalesOrders.add(orderItem);
                    break;

                case OFFERED:
                    offeredOrders.add(orderItem);
                    break;

                case OUTSOURCED:
                    outsourcedOrders.add(orderItem);
                    break;

                case ACCEPTED:
                    acceptedOrders.add(orderItem);
                    break;

                case STARTED:
                    startedOrders.add(orderItem);
                    break;

                case ON_HOLD:
                    onHoldOrders.add(orderItem);
                    break;

                case FINISHED:
                    finishedOrders.add(orderItem);
                    break;

                case CANCELLED:
                    cancelledOrders.add(orderItem);
                    break;

                case STORED:
                    storedOrders.add(orderItem);
                    break;

                default:
                    break;
            }
        }
    }

    private void setupPipelineGrid() throws ParseException {
        int[] sizes = {
                preSalesOrders.size(),
                offeredOrders.size(),
                outsourcedOrders.size(),
                acceptedOrders.size(),
                startedOrders.size(),
                onHoldOrders.size(),
                finishedOrders.size(),
                cancelledOrders.size(),
                storedOrders.size() };

        int rowsCount = findMaxList(sizes);

        Rows rows = new Rows();
        for (int i = 0; i < rowsCount; i++) {
            Row row = new Row();

            for (int columns = 0; columns < 9; columns++) {
                row.appendChild(new Label());
            }

            rows.appendChild(row);
        }

        pipelineGrid.appendChild(rows);

        // Fill data into first column and so on with other columns divided by Enter in code
        processList(preSalesOrders, 0);
        processList(offeredOrders, 1);
        processList(outsourcedOrders, 2);
        processList(acceptedOrders, 3);
        processList(startedOrders, 4);
        processList(onHoldOrders, 5);
        processList(finishedOrders, 6);
        processList(cancelledOrders, 7);
        processList(storedOrders, 8);
    }

    private void processList(List<Order> currentList, int index) {
        if ( !currentList.isEmpty() ) {
            for (int i = 0; i < currentList.size(); i++) {

                try {
                    String outputInit = getOrderInitDate(currentList.get(i));
                    String outputDeadline = getOrderDeadline(currentList.get(i));

                    ( (Label) pipelineGrid.getCell(i, index) ).setValue(currentList.get(i).getName());

                    String tooltipText = "Start date: " + outputInit +
                            "\n" + "End date: " + outputDeadline +
                            "\n" + "Progress: " + currentList.get(i).getAdvancePercentage() + " %";

                    ( (Label) pipelineGrid.getCell(i, index) ).setTooltiptext(tooltipText);
                    ( (Label) pipelineGrid.getCell(i, index) ).setClass("label-highlight");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Remove time, timezone from full-date string.
     */
    private String getOrderInitDate(Order order) throws ParseException {
        return new SimpleDateFormat("EEE MMM dd yyyy").format(order.getInitDate());
    }

    private String getOrderDeadline(Order order) throws ParseException {
        return (order.getDeadline() != null)
                ? new SimpleDateFormat("EEE MMM dd yyyy").format(order.getDeadline())
                : "-- empty --";
    }

    /**
     * Should be public!
     * Used in _pipeline.zul
     */
    public void showStoredColumn() throws ParseException {
        if ( storedColumnVisible.isChecked() ) {
            setStoredColumnVisible(true);
        }
        else if ( !storedColumnVisible.isChecked() ) {
            setStoredColumnVisible(false);
        }
    }

    private void setStoredColumnVisible(boolean value) {
        for (int i = 0; i < storedOrders.size(); i++) {
            ( pipelineGrid.getCell(i, 8) ).setVisible(value);
        }
    }

    private int findMaxList(int[] sizes) {
        int max = sizes[0];

        for (int i = 1; i < sizes.length; i++) {
            if ( sizes[i] > max ) {
                max = sizes[i];
            }
        }

        return max;
    }
}
