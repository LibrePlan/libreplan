package org.navalplanner.web.common;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Div;

/**
 * Controller for customMenu <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class CustomMenuController extends Div {

	private List<CustomMenuItem> firstLevel;

	public class CustomMenuItem {

		private final String name;
		private final String url;
		private final List<CustomMenuItem> children;

		public String getName() {
			return name;
		}

		public String getUrl() {
			return url;
		}

		public List<CustomMenuItem> getChildren() {
			return children;
		}

		public CustomMenuItem(String name, String url) {
			this.name = name;
			this.url = url;
			this.children = new ArrayList<CustomMenuItem>();
		}

		public CustomMenuItem(String name, String url,
				List<CustomMenuItem> children) {
			this.name = name;
			this.url = url;
			this.children = children;
		}

		public void appendChildren(CustomMenuItem newChildren) {
			this.children.add(newChildren);
		}

		public boolean getActiveParent() {
		    String requestPath = Executions.getCurrent().getDesktop()
            .getRequestPath();
            if (requestPath.contains(url) || url.contains(requestPath)) {
                return true;
            }
            return false;
		}

	}

	public CustomMenuController() {
		initializeMenu();
	}

	public void initializeMenu() {
		List<CustomMenuItem> l = new ArrayList<CustomMenuItem>();
		CustomMenuItem ci;

		ci = new CustomMenuItem("mainmenu.plannification",
				"/navalplanner-webapp/planner/main.zul");
		ci.appendChildren(new CustomMenuItem("mainmenu.plannification",
				"/navalplanner-webapp/planner/main.zul"));
		ci.appendChildren(new CustomMenuItem("mainmenu.company_overview",
				"/navalplanner-webapp/planner/main.zul"));
		ci.appendChildren(new CustomMenuItem("mainmenu.plannifications_list",
		"/navalplanner-webapp/planner/main.zul"));
		l.add(ci);

		ci = new CustomMenuItem("mainmenu.resources",
				"/navalplanner-webapp/resources/worker/worker.zul");
		ci.appendChildren(new CustomMenuItem("mainmenu.list_workers",
				"/navalplanner-webapp/resources/worker/worker.zul#list"));
		ci.appendChildren(new CustomMenuItem("mainmenu.manage_criterions",
				"/navalplanner-webapp/resources/criterions/criterions.zul"));
		l.add(ci);

		ci = new CustomMenuItem("mainmenu.orders",
				"/navalplanner-webapp/orders/orders.zul");
		ci.appendChildren(new CustomMenuItem("mainmenu.list_orders",
				"/navalplanner-webapp/orders/orders.zul"));
		ci.appendChildren(new CustomMenuItem("mainmenu.activity_work_types",
				"/navalplanner-webapp/orders/orders.zul"));
		ci.appendChildren(new CustomMenuItem("mainmenu.models",
				"/navalplanner-webapp/orders/orders.zul"));
		l.add(ci);

		ci = new CustomMenuItem("mainmenu.work_reports",
				"/navalplanner-webapp/workreports/workReportTypes.zul");
		ci.appendChildren(new CustomMenuItem("mainmenu.work_report_types",
		        "navalplanner-webapp/workreports/workReportTypes.zul"));
		ci.appendChildren(new CustomMenuItem("mainmenu.work_report_list",
				"/navalplanner-webapp/workreports/workReport.zul#list"));
		l.add(ci);

		ci = new CustomMenuItem("mainmenu.quality_management",
		"/navalplanner-webapp/");
		l.add(ci);

		ci = new CustomMenuItem("mainmenu.administration",
				"/navalplanner-webapp/");
		l.add(ci);

		this.firstLevel = l;
	}

	public List<CustomMenuItem> getCustomMenuItems() {
		return this.firstLevel;
	}

	public List<CustomMenuItem> getCustomMenuSecondaryItems() {
		String requestPath = Executions.getCurrent().getDesktop()
				.getRequestPath();
		for (CustomMenuItem ci : this.firstLevel) {
			if (requestPath.contains(ci.url) || ci.url.contains(requestPath)) {
				return ci.getChildren();
			}
		}
		return this.firstLevel.get(0).getChildren();
	}

}
