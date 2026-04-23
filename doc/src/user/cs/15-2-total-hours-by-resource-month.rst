Sestava celkových odpracovaných hodin podle zdroje v měsíci
############################################################

.. contents::

Účel
====

Tato sestava poskytuje celkový počet hodin odpracovaných každým zdrojem v daném měsíci. Tyto informace mohou být užitečné pro stanovení přesčasů pracovníků nebo, v závislosti na organizaci, počtu hodin, za které má být každý zdroj odměněn.

Aplikace sleduje pracovní výkazy jak pro pracovníky, tak pro stroje. U strojů sestava sčítá počet hodin jejich provozu v průběhu měsíce.

Vstupní parametry a filtry
==========================

Pro vygenerování této sestavy musí uživatelé zadat rok a měsíc, pro které chtějí načíst celkový počet hodin odpracovaných každým zdrojem.

Výstup
======

Výstupní formát je následující:

Záhlaví
-------

Záhlaví sestavy zobrazuje:

   *   *Rok*, ke kterému se data v sestavě vztahují.
   *   *Měsíc*, ke kterému se data v sestavě vztahují.

Zápatí
------

Zápatí zobrazuje datum, kdy byla sestava vygenerována.

Tělo
----

Datová část sestavy se skládá z jedné tabulky se dvěma sloupci:

   *   Jeden sloupec označený **Název** pro název zdroje.
   *   Jeden sloupec označený **Hodiny** s celkovým počtem hodin odpracovaných zdrojem v daném řádku.

Na konci je řádek, který agreguje celkový počet hodin odpracovaných všemi zdroji ve zadaném *měsíci* a *roce*.
