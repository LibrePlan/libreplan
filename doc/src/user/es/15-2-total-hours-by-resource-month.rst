Informe de horas totales trabajadas por recurso en un mes
#########################################################

.. contents::

Propósito
=========

Este informe permite obtener el número total de horas trabajadas por los recursos en un mes. Esto puede ser útil para calcular las horas extras hechas o, dependendiendo de la organización, la cantidad de horas que tiene que ser pagadas por recurso en un mes.

La aplicación permite registrar partes de trabajo tanto para los trabajadores como para las máquinas. Y, de acuerdo con esto, el informe en el caso de las máquinas indica las horas totales que las máquinas han estado funcionando en un determinado mes.

Parámetros de entrada y filtro
==============================

En el informe debe ser especificado el año y el mes para obtener el total de horas por recurso que han trabajado.

Salida
======

El formato de salida del informe es el siguiente:

Cabecera
--------

En la cabecera del informe se muestra;

   * El *año* al que pertenece el informe que se está extrayendo.
   * El *mes* al cual pertene los datos del informe mostrado.

Pie de página
-------------

En el pie de página se muestra la fecha en la que cúal el informe se sacó.

Cuerpo
------

El área de datos del informe consiste en una única sección en la que se incluye una tabal con dos columnas:

   * Una columna denominada **Nombre** para el nombre del recurso.
   * Una columna llamada **Horas** con la suma de todas las horas dedicadas por el recurso al que corresponde una fila.

Hay una final total agregadora del total de horas devotadas por cualquier de los recursos en el *mes*, *años* al que corresponde el informe.
