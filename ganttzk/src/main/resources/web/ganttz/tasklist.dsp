<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<script type="text/javascript">
    document.body.class = "yui-skin-sam";
</script>

<div id="scroll_container">

<c:set var="self" value="${requestScope.arg.self}"/>

<div id="${self.uuid}" z.type="ganttz.tasklist.Tasklist" z.autoz="true"${self.outerAttrs}">


<div id="listtasks">
    <c:forEach var="child" items="${self.children}">
        ${z:redraw(child, null)}
    </c:forEach>
</div>

</div>
</div>