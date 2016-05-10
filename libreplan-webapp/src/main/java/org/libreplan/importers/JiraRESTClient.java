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

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.libreplan.importers.jira.IssueDTO;
import org.libreplan.importers.jira.SearchResultDTO;
import org.libreplan.ws.cert.NaiveTrustProvider;
import org.libreplan.ws.common.impl.Util;

/**
 * Client to interact with Jira RESTful web service.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class JiraRESTClient {


    /**
     * Path for search operation in JIRA REST API
     */
    public static final String PATH_SEARCH = "rest/api/latest/search";

    /**
     * Path for authenticate session in JIRA REST API
     */
    public static final String PATH_AUTH_SESSION = "rest/auth/latest/session";

    /**
     * Fields to include in the response of rest/api/latest/search.
     */
    private static final String FIELDS_TO_INCLUDE_IN_RESPONSE = "summary,status,timetracking,worklog";

    /**
     * Max number of issues to return(default is 50)
     */
    private static final long MAX_RESULTS = 1000;

    private static final MediaType[] mediaTypes = new MediaType[] {
            MediaType.valueOf(MediaType.APPLICATION_JSON),
            MediaType.valueOf(MediaType.APPLICATION_XML) };


    /**
     * Queries Jira for all labels
     *
     * @param url
     *            the url from where to fetch data
     * @return String with the list of labels sepparated by comma
     */
    public static String getAllLables(String url) {
        WebClient client = WebClient.create(url).accept(mediaTypes);

        return client.get(String.class);
    }

    /**
     * Query Jira for all issues with the specified query parameter
     *
     * @param url
     *            the url(end point)
     * @param username
     *            the user name
     * @param password
     *            the password
     * @param path
     *            the path segment
     * @param query
     *            the query
     * @return list of jira issues
     */
    public static List<IssueDTO> getIssues(String url, String username, String password, String path, String query) {

        WebClient client = createClient(url);

        checkAutherization(client, username, password);

        // Go to baseURI
        client.back(true);

        client.path(path);

        if ( !query.isEmpty() ) {
            client.query("jql", query);
        }

        client.query("maxResults", MAX_RESULTS);
        client.query("fields", StringUtils.deleteWhitespace(FIELDS_TO_INCLUDE_IN_RESPONSE));

        SearchResultDTO searchResult = client.get(SearchResultDTO.class);

        return searchResult.getIssues();
    }

    /**
     * Creates WebClient
     *
     * @param url
     *            the url
     * @return the created WebClient
     */
    private static WebClient createClient(String url) {

        JacksonJaxbJsonProvider jacksonJaxbJsonProvider = new JacksonJaxbJsonProvider();
        jacksonJaxbJsonProvider.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return WebClient.create(url, Collections.singletonList(jacksonJaxbJsonProvider)).accept(mediaTypes);

    }

    /**
     * Request jira for authorization check
     *
     * @param client
     *            jira client
     * @param login
     *            login name
     * @param password
     *            login password
     */
    private static void checkAutherization(WebClient client, String login, String password) {
        NaiveTrustProvider.setAlwaysTrust(true);

        client.path(PATH_AUTH_SESSION);

        Util.addAuthorizationHeader(client, login, password);
        Response response = client.get();

        if ( response.getStatus() != Status.OK.getStatusCode() ) {
            throw new RuntimeException("Authorization failed");
        }
    }

}
