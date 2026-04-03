Prosjektinstrumentbord
######################

.. contents::

Prosjektinstrumentbordet er et *LibrePlan*-perspektiv som inneholder et sett med **KPI-er (nøkkelytelsesindikatorer)** for å hjelpe med å vurdere et prosjekts ytelse med tanke på:

   *   Arbeidsfremdrift
   *   Kostnad
   *   Status for tildelte ressurser
   *   Tidsbegrensninger

Fremdriftsytelsesindikatorer
============================

To indikatorer beregnes: prosjektets fremdriftsprosent og oppgavestatus.

Prosjektets fremdriftsprosent
-----------------------------

Dette diagrammet viser den samlede fremdriften for et prosjekt og sammenligner den med forventet fremdrift basert på *Gantt*-diagrammet.

Fremdrift representeres av to søyler:

   *   *Nåværende fremdrift:* Den nåværende fremdriften basert på de utførte målingene.
   *   *Forventet fremdrift:* Fremdriften prosjektet burde ha oppnådd på dette punktet, i henhold til prosjektplanen.

For å se den faktiske målte verdien for hver søyle, hold musepekeren over søylen.

Den samlede prosjektfremdriften estimeres ved hjelp av flere ulike metoder, ettersom det ikke finnes én enkelt universelt korrekt tilnærming:

   *   **Spredningsfremdrift:** Dette er fremdriftstypen satt som spredningsfremdrift på prosjektnivå. I dette tilfellet er det ingen måte å beregne en forventet verdi på, og bare den nåværende søylen vises.
   *   **Etter alle oppgavetimer:** Fremdriften for alle prosjektoppgaver beregnes som gjennomsnitt for å beregne den samlede verdien. Dette er et vektet gjennomsnitt som tar hensyn til antall timer tildelt hver oppgave.
   *   **Etter kritisk bane-timer:** Fremdriften for oppgaver som tilhører noen av prosjektets kritiske baner beregnes som gjennomsnitt for å oppnå den samlede verdien. Dette er et vektet gjennomsnitt som tar hensyn til de totale tildelte timene for hver involvert oppgave.
   *   **Etter kritisk bane-varighet:** Fremdriften for oppgaver som tilhører noen av de kritiske banene beregnes som vektet gjennomsnitt, men denne gangen med tanke på varigheten av hver involvert oppgave i stedet for de tildelte timene.

Oppgavestatus
-------------

Et sektordiagram viser prosentandelen av prosjektoppgaver i ulike tilstander. De definerte tilstandene er:

   *   **Fullført:** Fullførte oppgaver, identifisert med en fremdriftsverdi på 100%.
   *   **Pågår:** Oppgaver som er i gang. Disse oppgavene har en fremdriftsverdi som ikke er 0% eller 100%, eller noe arbeidstid er registrert.
   *   **Klar til å starte:** Oppgaver med 0% fremdrift, ingen registrert tid, alle deres *FINISH_TO_START*-avhengige oppgaver er *fullført*, og alle deres *START_TO_START*-avhengige oppgaver er *fullført* eller *pågår*.
   *   **Blokkert:** Oppgaver med 0% fremdrift, ingen registrert tid, og med foregående avhengige oppgaver som verken *pågår* eller er i *klar til å starte*-tilstand.

Kostnadsindikatorer
===================

