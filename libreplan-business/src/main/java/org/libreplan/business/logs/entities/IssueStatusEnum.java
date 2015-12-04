package org.libreplan.business.logs.entities;
import static org.libreplan.business.i18n.I18nHelper._;
/**
 * Defines INVESTIGATING, ESCALATED, RESOLVED enums
 * to be used as data type in
 * {@link IssueLog}
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public enum IssueStatusEnum {
    INVESTIGATING(_("INVESTIGATING")), ESCALATED(_("ESCALATED")), RESOLVED(_("RESOLVED"));

    private final String issueStatusEnum;

    IssueStatusEnum(String issueStatusEnum) {
        this.issueStatusEnum = issueStatusEnum;
    }

    public  String getDisplayName() {
        return issueStatusEnum;
    }

    public static IssueStatusEnum getDefault() {
        return IssueStatusEnum.INVESTIGATING;
    }
}
