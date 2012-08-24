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
                new Element("01.01.02", "Argumento original")
            ),
            new Container("01.02", "Música",
                new Element("01.02.01", "Derechos de autor músicas"),
                new Element("01.02.02", "Derechos de autor canciones")
            )
        ),
        new Container("02", "PERSONAL ARTÍSTICO",
            new Container("02.01", "Protagonistas",
                new Element("02.01.01", "Protagonista 1"),
                new Element("02.01.02", "Protagonista 2")
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
