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

package org.libreplan.importers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.libreplan.importers.tim.DataDTO;
import org.libreplan.importers.tim.DepartmentDTO;
import org.libreplan.importers.tim.DurationDTO;
import org.libreplan.importers.tim.FilterDTO;
import org.libreplan.importers.tim.PeriodDTO;
import org.libreplan.importers.tim.PersonDTO;
import org.libreplan.importers.tim.ProductDTO;
import org.libreplan.importers.tim.RegistrationDateDTO;
import org.libreplan.importers.tim.RosterCategoryDTO;
import org.libreplan.importers.tim.RosterDTO;
import org.libreplan.importers.tim.RosterRequestDTO;
import org.libreplan.importers.tim.RosterResponseDTO;
import org.libreplan.importers.tim.TimOptions;
import org.libreplan.importers.tim.TimeRegistrationDTO;
import org.libreplan.importers.tim.TimeRegistrationRequestDTO;
import org.libreplan.importers.tim.TimeRegistrationResponseDTO;

/**
 * Test for {@link TimSoapClient}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class TimSoapClientTest {

    private Properties properties = null;

    @Before
    public void loadProperties() throws FileNotFoundException, IOException {
        String filename = System.getProperty("user.dir")
                + "/../scripts/tim-connector/tim-conn.properties";
        properties = new Properties();
        properties.load(new FileInputStream(filename));
    }

    private TimeRegistrationDTO createTimeRegistration(String name,
            String productCode, LocalDate localDate, Double hours) {
        PersonDTO personDTO = new PersonDTO();
        personDTO.setName(name);
        personDTO.setOptions(TimOptions.UPDATE_OR_INSERT);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setOptions(TimOptions.UPDATE_OR_INSERT);
        productDTO.setCode(productCode);

        RegistrationDateDTO dateDTO = new RegistrationDateDTO();
        dateDTO.setOptions(TimOptions.UPDATE_OR_INSERT);
        dateDTO.setDate(localDate);

        DurationDTO durationDTO = new DurationDTO();
        durationDTO.setOptions(TimOptions.DECIMAL);
        durationDTO.setDuration(hours);

        TimeRegistrationDTO timeRegistrationDTO = new TimeRegistrationDTO();
        timeRegistrationDTO.setPerson(personDTO);
        timeRegistrationDTO.setProduct(productDTO);
        timeRegistrationDTO.setRegistrationDate(dateDTO);
        timeRegistrationDTO.setDuration(durationDTO);
        return timeRegistrationDTO;
    }

    private RosterRequestDTO createRosterRequest() {
        RosterDTO rosterDTO = new RosterDTO();
        rosterDTO.setStartDate(new LocalDate());
        rosterDTO.setEndDate(new LocalDate().plusDays(7));
        rosterDTO.setResourcePlanning(false);
        rosterDTO.setDayPlanning(false);
        rosterDTO.setCalendar(false);
        rosterDTO.setNonPlaned(true);
        rosterDTO.setFullDay(false);
        rosterDTO.setConcept(false);

        PeriodDTO periodeDTO = new PeriodDTO();
        periodeDTO.setStart(new org.joda.time.DateTime());
        periodeDTO.setEnd(new org.joda.time.DateTime().plusDays(7));
        List<PeriodDTO> periods = new ArrayList<PeriodDTO>();
        periods.add(periodeDTO);

        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setRef("4");

        RosterCategoryDTO rosterCategoryDTO = new RosterCategoryDTO();
        rosterCategoryDTO.setName(new String());
        List<RosterCategoryDTO> rosterCategories = new ArrayList<RosterCategoryDTO>();
        rosterCategories.add(rosterCategoryDTO);

        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setPeriods(periods);
        filterDTO.setDepartment(departmentDTO);

        rosterDTO.setFilter(filterDTO);

        List<PersonDTO> personDTOs = new ArrayList<PersonDTO>();
        personDTOs.add(new PersonDTO());
        rosterDTO.setPersons(personDTOs);

        rosterDTO.setRosterCategories(rosterCategories);

        rosterDTO.setDepartment(departmentDTO);

        rosterDTO.setPrecence(new String());

        rosterDTO.setPeriods(periods);

        RosterRequestDTO exportRosterRequestDTO = new RosterRequestDTO();
        DataDTO<RosterDTO> data = new DataDTO<RosterDTO>();
        data.setData(rosterDTO);

        exportRosterRequestDTO.setData(data);
        return exportRosterRequestDTO;

    }

    @Test
    @Ignore("Only working if you have a Tim server configured")
    public void testValidAuthorization() {
        boolean result = TimSoapClient.checkAuthorization(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
        assertTrue(result);
    }

    @Test
    public void testInvalidAuthorization() {
        boolean result = TimSoapClient.checkAuthorization(
                properties.getProperty("url"),
                properties.getProperty("username"), properties.getProperty(""));
        assertTrue(!result);
    }

    @Test
    @Ignore("Only working if you have a Tim server configured")
    public void testImportRostersFromFile() {
        String filename = System.getProperty("user.dir")
                + "/../scripts/tim_test/rosterResponse.xml";
        File file = new File(filename);
        RosterResponseDTO rosterResponseDTO = TimSoapClient
                .unmarshalRosterFromFile(file);
        if (rosterResponseDTO == null) {
            fail("Roster Response is null");
        }
        assertTrue(rosterResponseDTO.getRosters().size() > 0);
    }

    @Test
    @Ignore("Only working if you have a Tim server configured")
    public void testImportRostersFromServer() {
        RosterResponseDTO rosterResponseDTO = TimSoapClient
                .sendRequestReceiveResponse(properties.getProperty("url"),
                        properties.getProperty("username"),
                        properties.getProperty("password"),
                        createRosterRequest(), RosterResponseDTO.class);
        if (rosterResponseDTO == null) {
            fail("Roster Response is null");
        }
        assertTrue(rosterResponseDTO.getRosters().size() > 0);
    }

    @Test
    @Ignore("Only working if you have a Tim server configured")
    public void testExportTimeRegistrationWith1Item() {
        List<TimeRegistrationDTO> timeRegistrations = new ArrayList<TimeRegistrationDTO>();
        TimeRegistrationDTO timeRegistration = createTimeRegistration(
                "Baten, Jeroen", "5160", new LocalDate().minusDays(1), 9.00);
        timeRegistrations.add(timeRegistration);
        TimeRegistrationRequestDTO timeRegistrationRequestDTO = new TimeRegistrationRequestDTO();
        timeRegistrationRequestDTO.setTimeRegistrations(timeRegistrations);

        TimeRegistrationResponseDTO timeRegistrationResponse = TimSoapClient
                .sendRequestReceiveResponse(properties.getProperty("url"),
                        properties.getProperty("username"),
                        properties.getProperty("password"),
                        timeRegistrationRequestDTO,
                        TimeRegistrationResponseDTO.class);
        if (timeRegistrationResponse == null) {
            fail("Time Registration Response is null");
        }
        assertTrue(!timeRegistrationResponse.getRefs().isEmpty());
    }

    @Test
    @Ignore("Only working if you have a Tim server configured")
    public void testExportTimeRegistrationWith2Items() {
        List<TimeRegistrationDTO> timeRegistrationDTOs = new ArrayList<TimeRegistrationDTO>();
        TimeRegistrationDTO timeRegistrationDTO1 = createTimeRegistration(
                "Baten, Jeroen", "5160", new LocalDate(), 8.00);
        timeRegistrationDTOs.add(timeRegistrationDTO1);

        TimeRegistrationDTO timeRegistrationDTO2 = createTimeRegistration(
                "Baten, Jeroen", "5160", new LocalDate(), 9.00);
        timeRegistrationDTOs.add(timeRegistrationDTO2);

        TimeRegistrationRequestDTO timeRegistrationRequest = new TimeRegistrationRequestDTO();
        timeRegistrationRequest.setTimeRegistrations(timeRegistrationDTOs);

        TimeRegistrationResponseDTO timeRegistrationResponse = TimSoapClient
                .sendRequestReceiveResponse(properties.getProperty("url"),
                        properties.getProperty("username"),
                        properties.getProperty("password"),
                        timeRegistrationRequest,
                        TimeRegistrationResponseDTO.class);
        if (timeRegistrationResponse == null) {
            fail("Time Registration Response is null");
        }
        assertTrue(!timeRegistrationResponse.getRefs().isEmpty());
    }

}
