Projekty a prvky projektů
#########################

.. contents::

Projekty představují práci, kterou mají uživatelé programu vykonávat. Každý projekt odpovídá projektu, který společnost nabídne svým zákazníkům.

Projekt se skládá z jednoho nebo více prvků projektu. Každý prvek projektu představuje konkrétní část práce, která má být provedena, a definuje, jak má být práce na projektu plánována a prováděna. Prvky projektu jsou uspořádány hierarchicky, bez omezení hloubky hierarchie. Tato hierarchická struktura umožňuje dědění určitých funkcí, jako jsou štítky.

Následující sekce popisují operace, které mohou uživatelé provádět s projekty a prvky projektů.

Projekty
========

Projekt představuje projekt nebo práci vyžádanou zákazníkem od společnosti. Projekt identifikuje projekt v rámci plánování společnosti. Na rozdíl od komplexních správních programů vyžaduje LibrePlan pro projekt pouze určité klíčové informace. Tyto informace jsou:

*   **Název projektu:** Název projektu.
*   **Kód projektu:** Jedinečný kód projektu.
*   **Celková částka projektu:** Celková finanční hodnota projektu.
*   **Odhadované datum zahájení:** Plánované datum zahájení projektu.
*   **Datum ukončení:** Plánované datum dokončení projektu.
*   **Odpovědná osoba:** Osoba odpovědná za projekt.
*   **Popis:** Popis projektu.
*   **Přiřazený kalendář:** Kalendář přiřazený k projektu.
*   **Automatické generování kódů:** Nastavení pro pokyn systému automaticky generovat kódy pro prvky projektu a skupiny hodin.
*   **Přednost závislostí nebo omezení:** Uživatelé mohou zvolit, zda mají v případě konfliktů přednost závislosti nebo omezení.

Kompletní projekt však zahrnuje také další přidružené entity:

*   **Hodiny přiřazené projektu:** Celkový počet hodin přidělených projektu.
*   **Pokrok přiřazený projektu:** Dosažený pokrok projektu.
*   **Štítky:** Štítky přiřazené projektu.
*   **Kritéria přiřazená projektu:** Kritéria přidružená k projektu.
*   **Materiály:** Materiály potřebné pro projekt.
*   **Formuláře kvality:** Formuláře kvality přidružené k projektu.

Vytvoření nebo úprava projektu lze provést z několika míst v programu:

*   **Ze "Seznamu projektů" v přehledu společnosti:**

    *   **Úprava:** Klikněte na tlačítko editace požadovaného projektu.
    *   **Vytvoření:** Klikněte na "Nový projekt".

*   **Z projektu v Ganttově diagramu:** Přepněte na zobrazení podrobností projektu.

Při editaci projektu mají uživatelé přístup k následujícím záložkám:

*   **Editace podrobností projektu:** Tato obrazovka umožňuje uživatelům editovat základní podrobnosti projektu:

    *   Název
    *   Kód
    *   Odhadované datum zahájení
    *   Datum ukončení
    *   Odpovědná osoba
    *   Zákazník
    *   Popis

    .. figure:: images/order-edition.png
       :scale: 50

       Editace projektů

*   **Seznam prvků projektu:** Tato obrazovka umožňuje uživatelům provádět několik operací s prvky projektu:

    *   Vytvářet nové prvky projektu.
    *   Povýšit prvek projektu o jednu úroveň nahoru v hierarchii.
    *   Degradovat prvek projektu o jednu úroveň dolů v hierarchii.
    *   Odsadit prvek projektu (přesunout ho dolů v hierarchii).
    *   Zrušit odsazení prvku projektu (přesunout ho nahoru v hierarchii).
    *   Filtrovat prvky projektu.
    *   Mazat prvky projektu.
    *   Přesunout prvek v hierarchii přetažením.

    .. figure:: images/order-elements-list.png
       :scale: 40

       Seznam prvků projektu

*   **Přiřazené hodiny:** Tato obrazovka zobrazuje celkový počet hodin přiřazených projektu a seskupuje hodiny zadané v prvcích projektu.

    .. figure:: images/order-assigned-hours.png
       :scale: 50

       Přiřazení hodin přiřazených projektu pracovníky

*   **Pokrok:** Tato obrazovka umožňuje uživatelům přiřazovat typy pokroku a zadávat měření pokroku pro projekt. Více podrobností najdete v části "Pokrok".

