Schemaläggare
#############

.. contents::

Schemaläggaren är utformad för att schemalägga jobb dynamiskt. Den är utvecklad med *Spring Framework Quartz scheduler*.

För att använda schemaläggaren effektivt måste de jobb (Quartz-jobb) som ska schemaläggas skapas först. Dessa jobb kan sedan läggas till i databasen, eftersom alla jobb som ska schemaläggas lagras i databasen.

När schemaläggaren startar läser den de jobb som ska schemaläggas eller avschemalägas från databasen och schemalägger eller tar bort dem. Därefter kan jobb läggas till, uppdateras eller tas bort dynamiskt via användargränssnittet ``Jobbschemaläggning``.

.. NOTE::
   Schemaläggaren startar när LibrePlan-webbapplikationen startar och stoppas när applikationen stoppas.

.. NOTE::
   Denna schemaläggare stöder bara ``cron-uttryck`` för att schemalägga jobb.

Kriterierna som schemaläggaren använder för att schemalägga eller ta bort jobb när den startar är följande:

För alla jobb:

* Schemalägg

  * Jobbet har en *Anslutning*, och *Anslutningen* är aktiverad, och jobbet tillåts att schemaläggas.
  * Jobbet har ingen *Anslutning* och tillåts att schemaläggas.

* Ta bort

  * Jobbet har en *Anslutning*, och *Anslutningen* är inte aktiverad.
  * Jobbet har en *Anslutning*, och *Anslutningen* är aktiverad, men jobbet tillåts inte att schemaläggas.
  * Jobbet har ingen *Anslutning* och tillåts inte att schemaläggas.

.. NOTE::
   Jobb kan inte omschemalägas eller avschemalägas om de för närvarande körs.

Listvy för jobbschemaläggning
==============================

Listvyn ``Jobbschemaläggning`` gör det möjligt för användare att:

*   Lägga till ett nytt jobb.
*   Redigera ett befintligt jobb.
*   Ta bort ett jobb.
*   Starta en process manuellt.

Lägg till eller redigera jobb
==============================

Från listvyn ``Jobbschemaläggning`` klickar du på:

*   ``Skapa`` för att lägga till ett nytt jobb, eller
*   ``Redigera`` för att ändra det valda jobbet.

Båda åtgärderna öppnar ett ``jobbformulär`` för att skapa/redigera. ``Formuläret`` visar följande egenskaper:

*   Fält:

    *   **Jobbgrupp:** Jobbgruppens namn.
    *   **Jobbnamn:** Jobbets namn.
    *   **Cron-uttryck:** Ett skrivskyddat fält med en ``Redigera``-knapp för att öppna inmatningsfönstret för ``cron-uttryck``.
    *   **Jobbklassnamn:** En ``nedrullningslista`` för att välja jobbet (ett befintligt jobb).
    *   **Anslutning:** En ``nedrullningslista`` för att välja en anslutning. Det här är inte obligatoriskt.
    *   **Schema:** En kryssruta som anger om jobbet ska schemaläggas.

*   Knappar:

    *   **Spara:** Sparar eller uppdaterar ett jobb i både databasen och schemaläggaren. Användaren återgår sedan till ``Listvyn för jobbschemaläggning``.
    *   **Spara och fortsätt:** Samma som "Spara," men användaren återgår inte till ``Listvyn för jobbschemaläggning``.
    *   **Avbryt:** Ingenting sparas och användaren återgår till ``Listvyn för jobbschemaläggning``.

*   Och ett tippsavsnitt om syntaxen för cron-uttryck.

Popup-fönster för cron-uttryck
------------------------------

För att ange ``cron-uttrycket`` korrekt används ett popup-formulär för ``cron-uttryck``. I det här formuläret kan du ange det önskade ``cron-uttrycket``. Se även tipset om ``cron-uttrycket``. Om du anger ett ogiltigt ``cron-uttryck`` meddelas du omedelbart.

Ta bort jobb
============

Klicka på knappen ``Ta bort`` för att radera jobbet från både databasen och schemaläggaren. Resultatet av den här åtgärden (lyckad eller misslyckad) visas.

Starta jobb manuellt
====================

Som ett alternativ till att vänta på att jobbet ska köras enligt schemat kan du klicka på den här knappen för att starta processen direkt. Därefter visas information om lyckad eller misslyckad åtgärd i ett ``popup-fönster``.
