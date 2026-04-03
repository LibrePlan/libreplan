Raport łącznych godzin przepracowanych przez zasób w miesiącu
##############################################################

.. contents::

Cel
===

Ten raport podaje łączną liczbę godzin przepracowanych przez każdy zasób w danym miesiącu. Informacje te mogą być przydatne do ustalenia nadgodzin pracownika lub, w zależności od organizacji, liczby godzin, za które każdy zasób powinien otrzymać wynagrodzenie.

Aplikacja śledzi raporty pracy zarówno dla pracowników, jak i maszyn. W przypadku maszyn raport sumuje liczbę godzin ich pracy w danym miesiącu.

Parametry wejściowe i filtry
============================

Aby wygenerować ten raport, użytkownicy muszą podać rok i miesiąc, dla których chcą pobrać łączną liczbę godzin przepracowanych przez każdy zasób.

Wyniki
======

Format wyników jest następujący:

Nagłówek
--------

Nagłówek raportu wyświetla:

   *   *Rok*, którego dotyczą dane w raporcie.
   *   *Miesiąc*, którego dotyczą dane w raporcie.

Stopka
------

Stopka wyświetla datę wygenerowania raportu.

Treść
-----

Sekcja danych raportu składa się z pojedynczej tabeli z dwiema kolumnami:

   *   Jedna kolumna oznaczona **Nazwa** dla nazwy zasobu.
   *   Jedna kolumna oznaczona **Godziny** z łączną liczbą godzin przepracowanych przez zasób w danym wierszu.

Istnieje ostatni wiersz, który agreguje łączną liczbę godzin przepracowanych przez wszystkie zasoby w określonym *miesiącu* i *roku*.