Flere *Earned Value Management*-kostnadsindikatorer beregnes:

   *   **CV (Kostnadsavvik):** Differansen mellom *Opptjent verdi-kurven* og *Faktisk kostnad-kurven* på det nåværende tidspunktet. Positive verdier indikerer en fordel, og negative verdier indikerer et tap.
   *   **ACWP (Faktisk kostnad for utført arbeid):** Det totale antallet timer registrert i prosjektet på det nåværende tidspunktet.
   *   **CPI (Kostnadsytelsesindeks):** Forholdet *Opptjent verdi / Faktisk kostnad*.

        *   > 100 er gunstig, og indikerer at prosjektet er under budsjett.
        *   = 100 er også gunstig, og indikerer at kostnaden er nøyaktig i henhold til planen.
        *   < 100 er ugunstig, og indikerer at kostnaden for å fullføre arbeidet er høyere enn planlagt.
   *   **ETC (Estimat for å fullføre):** Gjenværende tid for å fullføre prosjektet.
   *   **BAC (Budsjett ved fullføring):** Det totale mengden arbeid tildelt i prosjektplanen.
   *   **EAC (Estimat ved fullføring):** Lederens projeksjon av totalkostnaden ved prosjektfullføring, basert på *CPI*.
   *   **VAC (Avvik ved fullføring):** Differansen mellom *BAC* og *EAC*.

        *   < 0 indikerer at prosjektet er over budsjett.
        *   > 0 indikerer at prosjektet er under budsjett.

Ressurser
=========

For å analysere prosjektet fra ressursenes synspunkt er det to forholdstall og et histogram tilgjengelig.

Histogram over estimeringsavvik for fullførte oppgaver
------------------------------------------------------

Dette histogrammet beregner avviket mellom antall timer tildelt prosjektoppgavene og det faktiske antallet timer brukt på dem.

Avviket beregnes som en prosentandel for alle fullførte oppgaver, og de beregnede avvikene representeres i et histogram. Den vertikale aksen viser antall oppgaver innenfor hvert avviksintervall. Seks avviksintervaller beregnes dynamisk.

Overtidsforhold
---------------

Dette forholdet oppsummerer overbelastningen av ressurser tildelt prosjektoppgavene. Det beregnes ved hjelp av formelen: **overtidsforhold = overbelastning / (belastning + overbelastning)**.

   *   = 0 er gunstig, og indikerer at ressursene ikke er overbelastet.
   *   > 0 er ugunstig, og indikerer at ressursene er overbelastet.

Tilgjengelighetsforhold
-----------------------

Dette forholdet oppsummerer den frie kapasiteten til ressursene som for øyeblikket er tildelt prosjektet. Derfor måler det ressursenes tilgjengelighet for å motta flere tildelinger uten å bli overbelastet. Det beregnes som: **tilgjengelighetsforhold = (1 - belastning/kapasitet) * 100**

   *   Mulige verdier er mellom 0% (fullt tildelt) og 100% (ikke tildelt).

Tid
===

To diagrammer er inkludert: et histogram for tidsavviket i sluttidspunktet for prosjektoppgaver og et sektordiagram for fristoverskridelser.

Forskudd eller forsinkelse i oppgavefullføring
----------------------------------------------

Denne beregningen fastslår differansen i dager mellom det planlagte sluttidspunktet for prosjektoppgaver og deres faktiske sluttidspunkt. Den planlagte fullføringsdatoen er hentet fra *Gantt*-diagrammet, og den faktiske sluttdatoen er hentet fra den siste registrerte tiden for oppgaven.

Forskudd eller forsinkelse i oppgavefullføring representeres i et histogram. Den vertikale aksen viser antall oppgaver med en forskudd/forsinkelse-dagverdi som tilsvarer abscissedagsintervallet. Seks dynamiske avviksintervaller for oppgavefullføring beregnes.

   *   Negative verdier betyr fullføring foran tidsplanen.
   *   Positive verdier betyr fullføring bak tidsplanen.

Fristoverskridelser
-------------------

Denne seksjonen beregner marginen til prosjektfristen, hvis den er satt. I tillegg viser et sektordiagram prosentandelen av oppgaver som overholder fristen sin. Tre typer verdier er inkludert i diagrammet:

   *   Prosentandel av oppgaver uten konfigurert frist.
   *   Prosentandel av avsluttede oppgaver med en faktisk sluttdato som er etter fristen. Den faktiske sluttdatoen er hentet fra den siste registrerte tiden for oppgaven.
   *   Prosentandel av avsluttede oppgaver med en faktisk sluttdato som er før fristen.
