Sestava práce a postupu projektu
#################################

.. contents::

Účel
====

Tato sestava poskytuje přehled stavu projektů s ohledem na postup i náklady.

Analyzuje aktuální postup každého projektu a porovnává jej s plánovaným postupem a dokončenou prací.

Sestava také zobrazuje několik poměrů souvisejících s náklady projektu a porovnává aktuální výkon s plánovaným výkonem.

Vstupní parametry a filtry
==========================

Existuje několik povinných parametrů:

   *   **Referenční datum:** Toto je datum používané jako referenční bod pro porovnání plánovaného stavu projektu s jeho skutečným výkonem. *Výchozí hodnota tohoto pole je aktuální datum*.

   *   **Typ postupu:** Toto je typ postupu použitý pro měření postupu projektu. Aplikace umožňuje měřit projekt současně různými typy postupu. Typ vybraný uživatelem v rozevírací nabídce se používá pro výpočet dat sestavy. Výchozí hodnota *typu postupu* je *spread*, což je speciální typ postupu, který používá preferovanou metodu měření postupu nastavenou pro každý prvek WBS.

Volitelné parametry jsou:

   *   **Datum zahájení:** Toto je nejdřívější datum zahájení projektů, které mají být zahrnuty do sestavy. Pokud je toto pole prázdné, neexistuje žádné minimální datum zahájení pro projekty.

   *   **Datum ukončení:** Toto je nejpozdější datum ukončení projektů, které mají být zahrnuty do sestavy. Všechny projekty, které skončí po *Datu ukončení*, budou vyloučeny.

   *   **Filtr podle projektů:** Tento filtr umožňuje uživatelům vybrat konkrétní projekty, které mají být zahrnuty do sestavy. Pokud nejsou do filtru přidány žádné projekty, sestava bude zahrnovat všechny projekty v databázi. Pro vyhledání požadovaného projektu je k dispozici prohledávatelná rozevírací nabídka. Projekty jsou do filtru přidávány kliknutím na tlačítko *Přidat*.

Výstup
======

Výstupní formát je následující:

Záhlaví
-------

Záhlaví sestavy zobrazuje následující pole:

   *   **Datum zahájení:** Datum zahájení filtru. Nezobrazuje se, pokud sestava není tímto polem filtrována.
   *   **Datum ukončení:** Datum ukončení filtru. Nezobrazuje se, pokud sestava není tímto polem filtrována.
   *   **Typ postupu:** Typ postupu použitý pro sestavu.
   *   **Projekty:** Toto označuje filtrované projekty, pro které je sestava generována. Zobrazí řetězec *Vše*, když sestava zahrnuje všechny projekty splňující ostatní filtry.
   *   **Referenční datum:** Povinné vstupní referenční datum vybrané pro sestavu.

Zápatí
------

Zápatí zobrazuje datum, kdy byla sestava vygenerována.

Tělo
----

Tělo sestavy se skládá ze seznamu projektů vybraných na základě vstupních filtrů.

Filtry fungují přidáváním podmínek, s výjimkou sady tvořené datovými filtry (*Datum zahájení*, *Datum ukončení*) a filtrem *Filtr podle projektů*. V tomto případě, pokud jsou vyplněny jeden nebo oba datové filtry a filtr *Filtr podle projektů* obsahuje seznam vybraných projektů, má přednost druhý filtr. To znamená, že projekty zahrnuté do sestavy jsou ty poskytnuté filtrem *Filtr podle projektů*, bez ohledu na datové filtry.

Je důležité poznamenat, že postup v sestavě je počítán jako zlomek jednoty v rozmezí 0 a 1.

Pro každý projekt vybraný k zahrnutí do výstupu sestavy jsou zobrazeny následující informace:

   * *Název projektu*.
   * *Celkové hodiny*. Celkové hodiny projektu jsou zobrazeny součtem hodin za každý úkol. Jsou zobrazeny dva typy celkových hodin:
      *   *Odhadované (OE)*. Toto je součet všech odhadovaných hodin ve WBS projektu. Představuje celkový počet hodin odhadovaných pro dokončení projektu.
      *   *Plánované (OP)*. V *LibrePlan* je možné mít dvě různé hodnoty: odhadovaný počet hodin pro úkol (počet hodin původně odhadovaných pro dokončení úkolu) a plánované hodiny (hodiny přidělené v plánu pro dokončení úkolu). Plánované hodiny mohou být stejné, menší nebo větší než odhadované hodiny a jsou stanoveny v pozdější fázi, operaci přiřazení. Proto jsou celkové plánované hodiny pro projekt součtem všech přidělených hodin pro jeho úkoly.
   * *Postup*. Jsou zobrazena tři měřítka týkající se celkového postupu typu zadaného ve vstupním filtru postupu pro každý projekt k referenčnímu datu:
      *   *Naměřený (PN)*. Toto je celkový postup s ohledem na měření postupu s datem dřívějším než *Referenční datum* ve vstupních parametrech sestavy. Jsou zohledněny všechny úkoly a součet je vážen počtem hodin pro každý úkol.
      *   *Imputovaný (PI)*. Toto je postup za předpokladu, že práce pokračuje stejným tempem jako dokončené hodiny pro úkol. Pokud je dokončeno X hodin z Y hodin pro úkol, celkový imputovaný postup je považován za X/Y.
      *   *Plánovaný (PP)*. Toto je celkový postup projektu podle plánovaného harmonogramu k referenčnímu datu. Pokud vše probíhalo přesně podle plánu, naměřený postup by měl být stejný jako plánovaný postup.
   * *Hodiny do data*. Existují dvě pole zobrazující počet hodin do referenčního data ze dvou perspektiv:
      *   *Plánované (HP)*. Toto číslo je součtem hodin přidělených jakémukoli úkolu v projektu s datem menším nebo rovným *Referenčnímu datu*.
      *   *Skutečné (HS)*. Toto číslo je součtem hodin vykázaných v pracovních výkazech pro jakýkoli z úkolů v projektu s datem menším nebo rovným *Referenčnímu datu*.
   * *Rozdíl*. Pod tímto záhlavím je několik metrik souvisejících s náklady:
      *   *Náklady*. Toto je rozdíl v hodinách mezi počtem vynaložených hodin s ohledem na naměřený postup a hodinami dokončenými do referenčního data. Vzorec je: *PN*OP - HS*.
      *   *Plánované*. Toto je rozdíl mezi hodinami vynaloženými podle celkového naměřeného postupu projektu a počtem plánovaných hodin do *Referenčního data*. Měří výhodu nebo zpoždění v čase. Vzorec je: *PN*OP - HP*.
      *   *Poměr nákladů*. Vypočítá se vydělením *PN* / *PI*. Pokud je větší než 1, znamená to, že projekt je v tomto okamžiku ziskový. Pokud je menší než 1, znamená to, že projekt prodělává.
      *   *Plánovaný poměr*. Vypočítá se vydělením *PN* / *PP*. Pokud je větší než 1, znamená to, že projekt je před harmonogramem. Pokud je menší než 1, znamená to, že projekt je za harmonogramem.
