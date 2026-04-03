Connectoren
###########

.. contents::

Connectoren zijn *LibrePlan*-clientapplicaties die kunnen worden gebruikt om te communiceren met (web)servers om gegevens op te halen, te verwerken en op te slaan. Er zijn momenteel drie connectoren: de JIRA-connector, de Tim Enterprise-connector en de e-mailconnector.

Configuratie
============

Connectoren moeten correct worden geconfigureerd voordat ze kunnen worden gebruikt. Ze kunnen worden geconfigureerd vanuit het scherm "Hoofdinstellingen" onder het tabblad "Connectoren".

Het connectorenscherm bevat:

*   **Vervolgkeuzelijst:** Een lijst met beschikbare connectoren.
*   **Scherm voor het bewerken van eigenschappen:** Een formulier voor het bewerken van eigenschappen van de geselecteerde connector.
*   **Knop Verbinding testen:** Een knop om de verbinding met de connector te testen.

Selecteer de connector die u wilt configureren uit de vervolgkeuzelijst met connectoren. Er wordt een bewerkingsformulier voor de eigenschappen van de geselecteerde connector weergegeven. In het bewerkingsformulier voor eigenschappen kunt u de eigenschapswaarden naar wens wijzigen en uw configuraties testen met behulp van de knop "Verbinding testen".

.. NOTE::

   De eigenschappen zijn geconfigureerd met standaardwaarden. De belangrijkste eigenschap is "Geactiveerd." Standaard is dit ingesteld op "N." Dit geeft aan dat de connector niet wordt gebruikt tenzij u de waarde wijzigt in "Y" en de wijzigingen opslaat.

JIRA-connector
==============

JIRA is een systeem voor het bijhouden van problemen en projecten.

De JIRA-connector is een applicatie die kan worden gebruikt om gegevens van de JIRA-webserver op te vragen voor JIRA-problemen en de reactie te verwerken. Het verzoek is gebaseerd op JIRA-labels. In JIRA kunnen labels worden gebruikt om problemen te categoriseren. Het verzoek is als volgt gestructureerd: haal alle problemen op die zijn gecategoriseerd door deze labelnaam.

De connector ontvangt de reactie, in dit geval de problemen, en converteert ze naar *LibrePlan*-"Projectelementen" en "Urenstaten".

De *JIRA-connector* moet correct worden geconfigureerd voordat deze kan worden gebruikt.

Configuratie
------------

Kies vanuit het scherm "Hoofdinstellingen" het tabblad "Connectoren". Selecteer in het connectorenscherm de JIRA-connector uit de vervolgkeuzelijst. Er wordt dan een scherm voor het bewerken van eigenschappen weergegeven.

In dit scherm kunt u de volgende eigenschapswaarden configureren:

*   **Geactiveerd:** J/N, waarmee u aangeeft of u de JIRA-connector wilt gebruiken. De standaardwaarde is "N."
*   **Server-URL:** Het absolute pad naar de JIRA-webserver.
*   **Gebruikersnaam en wachtwoord:** De gebruikersgegevens voor autorisatie.
*   **JIRA-labels: door komma's gescheiden lijst van labels of URL:** U kunt de label-URL invoeren of een door komma's gescheiden lijst van labels.
*   **Urentype:** Het type werkuren. De standaardwaarde is "Standaard."

.. NOTE::

   **JIRA-labels:** Momenteel ondersteunt de JIRA-webserver niet het verstrekken van een lijst met alle beschikbare labels. Als tijdelijke oplossing hebben we een eenvoudig PHP-script ontwikkeld dat een eenvoudige SQL-query uitvoert in de JIRA-database om alle afzonderlijke labels op te halen. U kunt dit PHP-script gebruiken als de "JIRA-labels-URL" of de gewenste labels invoeren als door komma's gescheiden tekst in het veld "JIRA-labels".

Klik ten slotte op de knop "Verbinding testen" om te controleren of u verbinding kunt maken met de JIRA-webserver en of uw configuraties correct zijn.

Synchronisatie
--------------

Vanuit het projectvenster, onder "Algemene gegevens," kunt u beginnen met het synchroniseren van projectelementen met JIRA-problemen.

Klik op de knop "Synchroniseren met JIRA" om de synchronisatie te starten.

