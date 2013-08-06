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
}
