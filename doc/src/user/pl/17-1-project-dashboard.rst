Panel projektu
##############

.. contents::

Panel projektu to perspektywa *LibrePlan* zawierająca zestaw **KPI (kluczowych wskaźników wydajności)** pomagających ocenić wyniki projektu pod względem:

   *   Postępu prac
   *   Kosztów
   *   Statusu przydzielonych zasobów
   *   Ograniczeń czasowych

Wskaźniki wydajności postępu
=============================

Obliczane są dwa wskaźniki: procent postępu projektu i status zadań.

Procent postępu projektu
------------------------

Ten wykres wyświetla ogólny postęp projektu, porównując go z oczekiwanym postępem na podstawie wykresu *Gantta*.

Postęp jest reprezentowany przez dwa paski:

   *   *Bieżący postęp:* Bieżący postęp na podstawie dokonanych pomiarów.
   *   *Oczekiwany postęp:* Postęp, jaki projekt powinien osiągnąć w tym momencie, zgodnie z planem projektu.

Aby zobaczyć rzeczywistą zmierzoną wartość dla każdego paska, najedź kursorem myszy na pasek.

Ogólny postęp projektu jest szacowany przy użyciu kilku różnych metod, ponieważ nie ma jednego, powszechnie poprawnego podejścia:

   *   **Postęp rozproszony:** Jest to typ postępu ustawiony jako postęp rozproszony na poziomie projektu. W tym przypadku nie ma możliwości obliczenia oczekiwanej wartości i wyświetlany jest tylko bieżący pasek.
   *   **Według wszystkich godzin zadań:** Postęp wszystkich zadań projektu jest uśredniany w celu obliczenia ogólnej wartości. Jest to średnia ważona uwzględniająca liczbę godzin przydzielonych do każdego zadania.
   *   **Według godzin ścieżki krytycznej:** Postęp zadań należących do dowolnej ze ścieżek krytycznych projektu jest uśredniany w celu uzyskania ogólnej wartości. Jest to średnia ważona uwzględniająca łączne przydzielone godziny dla każdego zaangażowanego zadania.
   *   **Według czasu trwania ścieżki krytycznej:** Postęp zadań należących do dowolnej ze ścieżek krytycznych jest uśredniany przy użyciu średniej ważonej, ale tym razem uwzględniającej czas trwania każdego zaangażowanego zadania zamiast przypisanych godzin.

Status zadań
------------

Wykres kołowy pokazuje procent zadań projektu w różnych stanach. Zdefiniowane stany to:

   *   **Zakończone:** Ukończone zadania, identyfikowane przez wartość postępu 100%.
   *   **W trakcie:** Zadania aktualnie realizowane. Zadania te mają wartość postępu inną niż 0% lub 100%, lub rejestrowany jest czas pracy.
   *   **Gotowe do rozpoczęcia:** Zadania z 0% postępem, bez zarejestrowanego czasu, których wszystkie zależne zadania *ZAKOŃCZENIE_DO_ROZPOCZĘCIA* są *zakończone*, a wszystkie zależne zadania *ROZPOCZĘCIE_DO_ROZPOCZĘCIA* są *zakończone* lub *w trakcie*.
   *   **Zablokowane:** Zadania z 0% postępem, bez zarejestrowanego czasu i z poprzednimi zależnymi zadaniami, które nie są ani *w trakcie*, ani w stanie *gotowe do rozpoczęcia*.

Wskaźniki kosztów
=================

