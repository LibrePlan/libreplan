Koblinger
#########

.. contents::

Koblinger er *LibrePlan*-klientapplikasjoner som kan brukes til å kommunisere med (web)servere for å hente data, behandle det og lagre det. For øyeblikket finnes det tre koblinger: JIRA-koblingen, Tim Enterprise-koblingen og e-postkoblingen.

Konfigurasjon
=============

Koblinger må konfigureres riktig før de kan brukes. De kan konfigureres fra skjermen "Hovedinnstillinger" under fanen "Koblinger".

Koblingsbildet inkluderer:

*   **Nedtrekksliste:** En liste over tilgjengelige koblinger.
*   **Egenskapsredigeringsskjerm:** Et egenskapsredigeringsskjema for den valgte koblingen.
*   **Test tilkobling-knapp:** En knapp for å teste tilkoblingen med koblingen.

Velg koblingen du vil konfigurere fra nedtrekkslisten over koblinger. Et egenskapsredigeringsskjema for den valgte koblingen vil vises. I egenskapsredigeringsskjemaet kan du endre egenskapsverdiene etter behov og teste konfigurasjonene ved hjelp av knappen "Test tilkobling".

.. NOTE::

   Egenskapene er konfigurert med standardverdier. Den viktigste egenskapen er "Aktivert." Som standard er den satt til "N." Dette indikerer at koblingen ikke vil bli brukt med mindre du endrer verdien til "Y" og lagrer endringene.

JIRA-kobling
============

JIRA er et system for sporings av saker og prosjekter.

JIRA-koblingen er en applikasjon som kan brukes til å anmode om data fra JIRA-webserveren for JIRA-saker og behandle svaret. Forespørselen er basert på JIRA-etiketter. I JIRA kan etiketter brukes til å kategorisere saker. Forespørselen er strukturert som følger: hent alle saker som er kategorisert med dette etikettnavnet.

Koblingen mottar svaret, som i dette tilfellet er sakene, og konverterer dem til *LibrePlan*-«Prosjektelementer» og «Timelister».

*JIRA-koblingen* må konfigureres riktig før den kan brukes.

Konfigurasjon
-------------

Fra skjermen "Hovedinnstillinger", velg fanen "Koblinger". I koblingsbildet, velg JIRA-koblingen fra nedtrekkslisten. Et egenskapsredigeringsbilde vil deretter vises.

I dette bildet kan du konfigurere følgende egenskapsverdier:

*   **Aktivert:** J/N, som indikerer om du ønsker å bruke JIRA-koblingen. Standarden er "N."
*   **Server-URL:** Den absolutte banen til JIRA-webserveren.
*   **Brukernavn og passord:** Brukerlegitimasjon for autorisasjon.
*   **JIRA-etiketter: kommaseparert liste over etiketter eller URL:** Du kan enten angi etikett-URL eller en kommaseparert liste over etiketter.
*   **Timetype:** Typen arbeidstimer. Standarden er "Default."

.. NOTE::

   **JIRA-etiketter:** For øyeblikket støtter ikke JIRA-webserveren å gi en liste over alle tilgjengelige etiketter. Som en løsning har vi utviklet et enkelt PHP-skript som utfører en enkel SQL-spørring i JIRA-databasen for å hente alle distinkte etiketter. Du kan enten bruke dette PHP-skriptet som "JIRA-etiketter-URL" eller angi etikettene du ønsker som kommaseparert tekst i feltet "JIRA-etiketter".

Til slutt klikker du på knappen "Test tilkobling" for å teste om du kan koble til JIRA-webserveren og at konfigurasjonene dine er korrekte.

Synkronisering
--------------

Fra prosjektvinduet, under "Generelle data," kan du starte synkronisering av prosjektelementer med JIRA-saker.

Klikk på knappen "Synkroniser med JIRA" for å starte synkroniseringen.

