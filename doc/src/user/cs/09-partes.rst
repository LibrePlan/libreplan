Pracovní výkazy
###############

.. contents::

Pracovní výkazy umožňují sledování hodin, které zdroje věnují úkolům, ke kterým jsou přiřazeny.

Program umožňuje uživatelům konfigurovat nové formuláře pro zadávání věnovaných hodin a specifikovat pole, která chtějí v těchto formulářích zobrazit. To umožňuje začlenění výkazů z úkolů provedených pracovníky a sledování jejich aktivity.

Než mohou uživatelé přidat položky pro zdroje, musí definovat alespoň jeden typ pracovního výkazu. Tento typ definuje strukturu výkazu včetně všech řádků, které se do něj přidávají. Uživatelé mohou v systému vytvářet libovolný počet typů pracovních výkazů.

Typy pracovních výkazů
======================

Pracovní výkaz se skládá z řady polí, která jsou společná pro celý výkaz, a ze sady řádků pracovního výkazu se specifickými hodnotami pro pole definovaná v každém řádku. Například zdroje a úkoly jsou společné pro všechny výkazy. Mohou však existovat jiná nová pole, například „incidenty", která nejsou vyžadována u všech typů výkazů.

Uživatelé mohou konfigurovat různé typy pracovních výkazů, aby si společnost mohla navrhnout své výkazy tak, aby vyhovovaly jejím specifickým potřebám:

.. figure:: images/work-report-types.png
   :scale: 40

   Typy pracovních výkazů

Správa typů pracovních výkazů umožňuje uživatelům konfigurovat tyto typy a přidávat nová textová pole nebo volitelné značky. Na první záložce pro úpravu typů pracovních výkazů je možné konfigurovat typ povinných atributů (zda se vztahují na celý výkaz nebo jsou specifikovány na úrovni řádků) a přidávat nová volitelná pole.

Povinná pole, která musí být ve všech pracovních výkazech, jsou následující:

*   **Název a kód:** Identifikační pole pro název typu pracovního výkazu a jeho kód.
*   **Datum:** Pole pro datum výkazu.
*   **Zdroj:** Pracovník nebo stroj uvedený ve výkazu nebo řádku pracovního výkazu.
*   **Prvek objednávky:** Kód prvku objednávky, ke kterému je přiřazena provedená práce.
*   **Správa hodin:** Určuje politiku přiřazování hodin, která se má použít:

    *   **Podle přiřazených hodin:** Hodiny jsou přiřazeny na základě přiřazených hodin.
    *   **Podle začátku a konce pracovní doby:** Hodiny jsou vypočítány na základě začátku a konce pracovní doby.
    *   **Podle počtu hodin a rozsahu začátku a konce:** Jsou povoleny odchylky a počet hodin má přednost.

Uživatelé mohou do výkazů přidávat nová pole:

*   **Typ značky:** Uživatelé mohou požádat systém, aby při vyplňování pracovního výkazu zobrazil značku. Například typ značky klienta, pokud uživatel chce do každého výkazu zadat klienta, pro kterého byla práce provedena.
*   **Volná pole:** Pole, do kterých lze do pracovního výkazu volně zadávat text.

.. figure:: images/work-report-type.png
   :scale: 50

   Vytvoření typu pracovního výkazu s personalizovanými poli

Uživatelé mohou konfigurovat pole data, zdroje a prvku objednávky tak, aby se zobrazovala v záhlaví výkazu, což znamená, že se vztahují na celý výkaz, nebo je lze přidat do každého z řádků.

Nakonec lze do záhlaví pracovního výkazu nebo do každého řádku pomocí polí „Doplňkový text" a „Typ značky" přidávat nová doplňková textová pole nebo značky k existujícím. Uživatelé mohou konfigurovat pořadí, ve kterém mají být tyto prvky zadávány, na záložce „Správa doplňkových polí a značek".

Seznam pracovních výkazů
========================

Po nakonfigurování formátu výkazů, které mají být začleněny do systému, mohou uživatelé zadat podrobnosti do vytvořeného formuláře podle struktury definované v příslušném typu pracovního výkazu. K tomu musí uživatelé postupovat takto:

*   Kliknout na tlačítko „Nový pracovní výkaz" přidružené k požadovanému výkazu ze seznamu typů pracovních výkazů.
*   Program poté zobrazí výkaz na základě konfigurací daných pro daný typ. Viz následující obrázek.

.. figure:: images/work-report-type.png
   :scale: 50

   Struktura pracovního výkazu na základě typu

*   Vybrat všechna pole zobrazená pro výkaz:

    *   **Zdroj:** Pokud bylo vybráno záhlaví, zdroj se zobrazí pouze jednou. Alternativně je pro každý řádek výkazu nutné zvolit zdroj.
    *   **Kód úkolu:** Kód úkolu, ke kterému je pracovní výkaz přiřazen. Podobně jako u ostatních polí, pokud je pole v záhlaví, hodnota se zadá jednou nebo tolikrát, kolikrát je potřeba na řádcích výkazu.
    *   **Datum:** Datum výkazu nebo každého řádku v závislosti na tom, zda je konfigurováno záhlaví nebo řádek.
    *   **Počet hodin:** Počet pracovních hodin v projektu.
    *   **Čas začátku a konce:** Čas začátku a konce práce pro výpočet konečných pracovních hodin. Toto pole se zobrazí pouze v případě politik přiřazování hodin „Podle začátku a konce pracovní doby" a „Podle počtu hodin a rozsahu začátku a konce."
    *   **Typ hodin:** Umožňuje uživatelům zvolit typ hodiny, např. „Standardní", „Přesčas" atd.

*   Kliknout na „Uložit" nebo „Uložit a pokračovat."
