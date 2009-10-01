<!--
  This file is part of ###PROJECT_NAME###

  Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
                     Desenvolvemento Tecnolóxico de Galicia

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->

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