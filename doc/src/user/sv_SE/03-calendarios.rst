Kalendrar
#########

.. contents::

Kalendrar är entiteter i programmet som definierar resursters arbetskapacitet. En kalender består av en serie dagar under året, där varje dag är uppdelad i tillgängliga arbetstimmar.

En allmän helgdag kan till exempel ha 0 tillgängliga arbetstimmar. Omvänt kan en typisk arbetsdag ha 8 timmar betecknade som tillgänglig arbetstid.

Det finns två primära sätt att definiera antalet arbetstimmar på en dag:

*   **Per veckodag:** Den här metoden fastställer ett standardantal arbetstimmar för varje dag i veckan. Måndagar kan till exempel vanligtvis ha 8 arbetstimmar.
*   **Via undantag:** Den här metoden tillåter specifika avvikelser från standardschemat för veckodagar. Till exempel kan måndagen den 30 januari ha 10 arbetstimmar, vilket åsidosätter det vanliga måndagsschemat.

Kalenderadministration
=======================

Kalendersystemet är hierarkiskt, vilket gör att du kan skapa baskalendrar och sedan härleda nya kalendrar från dem, vilket bildar en trädstruktur. En kalender som härleds från en kalender på högre nivå ärver dess dagliga scheman och undantag om de inte uttryckligen ändras. För att effektivt hantera kalendrar är det viktigt att förstå följande begrepp:

*   **Dagsunabhängighet:** Varje dag behandlas oberoende, och varje år har sin egen uppsättning dagar. Om den 8 december 2009 till exempel är en allmän helgdag innebär det inte automatiskt att den 8 december 2010 också är en allmän helgdag.
*   **Veckodagsbaserade arbetsdagar:** Standardarbetsdagar baseras på veckodagar. Om måndagar till exempel vanligtvis har 8 arbetstimmar, kommer alla måndagar i alla veckor under alla år att ha 8 tillgängliga timmar om inte ett undantag definieras.
*   **Undantag och undantagsperioder:** Du kan definiera undantag eller undantagsperioder för att avvika från standardschemat för veckodagar. Du kan till exempel ange en enstaka dag eller ett datumintervall med ett annat antal tillgängliga arbetstimmar än den allmänna regeln för dessa veckodagar.

.. figure:: images/calendar-administration.png
   :scale: 50

   Kalenderadministration

Kalenderadministration är tillgänglig via menyn "Administration". Därifrån kan användare utföra följande åtgärder:

1.  Skapa en ny kalender från grunden.
2.  Skapa en kalender härledd från en befintlig.
3.  Skapa en kalender som en kopia av en befintlig.
4.  Redigera en befintlig kalender.

Skapa en ny kalender
---------------------

För att skapa en ny kalender, klicka på knappen "Skapa". Systemet visar ett formulär där du kan konfigurera följande:

*   **Välj fliken:** Välj den flik du vill arbeta med:

    *   **Markera undantag:** Definiera undantag från standardschemat.
    *   **Arbetstimmar per dag:** Definiera standardarbetstimmarna för varje veckodag.

*   **Markera undantag:** Om du väljer alternativet "Markera undantag" kan du:

    *   Välja en specifik dag i kalendern.
    *   Välja typ av undantag. De tillgängliga typerna är: helgdag, sjukdom, strejk, allmän helgdag och arbetande helgdag.
    *   Välja slutdatum för undantagsperioden. (Det här fältet behöver inte ändras för endagsundantag.)
    *   Definiera antalet arbetstimmar under dagarna i undantagsperioden.
    *   Ta bort tidigare definierade undantag.

*   **Arbetstimmar per dag:** Om du väljer alternativet "Arbetstimmar per dag" kan du:

    *   Definiera tillgängliga arbetstimmar för varje veckodag (måndag, tisdag, onsdag, torsdag, fredag, lördag och söndag).
    *   Definiera olika veckovisa timfördelningar för framtida perioder.
    *   Ta bort tidigare definierade timfördelningar.

Dessa alternativ gör att användare kan anpassa kalendrar fullständigt efter sina specifika behov. Klicka på knappen "Spara" för att lagra eventuella ändringar som gjorts i formuläret.

.. figure:: images/calendar-edition.png
   :scale: 50

   Redigera kalendrar

.. figure:: images/calendar-exceptions.png
   :scale: 50

   Lägga till ett undantag i en kalender

Skapa härledda kalendrar
-------------------------

En härledd kalender skapas baserat på en befintlig kalender. Den ärver alla funktioner från den ursprungliga kalendern, men du kan ändra den för att inkludera olika alternativ.

Ett vanligt användningsfall för härledda kalendrar är när du har en allmän kalender för ett land, till exempel Spanien, och du behöver skapa en härledd kalender för att inkludera ytterligare allmänna helgdagar specifika för en region, till exempel Galicien.

Det är viktigt att notera att eventuella ändringar i den ursprungliga kalendern automatiskt sprids till den härledda kalendern, om inte ett specifikt undantag har definierats i den härledda kalendern. Till exempel kan den spanska kalendern ha en 8-timmars arbetsdag den 17 maj. Men kalendern för Galicien (en härledd kalender) kan sakna arbetstimmar samma dag eftersom det är en regional allmän helgdag. Om den spanska kalendern senare ändras till att ha 4 tillgängliga arbetstimmar per dag för veckan den 17 maj, kommer den galiciska kalendern också att ändras till att ha 4 tillgängliga arbetstimmar för varje dag den veckan, förutom den 17 maj, som förblir en icke-arbetsdag på grund av det definierade undantaget.

