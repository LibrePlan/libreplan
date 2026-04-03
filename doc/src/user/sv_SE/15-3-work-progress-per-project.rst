Rapport: Arbete och framsteg per projekt
########################################

.. contents::

Syfte
=====

Denna rapport ger en översikt över projektens status med hänsyn till både framsteg och kostnad.

Den analyserar det aktuella framsteget för varje projekt och jämför det med det planerade framsteget och det utförda arbetet.

Rapporten visar också flera nyckeltal relaterade till projektkostnad och jämför aktuell prestanda med planerad prestanda.

Inparametrar och filter
========================

Det finns flera obligatoriska parametrar:

   *   **Referensdatum:** Detta är det datum som används som referenspunkt för att jämföra projektets planerade status med dess faktiska prestanda. *Standardvärdet för detta fält är aktuellt datum*.

   *   **Framstegstyp:** Detta är den framstegstyp som används för att mäta projektets framsteg. Programmet tillåter att ett projekt mäts simultant med olika framstegstyper. Den typ som användaren väljer i rullgardinsmenyn används för att beräkna rapportdata. Standardvärdet för *framstegstypen* är *spread*, vilket är en speciell framstegstyp som använder den föredragna metoden för att mäta framsteg konfigurerat för varje WBS-element.

De valfria parametrarna är:

   *   **Startdatum:** Detta är det tidigaste startdatumet för projekt som ska inkluderas i rapporten. Om detta fält lämnas tomt finns det inget minimistartdatum för projekten.

   *   **Slutdatum:** Detta är det senaste slutdatumet för projekt som ska inkluderas i rapporten. Alla projekt som avslutas efter *Slutdatum* utesluts.

   *   **Filtrera efter projekt:** Detta filter gör det möjligt för användare att välja specifika projekt som ska inkluderas i rapporten. Om inga projekt läggs till i filtret inkluderar rapporten alla projekt i databasen. En sökbar rullgardinsmeny tillhandahålls för att hitta önskat projekt. Projekt läggs till filtret genom att klicka på knappen *Lägg till*.

Utdata
======

Utdataformatet är följande:

Rubrik
------

Rapportrubriken visar följande fält:

   *   **Startdatum:** Filteringens startdatum. Visas inte om rapporten inte filtreras efter detta fält.
   *   **Slutdatum:** Filteringens slutdatum. Visas inte om rapporten inte filtreras efter detta fält.
   *   **Framstegstyp:** Den framstegstyp som används för rapporten.
   *   **Projekt:** Anger de filtrerade projekten för vilka rapporten genereras. Strängen *Alla* visas när rapporten inkluderar alla projekt som uppfyller de andra filtren.
   *   **Referensdatum:** Det obligatoriska referensdatumet valt för rapporten.

Sidfot
------

Sidfoten visar det datum då rapporten genererades.

Brödtext
--------

Rapportens brödtext består av en lista över projekt valda baserat på inmatningsfiltren.

Filter fungerar genom att lägga till villkor, med undantag för det set som bildas av datumfiltren (*Startdatum*, *Slutdatum*) och *Filtrera efter projekt*. I det här fallet, om ett eller båda datumfiltren är ifyllda och *Filtrera efter projekt* har en lista med valda projekt, har det senare filtret prioritet. Det innebär att projekten som inkluderas i rapporten är de som anges i *Filtrera efter projekt*, oavsett datumfiltren.

Det är viktigt att notera att framsteg i rapporten beräknas som en bråkdel av enheten, i intervallet 0 till 1.

För varje projekt som valts för inkludering i rapportutdata visas följande information:

   * *Projektnamn*.
   * *Totalt antal timmar*. Det totala antalet timmar för projektet visas genom att summera timmarna för varje uppgift. Två typer av totalt antal timmar visas:
      *   *Uppskattade (TE)*. Detta är summan av alla uppskattade timmar i projektets WBS. Det representerar det totala antalet timmar som uppskattats för att slutföra projektet.
      *   *Planerade (TP)*. I *LibrePlan* är det möjligt att ha två olika värden: det uppskattade antalet timmar för en uppgift (antalet timmar som ursprungligen uppskattades för att slutföra uppgiften) och de planerade timmarna (timmarna tilldelade i planen för att slutföra uppgiften). De planerade timmarna kan vara lika med, mindre än eller större än de uppskattade timmarna och bestäms i ett senare skede, tilldelningsoperationen. Därför är det totala planerade antalet timmar för ett projekt summan av alla tilldelade timmar för dess uppgifter.
   * *Framsteg*. Tre mätningar relaterade till det övergripande framsteget av den typ som anges i framstegsinfiltret för varje projekt vid referensdatumet visas:
      *   *Mätt (PM)*. Detta är det övergripande framsteget med hänsyn till framstegsmätningar med ett datum tidigare än *Referensdatum* i rapportens inparametrar. Alla uppgifter beaktas och summan viktas efter antalet timmar för varje uppgift.
      *   *Tillräknat (PI)*. Detta är framsteget under antagandet att arbetet fortsätter i samma takt som de slutförda timmarna för en uppgift. Om X timmar av Y timmar för en uppgift är slutförda anses det övergripande tillräknade framsteget vara X/Y.
      *   *Planerat (PP)*. Detta är det övergripande framsteget för projektet enligt det planerade schemat vid referensdatumet. Om allt hände exakt som planerat borde det mätta framsteget vara detsamma som det planerade framsteget.
   * *Timmar till datum*. Det finns två fält som visar antalet timmar till referensdatumet från två perspektiv:
      *   *Planerade (HP)*. Detta antal är summan av timmar tilldelade till en uppgift i projektet med ett datum som är mindre än eller lika med *Referensdatum*.
      *   *Faktiska (HR)*. Detta antal är summan av timmar rapporterade i arbetsrapporterna för någon av uppgifterna i projektet med ett datum som är mindre än eller lika med *Referensdatum*.
   * *Skillnad*. Under denna rubrik finns flera mätvärden relaterade till kostnad:
      *   *Kostnad*. Detta är skillnaden i timmar mellan antalet timmar som spenderats med hänsyn till det mätta framsteget och de timmar som slutförts till referensdatumet. Formeln är: *PM*TP - HR*.
      *   *Planerad*. Detta är skillnaden mellan timmarna som spenderats enligt det övergripande mätta projektframsteget och antalet planerade till *Referensdatum*. Det mäter fördelen eller förseningen i tid. Formeln är: *PM*TP - HP*.
      *   *Kostnadskvot*. Beräknas genom att dividera *PM* / *PI*. Om det är större än 1 innebär det att projektet är lönsamt vid denna punkt. Om det är mindre än 1 innebär det att projektet förlorar pengar.
      *   *Planerad kvot*. Beräknas genom att dividera *PM* / *PP*. Om det är större än 1 innebär det att projektet ligger före schemat. Om det är mindre än 1 innebär det att projektet ligger efter schemat.
