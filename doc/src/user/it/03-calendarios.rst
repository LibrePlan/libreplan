Calendari
#########

.. contents::

I calendari sono entità all'interno del programma che definiscono la capacità lavorativa delle risorse. Un calendario è composto da una serie di giorni nell'arco dell'anno, con ogni giorno diviso in ore lavorative disponibili.

Ad esempio, un giorno festivo potrebbe avere 0 ore lavorative disponibili. Al contrario, una tipica giornata lavorativa potrebbe avere 8 ore designate come tempo di lavoro disponibile.

Esistono due modi principali per definire il numero di ore lavorative in un giorno:

*   **Per Giorno della Settimana:** Questo metodo stabilisce un numero standard di ore lavorative per ogni giorno della settimana. Ad esempio, il lunedì potrebbe normalmente avere 8 ore lavorative.
*   **Per Eccezione:** Questo metodo consente deviazioni specifiche dallo standard del giorno della settimana. Ad esempio, il lunedì 30 gennaio potrebbe avere 10 ore lavorative, sostituendo il normale orario del lunedì.

Amministrazione dei Calendari
==============================

Il sistema dei calendari è gerarchico e consente di creare calendari base e poi derivare nuovi calendari da essi, formando una struttura ad albero. Un calendario derivato da un calendario di livello superiore erediterà i suoi orari giornalieri e le eccezioni, a meno che non vengano esplicitamente modificati. Per gestire efficacemente i calendari, è importante comprendere i seguenti concetti:

*   **Indipendenza dei Giorni:** Ogni giorno è trattato in modo indipendente e ogni anno ha il suo insieme di giorni. Ad esempio, se l'8 dicembre 2009 è un giorno festivo, ciò non significa automaticamente che l'8 dicembre 2010 sia anch'esso un giorno festivo.
*   **Giorni Lavorativi Basati sui Giorni della Settimana:** I giorni lavorativi standard si basano sui giorni della settimana. Ad esempio, se il lunedì normalmente ha 8 ore lavorative, allora tutti i lunedì di tutte le settimane di tutti gli anni avranno 8 ore disponibili, a meno che non venga definita un'eccezione.
*   **Eccezioni e Periodi di Eccezione:** È possibile definire eccezioni o periodi di eccezione per deviare dalla pianificazione standard dei giorni della settimana. Ad esempio, è possibile specificare un singolo giorno o un intervallo di giorni con un numero diverso di ore lavorative disponibili rispetto alla regola generale per quei giorni della settimana.

.. figure:: images/calendar-administration.png
   :scale: 50

   Amministrazione dei Calendari

L'amministrazione dei calendari è accessibile tramite il menu "Amministrazione". Da lì, gli utenti possono eseguire le seguenti azioni:

1.  Creare un nuovo calendario da zero.
2.  Creare un calendario derivato da uno esistente.
3.  Creare un calendario come copia di uno esistente.
4.  Modificare un calendario esistente.

Creazione di un Nuovo Calendario
---------------------------------

Per creare un nuovo calendario, fare clic sul pulsante "Crea". Il sistema visualizzerà un modulo in cui è possibile configurare quanto segue:

*   **Selezionare la Scheda:** Scegliere la scheda su cui si desidera lavorare:

    *   **Marcatura delle Eccezioni:** Definire le eccezioni alla pianificazione standard.
    *   **Ore Lavorative per Giorno:** Definire le ore lavorative standard per ogni giorno della settimana.

*   **Marcatura delle Eccezioni:** Se si seleziona l'opzione "Marcatura delle Eccezioni", è possibile:

    *   Selezionare un giorno specifico nel calendario.
    *   Selezionare il tipo di eccezione. I tipi disponibili sono: festività, malattia, sciopero, giorno festivo nazionale e festività lavorativa.
    *   Selezionare la data di fine del periodo di eccezione. (Questo campo non ha bisogno di essere modificato per le eccezioni di un singolo giorno.)
    *   Definire il numero di ore lavorative durante i giorni del periodo di eccezione.
    *   Eliminare le eccezioni precedentemente definite.

