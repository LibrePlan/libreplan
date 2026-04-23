Rapport: Totalt antal timmar per resurs och månad
##################################################

.. contents::

Syfte
=====

Denna rapport ger det totala antalet timmar som arbetats av varje resurs under en given månad. Denna information kan vara användbar för att fastställa övertid för arbetare eller, beroende på organisationen, antalet timmar som varje resurs ska ersättas för.

Programmet spårar arbetsrapporter för både arbetare och maskiner. För maskiner summerar rapporten antalet timmar de var i drift under månaden.

Inparametrar och filter
========================

För att generera denna rapport måste användare ange det år och den månad för vilka de vill hämta det totala antalet timmar som arbetats av varje resurs.

Utdata
======

Utdataformatet är följande:

Rubrik
------

Rapportrubriken visar:

   *   Det *år* som data i rapporten avser.
   *   Den *månad* som data i rapporten avser.

Sidfot
------

Sidfoten visar det datum då rapporten genererades.

Brödtext
--------

Datasektionen i rapporten består av en enda tabell med två kolumner:

   *   En kolumn med etiketten **Namn** för resursens namn.
   *   En kolumn med etiketten **Timmar** med det totala antalet timmar som arbetats av resursen i den raden.

Det finns en sista rad som aggregerar det totala antalet timmar som arbetats av alla resurser under den specificerade *månaden* och *året*.
