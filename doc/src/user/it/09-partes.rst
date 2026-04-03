Rapporti di Lavoro
##################

.. contents::

I rapporti di lavoro consentono di monitorare le ore che le risorse dedicano alle attività a cui sono assegnate.

Il programma consente agli utenti di configurare nuovi moduli per l'inserimento delle ore dedicate, specificando i campi che si desidera visualizzare in questi moduli. Ciò permette di incorporare i rapporti delle attività svolte dai lavoratori e di monitorare la loro attività.

Prima che gli utenti possano aggiungere voci per le risorse, devono definire almeno un tipo di rapporto di lavoro. Questo tipo definisce la struttura del rapporto, incluse tutte le righe che vi vengono aggiunte. Gli utenti possono creare tutti i tipi di rapporto di lavoro necessari all'interno del sistema.

Tipi di Rapporto di Lavoro
==========================

Un rapporto di lavoro è composto da una serie di campi comuni all'intero rapporto e da un insieme di righe di rapporto di lavoro con valori specifici per i campi definiti in ciascuna riga. Ad esempio, le risorse e le attività sono comuni a tutti i rapporti. Tuttavia, possono esistere altri nuovi campi, come gli "incidenti", che non sono richiesti in tutti i tipi di rapporto.

Gli utenti possono configurare diversi tipi di rapporto di lavoro in modo che un'azienda possa progettare i propri rapporti per soddisfare le proprie esigenze specifiche:

.. figure:: images/work-report-types.png
   :scale: 40

   Tipi di Rapporto di Lavoro

La gestione dei tipi di rapporto di lavoro consente agli utenti di configurare questi tipi e aggiungere nuovi campi di testo o tag opzionali. Nella prima scheda per la modifica dei tipi di rapporto di lavoro, è possibile configurare il tipo per gli attributi obbligatori (se si applicano all'intero rapporto o sono specificati a livello di riga) e aggiungere nuovi campi opzionali.

I campi obbligatori che devono apparire in tutti i rapporti di lavoro sono i seguenti:

*   **Nome e Codice:** Campi di identificazione per il nome del tipo di rapporto di lavoro e il suo codice.
*   **Data:** Campo per la data del rapporto.
*   **Risorsa:** Lavoratore o macchina indicato nel rapporto o nella riga del rapporto di lavoro.
*   **Elemento Progetto:** Codice dell'elemento del progetto a cui viene attribuito il lavoro svolto.
*   **Gestione Ore:** Determina la politica di attribuzione delle ore da utilizzare, che può essere:

    *   **In Base alle Ore Assegnate:** Le ore sono attribuite in base alle ore assegnate.
    *   **In Base agli Orari di Inizio e Fine:** Le ore sono calcolate in base agli orari di inizio e fine.
    *   **In Base al Numero di Ore e all'Intervallo di Inizio e Fine:** Sono consentite discrepanze e il numero di ore ha la priorità.

Gli utenti possono aggiungere nuovi campi ai rapporti:

*   **Tipo di Tag:** Gli utenti possono richiedere al sistema di visualizzare un tag durante la compilazione del rapporto di lavoro. Ad esempio, il tipo di tag cliente, se l'utente desidera inserire il cliente per cui è stato svolto il lavoro in ciascun rapporto.
*   **Campi Liberi:** Campi in cui è possibile inserire testo liberamente nel rapporto di lavoro.

.. figure:: images/work-report-type.png
   :scale: 50

   Creazione di un Tipo di Rapporto di Lavoro con Campi Personalizzati

Gli utenti possono configurare i campi data, risorsa ed elemento del progetto in modo che appaiano nell'intestazione del rapporto, il che significa che si applicano all'intero rapporto, oppure possono essere aggiunti a ciascuna delle righe.

Infine, è possibile aggiungere nuovi campi di testo aggiuntivi o tag a quelli esistenti, nell'intestazione del rapporto di lavoro o in ogni riga, utilizzando rispettivamente i campi "Testo aggiuntivo" e "Tipo di tag". Gli utenti possono configurare l'ordine in cui questi elementi devono essere inseriti nella scheda "Gestione dei campi aggiuntivi e dei tag".

Elenco dei Rapporti di Lavoro
==============================

Una volta configurato il formato dei rapporti da incorporare nel sistema, gli utenti possono inserire i dettagli nel modulo creato in base alla struttura definita nel tipo di rapporto di lavoro corrispondente. Per farlo, gli utenti devono seguire questi passaggi:

*   Fare clic sul pulsante "Nuovo rapporto di lavoro" associato al rapporto desiderato dall'elenco dei tipi di rapporto di lavoro.
*   Il programma visualizza quindi il rapporto in base alle configurazioni fornite per il tipo. Vedere l'immagine seguente.

.. figure:: images/work-report-type.png
   :scale: 50

   Struttura del Rapporto di Lavoro in Base al Tipo

*   Selezionare tutti i campi mostrati per il rapporto:

    *   **Risorsa:** Se è stata scelta l'intestazione, la risorsa viene visualizzata una sola volta. In alternativa, per ogni riga del rapporto è necessario scegliere una risorsa.
    *   **Codice Attività:** Codice dell'attività a cui viene assegnato il rapporto di lavoro. Come per gli altri campi, se il campo si trova nell'intestazione, il valore viene inserito una volta o tante volte quante necessarie nelle righe del rapporto.
    *   **Data:** Data del rapporto o di ogni riga, a seconda che sia configurata l'intestazione o la riga.
    *   **Numero di Ore:** Il numero di ore di lavoro nel progetto.
    *   **Orari di Inizio e Fine:** Orari di inizio e fine del lavoro per calcolare le ore di lavoro definitive. Questo campo appare solo nel caso delle politiche di assegnazione delle ore "In Base agli Orari di Inizio e Fine" e "In Base al Numero di Ore e all'Intervallo di Inizio e Fine".
    *   **Tipo di Ore:** Consente agli utenti di scegliere il tipo di ora, ad es. "Normale", "Straordinario", ecc.

*   Fare clic su "Salva" o "Salva e continua".
