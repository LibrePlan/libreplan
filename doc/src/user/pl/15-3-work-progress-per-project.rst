Raport pracy i postępu według projektu
########################################

.. contents::

Cel
===

Ten raport zawiera przegląd statusu projektów, biorąc pod uwagę zarówno postęp, jak i koszty.

Analizuje bieżący postęp każdego projektu, porównując go z planowanym postępem i wykonaną pracą.

Raport wyświetla również kilka wskaźników związanych z kosztami projektu, porównując bieżące wyniki z planowanymi.

Parametry wejściowe i filtry
============================

Istnieje kilka obowiązkowych parametrów:

   *   **Data referencyjna:** Jest to data używana jako punkt odniesienia do porównania planowanego statusu projektu z jego rzeczywistymi wynikami. *Domyślna wartość tego pola to bieżąca data*.

   *   **Typ postępu:** Jest to typ postępu używany do mierzenia postępu projektu. Aplikacja umożliwia pomiar projektu jednocześnie za pomocą różnych typów postępu. Typ wybrany przez użytkownika w menu rozwijane jest używany do obliczania danych raportu. Domyślna wartość *typu postępu* to *rozproszony*, który jest specjalnym typem postępu używającym preferowanej metody pomiaru postępu skonfigurowanej dla każdego elementu SPP.

Parametry opcjonalne to:

   *   **Data początkowa:** Jest to najwcześniejsza data rozpoczęcia projektów do uwzględnienia w raporcie. Jeśli to pole pozostanie puste, nie ma minimalnej daty rozpoczęcia dla projektów.

   *   **Data końcowa:** Jest to najpóźniejsza data zakończenia projektów do uwzględnienia w raporcie. Wszystkie projekty kończące się po *Dacie końcowej* zostaną wykluczone.

   *   **Filtruj według projektów:** Ten filtr umożliwia użytkownikom wybór konkretnych projektów do uwzględnienia w raporcie. Jeśli do filtru nie zostaną dodane żadne projekty, raport będzie zawierał wszystkie projekty w bazie danych. Dostępne jest wyszukiwalne menu rozwijane do znalezienia żądanego projektu. Projekty są dodawane do filtru przez kliknięcie przycisku *Dodaj*.

Wyniki
======

Format wyników jest następujący:

Nagłówek
--------

Nagłówek raportu wyświetla następujące pola:

   *   **Data początkowa:** Data filtrowania startowego. Nie jest wyświetlana, jeśli raport nie jest filtrowany według tego pola.
   *   **Data końcowa:** Data filtrowania końcowego. Nie jest wyświetlana, jeśli raport nie jest filtrowany według tego pola.
   *   **Typ postępu:** Typ postępu używany w raporcie.
   *   **Projekty:** Wskazuje filtrowane projekty, dla których generowany jest raport. Pokaże ciąg *Wszystkie*, gdy raport obejmuje wszystkie projekty spełniające pozostałe filtry.
   *   **Data referencyjna:** Obowiązkowa data referencyjna wejściowa wybrana dla raportu.

Stopka
------

Stopka wyświetla datę wygenerowania raportu.

Treść
-----

Treść raportu składa się z listy projektów wybranych na podstawie filtrów wejściowych.

Filtry działają poprzez dodawanie warunków, z wyjątkiem zestawu utworzonego przez filtry dat (*Data początkowa*, *Data końcowa*) i *Filtruj według projektów*. W tym przypadku, jeśli jeden lub oba filtry dat są wypełnione, a *Filtruj według projektów* zawiera listę wybranych projektów, ten ostatni filtr ma pierwszeństwo. Oznacza to, że projekty uwzględnione w raporcie to projekty podane przez *Filtruj według projektów*, niezależnie od filtrów dat.

Ważne jest, aby pamiętać, że postęp w raporcie jest obliczany jako ułamek jedności, mieszczący się w przedziale od 0 do 1.

Dla każdego projektu wybranego do uwzględnienia w wynikach raportu wyświetlane są następujące informacje:

   * *Nazwa projektu*.
   * *Łączne godziny*. Łączne godziny projektu są wyświetlane przez dodanie godzin dla każdego zadania. Pokazywane są dwa typy łącznych godzin:
      *   *Szacowane (TE)*. Jest to suma wszystkich szacowanych godzin w SPP projektu. Reprezentuje łączną liczbę godzin szacowanych do ukończenia projektu.
      *   *Planowane (TP)*. W *LibrePlan* możliwe jest posiadanie dwóch różnych wielkości: szacowanej liczby godzin dla zadania (liczby godzin początkowo szacowanych do ukończenia zadania) i godzin planowanych (godzin przydzielonych w planie do ukończenia zadania). Godziny planowane mogą być równe, mniejsze lub większe niż godziny szacowane i są ustalane w późniejszej fazie, operacji przypisania. Dlatego łączne godziny planowane dla projektu to suma wszystkich przydzielonych godzin dla jego zadań.
   * *Postęp*. Pokazywane są trzy pomiary związane z ogólnym postępem typu określonego w filtrze wejściowym postępu dla każdego projektu na datę referencyjną:
      *   *Zmierzony (PM)*. Jest to ogólny postęp uwzględniający pomiary postępu z datą wcześniejszą niż *Data referencyjna* w parametrach wejściowych raportu. Brane są pod uwagę wszystkie zadania, a suma jest ważona liczbą godzin dla każdego zadania.
      *   *Przypisany (PI)*. Jest to postęp zakładający, że praca jest kontynuowana w tym samym tempie, co godziny ukończone dla zadania. Jeśli X godzin z Y godzin dla zadania jest ukończonych, ogólny przypisany postęp jest uważany za X/Y.
      *   *Planowany (PP)*. Jest to ogólny postęp projektu zgodnie z planowanym harmonogramem na datę referencyjną. Jeśli wszystko wydarzyło się dokładnie zgodnie z planem, zmierzony postęp powinien być taki sam jak planowany postęp.
   * *Godziny do daty*. Są dwa pola pokazujące liczbę godzin do daty referencyjnej z dwóch perspektyw:
      *   *Planowane (HP)*. Ta liczba to suma godzin przydzielonych do dowolnego zadania w projekcie z datą mniejszą lub równą *Dacie referencyjnej*.
      *   *Rzeczywiste (HR)*. Ta liczba to suma godzin zgłoszonych w raportach pracy dla dowolnego zadania w projekcie z datą mniejszą lub równą *Dacie referencyjnej*.
   * *Różnica*. Pod tym nagłówkiem znajduje się kilka wskaźników związanych z kosztami:
      *   *Koszt*. Jest to różnica godzin między liczbą godzin poniesionych, biorąc pod uwagę zmierzony postęp, a godzinami ukończonymi do daty referencyjnej. Formuła to: *PM*TP - HR*.
      *   *Planowane*. Jest to różnica między godzinami poniesionymi według ogólnego zmierzonego postępu projektu a liczbą zaplanowanych do *Daty referencyjnej*. Mierzy przewagę lub opóźnienie w czasie. Formuła to: *PM*TP - HP*.
      *   *Wskaźnik kosztów*. Jest obliczany przez podzielenie *PM* / *PI*. Jeśli jest większy niż 1, oznacza to, że projekt jest opłacalny w tym momencie. Jeśli jest mniejszy niż 1, oznacza to, że projekt przynosi straty.
      *   *Wskaźnik planowania*. Jest obliczany przez podzielenie *PM* / *PP*. Jeśli jest większy niż 1, oznacza to, że projekt wyprzedza harmonogram. Jeśli jest mniejszy niż 1, oznacza to, że projekt jest opóźniony.
