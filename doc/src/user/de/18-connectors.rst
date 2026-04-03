Konnektoren
###########

.. contents::

Konnektoren sind *LibrePlan*-Clientanwendungen, die zur Kommunikation mit (Web-)Servern verwendet werden können, um Daten abzurufen, zu verarbeiten und zu speichern. Derzeit gibt es drei Konnektoren: den JIRA-Konnektor, den Tim Enterprise-Konnektor und den E-Mail-Konnektor.

Konfiguration
=============

Konnektoren müssen ordnungsgemäß konfiguriert werden, bevor sie verwendet werden können. Sie können über den Bildschirm "Haupteinstellungen" unter der Registerkarte "Konnektoren" konfiguriert werden.

Der Konnektoren-Bildschirm umfasst:

*   **Dropdown-Liste:** Eine Liste der verfügbaren Konnektoren.
*   **Eigenschaftenbearbeitungsbildschirm:** Ein Eigenschaftenbearbeitungsformular für den ausgewählten Konnektor.
*   **Schaltfläche "Verbindung testen":** Eine Schaltfläche zum Testen der Verbindung mit dem Konnektor.

Wählen Sie den zu konfigurierenden Konnektor aus der Dropdown-Liste aus. Ein Eigenschaftenbearbeitungsformular für den ausgewählten Konnektor wird angezeigt. Im Eigenschaftenbearbeitungsformular können Sie die Eigenschaftswerte nach Bedarf ändern und Ihre Konfigurationen mit der Schaltfläche "Verbindung testen" testen.

.. NOTE::

   Die Eigenschaften sind mit Standardwerten konfiguriert. Die wichtigste Eigenschaft ist "Aktiviert". Standardmäßig ist sie auf "N" gesetzt. Dies bedeutet, dass der Konnektor nicht verwendet wird, bis Sie den Wert auf "J" ändern und die Änderungen speichern.

JIRA-Konnektor
==============

JIRA ist ein System zur Verfolgung von Vorgängen und Projekten.

Der JIRA-Konnektor ist eine Anwendung, mit der JIRA-Webserverdaten für JIRA-Vorgänge abgerufen und die Antwort verarbeitet werden kann. Die Anfrage basiert auf JIRA-Labels. In JIRA können Labels zur Kategorisierung von Vorgängen verwendet werden. Die Anfrage ist wie folgt strukturiert: Alle Vorgänge abrufen, die durch diesen Label-Namen kategorisiert sind.

Der Konnektor empfängt die Antwort – in diesem Fall die Vorgänge – und konvertiert sie in *LibrePlan*-„Projektelemente" und „Stundenzettel".

Der *JIRA-Konnektor* muss ordnungsgemäß konfiguriert werden, bevor er verwendet werden kann.

Konfiguration
-------------

Wählen Sie im Bildschirm "Haupteinstellungen" die Registerkarte "Konnektoren". Wählen Sie im Konnektoren-Bildschirm den JIRA-Konnektor aus der Dropdown-Liste aus. Daraufhin wird ein Eigenschaftenbearbeitungsbildschirm angezeigt.

In diesem Bildschirm können Sie die folgenden Eigenschaftswerte konfigurieren:

*   **Aktiviert:** J/N, gibt an, ob Sie den JIRA-Konnektor verwenden möchten. Der Standardwert ist "N."
*   **Server-URL:** Der absolute Pfad zum JIRA-Webserver.
*   **Benutzername und Passwort:** Die Benutzeranmeldeinformationen für die Autorisierung.
*   **JIRA-Labels: kommagetrennte Liste von Labels oder URL:** Sie können entweder die Label-URL oder eine kommagetrennte Liste von Labels eingeben.
*   **Stundentyp:** Die Art der Arbeitsstunden. Der Standardwert ist "Standard."

