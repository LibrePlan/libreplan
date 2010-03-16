Exercicios Grupo 1 - Sesión 2
#############################

.. _grupo1:
.. contents::


Exercicio  1. Visualización de camiño crítico e conclusións
===========================================================

Este exercicio consiste en familiarizarse coa asignación de recursos avanzada. O exercicio está composto en varios puntos:

    *  Ir ao pedido correspondente ao grupo 1, acceder a súa vista de planificación, identificar a tarefa 1 (*Coordinación*) e cambiar a súa asignación de 0.6 recursos por día a 1 recurso por día mantendo a asignación específica empregada. ¿Cantos días de adianto se conseguiu co incremento de asignación de recursos de 0.6 a 1 recurso por día?
       * Data de fin con asignación 0.6 recursos por día: ___________
       * Data de fin con asignación 1 recurso por día: _____________
       * Adianto: __________

  Co adianto que se consegue coa tarefa de coordinación, ¿conséguese acabar o pedido *grupo 1* con adianto debido a?

    * Visualizar o camiño crítico do pedido 1. ¿Está a tarefa de coordinación no camiño crítico? ___________

    * Ir a tarefa 2.1 do pedido (*Aillar camarote A*) e aumentar a asignación de recursos existente a 2 recursos por día mantendo a estratexia de asignación empregada *Calcular data fin*

       * Data de fin con asignación 1 recurso por día: __________
       * Data de fin con asignación 2 recurso por día: __________
       * Adianto: __________

    * Co adianto que se consegue coa tarefa 2.1 (*Aillar camarote A*), ¿ conséguese agora acabar o pedido do *grupo 1* antes ? ¿Cal é a causa? _______________

Exercicio 2.- Realizacións de asignacións planas en asignación avanzada
=======================================================================

Este exercicio consiste en acceder a pantalla de asignación avanzada do pedido *Grupo 1* e facer os seguintes cambios:

   * Acceder ao nivel de semana de zoom.
   * Poñer na semana 12 do ano 20 horas en lugar de 40 e engadir unha semana polo final da tarefa, semana 15, con 20 horas en lugar de 40.
   * Pulsar no botón gardar.
   * Ir a perspectiva de planificación do pedido, baixar a nivel de zoom de día e localizar as semanas 12 e 15. ¿Existe unha asignación de recursos por debaixo da liña de capacidade nas semanas 12 e 15? ¿É menor que a das outras semanas contiguas? __________

Exercicio 3.- Realización de asignación con interpolación lineal
================================================================

Esta tarefa consiste na realización dunha asignación utilizando interpolación lineal con tramos. A interpolación lineal vaise a facer sobre a tarefa do pedido *Grupo 1* co nome de *Coordinacion* e os tramos que se van a utilizar son os seguintes:

   * Ao 50% de lonxitude do proxecto hai que estar ao 20% de completitude da tarefa.
   * Ao 75% de lonxitude do proxecto hai que estar ao 50% de completitude da tarefa.

¿Cántos recursos por día se adican segundo a función de interpolación lineal en cada un dos tres tramos? ¿ Canto se adica agora as distintas semanas do proxecto? _______________

Exercicio 4.- Creación de tipo de parte de traballo
===================================================

O alumno ten que crear un tipo de parte de traballo cos seguintes datos:

**Campos obrigatorios:**

   * Nome do parte: Tipo Grupo 1
   * Código: tg1
   * Data: A nivel de *liña* de parte de traballo.
   * Recurso: A nivel de *cabeceira* de parte de traballo.
   * Elemento de pedido: A nivel de *liña* de parte de traballo.
   * Administración de horas: Imputación por número de horas asignadas.

**Campos opcionais:**

   * Crear un campo de texto a nivel de liña que se denomine *Incidencias* e que teña un tamaño de 20 caracteres.
   * Crear un campo de cabeceira que se denomine *Observacións* e que teña un tamaño de 40 caracteres.

Exercicio 5.- Imputación de horas de tipo de parte de traballo
==============================================================

Este exercicio consiste en introducir os seguintes partes de traballo do tipo *Tipo Grupo 1*:

   * Parte 1:

      * Cabeceira:

         * Recurso: Javier Perez Campos.
         * Observacions: Ningunha

      *  Liñas de partes de traballo:

         ============  ===============  =============================  =============  ===========
          Data          Incidencias      Elemento de pedido             Num Horas     Tipo
         ============  ===============  =============================  =============  ===========
         3 de Marzo     Ningunha        Coordinacion Pedido Grupo 1       6            Normales
         4 de Marzo     Ningunha        Coordinacion Pedido Grupo 1       5            Normales
         5 de Marzo     Ningunha        Coordinacion Pedido Grupo 1       8            Normales
         5 de Marzo     Ningunha        Coordinacion Pedido Grupo 1       2            Extras
         8 de Marzo     Orden xefe      Coordinacion Pedido Grupo 2       4            Normales
         ============  ===============  =============================  =============  ===========

   * Parte 2:

       * Cabeceira:

          * Recurso: Javier Martinez Alvarez.
          * Observacions: Ningunha

       * Liñas de partes de traballo:

          ============  ===============  =======================================  =============  ===========
            Data          Incidencias      Elemento de pedido                     Num Horas      Tipo
          ============  ===============  =======================================  =============  ===========
           25 de Mayo      Ningunha       Repasar soldadura ocos Pedido Grupo 1        8         Normales
           26 de Mayo      Ningunha       Repasar soldadura ocos Pedido Grupo 1        9         Normales
           27 de Mayo      Ningunha       Repasar soldadura ocos Pedido Grupo 1        8         Normales
           28 de Mayo      Ningunha       Repasar soldadura ocos Pedido Grupo 1        4         Extra
           31 de Mayo      Orden xefe     Repasar soldadura ocos Pedido Grupo 1        9         Normales
          ============  ===============  =======================================  =============  ===========

