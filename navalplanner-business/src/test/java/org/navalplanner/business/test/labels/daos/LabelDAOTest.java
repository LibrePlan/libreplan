package org.navalplanner.business.test.labels.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.labels.entities.LabelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@LabelDAO}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Transactional
public class LabelDAOTest {

    @Autowired
    ILabelDAO labelDAO;

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(labelDAO);
    }

    private Label createValidLabel() {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        Label label = Label.create(UUID.randomUUID().toString());
        label.setType(labelType);
        return label;
    }

    @Test
    public void testSaveLabel() {
        Label label = createValidLabel();
        labelDAO.save(label);
        assertTrue(label.getId() != null);
    }

    @Test
    public void testRemoveLabel() throws InstanceNotFoundException {
        Label label = createValidLabel();
        labelDAO.save(label);
        labelDAO.remove(label.getId());
        assertFalse(labelDAO.exists(label.getId()));
    }

    @Test
    public void testListLabels() {
        int previous = labelDAO.list(Label.class).size();
        Label label = createValidLabel();
        labelDAO.save(label);
        List<Label> list = labelDAO.list(Label.class);
        assertEquals(previous + 1, list.size());
    }
}
