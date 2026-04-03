Planificació de tasques
########################

.. _planificacion:
.. contents::

Planificació de tasques
========================

La planificació a LibrePlan és un procés que s'ha descrit al llarg de la guia de l'usuari, essent els capítols sobre projectes i assignació de recursos especialment importants. Aquest capítol descriu els procediments de planificació bàsics un cop el diagrama de Gantt i el projecte s'han configurat correctament.

.. figure:: images/planning-view.png
   :scale: 35

   Vista de planificació del treball

Com passa amb la vista general de l'empresa, la vista de planificació del projecte es divideix en diverses vistes basades en la informació que s'analitza. Les vistes disponibles per a un projecte específic són:

*   Vista de Planificació
*   Vista de Càrrega de Recursos
*   Vista de Llista de Projectes
*   Vista d'Assignació Avançada

Vista de Planificació
----------------------

La Vista de Planificació combina tres perspectives diferents:

*   **Planificació del projecte:** La planificació del projecte es mostra a la part superior dreta del programa com un diagrama de Gantt. Aquesta vista permet als usuaris moure temporalment les tasques, assignar dependències entre elles, definir fites i establir restriccions.
*   **Càrrega de recursos:** La Vista de Càrrega de Recursos, ubicada a la part inferior dreta de la pantalla, mostra la disponibilitat de recursos basant-se en les assignacions, en oposició a les assignacions realitzades a les tasques. La informació mostrada en aquesta vista és la següent:

    *   **Àrea violeta:** Indica una càrrega de recursos inferior al 100% de la seva capacitat.
    *   **Àrea verda:** Indica una càrrega de recursos inferior al 100%, resultant del fet que el recurs està planificat per a un altre projecte.
    *   **Àrea taronja:** Indica una càrrega de recursos superior al 100% com a resultat del projecte actual.
    *   **Àrea groga:** Indica una càrrega de recursos superior al 100% com a resultat d'altres projectes.

*   **Vista de gràfics i indicadors de valor guanyat:** Es pot visualitzar des de la pestanya "Valor guanyat". El gràfic generat es basa en la tècnica del valor guanyat, i els indicadors es calculen per a cada dia laborable del projecte. Els indicadors calculats són:

    *   **BCWS (Cost Pressupostat del Treball Programat):** La funció temporal acumulativa del nombre d'hores planificades fins a una data determinada. Serà 0 a l'inici planificat de la tasca i igual al nombre total d'hores planificades al final. Com tots els gràfics acumulatius, sempre augmentarà. La funció per a una tasca serà la suma de les assignacions diàries fins a la data de càlcul. Aquesta funció té valors per a tots els moments, sempre que s'hagin assignat recursos.
    *   **ACWP (Cost Real del Treball Realitzat):** La funció temporal acumulativa de les hores reportades en parts de treball fins a una data determinada. Aquesta funció només tindrà un valor de 0 abans de la data del primer part de treball de la tasca, i el seu valor continuarà augmentant a mesura que passi el temps i s'afegeixin hores de parts de treball. No tindrà valor després de la data de l'últim part de treball.
    *   **BCWP (Cost Pressupostat del Treball Realitzat):** La funció temporal acumulativa que inclou el valor resultant de multiplicar el progrés de la tasca per la quantitat de treball que s'estimava que requeriria la tasca per completar-se. Els valors d'aquesta funció augmenten a mesura que passa el temps, com ho fan els valors de progrés. El progrés es multiplica pel nombre total d'hores estimades per a totes les tasques. El valor BCWP és la suma dels valors de les tasques que es calculen. El progrés es totalitza quan es configura.
    *   **CV (Variació de Cost):** CV = BCWP - ACWP
    *   **SV (Variació de Programa):** SV = BCWP - BCWS
    *   **BAC (Pressupost en Finalització):** BAC = màx (BCWS)
    *   **EAC (Estimació en Finalització):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Variació en Finalització):** VAC = BAC - EAC
    *   **ETC (Estimació per Completar):** ETC = EAC - ACWP
    *   **CPI (Índex de Rendiment de Cost):** CPI = BCWP / ACWP
    *   **SPI (Índex de Rendiment de Programa):** SPI = BCWP / BCWS

