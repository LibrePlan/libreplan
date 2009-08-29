<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>

<div id="${self.uuid}" class="timetrackedTableWithLeftPane"
    z.type="ganttz.timetrackedTableWithLeftPane.TimeTrackedTableWithLeftPane"
    ${self.outerAttrs}">
    ${z:redraw(self.leftPane, null)}
    ${z:redraw(self.timeTrackedTable, null)}
</div>