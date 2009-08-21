package org.navalplanner.web.calendars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;

/**
 * Model for the {@link BaseCalendar} tree.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarsTreeModel extends SimpleTreeModel {

    private static Map<BaseCalendar, List<BaseCalendar>> relationParentChildren = new HashMap<BaseCalendar, List<BaseCalendar>>();

    public BaseCalendarsTreeModel(BaseCalendarTreeRoot root) {
        super(createRootNodeAndDescendants(root, root.getRootCalendars(), root
                .getDerivedCalendars()));
    }

    private static SimpleTreeNode createRootNodeAndDescendants(
            BaseCalendarTreeRoot root, List<BaseCalendar> rootCalendars,
            List<BaseCalendar> derivedCalendars) {

        fillHashParentChildren(rootCalendars, derivedCalendars);

        return new SimpleTreeNode(root, asNodes(rootCalendars));
    }

    private static List<SimpleTreeNode> asNodes(List<BaseCalendar> baseCalendars) {
        if (baseCalendars == null) {
            return new ArrayList<SimpleTreeNode>();
        }

        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (BaseCalendar baseCalendar : baseCalendars) {
            result.add(asNode(baseCalendar));
        }

        return result;
    }

    private static SimpleTreeNode asNode(BaseCalendar baseCalendar) {
        List<BaseCalendar> children = relationParentChildren.get(baseCalendar);
        return new SimpleTreeNode(baseCalendar, asNodes(children));
    }

    private static void fillHashParentChildren(
            List<BaseCalendar> rootCalendars,
            List<BaseCalendar> derivedCalendars) {
        for (BaseCalendar root : rootCalendars) {
            relationParentChildren.put(root, new ArrayList<BaseCalendar>());
        }

        for (BaseCalendar derived : derivedCalendars) {
            BaseCalendar parent = derived.getParent();
            List<BaseCalendar> siblings = relationParentChildren.get(parent);

            if (siblings == null) {
                siblings = new ArrayList<BaseCalendar>();
                siblings.add(derived);
                relationParentChildren.put(parent, siblings);
            } else {
                siblings.add(derived);
            }
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node == null) {
            return true;
        }

        SimpleTreeNode simpleTreeNode = (SimpleTreeNode) node;
        BaseCalendar baseCalendar = (BaseCalendar) simpleTreeNode.getData();

        List<BaseCalendar> children = relationParentChildren.get(baseCalendar);
        if (children == null) {
            return true;
        }

        return children.isEmpty();
    }

}
