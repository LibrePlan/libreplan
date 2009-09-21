package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zul.Messagebox;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
/**
 * A command that saves the changes in the taskElements.
 * It can be considered the final step in the conversation <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SaveCommand implements ISaveCommand {

    @Autowired
    private ITaskElementDAO taskElementDAO;

    private PlanningState state;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Override
    public void setState(PlanningState state) {
        this.state = state;
    }

    @Override
    @Transactional
    public void doAction(IContext<TaskElement> context) {
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                doTheSaving();
                return null;
            }
        });
        notifyUserThatSavingIsDone();
    }

    private void notifyUserThatSavingIsDone() {
        try {
            Messagebox.show(_("Scheduling saved"), _("Information"), Messagebox.OK,
                    Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doTheSaving() {
        for (TaskElement taskElement : state.getTasksToSave()) {
            taskElementDAO.save(taskElement);
            if (taskElement instanceof Task) {
                if (!((Task) taskElement).isValidResourceAllocationWorkers()) {
                    throw new RuntimeException(_("The task '{0}' has some repeated Worker assigned",
                                taskElement.getName()));
                }
                for (ResourceAllocation<?> resourceAllocation : ((Task) taskElement)
                        .getResourceAllocations()) {
                    resourceAllocation.dontPoseAsTransientObjectAnymore();
                    for (DayAssignment dayAssignment : (List<? extends DayAssignment>) resourceAllocation
                            .getAssignments()) {
                        dayAssignment.dontPoseAsTransientObjectAnymore();
                    }
                }
            }
        }
        for (TaskElement taskElement : state.getToRemove()) {
            if (taskElementDAO.exists(taskElement.getId())) {
                // it might have already been saved in a previous save action
                try {
                    taskElementDAO.remove(taskElement.getId());
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        taskElementDAO.removeOrphanedDayAssignments();
    }

    @Override
    public String getName() {
        return _("Save");
    }

}
