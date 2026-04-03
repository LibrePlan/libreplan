Connettori
##########

.. contents::

I connettori sono applicazioni client di *LibrePlan* che possono essere utilizzate per comunicare con server (web) al fine di recuperare dati, elaborarli e memorizzarli. Attualmente esistono tre connettori: il connettore JIRA, il connettore Tim Enterprise e il connettore E-mail.

Configurazione
==============

I connettori devono essere configurati correttamente prima di poter essere utilizzati. Possono essere configurati dalla schermata "Impostazioni principali" nella scheda "Connettori".

La schermata del connettore comprende:

*   **Elenco a discesa:** Un elenco dei connettori disponibili.
*   **Schermata di modifica proprietà:** Un modulo di modifica delle proprietà per il connettore selezionato.
*   **Pulsante di test connessione:** Un pulsante per testare la connessione con il connettore.

Selezionare il connettore da configurare dall'elenco a discesa dei connettori. Verrà visualizzato un modulo di modifica delle proprietà per il connettore selezionato. Nel modulo di modifica delle proprietà è possibile modificare i valori delle proprietà secondo le necessità e testare le configurazioni utilizzando il pulsante "Test connessione".

.. NOTE::

   Le proprietà sono configurate con valori predefiniti. La proprietà più importante è "Attivato". Per impostazione predefinita è impostata su "N". Ciò indica che il connettore non verrà utilizzato a meno che non si modifichi il valore in "Y" e si salvino le modifiche.

Connettore JIRA
===============

JIRA è un sistema di tracciamento di problemi e progetti.

Il connettore JIRA è un'applicazione che può essere utilizzata per richiedere dati dal server web JIRA relativi ai problemi JIRA ed elaborare la risposta. La richiesta si basa sulle etichette JIRA. In JIRA, le etichette possono essere usate per categorizzare i problemi. La richiesta è strutturata come segue: recupera tutti i problemi categorizzati da questo nome di etichetta.

Il connettore riceve la risposta, che in questo caso sono i problemi, e li converte in "Elementi del progetto" e "Fogli ore" di *LibrePlan*.

Il *connettore JIRA* deve essere configurato correttamente prima di poter essere utilizzato.

Configurazione
--------------

Dalla schermata "Impostazioni principali", scegliere la scheda "Connettori". Nella schermata dei connettori, selezionare il connettore JIRA dall'elenco a discesa. Verrà quindi visualizzata una schermata di modifica delle proprietà.

In questa schermata è possibile configurare i seguenti valori delle proprietà:

*   **Attivato:** Y/N, indica se si desidera utilizzare il connettore JIRA. Il valore predefinito è "N".
*   **URL del server:** Il percorso assoluto al server web JIRA.
*   **Nome utente e password:** Le credenziali utente per l'autorizzazione.
*   **Etichette JIRA: elenco separato da virgole di etichette o URL:** È possibile inserire l'URL dell'etichetta o un elenco di etichette separate da virgole.
*   **Tipo di ore:** Il tipo di ore lavorative. Il valore predefinito è "Default".

.. NOTE::

   **Etichette JIRA:** Attualmente il server web JIRA non supporta la fornitura di un elenco di tutte le etichette disponibili. Come soluzione alternativa, è stato sviluppato un semplice script PHP che esegue una semplice query SQL nel database JIRA per recuperare tutte le etichette distinte. È possibile utilizzare questo script PHP come "URL etichette JIRA" oppure inserire le etichette desiderate come testo separato da virgole nel campo "Etichette JIRA".

Infine, fare clic sul pulsante "Test connessione" per verificare che sia possibile connettersi al server web JIRA e che le configurazioni siano corrette.

Sincronizzazione
----------------

Dalla finestra del progetto, in "Dati generali", è possibile avviare la sincronizzazione degli elementi del progetto con i problemi JIRA.

Fare clic sul pulsante "Sincronizza con JIRA" per avviare la sincronizzazione.

