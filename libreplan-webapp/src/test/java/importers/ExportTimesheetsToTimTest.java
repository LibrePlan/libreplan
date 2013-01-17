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

package importers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.libreplan.importers.ExportTimesheetsToTim;
import org.libreplan.importers.TimSoapClient;
import org.libreplan.importers.tim.Duration;
import org.libreplan.importers.tim.Person;
import org.libreplan.importers.tim.Product;
import org.libreplan.importers.tim.RegistrationDate;
import org.libreplan.importers.tim.TimOptions;
import org.libreplan.importers.tim.TimeRegistration;
import org.libreplan.importers.tim.TimeRegistrationRequest;
import org.libreplan.importers.tim.TimeRegistrationResponse;

/**
 * Test for {@link ExportTimesheetsToTim}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class ExportTimesheetsToTimTest {

    private Properties properties = null;

    @Before
    public void loadProperties() throws FileNotFoundException, IOException {
        String filename = System.getProperty("user.dir")
                + "/../scripts/tim-connector/tim-conn.properties";
        properties = new Properties();
        properties.load(new FileInputStream(filename));
    }

    private TimeRegistration createTimeRegistration(String name,
            String productCode, LocalDate localDate, Double hours) {
        Person person = new Person();
        person.setName(name);
        person.setOptions(TimOptions.UPDATE_OR_INSERT);

        Product product = new Product();
        product.setOptions(TimOptions.UPDATE_OR_INSERT);
        product.setCode(productCode);

        RegistrationDate date = new RegistrationDate();
        date.setOptions(TimOptions.UPDATE_OR_INSERT);
        date.setDate(localDate);

        Duration duration = new Duration();
        duration.setOptions(TimOptions.DECIMAL);
        duration.setDuration(hours);

        TimeRegistration timeRegistration = new TimeRegistration();
        timeRegistration.setPerson(person);
        timeRegistration.setProduct(product);
        timeRegistration.setRegistrationDate(date);
        timeRegistration.setDuration(duration);
        return timeRegistration;
    }

    @Test
    public void testExporttTimeRegistrationWith1Item() {
        List<TimeRegistration> timeRegistrations = new ArrayList<TimeRegistration>();
        TimeRegistration timeRegistration = createTimeRegistration(
                "Baten, Jeroen", "5160", new LocalDate().minusDays(1),
                9.00);
        timeRegistrations.add(timeRegistration);
        TimeRegistrationRequest timeRegistrationRequest = new TimeRegistrationRequest();
        timeRegistrationRequest.setTimeRegistrations(timeRegistrations);

        TimeRegistrationResponse timeRegistrationResponse = TimSoapClient
                .sendRequestReceiveResponse(properties.getProperty("url"),
                        properties.getProperty("username"),
                        properties.getProperty("password"),
                        timeRegistrationRequest,
                        TimeRegistrationResponse.class);
        if (timeRegistrationResponse == null) {
            fail("Time Registration Response is null");
        }
        assertTrue(!timeRegistrationResponse.getRefs().isEmpty());
    }

    @Test
    public void testExportTimeRegistrationWith2Items() {
        List<TimeRegistration> timeRegistrations = new ArrayList<TimeRegistration>();
        TimeRegistration timeRegistration1 = createTimeRegistration(
                "Baten, Jeroen", "5160", new LocalDate(),
                8.00);
        timeRegistrations.add(timeRegistration1);

        TimeRegistration timeRegistration2 = createTimeRegistration(
                "Baten, Jeroen", "5160", new LocalDate(), 9.00);
        timeRegistrations.add(timeRegistration2);

        TimeRegistrationRequest timeRegistrationRequest = new TimeRegistrationRequest();
        timeRegistrationRequest.setTimeRegistrations(timeRegistrations);

        TimeRegistrationResponse timeRegistrationResponse = TimSoapClient
                .sendRequestReceiveResponse(properties.getProperty("url"),
                        properties.getProperty("username"),
                        properties.getProperty("password"),
                        timeRegistrationRequest, TimeRegistrationResponse.class);
        if (timeRegistrationResponse == null) {
            fail("Time Registration Response is null");
        }
        assertTrue(!timeRegistrationResponse.getRefs().isEmpty());
    }

}
