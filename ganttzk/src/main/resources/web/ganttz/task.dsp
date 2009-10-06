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


<div id="row${self.uuid}" class="row" z.valor="boxid="${self.uuid}">
    <div id="${self.uuid}" z.type="ganttz.task.Task" idTask="${self.id}"
        z.autoz="true"${self.outerAttrs}" class="box"
        onMouseover="zkTasklist.showTooltip('tasktooltip${self.uuid}');"
        onMouseOut="zkTasklist.hideTooltip('tasktooltip${self.uuid}');">
        <div class="completion"></div>
        <div class="completion2"></div>
		<div id="tasktooltip${self.uuid}" class="task_tooltip">TooltipText: </div>
    </div>
</div>