.. NOTE::

   **JIRA-Labels:** Derzeit unterstützt der JIRA-Webserver keine Bereitstellung einer Liste aller verfügbaren Labels. Als Umgehungslösung haben wir ein einfaches PHP-Skript entwickelt, das eine einfache SQL-Abfrage in der JIRA-Datenbank durchführt, um alle eindeutigen Labels abzurufen. Sie können dieses PHP-Skript entweder als "JIRA-Labels-URL" verwenden oder die gewünschten Labels als kommagetrennten Text im Feld "JIRA-Labels" eingeben.

Klicken Sie abschließend auf die Schaltfläche "Verbindung testen", um zu prüfen, ob Sie eine Verbindung zum JIRA-Webserver herstellen können und Ihre Konfigurationen korrekt sind.

Synchronisation
---------------

Im Projektfenster unter "Allgemeine Daten" können Sie die Synchronisation von Projektelementen mit JIRA-Vorgängen starten.

Klicken Sie auf die Schaltfläche "Mit JIRA synchronisieren", um die Synchronisation zu starten.

*   Wenn dies das erste Mal ist, wird ein Popup-Fenster (mit einer automatisch vervollständigten Liste von Labels) angezeigt. In diesem Fenster können Sie ein Label auswählen, mit dem synchronisiert werden soll, und auf "Synchronisation starten" klicken, um den Synchronisationsprozess zu beginnen, oder auf "Abbrechen", um ihn abzubrechen.

*   Wenn ein Label bereits synchronisiert ist, werden das zuletzt synchronisierte Datum und das Label im JIRA-Bildschirm angezeigt. In diesem Fall wird kein Popup-Fenster zur Auswahl eines Labels angezeigt. Stattdessen beginnt der Synchronisationsprozess direkt für das angezeigte (bereits synchronisierte) Label.

.. NOTE::

   Die Beziehung zwischen "Projekt" und "Label" ist eins-zu-eins. Nur ein Label kann mit einem "Projekt" synchronisiert werden.

.. NOTE::

   Nach erfolgreicher (Re-)Synchronisation werden die Informationen in die Datenbank geschrieben und der JIRA-Bildschirm mit dem zuletzt synchronisierten Datum und Label aktualisiert.

Die (Re-)Synchronisation wird in zwei Phasen durchgeführt:

*   **Phase 1:** Synchronisation der Projektelemente, einschließlich Fortschrittszuweisung und -messungen.
*   **Phase 2:** Synchronisation der Stundenzettel.

.. NOTE::

   Wenn Phase 1 fehlschlägt, wird Phase 2 nicht durchgeführt und keine Informationen in die Datenbank geschrieben.

.. NOTE::

   Die Erfolgs- oder Fehlerinformation wird in einem Popup-Fenster angezeigt.

Nach erfolgreichem Abschluss der Synchronisation wird das Ergebnis in der Registerkarte "Projektstrukturplan (PSP-Aufgaben)" des Bildschirms "Projektdetails" angezeigt. In dieser Benutzeroberfläche gibt es zwei Änderungen gegenüber dem Standard-PSP:

*   Die Spalte "Gesamte Aufgabenstunden" ist nicht änderbar (schreibgeschützt), da die Synchronisation einseitig ist. Aufgabenstunden können nur im JIRA-Webserver aktualisiert werden.
*   Die Spalte "Code" zeigt die JIRA-Vorgangsschlüssel an, die auch Hyperlinks zu den JIRA-Vorgängen sind. Klicken Sie auf den gewünschten Schlüssel, wenn Sie zum Dokument für diesen Schlüssel (JIRA-Vorgang) wechseln möchten.

Zeitplanung
-----------

Die Neusynchronisation von JIRA-Vorgängen kann auch über den Scheduler durchgeführt werden. Gehen Sie zum Bildschirm "Auftragsplanung". Dort können Sie einen JIRA-Auftrag zur Synchronisation konfigurieren. Der Auftrag sucht nach den zuletzt synchronisierten Labels in der Datenbank und synchronisiert sie entsprechend neu. Siehe auch das Scheduler-Handbuch.

