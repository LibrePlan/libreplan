Configurazione LDAP
###################

.. contents::

Questa schermata consente di stabilire una connessione con LDAP per delegare
l'autenticazione e/o l'autorizzazione.

È divisa in quattro aree diverse, che vengono spiegate di seguito:

Attivazione
===========

Quest'area viene utilizzata per impostare le proprietà che determinano come *LibrePlan* utilizza
LDAP.

Se il campo *Abilita autenticazione LDAP* è selezionato, *LibrePlan* interrogherà
LDAP ogni volta che un utente tenta di accedere all'applicazione.

Il campo *Usa ruoli LDAP* selezionato significa che viene stabilita una mappatura tra i ruoli LDAP e i
ruoli LibrePlan. Di conseguenza, i permessi di un utente in
LibrePlan dipenderanno dai ruoli che l'utente ha in LDAP.

Configurazione
==============

Questa sezione contiene i valori dei parametri per l'accesso a LDAP. *Base*, *UserDN* e
*Password* sono parametri utilizzati per connettersi a LDAP e cercare gli utenti. Pertanto,
l'utente specificato deve avere il permesso per eseguire questa operazione in LDAP. In fondo
a questa sezione c'è un pulsante per verificare se è possibile stabilire una connessione LDAP
con i parametri forniti. È consigliabile testare la connessione prima di
continuare la configurazione.

.. NOTE::

   Se il proprio LDAP è configurato per funzionare con l'autenticazione anonima, è possibile
   lasciare vuoti gli attributi *UserDN* e *Password*.

.. TIP::

   Per quanto riguarda la configurazione di *Active Directory (AD)*, il campo *Base* deve essere la
   posizione esatta in cui risiede l'utente collegato in AD.

   Esempio: ``ou=organizational_unit,dc=example,dc=org``

Autenticazione
==============

Qui è possibile configurare la proprietà nei nodi LDAP in cui il nome utente fornito
dovrebbe essere trovato. La proprietà *UserId* deve essere compilata con il nome della
proprietà in cui il nome utente è memorizzato in LDAP.

La casella di controllo *Salva le password nel database*, se selezionata, significa che la
password viene archiviata anche nel database LibrePlan. In questo modo, se LDAP è
offline o non raggiungibile, gli utenti LDAP possono autenticarsi nel database LibrePlan.
Se non è selezionata, gli utenti LDAP possono essere autenticati solo tramite LDAP.

Autorizzazione
==============

Questa sezione consente di definire una strategia per abbinare i ruoli LDAP con
i ruoli LibrePlan. La prima scelta è la strategia da utilizzare, a seconda dell'implementazione LDAP.

Strategia di Gruppo
-------------------

Quando si utilizza questa strategia, indica che LDAP ha una strategia di ruolo-gruppo.
Ciò significa che gli utenti in LDAP sono nodi che si trovano direttamente sotto un ramo che
rappresenta il gruppo.

Il seguente esempio rappresenta una struttura LDAP valida per l'utilizzo della strategia di gruppo.

* Struttura LDAP::

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

In questo caso, ogni gruppo avrà un attributo, ad esempio chiamato ``member``,
con l'elenco degli utenti appartenenti al gruppo:

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

La configurazione per questo caso è la seguente:

* Strategia di ricerca ruolo: ``Group strategy``
* Percorso gruppo: ``ou=groups``
* Proprietà ruolo: ``member``
* Query di ricerca ruolo: ``uid=[USER_ID],ou=people,dc=example,dc=org``

E, ad esempio, se si desidera abbinare alcuni ruoli:

* Amministrazione: ``cn=admins;cn=itpeople``
* Lettore servizi web: ``cn=itpeople``
* Scrittore servizi web: ``cn=itpeople``
* Lettura di tutti i progetti consentita: ``cn=admins``
* Modifica di tutti i progetti consentita: ``cn=admins``
* Creazione di progetti consentita: ``cn=workers``

Strategia per Proprietà
------------------------

Quando un amministratore decide di utilizzare questa strategia, indica che ogni utente
è un nodo LDAP e, all'interno del nodo, esiste una proprietà che rappresenta
il gruppo o i gruppi per l'utente. In questo caso, la configurazione non richiede il
parametro *Percorso gruppo*.

Il seguente esempio rappresenta una struttura LDAP valida per l'utilizzo della strategia per proprietà.

* Struttura LDAP::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Con Attributo**

In questo caso, ogni utente avrà un attributo, ad esempio chiamato ``group``,
con il nome del gruppo a cui appartiene:

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

   Questa strategia ha una limitazione: ogni utente può appartenere a un solo gruppo.

La configurazione per questo caso è la seguente:

* Strategia di ricerca ruolo: ``Property strategy``
* Percorso gruppo:
* Proprietà ruolo: ``group``
* Query di ricerca ruolo: ``[USER_ID]``

E, ad esempio, se si desidera abbinare alcuni ruoli:

* Amministrazione: ``admins;itpeople``
* Lettore servizi web: ``itpeople``
* Scrittore servizi web: ``itpeople``
* Lettura di tutti i progetti consentita: ``admins``
* Modifica di tutti i progetti consentita: ``admins``
* Creazione di progetti consentita: ``workers``

**Per Identificatore Utente**

È anche possibile utilizzare una soluzione alternativa per specificare i ruoli LibrePlan direttamente agli utenti
senza avere un attributo in ogni utente LDAP.

In questo caso, si specifica quali utenti hanno i diversi ruoli LibrePlan
tramite ``uid``.

La configurazione per questo caso è la seguente:

* Strategia di ricerca ruolo: ``Property strategy``
* Percorso gruppo:
* Proprietà ruolo: ``uid``
* Query di ricerca ruolo: ``[USER_ID]``

E, ad esempio, se si desidera abbinare alcuni ruoli:

* Amministrazione: ``admin1;it1``
* Lettore servizi web: ``it1;it2``
* Scrittore servizi web: ``it1;it2``
* Lettura di tutti i progetti consentita: ``admin1``
* Modifica di tutti i progetti consentita: ``admin1``
* Creazione di progetti consentita: ``worker1;worker2;worker3``

Corrispondenza dei Ruoli
------------------------

In fondo a questa sezione c'è una tabella con tutti i ruoli LibrePlan
e un campo di testo accanto a ciascuno. Questo serve per abbinare i ruoli. Ad esempio,
se un amministratore decide che il ruolo *Amministrazione* di LibrePlan corrisponde
ai ruoli *admin* e *administrators* di LDAP, il campo di testo deve contenere:
"``admin;administrators``". Il carattere per separare i ruoli è "``;``".

.. NOTE::

   Se si desidera specificare che tutti gli utenti o tutti i gruppi hanno un permesso, è possibile
   utilizzare un asterisco (``*``) come carattere jolly per fare riferimento a essi. Ad esempio, se
   si desidera che tutti abbiano il ruolo *Creazione di progetti consentita*, si configurerà
   la corrispondenza dei ruoli come segue:

   * Creazione di progetti consentita: ``*``
