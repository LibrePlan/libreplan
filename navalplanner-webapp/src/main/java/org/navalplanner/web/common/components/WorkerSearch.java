package org.navalplanner.web.common.components;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.resources.search.WorkerSearchController;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Window;

/**
 * ZK macro component for searching {@link Worker} entities
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@SuppressWarnings("serial")
public class WorkerSearch extends HtmlMacroComponent {

    List<Worker> workers = new ArrayList<Worker>();

    public Window getWindow() {
        return (Window) getFellow("workerSearch");
    }

    public List<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }

}