.. figure:: images/calendar-create-derived.png
   :scale: 50

   Skapa en härledd kalender

För att skapa en härledd kalender:

*   Gå till menyn *Administration*.
*   Klicka på alternativet *Kalenderadministration*.
*   Välj den kalender du vill använda som bas för den härledda kalendern och klicka på knappen "Skapa".
*   Systemet visar ett redigeringsformulär med samma egenskaper som formuläret som används för att skapa en kalender från grunden, förutom att de föreslagna undantagen och arbetstimmarna per veckodag baseras på den ursprungliga kalendern.

Skapa en kalender genom kopiering
----------------------------------

En kopierad kalender är en exakt kopia av en befintlig kalender. Den ärver alla funktioner från den ursprungliga kalendern, men du kan ändra den oberoende.

Den viktigaste skillnaden mellan en kopierad kalender och en härledd kalender är hur de påverkas av ändringar i originalet. Om den ursprungliga kalendern ändras förblir den kopierade kalendern oförändrad. Härledda kalendrar påverkas dock av ändringar i originalet, om inte ett undantag definieras.

Ett vanligt användningsfall för kopierade kalendrar är när du har en kalender för en plats, till exempel "Pontevedra", och du behöver en liknande kalender för en annan plats, till exempel "A Coruña", där de flesta funktioner är desamma. Ändringar i en kalender bör dock inte påverka den andra.

För att skapa en kopierad kalender:

*   Gå till menyn *Administration*.
*   Klicka på alternativet *Kalenderadministration*.
*   Välj den kalender du vill kopiera och klicka på knappen "Skapa".
*   Systemet visar ett redigeringsformulär med samma egenskaper som formuläret som används för att skapa en kalender från grunden, förutom att de föreslagna undantagen och arbetstimmarna per veckodag baseras på den ursprungliga kalendern.

Standardkalender
-----------------

En av de befintliga kalendrarna kan utses till standardkalender. Den här kalendern tilldelas automatiskt till alla entiteter i systemet som hanteras med kalendrar om inte en annan kalender anges.

För att ställa in en standardkalender:

*   Gå till menyn *Administration*.
*   Klicka på alternativet *Konfiguration*.
*   I fältet *Standardkalender*, välj den kalender du vill använda som programmets standardkalender.
*   Klicka på *Spara*.

.. figure:: images/default-calendar.png
   :scale: 50

   Ställa in en standardkalender

Tilldela en kalender till resurser
------------------------------------

Resurser kan bara aktiveras (dvs. ha tillgängliga arbetstimmar) om de har en tilldelad kalender med en giltig aktiveringsperiod. Om ingen kalender tilldelas en resurs tilldelas standardkalendern automatiskt, med en aktiveringsperiod som börjar på startdatumet och utan utgångsdatum.

.. figure:: images/resource-calendar.png
   :scale: 50

   Resurskalender

Du kan dock ta bort kalendern som tidigare tilldelats en resurs och skapa en ny kalender baserad på en befintlig. Detta möjliggör fullständig anpassning av kalendrar för enskilda resurser.

För att tilldela en kalender till en resurs:

*   Gå till alternativet *Redigera resurser*.
*   Välj en resurs och klicka på *Redigera*.
*   Välj fliken "Kalender".
*   Kalendern, tillsammans med dess undantag, arbetstimmar per dag och aktiveringsperioder, visas.
*   Varje flik har följande alternativ:

    *   **Undantag:** Definiera undantag och den period de gäller för, till exempel helgdagar, allmänna helgdagar eller andra arbetsdagar.
    *   **Arbetsvecka:** Ändra arbetstimmarna för varje veckodag (måndag, tisdag, etc.).
    *   **Aktiveringsperioder:** Skapa nya aktiveringsperioder för att återspegla start- och slutdatum för kontrakt som är kopplade till resursen. Se följande bild.

*   Klicka på *Spara* för att lagra informationen.
*   Klicka på *Ta bort* om du vill byta den kalender som tilldelats en resurs.

.. figure:: images/new-resource-calendar.png
   :scale: 50

   Tilldela en ny kalender till en resurs

Tilldela kalendrar till projekt
-------------------------------

Projekt kan ha en annan kalender än standardkalendern. För att ändra kalendern för ett projekt:

*   Gå till projektlistan i företagsöversikten.
*   Redigera det aktuella projektet.
*   Gå till fliken "Allmän information".
*   Välj den kalender som ska tilldelas från rullgardinsmenyn.
*   Klicka på "Spara" eller "Spara och fortsätt."

Tilldela kalendrar till uppgifter
-----------------------------------

Precis som med resurser och projekt kan du tilldela specifika kalendrar till enskilda uppgifter. Detta gör att du kan definiera olika kalendrar för specifika faser av ett projekt. För att tilldela en kalender till en uppgift:

*   Gå till planeringsvyn för ett projekt.
*   Högerklicka på den uppgift du vill tilldela en kalender.
*   Välj alternativet "Tilldela kalender".
*   Välj den kalender som ska tilldelas uppgiften.
*   Klicka på *Acceptera*.
