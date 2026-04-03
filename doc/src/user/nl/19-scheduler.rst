Planner
#######

.. contents::

De planner is ontworpen om taken dynamisch in te plannen. Hij is ontwikkeld met behulp van de *Spring Framework Quartz-planner*.

Om deze planner effectief te gebruiken, moeten de taken (Quartz-taken) die gepland moeten worden eerst worden aangemaakt. Vervolgens kunnen deze taken aan de database worden toegevoegd, omdat alle in te plannen taken in de database worden opgeslagen.

Wanneer de planner start, leest hij de in te plannen of uit te plannen taken uit de database en plant of verwijdert ze dienovereenkomstig. Daarna kunnen taken dynamisch worden toegevoegd, bijgewerkt of verwijderd via de gebruikersinterface ``Taakplanning``.

.. NOTE::
   De planner start wanneer de LibrePlan-webapplicatie start en stopt wanneer de applicatie stopt.

.. NOTE::
   Deze planner ondersteunt alleen ``cron-expressies`` voor het plannen van taken.

De criteria die de planner gebruikt om taken in te plannen of te verwijderen wanneer hij start, zijn als volgt:

Voor alle taken:

* Inplannen

  * Taak heeft een *Connector*, en de *Connector* is geactiveerd, en de taak mag worden ingepland.
  * Taak heeft geen *Connector* en mag worden ingepland.

* Verwijderen

  * Taak heeft een *Connector*, en de *Connector* is niet geactiveerd.
  * Taak heeft een *Connector*, en de *Connector* is geactiveerd, maar de taak mag niet worden ingepland.
  * Taak heeft geen *Connector* en mag niet worden ingepland.

.. NOTE::
   Taken kunnen niet opnieuw worden ingepland of worden verwijderd als ze momenteel worden uitgevoerd.

Lijstweergave taakplanning
==========================

De lijstweergave ``Taakplanning`` stelt gebruikers in staat om:

*   Een nieuwe taak toe te voegen.
*   Een bestaande taak te bewerken.
*   Een taak te verwijderen.
*   Een proces handmatig te starten.

Taak toevoegen of bewerken
==========================

Klik vanuit de lijstweergave ``Taakplanning`` op:

*   ``Aanmaken`` om een nieuwe taak toe te voegen, of
*   ``Bewerken`` om de geselecteerde taak te wijzigen.

Beide acties openen een formulier ``taak aanmaken/bewerken``. Het ``formulier`` toont de volgende eigenschappen:

*   Velden:

    *   **Taakgroep:** De naam van de taakgroep.
    *   **Taaknaam:** De naam van de taak.
    *   **Cron-expressie:** Een alleen-lezen veld met een knop ``Bewerken`` om het invoervenster voor de ``cron-expressie`` te openen.
    *   **Naam taakklasse:** Een ``vervolgkeuzelijst`` om de taak te selecteren (een bestaande taak).
    *   **Connector:** Een ``vervolgkeuzelijst`` om een connector te selecteren. Dit is niet verplicht.
    *   **Plannen:** Een selectievakje om aan te geven of deze taak moet worden ingepland.

*   Knoppen:

    *   **Opslaan:** Om een taak in zowel de database als de planner op te slaan of bij te werken. De gebruiker wordt daarna teruggebracht naar de ``lijstweergave Taakplanning``.
    *   **Opslaan en doorgaan:** Hetzelfde als "Opslaan", maar de gebruiker wordt niet teruggebracht naar de ``lijstweergave Taakplanning``.
    *   **Annuleren:** Er wordt niets opgeslagen en de gebruiker wordt teruggebracht naar de ``lijstweergave Taakplanning``.

*   En een sectie met hints over de syntaxis van cron-expressies.

Pop-up voor cron-expressies
---------------------------

Om de ``cron-expressie`` correct in te voeren, wordt een pop-upformulier voor ``cron-expressies`` gebruikt. In dit formulier kunt u de gewenste ``cron-expressie`` invoeren. Zie ook de hint over de ``cron-expressie``. Als u een ongeldige ``cron-expressie`` invoert, wordt u hiervan onmiddellijk op de hoogte gesteld.

Taak verwijderen
================

Klik op de knop ``Verwijderen`` om de taak te verwijderen uit zowel de database als de planner. Het succes of de mislukking van deze actie wordt weergegeven.

Taak handmatig starten
======================

Als alternatief voor het wachten tot de taak wordt uitgevoerd zoals gepland, kunt u op deze knop klikken om het proces direct te starten. Daarna wordt de informatie over succes of mislukking weergegeven in een ``pop-upvenster``.
