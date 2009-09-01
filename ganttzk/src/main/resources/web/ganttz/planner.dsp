<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>

<link rel="stylesheet" type="text/css" href="${self.contextPath}/zkau/web/js/yui/2.7.0/resize/assets/skins/sam/resize.css">

<c:include page="~./ganttz/css/task.css.dsp"/>

<script type="text/javascript">
/*<![CDATA[ */
    webapp_context_path = '${self.contextPath}';
/*]]> */
</script>

<div id="${self.uuid}" z.type="ganttz.planner.Planner" ${self.outerAttrs}>
    <c:forEach var="child" items="${self.children}">
        ${z:redraw(child, null)}
    </c:forEach>
</div>