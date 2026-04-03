Uppgiftsplanering
#################

.. _planificacion:
.. contents::

Uppgiftsplanering
=================

Planering i LibrePlan är en process som har beskrivits genomgående i användarguiden, med kapitlen om projekt och resurstilldelning som särskilt viktiga. Det här kapitlet beskriver de grundläggande planeringsrutinerna efter att projekten och Gantt-diagrammen har konfigurerats korrekt.

.. figure:: images/planning-view.png
   :scale: 35

   Vy för arbetsplanering

Precis som med företagsöversikten är projektplaneringsvyn uppdelad i flera vyer baserade på den information som analyseras. De tillgängliga vyerna för ett specifikt projekt är:

*   Planeringsvy
*   Resursbelastningsvy
*   Projektlistvy
*   Avancerad tilldelningsvy

Planeringsvy
-------------

Planeringsvyn kombinerar tre olika perspektiv:

*   **Projektplanering:** Projektplanering visas i den övre högra delen av programmet som ett Gantt-diagram. Den här vyn låter användare tillfälligt flytta uppgifter, tilldela beroenden mellan dem, definiera milstolpar och fastställa begränsningar.
*   **Resursbelastning:** Resursbelastningsvyn, som finns i skärmens nedre högra del, visar resurstillgänglighet baserat på tilldelningar, i motsats till de tilldelningar som gjorts till uppgifter. Informationen som visas i den här vyn är följande:

    *   **Lila område:** Indikerar en resursbelastning under 100% av dess kapacitet.
    *   **Grönt område:** Indikerar en resursbelastning under 100%, som beror på att resursen är planerad för ett annat projekt.
    *   **Orange område:** Indikerar en resursbelastning över 100% som ett resultat av det aktuella projektet.
    *   **Gult område:** Indikerar en resursbelastning över 100% som ett resultat av andra projekt.

*   **Diagramvy och indikatorerna för upparbetat värde:** Dessa kan visas från fliken "Upparbetat värde". Det genererade diagrammet baseras på tekniken för upparbetat värde, och indikatorerna beräknas för varje arbetsdag i projektet. De beräknade indikatorerna är:

    *   **BCWS (Budgeted Cost of Work Scheduled — Budgeterad kostnad för planerat arbete):** Den kumulativa tidsfunktionen för antalet timmar som planerats till ett visst datum. Den är 0 vid den planerade starten av uppgiften och lika med det totala antalet planerade timmar i slutet. Precis som alla kumulativa diagram ökar den alltid. Funktionen för en uppgift är summan av de dagliga tilldelningarna fram till beräkningsdatumet. Den här funktionen har värden för alla tidpunkter, förutsatt att resurser har tilldelats.
    *   **ACWP (Actual Cost of Work Performed — Faktisk kostnad för utfört arbete):** Den kumulativa tidsfunktionen för timmar rapporterade i arbetsrapporter till ett visst datum. Den här funktionen har bara ett värde på 0 innan datumet för uppgiftens första arbetsrapport, och dess värde fortsätter att öka allt eftersom tid går och arbetstider läggs till. Den har inget värde efter datumet för den senaste arbetsrapporten.
    *   **BCWP (Budgeted Cost of Work Performed — Budgeterad kostnad för utfört arbete):** Den kumulativa tidsfunktionen som inkluderar det resulterande värdet av att multiplicera uppgiftens framsteg med den mängd arbete som uppgiften uppskattades kräva för att slutföras. Den här funktionens värden ökar allt eftersom tid går, liksom framstegsvärden. Framsteg multipliceras med det totala antalet uppskattade timmar för alla uppgifter. BCWP-värdet är summan av värdena för de uppgifter som beräknas. Framsteg summeras när det konfigureras.
    *   **CV (Cost Variance — Kostnadsvariation):** CV = BCWP - ACWP
    *   **SV (Schedule Variance — Tidsplanesvariation):** SV = BCWP - BCWS
    *   **BAC (Budget at Completion — Budget vid slutförande):** BAC = max (BCWS)
    *   **EAC (Estimate at Completion — Uppskattning vid slutförande):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Variance at Completion — Variation vid slutförande):** VAC = BAC - EAC
    *   **ETC (Estimate to Complete — Uppskattning till slutförande):** ETC = EAC - ACWP
    *   **CPI (Cost Performance Index — Kostnadsprestandaindex):** CPI = BCWP / ACWP
    *   **SPI (Schedule Performance Index — Tidsplanesprestandaindex):** SPI = BCWP / BCWS

