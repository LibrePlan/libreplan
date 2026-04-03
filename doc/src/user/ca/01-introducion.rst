Introducció
###########

.. contents::

Aquest document descriu les funcionalitats de LibrePlan i proporciona informació als usuaris sobre com configurar i utilitzar l'aplicació.

LibrePlan és una aplicació web de codi obert per a la planificació de projectes. El seu objectiu principal és proporcionar una solució completa per a la gestió de projectes d'empresa. Per a qualsevol informació específica que necessiteu sobre aquest programari, contacteu amb l'equip de desenvolupament a http://www.libreplan.com/contact/

.. figure:: images/company_view.png
   :scale: 50

   Vista general de l'empresa

Vista general de l'empresa i gestió de vistes
=============================================

Tal com es mostra a la pantalla principal del programa (vegeu la captura de pantalla anterior) i la vista general de l'empresa, els usuaris poden veure una llista de projectes planificats. Això els permet entendre l'estat general de l'empresa quant a projectes i utilització de recursos. La vista general de l'empresa ofereix tres vistes diferenciades:

* **Vista de Planificació:** Aquesta vista combina dues perspectives:

   * **Seguiment de Projectes i Temps:** Cada projecte es representa amb un diagrama de Gantt, que indica les dates d'inici i de fi del projecte. Aquesta informació es mostra al costat del termini acordat. Aleshores es fa una comparació entre el percentatge de progrés assolit i el temps real dedicat a cada projecte. Això proporciona una imatge clara del rendiment de l'empresa en un moment determinat. Aquesta vista és la pàgina d'inici predeterminada del programa.
   * **Gràfic d'Utilització de Recursos de l'Empresa:** Aquest gràfic mostra informació sobre l'assignació de recursos als projectes, proporcionant un resum de l'ús de recursos de tota l'empresa. El color verd indica que l'assignació de recursos és inferior al 100% de la capacitat. La línia negra representa la capacitat total de recursos disponibles. El color groc indica que l'assignació de recursos supera el 100%. És possible tenir una infraassignació global mentre s'experimenta simultàniament una sobreassignació per a recursos específics.

* **Vista de Càrrega de Recursos:** Aquesta pantalla mostra una llista dels treballadors de l'empresa i les seves assignacions específiques de tasques, o assignacions genèriques basades en criteris definits. Per accedir a aquesta vista, feu clic a *Càrrega global de recursos*. Vegeu la imatge següent per a un exemple.
* **Vista d'Administració de Projectes:** Aquesta pantalla mostra una llista de projectes de l'empresa, permetent als usuaris realitzar les accions següents: filtrar, editar, eliminar, visualitzar la planificació o crear un nou projecte. Per accedir a aquesta vista, feu clic a *Llista de projectes*.

.. figure:: images/resources_global.png
   :scale: 50

   Vista general de recursos

.. figure:: images/order_list.png
   :scale: 50

   Estructura de desglosament del treball

La gestió de vistes descrita anteriorment per a la vista general de l'empresa és molt similar a la gestió disponible per a un projecte individual. Es pot accedir a un projecte de diverses maneres:

* Feu clic dret al diagrama de Gantt del projecte i seleccioneu *Planificar*.
* Accediu a la llista de projectes i feu clic a la icona del diagrama de Gantt.
* Creeu un nou projecte i canvieu la vista de projecte actual.

El programa ofereix les vistes següents per a un projecte:

* **Vista de Planificació:** Aquesta vista permet als usuaris visualitzar la planificació de tasques, dependències, fites i molt més. Consulteu la secció *Planificació* per a més detalls.
* **Vista de Càrrega de Recursos:** Aquesta vista permet als usuaris verificar la càrrega de recursos designada per a un projecte. El codi de colors és coherent amb la vista general de l'empresa: verd per a una càrrega inferior al 100%, groc per a una càrrega igual al 100% i vermell per a una càrrega superior al 100%. La càrrega pot provenir d'una tasca específica o d'un conjunt de criteris (assignació genèrica).
* **Vista d'Edició del Projecte:** Aquesta vista permet als usuaris modificar els detalls del projecte. Consulteu la secció *Projectes* per a més informació.
* **Vista Avançada d'Assignació de Recursos:** Aquesta vista permet als usuaris assignar recursos amb opcions avançades, com ara especificar hores per dia o les funcions assignades a realitzar. Consulteu la secció *Assignació de recursos* per a més informació.

