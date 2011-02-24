.. sectnum::

How To Create A New Report In NavalPlan
=======================================

.. contents::

NavalPlan uses **JasperReports** [1]_ to create reports in the application. This
document tries to explain how to create a new report in NavalPlan.

During this tutorial you are going to create a report that will show the list of
resources in NavalPlan.


Add entry on NavalPlan menu
---------------------------

First of all, you are going to add a new entry on *Reports* menu in NavalPlan,
this option will link to a new ``.zul`` file inside
``navalplanner-webapp/src/main/webapp/reports/`` that will be the basic
interface for users before generate the report.

Steps:

* Modify ``CustomMenuController.java`` to add a new ``subItem`` inside the
  ``topItem`` *Reports*::

    subItem(_("Resources List"),
        "/reports/resourcesListReport.zul",
        "15-informes.html")

You will see the new entry if you run NavalPlan, but the link is not going to
work as ``.zul`` page still does not exist.


Create basic HTML interface
---------------------------

You need an interface were users could specify some parameters (if needed) for
the report and then generate the expected result. This interface will be
linked from the menu entry added before. For the moment, you are going to create
a very basic interface, copying some parts from other reports.

Steps:

* Create a new file ``resourcesListReport.zul`` in
  ``navalplanner-webapp/src/main/webapp/reports/``. With the following content:

::

 <!--
     This file is part of NavalPlan

     Copyright (C) 2011 Igalia

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.

     You should have received a copy of the GNU Affero General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 -->

 <?page title="${i18n:_('NavalPlan: Resources List')}" id="reports"?>
 <?init class="org.zkoss.zkplus.databind.AnnotateDataBinderInit" ?>
 <?init class="org.zkoss.zk.ui.util.Composition" arg0="/common/layout/template.zul"?>

 <?link rel="stylesheet" type="text/css" href="/common/css/navalplan.css"?>
 <?link rel="stylesheet" type="text/css" href="/common/css/navalplan_zk.css"?>

 <?variable-resolver class="org.zkoss.zkplus.spring.DelegatingVariableResolver"?>

 <?component name="combobox_output_format" macroURI="combobox_output_format.zul"
     class="org.navalplanner.web.reports.ComboboxOutputFormat" ?>

 <?component name="extendedjasperreport"
     class="org.navalplanner.web.common.components.ExtendedJasperreport"
     extends="jasperreport" ?>

 <zk id="resourcesList" xmlns:n="http://www.zkoss.org/2005/zk/native">

     <window self="@{define(content)}"
         apply="org.navalplanner.web.reports.ResourcesListController"
         title="${i18n:_('Resources List')}"
         border="normal" >

         <!-- Select output format -->
         <panel title="${i18n:_('Format')}" border="normal"
             style="overflow:auto">
             <panelchildren>
                 <grid width="700px">
                     <columns>
                         <column width="200px" />
                         <column />
                     </columns>
                     <rows>
                         <row>
                             <label value="${i18n:_('Output format:')}" />
                             <combobox_output_format id="outputFormat" />
                         </row>
                     </rows>
                 </grid>
             </panelchildren>
         </panel>

         <hbox style="display: none" id="URItext">
             <label value="${i18n:_('Click on ')}" />
             <toolbarbutton id="URIlink" class="z-label" zclass="z-label"
                     label="${i18n:_('direct link')}" />
             <label value="${i18n:_(' to go to output directly')}" />
         </hbox>

         <separator spacing="10px" orient="horizontal" />

         <button label="Show" onClick="controller.showReport(report)" />

         <extendedjasperreport style="display: none" id="report" />

     </window>

 </zk>

This will create a basic interface for report with a combo to select the desired
output format for it and a button to generate the report. As we can see it uses
``resourcesListController`` that will be created in the next point.


Create a controller for new report
----------------------------------

As you can see previous ``.zul`` file defined uses a controller that will be in
charge to manage users interaction with report interface and call the proper
methods to generate the report itself and show it to the user.

There is already a controller called ``NavalplannerReportController`` which
implements most of the stuff needed for report controllers. So, controllers for
new reports are going to extend this class and re-implement some methods.

Steps:

* Create a new file ``ResourcesListController.java`` in
  ``navalplanner-webapp/src/main/java/org/navalplanner/web/reports/`` with the
  following content:

