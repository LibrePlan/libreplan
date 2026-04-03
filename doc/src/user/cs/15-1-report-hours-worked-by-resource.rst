Sestava odpracovaných hodin podle zdroje
########################################

.. contents::

Účel
====

Tato sestava extrahuje seznam úkolů a dobu, kterou jim zdroje věnovaly v zadaném období. Několik filtrů umožňuje uživatelům upřesnit dotaz tak, aby získali pouze požadované informace a vyloučili nepotřebná data.

Vstupní parametry a filtry
==========================

* **Data**.
    * *Typ*: Volitelné.
    * *Dvě datová pole*:
        * *Datum zahájení:* Toto je nejdřívější datum pro zahrnutí pracovních výkazů. Pracovní výkazy s daty dřívějšími než *Datum zahájení* jsou vyloučeny. Pokud je tento parametr ponechán prázdný, pracovní výkazy nejsou filtrovány podle *Data zahájení*.
        * *Datum ukončení:* Toto je nejpozdější datum pro zahrnutí pracovních výkazů. Pracovní výkazy s daty pozdějšími než *Datum ukončení* jsou vyloučeny. Pokud je tento parametr ponechán prázdný, pracovní výkazy nejsou filtrovány podle *Data ukončení*.

*   **Filtr podle pracovníků:**
    *   *Typ:* Volitelné.
    *   *Jak funguje:* Můžete vybrat jednoho nebo více pracovníků pro omezení pracovních výkazů na dobu sledovanou těmito konkrétními pracovníky. Pro přidání pracovníka jako filtru jej vyhledejte ve výběrovém poli a klikněte na tlačítko *Přidat*. Pokud je tento filtr ponechán prázdný, pracovní výkazy jsou načteny bez ohledu na pracovníka.

*   **Filtr podle štítků:**
    *   *Typ:* Volitelné.
    *   *Jak funguje:* Jako filtry můžete přidat jeden nebo více štítků jejich vyhledáním ve výběrovém poli a kliknutím na tlačítko *Přidat*. Tyto štítky slouží k výběru úkolů, které mají být zahrnuty do výsledků při výpočtu hodin jim věnovaných. Tento filtr lze použít na výkazy práce, úkoly, obojí nebo ani jedno.

*   **Filtr podle kritérií:**
    *   *Typ:* Volitelné.
    *   *Jak funguje:* Jako filtry můžete vybrat jedno nebo více kritérií jejich vyhledáním ve výběrovém poli a kliknutím na tlačítko *Přidat*. Tato kritéria slouží k výběru zdrojů, které splňují alespoň jedno z nich. Sestava zobrazí veškerý čas věnovaný zdroji, které splňují jedno z vybraných kritérií.

Výstup
======

Záhlaví
-------

Záhlaví sestavy zobrazuje filtry, které byly nakonfigurovány a použity na aktuální sestavu.

Zápatí
------

Datum, kdy byla sestava vygenerována, je uvedeno v zápatí.

Tělo
----

Tělo sestavy se skládá z několika skupin informací.

*   První úroveň agregace je podle zdroje. Veškerý čas věnovaný zdrojem je zobrazen společně pod záhlavím. Každý zdroj je identifikován:

    *   *Pracovník:* Příjmení, Jméno.
    *   *Stroj:* Název.

    Souhrnný řádek zobrazuje celkový počet hodin odpracovaných zdrojem.

*   Druhá úroveň seskupení je podle *data*. Všechny výkazy z konkrétního zdroje ke stejnému datu jsou zobrazeny společně.

    Souhrnný řádek zobrazuje celkový počet hodin odpracovaných zdrojem k danému datu.

*   Poslední úroveň uvádí pracovní výkazy pracovníka za daný den. Informace zobrazené pro každý řádek pracovního výkazu jsou:

    *   *Kód úkolu:* Kód úkolu, ke kterému jsou sledované hodiny přiřazeny.
    *   *Název úkolu:* Název úkolu, ke kterému jsou sledované hodiny přiřazeny.
    *   *Čas zahájení:* Volitelné. Čas, kdy zdroj začal pracovat na úkolu.
    *   *Čas ukončení:* Volitelné. Čas, kdy zdroj skončil s prací na úkolu k zadanému datu.
    *   *Textová pole:* Volitelné. Pokud má řádek pracovního výkazu textová pole, jsou zde zobrazeny vyplněné hodnoty. Formát je: <Název textového pole>:<Hodnota>
    *   *Štítky:* Závisí na tom, zda má model pracovního výkazu v definici pole štítku. Pokud existuje více štítků, jsou zobrazeny ve stejném sloupci. Formát je: <Název typu štítku>:<Hodnota štítku>