Què fa útil LibrePlan?
======================

LibrePlan és una eina de planificació de propòsit general desenvolupada per abordar els reptes en la planificació de projectes industrials que no estaven adequadament coberts per les eines existents. El desenvolupament de LibrePlan també va estar motivat pel desig de proporcionar una alternativa lliure, de codi obert i totalment basada en web a les eines de planificació propietàries.

Els conceptes centrals en què es basa el programa són els següents:

* **Vista General de l'Empresa i Multiprojecte:** LibrePlan està específicament dissenyat per proporcionar als usuaris informació sobre múltiples projectes que s'executen dins d'una empresa. Per tant, és inherentment un programa multiprojecte. L'enfocament del programa no es limita als projectes individuals, tot i que també hi ha disponibles vistes específiques per a projectes individuals.
* **Gestió de Vistes:** La vista general de l'empresa, o vista multiprojecte, va acompanyada de diverses vistes de la informació emmagatzemada. Per exemple, la vista general de l'empresa permet als usuaris veure els projectes i comparar el seu estat, veure la càrrega global de recursos de l'empresa i gestionar els projectes. Els usuaris també poden accedir a la vista de planificació, la vista de càrrega de recursos, la vista avançada d'assignació de recursos i la vista d'edició del projecte per a projectes individuals.
* **Criteris:** Els criteris són una entitat del sistema que permet la classificació tant dels recursos (humans i de màquines) com de les tasques. Els recursos han de complir determinats criteris, i les tasques requereixen que es compleixin criteris específics. Aquesta és una de les funcionalitats més importants del programa, ja que els criteris formen la base de l'assignació genèrica i aborden un repte significatiu en la indústria: la naturalesa laboriosa de la gestió de recursos humans i la dificultat de les estimacions de càrrega d'empresa a llarg termini.
* **Recursos:** Hi ha dos tipus de recursos: humans i de màquines. Els recursos humans són els treballadors de l'empresa, utilitzats per a la planificació, el seguiment i el control de la càrrega de treball de l'empresa. Els recursos de màquines, depenents de les persones que els operen, funcionen de manera similar als recursos humans.
* **Assignació de Recursos:** Una característica clau del programa és la capacitat d'assignar recursos de dues maneres: específicament i genèricament. L'assignació genèrica es basa en els criteris necessaris per completar una tasca i ha de ser complerta per recursos capaços de satisfer aquests criteris. Per entendre l'assignació genèrica, considereu aquest exemple: Joan Garcia és un soldador. Normalment, Joan Garcia seria assignat específicament a una tasca planificada. No obstant això, LibrePlan ofereix l'opció de seleccionar qualsevol soldador de l'empresa, sense necessitat d'especificar que Joan Garcia és la persona assignada.
* **Control de Càrrega de l'Empresa:** El programa permet un control fàcil de la càrrega de recursos de l'empresa. Aquest control s'estén tant al mig termini com al llarg termini, ja que els projectes actuals i futurs es poden gestionar dins del programa. LibrePlan proporciona gràfics que representen visualment la utilització de recursos.
* **Etiquetes:** Les etiquetes s'utilitzen per categoritzar les tasques del projecte. Amb aquestes etiquetes, els usuaris poden agrupar tasques per concepte, permetent una revisió posterior com a grup o després de filtrar.
* **Filtres:** Com que el sistema inclou naturalment elements que etiqueten o caracteritzen les tasques i els recursos, es poden utilitzar filtres de criteris o etiquetes. Això és molt útil per revisar informació categoritzada o generar informes específics basats en criteris o etiquetes.
* **Calendaris:** Els calendaris defineixen les hores productives disponibles per als diferents recursos. Els usuaris poden crear calendaris generals per a l'empresa o definir calendaris més específics, permetent la creació de calendaris per a recursos individuals i tasques.
* **Projectes i Elements de Projecte:** El treball sol·licitat pels clients es tracta com un projecte dins de l'aplicació, estructurat en elements de projecte. El projecte i els seus elements segueixen una estructura jeràrquica amb *x* nivells. Aquest arbre d'elements forma la base per a la planificació del treball.
* **Progrés:** El programa pot gestionar diversos tipus de progrés. El progrés d'un projecte es pot mesurar com a percentatge, en unitats, respecte al pressupost acordat i molt més. La responsabilitat de determinar quin tipus de progrés s'ha d'utilitzar per a la comparació en els nivells superiors del projecte recau en el responsable de planificació.
* **Tasques:** Les tasques són els elements de planificació fonamentals dins del programa. S'utilitzen per programar el treball a realitzar. Les característiques clau de les tasques inclouen: dependències entre tasques i el possible requisit que es compleixin criteris específics abans que es puguin assignar recursos.
* **Parts de Treball:** Aquests informes, presentats pels treballadors de l'empresa, detallen les hores treballades i les tasques associades a aquestes hores. Aquesta informació permet al sistema calcular el temps real emprat per completar una tasca en comparació amb el temps pressupostat. El progrés es pot comparar aleshores amb les hores reals dedicades.

