/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.reports;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;

import org.apache.commons.lang3.StringUtils;
import org.libreplan.business.common.Registry;
import org.springframework.web.context.ContextLoaderListener;
import org.zkoss.util.Locales;
import org.zkoss.zk.au.out.AuDownload;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.A;
import org.zkoss.zul.Hbox;

import com.libreplan.java.zk.components.JasperreportComponent;

/**
 *
 * Handles the basic behaviour of a Controller for showing reports.
 *
 * All reports consists of several input components and a show button which
 * retrieves the necessary data to build resulting report.
 * The method showReport takes care of this behaviour.
 * In addition, when a new report is shown, a link to the report shows up as well.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public abstract class LibrePlanReportController extends GenericForwardComposer<Component> {

    private static final String HTML = "html";

    protected ComboboxOutputFormat outputFormat;

    protected Hbox URItext;

    protected A URIlink;

    public void showReport(JasperreportComponent jasperreport) {
        final String type = outputFormat.getOutputFormat();

        jasperreport.setSrc(getReportName());
        jasperreport.setDatasource(getDataSource());
        jasperreport.setParameters(getParameters());
        jasperreport.setType(type);

        if ( type.equals(HTML) ) {
            URItext.setStyle("display: none");
            Executions.getCurrent().sendRedirect(jasperreport.getReportUrl(), "_blank");
        } else {
            /*
             * We cant use FileDownload.save(<url>) as it creates a new url
             * where the resource can't be find so we have to create ourselves the download request
             */
            Executions.getCurrent().addAuResponse(new AuDownload(jasperreport.getReportUrl()));
            URItext.setStyle("display: inline");
            URIlink.setHref(jasperreport.getReportUrl());
        }
    }

    protected Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("logo", getLogoLocation());
        parameters.put(JRParameter.REPORT_LOCALE, Locales.getCurrent());

        return parameters;
    }

    /**
     * Actually, this complex code needed because I decided to use isLazy="true" for images on report page.
     * If you will notice any issues with this code, refactor it to more simple code.
     */
    private String getLogoLocation() {
        String companyLogo = Registry
                .getConfigurationDAO()
                .getConfigurationWithReadOnlyTransaction()
                .getCompanyLogoURL();

        if ( outputFormat.getOutputFormat().equals(HTML) ) {

            /* If we need to send image to HTML page, we should use web application namespace */
            if ( StringUtils.isBlank(companyLogo) ) {
                companyLogo = "/common/img/logo.png";

            } else {
                /* In case when context of web server will be empty or libreplan-webapp or libreplan, or etc... */
                String[] url = Executions.getCurrent().getSession().getWebApp().getUpdateURI().split("/");

                /* In case when string.split("/") returns string array with size 3 instead of 2  */
                if ( url.length == 3 && "".equals(url[0]) && !"".equals(url[1]) ) {
                    url[0] = url[1];
                }

                if ( "".equals(url[0]) ) {
                    companyLogo = "/" + Registry
                            .getConfigurationDAO()
                            .getConfigurationWithReadOnlyTransaction()
                            .getCompanyLogoURL();
                } else {
                    companyLogo = "/" + url[0] + "/" + Registry
                            .getConfigurationDAO()
                            .getConfigurationWithReadOnlyTransaction()
                            .getCompanyLogoURL();
                }
            }
        } else {
            /*
             * If we need PDF or ODT, we should use namespace of hard drive,
             * because later it is going to be used with new File(companyLogo)
             */
            if ( StringUtils.isBlank(companyLogo) ) {
                try {
                    companyLogo = ContextLoaderListener
                            .getCurrentWebApplicationContext()
                            .getResource("\\common\\img\\logo.png")
                            .getFile()
                            .getPath();

                } catch (IOException ignored) {

                }
            } else {
                try {
                    companyLogo = ContextLoaderListener
                            .getCurrentWebApplicationContext()
                            .getResource("\\" + companyLogo)
                            .getFile()
                            .getPath();

                } catch (IOException ignored) {

                }
            }

        }

        return companyLogo;
    }

    protected abstract JRDataSource getDataSource();

    protected abstract String getReportName();
}
