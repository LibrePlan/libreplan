Xestión de recursos
###################

.. _recursos:
.. contents::

Xestión de recursos
===================

A aplicación xestiona dous tipos de recursos diferentes: recursos traballadores e recursos máquinas.

Os recursos traballadores representan os traballadores das empresas. As características principais son:
   * Satisfarán un ou varios criterios de tipo xenérico ou tipo traballador.
   * Son asignables específicamente a unha tarefa.
   * Son asignables como parte da asignación xenérica a unha tarefa que requira un criterio que satisfai o traballador.
   * Disporán de un calendario por defecto ou específico se así o decide o usuario.

Os recursos máquina representan as máquinas das empresas. As características principais son:
   * Satisfarán un ou varios criterios de tipo xenérico ou tipo máquina.
   * Son asignables específicamente a unha tarefa.
   * Son asignables como parte da asignación xenérica a unha tarefa que requira un criterio que satisfai a máquina.
   * Disporán de un calendario por defecto ou específico se así o decide o usuario.
   * Contará un unha pantalla de configuración na que se poderá establecer un valor *alfa* que represente a relación entre máquina e traballador.
      * O *alfa* representa canto tempo dun traballador é necesario para que a máquina funcione. Por exemplo, un alfa de 0.5 indica que de cada 8 horas de máquina son necesarias 4 de un traballador.
      * É posible asignar un *alfa* de xeito específico a un traballador, é dicir, elíxese o traballador que estará ocupado esa porcentaxe do seu tempo coa máquina.
      * Ou ben, é posible facer unha asignación xenérica en base a un criterio, de xeito que se asigna unha porcentaxe do uso a todos os criterios que satisfán ese criterio e teñen tempo dispoñible. O funcionamento da asignación xenérica será a mesma que a explicada para asignacións xenéricas a tarefas.

O usuario poderá crear, editar e invalidar (nunca borrar definitivamente) traballadores da empresa dende a pestana de "Recursos". Dende dita pestana existen as seguintes operacións:
   * Listado de traballadores: Os traballadores amosaranse listados e paxinados, dende onde poderán xestionar os seus datos.
   * Listado de máquinas: As máquinas amosaranse listados e paxinados, dende onde poderán xestionar os seus datos.

Xestión de traballadores
------------------------

A xestión de traballadores realizarase dende a pestana de "Recursos" e a operación de "______". Dende a lista de recursos é posible editar cada un dos traballadores premendo na icona estándar de edición.

Unha vez na edición dun recurso, o usuario poderá:

1) Editar os datos básicos de identificación do traballador.
      * Nome
      * Apelidos
      * DNI

.. figure:: images/worker-personal-data.png
   :scale: 70


2) Configurar os criterios que un traballador satisfai. O usuario poderá asignar calquera valor de criterio de tipo traballador ou xenérico que así considere a un traballador. É importante, para que a aplicación sexa utilizada en todo o seu valor, que os traballadores satisfagan criterios. Para asignar criterios o usuario debe:

   i. Buscar o criterio que desexa engadir e seleccionar o que encaixe coa súa procura.

   ii. Premer no botón de engadir.

   iii. Seleccionar data de inicio do criterio dende o momento que deba aplicarse.

   iv. Seleccionar a data de fin de aplicación do criterio ó recurso.

.. figure:: images/worker-criterions.png
   :scale: 70

3) Configurar un calendario específico para o traballador.

.. figure:: images/worker-calendar.png
   :scale: 70



Xestión de máquinas
-------------------

