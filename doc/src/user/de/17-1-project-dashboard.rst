Projekt-Dashboard
#################

.. contents::

Das Projekt-Dashboard ist eine *LibrePlan*-Perspektive, die eine Reihe von **KPIs (Key Performance Indicators)** enthält, um die Leistung eines Projekts in folgenden Bereichen zu bewerten:

   *   Arbeitsfortschritt
   *   Kosten
   *   Status zugewiesener Ressourcen
   *   Zeitliche Einschränkungen

Fortschritts-Leistungsindikatoren
==================================

Zwei Indikatoren werden berechnet: Projektfortschrittsprozentsatz und Aufgabenstatus.

Projektfortschrittsprozentsatz
-------------------------------

Dieses Diagramm zeigt den Gesamtfortschritt eines Projekts und vergleicht ihn mit dem erwarteten Fortschritt basierend auf dem *Gantt*-Diagramm.

Der Fortschritt wird durch zwei Balken dargestellt:

   *   *Aktueller Fortschritt:* Der aktuelle Fortschritt basierend auf den durchgeführten Messungen.
   *   *Erwarteter Fortschritt:* Der Fortschritt, den das Projekt zu diesem Zeitpunkt gemäß dem Projektplan hätte erreichen sollen.

Um den tatsächlich gemessenen Wert für jeden Balken anzuzeigen, fahren Sie mit dem Mauszeiger über den Balken.

Der Gesamtprojektfortschritt wird mit verschiedenen Methoden geschätzt, da es keinen einzigen, universell korrekten Ansatz gibt:

   *   **Verteilter Fortschritt:** Dies ist der Fortschrittstyp, der als verteilter Fortschritt auf Projektebene festgelegt ist. In diesem Fall gibt es keine Möglichkeit, einen erwarteten Wert zu berechnen, und nur der aktuelle Balken wird angezeigt.
   *   **Nach allen Aufgabenstunden:** Der Fortschritt aller Projektaufgaben wird gemittelt, um den Gesamtwert zu berechnen. Dies ist ein gewichteter Durchschnitt, der die Anzahl der jeder Aufgabe zugewiesenen Stunden berücksichtigt.
   *   **Nach kritischen Pfadstunden:** Der Fortschritt der Aufgaben, die zu einem der kritischen Pfade des Projekts gehören, wird gemittelt, um den Gesamtwert zu erhalten. Dies ist ein gewichteter Durchschnitt, der die gesamten zugewiesenen Stunden für jede betroffene Aufgabe berücksichtigt.
   *   **Nach kritischer Pfaddauer:** Der Fortschritt der Aufgaben, die zu einem der kritischen Pfade gehören, wird mittels eines gewichteten Durchschnitts gemittelt, wobei diesmal die Dauer jeder betroffenen Aufgabe anstelle der zugewiesenen Stunden berücksichtigt wird.

Aufgabenstatus
--------------

Ein Kreisdiagramm zeigt den Prozentsatz der Projektaufgaben in verschiedenen Zuständen. Die definierten Zustände sind:

   *   **Abgeschlossen:** Erledigte Aufgaben, erkennbar an einem Fortschrittswert von 100%.
   *   **In Bearbeitung:** Aufgaben, die derzeit laufen. Diese Aufgaben haben einen Fortschrittswert ungleich 0% oder 100%, oder es wurde Arbeitszeit erfasst.
   *   **Bereit zum Start:** Aufgaben mit 0% Fortschritt, keiner erfassten Zeit, allen *FINISH_TO_START*-abhängigen Aufgaben im Status *abgeschlossen* und allen *START_TO_START*-abhängigen Aufgaben im Status *abgeschlossen* oder *in Bearbeitung*.
   *   **Blockiert:** Aufgaben mit 0% Fortschritt, keiner erfassten Zeit und vorherigen abhängigen Aufgaben, die weder *in Bearbeitung* noch im Zustand *bereit zum Start* sind.

Kostenindiktatoren
==================

