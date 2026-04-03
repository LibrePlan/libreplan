Bericht über geleistete Stunden nach Ressource
##############################################

.. contents::

Zweck
=====

Dieser Bericht extrahiert eine Liste von Aufgaben und die Zeit, die Ressourcen ihnen innerhalb eines bestimmten Zeitraums gewidmet haben. Mehrere Filter ermöglichen es Benutzern, die Abfrage zu verfeinern, um nur die gewünschten Informationen zu erhalten und irrelevante Daten auszuschließen.

Eingabeparameter und Filter
============================

* **Daten**.
    * *Typ*: Optional.
    * *Zwei Datumsfelder*:
        * *Startdatum:* Dies ist das früheste Datum für einzuschließende Arbeitsberichte. Arbeitsberichte mit Datum vor dem *Startdatum* werden ausgeschlossen. Wenn dieser Parameter leer gelassen wird, werden Arbeitsberichte nicht nach *Startdatum* gefiltert.
        * *Enddatum:* Dies ist das späteste Datum für einzuschließende Arbeitsberichte. Arbeitsberichte mit Datum nach dem *Enddatum* werden ausgeschlossen. Wenn dieser Parameter leer gelassen wird, werden Arbeitsberichte nicht nach *Enddatum* gefiltert.

*   **Filter nach Mitarbeitern:**
    *   *Typ:* Optional.
    *   *Funktionsweise:* Sie können einen oder mehrere Mitarbeiter auswählen, um die Arbeitsberichte auf die von diesen bestimmten Mitarbeitern erfasste Zeit zu beschränken. Um einen Mitarbeiter als Filter hinzuzufügen, suchen Sie ihn in der Auswahl und klicken Sie auf die Schaltfläche *Hinzufügen*. Wenn dieser Filter leer gelassen wird, werden Arbeitsberichte unabhängig vom Mitarbeiter abgerufen.

*   **Filter nach Etiketten:**
    *   *Typ:* Optional.
    *   *Funktionsweise:* Sie können ein oder mehrere Etiketten als Filter hinzufügen, indem Sie in der Auswahl danach suchen und auf die Schaltfläche *Hinzufügen* klicken. Diese Etiketten werden verwendet, um die Aufgaben auszuwählen, die bei der Berechnung der ihnen gewidmeten Stunden in die Ergebnisse einbezogen werden sollen. Dieser Filter kann auf Stundenzettel, Aufgaben, beides oder keines angewendet werden.

*   **Filter nach Kriterien:**
    *   *Typ:* Optional.
    *   *Funktionsweise:* Sie können ein oder mehrere Kriterien auswählen, indem Sie in der Auswahl danach suchen und dann auf die Schaltfläche *Hinzufügen* klicken. Diese Kriterien werden verwendet, um die Ressourcen auszuwählen, die mindestens eines davon erfüllen. Der Bericht zeigt die gesamte Zeit, die von Ressourcen aufgewendet wurde, die eines der ausgewählten Kriterien erfüllen.

Ausgabe
=======

Kopfzeile
---------

Die Berichtskopfzeile zeigt die Filter an, die für den aktuellen Bericht konfiguriert und angewendet wurden.

Fußzeile
--------

Das Datum, an dem der Bericht erstellt wurde, ist in der Fußzeile aufgeführt.

Hauptteil
---------

Der Berichtshauptteil besteht aus mehreren Informationsgruppen.

*   Die erste Aggregationsebene erfolgt nach Ressource. Die gesamte von einer Ressource aufgewendete Zeit wird unterhalb der Kopfzeile zusammen angezeigt. Jede Ressource wird identifiziert durch:

    *   *Mitarbeiter:* Nachname, Vorname.
    *   *Maschine:* Name.

    Eine Zusammenfassungszeile zeigt die Gesamtzahl der von der Ressource geleisteten Stunden.

*   Die zweite Gruppierungsebene erfolgt nach *Datum*. Alle Berichte einer bestimmten Ressource am gleichen Datum werden zusammen angezeigt.

    Eine Zusammenfassungszeile zeigt die Gesamtzahl der von der Ressource an diesem Datum geleisteten Stunden.

*   Die letzte Ebene listet die Arbeitsberichte des Mitarbeiters an diesem Tag auf. Die für jede Arbeitsberichtszeile angezeigten Informationen sind:

    *   *Aufgabencode:* Der Code der Aufgabe, der die erfassten Stunden zugeordnet werden.
    *   *Aufgabenname:* Der Name der Aufgabe, der die erfassten Stunden zugeordnet werden.
    *   *Startzeit:* Dies ist optional. Es ist die Zeit, zu der die Ressource mit der Arbeit an der Aufgabe begann.
    *   *Endzeit:* Dies ist optional. Es ist die Zeit, zu der die Ressource die Arbeit an der Aufgabe am angegebenen Datum beendete.
    *   *Textfelder:* Dies ist optional. Wenn die Arbeitsberichtszeile Textfelder hat, werden die ausgefüllten Werte hier angezeigt. Das Format ist: <Name des Textfelds>:<Wert>
    *   *Etiketten:* Dies hängt davon ab, ob das Arbeitsberichtsmodell ein Etikettenfeld in seiner Definition hat. Wenn es mehrere Etiketten gibt, werden sie in derselben Spalte angezeigt. Das Format ist: <Name des Etikettentyps>:<Wert des Etiketts>
