LDAP 配置
#########

.. contents::

此界面允许您与 LDAP 建立连接，以委派验证和／或授权。

它分为四个不同的区域，以下分别说明：

启用
====

此区域用于设置决定 *LibrePlan* 如何使用 LDAP 的属性。

若勾选*启用 LDAP 验证*字段，每当用户尝试登录应用程序时，*LibrePlan* 将查询 LDAP。

勾选*使用 LDAP 角色*字段，表示建立 LDAP 角色与 LibrePlan 角色之间的映射关系。因此，用户在 LibrePlan 中的权限将取决于其在 LDAP 中拥有的角色。

配置
====

本节包含访问 LDAP 的参数值。*Base*、*UserDN* 和 *Password* 是用于连接 LDAP 和搜索用户的参数。因此，指定的用户必须具有在 LDAP 中执行此操作的权限。在本节底部，有一个按钮用于检查是否可以使用给定参数建立 LDAP 连接。建议在继续配置之前测试连接。

.. NOTE::

   若您的 LDAP 配置为使用匿名验证，您可以将 *UserDN* 和 *Password* 属性留空。

.. TIP::

   关于 *Active Directory (AD)* 配置，*Base* 字段必须是绑定用户在 AD 中所在的确切位置。

   示例：``ou=organizational_unit,dc=example,dc=org``

验证
====

在此，您可以配置 LDAP 节点中应找到给定用户名的属性。*UserId* 属性必须填入 LDAP 中存储用户名的属性名称。

勾选*在数据库中保存密码*复选框时，表示密码也存储在 LibrePlan 数据库中。如此一来，若 LDAP 离线或无法访问，LDAP 用户可以对 LibrePlan 数据库进行验证。若未勾选，LDAP 用户只能对 LDAP 进行验证。

授权
====

本节允许您定义将 LDAP 角色与 LibrePlan 角色进行匹配的策略。第一个选择是根据 LDAP 实现使用的策略。

组策略
------

当使用此策略时，表示 LDAP 具有角色组策略。这意味着 LDAP 中的用户是直接位于代表组的分支下的节点。

以下示例代表使用组策略的有效 LDAP 结构。

* LDAP 结构::

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

在此情况下，每个组都有一个属性，例如称为 ``member``，包含属于该组的用户列表：

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

此情况的配置如下：

* 角色搜索策略：``Group strategy``
* 组路径：``ou=groups``
* 角色属性：``member``
* 角色搜索查询：``uid=[USER_ID],ou=people,dc=example,dc=org``

例如，若您想要匹配某些角色：

* Administration: ``cn=admins;cn=itpeople``
* Web service reader: ``cn=itpeople``
* Web service writer: ``cn=itpeople``
* All projects read allowed: ``cn=admins``
* All projects edition allowed: ``cn=admins``
* Project creation allowed: ``cn=workers``

属性策略
--------

当管理员决定使用此策略时，表示每个用户是一个 LDAP 节点，且在节点内存在一个代表用户组的属性。在此情况下，配置不需要*组路径*参数。

以下示例代表使用属性策略的有效 LDAP 结构。

* LDAP 结构::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**使用属性**

在此情况下，每个用户都有一个属性，例如称为 ``group``，包含其所属组的名称：

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

   此策略有一个限制：每个用户只能属于一个组。

此情况的配置如下：

* 角色搜索策略：``Property strategy``
* 组路径：
* 角色属性：``group``
* 角色搜索查询：``[USER_ID]``

例如，若您想要匹配某些角色：

* Administration: ``admins;itpeople``
* Web service reader: ``itpeople``
* Web service writer: ``itpeople``
* All projects read allowed: ``admins``
* All projects edition allowed: ``admins``
* Project creation allowed: ``workers``

**依用户标识符**

您甚至可以使用一种变通方法，直接为用户指定 LibrePlan 角色，而无需在每个 LDAP 用户中设置属性。

在此情况下，您将通过 ``uid`` 指定哪些用户拥有不同的 LibrePlan 角色。

此情况的配置如下：

* 角色搜索策略：``Property strategy``
* 组路径：
* 角色属性：``uid``
* 角色搜索查询：``[USER_ID]``

例如，若您想要匹配某些角色：

* Administration: ``admin1;it1``
* Web service reader: ``it1;it2``
* Web service writer: ``it1;it2``
* All projects read allowed: ``admin1``
* All projects edition allowed: ``admin1``
* Project creation allowed: ``worker1;worker2;worker3``

角色匹配
--------

在本节底部，有一个表格列出所有 LibrePlan 角色，每个角色旁边都有一个文字字段。这用于匹配角色。例如，若管理员决定 LibrePlan 的 *Administration* 角色与 LDAP 的 *admin* 和 *administrators* 角色匹配，文字字段应包含："``admin;administrators``"。分隔角色的字符为"``;``"。

.. NOTE::

   若您想指定所有用户或所有组拥有某个权限，可以使用星号（``*``）作为通配符来引用它们。例如，若您希望所有人都拥有*允许创建项目*角色，您将如下配置角色匹配：

   * Project creation allowed: ``*``
