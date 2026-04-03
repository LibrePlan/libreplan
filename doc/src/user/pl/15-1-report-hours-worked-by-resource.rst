Raport godzin przepracowanych według zasobu
############################################

.. contents::

Cel
===

Ten raport wyodrębnia listę zadań i czas, jaki zasoby poświęciły na nie w określonym okresie. Kilka filtrów umożliwia użytkownikom zawężenie zapytania w celu uzyskania tylko żądanych informacji i wykluczenia zbędnych danych.

Parametry wejściowe i filtry
============================

* **Daty**.
    * *Typ*: Opcjonalny.
    * *Dwa pola daty*:
        * *Data rozpoczęcia:* Jest to najwcześniejsza data raportów pracy do uwzględnienia. Raporty pracy z datami wcześniejszymi niż *Data rozpoczęcia* są wykluczane. Jeśli ten parametr pozostanie pusty, raporty pracy nie są filtrowane według *Daty rozpoczęcia*.
        * *Data zakończenia:* Jest to najpóźniejsza data raportów pracy do uwzględnienia. Raporty pracy z datami późniejszymi niż *Data zakończenia* są wykluczane. Jeśli ten parametr pozostanie pusty, raporty pracy nie są filtrowane według *Daty zakończenia*.

*   **Filtruj według pracowników:**
    *   *Typ:* Opcjonalny.
    *   *Jak działa:* Można wybrać jednego lub więcej pracowników, aby ograniczyć raporty pracy do czasu zarejestrowanego przez tych konkretnych pracowników. Aby dodać pracownika jako filtr, wyszukaj go w selektorze i kliknij przycisk *Dodaj*. Jeśli filtr ten pozostanie pusty, raporty pracy są pobierane niezależnie od pracownika.

*   **Filtruj według etykiet:**
    *   *Typ:* Opcjonalny.
    *   *Jak działa:* Można dodać jedną lub więcej etykiet jako filtry, wyszukując je w selektorze i klikając przycisk *Dodaj*. Etykiety te służą do wyboru zadań, które mają być uwzględnione w wynikach przy obliczaniu godzin im poświęconych. Filtr ten można stosować do kart czasu pracy, zadań, obu lub żadnego z nich.

*   **Filtruj według kryteriów:**
    *   *Typ:* Opcjonalny.
    *   *Jak działa:* Można wybrać jedno lub więcej kryteriów, wyszukując je w selektorze, a następnie klikając przycisk *Dodaj*. Kryteria te służą do wyboru zasobów spełniających co najmniej jedno z nich. Raport pokaże cały czas poświęcony przez zasoby spełniające jedno z wybranych kryteriów.

Wyniki
======

Nagłówek
--------

Nagłówek raportu wyświetla filtry, które zostały skonfigurowane i zastosowane do bieżącego raportu.

Stopka
------

Data wygenerowania raportu jest podana w stopce.

Treść
-----

Treść raportu składa się z kilku grup informacji.

*   Pierwszy poziom agregacji jest według zasobu. Cały czas poświęcony przez zasób jest wyświetlany razem poniżej nagłówka. Każdy zasób jest identyfikowany przez:

    *   *Pracownik:* Nazwisko, Imię.
    *   *Maszyna:* Nazwa.

    Wiersz podsumowania pokazuje łączną liczbę godzin przepracowanych przez zasób.

*   Drugi poziom grupowania jest według *daty*. Wszystkie raporty z konkretnego zasobu z tej samej daty są wyświetlane razem.

    Wiersz podsumowania pokazuje łączną liczbę godzin przepracowanych przez zasób w tej dacie.

*   Ostatni poziom wymienia raporty pracy pracownika z danego dnia. Informacje wyświetlane dla każdego wiersza raportu pracy to:

    *   *Kod zadania:* Kod zadania, do którego przypisano śledzony czas.
    *   *Nazwa zadania:* Nazwa zadania, do którego przypisano śledzony czas.
    *   *Czas rozpoczęcia:* Jest opcjonalny. Jest to czas, w którym zasób rozpoczął pracę nad zadaniem.
    *   *Czas zakończenia:* Jest opcjonalny. Jest to czas, w którym zasób zakończył pracę nad zadaniem w określonej dacie.
    *   *Pola tekstowe:* Jest opcjonalny. Jeśli wiersz raportu pracy zawiera pola tekstowe, wypełnione wartości są tutaj wyświetlane. Format to: <Nazwa pola tekstowego>:<Wartość>
    *   *Etykiety:* Zależy od tego, czy model raportu pracy ma pole etykiety w swojej definicji. Jeśli etykiet jest kilka, są wyświetlane w tej samej kolumnie. Format to: <Nazwa typu etykiety>:<Wartość etykiety>
