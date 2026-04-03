Řídicí panel projektu
#####################

.. contents::

Řídicí panel projektu je perspektiva *LibrePlan*, která obsahuje sadu **KPI (klíčových ukazatelů výkonu)** pro pomoc při posuzování výkonu projektu z hlediska:

   *   Postupu práce
   *   Nákladů
   *   Stavu přidělených zdrojů
   *   Časových omezení

Ukazatele výkonu postupu
========================

Jsou vypočítány dva ukazatele: procentuální postup projektu a stav úkolů.

Procentuální postup projektu
----------------------------

Tento graf zobrazuje celkový postup projektu a porovnává jej s očekávaným postupem na základě *Ganttova* diagramu.

Postup je reprezentován dvěma sloupci:

   *   *Aktuální postup:* Aktuální postup na základě provedených měření.
   *   *Očekávaný postup:* Postup, kterého by měl projekt dosáhnout v tomto okamžiku podle projektového plánu.

Chcete-li zobrazit skutečně naměřenou hodnotu pro každý sloupec, najeďte kurzorem myši na daný sloupec.

Celkový postup projektu je odhadován několika různými metodami, protože neexistuje jediný, univerzálně správný přístup:

   *   **Rozložení postupu:** Toto je typ postupu nastavený jako rozložení postupu na úrovni projektu. V tomto případě neexistuje způsob, jak vypočítat očekávanou hodnotu, a zobrazuje se pouze aktuální sloupec.
   *   **Podle hodin všech úkolů:** Postup všech projektových úkolů je zprůměrován pro výpočet celkové hodnoty. Jde o vážený průměr, který zohledňuje počet hodin přidělených každému úkolu.
   *   **Podle hodin kritické cesty:** Postup úkolů patřících do některé z kritických cest projektu je zprůměrován pro získání celkové hodnoty. Jde o vážený průměr, který zohledňuje celkové přidělené hodiny pro každý zapojený úkol.
   *   **Podle doby trvání kritické cesty:** Postup úkolů patřících do některé z kritických cest je zprůměrován pomocí váženého průměru, ale tentokrát se bere v úvahu délka trvání každého zapojeného úkolu místo přidělených hodin.

Stav úkolů
----------

Výsečový graf ukazuje procentuální podíl projektových úkolů v různých stavech. Definované stavy jsou:

   *   **Dokončeno:** Dokončené úkoly, identifikované hodnotou postupu 100 %.
   *   **Probíhá:** Úkoly, které jsou aktuálně ve zpracování. Tyto úkoly mají hodnotu postupu jinou než 0 % nebo 100 %, nebo byl zaznamenán nějaký pracovní čas.
   *   **Připraveno ke spuštění:** Úkoly s 0% postupem, bez zaznamenaného času, jejichž všechny *FINISH_TO_START* závislé úkoly jsou *dokončeny* a všechny jejich *START_TO_START* závislé úkoly jsou *dokončeny* nebo *probíhají*.
   *   **Blokováno:** Úkoly s 0% postupem, bez zaznamenaného času a s předcházejícími závislými úkoly, které nejsou ani *probíhající* ani ve stavu *připraveno ke spuštění*.

Ukazatele nákladů
=================

Je vypočítáno několik ukazatelů nákladů *řízení dosažené hodnoty*:

   *   **CV (Odchylka nákladů):** Rozdíl mezi *křivkou dosažené hodnoty* a *křivkou skutečných nákladů* v aktuálním okamžiku. Kladné hodnoty indikují zisk a záporné hodnoty indikují ztrátu.
   *   **ACWP (Skutečné náklady provedené práce):** Celkový počet hodin sledovaných v projektu v aktuálním okamžiku.
   *   **CPI (Index výkonu nákladů):** Poměr *Dosažená hodnota / Skutečné náklady*.

        *   > 100 je příznivé, což indikuje, že projekt je pod rozpočtem.
        *   = 100 je také příznivé, což indikuje, že náklady jsou přesně podle plánu.
        *   < 100 je nepříznivé, což indikuje, že náklady na dokončení práce jsou vyšší než bylo plánováno.
   *   **ETC (Odhad do dokončení):** Zbývající čas do dokončení projektu.
   *   **BAC (Rozpočet při dokončení):** Celkové množství práce přidělené v projektovém plánu.
   *   **EAC (Odhad při dokončení):** Projekce vedoucího projektu o celkových nákladech při dokončení projektu, na základě *CPI*.
   *   **VAC (Odchylka při dokončení):** Rozdíl mezi *BAC* a *EAC*.

        *   < 0 indikuje, že projekt překračuje rozpočet.
        *   > 0 indikuje, že projekt je pod rozpočtem.

Zdroje
======

Pro analýzu projektu z pohledu zdrojů jsou poskytnuty dva poměry a histogram.

Histogram odchylky odhadů dokončených úkolů
-------------------------------------------

Tento histogram vypočítává odchylku mezi počtem hodin přidělených projektovým úkolům a skutečným počtem hodin jim věnovaných.

Odchylka je vypočítána jako procento pro všechny dokončené úkoly a vypočítané odchylky jsou reprezentovány v histogramu. Svislá osa ukazuje počet úkolů v každém intervalu odchylky. Je dynamicky vypočítáno šest intervalů odchylky.

Poměr přesčasů
--------------

Tento poměr shrnuje přetížení zdrojů přidělených k projektovým úkolům. Je vypočítán pomocí vzorce: **poměr přesčasů = přetížení / (zátěž + přetížení)**.

   *   = 0 je příznivé, což indikuje, že zdroje nejsou přetíženy.
   *   > 0 je nepříznivé, což indikuje, že zdroje jsou přetíženy.

Poměr dostupnosti
-----------------

Tento poměr shrnuje volnou kapacitu zdrojů aktuálně přidělených k projektovým úkolům. Měří tedy dostupnost zdrojů pro příjem dalších přiřazení bez přetížení. Je vypočítán jako: **poměr dostupnosti = (1 - zátěž/kapacita) * 100**

   *   Možné hodnoty jsou mezi 0 % (plně přiřazeno) a 100 % (nepřiřazeno).

Čas
===

Jsou zahrnuty dva grafy: histogram pro časovou odchylku v době dokončení projektových úkolů a výsečový graf pro porušení termínů.

Předstih nebo zpoždění dokončení úkolu
--------------------------------------

Tento výpočet určuje rozdíl ve dnech mezi plánovanou dobou ukončení projektových úkolů a jejich skutečnou dobou ukončení. Plánované datum dokončení je převzato z *Ganttova* diagramu a skutečné datum dokončení je převzato z posledního zaznamenaného času pro úkol.

Zpoždění nebo předstih dokončení úkolu je reprezentován v histogramu. Svislá osa ukazuje počet úkolů s hodnotou rozdílu dnů předstihu/zpoždění odpovídající intervalu dnů na vodorovné ose. Je vypočítáno šest dynamických intervalů odchylky dokončení úkolu.

   *   Záporné hodnoty znamenají dokončení před plánovaným termínem.
   *   Kladné hodnoty znamenají dokončení po plánovaném termínu.

Porušení termínů
----------------

Tato část vypočítává rezervu s termínem projektu, pokud je nastaven. Výsečový graf navíc ukazuje procento úkolů splňujících svůj termín. Graf obsahuje tři typy hodnot:

   *   Procento úkolů bez nastaveného termínu.
   *   Procento dokončených úkolů se skutečným datem ukončení pozdějším než jejich termín. Skutečné datum ukončení je převzato z posledního zaznamenaného času pro úkol.
   *   Procento dokončených úkolů se skutečným datem ukončení dřívějším než jejich termín.
