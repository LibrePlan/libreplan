Rapport: Arbeid og fremdrift per prosjekt
##########################################

.. contents::

Formål
======

Denne rapporten gir en oversikt over statusen til prosjekter, med tanke på både fremdrift og kostnad.

Den analyserer den nåværende fremdriften for hvert prosjekt og sammenligner den med planlagt fremdrift og utført arbeid.

Rapporten viser også flere forholdstall relatert til prosjektkostnad, der nåværende ytelse sammenlignes med planlagt ytelse.

Inngangsparametere og filtre
============================

Det er flere obligatoriske parametere:

   *   **Referansedato:** Dette er datoen som brukes som referansepunkt for å sammenligne den planlagte statusen for prosjektet med den faktiske ytelsen. *Standardverdien for dette feltet er gjeldende dato*.

   *   **Fremdriftstype:** Dette er fremdriftstypen som brukes til å måle prosjektfremdrift. Applikasjonen lar et prosjekt måles samtidig med ulike fremdriftstyper. Typen valgt av brukeren i nedtrekksmenyen brukes til å beregne rapportdataene. Standardverdien for *fremdriftstypen* er *spread*, som er en spesiell fremdriftstype som bruker den foretrukne metoden for å måle fremdrift konfigurert for hvert WBS-element.

De valgfrie parameterne er:

   *   **Startdato:** Dette er den tidligste startdatoen for prosjekter som skal inkluderes i rapporten. Hvis dette feltet er tomt, er det ingen minimumsstartdato for prosjektene.

   *   **Sluttdato:** Dette er den seneste sluttdatoen for prosjekter som skal inkluderes i rapporten. Alle prosjekter som avsluttes etter *Sluttdato* vil bli utelatt.

   *   **Filtrer etter prosjekter:** Dette filteret lar brukerne velge de spesifikke prosjektene som skal inkluderes i rapporten. Hvis ingen prosjekter er lagt til filteret, vil rapporten inkludere alle prosjekter i databasen. En søkbar nedtrekksmeny er tilgjengelig for å finne ønsket prosjekt. Prosjekter legges til filteret ved å klikke *Legg til*-knappen.

Utdata
======

Utdataformatet er som følger:

Topptekst
---------

Rapportens topptekst viser følgende felt:

   *   **Startdato:** Filterets startdato. Vises ikke hvis rapporten ikke er filtrert etter dette feltet.
   *   **Sluttdato:** Filterets sluttdato. Vises ikke hvis rapporten ikke er filtrert etter dette feltet.
   *   **Fremdriftstype:** Fremdriftstypen som brukes for rapporten.
   *   **Prosjekter:** Dette angir de filtrerte prosjektene rapporten er generert for. Det vil vise teksten *Alle* når rapporten inkluderer alle prosjekter som oppfyller de andre filtrene.
   *   **Referansedato:** Den obligatoriske referansedatoen valgt for rapporten.

Bunntekst
---------

Bunnteksten viser datoen rapporten ble generert.

Innhold
-------

Rapportens innhold består av en liste over prosjekter valgt basert på inngangsfiltrene.

Filtre fungerer ved å legge til betingelser, bortsett fra settet dannet av datofiltrene (*Startdato*, *Sluttdato*) og *Filtrer etter prosjekter*. I dette tilfellet, hvis ett eller begge datofiltre er fylt ut og *Filtrer etter prosjekter* har en liste over valgte prosjekter, har sistnevnte filter forrang. Dette betyr at prosjektene som er inkludert i rapporten er de som er oppgitt av *Filtrer etter prosjekter*, uavhengig av datofiltrene.

Det er viktig å merke seg at fremdrift i rapporten beregnes som en brøkdel av enhet, mellom 0 og 1.

For hvert prosjekt valgt for inkludering i rapportutdataene vises følgende informasjon:

   * *Prosjektnavn*.
   * *Totale timer*. Det totale antallet timer for prosjektet vises ved å summere timene for hver oppgave. To typer totale timer vises:
      *   *Estimert (TE)*. Dette er summen av alle estimerte timer i prosjektets WBS. Det representerer det totale antallet timer estimert for å fullføre prosjektet.
      *   *Planlagt (TP)*. I *LibrePlan* er det mulig å ha to forskjellige mengder: det estimerte antallet timer for en oppgave (antallet timer som opprinnelig ble estimert for å fullføre oppgaven) og de planlagte timene (timene tildelt i planen for å fullføre oppgaven). De planlagte timene kan være lik, mindre enn eller større enn de estimerte timene og bestemmes i en senere fase, tildelingsoperasjonen. Derfor er de totale planlagte timene for et prosjekt summen av alle tildelte timer for dets oppgaver.
   * *Fremdrift*. Tre målinger relatert til den samlede fremdriften for typen angitt i fremdriftsinngangsfilteret for hvert prosjekt på referansedatoen vises:
      *   *Målt (PM)*. Dette er den samlede fremdriften med tanke på fremdriftsmålinger med en dato tidligere enn *Referansedato* i rapportens inngangsparametere. Alle oppgaver tas i betraktning, og summen er vektet etter antall timer for hver oppgave.
      *   *Tilregnet (PI)*. Dette er fremdriften under forutsetning av at arbeidet fortsetter i samme tempo som de fullførte timene for en oppgave. Hvis X timer av Y timer for en oppgave er fullført, anses den samlede tilregnede fremdriften å være X/Y.
      *   *Planlagt (PP)*. Dette er den samlede fremdriften for prosjektet i henhold til den planlagte tidsplanen på referansedatoen. Hvis alt skjedde nøyaktig som planlagt, bør den målte fremdriften være den samme som den planlagte fremdriften.
   * *Timer frem til dato*. Det er to felt som viser antall timer frem til referansedatoen fra to perspektiver:
      *   *Planlagt (HP)*. Dette tallet er summen av timer tildelt en oppgave i prosjektet med en dato som er mindre enn eller lik *Referansedato*.
      *   *Faktisk (HR)*. Dette tallet er summen av timer rapportert i arbeidsrapportene for noen av oppgavene i prosjektet med en dato som er mindre enn eller lik *Referansedato*.
   * *Differanse*. Under denne overskriften er det flere mål knyttet til kostnad:
      *   *Kostnad*. Dette er differansen i timer mellom antall timer brukt, med tanke på den målte fremdriften, og timene fullført frem til referansedatoen. Formelen er: *PM*TP - HR*.
      *   *Planlagt*. Dette er differansen mellom timer brukt i henhold til den samlede målte prosjektfremdriften og antallet planlagt frem til *Referansedato*. Det måler fordelen eller forsinkelsen i tid. Formelen er: *PM*TP - HP*.
      *   *Kostnadsforhold*. Dette beregnes ved å dividere *PM* / *PI*. Hvis det er større enn 1, betyr det at prosjektet er lønnsomt på dette punktet. Hvis det er mindre enn 1, betyr det at prosjektet taper penger.
      *   *Planlagt forhold*. Dette beregnes ved å dividere *PM* / *PP*. Hvis det er større enn 1, betyr det at prosjektet er foran tidsplanen. Hvis det er mindre enn 1, betyr det at prosjektet er bak tidsplanen.
