<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>
<h3>Resources Load</h3>
<div id="${self.uuid}" z.type="ganttz.resourcesloadpanel.ResourcesLoadPanel" ${self.outerAttrs}>
    <c:forEach var="child" items="${self.children}">
        ${z:redraw(child, null)}
    </c:forEach>
</div>

<br/>