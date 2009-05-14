package org.navalplanner.business.resources.entities;

import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;

/**
 * This class models a worker.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class Worker extends Resource {
    @NotEmpty
    private String firstName;

    @NotEmpty
    private String surname;

    @NotEmpty
    private String nif;

    @Min(0)
    private int dailyHours;

    public Worker() {
    }

    public Worker(String firstName, String surname, String nif, int dailyHours) {
        this.firstName = firstName;
        this.surname = surname;
        this.nif = nif;
        this.dailyHours = dailyHours;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public int getDailyHours() {
        return dailyHours;
    }

    public void setDailyHours(int dailyHours) {
        this.dailyHours = dailyHours;
    }

    public int getDailyCapacity() {
        return dailyHours;
    }

}