*   **Ore Lavorative per Giorno:** Se si seleziona l'opzione "Ore Lavorative per Giorno", è possibile:

    *   Definire le ore lavorative disponibili per ogni giorno della settimana (lunedì, martedì, mercoledì, giovedì, venerdì, sabato e domenica).
    *   Definire diverse distribuzioni orarie settimanali per periodi futuri.
    *   Eliminare le distribuzioni orarie precedentemente definite.

Queste opzioni consentono agli utenti di personalizzare completamente i calendari in base alle loro esigenze specifiche. Fare clic sul pulsante "Salva" per salvare le modifiche apportate al modulo.

.. figure:: images/calendar-edition.png
   :scale: 50

   Modifica dei Calendari

.. figure:: images/calendar-exceptions.png
   :scale: 50

   Aggiunta di un'Eccezione a un Calendario

Creazione di Calendari Derivati
--------------------------------

Un calendario derivato viene creato sulla base di un calendario esistente. Eredita tutte le caratteristiche del calendario originale, ma è possibile modificarlo per includere opzioni diverse.

Un caso d'uso comune per i calendari derivati è quando si dispone di un calendario generale per un paese, come la Spagna, e si ha la necessità di creare un calendario derivato per includere ulteriori festività specifiche di una regione, come la Galizia.

È importante notare che qualsiasi modifica apportata al calendario originale si propagherà automaticamente al calendario derivato, a meno che non sia stata definita un'eccezione specifica nel calendario derivato. Ad esempio, il calendario spagnolo potrebbe prevedere una giornata lavorativa di 8 ore il 17 maggio. Tuttavia, il calendario della Galizia (un calendario derivato) potrebbe non prevedere ore lavorative quello stesso giorno perché è un giorno festivo regionale. Se in seguito il calendario spagnolo viene modificato per avere 4 ore lavorative disponibili al giorno per la settimana del 17 maggio, anche il calendario della Galizia cambierà per avere 4 ore lavorative disponibili per ogni giorno di quella settimana, ad eccezione del 17 maggio, che rimarrà un giorno non lavorativo a causa dell'eccezione definita.

.. figure:: images/calendar-create-derived.png
   :scale: 50

   Creazione di un Calendario Derivato

Per creare un calendario derivato:

*   Andare al menu *Amministrazione*.
*   Fare clic sull'opzione *Amministrazione dei calendari*.
*   Selezionare il calendario che si desidera utilizzare come base per il calendario derivato e fare clic sul pulsante "Crea".
*   Il sistema visualizzerà un modulo di modifica con le stesse caratteristiche del modulo utilizzato per creare un calendario da zero, ad eccezione del fatto che le eccezioni proposte e le ore lavorative per giorno della settimana si baseranno sul calendario originale.

Creazione di un Calendario per Copia
--------------------------------------

Un calendario copiato è un duplicato esatto di un calendario esistente. Eredita tutte le caratteristiche del calendario originale, ma è possibile modificarlo in modo indipendente.

La differenza principale tra un calendario copiato e un calendario derivato sta nel modo in cui vengono influenzati dalle modifiche all'originale. Se il calendario originale viene modificato, il calendario copiato rimane invariato. I calendari derivati, invece, sono influenzati dalle modifiche apportate all'originale, a meno che non venga definita un'eccezione.

Un caso d'uso comune per i calendari copiati è quando si dispone di un calendario per una posizione, come "Pontevedra", e si ha la necessità di un calendario simile per un'altra posizione, come "A Coruña", dove la maggior parte delle caratteristiche è la stessa. Tuttavia, le modifiche a un calendario non dovrebbero influenzare l'altro.

Per creare un calendario copiato:

