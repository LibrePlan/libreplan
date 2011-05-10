<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>

<div class="${self.CSSClass}" id="${self.uuid}" z.type="ganttz.dependency.Dependency"
    idTaskOrig="${self.idTaskOrig}" idTaskEnd="${self.idTaskEnd}" ${self.outerAttrs}
    type=${self.dependencyType}>
</div>
