package org.libreplan.business.recurring;

public enum RecurrencePeriodicity {

    NO_PERIODICTY(0), DAILY(1), WEEKLY(7), MONTHLY(30);

    int numberOfDays;

    private RecurrencePeriodicity(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public int getNumerOfDays() {
        return numberOfDays;
    }

    public boolean isNoPeriodicity() {
        return this == NO_PERIODICTY;
    }

    public int limitRepetitions(int repetitions) {
        if (isNoPeriodicity()) {
            return 0;
        }
        return repetitions;
    }

    public boolean isPeriodicity() {
        return this != NO_PERIODICTY;
    }
}
