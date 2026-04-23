Rapport: Timer arbeidet per ressurs
####################################

.. contents::

Formål
======

Denne rapporten henter en liste over oppgaver og den tiden ressurser har brukt på dem innenfor en angitt periode. Flere filtre lar brukerne avgrense spørringen for å få kun ønsket informasjon og utelate uvedkommende data.

Inngangsparametere og filtre
============================

* **Datoer**.
    * *Type*: Valgfri.
    * *To datofelt*:
        * *Startdato:* Dette er den tidligste datoen for arbeidsrapporter som skal inkluderes. Arbeidsrapporter med datoer tidligere enn *Startdato* utelates. Hvis denne parameteren er tom, filtreres ikke arbeidsrapporter etter *Startdato*.
        * *Sluttdato:* Dette er den seneste datoen for arbeidsrapporter som skal inkluderes. Arbeidsrapporter med datoer etter *Sluttdato* utelates. Hvis denne parameteren er tom, filtreres ikke arbeidsrapporter etter *Sluttdato*.

*   **Filtrer etter arbeidere:**
    *   *Type:* Valgfri.
    *   *Slik fungerer det:* Du kan velge én eller flere arbeidere for å begrense arbeidsrapportene til den tiden disse spesifikke arbeiderne har registrert. For å legge til en arbeider som filter, søk etter dem i velgeren og klikk *Legg til*-knappen. Hvis dette filteret er tomt, hentes arbeidsrapporter uavhengig av arbeider.

*   **Filtrer etter etiketter:**
    *   *Type:* Valgfri.
    *   *Slik fungerer det:* Du kan legge til én eller flere etiketter som filtre ved å søke etter dem i velgeren og klikke *Legg til*-knappen. Disse etikettene brukes til å velge oppgavene som skal inkluderes i resultatene ved beregning av timer brukt på dem. Dette filteret kan brukes på timelister, oppgaver, begge eller ingen.

*   **Filtrer etter kriterier:**
    *   *Type:* Valgfri.
    *   *Slik fungerer det:* Du kan velge ett eller flere kriterier ved å søke etter dem i velgeren og deretter klikke *Legg til*-knappen. Disse kriteriene brukes til å velge ressurser som oppfyller minst ett av dem. Rapporten vil vise all tid brukt av ressursene som oppfyller ett av de valgte kriteriene.

Utdata
======

Topptekst
---------

Rapportens topptekst viser filtrene som ble konfigurert og brukt på den gjeldende rapporten.

Bunntekst
---------

Datoen rapporten ble generert er oppført i bunnteksten.

Innhold
-------

Rapportens innhold består av flere informasjonsgrupper.

*   Det første aggregeringsnivået er etter ressurs. All tid brukt av en ressurs vises samlet under overskriften. Hver ressurs identifiseres ved:

    *   *Arbeider:* Etternavn, Fornavn.
    *   *Maskin:* Navn.

    En sammendragslinje viser det totale antallet timer arbeidet av ressursen.

*   Det andre grupperingsnivået er etter *dato*. Alle rapporter fra en bestemt ressurs på samme dato vises samlet.

    En sammendragslinje viser det totale antallet timer arbeidet av ressursen på den datoen.

*   Det siste nivået viser arbeidsrapportene for arbeideren den dagen. Informasjonen som vises for hver arbeidsrapportlinje er:

    *   *Oppgavekode:* Koden til oppgaven som de registrerte timene tilskrives.
    *   *Oppgavenavn:* Navnet på oppgaven som de registrerte timene tilskrives.
    *   *Starttidspunkt:* Dette er valgfritt. Det er tidspunktet da ressursen begynte å arbeide på oppgaven.
    *   *Sluttidspunkt:* Dette er valgfritt. Det er tidspunktet da ressursen avsluttet arbeidet på oppgaven den angitte datoen.
    *   *Tekstfelt:* Dette er valgfritt. Hvis arbeidsrapportlinjen har tekstfelt, vises de utfylte verdiene her. Formatet er: <Navn på tekstfelt>:<Verdi>
    *   *Etiketter:* Dette avhenger av om arbeidsrapportmodellen har et etikettfelt i sin definisjon. Hvis det er flere etiketter, vises de i samme kolonne. Formatet er: <Navn på etikettype>:<Verdi for etiketten>
