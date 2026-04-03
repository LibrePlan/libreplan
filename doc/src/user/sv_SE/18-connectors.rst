Anslutningar
############

.. contents::

Anslutningar är *LibrePlan*-klientapplikationer som kan användas för att kommunicera med (webb)servrar för att hämta data, bearbeta den och lagra den. För närvarande finns det tre anslutningar: JIRA-anslutningen, Tim Enterprise-anslutningen och e-postanslutningen.

Konfiguration
=============

Anslutningar måste konfigureras korrekt innan de kan användas. De kan konfigureras från skärmen "Huvudinställningar" under fliken "Anslutningar".

Anslutningsskärmen innehåller:

*   **Nedrullningslista:** En lista över tillgängliga anslutningar.
*   **Skärm för redigering av egenskaper:** Ett formulär för redigering av egenskaper för den valda anslutningen.
*   **Knappen Testa anslutning:** En knapp för att testa anslutningen med anslutningsenheten.

Välj den anslutning du vill konfigurera från nedrullningslistan. Ett formulär för redigering av egenskaper för den valda anslutningen visas. I formuläret kan du ändra egenskapsvärden efter behov och testa dina konfigurationer med knappen "Testa anslutning".

.. NOTE::

   Egenskaperna är konfigurerade med standardvärden. Den viktigaste egenskapen är "Aktiverad." Som standard är den inställd på "N." Det indikerar att anslutningen inte kommer att användas om du inte ändrar värdet till "Y" och sparar ändringarna.

JIRA-anslutning
===============

JIRA är ett system för ärendehantering och projektspårning.

JIRA-anslutningen är en applikation som kan användas för att begära data från JIRA-webbservern om JIRA-ärenden och bearbeta svaret. Begäran baseras på JIRA-etiketter. I JIRA kan etiketter användas för att kategorisera ärenden. Begäran är strukturerad enligt följande: hämta alla ärenden som kategoriseras med detta etikettnamn.

Anslutningen tar emot svaret, som i det här fallet är ärendena, och konverterar dem till *LibrePlan*-"Projektelement" och "Tidrapporter."

*JIRA-anslutningen* måste konfigureras korrekt innan den kan användas.

Konfiguration
-------------

Från skärmen "Huvudinställningar" väljer du fliken "Anslutningar". På anslutningsskärmen väljer du JIRA-anslutningen från nedrullningslistan. En skärm för redigering av egenskaper visas sedan.

På den här skärmen kan du konfigurera följande egenskapsvärden:

*   **Aktiverad:** Y/N, anger om du vill använda JIRA-anslutningen. Standardvärdet är "N."
*   **Server-URL:** Den absoluta sökvägen till JIRA-webbservern.
*   **Användarnamn och lösenord:** Användaruppgifter för auktorisering.
*   **JIRA-etiketter: kommaseparerad lista med etiketter eller URL:** Du kan antingen ange etikettens URL eller en kommaseparerad lista med etiketter.
*   **Timtyp:** Typen av arbetstimmar. Standardvärdet är "Default."

.. NOTE::

   **JIRA-etiketter:** För närvarande stöder inte JIRA-webbservern att tillhandahålla en lista över alla tillgängliga etiketter. Som en lösning har vi utvecklat ett enkelt PHP-skript som utför en enkel SQL-fråga i JIRA-databasen för att hämta alla unika etiketter. Du kan antingen använda detta PHP-skript som "JIRA-etikett-URL" eller ange de etiketter du vill ha som kommaseparerad text i fältet "JIRA-etiketter".

Klicka slutligen på knappen "Testa anslutning" för att kontrollera att du kan ansluta till JIRA-webbservern och att dina konfigurationer är korrekta.

Synkronisering
--------------

Från projektfönstret, under "Allmänna data," kan du starta synkronisering av projektelement med JIRA-ärenden.

Klicka på knappen "Synkronisera med JIRA" för att starta synkroniseringen.

