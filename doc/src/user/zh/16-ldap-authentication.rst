LDAP 設定
#########

.. contents::

此畫面允許您與 LDAP 建立連接，以委派驗證和／或授權。

它分為四個不同的區域，以下分別說明：

啟用
====

此區域用於設定決定 *LibrePlan* 如何使用 LDAP 的屬性。

若勾選*啟用 LDAP 驗證*欄位，每當使用者嘗試登入應用程式時，*LibrePlan* 將查詢 LDAP。

勾選*使用 LDAP 角色*欄位，表示建立 LDAP 角色與 LibrePlan 角色之間的對應關係。因此，使用者在 LibrePlan 中的權限將取決於其在 LDAP 中擁有的角色。

設定
====

本節包含存取 LDAP 的參數值。*Base*、*UserDN* 和 *Password* 是用於連接 LDAP 和搜尋使用者的參數。因此，指定的使用者必須具有在 LDAP 中執行此操作的權限。在本節底部，有一個按鈕用於檢查是否可以使用給定參數建立 LDAP 連接。建議在繼續設定之前測試連接。

.. NOTE::

   若您的 LDAP 設定為使用匿名驗證，您可以將 *UserDN* 和 *Password* 屬性留空。

.. TIP::

   關於 *Active Directory (AD)* 設定，*Base* 欄位必須是繫結使用者在 AD 中所在的確切位置。

   範例：``ou=organizational_unit,dc=example,dc=org``

驗證
====

在此，您可以設定 LDAP 節點中應找到給定使用者名稱的屬性。*UserId* 屬性必須填入 LDAP 中儲存使用者名稱的屬性名稱。

勾選*在資料庫中儲存密碼*核取方塊時，表示密碼也儲存在 LibrePlan 資料庫中。如此一來，若 LDAP 離線或無法存取，LDAP 使用者可以對 LibrePlan 資料庫進行驗證。若未勾選，LDAP 使用者只能對 LDAP 進行驗證。

授權
====

本節允許您定義將 LDAP 角色與 LibrePlan 角色進行匹配的策略。第一個選擇是根據 LDAP 實作使用的策略。

群組策略
--------

當使用此策略時，表示 LDAP 具有角色群組策略。這意味著 LDAP 中的使用者是直接位於代表群組的分支下的節點。

以下範例代表使用群組策略的有效 LDAP 結構。

* LDAP 結構::

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

在此情況下，每個群組都有一個屬性，例如稱為 ``member``，包含屬於該群組的使用者清單：

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

此情況的設定如下：

* 角色搜尋策略：``Group strategy``
* 群組路徑：``ou=groups``
* 角色屬性：``member``
* 角色搜尋查詢：``uid=[USER_ID],ou=people,dc=example,dc=org``

例如，若您想要匹配某些角色：

* Administration: ``cn=admins;cn=itpeople``
* Web service reader: ``cn=itpeople``
* Web service writer: ``cn=itpeople``
* All projects read allowed: ``cn=admins``
* All projects edition allowed: ``cn=admins``
* Project creation allowed: ``cn=workers``

屬性策略
--------

當管理員決定使用此策略時，表示每個使用者是一個 LDAP 節點，且在節點內存在一個代表使用者群組的屬性。在此情況下，設定不需要*群組路徑*參數。

以下範例代表使用屬性策略的有效 LDAP 結構。

* LDAP 結構::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**使用屬性**

在此情況下，每個使用者都有一個屬性，例如稱為 ``group``，包含其所屬群組的名稱：

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

   此策略有一個限制：每個使用者只能屬於一個群組。

此情況的設定如下：

* 角色搜尋策略：``Property strategy``
* 群組路徑：
* 角色屬性：``group``
* 角色搜尋查詢：``[USER_ID]``

例如，若您想要匹配某些角色：

* Administration: ``admins;itpeople``
* Web service reader: ``itpeople``
* Web service writer: ``itpeople``
* All projects read allowed: ``admins``
* All projects edition allowed: ``admins``
* Project creation allowed: ``workers``

**依使用者識別碼**

您甚至可以使用一種變通方法，直接為使用者指定 LibrePlan 角色，而無需在每個 LDAP 使用者中設定屬性。

在此情況下，您將透過 ``uid`` 指定哪些使用者擁有不同的 LibrePlan 角色。

此情況的設定如下：

* 角色搜尋策略：``Property strategy``
* 群組路徑：
* 角色屬性：``uid``
* 角色搜尋查詢：``[USER_ID]``

例如，若您想要匹配某些角色：

* Administration: ``admin1;it1``
* Web service reader: ``it1;it2``
* Web service writer: ``it1;it2``
* All projects read allowed: ``admin1``
* All projects edition allowed: ``admin1``
* Project creation allowed: ``worker1;worker2;worker3``

角色匹配
--------

在本節底部，有一個表格列出所有 LibrePlan 角色，每個角色旁邊都有一個文字欄位。這用於匹配角色。例如，若管理員決定 LibrePlan 的 *Administration* 角色與 LDAP 的 *admin* 和 *administrators* 角色匹配，文字欄位應包含："``admin;administrators``"。分隔角色的字元為"``;``"。

.. NOTE::

   若您想指定所有使用者或所有群組擁有某個權限，可以使用星號（``*``）作為萬用字元來引用它們。例如，若您希望所有人都擁有*允許建立專案*角色，您將如下設定角色匹配：

   * Project creation allowed: ``*``
