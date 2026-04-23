Panel de indicadores
####################

.. contents::

O panel de indicadores é unha perspectiva de *LibrePlan* que contén un
conxunto de *indicadores de rendemento* que axudan a coñecer como está
a resultar un proxecto de acordo a:

   * como está a progresar o traballo que hai que realizar para levalo a cabo.
   * canto está a custar.
   * o estado dos recursos asignados.
   * as restricións de tempo.

Indicadores de rendemento de proxecto
=====================================

Calculáronse dous indicadores: a porcentaxe de progreso de proxecto e o
estado das tarefas.

Porcentaxe de progreso de proxecto
----------------------------------

É unha gráfica onde o progreso global dun proxecto calcúlase e contrástase
co valor esperado de progreso que o proxecto debería ter de acordo ao
*Gantt*

O progreso represéntase a través de dúas barras:

   * **Progreso actual**. É o progreso existente no momento presente de acordo
     ás medicións realizadas.
   * **Progreso esperado**. É o progreso que o proxecto debería ter no
     momento actual de acordo á planificación creada.

O progreso global de proxecto estímase de varias maneiras diferentes, xa que non
existe unha maneira única correcta de facelo:

   * **Progreso propagado**. É o tipo de progreso marcado para propagar a nivel
     do proxecto. Neste caso, ademais, non existe maneira de calcular un
     valor de progreso esperado e, en consecuencia, unicamente móstrase a barra de progreso
     actual.
   * **Por horas de todas as tarefas**. O progreso de todas as tarefas do
     proxecto é promediado para calcular o valor global. É unha media
     ponderada que toma en conta o número de horas asignado a cada tarefa. 
   * **Por horas do camiño crítico**. O progreso de todas as tarefas
     pertencentes a algún dos camiños críticos do proxecto é promediado
     para obter o valor global. Faise unha media ponderada que toma en conta
     as horas asignadas totais a cada unha das tarefas implicadas.
   * **Por duración do camiño crítico**. O progreso das tarefas
     pertencentes a algún dos camiños críticos se promedia a través dunha
     media ponderada pero, nesta ocasión, tendo en conta a duración das
     tarefas implicadas en lugar das horas asignadas.

Estado das tarefas
------------------

O estado das tarefas represéntase a través dun gráfico de torta que recolle a porcentaxe das tarefas
do proxecto nos diferentes estados posibles. Estes estados posibles son os
seguintes:

   * **Finalizadas**. Son as tarefas completadas, detectadas por un valor de progreso
     do 100% medido.
   * **En curso**. Son as tarefas que se atopan empezadas. Teñen un valor de progreso
     diferente de 0% e de 100% ou, tamén, algún tempo dedicado.
   * **Preparadas para comezar**. Teñen un valor de progreso do 0%, non teñen
     tempo traballado imputado, todas as tarefas dependentes *FIN_A_INICIO*
     están *finalizadas* e todas as tarefas dependentes *INICIO_A_INICIO* están
     *finalizadas* ou *en curso*.
   * **Bloqueadas**. Son tarefas que teñen un 0% de progreso, sen tempo
     imputado e coas tarefas das que se depende previas nun estado
     diferente a *en curso* e a *preparadas para comezar*.

Indicadores de custo
====================

Hai varios indicadores de *Valor Gañado* calculados no panel:

   * **CV (Varianza de custo)**. É a diferenza entre a *Curva de Valor
     Gañado* e a *Curva de Custo Real* no momento presente. Valores
     positivos indican beneficio, mentres que valores negativos perda.
   * **ACWP (Custo real do traballo realizado)**. É o número total de horas
     imputadas no proxecto até o momento actual.
   * **CPI (Indice de rendemento en custo)**. É o cociente *Valor Gañado/ Custo
     real*.

      * > 100 é bo, significa que se está baixo orzamento.
      * = 100 é bo igualmente, significa está cun custo exactamente igual
        ao plan trazado.
      * < 100 é malo, significa que o custo de completar o traballo é máis
        alto que o planificado.

   * **ETC (Estimación para compleción)**. É o tempo que está pendente de realizar
     para finalizar o proxecto.
   * **BAC (Orzamento en compleción)**. É o tempo total asignado no plan
     de proxecto.
   * **VAC (Varianza en compleción)**. É a diferenza entre o *BAC* e o
     *ETC*.

      * < 0 é estar sobre orzamento.
      * > 0 é estar baixo orzamento.

Recursos
========

Para analizar o proxecto desde o punto de vista dos recursos proporciónanse 2 cocientes e 1 histograma.

Histograma de desvío estimado en tarefas completadas
----------------------------------------------------

Calcúlase o desvío entre o número de horas asignadas ás tarefas do
proxecto e o número final de horas dedicadas ás mesmas.

O desvío calcúlase en porcentaxe para todas para todas as tarefas terminadas e
os desvíos calculados represéntanse nun histograma. No eixo vertical móstranse o número de tarefas que están nun determinado intervalo de desvío.
Seis intervalos de desvío calcúlanse dinámicamente.

Cociente de sobrecarga
----------------------

Resume a sobrecarga dos recursos que se asignan nas tarefas do proxecto.
Calcúlase de acordo á fórmula: **cociente de sobrecarga = sobrecarga / (carga + sobrecarga)**.

   * = 0 é bo, significa que os recursos non están sobrecargados.
   * > 0 é malo, significa que os recursos están sobrecargados.

Cociente de dispoñibilidade
---------------------------

Resume a capacidade que está dispoñible para asignar nos recursos do
proxecto. Por tanto é unha medida da dispoñibilidade dos recursos para
recibir máis asignacións sen ser sobrecargados. Calcúlase como: **cociente de dispoñibilidade
= (1 - carga/capacidade)*100**

   * Os valores posibles están entre 0% (completamente asignados) e 100% (non
     asignados)

Tempo
=====

Inclúense dúas gráficas de tempo: un histograma para a desviación de tempo
á finalización das tarefas dos proxectos e un gráfico de torta
para as violacións de data de entrega.

Adianto ou atraso na compleción das tarefas
-------------------------------------------

Calcúlase a diferenza en días entre a data de finalización planificada
para as tarefas do proxecto e o seu tempo de finalización real. A data de terminación
prevista obtense do *Gantt* e a data de terminación real obtense a partir da data do traballo imputado á tarefa máis recente.

O atraso ou adianto na compleción das tarefas represéntase a través dun
histograma. No eixo vertical represéntanse o número de tarefas cun
número de días de adianto ou atraso incluídas no intervalo indicado
na abcisa. Calcúlase seis intervalos de desvío na compleción de tarefas de forma dinámica.

   * Valores negativos indican terminación antes de tempo.
   * Valores positivos indican terminación con atraso.

Violacións de data de entrega
-----------------------------

Por unha banda calcúlase a marxe coa data de entrega de proxecto, se esta é configúrada. Doutra banda píntase un gráfico de sectores coa porcentaxe de tarefas
que cumpren a data de entrega. Inclúense tres tipos de valores:

   * Porcentaxe de tarefas sen data de entrega configurada.
   * Porcentaxe de tares finalizadas cunha data de terminación real posterior
     á data de entrega configurada. A data de finalización real obtense
     o último traballo rexistrado na tarefa.
