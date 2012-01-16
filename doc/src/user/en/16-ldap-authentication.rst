LDAP Configuration
##################

.. contents::

This screen allows to establish a connection with LDAP to delegate
authentication and/or authorization.

It is divided in four different areas related which are explained below:

Activation
==========

This area is used to set the properties about the way in which *LibrePlan* uses
LDAP.

If the field *Enable LDAP authentication* is checked, *LibrePlan* will look in
LDAP each time an user tries to login in the application.

The field *Use LDAP roles* checked means that a mapping between LDAP roles and
LibrePlan roles is established, so the permissions for an user in LibrePlan
will depend on the roles in LDAP that the user has.

Configuration
=============

This section has the parameter values for accessing LDAP. *Base*, *UserDN* and
*Password* are parameters used to connect to LDAP and search for the users, so
given user must have permission to do that operation in LDAP. At bottom part of
this section there is a button to check if LDAP connection is possible with the
given parameters. It is a good idea to try it before continuing the
configuration.

.. NOTE::

   If your LDAP is configured to work with anonymous authentication you can
   leave empty *UserDN* and *Password* attributes.

Authentication
==============

Here can be configured the property in LDAP nodes where should be found the
given login name. The property *UserId* must be filled with the name of the
property where the login name is stored in LDAP.

The checkbox *Save passwords in database* when it is checked, means that the
password is stored also in LibrePlan database. In this way, if LDAP is offline
or unreachable, LDAP users could authenticate against LibrePlan database. If it
is not checked, LDAP users can only be authenticated against LDAP.

Authorization
=============

This section allows to define an strategy for matching LDAP roles with
LibrePlan roles. In fact, the first choice is the strategy to use, depending on
LDAP implementation.

Group strategy
--------------

When this strategy is used, it means that LDAP has a role-group strategy. It
means that users in LDAP are nodes that hang directly from a branch which
represents the group.

The next example represents a valid LDAP structure to use group strategy.

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

In this case, each group will have an attribute, for example called ``member``,
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

The configuration for this case is the following:

* Role search strategy: ``Group strategy``
* Group path: ``ou=groups``
* Role property: ``member``
* Role search query: ``uid=[USER_ID],ou=people,dc=example,dc=org``

And for example if you want to match some roles:

* Administration: ``cn=admins;cn=itpeople``
* Web service reader: ``cn=itpeople``
* Web service writer: ``cn=itpeople``
* All projects read allowed: ``cn=admins``
* All projects edition allowed: ``cn=admins``
* Project creation allowed: ``cn=workers``

Property strategy
-----------------

When administrator decides to use this strategy, it means that each user is a
LDAP node and in the node exists a property that represents the group(s) for
the user. In this case, the configuration does not need the parameter *Group
path*:

The next example represents a valid LDAP structure to use property strategy.

* LDAP structure::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

With attribute
..............

In this case, each user will have attribute, for example called ``group`` with
the name of the group to which it belongs:

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

   This strategy has a restriction, each user can belong only to one group.

The configuration for this case is the following:

* Role search strategy: ``Property strategy``
* Group path:
* Role property: ``group``
* Role search query: ``[USER_ID]``

And for example if you want to match some roles:

* Administration: ``admins;itpeople``
* Web service reader: ``itpeople``
* Web service writer: ``itpeople``
* All projects read allowed: ``admins``
* All projects edition allowed: ``admins``
* Project creation allowed: ``workers``

By user identifier
..................

You can even have a workaround to specify LibrePlan roles directly to users,
without having an attribute in each LDAP user.

In this case, you will specify which users have the different LibrePlan roles
by ``uid``.

The configuration for this case is the following:

* Role search strategy: ``Property strategy``
* Group path:
* Role property: ``uid``
* Role search query: ``[USER_ID]``

And for example if you want to match some roles:

* Administration: ``admin1;it1``
* Web service reader: ``it1;it2``
* Web service writer: ``it1;it2``
* All projects read allowed: ``admin1``
* All projects edition allowed: ``admin1``
* Project creation allowed: ``worker1;worker2;worker3``

Role matching
-------------

At the bottom of this section there is a table with all the LibrePlan roles and
a text field next to each one. This is for matching roles. For instance, if
administrator decides that *Administration* LibrePlan role matches with *admin*
and *administrators* roles of LDAP, in the text field should appear:
"``admin;administrators``". The character for splitting roles is "``;``".

.. NOTE::

   If you want to specify that all users or all groups have one permission you
   can use an asterisk (``*``) as wildcard to refer to them. For example, if you
   want that everybody has the role *Project creation allowed* you will
   configure the role matching as follows:

   * Project creation allowed: ``*``
