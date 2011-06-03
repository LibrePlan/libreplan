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
package org.navalplanner.web.orders.labels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.entities.Label;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class LabelsOnConversation {

    private final ILabelDAO labelDAO;

    private Set<Label> labels = null;

    public LabelsOnConversation(ILabelDAO labelDAO) {
        this.labelDAO = labelDAO;
    }

    public List<Label> getLabels() {
        loadLabelsIfDontExist();
        return new ArrayList<Label>(labels);
    }

    public void addLabel(Label label) {
        loadLabelsIfDontExist();
        Validate.notNull(label);
        labels.add(label);
    }

    private void loadLabelsIfDontExist() {
        if (this.labels != null) {
            return;
        }
        initializeLabels();
    }

    public void initializeLabels() {
        final List<Label> labels = labelDAO.getAll();
        initializeLabels(labels);
        this.labels = new HashSet<Label>(labels);
    }

    private void initializeLabels(Collection<Label> labels) {
        for (Label label : labels) {
            initializeLabel(label);
        }
    }

    private void initializeLabel(Label label) {
        label.getName();
        label.getType().getName();
    }

    public void reattachLabels() {
        loadLabelsIfDontExist();
        for (Label each : labels) {
            labelDAO.reattach(each);
        }
    }

}