*   Se è la prima volta, verrà visualizzata una finestra a comparsa (con un elenco di etichette completato automaticamente). In questa finestra è possibile selezionare un'etichetta con cui sincronizzare e fare clic sul pulsante "Avvia sincronizzazione" per avviare il processo di sincronizzazione, oppure fare clic sul pulsante "Annulla" per annullarlo.

*   Se un'etichetta è già sincronizzata, l'ultima data di sincronizzazione e l'etichetta verranno visualizzate nella schermata JIRA. In questo caso, non verrà visualizzata alcuna finestra a comparsa per la selezione di un'etichetta. Al contrario, il processo di sincronizzazione inizierà direttamente per quella etichetta visualizzata (già sincronizzata).

.. NOTE::

   La relazione tra "Progetto" ed "etichetta" è uno a uno. È possibile sincronizzare una sola etichetta con un "Progetto".

.. NOTE::

   In caso di (ri)sincronizzazione avvenuta con successo, le informazioni verranno scritte nel database e la schermata JIRA verrà aggiornata con l'ultima data di sincronizzazione e l'etichetta.

La (ri)sincronizzazione viene eseguita in due fasi:

*   **Fase 1:** Sincronizzazione degli elementi del progetto, inclusi assegnazione e misurazioni dei progressi.
*   **Fase 2:** Sincronizzazione dei fogli ore.

.. NOTE::

   Se la Fase 1 non riesce, la Fase 2 non verrà eseguita e nessuna informazione verrà scritta nel database.

.. NOTE::

   Le informazioni sul successo o sull'errore verranno visualizzate in una finestra a comparsa.

Al completamento con successo della sincronizzazione, il risultato verrà visualizzato nella scheda "Work Breakdown Structure (attività WBS)" della schermata "Dettagli progetto". In questa interfaccia, ci sono due modifiche rispetto alla WBS standard:

*   La colonna "Ore totali attività" non è modificabile (sola lettura) perché la sincronizzazione è unidirezionale. Le ore delle attività possono essere aggiornate solo nel server web JIRA.
*   La colonna "Codice" mostra le chiavi dei problemi JIRA, che sono anche collegamenti ipertestuali ai problemi JIRA. Fare clic sulla chiave desiderata per accedere al documento corrispondente (problema JIRA).

Pianificazione
--------------

La risincronizzazione dei problemi JIRA può essere eseguita anche tramite il pianificatore. Andare alla schermata "Pianificazione dei job". In quella schermata è possibile configurare un job JIRA per eseguire la sincronizzazione. Il job cerca le ultime etichette sincronizzate nel database e le risincronizza di conseguenza. Vedere anche il Manuale del pianificatore.

Connettore Tim Enterprise
=========================

Tim Enterprise è un prodotto olandese di Aenova. È un'applicazione web per l'amministrazione del tempo dedicato a progetti e attività.

Il connettore Tim è un'applicazione che può essere utilizzata per comunicare con il server Tim Enterprise per:

*   Esportare tutte le ore dedicate da un lavoratore (utente) a un progetto che potrebbero essere registrate in Tim Enterprise.
*   Importare tutti i turni del lavoratore (utente) per pianificare la risorsa in modo efficace.

Il *connettore Tim* deve essere configurato correttamente prima di poter essere utilizzato.

Configurazione
--------------

Dalla schermata "Impostazioni principali", scegliere la scheda "Connettori". Nella schermata dei connettori, selezionare il connettore Tim dall'elenco a discesa. Verrà quindi visualizzata una schermata di modifica delle proprietà.

In questa schermata è possibile configurare i seguenti valori delle proprietà:

*   **Attivato:** Y/N, indica se si desidera utilizzare il connettore Tim. Il valore predefinito è "N".
*   **URL del server:** Il percorso assoluto al server Tim Enterprise.
*   **Nome utente e password:** Le credenziali utente per l'autorizzazione.
*   **Numero di giorni del foglio ore verso Tim:** Il numero di giorni precedenti per cui si desidera esportare i fogli ore.
*   **Numero di giorni del turno da Tim:** Il numero di giorni successivi per cui si desidera importare i turni.
*   **Fattore di produttività:** Ore lavorative effettive in percentuale. Il valore predefinito è "100%".
*   **ID reparto per importare il turno:** ID dei reparti separati da virgole.

