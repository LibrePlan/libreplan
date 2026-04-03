Introduksjon
############

.. contents::

Dette dokumentet beskriver funksjonene i LibrePlan og gir brukere informasjon om hvordan de konfigurerer og bruker applikasjonen.

LibrePlan er en åpen kildekode-webapplikasjon for prosjektplanlegging. Hovedmålet er å tilby en helhetlig løsning for prosjektledelse i bedrifter. For spesifikk informasjon om denne programvaren, ta kontakt med utviklingsteamet på http://www.libreplan.com/contact/

.. figure:: images/company_view.png
   :scale: 50

   Bedriftsoversikt

Bedriftsoversikt og visningsbehandling
======================================

Som vist på programmets hovedskjerm (se forrige skjermbilde) og i bedriftsoversikten, kan brukere se en liste over planlagte prosjekter. Dette gir dem innsikt i bedriftens overordnede status når det gjelder prosjekter og ressursutnyttelse. Bedriftsoversikten tilbyr tre ulike visninger:

* **Planleggingsvisning:** Denne visningen kombinerer to perspektiver:

   * **Prosjekt- og tidssporing:** Hvert prosjekt er representert med et Gantt-diagram som viser prosjektets start- og sluttdato. Denne informasjonen vises sammen med den avtalte fristen. Det gjøres deretter en sammenligning mellom den oppnådde fremdriftsprosenten og den faktiske tiden brukt på hvert prosjekt. Dette gir et klart bilde av bedriftens ytelse til enhver tid. Denne visningen er programmets standard startside.
   * **Graf for bedriftens ressursutnyttelse:** Denne grafen viser informasjon om ressursallokering på tvers av prosjekter og gir et sammendrag av hele bedriftens ressursbruk. Grønt indikerer at ressursallokeringen er under 100 % av kapasiteten. Den svarte linjen representerer den totale tilgjengelige ressurskapasiteten. Gult indikerer at ressursallokeringen overstiger 100 %. Det er mulig å ha underallokering totalt sett, mens man samtidig opplever overallokering for spesifikke ressurser.

* **Ressursbelastningsvisning:** Denne skjermen viser en liste over bedriftens arbeidere og deres spesifikke oppgavetildelinger, eller generiske tildelinger basert på definerte kriterier. For å åpne denne visningen, klikk på *Samlet ressursbelastning*. Se bildet nedenfor for et eksempel.
* **Prosjektadministrasjonsvisning:** Denne skjermen viser en liste over bedriftens prosjekter og lar brukere utføre følgende handlinger: filtrere, redigere, slette, visualisere planlegging eller opprette et nytt prosjekt. For å åpne denne visningen, klikk på *Prosjektliste*.

.. figure:: images/resources_global.png
   :scale: 50

   Ressursoversikt

.. figure:: images/order_list.png
   :scale: 50

   Work Breakdown Structure

Visningsbehandlingen beskrevet ovenfor for bedriftsoversikten ligner svært mye på den som er tilgjengelig for et enkelt prosjekt. Et prosjekt kan åpnes på flere måter:

* Høyreklikk på Gantt-diagrammet for prosjektet og velg *Planlegg*.
* Gå til prosjektlisten og klikk på Gantt-diagramikonet.
* Opprett et nytt prosjekt og endre gjeldende prosjektvisning.

Programmet tilbyr følgende visninger for et prosjekt:

* **Planleggingsvisning:** Denne visningen lar brukere visualisere oppgaveplanlegging, avhengigheter, milepæler og mer. Se avsnittet *Planlegging* for mer informasjon.
* **Ressursbelastningsvisning:** Denne visningen lar brukere kontrollere den tildelte ressursbelastningen for et prosjekt. Fargekoden er konsistent med bedriftsoversikten: grønt for belastning under 100 %, gult for belastning lik 100 % og rødt for belastning over 100 %. Belastningen kan stamme fra en spesifikk oppgave eller et sett med kriterier (generisk allokering).
* **Prosjektredigeringsvisning:** Denne visningen lar brukere endre detaljene i prosjektet. Se avsnittet *Prosjekter* for mer informasjon.
* **Avansert ressursallokeringsvisning:** Denne visningen lar brukere allokere ressurser med avanserte alternativer, for eksempel å spesifisere timer per dag eller tildelte funksjoner som skal utføres. Se avsnittet *Ressursallokering* for mer informasjon.

Hva gjør LibrePlan nyttig?
==========================

