Taakplanning
############

.. _planificacion:
.. contents::

Taakplanning
============

Planning in LibrePlan is een proces dat door de hele gebruikershandleiding is beschreven, waarbij de hoofdstukken over projecten en resourcetoewijzing bijzonder belangrijk zijn. Dit hoofdstuk beschrijft de basisplanningsprocedures nadat het project en Gantt-diagrammen correct zijn geconfigureerd.

.. figure:: images/planning-view.png
   :scale: 35

   Werkplanningsweergave

Net als bij het bedrijfsoverzicht is de projectplanningsweergave verdeeld in meerdere weergaven op basis van de informatie die wordt geanalyseerd. De weergaven die beschikbaar zijn voor een specifiek project zijn:

*   Planningsweergave
*   Resourcebelastingweergave
*   Projectlijstweergave
*   Geavanceerde toewijzingweergave

Planningsweergave
-----------------

De planningsweergave combineert drie verschillende perspectieven:

*   **Projectplanning:** Projectplanning wordt weergegeven in het rechterbovengedeelte van het programma als een Gantt-diagram. Deze weergave stelt gebruikers in staat taken tijdelijk te verplaatsen, afhankelijkheden tussen hen toe te wijzen, mijlpalen te definiëren en beperkingen vast te stellen.
*   **Resourcebelasting:** De resourcebelastingweergave, in het rechterondergedeelte van het scherm, toont de beschikbaarheid van resources op basis van toewijzingen, in tegenstelling tot de toewijzingen aan taken. De informatie weergegeven in deze weergave is als volgt:

    *   **Paars gebied:** Geeft een resourcebelasting aan die lager is dan 100% van zijn capaciteit.
    *   **Groen gebied:** Geeft een resourcebelasting aan die lager is dan 100%, als gevolg van de planning van de resource voor een ander project.
    *   **Oranje gebied:** Geeft een resourcebelasting aan die hoger is dan 100% als gevolg van het huidige project.
    *   **Geel gebied:** Geeft een resourcebelasting aan die hoger is dan 100% als gevolg van andere projecten.

*   **Grafiekweergave en indicatoren van verdiende waarde:** Deze kunnen worden bekeken via het tabblad "Verdiende waarde". De gegenereerde grafiek is gebaseerd op de techniek van verdiende waarde, en de indicatoren worden berekend voor elke werkdag van het project. De berekende indicatoren zijn:

    *   **BCWS (Budgeted Cost of Work Scheduled):** De cumulatieve tijdfunctie voor het aantal uren gepland tot een bepaalde datum. Het zal 0 zijn aan het begin van de geplande taak en gelijk zijn aan het totale aantal geplande uren aan het einde. Zoals bij alle cumulatieve grafieken zal het altijd toenemen. De functie voor een taak is de som van de dagelijkse toewijzingen tot de berekeningsdatum. Deze functie heeft waarden voor alle tijdstippen, mits resources zijn toegewezen.
    *   **ACWP (Actual Cost of Work Performed):** De cumulatieve tijdfunctie voor de uren gerapporteerd in werkrapporten tot een bepaalde datum. Deze functie heeft slechts een waarde van 0 vóór de datum van het eerste werkrapport van de taak, en de waarde blijft toenemen naarmate de tijd verstrijkt en werkrapporturen worden toegevoegd. Na de datum van het laatste werkrapport heeft het geen waarde.
    *   **BCWP (Budgeted Cost of Work Performed):** De cumulatieve tijdfunctie die de resulterende waarde bevat van het vermenigvuldigen van taakvoortgang met de hoeveelheid werk die de taak naar schatting nodig had voor voltooiing. De waarden van deze functie nemen toe naarmate de tijd verstrijkt, net als de voortgangswaarden. Voortgang wordt vermenigvuldigd met het totale aantal geschatte uren voor alle taken. De BCWP-waarde is de som van de waarden voor de taken die worden berekend. Voortgang wordt getotaliseerd wanneer het is geconfigureerd.
    *   **CV (Kostenverschil):** CV = BCWP - ACWP
    *   **SV (Planningsverschil):** SV = BCWP - BCWS
    *   **BAC (Budget bij voltooiing):** BAC = max (BCWS)
    *   **EAC (Schatting bij voltooiing):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Verschil bij voltooiing):** VAC = BAC - EAC
    *   **ETC (Schatting tot voltooiing):** ETC = EAC - ACWP
    *   **CPI (Kostenprestatieindex):** CPI = BCWP / ACWP
    *   **SPI (Planningsprestatieindex):** SPI = BCWP / BCWS

