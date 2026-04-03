Introduktion
############

.. contents::

Detta dokument beskriver funktionerna i LibrePlan och ger användare information om hur man konfigurerar och använder applikationen.

LibrePlan är en webbapplikation med öppen källkod för projektplanering. Dess primära mål är att tillhandahålla en heltäckande lösning för företagets projektledning. För specifik information du kan behöva om denna programvara, kontakta utvecklingsteamet på http://www.libreplan.com/contact/

.. figure:: images/company_view.png
   :scale: 50

   Företagsöversikt

Företagsöversikt och vyhantering
=================================

Som visas på programmets huvudskärm (se föregående skärmdump) och i företagsöversikten kan användare se en lista över planerade projekt. Detta gör att de kan förstå företagets övergripande status avseende projekt och resursutnyttjande. Företagsöversikten erbjuder tre distinkta vyer:

* **Planeringsvy:** Denna vy kombinerar två perspektiv:

   * **Projekts- och tidsspårning:** Varje projekt representeras av ett Gantt-diagram som anger projektets start- och slutdatum. Denna information visas tillsammans med det överenskomna slutdatumet. En jämförelse görs sedan mellan uppnådd framstegsandel och den faktiska tid som lagts på varje projekt. Detta ger en tydlig bild av företagets prestanda vid varje given tidpunkt. Denna vy är programmets standardstartsida.
   * **Diagram över företagets resursutnyttjande:** Det här diagrammet visar information om resursfördelning mellan projekt och ger en sammanfattning av hela företagets resursanvändning. Grönt indikerar att resursfördelningen är under 100% av kapaciteten. Den svarta linjen representerar den totala tillgängliga resurskapaciteten. Gult indikerar att resursfördelningen överstiger 100%. Det är möjligt att ha underutnyttjande totalt sett och samtidigt uppleva överutnyttjande för specifika resurser.

* **Resursbelastningsvy:** Den här skärmen visar en lista över företagets medarbetare och deras specifika uppgiftstilldelningar eller generiska tilldelningar baserade på definierade kriterier. För att komma åt denna vy, klicka på *Övergripande resursbelastning*. Se följande bild för ett exempel.
* **Projektsadministrationsvy:** Den här skärmen visar en lista över företagets projekt och låter användare utföra följande åtgärder: filtrera, redigera, ta bort, visualisera planering eller skapa en ny projekt. För att komma åt denna vy, klicka på *Projektslista*.

.. figure:: images/resources_global.png
   :scale: 50

   Resursöversikt

.. figure:: images/order_list.png
   :scale: 50

   Arbetsfördelningsstruktur

Vyhanteringen som beskrivs ovan för företagsöversikten liknar mycket den hantering som finns tillgänglig för ett enskilt projekt. Ett projekt kan nås på flera sätt:

* Högerklicka på Gantt-diagrammet för projekten och välj *Plan*.
* Gå till projektslistan och klicka på Gantt-diagramikonen.
* Skapa en ny projekt och ändra den aktuella projektsvyn.

Programmet erbjuder följande vyer för en projekt:

* **Planeringsvy:** Den här vyn låter användare visualisera uppgiftsplanering, beroenden, milstolpar och mer. Se avsnittet *Planering* för ytterligare detaljer.
* **Resursbelastningsvy:** Den här vyn låter användare kontrollera den avsedda resursbelastningen för ett projekt. Färgkoden stämmer överens med företagsöversikten: grönt för en belastning under 100%, gult för en belastning lika med 100% och rött för en belastning över 100%. Belastningen kan ha sitt ursprung i en specifik uppgift eller en uppsättning kriterier (generisk tilldelning).
* **Projektsredigeringsvy:** Den här vyn låter användare ändra detaljerna för projekten. Se avsnittet *Projekt* för mer information.
* **Avancerad resurstilldelningsvy:** Den här vyn låter användare tilldela resurser med avancerade alternativ, till exempel att ange timmar per dag eller de tilldelade funktioner som ska utföras. Se avsnittet *Resurstilldelning* för mer information.

Vad gör LibrePlan användbart?
===============================

