Konektory
#########

.. contents::

Konektory jsou klientské aplikace *LibrePlan*, které lze použít ke komunikaci s (webovými) servery za účelem načítání dat, jejich zpracování a ukládání. V současné době existují tři konektory: konektor JIRA, konektor Tim Enterprise a e-mailový konektor.

Konfigurace
===========

Konektory musí být před použitím správně nakonfigurovány. Lze je nakonfigurovat na obrazovce „Hlavní nastavení" pod záložkou „Konektory".

Obrazovka konektoru zahrnuje:

*   **Rozevírací seznam:** Seznam dostupných konektorů.
*   **Obrazovka pro úpravu vlastností:** Formulář pro úpravu vlastností vybraného konektoru.
*   **Tlačítko pro testování připojení:** Tlačítko pro otestování připojení ke konektoru.

Ze rozevíracího seznamu konektorů vyberte konektor, který chcete konfigurovat. Zobrazí se formulář editoru vlastností pro vybraný konektor. Ve formuláři editoru vlastností můžete podle potřeby měnit hodnoty vlastností a testovat konfigurace pomocí tlačítka „Testovat připojení".

.. NOTE::

   Vlastnosti jsou nakonfigurovány s výchozími hodnotami. Nejdůležitější vlastností je „Aktivováno". Ve výchozím nastavení je nastavena na „N". To znamená, že konektor nebude použit, pokud hodnotu nezměníte na „Y" a změny neuložíte.

Konektor JIRA
=============

JIRA je systém pro sledování problémů a projektů.

Konektor JIRA je aplikace, která lze použít k vyžádání dat webového serveru JIRA pro problémy JIRA a zpracování odpovědi. Požadavek je založen na štítcích JIRA. V JIRA lze štítky použít ke kategorizaci problémů. Požadavek je strukturován takto: načíst všechny problémy kategorizované tímto názvem štítku.

Konektor přijme odpověď, která jsou v tomto případě problémy, a převede je na „Prvky projektů" a „Výkazy práce" *LibrePlan*.

*Konektor JIRA* musí být před použitím správně nakonfigurován.

Konfigurace
-----------

Na obrazovce „Hlavní nastavení" zvolte záložku „Konektory". Na obrazovce konektorů vyberte konektor JIRA z rozevíracího seznamu. Poté se zobrazí obrazovka editoru vlastností.

Na této obrazovce můžete nakonfigurovat následující hodnoty vlastností:

*   **Aktivováno:** Y/N, označující, zda chcete použít konektor JIRA. Výchozí hodnota je „N".
*   **URL serveru:** Absolutní cesta k webovému serveru JIRA.
*   **Uživatelské jméno a heslo:** Přihlašovací údaje uživatele pro autorizaci.
*   **Štítky JIRA: seznam štítků oddělených čárkou nebo URL:** Můžete zadat buď URL štítku nebo seznam štítků oddělených čárkami.
*   **Typ hodin:** Typ pracovních hodin. Výchozí hodnota je „Výchozí".

.. NOTE::

   **Štítky JIRA:** V současné době webový server JIRA nepodporuje poskytování seznamu všech dostupných štítků. Jako alternativní řešení jsme vyvinuli jednoduchý PHP skript, který provádí jednoduchý SQL dotaz v databázi JIRA pro načtení všech různých štítků. Můžete buď použít tento PHP skript jako „URL štítků JIRA" nebo zadat požadované štítky jako text oddělený čárkami v poli „Štítky JIRA".

Nakonec klikněte na tlačítko „Testovat připojení" pro ověření, zda se můžete připojit k webovému serveru JIRA a zda jsou vaše konfigurace správné.

Synchronizace
-------------

Z okna projektu pod „Obecná data" můžete zahájit synchronizaci prvků projektu s problémy JIRA.

Kliknutím na tlačítko „Synchronizovat s JIRA" zahájíte synchronizaci.

