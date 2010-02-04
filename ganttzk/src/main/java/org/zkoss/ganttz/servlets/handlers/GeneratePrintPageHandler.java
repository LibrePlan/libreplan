/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.zkoss.ganttz.servlets.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.ganttz.print.GanttDiagramURIStore;
import org.zkoss.ganttz.servlets.CallbackServlet.IServletRequestHandler;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class GeneratePrintPageHandler implements IServletRequestHandler {

    private static final String DEFAULT_TITLE = "Xestion-produccion";

    private String id;

    private String title = DEFAULT_TITLE;

    public GeneratePrintPageHandler(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        final String page = getPage();
        writer.write(page);
        writer.close();
    }

    private String getPage() {
        String result = "";
        result += page_template;
        result = result.replace("###TITLE###", getTitle());
        result = result.replace("###GANTT_DIAGRAM_IMAGES###", getDiagramImages());
        return result;
    }

    private String getDiagramImages() {
        String result = "";
        final List<String> URIs = GanttDiagramURIStore.getURIsById(id);
        for(String URI: URIs) {
            result += image_template.replace("###URI###", URI);
        }
        return result;
    }

    private static final String page_template = new StringBuilder()
        .append("<html>")
        .append("<head><title>###TITLE###</title></head>")
        .append("<body>###GANTT_DIAGRAM_IMAGES###</body>")
        .append("</html>")
        .toString();

    private static final String image_template = new StringBuilder()
        .append("<div>")
        .append("<img src='###URI###'/>")
        .append("</div>")
        .toString();

}
