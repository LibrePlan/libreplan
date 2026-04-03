Criteria
########

.. contents::

Criteria zijn elementen die binnen het programma worden gebruikt om zowel resources als taken te categoriseren. Taken vereisen specifieke criteria en resources moeten aan die criteria voldoen.

Hier is een voorbeeld van hoe criteria worden gebruikt: Een resource krijgt het criterium "lasser" toegewezen (wat betekent dat de resource voldoet aan de categorie "lasser"), en een taak vereist het criterium "lasser" om te worden voltooid. Bijgevolg worden, wanneer resources aan taken worden toegewezen met behulp van generieke toewijzing (in tegenstelling tot specifieke toewijzing), medewerkers met het criterium "lasser" in aanmerking genomen. Voor meer informatie over de verschillende soorten toewijzing, zie het hoofdstuk over resourcetoewijzing.

Het programma staat verschillende bewerkingen toe met betrekking tot criteria:

*   Criteriabeheer
*   Criteria toewijzen aan resources
*   Criteria toewijzen aan taken
*   Entiteiten filteren op basis van criteria. Taken en projectelementen kunnen op criteria worden gefilterd om verschillende bewerkingen binnen het programma uit te voeren.

In dit gedeelte wordt alleen de eerste functie, het criteriabeheer, uitgelegd. De twee soorten toewijzing worden later behandeld: resourcetoewijzing in het hoofdstuk "Resourcebeheer" en filtering in het hoofdstuk "Taakplanning".

Criteriabeheer
==============

Criteriabeheer is toegankelijk via het beheermenu:

.. figure:: images/menu.png
   :scale: 50

   Menu-tabbladen op het eerste niveau

De specifieke bewerking voor het beheren van criteria is *Criteria beheren*. Met deze bewerking kunt u de in het systeem beschikbare criteria weergeven.

.. figure:: images/lista-criterios.png
   :scale: 50

   Lijst van criteria

U kunt het formulier voor het aanmaken/bewerken van criteria openen door op de knop *Aanmaken* te klikken. Om een bestaand criterium te bewerken, klikt u op het bewerkingspictogram.

.. figure:: images/edicion-criterio.png
   :scale: 50

   Criteria bewerken

Het formulier voor het bewerken van criteria, zoals weergegeven in de vorige afbeelding, staat u toe de volgende bewerkingen uit te voeren:

*   **De naam van het criterium bewerken.**
*   **Aangeven of meerdere waarden gelijktijdig kunnen worden toegewezen of slechts één waarde voor het geselecteerde criteriumtype.** Een resource kan bijvoorbeeld aan twee criteria voldoen: "lasser" en "draaibankoperator."
*   **Het criteriumtype opgeven:**

    *   **Generiek:** Een criterium dat zowel voor machines als medewerkers kan worden gebruikt.
    *   **Medewerker:** Een criterium dat alleen voor medewerkers kan worden gebruikt.
    *   **Machine:** Een criterium dat alleen voor machines kan worden gebruikt.

*   **Aangeven of het criterium hiërarchisch is.** Soms moeten criteria hiërarchisch worden behandeld. Het toewijzen van een criterium aan een element wijst dit niet automatisch toe aan elementen die ervan zijn afgeleid. Een duidelijk voorbeeld van een hiërarchisch criterium is "locatie." Een persoon die de locatie "Galicië" heeft toegewezen gekregen, behoort ook tot "Spanje."
*   **Aangeven of het criterium geautoriseerd is.** Dit is hoe gebruikers criteria deactiveren. Zodra een criterium is aangemaakt en gebruikt in historische gegevens, kan het niet worden gewijzigd. In plaats daarvan kan het worden gedeactiveerd om te voorkomen dat het in selectielijsten verschijnt.
*   **Het criterium beschrijven.**
*   **Nieuwe waarden toevoegen.** Een tekstinvoerveld met de knop *Nieuw criterium* bevindt zich in het tweede deel van het formulier.
*   **De namen van bestaande criteriawaarden bewerken.**
*   **Criteriawaarden omhoog of omlaag verplaatsen in de lijst van huidige criteriawaarden.**
*   **Een criteriumwaarde uit de lijst verwijderen.**

Het beheersformulier voor criteria volgt het formuliergedrag dat in de inleiding is beschreven en biedt drie acties: *Opslaan*, *Opslaan en sluiten* en *Sluiten*.