LibrePlan er et generelt planleggingsverktøy utviklet for å håndtere utfordringer i industriell prosjektplanlegging som ikke var tilstrekkelig dekket av eksisterende verktøy. Utviklingen av LibrePlan var også motivert av ønsket om å tilby et gratis, åpen kildekode-basert og fullstendig nettbasert alternativ til proprietære planleggingsverktøy.

Kjerneprinsippene som programmet er bygget på, er som følger:

* **Bedrifts- og flerprosjektoversikt:** LibrePlan er spesielt utformet for å gi brukere informasjon om flere prosjekter som pågår i en bedrift. Det er derfor i bunn og grunn et flerprosjektprogram. Programmets fokus er ikke begrenset til enkeltprosjekter, selv om det også finnes spesifikke visninger for enkeltprosjekter.
* **Visningsbehandling:** Bedriftsoversikten, eller flerprosjektvisningen, er ledsaget av ulike visninger av den lagrede informasjonen. For eksempel lar bedriftsoversikten brukere se prosjekter og sammenligne statusen deres, se den samlede ressursbelastningen i bedriften og administrere prosjekter. Brukere kan også åpne planleggingsvisningen, ressursbelastningsvisningen, den avanserte ressursallokeringsvisningen og prosjektredigeringsvisningen for enkeltprosjekter.
* **Kriterier:** Kriterier er en systemenhet som muliggjør klassifisering av både ressurser (menneskelige og maskinelle) og oppgaver. Ressurser må oppfylle bestemte kriterier, og oppgaver krever at spesifikke kriterier er oppfylt. Dette er en av programmets viktigste funksjoner, ettersom kriterier danner grunnlaget for generisk allokering og adresserer en betydelig utfordring i industrien: den tidkrevende naturen til personalressursstyring og vanskeligheten med langsiktige estimater for bedriftens belastning.
* **Ressurser:** Det finnes to typer ressurser: menneskelige og maskinelle. Menneskelige ressurser er bedriftens arbeidere, brukt til planlegging, overvåking og kontroll av bedriftens arbeidsbelastning. Maskinressurser, avhengige av personene som betjener dem, fungerer på samme måte som menneskelige ressurser.
* **Ressursallokering:** En nøkkelfunksjon i programmet er muligheten til å tildele ressurser på to måter: spesifikt og generisk. Generisk allokering er basert på kriteriene som kreves for å fullføre en oppgave, og må oppfylles av ressurser som er i stand til å møte disse kriteriene. For å forstå generisk allokering, se dette eksempelet: Ola Nordmann er sveiser. Vanligvis ville Ola Nordmann bli spesifikt tildelt en planlagt oppgave. LibrePlan tilbyr imidlertid muligheten til å velge en hvilken som helst sveiser i bedriften, uten å måtte spesifisere at Ola Nordmann er den tildelte personen.
* **Bedriftens belastningskontroll:** Programmet gir enkel kontroll over bedriftens ressursbelastning. Denne kontrollen strekker seg til både mellom- og langsiktig, ettersom nåværende og fremtidige prosjekter kan administreres i programmet. LibrePlan tilbyr grafer som visuelt representerer ressursutnyttelsen.
* **Etiketter:** Etiketter brukes til å kategorisere prosjektoppgaver. Med disse etikettene kan brukere gruppere oppgaver etter konsept, noe som gir mulighet for senere gjennomgang som gruppe eller etter filtrering.
* **Filtre:** Fordi systemet naturlig inneholder elementer som merker eller karakteriserer oppgaver og ressurser, kan kriteriefiltre eller etiketter brukes. Dette er svært nyttig for å gjennomgå kategorisert informasjon eller generere spesifikke rapporter basert på kriterier eller etiketter.
* **Kalendere:** Kalendere definerer de tilgjengelige produktive timene for ulike ressurser. Brukere kan opprette generelle bedriftskalendere eller definere mer spesifikke kalendere, noe som gir mulighet for å opprette kalendere for individuelle ressurser og oppgaver.
* **Prosjekter og prosjektelementer:** Arbeid bestilt av klienter behandles som et prosjekt i applikasjonen, strukturert i prosjektelementer. Prosjektet og dets elementer følger en hierarkisk struktur med *x* nivåer. Dette elementtreet danner grunnlaget for arbeidsplanlegging.
* **Fremdrift:** Programmet kan håndtere ulike typer fremdrift. Et prosjekts fremdrift kan måles som en prosentandel, i enheter, mot det avtalte budsjettet og mer. Ansvaret for å bestemme hvilken type fremdrift som skal brukes til sammenligning på høyere prosjektnivåer, ligger hos planleggingssjefen.
* **Oppgaver:** Oppgaver er de grunnleggende planleggingselementene i programmet. De brukes til å planlegge arbeid som skal utføres. Nøkkelkarakteristikker for oppgaver inkluderer: avhengigheter mellom oppgaver og det potensielle kravet om at spesifikke kriterier må være oppfylt før ressurser kan allokeres.
* **Arbeidsrapporter:** Disse rapportene, innsendt av bedriftens arbeidere, beskriver timene som er arbeidet og oppgavene som er knyttet til disse timene. Denne informasjonen lar systemet beregne den faktiske tiden brukt på å fullføre en oppgave sammenlignet med budsjettert tid. Fremdrift kan deretter sammenlignes med de faktiske timene som er brukt.

