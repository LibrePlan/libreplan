Fremdrift
#########

.. contents::

Prosjektfremdrift angir i hvilken grad den estimerte fullføringstiden for prosjektet overholdes. Oppgavefremdrift angir i hvilken grad oppgaven fullføres i henhold til dens estimerte fullføring.

Generelt kan fremdrift ikke måles automatisk. Et erfarent stabsmedlem eller en sjekkliste må fastsette graden av fullføring for en oppgave eller et prosjekt.

Det er viktig å merke seg forskjellen mellom timene tildelt en oppgave eller et prosjekt og fremdriften til den oppgaven eller det prosjektet. Selv om antall timer brukt kan være mer eller mindre enn forventet, kan prosjektet ligge foran eller bak sin estimerte fullføring på den overvåkede dagen. Flere situasjoner kan oppstå fra disse to målingene:

*   **Færre timer forbrukt enn forventet, men prosjektet er forsinket:** Fremdriften er lavere enn estimert for den overvåkede dagen.
*   **Færre timer forbrukt enn forventet, og prosjektet er i rute:** Fremdriften er høyere enn estimert for den overvåkede dagen.
*   **Flere timer forbrukt enn forventet, og prosjektet er forsinket:** Fremdriften er lavere enn estimert for den overvåkede dagen.
*   **Flere timer forbrukt enn forventet, men prosjektet er i rute:** Fremdriften er høyere enn estimert for den overvåkede dagen.

Planleggingsvisningen lar deg sammenligne disse situasjonene ved å bruke informasjon om fremdriften som er gjort og timene som er brukt. Dette kapitlet vil forklare hvordan du legger inn informasjon for å overvåke fremdriften.

Filosofien bak fremdriftsovervåking er basert på at brukere definerer det nivået de ønsker å overvåke prosjektene sine på. For eksempel, hvis brukere ønsker å overvåke prosjekter, trenger de bare å legge inn informasjon for elementer på nivå 1. Hvis de ønsker mer presis overvåking på oppgavenivå, må de legge inn fremdriftsinformasjon på lavere nivåer. Systemet vil da aggregere dataene oppover gjennom hierarkiet.

Administrere fremdriftstyper
=============================

Bedrifter har varierende behov når de overvåker prosjektfremdrift, særlig oppgavene som er involvert. Systemet inkluderer derfor "fremdriftstyper". Brukere kan definere ulike fremdriftstyper for å måle en oppgaves fremdrift. For eksempel kan en oppgave måles som en prosentandel, men denne prosentandelen kan også oversettes til fremdrift i *Tonn* basert på avtalen med klienten.

En fremdriftstype har et navn, en maksimumsverdi og en presisjonsverdi:

*   **Navn:** Et beskrivende navn som brukere vil gjenkjenne når de velger fremdriftstypen. Dette navnet bør tydelig angi hva slags fremdrift som måles.
*   **Maksimumsverdi:** Den maksimale verdien som kan fastsettes for en oppgave eller et prosjekt som total fremdriftsmåling. For eksempel, hvis du jobber med *Tonn* og det normale maksimum er 4000 tonn, og ingen oppgave noensinne vil kreve mer enn 4000 tonn av et materiale, vil 4000 være maksimumsverdien.
*   **Presisjonsverdi:** Inkrementverdien som er tillatt for fremdriftstypen. For eksempel, hvis fremdrift i *Tonn* skal måles i hele tall, vil presisjonsverdi være 1. Fra det punktet av kan bare hele tall legges inn som fremdriftsmålinger (f.eks. 1, 2, 300).

Systemet har to standard fremdriftstyper:

*   **Prosentandel:** En generell fremdriftstype som måler fremdriften til et prosjekt eller en oppgave basert på en estimert fullføringsprosent. For eksempel er en oppgave 30 % fullført av de estimerte 100 % for en bestemt dag.
*   **Enheter:** En generell fremdriftstype som måler fremdrift i enheter uten å spesifisere enhetstypen. For eksempel innebærer en oppgave å opprette 3000 enheter, og fremdriften er 500 enheter av totalt 3000.

.. figure:: images/tipos-avances.png
   :scale: 50

   Administrasjon av fremdriftstyper

Brukere kan opprette nye fremdriftstyper som følger:

*   Gå til "Administrasjon"-seksjonen.
*   Klikk på "Administrer fremdriftstyper"-alternativet i menyen på andre nivå.
*   Systemet vil vise en liste over eksisterende fremdriftstyper.
*   For hver fremdriftstype kan brukere:

    *   Redigere
    *   Slette

*   Brukere kan deretter opprette en ny fremdriftstype.
*   Når de redigerer eller oppretter en fremdriftstype, viser systemet et skjema med følgende informasjon:

    *   Navn på fremdriftstypen.
    *   Maksimalt tillatt verdi for fremdriftstypen.
    *   Presisjonsverdi for fremdriftstypen.

Registrere fremdrift etter type
=================================

Fremdrift registreres for prosjektelementer, men kan også registreres ved hjelp av en snarvei fra planleggingsoppgavene. Brukere er ansvarlige for å bestemme hvilken fremdriftstype som skal knyttes til hvert prosjektelement.

Brukere kan legge inn én enkelt standard fremdriftstype for hele prosjektet.

Før de måler fremdrift, må brukere knytte den valgte fremdriftstypen til prosjektet. For eksempel kan de velge prosentvis fremdrift for å måle fremdriften på hele oppgaven eller en avtalt fremdriftshastighet hvis fremdriftsmålinger avtalt med klienten skal legges inn i fremtiden.

.. figure:: images/avance.png
   :scale: 40

   Skjerm for registrering av fremdrift med grafisk visualisering

For å legge inn fremdriftsmålinger:

*   Velg fremdriftstypen som fremdriften skal legges til for.
    *   Hvis ingen fremdriftstype finnes, må en ny opprettes.
*   I skjemaet som vises under feltene "Verdi" og "Dato", legg inn den absolutte verdien for målingen og datoen for målingen.
*   Systemet lagrer automatisk de inntastede dataene.

Sammenligne fremdrift for et prosjektelement
=============================================

Brukere kan grafisk sammenligne fremdriften som er gjort på prosjekter med målingene som er tatt. Alle fremdriftstyper har en kolonne med en avkryssingsknapp ("Vis"). Når denne knappen er valgt, vises fremdriftsdiagrammet over målinger tatt for prosjektelementet.

.. figure:: images/contraste-avance.png
   :scale: 40

   Sammenligning av flere fremdriftstyper
