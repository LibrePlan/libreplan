--------------------------------------
How To Develop A Use Case In LibrePlan
--------------------------------------

.. sectnum::

:Author: Manuel Rego Casasnovas
:Contact: rego@igalia.com
:Date: 15/08/2011
:Copyright:
  Some rights reserved. This document is distributed under the Creative
  Commons Attribution-ShareAlike 3.0 licence, available in
  http://creativecommons.org/licenses/by-sa/3.0/.
:Abstract:
  This is a guide about how to develop a use case in LibrePlan_. Following the
  different sections of this document you will end up developing a complete
  CRUD_ (create, read, update and delete) use case in the project.

  Thanks to this tutorial you will know the basic structure, different layers of
  application architecture and underlying technology stack. Summarizing, you
  will learn how to create a new entity, define an interface to manipulate it,
  store it on a database, add some kind of validation and integrate it with web
  services.

.. contents:: Table of Contents


Introduction
============

This manual is a kind of practical exercise that will be solved throughout the
different sections. It is required to have basic knowledge about Java software
platform in order to properly follow the document. Moreover, knowledge in
different Java frameworks used in LibrePlan like Hibernate, Spring, ZK, JUnit,
etc. would be a nice addition.

The goal of this document is develop a **complete CRUD use case in LibrePlan**.
The idea consists of create a new entity called ``StretchesFunctionTemplate``
that will be managed from application interface, just like any other entity in
the project.

``StretchesFunctionTemplate`` will be a class to define templates for different
``StretchesFunction`` that are used in advanced allocation window.
A ``StretchesFunction`` is a kind of assignment function which allow users
define different stretches in order to do resource allocations.

.. NOTE::

  Let's imagine that you have a task which lasts from May 1st to May 10th. Then
  users could define the following stretches:

  * Stretch 1:

    * Duration: 20%
    * Progress: 50%

  * Stretch 2:

    * Duration: 50%
    * Progress: 70%

  * Stretch 3:

    * Duration: 70%
    * Progress: 100%

  Where, for example, *Stretch 1* means that half of the work to be done in the
  task will be ready by the end of May 2nd (20% of task duration).

  Then LibrePlan will be perform different resource allocations according to
  function defined by user.

Thanks to the new class ``StretchesFunctionTemplate`` users will have the chance
to store repetitive ``StretchesFunction`` that they usually apply while
scheduling. This will allow users to define some kind of patterns for tasks,
where, for example, they know that always start with a lower load and then it is
increased at the end. They could create a ``StretchesFunctionTemplate`` defining
this behaviour and use it in all the tasks they want.

.. TIP::

   If you want to run LibrePlan in development mode you need to follow the next
   instructions:

   * Create a PostgreSQL database called ``libreplandev`` with permissions for a
     user ``libreplan`` with    password ``libreplan`` (see ``HACKING`` file for
     other databases and more info).

   * Compile LibrePlan with the following command from project root folder::

       mvn -DskipTests -P-userguide clean install

   * Launch Jetty from ``libreplan-webapp`` directory::

       cd libreplan-webapp
       mvn -P-userguide jetty:run

   * Access with a web browser to the following URL and login with default
     credentials (user ``admin`` and password ``admin``):
     http://localhost:8080/libreplan-webapp/


Domain entities
===============

First of all you need to create the new entity ``StretchesFunctionTemplate`` in
LibrePlan **business layer**.

Domain entities encapsulate application business data and part of their logic.
They are Hibernate_ entities, and therefore are retrieved and stored in a data
warehouse (usually a database). Mapping between Java classes and Hibernate is
done with ``.hbm.xml`` files. For example, file ``ResourceAllocations.hbm.xml``
contains ``StretchesFunction`` class mapping.

All domain entities in the project inherit from ``BaseEntity``. ``BaseEntity``
class has two attributes: ``id`` and ``version``. ``id`` is mandatory in order
to entity could be considered as an Hibernate entity. ``version`` attribute is
used to implement concurrency control method called `Optimistic Locking`_.

.. ADMONITION:: Optimistic Locking

  ``version`` field in entities is used to implement the concurrency control
  method in order to detect concurrency problems during execution.

  Let's imagine two users go to edit the same exception day type called
  "HOLIDAY" and both want to modify field ``color``. Currently in database you
  will have::

    name: HOLIDAY
    version: 1
    color: red

  First user changes color and sets "blue" as color and save the entity. When
  entity is stored, ``version`` is incremented by 1, and the result number must
  be greater than current value in database. In this case 2 is greater than 1 so
  it is properly stored on database::

    name: HOLIDAY
    version: 2
    color: blue

  Second user started at the same time, but it is going to try to save the same
  entity later than the first user. Second users sets color to "green" and try
  to store the entity. In this case the value for ``version`` is incremented
  from 1 (the original) to 2, but 2 is not greater than current value in
  database. Therefore, a concurrency problem has happened and second user will
  receive the following message:

  .. pull-quote::

    Another user has modified the same data, so the operation cannot be safely
    completed.

    Please try it again.

Entities instantiation
----------------------

In LibrePlan domain entities are never instantiated directly, but entities will
expose a **static method ``create()``** which will be responsible to return a
new instance. The rest of classes must call ``create()`` method of
``BaseEntity`` when they want to create a new instance of any entity. This is
usually implemented with something similar to the following code::

  public class MyNewEntity extends BaseEntity {

    public static MyNewEntity create() {
        return create(new MyNewEntity());
    }

    /**
     * Constructor for Hibernate. Do not use!
     */
    protected MyNewEntity() {
    }

  }

As you can see, it is defining a default constructor without parameters with
``protected`` visibility. Default constructor is mandatory because of Hibernate
need it, but it is marked with reduced visibility in order to avoid other
classes use it.

.. WARNING::

  In LibrePlan a lot of entities extends ``IntegrationEntity`` instead of
  ``BaseEntity``, anyway ``IntegrationEntity`` also extends ``BaseEntity``.

  ``IntegrationEntity`` is a base class for all domain entities that are going
  to be available via web services in the project. These entities have a
  ``code`` attribute, which unlike ``id`` is unique among the applications to be
  integrated (``id`` is only unique inside one LibrePlan instance).

In order to know if an object is new or not you will use method
``isNewObject()`` of ``BaseEntity``, you will never directly check if ``id``
attribute is ``null`` (transient entity).

.. ADMONITION:: State of objects in Hibernate

  Transient
    An object out of Hibernate session instantiated with ``new()``. Actually, in
    LibrePlan the method used is ``create()`` that calls ``new()`` at some
    point.

  Persistent
    A persistent entity, already stored on database, which is inside a
    Hibernate session.

  Detached
    A persistent entity out of Hibernate session.

New entity implementation
-------------------------

The **new entity ``StretchesFunctionTemplate``** will have the following
properties:

  * ``name``: A string to identify the template.
  * ``stretches``: A list of ``StretchTemplate`` a new class that will just have
    two attributes: ``durationProportion`` and ``progressProportion``. These
    would be two percentages defined as ``BigDecimal`` and one based, i.e., 20%
    will be 0.20.

``StretchTemplate`` will be a value object as every ``StretchTemplate`` will
belong just to one ``StretchesFunctionTemplate`` and would not be modified out
of this relationship. So, in this case ``StretchTemplate`` will not extends
``BaseEntity``.

You will need to create the following files (some excerpts of source code are
shown):

* ``StretchesFunctionTemplate.java``:

::

 package org.libreplan.business.planner.entities;

 ...

 /**
  * This will store repetitive patterns to be applied in different
  * {@link StretchesFunction}
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 public class StretchesFunctionTemplate extends BaseEntity implements
         IHumanIdentifiable {

    public static StretchesFunctionTemplate create(String name) {
        return create(new StretchesFunctionTemplate(name));
    }

    private String name;

    @Valid
    private List<StretchTemplate> stretches = new ArrayList<StretchTemplate>();

    /**
     * Default constructor for Hibernate. Do not use!
     */
    protected StretchesFunctionTemplate() {
    }

    ...

    @Override
    public String getHumanId() {
        return name;
    }

    ...

.. NOTE::

  ``IHumanIdentifiable`` is an interface that needs a human identifier to show
  in application UI. It defines the method ``getHumanId`` that returns a text
  identifier of the entity.

  As this entity is going to be edited from LibrePlan web interface, it
  implements ``IHumanIdentifiable``.


* ``StretchTemplate.java``:

::

 package org.libreplan.business.planner.entities;

 ...

 /**
  * This class is intended as a Hibernate component. It's formed by two
  * components, the duration proportion and the progress proportion. It
  * represents the different values of a {@link StretchesFunctionTemplate}.
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 public class StretchTemplate {

     public static StretchTemplate create(BigDecimal durationProportion,
             BigDecimal progressProportion) {
         return new StretchTemplate(durationProportion, progressProportion);
     }

     private BigDecimal durationProportion = BigDecimal.ZERO;
     private BigDecimal progressProportion = BigDecimal.ZERO;

     /**
      * Default constructor for Hibernate. Do not use!
      */
     protected StretchTemplate() {
     }

 ...

.. IMPORTANT::

  You should not forget to add license header in your new files specifying the
  license as explained in documentation section at `LibrePlan wiki`_. You can
  copy it from other files and modify year and copyright holder accordingly.

  Moreover, always remember to add, at least, a general comment explaining the
  purpose of your classes.


Model View Controller pattern
=============================

LibrePlan architecture follows MVC_ (Model-view-controller) pattern, which
isolates business logic from user interface allowing separation of different
layers in the application. View and controller will be explained later, now it
is time to explain **model layer** that is in charge of implement application
business or domain logic.

This model layer is formed by different elements. On the one hand, there are
domain entities and DAO_ (Data Access Object) classes which offer methods to
query and store domain objects. On the other hand there are ``XXXModel.java``
files, that are always associated to some controller.

.. ADMONITION:: Domain Driven Design

   The project follows approach proposed by DDD_. It tries that business logic
   remains encapsulated inside domain classes, as far as possible, otherwise
   it will be used a model layer.

   The idea is that every domain element will be responsible for itself, which
   means that it knows its business logic and exposes it to other objects
   through methods. Other operations were, for example, several objects are used
   could be written in model layer.

Actually, model classes do not access directly to database but they do it
through a DAO object. DAO classes are responsible for retrieve, query and store
domain entities on database, i.e. they implement the persistence layer only
accessible from model.

However, in the application domain elements can be used directly from view layer
for reading or modifying its content.


Persistence layer communication
-------------------------------

