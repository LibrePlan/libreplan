Arbeidsrapporter
################

.. contents::

Arbeidsrapporter gjør det mulig å overvåke timene som ressurser dedikerer til oppgavene de er tildelt.

Programmet lar brukere konfigurere nye skjemaer for å registrere dedikerte timer, og spesifisere feltene de ønsker skal vises i disse skjemaene. Dette gjør det mulig å innarbeide rapporter fra oppgaver utført av arbeidere og overvåke arbeideraktivitet.

Før brukere kan legge til oppføringer for ressurser, må de definere minst én arbeidsrapporttype. Denne typen definerer strukturen til rapporten, inkludert alle radene som legges til den. Brukere kan opprette så mange arbeidsrapporttyper som nødvendig i systemet.

Arbeidsrapporttyper
===================

En arbeidsrapport består av en serie felt som er felles for hele rapporten og et sett med arbeidsrapportlinjer med spesifikke verdier for feltene som er definert i hver rad. For eksempel er ressurser og oppgaver felles for alle rapporter. Det kan imidlertid finnes andre nye felt, som "hendelser", som ikke er påkrevd i alle rapporttyper.

Brukere kan konfigurere forskjellige arbeidsrapporttyper slik at et selskap kan utforme sine rapporter for å møte sine spesifikke behov:

.. figure:: images/work-report-types.png
   :scale: 40

   Arbeidsrapporttyper

Administrasjonen av arbeidsrapporttyper lar brukere konfigurere disse typene og legge til nye tekstfelt eller valgfrie koder. I den første fanen for redigering av arbeidsrapporttyper er det mulig å konfigurere typen for de obligatoriske attributtene (om de gjelder hele rapporten eller er spesifisert på linjenivå) og legge til nye valgfrie felt.

De obligatoriske feltene som må vises i alle arbeidsrapporter er som følger:

*   **Navn og kode:** Identifikasjonsfelt for arbeidsrapporttypens navn og kode.
*   **Dato:** Felt for rapportens dato.
*   **Ressurs:** Arbeider eller maskin som vises i rapporten eller arbeidsrapportlinjen.
*   **Prosjektelement:** Kode for prosjektelementet som det utførte arbeidet tilskrives.
*   **Timehåndtering:** Bestemmer timeatribusjonspolicyen som skal brukes, som kan være:

    *   **I henhold til tildelte timer:** Timer tilskrives basert på de tildelte timene.
    *   **I henhold til start- og sluttider:** Timer beregnes basert på start- og sluttider.
    *   **I henhold til antall timer og start- og sluttintervall:** Avvik er tillatt, og antall timer har prioritet.

Brukere kan legge til nye felt i rapportene:

*   **Kodetype:** Brukere kan be systemet om å vise en kode når de fyller ut arbeidsrapporten. For eksempel klientkodetypen, hvis brukeren ønsker å angi klienten som arbeidet ble utført for i hver rapport.
*   **Frie felt:** Felt der tekst kan skrives inn fritt i arbeidsrapporten.

.. figure:: images/work-report-type.png
   :scale: 50

   Opprette en arbeidsrapporttype med personaliserte felt

Brukere kan konfigurere dato-, ressurs- og prosjektelementfelt til å vises i rapporthodet, noe som betyr at de gjelder for hele rapporten, eller de kan legges til i hver av radene.

Til slutt kan nye tilleggstekstfelt eller koder legges til de eksisterende, i arbeidsrapporthodet eller i hver linje, ved å bruke henholdsvis feltene "Tilleggstekst" og "Kodetype". Brukere kan konfigurere rekkefølgen som disse elementene skal angis i, i fanen "Håndtering av tilleggsfelt og koder".

Arbeidsrapportliste
===================

Når formatet på rapportene som skal innarbeides i systemet er konfigurert, kan brukere angi detaljene i det opprettede skjemaet i henhold til strukturen som er definert i den tilsvarende arbeidsrapporttypen. For å gjøre dette må brukerne følge disse trinnene:

*   Klikk på "Ny arbeidsrapport"-knappen tilknyttet ønsket rapport fra listen over arbeidsrapporttyper.
*   Programmet viser deretter rapporten basert på konfigurasjonene gitt for typen. Se det følgende bildet.

.. figure:: images/work-report-type.png
   :scale: 50

   Struktur for arbeidsrapporten basert på type

*   Velg alle feltene som vises for rapporten:

    *   **Ressurs:** Hvis hodet er valgt, vises ressursen bare én gang. Alternativt, for hver linje i rapporten, er det nødvendig å velge en ressurs.
    *   **Oppgavekode:** Kode for oppgaven som arbeidsrapporten tilordnes. I likhet med resten av feltene, hvis feltet er i hodet, angis verdien én gang eller så mange ganger som nødvendig på linjene i rapporten.
    *   **Dato:** Dato for rapporten eller hver linje, avhengig av om hodet eller linjen er konfigurert.
    *   **Antall timer:** Antall arbeidstimer i prosjektet.
    *   **Start- og sluttider:** Start- og sluttider for arbeidet for å beregne endelige arbeidstimer. Dette feltet vises bare i tilfelle timeatribusjonsretningslinjene "I henhold til start- og sluttider" og "I henhold til antall timer og start- og sluttintervall".
    *   **Timetype:** Lar brukere velge timetype, f.eks. "Normal", "Ekstraordinær" osv.

*   Klikk "Lagre" eller "Lagre og fortsett."
