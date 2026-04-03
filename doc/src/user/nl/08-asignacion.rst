Resourcetoewijzing
##################

.. _asigacion_:
.. contents::

Resourcetoewijzing is een van de belangrijkste functies van het programma en kan op twee verschillende manieren worden uitgevoerd:

*   Specifieke toewijzing
*   Generieke toewijzing

Beide typen toewijzing worden in de volgende secties beschreven.

Om een van beide typen resourcetoewijzing uit te voeren, zijn de volgende stappen nodig:

*   Ga naar de planningsweergave van een project.
*   Klik met de rechtermuisknop op de taak die gepland moet worden.

.. figure:: images/resource-assignment-planning.png
   :scale: 50

   Menu Resourcetoewijzing

*   Het programma toont een scherm met de volgende informatie:

    *   **Lijst van te vervullen criteria:** Voor elke uurgroep wordt een lijst met vereiste criteria weergegeven.
    *   **Taakinformatie:** De begin- en einddatum van de taak.
    *   **Type berekening:** Het systeem stelt gebruikers in staat de strategie voor het berekenen van toewijzingen te kiezen:

        *   **Aantal uren berekenen:** Hiermee wordt het aantal benodigde uren van de toegewezen resources berekend, gegeven een einddatum en een aantal resources per dag.
        *   **Einddatum berekenen:** Hiermee wordt de einddatum van de taak berekend op basis van het aantal resources dat aan de taak is toegewezen en het totale aantal uren dat nodig is om de taak te voltooien.
        *   **Aantal resources berekenen:** Hiermee wordt het aantal benodigde resources berekend om de taak tegen een bepaalde datum te voltooien, gegeven een bekend aantal uren per resource.
    *   **Aanbevolen toewijzing:** Met deze optie kan het programma de te vervullen criteria en het totale aantal uren uit alle uurgroepen verzamelen en vervolgens een generieke toewijzing aanbevelen. Als er al een toewijzing bestaat, verwijdert het systeem deze en vervangt het door de nieuwe.
    *   **Toewijzingen:** Een lijst van gemaakte toewijzingen. In deze lijst worden de generieke toewijzingen weergegeven (het aantal is de lijst van vervulde criteria, het aantal uren en resources per dag). Elke toewijzing kan expliciet worden verwijderd door op de verwijderknop te klikken.

.. figure:: images/resource-assignment.png
   :scale: 50

   Resourcetoewijzing

*   Gebruikers selecteren "Resources zoeken."
*   Het programma toont een nieuw scherm bestaande uit een criteriastructuur en een lijst van medewerkers die de geselecteerde criteria aan de rechterkant vervullen:

.. figure:: images/resource-assignment-search.png
   :scale: 50

   Zoeken naar resourcetoewijzing

*   Gebruikers kunnen kiezen:

    *   **Specifieke toewijzing:** Zie de sectie "Specifieke toewijzing" voor details over deze optie.
    *   **Generieke toewijzing:** Zie de sectie "Generieke toewijzing" voor details over deze optie.

*   Gebruikers selecteren een lijst van criteria (generiek) of een lijst van medewerkers (specifiek). Meerdere selecties kunnen worden gemaakt door de "Ctrl"-toets ingedrukt te houden terwijl op elke medewerker/criterium wordt geklikt.
*   Gebruikers klikken vervolgens op de knop "Selecteren". Het is belangrijk te onthouden dat als een generieke toewijzing niet is geselecteerd, gebruikers een medewerker of machine moeten kiezen om de toewijzing uit te voeren. Als een generieke toewijzing is geselecteerd, is het voldoende voor gebruikers om één of meer criteria te kiezen.
*   Het programma toont vervolgens de geselecteerde criteria of resourcelijst in de lijst van toewijzingen op het oorspronkelijke resourcetoewijzingsscherm.
*   Gebruikers moeten de uren of resources per dag kiezen, afhankelijk van de toewijzingsmethode die in het programma wordt gebruikt.

