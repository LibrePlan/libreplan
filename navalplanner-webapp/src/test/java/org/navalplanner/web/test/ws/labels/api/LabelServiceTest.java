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

package org.navalplanner.web.test.ws.labels.api;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.ws.common.Util.mustEnd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.navalplanner.ws.common.api.ConstraintViolationDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.labels.api.ILabelService;
import org.navalplanner.ws.labels.api.LabelDTO;
import org.navalplanner.ws.labels.api.LabelTypeDTO;
import org.navalplanner.ws.labels.api.LabelTypeListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link ILabelService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class LabelServiceTest {

    @Autowired
    private ILabelService labelService;

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IAdHocTransactionService transactionService;

    private LabelType givenLabelTypeStored() {
        Label label1 = Label.create("label-name-1");
        Label label2 = Label.create("label-name-2");
        final LabelType labelType = LabelType.create("label-type-name"
                + UUID.randomUUID());

        labelType.addLabel(label1);
        labelType.addLabel(label2);

        labelTypeDAO.save(labelType);
        labelTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(labelType);
        labelType.dontPoseAsTransientObjectAnymore();

        return labelType;
    }

    @Test
    public void exportLabelTypes() {
        int previous = labelTypeDAO.getAll().size();
        LabelTypeListDTO labelTypes = labelService.getLabelTypes();
        assertTrue(labelTypes.labelTypes.size() == previous);
    }

    @Test
    public void exportLabelTypes2() {
        int previous = labelTypeDAO.getAll().size();

        LabelType labelType = givenLabelTypeStored();

        LabelTypeListDTO labelTypes = labelService.getLabelTypes();
        assertThat(labelTypes.labelTypes.size(), equalTo(previous + 1));

        for (LabelTypeDTO typeDTO : labelTypes.labelTypes) {
            if ((typeDTO.code.equalsIgnoreCase(labelType.getCode()))
                    && (typeDTO.labels.size() == 2)) {
                return;
            }
        }
        fail();
    }

    @Test
    public void importInvalidLabelWithoutAttributes() {
        int previous = labelTypeDAO.getAll().size();

        LabelTypeDTO labelTypeDTO = new LabelTypeDTO();

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = labelService
                .addLabelTypes(new LabelTypeListDTO(Arrays.asList(labelTypeDTO))).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        List<ConstraintViolationDTO> constraintViolations = instanceConstraintViolationsList
                .get(0).constraintViolations;
        // Mandatory fields: code, name
        assertThat(constraintViolations.size(), equalTo(2));
        for (ConstraintViolationDTO constraintViolationDTO : constraintViolations) {
            assertThat(constraintViolationDTO.fieldName, anyOf(mustEnd("code"),
                    mustEnd("name")));
        }

        assertThat(labelTypeDAO.getAll().size(), equalTo(previous));
    }

    @Test
    @NotTransactional
    public void importValidLabelType() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });

        LabelTypeDTO labelTypeDTO = new LabelTypeDTO("label-type-name"
                + UUID.randomUUID().toString(), new ArrayList<LabelDTO>());

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = labelService
                .addLabelTypes(new LabelTypeListDTO(Arrays.asList(labelTypeDTO))).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        int current = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });
        assertThat(current, equalTo(previous + 1));

    }

    @Test
    @NotTransactional
    public void importTwoValidLabelType() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });

        String nameType1 = "label-type-" + UUID.randomUUID().toString();
        String nameType2 = "label-type-" + UUID.randomUUID().toString();

        LabelTypeDTO labelTypeDTO1 = new LabelTypeDTO(nameType1,
                new ArrayList<LabelDTO>());
        LabelTypeDTO labelTypeDTO2 = new LabelTypeDTO(nameType2,
                new ArrayList<LabelDTO>());

        LabelTypeListDTO labelTypeDTOs = createLabelTypeListDTO(labelTypeDTO1,
                labelTypeDTO2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = labelService
                .addLabelTypes(labelTypeDTOs).instanceConstraintViolationsList;

        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        List<LabelType> labelTypes = transactionService
                .runOnTransaction(new IOnTransaction<List<LabelType>>() {
                    @Override
                    public List<LabelType> execute() {
                        return labelTypeDAO.getAll();
                    }
                });

        assertThat(labelTypes.size(), equalTo(previous + 2));

        int cont = 0;
        for (LabelType labelType : labelTypes) {
            if (labelType.getName().equals(nameType1)) {
                cont++;
            }
        }
        assertThat(cont, equalTo(1));

        cont = 0;
        for (LabelType labelType : labelTypes) {
            if (labelType.getName().equals(nameType2)) {
                cont++;
            }
        }
        assertThat(cont, equalTo(1));
    }

    @Test
    @NotTransactional
    public void importTwoLabelTypeWithRepeatedName() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });

        String labelTypeName = "label-type-nameX";
        LabelTypeDTO labelTypeDTO1 = new LabelTypeDTO(labelTypeName,
                new ArrayList<LabelDTO>());
        LabelTypeDTO labelTypeDTO2 = new LabelTypeDTO(labelTypeName,
                new ArrayList<LabelDTO>());

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = labelService
                .addLabelTypes(new LabelTypeListDTO(Arrays.asList(
                        labelTypeDTO1, labelTypeDTO2))).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));
        assertThat(instanceConstraintViolationsList.get(0).numItem,
                equalTo(new Long(2)));

        int current = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });
        // Just the first label type was stored
        assertThat(current, equalTo(previous + 1));
    }

    @Test
    @NotTransactional
    public void importValidLabelTypeWithTwoValidLabels() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });

        LabelDTO labelDTO1 = new LabelDTO("label-name-1");
        LabelDTO labelDTO2 = new LabelDTO("label-name-2");
        List<LabelDTO> labelDTOs = Arrays.asList(labelDTO1, labelDTO2);

        LabelTypeDTO labelTypeDTO = new LabelTypeDTO("label-type-nameY",
                labelDTOs);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = labelService
                .addLabelTypes(new LabelTypeListDTO(Arrays.asList(labelTypeDTO))).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(0));

        List<LabelType> labelTypes = transactionService
                .runOnTransaction(new IOnTransaction<List<LabelType>>() {
                    @Override
                    public List<LabelType> execute() {
                        List<LabelType> labelTypes = labelTypeDAO.getAll();
                        for (LabelType labelType : labelTypes) {
                            labelType.getLabels().size();
                        }
                        return labelTypes;
                    }
                });

        LabelType labelType = labelTypes.get(previous);
        assertThat(labelType.getName(), equalTo(labelTypeDTO.name));
        assertThat(labelType.getLabels().size(), equalTo(2));
        for (Label label : labelType.getLabels()) {
            assertThat(label.getName(), anyOf(equalTo(labelDTO1.name),
                    equalTo(labelDTO2.name)));
        }
    }

    @Test
    @NotTransactional
    public void importLabelTypeWithNameAlreadyOnDatabase() {
        int previous = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });

        String name = transactionService
                .runOnAnotherTransaction(new IOnTransaction<String>() {

                    @Override
                    public String execute() {
                        return givenLabelTypeStored().getName();
                    }

                });
        int current = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });
        assertThat(current, equalTo(previous + 1));

        LabelTypeDTO labelTypeDTO = new LabelTypeDTO(name,
                new ArrayList<LabelDTO>());

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = labelService
                .addLabelTypes(new LabelTypeListDTO(Arrays.asList(labelTypeDTO))).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        current = transactionService
                .runOnTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return labelTypeDAO.getAll().size();
                    }
                });
        assertThat(current, equalTo(previous + 1));
    }

    @Test
    public void importValidLabelTypeWithTwoLabelsWithTheSameName() {
        int previous = labelTypeDAO.getAll().size();

        String name = "label-name";
        LabelDTO labelDTO1 = new LabelDTO(name);
        LabelDTO labelDTO2 = new LabelDTO(name);
        List<LabelDTO> labelDTOs = Arrays.asList(labelDTO1, labelDTO2);

        LabelTypeDTO labelTypeDTO = new LabelTypeDTO("label-type-name",
                labelDTOs);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = labelService
                .addLabelTypes(new LabelTypeListDTO(Arrays.asList(labelTypeDTO))).instanceConstraintViolationsList;
        assertThat(instanceConstraintViolationsList.size(), equalTo(1));

        assertThat(labelTypeDAO.getAll().size(), equalTo(previous));
    }

    private LabelTypeListDTO createLabelTypeListDTO(LabelTypeDTO... labelTypes) {
        List<LabelTypeDTO> labelTypeList = new ArrayList<LabelTypeDTO>();
        for (LabelTypeDTO c : labelTypes) {
            labelTypeList.add(c);
        }
        return new LabelTypeListDTO(labelTypeList);
    }

}
