Planowanie Zadań
################

.. _planificacion:
.. contents::

Planowanie Zadań
================

Planowanie w LibrePlan to proces opisany w całym podręczniku użytkownika, przy czym rozdziały dotyczące projektów i przypisania zasobów są szczególnie ważne. Ten rozdział opisuje podstawowe procedury planowania po prawidłowym skonfigurowaniu projektów i wykresów Gantta.

.. figure:: images/planning-view.png
   :scale: 35

   Widok planowania pracy

Podobnie jak w przypadku przeglądu firmy, widok planowania projektu jest podzielony na kilka widoków na podstawie analizowanych informacji. Widoki dostępne dla konkretnego projektu to:

*   Widok planowania
*   Widok obciążenia zasobów
*   Widok listy projektów
*   Widok zaawansowanego przypisania

Widok Planowania
----------------

Widok planowania łączy trzy różne perspektywy:

*   **Planowanie projektu:** Planowanie projektu jest wyświetlane w prawej górnej części programu jako wykres Gantta. Ten widok umożliwia użytkownikom tymczasowe przenoszenie zadań, przypisywanie zależności między nimi, definiowanie kamieni milowych i ustanawianie ograniczeń.
*   **Obciążenie zasobów:** Widok obciążenia zasobów, zlokalizowany w prawej dolnej części ekranu, pokazuje dostępność zasobów na podstawie przypisań, w przeciwieństwie do przypisań dokonanych do zadań. Informacje wyświetlane w tym widoku są następujące:

    *   **Obszar fioletowy:** Wskazuje obciążenie zasobu poniżej 100% jego pojemności.
    *   **Obszar zielony:** Wskazuje obciążenie zasobu poniżej 100%, wynikające z zaplanowania zasobu do innego projektu.
    *   **Obszar pomarańczowy:** Wskazuje obciążenie zasobu powyżej 100% w wyniku bieżącego projektu.
    *   **Obszar żółty:** Wskazuje obciążenie zasobu powyżej 100% w wyniku innych projektów.

*   **Widok wykresu i wskaźniki wartości wypracowanej:** Można je przeglądać z zakładki "Wartość wypracowana". Wygenerowany wykres jest oparty na technice wartości wypracowanej, a wskaźniki są obliczane dla każdego dnia roboczego projektu. Obliczane wskaźniki to:

    *   **BCWS (Budgeted Cost of Work Scheduled):** Skumulowana funkcja czasu dla liczby zaplanowanych godzin do określonej daty. Będzie wynosić 0 na początku planowanego zadania i być równa całkowitej liczbie zaplanowanych godzin na końcu. Jak w przypadku wszystkich skumulowanych wykresów, zawsze będzie rosnąć. Funkcja dla zadania to suma dziennych przypisań do daty obliczenia. Ta funkcja ma wartości dla wszystkich momentów, pod warunkiem że zasoby zostały przypisane.
    *   **ACWP (Actual Cost of Work Performed):** Skumulowana funkcja czasu dla godzin zgłoszonych w raportach pracy do określonej daty. Ta funkcja będzie mieć wartość 0 tylko przed datą pierwszego raportu pracy zadania, a jej wartość będzie nadal rosnąć z upływem czasu i dodawaniem godzin z raportów pracy. Nie będzie mieć wartości po dacie ostatniego raportu pracy.
    *   **BCWP (Budgeted Cost of Work Performed):** Skumulowana funkcja czasu zawierająca wynikową wartość mnożenia postępu zadania przez ilość pracy, którą szacowano dla ukończenia zadania. Wartości tej funkcji rosną z upływem czasu, podobnie jak wartości postępu. Postęp jest mnożony przez całkowitą liczbę szacowanych godzin dla wszystkich zadań. Wartość BCWP to suma wartości dla obliczanych zadań. Postęp jest sumowany, gdy jest skonfigurowany.
    *   **CV (Odchylenie kosztów):** CV = BCWP - ACWP
    *   **SV (Odchylenie harmonogramu):** SV = BCWP - BCWS
    *   **BAC (Budżet przy zakończeniu):** BAC = max (BCWS)
    *   **EAC (Szacunek przy zakończeniu):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Odchylenie przy zakończeniu):** VAC = BAC - EAC
    *   **ETC (Szacunek do zakończenia):** ETC = EAC - ACWP
    *   **CPI (Wskaźnik efektywności kosztowej):** CPI = BCWP / ACWP
    *   **SPI (Wskaźnik efektywności harmonogramu):** SPI = BCWP / BCWS

