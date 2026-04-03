Projekte und Projektelemente
############################

.. contents::

Projekte repräsentieren die von Benutzern des Programms auszuführende Arbeit. Jeder Projekt entspricht einem Projekt, das das Unternehmen seinen Kunden anbieten wird.

Ein Projekt besteht aus einem oder mehreren Projektelementen. Jedes Projektelement stellt einen spezifischen Teil der auszuführenden Arbeit dar und legt fest, wie die Arbeit am Projekt geplant und ausgeführt werden soll. Projektelemente sind hierarchisch organisiert, ohne Einschränkungen hinsichtlich der Tiefe der Hierarchie. Diese hierarchische Struktur ermöglicht die Vererbung bestimmter Merkmale wie Etiketten.

Projekte
========

Ein Projekt stellt ein Projekt oder eine von einem Kunden beim Unternehmen angeforderte Arbeit dar. Der Projekt identifiziert das Projekt innerhalb der Unternehmensplanung. Im Gegensatz zu umfassenden Verwaltungsprogrammen erfordert LibrePlan nur bestimmte Schlüsseldetails für einen Projekt. Diese Details sind:

*   **Projektsname:** Der Name des Projekts.
*   **Projektscode:** Ein eindeutiger Code für den Projekt.
*   **Gesamtauftragsbetrag:** Der gesamte finanzielle Wert des Projekts.
*   **Geschätztes Startdatum:** Das geplante Startdatum für den Projekt.
*   **Enddatum:** Das geplante Fertigstellungsdatum für den Projekt.
*   **Verantwortliche Person:** Die Person, die für den Projekt verantwortlich ist.
*   **Beschreibung:** Eine Beschreibung des Projekts.
*   **Zugewiesener Kalender:** Der dem Projekt zugeordnete Kalender.
*   **Automatische Codegenerierung:** Eine Einstellung, die das System anweist, Codes für Projektelemente und Stundengruppen automatisch zu generieren.
*   **Präferenz zwischen Abhängigkeiten und Einschränkungen:** Benutzer können wählen, ob Abhängigkeiten oder Einschränkungen bei Konflikten Vorrang haben.

Ein vollständiger Projekt enthält jedoch auch andere zugehörige Entitäten:

*   **Dem Projekt zugewiesene Stunden:** Die dem Projekt zugeteilten Gesamtstunden.
*   **Dem Projekt zugeordneter Fortschritt:** Der am Projekt erzielte Fortschritt.
*   **Etiketten:** Dem Projekt zugewiesene Etiketten.
*   **Dem Projekt zugewiesene Kriterien:** Dem Projekt zugeordnete Kriterien.
*   **Materialien:** Für den Projekt erforderliche Materialien.
*   **Qualitätsformulare:** Dem Projekt zugehörige Qualitätsformulare.

Das Erstellen oder Bearbeiten eines Projekts kann von mehreren Stellen im Programm aus erfolgen:

*   **Aus der „Projektsliste" in der Unternehmensübersicht:**

    *   **Bearbeiten:** Klicken Sie auf die Bearbeitungsschaltfläche für den gewünschten Projekt.
    *   **Erstellen:** Klicken Sie auf „Neuer Projekt".

*   **Von einem Projekt im Gantt-Diagramm:** Wechseln Sie zur Projektsdetailansicht.

Beim Bearbeiten eines Projekts können Benutzer auf die folgenden Registerkarten zugreifen:

*   **Projektsdetails bearbeiten:** Dieser Bildschirm ermöglicht es Benutzern, grundlegende Projektsdetails zu bearbeiten:

    *   Name
    *   Code
    *   Geschätztes Startdatum
    *   Enddatum
    *   Verantwortliche Person
    *   Kunde
    *   Beschreibung

    .. figure:: images/order-edition.png
       :scale: 50

       Projekte bearbeiten

*   **Projektelementliste:** Dieser Bildschirm ermöglicht es Benutzern, mehrere Operationen an Projektelementen durchzuführen:

    *   Neue Projektelemente erstellen.
    *   Ein Projektelement eine Ebene in der Hierarchie nach oben befördern.
    *   Ein Projektelement eine Ebene in der Hierarchie nach unten stufen.
    *   Ein Projektelement einrücken (in der Hierarchie nach unten verschieben).
    *   Ein Projektelement ausrücken (in der Hierarchie nach oben verschieben).
    *   Projektelemente filtern.
    *   Projektelemente löschen.
    *   Ein Element durch Ziehen und Ablegen innerhalb der Hierarchie verschieben.

    .. figure:: images/order-elements-list.png
       :scale: 40

       Projektelementliste

