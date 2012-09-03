/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.business.templates.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines a default {@link BudgetTemplate} for LibrePlan Audiovisual.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum PredefinedBudgetTemplates {
    ICAA(new Container("ICAA", "Modelo ICAA",
        new Container("01", "GUIÓN Y MÚSICA",
            new Container("01.01", "Guión",
                new Element("01.01.01", "Derechos de autor"),
                new Element("01.01.02", "Argumento original"),
                new Element("01.01.03","Guión"),
                new Element("01.01.04","Diálogos Adicionales"),
                new Element("01.01.05","Traducciones")
            ),
            new Container("01.02", "Música",
                    new Element("01.02.01","Derechos autor músicas"),
                    new Element("01.02.02","Derechos autores  canciones"),
                    new Element("01.02.03","Compositor música de fondo"),
                    new Element("01.02.04","Arreglista"),
                    new Element("01.02.05","Director orquesta"),
                    new Element("01.02.06","Profesores grabación canciones"),
                    new Element("01.02.07","Prof. grabación música de fondo"),
                    new Element("01.02.08","Cantantes"),
                    new Element("01.02.09","Coros"),
                    new Element("01.02.10","Copisteria musical")
            )
        ),
        new Container("02", "PERSONAL ARTÍSTICO",
            new Container("02.01", "Protagonistas",
                new Element("02.01.01", "Protagonista 1"),
                new Element("02.01.02", "Protagonista 2")
            ),
            new Container("02.02", "Principales",
                    new Element("02.02.01", "Principal 1"),
                    new Element("02.02.02", "Principal 2")
                ),
            new Container("02.03", "Secundarios",
                new Element("02.03.01", "Secundario 1"),
                new Element("02.03.02", "Secundario 2")
            ),
            new Container("02.04", "Pequeñas partes",
                    new Element("02.04.01", "Act. pequeña parte 1"),
                    new Element("02.04.02", "Act. pequeña parte 2")
            ),
            new Container("02.05", "Figuración",
                    new Element("02.05.01","Agrupaciones"),
                    new Element("02.05.02","Local en 1"),
                    new Element("02.05.03","Local en ..."),
                    new Element("02.05.04","Local en ..."),
                    new Element("02.05.05","Dobles de luces")
            ),
            new Container("02.06", "Especialistas",
                    new Element("02.06.01","Dobles de acción"),
                    new Element("02.06.02","Maestro de armas … D…"),
                    new Element("02.06.03","Especialistas"),
                    new Element("02.06.04","Caballistas")
            ),
            new Container("02.07", "Especialistas",
                    new Element("02.07.01","Coreógrafo"),
                    new Element("02.07.02","Bailarines"),
                    new Element("02.07.03","Cuerpo de baile"),
                    new Element("02.07.04","Orquestas")
            ),
            new Container("02.08", "Especialistas",
                    new Element("02.08.01","Director de doblaje. D ..."),
                    new Element("02.08.02","Doblador para ..."),
                    new Element("02.08.03","Doblador para ...")
            )
        ),
        new Container("03", "EQUIPO TÉCNICO",
                new Container("03.01", "Dirección",
                        new Element("03.01.01","Director"),
                        new Element("03.01.02","Primer ayudante direc"),
                        new Element("03.01.03","Secretario de rodaje"),
                        new Element("03.01.04","Auxiliar de dirección"),
                        new Element("03.01.05","Director de reparto")
                ),
                new Container("03.02", "Producción",
                        new Element("03.02.01","Productor ejecutiv"),
                        new Element("03.02.02","Director producción"),
                        new Element("03.02.03","Jefe producción"),
                        new Element("03.02.04","Primer ayudante prod"),
                        new Element("03.02.05","Regidor"),
                        new Element("03.02.06","Auxiliar producción"),
                        new Element("02.08.07","Cajero-pagador"),
                        new Element("03.02.08","Secretaria producción")
               ),
               new Container("03.03", "Fotografía",
                       new Element("03.03.01","Director de fotografía. D."),
                       new Element("03.03.02","Segundo operador. D."),
                       new Element("03.03.03","Ayudante ( foquista ). D."),
                       new Element("03.03.04","Auxiliar de cámara. D."),
                       new Element("03.03.05","Fotógrafo de escenas. D.")
               ),
               new Container("03.04", "Decoración",
                       new Element("03.04.01","Decorador. D."),
                       new Element("03.04.02","Ayudante decoración. D."),
                       new Element("03.04.03","Ambientador. D."),
                       new Element("03.04.04","Atrecista. D."),
                       new Element("03.04.05","Tapicero. D."),
                       new Element("03.04.06","Constructor Jefe. D."),
                       new Element("03.04.07","Pintor. D."),
                       new Element("03.04.08","Pintor. D."),
                       new Element("03.04.09","Carpintero. D."),
                       new Element("03.04.10","Carpintero. D.")
               ),
               new Container("03.05", "Sastrería",
                       new Element("03.05.01","Figurinista. D."),
                       new Element("03.05.02","Jefe Sastreria. D."),
                       new Element("03.05.03","Sastra. D.")
               ),
               new Container("03.06", "Maquillaje",
                       new Element("03.06.01","Maquillador. D."),
                       new Element("03.06.02","Ayudante. D."),
                       new Element("03.06.03","Auxiliar. D.")
               ),
               new Container("03.07", "Peluquería",
                       new Element("03.07.01","Peluquero. D."),
                       new Element("03.07.02","Ayudante. D."),
                       new Element("03.07.03","Auxiliar. D.")
               ),
               new Container("03.08", "Efectos especiales y Efectos sonoros",
                       new Element("03.08.01","Jefe Efect. Especiales. D."),
                       new Element("03.08.02","Ayudante. D."),
                       new Element("03.08.03","Armero. D."),
                       new Element("03.08.04","Jefe efectos sonoros. D."),
                       new Element("03.08.05","Ambientes. D."),
                       new Element("03.08.06","Efectos sala. D.")
               ),
               new Container("03.09", "Sonido",
                       new Element("03.09.01","Jefe. D."),
                       new Element("03.09.02","Ayudante. D.")
               ),
               new Container("03.10", "Montaje",
                       new Element("03.10.01","Montador. D."),
                       new Element("03.10.02","Ayudante. D."),
                       new Element("03.10.03","Auxiliar. D.")
               ),
               new Container("03.11", "Electricistas y maquinistas",
                       new Element("03.11.01","Jefe Electricistas. D."),
                       new Element("03.11.02","Electricistas. D."),
                       new Element("03.11.03","Maquinistas. D.")
               ),
               new Container("03.12", "Personal complementario",
                       new Element("03.12.01","Asistencia sanitaria. D."),
                       new Element("03.12.02","Guardas. D."),
                       new Element("03.12.03","Peones. D.")
               ),
               new Container("03.13", "Segunda unidad",
                       new Element("03.13.01","Director. D."),
                       new Element("03.13.02","Jefe producción. D."),
                       new Element("03.13.03","Primer operador. D."),
                       new Element("03.13.04","Segundo Operador. D."),
                       new Element("03.13.05","Ayudante dirección. D."),
                       new Element("03.13.06","Ayudante producción. D."),
                       new Element("03.13.07","Ayudante cámara. D.")
               )
)
    ));

    private Element root;

    private PredefinedBudgetTemplates(Element root) {
        this.root = root;
    }

    public BudgetTemplate getBudgetTemplate() {
        BudgetTemplate budgetTemplate = BudgetTemplate.create();
        setCodeAndName(budgetTemplate, root);

        for (Element each : ((Container) root).children) {
            budgetTemplate.add(convertToTemplate(each));
        }

        return budgetTemplate;
    }

    private void setCodeAndName(OrderElementTemplate template, Element element) {
        template.setCode(element.code);
        template.setName(element.name);
    }

    private OrderElementTemplate convertToTemplate(Element element) {
        OrderElementTemplate template;
        if (element.isContainer()) {
            template = OrderLineGroupTemplate.createNew();
            for (Element each : ((Container) element).children) {
                ((OrderLineGroupTemplate) template)
                        .add(convertToTemplate(each));
            }
        } else {
            template = BudgetLineTemplate.createNew();
            ((BudgetLineTemplate) template)
                    .setBudgetLineType(BudgetLineTypeEnum.TOTAL_SALARY);
        }
        setCodeAndName(template, element);
        return template;
    }

}

class Element {
    String code;
    String name;

    Element(String code, String name) {
        this.code = code;
        this.name = name;
    }

    boolean isContainer() {
        return false;
    }
}

class Container extends Element {
    List<Element> children = new ArrayList<Element>();

    Container(String code, String name) {
        super(code, name);
        throw new UnsupportedOperationException(
                "Unable to create a container without children");
    }

    Container(String code, String name, Element... children) {
       super(code, name);
       this.children = Arrays.asList(children);
    }

    @Override
    boolean isContainer() {
        return true;
    }

}
