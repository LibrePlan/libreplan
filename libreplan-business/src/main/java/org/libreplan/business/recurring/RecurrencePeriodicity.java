package org.libreplan.business.recurring;

import static org.libreplan.business.i18n.I18nHelper._;

import org.apache.commons.lang.Validate;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;

public enum RecurrencePeriodicity {
    // the _ method used here it's just for marking for translation. The real
    // translation, depending on the locale of the user, would happen in the web
    // layer.
    NO_PERIODICTY(_("Not Recurrent"), null) {
        @Override
        public ReadablePeriod buildPeriod(int amount) {
            return Days.ZERO;
        }
    },
    DAILY(_("Daily"), _("day(s)")) {
        @Override
        public ReadablePeriod buildPeriod(int amount) {
            return Days.days(amount);
        }
    },
    WEEKLY(_("Weekly"), _("week(s)")) {
        @Override
        public ReadablePeriod buildPeriod(int amount) {
            return Weeks.weeks(amount);
        }
    },
    MONTHLY(_("Monthly"), _("month(s)")) {
        @Override
        public ReadablePeriod buildPeriod(int amount) {
            return Months.months(amount);
        }
    };

    private final String label;

    private final String unitLabel;

    private RecurrencePeriodicity(String label, String unitLabel) {
        Validate.notEmpty(label);
        this.label = label;
        this.unitLabel = unitLabel;
    }

    public boolean isNoPeriodicity() {
        return this == NO_PERIODICTY;
    }

    public String getLabel() {
        return label;
    }

    public String getUnitLabel() {
        return unitLabel;
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

    public ReadablePeriod getPeriod() {
        return buildPeriod(1);
    }

    public abstract ReadablePeriod buildPeriod(int amount);
}
