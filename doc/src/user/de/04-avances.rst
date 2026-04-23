Fortschritt
###########

.. contents::

Der Projektfortschritt zeigt an, inwieweit die geschätzte Fertigstellungszeit des Projekts eingehalten wird. Der Aufgabenfortschritt zeigt an, inwieweit die Aufgabe gemäß ihrer geschätzten Fertigstellung abgeschlossen wird.

Im Allgemeinen kann der Fortschritt nicht automatisch gemessen werden. Ein erfahrener Mitarbeiter oder eine Checkliste muss den Fertigstellungsgrad einer Aufgabe oder eines Projekts bestimmen.

Es ist wichtig, den Unterschied zwischen den einer Aufgabe oder einem Projekt zugewiesenen Stunden und dem Fortschritt dieser Aufgabe oder dieses Projekts zu beachten. Obwohl die Anzahl der verwendeten Stunden mehr oder weniger als erwartet sein kann, kann das Projekt am überwachten Tag vor oder hinter seinem geschätzten Abschluss liegen. Aus diesen zwei Messungen können sich verschiedene Situationen ergeben:

*   **Weniger Stunden verbraucht als erwartet, aber das Projekt liegt hinter dem Zeitplan:** Der Fortschritt ist am überwachten Tag geringer als geschätzt.
*   **Weniger Stunden verbraucht als erwartet, und das Projekt liegt vor dem Zeitplan:** Der Fortschritt ist am überwachten Tag höher als geschätzt.
*   **Mehr Stunden verbraucht als erwartet, und das Projekt liegt hinter dem Zeitplan:** Der Fortschritt ist am überwachten Tag geringer als geschätzt.
*   **Mehr Stunden verbraucht als erwartet, aber das Projekt liegt vor dem Zeitplan:** Der Fortschritt ist am überwachten Tag höher als geschätzt.

Die Planungsansicht ermöglicht es Ihnen, diese Situationen zu vergleichen, indem Sie Informationen über den erzielten Fortschritt und die verwendeten Stunden verwenden. In diesem Kapitel wird erläutert, wie Informationen zur Überwachung des Fortschritts eingegeben werden.

Die Philosophie hinter der Fortschrittsüberwachung basiert darauf, dass Benutzer die Ebene definieren, auf der sie ihre Projekte überwachen möchten. Wenn Benutzer beispielsweise Projekte überwachen möchten, müssen sie nur Informationen für Elemente der ersten Ebene eingeben. Wenn sie eine genauere Überwachung auf Aufgabenebene wünschen, müssen sie Fortschrittsinformationen auf niedrigeren Ebenen eingeben. Das System aggregiert die Daten dann aufwärts durch die Hierarchie.

Fortschrittstypen verwalten
============================

Unternehmen haben unterschiedliche Bedürfnisse bei der Überwachung des Projektfortschritts, insbesondere der beteiligten Aufgaben. Daher enthält das System „Fortschrittstypen". Benutzer können verschiedene Fortschrittstypen definieren, um den Fortschritt einer Aufgabe zu messen. Eine Aufgabe kann beispielsweise als Prozentsatz gemessen werden, aber dieser Prozentsatz kann auch in Fortschritt in *Tonnen* basierend auf der Vereinbarung mit dem Kunden umgewandelt werden.

Ein Fortschrittstyp hat einen Namen, einen Maximalwert und einen Präzisionswert:

*   **Name:** Ein beschreibender Name, den Benutzer beim Auswählen des Fortschrittstyps erkennen. Dieser Name sollte klar angeben, welche Art von Fortschritt gemessen wird.
*   **Maximalwert:** Der Maximalwert, der für eine Aufgabe oder ein Projekt als Gesamtfortschrittsmessung festgelegt werden kann.
*   **Präzisionswert:** Der für den Fortschrittstyp zulässige Inkrementwert. Wenn der Fortschritt in *Tonnen* beispielsweise in ganzen Zahlen gemessen werden soll, beträgt der Präzisionswert 1.

Das System hat zwei Standard-Fortschrittstypen:

*   **Prozentsatz:** Ein allgemeiner Fortschrittstyp, der den Fortschritt eines Projekts oder einer Aufgabe basierend auf einem geschätzten Fertigstellungsprozentsatz misst.
*   **Einheiten:** Ein allgemeiner Fortschrittstyp, der den Fortschritt in Einheiten ohne Angabe des Einheitentyps misst.

.. figure:: images/tipos-avances.png
   :scale: 50

   Verwaltung von Fortschrittstypen

Benutzer können neue Fortschrittstypen wie folgt erstellen:

*   Gehen Sie zum Bereich „Verwaltung".
*   Klicken Sie auf die Option „Fortschrittstypen verwalten" im Menü der zweiten Ebene.
*   Das System zeigt eine Liste vorhandener Fortschrittstypen an.
*   Für jeden Fortschrittstyp können Benutzer:

    *   Bearbeiten
    *   Löschen

*   Benutzer können dann einen neuen Fortschrittstyp erstellen.
*   Beim Bearbeiten oder Erstellen eines Fortschrittstyps zeigt das System ein Formular mit folgenden Informationen an:

    *   Name des Fortschrittstyps.
    *   Maximal zulässiger Wert für den Fortschrittstyp.
    *   Präzisionswert für den Fortschrittstyp.

Fortschritt nach Typ eingeben
==============================

Fortschritt wird für Projektelemente eingegeben, kann aber auch über eine Verknüpfung von den Planungsaufgaben eingegeben werden. Benutzer sind dafür verantwortlich, welchen Fortschrittstyp sie jedem Projektelement zuordnen.

Benutzer können einen einzigen Standard-Fortschrittstyp für das gesamte Projekt eingeben.

Bevor der Fortschritt gemessen wird, müssen Benutzer den gewählten Fortschrittstyp mit dem Projekt verknüpfen.

.. figure:: images/avance.png
   :scale: 40

   Fortschrittseingabe-Bildschirm mit grafischer Visualisierung

So geben Sie Fortschrittsmessungen ein:

*   Wählen Sie den Fortschrittstyp aus, zu dem der Fortschritt hinzugefügt werden soll.
    *   Wenn kein Fortschrittstyp vorhanden ist, muss ein neuer erstellt werden.
*   Geben Sie in dem Formular, das unter den Feldern „Wert" und „Datum" erscheint, den absoluten Wert der Messung und das Datum der Messung ein.
*   Das System speichert die eingegebenen Daten automatisch.

Fortschritt für ein Projektelement vergleichen
===============================================

Benutzer können den erzielten Fortschritt an Projekten grafisch mit den vorgenommenen Messungen vergleichen. Alle Fortschrittstypen haben eine Spalte mit einer Prüf-Schaltfläche („Anzeigen"). Wenn diese Schaltfläche ausgewählt wird, wird das Fortschrittsdiagramm der vorgenommenen Messungen für das Projektelement angezeigt.

.. figure:: images/contraste-avance.png
   :scale: 40

   Vergleich mehrerer Fortschrittstypen
