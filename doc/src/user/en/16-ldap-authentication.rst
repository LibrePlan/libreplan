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

This section has the parameter values for accessing LDAP.*Base, UserDN and
Password* are parameters used to connect to LDAP and search for the users, so
given user must have permission to do that operation in LDAP. At bottom part of
this section there is a button to check if LDAP connection is possible with the
given parameters. It is a good idea to try it before continuing the
configuration.

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
represents the group. In this way, considering as example an LDAP with a branch
(group) called *Admin* and two nodes (users) in the branch called *John* and
*William*, an administrator could assign to both users a role in LibrePlan. The
only parameter needed in this case is the *Group path* that represents the path
in LDAP to find the branches with the groups.

Property strategy
-----------------

When administrator decides to use this strategy, it means that each user is a
LDAP node and in the node exists a property that represents the group(s) for
the user. In this case, the configuration needs two parameters:

* *Role property*. It represents the property in user's node in LDAP which
  contains all the roles for that user.

* *Role search query*. It represents the path in LDAP to find the nodes of
  the users; in this case, note that is important to know that string
  "[USER_ID]" represents the place where the login name given in the login
  form should be placed to get the correct user's node in LDAP.

At the bottom of this section there is a table with all the LibrePlan roles and
a text field next to each one. This is for matching roles. For instance, if
administrator decides that *Administration* LibrePlan role matches with *admin*
and *administrators* roles of LDAP, in the text field should appear:
"admin;administrators". The character for splitting roles is ";".