Unha vez introducios os partes de traballo, as preguntas son:

  * Visualizar na pantalla de planificación de pedidos canto é a porcentaxe de horas que se imputaron as dúas tarefas as cales se imputaron partes de traballo:

     * Porcentaxe de horas imputadas en elemento de pedido *Coordinacion*: _____________
     * Porcentaxe de horas imputadas en elemento de pedido *Repasar soldadura ocos*: __________

   * Visualizar na pantalla dos elementos de pedido canto son o total de horas asignadas aos elementos de pedido:

      * Total de horas imputadas en elemento de pedido *Coordinacion*: ____________
      * Total de horas imputadas en elemento de pedido *Repasar soldadura ocos*: ___________

Exercicio 6 .- Introducir os seguintes tipos de horas en NavalPlan
==================================================================

   * **Tipo de hora:**

      * Código do tipo: nm_metal
      * Nome: Normal convenio metal
      * Prezo por defecto: 30
      * Activado: Sí.

   * **Tipo de hora:**

      * Código do tipo: ex_metal
      * Nome: Extra convenio metal
      * Prezo por defecto: 40
      * Activado: Sí.

Exercicio 7 .- Crear as seguintes categorias de coste
=====================================================

   * **Nome da categoria:** Operarios con menos de 5 anos de experiencia. Ten as seguintes asignacións de costes de horas:

        * Asignación 1:

         * *Tipo de hora:* Hora normal convenio metal
         * *Prezo por hora:* 25
         * *Data de inicio:* 01/01/2010
         * *Data de fin:* 31/05/2010

        * Asignación 2:

         * *Tipo de hora:* Hora normal convenio metal
         * *Prezo por hora:* 27
         * *Data de inicio:* 01/06/2010
         * *Data de fin:* - en branco -

        * Asignación 3:

         * *Tipo de hora:* Hora extra convenio metal
         * *Prezo por hora:* 30
         * *Data de inicio:* 01/06/2010
         * *Data de fin:* 31/05/2010

        * Asignación 4:

         * *Tipo de hora:* Hora extra convenio metal
         * *Prezo por hora:* 32
         * *Data de inicio:* 01/06/2010
         * *Data de fin:* - branco -

   * **Nome da categoría:** Operarios con máis de 5 anos de experiencia. Ten as seguintes asignacións de costes de horas:

        * Asignación 1:

         * *Tipo de hora:* Hora normal convenio metal
         * *Prezo por hora:* 30
         * *Data de inicio:* 01/01/2010
         * *Data de fin:* 31/05/2010

        * Asignación 2:

         * *Tipo de hora:* Hora normal convenio metal
         * *Prezo por hora:* 32
         * *Data de inicio:*  01/06/2010
         * *Data de fin:* - en branco -

        * Asignación 3:

         * *Tipo de hora:* Hora extra convenio metal
         * *Prezo por hora:* 40
         * *Data de inicio:* 01/06/2010
         * *Data de fin:* 31/05/2010

        * Asignación 4:

         * *Tipo de hora:* Hora extra convenio metal
         * *Prezo por hora:* 42
         * *Data de inicio:*  01/06/2010
         * *Data de fin:* - branco -

Exercicio 7 .- Asignar os traballadores as categorías de coste
==============================================================

Asignar os traballadores seguintes as categorías de coste que se indican.

         * Maria Perez Mariño - Operario con menos de 5 anos de experiencia - Dende 01/03/2010
         * Javier Perez Campos - Operario con máis de 5 anos de experiencia - Dende 01/03/2010


Exercicio 8 .- Visualizar o coste das tarefas do pedido
=========================================================================================================

Hai que visualizar o coste das tarefas do pedido *Grupo 1* a través do informe **Costes por recurso**.  ¿Canto é o coste que se leva gastado na tarefa de coordinacion? ___________


Exercicio 9 .- Administración de materiais
=============================================

Crear as seguintes categorías de materiais cos materiais que se indican en cada unha delas:

   1.- (Categoría) Tornillos
      1.1.- (Categoría) Tornillos de bronce
           -  (Material) Código: t1, Descrición: Tornillo: 15 mm, Prezo: 10, Unidades: unidades.
           -  (Material) Código: t2, Descrición: Tornillo: 20 mm, Prezo: 12, Unidades: unidades.
      1.2.- (Categoría) Tornillos de aceiro
           -  (Material) Código: t3, Descrición: Tornillo: 17 mm, Prezo: 10, Unidades: unidades.
           -  (Material) Código: t4, Descrición: Tornillo: 19 mm, Prezo: 12, Unidades: unidades.


