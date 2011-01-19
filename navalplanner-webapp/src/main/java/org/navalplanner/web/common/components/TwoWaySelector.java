/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.common.components;

import static org.navalplanner.web.I18nHelper._;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.web.common.Util;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * ZK macro component that shows two {@link Listbox} allowing to move objects
 * between each other.
 *
 * In the {@link Listbox} on the left you will have the assigned objects and in
 * the right the possible other objects to be assigned.
 *
 * Finally it provides methods to get the current assigned and unassigned
 * objects.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class TwoWaySelector extends HtmlMacroComponent {

    /**
     * A {@link Set} of objects that are assigned (so they're shown on the left
     * {@link Listbox})
     */
    private Set assignedObjects = new HashSet();

    /**
     * Title for the left {@link Listbox} (where assigned objects are shown)
     */
    private String assignedTitle = _("Assigned");

    /**
     * A {@link Set} of objects that are not assigned (so they're shown on the
     * right {@link Listbox})
     */
    private Set unassignedObjects = new HashSet();

    /**
     * Title for the right {@link Listbox} (where unassigned objects are shown)
     */
    private String unassignedTitle = _("Unassigned");

    /**
     * A {@link List} of properties to be shown on the {@link Listbox} for each
     * object.
     */
    private List<String> columns = null;

    /**
     * {@link ListitemRenderer} that knows how to paint an object according to
     * the {@link List} stored in the columns attribute. If columns is null then
     * the object will be rendered as a string.
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     */
    private transient ListitemRenderer renderer = new ListitemRenderer() {
        @Override
        public void render(Listitem item, Object data) throws Exception {

            Class<? extends Object> klass = data.getClass();
            Map<String, PropertyDescriptor> propertiesByName = getProperties(klass);

            // If a list of attributes is defined
            if (columns != null) {
                // For each attribute
                for (String column : columns) {
                    // Call the method to get the information
                    PropertyDescriptor propertyDescriptor = propertiesByName
                            .get(column);
                    if (propertyDescriptor == null) {
                        throw new RuntimeException(
                            _("Unknown attribute '{0}' in class {1}", column, klass.getName()));
                    }

                    String label = Objects.toString(propertyDescriptor
                            .getReadMethod().invoke(data));

                    // Add a new Listcell
                    item.appendChild(new Listcell(label));
                }
            } else { // If the list of attributes is not defined
                // Render the object as string
                item.setLabel(Objects.toString(data));
            }

            item.setValue(data);
        }

        /**
         * A {@link Map} that stores the information about the attributes for a
         * class.
         *
         * The information about attributes is stored with another Map where
         * keys are the properties name and the values the
         * {@link PropertyDescriptor}.
         */
        private Map<Class<?>, Map<String, PropertyDescriptor>> propertiesMapsCached = new HashMap<Class<?>, Map<String, PropertyDescriptor>>();

        /**
         * Creates a {@link Map} that relates the properties and their
         * {@link PropertyDescriptor} from the {@link BeanInfo}.
         *
         * @param info
         *            Information about the bean
         * @return A {@link Map} that relates properties name and
         *         {@link PropertyDescriptor}
         */
        private Map<String, PropertyDescriptor> buildPropertyDescriptorsMap(
                BeanInfo info) {
            PropertyDescriptor[] propertyDescriptors = info
                    .getPropertyDescriptors();
            Map<String, PropertyDescriptor> propertiesByName = new HashMap<String, PropertyDescriptor>();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                propertiesByName.put(propertyDescriptor.getName(),
                        propertyDescriptor);
            }
            return propertiesByName;
        }

        /**
         * Gets the attributes of a {@link Class} together with the
         * {@link PropertyDescriptor} of each property.
         *
         * @param klass
         *            The {@link Class} to get the properties
         * @return A {@link Map} that relates properties name and
         *         {@link PropertyDescriptor}
         * @throws IntrospectionException
         */
        private Map<String, PropertyDescriptor> getProperties(
                Class<? extends Object> klass) throws IntrospectionException {
            // If it's already cached
            if (propertiesMapsCached.containsKey(klass)) {
                return propertiesMapsCached.get(klass);
            }

            BeanInfo beanInfo = Introspector.getBeanInfo(klass);
            Map<String, PropertyDescriptor> result = buildPropertyDescriptorsMap(beanInfo);

            // Store in cache
            propertiesMapsCached.put(klass, result);

            return result;
        }
    };

    public void setAssignedTitle(String assignedTitle) {
        if (assignedTitle != null) {
            this.assignedTitle = assignedTitle;
        }
    }

    public String getAssignedTitle() {
        return assignedTitle;
    }

    public void setUnassignedTitle(String unassignedTitle) {
        if (unassignedTitle != null) {
            this.unassignedTitle = unassignedTitle;
        }
    }

    public String getUnassignedTitle() {
        return unassignedTitle;
    }

    public void setAssignedObjects(Set assignedObjects) {
        if (assignedObjects != null) {
            this.assignedObjects = assignedObjects;
        }
    }

    public Set getAssignedObjects() {
        return assignedObjects;
    }

    public void setUnassignedObjects(Set unassignedObjects) {
        if (assignedObjects != null) {
            this.unassignedObjects = unassignedObjects;
        }
    }

    public Set getUnassignedObjects() {
        return unassignedObjects;
    }

    /**
     * Sets the list of attributes to be shown when an object is renderer.
     *
     * @param columns
     *            A comma-separated string
     */
    public void setColumns(String columns) {
        if (columns != null) {
            // Remove white spaces
            columns = columns.replaceAll("\\s", "");

            if (!columns.isEmpty()) {
                // Split the string
                this.columns = Arrays.asList(columns.split(","));
            }
        }
    }

    public List<String> getColumns() {
        return columns;
    }

    public ListitemRenderer getRenderer() {
        return renderer;
    }

    /**
     * Assign (move to the left {@link Listbox}) the selected items from the
     * right {@link Listbox}. And reload both {@link Listbox} in order to
     * relfect the changes.
     *
     * @param unassignedObjectsListbox
     *            The right {@link Listbox}
     */
    public void assign(Listbox unassignedObjectsListbox) {
        Set<Listitem> selectedItems = unassignedObjectsListbox
                .getSelectedItems();
        for (Listitem listitem : selectedItems) {
            Object value = listitem.getValue();
            unassignedObjects.remove(value);
            assignedObjects.add(value);
        }
        Util.reloadBindings(unassignedObjectsListbox.getParent());
        Util.saveBindings(this);
    }

    /**
     * Unassign (move to the rigth {@link Listbox}) the selected items from the
     * left {@link Listbox}. And reload both {@link Listbox} in order to relfect
     * the changes.
     *
     * @param assignedObjectsListbox
     *            The left {@link Listbox}
     */
    public void unassign(Listbox assignedObjectsListbox) {
        Set<Listitem> selectedItems = assignedObjectsListbox.getSelectedItems();
        for (Listitem listitem : selectedItems) {
            Object value = listitem.getValue();
            assignedObjects.remove(value);
            unassignedObjects.add(value);
        }
        Util.reloadBindings(assignedObjectsListbox.getParent());
        Util.saveBindings(this);
    }

}