Tim Enterprise-Konnektor
========================

Tim Enterprise ist ein niederländisches Produkt von Aenova. Es ist eine webbasierte Anwendung zur Verwaltung der auf Projekte und Aufgaben aufgewendeten Zeit.

Der Tim-Konnektor ist eine Anwendung, die zur Kommunikation mit dem Tim Enterprise-Server verwendet werden kann, um:

*   Alle von einem Mitarbeiter (Benutzer) für ein Projekt aufgewendeten Stunden zu exportieren, die in Tim Enterprise erfasst werden könnten.
*   Alle Dienstpläne des Mitarbeiters (Benutzers) zu importieren, um die Ressource effektiv zu planen.

Der *Tim-Konnektor* muss ordnungsgemäß konfiguriert werden, bevor er verwendet werden kann.

Konfiguration
-------------

Wählen Sie im Bildschirm "Haupteinstellungen" die Registerkarte "Konnektoren". Wählen Sie im Konnektoren-Bildschirm den Tim-Konnektor aus der Dropdown-Liste aus. Daraufhin wird ein Eigenschaftenbearbeitungsbildschirm angezeigt.

In diesem Bildschirm können Sie die folgenden Eigenschaftswerte konfigurieren:

*   **Aktiviert:** J/N, gibt an, ob Sie den Tim-Konnektor verwenden möchten. Der Standardwert ist "N."
*   **Server-URL:** Der absolute Pfad zum Tim Enterprise-Server.
*   **Benutzername und Passwort:** Die Benutzeranmeldeinformationen für die Autorisierung.
*   **Anzahl der Tage für den Stundenzettel-Export nach Tim:** Die Anzahl der Tage rückwärts, für die Sie Stundenzettel exportieren möchten.
*   **Anzahl der Tage für den Dienstplan-Import aus Tim:** Die Anzahl der Tage vorwärts, für die Sie Dienstpläne importieren möchten.
*   **Produktivitätsfaktor:** Effektive Arbeitsstunden in Prozent. Der Standardwert ist "100%."
*   **Abteilungs-IDs für den Dienstplan-Import:** Kommagetrennte Abteilungs-IDs.

Klicken Sie abschließend auf die Schaltfläche "Verbindung testen", um zu prüfen, ob Sie eine Verbindung zum Tim Enterprise-Server herstellen können und Ihre Konfigurationen korrekt sind.

Export
------

Im Projektfenster unter "Allgemeine Daten" können Sie den Export von Stundenzetteln zum Tim Enterprise-Server starten.

Geben Sie den "Tim-Produktcode" ein und klicken Sie auf die Schaltfläche "Nach Tim exportieren", um den Export zu starten.

Der Tim-Konnektor fügt zusammen mit dem Produktcode die folgenden Felder hinzu:

*   Den vollständigen Namen des Mitarbeiters/Benutzers.
*   Das Datum, an dem der Mitarbeiter an einer Aufgabe gearbeitet hat.
*   Den Aufwand bzw. die für die Aufgabe geleisteten Stunden.
*   Eine Option, die angibt, ob Tim Enterprise die Registrierung aktualisieren oder eine neue einfügen soll.

Die Tim Enterprise-Antwort enthält nur eine Liste von Datensatz-IDs (ganzzahlige Werte). Damit ist es schwierig festzustellen, was schiefgelaufen ist, da die Antwortliste nur Zahlen enthält, die nicht mit den Anforderungsfeldern in Beziehung stehen. Die Exportanfrage (Registrierung in Tim) gilt als erfolgreich, wenn alle Listeneinträge keine "0"-Werte enthalten. Andernfalls ist die Exportanfrage für die Einträge mit "0"-Werten fehlgeschlagen. Daher können Sie nicht sehen, welche Anfrage fehlgeschlagen ist, da die Listeneinträge nur den Wert "0" enthalten. Die einzige Möglichkeit, dies zu ermitteln, besteht darin, die Protokolldatei auf dem Tim Enterprise-Server zu prüfen.

