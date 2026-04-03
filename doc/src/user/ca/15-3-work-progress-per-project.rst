Informe de Treball i Progrés per Projecte
##########################################

.. contents::

Propòsit
========

Aquest informe proporciona una visió general de l'estat dels projectes, considerant tant el progrés com el cost.

Analitza el progrés actual de cada projecte, comparant-lo amb el progrés planificat i el treball completat.

L'informe també mostra diverses ràtios relacionades amb el cost del projecte, comparant el rendiment actual amb el rendiment planificat.

Paràmetres d'Entrada i Filtres
===============================

Hi ha diversos paràmetres obligatoris:

   *   **Data de Referència:** És la data que s'utilitza com a punt de referència per comparar l'estat planificat del projecte amb el seu rendiment real. *El valor per defecte d'aquest camp és la data actual*.

   *   **Tipus de Progrés:** És el tipus de progrés que s'utilitza per mesurar el progrés del projecte. L'aplicació permet mesurar un projecte simultàniament amb diferents tipus de progrés. El tipus seleccionat per l'usuari al menú desplegable s'utilitza per calcular les dades de l'informe. El valor per defecte per al *tipus de progrés* és *repartit*, que és un tipus de progrés especial que utilitza el mètode preferit de mesura de progrés configurat per a cada element del WBS.

Els paràmetres opcionals són:

   *   **Data d'Inici:** És la data d'inici més primerenca perquè els projectes siguin inclosos a l'informe. Si aquest camp es deixa en blanc, no hi ha data d'inici mínima per als projectes.

   *   **Data de Fi:** És la data de fi més tardana perquè els projectes siguin inclosos a l'informe. Tots els projectes que acabin després de la *Data de Fi* seran exclosos.

   *   **Filtrar per Projectes:** Aquest filtre permet als usuaris seleccionar els projectes específics que s'han d'incloure a l'informe. Si no s'afegeix cap projecte al filtre, l'informe inclourà tots els projectes de la base de dades. Es proporciona un menú desplegable amb cerca per trobar el projecte desitjat. Els projectes s'afegeixen al filtre fent clic al botó *Afegir*.

Sortida
=======

El format de sortida és el següent:

Capçalera
---------

La capçalera de l'informe mostra els camps següents:

   *   **Data d'Inici:** La data d'inici del filtratge. No es mostra si l'informe no es filtra per aquest camp.
   *   **Data de Fi:** La data de fi del filtratge. No es mostra si l'informe no es filtra per aquest camp.
   *   **Tipus de Progrés:** El tipus de progrés utilitzat per a l'informe.
   *   **Projectes:** Indica els projectes filtrats per als quals es genera l'informe. Mostrarà la cadena *Tots* quan l'informe inclogui tots els projectes que satisfan els altres filtres.
   *   **Data de Referència:** La data de referència d'entrada obligatòria seleccionada per a l'informe.

Peu de Pàgina
-------------

El peu de pàgina mostra la data en què es va generar l'informe.

Cos
---

El cos de l'informe consisteix en una llista de projectes seleccionats en funció dels filtres d'entrada.

Els filtres funcionen afegint condicions, excepte per al conjunt format pels filtres de data (*Data d'Inici*, *Data de Fi*) i el *Filtrar per Projectes*. En aquest cas, si un o ambdós filtres de data estan emplenats i el *Filtrar per Projectes* té una llista de projectes seleccionats, aquest últim filtre té prioritat. Això significa que els projectes inclosos a l'informe són els proporcionats pel *Filtrar per Projectes*, independentment dels filtres de data.

És important tenir en compte que el progrés a l'informe es calcula com una fracció de la unitat, entre 0 i 1.

Per a cada projecte seleccionat per ser inclòs a la sortida de l'informe, es mostra la informació següent:

   * *Nom del Projecte*.
   * *Total d'Hores*. El total d'hores del projecte es mostra sumant les hores de cada tasca. Es mostren dos tipus de total d'hores:
      *   *Estimades (TE)*. És la suma de totes les hores estimades al WBS del projecte. Representa el nombre total d'hores estimades per completar el projecte.
      *   *Planificades (TP)*. A *LibrePlan*, és possible tenir dues quantitats diferents: el nombre estimat d'hores per a una tasca (el nombre d'hores inicialment estimat per completar la tasca) i les hores planificades (les hores assignades al pla per completar la tasca). Les hores planificades poden ser iguals, inferiors o superiors a les hores estimades i es determinen en una fase posterior, l'operació d'assignació. Per tant, el total d'hores planificades per a un projecte és la suma de totes les hores assignades per a les seves tasques.
   * *Progrés*. Es mostren tres mesures relacionades amb el progrés general del tipus especificat al filtre d'entrada de progrés per a cada projecte a la data de referència:
      *   *Mesurat (PM)*. És el progrés global considerant les mesures de progrés amb una data anterior a la *Data de Referència* als paràmetres d'entrada de l'informe. Es tenen en compte totes les tasques, i la suma és ponderada pel nombre d'hores de cada tasca.
      *   *Imputat (PI)*. És el progrés suposant que el treball continua al mateix ritme que les hores completades per a una tasca. Si s'han completat X hores de Y hores per a una tasca, el progrés imputat global es considera X/Y.
      *   *Planificat (PP)*. És el progrés global del projecte d'acord amb el calendari planificat a la data de referència. Si tot hagués succeït exactament com estava planificat, el progrés mesurat hauria de ser el mateix que el progrés planificat.
   * *Hores fins a la Data*. Hi ha dos camps que mostren el nombre d'hores fins a la data de referència des de dues perspectives:
      *   *Planificades (HP)*. Aquest número és la suma de les hores assignades a qualsevol tasca del projecte amb una data menor o igual a la *Data de Referència*.
      *   *Reals (HR)*. Aquest número és la suma de les hores informades als parts de treball per a qualsevol de les tasques del projecte amb una data menor o igual a la *Data de Referència*.
   * *Diferència*. Sota aquest encapçalament, hi ha diverses mètriques relacionades amb el cost:
      *   *Cost*. És la diferència en hores entre el nombre d'hores gastades, considerant el progrés mesurat, i les hores completades fins a la data de referència. La fórmula és: *PM*TP - HR*.
      *   *Planificat*. És la diferència entre les hores gastades d'acord amb el progrés global mesurat del projecte i el nombre planificat fins a la *Data de Referència*. Mesura l'avantatge o el retard en el temps. La fórmula és: *PM*TP - HP*.
      *   *Ràtio de Cost*. Es calcula dividint *PM* / *PI*. Si és superior a 1, significa que el projecte és rendible en aquest punt. Si és inferior a 1, significa que el projecte perd diners.
      *   *Ràtio Planificat*. Es calcula dividint *PM* / *PP*. Si és superior a 1, significa que el projecte va per davant del calendari. Si és inferior a 1, significa que el projecte va per darrere del calendari.