Infine, fare clic sul pulsante "Test connessione" per verificare che sia possibile connettersi al server Tim Enterprise e che le configurazioni siano corrette.

Esportazione
------------

Dalla finestra del progetto, in "Dati generali", è possibile avviare l'esportazione dei fogli ore verso il server Tim Enterprise.

Inserire il "Codice prodotto Tim" e fare clic sul pulsante "Esporta verso Tim" per avviare l'esportazione.

Il connettore Tim aggiunge i seguenti campi insieme al codice prodotto:

*   Il nome completo del lavoratore/utente.
*   La data in cui il lavoratore ha lavorato su un'attività.
*   Lo sforzo, ovvero le ore lavorate sull'attività.
*   Un'opzione che indica se Tim Enterprise deve aggiornare la registrazione o inserirne una nuova.

La risposta di Tim Enterprise contiene solo un elenco di ID record (numeri interi). Questo rende difficile determinare cosa è andato storto, poiché l'elenco di risposta contiene solo numeri non correlati ai campi della richiesta. Si presume che la richiesta di esportazione (registrazione in Tim) abbia avuto successo se tutte le voci dell'elenco non contengono valori "0". In caso contrario, la richiesta di esportazione è fallita per le voci che contengono valori "0". Pertanto, non è possibile vedere quale richiesta è fallita, poiché le voci dell'elenco contengono solo il valore "0". L'unico modo per determinarlo è esaminare il file di log sul server Tim Enterprise.

.. NOTE::

   In caso di esportazione avvenuta con successo, le informazioni verranno scritte nel database e la schermata Tim verrà aggiornata con l'ultima data di esportazione e il codice prodotto.

.. NOTE::

   Le informazioni sul successo o sull'errore verranno visualizzate in una finestra a comparsa.

Pianificazione dell'esportazione
---------------------------------

Il processo di esportazione può essere eseguito anche tramite il pianificatore. Andare alla schermata "Pianificazione dei job". In quella schermata è possibile configurare un job di esportazione Tim. Il job cerca gli ultimi fogli ore esportati nel database e li riesporta di conseguenza. Vedere anche il Manuale del pianificatore.

Importazione
------------

L'importazione dei turni funziona solo con l'ausilio del pianificatore. Non è disponibile un'interfaccia utente per questo, poiché non è necessario alcun input dall'utente. Andare alla schermata "Pianificazione dei job" e configurare un job di importazione Tim. Il job scorre tutti i reparti configurati nelle proprietà del connettore e importa tutti i turni per ciascun reparto. Vedere anche il Manuale del pianificatore.

Per l'importazione, il connettore Tim aggiunge i seguenti campi nella richiesta:

*   **Periodo:** Il periodo (dalla data - alla data) per cui si desidera importare il turno. Può essere fornito come criterio di filtro.
*   **Reparto:** Il reparto per cui si desidera importare il turno. I reparti sono configurabili.
*   I campi di interesse (come informazioni sulla persona, categoria del turno, ecc.) che il server Tim deve includere nella risposta.

La risposta all'importazione contiene i seguenti campi, sufficienti per gestire i giorni di eccezione in *LibrePlan*:

*   **Informazioni sulla persona:** Nome e nome di rete.
*   **Reparto:** Il reparto in cui lavora il lavoratore.
*   **Categoria del turno:** Informazioni sulla presenza/assenza (Aanwzig/afwezig) del lavoratore e il motivo (tipo di eccezione di *LibrePlan*) in caso di assenza del lavoratore.
*   **Data:** La data in cui il lavoratore è presente/assente.
*   **Ora:** L'ora di inizio della presenza/assenza, ad esempio 08:00.
*   **Durata:** Il numero di ore in cui il lavoratore è presente/assente.

Convertendo la risposta all'importazione in "Giorno di eccezione" di *LibrePlan*, vengono prese in considerazione le seguenti traduzioni:

