
<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>


<div id="row${self.uuid}" class="row" z.valor="boxid="${self.uuid}">
    <div id="${self.uuid}" z.type="ganttz.task.Task" idTask="${self.id}"
        z.autoz="true"${self.outerAttrs}">
        <div id="completion${self.uuid}" class="completion"></div>
    </div>
</div>
