Kritéria
########

.. contents::

Kritéria jsou prvky, které se v programu používají ke kategorizaci zdrojů i úkolů. Úkoly vyžadují splnění konkrétních kritérií a zdroje musí tato kritéria splňovat.

Zde je příklad použití kritérií: zdroji je přiřazeno kritérium "svářeč" (což znamená, že zdroj splňuje kategorii "svářeč") a úkol vyžaduje kritérium "svářeč" pro dokončení. V důsledku toho, když jsou zdroje přidělovány úkolům pomocí obecného přidělování (na rozdíl od konkrétního přidělování), budou zvažováni pracovníci s kritériem "svářeč". Více informací o různých typech přidělování naleznete v kapitole o přidělování zdrojů.

Program umožňuje několik operací zahrnujících kritéria:

*   Správa kritérií
*   Přiřazení kritérií zdrojům
*   Přiřazení kritérií úkolům
*   Filtrování entit na základě kritérií. Úkoly a prvky projektu lze filtrovat podle kritérií pro provádění různých operací v programu.

Tato sekce vysvětlí pouze první funkci, správu kritérií. Dva typy přidělování budou popsány později: přidělování zdrojů v kapitole "Správa zdrojů" a filtrování v kapitole "Plánování úkolů".

Správa kritérií
===============

Správa kritérií je přístupná přes menu správy:

.. figure:: images/menu.png
   :scale: 50

   Záložky nabídky první úrovně

Konkrétní operace pro správu kritérií je *Spravovat kritéria*. Tato operace umožňuje zobrazit seznam kritérií dostupných v systému.

.. figure:: images/lista-criterios.png
   :scale: 50

   Seznam kritérií

K formuláři pro vytvoření/editaci kritéria lze přistoupit kliknutím na tlačítko *Vytvořit*. Pro úpravu stávajícího kritéria klikněte na ikonu editace.

.. figure:: images/edicion-criterio.png
   :scale: 50

   Úprava kritérií

Formulář pro editaci kritérií, jak je znázorněno na předchozím obrázku, umožňuje provádět následující operace:

*   **Editovat název kritéria.**
*   **Určit, zda lze přiřadit více hodnot současně nebo pouze jednu hodnotu pro vybraný typ kritéria.** Například zdroj by mohl splňovat dvě kritéria, "svářeč" a "soustružník".
*   **Určit typ kritéria:**

    *   **Obecný:** Kritérium, které lze použít jak pro stroje, tak pro pracovníky.
    *   **Pracovník:** Kritérium, které lze použít pouze pro pracovníky.
    *   **Stroj:** Kritérium, které lze použít pouze pro stroje.

*   **Uvést, zda je kritérium hierarchické.** Někdy je třeba zacházet s kritérii hierarchicky. Například přiřazení kritéria prvku ho automaticky nepřiřadí prvkům, které z něj vycházejí. Jasným příkladem hierarchického kritéria je "umístění". Například osoba označená umístěním "Galicie" bude také patřit do "Španělska".
*   **Uvést, zda je kritérium autorizováno.** Takto uživatelé deaktivují kritéria. Jakmile bylo kritérium vytvořeno a použito v historických datech, nelze jej změnit. Místo toho jej lze deaktivovat, aby se nezobrazovalo ve výběrových seznamech.
*   **Popsat kritérium.**
*   **Přidat nové hodnoty.** Pole pro zadání textu s tlačítkem *Nové kritérium* se nachází v druhé části formuláře.
*   **Upravit názvy stávajících hodnot kritérií.**
*   **Přesouvat hodnoty kritérií nahoru nebo dolů v seznamu aktuálních hodnot kritérií.**
*   **Odebrat hodnotu kritéria ze seznamu.**

Formulář pro správu kritérií se řídí chováním formuláře popsaným v úvodu a nabízí tři akce: *Uložit*, *Uložit a zavřít* a *Zavřít*.
