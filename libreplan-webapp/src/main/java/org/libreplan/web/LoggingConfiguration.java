/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.libreplan.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * It tries to replace ${log-directory} property for a suitable location.
 *
 * @author Oscar Gonzalez Fernandez <ogonzalez@igalia.com>
 */
public class LoggingConfiguration implements ServletContextListener {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static Pattern propertyPattern = Pattern.compile("\\$\\{\\s*(.+?)\\s*\\}");

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if ( System.getProperty("libreplan-log-directory") != null ) {
            // log4j will do the replacement automatically
            return;
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("libreplan-log-directory", findLogDirectory(sce.getServletContext()));
        try {
            StringReader newConfiguration = new StringReader(getContents(replacements));
            new DOMConfigurator().doConfigure(newConfiguration, LogManager.getLoggerRepository());
        } catch (IOException e) {
            e.printStackTrace();
            // Let log4j be loaded without replacements
        }
    }

    private String findLogDirectory(ServletContext servletContext) {
        File result = logDirectoryFile(servletContext);
        return result != null ? result.getAbsolutePath() + "/" : "";
    }

    private File logDirectoryFile(ServletContext servletContext) {
        String applicationName = firstNotEmptyOrNull(
                servletContext.getContextPath(),
                servletContext.getServletContextName(),
                "LibrePlan");

        if ( isTomcat(servletContext) ) {
            File logDirectory = findTomcatLogDirectory();
            if ( logDirectory != null ) {
                return tryToAppendApplicationName(logDirectory, applicationName);
            }
        }

        File home = new File(System.getProperty("user.home"));

        return home.canWrite() ? tryToAppendApplicationName(home, applicationName) : null;
    }

    private File findTomcatLogDirectory() {
        File file = new File("/var/log/");
        if ( !file.isDirectory() ) {
            return null;
        }

        File[] tomcatLogDirectories = file.listFiles(pathname -> pathname.getName().contains("tomcat"));

        return tomcatLogDirectories.length == 0 ? null : tomcatLogDirectories[0];
    }

    private File tryToAppendApplicationName(File logDirectory, String applicationName) {
        File forApplication = new File(logDirectory, applicationName);
        return forApplication.mkdir() || forApplication.canWrite() ? forApplication : logDirectory;
    }

    private boolean isTomcat(ServletContext servletContext) {
        String serverInfo = servletContext.getServerInfo();
        return serverInfo != null && serverInfo.contains("Tomcat");
    }

    private static String firstNotEmptyOrNull(String... strings) {
        for (String each : strings) {
            if ( each != null && !each.isEmpty() ) {
                return each;
            }
        }
        return "";
    }

    private String getContents(Map<String, String> replacements) throws IOException {
        return withReplacements(replacements, getOriginalConfiguration());
    }

    private BufferedReader getOriginalConfiguration() {
        return new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("log4j.xml")));
    }

    private String withReplacements(Map<String, String> replacements, BufferedReader originalConfiguration) throws IOException {

        StringBuilder result = new StringBuilder();
        String line;

        while ((line = originalConfiguration.readLine()) != null) {
            result.append(doReplacement(replacements, line)).append(LINE_SEPARATOR);
        }

        return result.toString();
    }

    private static String doReplacement(Map<String, String> propertyReplacements, String line) {

        String result = line;
        Matcher matcher = propertyPattern.matcher(line);

        while (matcher.find()) {
            String propertyName = matcher.group(1);
            if ( propertyReplacements.containsKey(propertyName) ) {
                result = line.replace(matcher.group(), propertyReplacements.get(propertyName));
            }
        }

        return result;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
