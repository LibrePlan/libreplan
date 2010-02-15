<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>


<div id="row${self.uuid}" class="row" z.valor="boxid="${self.uuid}">
    <div id="${self.uuid}" z.type="ganttz.taskcontainer.TaskContainer" idTask="${self.id}"
       z.autoz="true"${self.outerAttrs}" class="taskgroup"
       onMouseover="zkTasklist.showTooltip('tasktooltip${self.uuid}');"
       onMouseOut="zkTasklist.hideTooltip('tasktooltip${self.uuid}');">
		<div class="task-labels">${self.labelsText}</div>
		<div class="task-resources">
			<div class="task-resources-inner">${self.resourcesText}</div>
		</div>
		<div class="taskcontainer_completion">
	        <div class="completion"></div>
	        <div class="completion2"></div>
		</div>
		<div class="border-container">
			<div class="taskgroup_start"></div>
			<div class="taskgroup_end"></div>
        </div>
		<div id="tasktooltip${self.uuid}" class="task_tooltip">${self.tooltipText}</div>
    </div>
</div>
