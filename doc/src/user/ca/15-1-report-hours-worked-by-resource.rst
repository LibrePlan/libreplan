Informe d'Hores Treballades per Recurs
#######################################

.. contents::

Propòsit
========

Aquest informe extreu una llista de tasques i el temps que els recursos hi han dedicat dins d'un període especificat. Diversos filtres permeten als usuaris refinar la consulta per obtenir només la informació desitjada i excloure les dades innecessàries.

Paràmetres d'Entrada i Filtres
===============================

* **Dates**.
    * *Tipus*: Opcional.
    * *Dos camps de data*:
        * *Data d'Inici:* És la data més primerenca perquè els parts de treball siguin inclosos. Els parts de treball amb dates anteriors a la *Data d'Inici* s'exclouen. Si aquest paràmetre es deixa en blanc, els parts de treball no es filtren per la *Data d'Inici*.
        * *Data de Fi:* És la data més tardana perquè els parts de treball siguin inclosos. Els parts de treball amb dates posteriors a la *Data de Fi* s'exclouen. Si aquest paràmetre es deixa en blanc, els parts de treball no es filtren per la *Data de Fi*.

*   **Filtrar per Treballadors:**
    *   *Tipus:* Opcional.
    *   *Com funciona:* Podeu seleccionar un o més treballadors per restringir els parts de treball al temps registrat per aquells treballadors específics. Per afegir un treballador com a filtre, cerqueu-lo al selector i feu clic al botó *Afegir*. Si aquest filtre es deixa buit, es recuperen els parts de treball independentment del treballador.

*   **Filtrar per Etiquetes:**
    *   *Tipus:* Opcional.
    *   *Com funciona:* Podeu afegir una o més etiquetes per usar-les com a filtres cercant-les al selector i fent clic al botó *Afegir*. Aquestes etiquetes s'utilitzen per seleccionar les tasques que s'han d'incloure als resultats en calcular les hores dedicades. Aquest filtre es pot aplicar als fulls de temps, les tasques, ambdós, o cap.

*   **Filtrar per Criteris:**
    *   *Tipus:* Opcional.
    *   *Com funciona:* Podeu seleccionar un o més criteris cercant-los al selector i fent clic al botó *Afegir*. Aquests criteris s'utilitzen per seleccionar els recursos que compleixen almenys un d'ells. L'informe mostrarà tot el temps dedicat pels recursos que compleixen un dels criteris seleccionats.

Sortida
=======

Capçalera
---------

La capçalera de l'informe mostra els filtres que s'han configurat i aplicat a l'informe actual.

Peu de Pàgina
-------------

La data en què es va generar l'informe es llista al peu de pàgina.

Cos
---

El cos de l'informe consisteix en diversos grups d'informació.

*   El primer nivell d'agregació és per recurs. Tot el temps dedicat per un recurs es mostra juntament sota la capçalera. Cada recurs s'identifica per:

    *   *Treballador:* Cognoms, Nom.
    *   *Màquina:* Nom.

    Una línia de resum mostra el nombre total d'hores treballades pel recurs.

*   El segon nivell d'agrupació és per *data*. Tots els informes d'un recurs específic en la mateixa data es mostren junts.

    Una línia de resum mostra el nombre total d'hores treballades pel recurs en aquella data.

*   El nivell final llista els parts de treball del treballador en aquell dia. La informació mostrada per a cada línia de part de treball és:

    *   *Codi de Tasca:* El codi de la tasca a la qual s'atribueixen les hores registrades.
    *   *Nom de Tasca:* El nom de la tasca a la qual s'atribueixen les hores registrades.
    *   *Hora d'Inici:* Aquesta és opcional. És l'hora en què el recurs va començar a treballar en la tasca.
    *   *Hora de Fi:* Aquesta és opcional. És l'hora en què el recurs va acabar de treballar en la tasca en la data especificada.
    *   *Camps de Text:* Aquesta és opcional. Si la línia del part de treball té camps de text, els valors emplenats es mostren aquí. El format és: <Nom del camp de text>:<Valor>
    *   *Etiquetes:* Depèn de si el model de part de treball té un camp d'etiqueta en la seva definició. Si hi ha múltiples etiquetes, es mostren a la mateixa columna. El format és: <Nom del tipus d'etiqueta>:<Valor de l'etiqueta>
