<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/yahoo-dom-event/yahoo-dom-event.js" ></script>

<script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/selector/selector-min.js"></script> 

<!-- Drag and Drop source file -->
<script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/dragdrop/dragdrop-min.js" ></script>

<!-- Combo-handled YUI JS files: -->
<script type="text/javascript" src="http://yui.yahooapis.com/combo?2.7.0/build/yahoo-dom-event/yahoo-dom-event.js&2.7.0/build/dragdrop/dragdrop-min.js"></script>

<!-- Combo-handled YUI CSS files: -->
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.7.0/build/resize/assets/skins/sam/resize.css">

<c:include page="~./ganttz/css/task.css.dsp"/>

<!-- Combo-handled YUI JS files: -->
<script type="text/javascript" src="http://yui.yahooapis.com/combo?2.7.0/build/yahoo-dom-event/yahoo-dom-event.js&2.7.0/build/element/element-min.js&2.7.0/build/dragdrop/dragdrop-min.js&2.7.0/build/resize/resize-min.js"></script>
<!-- Source file -->

<script type="text/javascript" src="http://yui.yahooapis.com/2.7.0/build/logger/logger-min.js"></script>

<script type="text/javascript">
document.body.class = "yui-skin-sam";
/*var myContainer = document.body.insertBefore(document.createElement("div"),document.body.childNodes[0]);
var myLogReader = new YAHOO.widget.LogReader(myContainer);*/
</script>



<c:set var="self" value="${requestScope.arg.self}"/>

<div id="${self.uuid}" z.type="ganttz.tasklist.Tasklist" z.autoz="true"${self.outerAttrs}" sameHeightElementId="${self.sameHeightElementId}">

<div id="listtasks">
    <c:forEach var="child" items="${self.children}">
        ${z:redraw(child, null)}
    </c:forEach>
</div>

</div>