A més de les funcions bàsiques, LibrePlan ofereix altres funcionalitats que el distingeixen de programes similars:

* **Integració amb ERP:** El programa pot importar directament informació dels sistemes ERP de l'empresa, inclosos projectes, recursos humans, parts de treball i criteris específics.
* **Gestió de Versions:** El programa pot gestionar múltiples versions de planificació, tot i que permet als usuaris revisar la informació de cada versió.
* **Gestió d'Historial:** El programa no elimina informació; simplement la marca com a invàlida. Això permet als usuaris revisar informació històrica mitjançant filtres de data.

Convencions d'usabilitat
========================

Informació sobre formularis
----------------------------
Abans de descriure les diverses funcions associades als mòduls més importants, hem d'explicar la navegació general i el comportament dels formularis.

Hi ha essencialment tres tipus de formularis d'edició:

* **Formularis amb un botó *Torna*:** Aquests formularis formen part d'un context més gran, i els canvis realitzats s'emmagatzemen en memòria. Els canvis només s'apliquen quan l'usuari desa explícitament tots els detalls de la pantalla des de la qual es va originar el formulari.
* **Formularis amb botons *Desa* i *Tanca*:** Aquests formularis permeten dues accions. La primera desa els canvis i tanca la finestra actual. La segona tanca la finestra sense desar cap canvi.
* **Formularis amb botons *Desa i continua*, *Desa* i *Tanca*:** Aquests formularis permeten tres accions. La primera desa els canvis i manté el formulari actual obert. La segona desa els canvis i tanca el formulari. La tercera tanca la finestra sense desar cap canvi.

Icones i botons estàndard
--------------------------

* **Editar:** En general, els registres del programa es poden editar fent clic en una icona que sembla un llapis sobre un quadern blanc.
* **Sagnat a l'esquerra:** Aquestes operacions s'utilitzen generalment per a elements dins d'una estructura en arbre que s'han de moure a un nivell més profund. Això es fa fent clic a la icona que sembla una fletxa verda que apunta cap a la dreta.
* **Sagnat a la dreta:** Aquestes operacions s'utilitzen generalment per a elements dins d'una estructura en arbre que s'han de moure a un nivell superior. Això es fa fent clic a la icona que sembla una fletxa verda que apunta cap a l'esquerra.
* **Eliminar:** Els usuaris poden eliminar informació fent clic a la icona de paperera.
* **Cercar:** La icona de lupa indica que el camp de text a la seva esquerra s'utilitza per cercar elements.

Pestanyes
---------
El programa utilitza pestanyes per organitzar els formularis d'edició i administració de contingut. Aquest mètode s'utilitza per dividir un formulari complet en diferents seccions, accessibles fent clic als noms de les pestanyes. Les altres pestanyes conserven el seu estat actual. En tots els casos, les opcions de desar i cancel·lar s'apliquen a tots els subformularis dins de les diferents pestanyes.

Accions explícites i ajuda contextual
--------------------------------------

El programa inclou components que proporcionen descripcions addicionals dels elements quan el ratolí es manté sobre d'ells durant un segon. Les accions que l'usuari pot realitzar s'indiquen en les etiquetes dels botons, en els textos d'ajuda associats, en les opcions del menú de navegació i en els menús contextuals que apareixen quan es fa clic dret a l'àrea del planificador. A més, es proporcionen dreceres per a les operacions principals, com ara fer doble clic en elements enumerats o utilitzar esdeveniments de tecla amb el cursor i la tecla Intro per afegir elements en navegar per formularis.
