Aufgabenplanung
###############

.. _planificacion:
.. contents::

Aufgabenplanung
===============

Die Planung in LibrePlan ist ein Prozess, der im gesamten Benutzerhandbuch beschrieben wurde, wobei die Kapitel zu Projekten und Ressourcenzuweisung besonders wichtig sind. Dieses Kapitel beschreibt die grundlegenden Planungsverfahren, nachdem das Projekt und die Gantt-Diagramme ordnungsgemäß konfiguriert wurden.

.. figure:: images/planning-view.png
   :scale: 35

   Arbeitsplanungsansicht

Wie bei der Unternehmensübersicht ist die Projektplanungsansicht in mehrere Ansichten unterteilt, basierend auf den zu analysierenden Informationen. Die für ein bestimmtes Projekt verfügbaren Ansichten sind:

*   Planungsansicht
*   Ressourcenauslastungsansicht
*   Projektlistenansicht
*   Erweiterte Zuweisungsansicht

Planungsansicht
---------------

Die Planungsansicht kombiniert drei verschiedene Perspektiven:

*   **Projektplanung:** Die Projektplanung wird im oberen rechten Teil des Programms als Gantt-Diagramm angezeigt. Diese Ansicht ermöglicht es Benutzern, Aufgaben vorübergehend zu verschieben, Abhängigkeiten zwischen ihnen zuzuweisen, Meilensteine zu definieren und Einschränkungen festzulegen.
*   **Ressourcenauslastung:** Die Ressourcenauslastungsansicht im unteren rechten Teil des Bildschirms zeigt die Ressourcenverfügbarkeit basierend auf Zuweisungen. Die in dieser Ansicht angezeigten Informationen sind:

    *   **Lila Bereich:** Zeigt eine Ressourcenauslastung unter 100 % ihrer Kapazität an.
    *   **Grüner Bereich:** Zeigt eine Ressourcenauslastung unter 100 % an, weil die Ressource für ein anderes Projekt geplant ist.
    *   **Orangefarbener Bereich:** Zeigt eine Ressourcenauslastung über 100 % als Folge des aktuellen Projekts an.
    *   **Gelber Bereich:** Zeigt eine Ressourcenauslastung über 100 % als Folge anderer Projekte an.

*   **Diagrammansicht und Earned-Value-Indikatoren:** Diese können über die Registerkarte „Earned Value" angezeigt werden. Die berechneten Indikatoren sind:

    *   **BCWS (Budgeted Cost of Work Scheduled):** Die kumulative Zeitfunktion für die Anzahl der bis zu einem bestimmten Datum geplanten Stunden.
    *   **ACWP (Actual Cost of Work Performed):** Die kumulative Zeitfunktion für die in Arbeitsberichten bis zu einem bestimmten Datum gemeldeten Stunden.
    *   **BCWP (Budgeted Cost of Work Performed):** Die kumulative Zeitfunktion, die den Ergebniswert der Multiplikation des Aufgabenfortschritts mit der Menge der Arbeit enthält, die für die Fertigstellung der Aufgabe geschätzt wurde.
    *   **CV (Cost Variance):** CV = BCWP - ACWP
    *   **SV (Schedule Variance):** SV = BCWP - BCWS
    *   **BAC (Budget at Completion):** BAC = max (BCWS)
    *   **EAC (Estimate at Completion):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Variance at Completion):** VAC = BAC - EAC
    *   **ETC (Estimate to Complete):** ETC = EAC - ACWP
    *   **CPI (Cost Performance Index):** CPI = BCWP / ACWP
    *   **SPI (Schedule Performance Index):** SPI = BCWP / BCWS

In der Projektplanungsansicht können Benutzer die folgenden Aktionen ausführen:

*   **Abhängigkeiten zuweisen:** Klicken Sie mit der rechten Maustaste auf eine Aufgabe, wählen Sie „Abhängigkeit hinzufügen" und ziehen Sie den Mauszeiger zur Aufgabe, der die Abhängigkeit zugewiesen werden soll.

    *   Um den Typ der Abhängigkeit zu ändern, klicken Sie mit der rechten Maustaste auf die Abhängigkeit und wählen Sie den gewünschten Typ.

*   **Einen neuen Meilenstein erstellen:** Klicken Sie auf die Aufgabe, vor der der Meilenstein hinzugefügt werden soll, und wählen Sie die Option „Meilenstein hinzufügen".
*   **Aufgaben ohne Beeinträchtigung von Abhängigkeiten verschieben:** Klicken Sie mit der rechten Maustaste auf den Aufgabenkörper und ziehen Sie ihn an die gewünschte Position.
*   **Einschränkungen zuweisen:** Klicken Sie auf die betreffende Aufgabe und wählen Sie die Option „Aufgabeneigenschaften". Die Einschränkungen, die festgelegt werden können, sind:

    *   **So früh wie möglich:** Zeigt an, dass die Aufgabe so früh wie möglich beginnen muss.
    *   **Nicht früher als:** Zeigt an, dass die Aufgabe nicht vor einem bestimmten Datum beginnen darf.
    *   **Beginnen an einem bestimmten Datum:** Zeigt an, dass die Aufgabe an einem bestimmten Datum beginnen muss.

Die Planungsansicht bietet auch verschiedene Verfahren, die als Anzeigeoptionen funktionieren:

*   **Zoomstufe:** Benutzer können die gewünschte Zoomstufe wählen. Es gibt mehrere Zoomstufen: jährlich, viermonatlich, monatlich, wöchentlich und täglich.
*   **Suchfilter:** Benutzer können Aufgaben nach Etiketten oder Kriterien filtern.
*   **Kritischer Pfad:** Als Ergebnis der Verwendung des *Dijkstra*-Algorithmus zur Berechnung von Pfaden in Graphen wurde der kritische Pfad implementiert.
*   **Etiketten anzeigen:** Ermöglicht Benutzern, die den Aufgaben in einem Projekt zugewiesenen Etiketten anzuzeigen.
*   **Ressourcen anzeigen:** Ermöglicht Benutzern, die den Aufgaben in einem Projekt zugewiesenen Ressourcen anzuzeigen.
*   **Drucken:** Ermöglicht Benutzern, das angezeigte Gantt-Diagramm zu drucken.

Ressourcenauslastungsansicht
-----------------------------

Die Ressourcenauslastungsansicht bietet eine Liste von Ressourcen, die eine Liste von Aufgaben oder Kriterien enthält, die Arbeitslasten generieren. Eine andere Farbe wird angezeigt, je nachdem, ob die Ressource eine Last hat, die höher oder niedriger als 100 % ist:

*   **Grün:** Last unter 100 %
*   **Orange:** 100 % Last
*   **Rot:** Last über 100 %

.. figure:: images/resource-load.png
   :scale: 35

   Ressourcenauslastungsansicht für ein bestimmtes Projekt

Projektlistenansicht
--------------------

Die Projektlistenansicht ermöglicht Benutzern, auf die Optionen zum Bearbeiten und Löschen von Projekten zuzugreifen. Weitere Informationen finden Sie im Kapitel „Projekte".

Erweiterte Zuweisungsansicht
-----------------------------

Die Erweiterte Zuweisungsansicht wird im Kapitel „Ressourcenzuweisung" ausführlich erläutert.
