Planlegger
##########

.. contents::

Planleggeren er utformet for å planlegge jobber dynamisk. Den er utviklet ved hjelp av *Spring Framework Quartz scheduler*.

For å bruke denne planleggeren effektivt må jobbene (Quartz-jobber) som skal planlegges opprettes først. Deretter kan disse jobbene legges til i databasen, ettersom alle jobber som skal planlegges lagres i databasen.

Når planleggeren starter, leser den jobbene som skal planlegges eller avplanlegges fra databasen og planlegger eller fjerner dem deretter. Etterpå kan jobber legges til, oppdateres eller fjernes dynamisk ved hjelp av brukergrensesnittet ``Jobbplanlegging``.

.. NOTE::
   Planleggeren starter når LibrePlan-webapplikasjonen starter og stopper når applikasjonen stopper.

.. NOTE::
   Denne planleggeren støtter bare ``cron-uttrykk`` for å planlegge jobber.

Kriteriene som planleggeren bruker for å planlegge eller fjerne jobber når den starter er som følger:

For alle jobber:

* Planlegg

  * Jobben har en *Kobling*, og *Koblingen* er aktivert, og jobben er tillatt å planlegges.
  * Jobben har ingen *Kobling* og er tillatt å planlegges.

* Fjern

  * Jobben har en *Kobling*, og *Koblingen* er ikke aktivert.
  * Jobben har en *Kobling*, og *Koblingen* er aktivert, men jobben er ikke tillatt å planlegges.
  * Jobben har ingen *Kobling* og er ikke tillatt å planlegges.

.. NOTE::
   Jobber kan ikke omplanlegges eller avplanlegges hvis de kjører for øyeblikket.

Listevisning for jobbplanlegging
================================

Listevisningen ``Jobbplanlegging`` lar brukerne:

*   Legge til en ny jobb.
*   Redigere en eksisterende jobb.
*   Fjerne en jobb.
*   Starte en prosess manuelt.

Legg til eller rediger jobb
===========================

Fra listevisningen ``Jobbplanlegging``, klikk:

*   ``Opprett`` for å legge til en ny jobb, eller
*   ``Rediger`` for å endre den valgte jobben.

Begge handlingene vil åpne et opprett/rediger ``jobbskjema``. ``Skjemaet`` viser følgende egenskaper:

*   Felt:

    *   **Jobbgruppe:** Navnet på jobbgruppen.
    *   **Jobbnavn:** Navnet på jobben.
    *   **Cron-uttrykk:** Et skrivebeskyttet felt med en ``Rediger``-knapp for å åpne inndatavinduet for ``cron-uttrykk``.
    *   **Jobbklassenavn:** En ``nedtrekksliste`` for å velge jobben (en eksisterende jobb).
    *   **Kobling:** En ``nedtrekksliste`` for å velge en kobling. Dette er ikke obligatorisk.
    *   **Planlegg:** En avmerkingsboks for å angi om denne jobben skal planlegges.

*   Knapper:

    *   **Lagre:** For å lagre eller oppdatere en jobb i både databasen og planleggeren. Brukeren returneres deretter til ``Listevisning for jobbplanlegging``.
    *   **Lagre og fortsett:** Det samme som "Lagre," men brukeren returneres ikke til ``Listevisning for jobbplanlegging``.
    *   **Avbryt:** Ingenting lagres, og brukeren returneres til ``Listevisning for jobbplanlegging``.

*   Og en hjelpeseksjon om syntaks for cron-uttrykk.

Popup for cron-uttrykk
----------------------

For å angi ``cron-uttrykket`` korrekt brukes et popup-skjema for ``cron-uttrykk``. I dette skjemaet kan du angi ønsket ``cron-uttrykk``. Se også hjelpeteksten om ``cron-uttrykket``. Hvis du angir et ugyldig ``cron-uttrykk``, vil du bli varslet umiddelbart.

Fjern jobb
==========

Klikk på knappen ``Fjern`` for å slette jobben fra både databasen og planleggeren. Suksessen eller feilen ved denne handlingen vil vises.

Start jobb manuelt
==================

Som et alternativ til å vente på at jobben kjøres som planlagt, kan du klikke på denne knappen for å starte prosessen direkte. Etterpå vil suksess- eller feilinformasjonen vises i et ``popup-vindu``.
