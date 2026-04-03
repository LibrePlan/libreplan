Framsteg
########

.. contents::

Projektframsteg indikerar i vilken grad den beräknade färdigställandetiden för projektet uppfylls. Uppgiftsframsteg indikerar i vilken grad uppgiften slutförs enligt sin beräknade tid för färdigställande.

I allmänhet kan framsteg inte mätas automatiskt. En erfaren medarbetare eller en checklista måste bestämma graden av färdigställande för en uppgift eller ett projekt.

Det är viktigt att notera skillnaden mellan de timmar som tilldelats en uppgift eller ett projekt och framstegen för den uppgiften eller projektet. Medan antalet använda timmar kan vara mer eller mindre än förväntat, kan projektet vara före eller efter sin beräknade färdigställande på den övervakade dagen. Flera situationer kan uppstå från dessa två mätningar:

*   **Färre timmar förbrukade än förväntat, men projektet är försenat:** Framstegen är lägre än beräknat för den övervakade dagen.
*   **Färre timmar förbrukade än förväntat, och projektet är i förväg:** Framstegen är högre än beräknat för den övervakade dagen.
*   **Fler timmar förbrukade än förväntat, och projektet är försenat:** Framstegen är lägre än beräknat för den övervakade dagen.
*   **Fler timmar förbrukade än förväntat, men projektet är i förväg:** Framstegen är högre än beräknat för den övervakade dagen.

Planeringsvyn låter dig jämföra dessa situationer med hjälp av information om de framsteg som gjorts och de timmar som använts. Det här kapitlet förklarar hur man anger information för att övervaka framsteg.

Filosofin bakom framstegsövervakning baseras på att användare definierar den nivå på vilken de vill övervaka sina projekt. Om användare till exempel vill övervaka projekt behöver de bara ange information för element på nivå 1. Om de vill ha mer exakt övervakning på uppgiftsnivå måste de ange framstegsinformation på lägre nivåer. Systemet aggregerar sedan data uppåt genom hierarkin.

Hantera framstegstyper
=======================

Företag har varierande behov när de övervakar projektframsteg, särskilt de inblandade uppgifterna. Därför innehåller systemet "framstegstyper." Användare kan definiera olika framstegstyper för att mäta en uppgifts framsteg. En uppgift kan till exempel mätas som en procentandel, men denna procentandel kan också översättas till framsteg i *ton* baserat på avtalet med klienten.

En framstegstyp har ett namn, ett maximivärde och ett precisionsvärde:

*   **Namn:** Ett beskrivande namn som användare känner igen när de väljer framstegstypen. Det här namnet bör tydligt ange vilken typ av framsteg som mäts.
*   **Maximivärde:** Det maximala värde som kan fastställas för en uppgift eller ett projekt som den totala framstegsmätningen. Om du till exempel arbetar med *ton* och det normala maximumet är 4 000 ton, och ingen uppgift någonsin kräver mer än 4 000 ton av något material, då är 4 000 det maximala värdet.
*   **Precisionsvärde:** Det inkrementvärde som är tillåtet för framstegstypen. Om framsteg i *ton* till exempel ska mätas i hela tal, är precisionsvärdet 1. Från och med det kan bara hela tal anges som framstegsmätningar (t.ex. 1, 2, 300).

Systemet har två standardframstegstyper:

*   **Procent:** En allmän framstegstyp som mäter framstegen för ett projekt eller en uppgift baserat på en beräknad färdigställandeprocentandel. En uppgift är till exempel 30% klar av de 100% som beräknats för en specifik dag.
*   **Enheter:** En allmän framstegstyp som mäter framsteg i enheter utan att ange typen av enhet. En uppgift innebär till exempel att skapa 3 000 enheter, och framstegen är 500 enheter av totalt 3 000.

.. figure:: images/tipos-avances.png
   :scale: 50

   Administration av framstegstyper

Användare kan skapa nya framstegstyper på följande sätt:

*   Gå till avsnittet "Administration".
*   Klicka på alternativet "Hantera typer av framsteg" i menyn på andra nivån.
*   Systemet visar en lista över befintliga framstegstyper.
*   För varje framstegstyp kan användare:

    *   Redigera
    *   Ta bort

*   Användare kan sedan skapa en ny framstegstyp.
*   Vid redigering eller skapande av en framstegstyp visar systemet ett formulär med följande information:

    *   Namn på framstegstypen.
    *   Maximalt tillåtet värde för framstegstypen.
    *   Precisionsvärde för framstegstypen.

Ange framsteg baserat på typ
=============================

Framsteg anges för projektelement, men det kan också anges med en genväg från planeringsuppgifterna. Användare ansvarar för att bestämma vilken framstegstyp som ska associeras med varje projektelement.

Användare kan ange en enda, standardframstegstyp för hela projektet.

Innan framsteg mäts måste användare associera den valda framstegstypen med projektet. De kan till exempel välja procentframsteg för att mäta framsteg på hela uppgiften eller en avtalad framstegstakt om framstegsmätningar som överenskommits med klienten ska anges i framtiden.

.. figure:: images/avance.png
   :scale: 40

   Skärm för framstegsregistrering med grafisk visualisering

För att ange framstegsmätningar:

*   Välj den framstegstyp till vilken framsteg ska läggas till.
    *   Om ingen framstegstyp finns måste en ny skapas.
*   I det formulär som visas under fälten "Värde" och "Datum", ange det absoluta värdet för mätningen och datumet för mätningen.
*   Systemet lagrar automatiskt de angivna uppgifterna.

Jämföra framsteg för ett projektelement
=============================================

Användare kan grafiskt jämföra de framsteg som gjorts på projekt med de mätningar som tagits. Alla framstegstyper har en kolumn med en kryssknapp ("Visa"). När den här knappen är markerad visas framstegsdiagrammet för mätningar tagna för projektelementet.

.. figure:: images/contraste-avance.png
   :scale: 40

   Jämförelse av flera framstegstyper
