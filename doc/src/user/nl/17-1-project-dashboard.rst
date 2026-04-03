Projectdashboard
################

.. contents::

Het projectdashboard is een *LibrePlan*-perspectief dat een set **KPI's (Key Performance Indicators)** bevat om de prestaties van een project te beoordelen op het gebied van:

   *   Werkvoortgang
   *   Kosten
   *   Status van toegewezen resources
   *   Tijdsbeperkingen

Voortgangsprestatie-indicatoren
================================

Er worden twee indicatoren berekend: het voortgangspercentage van het project en de taakstatus.

Voortgangspercentage van het project
-------------------------------------

Dit diagram toont de algehele voortgang van een project en vergelijkt deze met de verwachte voortgang op basis van het *Gantt*-diagram.

De voortgang wordt weergegeven door twee balken:

   *   *Huidige voortgang:* De huidige voortgang op basis van de genomen metingen.
   *   *Verwachte voortgang:* De voortgang die het project op dit punt zou moeten hebben bereikt, volgens het projectplan.

Beweeg de muiscursor over de balk om de werkelijk gemeten waarde voor elke balk te bekijken.

De algehele projectvoortgang wordt geschat met behulp van verschillende methoden, omdat er geen enkele universeel correcte aanpak is:

   *   **Verspreidende voortgang:** Dit is het voortgangstype dat is ingesteld als de verspreidende voortgang op projectniveau. In dit geval is er geen manier om een verwachte waarde te berekenen en wordt alleen de huidige balk weergegeven.
   *   **Op basis van alle taaknuren:** De voortgang van alle projecttaken wordt gemiddeld om de algehele waarde te berekenen. Dit is een gewogen gemiddelde dat rekening houdt met het aantal uren dat aan elke taak is toegewezen.
   *   **Op basis van kritiek-pad-uren:** De voortgang van taken die behoren tot een van de kritieke paden van het project wordt gemiddeld om de algehele waarde te verkrijgen. Dit is een gewogen gemiddelde dat rekening houdt met de totale toegewezen uren voor elke betrokken taak.
   *   **Op basis van kritiek-pad-duur:** De voortgang van taken die behoren tot een van de kritieke paden wordt gemiddeld met een gewogen gemiddelde, maar dit keer rekening houdend met de duur van elke betrokken taak in plaats van de toegewezen uren.

Taakstatus
----------

Een cirkeldiagram toont het percentage projecttaken in verschillende staten. De gedefinieerde staten zijn:

   *   **Voltooid:** Voltooide taken, geïdentificeerd door een voortgangswaarde van 100%.
   *   **Bezig:** Taken die momenteel worden uitgevoerd. Deze taken hebben een voortgangswaarde anders dan 0% of 100%, of er is enige werktijd bijgehouden.
   *   **Gereed om te starten:** Taken met 0% voortgang, geen bijgehouden tijd, waarbij alle *FINISH_TO_START*-afhankelijke taken *voltooid* zijn, en alle *START_TO_START*-afhankelijke taken *voltooid* of *bezig* zijn.
   *   **Geblokkeerd:** Taken met 0% voortgang, geen bijgehouden tijd, en met voorgaande afhankelijke taken die noch *bezig* zijn noch de status *gereed om te starten* hebben.

Kostenindicatoren
=================

