package org.libreplan.web.dashboard;


import org.libreplan.business.orders.entities.Order;

import org.libreplan.web.orders.IOrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import java.util.ArrayList;
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

    // TODO make archived checkbox
    // TODO highlited cell when hover?

    private List<Order> preSalesOrders = new ArrayList<Order>();
    private List<Order> offeredOrders = new ArrayList<Order>();
    private List<Order> outsorcedOrders = new ArrayList<Order>();
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
    }

    public List<Order> getOrders(){
        return orderModel.getOrders();
    }

    private void fillOrderLists() {
        List<Order> orderList = new ArrayList<Order>();
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
                    outsorcedOrders.add(orderItem);
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

    private void setupPipelineGrid(){
        int rowsCount = findMaxList(preSalesOrders.size(), offeredOrders.size(), outsorcedOrders.size(), acceptedOrders.size(),
                startedOrders.size(), onHoldOrders.size(), finishedOrders.size(), cancelledOrders.size(), storedOrders.size());

        Rows rows = new Rows();
        for (int i = 0; i < rowsCount; i++){
            Row row = new Row();
            for (int columns = 0; columns < 9; columns++) row.appendChild(new Label());
            rows.appendChild(row);
        }

        pipelineGrid.appendChild(rows);

        // Fill data into first column and so on with other columns devided by Enter in code...

        for (int i = 0; i < preSalesOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 0) ).setValue(preSalesOrders.get(i).getName());
            String tooltipText = "Start date: " + preSalesOrders.get(i).getInitDate() +
                    "\n" + "End date: " + preSalesOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 0) ).setTooltiptext(tooltipText);
        }

        for (int i = 0; i < offeredOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 1) ).setValue(offeredOrders.get(i).getName());
            String tooltipText = "Start date: " + offeredOrders.get(i).getInitDate() +
                    "\n" + "End date: " + offeredOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 1) ).setTooltiptext(tooltipText);
        }

        for (int i = 0; i < outsorcedOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 2) ).setValue(outsorcedOrders.get(i).getName());
            String tooltipText = "Start date: " + outsorcedOrders.get(i).getInitDate() +
                    "\n" + "End date: " + outsorcedOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 2) ).setTooltiptext(tooltipText);
        }

        for (int i = 0; i < acceptedOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 3) ).setValue(acceptedOrders.get(i).getName());
            String tooltipText = "Start date: " + acceptedOrders.get(i).getInitDate() +
                    "\n" + "End date: " + acceptedOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 3) ).setTooltiptext(tooltipText);
        }

        for (int i = 0; i < startedOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 4) ).setValue(startedOrders.get(i).getName());
            String tooltipText = "Start date: " + startedOrders.get(i).getInitDate() +
                    "\n" + "End date: " + startedOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 4) ).setTooltiptext(tooltipText);
        }

        for (int i = 0; i < onHoldOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 5) ).setValue(onHoldOrders.get(i).getName());
            String tooltipText = "Start date: " + onHoldOrders.get(i).getInitDate() +
                    "\n" + "End date: " + onHoldOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 5) ).setTooltiptext(tooltipText);
        }

        for (int i = 0; i < finishedOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 6) ).setValue(finishedOrders.get(i).getName());
            String tooltipText = "Start date: " + finishedOrders.get(i).getInitDate() +
                    "\n" + "End date: " + finishedOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 6) ).setTooltiptext(tooltipText);
        }

        for (int i = 0; i < cancelledOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 7) ).setValue(cancelledOrders.get(i).getName());
            String tooltipText = "Start date: " + cancelledOrders.get(i).getInitDate() +
                    "\n" + "End date: " + cancelledOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 7) ).setTooltiptext(tooltipText);
        }

        for (int i = 0; i < storedOrders.size(); i++){
            ( (Label) pipelineGrid.getCell(i, 8) ).setValue(storedOrders.get(i).getName());
            String tooltipText = "Start date: " + storedOrders.get(i).getInitDate() +
                    "\n" + "End date: " + storedOrders.get(i).getDeadline() +
                    "\n" + "Progress: ";
            ( (Label) pipelineGrid.getCell(i, 8) ).setTooltiptext(tooltipText);
        }
    }

    private int findMaxList(int preSales, int offered, int outsorced, int accepted, int started, int onHold, int finished,
                            int cancelled, int stored){

        int[] sizes = {preSales, offered, outsorced, accepted, started, onHold, finished, cancelled, stored};
        int max = sizes[0];

        for (int i = 1; i < sizes.length; i++)
            if ( sizes[i] > max ) max = sizes[i];

        return max;
    }
}
