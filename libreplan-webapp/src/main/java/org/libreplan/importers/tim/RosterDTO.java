/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.importers.tim;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * DTO representing a tim-connector Roster
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "bezettingblok")
public class RosterDTO {

    @XmlAttribute(name = "startdate", required = true)
    private LocalDate startDate;

    @XmlAttribute(name = "enddate", required = true)
    private LocalDate endDate;

    @XmlAttribute(name = "resource_planning")
    private Boolean resourcePlanning;

    @XmlAttribute(name = "day_planning")
    private Boolean dayPlanning;

    @XmlAttribute
    private Boolean calendar;

    @XmlAttribute(name = "non_planned")
    private Boolean nonPlaned;

    @XmlAttribute(name = "full_day")
    private Boolean fullDay;

    @XmlAttribute
    private Boolean concept;

    @XmlElement
    private FilterDTO filter;

    @XmlElement(name = "Persoon")
    private List<PersonDTO> persons;

    @XmlElement(name = "Roostercategorie")
    private List<RosterCategoryDTO> rosterCategories;

    @XmlElement(name = "Afdeling")
    private DepartmentDTO department;

    @XmlElement(name = "Datum", required = true, nillable = true)
    private LocalDate date;

    @XmlElement(name = "Tijd", required = true, nillable = true)
    private LocalTime time;

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name = "duur", required = true, nillable = true)
    private String duration;

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name = "aanwezigheid")
    private String precence;

    @XmlElement(name = "periode")
    private List<PeriodDTO> periods;

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name = "status")
    private String status;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getResourcePlanning() {
        return resourcePlanning;
    }

    public void setResourcePlanning(Boolean resourcePlanning) {
        this.resourcePlanning = resourcePlanning;
    }

    public Boolean getDayPlanning() {
        return dayPlanning;
    }

    public void setDayPlanning(Boolean dayPlanning) {
        this.dayPlanning = dayPlanning;
    }

    public Boolean getCalendar() {
        return calendar;
    }

    public void setCalendar(Boolean calendar) {
        this.calendar = calendar;
    }

    public Boolean getNonPlaned() {
        return nonPlaned;
    }

    public void setNonPlaned(Boolean nonPlaned) {
        this.nonPlaned = nonPlaned;
    }

    public Boolean getFullDay() {
        return fullDay;
    }

    public void setFullDay(Boolean fullDay) {
        this.fullDay = fullDay;
    }

    public Boolean getConcept() {
        return concept;
    }

    public void setConcept(Boolean concept) {
        this.concept = concept;
    }

    public FilterDTO getFilter() {
        return filter;
    }

    public void setFilter(FilterDTO filter) {
        this.filter = filter;
    }

    public List<PersonDTO> getPersons() {
        return persons;
    }

    public void setPersons(List<PersonDTO> persons) {
        this.persons = persons;
    }

    public List<RosterCategoryDTO> getRosterCategories() {
        return rosterCategories;
    }

    public void setRosterCategories(List<RosterCategoryDTO> rosterCategories) {
        this.rosterCategories = rosterCategories;
    }

    public DepartmentDTO getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDTO department) {
        this.department = department;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPrecence() {
        return precence;
    }

    public void setPrecence(String precence) {
        this.precence = precence;
    }

    public List<PeriodDTO> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PeriodDTO> periods) {
        this.periods = periods;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
