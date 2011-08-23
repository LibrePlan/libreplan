Informe de trabajo y progres por proyecto
#########################################

.. contents::

Propósito
=========

Este informe permite mostrar cúal es el estado global de los proyectos teniendo en cuenta dos variables: el progreso y el coste.

Se analiza el estado actual de progreso de un proyecto comparándolo con lo previsto de acuerdo a la planificación y al trabajo dedicado.

También se muestran varios ratios relacionados al coste del proyecto comparándolo el rendimiento actual con el teórico.

Parámetro de entrada y filtros
==============================

Hay varios parámetros obligatorios. Estos son:

   * **Fecha de referencia**. Es la fecha de referencia para hacer la comparación del estado previsto de planificación del proyecto en esa fecha con el rendimiento real del proyecto en esa citada fecha.

   * **Tipo de progreso**. Es el tipo de progreso que se quiere usar para medir el progreso global. En la aplicación un proyecto puede ser medido simultáneamente con diferentes tipos de progresos, y el seleccionado por el usuario en el combo de selección es el usado para calcular el informe. El valor por defecto para el tipo de progreso a usar es *propagado*, que es un tipo especial de progreso consistente en el uso en cada elemento del WBS del valor allí configurado como que propaga.

Con respecto a los campos opciones, son los siguientes:

   * **Fecha de inicio**. En la fecha de inicio mínima de los proyectos que se quieren incluir en el informe. Es opcional. Si no se especifica *fecha de inicio*, no existe fecha mínima para los proyectos a incluír.

   * **Fecha de fin**. Es la fecha máxima de fin de los proyectos para que estos sean incluidos en el informe. Todos los proyectos que terminan tras la *fecha de fin* son descartados.

   * **Filtro por proyectos**. Este filtro permite seleccionar un conjunto de proyectos a los que limitar los resultados del informe. Si no se añade ningún proyecto al filtro, se muestra el informe para todos los proyectos de la base de datos. Hay un selector autcompletable para encontrar los proyectos requeridos. Se añaden al filtro mediante la pulsación en el botón *Añadir*.

Salida
======

El formato de salida es la siguiente:

Cabecera
--------

In the report header the following fields are showed:

   * **Fecha de inicio**. Es el filtro por fecha de inicio. No se muestra si el informe no es filtrado por este campo.
   * **Fecha de fin**. Es el filtro por fecha fin. No se muestra si el usuario no lo rellena.
   * **Tipo de progreso**. Es el tipo de progreso usado por este informe.
   * **Proyectos**. Es un campo que informa acerca de los proyectos filtrados para los que se obtiene el prooyecto. Consiste en la cadena de texto *Todos* cuando el informe se obtiene para todos los proyectos que satisfacen el resto de los filtros.
   * **Fecha de referencia**. Muestra el campo de entrada obligatorio *fecha de referencia* utilizado en la extracción del informe.

Pie de página
-------------

Se muestra la fecha en la que el informe ha sido extraído.

Cuerpo
------

El cuerpo del informe consiste en la lista de proyectos que han sido seleccionados como resultado de los filtros de entrada.

Otra cosa importante es que el progreso en el informe es calculado en tantos por uno. Son valores entre el 0 y el 1.

Los filtros funcionan añadiendo condiciones que se aplican en cadena a excepción del conjunto formado por los filtros de fecha (*fecha de inicio*, *fecha de fin*) y el *filtro por proyectos*. En este caso, si uno de los filtros de fecha contiene algún valor y también el *filtro por proyectos* tiene algun proyecto configurado al mismo tiempo, entonces este último filtro es el que manda en la selección. Esto significa que los proyectos que se incluyen en el informe son los proporcionados por el *filtro por proyectos* independientemente de lo que haya en los *filtros de fechas*

Para cada proyecto seleccionado a ser incluido en la salida del informe, se muestra la siguiente información:
   * *El nombre del proyecto.*
   * *Las horas totales.* Las horas totales del proyecto se muestran mediante la adición de las horas de cada tarea. Se calculan dos tipos de horas totales:
      * *Estimadas (TE)*. Esta cantidad es la suma de todas las horas del WBS del proyecto. Es el número total de horas en las que un proyecto está estimado para ser completado.
      * *Planificadas (TP)*. En *LibrePlan* es posible tener dos cantidades diferentes. Las horsa estimadas de una tarea, que son el número de horas que a priori se piensa que son necesaras para terminar una tarea, y las horas planificadas, que son las horas asignadas en el plan para hacer la tarea. Las horas planificadas pueden ser igual, menos o más que las horas estimadas y se deciden en una fase posterior, en la operación de asignación. Por tanto, las horas totales planificadas de un proyecto es la suma de todas las horas asignadas de sus tareas.
   * *Progreso*. Se muestran tres medidas relacionadas con el tipo de progreso especificado en el filtro de entrada en la fecha de referencia:
      * *Medidas (PM)*. Es el progreso global considerando las medidas de progreso con una fecha menor o igual a la *fecha de referencia* en los campos de entrada para el informe. Además, se tiene en cuenta a todas last areas y la suma se pondera por el número de horas de cada tarea.
      * *Imputado (PI)*. Este el el progreso considerando que el trabajo realizado va a la misma velocidad que las horas dedicadas en las tareas. Si se hacen X horas de Y totales de una tarea, se considera que el progreso imputado global es X/Y.
   * *Horas hasta la fecha*. Son dos campos que muestran el número de horas hasta la fecha de referencia desde dos puntos de vista:
      * *Planeadas (HP)*. Este número es la adición de las horas asignadas en cualquier tarea con una fecha inferior a la *fecha de referencia*.
      * *Reales (HR).*. Este número es la adición de las horas imputadas en los partes de trabajo a cualquiera de las tareas de un proyecto con una fecha los partes de trabajo igual o inferior a la *fecha de referencia*.
   * *Diferencia*. Englobadas en esta sección se encuentras varias maneras de medir el coste:
      * *En coste*. Es la diferencia en horas entre el número de horas gastadas teniendo en cuenta el progreso medido y las horas dedicadas hasta la *fecha de referencia*. La fórmula es: *PM*TP - HR*.
      * *En planificación*. Es la diferencia entre las horas gastadas de acuerdo al progreso medido global y el número de horas planificadas hasta la fecha de referencia. Mide el adelanto o el retraso. La fórmula es: *PM*TP - HR*.
      * *Ratio de coste*. Se calcula dividiendo *PM*/*PI*. Si es mayor que 1, significa que el proyecto va en beneficios y si es mejor que 1, que se está perdiendo dinero
      * *Ratio en planificación.* Se cualcula dividiendo *PM*/*PP*. Si es mayor que 1, significa que el proyecto va adelantado y si es menor que 1 que va con retraso.