.. NOTE::

   Nach erfolgreichem Export werden die Informationen in die Datenbank geschrieben und der Tim-Bildschirm mit dem zuletzt exportierten Datum und Produktcode aktualisiert.

.. NOTE::

   Die Erfolgs- oder Fehlerinformation wird in einem Popup-Fenster angezeigt.

Export-Zeitplanung
------------------

Der Exportprozess kann auch über den Scheduler durchgeführt werden. Gehen Sie zum Bildschirm "Auftragsplanung". Dort können Sie einen Tim-Exportauftrag konfigurieren. Der Auftrag sucht nach den zuletzt exportierten Stundenzetteln in der Datenbank und exportiert sie entsprechend neu. Siehe auch das Scheduler-Handbuch.

Import
------

Der Import von Dienstplänen funktioniert nur mithilfe des Schedulers. Es gibt keine Benutzeroberfläche dafür, da keine Eingaben vom Benutzer erforderlich sind. Gehen Sie zum Bildschirm "Auftragsplanung" und konfigurieren Sie einen Tim-Importauftrag. Der Auftrag durchläuft alle in den Konnektor-Eigenschaften konfigurierten Abteilungen und importiert alle Dienstpläne für jede Abteilung. Siehe auch das Scheduler-Handbuch.

Beim Import fügt der Tim-Konnektor folgende Felder in die Anfrage ein:

*   **Zeitraum:** Der Zeitraum (Datum von – Datum bis), für den Sie den Dienstplan importieren möchten. Dieser kann als Filterkriterium angegeben werden.
*   **Abteilung:** Die Abteilung, für die Sie den Dienstplan importieren möchten. Abteilungen sind konfigurierbar.
*   Die interessierenden Felder (wie Personeninformation, Dienstplankategorie usw.), die der Tim-Server in seine Antwort einbeziehen soll.

Die Importantwort enthält die folgenden Felder, die zur Verwaltung der Ausnahmetage in *LibrePlan* ausreichend sind:

*   **Personeninformation:** Name und Netzwerkname.
*   **Abteilung:** Die Abteilung, in der der Mitarbeiter tätig ist.
*   **Dienstplankategorie:** Informationen zur An-/Abwesenheit (Aanwzig/afwezig) des Mitarbeiters und zum Grund (*LibrePlan*-Ausnahmetyp) bei Abwesenheit des Mitarbeiters.
*   **Datum:** Das Datum, an dem der Mitarbeiter anwesend/abwesend ist.
*   **Uhrzeit:** Die Startzeit der Anwesenheit/Abwesenheit, z. B. 08:00.
*   **Dauer:** Die Anzahl der Stunden, in denen der Mitarbeiter anwesend/abwesend ist.

Bei der Konvertierung der Importantwort in *LibrePlan*'s „Ausnahmetag" werden folgende Übersetzungen berücksichtigt:

*   Wenn die Dienstplankategorie den Namen "Vakantie" enthält, wird sie als "URLAUBSTAG" übersetzt.
*   Die Dienstplankategorie "Feestdag" wird als "FEIERTAG" übersetzt.
*   Alle übrigen Kategorien wie "Jus uren," "PLB uren" usw. müssen manuell zu den "Kalenderausnahmetagen" hinzugefügt werden.

Darüber hinaus ist der Dienstplan in der Importantwort in zwei oder drei Teile pro Tag unterteilt: zum Beispiel Dienstplan-Morgen, Dienstplan-Nachmittag und Dienstplan-Abend. *LibrePlan* erlaubt jedoch nur einen "Ausnahmetyp" pro Tag. Der Tim-Konnektor ist dann dafür verantwortlich, diese Teile zu einem Ausnahmetyp zusammenzuführen. Das heißt, die Dienstplankategorie mit der längsten Dauer wird als gültiger Ausnahmetyp angenommen, aber die Gesamtdauer ist die Summe aller Dauern dieser Kategorieteile.

