Scheduler
#########

.. contents::

Der Scheduler ist für die dynamische Planung von Aufträgen konzipiert. Er wurde mit dem *Spring Framework Quartz Scheduler* entwickelt.

Um diesen Scheduler effektiv zu nutzen, müssen zunächst die zu planenden Aufträge (Quartz-Aufträge) erstellt werden. Anschließend können diese Aufträge zur Datenbank hinzugefügt werden, da alle zu planenden Aufträge in der Datenbank gespeichert werden.

Wenn der Scheduler startet, liest er die zu planenden oder zu entplanenden Aufträge aus der Datenbank und plant bzw. entfernt sie entsprechend. Danach können Aufträge dynamisch über die Benutzeroberfläche ``Auftragsplanung`` hinzugefügt, aktualisiert oder entfernt werden.

.. NOTE::
   Der Scheduler startet, wenn die LibrePlan-Webanwendung startet, und stoppt, wenn die Anwendung stoppt.

.. NOTE::
   Dieser Scheduler unterstützt nur ``Cron-Ausdrücke`` zur Planung von Aufträgen.

Die Kriterien, anhand derer der Scheduler beim Start Aufträge plant oder entfernt, sind wie folgt:

Für alle Aufträge:

* Planen

  * Der Auftrag besitzt einen *Konnektor*, der *Konnektor* ist aktiviert, und der Auftrag darf geplant werden.
  * Der Auftrag besitzt keinen *Konnektor* und darf geplant werden.

* Entfernen

  * Der Auftrag besitzt einen *Konnektor*, und der *Konnektor* ist nicht aktiviert.
  * Der Auftrag besitzt einen *Konnektor*, der *Konnektor* ist aktiviert, aber der Auftrag darf nicht geplant werden.
  * Der Auftrag besitzt keinen *Konnektor* und darf nicht geplant werden.

.. NOTE::
   Aufträge können nicht neu geplant oder entplant werden, wenn sie gerade ausgeführt werden.

Listenansicht der Auftragsplanung
=================================

Die Ansicht ``Auftragsplanungsliste`` ermöglicht Benutzern:

*   Einen neuen Auftrag hinzufügen.
*   Einen vorhandenen Auftrag bearbeiten.
*   Einen Auftrag entfernen.
*   Einen Prozess manuell starten.

Auftrag hinzufügen oder bearbeiten
===================================

Klicken Sie in der Ansicht ``Auftragsplanungsliste``:

*   ``Erstellen``, um einen neuen Auftrag hinzuzufügen, oder
*   ``Bearbeiten``, um den ausgewählten Auftrag zu ändern.

Beide Aktionen öffnen ein ``Auftragsformular`` zum Erstellen/Bearbeiten. Das ``Formular`` zeigt die folgenden Eigenschaften an:

*   Felder:

    *   **Auftragsgruppe:** Der Name der Auftragsgruppe.
    *   **Auftragsname:** Der Name des Auftrags.
    *   **Cron-Ausdruck:** Ein schreibgeschütztes Feld mit einer Schaltfläche ``Bearbeiten``, um das Eingabefenster für den ``Cron-Ausdruck`` zu öffnen.
    *   **Auftragsklassenname:** Eine ``Dropdown-Liste`` zur Auswahl des Auftrags (ein vorhandener Auftrag).
    *   **Konnektor:** Eine ``Dropdown-Liste`` zur Auswahl eines Konnektors. Dies ist nicht zwingend erforderlich.
    *   **Zeitplanung:** Ein Kontrollkästchen, das angibt, ob dieser Auftrag geplant werden soll.

*   Schaltflächen:

    *   **Speichern:** Zum Speichern oder Aktualisieren eines Auftrags sowohl in der Datenbank als auch im Scheduler. Der Benutzer wird dann zur ``Auftragsplanungslistenansicht`` zurückgeleitet.
    *   **Speichern und fortfahren:** Wie "Speichern", aber der Benutzer wird nicht zur ``Auftragsplanungslistenansicht`` zurückgeleitet.
    *   **Abbrechen:** Es wird nichts gespeichert und der Benutzer wird zur ``Auftragsplanungslistenansicht`` zurückgeleitet.

*   Und ein Hinweisabschnitt zur Syntax von Cron-Ausdrücken.

Cron-Ausdruck-Popup
--------------------

Zur korrekten Eingabe des ``Cron-Ausdrucks`` wird ein ``Cron-Ausdruck``-Popup-Formular verwendet. In diesem Formular können Sie den gewünschten ``Cron-Ausdruck`` eingeben. Lesen Sie auch den Hinweis zum ``Cron-Ausdruck``. Wenn Sie einen ungültigen ``Cron-Ausdruck`` eingeben, werden Sie sofort benachrichtigt.

Auftrag entfernen
=================

Klicken Sie auf die Schaltfläche ``Entfernen``, um den Auftrag sowohl aus der Datenbank als auch aus dem Scheduler zu löschen. Der Erfolg oder Misserfolg dieser Aktion wird angezeigt.

Auftrag manuell starten
========================

Als Alternative zum Warten auf die planmäßige Ausführung des Auftrags können Sie auf diese Schaltfläche klicken, um den Prozess direkt zu starten. Danach werden die Erfolgs- oder Fehlerinformationen in einem ``Popup-Fenster`` angezeigt.
