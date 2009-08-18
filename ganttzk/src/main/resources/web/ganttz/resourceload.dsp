<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>

<div id="${self.uuid}" class="row_resourceload"
    z.autoz="true" ${self.outerAttrs}" >
    <span class="resourceload_name" id="${self.uuid}!real">${self.resourceLoadName}</span>
    <c:forEach var="child" items="${self.children}">
        <div id="loadinterval${child.uuid}" style="width: ${child.lenght}%;"
            class="taskassignmentinterval ${child.loadLevel}"></div>
    </c:forEach>
</div>
