<%--
band.dsp

{{IS_NOTE
	Purpose:
		Display Plotinfo of the Timeplot.
	Description:
		
	History:
		Sun Nov 20 12:20:13     2007, Created by Gu WeiXing
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/zk/core.dsp.tld" prefix="u" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<span id="${self.uuid}"${self.innerAttrs} z.type="timeplotz.timeplot.Plotinfo">
	<c:forEach var="child" items="${self.children}">
		${u:redraw(child, null)}
	</c:forEach>
</span>
