package org.navalplanner.web.workorders;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

/**
 * Controller for {@link WorkOrganization} view of WorkOrder entitites <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class WorkOrganizationController extends GenericForwardComposer {

    public WorkOrganizationController() {
    }

	public void move(Component self, Component dragged) {

        Treeitem elem = new Treeitem("Elemento");
        //elem.appendChild(dragged);
		self.appendChild(elem);
	}

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        comp.setVariable("controller", this, true);
    }

}