Specifieke toewijzing
=====================

Dit is de specifieke toewijzing van een resource aan een projecttaak. Met andere woorden, de gebruiker beslist welke specifieke medewerker (op naam en achternaam) of machine aan een taak moet worden toegewezen.

Specifieke toewijzing kan worden uitgevoerd op het scherm dat in deze afbeelding wordt getoond:

.. figure:: images/asignacion-especifica.png
   :scale: 50

   Specifieke resourcetoewijzing

Wanneer een resource specifiek wordt toegewezen, maakt het programma dagelijkse toewijzingen op basis van het geselecteerde percentage van dagelijks toegewezen resources, na vergelijking met de beschikbare resourcekalender. Een toewijzing van 0,5 resources voor een taak van 32 uur betekent bijvoorbeeld dat 4 uur per dag worden toegewezen aan de specifieke resource om de taak te voltooien (ervan uitgaande dat de werkkalender 8 uur per dag bedraagt).

Specifieke machinetoewijzing
----------------------------

Specifieke machinetoewijzing werkt op dezelfde manier als medewerkertoew­ijzing. Wanneer een machine aan een taak wordt toegewezen, slaat het systeem een specifieke uurentoewijzing op voor de gekozen machine. Het belangrijkste verschil is dat het systeem de lijst van toegewezen medewerkers of criteria doorzoekt op het moment dat de machine wordt toegewezen:

*   Als de machine een lijst van toegewezen medewerkers heeft, kiest het programma uit degenen die door de machine vereist worden, op basis van de toegewezen kalender. Als de machinekalender bijvoorbeeld 16 uur per dag is en de resourcekalender 8 uur, worden twee resources uit de lijst van beschikbare resources toegewezen.
*   Als de machine één of meer toegewezen criteria heeft, worden generieke toewijzingen gemaakt uit de resources die voldoen aan de aan de machine toegewezen criteria.

Generieke toewijzing
====================

Generieke toewijzing treedt op wanneer gebruikers resources niet specifiek kiezen maar de beslissing aan het programma overlaten, dat de belastingen verdeelt over de beschikbare resources van het bedrijf.

.. figure:: images/asignacion-xenerica.png
   :scale: 50

   Generieke resourcetoewijzing

Het toewijzingssysteem gebruikt de volgende aannames als basis:

*   Taken hebben criteria die van resources vereist worden.
*   Resources zijn geconfigureerd om criteria te vervullen.

Het systeem mislukt echter niet wanneer er geen criteria zijn toegewezen, maar wanneer alle resources voldoen aan de niet-vereiste criteria.

Het generieke toewijzingsalgoritme werkt als volgt:

*   Alle resources en dagen worden behandeld als containers waarin dagelijkse uurentoewijzingen passen, op basis van de maximale toewijzingscapaciteit in de taakkalender.
*   Het systeem zoekt naar de resources die voldoen aan het criterium.
*   Het systeem analyseert welke toewijzingen momenteel verschillende resources hebben die aan criteria voldoen.
*   De resources die aan de criteria voldoen worden gekozen uit degenen met voldoende beschikbaarheid.
*   Als er geen vrijere resources beschikbaar zijn, worden toewijzingen gemaakt aan de resources met minder beschikbaarheid.
*   Overtoewijzing van resources begint pas wanneer alle resources die voldoen aan de respectieve criteria voor 100% zijn toegewezen, totdat het totale benodigde bedrag om de taak uit te voeren is bereikt.

Generieke machinetoewijzing
---------------------------

Generieke machinetoewijzing werkt op dezelfde manier als medewerkertoewi­jzing. Wanneer een machine bijvoorbeeld aan een taak wordt toegewezen, slaat het systeem een generieke uurentoewijzing op voor alle machines die aan de criteria voldoen, zoals beschreven voor resources in het algemeen. Daarnaast voert het systeem de volgende procedure uit voor machines:

