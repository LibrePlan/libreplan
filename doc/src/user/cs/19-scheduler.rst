Plánovač
########

.. contents::

Plánovač je navržen pro dynamické plánování úloh. Je vyvinut pomocí *Spring Framework Quartz scheduler*.

Pro efektivní použití tohoto plánovače musí být nejprve vytvořeny úlohy (úlohy Quartz), které mají být naplánovány. Tyto úlohy pak lze přidat do databáze, protože všechny úlohy, které mají být naplánovány, jsou uloženy v databázi.

Když se plánovač spustí, načte z databáze úlohy, které mají být naplánovány nebo odstraněny ze seznamu plánovaných, a podle toho je naplánuje nebo odstraní. Poté lze úlohy dynamicky přidávat, aktualizovat nebo odstraňovat pomocí uživatelského rozhraní ``Plánování úloh``.

.. NOTE::
   Plánovač se spustí při spuštění webové aplikace LibrePlan a zastaví se při zastavení aplikace.

.. NOTE::
   Tento plánovač podporuje pro plánování úloh pouze ``výrazy cron``.

Kritéria, která plánovač používá při svém spuštění pro naplánování nebo odstranění úloh, jsou následující:

Pro všechny úlohy:

* Naplánovat

  * Úloha má *konektor* a *konektor* je aktivován a úloha smí být naplánována.
  * Úloha nemá *konektor* a smí být naplánována.

* Odstranit

  * Úloha má *konektor* a *konektor* není aktivován.
  * Úloha má *konektor* a *konektor* je aktivován, ale úloha nesmí být naplánována.
  * Úloha nemá *konektor* a nesmí být naplánována.

.. NOTE::
   Úlohy nelze přeplánovat ani odebrat ze seznamu plánovaných, pokud jsou právě spuštěny.

Zobrazení seznamu plánování úloh
================================

Zobrazení ``Seznam plánování úloh`` umožňuje uživatelům:

*   Přidat novou úlohu.
*   Upravit existující úlohu.
*   Odebrat úlohu.
*   Spustit proces ručně.

Přidat nebo upravit úlohu
=========================

Z pohledu ``Seznamu plánování úloh`` klikněte na:

*   ``Vytvořit`` pro přidání nové úlohy, nebo
*   ``Upravit`` pro úpravu vybrané úlohy.

Obě akce otevřou formulář pro vytvoření/úpravu ``úlohy``. ``Formulář`` zobrazuje následující vlastnosti:

*   Pole:

    *   **Skupina úloh:** Název skupiny úloh.
    *   **Název úlohy:** Název úlohy.
    *   **Výraz cron:** Pole pouze pro čtení s tlačítkem ``Upravit`` pro otevření vstupního okna ``výrazu cron``.
    *   **Název třídy úlohy:** ``Rozevírací seznam`` pro výběr úlohy (existující úlohy).
    *   **Konektor:** ``Rozevírací seznam`` pro výběr konektoru. Není povinné.
    *   **Naplánovat:** Zaškrtávací políčko pro označení, zda tuto úlohu naplánovat.

*   Tlačítka:

    *   **Uložit:** Pro uložení nebo aktualizaci úlohy v databázi i v plánovači. Uživatel je poté přesměrován zpět do ``Zobrazení seznamu plánování úloh``.
    *   **Uložit a pokračovat:** Stejné jako „Uložit", ale uživatel není přesměrován zpět do ``Zobrazení seznamu plánování úloh``.
    *   **Zrušit:** Nic se neuloží a uživatel je přesměrován zpět do ``Zobrazení seznamu plánování úloh``.

*   A část s nápovědou ohledně syntaxe výrazu cron.

Vyskakovací okno výrazu cron
----------------------------

Pro správné zadání ``výrazu cron`` se používá vyskakovací formulář ``výrazu cron``. V tomto formuláři můžete zadat požadovaný ``výraz cron``. Viz také nápověda k ``výrazu cron``. Pokud zadáte neplatný ``výraz cron``, budete o tom okamžitě upozorněni.

Odebrat úlohu
=============

Kliknutím na tlačítko ``Odebrat`` smažete úlohu z databáze i z plánovače. Úspěch nebo selhání této akce bude zobrazeno.

Spustit úlohu ručně
===================

Jako alternativu k čekání na spuštění úlohy podle plánu můžete kliknutím na toto tlačítko spustit proces přímo. Poté budou informace o úspěchu nebo selhání zobrazeny ve ``vyskakovacím okně``.
