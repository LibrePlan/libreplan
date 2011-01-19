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

import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public interface IAssignedLabelsModel<T> {

    public abstract void init(T element);

    public abstract void initializeLabel(Label label);

    public abstract List<Label> getLabels();

    public abstract List<Label> getInheritedLabels();

    public abstract Label createLabel(String labelName, LabelType labelType);

    public abstract void assignLabel(Label label);

    public abstract void deleteLabel(Label label);

    public abstract Label findLabelByNameAndType(String labelName,
            LabelType labelType);

    public abstract boolean isAssigned(Label label);

    public abstract List<Label> getAllLabels();

}