*   Om det är första gången visas ett popup-fönster (med en automatiskt ifylld lista med etiketter). I det här fönstret kan du välja en etikett att synkronisera med och klicka på knappen "Starta synkronisering" för att påbörja synkroniseringsprocessen, eller klicka på knappen "Avbryt" för att avbryta den.

*   Om en etikett redan är synkroniserad visas det senaste synkroniseringsdatumet och etiketten på JIRA-skärmen. I det här fallet visas inget popup-fönster för val av etikett. I stället startar synkroniseringsprocessen direkt för den visade (redan synkroniserade) etiketten.

.. NOTE::

   Förhållandet mellan "Projekt" och "etikett" är ett-till-ett. Endast en etikett kan synkroniseras med ett "Projekt."

.. NOTE::

   Vid lyckad (om)synkronisering skrivs informationen till databasen och JIRA-skärmen uppdateras med det senaste synkroniseringsdatumet och etiketten.

(Om)synkronisering utförs i två faser:

*   **Fas 1:** Synkronisering av projektelement, inklusive framstegsuppgifter och mätningar.
*   **Fas 2:** Synkronisering av tidrapporter.

.. NOTE::

   Om Fas 1 misslyckas utförs inte Fas 2 och ingen information skrivs till databasen.

.. NOTE::

   Informationen om lyckad eller misslyckad åtgärd visas i ett popup-fönster.

Vid lyckad synkronisering visas resultatet på fliken "Projektstruktur (WBS-uppgifter)" på skärmen "Projektdetaljer". I det här användargränssnittet finns två skillnader från standard-WBS:

*   Kolumnen "Totala uppgiftstimmar" är inte modifierbar (skrivskyddad) eftersom synkroniseringen är envägs. Uppgiftstimmar kan bara uppdateras på JIRA-webbservern.
*   Kolumnen "Kod" visar JIRA-ärendenycklarna, och de är också hyperlänkar till JIRA-ärendena. Klicka på önskad nyckel om du vill gå till dokumentet för den nyckeln (JIRA-ärende).

Schemaläggning
--------------

Omsynkronisering av JIRA-ärenden kan också utföras via schemaläggaren. Gå till skärmen "Jobbschemaläggning". På den skärmen kan du konfigurera ett JIRA-jobb för att utföra synkronisering. Jobbet söker efter de senast synkroniserade etiketterna i databasen och synkroniserar om dem. Se även Schemaläggarmanualen.

Tim Enterprise-anslutning
=========================

Tim Enterprise är en nederländsk produkt från Aenova. Det är en webbaserad applikation för administration av tid som läggs på projekt och uppgifter.

Tim-anslutningen är en applikation som kan användas för att kommunicera med Tim Enterprise-servern för att:

*   Exportera alla timmar som en anställd (användare) har lagt på ett projekt och som kan registreras i Tim Enterprise.
*   Importera alla scheman för den anställde (användaren) för att planera resursen effektivt.

*Tim-anslutningen* måste konfigureras korrekt innan den kan användas.

Konfiguration
-------------

Från skärmen "Huvudinställningar" väljer du fliken "Anslutningar". På anslutningsskärmen väljer du Tim-anslutningen från nedrullningslistan. En skärm för redigering av egenskaper visas sedan.

På den här skärmen kan du konfigurera följande egenskapsvärden:

*   **Aktiverad:** Y/N, anger om du vill använda Tim-anslutningen. Standardvärdet är "N."
*   **Server-URL:** Den absoluta sökvägen till Tim Enterprise-servern.
*   **Användarnamn och lösenord:** Användaruppgifter för auktorisering.
*   **Antal dagar tidrapport till Tim:** Antalet dagar bakåt som du vill exportera tidrapporter för.
*   **Antal dagar schema från Tim:** Antalet dagar framåt som du vill importera scheman för.
*   **Produktivitetsfaktor:** Effektiva arbetstimmar i procent. Standardvärdet är "100%."
*   **Avdelnings-ID för import av schema:** Kommaseparerade avdelnings-ID.