Exercicio 10 .- Asignación de materiais en elemento de pedido
=============================================================

Asignar os seguintes materiais os elementos de pedido *Grupo 1*:

   * Tarefa primeira do Bloque 2: Teito de madeira de camarote A

         * Tornillo t2, Data de recepción estimada: 15 de Abril, Unidades: 10, Prezo da unidade: 12, Estado: PENDING.

   * Tarefa segunda do Bloque 2: Cama e mesilla de camarote A

         * Tornillo t3, Data de recepción estimada: 9 de Mayo, Unidades: 10, Prezo da unidade: 10, Estado: PENDING.


Exercicio 11 .- Visualización de necesidades de materiais para o pedido
=======================================================================

Calcular o informe de necesidades de materiais para o pedido *Grupo 1*.

Exercicio 12 .- Creacion de formulario de calidade
==================================================

Crear un novo formulario de calidade:

   * *Nome*: Formulario de Calidade Grupo 1
   * *Tipo de Formulario*: Porcentaxe
   * *Notificar Avance*: Marcado

Introducir os seguintes elementos do formulario de calidade:

   * Control de calidade 1 -  25%
   * Control de calidade 2 -  50%
   * Control de calidade 3 -  75%
   * Control de calidade 4 - 100%


Exercicio 13 .- Asignación de formulario de calidade
====================================================

Asignar a pedido *Grupo 1* o formulario de Calidade Grupo1.

Marcar o control de calidade 1 como superado con data do 20 de Marzo de 2010.

Grabar o pedido.


Exercicio 14 .- Creación de formulario de calidade como avance
==============================================================

Ir a nivel de pedido *Grupo 1* a sección de Formularios de Calidade.

Marcar o formulario de Calidade Grupo1 que notifica Avance.

Marcar que o novo avance en base a calidade é o avance que propaga na sección de avances do pedido.


Exercicio 15 .- Subcontratación de tarefas
==========================================

Subcontratar a tarefa do pedido *Grupo 1*, *terceira do bloque 2*, é dicir, a tarefa con nome *Escotillas de camarote*.

Os datos da subcontratación serán:

   * Empresa externa: curso___(curso de destino)
   * Descrición do traballo: pedido do grupo 1 do curso ___(curso de orixe).
   * Prezo da subcontratación: 10000
   * Código da subcontratación: ped_gr1_cu1
   * Data de fin pedido: 1 de Decembro de 2010.

Unha vez marcada a tarefa como subcontratada realizar o envío do pedido a empresa curso2.

Exercicio 16 .- Reporte de avances sobre tarefas subcontratadas
===============================================================

Ir ao pedido *pedido do grupo 1 do curso ___* e introducir un avance de tipo *Subcontractor* con valor de 30% a data 15 de Marzo de 2010.

Ir a área de notificación de avances e enviar o avance introducido a empresa curso2.

Comprobar que a tarefa subcontratada do pedido  *Grupo 1*, *terceira do bloque 2* recibe a notificación de avances da empresa curso___.


Exercicio 17 .- Crear modelo de plantilla de traballo e aplicalo
================================================================

Crear un modelo de pedido do grupo de líneas de pedido co nome *Bloque 1* dentro do *Grupo 1* e co nome *modelo bloque 1 - Grupo 1*

Aplicar o *modelo bloque 1 - Grupo 1*  ao pedido do *Grupo 1*.

Consultar o modelo *modelo bloque 1 - Grupo 1* e consultar o histórico de asignacións e pestaña de histórico de estadísticas do modelo.

Exercicio 18 .- Crear usuarios e configurar os seus permisos
============================================================

Crear un usuario cos seguintes datos:

   * Nome de usuario: grupo1_permisos
   * Contrasional: grupo1_permisos
   * Roles de usuario: Ningún.
   * Perfís de usuario: Ningún.

Acceder ao pedido con nome *Grupo 1* e dar permiso de lectura ao usuario *grupo1_permisos*.

Saír da aplicación do usuario co que se está conectado *grupo1* e entrar co novo usuario *grupo1_permisos*. Comprobar que ao entrar co usuario *grupo1_permisos* só se pode ver o pedido *Grupo 1* e que non se pode modificar.

Probar que se se configura no pedido *Grupo 1* o usuario *grupo1_permisos* con permiso de escritura ao entrar con él pódese modificar o pedido *Grupo 1*.

Exercicio 19 .- Outros informes
===============================

Visualizar o informe *Progreso de traballo por tarefa* para o pedido do *Grupo 1*

Datos para interpretar o  informe:

   * Diferencia en planificación: (Avance Medido * Horas planificadas total) - Horas planificadas
   * Diferencia en coste: (Avance Medido * Horas planificadas total) - Horas imputadas
   * Ratio desfase en coste: Avance Medido / Avance imputado
   * Ratio desfase en planificación: Avance Medido / Avance planificado
