Planificador
############

.. contents::

El planificador està dissenyat per planificar treballs de manera dinàmica. S'ha desenvolupat usant el *Spring Framework Quartz scheduler*.

Per usar aquest planificador de manera efectiva, primer cal crear els treballs (treballs Quartz) que s'han de planificar. Després, aquests treballs es poden afegir a la base de dades, ja que tots els treballs a planificar s'emmagatzemen a la base de dades.

Quan el planificador s'inicia, llegeix de la base de dades els treballs que cal planificar o desplanificar i els planifica o elimina en conseqüència. Posteriorment, els treballs es poden afegir, actualitzar o eliminar de manera dinàmica usant la interfície d'usuari ``Planificació de treballs``.

.. NOTE::
   El planificador s'inicia quan s'inicia l'aplicació web LibrePlan i s'atura quan l'aplicació s'atura.

.. NOTE::
   Aquest planificador admet únicament ``expressions cron`` per planificar treballs.

Els criteris que el planificador usa per planificar o eliminar treballs quan s'inicia són els següents:

Per a tots els treballs:

* Planificar

  * El treball té un *Connector*, el *Connector* està activat i el treball té permís per ser planificat.
  * El treball no té *Connector* i té permís per ser planificat.

* Eliminar

  * El treball té un *Connector* i el *Connector* no està activat.
  * El treball té un *Connector*, el *Connector* està activat, però el treball no té permís per ser planificat.
  * El treball no té *Connector* i no té permís per ser planificat.

.. NOTE::
   Els treballs no es poden replanificar ni desplanificar si s'estan executant en aquell moment.

Vista de llista de planificació de treballs
===========================================

La vista ``Llista de planificació de treballs`` permet als usuaris:

*   Afegir un nou treball.
*   Editar un treball existent.
*   Eliminar un treball.
*   Iniciar un procés manualment.

Afegir o editar un treball
==========================

Des de la vista ``Llista de planificació de treballs``, feu clic a:

*   ``Crear`` per afegir un nou treball, o
*   ``Editar`` per modificar el treball seleccionat.

Ambdues accions obriran un ``formulari`` de creació/edició de treball. El ``formulari`` mostra les propietats següents:

*   Camps:

    *   **Grup de treballs:** El nom del grup de treballs.
    *   **Nom del treball:** El nom del treball.
    *   **Expressió cron:** Un camp de només lectura amb un botó ``Editar`` per obrir la finestra d'entrada de l'``expressió cron``.
    *   **Nom de la classe del treball:** Una ``llista desplegable`` per seleccionar el treball (un treball existent).
    *   **Connector:** Una ``llista desplegable`` per seleccionar un connector. No és obligatori.
    *   **Planificar:** Una casella de selecció per indicar si cal planificar aquest treball.

*   Botons:

    *   **Desar:** Per desar o actualitzar un treball tant a la base de dades com al planificador. L'usuari torna llavors a la ``vista Llista de planificació de treballs``.
    *   **Desar i continuar:** Igual que "Desar", però l'usuari no torna a la ``vista Llista de planificació de treballs``.
    *   **Cancel·lar:** No es desa res i l'usuari torna a la ``vista Llista de planificació de treballs``.

*   I una secció d'ajuda sobre la sintaxi de les expressions cron.

Finestra emergent de l'expressió cron
--------------------------------------

Per introduir correctament l'``expressió cron``, s'usa un formulari emergent d'``expressió cron``. En aquest formulari, podeu introduir l'``expressió cron`` desitjada. Vegeu també l'ajuda sobre l'``expressió cron``. Si introduïu una ``expressió cron`` no vàlida, se us notificarà immediatament.

Eliminar un treball
===================

Feu clic al botó ``Eliminar`` per suprimir el treball tant de la base de dades com del planificador. Es mostrarà l'èxit o el fracàs d'aquesta acció.

Iniciar un treball manualment
==============================

Com a alternativa a esperar que el treball s'executi segons la planificació, podeu fer clic en aquest botó per iniciar el procés directament. Posteriorment, la informació sobre l'èxit o el fracàs es mostrarà en una ``finestra emergent``.
