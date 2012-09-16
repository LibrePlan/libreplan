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
            new Container("02.07", "Ballet y orquestas",
                new Element("02.07.01","Coreógrafo"),
                new Element("02.07.02","Bailarines"),
                new Element("02.07.03","Cuerpo de baile"),
                new Element("02.07.04","Orquestas")
            ),
            new Container("02.08", "Doblaje",
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
        ),

        new Container("04", "ESCENOGRAFÍA",
            new Container("04.01", "Decorados y escenarios",
                new Element("04.01.01","Construcción y montaje de decorados en plató"),
                new Element("04.01.02","Derribo decorados"),
                new Element("04.01.03","Construcción en exteriores"),
                new Element("04.01.04","Construcción en interiores naturales"),
                new Element("04.01.05","Maquetas"),
                new Element("04.01.06","Florillos"),
                new Element("04.01.07","Alquiler decorados"),
                new Element("04.01.08","Alquiler de interiores naturales")
            ),
            new Container("04.02", "Ambientación",
                  new Element("04.02.01","Mobiliario alquilado"),
                  new Element("04.02.02","Atrezzo alquilado"),
                  new Element("04.02.03","Mobiliario adquirido"),
                  new Element("04.02.04","Atrezzo adquirido"),
                  new Element("04.02.05","Jardinería"),
                  new Element("04.02.06","Armería"),
                  new Element("04.02.07","Vehículos en escena"),
                  new Element("04.02.08","Comidas en escena"),
                  new Element("04.02.08","Material efectos especiales")
              ),
              new Container("04.03", "Vestuario",
                  new Element("04.03.01","Vestuario alquilado"),
                  new Element("04.03.02","Vestuario adquirido"),
                  new Element("04.03.03","Zapatería"),
                  new Element("04.03.04","Complementos"),
                  new Element("04.03.05","Complementos ..."),
                  new Element("04.03.06","Materiales sastrería")
              ),
              new Container("04.04", "Semimovientes y carruajes",
                  new Element("04.04.01","Animales"),
                  new Element("04.04.02","Animales ..."),
                  new Element("04.04.03","Cuadras y piensos"),
                  new Element("04.04.04","Cuadras y piensos ..."),
                  new Element("04.04.05","Cuadras y piensos ..."),
                  new Element("04.04.06","Carruajes")
              ),
              new Container("04.05", "Varios",
                  new Element("04.05.01","Material peluquería"),
                  new Element("04.05.02","Material maquillaje")
              )
          ),

          new Container("05", "ESTUDIOS RODAJE/SONORIZACIÓN Y VARIOS PRODUCCIÓN",
              new Container("05.01", "Estudios de rodaje",
                  new Element("05.01.01","Alquiler de platós"),
                  new Element("05.01.02","Rodaje en exteriores estudio"),
                  new Element("05.01.03","Fluido eléctrico del estudio"),
                  new Element("05.01.04","Fluido eléctrico del estudio ..."),
                  new Element("05.01.05","Instalaciones complementarias")
              ),
              new Container("05.02", "Montaje y sonorización",
                  new Element("05.02.01","Alquiler de platós"),
                  new Element("05.02.02","Rodaje en exteriores estudio"),
                  new Element("05.02.03","Fluido eléctrico del estudio"),
                  new Element("05.02.04","Fluido eléctrico del estudio ..."),
                  new Element("05.02.05","Instalaciones complementarias"),
                  new Element("05.02.06","Alquiler de platós"),
                  new Element("05.02.07","Rodaje en exteriores estudio"),
                  new Element("05.02.08","Fluido eléctrico del estudio"),
                  new Element("05.02.09","Fluido eléctrico del estudio ..."),
                  new Element("05.02.10","Instalaciones complementarias"),
                  new Element("05.02.11","Alquiler de platós"),
                  new Element("05.02.12","Rodaje en exteriores estudio"),
                  new Element("05.02.13","Fluido eléctrico del estudio"),
                  new Element("05.02.14","Fluido eléctrico del estudio ..."),
                  new Element("05.02.15","Instalaciones complementarias")
              ),
              new Container("05.03", "Varios producción",
                  new Element("05.03.01","Copias de guión"),
                  new Element("05.03.02","Fotocopias en rodaje"),
                  new Element("05.03.03","Teléfono en fechas de rodaje"),
                  new Element("05.03.04","Alquiler camerinos exteriores"),
                  new Element("05.03.05","Alquiler de caravanas"),
                  new Element("05.03.06","Alquiler oficina exteriores"),
                  new Element("05.03.07","Almacenes varios"),
                  new Element("05.03.08","Garajes en fechas de rodaje"),
                  new Element("05.03.09","Garajes en fechas de rodaje ... "),
                  new Element("05.03.10","Limpieza, etc. lugares de rodaje"),
                  new Element("05.03.11","Comunicaciones en rodaje")
              )
          ),

          new Container("06", "MAQUINARIA DE RODAJE Y TRANSPORTE",
              new Container("06.01", "Maquinaria y elementos de rodaje",
                  new Element("06.01.01","Camara principal"),
                  new Element("06.01.02","Cámaras secundarias"),
                  new Element("06.01.03","Objetivos especiales y complementarios"),
                  new Element("06.01.04","Accesorios"),
                  new Element("06.01.05","Accesorios..."),
                  new Element("06.01.06","Accesorios..."),
                  new Element("06.01.07","Material iluminación alquilado"),
                  new Element("06.01.08","Material maquinistas alquilado"),
                  new Element("06.01.09","Material iluminación adquirido"),
                  new Element("06.01.10","Material maquinistas adquirido"),
                  new Element("06.01.11","Grúas"),
                  new Element("06.01.12","Otros materiales iluminación maquinistas"),
                  new Element("06.01.13","Cámara Car"),
                  new Element("06.01.14","Alquiler camerinos exteriores"),
                  new Element("06.01.15","Plataforma"),
                  new Element("06.01.16","Grupo electrogeno"),
                  new Element("06.01.17","Helicóptero, aviones, etc."),
                  new Element("06.01.18","Helicóptero, aviones, etc. ..."),
                  new Element("06.01.19","Helicóptero, aviones, etc. ..."),
                  new Element("06.01.20","Helicóptero, aviones, etc. ..."),
                  new Element("06.01.21","Equipo de sonido principal"),
                  new Element("06.01.21","Equipo sonido complementario"),
                  new Element("06.01.21","Fluido eléctrico (enganches)")
              ),
              new Container("06.02","Transportes",
                  new Element("06.02.01","Coches de producción"),
                  new Element("06.02.02","Coches de producción..."),
                  new Element("06.02.03","Coches de producción..."),
                  new Element("06.02.04","Coches de producción..."),
                  new Element("06.02.05","Coches de producción..."),
                  new Element("06.02.06","Coches de producción..."),
                  new Element("06.02.07","Alquiler coches sin conductor"),
                  new Element("06.02.08","Furgonetas de cámaras"),
                  new Element("06.02.09","Furgoneta de ..."),
                  new Element("06.02.10","Furgoneta de ..."),
                  new Element("06.02.11","Camión de ..."),
                  new Element("06.02.12","Camión de ..."),
                  new Element("06.02.13","Camión de ..."),
                  new Element("06.02.14","Camión de ..."),
                  new Element("06.02.15","Autobuses"),
                  new Element("06.02.16","Taxis en fechas de rodaje"),
                  new Element("06.02.17","Facturaciones"),
                  new Element("06.02.18","Aduanas y fletes")
              )
         ),

         new Container("07", "VIAJES, HOTELES Y COMIDAS",
             new Container("07.01", "Localizaciones",
                 new Element("07.01.01","Viaje a ... fecha ... "),
                 new Element("07.01.02","Viaje a ... fecha ... "),
                 new Element("07.01.03","Viaje a ... fecha ... "),
                 new Element("07.01.04","Viaje a ... fecha ... "),
                 new Element("07.01.05","Gastos locomoción")
             ),
             new Container("07.02", "Viajes",
                 new Element("07.02.01","... personas a ... "),
                 new Element("07.02.02","... personas a ... "),
                 new Element("07.02.03"," ... personas a ... ")
             ),
             new Container("07.03", "Hoteles y comidas",
                 new Element("07.03.01","Facturación hotel ..."),
                 new Element("07.03.02","Facturación hotel ..."),
                 new Element("07.03.03","Comidas en fechas de rodaje")
             )
         ),

         new Container("08", "PELÍCULA VIRGEN",
             new Container("08.01", "Negativo",
                 new Element("08.01.01","Negativo de color ... ASA"),
                 new Element("08.01.02","Negativo de color ... ASA"),
                 new Element("08.01.03","Negativo de blanco y negro"),
                 new Element("08.01.04","Negativo de sonido"),
                 new Element("08.01.05","Internegativo"),
                 new Element("08.01.06","Duplicating")
             ),
             new Container("08.02", "Positivo",
                 new Element("08.02.01","Positivo imagen color"),
                 new Element("08.02.02","Positivo imagen B.y N"),
                 new Element("08.02.03","Positivo primera copia estándar"),
                 new Element("08.02.01","Positivo segunda copia estándar"),
                 new Element("08.02.02","Interpositivo"),
                 new Element("08.02.03","Lavender")
             ),
             new Container("08.03", "Magnéticos y varios",
                 new Element("08.03.01","Magnético 35/16 mm (nuevo)"),
                 new Element("08.03.02","Magnético 35/16 mm (usado)"),
                 new Element("08.03.03","Magnético 1/4 pulgada"),
                 new Element("08.03.03","Magnético 1/4 pulgada ..."),
                 new Element("08.03.03","Material fotografías escenas"),
                 new Element("08.03.03","Otros materiales")
             )
         ),

         new Container("09", "LABORATORIO",
             new Container("09.01", "Revelado",
                 new Element("09.01.01","De imagen color"),
                 new Element("09.01.02","De imagen B. y N"),
                 new Element("09.01.03","De internegativo"),
                 new Element("09.01.04","De Duplicating"),
                 new Element("09.01.05","De sonido")
             ),
             new Container("09.02", "Positivado",
                 new Element("09.02.01","De imagen color"),
                 new Element("09.02.02","De imagen B. y N"),
                 new Element("09.02.03","De interpositivo"),
                 new Element("09.02.04","De Lavender"),
                 new Element("09.02.05","De primera copia estándar"),
                 new Element("09.02.06","De segunda copia estándar")
             ),
             new Container("09.03", "Varios",
                 new Element("09.03.01","Corte de negativo"),
                 new Element("09.03.02","Descarte"),
                 new Element("09.03.03","Clasificación y archivo"),
                 new Element("09.03.04","Sincronización negativos"),
                 new Element("09.03.05","Otros trabajos"),
                 new Element("09.03.06","Trucajes"),
                 new Element("09.03.07","Títulos de crédito"),
                 new Element("09.03.08","Laboratorio fotografías"),
                 new Element("09.03.09","Animación"),
                 new Element("09.03.09","Imágenes de archivo"),
                 new Element("09.03.09","Animación")
             )
         ),

         new Container("10", "SEGUROS",
             new Container("10.01", "Seguros",
                 new Element("10.01.01","Seguro de negativo"),
                 new Element("10.01.02","Seguro de materiales de rodaje"),
                 new Element("10.01.03","Seguro de responsabilidad civil"),
                 new Element("10.01.04","Seguro de accidentes"),
                 new Element("10.01.05","Seguro de interrupción de rodaje"),
                 new Element("10.01.06","Seguro de buen fín"),
                 new Element("10.01.07","Seguro de buen fín..."),
                 new Element("10.01.08","Seguridad Social (Rég. General) (Cuotas de empresa)"),
                 new Element("10.01.09","Seguridad Social (Rég. General) (Cuotas de empresa) ...")
             )
         ),

         new Container("11", "GASTOS GENERALES",
             new Container("11.01", "Generales",
                 new Element("11.01.01","Alquiler de oficina"),
                 new Element("11.01.02","Personal administrativo"),
                 new Element("11.01.03","Mensajería"),
                 new Element("11.01.04","Correo y Telégrafo"),
                 new Element("11.01.05","Teléfonos"),
                 new Element("11.01.06","Taxis y gastos de locomoción fuera de fechas de rodaje"),
                 new Element("11.01.07","Luz, agua, limpieza"),
                 new Element("11.01.08","Material de oficina"),
                 new Element("11.01.09","Comidas pre y post rodaje"),
                 new Element("11.01.10","Gestoria Seguros Sociales")
             )
         ),

         new Container("12", "GASTOS DE EXPLOTACIÓN, COMERCIAL Y FINANCIEROS",
             new Container("12.01", "CRI y copias",
                 new Element("12.01.01","CRI o Internegativo"),
                 new Element("12.01.02","Copias")
             ),
             new Container("12.02", "Publicidad",
                 new Element("12.02.01","Publicidad ..."),
                 new Element("12.02.02","Publicidad ..."),
                 new Element("12.02.03","Publicidad ..."),
                 new Element("12.02.04","Trayler (Laboratorio, copias, difusión)"),
                 new Element("12.02.05","Making off")
             ),
             new Container("12.03", "Intereses pasivos",
                 new Element("12.03.01","Intereses pasivos y gastos de negociación de préstamos")
             )
         )
    )
);

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