*   Hvis dette er første gang, vil et popup-vindu (med en automatisk utfylt liste over etiketter) vises. I dette vinduet kan du velge en etikett å synkronisere med og klikke på knappen "Start synkronisering" for å starte synkroniseringsprosessen, eller klikke på "Avbryt"-knappen for å avbryte den.

*   Hvis en etikett allerede er synkronisert, vil den siste synkroniseringsdatoen og etiketten vises i JIRA-bildet. I dette tilfellet vises ikke et popup-vindu for å velge en etikett. I stedet vil synkroniseringsprosessen starte direkte for den viste (allerede synkroniserte) etiketten.

.. NOTE::

   Forholdet mellom "Prosjekt" og "etikett" er én-til-én. Bare én etikett kan synkroniseres med ett "Prosjekt."

.. NOTE::

   Ved vellykket (re)synkronisering vil informasjonen bli skrevet til databasen, og JIRA-bildet vil bli oppdatert med den siste synkroniseringsdatoen og etiketten.

(Re)synkronisering utføres i to faser:

*   **Fase 1:** Synkronisering av prosjektelementer, inkludert fremdriftstildeling og målinger.
*   **Fase 2:** Synkronisering av timelister.

.. NOTE::

   Hvis Fase 1 mislykkes, vil ikke Fase 2 utføres, og ingen informasjon vil bli skrevet til databasen.

.. NOTE::

   Suksess- eller feilinformasjonen vil vises i et popup-vindu.

Etter vellykket fullføring av synkronisering vil resultatet vises i fanen "Work Breakdown Structure (WBS-oppgaver)" i bildet "Prosjektdetaljer". I dette brukergrensesnittet er det to endringer fra standard WBS:

*   Kolonnen "Totale oppgavetimer" er uredigerbar (skrivebeskyttet) fordi synkroniseringen er enveis. Oppgavetimer kan bare oppdateres på JIRA-webserveren.
*   Kolonnen "Kode" viser JIRA-saksnøkler, og de er også hyperkoblinger til JIRA-sakene. Klikk på ønsket nøkkel hvis du vil gå til dokumentet for den nøkkelen (JIRA-sak).

Planlegging
-----------

Resynkronisering av JIRA-saker kan også utføres gjennom planleggeren. Gå til bildet "Jobbplanlegging". I det bildet kan du konfigurere en JIRA-jobb for å utføre synkronisering. Jobben søker etter de sist synkroniserte etikettene i databasen og resynkroniserer dem deretter. Se også Planlegger-håndboken.

Tim Enterprise-kobling
=======================

Tim Enterprise er et nederlandsk produkt fra Aenova. Det er en nettbasert applikasjon for administrasjon av tid brukt på prosjekter og oppgaver.

Tim-koblingen er en applikasjon som kan brukes til å kommunisere med Tim Enterprise-serveren for å:

*   Eksportere alle timer brukt av en arbeider (bruker) på et prosjekt som kan registreres i Tim Enterprise.
*   Importere alle rostrene til arbeideren (brukeren) for å planlegge ressursen effektivt.

*Tim-koblingen* må konfigureres riktig før den kan brukes.

Konfigurasjon
-------------

Fra skjermen "Hovedinnstillinger", velg fanen "Koblinger". I koblingsbildet, velg Tim-koblingen fra nedtrekkslisten. Et egenskapsredigeringsbilde vil deretter vises.

I dette bildet kan du konfigurere følgende egenskapsverdier:

*   **Aktivert:** J/N, som indikerer om du ønsker å bruke Tim-koblingen. Standarden er "N."
*   **Server-URL:** Den absolutte banen til Tim Enterprise-serveren.
*   **Brukernavn og passord:** Brukerlegitimasjon for autorisasjon.
*   **Antall dager timelisteeksport til Tim:** Antall dager tilbake du vil eksportere timelistene.
*   **Antall dager rosterimport fra Tim:** Antall dager fremover du vil importere rostrene.
*   **Produktivitetsfaktor:** Effektive arbeidstimer i prosent. Standarden er "100%."
*   **Avdelings-ID-er for rosterimport:** Kommaseparerte avdelings-ID-er.

