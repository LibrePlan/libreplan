package org.navalplanner.business.test.workorders.services;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.OnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.test.resources.daos.CriterionSatisfactionDAOTest;
import org.navalplanner.business.workorders.entities.ActivityWork;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.business.workorders.entities.TaskWork;
import org.navalplanner.business.workorders.entities.TaskWorkContainer;
import org.navalplanner.business.workorders.entities.TaskWorkLeaf;
import org.navalplanner.business.workorders.services.IProjectWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

/**
 * Tests for {@link ProjectWork}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class ProjectWorkServiceTest {

    private static ProjectWork createValidProjectWork() {
        ProjectWork projectWork = new ProjectWork();
        projectWork.setDescription("description");
        projectWork.setCustomer("blabla");
        projectWork.setInitDate(CriterionSatisfactionDAOTest.year(2000));
        projectWork.setName("name");
        projectWork.setResponsible("responsible");
        return projectWork;
    }

    @Autowired
    private IProjectWorkService projectWorkService;

    @Test
    public void testCreation() throws ValidationException {
        ProjectWork projectWork = createValidProjectWork();
        projectWorkService.save(projectWork);
        assertTrue(projectWorkService.exists(projectWork));
    }

    @Test
    public void testListing() throws Exception {
        List<ProjectWork> list = projectWorkService.getProjectWorks();
        projectWorkService.save(createValidProjectWork());
        assertThat(projectWorkService.getProjectWorks().size(), equalTo(list
                .size() + 1));
    }

    @Test
    public void testRemove() throws Exception {
        ProjectWork projectWork = createValidProjectWork();
        projectWorkService.save(projectWork);
        assertTrue(projectWorkService.exists(projectWork));
        projectWorkService.remove(projectWork);
        assertFalse(projectWorkService.exists(projectWork));
    }

    @Test(expected = ValidationException.class)
    public void shouldSendValidationExceptionIfEndDateIsBeforeThanStartingDate()
            throws ValidationException {
        ProjectWork projectWork = createValidProjectWork();
        projectWork.setEndDate(CriterionSatisfactionDAOTest.year(0));
        projectWorkService.save(projectWork);
    }

    @Test
    public void testFind() throws Exception {
        ProjectWork projectWork = createValidProjectWork();
        projectWorkService.save(projectWork);
        assertThat(projectWorkService.find(projectWork.getId()), notNullValue());
    }

    @Test
    @NotTransactional
    public void testOrderPreserved() throws ValidationException,
            InstanceNotFoundException {
        final ProjectWork projectWork = createValidProjectWork();
        final TaskWork[] containers = new TaskWorkContainer[10];
        for (int i = 0; i < containers.length; i++) {
            containers[i] = new TaskWorkContainer();
            containers[i].setName("bla");
            projectWork.add(containers[i]);
        }
        TaskWorkContainer container = (TaskWorkContainer) containers[0];
        container.setName("container");
        final TaskWork[] tasks = new TaskWork[10];
        for (int i = 0; i < tasks.length; i++) {
            TaskWorkLeaf leaf = createValidLeaf("bla");
            tasks[i] = leaf;
            container.add(leaf);
        }
        projectWorkService.save(projectWork);
        projectWorkService.onTransaction(new OnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    ProjectWork reloaded = projectWorkService.find(projectWork
                            .getId());
                    List<TaskWork> taskWorks = reloaded.getTaskWorks();
                    for (int i = 0; i < containers.length; i++) {
                        assertThat(taskWorks.get(i).getId(),
                                equalTo(containers[i].getId()));
                    }
                    TaskWorkContainer container = (TaskWorkContainer) reloaded
                            .getTaskWorks().iterator().next();
                    List<TaskWork> children = container.getChildren();
                    for (int i = 0; i < tasks.length; i++) {
                        assertThat(children.get(i).getId(), equalTo(tasks[i]
                                .getId()));
                    }
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
        projectWorkService.remove(projectWork);
    }

    private TaskWorkLeaf createValidLeaf(String parameter) {
        TaskWorkLeaf result = new TaskWorkLeaf();
        result.setName(parameter);
        return result;
    }

    @Test
    @NotTransactional
    public void testAddingTaskWork() throws Exception {
        final ProjectWork projectWork = createValidProjectWork();
        TaskWorkContainer container = new TaskWorkContainer();
        container.setName("bla");
        TaskWorkLeaf leaf = new TaskWorkLeaf();
        leaf.setName("leaf");
        container.add(leaf);
        projectWork.add(container);
        ActivityWork activityWork = new ActivityWork();
        activityWork.setWorkingHours(3);
        leaf.addActivity(activityWork);
        projectWorkService.save(projectWork);
        projectWorkService.onTransaction(new OnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    ProjectWork reloaded = projectWorkService.find(projectWork
                            .getId());
                    assertFalse(projectWork == reloaded);
                    assertThat(reloaded.getTaskWorks().size(), equalTo(1));
                    TaskWorkContainer containerReloaded = (TaskWorkContainer) reloaded
                            .getTaskWorks().get(0);
                    assertThat(containerReloaded.getActivities().size(),
                            equalTo(0));
                    assertThat(containerReloaded.getChildren().size(),
                            equalTo(1));
                    TaskWork leaf = containerReloaded.getChildren().get(0);
                    assertThat(leaf.getActivities().size(), equalTo(1));
                    projectWorkService.remove(projectWork);
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }
}