Im Gegensatz zu *LibrePlan* bedeutet in Tim Enterprise die Gesamtdauer im Urlaubsfall, dass der Mitarbeiter für diese Gesamtdauer nicht verfügbar ist. In *LibrePlan* hingegen sollte die Gesamtdauer bei Urlaub des Mitarbeiters null sein. Der Tim-Konnektor übernimmt auch diese Übersetzung.

E-Mail-Konnektor
================

E-Mail ist eine Methode zum Austausch digitaler Nachrichten von einem Autor an einen oder mehrere Empfänger.

Der E-Mail-Konnektor kann verwendet werden, um Verbindungseigenschaften für einen SMTP-Server (Simple Mail Transfer Protocol) festzulegen.

Der *E-Mail-Konnektor* muss ordnungsgemäß konfiguriert werden, bevor er verwendet werden kann.

Konfiguration
-------------

Wählen Sie im Bildschirm "Haupteinstellungen" die Registerkarte "Konnektoren". Wählen Sie im Konnektoren-Bildschirm den E-Mail-Konnektor aus der Dropdown-Liste aus. Daraufhin wird ein Eigenschaftenbearbeitungsbildschirm angezeigt.

In diesem Bildschirm können Sie die folgenden Eigenschaftswerte konfigurieren:

*   **Aktiviert:** J/N, gibt an, ob Sie den E-Mail-Konnektor verwenden möchten. Der Standardwert ist "N."
*   **Protokoll:** Die Art des SMTP-Protokolls.
*   **Host:** Der absolute Pfad zum SMTP-Server.
*   **Port:** Der Port des SMTP-Servers.
*   **Absenderadresse:** Die E-Mail-Adresse des Nachrichtenabsenders.
*   **Benutzername:** Der Benutzername für den SMTP-Server.
*   **Passwort:** Das Passwort für den SMTP-Server.

Klicken Sie abschließend auf die Schaltfläche "Verbindung testen", um zu prüfen, ob Sie eine Verbindung zum SMTP-Server herstellen können und Ihre Konfigurationen korrekt sind.

E-Mail-Vorlage bearbeiten
--------------------------

Im Projektfenster unter "Konfiguration" und dann "E-Mail-Vorlagen bearbeiten" können Sie die E-Mail-Vorlagen für Nachrichten ändern.

Sie können wählen:

*   **Vorlagensprache:**
*   **Vorlagentyp:**
*   **E-Mail-Betreff:**
*   **Vorlageninhalt:**

Sie müssen die Sprache angeben, da die Webanwendung E-Mails an Benutzer in der Sprache sendet, die sie in ihren Einstellungen gewählt haben. Sie müssen den Vorlagentyp auswählen. Der Typ ist die Benutzerrolle, d. h., diese E-Mail wird nur an Benutzer gesendet, die die ausgewählte Rolle (den Typ) besitzen. Sie müssen den E-Mail-Betreff festlegen. Der Betreff ist eine kurze Zusammenfassung des Themas der Nachricht. Sie müssen den E-Mail-Inhalt festlegen. Dies sind beliebige Informationen, die Sie an den Benutzer senden möchten. Es gibt auch einige Schlüsselwörter, die Sie in der Nachricht verwenden können; die Webanwendung verarbeitet sie und setzt anstelle des Schlüsselworts einen neuen Wert.

E-Mails zeitplanen
------------------

Das Senden von E-Mails kann nur über den Scheduler durchgeführt werden. Gehen Sie zu "Konfiguration" und dann zum Bildschirm "Auftragsplanung". Dort können Sie einen E-Mail-Sendeauftrag konfigurieren. Der Auftrag nimmt eine Liste von E-Mail-Benachrichtigungen, sammelt Daten und sendet sie an die E-Mail-Adresse des Benutzers. Siehe auch das Scheduler-Handbuch.

.. NOTE::

   Die Erfolgs- oder Fehlerinformation wird in einem Popup-Fenster angezeigt.
