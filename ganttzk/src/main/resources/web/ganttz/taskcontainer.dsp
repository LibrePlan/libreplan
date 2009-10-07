<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>


<div id="row${self.uuid}" class="row" z.valor="boxid="${self.uuid}">
    <div id="${self.uuid}" z.type="ganttz.taskcontainer.TaskContainer" idTask="${self.id}"
       z.autoz="true"${self.outerAttrs}" class="taskgroup">
	<div class="taskcontainer_completion">
	        <div class="completion"></div>
	        <div class="completion2"></div>
	</div>
        <div class="taskgroup_start"></div>
        <div class="taskgroup_end"></div>
    </div>
</div>