In order to access domain entities it will always exist a **DAO class** for each
entity type. This DAO class inherits from ``GenericDAOHibernate``, which
provides the methods needed to implement common persistence behaviour.

If you want that a model has access to a DAO class, you have to insert an
attribute in your model, for example, a variable called
``tretchesFunctionTemplateDAO`` with type ``IStretchesFunctionTemplateDAO``::

    @Autowired
    private IStretchesFunctionTemplateDAO stretchesFunctionTemplateDAO;

Take into account that this attribute has an interface as type. This interface,
``IStretchesFunctionTemplateDAO``, will have associated an implementation class
called ``StretchesFunctionTemplateDAO``. Spring_ framework is in charge to
inject this implementation class in the variable. For this to happen, it is
needed to mark the attribute with ``@Autowired`` annotation. This will be also
needed to add some special annotations, interpreted by Spring, at implementation
class.

There is also an interface ``IGenericDAOHibernate`` implemented by
``GenericDAOHibernate``. So, your new interface will extend this one.

Then you will have the following files:

* ``IStretchesFunctionTemplateDAO.java``:

::

 package org.libreplan.business.planner.daos;

 ...

 /**
  * DAO interface for {@link StretchesFunctionTemplate}
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 public interface IStretchesFunctionTemplateDAO extends
         IGenericDAO<StretchesFunctionTemplate, Long> {

 }

* ``StretchesFunctionTemplateDAO.java``:

::

 package org.libreplan.business.planner.daos;

 ...

 /**
  * DAO for {@link StretchesFunctionTemplate}
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 @Repository
 @Scope(BeanDefinition.SCOPE_SINGLETON)
 public class StretchesFunctionTemplateDAO extends
         GenericDAOHibernate<StretchesFunctionTemplate, Long> implements
         IStretchesFunctionTemplateDAO {

 }

.. ADMONITION:: Inversion of control

  `Inversion of control`_ pattern, or Dependency Injection, is based on object
  oriented programming principle: "develop in terms of interfaces and
  functionality instead of concrete implementation details".

  In LibrePlan for each DAO class there is an interface class `IXXXDAO`. Models
  always use these interface classes. Spring framework instantiates a class for
  each interface type and injects it in the corresponding variable.

.. NOTE::

  As you can see DAO class is being defined as a singleton with the following
  line::

    @Scope(BeanDefinition.SCOPE_SINGLETON)

  This is because of DAO classes are not going to store any state variable, so
  methods only depends on parameters. Thus, just an instance of a DAO class is
  enough for any place where it is used.

Summarizing, persistence layer encapsulates all operations related to Hibernate
communication for retrieving, querying and storing entities on database.
Therefore, you will not need to use Hibernate API directly in LibrePlan source
code in order to perform operations like: start transaction, commit
transaction, rollback, etc.

Database schema
---------------

Moreover, you need to define **Hibernate mapping** for the new entity
``StretchesFunctionTemplate``. Like this new entity is related with allocations
you will use ``ResourceAllocations.hbm.xml`` file and, then, add the following
lines (in other cases you should look for the proper ``.hbm.xml`` file or just
create a new one if needed)::

    <!-- StretchesFunctionTemplate -->
    <class name="StretchesFunctionTemplate" table="stretches_function_template">
        <id name="id" access="property" type="long">
            <generator class="hilo">
                <param name="max_lo">100</param>
            </generator>
        </id>
        <version name="version" access="property" type="long" />

        <property name="name" access="property" not-null="true" />

        <list name="stretches" table="stretch_template">
            <key column="stretches_function_template_id" />
            <list-index column="stretch_position" />

            <composite-element class="StretchTemplate">
                <property name="durationProportion" column="duration_proportion"
                    not-null="true" />
                <property name="progressProportion" column="progress_proportion"
                    not-null="true" />
            </composite-element>
        </list>
    </class>

However, this is not enough in order to store the new entity on database,
because of tables are not created yet. Usually, tables are automatically created
by Hibernate, but this is disabled in LibrePlan, and Hibernates just validates
that database structure matches with mapping specifications in ``hbm.xml``
files. The reason to disable automatic schema creation is for having a proper
control over `database refactorings`_, this allows that application manage
migrations between databases of different LibrePlan versions. Only testing
database is created automatically in the project.

Liquibase_ is the tool used to manage these **database refactorings**.
Developers have to specify in a changelog file the changes to be applied on
database when they modify any mapping. Then you will need to add the following
lines in the proper ``db.changelog-XXX.xml`` file::

    <changeSet author="mrego" id="create-tables-related-to-stretches_function_template">
        <comment>Create new new tables and indexes related with StretchesFunctionTemplate entity</comment>

        <createTable tableName="stretches_function_template">
            <column name="id" type="BIGINT">
                <constraints nullable="false" primaryKey="true" primaryKeyName="stretches_function_template_pkey"/>
            </column>
            <column name="version" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <createTable tableName="stretch_template">
            <column name="stretches_function_template_id" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="duration_proportion" type="DECIMAL(19,2)">
                <constraints nullable="false"/>
            </column>
            <column name="progress_proportion" type="DECIMAL(19,2)">
                <constraints nullable="false"/>
            </column>
            <column name="stretch_position" type="INTEGER">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <addPrimaryKey
            columnNames="stretches_function_template_id, stretch_position"
            constraintName="stretch_template_pkey"
            tableName="stretch_template"/>

        <addForeignKeyConstraint
            baseColumnNames="stretches_function_template_id"
                baseTableName="stretch_template"
                constraintName="stretch_template_stretches_function_template_id_fkey"
                deferrable="false" initiallyDeferred="false"
                onDelete="NO ACTION" onUpdate="NO ACTION"
                referencedColumnNames="id"
                referencedTableName="stretches_function_template"
                referencesUniqueColumn="false"/>
    </changeSet>

As you can see, this specify the different tables to be created on database and
also some constraints like foreign keys. Usually you can take a look to other
Liquibase changes to know how to create a table or some field. Also a good idea
is to check the result of your changeset against testing database (which is
created automatically), in this way you will be sure that your changes are
right.


Interface
=========

Let's move to **view layer**, now that you already know how is the new entity,
which attributes it has and so on. You are ready to start developing the
interface and start to see something working in the application. LibrePlan uses
ZK_ framework for UI development.

Menu entry
----------

First, the new entity ``StretchesFunctionTemplate`` will be a managed by
application administrator. For that reason, you need to add a new option on
*Administration / Management* menu.

Class ``CustomMenuController`` is in charge to create options menu which appears
in top part of the application. Then you need to modify method
``initializeMenu()`` in ``CustomMenuController`` to add a new ``subItem`` inside
the ``topItem`` *Administration / Management*::

    subItem(_("Stretches Function Templates"),
        "/planner/stretchesFunctionTemplate.zul",
        "")

This option will link to a new ``.zul`` file that will be interface for
application users in order to manage ``StretchesFunctionTemplate`` entity. When
you click the new entry, LibrePlan will the load ``.zul`` file (but, at this
moment, the link is not going to work as ``.zul`` page does not exist yet).

Main ``.zul`` page
------------------

Then you will create the file ``stretchesFunctionTemplate.zul`` inside
``libreplan-webapp/src/main/webapp/planner/`` folder with the following
content:

::

 <?page id="exceptionDayTypesList" title="${i18n:_('LibrePlan: Stretches Function Templates')}" ?>
 <?init class="org.zkoss.zkplus.databind.AnnotateDataBinderInit" ?>
 <?init class="org.zkoss.zk.ui.util.Composition" arg0="/common/layout/template.zul"?>

 <?link rel="shortcut icon" href="/common/img/favicon.ico" type="image/x-icon"?>
 <?link rel="stylesheet" type="text/css" href="/common/css/libreplan.css"?>
 <?link rel="stylesheet" type="text/css" href="/common/css/libreplan_zk.css"?>

 <?component name="list" inline="true" macroURI="_listStretchesFunctionTemplates.zul"?>
 <?component name="edit" inline="true" macroURI="_editStretchesFunctionTemplate.zul"?>

 <zk>
     <window self="@{define(content)}"
         apply="org.libreplan.web.planner.allocation.streches.StretchesFunctionTemplateCRUDController">
         <vbox id="messagesContainer"/>
         <list id="listWindow"/>
         <edit id="editWindow"/>
     </window>
 </zk>

This file contains a ``.zul`` page which contains a window that has another
window to list (``list``) elements and another for editing them (``edit``).

::

 <?page id=”” title=”${i18n:_('LibrePlan: Exception Days')}” ?>

This line define that the document is a page.

::

 <?init class="org.zkoss.zkplus.databind.AnnotateDataBinderInit" ?>

It is needed because of you are going to use **bindings** in this page.

.. NOTE::

  ``<?init ... ?>`` labels are always the first ones to be evaluated inside a
  page. And they always receive a class as parameter, they instantiate it and
  call its ``init()`` method.

.. ADMONITION:: Data Binding

  A binding is the ability to evaluate a data element (for example, a bean) in
  execution time from a ``.zul`` page. Evaluation, which finally executes a
  method, could be used to get data from the object or modify its properties.

  Usually bindings are used in components like ``Listbox``, ``Grid`` and
  ``Tree``. These components have the possibility to be fed by dynamic data
  (*live-data*). Because these components receive dynamic data, it is not
  possible to determine how many rows are going to be shown before knowing the
  real data. These components allow build a generic row that will be repeated
  for each element in the collection. When component is rendered, bindings are
  evaluated in order to get concrete value. For example::

    <list model="@{controller.elements}" >
        <rows each="" value="">
            <row>
                <label value="@{element.name}" />
            </row>
        </rows>
    </list>

  When component is evaluated, ``controller.getElements()`` will be called and
  a collection of elements will be returned. For each returned element,
  ``element.getName()`` method will be executed, and then value of name
  attribute will be printed as a label.

  Symbols marked with ``@{...}`` are bindings. These expressions will be only
  evaluated if the following directive is included in the ``.zul`` page::

    <?init class="org.zkoss.zkplus.databind.AnnotateDataBinderInit" ?>

::

 <?init class="org.zkoss.zk.ui.util.Composition"
     arg0="/common/layout/template.zul"?>

It is a composition component. ``arg0`` attribute makes reference to a `.zul`
file which is used as layout for current page. In this layout is specified that
a component defined as ``content`` will be inserted. Your page will define a
window marked as ``content``, that will be inserted in ``template.zul`` page.

``apply`` attribute
-------------------

The basis for implementing MVC pattern in ZK is ``apply`` attribute.

Your page defines a component ``Window`` with an ``apply`` attribute assigned::

    <window self="@{define(content)}"
            apply="org.libreplan.web.planner.allocation.streches.StretchesFunctionTemplateCRUDController">

It links this ``Window`` component with a ``.java`` file, thereby the Java class
will be able to access and manipulate components defined inside ``window`` tag.
This class will play controller role for this ``.zul`` page (view).

Communication between view and controller
-----------------------------------------

If you want that ``.zul`` components will be accessible from controller just use
the same identifier in ``.zul`` and Java. For example:

::

 package org.libreplan.web.common;

 ...

 /**
  * Abstract class defining common behavior for controllers of CRUD screens. <br />
  *
  * Those screens must define the following components:
  * <ul>
  * <li>{@link #messagesContainer}: A {@link Component} to show the different
  * messages to users.</li>
  * <li>{@link #listWindow}: A {@link Window} where the list of elements is
  * shown.</li>
  * <li>{@link #editWindow}: A {@link Window} with creation/edition form.</li>
  *
  * @author Manuel Rego Casasnovas <rego@igalia.com>
  */
 @SuppressWarnings("serial")
 public abstract class BaseCRUDController<T extends IHumanIdentifiable> extends
         GenericForwardComposer {
 ...

This matching is automatic and is done by ZK. In order that this works it is
needed that your controller inherits from ``GenericForwarComposer`` (which in
turn extends ``GenericAutowireComposer``, that is the class doing this kind of
"magic").

Thanks to this you will be able to access view from controller, but not the
other way around. If you want to do this you need to define a variable inside
``Window`` component that will contain a reference to controller instance. The
steps to do this are the following ones:

* Your controller will override method ``doAfterCompose``.
* This method receives a component which is the window associated to the
  controller through ``apply`` attribute.
* In ``Window`` you will use ``setAttribute`` method in order to create a
  variable called ``controller`` that will contain a reference to controller.

::

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("controller", this);

        ...
    }

