Gestió de Costos
#################

.. _costes:
.. contents::

Costos
======

La gestió de costos permet als usuaris estimar els costos dels recursos utilitzats en un projecte. Per gestionar els costos, cal definir les entitats següents:

*   **Tipus d'Hores:** Indiquen els tipus d'hores treballades per un recurs. Els usuaris poden definir tipus d'hores tant per a màquines com per a treballadors. Alguns exemples de tipus d'hores inclouen: "Hores addicionals pagades a 20 € per hora." Es poden definir els camps següents per als tipus d'hores:

    *   **Codi:** Codi extern del tipus d'hora.
    *   **Nom:** Nom del tipus d'hora. Per exemple, "Addicional."
    *   **Tarifa per Defecte:** Tarifa base per defecte del tipus d'hora.
    *   **Activació:** Indica si el tipus d'hora és actiu o no.

*   **Categories de Cost:** Les categories de cost defineixen els costos associats a diferents tipus d'hores durant períodes específics (que poden ser indefinits). Per exemple, el cost de les hores addicionals per a treballadors qualificats de primer grau l'any vinent és de 24 € per hora. Les categories de cost inclouen:

    *   **Nom:** Nom de la categoria de cost.
    *   **Activació:** Indica si la categoria és activa o no.
    *   **Llista de Tipus d'Hores:** Aquesta llista defineix els tipus d'hores inclosos en la categoria de cost. Especifica els períodes i les tarifes per a cada tipus d'hora. Per exemple, a mesura que canvien les tarifes, cada any es pot incloure en aquesta llista com un període de tipus d'hora, amb una tarifa horària específica per a cada tipus d'hora (que pot diferir de la tarifa horària per defecte d'aquell tipus d'hora).

Gestió dels Tipus d'Hores
--------------------------

Els usuaris han de seguir aquests passos per registrar els tipus d'hores:

*   Seleccioneu "Gestionar tipus d'hores treballades" al menú "Administració."
*   El programa mostra una llista dels tipus d'hores existents.

.. figure:: images/hour-type-list.png
   :scale: 35

   Llista de Tipus d'Hores

*   Feu clic a "Editar" o "Crear."
*   El programa mostra un formulari d'edició del tipus d'hora.

.. figure:: images/hour-type-edit.png
   :scale: 50

   Edició de Tipus d'Hores

*   Els usuaris poden introduir o canviar:

    *   El nom del tipus d'hora.
    *   El codi del tipus d'hora.
    *   La tarifa per defecte.
    *   Activació/desactivació del tipus d'hora.

*   Feu clic a "Desar" o "Desar i continuar."

Categories de Cost
------------------

Els usuaris han de seguir aquests passos per registrar les categories de cost:

*   Seleccioneu "Gestionar categories de cost" al menú "Administració."
*   El programa mostra una llista de les categories existents.

.. figure:: images/category-cost-list.png
   :scale: 50

   Llista de Categories de Cost

*   Feu clic al botó "Editar" o "Crear."
*   El programa mostra un formulari d'edició de la categoria de cost.

.. figure:: images/category-cost-edit.png
   :scale: 50

   Edició de Categories de Cost

*   Els usuaris introdueixen o canvien:

    *   El nom de la categoria de cost.
    *   L'activació/desactivació de la categoria de cost.
    *   La llista de tipus d'hores inclosos a la categoria. Tots els tipus d'hores tenen els camps següents:

        *   **Tipus d'Hora:** Trieu un dels tipus d'hores existents al sistema. Si no n'hi ha cap, s'ha de crear un tipus d'hora (aquest procés s'explica a la subsecció anterior).
        *   **Data d'Inici i Fi:** Les dates d'inici i fi (aquesta última és opcional) per al període que s'aplica a la categoria de cost.
        *   **Tarifa Horària:** La tarifa horària per a aquesta categoria específica.

*   Feu clic a "Desar" o "Desar i continuar."

L'assignació de categories de cost als recursos es descriu al capítol sobre recursos. Aneu a la secció "Recursos."
