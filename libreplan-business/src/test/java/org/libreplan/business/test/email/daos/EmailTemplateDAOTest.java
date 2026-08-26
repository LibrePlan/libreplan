/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.test.email.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.email.daos.IEmailTemplateDAO;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.settings.entities.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link EmailTemplateDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 */
public class EmailTemplateDAOTest {

    @Autowired
    private IEmailTemplateDAO emailTemplateDAO;

    private EmailTemplate createValid(EmailTemplateEnum type, Language language) {
        EmailTemplate template = new EmailTemplate();
        template.setType(type);
        template.setLanguage(language);
        emailTemplateDAO.save(template);
        return template;
    }

    @Test
    @Transactional
    public void testGetAllIncludesSaved() {
        EmailTemplate template = createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, Language.ENGLISH_LANGUAGE);
        boolean found = false;
        for (EmailTemplate t : emailTemplateDAO.getAll()) {
            if (t.getId().equals(template.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindByTypeOnlyReturnsMatchingType() {
        EmailTemplate matching =
                createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, Language.ENGLISH_LANGUAGE);
        createValid(EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START, Language.ENGLISH_LANGUAGE);

        List<EmailTemplate> result = emailTemplateDAO.findByType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);

        boolean found = false;
        for (EmailTemplate t : result) {
            assertEquals(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, t.getType());
            if (t.getId().equals(matching.getId())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @Transactional
    public void testFindByTypeAndLanguageRequiresBothToMatch() {
        EmailTemplate template = createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, Language.ENGLISH_LANGUAGE);

        assertEquals(template.getId(), emailTemplateDAO.findByTypeAndLanguage(
                EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, Language.ENGLISH_LANGUAGE).getId());

        assertNull(emailTemplateDAO.findByTypeAndLanguage(
                EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, Language.SPANISH_LANGUAGE));
        assertNull(emailTemplateDAO.findByTypeAndLanguage(
                EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START, Language.ENGLISH_LANGUAGE));
    }

    @Test
    @Transactional
    public void testDeleteRemovesEntity() {
        EmailTemplate template = createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, Language.ENGLISH_LANGUAGE);
        emailTemplateDAO.delete(template);
        assertFalse(emailTemplateDAO.exists(template.getId()));
    }

    @Test
    @Transactional
    public void testDeleteDoesNotThrowWhenAlreadyRemoved() throws InstanceNotFoundException {
        EmailTemplate template = createValid(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED, Language.ENGLISH_LANGUAGE);
        emailTemplateDAO.remove(template.getId());
        emailTemplateDAO.delete(template);
    }

}
