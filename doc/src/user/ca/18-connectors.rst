Connectors
##########

.. contents::

Els connectors són aplicacions client de *LibrePlan* que es poden usar per comunicar-se amb servidors (web) per recuperar dades, processar-les i emmagatzemar-les. Actualment, hi ha tres connectors: el connector JIRA, el connector Tim Enterprise i el connector de correu electrònic.

Configuració
============

Els connectors s'han de configurar correctament abans de poder-los usar. Es poden configurar des de la pantalla "Configuració principal" sota la pestanya "Connectors."

La pantalla del connector inclou:

*   **Llista desplegable:** Una llista de connectors disponibles.
*   **Pantalla d'edició de propietats:** Un formulari d'edició de propietats per al connector seleccionat.
*   **Botó de prova de connexió:** Un botó per provar la connexió amb el connector.

Seleccioneu el connector que voleu configurar de la llista desplegable de connectors. Es mostrarà un formulari d'editor de propietats per al connector seleccionat. Al formulari d'editor de propietats, podeu canviar els valors de les propietats segons sigui necessari i provar les vostres configuracions usant el botó "Provar connexió."

.. NOTE::

   Les propietats estan configurades amb valors per defecte. La propietat més important és "Activat." Per defecte, s'estableix en "N." Això indica que el connector no s'usarà tret que canvieu el valor a "Y" i deseu els canvis.

Connector JIRA
==============

JIRA és un sistema de seguiment d'incidències i projectes.

El connector JIRA és una aplicació que es pot usar per sol·licitar dades del servidor web JIRA per a les incidències JIRA i processar la resposta. La sol·licitud es basa en etiquetes JIRA. A JIRA, les etiquetes es poden usar per categoritzar les incidències. La sol·licitud s'estructura de la manera següent: recuperar totes les incidències categoritzades per aquest nom d'etiqueta.

El connector rep la resposta, que en aquest cas són les incidències, i les converteix en "Elements de projecte" i "Fulls de temps" de *LibrePlan*.

El *connector JIRA* s'ha de configurar correctament abans de poder-lo usar.

Configuració
------------

Des de la pantalla "Configuració principal", trieu la pestanya "Connectors." A la pantalla de connectors, seleccioneu el connector JIRA de la llista desplegable. Llavors es mostrarà una pantalla d'editor de propietats.

En aquesta pantalla, podeu configurar els valors de propietat següents:

*   **Activat:** Y/N, indicant si voleu usar el connector JIRA. El valor per defecte és "N."
*   **URL del servidor:** La ruta absoluta al servidor web JIRA.
*   **Nom d'usuari i contrasenya:** Les credencials d'usuari per a l'autorització.
*   **Etiquetes JIRA: llista d'etiquetes separades per comes o URL:** Podeu introduir l'URL de l'etiqueta o una llista d'etiquetes separades per comes.
*   **Tipus d'hores:** El tipus d'hores de treball. El valor per defecte és "Per defecte."

.. NOTE::

   **Etiquetes JIRA:** Actualment, el servidor web JIRA no admet proporcionar una llista de totes les etiquetes disponibles. Com a solució alternativa, hem desenvolupat un script PHP senzill que realitza una consulta SQL senzilla a la base de dades JIRA per obtenir totes les etiquetes diferents. Podeu usar aquest script PHP com a "URL d'etiquetes JIRA" o introduir les etiquetes que voleu com a text separat per comes al camp "Etiquetes JIRA."

Finalment, feu clic al botó "Provar connexió" per provar si podeu connectar-vos al servidor web JIRA i que les vostres configuracions siguin correctes.

Sincronització
--------------

Des de la finestra del projecte, sota "Dades generals", podeu iniciar la sincronització dels elements de projecte amb les incidències JIRA.

Feu clic al botó "Sincronitzar amb JIRA" per iniciar la sincronització.