Til slutt klikker du på knappen "Test tilkobling" for å teste om du kan koble til Tim Enterprise-serveren og at konfigurasjonene dine er korrekte.

Eksport
-------

Fra prosjektvinduet, under "Generelle data," kan du starte eksport av timelister til Tim Enterprise-serveren.

Angi "Tim-produktkode" og klikk på knappen "Eksporter til Tim" for å starte eksporten.

Tim-koblingen legger til følgende felt sammen med produktkoden:

*   Arbeiderens/brukerens fulle navn.
*   Datoen arbeideren arbeidet på en oppgave.
*   Innsatsen, eller timer arbeidet på oppgaven.
*   Et alternativ som angir om Tim Enterprise skal oppdatere registreringen eller sette inn en ny.

Tim Enterprise-svaret inneholder bare en liste over post-ID-er (heltall). Dette gjør det vanskelig å avgjøre hva som gikk galt, ettersom svarlisten bare inneholder tall som ikke er relatert til forespørselfeltene. Eksportforespørselen (registrering i Tim) antas å ha lyktes hvis alle listeoppføringene ikke inneholder "0"-verdier. Ellers har eksportforespørselen mislyktes for de oppføringene som inneholder "0"-verdier. Derfor kan du ikke se hvilken forespørsel som mislyktes, ettersom listeoppføringene bare inneholder verdien "0." Den eneste måten å fastslå dette på er å undersøke loggfilen på Tim Enterprise-serveren.

.. NOTE::

   Etter vellykket eksport vil informasjonen bli skrevet til databasen, og Tim-bildet vil bli oppdatert med den siste eksportdatoen og produktkoden.

.. NOTE::

   Suksess- eller feilinformasjonen vil vises i et popup-vindu.

Planlegging av eksport
----------------------

Eksportprosessen kan også utføres gjennom planleggeren. Gå til bildet "Jobbplanlegging". I det bildet kan du konfigurere en Tim eksport-jobb. Jobben søker etter de sist eksporterte timelistene i databasen og re-eksporterer dem deretter. Se også Planlegger-håndboken.

Import
------

Import av rostre fungerer bare ved hjelp av planleggeren. Det finnes ikke noe brukergrensesnitt designet for dette, ettersom ingen inndata er nødvendig fra brukeren. Gå til bildet "Jobbplanlegging" og konfigurer en Tim import-jobb. Jobben går gjennom alle avdelinger konfigurert i koblingens egenskaper og importerer alle rostre for hver avdeling. Se også Planlegger-håndboken.

For import legger Tim-koblingen til følgende felt i forespørselen:

*   **Periode:** Perioden (dato fra - dato til) du vil importere rosteret for. Dette kan angis som et filterkriterium.
*   **Avdeling:** Avdelingen du vil importere rosteret for. Avdelinger er konfigurerbare.
*   Feltene du er interessert i (som Personinfo, RosterCategory, osv.) som Tim-serveren skal inkludere i svaret.

Importsvaret inneholder følgende felt, som er tilstrekkelig for å administrere unntaksdager i *LibrePlan*:

*   **Personinfo:** Navn og nettverksnavn.
*   **Avdeling:** Avdelingen arbeideren jobber i.
*   **Rosterkategori:** Informasjon om tilstedeværelse/fravær (Aanwzig/afwezig) for arbeideren og årsaken (*LibrePlan*-unntakstype) i tilfelle arbeideren er fraværende.
*   **Dato:** Datoen arbeideren er tilstede/fraværende.
*   **Tid:** Starttidspunktet for tilstedeværelse/fravær, for eksempel 08:00.
*   **Varighet:** Antall timer arbeideren er tilstede/fraværende.

Ved konvertering av importsvaret til *LibrePlan*s «Unntaksdag» tas følgende oversettelser i betraktning:

