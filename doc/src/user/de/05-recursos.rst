Ressourcenverwaltung
####################

.. _recursos:
.. contents::

Das Programm verwaltet zwei verschiedene Arten von Ressourcen: Personal und Maschinen.

Personalressourcen
------------------

Personalressourcen repräsentieren die Mitarbeiter des Unternehmens. Ihre wichtigsten Merkmale sind:

*   Sie erfüllen ein oder mehrere generische oder mitarbeiterspezifische Kriterien.
*   Sie können einer Aufgabe spezifisch zugewiesen werden.
*   Sie können einer Aufgabe, die ein Ressourcenkriterium erfordert, generisch zugewiesen werden.
*   Sie können nach Bedarf einen Standard- oder spezifischen Kalender haben.

Maschinenressourcen
-------------------

Maschinenressourcen repräsentieren die Maschinen des Unternehmens. Ihre wichtigsten Merkmale sind:

*   Sie erfüllen ein oder mehrere generische oder maschinenspezifische Kriterien.
*   Sie können einer Aufgabe spezifisch zugewiesen werden.
*   Sie können einer Aufgabe, die ein Maschinenkriterium erfordert, generisch zugewiesen werden.
*   Sie können nach Bedarf einen Standard- oder spezifischen Kalender haben.
*   Das Programm enthält einen Konfigurationsbildschirm, auf dem ein *Alpha*-Wert definiert werden kann, um das Maschinen-/Mitarbeiterverhältnis darzustellen.

    *   Der *Alpha*-Wert gibt die Menge an Mitarbeiterzeit an, die für den Betrieb der Maschine erforderlich ist. Zum Beispiel bedeutet ein Alpha-Wert von 0,5, dass jede 8 Stunden Maschinenbetrieb 4 Stunden Mitarbeiterzeit erfordert.
    *   Benutzer können einem Mitarbeiter spezifisch einen *Alpha*-Wert zuweisen und diesen Mitarbeiter für diesen Prozentsatz der Zeit für den Maschinenbetrieb designieren.
    *   Benutzer können auch eine generische Zuweisung basierend auf einem Kriterium vornehmen, sodass allen Ressourcen, die dieses Kriterium erfüllen und verfügbare Zeit haben, ein Nutzungsprozentsatz zugewiesen wird.

Ressourcen verwalten
--------------------

Benutzer können Mitarbeiter und Maschinen im Unternehmen erstellen, bearbeiten und deaktivieren (aber nicht dauerhaft löschen), indem sie zum Bereich „Ressourcen" navigieren. Dieser Bereich bietet die folgenden Funktionen:

*   **Liste der Mitarbeiter:** Zeigt eine nummerierte Liste von Mitarbeitern an, mit der Benutzer deren Details verwalten können.
*   **Liste der Maschinen:** Zeigt eine nummerierte Liste von Maschinen an, mit der Benutzer deren Details verwalten können.

Mitarbeiter verwalten
======================

Die Mitarbeiterverwaltung erfolgt, indem Sie zum Bereich „Ressourcen" gehen und dann „Liste der Mitarbeiter" auswählen. Benutzer können jeden Mitarbeiter in der Liste bearbeiten, indem sie auf das Standard-Bearbeitungssymbol klicken.

Beim Bearbeiten eines Mitarbeiters können Benutzer auf die folgenden Registerkarten zugreifen:

1.  **Mitarbeiterdaten:** Diese Registerkarte ermöglicht es Benutzern, die grundlegenden Identifikationsdaten des Mitarbeiters zu bearbeiten:

    *   Vorname
    *   Nachname(n)
    *   Nationale Ausweisdokumentnummer (DNI)
    *   Warteschlangenbasierte Ressource (siehe Abschnitt zu warteschlangenbasierten Ressourcen)

    .. figure:: images/worker-personal-data.png
       :scale: 50

       Persönliche Daten von Mitarbeitern bearbeiten

2.  **Kriterien:** Diese Registerkarte wird verwendet, um die Kriterien zu konfigurieren, die ein Mitarbeiter erfüllt. Benutzer können alle Mitarbeiter- oder generischen Kriterien zuweisen, die sie für angemessen halten. Es ist entscheidend, dass Mitarbeiter Kriterien erfüllen, um die Funktionalität des Programms zu maximieren. So weisen Sie Kriterien zu:

    i.  Klicken Sie auf die Schaltfläche „Kriterien hinzufügen".
    ii. Suchen Sie nach dem hinzuzufügenden Kriterium und wählen Sie das am besten geeignete aus.
    iii. Klicken Sie auf die Schaltfläche „Hinzufügen".
    iv. Wählen Sie das Startdatum aus, ab dem das Kriterium gilt.
    v.  Wählen Sie das Enddatum für die Anwendung des Kriteriums auf die Ressource aus. Dieses Datum ist optional, wenn das Kriterium als unbefristet gilt.

    .. figure:: images/worker-criterions.png
       :scale: 50

       Kriterien mit Mitarbeitern verknüpfen

3.  **Kalender:** Diese Registerkarte ermöglicht es Benutzern, einen spezifischen Kalender für den Mitarbeiter zu konfigurieren. Allen Mitarbeitern ist ein Standardkalender zugewiesen; es ist jedoch möglich, jedem Mitarbeiter basierend auf einem vorhandenen Kalender einen spezifischen Kalender zuzuweisen.

    .. figure:: images/worker-calendar.png
       :scale: 50

       Kalender-Registerkarte für eine Ressource

