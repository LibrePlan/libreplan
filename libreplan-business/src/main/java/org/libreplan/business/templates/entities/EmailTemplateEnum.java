package org.libreplan.business.templates.entities;

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Available E-mail templates
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 28.09.15.
 *
 * TEMPLATE_N(_("Template N")) - for i18n
 * TEMPLATE_A("Template A") - for general use (no internationalizing)
 */
public enum EmailTemplateEnum {

    TEMPLATE_TASK_ASSIGNED_TO_RESOURCE(_("Task assigned to resource")),
    TEMPLATE_TEMPLATE_1("Test template");

    private final String templateType;

    EmailTemplateEnum(String templateType) {
        this.templateType = templateType;
    }

    public String getTemplateType() {
        return templateType;
    }
}
