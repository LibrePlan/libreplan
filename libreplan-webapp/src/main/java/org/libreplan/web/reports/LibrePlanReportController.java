/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.common.Registry;
import org.zkoss.util.Locales;
import org.zkoss.zk.au.out.AuDownload;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.A;
import org.zkoss.zul.Hbox;

import com.igalia.java.zk.components.JasperreportComponent;

/**
 *
 * Handles the basic behaviour of a Controller for showing reports
 *
 * All reports consists of several input components and a show button which retrieves the necessary data to
 * build resulting report. The method showReport takes care of this behaviour. In addition, when a new report
 * is shown, a link to the report shows up as well.
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public abstract class LibrePlanReportController extends GenericForwardComposer {

    private static final String HTML = "html";

    protected ComboboxOutputFormat outputFormat;

    protected Hbox URItext;

    protected A URIlink;

    private static Set<String> supportedLanguages = new HashSet<String>() {{
        add("en");
        add("es");
        add("gl");
        add("it");
    }};

    private final String DEFAULT_LANG = "en";

    public void showReport(JasperreportComponent jasperreport){
        final String type = outputFormat.getOutputFormat();

//        LibrePlanReport report = new LibrePlanReport(jasperreport,
//                getReportName());
//        report.setDatasource(getDataSource());
//        report.setParameters(getParameters());
//        report.show(type);

        jasperreport.setSrc(getReportName());
        jasperreport.setDatasource(getDataSource());
        jasperreport.setParameters(getParameters());
        jasperreport.setType(type);


        if (type.equals(HTML)) {
            URItext.setStyle("display: none");
            Executions.getCurrent().sendRedirect(jasperreport.getReportUrl(), "_blank");
        } else {
            /*
             * We cant use FileDownload.save(<url>) as it creates a new url
             * where the resource can't be find so we have to create ourselves
             * the download request
             * */
            Executions.getCurrent().addAuResponse(new AuDownload(jasperreport.getReportUrl()));
          URItext.setStyle("display: inline");
          URIlink.setHref(jasperreport.getReportUrl());
        }
    }

    protected Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        String companyLogo = Registry.getConfigurationDAO()
                .getConfigurationWithReadOnlyTransaction().getCompanyLogoURL();
        if (StringUtils.isBlank(companyLogo)) {
            companyLogo = "/logos/logo.png";
        }
        parameters.put("logo", companyLogo);
        parameters.put(JRParameter.REPORT_LOCALE, getCurrentLocale());
        return parameters;
    }

    private String getLanguage() {
        String lang = Locales.getCurrent().getLanguage();
        if (!supportedLanguages.contains(lang)) {
            lang = DEFAULT_LANG;
        }
        return lang;
    }

    private Locale getCurrentLocale() {
        return new Locale(getLanguage());
    }

    protected abstract JRDataSource getDataSource();

    protected abstract String getReportName();
}
