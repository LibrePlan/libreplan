Harmonogram
###########

.. contents::

Harmonogram jest zaprojektowany do dynamicznego planowania zadań. Jest opracowany przy użyciu *Spring Framework Quartz scheduler*.

Aby efektywnie korzystać z tego harmonogramu, należy najpierw utworzyć zadania (zadania Quartz), które mają być planowane. Następnie zadania te można dodać do bazy danych, ponieważ wszystkie zadania do zaplanowania są przechowywane w bazie danych.

Gdy harmonogram startuje, odczytuje z bazy danych zadania do zaplanowania lub odplanowania i odpowiednio je planuje lub usuwa. Następnie zadania mogą być dynamicznie dodawane, aktualizowane lub usuwane przy użyciu interfejsu użytkownika ``Harmonogramowanie zadań``.

.. NOTE::
   Harmonogram startuje, gdy aplikacja LibrePlan uruchamia się i zatrzymuje, gdy aplikacja się zatrzymuje.

.. NOTE::
   Ten harmonogram obsługuje tylko ``wyrażenia cron`` do planowania zadań.

Kryteria, których harmonogram używa do planowania lub usuwania zadań po uruchomieniu, są następujące:

Dla wszystkich zadań:

* Zaplanuj

  * Zadanie ma *Łącznik*, a *Łącznik* jest aktywowany, i zadanie może być zaplanowane.
  * Zadanie nie ma *Łącznika* i może być zaplanowane.

* Usuń

  * Zadanie ma *Łącznik*, a *Łącznik* nie jest aktywowany.
  * Zadanie ma *Łącznik*, a *Łącznik* jest aktywowany, ale zadanie nie może być zaplanowane.
  * Zadanie nie ma *Łącznika* i nie może być zaplanowane.

.. NOTE::
   Zadań nie można ponownie zaplanować ani odplanować, jeśli są aktualnie uruchomione.

Widok listy harmonogramowania zadań
=====================================

Widok ``Lista harmonogramowania zadań`` umożliwia użytkownikom:

*   Dodanie nowego zadania.
*   Edycję istniejącego zadania.
*   Usunięcie zadania.
*   Ręczne uruchomienie procesu.

Dodawanie lub edytowanie zadania
=================================

Z widoku ``Lista harmonogramowania zadań`` kliknij:

*   ``Utwórz``, aby dodać nowe zadanie, lub
*   ``Edytuj``, aby zmodyfikować wybrane zadanie.

Obie akcje otworzą formularz tworzenia/edycji ``zadania``. ``Formularz`` wyświetla następujące właściwości:

*   Pola:

    *   **Grupa zadań:** Nazwa grupy zadań.
    *   **Nazwa zadania:** Nazwa zadania.
    *   **Wyrażenie cron:** Pole tylko do odczytu z przyciskiem ``Edytuj`` do otwierania okna wprowadzania ``wyrażenia cron``.
    *   **Nazwa klasy zadania:** ``Lista rozwijana`` do wyboru zadania (istniejące zadanie).
    *   **Łącznik:** ``Lista rozwijana`` do wyboru łącznika. Nie jest obowiązkowe.
    *   **Harmonogram:** Pole wyboru wskazujące, czy zaplanować to zadanie.

*   Przyciski:

    *   **Zapisz:** Aby zapisać lub zaktualizować zadanie zarówno w bazie danych, jak i w harmonogramie. Użytkownik jest następnie przenoszony do ``Widoku listy harmonogramowania zadań``.
    *   **Zapisz i kontynuuj:** To samo co „Zapisz", ale użytkownik nie jest przenoszony do ``Widoku listy harmonogramowania zadań``.
    *   **Anuluj:** Nic nie jest zapisywane, a użytkownik jest przenoszony do ``Widoku listy harmonogramowania zadań``.

*   Oraz sekcja wskazówek dotyczących składni wyrażeń cron.

Wyskakujące okienko wyrażenia cron
-----------------------------------

Aby poprawnie wprowadzić ``wyrażenie cron``, używany jest formularz wyskakujący ``wyrażenia cron``. W tym formularzu możesz wprowadzić żądane ``wyrażenie cron``. Zapoznaj się również ze wskazówką dotyczącą ``wyrażenia cron``. Jeśli wprowadzisz nieprawidłowe ``wyrażenie cron``, zostaniesz natychmiast powiadomiony.

Usuwanie zadania
================

Kliknij przycisk ``Usuń``, aby usunąć zadanie zarówno z bazy danych, jak i z harmonogramu. Wyświetlone zostaną informacje o powodzeniu lub niepowodzeniu tej akcji.

Ręczne uruchamianie zadania
============================

Jako alternatywa dla oczekiwania na uruchomienie zadania zgodnie z harmonogramem, możesz kliknąć ten przycisk, aby bezpośrednio uruchomić proces. Następnie informacje o powodzeniu lub niepowodzeniu zostaną wyświetlone w ``wyskakującym okienku``.