After that from ``.zul``, you will make reference to a variable called
``controller`` (either from a binding or in order to execute any method when an
event is dispatched). In this way you could see that view can also access to
controller. For example with the following lines::

    <!-- Call method getStretchesFunctionTemplates from view -->
    <list model="@{controller.stretchesFunctionTemplates}">

    <!-- When a button is clicked call method goToEditForm() -->
    <button onClick="controller.goToEditForm()" />

As you can see in last example, when an event is launched is not needed to use
data binding.

``BaseCRUDController`` is a generic class with common behaviour for controllers
of CRUD screens. It defines a set of methods with a common functionality and
delegates on some abstract methods that should be implemented in the subclasses.

For this example you will create a new controller
``StretchesFunctionTemplateCRUDController`` as a subclass of
``BaseCRUDController``.

::

 package org.libreplan.web.planner.allocation.streches;

 ...

 /**
  * CRUD controller for {@link StretchesFunctionTemplate}.
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 public class StretchesFunctionTemplateCRUDController extends
         BaseCRUDController<StretchesFunctionTemplate> {

 ...


ZK macro components
-------------------

Your page ``stretchesFunctionTemplate.zul`` defines two macro components:
``list`` and ``edit``. These macro components implement list view and
edit/creation view respectively.

::

 <?component name="list" inline="true" macroURI="_listStretchesFunctionTemplates.zul"?>

This line declares a macro component called ``list`` associated to page
``_listStretchesFunctionTemplates.zul``. ``inline`` attribute indicates that the
macro component is on the same scope as the component which contains it, i.e.,
``window`` component could see ``list`` component and the other way around.
Inside the same scope or namespace there can not be repeated identifiers (``id``
attributes). However, ``window`` component creates a new namespace. Inside
different namespaces identifiers could be repeated.

Another consequence is that from the main window, which is associated with
controller, you can not access components defined in ``list`` or ``edit``. For
example, ``list`` contains a ``Grid`` called
``listStretchesFunctionTemplates``::

 public class StretchesFunctionTemplateCRUDController extends
         BaseCRUDController<StretchesFunctionTemplate> {

     ...

     private Grid listStretchesFunctionTemplates;

     @Override
     public void doAfterCompose(Component comp) throws Exception {
         ...
         listStretchesFunctionTemplates.getModel();
     }

     ...

Access to ``listStretchesFunctionTemplates`` will cause a
``NullPointerException``, because of ``listStretchesFunctionTemplates`` is not
in main window namespace. But, you could access indirectly to component from
controller through ``list`` component, because this is accessible from
controller. For example::

 public class StretchesFunctionTemplateCRUDController extends
         BaseCRUDController<StretchesFunctionTemplate> {

     ...

     private Grid listStretchesFunctionTemplates;

     @Override
     public void doAfterCompose(Component comp) throws Exception {
         ...
         listStretchesFunctionTemplates = (Grid) listWindow
                 .getFellowIfAny("listStretchesFunctionTemplates");
         listStretchesFunctionTemplates.getModel();
     }

     ...

Another important issue when implementing CRUD use cases is that general view
contains both ``list`` and ``edit`` component. These components are rendered
and shown when page is loaded. Class ``OnlyOneVisible`` is used in controller to
manage which one will be visible at a given time. You can find the following
pieces of code in ``BaseCRUDController``::

     private OnlyOneVisible visibility;

     ...

     private OnlyOneVisible getVisibility() {
         if (visibility == null) {
             visibility = new OnlyOneVisible(listWindow, editWindow);
         }
         return visibility;
     }

     /**
      * Show list window and reload bindings
      */
     protected void showListWindow() {
         getVisibility().showOnly(listWindow);
         Util.reloadBindings(listWindow);
     }

     /**
      * Show edit form with different title depending on controller state and
      * reload bindings
      */
     protected void showEditWindow() {
         getVisibility().showOnly(editWindow);
         updateWindowTitle();
         Util.reloadBindings(editWindow);
     }

And at the end of ``doAfterCompose`` method there is a call to
``showListWindow``, that shows the list view and use ``OnlyOneVisible`` class
to hide edit/creation form.


Messages for users
------------------

::

         <vbox id="messagesContainer"/>

Defines a container to show messages to users. These messages usually appear in
the top of current window inside a box. There is a default implementation in a
class called ``MessagesForUser`` which is used in all controllers to show
messages to users in a similar way for the whole application.

Apart from previous line on ``.zul`` file you will see the following lines
inside ``doAfterCompose`` method in ``BaseCRUDController``::

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    ...

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        ...
        messagesForUser = new MessagesForUser(messagesContainer);
        ...
    }

These lines instantiate a new object of ``MessagesForUser`` class using the
container defined at ``.zul`` page. Then when you want to notify or show a
message to the users you will use some method defined at ``IMessagesForUser``.
For example::

            messagesForUser.showMessage(Level.INFO,
                    _("Stretches function template saved"));


List view
---------

For the moment you just have the code needed for the main page
``stretchesFunctionTemplate.zul``. At this point you are going to create the
list view interface in a file called ``_listStretchesFunctionTemplates.zul``
(in the same folder than main page file
``libreplan-webapp/src/main/webapp/planner/``). This file will have the
following content:

::

  <window id="${arg.id}" title="${i18n:_('Stretches Function Templates List')}">

     <grid id="listStretchesFunctionTemplates"
         model="@{controller.stretchesFunctionTemplates}"
         mold="paging" pageSize="10" fixedLayout="true">

         <columns>
             <column label="${i18n:_('Name')}" sort="auto(lower(name))" />
             <column label="${i18n:_('Operations')}" />
         </columns>
         <rows>
            <row self="@{each='stretchesFunctionTemplate'}"
                value="@{stretchesFunctionTemplate}">
                <label value="@{stretchesFunctionTemplate.name}" />
                 <!-- Operations -->
                 <hbox>
                     <button sclass="icono" image="/common/img/ico_editar1.png"
                         hoverImage="/common/img/ico_editar.png"
                         tooltiptext="${i18n:_('Edit')}"
                         onClick="controller.goToEditForm(self.parent.parent.value)"/>
                     <button sclass="icono" image="/common/img/ico_borrar1.png"
                         hoverImage="/common/img/ico_borrar.png"
                         tooltiptext="${i18n:_('Delete')}"
                         onClick="controller.confirmDelete(self.parent.parent.value)"/>
                 </hbox>
             </row>
         </rows>
     </grid>

     <button label="${i18n:_('Create')}" onClick="controller.goToCreateForm()"
         sclass="create-button global-action"/>

  </window>

In the next paragraphs different parts of the file will be reviewed.

::

     <grid id="listStretchesFunctionTemplates"
         model="@{controller.stretchesFunctionTemplates}"

``Grid`` is a visual ZK component with some sorting features. As you can see,
``model`` attribute is set, which means that a method called
``getStretchesFunctionTemplates`` in controller will be called. This method
will have the responsibility to communicate with model layer in order to get the
list of ``StretchesFunctionTemplate`` from database.

::

             <column label="${i18n:_('Name')}" sort="auto(lower(name))" />

Thanks to this custom component you are able to define that *Name* column will
by sorted by default in ascending order.

::

             <row self="@{each='stretchesFunctionTemplate'}"
                value="@{stretchesFunctionTemplate}">

With this line you are doing 2 different things:

* Define a variable to represent each instance in the collection defined at
  ``model`` attribute. It uses ``self`` for this and set the name
  ``stretchesFunctionTemplate`` that will only be seen by this component and its
  children.
* Set value for ``Row`` to current ``StretchesFunctionTemplate`` being iterated.
  This will allow to access associated entity for each row in the list.

::

                 <label value="@{stretchesFunctionTemplate.name}" />

This line will access to ``name`` attribute for entity
``StretchesFunctionTemplate`` and show it as a label.

::

                     <button sclass="icono" image="/common/img/ico_editar1.png"
                         hoverImage="/common/img/ico_editar.png"
                         tooltiptext="${i18n:_('Edit')}"
                         onClick="controller.goToEditForm(self.parent.parent.value)"/>

An edit button is added for each row, and ``onClick`` event is associated with a
call to some method in the controller. In this case the method called is
``goToEditForm`` and argument is the ``StretchesFunctionTemplate`` associated
with current row. In order to access to the entity go to parent components till
``Row`` and get value there. There is also a delete button with similar
implementation.

::

     <button label="${i18n:_('Create')}" onClick="controller.goToCreateForm()"
         sclass="create-button global-action"/>

The last part is another button which will call a different method on controller
in order to show create form for a new ``StretchesFunctionTemplate`` entity.