4.  **Kostenkategorie:** Diese Registerkarte ermöglicht es Benutzern, die Kostenkategorie zu konfigurieren, die ein Mitarbeiter während eines bestimmten Zeitraums erfüllt. Diese Informationen werden verwendet, um die mit einem Mitarbeiter in einem Projekt verbundenen Kosten zu berechnen.

    .. figure:: images/worker-costcategory.png
       :scale: 50

       Kostenkategorie-Registerkarte für eine Ressource

Die Ressourcenzuweisung wird im Abschnitt „Ressourcenzuweisung" erläutert.

Maschinen verwalten
====================

Maschinen werden für alle Zwecke als Ressourcen behandelt. Ähnlich wie Mitarbeiter können Maschinen daher verwaltet und Aufgaben zugewiesen werden. Die Ressourcenzuweisung wird im Abschnitt „Ressourcenzuweisung" behandelt, der die spezifischen Merkmale von Maschinen erläutern wird.

Maschinen werden über den Menüeintrag „Ressourcen" verwaltet. Dieser Bereich enthält eine Operation namens „Maschinenliste", die die Maschinen des Unternehmens anzeigt. Benutzer können eine Maschine aus dieser Liste bearbeiten oder löschen.

Beim Bearbeiten von Maschinen zeigt das System eine Reihe von Registerkarten zur Verwaltung verschiedener Details an:

1.  **Maschinendetails:** Diese Registerkarte ermöglicht es Benutzern, die Identifikationsdetails der Maschine zu bearbeiten:

    i.  Name
    ii. Maschinencode
    iii. Beschreibung der Maschine

    .. figure:: images/machine-data.png
       :scale: 50

       Maschinendetails bearbeiten

2.  **Kriterien:** Wie bei Mitarbeiterressourcen wird diese Registerkarte verwendet, um Kriterien hinzuzufügen, die die Maschine erfüllt. Maschinen können zwei Arten von Kriterien zugewiesen werden: maschinenspezifische oder generische. Mitarbeiterkriterien können Maschinen nicht zugewiesen werden. So weisen Sie Kriterien zu:

    i.  Klicken Sie auf die Schaltfläche „Kriterien hinzufügen".
    ii. Suchen Sie nach dem hinzuzufügenden Kriterium und wählen Sie das am besten geeignete aus.
    iii. Wählen Sie das Startdatum aus, ab dem das Kriterium gilt.
    iv. Wählen Sie das Enddatum für die Anwendung des Kriteriums auf die Ressource aus.
    v.  Klicken Sie auf die Schaltfläche „Speichern und fortfahren".

    .. figure:: images/machine-criterions.png
       :scale: 50

       Kriterien Maschinen zuweisen

3.  **Kalender:** Diese Registerkarte ermöglicht es Benutzern, einen spezifischen Kalender für die Maschine zu konfigurieren.

    .. figure:: images/machine-calendar.png
       :scale: 50

       Kalender Maschinen zuweisen

4.  **Maschinenkonfiguration:** Diese Registerkarte ermöglicht es Benutzern, das Verhältnis von Maschinen zu Mitarbeiterressourcen zu konfigurieren. Eine Maschine hat einen Alpha-Wert, der das Maschinen-/Mitarbeiterverhältnis angibt. Die Zuordnung eines Mitarbeiters zu einer Maschine kann auf zwei Arten erfolgen:

    i.  **Spezifische Zuweisung:** Weisen Sie einen Datumsbereich zu, in dem der Mitarbeiter der Maschine zugewiesen ist.
    ii. **Generische Zuweisung:** Weisen Sie Kriterien zu, die von den der Maschine zugewiesenen Mitarbeitern erfüllt werden müssen.

    .. figure:: images/machine-configuration.png
       :scale: 50

       Konfiguration von Maschinen

5.  **Kostenkategorie:** Diese Registerkarte ermöglicht es Benutzern, die Kostenkategorie zu konfigurieren, die eine Maschine während eines bestimmten Zeitraums erfüllt.

    .. figure:: images/machine-costcategory.png
       :scale: 50

       Kostenkategorien Maschinen zuweisen

Virtuelle Mitarbeitergruppen
=============================

Das Programm ermöglicht es Benutzern, virtuelle Mitarbeitergruppen zu erstellen, die keine echten Mitarbeiter sind, sondern simuliertes Personal. Diese Gruppen ermöglichen es Benutzern, erhöhte Produktionskapazität zu bestimmten Zeiten basierend auf den Kalendereinstellungen zu modellieren.

Die Registerkarten zum Erstellen virtueller Mitarbeitergruppen sind dieselben wie für die Konfiguration von Mitarbeitern:

*   Allgemeine Details
*   Zugewiesene Kriterien
*   Kalender
*   Zugeordnete Stunden

Der Unterschied zwischen virtuellen Mitarbeitergruppen und tatsächlichen Mitarbeitern besteht darin, dass virtuelle Mitarbeitergruppen einen Namen für die Gruppe und eine Menge haben, die die Anzahl der tatsächlichen Personen in der Gruppe darstellt.

.. figure:: images/virtual-resources.png
   :scale: 50

   Virtuelle Ressourcen

Warteschlangenbasierte Ressourcen
==================================

Warteschlangenbasierte Ressourcen sind eine spezifische Art von Produktivelement, das entweder nicht zugewiesen sein oder 100 % Engagement haben kann. Mit anderen Worten, sie können nicht mehr als eine Aufgabe gleichzeitig geplant haben und können auch nicht überbelastet werden.

Für jede warteschlangenbasierte Ressource wird automatisch eine Warteschlange erstellt. Die für diese Ressourcen geplanten Aufgaben können mit den bereitgestellten Zuweisungsmethoden spezifisch verwaltet werden.
