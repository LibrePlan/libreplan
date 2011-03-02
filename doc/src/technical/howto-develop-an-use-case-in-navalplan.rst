How To Develop An Use Case In NavalPlan
=======================================

.. sectnum::

:Author: Manuel Rego Casasnovas
:Contact: mrego@igalia.com
:Date: 02/03/2011
:Copyright:
  Some rights reserved. This document is distributed under the Creative
  Commons Attribution-ShareAlike 3.0 licence, available in
  http://creativecommons.org/licenses/by-sa/3.0/.
:Abstract:
  The goal of this document is develop a CRUD_ (create, read, update and delete)
  use case in NavalPlan_. Through carrying out this exercise you will see the
  basic structure of NavalPlan and underlying technology stack.

.. contents:: Table of Contents


Introduction
------------

Use case to be developed consists of create a new entity called
``StretchesFunctionTemplate`` that will be managed from NavalPlan interface.
Summarizing, you will learn how to create a new entity, define an interface to
manipulate it, store it on a database and integrate it with web services.

``StretchesFunctionTemplate`` will be a class to define templates for different
``StretchesFunction`` that are used in advanced allocation window.
``StretchesFunction`` is a kind of assignment function which allow users define
different stretches in order to do resource allocations.

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

  Then NavalPlan will be perform different resource allocations according to
  function defined by user.

Thanks to the new class ``StretchesFunctionTemplate`` users will have the chance
to store repetitive ``StretchesFunction`` that they usually apply while
scheduling. This will allow users to define some kind of patterns for tasks,
where for example they know that always start with a lower load and then it is
increased at the end. They could create a ``StretchesFunctionTemplate`` defining
this behaviour and use it in all the tasks they want.


Domain entities
---------------

First of all you need to create the new entity ``StretchesFunctionTemplate`` in
NavalPlan business layer.

Domain entities encapsulate application business data and part of their logic.
They are Hibernate entities, and therefore are retrieved and stored in a data
warehouse (usually a database). Mapping between Java classes and Hibernate is
done with ``.hbm.xml`` files. For example, file ``ResourceAllocations.hbm.xml``
contains ``StretchesFunction`` class mapping.

All NavalPlan domain entities inherit from ``BaseEntity``. ``BaseEntity`` class
has two attributes: ``id`` and ``version``. ``id`` is mandatory in order to
entity could be considered as an Hibernate entity. ``version`` attribute is used
to impelmement concurrency control method called `Optimistic Locking`_.

.. ADMONITION:: Optimistic Locking

  ``version`` field in entities is used to implement the concurrency control
  method in order to detect concurrency problems during execution.

  Let's imagine 2 users go to edit the same exception day type called "HOLIDAY"
  and both want to modify field ``color``. Currently in database you will have::

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

In NavalPlan domain entities are never instantiated directly, but entities will
expose a static method ``create()`` which will be responsible to return a new
instance. The rest of classes must call ``create()`` method of ``BaseEntity``
when they want to create a new instance of any entity. This is usually
implemented with something similar to the following code::

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

.. WARNING::

  In NavalPlan a lot of entities extends ``IntegrationEntity`` instead of
  ``BaseEntity``, anyway ``IntegrationEntity`` also extends ``BaseEntity``.

  ``IntegrationEntity`` is a base class for all domain entities that are going
  to be available via web services in NavalPlan. These entities have a ``code``
  attribute, which unlike ``id`` is unique among the applications to be
  integrated (``id`` is only unique inside one NavalPlan instance).

In order to know if an object is new or not you will use method
``isNewObject()`` of ``BaseEntity``, you will never directly check if ``id``
attribute is ``null`` (transient entity).

.. ADMONITION:: State of objects in Hibernate

  Transient
    An object out of Hibernate session instantiated with ``new()``. Actually, in
    NavalPlan ``create()`` that calls ``new()`` at some point.

  Persistent
    A persistent entity, already stored on database, which is inside a
    Hibernate session.

  Detached
    A persistent entity out of Hibernate session.

