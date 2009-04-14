package org.navalplanner.business.test.resources.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.IResourceDao;
import org.navalplanner.business.resources.entities.ResourceGroup;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={BUSINESS_SPRING_CONFIG_FILE,
    BUSINESS_SPRING_CONFIG_TEST_FILE})
@Transactional
public class ResourceServiceTest {
    
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private IResourceDao resourceDao;
        
    @Test
    public void testAddResourceToResourceGroup() 
        throws InstanceNotFoundException {
        
        /* Two workers. One of them belongs to a resource group. */
        Worker worker1 = new Worker("worker-1", "worker-1-surname", 
            "11111111A", 8);
        Worker worker2 = new Worker("worker-2", "worker-2-surname", 
            "22222222B", 7);
        ResourceGroup resourceGroup1 = new ResourceGroup();
        resourceGroup1.addResource(worker1);
        resourceService.saveResource(resourceGroup1); // worker1 is also saved.
        resourceService.saveResource(worker2);
        
        /* A resource group. */
        ResourceGroup resourceGroup2 = new ResourceGroup();
        resourceService.saveResource(resourceGroup2);
        
        /* Add workers to resource group. */
        resourceService.addResourceToResourceGroup(worker1.getId(),
            resourceGroup2.getId());
        resourceService.addResourceToResourceGroup(worker2.getId(),
            resourceGroup2.getId());
        
        /* Check resource group. */
        ResourceGroup resourceGroup = (ResourceGroup) 
            resourceService.findResource(resourceGroup2.getId());
        
        assertEquals(2, resourceGroup.getResources().size());
        assertTrue(resourceGroup.getResources().contains(worker1));
        assertTrue(resourceGroup.getResources().contains(worker2));
        
        /* Check worker1 is no longer in group 1. */
        assertFalse(resourceGroup1.getResources().contains(worker1));
        
    }
    
    @Test
    public void testGetResourceDailyCapacity() 
        throws InstanceNotFoundException {
        
        /* Three workers. */
        Worker worker1 = new Worker("worker-1", "worker-1-surname", 
            "11111111A", 8);
        Worker worker2 = new Worker("worker-2", "worker-2-surname", 
            "22222222B", 7);
        Worker worker3 = new Worker("worker-3", "worker-3-surname", 
            "33333333C", 6);
        
        /* A group of two workers. */
        ResourceGroup resourceGroup1 = new ResourceGroup();
        Worker worker4 = new Worker("worker-4", "worker-4-surname", 
            "44444444D", 5);
        Worker worker5 = new Worker("worker-5", "worker-5-surname", 
            "55555555E", 4);
        resourceGroup1.addResource(worker4);
        resourceGroup1.addResource(worker5);
        
        /* 
         * A complex group containing the first three workers and a group with 
         * the last two workers.
         */
        ResourceGroup resourceGroup2 = new ResourceGroup();
        resourceGroup2.addResource(worker1);
        resourceGroup2.addResource(worker2);
        resourceGroup2.addResource(worker3);
        resourceGroup2.addResource(resourceGroup1);
        
        /* Calculate total daily capacity. */
        int totalDailyCapacity = 
            worker1.getDailyCapacity() + worker2.getDailyCapacity() +
            worker3.getDailyCapacity() + worker4.getDailyCapacity() +
            worker5.getDailyCapacity();
        
        /* Save the second group (and in consequence all resources). */
        resourceService.saveResource(resourceGroup2);
        
        /* Test ResourceService's getResourceDailyCapacity. */
        int resourceGroupDailyCapacity = 
            resourceService.getResourceDailyCapacity(resourceGroup2.getId());
        
        assertEquals(totalDailyCapacity, resourceGroupDailyCapacity);        
        
    }
    
    @Test
    public void testRemoveResource() throws InstanceNotFoundException {
        
        /* A group of three workers. */
        ResourceGroup resourceGroup = new ResourceGroup();
        Worker worker1 = new Worker("worker-1", "worker-2-surname", 
            "11111111A", 8);
        Worker worker2 = new Worker("worker-2", "worker-3-surname", 
            "22222222B", 6);
        Worker worker3 = new Worker("worker-3", "worker-3-surname", 
            "33333333C", 4);
        resourceGroup.addResource(worker1);
        resourceGroup.addResource(worker2);
        resourceGroup.addResource(worker3);
        resourceService.saveResource(resourceGroup);
        
        /* Remove worker 3. */
        resourceService.removeResource(worker3.getId());
        
        /* Check worker 3 does not exist. */
        assertFalse(resourceDao.exists(worker3.getId()));
        
        /* 
         * Check worker 3 is not in resource group and the other workers
         * are still in the group. 
         */
        assertFalse(resourceGroup.getResources().contains(worker3));
        assertTrue(resourceGroup.getResources().contains(worker1));
        assertTrue(resourceGroup.getResources().contains(worker2));
        
        /* Remove the group. */
        resourceService.removeResource(resourceGroup.getId());
        
        /* Check the resource group does not exist. */
        assertFalse(resourceDao.exists(resourceGroup.getId()));
        
        /* Check workers still exist. */
        assertTrue(resourceDao.exists(worker1.getId()));
        assertTrue(resourceDao.exists(worker2.getId()));
        
        /* Check workers do not belong to any resource group. */
        assertNull(worker1.getResourceGroup());
        assertNull(worker2.getResourceGroup());
        
        /* Remove workers. */
        resourceService.removeResource(worker1.getId());
        resourceService.removeResource(worker2.getId());
        
        /* Check workers do not exist. */
        assertFalse(resourceDao.exists(worker1.getId()));
        assertFalse(resourceDao.exists(worker2.getId()));

    }

}
