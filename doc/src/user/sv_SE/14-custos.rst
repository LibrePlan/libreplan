Kostnadshantering
#################

.. _costes:
.. contents::

Kostnader
=========

Kostnadshantering låter användare uppskatta kostnaderna för resurser som används i ett projekt. För att hantera kostnader måste följande entiteter definieras:

*   **Timtyper:** Dessa anger typerna av timmar som arbetatS av en resurs. Användare kan definiera timtyper för både maskiner och arbetare. Exempel på timtyper: "Extra timmar till €20 per timme." Följande fält kan definieras för timtyper:

    *   **Kod:** Extern kod för timtypen.
    *   **Namn:** Namn på timtypen. Till exempel "Extra".
    *   **Standardtaxa:** Grundläggande standardtaxa för timtypen.
    *   **Aktivering:** Anger om timtypen är aktiv eller inte.

*   **Kostnadskategorier:** Kostnadskategorier definierar kostnaderna som är förknippade med olika timtyper under specifika perioder (som kan vara obegränsade). Till exempel är kostnaden för extra timmar för första gradens yrkesarbetare nästa år €24 per timme. Kostnadskategorier inkluderar:

    *   **Namn:** Kostnadskategorins namn.
    *   **Aktivering:** Anger om kategorin är aktiv eller inte.
    *   **Lista över timtyper:** Denna lista definierar timtyperna som ingår i kostnadskategorin. Den specificerar perioder och taxor för varje timtyp. Till exempel, när taxorna ändras, kan varje år ingå i denna lista som en timtypsperiod med en specifik timtaxa för varje timtyp (som kan skilja sig från standardtimtaxan för den timtypen).

Hantera timtyper
----------------

Användare måste följa dessa steg för att registrera timtyper:

*   Välj "Hantera arbetade timtyper" i menyn "Administration".
*   Programmet visar en lista över befintliga timtyper.

.. figure:: images/hour-type-list.png
   :scale: 35

   Lista över timtyper

*   Klicka på "Redigera" eller "Skapa".
*   Programmet visar ett redigeringsformulär för timtypen.

.. figure:: images/hour-type-edit.png
   :scale: 50

   Redigera timtyper

*   Användare kan ange eller ändra:

    *   Timtypens namn.
    *   Timtypens kod.
    *   Standardtaxan.
    *   Aktivering/inaktivering av timtypen.

*   Klicka på "Spara" eller "Spara och fortsätt".

Kostnadskategorier
------------------

Användare måste följa dessa steg för att registrera kostnadskategorier:

*   Välj "Hantera kostnadskategorier" i menyn "Administration".
*   Programmet visar en lista över befintliga kategorier.

.. figure:: images/category-cost-list.png
   :scale: 50

   Lista över kostnadskategorier

*   Klicka på knappen "Redigera" eller "Skapa".
*   Programmet visar ett redigeringsformulär för kostnadskategorin.

.. figure:: images/category-cost-edit.png
   :scale: 50

   Redigera kostnadskategorier

*   Användare anger eller ändrar:

    *   Kostnadskategorins namn.
    *   Aktivering/inaktivering av kostnadskategorin.
    *   Listan över timtyper som ingår i kategorin. Alla timtyper har följande fält:

        *   **Timtyp:** Välj en av de befintliga timtyperna i systemet. Om ingen finns måste en timtyp skapas (denna process förklaras i föregående underavsnitt).
        *   **Start- och slutdatum:** Start- och slutdatum (det senare är valfritt) för den period som gäller för kostnadskategorin.
        *   **Timtaxa:** Timtaxan för denna specifika kategori.

*   Klicka på "Spara" eller "Spara och fortsätt".

Tilldelningen av kostnadskategorier till resurser beskrivs i kapitlet om resurser. Gå till avsnittet "Resurser".
