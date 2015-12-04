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
    PROBLEM_OR_CONCERN(_("PROBLEM OR CONCERN")), REQUEST_FOR_CHANGE(_("REQUEST FOR CHANGE")), OFF_SPECIFICATION(_("OFF SPECIFICATON"));

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
