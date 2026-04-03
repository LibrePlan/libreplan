Dashboard di Progetto
#####################

.. contents::

Il dashboard di progetto è una prospettiva di *LibrePlan* che contiene un insieme di **KPI (Indicatori Chiave di Prestazione)** per aiutare a valutare le prestazioni di un progetto in termini di:

   *   Avanzamento del lavoro
   *   Costi
   *   Stato delle risorse allocate
   *   Vincoli temporali

Indicatori di Prestazione dell'Avanzamento
==========================================

Vengono calcolati due indicatori: la percentuale di avanzamento del progetto e lo stato delle attività.

Percentuale di Avanzamento del Progetto
---------------------------------------

Questo grafico mostra l'avanzamento complessivo di un progetto, confrontandolo con l'avanzamento previsto in base al diagramma di *Gantt*.

L'avanzamento è rappresentato da due barre:

   *   *Avanzamento Corrente:* L'avanzamento corrente in base alle misurazioni effettuate.
   *   *Avanzamento Previsto:* L'avanzamento che il progetto avrebbe dovuto raggiungere a questo punto, secondo il piano di progetto.

Per visualizzare il valore misurato effettivo per ciascuna barra, passare il cursore del mouse sopra la barra.

L'avanzamento complessivo del progetto viene stimato utilizzando diversi metodi, poiché non esiste un approccio unico e universalmente corretto:

   *   **Avanzamento Distribuito:** È il tipo di avanzamento impostato come avanzamento distribuito a livello di progetto. In questo caso, non è possibile calcolare un valore atteso e viene visualizzata solo la barra corrente.
   *   **Per Tutte le Ore delle Attività:** L'avanzamento di tutte le attività del progetto viene mediato per calcolare il valore complessivo. Questa è una media ponderata che considera il numero di ore allocate a ciascuna attività.
   *   **Per Ore del Percorso Critico:** L'avanzamento delle attività appartenenti a uno qualsiasi dei percorsi critici del progetto viene mediato per ottenere il valore complessivo. Questa è una media ponderata che considera le ore totali allocate per ciascuna attività coinvolta.
   *   **Per Durata del Percorso Critico:** L'avanzamento delle attività appartenenti a uno qualsiasi dei percorsi critici viene mediato utilizzando una media ponderata, ma questa volta considerando la durata di ciascuna attività coinvolta invece delle ore assegnate.

Stato delle Attività
--------------------

Un grafico a torta mostra la percentuale delle attività del progetto nei diversi stati. Gli stati definiti sono:

   *   **Terminato:** Attività completate, identificate da un valore di avanzamento del 100%.
   *   **In Corso:** Attività attualmente in corso. Queste attività hanno un valore di avanzamento diverso da 0% o 100%, oppure è stato tracciato del tempo di lavoro.
   *   **Pronto per Iniziare:** Attività con 0% di avanzamento, nessun tempo tracciato, tutte le attività dipendenti con *FINISH_TO_START* sono *terminate* e tutte le attività dipendenti con *START_TO_START* sono *terminate* o *in corso*.
   *   **Bloccato:** Attività con 0% di avanzamento, nessun tempo tracciato, e con attività dipendenti precedenti che non sono né *in corso* né nello stato *pronto per iniziare*.

Indicatori di Costo
===================