LibrePlan är ett allmänt planeringsverktyg som utvecklats för att hantera utmaningar inom industriell projektplanering som inte täcktes tillräckligt av befintliga verktyg. Utvecklingen av LibrePlan motiverades också av önskan att erbjuda ett fritt, öppen källkods- och helt webbaserat alternativ till proprietära planeringsverktyg.

Kärnkoncepten som ligger till grund för programmet är följande:

* **Företags- och flerprojektöversikt:** LibrePlan är specifikt utformat för att ge användare information om flera projekt som genomförs inom ett företag. Det är därför i grunden ett flerprojektprogram. Programmets fokus är inte begränsat till enskilda projekt, även om specifika vyer för enskilda projekt också finns tillgängliga.
* **Vyhantering:** Företagsöversikten eller flerprojektvyn åtföljs av olika vyer av den lagrade informationen. Till exempel låter företagsöversikten användare visa projekt och jämföra deras status, visa företagets övergripande resursbelastning och hantera projekt. Användare kan också komma åt planeringsvy, resursbelastningsvy, avancerad resurstilldelningsvy och projektsredigeringsvy för enskilda projekt.
* **Kriterier:** Kriterier är en systemenhet som möjliggör klassificering av både resurser (mänskliga och maskinella) och uppgifter. Resurser måste uppfylla vissa kriterier, och uppgifter kräver att specifika kriterier uppfylls. Detta är en av programmets viktigaste funktioner, eftersom kriterier utgör grunden för generisk tilldelning och hanterar en viktig utmaning i branschen: den tidskrävande karaktären av hantering av mänskliga resurser och svårigheten med långsiktiga företagsbelastningsuppskattningar.
* **Resurser:** Det finns två typer av resurser: mänskliga och maskinella. Mänskliga resurser är företagets medarbetare, som används för planering, övervakning och kontroll av företagets arbetsbelastning. Maskinresurser, beroende av de människor som driver dem, fungerar på liknande sätt som mänskliga resurser.
* **Resurstilldelning:** En nyckelfunktion i programmet är möjligheten att tilldela resurser på två sätt: specifikt och generiskt. Generisk tilldelning baseras på de kriterier som krävs för att slutföra en uppgift och måste uppfyllas av resurser som kan möta dessa kriterier. För att förstå generisk tilldelning, tänk på detta exempel: Johan Svensson är en svetsare. Vanligtvis skulle Johan Svensson specifikt tilldelas en planerad uppgift. LibrePlan erbjuder dock möjligheten att välja vilken svetsare som helst inom företaget, utan att behöva specificera att Johan Svensson är den tilldelade personen.
* **Kontroll av företagsbelastning:** Programmet möjliggör enkel kontroll av företagets resursbelastning. Denna kontroll sträcker sig till både medellång och lång sikt, eftersom aktuella och framtida projekt kan hanteras i programmet. LibrePlan tillhandahåller diagram som visuellt representerar resursutnyttjande.
* **Etiketter:** Etiketter används för att kategorisera projektuppgifter. Med dessa etiketter kan användare gruppera uppgifter efter koncept, vilket möjliggör senare granskning som en grupp eller efter filtrering.
* **Filter:** Eftersom systemet naturligtvis innehåller element som etiketterar eller karakteriserar uppgifter och resurser kan kriteriefilter eller etiketter användas. Detta är mycket användbart för att granska kategoriserad information eller generera specifika rapporter baserade på kriterier eller etiketter.
* **Kalendrar:** Kalendrar definierar tillgängliga produktiva timmar för olika resurser. Användare kan skapa allmänna företagskalendrar eller definiera mer specifika kalendrar, vilket möjliggör skapandet av kalendrar för enskilda resurser och uppgifter.
* **Projekt och projektelement:** Arbete som begärs av klienter behandlas som en projekt i applikationen, strukturerad i projektelement. Projekten och dess element följer en hierarkisk struktur med *x* nivåer. Detta elementträd bildar grunden för arbetsplanering.
* **Framsteg:** Programmet kan hantera olika typer av framsteg. Ett projekts framsteg kan mätas som en procentandel, i enheter, mot den överenskomna budgeten och mer. Ansvaret för att bestämma vilken typ av framsteg som ska användas för jämförelse på högre projektnivåer ligger hos planeringschefen.
* **Uppgifter:** Uppgifter är de grundläggande planeringselementen i programmet. De används för att schemalägga arbete som ska utföras. Viktiga egenskaper hos uppgifter inkluderar: beroenden mellan uppgifter och det potentiella kravet på att specifika kriterier uppfylls innan resurser kan tilldelas.
* **Arbetsrapporter:** Dessa rapporter, inlämnade av företagets medarbetare, detaljerar de arbetade timmarna och de uppgifter som är kopplade till dessa timmar. Denna information gör att systemet kan beräkna den faktiska tid som krävs för att slutföra en uppgift jämfört med den budgeterade tiden. Framsteg kan sedan jämföras mot de faktiskt använda timmarna.