::

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

 package org.navalplanner.web.reports;

 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.JREmptyDataSource;

 import org.zkoss.zk.ui.Component;

 /**
  * Controller for UI operations of Resources List report.
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 public class ResourcesListController extends NavalplannerReportController {

     private static final String REPORT_NAME = "resourcesListReport";

     @Override
     public void doAfterCompose(Component comp) throws Exception {
         super.doAfterCompose(comp);
         comp.setVariable("controller", this, true);
     }

     @Override
     protected String getReportName() {
         return REPORT_NAME;
     }

     @Override
     protected JRDataSource getDataSource() {
         return new JREmptyDataSource();
     }

 }

Now if you run NavalPlan and access to the new menu entry you will see the
simple form allowing you to choose the output format for the report and also the
button to show it (that will not work yet).


Create a DTO
------------

As usually reports show information extracted from database but with some
specific modifications, for example, merging data from different database
tables; you will need to define a DTO (Data Transfer Object) with the fields
that you want to show in the report.

In your case the DTO is pretty simple, you will show for each resource: code and
name.

Steps:

* Create a new file ``ResourcesListDTO.java`` in
  ``navalplanner-business/src/main/java/org/navalplanner/business/reports/dtos/``
  with the following content:

::

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

 package org.navalplanner.business.reports.dtos;

 /**
  * DTO for Resources List report data.
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 public class ResourcesListDTO {

     private String code;

     private String name;

     public ResourcesListDTO(String code, String name) {
         this.code = code;
         this.name = name;
     }

     public String getCode() {
         return code;
     }

     public String getName() {
         return name;
     }

 }

A list of DTOs will be passed to JasperReports in order to generate the report
with the data.


Define report layout (iReport)
------------------------------

Now that you know which data you are going to show in the report (check DTOs
attributes) you should define the JasperReports format with a XML.

You need to install **iReport** [2]_, it is a tool used to define and design
report layouts, which provides a visual interface to define ``.jrxml`` file.

Steps:

* Download iReport **3.7.0** (``tar.gz``) from SourceForge.net:
  https://sourceforge.net/projects/ireport/files/iReport/

* Uncompress file::

    tar -xvzf iReport-3.7.0.tar.gz

* Launch iReport::

    cd iReport-3.7.0/
    ./bin/ireport

* Open some existent NavalPlan report (e.g.
  ``hoursWorkedPerWorkerInAMonthReport.jrxml``) under
  ``navalplanner-webapp/src/main/jasper`` to use as template to keep the same
  layout and save it with the name of the new report
  ``resourcesListReport.jrxml`` in the same folder.

  This will allow us to keep coherence between reports in regard to design,
  header, footer, etc.

* Set report name to ``resourcesList``.

* Set resource bundle to ``resourcesList``.

* Remove following parameters:

  * ``startingDate``
  * ``endingDate``
  * ``showNote``

* Remove all the fields and add the following:

  * Name: ``code``, class: ``java.lang.String``
  * Name: ``name``, class: ``java.lang.String``

* Remove following variables:

  * ``sumHoursPerDay``
  * ``sumHoursPerWorker``

* Remove following elements in *Title* band:

  * ``$R{date.start}``
  * ``$R{date.end}``
  * ``$P{startingDate}``
  * ``$P{endingDate}``
  * ``$R{note1}``
  * Label: ``*``

* Remove group *Worker group Group Header 1*.

* Remove group *Date group Group Header 1*.

* Remove columns in *Detail 1* band in order to leave only 2 columns:
  ``$F{code}`` and ``$F{name}``.

Now you have defined a very basic report layout using some common elements
with other NavalPlan reports like header and footer. The result in iReport would
be something similar to the screenshot.

.. figure:: ireport-resources-list-report.png
   :alt: iRerpot screenshot for Resources List report
   :width: 100%

   iReport screenshot for Resources List report


Add report bundle for translation strings
-----------------------------------------

Once defined the report format with *iReport* you need to create an special
directory to put there translation files related with report strings.

Steps:

* Create directory called ``resourcesList_Bundle`` in
  ``navalplanner-webapp/src/main/jasper/``::

    mkdir navalplanner-webapp/src/main/jasper/resourcesList_Bundle

  You can check bundle folders of other reports in the same directory to see
  more   examples, but it basically contains the properties files with different
  translations for the project.

* Create a file called ``resourcesList.properties`` inside the new directory
  with the following content:

::

 # Locale for resourcesListReport.jrxml
 title = Resources List Report
 subtitle = List of resources
 page = page
 of = of

* Copy this file to a new one for English locale::

    cp resourcesList.properties resourcesList_en_US.properties

* Add the following lines in main ``pom.xml`` file at project root folder,
  in ``Report bundle directories`` section::

    <resource>
        <directory>../navalplanner-webapp/src/main/jasper/resourcesList_Bundle/</directory>
    </resource>

Now jun can run NavalPlan and see the report already working, but as you are not
sending it any data (currently you are using ``JREmptyDataSource``) the report
will appear empty but you can see header and footer.


Create some example data and see your first report
--------------------------------------------------

At that point you have everything ready to generate your first report, but you
need to show some data in the report. So, you are going to add some example data
manually created to see the final result.

Steps:

* Modify ``getDataSource`` method in ``ResourcesListController`` created before
  and use the following content as example:

::

     @Override
     protected JRDataSource getDataSource() {
         // Example data
         ResourcesListDTO resource1 = new ResourcesListDTO("1", "Jonh Doe");
         ResourcesListDTO resource2 = new ResourcesListDTO("2", "Richard Roe");

         List<ResourcesListDTO> workersListDTOs = Arrays.asList(resource1,
                 resource2);

         return new JRBeanCollectionDataSource(workersListDTOs);
     }

* Compile NavalPlan with the following command from project root folder::

    mvn -DskipTests -P-userguide clean install

* Launch Jetty from ``navalplanner-webapp`` directory::

    cd navalplanner-webapp
    mvn -P-userguide jetty:run

  Then if you go to the new menu entry called *Resources List* in *Reports* you
  will be able to generate a report with the resources added as example data.
  The report still lacks a good design and format, but at least you are able to
  see how the basic functionality of JasperReports in NavalPlan is integrated.


.. [1] http://jasperforge.org/jasperreports
.. [2] http://jasperforge.org/projects/ireport
