Rapporto del Totale delle Ore Lavorate per Risorsa in un Mese
##############################################################

.. contents::

Scopo
=====

Questo rapporto fornisce il numero totale di ore lavorate da ciascuna risorsa in un determinato mese. Queste informazioni possono essere utili per determinare gli straordinari dei lavoratori o, a seconda dell'organizzazione, il numero di ore per cui ciascuna risorsa dovrebbe essere compensata.

L'applicazione tiene traccia dei rapporti di lavoro sia per i lavoratori che per le macchine. Per le macchine, il rapporto somma il numero di ore di operatività durante il mese.

Parametri di Input e Filtri
============================

Per generare questo rapporto, gli utenti devono specificare l'anno e il mese per i quali desiderano recuperare il numero totale di ore lavorate da ciascuna risorsa.

Output
======

Il formato di output è il seguente:

Intestazione
------------

L'intestazione del rapporto visualizza:

   *   L'*anno* a cui si riferiscono i dati del rapporto.
   *   Il *mese* a cui si riferiscono i dati del rapporto.

Piè di Pagina
-------------

Il piè di pagina visualizza la data in cui il rapporto è stato generato.

Corpo
-----

La sezione dei dati del rapporto consiste in una singola tabella con due colonne:

   *   Una colonna denominata **Nome** per il nome della risorsa.
   *   Una colonna denominata **Ore** con il numero totale di ore lavorate dalla risorsa in quella riga.

C'è una riga finale che aggrega il numero totale di ore lavorate da tutte le risorse durante il *mese* e l'*anno* specificati.