To sum up, this ``.zul`` file will create a very simple list with the name of
each ``StretchesFunctionTemplate`` and buttons to edit or remove items in each
row. And also adds another button which will allow to create new entities.


Edit/Create view
----------------

Now you are going to create a file called
``_editStretchesFunctionTemplate.zul``, this file defines the form to create and
edit ``StretchesFunctionTemplate`` entities. It is used for both creation and
edition process. The file will have the following content:

::

 <window id="${arg.id}">
     <caption id="caption" sclass="caption-title" />
     <tabbox>
         <tabs>
             <tab label="${i18n:_('Edit')}" />
         </tabs>
         <tabpanels>
             <tabpanel>
                 <grid fixedLayout="true">
                     <columns>
                         <column width="200px" />
                         <column />
                     </columns>
                     <rows>
                         <row>
                             <label value="${i18n:_('Name')}" />
                             <textbox id="tbName"
                                 value="@{controller.stretchesFunctionTemplate.name}"
                                 width="300px"
                                 onBlur="controller.updateWindowTitle()" />
                         </row>
                     </rows>
                 </grid>

                 <groupbox closable="false">
                     <caption label="${i18n:_('Stretches')}" />
                     <vbox>
                         <hbox align="center">
                             <label value="${i18n:_('New stretch:')}" />
                             <label value="${i18n:_('Duration Percentage')}" />
                             <intbox id="durationPercentage" width="50px"
                                 value="0" onOK="controller.addStretchTemplate();" />
                             <label value="${i18n:_('Progress Percentage')}" />
                             <intbox id="progressPercentage" width="50px"
                                 value="0" onOK="controller.addStretchTemplate();" />
                             <button id="add_new_stretch_template" label="${i18n:_('Add')}"
                                 onClick="controller.addStretchTemplate();" />
                         </hbox>
                     </vbox>
                     <grid id="stretchTemplates"
                         model="@{controller.stretchTemplates}"
                         rowRenderer="@{controller.stretchTemplatesRenderer}"
                         mold="paging" pageSize="10" fixedLayout="true">
                         <columns>
                             <column label="${i18n:_('Duration Percentage')}" />
                             <column label="${i18n:_('Progress Percentage')}" />
                             <column label="${i18n:_('Operations')}" />
                         </columns>
                     </grid>
                 </groupbox>

             </tabpanel>
         </tabpanels>
     </tabbox>

     <!-- Control buttons -->
     <button onClick="controller.saveAndExit()"
         label="${i18n:_('Save')}"
         sclass="save-button global-action" />
     <button onClick="controller.saveAndContinue()"
         label="${i18n:_('Save and Continue')}"
         sclass="save-button global-action" />
     <button onClick="controller.cancelForm()"
         label="${i18n:_('Cancel')}"
         sclass="cancel-button global-action" />

 </window>

Now, let's take a look to the most important parts of the file.

::

                             <label value="${i18n:_('Name')}" />
                             <textbox id="tbName"
                                 value="@{controller.stretchesFunctionTemplate.name}"
                                 width="300px"
                                 onBlur="controller.updateWindowTitle()"  />

This will create a ``Textbox`` field in the form. As you can see, it is using
data bindings, which means that different methods will be automatically called
for get and set ``name`` attribute of entity.

In this case, first method ``getStretchesFunctionTemplate`` in controller will
be called, which will return current entity being edited or created. Then
method ``getName`` or ``setName`` of entity will be called as appropriate.

::

                             <label value="${i18n:_('New stretch:')}" />
                             <label value="${i18n:_('Duration Percentage')}" />
                             <intbox id="durationPercentage" width="50px"
                                 value="0" onOK="controller.addStretchTemplate();" />
                             <label value="${i18n:_('Progress Percentage')}" />
                             <intbox id="progressPercentage" width="50px"
                                 value="0" onOK="controller.addStretchTemplate();" />
                             <button id="add_new_stretch_template" label="${i18n:_('Add')}"
                                 onClick="controller.addStretchTemplate();" />

In order to define new ``StretchTemplate`` for current entity, some fields are
added. Two ``Intbox`` fields and a button, all of them associated to
``addStretchTemplate`` method in controller that will be called to perform the
operation.

::

                    <grid id="stretchTemplates"
                        model="@{controller.stretchTemplates}"
                        rowRenderer="@{controller.stretchTemplatesRenderer}"
                        mold="paging" pageSize="10" fixedLayout="true">
                        <columns>
                            <column label="${i18n:_('Duration Percentage')}" />
                            <column label="${i18n:_('Progress Percentage')}" />
                            <column label="${i18n:_('Operations')}" />
                        </columns>
                    </grid>

List of ``StretchTemplate`` will be shown inside a ``Grid``. You define
``model`` just like in ``_listStretchesFunctionTemplates.zul`` but for
``StretchTemplate`` entities in this case. However, you are not using ``Row``
elements, instead,  you are setting ``rowRenderer`` attribute that will
call to a method in controller. This method will return a ``RowRenderer`` that
will know how to show information about a ``StretchTemplate``.


Conversation model
==================

Model always contains state variables which are being modified by use case. For
example, model for CRUD use case, that is going to allow manage
``StretchesFunctionTemplate`` entities, will have a **conversation state** with
the current entity being created or edited. The series of steps that modify the
entity are called **conversation**.

Every conversation has a starting point and an ending one. Class ``XXXModel`` is
in charge of implement the conversation. Similar to what happens in DAOs case,
models will always implement an interface ``IXXXModel``, which will define
conversation steps. In LibrePlan there are some kind of naming conventions in
order to implement conversations.

.. ADMONITION:: Conversation naming conventions

  In order to name the steps of a conversation it is recommended to use the
  following conventions:

  * If there is only one operation which starts the conversation, then name
    ``init`` should be used (e.g. ``IXXXModel::init``). If conversation can
    start with different operations, names will be prefixed with ``init`` (e.g.
    ``IXXXModel::initCreate``, ``IXXXModel::initEdit``, etc.).

  * If there is only one operation to successfully finish conversation, then
    name ``confirm`` should be used (e.g. ``IXXXModel::confirm``). If it is
    possible to end conversation successfully with different operations, names
    will be prefixed with ``confirm`` (e.g. ``IXXXModel::confirmSave``,
    ``IXXXModel::confirmRemove``, etc.).

  * Operation to cancel changes will be called ``cancel`` (e.g.
    ``IXXXModel::cancel``).

Usually when defining models you should add documentation about conversation
protocol:

Conversation state
  Entity (or entities) being manipulated in the conversation. In some cases
  other different objects will be kept in memory if needed.

Non conversational steps (or independent steps)
  Specify operations not involved in conversation.

Conversation protocol
  * Initial step: Indicates (exclusive) operations which allow start a
    conversation.
  * Intermediate steps: Specify methods that are invoked once the conversation
    is started and before the end step is executed.
  * End step: Set of (exclusive) operations which finish the conversation.

The application uses ``session-per-request-with-detached-objects`` pattern, it
is a way to implement the conversation model. This is usually valid for
applications that extract data from a database, user make some operations with
this data (*think-time*), and after that they are stored in database.

In the project when you start an edition conversation, you will retrieve an
entity from database (through DAO) and keep it in memory as state variable in
model (conversation state). This variable will be detached, i.e., a stored
variable that is not inside a Hibernate session). Hibernate allows to modify
detached entity out of a session. But, after that, it should be needed to open a
transaction in order to store entity on database.

You should be careful working with detached objects because of you could easily
get errors like: ``ObjectNotBoundInSession``, ``LazyInitializationException``
(trying to access a entity marked as lazy) or ``DuplicateSessionInObject`` (two
objects of same instance in the same session).

On the contrary, ``session-per-conversation`` pattern always keep Hibernate
session open, so there will be no objects with detached state. This pattern is
suitable for applications with low *think-time*.


Communication between model and controller
------------------------------------------

Following the approach explained before, in this use case you are going to
have a model ``StretchesFunctionTemplateModel`` and its interface called
``IStretchesFunctionTemplateModel``. That means that you will have two new files
inside
``libreplan-webapp/src/main/java/org/libreplan/web/planner/allocation/streches/``
folder:

* ``IStretchesFunctionTemplateModel.java``::

    package org.libreplan.web.planner.allocation.streches;

    ...

    public interface IStretchesFunctionTemplateModel {
        ...
    }

* ``StretchesFunctionTemplateModel.java``::

    package org.libreplan.web.planner.allocation.streches;

    ...

    @Service
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @OnConcurrentModification(goToPage = "/planner/stretchesFunctionTemplate.zul")
    public class StretchesFunctionTemplateModel implements
            IStretchesFunctionTemplateModel {
        ...
    }

As you can see, model is a Spring bean, in order that controller communicates
with model, you need to do two different things:

* Add the following line at ``.zul`` page (this is not really needed because of
  this line is already in ``template.zul``)::

    <?variable-resolver class="org.zkoss.zkplus.spring.DelegatingVariableResolver"?>

* Add a new ``IStretchesFunctionTemplateModel`` type attribute in controller
  class (``StretchesFunctionTemplateCRUDController``). This attribute must be
  called ``stretchesFunctionTemplateModel``, because of ``variable-resolver``
  will get the object from Spring based on name::

    private IStretchesFunctionTemplateModel stretchesFunctionTemplateModel;

This is the way provided by ZK to do something similar to dependency injection,
in order to use model from controller (which is not inside Spring context). This
is why ``@Autowired`` is not needed, but on the other hand you need to use a
specific name for variable.

.. NOTE::

  Model classes are defined with prototype scope::

    @Scope(BeanDefinition.SCOPE_PROTOTYPE)

  The reason is that models are going to keep conversation state in a variable,
  so in that case new instance are going to be needed every time model is used.


Developing the conversation
---------------------------

At this point you are going to start to develop controller and model in order to
implement the use case.

Non conversational step
.......................

For example you could start to work in the list view, if you review
``_listStretchesFunctionTemplates.zul`` code you will see that method
``getStretchesFunctionTemplates`` in controller is going to be called.
Implementation for this method is usually simple and similar to the next
example.

* ``StretchesFunctionTemplateCRUDController``::

    public List<StretchesFunctionTemplate> getStretchesFunctionTemplates() {
        return stretchesFunctionTemplateModel.getStretchesFunctionTemplates();
    }

* ``IStretchesFunctionTemplateModel``::

    /*
     * Non conversational steps
     */

    List<StretchesFunctionTemplate> getStretchesFunctionTemplates();

* ``StretchesFunctionTemplateModel``::

    @Override
    @Transactional(readOnly = true)
    public List<StretchesFunctionTemplate> getStretchesFunctionTemplates() {
        return stretchesFunctionTemplateDAO.getAll();
    }

