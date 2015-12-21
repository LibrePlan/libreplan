package org.libreplan.business.logs.entities;

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Defines PROBLEM_OR_CONCERN, REQUEST_FOR_CHANGE, OFF_SPECIFICATION enums
 * to be used as data type in
 * {@link IssueLog}
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public enum IssueTypeEnum {
    PROBLEM_OR_CONCERN(_("Problem or concern")), REQUEST_FOR_CHANGE(_("Request for change")), OFF_SPECIFICATION(_("Off specification"));

    private final String issueTypeEnum;

    IssueTypeEnum(String issueTypeEnum) {
         this.issueTypeEnum = issueTypeEnum;
    }

    public  String getDisplayName() {
        return issueTypeEnum;
    }

    public static IssueTypeEnum getDefault() {
        return IssueTypeEnum.OFF_SPECIFICATION;
    }
}
