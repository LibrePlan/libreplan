<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>

<div id="${self.uuid}" z.type="ganttz.ganttpanel.GanttPanel" ${self.outerAttrs}>
    <div id="ganttpanel">
        <c:forEach var="child" items="${self.children}">
            ${z:redraw(child, null)}
        </c:forEach>
    </div>
</div>

<br/>

<div id="ganttpanel_scroller_x">
    <div id="ganttpanel_inner_scroller_x"></div>
</div>
<div id="ganttpanel_scroller_y"><div id="ganttpanel_inner_scroller_y"></div>
