package org.navalplanner.web.workorders;

import org.zkoss.zk.ui.event.Event;
import java.awt.dnd.DragSourceEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.navalplanner.web.common.Util;
import org.zkoss.ganttz.Task;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.metainfo.EventHandlerMap;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Tree;
import org.zkoss.zul.api.Window;
import org.zkoss.zul.event.TreeDataListener;

/**
 * Controller for {@link WorkOrganization} view of WorkOrder entitites <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class WorkOrganizationController extends GenericForwardComposer {

    private Tree tree;
    private WorkOrganizationModel model;
    private List taskWorkList = new ArrayList();
    private TaskWorkTreeitemRenderer renderer = new TaskWorkTreeitemRenderer();

    public WorkOrganizationController() {
    }

    public TaskWorkTreeitemRenderer getRenderer() {
        return renderer;
    }

    public WorkOrganizationController(Tree tree) {
        this.tree = tree;
    }

    public void move(Component self, Component dragged) {
        /* if (self instanceof Treeitem) {
        if (dragged.getParent().getId().equals("right")) {
        self.insertBefore(dragged, self.getNextSibling());
        } else {
        self.insertBefore(dragged, self.getNextSibling());
        }
        } else {
        self.appendChild(dragged);
        } */

        Treeitem elem = new Treeitem("Elemento");
        //elem.appendChild(dragged);

        if (self != null) {
            System.out.println("SELF: " + self.toString());
        }
        if (dragged != null) {
            System.out.println("DRAGGED: " + dragged.toString());
        }

        self.appendChild(elem);
    }

    public void addTaskWork() {
        if (tree == null) {
            System.out.println("Tree is null");
        } else {
            int index = tree.getSelectedCount();
            taskWorkList.add(
                    index,
                    new SimpleTreeNode(new TaskWork("Nueva tarea", null, null, 10), new ArrayList()));
            this.tree.setModel(this.model);
        }
        Util.reloadBindings(tree);
    }

    public void removeTaskWork() {
        if (tree == null) {
            System.out.println("Tree is null");
        } else {
            if (!(taskWorkList.isEmpty())) {
                // Handle subchildren!
                int index = tree.getSelectedCount();
//                System.out.println(
//                        "TREE+ "+tree.getSelectedItems().toArray().toString());

                taskWorkList.remove(index);

                this.tree.setModel(this.model);
            }
        }
        Util.reloadBindings(tree);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);

        // Get real TaskWorkList
        List children1 = new ArrayList();

        children1.add(new SimpleTreeNode(new TaskWork("uno", null, null, 10), new ArrayList()));
        children1.add(new SimpleTreeNode(new TaskWork("dos", null, null, 10), new ArrayList()));
        children1.add(new SimpleTreeNode(new TaskWork("tres", null, null, 10), new ArrayList()));
        children1.add(new SimpleTreeNode(new TaskWork("cuatro", null, null, 10), new ArrayList()));

        SimpleTreeNode stn1 =
                new SimpleTreeNode(new TaskWork("uno", null, null, 10), children1);
        SimpleTreeNode stn2 =
                new SimpleTreeNode(new TaskWork("dos", null, null, 10), children1);
        SimpleTreeNode stn3 =
                new SimpleTreeNode(new TaskWork("tres", null, null, 10), new ArrayList());
        SimpleTreeNode stn4 =
                new SimpleTreeNode(new TaskWork("cuatro", null, null, 10), children1);

        taskWorkList.add(stn1);
        taskWorkList.add(stn2);
        taskWorkList.add(stn3);
        taskWorkList.add(stn4);

        this.model = new WorkOrganizationModel(taskWorkList);

//      Set model (annalize parameter values)
//        this.model = new WorkOrganizationModel(taskWorkList);
//        this.tree.setModel(this.model);
    }

    public WorkOrganizationModel getTasksWork() {
        return this.model;
    }

// -------------------------------------------------
// -------------------------------------------------
    public class TaskWork {

        private String name;
        private Date startDate;
        private Date endDate;
        private int hours;

        public TaskWork(String name, Date startdate, Date enddate, int hours) {
            this.name = name;
            this.startDate = startdate;
            this.endDate = enddate;
            this.hours = hours;
        }

        public Date getEndDate() {
            return endDate;
        }

        public int getHours() {
            return hours;
        }

        public String getName() {
            return name;
        }

        public Date getStartDate() {
            return startDate;
        }
    }

// -------------------------------------------------
// -------------------------------------------------
    public class TaskWorkTreeitemRenderer implements TreeitemRenderer {

        public void TaskWorkTreeitemRenderer() {
        }

        @Override
        public void render(Treeitem item, Object data) throws Exception {
            SimpleTreeNode t = (SimpleTreeNode) data;
            TaskWork taskWork = (TaskWork) t.getData();
            //Contruct treecells
            Treecell tcName = new Treecell(taskWork.getName());
            tcName.appendChild(new Textbox(taskWork.getName()));
            Treecell tcAccountId = new Treecell();
            tcAccountId.appendChild(new Textbox("" + taskWork.getHours()));
            Treecell tcDateStart = new Treecell();
            tcDateStart.appendChild(new Datebox(taskWork.getStartDate()));
            Treecell tcDateEnd = new Treecell();
            tcDateStart.appendChild(new Datebox(taskWork.getEndDate()));
            Treerow tr = null;
            /*
             * Since only one treerow is allowed, if treerow is not null,
             * append treecells to it. If treerow is null, contruct a new
             * treerow and attach it to item.
             */
            if (item.getTreerow() == null) {
                tr = new Treerow();
                tr.setParent(item);
            } else {
                tr = item.getTreerow();
                tr.getChildren().clear();
            }
            //Attach treecells to treerow
            tr.setDraggable("true");
            tr.setDroppable("true");

            tcName.setParent(tr);
            tcDateStart.setParent(tr);
            tcDateEnd.setParent(tr);
            tcAccountId.setParent(tr);
            item.setOpen(false);

            tr.addEventListener("onDrop", new EventListener() {

                @Override
                public void onEvent(org.zkoss.zk.ui.event.Event arg0) throws Exception {
                    DropEvent dropEvent = (DropEvent) arg0;
                    move((Component) dropEvent.getTarget(),
                            (Component) dropEvent.getDragged());
                    System.out.println("Dragging"+dropEvent.toString());
                }
            });

        }
    }
}