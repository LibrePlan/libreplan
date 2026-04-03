Introduzione
############

.. contents::

Questo documento descrive le funzionalità di LibrePlan e fornisce informazioni all'utente su come configurare e utilizzare l'applicazione.

LibrePlan è un'applicazione web open-source per la pianificazione dei progetti. Il suo obiettivo principale è fornire una soluzione completa per la gestione dei progetti aziendali. Per qualsiasi informazione specifica necessaria su questo software, si prega di contattare il team di sviluppo all'indirizzo http://www.libreplan.com/contact/

.. figure:: images/company_view.png
   :scale: 50

   Panoramica aziendale

Panoramica Aziendale e Gestione delle Viste
===========================================

Come mostrato nella schermata principale del programma (si veda la schermata precedente) e nella panoramica aziendale, gli utenti possono visualizzare un elenco dei progetti pianificati. Ciò consente loro di comprendere lo stato generale dell'azienda riguardo ai progetti e all'utilizzo delle risorse. La panoramica aziendale offre tre viste distinte:

* **Vista Pianificazione:** Questa vista combina due prospettive:

   * **Monitoraggio dei Progetti e del Tempo:** Ogni progetto è rappresentato da un diagramma di Gantt, che indica le date di inizio e fine del progetto. Queste informazioni vengono visualizzate insieme alla scadenza concordata. Viene quindi effettuato un confronto tra la percentuale di avanzamento raggiunta e il tempo effettivamente dedicato a ciascun progetto. Questo fornisce un quadro chiaro delle prestazioni aziendali in qualsiasi momento. Questa vista è la pagina iniziale predefinita del programma.
   * **Grafico dell'Utilizzo delle Risorse Aziendali:** Questo grafico visualizza informazioni sull'allocazione delle risorse tra i progetti, fornendo un riepilogo dell'utilizzo delle risorse dell'intera azienda. Il verde indica che l'allocazione delle risorse è inferiore al 100% della capacità. La linea nera rappresenta la capacità totale delle risorse disponibili. Il giallo indica che l'allocazione delle risorse supera il 100%. È possibile avere una sotto-allocazione complessiva pur avendo contemporaneamente una sovra-allocazione per risorse specifiche.

* **Vista Carico Risorse:** Questa schermata visualizza un elenco dei lavoratori dell'azienda e le loro specifiche assegnazioni di attività, o assegnazioni generiche basate su criteri definiti. Per accedere a questa vista, fare clic su *Carico globale delle risorse*. Vedere l'immagine seguente per un esempio.
* **Vista Amministrazione Progetti:** Questa schermata visualizza un elenco dei progetti aziendali, consentendo agli utenti di eseguire le seguenti azioni: filtrare, modificare, eliminare, visualizzare la pianificazione o creare un nuovo progetto. Per accedere a questa vista, fare clic su *Elenco progetti*.

.. figure:: images/resources_global.png
   :scale: 50

   Panoramica delle Risorse

.. figure:: images/order_list.png
   :scale: 50

   Work Breakdown Structure

La gestione delle viste descritta sopra per la panoramica aziendale è molto simile alla gestione disponibile per un singolo progetto. A un progetto si può accedere in diversi modi:

* Fare clic con il pulsante destro del mouse sul diagramma di Gantt del progetto e selezionare *Pianifica*.
* Accedere all'elenco dei progetti e fare clic sull'icona del diagramma di Gantt.
* Creare un nuovo progetto e modificare la vista del progetto corrente.

Il programma offre le seguenti viste per un progetto:

* **Vista Pianificazione:** Questa vista consente agli utenti di visualizzare la pianificazione delle attività, le dipendenze, le pietre miliari e altro ancora. Consultare la sezione *Pianificazione* per ulteriori dettagli.
* **Vista Carico Risorse:** Questa vista consente agli utenti di verificare il carico di risorse designato per un progetto. Il codice colore è coerente con la panoramica aziendale: verde per un carico inferiore al 100%, giallo per un carico pari al 100% e rosso per un carico superiore al 100%. Il carico può derivare da un'attività specifica o da un insieme di criteri (assegnazione generica).
* **Vista Modifica Progetto:** Questa vista consente agli utenti di modificare i dettagli del progetto. Consultare la sezione *Progetti* per ulteriori informazioni.
* **Vista Allocazione Avanzata delle Risorse:** Questa vista consente agli utenti di allocare risorse con opzioni avanzate, come la specifica delle ore per giorno o delle funzioni allocate da svolgere. Consultare la sezione *Allocazione delle risorse* per ulteriori informazioni.