*   **Štítky:** Tato obrazovka umožňuje uživatelům přiřazovat štítky k projektu a zobrazovat dříve přiřazené přímé a nepřímé štítky. Podrobný popis správy štítků najdete v následující části o editaci prvků projektu.

    .. figure:: images/order-labels.png
       :scale: 35

       Štítky projektu

*   **Kritéria:** Tato obrazovka umožňuje uživatelům přiřazovat kritéria, která se budou vztahovat na všechny úkoly v rámci projektu. Tato kritéria budou automaticky aplikována na všechny prvky projektu, s výjimkou těch, které byly explicitně zrušeny. Lze také zobrazit skupiny hodin prvků projektu, které jsou seskupeny podle kritérií, což uživatelům umožňuje identifikovat kritéria potřebná pro projekt.

    .. figure:: images/order-criterions.png
       :scale: 50

       Kritéria projektu

*   **Materiály:** Tato obrazovka umožňuje uživatelům přiřazovat materiály k projektům. Materiály lze vybírat z dostupných kategorií materiálů v programu. Správa materiálů probíhá takto:

    *   V dolní části obrazovky vyberte záložku "Hledat materiály".
    *   Zadejte text pro vyhledávání materiálů nebo vyberte kategorie, pro které chcete materiály najít.
    *   Systém vyfiltruje výsledky.
    *   Vyberte požadované materiály (více materiálů lze vybrat stisknutím klávesy "Ctrl").
    *   Klikněte na "Přiřadit".
    *   Systém zobrazí seznam materiálů již přiřazených projektu.
    *   Vyberte jednotky a stav, které mají být přiřazeny projektu.
    *   Klikněte na "Uložit" nebo "Uložit a pokračovat".
    *   Pro správu příjmu materiálů klikněte na "Rozdělit" pro změnu stavu částečného množství materiálu.

    .. figure:: images/order-material.png
       :scale: 50

       Materiály přidružené k projektu

*   **Kvalita:** Uživatelé mohou k projektu přiřadit formulář kvality. Tento formulář je poté vyplněn, aby bylo zajištěno, že určité činnosti spojené s projektem byly provedeny. Podrobnosti o správě formulářů kvality najdete v následující části o editaci prvků projektu.

    .. figure:: images/order-quality.png
       :scale: 50

       Formulář kvality přidružený k projektu

Editace prvků projektu
======================

Prvky projektu se editují ze záložky "Seznam prvků projektu" kliknutím na ikonu editace. Tím se otevře nová obrazovka, kde mohou uživatelé:

*   Editovat informace o prvku projektu.
*   Zobrazovat hodiny přiřazené prvkům projektu.
*   Spravovat pokrok prvků projektu.
*   Spravovat štítky projektu.
*   Spravovat kritéria vyžadovaná prvkem projektu.
*   Spravovat materiály.
*   Spravovat formuláře kvality.

Následující podkategorie podrobně popisují každou z těchto operací.

Editace informací o prvku projektu
------------------------------------

Editace informací o prvku projektu zahrnuje úpravu následujících podrobností:

*   **Název prvku projektu:** Název prvku projektu.
*   **Kód prvku projektu:** Jedinečný kód prvku projektu.
*   **Datum zahájení:** Plánované datum zahájení prvku projektu.
*   **Odhadované datum ukončení:** Plánované datum dokončení prvku projektu.
*   **Celkové hodiny:** Celkový počet hodin přidělených prvku projektu. Tyto hodiny lze vypočítat ze přidaných skupin hodin nebo zadat přímo. Při přímém zadání musí být hodiny rozděleny mezi skupiny hodin a v případě, že procenta neodpovídají počátečním procentům, musí být vytvořena nová skupina hodin.
*   **Skupiny hodin:** K prvku projektu lze přidat jednu nebo více skupin hodin. **Účelem těchto skupin hodin** je definovat požadavky na zdroje, které budou přiděleny k vykonání práce.
*   **Kritéria:** Lze přidat kritéria, která musí být splněna, aby bylo umožněno obecné přidělení pro prvek projektu.

.. figure:: images/order-element-edition.png
   :scale: 50

   Editace prvků projektu

Zobrazení hodin přiřazených prvkům projektu
---------------------------------------------

Záložka "Přiřazené hodiny" umožňuje uživatelům zobrazit pracovní výkazy přidružené k prvku projektu a zjistit, kolik z odhadovaných hodin již bylo splněno.

