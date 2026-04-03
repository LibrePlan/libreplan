Rapport werk en voortgang per project
######################################

.. contents::

Doel
====

Dit rapport geeft een overzicht van de status van projecten, rekening houdend met zowel voortgang als kosten.

Het analyseert de huidige voortgang van elk project en vergelijkt deze met de geplande voortgang en het voltooide werk.

Het rapport toont ook verschillende ratio's met betrekking tot de projectkosten en vergelijkt de huidige prestaties met de geplande prestaties.

Invoerparameters en filters
============================

Er zijn verschillende verplichte parameters:

   *   **Referentiedatum:** Dit is de datum die wordt gebruikt als referentiepunt voor het vergelijken van de geplande status van het project met de werkelijke prestaties. *De standaardwaarde voor dit veld is de huidige datum*.

   *   **Voortgangstype:** Dit is het voortgangstype dat wordt gebruikt om de projectvoortgang te meten. De applicatie maakt het mogelijk een project gelijktijdig te meten met verschillende voortgangstypen. Het type dat door de gebruiker wordt geselecteerd in het vervolgkeuzemenu wordt gebruikt voor het berekenen van de rapportgegevens. De standaardwaarde voor het *voortgangstype* is *spread*, een speciaal voortgangstype dat de voorkeursmethode gebruikt voor het meten van voortgang zoals geconfigureerd voor elk WBS-element.

De optionele parameters zijn:

   *   **Begindatum:** Dit is de vroegste startdatum voor projecten die in het rapport worden opgenomen. Als dit veld leeg wordt gelaten, is er geen minimale startdatum voor de projecten.

   *   **Einddatum:** Dit is de laatste einddatum voor projecten die in het rapport worden opgenomen. Alle projecten die na de *Einddatum* eindigen, worden uitgesloten.

   *   **Filteren op projecten:** Met dit filter kunnen gebruikers de specifieke projecten selecteren die in het rapport worden opgenomen. Als er geen projecten aan het filter worden toegevoegd, omvat het rapport alle projecten in de database. Er is een doorzoekbaar vervolgkeuzemenu beschikbaar om het gewenste project te vinden. Projecten worden aan het filter toegevoegd door op de knop *Toevoegen* te klikken.

Uitvoer
=======

Het uitvoerformaat is als volgt:

Koptekst
--------

De koptekst van het rapport toont de volgende velden:

   *   **Begindatum:** De filterbegindatum. Dit wordt niet weergegeven als het rapport niet op dit veld is gefilterd.
   *   **Einddatum:** De filtereinddatum. Dit wordt niet weergegeven als het rapport niet op dit veld is gefilterd.
   *   **Voortgangstype:** Het voortgangstype dat voor het rapport is gebruikt.
   *   **Projecten:** Dit geeft de gefilterde projecten aan waarvoor het rapport is gegenereerd. Het toont de tekst *Alle* wanneer het rapport alle projecten omvat die aan de andere filters voldoen.
   *   **Referentiedatum:** De verplichte ingevoerde referentiedatum die voor het rapport is geselecteerd.

Voettekst
---------

De voettekst toont de datum waarop het rapport is gegenereerd.

Hoofdtekst
----------

De hoofdtekst van het rapport bestaat uit een lijst van projecten die zijn geselecteerd op basis van de invoerfilters.

Filters werken door voorwaarden toe te voegen, behalve voor de set die wordt gevormd door de datumfilters (*Begindatum*, *Einddatum*) en het *Filteren op projecten*. In dit geval, als één of beide datumfilters zijn ingevuld en het *Filteren op projecten* een lijst van geselecteerde projecten heeft, heeft het laatste filter voorrang. Dit betekent dat de projecten die in het rapport zijn opgenomen, die zijn die zijn opgegeven door het *Filteren op projecten*, ongeacht de datumfilters.

Het is belangrijk op te merken dat voortgang in het rapport wordt berekend als een breuk van eenheid, variërend tussen 0 en 1.

Voor elk project dat is geselecteerd voor opname in de rapportuitvoer, wordt de volgende informatie weergegeven:

   * *Projectnaam*.
   * *Totaal aantal uren*. De totale uren voor het project worden weergegeven door de uren voor elke taak op te tellen. Er worden twee soorten totale uren weergegeven:
      *   *Geschat (GE)*. Dit is de som van alle geschatte uren in de WBS van het project. Het vertegenwoordigt het totale aantal uren dat is geschat om het project te voltooien.
      *   *Gepland (GP)*. In *LibrePlan* is het mogelijk twee verschillende aantallen te hebben: het geschatte aantal uren voor een taak (het aantal uren dat aanvankelijk werd geschat om de taak te voltooien) en de geplande uren (de uren die in het plan zijn toegewezen om de taak te voltooien). De geplande uren kunnen gelijk zijn aan, minder dan of meer dan de geschatte uren, en worden bepaald in een latere fase, de toewijzingsbewerking. Daarom zijn de totale geplande uren voor een project de som van alle toegewezen uren voor de taken.
   * *Voortgang*. Er worden drie metingen weergegeven met betrekking tot de algehele voortgang van het in het voortgangsfilter opgegeven type voor elk project op de referentiedatum:
      *   *Gemeten (GM)*. Dit is de algehele voortgang op basis van de voortgangsmetingen met een datum eerder dan de *Referentiedatum* in de invoerparameters van het rapport. Alle taken worden in aanmerking genomen, en de som is gewogen naar het aantal uren voor elke taak.
      *   *Toegerekend (GT)*. Dit is de voortgang ervan uitgaande dat het werk doorgaat in hetzelfde tempo als de voltooide uren voor een taak. Als X uren van Y uren voor een taak zijn voltooid, wordt de algehele toegerekende voortgang beschouwd als X/Y.
      *   *Gepland (GG)*. Dit is de algehele voortgang van het project volgens het geplande schema op de referentiedatum. Als alles precies zo is verlopen als gepland, zou de gemeten voortgang gelijk moeten zijn aan de geplande voortgang.
   * *Uren tot datum*. Er zijn twee velden die het aantal uren tot de referentiedatum vanuit twee perspectieven weergeven:
      *   *Gepland (GP)*. Dit getal is de som van de uren die zijn toegewezen aan een taak in het project met een datum kleiner dan of gelijk aan de *Referentiedatum*.
      *   *Werkelijk (GW)*. Dit getal is de som van de uren die zijn gerapporteerd in de urenbriefjes voor een van de taken in het project met een datum kleiner dan of gelijk aan de *Referentiedatum*.
   * *Verschil*. Onder deze koptekst staan verschillende statistieken met betrekking tot kosten:
      *   *Kosten*. Dit is het verschil in uren tussen het aantal bestede uren, rekening houdend met de gemeten voortgang, en de voltooide uren tot de referentiedatum. De formule is: *GM*GP - GW*.
      *   *Gepland*. Dit is het verschil tussen de bestede uren volgens de algehele gemeten projectvoortgang en het geplande aantal tot de *Referentiedatum*. Het meet het voordeel of de vertraging in tijd. De formule is: *GM*GP - GP*.
      *   *Kostenratio*. Dit wordt berekend door *GM* / *GT* te delen. Als dit groter is dan 1, betekent dit dat het project op dit punt winstgevend is. Als het kleiner is dan 1, betekent dit dat het project geld verliest.
      *   *Planningsratio*. Dit wordt berekend door *GM* / *GG* te delen. Als dit groter is dan 1, betekent dit dat het project voor op schema ligt. Als het kleiner is dan 1, betekent dit dat het project achter op schema ligt.