W widoku planowania projektu użytkownicy mogą wykonywać następujące działania:

*   **Przypisywanie zależności:** Kliknij prawym przyciskiem myszy zadanie, wybierz "Dodaj zależność" i przeciągnij wskaźnik myszy do zadania, do którego należy przypisać zależność.

    *   Aby zmienić typ zależności, kliknij prawym przyciskiem myszy zależność i wybierz żądany typ.

*   **Tworzenie nowego kamienia milowego:** Kliknij zadanie, przed którym ma zostać dodany kamień milowy, i wybierz opcję "Dodaj kamień milowy". Kamienie milowe można przenosić, wybierając kamień milowy wskaźnikiem myszy i przeciągając go w żądane miejsce.
*   **Przenoszenie zadań bez zakłócania zależności:** Kliknij prawym przyciskiem myszy treść zadania i przeciągnij ją do żądanego miejsca. Jeśli żadne ograniczenia ani zależności nie zostaną naruszone, system zaktualizuje dzienne przypisanie zasobów do zadania i umieści zadanie we wybranej dacie.
*   **Przypisywanie ograniczeń:** Kliknij dane zadanie i wybierz opcję "Właściwości zadania". Pojawi się okno podręczne z polem "Ograniczenia", które można modyfikować. Ograniczenia mogą kolidować z zależnościami, dlatego każdy projekt określa, czy zależności mają priorytet nad ograniczeniami. Ograniczenia, które można ustanowić, to:

    *   **Jak najwcześniej:** Wskazuje, że zadanie musi się rozpocząć jak najwcześniej.
    *   **Nie wcześniej niż:** Wskazuje, że zadanie nie może się rozpocząć przed określoną datą.
    *   **Rozpocznij w określonym dniu:** Wskazuje, że zadanie musi się rozpocząć w określonym dniu.

Widok planowania oferuje również kilka procedur, które funkcjonują jako opcje wyświetlania:

*   **Poziom powiększenia:** Użytkownicy mogą wybrać żądany poziom powiększenia. Dostępnych jest kilka poziomów powiększenia: roczny, czteromiesięczny, miesięczny, tygodniowy i dzienny.
*   **Filtry wyszukiwania:** Użytkownicy mogą filtrować zadania na podstawie etykiet lub kryteriów.
*   **Ścieżka krytyczna:** W wyniku użycia algorytmu *Dijkstry* do obliczania ścieżek na grafach zaimplementowano ścieżkę krytyczną. Można ją przeglądać, klikając przycisk "Ścieżka krytyczna" w opcjach wyświetlania.
*   **Pokaż etykiety:** Umożliwia użytkownikom przeglądanie etykiet przypisanych do zadań w projekcie, które można przeglądać na ekranie lub drukować.
*   **Pokaż zasoby:** Umożliwia użytkownikom przeglądanie zasobów przypisanych do zadań w projekcie, które można przeglądać na ekranie lub drukować.
*   **Drukowanie:** Umożliwia użytkownikom drukowanie przeglądanego wykresu Gantta.

Widok Obciążenia Zasobów
-------------------------

Widok obciążenia zasobów zapewnia listę zasobów zawierającą listę zadań lub kryteriów, które generują obciążenia pracą. Każde zadanie lub kryterium jest wyświetlane jako wykres Gantta, dzięki czemu można zobaczyć daty rozpoczęcia i zakończenia obciążenia. Wyświetlany jest różny kolor zależnie od tego, czy zasób ma obciążenie wyższe czy niższe niż 100%:

*   **Zielony:** Obciążenie niższe niż 100%
*   **Pomarańczowy:** 100% obciążenia
*   **Czerwony:** Obciążenie powyżej 100%

.. figure:: images/resource-load.png
   :scale: 35

   Widok obciążenia zasobów dla konkretnego projektu

Jeśli wskaźnik myszy zostanie umieszczony na wykresie Gantta zasobu, zostanie wyświetlony procent obciążenia dla pracownika.

Widok Listy Projektów
---------------------

Widok listy projektów umożliwia użytkownikom dostęp do opcji edytowania i usuwania projektów. Patrz rozdział "Projekty" w celu uzyskania dodatkowych informacji.

Widok Zaawansowanego Przypisania
---------------------------------

Widok zaawansowanego przypisania jest szczegółowo wyjaśniony w rozdziale "Przypisanie zasobów".
