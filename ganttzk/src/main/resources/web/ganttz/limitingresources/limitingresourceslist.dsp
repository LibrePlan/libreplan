<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>

<c:set var="self" value="${requestScope.arg.self}"/>

<div id="${self.uuid}" ${self.outerAttrs} class="limitingresourceslist"
    z.type="ganttz.limitingresources.limitingresourceslist.LimitingResourcesList">
    <c:forEach var="child" items="${self.children}">
        ${z:redraw(child, null)}
    </c:forEach>
</div>


<!-- FIX: hardcoded dependency list test -->
<div z.autoz="true" z.type="ganttz.dependencylist.Dependencylist" id="z_7i_p73">
<div id="listdependencies">

    <div type="END_START" z.ctx="uuid(z_7i_383)" idtaskend="z_7i_v73" idtaskorig="z_7i_t73" z.type="ganttz.dependency.Dependency" id="z_7i_283" class="dependency">
		<img class="start extra_padding" src="/navalplanner-webapp/zkau/web/ganttz/img/pixel.gif" style="top: -35px; height: 5px; left: 222px; width:1px;">
		<img class="mid extra_padding" src="/navalplanner-webapp/zkau/web/ganttz/img/pixel.gif" style="top: -30px; width: 58px; left: 222px;height:1px;">
		<img class="end extra_padding" src="/navalplanner-webapp/zkau/web/ganttz/img/pixel.gif" style="top: -35px; left: 280px; height: 5px;width:1px;">
		<img class="arrow extra_padding" src="/navalplanner-webapp/zkau/web/ganttz/img/arrow.png" style="top: -45px; left: 275px; image-orientation: 270deg;">
	</div>

    <div type="END_START" idtaskend="z_7i_v73" idtaskorig="z_7i_t73" z.type="ganttz.dependency.Dependency" id="z_7i_283" class="dependency">
		<img class="start extra_padding" src="/navalplanner-webapp/zkau/web/ganttz/img/pixel.gif" style="top: -31px; left: 242px; height: 20px;width:1px;">
		<img class="mid extra_padding" src="/navalplanner-webapp/zkau/web/ganttz/img/pixel.gif" style="top: -49px; width: 18px; left: 242px;height:1px;">
		<img class="end extra_padding" src="/navalplanner-webapp/zkau/web/ganttz/img/pixel.gif" style="top: -31px; left: 242px; height: 20px;width:1px;">
		<img class="arrow extra_padding" src="/navalplanner-webapp/zkau/web/ganttz/img/arrow.png" style="top: -36px; left: 252px;border-color:red;">
	</div>


</div>

</div>