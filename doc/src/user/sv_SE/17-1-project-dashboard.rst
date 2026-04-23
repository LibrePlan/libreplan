Projektinstrumentpanel
######################

.. contents::

Projektinstrumentpanelen är ett *LibrePlan*-perspektiv som innehåller en uppsättning **KPI:er (Key Performance Indicators)** för att hjälpa till att bedöma ett projekts prestanda avseende:

   *   Arbetsframsteg
   *   Kostnad
   *   Status för tilldelade resurser
   *   Tidsbegränsningar

Framstegsprestandaindikatorer
==============================

Två indikatorer beräknas: projektframstegsprocent och uppgiftsstatus.

Projektframstegsprocent
-----------------------

Det här diagrammet visar det övergripande framsteget för ett projekt och jämför det med det förväntade framsteget baserat på *Gantt*-schemat.

Framsteget representeras av två staplar:

   *   *Aktuellt framsteg:* Det aktuella framsteget baserat på genomförda mätningar.
   *   *Förväntat framsteg:* Det framsteg projektet borde ha uppnått vid denna tidpunkt, enligt projektplanen.

Håll muspekaren över respektive stapel för att se det faktiskt uppmätta värdet.

Det övergripande projektframsteget uppskattas med flera olika metoder, eftersom det inte finns någon enda universellt korrekt metod:

   *   **Spridande framsteg:** Det här är den framstegstyp som är inställd som spridande framsteg på projektnivå. I det här fallet finns inget sätt att beräkna ett förväntat värde, och endast den aktuella stapeln visas.
   *   **Baserat på alla uppgiftstimmar:** Framsteget för alla projektuppgifter beräknas som ett genomsnitt för att ge det övergripande värdet. Det här är ett vägt genomsnitt som tar hänsyn till antalet timmar som tilldelats varje uppgift.
   *   **Baserat på kritisk väg-timmar:** Framsteget för uppgifter som tillhör någon av projektets kritiska vägar beräknas som ett genomsnitt för att ge det övergripande värdet. Det här är ett vägt genomsnitt som tar hänsyn till det totala antalet tilldelade timmar för varje berörd uppgift.
   *   **Baserat på kritisk väg-varaktighet:** Framsteget för uppgifter som tillhör någon av de kritiska vägarna beräknas som ett vägt genomsnitt, men den här gången med hänsyn till varaktigheten för varje berörd uppgift i stället för tilldelade timmar.

Uppgiftsstatus
--------------

Ett cirkeldiagram visar procentandelen projektuppgifter i olika tillstånd. De definierade tillstånden är:

   *   **Avslutad:** Slutförda uppgifter, identifierade av ett framstegsvärde på 100 %.
   *   **Pågående:** Uppgifter som för närvarande är under arbete. Dessa uppgifter har ett framstegsvärde som skiljer sig från 0 % eller 100 %, eller har registrerad arbetstid.
   *   **Redo att starta:** Uppgifter med 0 % framsteg, ingen registrerad tid, där alla *FINISH_TO_START*-beroende uppgifter är *avslutade* och alla *START_TO_START*-beroende uppgifter är *avslutade* eller *pågående*.
   *   **Blockerad:** Uppgifter med 0 % framsteg, ingen registrerad tid, och med föregående beroende uppgifter som varken är *pågående* eller i tillståndet *redo att starta*.

Kostnadsindikatorer
===================