*   Pokud se jedná o první synchronizaci, zobrazí se vyskakovací okno (s automaticky doplněným seznamem štítků). V tomto okně můžete vybrat štítek pro synchronizaci a kliknout na tlačítko „Zahájit synchronizaci" pro spuštění procesu synchronizace nebo kliknout na tlačítko „Zrušit" pro jeho zrušení.

*   Pokud je štítek již synchronizován, na obrazovce JIRA se zobrazí datum poslední synchronizace a štítek. V tomto případě se nezobrazí vyskakovací okno pro výběr štítku. Místo toho se proces synchronizace spustí přímo pro zobrazený (již synchronizovaný) štítek.

.. NOTE::

   Vztah mezi „Projektem" a „štítkem" je jedna ku jedné. Pouze jeden štítek lze synchronizovat s jedním „Projektem".

.. NOTE::

   Po úspěšné (re)synchronizaci budou informace zapsány do databáze a obrazovka JIRA bude aktualizována datem poslední synchronizace a štítkem.

(Re)synchronizace probíhá ve dvou fázích:

*   **Fáze 1:** Synchronizace prvků projektu, včetně přiřazení postupu a měření.
*   **Fáze 2:** Synchronizace výkazů práce.

.. NOTE::

   Pokud fáze 1 selže, fáze 2 nebude provedena a žádné informace nebudou zapsány do databáze.

.. NOTE::

   Informace o úspěchu nebo selhání budou zobrazeny ve vyskakovacím okně.

Po úspěšném dokončení synchronizace bude výsledek zobrazen na záložce „Struktura rozdělení práce (úkoly WBS)" obrazovky „Podrobnosti projektu". V tomto rozhraní existují dvě změny oproti standardní WBS:

*   Sloupec „Celkové hodiny úkolu" je neupravitelný (pouze pro čtení), protože synchronizace je jednosměrná. Hodiny úkolu lze aktualizovat pouze na webovém serveru JIRA.
*   Sloupec „Kód" zobrazuje klíče problémů JIRA a jsou také hypertextovými odkazy na problémy JIRA. Kliknutím na požadovaný klíč přejdete na dokument pro daný klíč (problém JIRA).

Plánování
---------

Resynchronizaci problémů JIRA lze také provést prostřednictvím plánovače. Přejděte na obrazovku „Plánování úloh". Na této obrazovce můžete nakonfigurovat úlohu JIRA pro provedení synchronizace. Úloha vyhledá poslední synchronizované štítky v databázi a podle toho je resynchronizuje. Viz také příručka plánovače.

Konektor Tim Enterprise
=======================

Tim Enterprise je nizozemský produkt od společnosti Aenova. Jedná se o webovou aplikaci pro správu času stráveného na projektech a úkolech.

Konektor Tim je aplikace, která lze použít ke komunikaci se serverem Tim Enterprise za účelem:

*   Exportu všech hodin strávených pracovníkem (uživatelem) na projektu, které by mohly být registrovány v Tim Enterprise.
*   Importu všech rozvrhů pracovníka (uživatele) pro efektivní plánování zdrojů.

*Konektor Tim* musí být před použitím správně nakonfigurován.

Konfigurace
-----------

Na obrazovce „Hlavní nastavení" zvolte záložku „Konektory". Na obrazovce konektorů vyberte konektor Tim z rozevíracího seznamu. Poté se zobrazí obrazovka editoru vlastností.

Na této obrazovce můžete nakonfigurovat následující hodnoty vlastností:

*   **Aktivováno:** Y/N, označující, zda chcete použít konektor Tim. Výchozí hodnota je „N".
*   **URL serveru:** Absolutní cesta k serveru Tim Enterprise.
*   **Uživatelské jméno a heslo:** Přihlašovací údaje uživatele pro autorizaci.
*   **Počet dní výkazů práce do Tim:** Počet dní zpět, za které chcete exportovat výkazy práce.
*   **Počet dní rozvrhu z Tim:** Počet dní dopředu, za které chcete importovat rozvrhy.
*   **Faktor produktivity:** Efektivní pracovní hodiny v procentech. Výchozí hodnota je „100 %".
*   **ID oddělení pro import rozvrhu:** ID oddělení oddělená čárkami.