As you can see, you need to use ``@Transactional`` annotation in
``getStretchesFunctionTemplates`` method. This is needed in order to access DAO
object inside Hibernate session in order to get entities from database. If you
just need to query data, like in this case, you should mark transaction as read
only (``@Transactional(readOnly = true)``). Moreover,  method
``getStretchesFunctionTemplates`` in model is not involved in conversation
protocol.

On the other hand, you will also need to implement ``getAll`` method in DAO that
would be quite simple::

    @Override
    public List<StretchesFunctionTemplate> getAll() {
        return list(StretchesFunctionTemplate.class);
    }

Conversational steps
....................

Now you are going to implement the form to create a new
``StretchesFunctionTemplate``. As you can see in the ``.zul`` page, the method
called in order to create a new entity is ``goToCreateForm``. This method will
start the conversation between controller and model, and it's already
implemented in ``BaseCRUDController``::

    /**
     * Show edit form with different title depending on controller state and
     * reload bindings
     */
    protected void showEditWindow() {
        getVisibility().showOnly(editWindow);
        updateWindowTitle();
        Util.reloadBindings(editWindow);
    }

    ...

    /**
     * Show create form. Delegate in {@link #initCreate()} that should be
     * implemented in subclasses.
     */
    public final void goToCreateForm() {
        state = CRUDControllerState.CREATE;
        initCreate();
        showEditWindow();
    }

    /**
     * Performs needed operations to initialize the creation of a new entity.
     */
    protected abstract void initCreate();

.. NOTE::

  Method ``Util::reloadBindings`` forces reload of bindings used in a component.
  For example, this is needed to refresh a list of items when some of them are
  added or removed.

This method delegates in ``initCreate`` that should be implemented in
``StretchesFunctionTemplateCRUDController``. Moreover it opens ``editWindow``
and then reload information in the form.

Implementation of ``initCreate`` in ``StretchesFunctionTemplateConverter``::

    @Override
    protected void initCreate() {
        stretchesFunctionTemplateModel.initCreate();
    }

Then this method calls to ``initCreate`` in model to start the conversation.
Then you need to add the following lines in model (remember to create method in
interface too)::

    /**
     * Conversation state
     */
    private StretchesFunctionTemplate stretchesFunctionTemplate;

    ...

    /*
     * Initial conversation steps
     */

    @Override
    public void initCreate() {
        this.stretchesFunctionTemplate = StretchesFunctionTemplate.create("");
    }

Thanks to the first line, model will keep in memory current entity being created
or edited. As you can see in the method, a new instance of the entity is created
and assigned to state variable.

As you are using data bindings for ``StretchesFunctionTemplate`` name, then when
user modify this field, attribute ``name`` in entity will be automatically set.

In order to allow users add new ``StretchTemplate`` you need to implement method
``addStretchTemplate`` in controller. As usual this method delegates in model in
oder to perform the real operation. You need to override ``doAfterCompose`` at
``StretchesFunctionTemplateCRUDController`` in order to be able to access input
elements in the form and create the new method::

    private Grid stretchTemplates;
    private Intbox durationPercentage;
    private Intbox progressPercentage;

    ...

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        stretchTemplates = (Grid) editWindow.getFellow("stretchTemplates");
        durationPercentage = (Intbox) stretchTemplates
                .getFellow("durationPercentage");
        progressPercentage = (Intbox) stretchTemplates
                .getFellow("progressPercentage");
    }

    ...

    public static BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public void addStretchTemplate() {
        BigDecimal duration = BigDecimal.valueOf(durationPercentage.getValue())
                .divide(HUNDRED);
        BigDecimal progress = BigDecimal.valueOf(progressPercentage.getValue())
                .divide(HUNDRED);
        stretchesFunctionTemplateModel.addStretchTemplate(duration, progress);

        clearStrechTemplateFields();
        Util.reloadBindings(stretchTemplates);
    }

    private void clearStrechTemplateFields() {
        durationPercentage.setValue(0);
        progressPercentage.setValue(0);
    }

In model, you will need to create an intermediate conversation step, that will
modify current ``StretchesFunctionTemplate`` entity adding a new instance of
``StretchTemplate``::

    @Override
    public void addStretchTemplate(BigDecimal durationProportion,
            BigDecimal progressProportion) {
        stretchesFunctionTemplate.addStretch(StretchTemplate.create(
                durationProportion, progressProportion));
    }

Then you need to implement a ``RowRenderer`` in controller to be used in order
to show ``StrechTemplate`` information in the window::

    public RowRenderer getStretchTemplatesRenderer() {
        return new RowRenderer() {
            @Override
            public void render(Row row, Object data) throws Exception {
                final StretchTemplate stretchTemplate = (StretchTemplate) data;

                row.appendChild(new Label(toStringPercentage(stretchTemplate
                        .getDurationProportion())));
                row.appendChild(new Label(toStringPercentage(stretchTemplate
                        .getProgressProportion())));

                row.appendChild(Util.createRemoveButton(new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        stretchesFunctionTemplateModel
                                .removeStretchTemplate(stretchTemplate);
                    }
                }));
            }

            private String toStringPercentage(BigDecimal value) {
                return value.multiply(HUNDRED).toBigInteger().toString() + " %";
            }
        };
    }

You will implement ``RowRenderer`` interface, and add the different components
for each column in ``Row`` element. Moreover, you will create a remove button,
associated with a new method in the model which removes the ``StretchTemplate``.

The last step is to close the conversation successfully or not. For this there
are already several methods in ``BaseCRUDController``::

    /**
     * Save current form and go to list view. Delegate in {@link #save()} that
     * should be implemented in subclasses.
     */
    public final void saveAndExit() {
        try {
            saveCommonActions();
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    /**
     * Common save actions:<br />
     * <ul>
     * <li>Delegate in {@link #beforeSaving()} that could be implemented if
     * needed in subclasses.</li>
     * <li>Use {@link ConstraintChecker} to validate form.</li>
     * <li>Delegate in {@link #save()} that should be implemented in subclasses.
     * </li>
     * <li>Show message to user.</li>
     * </ul>
     *
     * @throws ValidationException
     *             If form is not valid or save has any validation problem
     */
    private void saveCommonActions() throws ValidationException {
        beforeSaving();

        save();

        messagesForUser.showMessage(
                Level.INFO,
                _("{0} \"{1}\" saved", getEntityType(), getEntityBeingEdited()
                        .getHumanId()));
    }

    /**
     * Save current form and continue in edition view. Delegate in
     * {@link #save()} that should be implemented in subclasses.
     */
    public final void saveAndContinue() {
        try {
            saveCommonActions();
            goToEditForm(getEntityBeingEdited());
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    /**
     * Performs additional operations before saving (usually do some checks or
     * generate codes of related entities).
     *
     * Default behavior use {@link ConstraintChecker} to see if
     * {@link #editWindow} is valid, however it could be overridden if needed.
     */
    protected void beforeSaving() throws ValidationException {
        ConstraintChecker.isValid(editWindow);
    }

    /**
     * Performs actions to save current form
     *
     * @throws ValidationException
     *             If entity is not valid
     */
    protected abstract void save() throws ValidationException;

    /**
     * Close form and go to list view. Delegate in {@link #cancel()} that could
     * be implemented in subclasses if needed.
     */
    public final void cancelForm() {
        cancel();
        goToList();
    }

    /**
     * Performs needed actions to cancel edition
     *
     * Default behavior do nothing, however it could be overridden if needed.
     */
    protected void cancel() {
        // Do nothing
    }


Then you will need to implement the following methods in controller. ``cancel``
is not mandatory but here is used to show an example about it::

    @Override
    protected void save() throws ValidationException {
        stretchesFunctionTemplateModel.confirmSave();
    }

    @Override
    protected void cancel() {
        stretchesFunctionTemplateModel.cancel();
    }

``save`` method will call ``confirmSave`` in model and return true if the
operation is properly performed. Then depending if user will stay or not in
current window, a different operation is done. ``cancel`` method again will
delegate in model calling ``cancel``. Then two new methods will appear in
model::

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        stretchesFunctionTemplateDAO.save(stretchesFunctionTemplate);
    }

    @Override
    public void cancel() {
        stretchesFunctionTemplate = null;
    }

As you can see, now ``@Transactional`` is used in ``confirmSave`` method without
read only attribute as this operation is going to store entity on database.

All these steps will carry out a complete conversation in LibrePlan. In this
case this conversation will allow users to create new
``StretchesFunctionTemplate`` entities and store them on database (if they do
not cancel the operation).


Solving issues with detached objects
------------------------------------

As it was already stated, you need to be careful managing **detached objects**.
For example, if you think in edit an already stored
``StretchesFunctionTemplate``, you will have a very similar method to
``goToCreateForm`` in ``BaseCRUDController``::

    /**
     * Show edit form for entity passed as parameter. Delegate in
     * {@link #initEdit(entity)} that should be implemented in subclasses.
     *
     * @param entity
     *            Entity to be edited
     */
    public final void goToEditForm(T entity) {
        state = CRUDControllerState.EDIT;
        initEdit(entity);
        showEditWindow();
    }

    /**
     * Performs needed operations to initialize the edition of a new entity.
     *
     * @param entity
     *            Entity to be edited
     */
    protected abstract void initEdit(T entity);

And the specific implementation in ``StretchesFunctionTemplateCRUDController``::

    @Override
    protected void initEdit(StretchesFunctionTemplate stretchesFunctionTemplate) {
        stretchesFunctionTemplateModel.initEdit(stretchesFunctionTemplate);
    }

Then a new method called ``initEdit`` will appear in model as initial
conversation step. First you could think in create this method as follows::

    @Override
    public void initEdit(StretchesFunctionTemplate stretchesFunctionTemplate) {
        this.stretchesFunctionTemplate = stretchesFunctionTemplate;
    }

In that case you will get a ``LazyInitializationException`` with the following
message:

  .. pull-quote::

    Run-time error: failed to lazily initialize a collection of role:
    org.libreplan.business.planner.entities.StretchesFunctionTemplate.stretches,
    no session or session was closed . Error was registered and it will be fixed as
    soon as possible.

This is because of ``editWindow`` is calling ``getStretchTemplates``, that at
some point will end up calling ``getStretches`` on entity. This collection is
a proxy because by default Hibernate relations are lazy. You have two different
approaches to fix this issue:

a) Add ``@Transactional`` annotation to open Hibernate session, reattach entity
   (i.e. put on session currently detached entity) and navigate the collection
   to avoid proxies::

    @Override
    @Transactional(readOnly = true)
    public void initEdit(StretchesFunctionTemplate stretchesFunctionTemplate) {
        this.stretchesFunctionTemplate = stretchesFunctionTemplate;
        stretchesFunctionTemplateDAO.reattach(this.stretchesFunctionTemplate);
        this.stretchesFunctionTemplate.getStretches().size();
    }

b) Or modify entity mapping to avoid lazy relation::

    <list name="stretches" table="stretch_template" lazy="false">

The option chosen will depend on each specific case and you should select the
more convenient way. If every time you load the entity you are going to access
to relation then changing the mapping will be the best solution. Otherwise, if
you are just going to retrieve entities and show name information in the listing
(as you are doing till this moment) you could prefer not load the relation and
then select the other option.


Testing
=======

LibrePlan uses JUnit_ testing framework, as a tool to check application
behaviour based on **unit tests**. The main classes tested are: entities, models
and DAOs. These tests are executed automatically when project is compiled, thus
allow developers check that their changes do not break other parts of
application.

It is strongly recommended to create test for entities and models, in order to
ensure that business logic is working properly.

.. ADMONITION:: Test Driven Development

  Project developers usually follow, although not strictly, TDD_ while
  programming use cases. The main idea behind TDD is:

  * First write a test to define a expected feature in a class. At this moment,
    this is going to be a failing test.
  * After that, modify class to fulfill requirements specified by test.
    Producing code to pass the test.

An example of unit test
-----------------------

For example, in order to test that defined mapping is right you can define a
test for your DAO. Simply create the following file called
``StretchesFunctionTemplateDAOTest.java`` in
``libreplan-business/src/test/java/org/libreplan/business/test/planner/daos/``:

::

 package org.libreplan.business.test.planner.daos;

 ...

 @Transactional
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
         BUSINESS_SPRING_CONFIG_TEST_FILE })
 public class StretchesFunctionTemplateDAOTest {

     @Autowired
     private IStretchesFunctionTemplateDAO dao;

     private StretchesFunctionTemplate stretchesFunctionTemplate;

     private void givenValidStretchesFunctionTemplate() {
         stretchesFunctionTemplate = StretchesFunctionTemplate
                 .create("stretches-function-template-name-"
                 + UUID.randomUUID());
         stretchesFunctionTemplate.addStretch(StretchTemplate.create(
                 new BigDecimal(0.25), new BigDecimal(0.1)));
         stretchesFunctionTemplate.addStretch(StretchTemplate.create(
                 new BigDecimal(0.75), new BigDecimal(0.9)));
     }

     @Test
     public void afterSavingAStretchesFunctionTemplateItExists() {
         givenValidStretchesFunctionTemplate();
         dao.save(stretchesFunctionTemplate);
         assertTrue(dao.exists(stretchesFunctionTemplate.getId()));
     }

 }