Perché LibrePlan È Utile?
=========================

LibrePlan è uno strumento di pianificazione generico sviluppato per affrontare le sfide nella pianificazione di progetti industriali che non erano adeguatamente coperte dagli strumenti esistenti. Lo sviluppo di LibrePlan è stato anche motivato dal desiderio di fornire un'alternativa gratuita, open-source e interamente basata sul web agli strumenti di pianificazione proprietari.

I concetti fondamentali alla base del programma sono i seguenti:

* **Panoramica Aziendale e Multi-progetto:** LibrePlan è specificamente progettato per fornire agli utenti informazioni su più progetti in corso all'interno di un'azienda. È quindi intrinsecamente un programma multi-progetto. L'attenzione del programma non è limitata ai singoli progetti, sebbene siano disponibili anche viste specifiche per i singoli progetti.
* **Gestione delle Viste:** La panoramica aziendale, o vista multi-progetto, è accompagnata da varie viste delle informazioni memorizzate. Ad esempio, la panoramica aziendale consente agli utenti di visualizzare i progetti e confrontarne lo stato, vedere il carico complessivo delle risorse aziendali e gestire i progetti. Gli utenti possono anche accedere alla vista pianificazione, alla vista carico risorse, alla vista allocazione avanzata delle risorse e alla vista modifica progetto per i singoli progetti.
* **Criteri:** I criteri sono un'entità di sistema che consente la classificazione sia delle risorse (umane e meccaniche) che delle attività. Le risorse devono soddisfare determinati criteri e le attività richiedono che vengano soddisfatti criteri specifici. Questa è una delle funzionalità più importanti del programma, poiché i criteri costituiscono la base dell'allocazione generica e affrontano una sfida significativa nel settore: la natura dispendiosa in termini di tempo della gestione delle risorse umane e la difficoltà delle stime del carico aziendale a lungo termine.
* **Risorse:** Esistono due tipi di risorse: umane e meccaniche. Le risorse umane sono i lavoratori dell'azienda, utilizzati per pianificare, monitorare e controllare il carico di lavoro dell'azienda. Le risorse meccaniche, dipendenti dalle persone che le operano, funzionano in modo simile alle risorse umane.
* **Allocazione delle Risorse:** Una caratteristica chiave del programma è la capacità di designare le risorse in due modi: specificamente e genericamente. L'allocazione generica si basa sui criteri necessari per completare un'attività e deve essere soddisfatta dalle risorse in grado di soddisfare tali criteri. Per comprendere l'allocazione generica, si consideri questo esempio: Mario Rossi è un saldatore. Tipicamente, Mario Rossi verrebbe specificamente assegnato a un'attività pianificata. Tuttavia, LibrePlan offre la possibilità di selezionare qualsiasi saldatore all'interno dell'azienda, senza la necessità di specificare che Mario Rossi è la persona assegnata.
* **Controllo del Carico Aziendale:** Il programma consente un facile controllo del carico delle risorse aziendali. Questo controllo si estende sia al medio che al lungo termine, poiché i progetti attuali e futuri possono essere gestiti all'interno del programma. LibrePlan fornisce grafici che rappresentano visivamente l'utilizzo delle risorse.
* **Etichette:** Le etichette vengono utilizzate per categorizzare le attività del progetto. Con queste etichette, gli utenti possono raggruppare le attività per concetto, consentendo una revisione successiva come gruppo o dopo il filtraggio.
* **Filtri:** Poiché il sistema include naturalmente elementi che etichettano o caratterizzano attività e risorse, è possibile utilizzare filtri per criteri o etichette. Questo è molto utile per rivedere le informazioni categorizzate o generare report specifici basati su criteri o etichette.
* **Calendari:** I calendari definiscono le ore produttive disponibili per le diverse risorse. Gli utenti possono creare calendari aziendali generali o definire calendari più specifici, consentendo la creazione di calendari per singole risorse e attività.
* **Progetti ed Elementi del Progetto:** Il lavoro richiesto dai clienti viene trattato come un progetto all'interno dell'applicazione, strutturato in elementi del progetto. Il progetto e i suoi elementi seguono una struttura gerarchica con *x* livelli. Questo albero di elementi costituisce la base per la pianificazione del lavoro.
* **Avanzamento:** Il programma può gestire vari tipi di avanzamento. L'avanzamento di un progetto può essere misurato come percentuale, in unità, rispetto al budget concordato e altro ancora. La responsabilità di determinare quale tipo di avanzamento utilizzare per il confronto ai livelli superiori del progetto spetta al responsabile della pianificazione.
* **Attività:** Le attività sono gli elementi di pianificazione fondamentali all'interno del programma. Vengono utilizzate per pianificare il lavoro da svolgere. Le caratteristiche principali delle attività includono: le dipendenze tra attività e il potenziale requisito che vengano soddisfatti criteri specifici prima che le risorse possano essere allocate.
* **Rapporti di Lavoro:** Questi rapporti, inviati dai lavoratori dell'azienda, descrivono in dettaglio le ore lavorate e le attività associate a quelle ore. Queste informazioni consentono al sistema di calcolare il tempo effettivo impiegato per completare un'attività rispetto al tempo preventivato. L'avanzamento può quindi essere confrontato con le ore effettive utilizzate.

