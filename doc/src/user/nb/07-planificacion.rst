Oppgaveplanlegging
##################

.. _planificacion:
.. contents::

Oppgaveplanlegging
===================

Planlegging i LibrePlan er en prosess som er beskrevet gjennom hele brukerhåndboken, der kapitlene om prosjekter og ressurstildeling er særlig viktige. Dette kapitlet beskriver de grunnleggende planleggingsprosedyrene etter at prosjektet og Gantt-diagrammene er riktig konfigurert.

.. figure:: images/planning-view.png
   :scale: 35

   Visning av arbeidsplanlegging

Som med bedriftsoversikten er prosjektplanleggingsvisningen delt inn i flere visninger basert på informasjonen som analyseres. Visningene som er tilgjengelige for et spesifikt prosjekt, er:

*   Planleggingsvisning
*   Ressursbelastningsvisning
*   Prosjektlistevisning
*   Avansert tildelingsvisning

Planleggingsvisning
--------------------

Planleggingsvisningen kombinerer tre ulike perspektiver:

*   **Prosjektplanlegging:** Prosjektplanlegging vises i øvre høyre del av programmet som et Gantt-diagram. Denne visningen lar brukere flytte oppgaver midlertidig, tildele avhengigheter mellom dem, definere milepæler og etablere restriksjoner.
*   **Ressursbelastning:** Ressursbelastningsvisningen, som er plassert i nedre høyre del av skjermen, viser ressursstilgjengelighet basert på tildelinger, i motsetning til tildelingene som er gjort til oppgaver. Informasjonen som vises i denne visningen, er som følger:

    *   **Lilla område:** Indikerer en ressursbelastning under 100 % av kapasiteten.
    *   **Grønt område:** Indikerer en ressursbelastning under 100 %, som følge av at ressursen er planlagt for et annet prosjekt.
    *   **Oransje område:** Indikerer en ressursbelastning over 100 % som følge av det gjeldende prosjektet.
    *   **Gult område:** Indikerer en ressursbelastning over 100 % som følge av andre prosjekter.

*   **Grafvisning og indikatorer for opptjent verdi:** Disse kan vises fra fanen "Opptjent verdi". Den genererte grafen er basert på teknikken for opptjent verdi, og indikatorene beregnes for hver arbeidsdag i prosjektet. De beregnede indikatorene er:

    *   **BCWS (Budsjettert kostnad for planlagt arbeid):** Den kumulative tidsfunksjonen for antall planlagte timer til en bestemt dato. Den vil være 0 ved den planlagte starten på oppgaven og lik det totale antallet planlagte timer på slutten. Som med alle kumulative grafer vil den alltid øke. Funksjonen for en oppgave vil være summen av de daglige tildelingene til beregningsdatoen. Denne funksjonen har verdier for alle tider, forutsatt at ressurser er tildelt.
    *   **ACWP (Faktisk kostnad for utført arbeid):** Den kumulative tidsfunksjonen for timene rapportert i arbeidsrapporter til en bestemt dato. Denne funksjonen vil bare ha en verdi på 0 før datoen for oppgavens første arbeidsrapport, og verdien vil fortsette å øke etter hvert som tid går og arbeidsrapporttimer legges til. Den vil ikke ha noen verdi etter datoen for den siste arbeidsrapporten.
    *   **BCWP (Budsjettert kostnad for utført arbeid):** Den kumulative tidsfunksjonen som inkluderer den resulterende verdien av å multiplisere oppgavefremdriften med mengden arbeid som oppgaven ble estimert å kreve for fullføring. Verdiene for denne funksjonen øker etter hvert som tid går, det samme gjelder fremdriftsverdier. Fremdriften multipliseres med det totale antallet estimerte timer for alle oppgaver. BCWP-verdien er summen av verdiene for oppgavene som beregnes. Fremdriften summeres når den er konfigurert.
    *   **CV (Kostvarians):** CV = BCWP - ACWP
    *   **SV (Planleggingsvarians):** SV = BCWP - BCWS
    *   **BAC (Budsjett ved fullføring):** BAC = maks (BCWS)
    *   **EAC (Estimat ved fullføring):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Varians ved fullføring):** VAC = BAC - EAC
    *   **ETC (Estimat for å fullføre):** ETC = EAC - ACWP
    *   **CPI (Kostnadsytelsesindeks):** CPI = BCWP / ACWP
    *   **SPI (Planleggingsytelsesindeks):** SPI = BCWP / BCWS