As you can see, you need some Spring annotations to run test inside a Spring
context in order to be able to use ``@Autowired`` for different Spring beans, in
that case the DAO class.

Methods annotated with ``@Test`` will be the ones executed in order to check
different things with methods like ``assertTrue``.


Validation
----------

In all applications you usually need to validate different data in several
situations. In order to avoid duplicate validations in different layers,
validation logic should take place in domain model. LibrePlan uses `Hibernate
Validator`_ for this task.

Basic validations
.................

Entities should be in charge to validate themselves, which means that some
validations should be done in entities. For example,
``StretchesFunctionTemplate`` needs to have a name, then you will add following
annotation::

    @NotEmpty(message = "name not specified or empty")
    public String getName() {
        return name;
    }

.. NOTE::

  The different validation annotations like ``@NotNull``, ``@NotEmpty``,
  ``@Valid``, etc. should be in ``getXXX`` methods, instead of variables, in
  order to avoid proxies when trying to validate entities, because of lazy
  initialization in Hibernate.

Then you could add the following test to check that
``StretchesFunctionTemplate`` without name are not stored in database::

    @Test(expected = ValidationException.class)
    public void tryToSaveStretchesFunctionTemplateWithoutName() {
        stretchesFunctionTemplate = StretchesFunctionTemplate.create("");
        dao.save(stretchesFunctionTemplate);
    }

As you can see here, it is being checked that a ``ValidationException`` is
thrown when it is trying to store an entity with empty name.

Validating related entities
...........................

Let's go a bit further and try to also validate ``StretchTemplate`` entity,
which is used by ``StretchesFunctionTemplate``, in order to check that values
for proportions should be between 0 and 1. Then you could think in define the
following unit test::

    @Test(expected = ValidationException.class)
    public void tryToSaveStretchesFunctionTemplateWithoutNullStretchTemplate() {
        stretchesFunctionTemplate = StretchesFunctionTemplate
                .create("stretches-function-template-name-" + UUID.randomUUID());
        stretchesFunctionTemplate.addStretch(StretchTemplate.create(
                BigDecimal.TEN, BigDecimal.TEN));
        dao.save(stretchesFunctionTemplate);
    }

If you run this test now it is going to fail as not exception is going to be
thrown. Then you will add ``@Min`` and ``@Max`` annotations to these attributes
in class ``StretchTemplate``::

    @Min(value = 0, message = "duration proportion is one based percentage so it "
            + "should be greater than or equal to 0")
    @Max(value = 1, message = "duration proportion is one based percentage so it "
            + "should be less than or equal to 1")
    public BigDecimal getDurationProportion() {
        return durationProportion;
    }

    @Min(value = 0, message = "progress proportion is one based percentage so it "
            + "should be greater than or equal to 0")
    @Max(value = 1, message = "progress proportion is one based percentage so it "
            + "should be less than or equal to 1")
    public BigDecimal getProgressProportion() {
        return progressProportion;
    }

Anyway, test is going to keep failing and you are not getting any
``ValidationException`` yet. This is because of relations are not automatically
navigated during validation, you need to manually specify ``@Valid`` annotation
in order to also validate depending entities. So, you just need to modify
``StretchesFunctionTemplate`` to add the annotation and then test would be
successfully passed::

    @Valid
    public List<StretchTemplate> getStretches() {
        return Collections.unmodifiableList(stretches);
    }

Complex validations
...................

Sometimes you need more complex validations than simply check if a field is
empty or it has some value, in this case you will have to use ``@AssertTrue``
annotation. There is a convention in LibrePlan for methods annotated with
``@AssertTrue`` that names should start with ``checkConstraint`` prefix.

For example, maybe you want to check that inside a ``StretchesFunctionTemplate``
different ``StretchTemplate`` are correlative. E.g. if you have a stretch with
duration 20% and progress 50%, next stretch should have a greater or equal
progress; then a new stretch with duration 40% and progress 30% is not valid it
should be at least 50% of progress or a greater value.

Then if you follow TDD, you could add a new test to check if this issue is being
properly validated::

    @Test(expected = ValidationException.class)
    public void checkStretchesProgressOrder() {
        stretchesFunctionTemplate = StretchesFunctionTemplate
                .create("stretches-function-template-name-" + UUID.randomUUID());
        stretchesFunctionTemplate.addStretch(StretchTemplate.create(
                new BigDecimal(0.20), new BigDecimal(0.50)));
        stretchesFunctionTemplate.addStretch(StretchTemplate.create(
                new BigDecimal(0.40), new BigDecimal(0.30)));
        dao.save(stretchesFunctionTemplate);
    }

In order to implement this behaviour you will add following method in
``StretchesFunctionTemplate`` entity::

    @AssertTrue(message = "Some stretch has less progress value than the "
            + "previous stretch")
    public boolean checkConstraintStretchesProgressOrder() {
        if (stretches.isEmpty()) {
            return true;
        }

        sortStretchesByDuration();

        Iterator<StretchTemplate> iterator = stretches.iterator();
        StretchTemplate previous = iterator.next();
        while (iterator.hasNext()) {
            StretchTemplate current = iterator.next();
            if (current.getProgressProportion().compareTo(
                    previous.getProgressProportion()) <= 0) {
                return false;
            }
            previous = current;
        }

        return true;
    }

    private void sortStretchesByDuration() {
        Collections.sort(stretches, new Comparator<StretchTemplate>() {
            @Override
            public int compare(StretchTemplate o1, StretchTemplate o2) {
                return o1.getDurationProportion().compareTo(
                        o2.getDurationProportion());
            }
        });
    }

At this moment, when a ``StretchesFunctionTemplate`` entity is stored on
database, this constraint will be checked in order to avoid save wrong data.

.. WARNING::

  The project uses a special approach for validating objects when saving, it is
  defined in ``GenericDAOHibernate``::

    /**
     * It's necessary to save and validate later.
     *
     * Validate may retrieve the entity from DB and put it into the Session, which can eventually lead to
     * a NonUniqueObject exception. Save works here to reattach the object as well as saving.
     */
    public void save(E entity) throws ValidationException {
        getSession().saveOrUpdate(entity);
        entity.validate();
    }

  As you can see, before validating the entity the application saves it and then
  checks that all validations run successfully. This could lead to some
  "strange" results while developing test.

  For example, if you are testing that a value could not be ``null`` and it is
  defined with a ``not-null`` constraint in database mapping. You will add
  ``@NotNull`` annotation and create a test expecting a ``ValidationException``.
  However, as LibrePlan is not able to store in database the entity (because of
  a database constraint) you will always get a
  ``DataIntegrityViolationException``.

Interface validations
---------------------

Even, when it is already stated that validations have to be done in domain
entities, in order to check business logic in proper layer and avoid possible
issues because of wrong data is stored on database. It is also possible to
replicate some of these validations in view layer, in order to show to users
better error messages and prevent them to send invalid data to server.

ZK provides an easy way to add constraints to form fields. For example, in
``StretchesFunctionTemplate`` entity name can not be empty so you could add the
following validation on ``_editStretchesFunctionTemplate.zul`` file::

                            <label value="${i18n:_('Name')}" />
                            <textbox id="tbName"
                                value="@{controller.stretchesFunctionTemplate.name}"
                                width="300px"
                                onBlur="controller.updateWindowTitle()"
                                constraint="no empty:${i18n:_('cannot be empty')}" />

Now, if users set an empty name, they will receive an error in a pop-up. However,
if they click *Save* button, the request to sever will be sent and then they
will get validations errors due to Hibernate constraints.