Mehrere *Earned Value Management*-Kostenkennzahlen werden berechnet:

   *   **CV (Kostenabweichung):** Die Differenz zwischen der *Earned Value-Kurve* und der *Ist-Kostenkurve* zum aktuellen Zeitpunkt. Positive Werte zeigen einen Gewinn an, negative Werte einen Verlust.
   *   **ACWP (Ist-Kosten durchgeführter Arbeit):** Die Gesamtanzahl der im Projekt zum aktuellen Zeitpunkt erfassten Stunden.
   *   **CPI (Kostenleistungsindex):** Das Verhältnis *Earned Value / Ist-Kosten*.

        *   > 100 ist günstig und zeigt an, dass das Projekt unter dem Budget liegt.
        *   = 100 ist ebenfalls günstig und zeigt an, dass die Kosten genau dem Plan entsprechen.
        *   < 100 ist ungünstig und zeigt an, dass die Kosten zur Fertigstellung der Arbeit höher als geplant sind.
   *   **ETC (Schätzung bis zur Fertigstellung):** Die verbleibende Zeit bis zum Abschluss des Projekts.
   *   **BAC (Budget bei Abschluss):** Der Gesamtumfang der im Projektplan zugewiesenen Arbeit.
   *   **EAC (Schätzung bei Abschluss):** Die Prognose des Managers für die Gesamtkosten bei Projektabschluss, basierend auf dem *CPI*.
   *   **VAC (Abweichung bei Abschluss):** Die Differenz zwischen *BAC* und *EAC*.

        *   < 0 zeigt an, dass das Projekt das Budget überschreitet.
        *   > 0 zeigt an, dass das Projekt unter dem Budget liegt.

Ressourcen
==========

Um das Projekt aus der Perspektive der Ressourcen zu analysieren, werden zwei Kennzahlen und ein Histogramm bereitgestellt.

Histogramm der Schätzungsabweichung bei abgeschlossenen Aufgaben
-----------------------------------------------------------------

Dieses Histogramm berechnet die Abweichung zwischen der Anzahl der Stunden, die den Projektaufgaben zugewiesen wurden, und der tatsächlichen Anzahl der ihnen gewidmeten Stunden.

Die Abweichung wird als Prozentsatz für alle abgeschlossenen Aufgaben berechnet, und die berechneten Abweichungen werden in einem Histogramm dargestellt. Die vertikale Achse zeigt die Anzahl der Aufgaben innerhalb jedes Abweichungsintervalls. Sechs Abweichungsintervalle werden dynamisch berechnet.

Überstundenquote
----------------

Diese Kennzahl fasst die Überlastung der dem Projekt zugewiesenen Ressourcen zusammen. Sie wird nach der Formel berechnet: **Überstundenquote = Überlastung / (Auslastung + Überlastung)**.

   *   = 0 ist günstig und zeigt an, dass die Ressourcen nicht überlastet sind.
   *   > 0 ist ungünstig und zeigt an, dass die Ressourcen überlastet sind.

Verfügbarkeitsquote
-------------------

Diese Kennzahl fasst die freie Kapazität der dem Projekt aktuell zugewiesenen Ressourcen zusammen. Sie misst daher die Verfügbarkeit der Ressourcen für weitere Zuweisungen ohne Überlastung. Sie wird berechnet als: **Verfügbarkeitsquote = (1 - Auslastung/Kapazität) * 100**

   *   Mögliche Werte liegen zwischen 0% (vollständig zugewiesen) und 100% (nicht zugewiesen).

Zeit
====

Zwei Diagramme sind enthalten: ein Histogramm für die Zeitabweichung beim Abschluss von Projektaufgaben und ein Kreisdiagramm für Fristüberschreitungen.

Vorsprung oder Rückstand beim Aufgabenabschluss
------------------------------------------------

Diese Berechnung bestimmt die Differenz in Tagen zwischen dem geplanten Endzeitpunkt für Projektaufgaben und ihrem tatsächlichen Endzeitpunkt. Das geplante Abschlussdatum wird aus dem *Gantt*-Diagramm entnommen, und das tatsächliche Abschlussdatum wird aus der zuletzt erfassten Zeit für die Aufgabe entnommen.

Der Rückstand oder Vorsprung beim Aufgabenabschluss wird in einem Histogramm dargestellt. Die vertikale Achse zeigt die Anzahl der Aufgaben mit einem Vorsprung-/Rückstandsdifferenzwert, der dem Tagesintervall der Abszisse entspricht. Sechs dynamische Abweichungsintervalle für den Aufgabenabschluss werden berechnet.

   *   Negative Werte bedeuten einen Abschluss vor dem geplanten Zeitpunkt.
   *   Positive Werte bedeuten einen Abschluss nach dem geplanten Zeitpunkt.

Fristüberschreitungen
---------------------

Dieser Abschnitt berechnet den Spielraum zur Projektfrist, sofern eine gesetzt ist. Außerdem zeigt ein Kreisdiagramm den Prozentsatz der Aufgaben, die ihre Frist einhalten. Drei Wertetypen sind im Diagramm enthalten:

   *   Prozentsatz der Aufgaben ohne konfigurierte Frist.
   *   Prozentsatz der abgeschlossenen Aufgaben mit einem tatsächlichen Enddatum später als ihre Frist. Das tatsächliche Enddatum wird aus der zuletzt erfassten Zeit für die Aufgabe entnommen.
   *   Prozentsatz der abgeschlossenen Aufgaben mit einem tatsächlichen Enddatum früher als ihre Frist.
