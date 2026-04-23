Etiketter
#########

.. contents::

Etiketter är entiteter som används i programmet för att konceptuellt organisera uppgifter eller projektelement.

Etiketter kategoriseras enligt etiketttyp. En etikett kan bara tillhöra en etiketttyp; användare kan dock skapa många liknande etiketter som tillhör olika etiketttyper.

Etiketttyper
============

Etiketttyper används för att gruppera de typer av etiketter som användare vill hantera i programmet. Här är några exempel på möjliga etiketttyper:

*   **Klient:** Användare kan vara intresserade av att märka uppgifter, projekt eller projektelement i relation till den klient som begär dem.
*   **Område:** Användare kan vara intresserade av att märka uppgifter, projekt eller projektelement i relation till de områden där de utförs.

Administration av etiketttyper hanteras från menyalternativet "Administration". Det är här användare kan redigera etiketttyper, skapa nya etiketttyper och lägga till etiketter i etiketttyper. Användare kan komma åt etikettlistan från det här alternativet.

.. figure:: images/tag-types-list.png
   :scale: 50

   Lista över etiketttyper

Från listan över etiketttyper kan användare:

*   Skapa en ny etiketttyp.
*   Redigera en befintlig etiketttyp.
*   Ta bort en etiketttyp med alla dess etiketter.

Redigering och skapande av etiketter delar samma formulär. Från det här formuläret kan användaren tilldela ett namn till etiketttypen, skapa eller ta bort etiketter och lagra ändringarna. Förfarandet är följande:

*   Välj en etikett att redigera eller klicka på skapandeknappen för en ny.
*   Systemet visar ett formulär med en textinmatning för namnet och en lista med textinmatningar med befintliga och tilldelade etiketter.
*   Om användare vill lägga till en ny etikett måste de klicka på knappen "Ny etikett".
*   Systemet visar en ny rad i listan med en tom textruta som användare måste redigera.
*   Användare anger ett namn för etiketten.
*   Systemet lägger till namnet i listan.
*   Användare klickar på "Spara" eller "Spara och fortsätt" för att fortsätta redigera formuläret.

.. figure:: images/tag-types-edition.png
   :scale: 50

   Redigering av etiketttyper

Etiketter
=========

Etiketter är entiteter som tillhör en etiketttyp. Dessa entiteter kan tilldelas projektelement. Att tilldela en etikett till ett projektelement innebär att alla element som härstammar från detta element ärver den etikett de tillhör. Att ha en tilldelad etikett innebär att dessa entiteter kan filtreras där sökningar kan utföras:

*   Söka efter uppgifter i Gantt-diagrammet.
*   Söka efter projektelement i listan över projektelement.
*   Filter för rapporter.

Tilldelningen av etiketter till projektelement behandlas i kapitlet om projekt.
