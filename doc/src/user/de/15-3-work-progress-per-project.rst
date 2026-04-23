Bericht über Arbeit und Fortschritt je Projekt
###############################################

.. contents::

Zweck
=====

Dieser Bericht bietet einen Überblick über den Status von Projekten, wobei sowohl Fortschritt als auch Kosten berücksichtigt werden.

Er analysiert den aktuellen Fortschritt jedes Projekts und vergleicht ihn mit dem geplanten Fortschritt und der erledigten Arbeit.

Der Bericht zeigt außerdem mehrere Kennzahlen zu Projektkosten und vergleicht die aktuelle Leistung mit der geplanten Leistung.

Eingabeparameter und Filter
============================

Es gibt mehrere Pflichtparameter:

   *   **Referenzdatum:** Dies ist das Datum, das als Referenzpunkt für den Vergleich des geplanten Projektstatus mit der tatsächlichen Leistung verwendet wird. *Der Standardwert für dieses Feld ist das aktuelle Datum*.

   *   **Fortschrittstyp:** Dies ist der Fortschrittstyp, der zur Messung des Projektfortschritts verwendet wird. Die Anwendung ermöglicht es, ein Projekt gleichzeitig mit verschiedenen Fortschrittstypen zu messen. Der vom Benutzer im Dropdown-Menü ausgewählte Typ wird zur Berechnung der Berichtsdaten verwendet. Der Standardwert für den *Fortschrittstyp* ist *spread* (verteilt), ein spezieller Fortschrittstyp, der die für jedes WBS-Element konfigurierte bevorzugte Methode zur Messung des Fortschritts verwendet.

Die optionalen Parameter sind:

   *   **Startdatum:** Dies ist das früheste Startdatum für Projekte, die in den Bericht aufgenommen werden sollen. Wenn dieses Feld leer gelassen wird, gibt es kein Mindeststartdatum für die Projekte.

   *   **Enddatum:** Dies ist das späteste Enddatum für Projekte, die in den Bericht aufgenommen werden sollen. Alle Projekte, die nach dem *Enddatum* enden, werden ausgeschlossen.

   *   **Nach Projekten filtern:** Dieser Filter ermöglicht es Benutzern, die spezifischen Projekte auszuwählen, die in den Bericht aufgenommen werden sollen. Wenn dem Filter keine Projekte hinzugefügt werden, enthält der Bericht alle Projekte in der Datenbank. Ein durchsuchbares Dropdown-Menü ist vorhanden, um das gewünschte Projekt zu finden. Projekte werden dem Filter durch Klicken auf die Schaltfläche *Hinzufügen* hinzugefügt.

Ausgabe
=======

Das Ausgabeformat ist wie folgt:

Überschrift
-----------

Die Berichtskopfzeile zeigt die folgenden Felder:

   *   **Startdatum:** Das Filterstartdatum. Wird nicht angezeigt, wenn der Bericht nicht nach diesem Feld gefiltert wird.
   *   **Enddatum:** Das Filterenddatum. Wird nicht angezeigt, wenn der Bericht nicht nach diesem Feld gefiltert wird.
   *   **Fortschrittstyp:** Der für den Bericht verwendete Fortschrittstyp.
   *   **Projekte:** Gibt die gefilterten Projekte an, für die der Bericht erstellt wird. Es wird die Zeichenfolge *Alle* angezeigt, wenn der Bericht alle Projekte enthält, die die anderen Filter erfüllen.
   *   **Referenzdatum:** Das obligatorische Eingabe-Referenzdatum, das für den Bericht ausgewählt wurde.

Fußzeile
--------

Die Fußzeile zeigt das Datum an, an dem der Bericht erstellt wurde.

Hauptteil
---------

Der Hauptteil des Berichts besteht aus einer Liste von Projekten, die anhand der Eingabefilter ausgewählt wurden.

Filter funktionieren durch das Hinzufügen von Bedingungen, außer für die Kombination aus den Datumsfiltern (*Startdatum*, *Enddatum*) und dem *Filter nach Projekten*. In diesem Fall, wenn einer oder beide Datumsfilter ausgefüllt sind und der *Filter nach Projekten* eine Liste ausgewählter Projekte enthält, hat letzterer Filter Vorrang. Das bedeutet, dass die im Bericht enthaltenen Projekte diejenigen sind, die durch den *Filter nach Projekten* angegeben wurden, unabhängig von den Datumsfiltern.

Es ist wichtig zu beachten, dass der Fortschritt im Bericht als Bruch der Einheit berechnet wird und zwischen 0 und 1 liegt.

