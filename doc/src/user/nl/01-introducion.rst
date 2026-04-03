Inleiding
#########

.. contents::

Dit document beschrijft de functies van LibrePlan en biedt gebruikersinformatie over het configureren en gebruiken van de applicatie.

LibrePlan is een open-source webapplicatie voor projectplanning. Het primaire doel is het bieden van een uitgebreide oplossing voor projectbeheer in bedrijven. Voor specifieke informatie over deze software kunt u contact opnemen met het ontwikkelteam op http://www.libreplan.com/contact/

.. figure:: images/company_view.png
   :scale: 50

   Bedrijfsoverzicht

Bedrijfsoverzicht en Weergavebeheer
=====================================

Zoals te zien op het hoofdscherm van het programma (zie de vorige schermafbeelding) en het bedrijfsoverzicht, kunnen gebruikers een lijst van geplande projecten bekijken. Dit stelt hen in staat de algemene status van het bedrijf te begrijpen wat betreft projecten en gebruik van middelen. Het bedrijfsoverzicht biedt drie verschillende weergaven:

* **Planningsweergave:** Deze weergave combineert twee perspectieven:

   * **Project- en tijdbewaking:** Elk project wordt weergegeven door een Gantt-diagram, dat de start- en einddatum van het project aangeeft. Deze informatie wordt naast de overeengekomen deadline weergegeven. Vervolgens wordt een vergelijking gemaakt tussen het behaalde voortgangspercentage en de werkelijke tijd die aan elk project is besteed. Dit biedt op elk moment een duidelijk beeld van de prestaties van het bedrijf. Deze weergave is de standaard startpagina van het programma.
   * **Grafiek voor bedrijfsresourcegebruik:** Deze grafiek toont informatie over resourcetoewijzing over projecten heen en biedt een samenvatting van het resourcegebruik van het gehele bedrijf. Groen geeft aan dat de resourcetoewijzing onder 100% van de capaciteit ligt. De zwarte lijn vertegenwoordigt de totale beschikbare resourcecapaciteit. Geel geeft aan dat de resourcetoewijzing boven 100% ligt. Het is mogelijk dat er overall onderbelasting is terwijl tegelijkertijd overbelasting optreedt voor specifieke resources.

* **Resourcebelastingweergave:** Dit scherm toont een lijst van de medewerkers van het bedrijf en hun specifieke taaktoewijzingen, of generieke toewijzingen op basis van gedefinieerde criteria. Klik op *Totale belasting van resources* om deze weergave te openen. Zie de volgende afbeelding voor een voorbeeld.
* **Projectbeheersweergave:** Dit scherm toont een lijst van bedrijfsprojecten en stelt gebruikers in staat de volgende acties uit te voeren: filteren, bewerken, verwijderen, planning visualiseren of een nieuw project aanmaken. Klik op *Projectlijst* om deze weergave te openen.

.. figure:: images/resources_global.png
   :scale: 50

   Resourceoverzicht

.. figure:: images/order_list.png
   :scale: 50

   Werkstructuur

Het hierboven beschreven weergavebeheer voor het bedrijfsoverzicht lijkt sterk op het beheer dat beschikbaar is voor een enkel project. Een project kan op verschillende manieren worden geopend:

* Klik met de rechtermuisknop op het Gantt-diagram voor het project en selecteer *Plannen*.
* Open de projectlijst en klik op het Gantt-diagrampictogram.
* Maak een nieuw project aan en verander de huidige projectweergave.

Het programma biedt de volgende weergaven voor een project:

* **Planningsweergave:** Deze weergave stelt gebruikers in staat taakplanning, afhankelijkheden, mijlpalen en meer te visualiseren. Zie de sectie *Planning* voor meer details.
* **Resourcebelastingweergave:** Deze weergave stelt gebruikers in staat de aangewezen resourcebelasting voor een project te controleren. De kleurcodering is consistent met het bedrijfsoverzicht: groen voor een belasting van minder dan 100%, geel voor een belasting van precies 100% en rood voor een belasting van meer dan 100%. De belasting kan afkomstig zijn van een specifieke taak of een reeks criteria (generieke toewijzing).
* **Projectbewerking:** Deze weergave stelt gebruikers in staat de details van het project te wijzigen. Zie de sectie *Projecten* voor meer informatie.
* **Geavanceerde resourcetoewijzingweergave:** Deze weergave stelt gebruikers in staat resources toe te wijzen met geavanceerde opties, zoals het specificeren van uren per dag of de toe te wijzen functies. Zie de sectie *Resourcetoewijzing* voor meer informatie.

Wat Maakt LibrePlan Nuttig?
============================