*   Als dit de eerste keer is, wordt er een pop-upvenster weergegeven (met een automatisch aangevulde lijst van labels). In dit venster kunt u een label selecteren om mee te synchroniseren en op de knop "Synchronisatie starten" klikken om het synchronisatieproces te beginnen, of op de knop "Annuleren" klikken om het te annuleren.

*   Als een label al is gesynchroniseerd, worden de datum van de laatste synchronisatie en het label weergegeven in het JIRA-scherm. In dit geval wordt er geen pop-upvenster weergegeven om een label te selecteren. In plaats daarvan wordt het synchronisatieproces direct gestart voor dat weergegeven (al gesynchroniseerde) label.

.. NOTE::

   De relatie tussen "Project" en "label" is één-op-één. Er kan slechts één label worden gesynchroniseerd met één "Project."

.. NOTE::

   Bij succesvolle (her)synchronisatie worden de gegevens naar de database geschreven en wordt het JIRA-scherm bijgewerkt met de datum van de laatste synchronisatie en het label.

(Her)synchronisatie wordt uitgevoerd in twee fasen:

*   **Fase 1:** Synchroniseren van projectelementen, inclusief voortgangstoewijzing en -metingen.
*   **Fase 2:** Synchroniseren van urenstaten.

.. NOTE::

   Als Fase 1 mislukt, wordt Fase 2 niet uitgevoerd en worden er geen gegevens naar de database geschreven.

.. NOTE::

   De informatie over succes of mislukking wordt weergegeven in een pop-upvenster.

Na succesvolle voltooiing van de synchronisatie wordt het resultaat weergegeven in het tabblad "Werkopsplitsingsstructuur (WBS-taken)" van het scherm "Projectdetails". In deze gebruikersinterface zijn er twee wijzigingen ten opzichte van de standaard WBS:

*   De kolom "Totaal taaknuren" is niet bewerkbaar (alleen-lezen) omdat de synchronisatie eenrichtingsverkeer is. Taaknuren kunnen alleen worden bijgewerkt in de JIRA-webserver.
*   De kolom "Code" toont de JIRA-probleemsleutels, en ze zijn ook hyperlinks naar de JIRA-problemen. Klik op de gewenste sleutel als u naar het document voor die sleutel (JIRA-probleem) wilt gaan.

Planning
--------

Hersynchronisatie van JIRA-problemen kan ook worden uitgevoerd via de planner. Ga naar het scherm "Taakplanning". In dat scherm kunt u een JIRA-taak configureren om synchronisatie uit te voeren. De taak zoekt naar de als laatste gesynchroniseerde labels in de database en synchroniseert ze dienovereenkomstig opnieuw. Zie ook de Plannerhandleiding.

Tim Enterprise-connector
========================

Tim Enterprise is een Nederlands product van Aenova. Het is een webapplicatie voor de registratie van bestede tijd aan projecten en taken.

De Tim-connector is een applicatie die kan worden gebruikt om te communiceren met de Tim Enterprise-server om:

*   Alle uren die een werknemer (gebruiker) heeft besteed aan een project te exporteren die in Tim Enterprise kunnen worden geregistreerd.
*   Alle roosters van de werknemer (gebruiker) te importeren om de resource effectief te plannen.

De *Tim-connector* moet correct worden geconfigureerd voordat deze kan worden gebruikt.

Configuratie
------------

Kies vanuit het scherm "Hoofdinstellingen" het tabblad "Connectoren". Selecteer in het connectorenscherm de Tim-connector uit de vervolgkeuzelijst. Er wordt dan een scherm voor het bewerken van eigenschappen weergegeven.

In dit scherm kunt u de volgende eigenschapswaarden configureren:

*   **Geactiveerd:** J/N, waarmee u aangeeft of u de Tim-connector wilt gebruiken. De standaardwaarde is "N."
*   **Server-URL:** Het absolute pad naar de Tim Enterprise-server.
*   **Gebruikersnaam en wachtwoord:** De gebruikersgegevens voor autorisatie.
*   **Aantal dagen urenstaat naar Tim:** Het aantal dagen terug dat u de urenstaten wilt exporteren.
*   **Aantal dagen rooster vanuit Tim:** Het aantal dagen vooruit dat u de roosters wilt importeren.
*   **Productiviteitsfactor:** Effectieve werkuren in procenten. De standaardwaarde is "100%."
*   **Afdeling-ID's voor het importeren van roosters:** Door komma's gescheiden afdeling-ID's.

