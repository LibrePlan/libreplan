Informes de Treball
###################

.. contents::

Els informes de treball permeten el seguiment de les hores que els recursos dediquen a les tasques a les quals estan assignats.

El programa permet als usuaris configurar nous formularis per introduir les hores dedicades, especificant els camps que volen que apareguin en aquests formularis. Això permet incorporar informes de les tasques realitzades pels treballadors i el seguiment de l'activitat dels treballadors.

Abans que els usuaris puguin afegir entrades per als recursos, han de definir almenys un tipus d'informe de treball. Aquest tipus defineix l'estructura de l'informe, incloses totes les files que s'hi afegeixen. Els usuaris poden crear tants tipus d'informes de treball com sigui necessari dins del sistema.

Tipus d'Informes de Treball
===========================

Un informe de treball consisteix en una sèrie de camps comuns a tot l'informe i un conjunt de línies d'informe de treball amb valors específics per als camps definits a cada fila. Per exemple, els recursos i les tasques són comuns a tots els informes. No obstant això, hi pot haver altres camps nous, com ara "incidents", que no es requereixen en tots els tipus d'informes.

Els usuaris poden configurar diferents tipus d'informes de treball perquè una empresa pugui dissenyar els seus informes per satisfer les seves necessitats específiques:

.. figure:: images/work-report-types.png
   :scale: 40

   Tipus d'Informes de Treball

L'administració dels tipus d'informes de treball permet als usuaris configurar aquests tipus i afegir nous camps de text o etiquetes opcionals. A la primera pestanya per editar els tipus d'informes de treball, és possible configurar el tipus per als atributs obligatoris (si s'apliquen a tot l'informe o s'especifiquen al nivell de línia) i afegir nous camps opcionals.

Els camps obligatoris que han d'aparèixer en tots els informes de treball són els següents:

*   **Nom i Codi:** Camps d'identificació per al nom del tipus d'informe de treball i el seu codi.
*   **Data:** Camp per a la data de l'informe.
*   **Recurs:** Treballador o màquina que apareix a l'informe o a la línia de l'informe de treball.
*   **Element de Projecte:** Codi de l'element de projecte al qual s'atribueix el treball realitzat.
*   **Gestió d'Hores:** Determina la política d'atribució d'hores a utilitzar, que pot ser:

    *   **Segons les Hores Assignades:** Les hores s'atribueixen en funció de les hores assignades.
    *   **Segons les Hores d'Inici i Fi:** Les hores es calculen en funció de les hores d'inici i fi.
    *   **Segons el Nombre d'Hores i el Rang d'Inici i Fi:** Es permeten discrepàncies i el nombre d'hores té prioritat.

Els usuaris poden afegir nous camps als informes:

*   **Tipus d'Etiqueta:** Els usuaris poden demanar al sistema que mostri una etiqueta en completar l'informe de treball. Per exemple, el tipus d'etiqueta de client, si l'usuari vol introduir el client per al qual s'ha realitzat el treball a cada informe.
*   **Camps Lliures:** Camps on es pot introduir text lliurement a l'informe de treball.

.. figure:: images/work-report-type.png
   :scale: 50

   Creació d'un Tipus d'Informe de Treball amb Camps Personalitzats

Els usuaris poden configurar els camps de data, recurs i element de projecte perquè apareguin a la capçalera de l'informe, la qual cosa significa que s'apliquen a tot l'informe, o es poden afegir a cadascuna de les files.

Finalment, es poden afegir nous camps de text addicionals o etiquetes als existents, a la capçalera de l'informe de treball o a cada línia, mitjançant els camps "Text addicional" i "Tipus d'etiqueta", respectivament. Els usuaris poden configurar l'ordre en què s'han d'introduir aquests elements a la pestanya "Gestió de camps addicionals i etiquetes".

Llista d'Informes de Treball
============================

Un cop s'ha configurat el format dels informes a incorporar al sistema, els usuaris poden introduir les dades al formulari creat d'acord amb l'estructura definida al tipus d'informe de treball corresponent. Per fer-ho, els usuaris han de seguir aquests passos:

*   Fer clic al botó "Nou informe de treball" associat a l'informe desitjat de la llista de tipus d'informes de treball.
*   El programa mostra aleshores l'informe basat en les configuracions donades per al tipus. Vegeu la imatge següent.

.. figure:: images/work-report-type.png
   :scale: 50

   Estructura de l'Informe de Treball Basat en el Tipus

*   Seleccioneu tots els camps mostrats per a l'informe:

    *   **Recurs:** Si s'ha triat la capçalera, el recurs només es mostra una vegada. Alternativament, per a cada línia de l'informe, cal triar un recurs.
    *   **Codi de Tasca:** Codi de la tasca a la qual s'assigna l'informe de treball. De manera similar a la resta de camps, si el camp és a la capçalera, el valor s'introdueix una vegada o tantes vegades com sigui necessari a les línies de l'informe.
    *   **Data:** Data de l'informe o de cada línia, depenent de si s'ha configurat la capçalera o la línia.
    *   **Nombre d'Hores:** El nombre d'hores de treball al projecte.
    *   **Hores d'Inici i Fi:** Hores d'inici i fi del treball per calcular les hores de treball definitives. Aquest camp només apareix en cas de les polítiques d'assignació d'hores "Segons les Hores d'Inici i Fi" i "Segons el Nombre d'Hores i el Rang d'Inici i Fi."
    *   **Tipus d'Hores:** Permet als usuaris triar el tipus d'hora, p. ex., "Normal", "Extraordinari", etc.

*   Feu clic a "Desar" o "Desar i continuar."