I tillegg til kjernefunksjonene tilbyr LibrePlan andre funksjoner som skiller det fra lignende programmer:

* **Integrasjon med ERP:** Programmet kan direkte importere informasjon fra bedriftens ERP-systemer, inkludert prosjekter, menneskelige ressurser, arbeidsrapporter og spesifikke kriterier.
* **Versjonsbehandling:** Programmet kan håndtere flere planleggingsversjoner, mens det fortsatt lar brukere gjennomgå informasjonen fra hver versjon.
* **Historikkbehandling:** Programmet sletter ikke informasjon; det merker den bare som ugyldig. Dette lar brukere gjennomgå historisk informasjon ved hjelp av datofiltre.

Brukervennlighetskonvensjoner
==============================

Informasjon om skjemaer
------------------------
Før vi beskriver de ulike funksjonene knyttet til de viktigste modulene, må vi forklare den generelle navigasjonen og skjemaoppførselen.

Det finnes i hovedsak tre typer redigeringsskjemaer:

* **Skjemaer med en *Tilbake*-knapp:** Disse skjemaene er en del av en større kontekst, og endringene som gjøres, lagres i minnet. Endringene anvendes bare når brukeren eksplisitt lagrer alle detaljene på skjermen som skjemaet ble startet fra.
* **Skjemaer med *Lagre*- og *Lukk*-knapper:** Disse skjemaene gir mulighet for to handlinger. Den første lagrer endringene og lukker det gjeldende vinduet. Den andre lukker vinduet uten å lagre endringer.
* **Skjemaer med *Lagre og fortsett*-, *Lagre*- og *Lukk*-knapper:** Disse skjemaene gir mulighet for tre handlinger. Den første lagrer endringene og holder gjeldende skjema åpent. Den andre lagrer endringene og lukker skjemaet. Den tredje lukker vinduet uten å lagre endringer.

Standard ikoner og knapper
---------------------------

* **Redigering:** Generelt kan poster i programmet redigeres ved å klikke på et ikon som ligner en blyant på en hvit notatbok.
* **Venstre innrykk:** Disse operasjonene brukes vanligvis for elementer innenfor en trestruktur som må flyttes til et dypere nivå. Dette gjøres ved å klikke på ikonet som ligner en grønn pil som peker til høyre.
* **Høyre innrykk:** Disse operasjonene brukes vanligvis for elementer innenfor en trestruktur som må flyttes til et høyere nivå. Dette gjøres ved å klikke på ikonet som ligner en grønn pil som peker til venstre.
* **Sletting:** Brukere kan slette informasjon ved å klikke på søppelkasse-ikonet.
* **Søk:** Forstørrelsesglass-ikonet indikerer at tekstfeltet til venstre for det brukes til å søke etter elementer.

Faner
------
Programmet bruker faner til å organisere skjemaer for innholdsredigering og administrasjon. Denne metoden brukes til å dele et omfattende skjema inn i ulike seksjoner, som er tilgjengelige ved å klikke på fanenavnene. De andre fanene beholder sin nåværende status. I alle tilfeller gjelder lagre- og avbryt-alternativene for alle delskjemaer i de ulike fanene.

Eksplisitte handlinger og konteksthjelp
-----------------------------------------

Programmet inneholder komponenter som gir ytterligere beskrivelser av elementer når musen holdes over dem i ett sekund. Handlingene brukeren kan utføre, er angitt på knappeetikettene, i hjelpetekstene tilknyttet dem, i navigasjonsmenyvalgene og i kontekstmenyer som vises ved høyreklikk i planleggingsområdet. Videre er det gitt snarveier for hovedoperasjonene, for eksempel dobbeltklikk på listede elementer eller bruk av tastaturhendelser med markøren og Enter-tasten for å legge til elementer ved navigering gjennom skjemaer.