Klik ten slotte op de knop "Verbinding testen" om te controleren of u verbinding kunt maken met de Tim Enterprise-server en of uw configuraties correct zijn.

Exporteren
----------

Vanuit het projectvenster, onder "Algemene gegevens," kunt u beginnen met het exporteren van urenstaten naar de Tim Enterprise-server.

Voer de "Tim-productcode" in en klik op de knop "Exporteren naar Tim" om het exporteren te starten.

De Tim-connector voegt de volgende velden toe samen met de productcode:

*   De volledige naam van de werknemer/gebruiker.
*   De datum waarop de werknemer aan een taak heeft gewerkt.
*   De inspanning, of uren gewerkt aan de taak.
*   Een optie die aangeeft of Tim Enterprise de registratie moet bijwerken of een nieuwe moet invoegen.

De Tim Enterprise-reactie bevat alleen een lijst van record-ID's (gehele getallen). Dit maakt het moeilijk om te bepalen wat er fout is gegaan, omdat de reactielijst alleen nummers bevat die niet gerelateerd zijn aan de aanvraagvelden. De exportaanvraag (registratie in Tim) wordt als geslaagd beschouwd als alle lijstvermeldingen geen "0"-waarden bevatten. Anders is de exportaanvraag mislukt voor die vermeldingen die "0"-waarden bevatten. Daarom kunt u niet zien welke aanvraag is mislukt, omdat de lijstvermeldingen alleen de waarde "0" bevatten. De enige manier om dit te bepalen, is door het logbestand op de Tim Enterprise-server te onderzoeken.

.. NOTE::

   Bij succesvol exporteren worden de gegevens naar de database geschreven en wordt het Tim-scherm bijgewerkt met de datum van de laatste export en de productcode.

.. NOTE::

   De informatie over succes of mislukking wordt weergegeven in een pop-upvenster.

Exporteren plannen
------------------

Het exportproces kan ook worden uitgevoerd via de planner. Ga naar het scherm "Taakplanning". In dat scherm kunt u een Tim Export-taak configureren. De taak zoekt naar de als laatste geëxporteerde urenstaten in de database en exporteert ze dienovereenkomstig opnieuw. Zie ook de Plannerhandleiding.

Importeren
----------

Het importeren van roosters werkt alleen met behulp van de planner. Er is geen gebruikersinterface ontworpen hiervoor, omdat er geen invoer van de gebruiker nodig is. Ga naar het scherm "Taakplanning" en configureer een Tim Import-taak. De taak doorloopt alle afdelingen die zijn geconfigureerd in de connectoreigenschappen en importeert alle roosters voor elke afdeling. Zie ook de Plannerhandleiding.

Voor importeren voegt de Tim-connector de volgende velden toe aan het verzoek:

*   **Periode:** De periode (datum van - datum tot) waarvoor u het rooster wilt importeren. Dit kan worden opgegeven als filtercriterium.
*   **Afdeling:** De afdeling waarvoor u het rooster wilt importeren. Afdelingen zijn configureerbaar.
*   De velden waarin u geïnteresseerd bent (zoals Persoonsinfo, RoosterCategorie, enz.) die de Tim-server moet opnemen in zijn reactie.

De importreactie bevat de volgende velden, die voldoende zijn om de uitzonderingsdagen in *LibrePlan* te beheren:

*   **Persoonsinfo:** Naam en netwerknaam.
*   **Afdeling:** De afdeling waarin de werknemer werkt.
*   **Roostercategorie:** Informatie over de aanwezigheid/afwezigheid (Aanwezig/afwezig) van de werknemer en de reden (*LibrePlan*-uitzonderingstype) in geval de werknemer afwezig is.
*   **Datum:** De datum waarop de werknemer aanwezig/afwezig is.
*   **Tijd:** De begintijd van aanwezigheid/afwezigheid, bijvoorbeeld 08:00.
*   **Duur:** Het aantal uren dat de werknemer aanwezig/afwezig is.

Bij het omzetten van de importreactie naar de "Uitzonderingsdag" van *LibrePlan* worden de volgende vertalingen in aanmerking genomen:

