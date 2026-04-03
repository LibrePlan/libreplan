Kostenverwaltung
################

.. _costes:
.. contents::

Kosten
======

Die Kostenverwaltung ermöglicht Benutzern, die Kosten der in einem Projekt eingesetzten Ressourcen zu schätzen. Zur Verwaltung von Kosten müssen folgende Entitäten definiert werden:

*   **Stundenarten:** Diese geben die Arten von Stunden an, die eine Ressource arbeitet. Benutzer können Stundenarten sowohl für Maschinen als auch für Mitarbeiter definieren. Beispiele für Stundenarten sind: „Zusatzstunden zu 20 € pro Stunde." Folgende Felder können für Stundenarten definiert werden:

    *   **Code:** Externer Code für die Stundenart.
    *   **Name:** Name der Stundenart. Zum Beispiel „Zusatzstunden".
    *   **Standardsatz:** Grundlegender Standardsatz für die Stundenart.
    *   **Aktivierung:** Gibt an, ob die Stundenart aktiv ist oder nicht.

*   **Kostenkategorien:** Kostenkategorien definieren die Kosten, die mit verschiedenen Stundenarten während bestimmter Zeiträume (die unbegrenzt sein können) verbunden sind. Beispielsweise betragen die Kosten für Zusatzstunden für Facharbeiter der ersten Klasse im folgenden Jahr 24 € pro Stunde. Kostenkategorien umfassen:

    *   **Name:** Name der Kostenkategorie.
    *   **Aktivierung:** Gibt an, ob die Kategorie aktiv ist oder nicht.
    *   **Liste der Stundenarten:** Diese Liste definiert die in der Kostenkategorie enthaltenen Stundenarten. Sie gibt die Zeiträume und Sätze für jede Stundenart an. Zum Beispiel können bei sich ändernden Sätzen die einzelnen Jahre als Stundenart-Zeitraum in dieser Liste aufgeführt werden, mit einem bestimmten Stundensatz für jede Stundenart (der vom Standardstundensatz für diese Stundenart abweichen kann).

Stundenarten verwalten
-----------------------

Benutzer müssen folgende Schritte ausführen, um Stundenarten zu registrieren:

*   Wählen Sie im Menü „Verwaltung" die Option „Geleistete Stundenarten verwalten".
*   Das Programm zeigt eine Liste der vorhandenen Stundenarten an.

.. figure:: images/hour-type-list.png
   :scale: 35

   Liste der Stundenarten

*   Klicken Sie auf „Bearbeiten" oder „Erstellen".
*   Das Programm zeigt ein Bearbeitungsformular für Stundenarten an.

.. figure:: images/hour-type-edit.png
   :scale: 50

   Stundenarten bearbeiten

*   Benutzer können folgendes eingeben oder ändern:

    *   Den Namen der Stundenart.
    *   Den Code der Stundenart.
    *   Den Standardsatz.
    *   Die Aktivierung/Deaktivierung der Stundenart.

*   Klicken Sie auf „Speichern" oder „Speichern und weiter".

Kostenkategorien
----------------

Benutzer müssen folgende Schritte ausführen, um Kostenkategorien zu registrieren:

*   Wählen Sie im Menü „Verwaltung" die Option „Kostenkategorien verwalten".
*   Das Programm zeigt eine Liste der vorhandenen Kategorien an.

.. figure:: images/category-cost-list.png
   :scale: 50

   Liste der Kostenkategorien

*   Klicken Sie auf die Schaltfläche „Bearbeiten" oder „Erstellen".
*   Das Programm zeigt ein Bearbeitungsformular für Kostenkategorien an.

.. figure:: images/category-cost-edit.png
   :scale: 50

   Kostenkategorien bearbeiten

*   Benutzer geben folgendes ein oder ändern es:

    *   Den Namen der Kostenkategorie.
    *   Die Aktivierung/Deaktivierung der Kostenkategorie.
    *   Die Liste der in der Kategorie enthaltenen Stundenarten. Alle Stundenarten haben folgende Felder:

        *   **Stundenart:** Wählen Sie eine der im System vorhandenen Stundenarten aus. Falls keine vorhanden sind, muss eine Stundenart erstellt werden (dieser Vorgang wird im vorherigen Unterabschnitt erläutert).
        *   **Start- und Enddatum:** Das Start- und Enddatum (letzteres ist optional) für den Zeitraum, der für die Kostenkategorie gilt.
        *   **Stundensatz:** Der Stundensatz für diese spezifische Kategorie.

*   Klicken Sie auf „Speichern" oder „Speichern und weiter".

Die Zuweisung von Kostenkategorien zu Ressourcen wird im Kapitel über Ressourcen beschrieben. Gehen Sie zum Abschnitt „Ressourcen".