In order to do not follow with the conversation when user has not filled the
right data ``BaseCRUDController`` uses ``ConstraintChecker`` utility in default
``beforeSaving`` method::

    /**
     * Performs additional operations before saving (usually do some checks or
     * generate codes of related entities).
     *
     * Default behavior use {@link ConstraintChecker} to see if
     * {@link #editWindow} is valid, however it could be overridden if needed.
     */
    protected void beforeSaving() throws ValidationException {
        ConstraintChecker.isValid(editWindow);
    }


Web services
============

LibrePlan provides **web services** as integration tool for third party
applications that want to get/send data from/to application. Project
implementation to perform this task is based in REST_ (Representational State
Transfer) services with the following behaviour:

* All integration entities will have a code that will allow them to be
  identified for both LibrePlan and third party application. It is important to
  stress that this ``code`` attribute will be different to Hibernate ``id``
  attribute, which is an internal identifier for the database and could be
  repeated in different instances of LibrePlan.

* When the application receive an entity via web service, it follows the next
  steps:

  * Check if entity already exists on database. Using ``code`` attribute for
    this.
  * If entity does not exist, then it is created the new entity and stored on
    database.
  * If entity already exists, then it is modified and changes are stored on
    database.

* Delete operation is not going to be allowed, because of remove some entity
  could take side effects in other schedules done in LibrePlan. Anyway, it is
  possible that some entities provide an attribute to deactivate them in the
  system, this could be changed with a modification operation.

Application entities will be represented as XML files in order to be sent or
received as web service data.

Convert into ``IntegrationEntity``
----------------------------------

A lot of entities in the project can be considered **integration entities**,
i.e. suitable entities to be sent/received to/from other applications. As this
is a common case a new class ``IntegrationEntity`` was defined and all these
entities extends this class instead of ``BaseEntity``. Actually,
``IntegrationEntity`` extends in turn ``BaseEntity``.

For example, as part of this exercise you are going to become
``StretchesFunctionTemplate`` in an integration entity. Even when it could not
have be really needed for the moment, it is useful as a test case in order to
know how to develop a web service in the application.

First of all, you need to make that ``StretchesFunctionTemplate`` inherits from
``IntegrationEntity``::

 public class StretchesFunctionTemplate extends IntegrationEntity implements
         IHumanIdentifiable {
     ...
 }

This fact means that ``StretchesFunctionTemplate`` entity has a new attribute
called ``code``. Thus, you will need to modify Hibernate mapping in
``ResourceAllocations.hbm.xml`` file in order to add the following line::

        <property name="code" access="property" not-null="true" unique="true"/>

And you will need to add a new changeset to Liquibase changelog in
``db.changelog-1.0.xml`` file::

    <changeSet id="add-new-column-code-to-stretches_function_template" author="mrego">
        <comment>Add new column code in table stretches_function_template with not-null constraint</comment>
        <addColumn tableName="stretches_function_template">
            <column name="code" type="VARCHAR(255)" />
        </addColumn>
        <addNotNullConstraint tableName="stretches_function_template"
            columnName="code"
            defaultNullValue=""
            columnDataType="VARCHAR(255)" />
    </changeSet>

.. WARNING::

  This Liquibase changeset is just an example and should not be used as is in
  the real world. The reason is that if there are already
  ``StretchesFunctionTemplate`` entities stored in database, this changeset is
  setting ``code`` attribute to empty, which is wrong as code should be unique.
  This should be fixed using some kind of custom refactorization provided by
  Liquibase, that would generate random codes for currently stored entities.

``IntegrationEntity`` is an abstract class, thus you need to override abstract
method ``getIntegrationEntityDAO``. This method should return DAO of this
entity, that will be used to check that code is not repeated when entity is
validated.

However, before implementing this method you need to modify entity DAO to extend
``IntegrationEntityDAO``. This provides a standard implementation for several
methods in order to check constraints related with ``code`` field. In order to
do this you will need to modify both interface and DAO implementation::

 public interface IStretchesFunctionTemplateDAO extends
         IIntegrationEntityDAO<StretchesFunctionTemplate> {
     ...
 }

 public class StretchesFunctionTemplateDAO extends
         IntegrationEntityDAO<StretchesFunctionTemplate> implements
         IStretchesFunctionTemplateDAO {
     ...
 }

It is very convenient to use these common classes as you will have a lot of
functionalities automatically added to your entity. Now, you are ready to
implement ``getIntegrationEntityDAO`` in the entity. Just one more problem, you
need to know how to access DAO from an entity, when entities are not in a Spring
context. For this purpose a class called ``Registry`` exists in LibrePlan, so
before modifying entity you will add the following lines to ``Registry``::

    @Autowired
    private IStretchesFunctionTemplateDAO stretchesFunctionTemplateDAO;

    public static IStretchesFunctionTemplateDAO getStretchesFunctionTemplateDAO() {
        return getInstance().stretchesFunctionTemplateDAO;
    }

And then you will override ``getIntegrationEntityDAO`` in
``StretchesFunctionTemplate``::

    @Override
    protected IStretchesFunctionTemplateDAO getIntegrationEntityDAO() {
        return Registry.getStretchesFunctionTemplateDAO();
    }

At this moment, your entity ``StretchesFunctionTemplate`` is an integration
entity, so it is valid to implement a web service providing import and export
facilities for this entity.

.. NOTE::

  Integration entities usually will show ``code`` attribute in the interface, in
  order that users could uniquely reference to one entity. Moreover, this code
  usually follows some kind of sequence prefixed with entity name, these
  sequences are managed in *Configuration* window at LibrePlan.

  So, if you want ``StretchesFunctionTemplate`` entity will be a common
  integration entity in the application you will need to do something similar to
  other entities:

  * Add your entity in ``EntityNameEnum``.
  * Modify *Configuration* window in order to allow manage the new sequence for
    your entity.
  * Reuse ``IIntegrationEntityModel`` and ``IntegrationEntityModel`` in your
    model. Those will provide standard methods to generate entity sequence.

  For the moment, as it is not really necessary for this exercise, this part
  will be omitted in this document.

Implement export web service
----------------------------

Now you are going to implement the **export service** for
``StretchesFunctionTemplate`` entity. Thanks to this service, third party
applications could access to the list of ``StretchesFunctionTemplate`` defined
in the system. Web services classes are under
``libreplan-webapp/src/main/java/org/libreplan/ws/`` folder, inside it you
should create a new directory ``stretchesfunctiontemplates`` with two
subdirectories ``api`` and ``impl``.

Again, like in previous point, there are some classes already defined which
provide main functionality needed to implement the web service. You will extends
these classes throughout the sample.

Defining service interface
..........................

First of all, you will create an interface inside ``api`` folder. This interface
will define a method to export all ``StretchesFunctionTemplate`` entities stored
in application database::

 package org.libreplan.ws.stretchesfunctiontemplates.api;

 ...

 public interface IStretchesFunctionTemplateService {

     public StretchesFunctionTemplateListDTO getStretchesFunctionTemplates();

 }

Mapping between entities and XMLs
.................................

As you can see, web service interface uses a DTO_ (Data Transfer Object) class,
as you do not need to export all the business logic managed by entities you will
create lighter classes (DTOs) in order to export and import data associated with
web services.

Then you are going to define all DTOs (inside ``api`` folder), needed for
``StretchesFunctionTemplate`` entity. You will need three DTOs:

* ``StretchesFunctionTemplateListDTO``:

::

 package org.libreplan.ws.stretchesfunctiontemplates.api;

 ...

 @XmlRootElement(name = "stretches-function-template-list")
 public class StretchesFunctionTemplateListDTO {

     @XmlElement(name = "stretches-function-template")
     public List<StretchesFunctionTemplateDTO> stretchesFunctionTemplateDTOs = new ArrayList<StretchesFunctionTemplateDTO>();

     public StretchesFunctionTemplateListDTO() {
     }

     public StretchesFunctionTemplateListDTO(
             List<StretchesFunctionTemplateDTO> stretchesFunctionTemplateDTOs) {
         this.stretchesFunctionTemplateDTOs = stretchesFunctionTemplateDTOs;
     }

 }

* ``StretchesFunctionTemplateDTO``:

::

 package org.libreplan.ws.stretchesfunctiontemplates.api;

 ...

 public class StretchesFunctionTemplateDTO extends IntegrationEntityDTO {

     public final static String ENTITY_TYPE = "stretches-function-template";

     @XmlAttribute
     public String name;

     @XmlElementWrapper(name = "stretches-list")
     @XmlElement(name = "stretch-template")
     public List<StretchTemplateDTO> stretches = new ArrayList<StretchTemplateDTO>();

     public StretchesFunctionTemplateDTO() {
     }

     public StretchesFunctionTemplateDTO(String code, String name,
             List<StretchTemplateDTO> stretches) {
         super(code);
         this.name = name;
         this.stretches = stretches;

     }

     public StretchesFunctionTemplateDTO(String name,
             List<StretchTemplateDTO> stretches) {
         this(generateCode(), name, stretches);
     }

     @Override
     public String getEntityType() {
         return ENTITY_TYPE;
     }

 }

* ``StretchesFunctionTemplateDTO``:

::

 package org.libreplan.ws.stretchesfunctiontemplates.api;

 ...

 public class StretchTemplateDTO {

     @XmlAttribute(name = "duration-proportion")
     public BigDecimal durationProportion;

     @XmlAttribute(name = "progress-proportion")
     public BigDecimal progressProportion;

     public StretchTemplateDTO() {
     }

     public StretchTemplateDTO(BigDecimal durationProportion,
             BigDecimal progressProportion) {
         this.durationProportion = durationProportion;
         this.progressProportion = progressProportion;
     }

 }

In these classes you can see that LibrePlan uses JAXB_ for XML bindings. This
makes really easy mapping between Java classes and XML representations providing
annotations like ``@XmlAttribute``, ``@XmlElement``, etc.

Moreover, you also need a file called ``package-info.java`` in ``api`` folder in
order to define namespace for REST service. This file will have the following
content::

 @javax.xml.bind.annotation.XmlSchema(
     elementFormDefault=javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
     namespace=WSCommonGlobalNames.REST_NAMESPACE
 )
 package org.libreplan.ws.stretchesfunctiontemplates.api;
 import org.libreplan.ws.common.api.WSCommonGlobalNames;

Extending ``GenericRESTService``
................................

