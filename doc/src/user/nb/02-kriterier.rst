Kriterier
#########

.. contents::

Kriterier er elementer som brukes i programmet for å kategorisere både ressurser og oppgaver. Oppgaver krever spesifikke kriterier, og ressurser må oppfylle disse kriteriene.

Her er et eksempel på hvordan kriterier brukes: En ressurs tildeles kriteriet "sveiser" (noe som betyr at ressursen oppfyller kategorien "sveiser"), og en oppgave krever at "sveiser"-kriteriet er oppfylt. Følgelig vil arbeidere med "sveiser"-kriteriet bli vurdert når ressurser allokeres til oppgaver ved hjelp av generisk allokering (i motsetning til spesifikk allokering). For mer informasjon om de ulike allokeringstypene, se kapitlet om ressursallokering.

Programmet tillater flere operasjoner som involverer kriterier:

*   Kritierieadministrasjon
*   Tildeling av kriterier til ressurser
*   Tildeling av kriterier til oppgaver
*   Filtrering av enheter basert på kriterier. Oppgaver og prosjektelementer kan filtreres etter kriterier for å utføre ulike operasjoner i programmet.

Denne seksjonen vil bare forklare den første funksjonen, kritierieadministrasjon. De to allokeringstypene vil bli dekket senere: ressursallokering i kapitlet "Ressursbehandling" og filtrering i kapitlet "Oppgaveplanlegging".

Kritierieadministrasjon
========================

Kritierieadministrasjon er tilgjengelig via administrasjonsmenyen:

.. figure:: images/menu.png
   :scale: 50

   Faner i menyen på første nivå

Den spesifikke operasjonen for å administrere kriterier er *Administrer kriterier*. Denne operasjonen lar deg liste opp kriteriene som er tilgjengelige i systemet.

.. figure:: images/lista-criterios.png
   :scale: 50

   Liste over kriterier

Du kan åpne skjemaet for å opprette/redigere kriterium ved å klikke på *Opprett*-knappen. For å redigere et eksisterende kriterium, klikk på redigeringsikonet.

.. figure:: images/edicion-criterio.png
   :scale: 50

   Redigering av kriterier

Redigeringsskjemaet for kriterier, som vist i forrige bilde, lar deg utføre følgende operasjoner:

*   **Redigere kriteriumets navn.**
*   **Angi om flere verdier kan tildeles samtidig eller bare én verdi for den valgte kriteriumtypen.** For eksempel kan en ressurs oppfylle to kriterier: "sveiser" og "dreier".
*   **Angi kriteriumtypen:**

    *   **Generisk:** Et kriterium som kan brukes for både maskiner og arbeidere.
    *   **Arbeider:** Et kriterium som bare kan brukes for arbeidere.
    *   **Maskin:** Et kriterium som bare kan brukes for maskiner.

*   **Angi om kriteriet er hierarkisk.** Noen ganger må kriterier behandles hierarkisk. For eksempel tildeler ikke tildeling av et kriterium til et element det automatisk til elementer som er avledet fra det. Et tydelig eksempel på et hierarkisk kriterium er "sted". For eksempel vil en person som er tildelt stedet "Galicia" også tilhøre "Spania".
*   **Angi om kriteriet er autorisert.** Dette er hvordan brukere deaktiverer kriterier. Når et kriterium er opprettet og brukt i historiske data, kan det ikke endres. I stedet kan det deaktiveres for å forhindre at det vises i valglister.
*   **Beskrive kriteriet.**
*   **Legge til nye verdier.** Et tekstinndatafelt med *Nytt kriterium*-knappen er plassert i den andre delen av skjemaet.
*   **Redigere navnene på eksisterende kriteriumverdier.**
*   **Flytte kriteriumverdier opp eller ned i listen over gjeldende kriteriumverdier.**
*   **Fjerne en kriteriumverdi fra listen.**

Kritierieadministrasjonsskjemaet følger skjemaoppførselen beskrevet i innledningen og tilbyr tre handlinger: *Lagre*, *Lagre og lukk* og *Lukk*.
