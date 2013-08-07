package org.libreplan.business.recurring;

import static org.libreplan.business.i18n.I18nHelper._;

import org.apache.commons.lang.Validate;

public enum RecurrencePeriodicity {
    // the _ method used here it's just for marking for translation. The real
    // translation, depending on the locale of the user, would happen in the web
    // layer.
    NO_PERIODICTY(_("Not Recurrent")), DAILY(_("Daily")), WEEKLY(_("Weekly")), MONTHLY(
            _("Monthly"));

    private String label;

    private RecurrencePeriodicity(String label) {
        Validate.notEmpty(label);
        this.label = label;
    }

    public boolean isNoPeriodicity() {
        return this == NO_PERIODICTY;
    }

    public String getLabel() {
        return label;
    }

    public int limitRepetitions(int repetitions) {
        if (isNoPeriodicity()) {
            return 0;
        }
        return repetitions;
    }

    public int limitAmountOfPeriods(int amountOfPeriods) {
        if (isNoPeriodicity()) {
            return 0;
        }
        return amountOfPeriods;
    }

    public boolean isPeriodicity() {
        return this != NO_PERIODICTY;
    }
}
