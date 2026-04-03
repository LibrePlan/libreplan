Panel de indicadores
####################

.. contents::

El panel de indicadores es una perspectiva de *LibrePlan* que contiene un
conjunto de *indicadores de rendimiento* que ayudan a conocer como está
resultando un proyecto de acuerdo a:

   * cómo está progresando el trabajo que hay que realizar para llevarlo a cabo.
   * cuánto está costando.
   * el estado de los recursos asignados.
   * las restricciones de tiempo.

Indicadores de rendimiento de proyecto
======================================

Se han calculado dos indicadores: el porcentaje de progreso de proyecto y el
estado de las tareas.

Porcentaje de progreso de proyecto
----------------------------------

Es una gráfica donde el progreso global de un proyecto se calcula y se contrasta
con el valor esperado de progreso que el proyecto debería tener de acuerdo al
*Gantt*

El progreso se representa a través de dos barras:

   * **Progreso actual**. Es el progreso existente en el momento presente de
     acuerdo a las mediciones realizadas.
   * **Progreso esperado**. Es el progreso que el proyecto debería tener en el
     momento actual de acuerdo a la planificación creada.

El progreso global de proyecto se estima de varias maneras diferentes, ya que no
existe una manera única correcta de hacerlo:

   * **Progreso propagado**. Es el tipo de progreso marcado para propagar a
     nivel del proyecto. En este caso, además, no existe manera de calcular un
     valor de progreso esperado y, en consecuencia, únicamente se muestra la barra de
     progreso actual.
   * **Por horas de todas las tareas**. El progreso de todas las tareas del
     proyecto es promediado para calcular el valor global. Es una media
     ponderada que toma en cuenta el número de horas asignado a cada tarea. 
   * **Por horas del camino crítico**. El progreso de todas las tareas
     pertenecientes a alguno de los caminos críticos del proyecto es promediado
     para obtener el valor global. Se hace una media ponderada que toma en
     cuenta las horas asignadas totales a cada una de las tareas implicadas.
   * **Por duración del camino crítico**. El progreso de las tareas
     pertenecientes a alguno de los caminos críticos se promedia a través de una
     media ponderada pero, en esta ocasión, teniendo en cuenta la duración de
     las tareas implicadas en lugar de las horas asignadas.

Estado de las tareas
--------------------

El estado de las tareas se representa a través de un gráfico de tarta que recoge el porcentaje de las tareas
del proyecto en los diferentes estados posibles. Estos estados posibles son los
siguientes:

   * **Finalizadas**. Son las tareas completadas, detectadas por un valor de
     progreso del 100% medido.
   * **En curso**. Son las tareas que se encuentra empezadas. Tienen un valor de
     progreso diferente de 0% y de 100% o, también, algún tiempo dedicado.
   * **Preparadas para comenzar**. Tienen un valor de progreso del 0%, no tienen
     tiempo trabajado imputado, todas las tareas dependientes *FIN_A_INICIO*
     están *finalizadas* y todas las tareas dependientes *INICIO_A_INICIO* están
     *finalizadas* o *en curso*.
   * **Bloqueadas**. Son tareas que tienen un 0% de progreso, sin tiempo
     imputado y con las tareas de las que se depende previas en un estado
     diferente a *en curso* y a *preparadas para comenzar*.

Indicadores de coste
====================

Hay varios indicadores de *Valor Ganado* calculados en el panel:

   * **CV (Varianza de coste)**. Es la diferencia entre la *curva de Valor
     Ganado* y la *curva de Coste Real* en el momento presente. Valores
     positivos indican beneficio, mientras que valores negativos pérdida.
   * **ACWP (Coste real del trabajo realizado)**. Es el número total de horas
     imputadas en el proyecto hasta el momento actual.
   * **CPI (Indice de rendimiento en coste)**. Es el ratio *Valor Ganado/ Coste
     real*.

      * > 100 es bueno, significa que se está bajo presupuesto.
      * = 100 es bueno igualmente, significa está con un coste exactamente igual
        al plan trazado.
      * < 100 es malo, significa que el coste de completar el trabajo es más
        alto que el planificado.

   * **ETC (Estimación para compleción)**. Es el tiempo que está pendiente de
     realizar para finalizar el proyecto.
   * **BAC (Presupuesto en compleción)**. Es el tiempo total asignado en el plan
     de proyecto.
   * **VAC (Varianza en compleción)**. Es la diferencia entre el *BAC* y el
     *ETC*.

      * < 0 es estar sobre presupuesto.
      * > 0 es estar bajo presupuesto.

Recursos
========

Para analizar el proyecto desde el punto de vista de los recursos se
proporcionan 2 ratios y 1 histograma.

Histograma de desvío estimado en tareas completadas
---------------------------------------------------

Se calcula el desvío entre el número de horas asignadas a las tareas del
proyecto y el número final de horas dedicadas a las mismas.

El desvío se calcula en porcentaje para todas para todas las tareas terminadas y
los desvíos calculados se representan en un histograma. En el eje vertical se
muestran el número de tareas que están en un determinado intervalo de desvío.
Seis intervalos de desvío se calculan dinámicamente.

Ratio de sobrecarga
-------------------

Resume la sobrecarga de los recursos que se asignan en las tareas del proyecto.
Se calcula de acuerdo a la fórmula: **ratio de sobrecarga = sobrecarga / (carga + sobrecarga)**.

   * = 0 es bueno, significa que los recursos no están sobrecargados.
   * > 0 es malo, significa que los recursos están sobrecargados.

Ratio de disponibilidad
-----------------------

Resume la capacidad que está disponible para asignar en los recursos del
proyecto. Por tanto es una medida de la disponibilidad de los recursos para
recibir más asignaciones sin ser sobrecargados. Se calcula como: **ratio de
disponibilidad = (1 - carga/capacidad)*100**

   * Los valores posibles están entre 0% (completamente asignados) y 100% (no
     asignados)

Tiempo
======

Se incluyen dos gráficas de tiempo: un histograma para la desviación de tiempo
a la finalización de las tareas de los proyectos y un gráfico de
tarta para las violaciones de fecha de entrega.
   
Adelanto o retraso en la compleción de las tareas
-------------------------------------------------

Se calcula la diferencia en días entre la fecha de finalización planificada
para las tareas del proyecto y su tiempo de finalización real. La fecha de
terminación prevista se obtiene del *Gantt* y la fecha de terminación real se
obtiene a partir de la fecha del trabajo imputado a la tarea más reciente.

El retraso o adelanto en la compleción de las tareas se representa a través de
un histograma. En el eje vertical se representan el número de tareas con un
número de días de adelanto o retraso incluidas en el intervalo indicado
en la abcisa. Se calcula seis intervalos de desvío en la compleción de tareas
de forma dinámica.

   * Valores negativos indican terminación antes de tiempo.
   * Valores positivos indican terminación con retraso.

Violaciones de fecha de entrega
-------------------------------

Por un lado se calcula el margen con la fecha de entrega de proyecto, si esta se
configura. Por otro lado se pinta un gráfico de sectores con el porcentaje de
tareas que cumplen la fecha de entrega. Se incluyen tres tipos de valores:

   * Porcentaje de tareas sin fecha de entrega configurada.
   * Porcentaje de tares finalizadas con una fecha de terminación real posterior
     a la fecha de entrega configurada. La fecha de finalización real se obtiene
     el último trabajo registrado en la tarea.
   * Porcentaje de tareas finalizadas con una fecha de terminación real anterior
     a su fecha de entrega.