.. figure:: images/order-element-hours.png
   :scale: 50

   Hodiny přiřazené prvkům projektu

Obrazovka je rozdělena na dvě části:

*   **Seznam pracovních výkazů:** Uživatelé mohou zobrazit seznam pracovních výkazů přidružených k prvku projektu, včetně data a času, zdroje a počtu hodin věnovaných úkolu.
*   **Využití odhadovaných hodin:** Systém vypočítá celkový počet hodin věnovaných úkolu a porovná je s odhadovanými hodinami.

Správa pokroku prvků projektu
------------------------------

Zadávání typů pokroku a správa pokroku prvků projektu je popsána v kapitole "Pokrok".

Správa štítků projektu
-----------------------

Štítky, jak jsou popsány v kapitole o štítcích, umožňují uživatelům kategorizovat prvky projektu. To uživatelům umožňuje seskupovat informace o plánování nebo projektech na základě těchto štítků.

Uživatelé mohou přiřadit štítky přímo k prvku projektu nebo k prvku projektu vyšší úrovně v hierarchii. Jakmile je štítek přiřazen kteroukoli z těchto metod, prvek projektu a Související plánovací úkol jsou přidruženy ke štítku a lze je použít pro následné filtrování.

.. figure:: images/order-element-tags.png
   :scale: 50

   Přiřazení štítků prvkům projektu

Jak je znázorněno na obrázku, uživatelé mohou ze záložky **Štítky** provádět následující akce:

*   **Zobrazit zděděné štítky:** Zobrazit štítky přidružené k prvku projektu, které byly zděděny z prvku projektu vyšší úrovně. Plánovací úkol přidružený ke každému prvku projektu má stejné přidružené štítky.
*   **Zobrazit přímo přiřazené štítky:** Zobrazit štítky přímo přidružené k prvku projektu pomocí formuláře přiřazení štítků nižší úrovně.
*   **Přiřadit existující štítky:** Přiřadit štítky jejich vyhledáváním mezi dostupnými štítky ve formuláři pod seznamem přímých štítků. Pro vyhledání štítku klikněte na ikonu lupy nebo zadejte první písmena štítku do textového pole pro zobrazení dostupných možností.
*   **Vytvořit a přiřadit nové štítky:** Vytvořit nové štítky přidružené k existujícímu typu štítku z tohoto formuláře. Za tímto účelem vyberte typ štítku a zadejte hodnotu štítku pro vybraný typ. Systém automaticky vytvoří štítek a přiřadí jej k prvku projektu po kliknutí na "Vytvořit a přiřadit".

Správa kritérií vyžadovaných prvkem projektu a skupinami hodin
--------------------------------------------------------------

Jak projekt, tak prvek projektu mohou mít přiřazena kritéria, která musí být splněna, aby mohla být práce provedena. Kritéria mohou být přímá nebo nepřímá:

*   **Přímá kritéria:** Jsou přiřazena přímo prvku projektu. Jedná se o kritéria vyžadovaná skupinami hodin prvku projektu.
*   **Nepřímá kritéria:** Jsou přiřazena prvkům projektu vyšší úrovně v hierarchii a jsou dědičná editovaným prvkem.

Kromě požadovaných kritérií lze definovat jednu nebo více skupin hodin, které jsou součástí prvku projektu. To závisí na tom, zda prvek projektu obsahuje jiné prvky projektu jako podřízené uzly nebo zda jde o listový uzel. V prvním případě lze informace o hodinách a skupinách hodin pouze zobrazovat. Listové uzly však lze editovat. Listové uzly fungují takto:

*   Systém vytvoří výchozí skupinu hodin přidruženou k prvku projektu. Podrobnosti, které lze pro skupinu hodin upravit, jsou:

    *   **Kód:** Kód skupiny hodin (pokud se negeneruje automaticky).
    *   **Typ kritéria:** Uživatelé mohou zvolit přiřazení kritéria stroje nebo pracovníka.
    *   **Počet hodin:** Počet hodin ve skupině hodin.
    *   **Seznam kritérií:** Kritéria, která mají být aplikována na skupinu hodin. Pro přidání nových kritérií klikněte na "Přidat kritérium" a vyberte jedno z vyhledávače, který se zobrazí po kliknutí na tlačítko.