Nakonec klikněte na tlačítko „Testovat připojení" pro ověření, zda se můžete připojit k serveru Tim Enterprise a zda jsou vaše konfigurace správné.

Export
------

Z okna projektu pod „Obecná data" můžete zahájit export výkazů práce na server Tim Enterprise.

Zadejte „Kód produktu Tim" a kliknutím na tlačítko „Exportovat do Tim" zahájíte export.

Konektor Tim přidává spolu s kódem produktu následující pole:

*   Celé jméno pracovníka/uživatele.
*   Datum, kdy pracovník pracoval na úkolu.
*   Úsilí nebo odpracované hodiny na úkolu.
*   Možnost označující, zda má Tim Enterprise aktualizovat registraci nebo vložit novou.

Odpověď Tim Enterprise obsahuje pouze seznam ID záznamů (celá čísla). To ztěžuje zjišťování, co se pokazilo, protože seznam odpovědí obsahuje pouze čísla nesouvisející s poli požadavku. Požadavek na export (registrace v Tim) se předpokládá úspěšným, pokud všechny položky seznamu neobsahují hodnoty „0". Jinak požadavek na export selhal pro ty položky, které obsahují hodnoty „0". Proto nelze zjistit, který požadavek selhal, protože položky seznamu obsahují pouze hodnotu „0". Jediným způsobem, jak to zjistit, je prozkoumat soubor protokolu na serveru Tim Enterprise.

.. NOTE::

   Po úspěšném exportu budou informace zapsány do databáze a obrazovka Tim bude aktualizována datem posledního exportu a kódem produktu.

.. NOTE::

   Informace o úspěchu nebo selhání budou zobrazeny ve vyskakovacím okně.

Plánování exportu
-----------------

Proces exportu lze také provést prostřednictvím plánovače. Přejděte na obrazovku „Plánování úloh". Na této obrazovce můžete nakonfigurovat úlohu exportu Tim. Úloha vyhledá poslední exportované výkazy práce v databázi a podle toho je znovu exportuje. Viz také příručka plánovače.

Import
------

Import rozvrhů funguje pouze s pomocí plánovače. Pro tuto funkci není navrženo žádné uživatelské rozhraní, protože od uživatele nejsou vyžadovány žádné vstupy. Přejděte na obrazovku „Plánování úloh" a nakonfigurujte úlohu importu Tim. Úloha prochází všemi odděleními nakonfigurovanými ve vlastnostech konektoru a importuje všechny rozvrhy pro každé oddělení. Viz také příručka plánovače.

Pro import konektor Tim přidává v požadavku následující pole:

*   **Období:** Období (datum od - datum do), pro které chcete importovat rozvrh. Toto lze poskytnout jako kritérium filtru.
*   **Oddělení:** Oddělení, pro které chcete importovat rozvrh. Oddělení jsou konfigurovatelná.
*   Pole, která vás zajímají (jako informace o osobě, kategorie rozvrhu atd.), která by měl server Tim zahrnout do své odpovědi.

Odpověď importu obsahuje následující pole, která jsou dostatečná pro správu výjimečných dní v *LibrePlan*:

*   **Informace o osobě:** Jméno a síťové jméno.
*   **Oddělení:** Oddělení, ve kterém pracovník pracuje.
*   **Kategorie rozvrhu:** Informace o přítomnosti/nepřítomnosti (Aanwezig/afwezig) pracovníka a důvod (typ výjimky *LibrePlan*) v případě nepřítomnosti pracovníka.
*   **Datum:** Datum přítomnosti/nepřítomnosti pracovníka.
*   **Čas:** Čas zahájení přítomnosti/nepřítomnosti, například 08:00.
*   **Trvání:** Počet hodin přítomnosti/nepřítomnosti pracovníka.

Při převodu odpovědi importu na „Výjimečný den" *LibrePlan* jsou zohledněny následující překlady:

*   Pokud kategorie rozvrhu obsahuje název „Vakantie", bude přeložena jako „DOVOLENÁ ZDROJE".
*   Kategorie rozvrhu „Feestdag" bude přeložena jako „STÁTNÍ SVÁTEK".
*   Všechny ostatní, jako „Jus uren", „PLB uren" atd., by měly být přidány do „Výjimečných dní kalendáře" ručně.

Navíc v odpovědi importu je rozvrh rozdělen na dvě nebo tři části za den: například rozvrh-ráno, rozvrh-odpoledne a rozvrh-večer. *LibrePlan* však umožňuje pouze jeden „Typ výjimky" za den. Konektor Tim je pak zodpovědný za sloučení těchto částí do jednoho typu výjimky. To znamená, že kategorie rozvrhu s nejvyšší dobou trvání je považována za platný typ výjimky, ale celková doba trvání je součtem všech dob trvání těchto částí kategorie.

Na rozdíl od *LibrePlan*, v Tim Enterprise celková doba trvání v případě, kdy je pracovník na dovolené, znamená, že pracovník není po tuto celkovou dobu k dispozici. Avšak v *LibrePlan*, pokud je pracovník na dovolené, celková doba trvání by měla být nulová. Konektor Tim tento překlad také zpracovává.

E-mailový konektor
==================

E-mail je metoda výměny digitálních zpráv od autora jednomu nebo více příjemcům.

E-mailový konektor lze použít k nastavení vlastností připojení serveru Simple Mail Transfer Protocol (SMTP).

*E-mailový konektor* musí být před použitím správně nakonfigurován.

Konfigurace
-----------

Na obrazovce „Hlavní nastavení" zvolte záložku „Konektory". Na obrazovce konektorů vyberte e-mailový konektor z rozevíracího seznamu. Poté se zobrazí obrazovka editoru vlastností.

Na této obrazovce můžete nakonfigurovat následující hodnoty vlastností:

*   **Aktivováno:** Y/N, označující, zda chcete použít e-mailový konektor. Výchozí hodnota je „N".
*   **Protokol:** Typ protokolu SMTP.
*   **Hostitel:** Absolutní cesta k serveru SMTP.
*   **Port:** Port serveru SMTP.
*   **Adresa odesílatele:** E-mailová adresa odesílatele zprávy.
*   **Uživatelské jméno:** Uživatelské jméno pro server SMTP.
*   **Heslo:** Heslo pro server SMTP.

Nakonec klikněte na tlačítko „Testovat připojení" pro ověření, zda se můžete připojit k serveru SMTP a zda jsou vaše konfigurace správné.

Úprava šablony e-mailu
----------------------

Z okna projektu pod „Konfigurace" a poté „Upravit šablony e-mailů" můžete upravovat šablony e-mailů pro zprávy.

Můžete zvolit:

*   **Jazyk šablony:**
*   **Typ šablony:**
*   **Předmět e-mailu:**
*   **Obsah šablony:**

Musíte zadat jazyk, protože webová aplikace bude posílat e-maily uživatelům v jazyce, který si zvolili ve svých preferencích. Musíte zvolit typ šablony. Typ je role uživatele, což znamená, že tento e-mail bude odeslán pouze uživatelům, kteří jsou ve vybrané roli (typu). Musíte nastavit předmět e-mailu. Předmět je stručné shrnutí tématu zprávy. Musíte nastavit obsah e-mailu. Toto jsou veškeré informace, které chcete odeslat uživateli. Existují také některá klíčová slova, která můžete ve zprávě použít; webová aplikace je zpracuje a místo klíčového slova nastaví novou hodnotu.

Plánování e-mailů
-----------------

Odesílání e-mailů lze provést pouze prostřednictvím plánovače. Přejděte na „Konfigurace" a poté na obrazovku „Plánování úloh". Na této obrazovce můžete nakonfigurovat úlohu odesílání e-mailů. Úloha vezme seznam e-mailových oznámení, shromáždí data a odešle je na e-mail uživatele. Viz také příručka plánovače.

.. NOTE::

   Informace o úspěchu nebo selhání budou zobrazeny ve vyskakovacím okně.