*   Hvis rosterkategorien inneholder navnet "Vakantie," vil det bli oversatt til "RESOURCE HOLIDAY."
*   Rosterkategorien "Feestdag" vil bli oversatt til "BANK HOLIDAY."
*   Alle de øvrige, som "Jus uren," "PLB uren," osv., bør legges til i "Kalenderunntaksdager" manuelt.

Dessuten er rosteret i importsvaret delt inn i to eller tre deler per dag: for eksempel roster-morgen, roster-ettermiddag og roster-kveld. Men *LibrePlan* tillater bare én «Unntakstype» per dag. Tim-koblingen er da ansvarlig for å slå sammen disse delene som én unntakstype. Det vil si at rosterkategorien med lengst varighet anses å være en gyldig unntakstype, men den totale varigheten er summen av alle varighetene for disse kategoridelene.

I motsetning til *LibrePlan*, betyr total varighet i Tim Enterprise i tilfelle arbeideren er på ferie at arbeideren ikke er tilgjengelig for den totale varigheten. Men i *LibrePlan*, hvis arbeideren er på ferie, bør den totale varigheten være null. Tim-koblingen håndterer også denne oversettelsen.

E-postkobling
=============

E-post er en metode for utveksling av digitale meldinger fra en avsender til én eller flere mottakere.

E-postkoblingen kan brukes til å angi tilkoblingsegenskaper for Simple Mail Transfer Protocol (SMTP)-serveren.

*E-postkoblingen* må konfigureres riktig før den kan brukes.

Konfigurasjon
-------------

Fra skjermen "Hovedinnstillinger", velg fanen "Koblinger". I koblingsbildet, velg e-postkoblingen fra nedtrekkslisten. Et egenskapsredigeringsbilde vil deretter vises.

I dette bildet kan du konfigurere følgende egenskapsverdier:

*   **Aktivert:** J/N, som indikerer om du ønsker å bruke e-postkoblingen. Standarden er "N."
*   **Protokoll:** Typen SMTP-protokoll.
*   **Vert:** Den absolutte banen til SMTP-serveren.
*   **Port:** Porten til SMTP-serveren.
*   **Fra-adresse:** E-postadressen til meldingsavsenderen.
*   **Brukernavn:** Brukernavnet for SMTP-serveren.
*   **Passord:** Passordet for SMTP-serveren.

Til slutt klikker du på knappen "Test tilkobling" for å teste om du kan koble til SMTP-serveren og at konfigurasjonene dine er korrekte.

Rediger e-postmal
-----------------

Fra prosjektvinduet, under "Konfigurasjon" og deretter "Rediger e-postmaler," kan du endre e-postmalene for meldinger.

Du kan velge:

*   **Malspråk:**
*   **Maltype:**
*   **E-postemne:**
*   **Malinnhold:**

Du må angi språket fordi webapplikasjonen sender e-poster til brukere på det språket de har valgt i innstillingene sine. Du må velge maltypen. Typen er brukerrollen, noe som betyr at denne e-posten kun sendes til brukere som har den valgte rollen (typen). Du må angi e-postemnet. Emnet er et kort sammendrag av temaet for meldingen. Du må angi e-postinnholdet. Dette er all informasjon du vil sende til brukeren. Det er også noen nøkkelord du kan bruke i meldingen; webapplikasjonen vil parse dem og angi en ny verdi i stedet for nøkkelordet.

Planlegging av e-poster
-----------------------

Sending av e-poster kan bare utføres gjennom planleggeren. Gå til "Konfigurasjon," deretter bildet "Jobbplanlegging". I det bildet kan du konfigurere en jobb for sending av e-post. Jobben tar en liste over e-postvarsler, samler data og sender det til brukerens e-post. Se også Planlegger-håndboken.

.. NOTE::

   Suksess- eller feilinformasjonen vil vises i et popup-vindu.
