Rapport gewerkte uren per resource
###################################

.. contents::

Doel
====

Dit rapport extraheert een lijst van taken en de tijd die resources daaraan hebben besteed binnen een bepaalde periode. Verschillende filters stellen gebruikers in staat de zoekopdracht te verfijnen om alleen de gewenste informatie te verkrijgen en overbodige gegevens uit te sluiten.

Invoerparameters en filters
============================

* **Datums**.
    * *Type*: Optioneel.
    * *Twee datumvelden*:
        * *Begindatum:* Dit is de vroegste datum voor urenbriefjes die worden opgenomen. Urenbriefjes met datums eerder dan de *Begindatum* worden uitgesloten. Als deze parameter leeg wordt gelaten, worden urenbriefjes niet gefilterd op *Begindatum*.
        * *Einddatum:* Dit is de laatste datum voor urenbriefjes die worden opgenomen. Urenbriefjes met datums later dan de *Einddatum* worden uitgesloten. Als deze parameter leeg wordt gelaten, worden urenbriefjes niet gefilterd op *Einddatum*.

*   **Filteren op werknemers:**
    *   *Type:* Optioneel.
    *   *Werking:* U kunt één of meer werknemers selecteren om de urenbriefjes te beperken tot de tijd die door die specifieke werknemers is bijgehouden. Om een werknemer als filter toe te voegen, zoekt u ernaar in de selector en klikt u op de knop *Toevoegen*. Als dit filter leeg wordt gelaten, worden urenbriefjes opgehaald ongeacht de werknemer.

*   **Filteren op labels:**
    *   *Type:* Optioneel.
    *   *Werking:* U kunt één of meer labels toevoegen als filters door ernaar te zoeken in de selector en op de knop *Toevoegen* te klikken. Deze labels worden gebruikt om de taken te selecteren die worden opgenomen in de resultaten bij het berekenen van de daaraan bestede uren. Dit filter kan worden toegepast op urenstaten, taken, beide of geen van beide.

*   **Filteren op criteria:**
    *   *Type:* Optioneel.
    *   *Werking:* U kunt één of meer criteria selecteren door ernaar te zoeken in de selector en vervolgens op de knop *Toevoegen* te klikken. Deze criteria worden gebruikt om de resources te selecteren die aan ten minste één ervan voldoen. Het rapport toont alle tijd die is besteed door resources die aan één van de geselecteerde criteria voldoen.

Uitvoer
=======

Koptekst
--------

De koptekst van het rapport toont de filters die zijn geconfigureerd en toegepast op het huidige rapport.

Voettekst
---------

De datum waarop het rapport is gegenereerd staat in de voettekst.

Hoofdtekst
----------

De hoofdtekst van het rapport bestaat uit meerdere informatiegroepen.

*   Het eerste aggregatieniveau is per resource. Alle tijd die door een resource is besteed, wordt samen weergegeven onder de koptekst. Elke resource wordt geïdentificeerd door:

    *   *Werknemer:* Achternaam, Voornaam.
    *   *Machine:* Naam.

    Een samenvattingsregel toont het totale aantal uren dat door de resource is gewerkt.

*   Het tweede groeperingsniveau is per *datum*. Alle rapporten van een specifieke resource op dezelfde datum worden samen weergegeven.

    Een samenvattingsregel toont het totale aantal uren dat door de resource op die datum is gewerkt.

*   Het laatste niveau geeft de urenbriefjes voor de werknemer op die dag weer. De informatie die voor elke urenbriefjesregel wordt weergegeven, is:

    *   *Taakcode:* De code van de taak waaraan de bijgehouden uren worden toegeschreven.
    *   *Taaknaam:* De naam van de taak waaraan de bijgehouden uren worden toegeschreven.
    *   *Begintijd:* Dit is optioneel. Het is het tijdstip waarop de resource begon te werken aan de taak.
    *   *Eindtijd:* Dit is optioneel. Het is het tijdstip waarop de resource klaar was met werken aan de taak op de opgegeven datum.
    *   *Tekstvelden:* Dit is optioneel. Als de urenbriefjesregel tekstvelden heeft, worden de ingevulde waarden hier weergegeven. Het formaat is: <Naam van het tekstveld>:<Waarde>
    *   *Labels:* Dit is afhankelijk van of het urenbriefjesmodel een labelveld heeft in zijn definitie. Als er meerdere labels zijn, worden deze in dezelfde kolom weergegeven. Het formaat is: <Naam van het labeltype>:<Waarde van het label>
