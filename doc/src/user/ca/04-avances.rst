Progrés
#######

.. contents::

El progrés del projecte indica el grau en el qual s'està complint el temps de finalització estimat del projecte. El progrés de la tasca indica el grau en el qual la tasca s'està completant d'acord amb la seva finalització estimada.

En general, el progrés no es pot mesurar automàticament. Un membre del personal amb experiència o una llista de verificació ha de determinar el grau de finalització d'una tasca o projecte.

És important tenir en compte la distinció entre les hores assignades a una tasca o projecte i el progrés d'aquella tasca o projecte. Tot i que el nombre d'hores utilitzades pot ser més o menys de l'esperat, el projecte pot estar per davant o per darrere de la seva finalització estimada en el dia supervisat. D'aquests dos mesuraments poden sorgir diverses situacions:

*   **Menys hores consumides de les esperades, però el projecte va amb retard:** El progrés és inferior a l'estimat per al dia supervisat.
*   **Menys hores consumides de les esperades, i el projecte va avançat:** El progrés és superior a l'estimat per al dia supervisat.
*   **Més hores consumides de les esperades, i el projecte va amb retard:** El progrés és inferior a l'estimat per al dia supervisat.
*   **Més hores consumides de les esperades, però el projecte va avançat:** El progrés és superior a l'estimat per al dia supervisat.

La vista de planificació us permet comparar aquestes situacions mitjançant informació sobre el progrés realitzat i les hores utilitzades. Aquest capítol explicarà com introduir informació per supervisar el progrés.

La filosofia del seguiment del progrés es basa que els usuaris defineixin el nivell al qual volen supervisar els seus projectes. Per exemple, si els usuaris volen supervisar els projectes, només han d'introduir informació per als elements de nivell 1. Si volen una supervisió més precisa a nivell de tasca, han d'introduir informació de progrés a nivells inferiors. El sistema agregarà aleshores les dades cap amunt a través de la jerarquia.

Gestió de tipus de progrés
===========================

Les empreses tenen necessitats variables a l'hora de supervisar el progrés del projecte, particularment les tasques implicades. Per tant, el sistema inclou "tipus de progrés". Els usuaris poden definir diferents tipus de progrés per mesurar el progrés d'una tasca. Per exemple, una tasca es pot mesurar com a percentatge, però aquest percentatge també es pot traduir en progrés en *Tones* basat en l'acord amb el client.

Un tipus de progrés té un nom, un valor màxim i un valor de precisió:

*   **Nom:** Un nom descriptiu que els usuaris reconeixeran en seleccionar el tipus de progrés. Aquest nom hauria d'indicar clarament quin tipus de progrés s'està mesurant.
*   **Valor màxim:** El valor màxim que es pot establir per a una tasca o projecte com a mesura total del progrés. Per exemple, si esteu treballant amb *Tones* i el màxim normal és de 4000 tones, i cap tasca requerirà mai més de 4000 tones de qualsevol material, llavors 4000 seria el valor màxim.
*   **Valor de precisió:** El valor d'increment permès per al tipus de progrés. Per exemple, si el progrés en *Tones* s'ha de mesurar en nombres enters, el valor de precisió seria 1. A partir d'aquell moment, només es podran introduir nombres enters com a mesures de progrés (p. ex., 1, 2, 300).

El sistema té dos tipus de progrés predeterminats:

*   **Percentatge:** Un tipus de progrés general que mesura el progrés d'un projecte o tasca basat en un percentatge de finalització estimat. Per exemple, una tasca és un 30% completa dels 100% estimats per a un dia específic.
*   **Unitats:** Un tipus de progrés general que mesura el progrés en unitats sense especificar el tipus d'unitat. Per exemple, una tasca implica crear 3000 unitats, i el progrés és de 500 unitats del total de 3000.

.. figure:: images/tipos-avances.png
   :scale: 50

   Administració de tipus de progrés

Els usuaris poden crear nous tipus de progrés de la manera següent:

*   Aneu a la secció "Administració".
*   Feu clic a l'opció "Gestionar tipus de progrés" al menú de segon nivell.
*   El sistema mostrarà una llista de tipus de progrés existents.
*   Per a cada tipus de progrés, els usuaris poden:

    *   Editar
    *   Eliminar

*   Els usuaris poden crear un nou tipus de progrés.
*   En editar o crear un tipus de progrés, el sistema mostra un formulari amb la informació següent:

    *   Nom del tipus de progrés.
    *   Valor màxim permès per al tipus de progrés.
    *   Valor de precisió per al tipus de progrés.

Introducció de progrés basat en el tipus
=========================================

El progrés s'introdueix per als elements de projecte, però també es pot introduir mitjançant una drecera des de les tasques de planificació. Els usuaris són responsables de decidir quin tipus de progrés associar a cada element de projecte.

Els usuaris poden introduir un únic tipus de progrés predeterminat per a tot el projecte.

Abans de mesurar el progrés, els usuaris han d'associar el tipus de progrés escollit amb el projecte. Per exemple, podrien triar el progrés percentual per mesurar el progrés en la tasca completa o una taxa de progrés acordada si s'introduiran en el futur mesures de progrés acordades amb el client.

.. figure:: images/avance.png
   :scale: 40

   Pantalla d'introducció de progrés amb visualització gràfica

Per introduir mesures de progrés:

*   Seleccioneu el tipus de progrés al qual s'afegirà el progrés.
    *   Si no existeix cap tipus de progrés, cal crear-ne un de nou.
*   Al formulari que apareix sota els camps "Valor" i "Data", introduïu el valor absolut de la mesura i la data de la mesura.
*   El sistema emmagatzema automàticament les dades introduïdes.

Comparació del progrés d'un element de projecte
================================================

Els usuaris poden comparar gràficament el progrés realitzat en els projectes amb les mesures preses. Tots els tipus de progrés tenen una columna amb un botó de verificació ("Mostrar"). Quan se selecciona aquest botó, es mostra el gràfic de progrés de les mesures preses per a l'element de projecte.

.. figure:: images/contraste-avance.png
   :scale: 40

   Comparació de diversos tipus de progrés