Klicka slutligen på knappen "Testa anslutning" för att kontrollera att du kan ansluta till Tim Enterprise-servern och att dina konfigurationer är korrekta.

Export
------

Från projektfönstret, under "Allmänna data," kan du starta export av tidrapporter till Tim Enterprise-servern.

Ange "Tim-produktkod" och klicka på knappen "Exportera till Tim" för att starta exporten.

Tim-anslutningen lägger till följande fält tillsammans med produktkoden:

*   Den anställdes/användarens fullständiga namn.
*   Det datum då den anställde arbetade med en uppgift.
*   Insatsen, eller antalet timmar som arbetats med uppgiften.
*   Ett alternativ som anger om Tim Enterprise ska uppdatera registreringen eller infoga en ny.

Tim Enterprise-svaret innehåller bara en lista med post-ID (heltal). Det gör det svårt att avgöra vad som gick fel, eftersom svarslistan bara innehåller siffror som inte är relaterade till fälten i begäran. Exportbegäran (registrering i Tim) antas ha lyckats om inga av listposterna innehåller "0"-värden. Annars har exportbegäran misslyckats för de poster som innehåller "0"-värden. Du kan därför inte se vilken begäran som misslyckades, eftersom listposterna bara innehåller värdet "0." Det enda sättet att avgöra detta är att undersöka loggfilen på Tim Enterprise-servern.

.. NOTE::

   Vid lyckad export skrivs informationen till databasen och Tim-skärmen uppdateras med det senaste exportdatumet och produktkoden.

.. NOTE::

   Informationen om lyckad eller misslyckad åtgärd visas i ett popup-fönster.

Schemalagd export
-----------------

Exportprocessen kan också utföras via schemaläggaren. Gå till skärmen "Jobbschemaläggning". På den skärmen kan du konfigurera ett Tim Export-jobb. Jobbet söker efter de senast exporterade tidrapporterna i databasen och exporterar om dem. Se även Schemaläggarmanualen.

Import
------

Import av scheman fungerar bara med hjälp av schemaläggaren. Det finns inget användargränssnitt utformat för detta, eftersom ingen inmatning krävs från användaren. Gå till skärmen "Jobbschemaläggning" och konfigurera ett Tim Import-jobb. Jobbet loopar igenom alla avdelningar som konfigurerats i anslutningsegenskaperna och importerar alla scheman för varje avdelning. Se även Schemaläggarmanualen.

För import lägger Tim-anslutningen till följande fält i begäran:

*   **Period:** Perioden (datum från - datum till) för vilken du vill importera schemat. Det här kan anges som ett filterkriterium.
*   **Avdelning:** Avdelningen för vilken du vill importera schemat. Avdelningar är konfigurerbara.
*   Fälten du är intresserad av (som personinfo, SchemakategoriCategory, osv.) som Tim-servern ska inkludera i sitt svar.

Importsvaret innehåller följande fält, vilka är tillräckliga för att hantera undantagsdagar i *LibrePlan*:

*   **Personinfo:** Namn och nätverksnamn.
*   **Avdelning:** Avdelningen som den anställde arbetar i.
*   **Schemakategori:** Information om närvaro/frånvaro (Aanwzig/afwezig) för den anställde och anledningen (*LibrePlan* undantagstyp) om den anställde är frånvarande.
*   **Datum:** Det datum då den anställde är närvarande/frånvarande.
*   **Tid:** Starttiden för närvaro/frånvaro, till exempel 08:00.
*   **Varaktighet:** Antalet timmar som den anställde är närvarande/frånvarande.

Vid konvertering av importsvaret till *LibrePlan*:s "Undantagsdag" beaktas följande översättningar:

