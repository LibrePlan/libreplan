/*
 * This file is part of NavalPlan
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
package org.navalplanner.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
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

    private static final String lineSeparator = System
            .getProperty("line.separator");

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (System.getProperty("navalplan-log-directory") != null) {
            // log4j will do the replacement automatically.
            return;
        }

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("navalplan-log-directory",
                findLogDirectory(sce.getServletContext()));
        try {
            StringReader newConfiguration = new StringReader(
                    getContents(replacements));
            new DOMConfigurator().doConfigure(newConfiguration,
                    LogManager.getLoggerRepository());
        } catch (IOException e) {
            e.printStackTrace();
            // let log4j be loaded without replacements
        }
    }

    private String findLogDirectory(ServletContext servletContext) {
        File result = logDirectoryFile(servletContext);
        if (result != null) {
            return result.getAbsolutePath() + "/";
        }
        return "";
    }

    private File logDirectoryFile(ServletContext servletContext) {
        String applicationName = firstNotEmptyOrNull(
                servletContext.getContextPath(),
                servletContext.getServletContextName(), "LibrePlan");
        if (isTomcat(servletContext)) {
            File logDirectory = findTomcatLogDirectory();
            if (logDirectory != null) {
                return tryToAppendApplicationName(logDirectory, applicationName);
            }
        }

        File home = new File(System.getProperty("user.home"));
        if (home.canWrite()) {
            return tryToAppendApplicationName(home, applicationName);
        }

        return null;
    }

    private File findTomcatLogDirectory() {
        File file = new File("/var/log/");
        if (!file.isDirectory()) {
            return null;
        }
        File[] tomcatLogDirectories = file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().contains("tomcat");
            }
        });
        if (tomcatLogDirectories.length == 0) {
            return null;
        }
        return tomcatLogDirectories[0];
    }

    private File tryToAppendApplicationName(File logDirectory,
            String applicationName) {
        File forApplication = new File(logDirectory, applicationName);
        if (forApplication.mkdir() || forApplication.canWrite()) {
            return forApplication;
        }
        return logDirectory;
    }

    private boolean isTomcat(ServletContext servletContext) {
        String serverInfo = servletContext.getServerInfo();
        return serverInfo != null && serverInfo.contains("Tomcat");
    }

    private static String firstNotEmptyOrNull(String... strings) {
        for (String each : strings) {
            if (each != null && !each.isEmpty()) {
                return each;
            }
        }
        return "";
    }

    private String getContents(Map<String, String> replacements)
            throws IOException {
        return withReplacements(replacements, getOriginalConfiguration());
    }

    private BufferedReader getOriginalConfiguration() {
        return new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("log4j.xml")));
    }

    private String withReplacements(Map<String, String> replacements,
            BufferedReader originalConfiguration)
            throws IOException {
        StringBuilder result = new StringBuilder();
        String line = null;
        while ((line = originalConfiguration.readLine()) != null) {
            result.append(doReplacement(replacements, line)).append(
                    lineSeparator);
        }
        return result.toString();
    }

    private static Pattern propertyPattern = Pattern
            .compile("\\$\\{\\s*(.+?)\\s*\\}");

    private static String doReplacement(
            Map<String, String> propertyReplacements, String line) {

        String result = line;
        Matcher matcher = propertyPattern.matcher(line);
        while (matcher.find()) {
            String propertyName = matcher.group(1);
            if (propertyReplacements.containsKey(propertyName)) {
                result = line.replace(matcher.group(),
                        propertyReplacements.get(propertyName));
            }
        }
        return result;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