In de projectplanningsweergave kunnen gebruikers de volgende acties uitvoeren:

*   **Afhankelijkheden toewijzen:** Klik met de rechtermuisknop op een taak, kies "Afhankelijkheid toevoegen" en sleep de muisaanwijzer naar de taak waaraan de afhankelijkheid moet worden toegewezen.

    *   Om het type afhankelijkheid te wijzigen, klikt u met de rechtermuisknop op de afhankelijkheid en kiest u het gewenste type.

*   **Een nieuwe mijlpaal aanmaken:** Klik op de taak vóór welke de mijlpaal moet worden toegevoegd en selecteer de optie "Mijlpaal toevoegen". Mijlpalen kunnen worden verplaatst door de mijlpaal te selecteren met de muisaanwijzer en deze naar de gewenste positie te slepen.
*   **Taken verplaatsen zonder afhankelijkheden te verstoren:** Klik met de rechtermuisknop op het taaklichaam en sleep het naar de gewenste positie. Als er geen beperkingen of afhankelijkheden worden geschonden, werkt het systeem de dagelijkse toewijzing van resources aan de taak bij en plaatst het de taak op de geselecteerde datum.
*   **Beperkingen toewijzen:** Klik op de betreffende taak en selecteer de optie "Taakeigenschappen". Er verschijnt een pop-upvenster met een veld "Beperkingen" dat kan worden gewijzigd. Beperkingen kunnen conflicteren met afhankelijkheden, daarom specificeert elk project of afhankelijkheden prioriteit hebben boven beperkingen. De beperkingen die kunnen worden ingesteld zijn:

    *   **Zo snel mogelijk:** Geeft aan dat de taak zo snel mogelijk moet beginnen.
    *   **Niet voor:** Geeft aan dat de taak niet vóór een bepaalde datum mag beginnen.
    *   **Starten op een specifieke datum:** Geeft aan dat de taak op een specifieke datum moet beginnen.

De planningsweergave biedt ook verschillende procedures die functioneren als weergaveopties:

*   **Zoomniveau:** Gebruikers kunnen het gewenste zoomniveau kiezen. Er zijn verschillende zoomniveaus: jaarlijks, viermaandelijks, maandelijks, wekelijks en dagelijks.
*   **Zoekfilters:** Gebruikers kunnen taken filteren op basis van labels of criteria.
*   **Kritiek pad:** Als resultaat van het gebruik van het *Dijkstra*-algoritme voor het berekenen van paden op grafieken, werd het kritieke pad geïmplementeerd. Het kan worden bekeken door op de knop "Kritiek pad" te klikken in de weergaveopties.
*   **Labels tonen:** Stelt gebruikers in staat de labels die aan taken in een project zijn toegewezen te bekijken, die op het scherm kunnen worden bekeken of afgedrukt.
*   **Resources tonen:** Stelt gebruikers in staat de resources die aan taken in een project zijn toegewezen te bekijken, die op het scherm kunnen worden bekeken of afgedrukt.
*   **Afdrukken:** Stelt gebruikers in staat het Gantt-diagram dat wordt bekeken af te drukken.

Resourcebelastingweergave
--------------------------

De resourcebelastingweergave biedt een lijst van resources die een lijst van taken of criteria bevat die werklast genereren. Elke taak of elk criterium wordt weergegeven als een Gantt-diagram zodat de start- en einddatums van de belasting kunnen worden gezien. Er wordt een andere kleur weergegeven afhankelijk van of de resource een belasting heeft die hoger of lager is dan 100%:

*   **Groen:** Belasting lager dan 100%
*   **Oranje:** 100% belasting
*   **Rood:** Belasting hoger dan 100%

.. figure:: images/resource-load.png
   :scale: 35

   Resourcebelastingweergave voor een specifiek project

Als de muisaanwijzer op het Gantt-diagram van de resource wordt geplaatst, wordt het belastingspercentage voor de medewerker weergegeven.

Projectlijstweergave
---------------------

De projectlijstweergave stelt gebruikers in staat de opties voor het bewerken en verwijderen van projecten te openen. Zie het hoofdstuk "Projecten" voor meer informatie.

Geavanceerde Toewijzingweergave
--------------------------------

De geavanceerde toewijzingweergave wordt uitgebreid uitgelegd in het hoofdstuk "Resourcetoewijzing".