*   Andare al menu *Amministrazione*.
*   Fare clic sull'opzione *Amministrazione dei calendari*.
*   Selezionare il calendario che si desidera copiare e fare clic sul pulsante "Crea".
*   Il sistema visualizzerà un modulo di modifica con le stesse caratteristiche del modulo utilizzato per creare un calendario da zero, ad eccezione del fatto che le eccezioni proposte e le ore lavorative per giorno della settimana si baseranno sul calendario originale.

Calendario Predefinito
-----------------------

Uno dei calendari esistenti può essere designato come calendario predefinito. Questo calendario verrà assegnato automaticamente a qualsiasi entità nel sistema gestita con calendari, a meno che non venga specificato un calendario diverso.

Per impostare un calendario predefinito:

*   Andare al menu *Amministrazione*.
*   Fare clic sull'opzione *Configurazione*.
*   Nel campo *Calendario predefinito*, selezionare il calendario che si desidera utilizzare come calendario predefinito del programma.
*   Fare clic su *Salva*.

.. figure:: images/default-calendar.png
   :scale: 50

   Impostazione di un Calendario Predefinito

Assegnazione di un Calendario alle Risorse
-------------------------------------------

Le risorse possono essere attivate (ovvero avere ore lavorative disponibili) solo se dispongono di un calendario assegnato con un periodo di attivazione valido. Se a una risorsa non viene assegnato alcun calendario, viene assegnato automaticamente il calendario predefinito, con un periodo di attivazione che inizia alla data di inizio e non ha data di scadenza.

.. figure:: images/resource-calendar.png
   :scale: 50

   Calendario della Risorsa

Tuttavia, è possibile eliminare il calendario precedentemente assegnato a una risorsa e creare un nuovo calendario basato su uno esistente. Ciò consente una personalizzazione completa dei calendari per le singole risorse.

Per assegnare un calendario a una risorsa:

*   Andare all'opzione *Modifica risorse*.
*   Selezionare una risorsa e fare clic su *Modifica*.
*   Selezionare la scheda "Calendario".
*   Verranno visualizzati il calendario, le sue eccezioni, le ore lavorative per giorno e i periodi di attivazione.
*   Ogni scheda avrà le seguenti opzioni:

    *   **Eccezioni:** Definire le eccezioni e il periodo a cui si applicano, come festività, giorni festivi nazionali o giorni lavorativi diversi.
    *   **Settimana Lavorativa:** Modificare le ore lavorative per ogni giorno della settimana (lunedì, martedì, ecc.).
    *   **Periodi di Attivazione:** Creare nuovi periodi di attivazione per riflettere le date di inizio e fine dei contratti associati alla risorsa. Vedere l'immagine seguente.

*   Fare clic su *Salva* per memorizzare le informazioni.
*   Fare clic su *Elimina* se si desidera modificare il calendario assegnato a una risorsa.

.. figure:: images/new-resource-calendar.png
   :scale: 50

   Assegnazione di un Nuovo Calendario a una Risorsa

Assegnazione di Calendari ai Progetti
--------------------------------------

I progetti possono avere un calendario diverso da quello predefinito. Per modificare il calendario di un progetto:

*   Accedere all'elenco dei progetti nella panoramica aziendale.
*   Modificare il progetto in questione.
*   Accedere alla scheda "Informazioni generali".
*   Selezionare il calendario da assegnare dal menu a discesa.
*   Fare clic su "Salva" o "Salva e continua".

Assegnazione di Calendari alle Attività
-----------------------------------------

Analogamente alle risorse e ai progetti, è possibile assegnare calendari specifici alle singole attività. Ciò consente di definire calendari diversi per fasi specifiche di un progetto. Per assegnare un calendario a un'attività:

*   Accedere alla vista di pianificazione di un progetto.
*   Fare clic con il pulsante destro del mouse sull'attività a cui si desidera assegnare un calendario.
*   Selezionare l'opzione "Assegna calendario".
*   Selezionare il calendario da assegnare all'attività.
*   Fare clic su *Accetta*.
