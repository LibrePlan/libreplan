Rapport: Arbetade timmar per resurs
####################################

.. contents::

Syfte
=====

Denna rapport extraherar en lista över uppgifter och den tid som resurser har ägnat åt dem inom en specificerad period. Flera filter gör det möjligt för användare att förfina frågan för att endast få den önskade informationen och utesluta irrelevanta data.

Inparametrar och filter
========================

* **Datum**.
    * *Typ*: Valfritt.
    * *Två datumfält*:
        * *Startdatum:* Detta är det tidigaste datumet för arbetsrapporter som ska inkluderas. Arbetsrapporter med datum tidigare än *Startdatum* exkluderas. Om denna parameter lämnas tom filtreras inte arbetsrapporter efter *Startdatum*.
        * *Slutdatum:* Detta är det senaste datumet för arbetsrapporter som ska inkluderas. Arbetsrapporter med datum senare än *Slutdatum* exkluderas. Om denna parameter lämnas tom filtreras inte arbetsrapporter efter *Slutdatum*.

*   **Filtrera efter arbetare:**
    *   *Typ:* Valfritt.
    *   *Hur det fungerar:* Du kan välja en eller flera arbetare för att begränsa arbetsrapporterna till den tid som spårats av dessa specifika arbetare. Sök efter en arbetare i selektorn och klicka på knappen *Lägg till* för att lägga till den som filter. Om detta filter lämnas tomt hämtas arbetsrapporter oavsett arbetare.

*   **Filtrera efter etiketter:**
    *   *Typ:* Valfritt.
    *   *Hur det fungerar:* Du kan lägga till en eller flera etiketter som filter genom att söka efter dem i selektorn och klicka på knappen *Lägg till*. Dessa etiketter används för att välja vilka uppgifter som ska inkluderas i resultaten vid beräkning av timmar dedikerade till dem. Detta filter kan tillämpas på tidrapporter, uppgifter, båda eller ingen.

*   **Filtrera efter kriterier:**
    *   *Typ:* Valfritt.
    *   *Hur det fungerar:* Du kan välja ett eller flera kriterier genom att söka efter dem i selektorn och sedan klicka på knappen *Lägg till*. Dessa kriterier används för att välja resurser som uppfyller minst ett av dem. Rapporten visar all tid dedikerad av resurser som uppfyller ett av de valda kriterierna.

Utdata
======

Rubrik
------

Rapportrubriken visar de filter som konfigurerades och tillämpades på den aktuella rapporten.

Sidfot
------

Datumet då rapporten genererades anges i sidfoten.

Brödtext
--------

Rapportens brödtext består av flera grupper av information.

*   Den första aggregeringsnivån är per resurs. All tid dedikerad av en resurs visas tillsammans under rubriken. Varje resurs identifieras av:

    *   *Arbetare:* Efternamn, Förnamn.
    *   *Maskin:* Namn.

    En sammanfattningsrad visar det totala antalet timmar som arbetats av resursen.

*   Den andra grupperingsnivån är per *datum*. Alla rapporter från en specifik resurs på samma datum visas tillsammans.

    En sammanfattningsrad visar det totala antalet timmar som arbetats av resursen det datumet.

*   Den sista nivån listar arbetsrapporterna för arbetaren den dagen. Den information som visas för varje arbetsrapportrad är:

    *   *Uppgiftskod:* Koden för uppgiften som de spårade timmarna tillskrivs.
    *   *Uppgiftsnamn:* Namnet på uppgiften som de spårade timmarna tillskrivs.
    *   *Starttid:* Detta är valfritt. Det är den tid då resursen började arbeta med uppgiften.
    *   *Sluttid:* Detta är valfritt. Det är den tid då resursen slutade arbeta med uppgiften det specificerade datumet.
    *   *Textfält:* Detta är valfritt. Om arbetsrapportraden har textfält visas de ifyllda värdena här. Formatet är: <Textfältets namn>:<Värde>
    *   *Etiketter:* Detta beror på om arbetsrapportmodellen har ett etikettfält i sin definition. Om det finns flera etiketter visas de i samma kolumn. Formatet är: <Etikettypens namn>:<Etikettens värde>