Then you are going to implement web service interface with a new class which
will extend ``GenericRESTService``. This class provides generic stuff for
implementing new REST services. So, you are going to create the following class
inside ``impl`` folder this time::

 package org.libreplan.ws.stretchesfunctiontemplates.impl;

 ...

 @Path("/stretchesfunctiontemplates/")
 @Produces("application/xml")
 @Service("stretchesFunctionTemplateServiceREST")
 public class StretchesFunctionTemplateServiceREST extends
         GenericRESTService<StretchesFunctionTemplate, StretchesFunctionTemplateDTO>
         implements IStretchesFunctionTemplateService {

     @Autowired
     private IStretchesFunctionTemplateDAO dao;

     @Override
     protected IIntegrationEntityDAO<StretchesFunctionTemplate> getIntegrationEntityDAO() {
         return dao;
     }

     @Override
     protected StretchesFunctionTemplateDTO toDTO(
             StretchesFunctionTemplate entity) {
         return StretchesFunctionTemplateConverter.toDTO(entity);
     }

     @Override
     protected StretchesFunctionTemplate toEntity(
             StretchesFunctionTemplateDTO entityDTO) throws ValidationException,
             RecoverableErrorException {
         // Not needed for export service
         return null;
     }

     @Override
     protected void updateEntity(StretchesFunctionTemplate entity,
             StretchesFunctionTemplateDTO entityDTO) throws ValidationException,
             RecoverableErrorException {
         // Not needed for export service
     }

     @Override
     @GET
     @Transactional(readOnly = true)
     public StretchesFunctionTemplateListDTO getStretchesFunctionTemplates() {
         return new StretchesFunctionTemplateListDTO(findAll());
     }

 }

Let's split this file in small hunks in order to explain different annotations.
Take into account that the project uses JAX-RS_ (Java API for RESTful Web
Services) to create web services.

::

 @Path("/stretchesfunctiontemplates/")

It is a JAX-RS annotation to indicates the URI for the web service. In the
application it is the entity name in lowercase and plural usually.

::

 @Produces("application/xml")

Another JAX-RS annotation which indicates the media type for a method. In this
case it is used at class level, which means that all methods for this web
service produce XML results. This is true in LibrePlan, as even methods to
import data will return an XML with the list of errors during the operation or
an empty list if it was performed successfully.

::

 @Service("stretchesFunctionTemplateServiceREST")

In this case it is a Spring annotation which indicates that the class is a
service. Then you will need to add it in
``libreplan-webapp-spring-config.xml`` file::

        <jaxrs:serviceBeans>
            ...
            <ref bean="stretchesFunctionTemplateServiceREST"/>
        </jaxrs:serviceBeans>

::

 public class StretchesFunctionTemplateServiceREST extends
         GenericRESTService<StretchesFunctionTemplate, StretchesFunctionTemplateDTO>
         implements IStretchesFunctionTemplateService {

As you can see, new class extends ``GenericRESTService`` an abstract class that
provides common functionality for web services. It also implements web service
interface, where you indicate methods provided by this web service.

::

     @Autowired
     private IStretchesFunctionTemplateDAO dao;

Like this class is marked as a Spring service, you could use ``@Autowired``
annotation to inject DAO class for this entity.

::

     @Override
     protected IIntegrationEntityDAO<StretchesFunctionTemplate> getIntegrationEntityDAO() {
         return dao;
     }

This is an abstract method that you need to implement, it simply returns DAO
class for ``StretchesFunctionTemplate`` entity.

::

     @Override
     protected StretchesFunctionTemplateDTO toDTO(
             StretchesFunctionTemplate entity) {
         return StretchesFunctionTemplateConverter.toDTO(entity);
     }

Another abstract method overridden, in this case it should create a DTO from an
entity. As you can see it delegates the conversion in a special class
``StretchesFunctionTemplateConverter``.

::

     @Override
     protected StretchesFunctionTemplate toEntity(
             StretchesFunctionTemplateDTO entityDTO) throws ValidationException,
             RecoverableErrorException {
         // Not needed for export service
         return null;
     }

Similar to previous method, but on the other way around. This will create an
entity from a DTO. This is used when you are implementing an import service and
you receive new entities. Again, it usually delegates in a converter class.

::

     @Override
     protected void updateEntity(StretchesFunctionTemplate entity,
             StretchesFunctionTemplateDTO entityDTO) throws ValidationException,
             RecoverableErrorException {
         // Not needed for export service
     }

This will be used when you receive an already existent entity in order to update
it from a DTO. Like previous ones, it usually delegates in a converter class.

::

     @Override
     @GET
     @Transactional(readOnly = true)
     public StretchesFunctionTemplateListDTO getStretchesFunctionTemplates() {
         return new StretchesFunctionTemplateListDTO(findAll());
     }

Finally, this is the implementation for the only method provided by web service
interface, which will export all ``StretchesFunctionTemplate`` stored in
the system. Method is marked with ``@GET`` JAX-RS annotation, which indicates
that current method will respond to HTTP ``GET`` requests. Moreover, it is also
needed open a transaction, as it is going to use DAO in ``findAll`` method
implemented by ``GenericRESTService``.

Converting entities to/from DTOs
................................

The last step will be implement the converter class. In this case you will just
need to implement the method to convert from ``StretchesFunctionTemplate``
entity to a DTO.

This is a simply class that will be inside ``impl`` folder and will have the
following content::

 package org.libreplan.ws.stretchesfunctiontemplates.impl;

 ...

 public final class StretchesFunctionTemplateConverter {

     private StretchesFunctionTemplateConverter() {
     }

     public final static StretchesFunctionTemplateDTO toDTO(
             StretchesFunctionTemplate stretchesFunctionTemplate) {
         // Convert stretches
         List<StretchTemplateDTO> stretchTemplateDTOs = new ArrayList<StretchTemplateDTO>();
         for (StretchTemplate each : stretchesFunctionTemplate.getStretches()) {
             stretchTemplateDTOs.add(toDTO(each));
         }

         return new StretchesFunctionTemplateDTO(stretchesFunctionTemplate
                 .getCode(), stretchesFunctionTemplate.getName(), stretchTemplateDTOs);
     }

     private static StretchTemplateDTO toDTO(StretchTemplate stretchTemplate) {
         return new StretchTemplateDTO(stretchTemplate.getDurationProportion(),
                 stretchTemplate.getProgressProportion());
     }

 }

Now you are ready to test your web service. If you go to this URL
http://localhost:8080/libreplan-webapp/ws/rest/stretchesfunctiontemplates/,
and login with a user that has permission to access web services (e.g. user
``wsreader`` with password ``wsreader``) you will get a XML with the list of
``StretchesFunctionTemplate`` stored in the application.

.. NOTE::

  It is recommended to define some JUnit test for each web service in order to
  validate if they are working properly. In this case it is even more important
  as developers could not take into account web services when they are changing
  some entities in the business logic. Thanks to this tests developers will
  detect problems at the exact moment in which they are doing the changes.

  You can take a look to other already existent services in order to create your
  test for the new one that, following the convention, will be called
  ``StretchesFunctionTemplateServiceTest``.

Web services scripts
--------------------

Export services are easily tested just accessing URL with any web browser.
However, in the case of import services you will need to use HTTP ``POST``
method in order to test them. For this reason some convenient scripts were
created in project repository inside ``scripts/rest-clients`` directory.

.. NOTE::

  Currently these scripts recommends Tidy to be installed in your system
  for a better output. You could install them in a Debian based distribution
  with the following command as root::

    apt-get install tidy

Then for this example you will create a script called
``export-stretches-function-templates.sh``, that will be very similar to the
rest of export scripts just changing web service path::

  #!/bin/sh

  . ./rest-common-env.sh

  . ./export.sh stretchesfunctiontemplates $*

Script will request user and password in order to access to web service, so you
could use ``wsreader`` user to check that it works properly.


Conclusion
==========

Proposed exercise, even when not fully resolved, allows navigate through basic
architecture of LibrePlan and know better different elements involved in each
layer.

Project view is implemented with ZK web framework. Views are stored in files
with ``.zul`` extension. It is possible grouping components into each other
and, even, create macro components in order to divide views or reuse components.

Every window is associated with a controller class. Controller contains program
source code needed to interact with user and communicate with business logic
layer. Every ``XXXController`` class will have access to business logic through
a ``XXXModel`` class.

Business logic layer is implemented in ``XXXModel`` classes. Domain entities,
apart from contain data which will be stored, also contain business operations.
The philosophy is that, as far as possible, every domain entity knows himself
and offers operations to other entities through its methods.

Model classes do not access directly to database, but do so through a DAO
(persistence class). There is a DAO for each domain entity. DAO offers different
operations for retrieval, query and store entities. Models are not going to
access to concrete classes, they will use interfaces (dependency injection).

Hibernate is the persistence framework used in the application. There is two
base classes ``GenericDAOHibernate`` and ``BaseEntity`` which encapsulate main
part of Hibernate API. Every DAOs and entities inherit from these two classes
respectively. Every domain entities must to have Hibernate mapping. Mapping is
done in ``.hbm.xml`` files.

LibrePlan uses testing framework JUnit. Moreover, Hibernate Validator is used to
validate business logic. Business logic is always tested and validated in model
layer, where entities are responsible to validate their own data.

Finally, you created an export web service for the new entity. Project
services uses XML APIs provided by Java (like JAXB and JAX-RS) which make easier
developing new web services. For each web service, some test scripts are
provided in order to check implementation.


.. _LibrePlan: http://www.libreplan.com/
.. _CRUD: http://en.wikipedia.org/wiki/Create,_read,_update_and_delete
.. _Hibernate: http://www.hibernate.org/
.. _`Optimistic Locking`: http://en.wikipedia.org/wiki/Optimistic_locking
.. _`LibrePlan wiki`: http://wiki.libreplan.org/
.. _MVC: http://en.wikipedia.org/wiki/Model_view_controller
.. _DAO: http://en.wikipedia.org/wiki/Data_Access_Object
.. _DDD: http://en.wikipedia.org/wiki/Domain_driven_design
.. _Spring: http://www.springsource.org/
.. _`Inversion of control`: http://en.wikipedia.org/wiki/Inversion_of_control
.. _`database refactorings`: http://en.wikipedia.org/wiki/Database_refactoring
.. _Liquibase: http://www.liquibase.org/
.. _ZK: http://www.zkoss.org/
.. _JUnit: http://junit.sourceforge.net/
.. _TDD: http://en.wikipedia.org/wiki/Test_driven_development
.. _`Hibernate Validator`: http://www.hibernate.org/subprojects/validator.html
.. _REST: http://en.wikipedia.org/wiki/Representational_State_Transfer
.. _DTO: http://en.wikipedia.org/wiki/Data_Transfer_Object
.. _JAXB: http://en.wikipedia.org/wiki/Java_Architecture_for_XML_Binding
.. _JAX-RS: http://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services