LibrePlan is een algemeen planningshulpmiddel ontwikkeld om uitdagingen in industriële projectplanning aan te pakken die niet adequaat werden gedekt door bestaande hulpmiddelen. De ontwikkeling van LibrePlan werd ook gemotiveerd door de wens om een gratis, open-source en volledig webgebaseerd alternatief te bieden voor propriëtaire planningshulpmiddelen.

De kernconcepten die het programma ondersteunen zijn als volgt:

* **Bedrijfs- en multi-projectoverzicht:** LibrePlan is specifiek ontworpen om gebruikers informatie te bieden over meerdere projecten die binnen een bedrijf worden uitgevoerd. Daarom is het inherent een multi-projectprogramma. De focus van het programma is niet beperkt tot individuele projecten, hoewel specifieke weergaven voor individuele projecten ook beschikbaar zijn.
* **Weergavebeheer:** Het bedrijfsoverzicht, of de multi-projectweergave, gaat gepaard met verschillende weergaven van de opgeslagen informatie. Het bedrijfsoverzicht stelt gebruikers bijvoorbeeld in staat projecten te bekijken en hun status te vergelijken, de algehele resourcebelasting van het bedrijf te bekijken en projecten te beheren. Gebruikers kunnen ook de planningsweergave, resourcebelastingweergave, geavanceerde resourcetoewijzingweergave en projectbewerkingsweergave voor individuele projecten openen.
* **Criteria:** Criteria zijn een systeementiteit die de classificatie van zowel resources (menselijk en machine) als taken mogelijk maakt. Resources moeten aan bepaalde criteria voldoen en taken vereisen dat specifieke criteria worden vervuld. Dit is een van de belangrijkste functies van het programma, omdat criteria de basis vormen van generieke toewijzing en een significante uitdaging in de industrie aanpakken: de tijdrovende aard van menselijk resourcebeheer en de moeilijkheid van langetermijnschattingen van bedrijfsbelasting.
* **Resources:** Er zijn twee soorten resources: menselijk en machine. Menselijke resources zijn de medewerkers van het bedrijf, gebruikt voor planning, bewaking en controle van de werkbelasting van het bedrijf. Machineresources, afhankelijk van de mensen die ze bedienen, functioneren op dezelfde manier als menselijke resources.
* **Resourcetoewijzing:** Een belangrijk kenmerk van het programma is de mogelijkheid om resources op twee manieren aan te wijzen: specifiek en generiek. Generieke toewijzing is gebaseerd op de criteria die nodig zijn om een taak te voltooien en moet worden vervuld door resources die aan die criteria kunnen voldoen. Om generieke toewijzing te begrijpen, overweeg dit voorbeeld: Jan Jansen is een lasser. Normaal gesproken zou Jan Jansen specifiek worden toegewezen aan een geplande taak. LibrePlan biedt echter de optie om elke lasser binnen het bedrijf te selecteren, zonder dat hoeft te worden gespecificeerd dat Jan Jansen de toegewezen persoon is.
* **Bedrijfsbelastingsbeheer:** Het programma maakt eenvoudig beheer van de resourcebelasting van het bedrijf mogelijk. Dit beheer strekt zich uit tot zowel de middellange als de lange termijn, omdat huidige en toekomstige projecten binnen het programma kunnen worden beheerd. LibrePlan biedt grafieken die het resourcegebruik visueel weergeven.
* **Labels:** Labels worden gebruikt om projecttaken te categoriseren. Met deze labels kunnen gebruikers taken groeperen op concept, waardoor ze later als groep of na filtering kunnen worden bekeken.
* **Filters:** Omdat het systeem van nature elementen bevat die taken en resources labelen of kenmerken, kunnen criteriafilters of labels worden gebruikt. Dit is zeer nuttig voor het beoordelen van gecategoriseerde informatie of het genereren van specifieke rapporten op basis van criteria of labels.
* **Kalenders:** Kalenders definiëren de beschikbare productieve uren voor verschillende resources. Gebruikers kunnen algemene bedrijfskalenders aanmaken of meer specifieke kalenders definiëren, wat het aanmaken van kalenders voor individuele resources en taken mogelijk maakt.
* **Projecten en projectelementen:** Werk dat door klanten wordt aangevraagd, wordt binnen de applicatie behandeld als een project, gestructureerd in projectelementen. Het project en zijn elementen volgen een hiërarchische structuur met *x* niveaus. Deze elementenboom vormt de basis voor werkplanning.
* **Voortgang:** Het programma kan verschillende soorten voortgang beheren. De voortgang van een project kan worden gemeten als percentage, in eenheden, ten opzichte van het overeengekomen budget en meer. De verantwoordelijkheid voor het bepalen welk type voortgang gebruikt wordt voor vergelijking op hogere projectniveaus ligt bij de planningsbeheerder.
* **Taken:** Taken zijn de fundamentele planningselementen binnen het programma. Ze worden gebruikt om te plannen welk werk uitgevoerd moet worden. Belangrijke kenmerken van taken zijn: afhankelijkheden tussen taken en de mogelijke vereiste dat aan specifieke criteria moet worden voldaan voordat resources kunnen worden toegewezen.
* **Werkrapporten:** Deze rapporten, ingediend door de medewerkers van het bedrijf, bevatten details over de gewerkte uren en de taken die aan die uren zijn gekoppeld. Deze informatie stelt het systeem in staat de werkelijk bestede tijd voor het voltooien van een taak te berekenen in vergelijking met de geplande tijd. Voortgang kan vervolgens worden vergeleken met de werkelijk gebruikte uren.