Er worden verschillende *Earned Value Management*-kostenindicatoren berekend:

   *   **CV (Kostenafwijking):** Het verschil tussen de *Earned Value-curve* en de *Werkelijke kostencurve* op het huidige moment. Positieve waarden wijzen op een voordeel en negatieve waarden op een verlies.
   *   **ACWP (Werkelijke kosten van uitgevoerd werk):** Het totale aantal bijgehouden uren in het project op het huidige moment.
   *   **CPI (Kostenprestatie-index):** De verhouding *Earned Value / Werkelijke kosten*.

        *   > 100 is gunstig, wat aangeeft dat het project onder het budget ligt.
        *   = 100 is ook gunstig, wat aangeeft dat de kosten precies conform het plan zijn.
        *   < 100 is ongunstig, wat aangeeft dat de kosten voor het voltooien van het werk hoger zijn dan gepland.
   *   **ETC (Schatting om te voltooien):** De resterende tijd om het project te voltooien.
   *   **BAC (Budget bij voltooiing):** Het totale hoeveelheid werk dat is toegewezen in het projectplan.
   *   **EAC (Schatting bij voltooiing):** De projectie van de manager van de totale kosten bij projectvoltooiing, op basis van de *CPI*.
   *   **VAC (Afwijking bij voltooiing):** Het verschil tussen de *BAC* en de *EAC*.

        *   < 0 geeft aan dat het project boven het budget ligt.
        *   > 0 geeft aan dat het project onder het budget ligt.

Resources
=========

Om het project vanuit het perspectief van resources te analyseren, worden twee ratio's en een histogram verstrekt.

Histogram voor geschatte afwijking bij voltooide taken
------------------------------------------------------

Dit histogram berekent de afwijking tussen het aantal uren dat is toegewezen aan de projecttaken en het werkelijke aantal uren dat daaraan is besteed.

De afwijking wordt berekend als een percentage voor alle voltooide taken, en de berekende afwijkingen worden weergegeven in een histogram. De verticale as toont het aantal taken binnen elk afwijkingsinterval. Er worden zes afwijkingsintervallen dynamisch berekend.

Overurenratio
-------------

Deze ratio vat de overbelasting samen van resources die zijn toegewezen aan de projecttaken. Het wordt berekend met de formule: **overurenratio = overbelasting / (belasting + overbelasting)**.

   *   = 0 is gunstig, wat aangeeft dat de resources niet overbelast zijn.
   *   > 0 is ongunstig, wat aangeeft dat de resources overbelast zijn.

Beschikbaarheidsratio
---------------------

Deze ratio vat de vrije capaciteit samen van de resources die momenteel zijn toegewezen aan het project. Het meet dus de beschikbaarheid van de resources om meer toewijzingen te ontvangen zonder overbelast te raken. Het wordt berekend als: **beschikbaarheidsratio = (1 - belasting/capaciteit) * 100**

   *   Mogelijke waarden liggen tussen 0% (volledig toegewezen) en 100% (niet toegewezen).

Tijd
====

Er zijn twee diagrammen opgenomen: een histogram voor de tijdsafwijking in de eindtijd van projecttaken en een cirkeldiagram voor deadline-overschrijdingen.

Vroeg of laat voltooide taken
------------------------------

Deze berekening bepaalt het verschil in dagen tussen de geplande eindtijd voor projecttaken en hun werkelijke eindtijd. De geplande voltooiingsdatum wordt overgenomen uit het *Gantt*-diagram, en de werkelijke einddatum wordt overgenomen uit de laatste bijgehouden tijd voor de taak.

De vertraging of het voordeel bij taakvoltooi­ing wordt weergegeven in een histogram. De verticale as toont het aantal taken met een waarde voor het aantal voor-/achterloopdagen dat overeenkomt met het intervalvak op de horizontale as. Er worden zes dynamische taakvoltooi­ingsafwijkingsintervallen berekend.

   *   Negatieve waarden betekenen vroeger dan gepland voltooien.
   *   Positieve waarden betekenen later dan gepland voltooien.

Deadline-overschrijdingen
--------------------------

In deze sectie wordt de marge ten opzichte van de projectdeadline berekend, indien ingesteld. Bovendien toont een cirkeldiagram het percentage taken dat zijn deadline haalt. Er zijn drie soorten waarden opgenomen in het diagram:

   *   Percentage taken zonder geconfigureerde deadline.
   *   Percentage voltooide taken met een werkelijke einddatum later dan hun deadline. De werkelijke einddatum wordt overgenomen uit de laatste bijgehouden tijd voor de taak.
   *   Percentage voltooide taken met een werkelijke einddatum eerder dan hun deadline.