Für jedes für die Berichtsausgabe ausgewählte Projekt werden die folgenden Informationen angezeigt:

   * *Projektname*.
   * *Gesamtstunden*. Die Gesamtstunden für das Projekt werden durch Addition der Stunden für jede Aufgabe angezeigt. Es werden zwei Arten von Gesamtstunden angezeigt:
      *   *Geschätzt (TE)*. Dies ist die Summe aller geschätzten Stunden im WBS des Projekts. Sie gibt die Gesamtanzahl der zur Fertigstellung des Projekts geschätzten Stunden an.
      *   *Geplant (TP)*. In *LibrePlan* ist es möglich, zwei verschiedene Mengen zu haben: die geschätzte Anzahl von Stunden für eine Aufgabe (die anfänglich zur Fertigstellung der Aufgabe geschätzte Stundenanzahl) und die geplanten Stunden (die im Plan zur Fertigstellung der Aufgabe zugeteilten Stunden). Die geplanten Stunden können gleich, kleiner oder größer als die geschätzten Stunden sein und werden in einer späteren Phase, dem Zuweisungsvorgang, festgelegt. Daher sind die gesamten geplanten Stunden für ein Projekt die Summe aller zugeteilten Stunden für seine Aufgaben.
   * *Fortschritt*. Es werden drei Messungen zum Gesamtfortschritt des im Fortschritts-Eingabefilter angegebenen Typs für jedes Projekt zum Referenzdatum angezeigt:
      *   *Gemessen (PM)*. Dies ist der Gesamtfortschritt unter Berücksichtigung der Fortschrittsmessungen mit einem Datum, das früher als das *Referenzdatum* in den Eingabeparametern des Berichts liegt. Alle Aufgaben werden berücksichtigt, und die Summe wird nach der Anzahl der Stunden für jede Aufgabe gewichtet.
      *   *Zugerechnet (PI)*. Dies ist der Fortschritt unter der Annahme, dass die Arbeit im gleichen Tempo wie die abgeschlossenen Stunden einer Aufgabe fortgesetzt wird. Wenn X von Y Stunden einer Aufgabe abgeschlossen sind, wird der zugerechnete Gesamtfortschritt als X/Y betrachtet.
      *   *Geplant (PP)*. Dies ist der Gesamtfortschritt des Projekts gemäß dem geplanten Zeitplan zum Referenzdatum. Wenn alles genau wie geplant verlaufen wäre, sollte der gemessene Fortschritt dem geplanten Fortschritt entsprechen.
   * *Stunden bis Datum*. Es gibt zwei Felder, die die Anzahl der Stunden bis zum Referenzdatum aus zwei Perspektiven zeigen:
      *   *Geplant (HP)*. Diese Zahl ist die Summe der Stunden, die einer beliebigen Aufgabe im Projekt mit einem Datum kleiner oder gleich dem *Referenzdatum* zugeordnet sind.
      *   *Tatsächlich (HR)*. Diese Zahl ist die Summe der in den Arbeitsberichten für alle Aufgaben im Projekt gemeldeten Stunden mit einem Datum kleiner oder gleich dem *Referenzdatum*.
   * *Differenz*. Unter dieser Überschrift befinden sich mehrere Kostenkennzahlen:
      *   *Kosten*. Dies ist die Differenz in Stunden zwischen der Anzahl der aufgewendeten Stunden unter Berücksichtigung des gemessenen Fortschritts und den bis zum Referenzdatum abgeschlossenen Stunden. Die Formel lautet: *PM*TP - HR*.
      *   *Geplant*. Dies ist die Differenz zwischen den aufgewendeten Stunden gemäß dem gemessenen Gesamtprojektfortschritt und der bis zum *Referenzdatum* geplanten Anzahl. Es misst den Vor- oder Rückstand in der Zeit. Die Formel lautet: *PM*TP - HP*.
      *   *Kostenverhältnis*. Wird durch Division von *PM* / *PI* berechnet. Wenn es größer als 1 ist, bedeutet das, dass das Projekt an diesem Punkt rentabel ist. Wenn es kleiner als 1 ist, bedeutet das, dass das Projekt Verluste macht.
      *   *Planungsverhältnis*. Wird durch Division von *PM* / *PP* berechnet. Wenn es größer als 1 ist, bedeutet das, dass das Projekt im Zeitplan voraus ist. Wenn es kleiner als 1 ist, bedeutet das, dass das Projekt hinter dem Zeitplan zurückliegt.
