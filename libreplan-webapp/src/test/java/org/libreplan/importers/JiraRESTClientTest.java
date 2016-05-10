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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.ProcessingException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.libreplan.importers.jira.IssueDTO;

/**
 * Test for {@link JiraRESTClient }
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class JiraRESTClientTest {

    private Properties properties = null;

    @Before
    public void loadProperties() throws IOException {

        String filename = System.getProperty("user.dir") + "/../scripts/jira-connector/jira-conn.properties";

        properties = new Properties();
        properties.load(new FileInputStream(filename));

    }

    private String getJiraLabel(String label) {
        return "labels=" + label;
    }

    @Test
    @Ignore("Only working if you have a JIRA server configured")
    public void testGetAllLablesFromValidLabelUrl() {
        String labels = JiraRESTClient.getAllLables(properties.getProperty("label_url"));
        List<String> result = Arrays.asList(StringUtils.split(labels, ","));

        assertTrue(result.size() > 0);
    }

    @Test(expected = ProcessingException.class)
    public void testGetAllLablesFromInValidLabelUrl() {
        JiraRESTClient.getAllLables("");
    }

    @Test
    @Ignore("Only working if you have a JIRA server configured")
    public void testGetIssuesForValidLabelAndAuthorizedUser() {
        List<IssueDTO> issues = JiraRESTClient.getIssues(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"), JiraRESTClient.PATH_SEARCH,
                getJiraLabel(properties.getProperty("label")));

        assertTrue(issues.size() > 0);
    }

    @Test(expected = RuntimeException.class)
    public void testGetIssuesForValidLabelButUnAuthorizedUser() {
        JiraRESTClient.getIssues(
                properties.getProperty("url"), "", "",
                JiraRESTClient.PATH_SEARCH,
                getJiraLabel(properties.getProperty("label")));
    }

    @Test
    @Ignore("Only working if you have a JIRA server configured")
    public void testGetIssuesForEmptyLabel() {
        List<IssueDTO> issues = JiraRESTClient.getIssues(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"), JiraRESTClient.PATH_SEARCH, "");

        assertTrue(issues.size() > 0);
    }
}