Utöver kärnfunktionerna erbjuder LibrePlan andra funktioner som skiljer det från liknande program:

* **Integration med ERP:** Programmet kan direkt importera information från företagets ERP-system, inklusive projekt, mänskliga resurser, arbetsrapporter och specifika kriterier.
* **Versionshantering:** Programmet kan hantera flera planeringsversioner och låter samtidigt användare granska information från varje version.
* **Historikhantering:** Programmet tar inte bort information; det markerar den bara som ogiltig. Detta gör att användare kan granska historisk information med hjälp av datumfilter.

Användbarhetsprinciper
=======================

Information om formulär
------------------------
Innan vi beskriver de olika funktionerna förknippade med de viktigaste modulerna måste vi förklara den allmänna navigeringen och formulärbeteendet.

Det finns i huvudsak tre typer av redigeringsformulär:

* **Formulär med en *Tillbaka*-knapp:** Dessa formulär är en del av ett större sammanhang och de ändringar som görs lagras i minnet. Ändringarna tillämpas bara när användaren uttryckligen sparar alla detaljer på skärmen som formuläret ursprungligen kom ifrån.
* **Formulär med *Spara*- och *Stäng*-knappar:** Dessa formulär tillåter två åtgärder. Den första sparar ändringarna och stänger det aktuella fönstret. Den andra stänger fönstret utan att spara några ändringar.
* **Formulär med *Spara och fortsätt*-, *Spara*- och *Stäng*-knappar:** Dessa formulär tillåter tre åtgärder. Den första sparar ändringarna och håller det aktuella formuläret öppet. Den andra sparar ändringarna och stänger formuläret. Den tredje stänger fönstret utan att spara några ändringar.

Standardikoner och knappar
---------------------------

* **Redigering:** I allmänhet kan poster i programmet redigeras genom att klicka på en ikon som ser ut som en penna på en vit anteckningsbok.
* **Indrag åt vänster:** Dessa operationer används i allmänhet för element inom en trädstruktur som behöver flyttas till en djupare nivå. Detta görs genom att klicka på ikonen som ser ut som en grön pil som pekar åt höger.
* **Indrag åt höger:** Dessa operationer används i allmänhet för element inom en trädstruktur som behöver flyttas till en högre nivå. Detta görs genom att klicka på ikonen som ser ut som en grön pil som pekar åt vänster.
* **Radering:** Användare kan ta bort information genom att klicka på papperskorgsikonen.
* **Sökning:** Förstoringsglasets ikon indikerar att textfältet till vänster om det används för att söka efter element.

Flikar
------
Programmet använder flikar för att organisera innehållsredigerings- och administrationsformulär. Denna metod används för att dela upp ett övergripande formulär i olika sektioner som är tillgängliga genom att klicka på fliknamnen. De andra flikarna behåller sin aktuella status. I alla fall gäller spara- och avbryt-alternativen för alla underformulär inom de olika flikarna.

Explicita åtgärder och kontexthjälp
--------------------------------------

Programmet innehåller komponenter som ger ytterligare beskrivningar av element när musen håller över dem i en sekund. De åtgärder som användaren kan utföra anges på knappetiketter, i hjälptexter kopplade till dem, i navigeringsmenyalternativen och i kontextmenyerna som visas när man högerklickar i planerarens område. Dessutom tillhandahålls genvägar för de viktigaste operationerna, till exempel dubbelklick på listade element eller användning av tangenthändelser med markören och Enter-tangenten för att lägga till element vid navigering genom formulär.
