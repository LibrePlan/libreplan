package org.navalplanner.business.resources.entities;

import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;

/**
 * This class models a worker.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class Worker extends Resource {

    public static Worker create() {
        Worker worker = new Worker();
        worker.setNewObject(true);
        return worker;
    }

    public static Worker create(String firstName, String surname, String nif,
            int dailyHours) {
        Worker worker = new Worker(firstName, surname, nif, dailyHours);
        worker.setNewObject(true);
        return worker;
    }

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String surname;

    @NotEmpty
    private String nif;

    @Min(0)
    private int dailyHours;

    /**
     * Constructor for hibernate. Do not use!
     */
    public Worker() {

    }

    private Worker(String firstName, String surname, String nif, int dailyHours) {
        this.firstName = firstName;
        this.surname = surname;
        this.nif = nif;
        this.dailyHours = dailyHours;
    }

    @Override
    public String getDescription(){
        return getFirstName()+" "+getSurname();
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
