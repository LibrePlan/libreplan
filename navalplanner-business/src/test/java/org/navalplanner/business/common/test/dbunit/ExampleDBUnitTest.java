package org.navalplanner.business.common.test.dbunit;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.springframework.beans.factory.annotation.Autowired;

public class ExampleDBUnitTest extends AbstractDBUnitTest {

    @Autowired
    private IAdvanceTypeDAO advanceDAO;

    @Test
    @Ignore
    public void percentageInsertedInDB() {
        AdvanceType advance = advanceDAO.findByName("percentage");
        assertEquals("percentage",advance.getUnitName());
    }

}
