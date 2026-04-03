Gestione delle Risorse
######################

.. _recursos:
.. contents::

Il programma gestisce due tipi distinti di risorse: personale e macchine.

Risorse di Personale
---------------------

Le risorse di personale rappresentano i lavoratori dell'azienda. Le loro caratteristiche principali sono:

*   Soddisfano uno o più criteri generici o specifici per i lavoratori.
*   Possono essere specificamente assegnate a un'attività.
*   Possono essere assegnate genericamente a un'attività che richiede un criterio di risorsa.
*   Possono avere un calendario predefinito o specifico, secondo necessità.

Risorse Macchina
-----------------

Le risorse macchina rappresentano il macchinario aziendale. Le loro caratteristiche principali sono:

*   Soddisfano uno o più criteri generici o specifici per le macchine.
*   Possono essere specificamente assegnate a un'attività.
*   Possono essere assegnate genericamente a un'attività che richiede un criterio macchina.
*   Possono avere un calendario predefinito o specifico, secondo necessità.
*   Il programma include una schermata di configurazione in cui è possibile definire un valore *alpha* per rappresentare il rapporto macchina/lavoratore.

    *   Il valore *alpha* indica la quantità di tempo del lavoratore necessaria per far funzionare la macchina. Ad esempio, un valore alpha di 0,5 significa che ogni 8 ore di funzionamento della macchina richiedono 4 ore del tempo di un lavoratore.
    *   Gli utenti possono assegnare un valore *alpha* specificamente a un lavoratore, designando quel lavoratore per far funzionare la macchina per quella percentuale di tempo.
    *   Gli utenti possono anche effettuare un'assegnazione generica basata su un criterio, in modo che una percentuale di utilizzo venga assegnata a tutte le risorse che soddisfano quel criterio e che hanno tempo disponibile. L'assegnazione generica funziona in modo simile all'assegnazione generica per le attività, come descritto in precedenza.

Gestione delle Risorse
-----------------------

Gli utenti possono creare, modificare e disattivare (ma non eliminare definitivamente) lavoratori e macchine all'interno dell'azienda navigando nella sezione "Risorse". Questa sezione fornisce le seguenti funzionalità:

*   **Elenco dei Lavoratori:** Visualizza un elenco numerato dei lavoratori, consentendo agli utenti di gestire i loro dettagli.
*   **Elenco delle Macchine:** Visualizza un elenco numerato delle macchine, consentendo agli utenti di gestire i loro dettagli.

Gestione dei Lavoratori
========================

La gestione dei lavoratori è accessibile andando alla sezione "Risorse" e poi selezionando "Elenco dei lavoratori". Gli utenti possono modificare qualsiasi lavoratore nell'elenco facendo clic sull'icona di modifica standard.

Quando si modifica un lavoratore, gli utenti possono accedere alle seguenti schede:

1.  **Dettagli del Lavoratore:** Questa scheda consente agli utenti di modificare i dettagli di identificazione di base del lavoratore:

    *   Nome
    *   Cognome/i
    *   Documento di identità nazionale (DNI)
    *   Risorsa basata su coda (vedere la sezione sulle Risorse Basate su Coda)

    .. figure:: images/worker-personal-data.png
       :scale: 50

       Modifica dei Dati Personali dei Lavoratori

2.  **Criteri:** Questa scheda viene utilizzata per configurare i criteri che un lavoratore soddisfa. Gli utenti possono assegnare qualsiasi criterio lavoratore o generico che ritengano appropriato. È fondamentale che i lavoratori soddisfino i criteri per massimizzare la funzionalità del programma. Per assegnare i criteri:

    i.  Fare clic sul pulsante "Aggiungi criteri".
    ii. Cercare il criterio da aggiungere e selezionare quello più appropriato.
    iii. Fare clic sul pulsante "Aggiungi".
    iv. Selezionare la data di inizio in cui il criterio diventa applicabile.
    v.  Selezionare la data di fine per l'applicazione del criterio alla risorsa. Questa data è facoltativa se il criterio è considerato indefinito.

    .. figure:: images/worker-criterions.png
       :scale: 50

       Associazione dei Criteri ai Lavoratori

3.  **Calendario:** Questa scheda consente agli utenti di configurare un calendario specifico per il lavoratore. A tutti i lavoratori viene assegnato un calendario predefinito; tuttavia, è possibile assegnare un calendario specifico a ciascun lavoratore basandosi su un calendario esistente.

    .. figure:: images/worker-calendar.png
       :scale: 50

       Scheda Calendario per una Risorsa

4.  **Categoria di Costo:** Questa scheda consente agli utenti di configurare la categoria di costo che un lavoratore soddisfa durante un determinato periodo. Queste informazioni vengono utilizzate per calcolare i costi associati a un lavoratore su un progetto.

    .. figure:: images/worker-costcategory.png
       :scale: 50

       Scheda Categoria di Costo per una Risorsa

L'assegnazione delle risorse è spiegata nella sezione "Assegnazione delle Risorse".

Gestione delle Macchine
========================

Le macchine vengono trattate come risorse a tutti gli effetti. Pertanto, in modo simile ai lavoratori, le macchine possono essere gestite e assegnate alle attività. L'assegnazione delle risorse è trattata nella sezione "Assegnazione delle Risorse", che spiegherà le caratteristiche specifiche delle macchine.

