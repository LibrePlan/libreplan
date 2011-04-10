<%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/zk/core.dsp.tld" prefix="u"%>
<c:set var="self" value="${requestScope.arg.self}" />
<div id="${self.uuid}"${self.innerAttrs} z.type="timeplotz.timeplot.Timeplot">
<div id="${self.uuid}!timeplot"
	style="width: ${self.width} ; height: ${self.height} ;border: 1px solid #aaa">
</div>
<c:forEach var="child" items="${self.children}">
		${u:redraw(child, null)}
	</c:forEach>
</div>
