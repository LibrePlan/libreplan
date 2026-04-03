Rapport: Totalt antall timer per ressurs per måned
####################################################

.. contents::

Formål
======

Denne rapporten gir det totale antallet timer arbeidet av hver ressurs i en gitt måned. Denne informasjonen kan være nyttig for å fastslå overtid for arbeidere eller, avhengig av organisasjonen, antall timer hver ressurs skal godtgjøres for.

Applikasjonen sporer arbeidsrapporter for både arbeidere og maskiner. For maskiner summerer rapporten antallet timer de var i drift i løpet av måneden.

Inngangsparametere og filtre
============================

For å generere denne rapporten må brukerne angi år og måned de ønsker å hente det totale antallet timer arbeidet av hver ressurs for.

Utdata
======

Utdataformatet er som følger:

Topptekst
---------

Rapportens topptekst viser:

   *   *Året* dataene i rapporten gjelder for.
   *   *Måneden* dataene i rapporten gjelder for.

Bunntekst
---------

Bunnteksten viser datoen rapporten ble generert.

Innhold
-------

Dataseksjonen i rapporten består av én enkelt tabell med to kolonner:

   *   Én kolonne merket **Navn** for ressursens navn.
   *   Én kolonne merket **Timer** med det totale antallet timer arbeidet av ressursen i den raden.

Det er en siste rad som summerer det totale antallet timer arbeidet av alle ressurser i den angitte *måneden* og *året*.