Naast de kernfuncties biedt LibrePlan andere functies die het onderscheiden van vergelijkbare programma's:

* **Integratie met ERP:** Het programma kan direct informatie importeren van bedrijfs-ERP-systemen, inclusief projecten, menselijke resources, werkrapporten en specifieke criteria.
* **Versiebeheer:** Het programma kan meerdere planningsversies beheren, terwijl het gebruikers nog steeds toestaat de informatie van elke versie te bekijken.
* **Geschiedenisbeheer:** Het programma verwijdert geen informatie; het markeert die alleen als ongeldig. Dit stelt gebruikers in staat historische informatie te bekijken met behulp van datumfilters.

Gebruiksconventies
==================

Informatie Over Formulieren
----------------------------
Voordat we de verschillende functies beschrijven die verband houden met de belangrijkste modules, moeten we de algemene navigatie en het gedrag van formulieren uitleggen.

Er zijn in essentie drie soorten bewerkingsformulieren:

* **Formulieren met een *Terug*-knop:** Deze formulieren maken deel uit van een grotere context en de gemaakte wijzigingen worden in het geheugen opgeslagen. De wijzigingen worden pas toegepast wanneer de gebruiker expliciet alle details opslaat op het scherm waaruit het formulier afkomstig is.
* **Formulieren met *Opslaan* en *Sluiten*-knoppen:** Deze formulieren staan twee acties toe. De eerste slaat de wijzigingen op en sluit het huidige venster. De tweede sluit het venster zonder wijzigingen op te slaan.
* **Formulieren met *Opslaan en doorgaan*, *Opslaan* en *Sluiten*-knoppen:** Deze formulieren staan drie acties toe. De eerste slaat de wijzigingen op en houdt het huidige formulier open. De tweede slaat de wijzigingen op en sluit het formulier. De derde sluit het venster zonder wijzigingen op te slaan.

Standaardpictogrammen en -knoppen
-----------------------------------

* **Bewerken:** In het algemeen kunnen records in het programma worden bewerkt door op een pictogram te klikken dat eruitziet als een potlood op een wit notitieboekje.
* **Inspringen naar rechts:** Deze bewerkingen worden over het algemeen gebruikt voor elementen binnen een boomstructuur die naar een dieper niveau moeten worden verplaatst. Dit wordt gedaan door op het pictogram te klikken dat eruitziet als een groene pijl die naar rechts wijst.
* **Inspringen naar links:** Deze bewerkingen worden over het algemeen gebruikt voor elementen binnen een boomstructuur die naar een hoger niveau moeten worden verplaatst. Dit wordt gedaan door op het pictogram te klikken dat eruitziet als een groene pijl die naar links wijst.
* **Verwijderen:** Gebruikers kunnen informatie verwijderen door op het prullenbakpictogram te klikken.
* **Zoeken:** Het vergrootglaspictogram geeft aan dat het tekstveld ernaast wordt gebruikt voor het zoeken naar elementen.

Tabbladen
----------
Het programma gebruikt tabbladen om bewerkings- en beheerformulieren voor inhoud te organiseren. Deze methode wordt gebruikt om een uitgebreid formulier op te delen in verschillende secties, toegankelijk door op de namen van de tabbladen te klikken. De andere tabbladen behouden hun huidige status. In alle gevallen zijn de opslaan- en annuleeropties van toepassing op alle subformulieren binnen de verschillende tabbladen.

Expliciete Acties en Contexthulp
----------------------------------

Het programma bevat componenten die aanvullende beschrijvingen van elementen bieden wanneer de muis er een seconde overheen beweegt. De acties die de gebruiker kan uitvoeren, worden aangegeven op de knoplabels, in de bijbehorende helpteksten, in de navigatiemenuopties en in de contextmenu's die verschijnen bij rechtsklikken in het plannergebied. Bovendien worden snelkoppelingen geboden voor de belangrijkste bewerkingen, zoals dubbelklikken op vermelde elementen of het gebruik van toetsgebeurtenissen met de cursor en de Enter-toets om elementen toe te voegen bij het navigeren door formulieren.