Obliczanych jest kilka wskaźników kosztów *Earned Value Management*:

   *   **CV (Odchylenie kosztów):** Różnica między krzywą *Earned Value* a krzywą *Actual Cost* w bieżącym momencie. Wartości dodatnie wskazują zysk, a wartości ujemne wskazują stratę.
   *   **ACWP (Rzeczywisty koszt wykonanej pracy):** Łączna liczba godzin zarejestrowanych w projekcie w bieżącym momencie.
   *   **CPI (Wskaźnik wydajności kosztowej):** Stosunek *Earned Value / Actual Cost*.

        *   > 100 jest korzystne, wskazując, że projekt jest poniżej budżetu.
        *   = 100 jest również korzystne, wskazując, że koszt jest dokładnie zgodny z planem.
        *   < 100 jest niekorzystne, wskazując, że koszt ukończenia pracy jest wyższy niż planowano.
   *   **ETC (Szacunek do ukończenia):** Pozostały czas do ukończenia projektu.
   *   **BAC (Budżet przy ukończeniu):** Łączna ilość pracy przydzielonej w planie projektu.
   *   **EAC (Szacunek przy ukończeniu):** Projekcja kierownika dotycząca łącznego kosztu przy ukończeniu projektu, na podstawie *CPI*.
   *   **VAC (Odchylenie przy ukończeniu):** Różnica między *BAC* a *EAC*.

        *   < 0 wskazuje, że projekt przekracza budżet.
        *   > 0 wskazuje, że projekt jest poniżej budżetu.

Zasoby
======

Aby analizować projekt z punktu widzenia zasobów, dostępne są dwa wskaźniki i histogram.

Histogram odchylenia szacunków dla ukończonych zadań
-----------------------------------------------------

Ten histogram oblicza odchylenie między liczbą godzin przydzielonych do zadań projektu a rzeczywistą liczbą godzin im poświęconych.

Odchylenie jest obliczane jako procent dla wszystkich ukończonych zadań, a obliczone odchylenia są reprezentowane w histogramie. Oś pionowa pokazuje liczbę zadań w każdym przedziale odchylenia. Sześć przedziałów odchylenia jest dynamicznie obliczanych.

Wskaźnik nadgodzin
------------------

Ten wskaźnik podsumowuje przeciążenie zasobów przydzielonych do zadań projektu. Jest obliczany według wzoru: **wskaźnik nadgodzin = przeciążenie / (obciążenie + przeciążenie)**.

   *   = 0 jest korzystne, wskazując, że zasoby nie są przeciążone.
   *   > 0 jest niekorzystne, wskazując, że zasoby są przeciążone.

Wskaźnik dostępności
--------------------

Ten wskaźnik podsumowuje wolną pojemność zasobów aktualnie przydzielonych do projektu. Dlatego mierzy dostępność zasobów do przyjęcia kolejnych przydziałów bez przeciążenia. Jest obliczany jako: **wskaźnik dostępności = (1 - obciążenie/pojemność) * 100**

   *   Możliwe wartości mieszczą się w przedziale od 0% (w pełni przydzielone) do 100% (nieprzydzielone).

Czas
====

Uwzględnione są dwa wykresy: histogram odchylenia czasu w czasie zakończenia zadań projektu oraz wykres kołowy dla naruszeń terminów.

Wyprzedzenie lub opóźnienie ukończenia zadania
----------------------------------------------

To obliczenie wyznacza różnicę w dniach między planowanym czasem zakończenia zadań projektu a ich rzeczywistym czasem zakończenia. Planowana data ukończenia jest pobierana z wykresu *Gantta*, a rzeczywista data zakończenia jest pobierana z ostatniego zarejestrowanego czasu dla zadania.

Opóźnienie lub wyprzedzenie w ukończeniu zadania jest reprezentowane w histogramie. Oś pionowa pokazuje liczbę zadań z wartością różnicy dni wyprzedzenia/opóźnienia odpowiadającą przedziałowi dni na osi odciętych. Obliczane jest sześć dynamicznych przedziałów odchylenia ukończenia zadań.

   *   Wartości ujemne oznaczają ukończenie przed harmonogramem.
   *   Wartości dodatnie oznaczają ukończenie po harmonogramie.

Naruszenia terminów
-------------------

Ta sekcja oblicza margines do terminu projektu, jeśli jest ustawiony. Ponadto wykres kołowy pokazuje procent zadań dotrzymujących swoich terminów. W wykresie uwzględnione są trzy typy wartości:

   *   Procent zadań bez skonfigurowanego terminu.
   *   Procent zakończonych zadań z rzeczywistą datą zakończenia późniejszą niż ich termin. Rzeczywista data zakończenia jest pobierana z ostatniego zarejestrowanego czasu dla zadania.
   *   Procent zakończonych zadań z rzeczywistą datą zakończenia wcześniejszą niż ich termin.
