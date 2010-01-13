Avances
#######

.. contents::

O avance dun proxecto marca o grao no que se está cumprindo co alcance estimado para a realización do mesmo, ademais, o avance dunha tarefa indica ese mesmo grao para o alcance estimado para dita tarefa.

Xeralmente os avances non teñen un modo automático de ser medidos, e é unha persoa quen en base á experiencia ou á realización de unha lista de chequeo determina o grao de compleción de unha tarefa ou un proxecto.

Cabe destacar que hai unha diferencia importante entre a uso de horas asignadas a unha tarefa ou proxecto, co grao de avance nesa mesma tarefa ou proxecto. Mentres que o uso de horas pode estar en desvío ou non, o proxecto pode estar nun grao de avance inferior ou superior ó estimado para o día no que se está controlando. Prodúcense, debido a estas dúas medidas, varias posibles situacións:

* Consumíronse menos horas das estimadas para o elemento a medir e ó mesmo tempo o proxecto está indo máis lento do estimado, porque o avance é inferior ó estimado para o día de control.
* Consumíronse menos horas das estimadas para o elemento a medir e ó mesmo tempo o proxecto está indo máis rápido do estimado, porque o avance é inferior ó estimado para o día de control.
* Consumíronse máis horas das estimadas e ó mesmo tempo o proxecto está indo máis lento do estimado, porque o avance é inferior ó estimado para o día de control.
* Consumíronse máis horas das estimadas e ó mesmo tempo o proxecto está indo máis rápido do estimado, porque o avance é inferior ó estimado para o día de control.

O contraste de estas posibles situacións é posible realizalo dende a propia planificación, utilizando información do grao de avance e por outro lado do grao de uso de horas. Neste capítulo tratarase a introdución da información para poder levar un control do avance.

A filosofía implantada no proxecto para o control do avance está baseada en que o usuario divida ata o punto no que desexa o control de avances dos seus proxectos. En consecuencia, se o usuario desexa controlar a nivel de pedido, só debe introducir información nos elementos de nivel 1, cando se desexa poder dispoñer de un control máis fino sobre as tarefas, debe introducir información de avances en niveis inferiores, sendo o sistema que propaga cara arriba na xerarquía todos os datos.

Xestión de tipos de avance
==========================

Cada empresa pode ter unhas necesidades diferentes de control do avance dos seus proxectos, e concretamente das tarefas que os compoñen. Por esta razón, foi necesario contemplar a existencia dunhas entidades no sistema chamadas "tipos de avance". Os tipos de avance son diferentes tipoloxías que cada usuario pode dar de alta no sistema para medir o avance dunha tarefa. Por exemplo, unha tarefa pode ser medida porcentualmente, pero ó mesmo tempo ese avance porcentual se traduce en un avance en *Toneladas* sobre o acordado co cliente.

Un tipo de avance está caracterizado por un nome, un valor máximo e unha precisión:

* Nome: Será un nome representativo que o usuario debe recordar para cando seleccione a asignación de avances sexa capaz de entender que tipo de avance está medindo.
* Valor máximo: É o valor máximo que se lle permite a unha tarefa ou proxecto establecer como medida total de avance. Por exemplo, traballando con *Toneladas*, se se considera que o máximo normal en toneladas é de 4000 e nunca vai a haber tarefas que requiran realizar máis de 4000 toneladas de algún material, ese debería ser o valor máximo establecido.
* Precisión: É o valor dos incrementos que se permiten para o tipo de avance creado. Por exemplo, se o avance en *Toneladas* se vai a medir en valores redondeados, podería ser 1 a precisión. Dende ese momento, só se poderían introducir medidas de avance con números enteiros, por exemplo, 1, 2, 300, etc.

O sistema conta con dous tipos de avance creados por defecto:

* Porcentual: Tipo de avance xeral que permite medir o avance dun proxecto ou tarefa en base ó porcentaxe que se estima de compleción do mesmo, por exemplo, unha tarefa está ó 30% respecto ó 100% estimado nun día concreto.
* Unidades: Tipo de avance xeral que permite medir o avance en unidades sen necesidade de especificar as unidades concretas. A tarefa comprendía a creación de 3000 unidades e o avance son 500 unidades sobre as 3000 estimadas.

.. figure:: images/tipos-avances.png
   :scale: 50

   Administración de tipos de avances

O usuario pode crear novos tipos de avance do seguinte xeito:

* O usuario accede á sección de "Administración".
* Preme na opción do menú de segundo nivel "Xestionar tipos de avance".
* O sistema amosa un listado de tipos de avance existentes.
* Con cada tipo de avance o usuario pode:

  * Editar
  * Borrar

* A maiores, o usuario pode crear un tipo de avance novo.
* Coa edición ou a creación, o sistema amosa un formulario coa seguinte información:

  * Nome do tipo de avance.
  * Valor máximo que acepta o tipo de avance.
  * Precisión do tipo de avance.


Introdución de avances en base a tipo
=====================================

A introdución dos avances realízase sobre os elementos de pedido, sen embargo, é posible facelo con un atallo dende as tarefas de planificación. É responsabilidade do usuario da aplicación a decisión sobre que tipos de avance desexa asociar a cada elemento de pedido.

É posible introducir un tipo de avance único e defectivo para todo un pedido.

Antes de poder realizar medidas de avance, é necesario asociar o tipo elixido ó pedido, por exemplo, un avance de tipo porcentaxe para medir porcentualmente os avances realizados sobre o total da tarefa, ou un avance de tipo pactado por se se queren introducir a futuro medicións de avances pactadas co cliente.

.. figure:: images/avance.png
   :scale: 40

   Pantalla de introdución de avances con visualización gráfica.

Para introducir medicións de avance é necesario realizar o seguinte:

* Seleccionar o tipo de avance para o que desexa introducir o avance.

  * Se non existe ningún tipo de avance é necesario engadir un tipo de avance.

* No formulario que aparece debaixo cos campos "Valor" e "Data" introducir o valor absoluto da medida e a data que se lle desexa asignar á medida tomada.
* O sistema almacena automaticamente os datos introducidos.



Contraste de avances sobre un elemento do pedido
================================================

É posible contrastar graficamente as evolucións dos avances dos pedidos en base ás medicións realizadas. Cada tipo de avance dispón dunha columna con botóns de chequeo (de título "Amosar") o cal ó ser marcado se amosa a gráfica de evolucións de medidas realizadas sobre o elemento de pedido.

.. figure:: images/contraste-avance.png
   :scale: 40

   Contraste de varios avances.


