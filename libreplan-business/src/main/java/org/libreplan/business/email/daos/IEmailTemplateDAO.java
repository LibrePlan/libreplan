package org.libreplan.business.email.daos;

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.email.entities.EmailTemplate;

/**
 * DAO interface for the <code>EmailTemplate</code> entity.
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 29.09.15.
 */
public interface IEmailTemplateDAO extends IGenericDAO<EmailTemplate, Long>{

    String initializeContent();
    String getContentBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal);
    String getContentBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal);
}
