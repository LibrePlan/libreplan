Kryteria
########

.. contents::

Kryteria to elementy używane w programie do kategoryzowania zarówno zasobów, jak i zadań. Zadania wymagają spełnienia konkretnych kryteriów, a zasoby muszą spełniać te kryteria.

Oto przykład użycia kryteriów: Zasób otrzymuje kryterium "spawacz" (co oznacza, że zasób spełnia kategorię "spawacz"), a zadanie wymaga spełnienia kryterium "spawacz". W rezultacie, gdy zasoby są przydzielane do zadań przy użyciu przypisania generycznego (w przeciwieństwie do przypisania konkretnego), brani pod uwagę będą pracownicy spełniający kryterium "spawacz". Więcej informacji na temat różnych typów przypisań można znaleźć w rozdziale dotyczącym alokacji zasobów.

Program pozwala na kilka operacji obejmujących kryteria:

*   Administracja kryteriami
*   Przypisywanie kryteriów do zasobów
*   Przypisywanie kryteriów do zadań
*   Filtrowanie jednostek na podstawie kryteriów. Zadania i elementy projektów mogą być filtrowane według kryteriów w celu wykonywania różnych operacji w programie.

W tej sekcji zostanie wyjaśniona tylko pierwsza funkcja, administracja kryteriami. Dwa typy przypisań zostaną omówione później: alokacja zasobów w rozdziale "Zarządzanie zasobami" oraz filtrowanie w rozdziale "Planowanie zadań".

Administracja Kryteriami
========================

Administracja kryteriami jest dostępna za pośrednictwem menu administracji:

.. figure:: images/menu.png
   :scale: 50

   Zakładki menu pierwszego poziomu

Konkretna operacja do zarządzania kryteriami to *Zarządzaj kryteriami*. Ta operacja umożliwia wyświetlenie listy kryteriów dostępnych w systemie.

.. figure:: images/lista-criterios.png
   :scale: 50

   Lista kryteriów

Możesz uzyskać dostęp do formularza tworzenia/edycji kryterium, klikając przycisk *Utwórz*. Aby edytować istniejące kryterium, kliknij ikonę edycji.

.. figure:: images/edicion-criterio.png
   :scale: 50

   Edytowanie kryteriów

Formularz edycji kryteriów, jak pokazano na poprzednim obrazie, umożliwia wykonywanie następujących operacji:

*   **Edytowanie nazwy kryterium.**
*   **Określanie, czy wiele wartości może być przypisanych jednocześnie, czy tylko jedna wartość dla wybranego typu kryterium.** Na przykład zasób może spełniać dwa kryteria: "spawacz" i "operator tokarki."
*   **Określanie typu kryterium:**

    *   **Ogólne:** Kryterium, które może być używane zarówno dla maszyn, jak i pracowników.
    *   **Pracownik:** Kryterium, które może być używane tylko dla pracowników.
    *   **Maszyna:** Kryterium, które może być używane tylko dla maszyn.

*   **Wskazywanie, czy kryterium jest hierarchiczne.** Czasami kryteria muszą być traktowane hierarchicznie. Na przykład przypisanie kryterium do elementu nie przypisuje go automatycznie do elementów z niego pochodzących. Wyraźnym przykładem hierarchicznego kryterium jest "lokalizacja." Na przykład osoba oznaczona jako należąca do "Galicji" będzie również należeć do "Hiszpanii."
*   **Wskazywanie, czy kryterium jest autoryzowane.** W ten sposób użytkownicy dezaktywują kryteria. Gdy kryterium zostało już utworzone i użyte w danych historycznych, nie można go zmienić. Zamiast tego można je dezaktywować, aby nie pojawiało się na listach wyboru.
*   **Opisywanie kryterium.**
*   **Dodawanie nowych wartości.** Pole wprowadzania tekstu z przyciskiem *Nowe kryterium* znajduje się w drugiej części formularza.
*   **Edytowanie nazw istniejących wartości kryteriów.**
*   **Przesuwanie wartości kryteriów w górę lub w dół na liście bieżących wartości kryteriów.**
*   **Usuwanie wartości kryterium z listy.**

Formularz administracji kryteriami jest zgodny z zachowaniem formularzy opisanym we wprowadzeniu i oferuje trzy akcje: *Zapisz*, *Zapisz i zamknij* oraz *Zamknij*.