*   Om schemakategorin innehåller namnet "Vakantie" översätts det till "RESOURCE HOLIDAY."
*   Schemakategorin "Feestdag" översätts till "BANK HOLIDAY."
*   Alla övriga, som "Jus uren," "PLB uren," osv., bör läggas till i "Kalenderundantagsdagar" manuellt.

Dessutom är schemat i importsvaret uppdelat i två eller tre delar per dag: till exempel schema-morgon, schema-eftermiddag och schema-kväll. *LibrePlan* tillåter dock bara en "Undantagstyp" per dag. Tim-anslutningen ansvarar då för att slå samman dessa delar till en undantagstyp. Det vill säga att schemakategorin med längst varaktighet antas vara en giltig undantagstyp, men den totala varaktigheten är summan av alla varaktigheter för dessa kategoridelar.

Till skillnad från *LibrePlan* innebär den totala varaktigheten i Tim Enterprise när en anställd är på semester att den anställde inte är tillgänglig under denna totala varaktighet. I *LibrePlan* ska dock den totala varaktigheten vara noll om den anställde är på semester. Tim-anslutningen hanterar även denna översättning.

E-postanslutning
================

E-post är en metod för att utbyta digitala meddelanden från en avsändare till en eller flera mottagare.

E-postanslutningen kan användas för att ange anslutningsegenskaper för SMTP-servern (Simple Mail Transfer Protocol).

*E-postanslutningen* måste konfigureras korrekt innan den kan användas.

Konfiguration
-------------

Från skärmen "Huvudinställningar" väljer du fliken "Anslutningar". På anslutningsskärmen väljer du E-postanslutningen från nedrullningslistan. En skärm för redigering av egenskaper visas sedan.

På den här skärmen kan du konfigurera följande egenskapsvärden:

*   **Aktiverad:** Y/N, anger om du vill använda E-postanslutningen. Standardvärdet är "N."
*   **Protokoll:** Typen av SMTP-protokoll.
*   **Värd:** Den absoluta sökvägen till SMTP-servern.
*   **Port:** Porten för SMTP-servern.
*   **Från-adress:** E-postadressen för meddelandets avsändare.
*   **Användarnamn:** Användarnamnet för SMTP-servern.
*   **Lösenord:** Lösenordet för SMTP-servern.

Klicka slutligen på knappen "Testa anslutning" för att kontrollera att du kan ansluta till SMTP-servern och att dina konfigurationer är korrekta.

Redigera e-postmall
-------------------

Från projektfönstret, under "Konfiguration" och sedan "Redigera e-postmallar," kan du ändra e-postmallarna för meddelanden.

Du kan välja:

*   **Mallspråk:**
*   **Malltyp:**
*   **E-postämne:**
*   **Mallinnehåll:**

Du måste ange språket eftersom webbapplikationen skickar e-postmeddelanden till användare på det språk de har valt i sina inställningar. Du måste välja malltypen. Typen är användarrollen, vilket innebär att det här e-postmeddelandet bara skickas till användare som är i den valda rollen (typen). Du måste ange e-postämnet. Ämnet är en kort sammanfattning av meddelandets ämne. Du måste ange e-postinnehållet. Det är vilken information som helst som du vill skicka till användaren. Det finns också några nyckelord som du kan använda i meddelandet; webbapplikationen tolkar dem och anger ett nytt värde i stället för nyckelordet.

Schemaläggning av e-postmeddelanden
------------------------------------

Sändning av e-postmeddelanden kan bara utföras via schemaläggaren. Gå till "Konfiguration," sedan skärmen "Jobbschemaläggning". På den skärmen kan du konfigurera ett e-postsändningsjobb. Jobbet tar en lista med e-postmeddelanden, samlar in data och skickar det till användarens e-postadress. Se även Schemaläggarmanualen.

.. NOTE::

   Informationen om lyckad eller misslyckad åtgärd visas i ett popup-fönster.
