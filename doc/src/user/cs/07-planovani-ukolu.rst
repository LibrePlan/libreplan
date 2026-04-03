Plánování úkolů
###############

.. _planificacion:
.. contents::

Plánování úkolů
===============

Plánování v LibrePlan je proces, který byl popsán v celé uživatelské příručce, přičemž zvláště důležité jsou kapitoly o projektech a přiřazování zdrojů. Tato kapitola popisuje základní postupy plánování po správné konfiguraci projektu a Ganttových diagramů.

.. figure:: images/planning-view.png
   :scale: 35

   Pohled na plánování práce

Stejně jako v přehledu společnosti je pohled na plánování projektu rozdělen do několika pohledů podle analyzovaných informací. Pohledy dostupné pro konkrétní projekt jsou:

*   Pohled na plánování
*   Pohled na zatížení zdrojů
*   Pohled na seznam projektů
*   Pohled na pokročilé přiřazování

Pohled na plánování
-------------------

Pohled na plánování kombinuje tři různé perspektivy:

*   **Plánování projektu:** Plánování projektu je zobrazeno v pravé horní části programu jako Ganttův diagram. Tento pohled umožňuje uživatelům dočasně přesouvat úkoly, přiřazovat závislosti mezi nimi, definovat milníky a stanovovat omezení.
*   **Zatížení zdrojů:** Pohled na zatížení zdrojů, umístěný v pravé dolní části obrazovky, zobrazuje dostupnost zdrojů na základě přiřazení, na rozdíl od přiřazení provedených k úkolům. Informace zobrazené v tomto pohledu jsou následující:

    *   **Fialová oblast:** Označuje zatížení zdroje pod 100 % jeho kapacity.
    *   **Zelená oblast:** Označuje zatížení zdroje pod 100 %, plynoucí z toho, že zdroj je plánován pro jiný projekt.
    *   **Oranžová oblast:** Označuje zatížení zdroje nad 100 % v důsledku aktuálního projektu.
    *   **Žlutá oblast:** Označuje zatížení zdroje nad 100 % v důsledku jiných projektů.

*   **Graf a ukazatele dosažené hodnoty:** Tyto lze zobrazit z karty „Dosažená hodnota". Vygenerovaný graf je založen na technice dosažené hodnoty a ukazatele jsou vypočítány pro každý pracovní den projektu. Vypočítané ukazatele jsou:

    *   **BCWS (Plánované náklady naplánované práce):** Kumulativní časová funkce počtu hodin plánovaných do určitého data. Na začátku plánovaného úkolu bude nulová a na konci se bude rovnat celkovému počtu plánovaných hodin. Jako všechny kumulativní grafy bude vždy růst. Funkce pro úkol je součtem denních přiřazení do data výpočtu. Tato funkce má hodnoty po celou dobu za předpokladu, že byly přiřazeny zdroje.
    *   **ACWP (Skutečné náklady provedené práce):** Kumulativní časová funkce hodin vykázaných v pracovních výkazech do určitého data. Tato funkce bude mít hodnotu 0 pouze před datem prvního pracovního výkazu úkolu a její hodnota bude nadále růst s přibývajícím časem a přidáváním hodin pracovních výkazů. Po datu posledního pracovního výkazu nebude mít žádnou hodnotu.
    *   **BCWP (Plánované náklady provedené práce):** Kumulativní časová funkce zahrnující výslednou hodnotu součinu postupu úkolu a množství práce odhadnutého k dokončení úkolu. Hodnoty této funkce rostou s přibývajícím časem stejně jako hodnoty postupu. Postup je násoben celkovým počtem odhadovaných hodin pro všechny úkoly. Hodnota BCWP je součtem hodnot pro počítané úkoly. Postup se sčítá při konfiguraci.
    *   **CV (Odchylka nákladů):** CV = BCWP - ACWP
    *   **SV (Odchylka harmonogramu):** SV = BCWP - BCWS
    *   **BAC (Rozpočet při dokončení):** BAC = max (BCWS)
    *   **EAC (Odhad při dokončení):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Odchylka při dokončení):** VAC = BAC - EAC
    *   **ETC (Odhad do dokončení):** ETC = EAC - ACWP
    *   **CPI (Index výkonu nákladů):** CPI = BCWP / ACWP
    *   **SPI (Index výkonu harmonogramu):** SPI = BCWP / BCWS