*   Voor alle machines die zijn gekozen voor generieke toewijzing:

    *   Het verzamelt de configuratie-informatie van de machine: alfawaarde, toegewezen medewerkers en criteria.
    *   Als de machine een toegewezen lijst van medewerkers heeft, kiest het programma het aantal dat door de machine vereist wordt, afhankelijk van de toegewezen kalender. Als de machinekalender bijvoorbeeld 16 uur per dag is en de resourcekalender 8 uur, wijst het programma twee resources uit de lijst van beschikbare resources toe.
    *   Als de machine één of meer toegewezen criteria heeft, maakt het programma generieke toewijzingen uit de resources die voldoen aan de aan de machine toegewezen criteria.

Geavanceerde toewijzing
=======================

Met geavanceerde toewijzingen kunnen gebruikers toewijzingen ontwerpen die automatisch door de applicatie worden uitgevoerd om ze te personaliseren. Via deze procedure kunnen gebruikers handmatig de dagelijkse uren kiezen die resources besteden aan toegewezen taken of een functie definiëren die op de toewijzing wordt toegepast.

De stappen voor het beheren van geavanceerde toewijzingen zijn:

*   Ga naar het venster voor geavanceerde toewijzing. Er zijn twee manieren om toegang te krijgen tot geavanceerde toewijzingen:

    *   Ga naar een specifiek project en verander de weergave naar geavanceerde toewijzing. In dit geval worden alle taken van het project en toegewezen resources (specifiek en generiek) weergegeven.
    *   Ga naar het venster voor resourcetoewijzing door op de knop "Geavanceerde toewijzing" te klikken. In dit geval worden de toewijzingen die de resources (generiek en specifiek) weergeven die aan een taak zijn toegewezen, getoond.

.. figure:: images/advance-assignment.png
   :scale: 45

   Geavanceerde resourcetoewijzing

*   Gebruikers kunnen het gewenste zoomniveau kiezen:

    *   **Zoomniveaus groter dan één dag:** Als gebruikers de toegewezen uurwaarde wijzigen naar een week-, maand-, viermaands- of zesmaandsperiode, verdeelt het systeem de uren lineair over alle dagen gedurende de gekozen periode.
    *   **Dagelijks zoomen:** Als gebruikers de toegewezen uurwaarde wijzigen naar een dag, gelden deze uren alleen voor die dag. Gebruikers kunnen daardoor bepalen hoeveel uren ze per dag willen toewijzen aan taakresources.

*   Gebruikers kunnen ervoor kiezen een geavanceerde toewijzingsfunctie te ontwerpen. Daarvoor moeten gebruikers:

    *   De functie kiezen uit de selectielijst die naast elke resource verschijnt en klikken op "Configureren."
    *   Het systeem toont een nieuw venster als de gekozen functie specifiek geconfigureerd moet worden. Ondersteunde functies:

        *   **Segmenten:** Een functie waarmee gebruikers segmenten kunnen definiëren waarop een polynoomfunctie wordt toegepast. De functie per segment wordt als volgt geconfigureerd:

            *   **Datum:** De datum waarop het segment eindigt. Als de volgende waarde (lengte) is ingesteld, wordt de datum berekend; anders wordt de lengte berekend.
            *   **De lengte van elk segment definiëren:** Dit geeft aan welk percentage van de taakduur voor het segment nodig is.
            *   **De hoeveelheid werk definiëren:** Dit geeft aan welk werkbelastingspercentage naar verwachting in dit segment wordt voltooid. De hoeveelheid werk moet incrementeel zijn. Als er bijvoorbeeld een segment van 10% is, moet het volgende groter zijn (bijvoorbeeld 20%).
            *   **Segmentgrafieken en geaccumuleerde belastingen.**

    *   Gebruikers klikken vervolgens op "Accepteren."
    *   Het programma slaat de functie op en past deze toe op de dagelijkse resourcetoewijzingen.

.. figure:: images/stretches.png
   :scale: 40

   Configuratie van de segmentfunctie