Vengono calcolati diversi indicatori di costo basati sulla *Gestione del Valore Guadagnato*:

   *   **CV (Varianza dei Costi):** La differenza tra la *curva del Valore Guadagnato* e la *curva del Costo Effettivo* al momento corrente. I valori positivi indicano un beneficio e i valori negativi indicano una perdita.
   *   **ACWP (Costo Effettivo del Lavoro Eseguito):** Il numero totale di ore tracciate nel progetto al momento corrente.
   *   **CPI (Indice di Prestazione dei Costi):** Il rapporto *Valore Guadagnato / Costo Effettivo*.

        *   > 100 è favorevole, indica che il progetto è sotto budget.
        *   = 100 è anche favorevole, indica che il costo è esattamente in linea con il piano.
        *   < 100 è sfavorevole, indica che il costo per completare il lavoro è superiore al previsto.
   *   **ETC (Stima per Completare):** Il tempo rimanente per completare il progetto.
   *   **BAC (Budget al Completamento):** La quantità totale di lavoro allocato nel piano di progetto.
   *   **EAC (Stima al Completamento):** La proiezione del manager del costo totale al completamento del progetto, basata sul *CPI*.
   *   **VAC (Varianza al Completamento):** La differenza tra il *BAC* e l'*EAC*.

        *   < 0 indica che il progetto è oltre il budget.
        *   > 0 indica che il progetto è sotto budget.

Risorse
=======

Per analizzare il progetto dal punto di vista delle risorse, vengono forniti due indici e un istogramma.

Istogramma della Deviazione Stimata sulle Attività Completate
-------------------------------------------------------------

Questo istogramma calcola la deviazione tra il numero di ore allocate alle attività del progetto e il numero effettivo di ore ad esse dedicate.

La deviazione è calcolata come percentuale per tutte le attività terminate, e le deviazioni calcolate sono rappresentate in un istogramma. L'asse verticale mostra il numero di attività all'interno di ciascun intervallo di deviazione. Vengono calcolati dinamicamente sei intervalli di deviazione.

Indice di Straordinario
-----------------------

Questo indice riassume il sovraccarico delle risorse allocate alle attività del progetto. Viene calcolato usando la formula: **indice di straordinario = sovraccarico / (carico + sovraccarico)**.

   *   = 0 è favorevole, indica che le risorse non sono sovraccaricate.
   *   > 0 è sfavorevole, indica che le risorse sono sovraccaricate.

Indice di Disponibilità
-----------------------

Questo indice riassume la capacità libera delle risorse attualmente allocate al progetto. Pertanto, misura la disponibilità delle risorse a ricevere ulteriori allocazioni senza essere sovraccaricate. Viene calcolato come: **indice di disponibilità = (1 - carico/capacità) * 100**

   *   I valori possibili sono compresi tra 0% (completamente assegnato) e 100% (non assegnato).

Tempo
=====

Sono inclusi due grafici: un istogramma per la deviazione temporale nel tempo di completamento delle attività del progetto e un grafico a torta per le violazioni delle scadenze.

Anticipo o Ritardo nel Completamento delle Attività
----------------------------------------------------

Questo calcolo determina la differenza in giorni tra il tempo di fine pianificato per le attività del progetto e il loro tempo di fine effettivo. La data di completamento pianificata viene presa dal diagramma di *Gantt* e la data di fine effettiva viene presa dall'ultimo tempo tracciato per l'attività.

Il ritardo o l'anticipo nel completamento delle attività è rappresentato in un istogramma. L'asse verticale mostra il numero di attività con un valore di differenza di giorni di anticipo/ritardo corrispondente all'intervallo di giorni dell'ascissa. Vengono calcolati sei intervalli dinamici di deviazione del completamento delle attività.

   *   I valori negativi indicano il completamento in anticipo rispetto alla pianificazione.
   *   I valori positivi indicano il completamento in ritardo rispetto alla pianificazione.

Violazioni delle Scadenze
--------------------------

Questa sezione calcola il margine con la scadenza del progetto, se impostata. Inoltre, un grafico a torta mostra la percentuale di attività che rispettano la loro scadenza. Nel grafico sono inclusi tre tipi di valori:

   *   Percentuale di attività senza una scadenza configurata.
   *   Percentuale di attività terminate con una data di fine effettiva successiva alla loro scadenza. La data di fine effettiva viene presa dall'ultimo tempo tracciato per l'attività.
   *   Percentuale di attività terminate con una data di fine effettiva precedente alla loro scadenza.
