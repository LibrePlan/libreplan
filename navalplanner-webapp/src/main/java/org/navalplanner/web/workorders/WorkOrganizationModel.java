package org.navalplanner.web.workorders;

import java.util.List;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.event.TreeDataListener;

/**
 * Model for WorkOrganization <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * Pending of real implementation of methods
 */
public class WorkOrganizationModel extends SimpleTreeModel {

    public WorkOrganizationModel(List children) {
        super(new SimpleTreeNode("Root", children));
    }

}