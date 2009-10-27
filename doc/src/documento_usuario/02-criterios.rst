Criterios
#########

.. contents::

Os criterios serán os elementos que se utilizarán de xeito transversal na aplicación para categorizar os recursos e ás tarefas. Un exemplo moi sinxelo de utilización de criterios é o feito de asignar a un recurso o criterio “soldador” e requirir nas tarefas que sexa necesario cumprir o criterio “soldador” para realizar asignación de recursos.

No proxecto, existen varias funcionalidades a realizar con criterios:
   * Administración de criterios
   * Asignación de criterios a recursos.
   * Asignación de criterios a tarefas.

Das tres funcionalidades anteriores só se explicará a primeira delas nesta sección deixando para seccións posteriores os dous tipos de asignación, a de recursos na sección `Xestión de recursos`_ e a de tarefas en `Planificación de tarefas`_.


Administración de criterios
===========================

A administración de criterios será accesible dende o menú de administración:

.. figure:: images/menu.png
   :scale: 70

A operación específica para administrar criterios é *Manage Criteriums*. A partir de dita operación é posible listar os criterios dispoñibles no sistema.

.. figure:: images/lista-criterios.png
   :scale: 70

Premendo no botón *Create* poderase acceder ó formulario de creación/edición de un criterio. A edición de un criterio farase premendo na icona de edición do mesmo.

.. figure:: images/edicion-criterio.png
   :scale: 55


No formulario de edición de criterios que se amosa na imaxe anterior poderanse realizar as seguintes operacións:
   * Edición do nome do criterio
   * Indicar se é posible asignar varios valores simultáneamente ó mesmo elemento para o tipo de criterio seleccionado. Por exemplo, un recurso que satisfai dous criterios, soldador e torneiro.
   * Indicar o tipo do criterio:
      * Tipo xenérico: Criterio que poderá satisfacer indistintamente unha máquina ou un traballador.
      * Tipo traballador: Criterio que poderá satisfacer un traballador exclusivamente.
      * Tipo máquina: Criterio que poderá satisfacer unha máquina exclusivamente.
   * Indicar se o criterio é xerárquico ou non. Existen casos nos que os criterios deben ser tratados xerárquicamente, é dicir, que o feito de ser un criterio asignado a un elemento non folla faga que este criterio estea asignado automáticamente ós fillos. Un exemplo claro de xerarquización de criterios é o criterio localización, unha persoa que teña asignado Galicia como localización pertencerá á localización España por ser xerárquico.
   * Indicar se o criterio está habilitado e deshabilitado. Esta é a forma de borrar criterios. Debido a que unha vez creado un criterio e utilizado en datos históricos, estes non poden ser cambiados, o criterio debe existir no sistema. Para evitar que este criterio saia en diferentes elementos de selección, poderá ser invalidado.
   * Realizar unha descrición do criterio.
   * Engadir novos valores. Na segunda parte do formulario aparece unha entrada de texto con un botón *New Criterion*
   * Editar o nome dos criterios existentes.
   * Desplazar verticalmente os criterios na lista dos existentes.
   * Eliminar un valor de criterio da lista.

O formulario de administración de criterios é un formulario que responde ás características dos formularios comentados na introducción como de 3 operacións (gardar, gardar e pechar e pechar).