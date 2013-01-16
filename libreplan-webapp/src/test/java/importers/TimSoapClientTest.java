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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.libreplan.importers.TimSoapClient;

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

    @Test
    public void testAuthorization() {
        boolean result = TimSoapClient.checkAuthorization(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
        assertTrue(result);
    }

}