*   Als de roostercategorie de naam "Vakantie" bevat, wordt deze vertaald naar "RESOURCE HOLIDAY."
*   De roostercategorie "Feestdag" wordt vertaald naar "BANK HOLIDAY."
*   Al het overige, zoals "Jus uren," "PLB uren," enz., moet handmatig worden toegevoegd aan de "Kalendervrijstellingsdagen".

Bovendien is in de importreactie het rooster verdeeld in twee of drie delen per dag: bijvoorbeeld rooster-ochtend, rooster-middag en rooster-avond. *LibrePlan* staat echter slechts één "Uitzonderingstype" per dag toe. De Tim-connector is dan verantwoordelijk voor het samenvoegen van deze delen tot één uitzonderingstype. Dat wil zeggen, de roostercategorie met de langste duur wordt beschouwd als het geldige uitzonderingstype, maar de totale duur is de som van alle duren van deze categoriestukken.

In tegenstelling tot *LibrePlan* betekent in Tim Enterprise de totale duur wanneer de werknemer met vakantie is dat de werknemer niet beschikbaar is voor die totale duur. In *LibrePlan* echter, als de werknemer met vakantie is, moet de totale duur nul zijn. De Tim-connector verwerkt ook deze vertaling.

E-mailconnector
===============

E-mail is een methode voor het uitwisselen van digitale berichten van een auteur naar één of meer ontvangers.

De e-mailconnector kan worden gebruikt om verbindingseigenschappen voor Simple Mail Transfer Protocol (SMTP)-servers in te stellen.

De *e-mailconnector* moet correct worden geconfigureerd voordat deze kan worden gebruikt.

Configuratie
------------

Kies vanuit het scherm "Hoofdinstellingen" het tabblad "Connectoren". Selecteer in het connectorenscherm de e-mailconnector uit de vervolgkeuzelijst. Er wordt dan een scherm voor het bewerken van eigenschappen weergegeven.

In dit scherm kunt u de volgende eigenschapswaarden configureren:

*   **Geactiveerd:** J/N, waarmee u aangeeft of u de e-mailconnector wilt gebruiken. De standaardwaarde is "N."
*   **Protocol:** Het type SMTP-protocol.
*   **Host:** Het absolute pad naar de SMTP-server.
*   **Poort:** De poort van de SMTP-server.
*   **Afzenderadres:** Het e-mailadres van de afzender van het bericht.
*   **Gebruikersnaam:** De gebruikersnaam voor de SMTP-server.
*   **Wachtwoord:** Het wachtwoord voor de SMTP-server.

Klik ten slotte op de knop "Verbinding testen" om te controleren of u verbinding kunt maken met de SMTP-server en of uw configuraties correct zijn.

E-mailsjabloon bewerken
-----------------------

Vanuit het projectvenster, onder "Configuratie" en vervolgens "E-mailsjablonen bewerken," kunt u de e-mailsjablonen voor berichten wijzigen.

U kunt kiezen uit:

*   **Sjabloontaal:**
*   **Sjabloontype:**
*   **E-mailonderwerp:**
*   **Sjablooninhoud:**

U moet de taal opgeven omdat de webapplicatie e-mails naar gebruikers stuurt in de taal die zij hebben gekozen in hun voorkeuren. U moet het sjabloontype kiezen. Het type is de gebruikersrol, wat betekent dat deze e-mail alleen wordt verzonden naar gebruikers die de geselecteerde rol (type) hebben. U moet het e-mailonderwerp instellen. Het onderwerp is een korte samenvatting van het onderwerp van het bericht. U moet de e-mailinhoud instellen. Dit is alle informatie die u naar de gebruiker wilt sturen. Er zijn ook enkele trefwoorden die u in het bericht kunt gebruiken; de webapplicatie parseert deze en stelt een nieuwe waarde in in plaats van het trefwoord.

E-mails plannen
---------------

Het verzenden van e-mails kan alleen worden uitgevoerd via de planner. Ga naar "Configuratie" en vervolgens naar het scherm "Taakplanning". In dat scherm kunt u een taak voor het verzenden van e-mails configureren. De taak neemt een lijst van e-mailmeldingen, verzamelt gegevens en stuurt deze naar het e-mailadres van de gebruiker. Zie ook de Plannerhandleiding.

.. NOTE::

   De informatie over succes of mislukking wordt weergegeven in een pop-upvenster.