V pohledu na plánování projektu mohou uživatelé provádět následující akce:

*   **Přiřazování závislostí:** Klikněte pravým tlačítkem myši na úkol, zvolte „Přidat závislost" a přetáhněte ukazatel myši na úkol, ke kterému má být závislost přiřazena.

    *   Chcete-li změnit typ závislosti, klikněte pravým tlačítkem myši na závislost a zvolte požadovaný typ.

*   **Vytvoření nového milníku:** Klikněte na úkol, před který má být milník přidán, a vyberte možnost „Přidat milník". Milníky lze přesunout výběrem milníku ukazatelem myši a přetažením na požadovanou pozici.
*   **Přesouvání úkolů bez narušení závislostí:** Klikněte pravým tlačítkem myši na tělo úkolu a přetáhněte jej na požadovanou pozici. Pokud nejsou porušena žádná omezení ani závislosti, systém aktualizuje denní přiřazení zdrojů k úkolu a umístí úkol na vybrané datum.
*   **Přiřazování omezení:** Klikněte na příslušný úkol a vyberte možnost „Vlastnosti úkolu". Zobrazí se vyskakovací okno s polem „Omezení", které lze upravit. Omezení mohou být v konfliktu se závislostmi, proto každý projekt určuje, zda mají závislosti přednost před omezeními. Omezení, která lze stanovit, jsou:

    *   **Co nejdříve:** Označuje, že úkol musí začít co nejdříve.
    *   **Ne dříve než:** Označuje, že úkol nesmí začít před určitým datem.
    *   **Zahájení k určitému datu:** Označuje, že úkol musí začít k určitému datu.

Pohled na plánování nabízí také několik postupů, které fungují jako možnosti zobrazení:

*   **Úroveň přiblížení:** Uživatelé mohou zvolit požadovanou úroveň přiblížení. K dispozici je několik úrovní přiblížení: roční, čtyřměsíční, měsíční, týdenní a denní.
*   **Vyhledávací filtry:** Uživatelé mohou filtrovat úkoly podle štítků nebo kritérií.
*   **Kritická cesta:** Jako výsledek použití algoritmu *Dijkstra* pro výpočet cest v grafech byla implementována kritická cesta. Lze ji zobrazit kliknutím na tlačítko „Kritická cesta" v možnostech zobrazení.
*   **Zobrazit štítky:** Umožňuje uživatelům zobrazit štítky přiřazené k úkolům v projektu, které lze zobrazit na obrazovce nebo vytisknout.
*   **Zobrazit zdroje:** Umožňuje uživatelům zobrazit zdroje přiřazené k úkolům v projektu, které lze zobrazit na obrazovce nebo vytisknout.
*   **Tisk:** Umožňuje uživatelům vytisknout zobrazený Ganttův diagram.

Pohled na zatížení zdrojů
-------------------------

Pohled na zatížení zdrojů poskytuje seznam zdrojů obsahující seznam úkolů nebo kritérií, která generují pracovní zátěže. Každý úkol nebo kritérium je zobrazeno jako Ganttův diagram, aby bylo vidět datum zahájení a ukončení zátěže. Různá barva je zobrazena podle toho, zda má zdroj zátěž vyšší nebo nižší než 100 %:

*   **Zelená:** Zátěž nižší než 100 %
*   **Oranžová:** 100% zátěž
*   **Červená:** Zátěž nad 100 %

.. figure:: images/resource-load.png
   :scale: 35

   Pohled na zatížení zdrojů pro konkrétní projekt

Pokud je ukazatel myši umístěn na Ganttově diagramu zdroje, zobrazí se procentuální zátěž pracovníka.

Pohled na seznam projektů
--------------------------

Pohled na seznam projektů umožňuje uživatelům přistupovat k možnostem úpravy a mazání projektů. Více informací naleznete v kapitole „Projekty".

Pohled na pokročilé přiřazování
---------------------------------

Pohled na pokročilé přiřazování je podrobně vysvětlen v kapitole „Přiřazování zdrojů".