*   Uživatelé mohou přidávat nové skupiny hodin s odlišnými vlastnostmi od předchozích skupin hodin. Například prvek projektu může vyžadovat svářeče (30 hodin) a lakýrníka (40 hodin).

.. figure:: images/order-element-criterion.png
   :scale: 50

   Přiřazení kritérií prvkům projektu

Správa materiálů
-----------------

Materiály jsou v projektech spravovány jako seznam přidružený ke každému prvku projektu nebo k projektu obecně. Seznam materiálů obsahuje následující pole:

*   **Kód:** Kód materiálu.
*   **Datum:** Datum přidružené k materiálu.
*   **Jednotky:** Požadovaný počet jednotek.
*   **Typ jednotky:** Typ jednotky používaný pro měření materiálu.
*   **Jednotková cena:** Cena za jednotku.
*   **Celková cena:** Celková cena (vypočítaná vynásobením jednotkové ceny počtem jednotek).
*   **Kategorie:** Kategorie, do které materiál patří.
*   **Stav:** Stav materiálu (např. Přijato, Vyžádáno, Čeká, Zpracovává se, Zrušeno).

Práce s materiály probíhá takto:

*   V prvku projektu vyberte záložku "Materiály".
*   Systém zobrazí dvě podzáložky: "Materiály" a "Hledat materiály".
*   Pokud prvek projektu nemá přiřazeny žádné materiály, první záložka bude prázdná.
*   V levé dolní části okna klikněte na "Hledat materiály".
*   Systém zobrazí seznam dostupných kategorií a přidružených materiálů.

.. figure:: images/order-element-material-search.png
   :scale: 50

   Vyhledávání materiálů

*   Vyberte kategorie pro upřesnění vyhledávání materiálů.
*   Systém zobrazí materiály náležející do vybraných kategorií.
*   Ze seznamu materiálů vyberte materiály, které mají být přiřazeny prvku projektu.
*   Klikněte na "Přiřadit".
*   Systém zobrazí vybraný seznam materiálů na záložce "Materiály" s novými poli k vyplnění.

.. figure:: images/order-element-material-assign.png
   :scale: 50

   Přiřazení materiálů prvkům projektu

*   Vyberte jednotky, stav a datum pro přiřazené materiály.

Pro následné sledování materiálů je možné změnit stav skupiny jednotek přijatého materiálu. Postupuje se takto:

*   Klikněte na tlačítko "Rozdělit" v seznamu materiálů napravo od každého řádku.
*   Vyberte počet jednotek, na které má být řádek rozdělen.
*   Program zobrazí dva řádky s rozděleným materiálem.
*   Změňte stav řádku obsahujícího materiál.

Výhodou použití tohoto nástroje pro dělení je možnost přijímat částečné dodávky materiálu, aniž by bylo nutné čekat na celou dodávku pro označení jako přijatou.

Správa formulářů kvality
--------------------------

Některé prvky projektu vyžadují certifikaci, že určité úkoly byly splněny, než mohou být označeny jako dokončené. Proto program obsahuje formuláře kvality, které se skládají ze seznamu otázek, které jsou považovány za důležité, pokud jsou zodpovězeny kladně.

Je důležité si uvědomit, že formulář kvality musí být vytvořen předem, aby mohl být přiřazen prvku projektu.

Správa formulářů kvality:

*   Přejděte na záložku "Formuláře kvality".

    .. figure:: images/order-element-quality.png
       :scale: 50

       Přiřazení formulářů kvality prvkům projektu

*   Program má vyhledávač formulářů kvality. Existují dva typy formulářů kvality: podle prvku nebo podle procenta.

    *   **Prvek:** Každý prvek je nezávislý.
    *   **Procento:** Každá otázka zvyšuje pokrok prvku projektu o procento. Procenta musí být schopna sečíst na 100 %.

*   Vyberte jeden z formulářů vytvořených v administračním rozhraní a klikněte na "Přiřadit".
*   Program přiřadí zvolený formulář ze seznamu formulářů přiřazených prvku projektu.
*   Klikněte na tlačítko "Editovat" u prvku projektu.
*   Program zobrazí otázky formuláře kvality v dolním seznamu.
*   Označte jako splněné otázky, které byly dokončeny.

    *   Pokud je formulář kvality postaven na procentech, otázky se zodpovídají v pořadí.
    *   Pokud je formulář kvality postaven na prvcích, otázky lze zodpovídat v libovolném pořadí.
