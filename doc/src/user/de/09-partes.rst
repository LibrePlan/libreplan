Arbeitsberichte
###############

.. contents::

Arbeitsberichte ermöglichen die Überwachung der Stunden, die Ressourcen den ihnen zugewiesenen Aufgaben widmen.

Das Programm ermöglicht es Benutzern, neue Formulare für die Eingabe gewidmeter Stunden zu konfigurieren und dabei die Felder anzugeben, die in diesen Formularen erscheinen sollen. Dies ermöglicht die Einbeziehung von Berichten über von Mitarbeitern durchgeführte Aufgaben und die Überwachung der Mitarbeiteraktivitäten.

Bevor Benutzer Einträge für Ressourcen hinzufügen können, müssen sie mindestens einen Arbeitsberichtstyp definieren.

Arbeitsberichtstypen
====================

Ein Arbeitsbericht besteht aus einer Reihe von Feldern, die für den gesamten Bericht gemeinsam sind, und einem Satz von Arbeitsberichtszeilen mit spezifischen Werten für die in jeder Zeile definierten Felder.

Benutzer können verschiedene Arbeitsberichtstypen konfigurieren, damit ein Unternehmen seine Berichte nach seinen spezifischen Bedürfnissen gestalten kann:

.. figure:: images/work-report-types.png
   :scale: 40

   Arbeitsberichtstypen

Die Pflichtfelder, die in allen Arbeitsberichten erscheinen müssen, sind folgende:

*   **Name und Code:** Identifikationsfelder für den Namen des Arbeitsberichtstyps und seinen Code.
*   **Datum:** Feld für das Datum des Berichts.
*   **Ressource:** Mitarbeiter oder Maschine, der/die im Bericht oder in der Arbeitsberichtszeile erscheint.
*   **Projektelement:** Code für das Projektelement, dem die durchgeführte Arbeit zugeordnet wird.
*   **Stundenverwaltung:** Bestimmt die zu verwendende Stundenerfassungsrichtlinie:

    *   **Gemäß zugewiesenen Stunden:** Stunden werden basierend auf den zugewiesenen Stunden erfasst.
    *   **Gemäß Start- und Endzeiten:** Stunden werden basierend auf Start- und Endzeiten berechnet.
    *   **Gemäß Stundenanzahl und Start-/Endbereich:** Abweichungen sind zulässig, und die Stundenanzahl hat Vorrang.

Benutzer können neue Felder zu den Berichten hinzufügen:

*   **Markierungstyp:** Benutzer können das System auffordern, eine Markierung beim Ausfüllen des Arbeitsberichts anzuzeigen.
*   **Freie Felder:** Felder, in denen Text frei in den Arbeitsbericht eingegeben werden kann.

.. figure:: images/work-report-type.png
   :scale: 50

   Erstellen eines Arbeitsberichtstyps mit personalisierten Feldern

Arbeitsberichtsliste
====================

Sobald das Format der in das System aufzunehmenden Berichte konfiguriert wurde, können Benutzer die Details in das erstellte Formular gemäß der in dem entsprechenden Arbeitsberichtstyp definierten Struktur eingeben:

*   Klicken Sie auf die Schaltfläche „Neuer Arbeitsbericht" neben dem gewünschten Bericht aus der Liste der Arbeitsberichtstypen.
*   Das Programm zeigt dann den Bericht basierend auf den für den Typ angegebenen Konfigurationen an.

.. figure:: images/work-report-type.png
   :scale: 50

   Struktur des Arbeitsberichts basierend auf Typ

*   Wählen Sie alle für den Bericht angezeigten Felder aus:

    *   **Ressource:** Wenn die Kopfzeile gewählt wurde, wird die Ressource nur einmal angezeigt.
    *   **Aufgabencode:** Code der Aufgabe, der der Arbeitsbericht zugewiesen wird.
    *   **Datum:** Datum des Berichts oder jeder Zeile.
    *   **Stundenanzahl:** Die Anzahl der Arbeitsstunden im Projekt.
    *   **Start- und Endzeiten:** Start- und Endzeiten für die Arbeit zur Berechnung der endgültigen Arbeitsstunden.
    *   **Stundentyp:** Ermöglicht Benutzern, den Stundentyp auszuwählen, z. B. „Normal", „Außerordentlich" usw.

*   Klicken Sie auf „Speichern" oder „Speichern und fortfahren".
