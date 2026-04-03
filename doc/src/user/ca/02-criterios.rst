Criteris
########

.. contents::

Els criteris són elements del programa que s'utilitzen per categoritzar tant els recursos com les tasques. Les tasques requereixen criteris específics, i els recursos han de complir aquests criteris.

Aquí hi ha un exemple de com s'utilitzen els criteris: a un recurs se li assigna el criteri "soldador" (és a dir, el recurs compleix la categoria "soldador") i una tasca requereix el criteri "soldador" per completar-se. En conseqüència, quan s'assignen recursos a tasques mitjançant l'assignació genèrica (en oposició a l'assignació específica), es tindran en compte els treballadors amb el criteri "soldador". Per obtenir més informació sobre els diferents tipus d'assignació, consulteu el capítol sobre assignació de recursos.

El programa permet diverses operacions amb criteris:

*   Administració de criteris
*   Assignació de criteris a recursos
*   Assignació de criteris a tasques
*   Filtrar entitats basant-se en criteris. Les tasques i els elements de projecte es poden filtrar per criteris per realitzar diverses operacions dins del programa.

Aquesta secció només explicarà la primera funció, l'administració de criteris. Els dos tipus d'assignació es tractaran més endavant: l'assignació de recursos al capítol "Gestió de Recursos" i el filtratge al capítol "Planificació de Tasques".

Administració de criteris
==========================

L'administració de criteris és accessible a través del menú d'administració:

.. figure:: images/menu.png
   :scale: 50

   Pestanyes del menú de primer nivell

L'operació específica per gestionar criteris és *Gestionar criteris*. Aquesta operació us permet llistar els criteris disponibles al sistema.

.. figure:: images/lista-criterios.png
   :scale: 50

   Llista de criteris

Podeu accedir al formulari de creació/edició de criteris fent clic al botó *Crear*. Per editar un criteri existent, feu clic a la icona d'edició.

.. figure:: images/edicion-criterio.png
   :scale: 50

   Edició de criteris

El formulari d'edició de criteris, tal com es mostra a la imatge anterior, us permet realitzar les operacions següents:

*   **Editar el nom del criteri.**
*   **Especificar si es poden assignar múltiples valors simultàniament o només un valor per al tipus de criteri seleccionat.** Per exemple, un recurs podria complir dos criteris, "soldador" i "torner".
*   **Especificar el tipus de criteri:**

    *   **Genèric:** Un criteri que es pot utilitzar tant per a màquines com per a treballadors.
    *   **Treballador:** Un criteri que només es pot utilitzar per a treballadors.
    *   **Màquina:** Un criteri que només es pot utilitzar per a màquines.

*   **Indicar si el criteri és jeràrquic.** De vegades, els criteris han de ser tractats jeràrquicament. Per exemple, l'assignació d'un criteri a un element no l'assigna automàticament als elements derivats d'ell. Un exemple clar d'un criteri jeràrquic és la "ubicació". Per exemple, una persona designada amb la ubicació "Galícia" també pertanyerà a "Espanya".
*   **Indicar si el criteri és autoritzat.** Aquesta és la manera en què els usuaris desactiven els criteris. Un cop s'ha creat un criteri i s'ha utilitzat en dades històriques, no es pot modificar. En comptes d'això, es pot desactivar per evitar que aparegui en les llistes de selecció.
*   **Descriure el criteri.**
*   **Afegir nous valors.** Un camp d'entrada de text amb el botó *Nou criteri* es troba a la segona part del formulari.
*   **Editar els noms dels valors de criteris existents.**
*   **Moure valors de criteris amunt o avall a la llista de valors de criteris actuals.**
*   **Eliminar un valor de criteri de la llista.**

El formulari d'administració de criteris segueix el comportament del formulari descrit a la introducció, oferint tres accions: *Desa*, *Desa i tanca* i *Tanca*.
