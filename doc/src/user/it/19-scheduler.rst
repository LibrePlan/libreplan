Pianificatore
#############

.. contents::

Il pianificatore è progettato per pianificare i job in modo dinamico. È sviluppato utilizzando il *framework Spring Quartz scheduler*.

Per utilizzare questo pianificatore efficacemente, i job (Quartz job) da pianificare devono essere creati prima. Questi job possono quindi essere aggiunti al database, poiché tutti i job da pianificare sono memorizzati nel database.

All'avvio del pianificatore, questo legge i job da pianificare o de-pianificare dal database e li pianifica o rimuove di conseguenza. Successivamente, i job possono essere aggiunti, aggiornati o rimossi dinamicamente utilizzando l'interfaccia utente ``Pianificazione dei job``.

.. NOTE::
   Il pianificatore si avvia quando l'applicazione web LibrePlan si avvia e si arresta quando l'applicazione si arresta.

.. NOTE::
   Questo pianificatore supporta solo le ``espressioni cron`` per pianificare i job.

I criteri che il pianificatore utilizza per pianificare o rimuovere i job all'avvio sono i seguenti:

Per tutti i job:

* Pianifica

  * Il job ha un *Connettore*, il *Connettore* è attivato e il job può essere pianificato.
  * Il job non ha un *Connettore* ed è consentita la pianificazione.

* Rimuovi

  * Il job ha un *Connettore* e il *Connettore* non è attivato.
  * Il job ha un *Connettore*, il *Connettore* è attivato, ma il job non può essere pianificato.
  * Il job non ha un *Connettore* e non è consentita la pianificazione.

.. NOTE::
   I job non possono essere ripianificati o de-pianificati se sono attualmente in esecuzione.

Vista elenco pianificazione job
================================

La vista ``Elenco pianificazione job`` consente agli utenti di:

*   Aggiungere un nuovo job.
*   Modificare un job esistente.
*   Rimuovere un job.
*   Avviare un processo manualmente.

Aggiungere o modificare un job
================================

Dalla vista ``Elenco pianificazione job``, fare clic su:

*   ``Crea`` per aggiungere un nuovo job, oppure
*   ``Modifica`` per modificare il job selezionato.

Entrambe le azioni apriranno un ``modulo di creazione/modifica del job``. Il ``modulo`` visualizza le seguenti proprietà:

*   Campi:

    *   **Gruppo del job:** Il nome del gruppo del job.
    *   **Nome del job:** Il nome del job.
    *   **Espressione cron:** Un campo di sola lettura con un pulsante ``Modifica`` per aprire la finestra di inserimento dell'``espressione cron``.
    *   **Nome della classe del job:** Un ``elenco a discesa`` per selezionare il job (un job esistente).
    *   **Connettore:** Un ``elenco a discesa`` per selezionare un connettore. Non è obbligatorio.
    *   **Pianifica:** Una casella di controllo per indicare se pianificare questo job.

*   Pulsanti:

    *   **Salva:** Per salvare o aggiornare un job sia nel database che nel pianificatore. L'utente viene quindi riportato alla ``Vista elenco pianificazione job``.
    *   **Salva e continua:** Come "Salva", ma l'utente non viene riportato alla ``Vista elenco pianificazione job``.
    *   **Annulla:** Nulla viene salvato e l'utente viene riportato alla ``Vista elenco pianificazione job``.

*   E una sezione di suggerimenti sulla sintassi dell'espressione cron.

Finestra a comparsa espressione cron
--------------------------------------

Per inserire correttamente l'``espressione cron``, viene utilizzato un modulo a comparsa per l'``espressione cron``. In questo modulo è possibile inserire l'``espressione cron`` desiderata. Vedere anche il suggerimento sull'``espressione cron``. Se si inserisce un'``espressione cron`` non valida, si riceverà una notifica immediata.

Rimuovere un job
=================

Fare clic sul pulsante ``Rimuovi`` per eliminare il job sia dal database che dal pianificatore. Verrà visualizzato il successo o il fallimento di questa azione.

Avviare un job manualmente
===========================

Come alternativa all'attesa che il job venga eseguito come pianificato, è possibile fare clic su questo pulsante per avviare il processo direttamente. Successivamente, le informazioni sul successo o sul fallimento verranno visualizzate in una ``finestra a comparsa``.
