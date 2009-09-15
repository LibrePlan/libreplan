package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Set;

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

    public static Worker create(String firstName, String surname, String nif) {
        Worker worker = new Worker(firstName, surname, nif);
        worker.setNewObject(true);
        return worker;
    }

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String surname;

    @NotEmpty
    private String nif;

    /**
     * Constructor for hibernate. Do not use!
     */
    public Worker() {

    }

    private Worker(String firstName, String surname, String nif) {
        this.firstName = firstName;
        this.surname = surname;
        this.nif = nif;
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

    public String getName() {
        return firstName + " " + surname;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public boolean satisfiesCriterions(Set<Criterion> criterions) {
        ICriterion compositedCriterion = CriterionCompounder.buildAnd(
                new ArrayList<ICriterion>(criterions)).getResult();
        return compositedCriterion.isSatisfiedBy(this);
    }

}
