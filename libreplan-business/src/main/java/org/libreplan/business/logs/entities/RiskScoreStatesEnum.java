package org.libreplan.business.logs.entities;
import static org.libreplan.business.i18n.I18nHelper._t;
/**
 * Defines ZERO, ONE, TWO, THREE, FOUR, SIX, NINE
 * to be used as data type in
 * {@link RiskLog}
 *
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public enum RiskScoreStatesEnum {
    ZERO(_t("0")), ONE(_t("1")), TWO(_t("2")), THREE(_t("3")), FOUR(_t("4")), SIX(_t("6")), NINE(_t("9")) ;

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
