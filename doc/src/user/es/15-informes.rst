Informes
########

.. _informes:
.. contents::


Informes
========

"NavalPlan" está integrado con *JasperReports* para la gestión de informes lo cual permite implantar diversos informes que analiza datos existentes en la aplicación.

Los informes definidos son:

* Informe de pedidos. Muestra una fila por cada tarea del pedido. Los campos que se muestran por cada tarea son:

   * Código
   * Nombre
   * Fecha estimada inicio
   * Data primero parte
   * Fecha estimada fin
   * Data último parte
   * Data *deadline* (se existe)
   * Porcentaje de Avance (el marcado como propaga).
   * Estado actual: Finalizada, En Curso, Pendiente, Bloqueada.
   * Estado *deadline*: Superado, No superado, En blanco (si no existe deadline).

* Informe de partes de trabajo. El informe muestra el resumen de horas trabajadas según los partes de trabajo.
* Informe de horas trabajadas por trabajador o grupo de trabajadores. Informe que muestra el resumen por fecha y subtotal de horas trabajadas por recurso existente en la aplicación.
* Informe de lista de avances de los proyectos. Informe que muestra una fila por cada pedido filtrado de modo que se visualiza el avance de los distintos tipos de avances seleccionados.
* Informe que lista las horas trabajadas de un proyecto. Permite conocer los siguientes parámetros:

   * Horas estimadas (HE)
   * Horas planificadas totales (HP Total)
   * Horas planificadas (HP)
   * Horas reales (HR)
   * Avance medido (AM)
   * Avance imputado (AI)
   * Avance planificado (AP)
   * Desfase en Costo (CV)
   * Desfase en Planificación (SV)
   * Ratio desfase en costo (CPI)
   * Ratio desfase en planificación (SPI)

* Informe de horas realizadas e imputadas por tipo de trabajo.
* Informe de horas realizadas e imputadas por etiquetas.
* Informe de costos de los recursos asignados la tareas en base a los tipos de horas.
