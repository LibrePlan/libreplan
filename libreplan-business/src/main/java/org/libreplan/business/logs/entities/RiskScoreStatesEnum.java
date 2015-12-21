package org.libreplan.business.logs.entities;
import static org.libreplan.business.i18n.I18nHelper._;
/**
 * Defines ZERO, ONE, TWO, THREE, FOUR, SIX, NINE
 * to be used as data type in
 * {@link RiskLog}
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public enum RiskScoreStatesEnum {
    ZERO(_("0")), ONE(_("1")), TWO(_("2")), THREE(_("3")), FOUR(_("4")), SIX(_("6")), NINE(_("9")) ;

    private final String riskScoreStateEnum;

    RiskScoreStatesEnum(String riskScoreStateEnum) {
        this.riskScoreStateEnum = riskScoreStateEnum;
    }

    public  String getDisplayName() {
        return riskScoreStateEnum;
    }

    public static RiskScoreStatesEnum getDefault() {
        return RiskScoreStatesEnum.ZERO;
    }
}
