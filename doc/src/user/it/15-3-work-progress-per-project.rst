Rapporto del Lavoro e dell'Avanzamento per Progetto
####################################################

.. contents::

Scopo
=====

Questo rapporto fornisce una panoramica dello stato dei progetti, considerando sia l'avanzamento che i costi.

Analizza l'avanzamento corrente di ogni progetto, confrontandolo con l'avanzamento pianificato e il lavoro completato.

Il rapporto visualizza anche diversi indici relativi ai costi del progetto, confrontando le prestazioni correnti con quelle pianificate.

Parametri di Input e Filtri
============================

Ci sono diversi parametri obbligatori:

   *   **Data di Riferimento:** È la data utilizzata come punto di riferimento per confrontare lo stato pianificato del progetto con le sue prestazioni effettive. *Il valore predefinito per questo campo è la data corrente*.

   *   **Tipo di Avanzamento:** È il tipo di avanzamento utilizzato per misurare il progresso del progetto. L'applicazione consente di misurare un progetto simultaneamente con diversi tipi di avanzamento. Il tipo selezionato dall'utente nel menu a discesa viene utilizzato per calcolare i dati del rapporto. Il valore predefinito per il *tipo di avanzamento* è *distribuito*, che è un tipo di avanzamento speciale che utilizza il metodo preferito di misurazione dell'avanzamento configurato per ogni elemento WBS.

I parametri facoltativi sono:

   *   **Data di Inizio:** È la data di inizio più antica per i progetti da includere nel rapporto. Se questo campo viene lasciato vuoto, non vi è una data di inizio minima per i progetti.

   *   **Data di Fine:** È la data di fine più recente per i progetti da includere nel rapporto. Tutti i progetti che terminano dopo la *Data di Fine* saranno esclusi.

   *   **Filtro per Progetti:** Questo filtro consente agli utenti di selezionare i progetti specifici da includere nel rapporto. Se non vengono aggiunti progetti al filtro, il rapporto includerà tutti i progetti nel database. Viene fornito un menu a discesa con ricerca per trovare il progetto desiderato. I progetti vengono aggiunti al filtro facendo clic sul pulsante *Aggiungi*.

Output
======

Il formato di output è il seguente:

Intestazione
------------

L'intestazione del rapporto visualizza i seguenti campi:

   *   **Data di Inizio:** La data di inizio del filtro. Non viene visualizzata se il rapporto non è filtrato per questo campo.
   *   **Data di Fine:** La data di fine del filtro. Non viene visualizzata se il rapporto non è filtrato per questo campo.
   *   **Tipo di Avanzamento:** Il tipo di avanzamento utilizzato per il rapporto.
   *   **Progetti:** Indica i progetti filtrati per i quali viene generato il rapporto. Mostrerà la stringa *Tutti* quando il rapporto include tutti i progetti che soddisfano gli altri filtri.
   *   **Data di Riferimento:** La data di riferimento di input obbligatoria selezionata per il rapporto.

Piè di Pagina
-------------

Il piè di pagina visualizza la data in cui il rapporto è stato generato.

Corpo
-----

Il corpo del rapporto consiste in un elenco di progetti selezionati in base ai filtri di input.

I filtri funzionano aggiungendo condizioni, ad eccezione dell'insieme formato dai filtri per data (*Data di Inizio*, *Data di Fine*) e dal *Filtro per Progetti*. In questo caso, se uno o entrambi i filtri per data sono compilati e il *Filtro per Progetti* ha un elenco di progetti selezionati, quest'ultimo filtro ha la precedenza. Ciò significa che i progetti inclusi nel rapporto sono quelli forniti dal *Filtro per Progetti*, indipendentemente dai filtri per data.

È importante notare che l'avanzamento nel rapporto è calcolato come una frazione dell'unità, compresa tra 0 e 1.

Per ogni progetto selezionato per l'inclusione nell'output del rapporto, vengono visualizzate le seguenti informazioni:

   * *Nome Progetto*.
   * *Ore Totali*. Le ore totali per il progetto vengono mostrate sommando le ore per ogni attività. Vengono mostrati due tipi di ore totali:
      *   *Stimate (TE)*. È la somma di tutte le ore stimate nella WBS del progetto. Rappresenta il numero totale di ore stimate per completare il progetto.
      *   *Pianificate (TP)*. In *LibrePlan*, è possibile avere due quantità diverse: il numero stimato di ore per un'attività (il numero di ore inizialmente stimate per completare l'attività) e le ore pianificate (le ore allocate nel piano per completare l'attività). Le ore pianificate possono essere uguali, inferiori o superiori alle ore stimate e sono determinate in una fase successiva, l'operazione di assegnazione. Pertanto, le ore totali pianificate per un progetto sono la somma di tutte le ore allocate per le sue attività.
   * *Avanzamento*. Vengono mostrate tre misurazioni relative all'avanzamento complessivo del tipo specificato nel filtro di input dell'avanzamento per ogni progetto alla data di riferimento:
      *   *Misurato (PM)*. È l'avanzamento complessivo considerando le misurazioni dell'avanzamento con una data precedente alla *Data di Riferimento* nei parametri di input del rapporto. Vengono prese in considerazione tutte le attività e la somma è ponderata per il numero di ore di ogni attività.
      *   *Imputato (PI)*. È l'avanzamento assumendo che il lavoro continui allo stesso ritmo delle ore completate per un'attività. Se X ore su Y ore di un'attività sono completate, l'avanzamento imputato complessivo è considerato X/Y.
      *   *Pianificato (PP)*. È l'avanzamento complessivo del progetto in base alla pianificazione prevista alla data di riferimento. Se tutto si fosse svolto esattamente come pianificato, l'avanzamento misurato dovrebbe essere uguale all'avanzamento pianificato.
   * *Ore fino alla Data*. Ci sono due campi che mostrano il numero di ore fino alla data di riferimento da due prospettive:
      *   *Pianificate (HP)*. Questo numero è la somma delle ore allocate a qualsiasi attività del progetto con una data inferiore o uguale alla *Data di Riferimento*.
      *   *Effettive (HR)*. Questo numero è la somma delle ore riportate nei rapporti di lavoro per qualsiasi attività del progetto con una data inferiore o uguale alla *Data di Riferimento*.
   * *Differenza*. Sotto questa voce, ci sono diverse metriche relative ai costi:
      *   *Costo*. È la differenza in ore tra il numero di ore spese, considerando l'avanzamento misurato, e le ore completate fino alla data di riferimento. La formula è: *PM*TP - HR*.
      *   *Pianificato*. È la differenza tra le ore spese in base all'avanzamento complessivo misurato del progetto e quelle pianificate fino alla *Data di Riferimento*. Misura il vantaggio o il ritardo nel tempo. La formula è: *PM*TP - HP*.
      *   *Indice di Costo*. Viene calcolato dividendo *PM* / *PI*. Se è maggiore di 1, significa che il progetto è redditizio in questo momento. Se è minore di 1, significa che il progetto sta perdendo denaro.
      *   *Indice Pianificato*. Viene calcolato dividendo *PM* / *PP*. Se è maggiore di 1, significa che il progetto è in anticipo rispetto alla pianificazione. Se è minore di 1, significa che il progetto è in ritardo rispetto alla pianificazione.
