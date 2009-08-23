<script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/selector/selector-min.js"></script>

<!-- Combo-handled YUI JS files: -->
<script type="text/javascript" src="http://yui.yahooapis.com/combo?2.7.0/build/yahoo-dom-event/yahoo-dom-event.js&2.7.0/build/dragdrop/dragdrop-min.js"></script>

<!-- Combo-handled YUI CSS files: -->
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.7.0/build/resize/assets/skins/sam/resize.css">

<c:include page="~./ganttz/css/task.css.dsp"/>

<!-- Combo-handled YUI JS files: -->
<script type="text/javascript" src="http://yui.yahooapis.com/combo?2.7.0/build/yahoo-dom-event/yahoo-dom-event.js&2.7.0/build/element/element-min.js&2.7.0/build/dragdrop/dragdrop-min.js&2.7.0/build/resize/resize-min.js"></script>
<!-- Source file -->

<script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/logger/logger-min.js"></script>

<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>

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