Kostnadsadministrasjon
######################

.. _costes:
.. contents::

Kostnader
=========

Kostnadsadministrasjon lar brukere estimere kostnadene for ressurser som brukes i et prosjekt. For å administrere kostnader må følgende enheter defineres:

*   **Timetyper:** Disse angir typene timer arbeidet av en ressurs. Brukere kan definere timetyper for både maskiner og arbeidere. Eksempler på timetyper inkluderer: "Tilleggstimer betalt med €20 per time." Følgende felt kan defineres for timetyper:

    *   **Kode:** Ekstern kode for timetypen.
    *   **Navn:** Navn på timetypen. For eksempel "Tillegg".
    *   **Standard sats:** Grunnleggende standard sats for timetypen.
    *   **Aktivering:** Angir om timetypen er aktiv eller ikke.

*   **Kostnadskategorier:** Kostnadskategorier definerer kostnadene knyttet til forskjellige timetyper i bestemte perioder (som kan være ubestemte). For eksempel er kostnaden for tilleggstimer for faglærte arbeidere av første grad neste år €24 per time. Kostnadskategorier inkluderer:

    *   **Navn:** Navn på kostnadskategorien.
    *   **Aktivering:** Angir om kategorien er aktiv eller ikke.
    *   **Liste over timetyper:** Denne listen definerer timetypene som er inkludert i kostnadskategorien. Den spesifiserer periodene og satsene for hver timetype. For eksempel, ettersom satsene endres, kan hvert år inkluderes i denne listen som en timetypeperiode, med en spesifikk timesats for hver timetype (som kan avvike fra standard timesats for den timetypen).

Administrere timetyper
-----------------------

Brukere må følge disse trinnene for å registrere timetyper:

*   Velg "Administrer timetyper arbeidet" i "Administrasjon"-menyen.
*   Programmet viser en liste over eksisterende timetyper.

.. figure:: images/hour-type-list.png
   :scale: 35

   Liste over timetyper

*   Klikk "Rediger" eller "Opprett".
*   Programmet viser et redigeringsskjema for timetype.

.. figure:: images/hour-type-edit.png
   :scale: 50

   Redigere timetyper

*   Brukere kan angi eller endre:

    *   Timetypenavnet.
    *   Timetypekoden.
    *   Standardsatsen.
    *   Aktivering/deaktivering av timetype.

*   Klikk "Lagre" eller "Lagre og fortsett."

Kostnadskategorier
------------------

Brukere må følge disse trinnene for å registrere kostnadskategorier:

*   Velg "Administrer kostnadskategorier" i "Administrasjon"-menyen.
*   Programmet viser en liste over eksisterende kategorier.

.. figure:: images/category-cost-list.png
   :scale: 50

   Liste over kostnadskategorier

*   Klikk på "Rediger"- eller "Opprett"-knappen.
*   Programmet viser et redigeringsskjema for kostnadskategori.

.. figure:: images/category-cost-edit.png
   :scale: 50

   Redigere kostnadskategorier

*   Brukere angir eller endrer:

    *   Navnet på kostnadskategorien.
    *   Aktivering/deaktivering av kostnadskategorien.
    *   Listen over timetyper som er inkludert i kategorien. Alle timetyper har følgende felt:

        *   **Timetype:** Velg én av de eksisterende timetypene i systemet. Hvis ingen finnes, må en timetype opprettes (denne prosessen er forklart i forrige underavsnitt).
        *   **Start- og sluttdato:** Start- og sluttdatoene (sistnevnte er valgfri) for perioden som gjelder for kostnadskategorien.
        *   **Timesats:** Timesatsen for denne spesifikke kategorien.

*   Klikk "Lagre" eller "Lagre og fortsett."

Tilordning av kostnadskategorier til ressurser er beskrevet i kapittelet om ressurser. Gå til "Ressurser"-avsnittet.