Oltre alle funzioni principali, LibrePlan offre altre funzionalità che lo distinguono da programmi simili:

* **Integrazione con ERP:** Il programma può importare direttamente le informazioni dai sistemi ERP aziendali, inclusi progetti, risorse umane, rapporti di lavoro e criteri specifici.
* **Gestione delle Versioni:** Il programma può gestire più versioni di pianificazione, pur consentendo agli utenti di rivedere le informazioni di ciascuna versione.
* **Gestione della Cronologia:** Il programma non elimina le informazioni; le contrassegna semplicemente come non valide. Ciò consente agli utenti di rivedere le informazioni storiche utilizzando filtri per data.

Convenzioni di Usabilità
========================

Informazioni sui Moduli
-----------------------
Prima di descrivere le varie funzioni associate ai moduli più importanti, è necessario spiegare la navigazione generale e il comportamento dei moduli.

Esistono essenzialmente tre tipi di moduli di modifica:

* **Moduli con un pulsante *Torna*:** Questi moduli fanno parte di un contesto più ampio e le modifiche apportate vengono memorizzate in memoria. Le modifiche vengono applicate solo quando l'utente salva esplicitamente tutti i dettagli nella schermata da cui è originato il modulo.
* **Moduli con pulsanti *Salva* e *Chiudi*:** Questi moduli consentono due azioni. La prima salva le modifiche e chiude la finestra corrente. La seconda chiude la finestra senza salvare le modifiche.
* **Moduli con pulsanti *Salva e continua*, *Salva* e *Chiudi*:** Questi moduli consentono tre azioni. La prima salva le modifiche e mantiene aperto il modulo corrente. La seconda salva le modifiche e chiude il modulo. La terza chiude la finestra senza salvare le modifiche.

Icone e Pulsanti Standard
--------------------------

* **Modifica:** In generale, i record nel programma possono essere modificati facendo clic su un'icona che assomiglia a una matita su un taccuino bianco.
* **Rientro a Sinistra:** Queste operazioni vengono generalmente utilizzate per gli elementi all'interno di una struttura ad albero che devono essere spostati a un livello più profondo. Questo viene fatto facendo clic sull'icona che assomiglia a una freccia verde che punta a destra.
* **Rientro a Destra:** Queste operazioni vengono generalmente utilizzate per gli elementi all'interno di una struttura ad albero che devono essere spostati a un livello superiore. Questo viene fatto facendo clic sull'icona che assomiglia a una freccia verde che punta a sinistra.
* **Eliminazione:** Gli utenti possono eliminare le informazioni facendo clic sull'icona del cestino.
* **Ricerca:** L'icona della lente d'ingrandimento indica che il campo di testo alla sua sinistra viene utilizzato per la ricerca di elementi.

Schede
------
Il programma utilizza le schede per organizzare i moduli di modifica e amministrazione del contenuto. Questo metodo viene utilizzato per dividere un modulo completo in diverse sezioni, accessibili facendo clic sui nomi delle schede. Le altre schede mantengono il loro stato corrente. In tutti i casi, le opzioni di salvataggio e annullamento si applicano a tutti i sottomoduli nelle diverse schede.

Azioni Esplicite e Aiuto Contestuale
-------------------------------------

Il programma include componenti che forniscono descrizioni aggiuntive degli elementi quando il mouse vi si sofferma sopra per un secondo. Le azioni che l'utente può eseguire sono indicate sulle etichette dei pulsanti, nei testi di aiuto associati, nelle opzioni del menu di navigazione e nei menu contestuali che appaiono quando si fa clic con il pulsante destro del mouse nell'area del pianificatore. Inoltre, vengono forniti collegamenti rapidi per le operazioni principali, come il doppio clic sugli elementi elencati o l'utilizzo di eventi della tastiera con il cursore e il tasto Invio per aggiungere elementi durante la navigazione attraverso i moduli.
