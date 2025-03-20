LDAP Configuration
##################

.. contents::

This screen allows you to establish a connection with LDAP to delegate
authentication and/or authorization.

It is divided into four different areas, which are explained below:

Activation
==========

This area is used to set the properties that determine how *LibrePlan* uses
LDAP.

If the field *Enable LDAP authentication* is checked, *LibrePlan* will query
LDAP each time a user attempts to log in to the application.

The field *Use LDAP roles* checked means that a mapping between LDAP roles and
LibrePlan roles is established. Consequently, the permissions for a user in
LibrePlan will depend on the roles the user has in LDAP.

Configuration
=============

This section contains the parameter values for accessing LDAP. *Base*, *UserDN*, and
*Password* are parameters used to connect to LDAP and search for users. Therefore,
the specified user must have permission to perform this operation in LDAP. At the
bottom of this section, there is a button to check if an LDAP connection is
possible with the given parameters. It is advisable to test the connection before
continuing the configuration.

.. NOTE::

   If your LDAP is configured to work with anonymous authentication, you can
   leave the *UserDN* and *Password* attributes empty.

.. TIP::

   Regarding *Active Directory (AD)* configuration, the *Base* field must be the
   exact location where the bound user resides in AD.

   Example: ``ou=organizational_unit,dc=example,dc=org``

Authentication
==============

Here, you can configure the property in LDAP nodes where the given username
should be found. The property *UserId* must be filled with the name of the
property where the username is stored in LDAP.

The checkbox *Save passwords in database*, when checked, means that the
password is also stored in the LibrePlan database. In this way, if LDAP is
offline or unreachable, LDAP users can authenticate against the LibrePlan
database. If it is not checked, LDAP users can only be authenticated against
LDAP.

Authorization
=============

This section allows you to define a strategy for matching LDAP roles with
LibrePlan roles. The first choice is the strategy to use, depending on the
LDAP implementation.

Group Strategy
--------------

When this strategy is used, it indicates that LDAP has a role-group strategy.
This means that users in LDAP are nodes that are directly under a branch that
represents the group.

The next example represents a valid LDAP structure for using the group strategy.

* LDAP structure::

   dc=example,dc=org
   |- ou=groups
      |- cn=admins
      |- cn=itpeople
      |- cn=workers
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

In this case, each group will have an attribute, for example, called ``member``,
with the list of users belonging to the group:

* ``cn=admins``:

  * ``member: uid=admin1,ou=people,dc=example,dc=org``
  * ``member: uid=it1,ou=people,dc=example,dc=org``

* ``cn=itpeople``:

  * ``member: uid=it1,ou=people,dc=example,dc=org``
  * ``member: uid=it2,ou=people,dc=example,dc=org``

* ``cn=workers``:

  * ``member: uid=worker1,ou=people,dc=example,dc=org``
  * ``member: uid=worker2,ou=people,dc=example,dc=org``
  * ``member: uid=worker3,ou=people,dc=example,dc=org``

The configuration for this case is as follows:

* Role search strategy: ``Group strategy``
* Group path: ``ou=groups``
* Role property: ``member``
* Role search query: ``uid=[USER_ID],ou=people,dc=example,dc=org``

And, for example, if you want to match some roles:

* Administration: ``cn=admins;cn=itpeople``
* Web service reader: ``cn=itpeople``
* Web service writer: ``cn=itpeople``
* All projects read allowed: ``cn=admins``
* All projects edition allowed: ``cn=admins``
* Project creation allowed: ``cn=workers``

Property Strategy
-----------------

When an administrator decides to use this strategy, it indicates that each user
is an LDAP node, and within the node, there exists a property that represents
the group(s) for the user. In this case, the configuration does not require the
*Group path* parameter.

The next example represents a valid LDAP structure for using the property strategy.

* LDAP structure::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**With Attribute**

In this case, each user will have an attribute, for example, called ``group``,
with the name of the group to which they belong:

* ``uid=admin1``:

  * ``group: admins``

* ``uid=it1``:

  * ``group: itpeople``

* ``uid=it2``:

  * ``group: itpeople``

* ``uid=worker1``:

  * ``group: workers``

* ``uid=worker2``:

  * ``group: workers``

* ``uid=worker3``:

  * ``group: workers``


.. WARNING::

   This strategy has a restriction: each user can belong to only one group.

The configuration for this case is as follows:

* Role search strategy: ``Property strategy``
* Group path:
* Role property: ``group``
* Role search query: ``[USER_ID]``

And, for example, if you want to match some roles:

* Administration: ``admins;itpeople``
* Web service reader: ``itpeople``
* Web service writer: ``itpeople``
* All projects read allowed: ``admins``
* All projects edition allowed: ``admins``
* Project creation allowed: ``workers``

**By User Identifier**

You can even use a workaround to specify LibrePlan roles directly to users
without having an attribute in each LDAP user.

In this case, you will specify which users have the different LibrePlan roles
by ``uid``.

The configuration for this case is as follows:

* Role search strategy: ``Property strategy``
* Group path:
* Role property: ``uid``
* Role search query: ``[USER_ID]``

And, for example, if you want to match some roles:

* Administration: ``admin1;it1``
* Web service reader: ``it1;it2``
* Web service writer: ``it1;it2``
* All projects read allowed: ``admin1``
* All projects edition allowed: ``admin1``
* Project creation allowed: ``worker1;worker2;worker3``

Role Matching
-------------

At the bottom of this section, there is a table with all the LibrePlan roles
and a text field next to each one. This is for matching roles. For instance,
if an administrator decides that the *Administration* LibrePlan role matches
the *admin* and *administrators* roles of LDAP, the text field should contain:
"``admin;administrators``". The character for splitting roles is "``;``".

.. NOTE::

   If you want to specify that all users or all groups have one permission, you
   can use an asterisk (``*``) as a wildcard to refer to them. For example, if
   you want everyone to have the *Project creation allowed* role, you will
   configure the role matching as follows:

   * Project creation allowed: ``*``