*   Se la categoria del turno contiene il nome "Vakantie", verrà tradotta in "FERIE DELLA RISORSA".
*   La categoria del turno "Feestdag" verrà tradotta in "FESTIVITÀ".
*   Tutte le altre, come "Jus uren", "PLB uren", ecc., devono essere aggiunte manualmente ai "Giorni di eccezione del calendario".

Inoltre, nella risposta all'importazione, il turno è diviso in due o tre parti al giorno: ad esempio, turno mattina, turno pomeriggio e turno sera. Tuttavia, *LibrePlan* consente un solo "Tipo di eccezione" al giorno. Il connettore Tim è quindi responsabile di unire queste parti in un unico tipo di eccezione. Vale a dire, la categoria del turno con la durata maggiore è considerata il tipo di eccezione valido, ma la durata totale è la somma di tutte le durate di queste parti della categoria.

Contrariamente a *LibrePlan*, in Tim Enterprise la durata totale in caso di ferie del lavoratore indica che il lavoratore non è disponibile per quella durata totale. Tuttavia, in *LibrePlan*, se il lavoratore è in ferie, la durata totale deve essere zero. Il connettore Tim gestisce anche questa conversione.

Connettore E-mail
=================

L'e-mail è un metodo di scambio di messaggi digitali da un autore a uno o più destinatari.

Il connettore E-mail può essere utilizzato per impostare le proprietà di connessione al server SMTP (Simple Mail Transfer Protocol).

Il *connettore E-mail* deve essere configurato correttamente prima di poter essere utilizzato.

Configurazione
--------------

Dalla schermata "Impostazioni principali", scegliere la scheda "Connettori". Nella schermata dei connettori, selezionare il connettore E-mail dall'elenco a discesa. Verrà quindi visualizzata una schermata di modifica delle proprietà.

In questa schermata è possibile configurare i seguenti valori delle proprietà:

*   **Attivato:** Y/N, indica se si desidera utilizzare il connettore E-mail. Il valore predefinito è "N".
*   **Protocollo:** Il tipo di protocollo SMTP.
*   **Host:** Il percorso assoluto al server SMTP.
*   **Porta:** La porta del server SMTP.
*   **Indirizzo mittente:** L'indirizzo e-mail del mittente del messaggio.
*   **Nome utente:** Il nome utente per il server SMTP.
*   **Password:** La password per il server SMTP.

Infine, fare clic sul pulsante "Test connessione" per verificare che sia possibile connettersi al server SMTP e che le configurazioni siano corrette.

Modifica del modello e-mail
----------------------------

Dalla finestra del progetto, in "Configurazione" e poi "Modifica modelli e-mail", è possibile modificare i modelli e-mail per i messaggi.

È possibile scegliere:

*   **Lingua del modello:**
*   **Tipo di modello:**
*   **Oggetto dell'e-mail:**
*   **Contenuto del modello:**

È necessario specificare la lingua perché l'applicazione web invierà e-mail agli utenti nella lingua che hanno scelto nelle loro preferenze. È necessario scegliere il tipo di modello. Il tipo corrisponde al ruolo utente, il che significa che questa e-mail verrà inviata solo agli utenti che ricoprono il ruolo selezionato (tipo). È necessario impostare l'oggetto dell'e-mail. L'oggetto è un breve riassunto dell'argomento del messaggio. È necessario impostare il contenuto dell'e-mail. Queste sono le informazioni che si desidera inviare all'utente. Esistono anche alcune parole chiave che è possibile utilizzare nel messaggio; l'applicazione web le analizzerà e imposterà un nuovo valore al posto della parola chiave.

Pianificazione delle e-mail
----------------------------

L'invio di e-mail può essere eseguito solo tramite il pianificatore. Andare a "Configurazione", quindi alla schermata "Pianificazione dei job". In quella schermata è possibile configurare un job di invio e-mail. Il job prende un elenco di notifiche e-mail, raccoglie dati e li invia all'e-mail dell'utente. Vedere anche il Manuale del pianificatore.

.. NOTE::

   Le informazioni sul successo o sull'errore verranno visualizzate in una finestra a comparsa.