The new entity ``StretchesFunctionTemplate`` will have the following properties:

  * ``name``: A string to identify the template.
  * ``stretches``: A list of ``StretchTemplate`` a new class that will just have
    two attributes: ``durationPercentage`` and ``progressPercentage``.

``StretchTemplate`` will be a value object as every ``StretchTemplate`` will
belong just to one ``StretchesFunctionTemplate`` and would not be modified out
of this relationship. So, in this case ``StretchTemplate`` will not extends
``BaseEntity``.

You will need to create the following files (some excerpts of source code are
shown):

* ``StretchesFunctionTemplate.java``:

::

 package org.navalplanner.business.planner.entities;

 ...

 /**
  * This will store repetitive patterns to be applied in different
  * {@link StretchesFunction}
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 public class StretchesFunctionTemplate extends BaseEntity {

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

* ``StretchTemplate.java``:

::

 package org.navalplanner.business.planner.entities;

 ...

 /**
  * This class is intended as a Hibernate component. It's formed by two
  * components, the duration percentage and the progress percentage. It
  * represents the different values of a {@link StretchesFunctionTemplate}.
  *
  * @author Manuel Rego Casasnovas <mrego@igalia.com>
  */
 public class StretchTemplate {

     public static StretchTemplate create(BigDecimal durationPercentage,
             BigDecimal progressPercentage) {
         return new StretchTemplate(durationPercentage, progressPercentage);
     }

     private BigDecimal durationPercentage = BigDecimal.ZERO;
     private BigDecimal progressPercentage = BigDecimal.ZERO;

     /**
      * Default constructor for Hibernate. Do not use!
      */
     protected StretchTemplate() {
     }

 ...

.. IMPORTANT::

  You should not forget to add license header in your new files specifying the
  license as explained in documentation section at `NavalPlan wiki`_. You can
  copy it from other files and modify year and copyright holder accordingly.

  Moreover, always remember to add, at least, a general comment explaining the
  purpose of your classes.


Model View Controller pattern
-----------------------------

NavalPlan architecture follows MVC_ pattern, which isolates business logic from
user interface allowing separation of different layers in the application. View
and controller will be explained later, now it is time to explain model layer
that is in charge of implement application business or domain logic.

This model layer is formed by different elements. On the one hand, we have
domain entities and DAO_ (Data Access Object) classes which offer methods to
query and store domain objects. On the other hand we have ``XXXModel.java``
files, that are always associated to some controller.

.. ADMONITION:: Domain Driven Design

   NavalPlan follows approach proposed by DDD_. It tries that business logic
   remains encapsulated inside domain classes, a as far as possible, otherwise
   it will be used a model layer.

   The idea is that every domain element will be reposible for itself, which
   means that it knows its business logic and exposes it to other objects
   through methods. Other operations were, for example, several objects are used
   could be written in model layer.

Actually, model classes do not access directly to database but they do through a
DAO object. DAO classes are responsible for retrieve, query and store domain
entities on database, i.e. they implement the persistence layer only accessible
from model.

However, in NavalPlan domain elements can be used directly from view for reading
or modifying its content.


Persistence layer communication
-------------------------------

In order to access domain entities it will always exist a DAO class for each
entity type. This DAO class inherites from ``GenericDAOHibernate``, this class
provides the methods needed to implement common persistence behaviour.

If you want that a model has access to a DAO class, you have to insert an
attribute in your model, for example, a variable called
``tretchesFunctionTemplateDAO`` with type ``IStretchesFunctionTemplateDAO``::

    @Autowired
    private IStretchesFunctionTemplateDAO stretchesFunctionTemplateDAO;

Take into account that this attribute has an interface as type. This interface,
``IStretchesFunctionTemplateDAO``, will have associated an implementation class
called ``StretchesFunctionTemplateDAO``. Spring_ framework is in charge to
inject this implementation class in the variable. In order to this happens, it
is needed to mark the attribute with ``@Autowired`` annotation. This will be
also needed to add some special annotations, interpreted by Spring, at
implementation class.

There also an interface ``IGenericDAOHibernate`` implemented by
``GenericDAOHibernate``.

Then you will have the following files:

* ``IStretchesFunctionTemplateDAO.java``:

::

 package org.navalplanner.business.planner.daos;

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

 package org.navalplanner.business.planner.daos;

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

  In NavalPlan for each DAO class exist an interface class `IXXXDAO`. Models
  always use these interface classes. Spring framework instantiates a class for
  each interface type and injects it in the corresponding variable.

Summarizing, persistence layer encapsulates all operations related to Hibernate
communication for retrieving, querying and storing entities on database.
Therefore, you will not need to use Hibernate API directly in NavalPlan source
code in order to perform operations like: start transaction, commit
transaction, rollback, etc.

Moreover, you need to define Hibernate mapping for the new entity
``StretchesFunctionTemplate``. Like this new entity is related with allocations
you will use ``ResourceAllocations.hbm.xml`` and add the following lines (in
other cases you should look for the proper ``.hbm.xml`` file or just create a
new one if needed)::

    <!-- StretchesFunctionTemplate -->
    <class name="StretchesFunctionTemplate" table="stretches_function_template">
        <id name="id" access="property" type="long">
            <generator class="hilo">
                <param name="max_lo">100</param>
            </generator>
        </id>
        <version name="version" access="property" type="long" />

        <property name="name" access="property" not-null="true" unique="true"/>

        <list name="stretches" table="stretch_template">
            <key column="stretches_function_template_id" />
            <list-index column="stretch_position" />

            <composite-element class="StretchTemplate">
                <property name="durationPercentage" column="duration_percentage"
                    not-null="true" />
                <property name="progressPercentage" column="progress_percentage"
                    not-null="true" />
            </composite-element>
        </list>
    </class>

However, this is not enough in order to store the new entity on database,
because of tables are not created yet. Usually, tables are automatically created
by Hibernate, but this is disabled in NavalPlan, and Hibernates just validates
that database structure matches with mapping specifications in ``hbm.xml``
files. The reason to disable automatic schema creation is in order to have a
proper control over `database refactorings`_, this allows NavalPlan to manage
migrations between databases of different NavalPlan versions. Only testing
database is created automatically in NavalPlan.

Liquibase_ is the tool used to manage these database refactorings. Developers
have to specify via a changelog file the changes to be applied on database when
they modify any mapping. Then you will need to add the following lines in the
proper ``db.changelog-XXX.xml`` file::

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

        <addUniqueConstraint columnNames="name"
            constraintName="stretches_function_template_name_key"
            deferrable="false" disabled="false" initiallyDeferred="false"
            tableName="stretches_function_template"/>

        <createTable tableName="stretch_template">
            <column name="stretches_function_template_id" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="duration_percentage" type="DECIMAL(19,2)">
                <constraints nullable="false"/>
            </column>
            <column name="progress_percentage" type="DECIMAL(19,2)">
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

As you can see this specify the different tables to be created on database and
also some constraints like foreign keys. Usually you can take a look to other
Liquibase changes to know how to create a table or some field. Also a good idea
is to check the result of your changeset against testing database (which is
created automatically), thus you will be sure that your changes are right.



.. _CRUD: http://en.wikipedia.org/wiki/Create,_read,_update_and_delete
.. _NavalPlan: http://www.navalplan.org/en/
.. _`Optimistic Locking`: http://en.wikipedia.org/wiki/Optimistic_locking
.. _`NavalPlan wiki`: http://wiki.navalplan.org/
.. _MVC: http://en.wikipedia.org/wiki/Model_view_controller
.. _DAO: http://en.wikipedia.org/wiki/Data_Access_Object
.. _DDD: http://en.wikipedia.org/wiki/Domain_driven_design
.. _Spring: http://www.springsource.org/
.. _`Inversion of control`: http://en.wikipedia.org/wiki/Inversion_of_control
.. _`database refactorings`: http://en.wikipedia.org/wiki/Database_refactoring
.. _Liquibase: http://www.liquibase.org/