I projektplaneringsvyn kan användare utföra följande åtgärder:

*   **Tilldela beroenden:** Högerklicka på en uppgift, välj "Lägg till beroende" och dra muspekaren till den uppgift som beroendet ska tilldelas.

    *   För att ändra typen av beroende, högerklicka på beroendet och välj önskad typ.

*   **Skapa en ny milstolpe:** Klicka på uppgiften innan vilken milstolpen ska läggas till och välj alternativet "Lägg till milstolpe." Milstolpar kan flyttas genom att markera milstolpen med muspekaren och dra den till önskad position.
*   **Flytta uppgifter utan att störa beroenden:** Högerklicka på uppgiftens kropp och dra den till önskad position. Om inga begränsningar eller beroenden kränks uppdaterar systemet den dagliga resurstilldelningen till uppgiften och placerar uppgiften på det valda datumet.
*   **Tilldela begränsningar:** Klicka på den aktuella uppgiften och välj alternativet "Uppgiftsegenskaper." Ett popup-fönster visas med ett fält "Begränsningar" som kan ändras. Begränsningar kan konflikta med beroenden, varför varje projekt anger om beroenden har prioritet över begränsningar. De begränsningar som kan fastställas är:

    *   **Så snart som möjligt:** Indikerar att uppgiften måste starta så snart som möjligt.
    *   **Inte före:** Indikerar att uppgiften inte får starta före ett visst datum.
    *   **Starta på ett specifikt datum:** Indikerar att uppgiften måste starta på ett specifikt datum.

Planeringsvyn erbjuder också flera procedurer som fungerar som visningsalternativ:

*   **Zoomnivå:** Användare kan välja önskad zoomnivå. Det finns flera zoomnivåer: årlig, fyramånaders, månads-, vecko- och daglig.
*   **Sökfilter:** Användare kan filtrera uppgifter baserat på etiketter eller kriterier.
*   **Kritisk väg:** Som ett resultat av att använda *Dijkstra*-algoritmen för att beräkna vägar på grafer implementerades den kritiska vägen. Den kan visas genom att klicka på knappen "Kritisk väg" i visningsalternativen.
*   **Visa etiketter:** Gör det möjligt för användare att visa etiketter tilldelade uppgifter i ett projekt, som kan visas på skärmen eller skrivas ut.
*   **Visa resurser:** Gör det möjligt för användare att visa resurser tilldelade uppgifter i ett projekt, som kan visas på skärmen eller skrivas ut.
*   **Skriv ut:** Gör det möjligt för användare att skriva ut det Gantt-diagram som visas.

Resursbelastningsvy
--------------------

Resursbelastningsvyn ger en lista över resurser som innehåller en lista med uppgifter eller kriterier som genererar arbetsbelastningar. Varje uppgift eller kriterium visas som ett Gantt-diagram så att start- och slutdatum för belastningen kan ses. En annan färg visas beroende på om resursen har en belastning som är högre eller lägre än 100%:

*   **Grönt:** Belastning lägre än 100%
*   **Orange:** 100% belastning
*   **Rött:** Belastning över 100%

.. figure:: images/resource-load.png
   :scale: 35

   Resursbelastningsvy för en specifik projekt

Om muspekaren placeras på resursens Gantt-diagram visas belastningsprocenten för medarbetaren.

Projektlistvy
-------------------

Projektlistvyn låter användare komma åt alternativen för redigering och borttagning av projekt. Se kapitlet "Projekt" för mer information.

Avancerad tilldelningsvy
-------------------------

Den avancerade tilldelningsvyn förklaras ingående i kapitlet "Resurstilldelning".
