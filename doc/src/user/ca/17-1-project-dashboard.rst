Tauler de Control del Projecte
##############################

.. contents::

El tauler de control del projecte és una perspectiva de *LibrePlan* que conté un conjunt d'**ICP (Indicadors Clau de Rendiment)** per ajudar a avaluar el rendiment d'un projecte en termes de:

   *   Progrés del treball
   *   Cost
   *   Estat dels recursos assignats
   *   Restriccions de temps

Indicadors de Rendiment del Progrés
=====================================

Es calculen dos indicadors: el percentatge de progrés del projecte i l'estat de les tasques.

Percentatge de Progrés del Projecte
-------------------------------------

Aquest gràfic mostra el progrés global d'un projecte, comparant-lo amb el progrés esperat basat en el gràfic *Gantt*.

El progrés es representa amb dues barres:

   *   *Progrés Actual:* El progrés actual basat en les mesures preses.
   *   *Progrés Esperat:* El progrés que el projecte hauria d'haver assolit en aquest punt, d'acord amb el pla del projecte.

Per veure el valor mesurat real de cada barra, passeu el cursor del ratolí per sobre de la barra.

El progrés global del projecte s'estima utilitzant diversos mètodes diferents, ja que no hi ha un únic enfocament universalment correcte:

   *   **Progrés Repartit:** És el tipus de progrés establert com a progrés repartit al nivell del projecte. En aquest cas, no hi ha manera de calcular un valor esperat, i només es mostra la barra actual.
   *   **Per Totes les Hores de Tasca:** El progrés de totes les tasques del projecte es fa la mitjana per calcular el valor global. És una mitjana ponderada que considera el nombre d'hores assignades a cada tasca.
   *   **Per les Hores del Camí Crític:** El progrés de les tasques que pertanyen a qualsevol dels camins crítics del projecte es fa la mitjana per obtenir el valor global. És una mitjana ponderada que considera el total d'hores assignades per a cada tasca implicada.
   *   **Per la Durada del Camí Crític:** El progrés de les tasques que pertanyen a qualsevol dels camins crítics es fa la mitjana ponderada, però aquesta vegada considerant la durada de cada tasca implicada en lloc de les hores assignades.

Estat de les Tasques
--------------------

Un gràfic de sectors mostra el percentatge de tasques del projecte en diferents estats. Els estats definits són:

   *   **Acabades:** Tasques completades, identificades per un valor de progrés del 100%.
   *   **En Curs:** Tasques que estan en curs. Aquestes tasques tenen un valor de progrés diferent del 0% o del 100%, o s'ha registrat algun temps de treball.
   *   **Llestes per Iniciar:** Tasques amb un 0% de progrés, sense temps registrat, totes les seves tasques dependents *FINISH_TO_START* estan *acabades*, i totes les seves tasques dependents *START_TO_START* estan *acabades* o *en curs*.
   *   **Bloquejades:** Tasques amb un 0% de progrés, sense temps registrat, i amb tasques dependents anteriors que no estan ni *en curs* ni en l'estat *llesta per iniciar*.

Indicadors de Cost
==================

Es calculen diversos indicadors de cost de *Gestió del Valor Guanyat*:

   *   **CV (Variació del Cost):** La diferència entre la *corba del Valor Guanyat* i la *corba del Cost Real* en el moment actual. Els valors positius indiquen un benefici, i els valors negatius indiquen una pèrdua.
   *   **ACWP (Cost Real del Treball Realitzat):** El nombre total d'hores registrades al projecte en el moment actual.
   *   **CPI (Índex de Rendiment del Cost):** La ràtio *Valor Guanyat / Cost Real*.

        *   > 100 és favorable, indicant que el projecte està per sota del pressupost.
        *   = 100 també és favorable, indicant que el cost s'ajusta exactament al pla.
        *   < 100 és desfavorable, indicant que el cost de completar el treball és superior al planificat.
   *   **ETC (Estimació per Completar):** El temps restant per completar el projecte.
   *   **BAC (Pressupost a la Finalització):** La quantitat total de treball assignat al pla del projecte.
   *   **EAC (Estimació a la Finalització):** La projecció del gestor del cost total a la finalització del projecte, basada en el *CPI*.
   *   **VAC (Variació a la Finalització):** La diferència entre el *BAC* i el *EAC*.

        *   < 0 indica que el projecte supera el pressupost.
        *   > 0 indica que el projecte està per sota del pressupost.

Recursos
========

Per analitzar el projecte des del punt de vista dels recursos, es proporcionen dues ràtios i un histograma.

Histograma de Desviació de l'Estimació en Tasques Acabades
----------------------------------------------------------

Aquest histograma calcula la desviació entre el nombre d'hores assignades a les tasques del projecte i el nombre real d'hores dedicades a elles.

La desviació es calcula com un percentatge per a totes les tasques acabades, i les desviacions calculades es representen en un histograma. L'eix vertical mostra el nombre de tasques dins de cada interval de desviació. Es calculen dinàmicament sis intervals de desviació.

Ràtio d'Hores Extres
--------------------

Aquesta ràtio resumeix la sobrecàrrega dels recursos assignats a les tasques del projecte. Es calcula amb la fórmula: **ràtio d'hores extres = sobrecàrrega / (càrrega + sobrecàrrega)**.

   *   = 0 és favorable, indicant que els recursos no estan sobrecarregats.
   *   > 0 és desfavorable, indicant que els recursos estan sobrecarregats.

Ràtio de Disponibilitat
-----------------------

Aquesta ràtio resumeix la capacitat lliure dels recursos assignats actualment al projecte. Per tant, mesura la disponibilitat dels recursos per rebre més assignacions sense quedar sobrecarregats. Es calcula com: **ràtio de disponibilitat = (1 - càrrega/capacitat) * 100**

   *   Els valors possibles estan entre el 0% (totalment assignat) i el 100% (no assignat).

Temps
=====

S'inclouen dos gràfics: un histograma per a la desviació de temps en el temps d'acabament de les tasques del projecte i un gràfic de sectors per a les violacions de termini.

Avançament o Retard en la Finalització de Tasques
--------------------------------------------------

Aquest càlcul determina la diferència en dies entre el temps d'acabament planificat de les tasques del projecte i el seu temps d'acabament real. La data de finalització planificada es pren del gràfic *Gantt*, i la data de finalització real es pren de l'últim temps registrat per a la tasca.

El retard o avançament en la finalització de les tasques es representa en un histograma. L'eix vertical mostra el nombre de tasques amb un valor de diferència de dies d'avançament/retard corresponent a l'interval de dies de l'abscissa. Es calculen sis intervals dinàmics de desviació en la finalització de tasques.

   *   Els valors negatius signifiquen acabar per davant del calendari.
   *   Els valors positius signifiquen acabar per darrere del calendari.

Violacions de Termini
---------------------

Aquesta secció calcula el marge amb el termini del projecte, si n'hi ha. A més, un gràfic de sectors mostra el percentatge de tasques que compleixen el seu termini. El gràfic inclou tres tipus de valors:

   *   Percentatge de tasques sense un termini configurat.
   *   Percentatge de tasques acabades amb una data de finalització real posterior al seu termini. La data de finalització real es pren de l'últim temps registrat per a la tasca.
   *   Percentatge de tasques acabades amb una data de finalització real anterior al seu termini.
