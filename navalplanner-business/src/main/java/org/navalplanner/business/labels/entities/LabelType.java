/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.labels.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;

/**
 * LabeType entity
 *
 * @author Diego Pino Garcia<dpino@igalia.com>
 *
 */
public class LabelType extends IntegrationEntity implements Comparable {

    @NotEmpty(message = "name not specified")
    private String name;

    private Set<Label> labels = new HashSet<Label>();

    // Default constructor, needed by Hibernate
    // At least package visibility, https://www.hibernate.org/116.html#A6
    protected LabelType() {

    }

    public static LabelType create(String name) {
        return create(new LabelType(name));
    }

    protected LabelType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Label> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public void addLabel(Label label) {
        Validate.notNull(label);
        labels.add(label);
    }

    public void removeLabel(Label label) {
        labels.remove(label);
    }

    @Override
    public int compareTo(Object arg0) {
        if (getName() != null) {
            return getName().compareTo(((LabelType) arg0).getName());
        }
        return -1;
    }

    @Override
    protected ILabelTypeDAO getIntegrationEntityDAO() {
        return Registry.getLabelTypeDAO();
    }
}
