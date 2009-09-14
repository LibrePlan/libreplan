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
import org.navalplanner.business.labels.daos.ILabelTypeDAO;
import org.navalplanner.business.labels.entities.LabelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Test for {@LabelTypeDAO}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Transactional
public class LabelTypeDAOTest {

    @Autowired
    ILabelTypeDAO labelTypeDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(labelTypeDAO);
    }

    @Test
    public void testSaveLabelType() {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        assertTrue(labelType.getId() != null);
    }

    @Test
    public void testRemoveLabelType() throws InstanceNotFoundException {
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        labelTypeDAO.remove(labelType.getId());
        assertFalse(labelTypeDAO.exists(labelType.getId()));
    }

    @Test
    public void testListLabelTypes() {
        int previous = labelTypeDAO.list(LabelType.class).size();
        LabelType labelType = LabelType.create(UUID.randomUUID().toString());
        labelTypeDAO.save(labelType);
        List<LabelType> list = labelTypeDAO.list(LabelType.class);
        assertEquals(previous + 1, list.size());
    }
}
