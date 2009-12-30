Informes
########

.. _informes:
.. contents::


Informes
========

"Navalpro" está integrado con *JasperReports* para a xestión de informes o cal permite implantar diversos informes que analiza datos existentes na aplicación.

Os informes definidos son:

* Informe de pedidos. O que vai amosa o informe é unha fila por cada tarefa do pedido. Os campos que se amosan por cada tarefa son:

   * Código
   * Nome
   * Data estimada inicio
   * Data primeiro parte
   * Data estimada fin
   * Data último parte
   * Data deadline (se existe)
   * Prorcentaxe de Avance (o marcado como propaga).
   * Estado actual: Finalizada, En Curso, Pendente, Bloqueada.
   * Estado deadline: Superado, Non superado, En blanco (se non existe deadline).

* Informe de partes de traballo. O informe amosa o resumo de horas traballadas segundo os partes de traballo.
* Informe de horas traballadas por traballador ou grupo de traballadores. Informe que amosa o resumo por data e subtotal de horas traballadas por recurso existente na aplicación.
* Informe de lista de avances dos proxectos. Informe que amosa unha fila por cada pedido filtrado de modo que se visualiza o avance dos distintos tipos de avances seleccionados.
* Informe que lista as horas traballadas de un proxecto. Permite coñecer as:

   * Horas estimadas (HE)
   * Horas planificadas totales (HP Total)
   * Horas planificadas (HP)
   * Horas reais (HR)
   * Avance medido (AM)
   * Avance imputado (AI)
   * Avance planificado (AP)
   * Desfase en Coste (CV)
   * Desfase en Planificación (SV)
   * Ratio desfase en coste (CPI)
   * Ratio desfase en planificación (SPI)

* Informe de horas realizadas e imputadas por tipo de traballo.
* Informe de horas realizadas e imputadas por etiquetas.
* Informe de custos dos recursos asignados a tarefas en base ós tipos de horas.