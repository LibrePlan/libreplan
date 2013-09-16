package org.libreplan.business.recurring;

import static org.libreplan.business.i18n.I18nHelper._;

import org.apache.commons.lang.Validate;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDate.Property;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;

public enum RecurrencePeriodicity {
    // the _ method used here it's just for marking for translation. The real
    // translation, depending on the locale of the user, would happen in the web
    // layer.
    NO_PERIODICTY(_("Not Recurrent"), "") {
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

        @Override
        public void checkRepeatOnDay(int day) {
            if (day < 1 || day > 7) {
                throw new IllegalArgumentException("day received: " + day
                        + ". It must be within [1, 7]");
            }
        }

        @Override
        public LocalDate adjustToDay(LocalDate date, Integer repeatOnDay) {
            checkRepeatOnDay(repeatOnDay);
            return date.dayOfWeek().setCopy(repeatOnDay);
        }
    },
    MONTHLY(_("Monthly"), _("month(s)")) {
        @Override
        public ReadablePeriod buildPeriod(int amount) {
            return Months.months(amount);
        }

        @Override
        public void checkRepeatOnDay(int day) {
            if(day < 1 || day > 31){
                throw new IllegalArgumentException("day received: " + day
                        + ". It must be within [1, 31]");
            }
        }

        @Override
        public LocalDate adjustToDay(LocalDate date, Integer onDay) {
            checkRepeatOnDay(onDay);

            Property dayOfMonth = date.dayOfMonth();
            int maximumValue = dayOfMonth.getMaximumValue();
            return dayOfMonth.setCopy(Math.min(maximumValue, onDay));
        }
    };

    private final String label;

    private final String unitLabel;

    private RecurrencePeriodicity(String label, String unitLabel) {
        Validate.notEmpty(label);
        Validate.notNull(unitLabel);
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

    public void checkRepeatOnDay(int day) {
        throw new IllegalArgumentException(this
                + " doesn't support repeat on day");
    }

    public LocalDate adjustToDay(LocalDate date, Integer onDay) {
        return date;
    }
}