A la vista de planificació del projecte, els usuaris poden realitzar les accions següents:

*   **Assignar dependències:** Feu clic dret a una tasca, trieu "Afegir dependència" i arrossegueu el punter del ratolí fins a la tasca a la qual s'ha d'assignar la dependència.

    *   Per canviar el tipus de dependència, feu clic dret a la dependència i trieu el tipus desitjat.

*   **Crear una nova fita:** Feu clic a la tasca abans de la qual s'ha d'afegir la fita i seleccioneu l'opció "Afegir fita". Les fites es poden moure seleccionant la fita amb el punter del ratolí i arrossegant-la a la posició desitjada.
*   **Moure tasques sense pertorbar les dependències:** Feu clic dret al cos de la tasca i arrossegueu-la a la posició desitjada. Si no es violen cap restricció ni dependència, el sistema actualitzarà l'assignació diària de recursos a la tasca i la col·locarà a la data seleccionada.
*   **Assignar restriccions:** Feu clic a la tasca en qüestió i seleccioneu l'opció "Propietats de la tasca". Apareixerà una finestra emergent amb un camp "Restriccions" que es pot modificar. Les restriccions poden entrar en conflicte amb les dependències, raó per la qual cada projecte especifica si les dependències tenen prioritat sobre les restriccions. Les restriccions que es poden establir són:

    *   **Tan aviat com sigui possible:** Indica que la tasca ha de començar tan aviat com sigui possible.
    *   **No abans de:** Indica que la tasca no ha de començar abans d'una data determinada.
    *   **Iniciar en una data específica:** Indica que la tasca ha de començar en una data específica.

La vista de planificació també ofereix diversos procediments que funcionen com a opcions de visualització:

*   **Nivell de zoom:** Els usuaris poden triar el nivell de zoom desitjat. Hi ha diversos nivells de zoom: anual, quadrimestral, mensual, setmanal i diari.
*   **Filtres de cerca:** Els usuaris poden filtrar tasques basant-se en etiquetes o criteris.
*   **Camí crític:** Com a resultat de l'ús de l'algorisme *Dijkstra* per calcular camins en gràfics, es va implementar el camí crític. Es pot visualitzar fent clic al botó "Camí crític" en les opcions de visualització.
*   **Mostrar etiquetes:** Permet als usuaris veure les etiquetes assignades a les tasques d'un projecte, que es poden veure a la pantalla o imprimir.
*   **Mostrar recursos:** Permet als usuaris veure els recursos assignats a les tasques d'un projecte, que es poden veure a la pantalla o imprimir.
*   **Imprimir:** Permet als usuaris imprimir el diagrama de Gantt que es visualitza.

Vista de Càrrega de Recursos
------------------------------

La Vista de Càrrega de Recursos proporciona una llista de recursos que conté una llista de tasques o criteris que generen càrregues de treball. Cada tasca o criteri es mostra com un diagrama de Gantt per poder veure les dates d'inici i fi de la càrrega. Es mostra un color diferent depenent de si el recurs té una càrrega superior o inferior al 100%:

*   **Verd:** Càrrega inferior al 100%
*   **Taronja:** Càrrega del 100%
*   **Vermell:** Càrrega superior al 100%

.. figure:: images/resource-load.png
   :scale: 35

   Vista de càrrega de recursos per a un projecte específic

Si el punter del ratolí es col·loca al diagrama de Gantt del recurs, es mostrarà el percentatge de càrrega del treballador.

Vista de Llista de Projectes
-----------------------------

La Vista de Llista de Projectes permet als usuaris accedir a les opcions d'edició i eliminació de projectes. Consulteu el capítol "Projectes" per a més informació.

Vista d'Assignació Avançada
-----------------------------

La Vista d'Assignació Avançada s'explica en profunditat al capítol "Assignació de recursos".
