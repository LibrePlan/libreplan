/*
 * This file is part of LibrePlan
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

package org.libreplan.web.resources.machine;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.business.resources.entities.ICriterionType;
import org.libreplan.business.resources.entities.Interval;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.web.common.IntegrationEntityModel;
import org.libreplan.web.resources.worker.CriterionSatisfactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.WrongValueException;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */

@Service()
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedMachineCriterionsModel extends IntegrationEntityModel
        implements IAssignedMachineCriterionsModel {

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionDAO entitySequenceDAO;

    private List<CriterionWithItsType> criterionsWithItsTypes = new ArrayList<CriterionWithItsType>();

    private Resource resource;

    private CriterionSatisfaction currentCriterionSatisfaction;

    private Set<CriterionSatisfactionDTO> criterionSatisfactionDTOs = new HashSet<CriterionSatisfactionDTO>();

    private static List<ResourceEnum> applicableResources = new ArrayList<ResourceEnum>();

    static {
        applicableResources.add(ResourceEnum.MACHINE);
    }

    private static List<ResourceEnum> applicableWorkerResources = new ArrayList<ResourceEnum>();

    static {
        applicableWorkerResources.add(ResourceEnum.WORKER);
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForEdit(Resource resource) {
        this.resource = resource;
        if (resource != null) {
            reattachmentResource();
            initDTOs();
        }
    }

    @Override
    public void prepareForCreate(Resource resource) {
        this.resource = resource;
        criterionsWithItsTypes = new ArrayList<CriterionWithItsType>();
        criterionSatisfactionDTOs = new HashSet<CriterionSatisfactionDTO>();
    }

    @Override
    @Transactional(readOnly = true)
    public void reattachmentResource() {
        resourceDAO.reattach(resource);
        initializeCriterionSatisfactions(resource.getCriterionSatisfactions());
    }

    private void initializeCriterionSatisfactions(
            Set<CriterionSatisfaction> criterionsSatisfaction) {
        Set<CriterionType> types = new HashSet<CriterionType>();
        for (CriterionSatisfaction criterionSatisfaction : criterionsSatisfaction) {
            Criterion criterion = criterionSatisfaction.getCriterion();
            initializeCriterion(criterion);

            if (!types.contains(criterionSatisfaction.getCriterion().getType())) {
                types.add(criterionSatisfaction.getCriterion().getType());
                reattachCriterionType(criterionSatisfaction.getCriterion()
                    .getType());
            }
        }
    }

    private void initializeCriterion(Criterion criterion) {
        criterionDAO.reattachUnmodifiedEntity(criterion);
        criterion.getName();
        if (criterion.getParent() != null) {
            criterion.getParent().getName();
        }
    }

    private void reattachCriterionType(CriterionType criterionType) {
        criterionTypeDAO.reattach(criterionType);
    }

    private void initDTOs() {
        criterionSatisfactionDTOs = new HashSet<CriterionSatisfactionDTO>();
        for (CriterionSatisfaction criterionSatisfaction : resource
                .getCriterionSatisfactions()) {
            if (!criterionSatisfaction.isIsDeleted()) {
                CriterionSatisfactionDTO dto = new CriterionSatisfactionDTO(
                        criterionSatisfaction);
                criterionSatisfactionDTOs.add(dto);
            }
        }
    }

    @Override
    public List<CriterionSatisfactionDTO> getAllCriterionSatisfactions() {
        if (resource == null) {
            return new ArrayList<CriterionSatisfactionDTO>();
        }
        return allSatisfactionsDTO();
    }

    @Override
    public List<CriterionSatisfactionDTO> getFilterCriterionSatisfactions() {
        if (resource == null) {
            return new ArrayList<CriterionSatisfactionDTO>();
        }
        return filterSatisfactionsDTO();
    }

    @Override
    public void addCriterionSatisfaction() {
        CriterionSatisfactionDTO criterionSatisfactionDTO = new CriterionSatisfactionDTO();
        this.criterionSatisfactionDTOs.add(criterionSatisfactionDTO);
    }

    private List<CriterionSatisfactionDTO> allSatisfactionsDTO() {
        List<CriterionSatisfactionDTO> satisfactions = new ArrayList<CriterionSatisfactionDTO>();
        for (CriterionSatisfactionDTO criterionSatisfactionDTO : criterionSatisfactionDTOs) {
            if (!criterionSatisfactionDTO.isIsDeleted()) {
                satisfactions.add(criterionSatisfactionDTO);
            }
        }
        return satisfactions;
    }

    private List<CriterionSatisfactionDTO> filterSatisfactionsDTO() {
        List<CriterionSatisfactionDTO> satisfactions = new ArrayList<CriterionSatisfactionDTO>();
        for (CriterionSatisfactionDTO criterionSatisfactionDTO : criterionSatisfactionDTOs) {
            if ((!criterionSatisfactionDTO.isIsDeleted())
                    && (criterionSatisfactionDTO.isCurrent())) {
                satisfactions.add(criterionSatisfactionDTO);
            }
        }
        return satisfactions;
    }

    @Override
    public void remove(CriterionSatisfactionDTO criterionSatisfactionDTO) {
        if (criterionSatisfactionDTO.isNewObject()) {
            criterionSatisfactionDTOs.remove(criterionSatisfactionDTO);
        } else {
            criterionSatisfactionDTO.setIsDeleted(true);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CriterionWithItsType> getCriterionWithItsType() {
        criterionsWithItsTypes = new ArrayList<CriterionWithItsType>();
        List<CriterionType> listTypes = getCriterionTypes();
        for (CriterionType criterionType : listTypes) {
            List<Criterion> listCriterion = getDirectCriterions(criterionType);
            getCriterionWithItsType(criterionType, listCriterion);
        }
        return criterionsWithItsTypes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CriterionWithItsType> getCriterionWorkersWithItsType() {
        criterionsWithItsTypes = new ArrayList<CriterionWithItsType>();
        List<CriterionType> listTypes = getCriterionWorkersTypes();
        for (CriterionType criterionType : listTypes) {
            List<Criterion> listCriterion = getDirectCriterions(criterionType);
            getCriterionWithItsType(criterionType, listCriterion);
        }
        return criterionsWithItsTypes;
    }


    @Transactional(readOnly = true)
    private List<CriterionType> getCriterionTypes() {
        return criterionTypeDAO
                .getCriterionTypesByResources(applicableResources);
    }

    @Transactional(readOnly = true)
    private List<CriterionType> getCriterionWorkersTypes() {
        return criterionTypeDAO
                .getCriterionTypesByResources(applicableWorkerResources);
    }

    private void getCriterionWithItsType(CriterionType type,
            List<Criterion> children) {
        for (Criterion criterion : children) {
            // Create the criterion with its criterionType and its Hierarchy
            // label
            CriterionWithItsType criterionAndType = new CriterionWithItsType(
                    type, criterion);

            // Add to the list
            criterionsWithItsTypes.add(criterionAndType);
            getCriterionWithItsType(type, criterion.getSortedChildren());
        }
    }

    private static List<Criterion> getDirectCriterions(
            CriterionType criterionType) {
        List<Criterion> criterions = new ArrayList<Criterion>();
        for (Criterion criterion : criterionType.getSortCriterions()) {
            if (criterion.getParent() == null) {
                criterions.add(criterion);
            }
        }
        return criterions;
    }

    @Override
    public void setCriterionWithItsType(
            CriterionSatisfactionDTO criterionSatisfactionDTO,
            CriterionWithItsType criterionAndType) throws WrongValueException {
        criterionSatisfactionDTO.setCriterionWithItsType(criterionAndType);
    }

    @Override
    public boolean checkSameCriterionAndSameInterval(
            CriterionSatisfactionDTO satisfaction) {
        return existSameCriterionAndInterval(satisfaction);
    }

    @Override
    public boolean checkNotAllowSimultaneousCriterionsPerResource(
            CriterionSatisfactionDTO satisfaction) {
        ICriterionType<?> type = satisfaction.getCriterionWithItsType()
                .getType();
        if (type.isAllowSimultaneousCriterionsPerResource()) {
            return false;
        }
        return existSameCriterionTypeAndInterval(satisfaction);
    }

    private boolean existSameCriterionTypeAndInterval(
            CriterionSatisfactionDTO satisfaction) {
        for (CriterionSatisfactionDTO otherSatisfaction : criterionSatisfactionDTOs) {
            if ((!otherSatisfaction.equals(satisfaction))
                    && (sameCriterionType(otherSatisfaction, satisfaction))
                    && (sameInterval(otherSatisfaction, satisfaction))) {
                return true;
            }
        }
        return false;
    }

    private boolean existSameCriterionAndInterval(
            CriterionSatisfactionDTO satisfaction) {
        for (CriterionSatisfactionDTO otherSatisfaction : criterionSatisfactionDTOs) {
            if ((!otherSatisfaction.equals(satisfaction))
                    && (!otherSatisfaction.isIsDeleted())
                    && (!satisfaction.isIsDeleted())
                    && (sameCriterion(otherSatisfaction, satisfaction))
                    && (sameInterval(otherSatisfaction, satisfaction))) {
                return true;
            }
        }
        return false;
    }

    private boolean sameCriterion(CriterionSatisfactionDTO otherSatisfaction,
            CriterionSatisfactionDTO satisfaction) {
        if (otherSatisfaction.getCriterionWithItsType() == null){
            return false;
        }
        Criterion otherCriterion = otherSatisfaction.getCriterionWithItsType()
                .getCriterion();
        return otherCriterion.getId().equals(
                satisfaction.getCriterionWithItsType().getCriterion().getId());
    }

    private boolean sameCriterionType(
            CriterionSatisfactionDTO otherSatisfaction,
            CriterionSatisfactionDTO satisfaction) {
        if (otherSatisfaction.getCriterionWithItsType() == null) {
            return false;
        }
        ICriterionType<?> criterionType = otherSatisfaction
                .getCriterionWithItsType().getType();
        return criterionType.equals(satisfaction.getCriterionWithItsType()
                .getType());
    }

    private boolean sameInterval(CriterionSatisfactionDTO otherSatisfaction,
            CriterionSatisfactionDTO satisfaction) {
        if (otherSatisfaction.getStartDate() == null) {
            return false;
        }
        Interval otherInterval = otherSatisfaction.getInterval();
        return satisfaction.overlapsWith(otherInterval);
    }

    private void addNewSatisfaction(CriterionSatisfactionDTO satisfactionDTO) {
        Interval interval = satisfactionDTO.getInterval();
        CriterionSatisfaction satisfaction = resource.addSatisfaction(
                satisfactionDTO.getCriterionWithItsType(),
                interval);

        // set the autogenerated code
        currentCriterionSatisfaction = satisfaction;
        setDefaultCode();
    }

    private void removeSatisfaction(CriterionSatisfactionDTO satisfactionDTO) {
        CriterionSatisfaction satisfaction = satisfactionDTO
                .getCriterionSatisfaction();
        satisfaction.setIsDeleted(true);
    }

    private void updateSatisfaction(CriterionSatisfactionDTO satisfactionDTO) {
        CriterionSatisfaction satisfaction = satisfactionDTO
                .getCriterionSatisfaction();
        Criterion newCriterion = satisfactionDTO.getCriterionWithItsType()
                .getCriterion();
        satisfaction.setCriterion(newCriterion);
        satisfaction.setStartDate(satisfactionDTO.getStart());
        satisfaction.setEndDate(satisfactionDTO.getEnd());
    }

    @Override
    public void validate() throws ValidationException, IllegalStateException {
        validateDTOs();
    }

    @Override
    public void confirm() throws ValidationException, IllegalStateException {
        updateDTOs();
    }

    private void validateDTOs() throws ValidationException {
        for (CriterionSatisfactionDTO satisfactionDTO : getWithCriterionAssignedDTOs()) {
            Criterion criterion = satisfactionDTO.getCriterionWithItsType()
                    .getCriterion();
            if (checkSameCriterionAndSameInterval(satisfactionDTO)) {
                throw new IllegalStateException(_("The {0} can not be assigned to this resource. Its interval overlaps with other criterion", criterion.getName()));
            }
            if (checkNotAllowSimultaneousCriterionsPerResource(satisfactionDTO)) {
                throw new IllegalStateException(_("The {0} is not valid. Other value exists from the same criterion type", criterion.getName()));
            }
        }
    }

    private List<CriterionSatisfactionDTO> getWithCriterionAssignedDTOs() {
        return CriterionSatisfactionDTO
                .keepHavingCriterion(criterionSatisfactionDTOs);
    }

    private void updateDTOs() throws ValidationException, IllegalStateException {
        // Create a new list of Criterion satisfaction
        Set<CriterionSatisfaction> newList = new HashSet<CriterionSatisfaction>();
        for (CriterionSatisfactionDTO satisfactionDTO : getWithCriterionAssignedDTOs()) {
            CriterionSatisfaction satisfaction;
            if (satisfactionDTO.isNewObject()) {
                Criterion criterion = satisfactionDTO.getCriterionWithItsType()
                        .getCriterion();
                Interval interval = satisfactionDTO.getInterval();
                satisfaction = CriterionSatisfaction.create(criterion,
                        resource, interval);

                // set the autogenerated code
                currentCriterionSatisfaction = satisfaction;
                setDefaultCode();
            } else {
                satisfaction = satisfactionDTO.getCriterionSatisfaction();
                if (satisfactionDTO.isIsDeleted()) {
                    satisfaction.setIsDeleted(true);
                } else {
                    satisfaction.setStartDate(satisfactionDTO.getStart());
                    if (satisfactionDTO.getEndDate() != null) {
                        satisfaction.finish(satisfactionDTO.getEnd());
                    } else {
                        satisfaction.noFinish();
                    }
                }
            }
            newList.add(satisfaction);
        }
        resource.addSatisfactions(newList);
    }

    @Override
    protected Set<IntegrationEntity> getChildren() {
        return new HashSet<IntegrationEntity>();
    }

    @Override
    public IntegrationEntity getCurrentEntity() {
        return currentCriterionSatisfaction;
    }

    @Override
    public EntityNameEnum getEntityName() {
        return EntityNameEnum.CRITERION_SATISFACTION;
    }

}