Flera *Earned Value Management*-kostnadsindikatorer beräknas:

   *   **CV (Cost Variance):** Skillnaden mellan *Earned Value-kurvan* och *Actual Cost-kurvan* vid det aktuella tillfället. Positiva värden indikerar en vinst och negativa värden indikerar en förlust.
   *   **ACWP (Actual Cost of Work Performed):** Det totala antalet timmar som registrerats i projektet vid det aktuella tillfället.
   *   **CPI (Cost Performance Index):** Förhållandet *Earned Value / Actual Cost*.

        *   > 100 är gynnsamt och indikerar att projektet är under budget.
        *   = 100 är också gynnsamt och indikerar att kostnaden är precis enligt plan.
        *   < 100 är ogynnsamt och indikerar att kostnaden för att slutföra arbetet är högre än planerat.
   *   **ETC (Estimate To Complete):** Den återstående tiden för att slutföra projektet.
   *   **BAC (Budget At Completion):** Det totala arbete som tilldelats i projektplanen.
   *   **EAC (Estimate At Completion):** Projektledarens prognos för den totala kostnaden vid projektets slutförande, baserat på *CPI*.
   *   **VAC (Variance At Completion):** Skillnaden mellan *BAC* och *EAC*.

        *   < 0 indikerar att projektet är över budget.
        *   > 0 indikerar att projektet är under budget.

Resurser
========

För att analysera projektet ur resursperspektiv tillhandahålls två nyckeltal och ett histogram.

Histogram över uppskattningsavvikelse för avslutade uppgifter
-------------------------------------------------------------

Det här histogrammet beräknar avvikelsen mellan antalet timmar som tilldelats projektuppgifterna och det faktiska antalet timmar som ägnats åt dem.

Avvikelsen beräknas som en procentandel för alla avslutade uppgifter, och de beräknade avvikelserna representeras i ett histogram. Den vertikala axeln visar antalet uppgifter inom varje avvikelseintervall. Sex avvikelseintervall beräknas dynamiskt.

Övertidskvot
------------

Det här nyckeltalet sammanfattar överbelastningen av resurser som tilldelats projektuppgifterna. Det beräknas med formeln: **övertidskvot = överbelastning / (belastning + överbelastning)**.

   *   = 0 är gynnsamt och indikerar att resurserna inte är överbelastade.
   *   > 0 är ogynnsamt och indikerar att resurserna är överbelastade.

Tillgänglighetskvot
-------------------

Det här nyckeltalet sammanfattar den lediga kapaciteten hos de resurser som för närvarande är tilldelade till projektet. Det mäter alltså resursernas förmåga att ta emot fler tilldelningar utan att bli överbelastade. Det beräknas som: **tillgänglighetskvot = (1 - belastning/kapacitet) * 100**

   *   Möjliga värden är mellan 0 % (fullt tilldelad) och 100 % (inte tilldelad).

Tid
===

Två diagram ingår: ett histogram för tidsavvikelse i slutförandetiden för projektuppgifter och ett cirkeldiagram för deadlineöverträdelser.

Försprång eller fördröjning i uppgiftsslutförande
-------------------------------------------------

Beräkningen bestämmer skillnaden i dagar mellan den planerade sluttiden för projektuppgifter och deras faktiska sluttid. Det planerade slutförandedatumet hämtas från *Gantt*-schemat och det faktiska slutdatumet hämtas från den senast registrerade tiden för uppgiften.

Fördröjningen eller försprånget i uppgiftsslutförandet representeras i ett histogram. Den vertikala axeln visar antalet uppgifter med ett försprångs-/fördröjningsdagsvärde som motsvarar dagsintervallet på abskissan. Sex dynamiska intervall för uppgiftsslutförandeavvikelse beräknas.

   *   Negativa värden innebär att uppgiften slutförs före schemat.
   *   Positiva värden innebär att uppgiften slutförs efter schemat.

Deadlineöverträdelser
---------------------

Det här avsnittet beräknar marginalen till projektets deadline, om en sådan har angetts. Dessutom visar ett cirkeldiagram procentandelen uppgifter som uppfyller sin deadline. Tre typer av värden ingår i diagrammet:

   *   Procentandel uppgifter utan konfigurerad deadline.
   *   Procentandel avslutade uppgifter med ett faktiskt slutdatum som är senare än deras deadline. Det faktiska slutdatumet hämtas från den senast registrerade tiden för uppgiften.
   *   Procentandel avslutade uppgifter med ett faktiskt slutdatum som är tidigare än deras deadline.