I prosjektplanleggingsvisningen kan brukere utføre følgende handlinger:

*   **Tildele avhengigheter:** Høyreklikk på en oppgave, velg "Legg til avhengighet" og dra musepekeren til oppgaven som avhengigheten skal tildeles til.

    *   For å endre avhengighetstypen, høyreklikk på avhengigheten og velg ønsket type.

*   **Opprette en ny milepæl:** Klikk på oppgaven som milepælen skal legges til foran, og velg alternativet "Legg til milepæl". Milepæler kan flyttes ved å velge milepælen med musepekeren og dra den til ønsket posisjon.
*   **Flytte oppgaver uten å forstyrre avhengigheter:** Høyreklikk på oppgavens kropp og dra den til ønsket posisjon. Hvis ingen restriksjoner eller avhengigheter brytes, vil systemet oppdatere den daglige tildelingen av ressurser til oppgaven og plassere oppgaven på den valgte datoen.
*   **Tildele restriksjoner:** Klikk på den aktuelle oppgaven og velg alternativet "Oppgaveegenskaper". Et popup-vindu vises med et "Restriksjoner"-felt som kan endres. Restriksjoner kan komme i konflikt med avhengigheter, og det er derfor hvert prosjekt angir om avhengigheter har prioritet over restriksjoner. Restriksjonene som kan etableres, er:

    *   **Så tidlig som mulig:** Indikerer at oppgaven må starte så tidlig som mulig.
    *   **Ikke før:** Indikerer at oppgaven ikke må starte før en bestemt dato.
    *   **Start på en bestemt dato:** Indikerer at oppgaven må starte på en bestemt dato.

Planleggingsvisningen tilbyr også flere prosedyrer som fungerer som visningsalternativer:

*   **Zoomnivå:** Brukere kan velge ønsket zoomnivå. Det finnes flere zoomnivåer: årlig, firemånedlig, månedlig, ukentlig og daglig.
*   **Søkefiltre:** Brukere kan filtrere oppgaver basert på etiketter eller kriterier.
*   **Kritisk sti:** Som et resultat av å bruke *Dijkstra*-algoritmen til å beregne stier på grafer, ble den kritiske stien implementert. Den kan vises ved å klikke på "Kritisk sti"-knappen i visningsalternativene.
*   **Vis etiketter:** Gjør det mulig for brukere å se etikettene som er tildelt oppgaver i et prosjekt, som kan vises på skjermen eller skrives ut.
*   **Vis ressurser:** Gjør det mulig for brukere å se ressursene som er tildelt oppgaver i et prosjekt, som kan vises på skjermen eller skrives ut.
*   **Skriv ut:** Gjør det mulig for brukere å skrive ut Gantt-diagrammet som vises.

Ressursbelastningsvisning
--------------------------

Ressursbelastningsvisningen gir en liste over ressurser som inneholder en liste over oppgaver eller kriterier som genererer arbeidsbelastninger. Hver oppgave eller kriterium vises som et Gantt-diagram slik at start- og sluttdatoene for belastningen kan ses. En annen farge vises avhengig av om ressursen har en belastning som er høyere eller lavere enn 100 %:

*   **Grønt:** Belastning lavere enn 100 %
*   **Oransje:** 100 % belastning
*   **Rødt:** Belastning over 100 %

.. figure:: images/resource-load.png
   :scale: 35

   Ressursbelastningsvisning for et spesifikt prosjekt

Hvis musepekeren plasseres på ressursens Gantt-diagram, vises belastningsprosenten for arbeideren.

Prosjektlistevisning
---------------------

Prosjektlistevisningen gir brukere tilgang til alternativene for å redigere og slette prosjekter. Se kapitlet "Prosjekter" for mer informasjon.

Avansert tildelingsvisning
---------------------------

Den avanserte tildelingsvisningen er grundig forklart i kapitlet "Ressurstildeling".