*   Si és la primera vegada, es mostrarà una finestra emergent (amb una llista d'etiquetes autocompletada). En aquesta finestra, podeu seleccionar una etiqueta per sincronitzar i fer clic al botó "Iniciar sincronització" per iniciar el procés de sincronització, o fer clic al botó "Cancel·lar" per cancel·lar-lo.

*   Si una etiqueta ja està sincronitzada, la data de l'última sincronització i l'etiqueta es mostraran a la pantalla JIRA. En aquest cas, no es mostrarà cap finestra emergent per seleccionar una etiqueta. En canvi, el procés de sincronització s'iniciarà directament per a aquesta etiqueta (ja sincronitzada) que es mostra.

.. NOTE::

   La relació entre "Projecte" i "etiqueta" és d'un a un. Només es pot sincronitzar una etiqueta amb un "Projecte."

.. NOTE::

   Quan la (re)sincronització és exitosa, la informació s'escriurà a la base de dades, i la pantalla JIRA s'actualitzarà amb la data i l'etiqueta de l'última sincronització.

La (re)sincronització es realitza en dues fases:

*   **Fase 1:** Sincronització dels elements de projecte, inclosa l'assignació i les mesures de progrés.
*   **Fase 2:** Sincronització dels fulls de temps.

.. NOTE::

   Si la Fase 1 falla, la Fase 2 no es realitzarà, i cap informació s'escriurà a la base de dades.

.. NOTE::

   La informació sobre l'èxit o el fracàs es mostrarà en una finestra emergent.

Quan es completa la sincronització exitosament, el resultat es mostrarà a la pestanya "Estructura de Desglossament del Treball (tasques WBS)" de la pantalla "Detalls del projecte." En aquesta interfície d'usuari, hi ha dos canvis respecte al WBS estàndard:

*   La columna "Total d'hores de la tasca" no és modificable (només lectura) perquè la sincronització és unidireccional. Les hores de la tasca només es poden actualitzar al servidor web JIRA.
*   La columna "Codi" mostra les claus de les incidències JIRA, i també són hiperenllaços a les incidències JIRA. Feu clic a la clau desitjada si voleu anar al document per a aquella clau (incidència JIRA).

Planificació
------------

La resincronització de les incidències JIRA també es pot realitzar a través del planificador. Aneu a la pantalla "Planificació de treballs." En aquella pantalla, podeu configurar un treball JIRA per realitzar la sincronització. El treball cerca les últimes etiquetes sincronitzades a la base de dades i les resincronitza en conseqüència. Consulteu també el Manual del Planificador.

Connector Tim Enterprise
========================

Tim Enterprise és un producte holandès d'Aenova. És una aplicació web per a l'administració del temps dedicat a projectes i tasques.

El connector Tim és una aplicació que es pot usar per comunicar-se amb el servidor Tim Enterprise per:

*   Exportar totes les hores dedicades per un treballador (usuari) a un projecte que podrien registrar-se a Tim Enterprise.
*   Importar tots els torns del treballador (usuari) per planificar el recurs de manera eficient.

El *connector Tim* s'ha de configurar correctament abans de poder-lo usar.

Configuració
------------

Des de la pantalla "Configuració principal", trieu la pestanya "Connectors." A la pantalla de connectors, seleccioneu el connector Tim de la llista desplegable. Llavors es mostrarà una pantalla d'editor de propietats.

En aquesta pantalla, podeu configurar els valors de propietat següents:

*   **Activat:** Y/N, indicant si voleu usar el connector Tim. El valor per defecte és "N."
*   **URL del servidor:** La ruta absoluta al servidor Tim Enterprise.
*   **Nom d'usuari i contrasenya:** Les credencials d'usuari per a l'autorització.
*   **Nombre de dies de full de temps a Tim:** El nombre de dies enrere que voleu exportar els fulls de temps.
*   **Nombre de dies de torns des de Tim:** El nombre de dies endavant que voleu importar els torns.
*   **Factor de productivitat:** Hores de treball efectives en percentatge. El valor per defecte és "100%."
*   **IDs de departament per importar torns:** IDs de departament separats per comes.

Finalment, feu clic al botó "Provar connexió" per provar si podeu connectar-vos al servidor Tim Enterprise i que les vostres configuracions siguin correctes.

Exportació
----------

Des de la finestra del projecte, sota "Dades generals", podeu iniciar l'exportació dels fulls de temps al servidor Tim Enterprise.

Introduïu el "Codi de producte Tim" i feu clic al botó "Exportar a Tim" per iniciar l'exportació.

El connector Tim afegeix els camps següents juntament amb el codi de producte:

*   El nom complet del treballador/usuari.
*   La data en què el treballador va treballar en una tasca.
*   L'esforç, o hores treballades en la tasca.
*   Una opció que indica si Tim Enterprise ha d'actualitzar el registre o inserir-ne un de nou.

La resposta de Tim Enterprise conté només una llista d'IDs de registre (enters). Això fa difícil determinar què ha anat malament, ja que la llista de respostes conté només números no relacionats amb els camps de la sol·licitud. La sol·licitud d'exportació (registre a Tim) es considera exitosa si totes les entrades de la llista no contenen valors "0." En cas contrari, la sol·licitud d'exportació ha fallat per a les entrades que contenen valors "0." Per tant, no podeu veure quina sol·licitud ha fallat, ja que les entrades de la llista contenen només el valor "0." L'única manera de determinar-ho és examinar el fitxer de registre al servidor Tim Enterprise.

.. NOTE::

   Quan l'exportació és exitosa, la informació s'escriurà a la base de dades, i la pantalla Tim s'actualitzarà amb la data i el codi de producte de l'última exportació.

.. NOTE::

   La informació sobre l'èxit o el fracàs es mostrarà en una finestra emergent.

Planificació de l'Exportació
----------------------------

El procés d'exportació també es pot realitzar a través del planificador. Aneu a la pantalla "Planificació de treballs." En aquella pantalla, podeu configurar un treball d'exportació Tim. El treball cerca els últims fulls de temps exportats a la base de dades i els reexporta en conseqüència. Consulteu també el manual del Planificador.

Importació
----------

La importació de torns només funciona amb l'ajuda del planificador. No hi ha cap interfície d'usuari dissenyada per a això, ja que no cal cap entrada de l'usuari. Aneu a la pantalla "Planificació de treballs" i configureu un treball d'importació Tim. El treball itera per tots els departaments configurats a les propietats del connector i importa tots els torns per a cada departament. Consulteu també el Manual del Planificador.

Per a la importació, el connector Tim afegeix els camps següents a la sol·licitud:

*   **Període:** El període (data d'inici - data de fi) per al qual voleu importar el torn. Això es pot proporcionar com a criteri de filtre.
*   **Departament:** El departament per al qual voleu importar el torn. Els departaments es poden configurar.
*   Els camps que us interessen (com ara informació de persona, RosterCategory, etc.) que el servidor Tim ha d'incloure a la seva resposta.

La resposta d'importació conté els camps següents, que són suficients per gestionar els dies d'excepció a *LibrePlan*:

*   **Informació de la persona:** Nom i nom de xarxa.
*   **Departament:** El departament en què treballa el treballador.
*   **Categoria de torn:** Informació sobre la presència/absència (Aanwzig/afwezig) del treballador i el motiu (tipus d'excepció de *LibrePlan*) en cas que el treballador estigui absent.
*   **Data:** La data en què el treballador és present/absent.
*   **Hora:** L'hora d'inici de la presència/absència, per exemple, 08:00.
*   **Durada:** El nombre d'hores que el treballador és present/absent.

En convertir la resposta d'importació a "Dia d'excepció" de *LibrePlan*, es tenen en compte les traduccions següents:

*   Si la categoria de torn conté el nom "Vakantie," es traduirà a "VACANCES DEL RECURS."
*   La categoria de torn "Feestdag" es traduirà a "DIA FESTIU."
*   Tota la resta, com "Jus uren," "PLB uren," etc., s'hauria d'afegir manualment als "Dies d'excepció del calendari."

A més, a la resposta d'importació, el torn es divideix en dues o tres parts per dia: per exemple, torn-matí, torn-tarda i torn-nit. Tanmateix, *LibrePlan* permet només un "Tipus d'excepció" per dia. El connector Tim és llavors responsable de fusionar aquestes parts com un tipus d'excepció. És a dir, la categoria de torn amb la durada més llarga es considera un tipus d'excepció vàlid, però la durada total és la suma de totes les durades d'aquestes parts de categoria.

Al contrari de *LibrePlan*, a Tim Enterprise, la durada total en el cas que el treballador estigui de vacances significa que el treballador no és disponible per a aquella durada total. Tanmateix, a *LibrePlan*, si el treballador és de vacances, la durada total hauria de ser zero. El connector Tim també gestiona aquesta traducció.

Connector de Correu Electrònic
===============================

El correu electrònic és un mètode d'intercanvi de missatges digitals d'un autor a un o més destinataris.

El connector de correu electrònic es pot usar per establir les propietats de connexió del servidor de Protocol Simple de Transferència de Correu (SMTP).

El *connector de correu electrònic* s'ha de configurar correctament abans de poder-lo usar.

Configuració
------------

Des de la pantalla "Configuració principal", trieu la pestanya "Connectors." A la pantalla de connectors, seleccioneu el connector de correu electrònic de la llista desplegable. Llavors es mostrarà una pantalla d'editor de propietats.

En aquesta pantalla, podeu configurar els valors de propietat següents:

*   **Activat:** Y/N, indicant si voleu usar el connector de correu electrònic. El valor per defecte és "N."
*   **Protocol:** El tipus de protocol SMTP.
*   **Amfitrió:** La ruta absoluta al servidor SMTP.
*   **Port:** El port del servidor SMTP.
*   **Adreça del remitent:** L'adreça de correu electrònic del remitent del missatge.
*   **Nom d'usuari:** El nom d'usuari per al servidor SMTP.
*   **Contrasenya:** La contrasenya per al servidor SMTP.

Finalment, feu clic al botó "Provar connexió" per provar si podeu connectar-vos al servidor SMTP i que les vostres configuracions siguin correctes.

Editar Plantilla de Correu Electrònic
--------------------------------------

Des de la finestra del projecte, sota "Configuració" i després "Editar plantilles de correu electrònic," podeu modificar les plantilles de correu electrònic per als missatges.

Podeu triar:

*   **Idioma de la plantilla:**
*   **Tipus de plantilla:**
*   **Assumpte del correu electrònic:**
*   **Continguts de la plantilla:**

Cal especificar l'idioma perquè l'aplicació web enviarà correus electrònics als usuaris en l'idioma que han triat a les seves preferències. Cal triar el tipus de plantilla. El tipus és el rol d'usuari, que significa que aquest correu electrònic s'enviarà només als usuaris que estiguin al rol seleccionat (tipus). Cal establir l'assumpte del correu electrònic. L'assumpte és un breu resum del tema del missatge. Cal establir el contingut del correu electrònic. Es tracta de qualsevol informació que voleu enviar a l'usuari. També hi ha algunes paraules clau que podeu usar al missatge; l'aplicació web les analitzarà i establirà un nou valor en lloc de la paraula clau.

Planificació de Correus Electrònics
------------------------------------

L'enviament de correus electrònics només es pot realitzar a través del planificador. Aneu a "Configuració," i després a la pantalla "Planificació de treballs." En aquella pantalla, podeu configurar un treball d'enviament de correus electrònics. El treball pren una llista de notificacions de correu electrònic, reuneix dades i les envia al correu electrònic de l'usuari. Consulteu també el manual del Planificador.

.. NOTE::

   La informació sobre l'èxit o el fracàs es mostrarà en una finestra emergent.