*   **Zugewiesene Stunden:** Dieser Bildschirm zeigt die dem Projekt zugeordneten Gesamtstunden an und gruppiert die in den Projektelementen eingegebenen Stunden.

    .. figure:: images/order-assigned-hours.png
       :scale: 50

       Von Mitarbeitern dem Projekt zugeordnete Stunden zuweisen

*   **Fortschritt:** Dieser Bildschirm ermöglicht es Benutzern, Fortschrittstypen zuzuweisen und Fortschrittsmessungen für den Projekt einzugeben. Weitere Details finden Sie im Abschnitt „Fortschritt".

*   **Etiketten:** Dieser Bildschirm ermöglicht es Benutzern, einem Projekt Etiketten zuzuweisen und zuvor zugewiesene direkte und indirekte Etiketten anzuzeigen.

    .. figure:: images/order-labels.png
       :scale: 35

       Projektsetiketten

*   **Kriterien:** Dieser Bildschirm ermöglicht es Benutzern, Kriterien zuzuweisen, die für alle Aufgaben innerhalb des Projekts gelten.

    .. figure:: images/order-criterions.png
       :scale: 50

       Projektskriterien

*   **Materialien:** Dieser Bildschirm ermöglicht es Benutzern, Materialien Projekten zuzuweisen. Materialien werden wie folgt verwaltet:

    *   Wählen Sie die Registerkarte „Materialien suchen" unten auf dem Bildschirm.
    *   Geben Sie Text ein, um nach Materialien zu suchen, oder wählen Sie die Kategorien aus, für die Sie Materialien finden möchten.
    *   Das System filtert die Ergebnisse.
    *   Wählen Sie die gewünschten Materialien aus (mehrere Materialien können durch Drücken der Taste „Strg" ausgewählt werden).
    *   Klicken Sie auf „Zuweisen".
    *   Das System zeigt die Liste der bereits dem Projekt zugewiesenen Materialien an.
    *   Wählen Sie die Einheiten und den Status, die dem Projekt zugewiesen werden sollen.
    *   Klicken Sie auf „Speichern" oder „Speichern und fortfahren".
    *   Um den Materialempfang zu verwalten, klicken Sie auf „Teilen", um den Status einer Teilmenge von Material zu ändern.

    .. figure:: images/order-material.png
       :scale: 50

       Materialien, die einem Projekt zugeordnet sind

*   **Qualität:** Benutzer können dem Projekt ein Qualitätsformular zuweisen.

    .. figure:: images/order-quality.png
       :scale: 50

       Dem Projekt zugehöriges Qualitätsformular

Projektelemente bearbeiten
=============================

Projektelemente werden aus der Registerkarte „Projektelementliste" bearbeitet, indem auf das Bearbeitungssymbol geklickt wird. Dadurch öffnet sich ein neuer Bildschirm, auf dem Benutzer:

*   Informationen über das Projektelement bearbeiten können.
*   Dem Projektelement zugeordnete Stunden anzeigen können.
*   Fortschritt von Projektelementen verwalten können.
*   Projektsbezeichnungen verwalten können.
*   Vom Projektelement benötigte Kriterien verwalten können.
*   Materialien verwalten können.
*   Qualitätsformulare verwalten können.

Informationen über das Projektelement bearbeiten
--------------------------------------------------

Das Bearbeiten von Informationen über das Projektelement umfasst das Ändern der folgenden Details:

*   **Projektelementname:** Der Name des Projektelements.
*   **Projektelementcode:** Ein eindeutiger Code für das Projektelement.
*   **Startdatum:** Das geplante Startdatum des Projektelements.
*   **Geschätztes Enddatum:** Das geplante Fertigstellungsdatum des Projektelements.
*   **Gesamtstunden:** Die dem Projektelement zugewiesenen Gesamtstunden.
*   **Stundengruppen:** Dem Projektelement können eine oder mehrere Stundengruppen hinzugefügt werden. **Der Zweck dieser Stundengruppen** besteht darin, die Anforderungen an die Ressourcen zu definieren, die für die Ausführung der Arbeit zugewiesen werden.
*   **Kriterien:** Es können Kriterien hinzugefügt werden, die erfüllt sein müssen, um eine generische Zuweisung für das Projektelement zu ermöglichen.

.. figure:: images/order-element-edition.png
   :scale: 50

   Projektelemente bearbeiten

Dem Projektelement zugeordnete Stunden anzeigen
-------------------------------------------------

Die Registerkarte „Zugewiesene Stunden" ermöglicht es Benutzern, die einem Projektelement zugehörigen Arbeitsberichte anzuzeigen und zu sehen, wie viele der geschätzten Stunden bereits abgeschlossen wurden.

.. figure:: images/order-element-hours.png
   :scale: 50

   Projektelementen zugewiesene Stunden

Der Bildschirm ist in zwei Teile gegliedert:

*   **Arbeitsberichtsliste:** Benutzer können die Liste der dem Projektelement zugehörigen Arbeitsberichte anzeigen, einschließlich Datum und Uhrzeit, Ressource und Anzahl der für die Aufgabe aufgewendeten Stunden.
*   **Verwendung der geschätzten Stunden:** Das System berechnet die Gesamtstundenzahl für die Aufgabe und vergleicht sie mit den geschätzten Stunden.

Fortschritt von Projektelementen verwalten
--------------------------------------------

Das Eingeben von Fortschrittstypen und die Verwaltung des Fortschritts von Projektelementen wird im Kapitel „Fortschritt" beschrieben.

Projektsbezeichnungen verwalten
---------------------------------

Etiketten, wie im Kapitel über Etiketten beschrieben, ermöglichen es Benutzern, Projektelemente zu kategorisieren.

.. figure:: images/order-element-tags.png
   :scale: 50

   Etiketten für Projektelemente zuweisen

Vom Projektelement benötigte Kriterien und Stundengruppen verwalten
---------------------------------------------------------------------

Sowohl ein Projekt als auch ein Projektelement können Kriterien zugewiesen haben, die für die Ausführung der Arbeit erfüllt sein müssen. Kriterien können direkt oder indirekt sein:

*   **Direkte Kriterien:** Diese werden dem Projektelement direkt zugewiesen.
*   **Indirekte Kriterien:** Diese werden übergeordneten Projektelementen in der Hierarchie zugewiesen und vom bearbeiteten Element geerbt.

.. figure:: images/order-element-criterion.png
   :scale: 50

   Kriterien Projektelementen zuweisen

Materialien verwalten
----------------------

Materialien werden in Projekten als Liste verwaltet, die jedem Projektelement oder einem Projekt im Allgemeinen zugeordnet ist. Die Materialliste enthält folgende Felder:

*   **Code:** Der Materialcode.
*   **Datum:** Das dem Material zugeordnete Datum.
*   **Einheiten:** Die erforderliche Anzahl von Einheiten.
*   **Einheitentyp:** Die zur Messung des Materials verwendete Einheit.
*   **Stückpreis:** Der Preis pro Einheit.
*   **Gesamtpreis:** Der Gesamtpreis (berechnet durch Multiplikation des Stückpreises mit der Anzahl der Einheiten).
*   **Kategorie:** Die Kategorie, zu der das Material gehört.
*   **Status:** Der Status des Materials (z. B. Empfangen, Angefordert, Ausstehend, In Bearbeitung, Storniert).

.. figure:: images/order-element-material-search.png
   :scale: 50

   Materialien suchen

.. figure:: images/order-element-material-assign.png
   :scale: 50

   Materialien Projektelementen zuweisen

Qualitätsformulare verwalten
------------------------------

Einige Projektelemente erfordern die Bestätigung, dass bestimmte Aufgaben abgeschlossen wurden, bevor sie als abgeschlossen markiert werden können.

*   Gehen Sie zur Registerkarte „Qualitätsformulare".

    .. figure:: images/order-element-quality.png
       :scale: 50

       Qualitätsformulare Projektelementen zuweisen

*   Das Programm verfügt über eine Suchmaschine für Qualitätsformulare. Es gibt zwei Arten von Qualitätsformularen: nach Element oder nach Prozentsatz.

    *   **Element:** Jedes Element ist unabhängig.
    *   **Prozentsatz:** Jede Frage erhöht den Fortschritt des Projektelements um einen Prozentsatz. Die Prozentsätze müssen sich auf 100 % addieren lassen.

*   Wählen Sie eines der in der Verwaltungsschnittstelle erstellten Formulare aus und klicken Sie auf „Zuweisen".
*   Das Programm weist das ausgewählte Formular aus der Liste der dem Projektelement zugewiesenen Formulare zu.
*   Klicken Sie auf die Schaltfläche „Bearbeiten" beim Projektelement.
*   Das Programm zeigt die Fragen aus dem Qualitätsformular in der unteren Liste an.
*   Markieren Sie die abgeschlossenen Fragen als erreicht.

    *   Wenn das Qualitätsformular auf Prozentsätzen basiert, werden die Fragen in einer bestimmten Reihenfolge beantwortet.
    *   Wenn das Qualitätsformular auf Elementen basiert, können die Fragen in beliebiger Reihenfolge beantwortet werden.
