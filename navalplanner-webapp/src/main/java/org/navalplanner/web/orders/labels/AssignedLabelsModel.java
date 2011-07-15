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
import java.util.List;

import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class AssignedLabelsModel<T> implements IAssignedLabelsModel<T> {

    @Autowired
    private ILabelDAO labelDAO;

    private T element;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Transactional(readOnly = true)
    public void init(T element) {
        this.element = element;
        initializeElementAndChildren(this.element);
    }

    private void initializeElementAndChildren(T element) {
        reattachLabels();
        initialize(element);
        T current = getParent(element);
        while (current != null) {
            initialize(current);
            current = getParent(current);
        }
        initializeChildren(element);
    }

    protected abstract T getParent(T element);

    private void initializeChildren(T element) {
        initialize(element);
        for (T child : getChildren(element)) {
            initializeChildren(child);
        }
    }

    protected abstract List<T> getChildren(T element);

    private void initialize(T orderElement) {
        reattach(orderElement);
        initializeLabels(getLabels(orderElement));
    }

    protected abstract List<Label> getLabels(T orderElement);

    protected abstract void reattach(T element);

    private void reattachLabels() {
        for (Label label : getLabelsOnConversation()) {
            labelDAO.reattach(label);
        }
    }

    protected abstract List<Label> getLabelsOnConversation();

    private void initializeLabels(Collection<Label> labels) {
        for (Label label : labels) {
            initializeLabel(label);
        }
    }

    public void initializeLabel(Label label) {
        label.getName();
        label.getType().getName();
    }

    @Transactional(readOnly = true)
    public List<Label> getLabels() {
        List<Label> result = new ArrayList<Label>();
        if (element != null && getLabels(element) != null) {
            reattachLabels();
            result.addAll(getLabels(element));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Label> getInheritedLabels() {
        List<Label> result = new ArrayList<Label>();
        if (element != null) {
            reattachLabels();
            T parent = getParent(element);
            while (parent != null) {
                result.addAll(getLabels(parent));
                parent = getParent(parent);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Label createLabel(final String labelName,
            final LabelType labelType) {
        Label label = createAndSaveLabelOrGetFromDatabase(labelName, labelType);
        addLabelToConversation(label);
        return label;
    }

    private Label createAndSaveLabelOrGetFromDatabase(final String labelName,
            final LabelType labelType) {
        Label label;
        try {
            label = saveLabelOnAnotherTransaction(labelName, labelType);
            label.dontPoseAsTransientObjectAnymore();
        } catch (DataIntegrityViolationException e) {
            // Label was already created by another user while editing the order
            label = labelDAO.findByNameAndType(labelName, labelType);
            forceLoad(label);
        }
        return label;
    }

    private Label saveLabelOnAnotherTransaction(final String labelName,
            final LabelType labelType) {
        return adHocTransactionService
                .runOnAnotherTransaction(new IOnTransaction<Label>() {
                    @Override
                    public Label execute() {
                        Label label = Label.create(labelName);
                        label.setType(labelType);
                        labelDAO.save(label);
                        return label;
                    }
                });
    }

    private void forceLoad(Label label) {
        label.getType().getName();
    }

    protected abstract void addLabelToConversation(Label label);

    @Transactional(readOnly = true)
    public void assignLabel(Label label) {
        reattachLabels();
        addLabelToElement(element, label);
    }

    protected abstract void addLabelToElement(T element, Label label);

    @Transactional(readOnly = true)
    public void deleteLabel(Label label) {
        reattachLabels();
        removeLabel(element, label);
    }

    protected abstract void removeLabel(T element, Label label);

    @Transactional(readOnly = true)
    public Label findLabelByNameAndType(String labelName, LabelType labelType) {
        Label label = findLabelByNameAndTypeName(labelName, labelType.getName());
        if (label != null) {
            initializeLabel(label);
        }
        return label;
    }

    /**
     * Search {@link Label} by name and type in cache of labels
     * @param labelName
     * @param labelTypeName
     * @return
     */
    private Label findLabelByNameAndTypeName(String labelName,
            String labelTypeName) {
        for (Label label : getLabelsOnConversation()) {
            if (label.getName().equals(labelName)
                    && label.getType().getName().equals(labelTypeName)) {
                return label;
            }
        }
        return null;
    }

    public boolean isAssigned(Label label) {
        for (Label each : getLabels(element)) {
            if (each.getId().equals(label.getId())) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Label> getAllLabels() {
        return getLabelsOnConversation();
    }

}