Le macchine vengono gestite dalla voce di menu "Risorse". Questa sezione ha un'operazione chiamata "Elenco macchine", che visualizza le macchine dell'azienda. Gli utenti possono modificare o eliminare una macchina da questo elenco.

Quando si modificano le macchine, il sistema visualizza una serie di schede per la gestione di diversi dettagli:

1.  **Dettagli della Macchina:** Questa scheda consente agli utenti di modificare i dettagli di identificazione della macchina:

    i.  Nome
    ii. Codice macchina
    iii. Descrizione della macchina

    .. figure:: images/machine-data.png
       :scale: 50

       Modifica dei Dettagli della Macchina

2.  **Criteri:** Come per le risorse lavoratore, questa scheda viene utilizzata per aggiungere criteri che la macchina soddisfa. Alle macchine possono essere assegnati due tipi di criteri: specifici per le macchine o generici. I criteri lavoratore non possono essere assegnati alle macchine. Per assegnare i criteri:

    i.  Fare clic sul pulsante "Aggiungi criteri".
    ii. Cercare il criterio da aggiungere e selezionare quello più appropriato.
    iii. Selezionare la data di inizio in cui il criterio diventa applicabile.
    iv. Selezionare la data di fine per l'applicazione del criterio alla risorsa. Questa data è facoltativa se il criterio è considerato indefinito.
    v.  Fare clic sul pulsante "Salva e continua".

    .. figure:: images/machine-criterions.png
       :scale: 50

       Assegnazione dei Criteri alle Macchine

3.  **Calendario:** Questa scheda consente agli utenti di configurare un calendario specifico per la macchina. A tutte le macchine viene assegnato un calendario predefinito; tuttavia, è possibile assegnare un calendario specifico a ciascuna macchina basandosi su un calendario esistente.

    .. figure:: images/machine-calendar.png
       :scale: 50

       Assegnazione dei Calendari alle Macchine

4.  **Configurazione della Macchina:** Questa scheda consente agli utenti di configurare il rapporto tra macchine e risorse lavoratore. Una macchina ha un valore alpha che indica il rapporto macchina/lavoratore. Come accennato in precedenza, un valore alpha di 0,5 indica che 0,5 persone sono necessarie per ogni giornata intera di funzionamento della macchina. In base al valore alpha, il sistema assegna automaticamente i lavoratori associati alla macchina una volta che la macchina viene assegnata a un'attività. L'associazione di un lavoratore a una macchina può essere effettuata in due modi:

    i.  **Assegnazione Specifica:** Assegnare un intervallo di date durante il quale il lavoratore è assegnato alla macchina. Questa è un'assegnazione specifica, poiché il sistema assegna automaticamente le ore al lavoratore quando la macchina è pianificata.
    ii. **Assegnazione Generica:** Assegnare criteri che devono essere soddisfatti dai lavoratori assegnati alla macchina. Questo crea un'assegnazione generica dei lavoratori che soddisfano i criteri.

    .. figure:: images/machine-configuration.png
       :scale: 50

       Configurazione delle Macchine

5.  **Categoria di Costo:** Questa scheda consente agli utenti di configurare la categoria di costo che una macchina soddisfa durante un determinato periodo. Queste informazioni vengono utilizzate per calcolare i costi associati a una macchina su un progetto.

    .. figure:: images/machine-costcategory.png
       :scale: 50

       Assegnazione delle Categorie di Costo alle Macchine

Gruppi di Lavoratori Virtuali
==============================

Il programma consente agli utenti di creare gruppi di lavoratori virtuali, che non sono lavoratori reali ma personale simulato. Questi gruppi consentono agli utenti di modellare l'aumento della capacità produttiva in momenti specifici, in base alle impostazioni del calendario.

I gruppi di lavoratori virtuali consentono agli utenti di valutare come la pianificazione del progetto sarebbe influenzata dall'assunzione e dall'assegnazione di personale che soddisfa criteri specifici, aiutando così il processo decisionale.

Le schede per la creazione di gruppi di lavoratori virtuali sono le stesse di quelle per la configurazione dei lavoratori:

*   Dettagli Generali
*   Criteri Assegnati
*   Calendari
*   Ore Associate

La differenza tra i gruppi di lavoratori virtuali e i lavoratori effettivi è che i gruppi di lavoratori virtuali hanno un nome per il gruppo e una quantità, che rappresenta il numero di persone reali nel gruppo. C'è anche un campo per i commenti, dove è possibile fornire informazioni aggiuntive, come quale progetto richiederebbe l'assunzione di un equivalente al gruppo di lavoratori virtuali.

.. figure:: images/virtual-resources.png
   :scale: 50

   Risorse Virtuali

Risorse Basate su Coda
=======================

Le risorse basate su coda sono un tipo specifico di elemento produttivo che può essere non assegnato o avere una dedica del 100%. In altre parole, non possono avere più di un'attività pianificata contemporaneamente e non possono essere sovra-allocate.

Per ogni risorsa basata su coda, viene creata automaticamente una coda. Le attività pianificate per queste risorse possono essere gestite specificamente utilizzando i metodi di assegnazione forniti, creando assegnazioni automatiche tra attività e code che soddisfano i criteri richiesti, o spostando attività tra code.
