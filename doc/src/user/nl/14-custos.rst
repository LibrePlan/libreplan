Kostenbeheer
############

.. _costes:
.. contents::

Kosten
======

Met kostenbeheer kunnen gebruikers de kosten schatten van resources die in een project worden gebruikt. Om kosten te beheren, moeten de volgende entiteiten worden gedefinieerd:

*   **Uurtypes:** Deze geven de soorten uren aan die door een resource worden gewerkt. Gebruikers kunnen uurtypes definiëren voor zowel machines als werknemers. Voorbeelden van uurtypes zijn: "Extra uren betaald à €20 per uur." De volgende velden kunnen worden gedefinieerd voor uurtypes:

    *   **Code:** Externe code voor het uurtype.
    *   **Naam:** Naam van het uurtype. Bijvoorbeeld "Extra".
    *   **Standaardtarief:** Basistarieftarief voor het uurtype.
    *   **Activering:** Geeft aan of het uurtype actief is of niet.

*   **Kostencategorieën:** Kostencategorieën definiëren de kosten die gekoppeld zijn aan verschillende uurtypes gedurende specifieke perioden (die onbepaald kunnen zijn). Bijvoorbeeld: de kosten van extra uren voor eersteklasse vakarbeiders in het volgende jaar zijn €24 per uur. Kostencategorieën bevatten:

    *   **Naam:** Naam van de kostencategorie.
    *   **Activering:** Geeft aan of de categorie actief is of niet.
    *   **Lijst van uurtypes:** Deze lijst definieert de uurtypes die zijn opgenomen in de kostencategorie. De perioden en tarieven voor elk uurtype worden hierin vastgelegd. Omdat tarieven veranderen, kan elk jaar worden opgenomen in deze lijst als een uurtype-periode, met een specifiek uurtarief voor elk uurtype (dat kan afwijken van het standaard uurtarief voor dat uurtype).

Uurtypes beheren
----------------

Gebruikers moeten de volgende stappen volgen om uurtypes te registreren:

*   Selecteer "Gewerkte uurtypes beheren" in het menu "Beheer".
*   Het programma toont een lijst van bestaande uurtypes.

.. figure:: images/hour-type-list.png
   :scale: 35

   Lijst van uurtypes

*   Klik op "Bewerken" of "Aanmaken".
*   Het programma toont een bewerkingsformulier voor het uurtype.

.. figure:: images/hour-type-edit.png
   :scale: 50

   Uurtypes bewerken

*   Gebruikers kunnen het volgende invoeren of wijzigen:

    *   De naam van het uurtype.
    *   De code van het uurtype.
    *   Het standaardtarief.
    *   Activering/deactivering van het uurtype.

*   Klik op "Opslaan" of "Opslaan en doorgaan".

Kostencategorieën
-----------------

Gebruikers moeten de volgende stappen volgen om kostencategorieën te registreren:

*   Selecteer "Kostencategorieën beheren" in het menu "Beheer".
*   Het programma toont een lijst van bestaande categorieën.

.. figure:: images/category-cost-list.png
   :scale: 50

   Lijst van kostencategorieën

*   Klik op de knop "Bewerken" of "Aanmaken".
*   Het programma toont een bewerkingsformulier voor kostencategorieën.

.. figure:: images/category-cost-edit.png
   :scale: 50

   Kostencategorieën bewerken

*   Gebruikers voeren het volgende in of wijzigen het:

    *   De naam van de kostencategorie.
    *   De activering/deactivering van de kostencategorie.
    *   De lijst van uurtypes die in de categorie zijn opgenomen. Alle uurtypes hebben de volgende velden:

        *   **Uurtype:** Kies een van de bestaande uurtypes in het systeem. Als er geen bestaan, moet een uurtype worden aangemaakt (dit proces wordt uitgelegd in de vorige subsectie).
        *   **Begin- en einddatum:** De begin- en einddatum (de laatste is optioneel) voor de periode die van toepassing is op de kostencategorie.
        *   **Uurtarief:** Het uurtarief voor deze specifieke categorie.

*   Klik op "Opslaan" of "Opslaan en doorgaan".

De toewijzing van kostencategorieën aan resources wordt beschreven in het hoofdstuk over resources. Ga naar de sectie "Resources".
