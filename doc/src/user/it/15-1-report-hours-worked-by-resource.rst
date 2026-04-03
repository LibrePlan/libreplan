Rapporto delle Ore Lavorate per Risorsa
########################################

.. contents::

Scopo
=====

Questo rapporto estrae un elenco di attività e il tempo che le risorse hanno dedicato ad esse in un periodo specificato. Diversi filtri consentono agli utenti di affinare la query per ottenere solo le informazioni desiderate ed escludere i dati superflui.

Parametri di Input e Filtri
============================

* **Date**.
    * *Tipo*: Facoltativo.
    * *Due campi data*:
        * *Data di Inizio:* È la data più antica per i rapporti di lavoro da includere. I rapporti di lavoro con date precedenti alla *Data di Inizio* sono esclusi. Se questo parametro viene lasciato vuoto, i rapporti di lavoro non vengono filtrati per *Data di Inizio*.
        * *Data di Fine:* È la data più recente per i rapporti di lavoro da includere. I rapporti di lavoro con date successive alla *Data di Fine* sono esclusi. Se questo parametro viene lasciato vuoto, i rapporti di lavoro non vengono filtrati per *Data di Fine*.

*   **Filtro per Lavoratori:**
    *   *Tipo:* Facoltativo.
    *   *Come funziona:* È possibile selezionare uno o più lavoratori per limitare i rapporti di lavoro al tempo tracciato da quei specifici lavoratori. Per aggiungere un lavoratore come filtro, cercarlo nel selettore e fare clic sul pulsante *Aggiungi*. Se questo filtro viene lasciato vuoto, i rapporti di lavoro vengono recuperati indipendentemente dal lavoratore.

*   **Filtro per Etichette:**
    *   *Tipo:* Facoltativo.
    *   *Come funziona:* È possibile aggiungere una o più etichette da utilizzare come filtri cercandole nel selettore e facendo clic sul pulsante *Aggiungi*. Queste etichette vengono utilizzate per selezionare le attività da includere nei risultati durante il calcolo delle ore dedicate ad esse. Questo filtro può essere applicato ai fogli presenze, alle attività, a entrambi o a nessuno dei due.

*   **Filtro per Criteri:**
    *   *Tipo:* Facoltativo.
    *   *Come funziona:* È possibile selezionare uno o più criteri cercandoli nel selettore e quindi facendo clic sul pulsante *Aggiungi*. Questi criteri vengono utilizzati per selezionare le risorse che soddisfano almeno uno di essi. Il rapporto mostrerà tutto il tempo dedicato dalle risorse che soddisfano uno dei criteri selezionati.

Output
======

Intestazione
------------

L'intestazione del rapporto visualizza i filtri configurati e applicati al rapporto corrente.

Piè di Pagina
-------------

La data in cui il rapporto è stato generato è elencata nel piè di pagina.

Corpo
-----

Il corpo del rapporto è composto da diversi gruppi di informazioni.

*   Il primo livello di aggregazione è per risorsa. Tutto il tempo dedicato da una risorsa viene mostrato insieme sotto l'intestazione. Ogni risorsa è identificata da:

    *   *Lavoratore:* Cognome, Nome.
    *   *Macchina:* Nome.

    Una riga di riepilogo mostra il numero totale di ore lavorate dalla risorsa.

*   Il secondo livello di raggruppamento è per *data*. Tutti i rapporti di una risorsa specifica nella stessa data vengono mostrati insieme.

    Una riga di riepilogo mostra il numero totale di ore lavorate dalla risorsa in quella data.

*   Il livello finale elenca i rapporti di lavoro del lavoratore in quel giorno. Le informazioni visualizzate per ogni riga del rapporto di lavoro sono:

    *   *Codice Attività:* Il codice dell'attività a cui sono attribuite le ore tracciate.
    *   *Nome Attività:* Il nome dell'attività a cui sono attribuite le ore tracciate.
    *   *Ora di Inizio:* Facoltativo. È l'ora in cui la risorsa ha iniziato a lavorare sull'attività.
    *   *Ora di Fine:* Facoltativo. È l'ora in cui la risorsa ha terminato di lavorare sull'attività nella data specificata.
    *   *Campi di Testo:* Facoltativo. Se la riga del rapporto di lavoro ha campi di testo, i valori inseriti vengono mostrati qui. Il formato è: <Nome del campo di testo>:<Valore>
    *   *Etichette:* Dipende dal fatto che il modello del rapporto di lavoro abbia un campo etichetta nella sua definizione. Se ci sono più etichette, vengono mostrate nella stessa colonna. Il formato è: <Nome del tipo di etichetta>:<Valore dell'etichetta>
