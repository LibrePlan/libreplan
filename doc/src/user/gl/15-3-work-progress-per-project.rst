Informe de traballo e progres por proxecto
##########################################

.. contents::

Propósito
=========

Este informe permite mostrar cal é o estado global dos proxectos tendo en conta dúas variables: o progreso e o custo.

Analízase o estado actual de progreso dun proxecto comparándoo co previsto de acordo á planificación e ao traballo dedicado.

Tamén se mostran varios indicadores relacionados ao custo do proxecto comparándoo o rendemento actual co teórico.

Parámetros de entrada e filtros
===============================

Hai varios parámetros obrigatorios. Estes son:

   * **Data de referencia**. É a data de referencia para facer a comparación do estado previsto de planificación do proxecto nesa data co rendemento real do proxecto nesa citada data.

   * **Tipo de progreso**. É o tipo de progreso que se quere usar para medir o progreso global. Na aplicación un proxecto pode ser medido simultaneamente con diferentes tipos de progresos, e o seleccionado polo usuario no combo de selección é o usado para calcular o informe. O valor por defecto para o tipo de progreso a usar é *propagado*, que é un tipo especial de progreso consistente no uso en cada elemento do WBS do valor alí configurado como que propaga.

Con respecto aos campos opcións, son os seguintes:

   * **Data de inicio**. A data de inicio mínima dos proxectos que se queren incluír no informe. É opcional. Se non se especifica *data de inicio*, non existe data mínima para os proxectos a incluír.

   * **Data de fin**. É a data máxima de fin dos proxectos para que estes sexan incluídos no informe. Todos os proxectos que terminan tras a *data de fin* son descartados.

   * **Filtro por proxectos**. Este filtro permite seleccionar un conxunto de proxectos aos que limitar os resultados do informe. Se non se engade ningún proxecto ao filtro, móstrase o informe para todos os proxectos da base de datos. Hai un selector autcompletable para atopar os proxectos requiridos. Engádense ao filtro mediante a pulsación no botón *Engadir*.

Saída
=====

O formato de saída é a seguinte:

Cabeceira
---------

Na cabeceira do informe inclúense os seguintes parámetros de entrada:

   * **Data de inicio**. É o filtro por data de inicio. Non se mostra se o informe non é filtrado por este campo.
   * **Data de fin**. É o filtro por data fin. Non se mostra se o usuario non o enche.
   * **Tipo de progreso**. É o tipo de progreso usado por este informe.
   * **Proxectos**. É un campo que informa acerca dos proxectos filtrados para os que se obtén o proxecto. Consiste na cadea de texto *Todos* cando o informe obtense para todos os proxectos que satisfán o resto dos filtros.
   * **Data de referencia**. Mostra o campo de entrada obrigatorio *data de referencia* utilizado na extracción do informe.

Pé de páxina
-------------

Móstrase a data na que o informe foi extraído.

Corpo
------

O corpo do informe consiste na lista de proxectos que foron seleccionados como resultado dos filtros de entrada.

Outra cousa importante é que o progreso no informe é calculado en tantos por un. Son valores entre o 0 e o 1.

Os filtros funcionan engadindo condicións que se aplican en cadea a excepción do conxunto formado polos filtros de data (*data de inicio*, *data de fin*) e o *filtro por proxectos*. Neste caso, se un dos filtros de data contén algún valor e tamén o *filtro por proxectos* ten algun proxecto configurado ao mesmo tempo, entón este último filtro é o que manda na selección. Isto significa que os proxectos que se inclúen no informe son os proporcionados polo *filtro por proxectos* independentemente do que haxa nos *filtros de datas*

Para cada proxecto seleccionado a ser incluído na saída do informe, móstrase a seguinte información:
   * *O nome do proxecto.*
   * *As horas totais.* As horas totais do proxecto móstranse mediante a adición das horas de cada tarefa. Calcúlanse dous tipos de horas totais:
      * *Estimadas (HE)*. Esta cantidade é a suma de todas as horas do WBS do proxecto. É o número total de horas nas que un proxecto está estimado para ser completado.
      * *Planificadas (TP)*. En *LibrePlan* é posible ter dúas cantidades diferentes. As horas estimadas dunha tarefa, que son o número de horas que a priori se pensa que son necesaras para terminar unha tarefa, e as horas planificadas, que son as horas asignadas no plan para facer a tarefa. As horas planificadas poden ser igual, menos ou máis que as horas estimadas e decídense nunha fase posterior, na operación de asignación. Por tanto, as horas totais planificadas dun proxecto é a suma de todas as horas asignadas das súas tarefas.
   * *Progreso*. Móstranse tres medidas relacionadas co tipo de progreso especificado no filtro de entrada na data de referencia:
      * *Medidas (PM)*. É o progreso global considerando as medidas de progreso cunha data menor ou igual á *data de referencia* nos campos de entrada para o informe. Ademais, tense en conta a todas as tarefas e a suma pondérase polo número de horas de cada tarefa.
      * *Imputado (PI)*. Este é o progreso considerando que o traballo realizado vai á mesma velocidade que as horas dedicadas nas tarefas. Se se fan X horas de E totais dunha tarefa, considérase que o progreso imputado global é X/E.
   * *Horas até a data*. Son dous campos que mostran o número de horas até a data de referencia desde dous puntos de vista:
      * *Planeadas (HP)*. Este número é a adición das horas asignadas en calquera tarefa cunha data inferior á *data de referencia*.
      * *Reais (HR).*. Este número é a adición das horas imputadas nos partes de traballo a calquera das tarefas dun proxecto cunha data os partes de traballo igual ou inferior á *data de referencia*.
   * *Diferenza*. Englobadas nesta sección atópanse varias maneiras de medir o custo:
      * *En custo*. É a diferenza en horas entre o número de horas gastadas tendo en conta o progreso medido e as horas dedicadas até a *data de referencia*. A fórmula é: *PM*TP - HR*.
      * *En planificación*. É a diferenza entre as horas gastadas de acordo ao progreso medido global e o número de horas planificadas até a data de referencia. Mide o adianto ou o atraso. A fórmula é: *PM*TP - HR*.
      * *Cociente de custo*. Calcúlase dividindo *PM*/*PI*. Se é maior que 1, significa que o proxecto vai en beneficios e se é menor que 1, que se está perdendo diñeiro
      * *Cociente en planificación.* Se calcula dividindo *PM*/*PP*. Se é maior que 1, significa que o proxecto vai adiantado e se é menor que 1 que vai con atraso.
