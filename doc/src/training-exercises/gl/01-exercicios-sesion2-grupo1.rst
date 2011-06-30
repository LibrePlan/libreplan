Exercicios Grupo 1 - Sesión 2
#############################

.. contents::

Exercicio  1. Camiño crítico, Montecarlo, consolidación de progresos (30m)
==========================================================================

A) Este exercicio consiste en familiarizarse coa asignación de recursos. O exercicio está composto en varios puntos:

    *  Ir ao pedido correspondente ao grupo 1, acceder a súa vista de planificación, identificar a tarefa 1 (*Xestión*) e cambiar a súa asignación de 0.6 recursos por día a 1 recurso por día mantendo a asignación específica empregada. ¿Cantos días de adianto se conseguiu co incremento de asignación de recursos de 0.6 a 1 recurso por día?

       * Data de fin con asignación 0.6 recursos por día: ___________
       * Data de fin con asignación 1 recurso por día: _____________
       * Adianto: __________

  Co adianto que se consegue coa tarefa de "Xestión", ¿conséguese acabar o pedido *grupo 1* con adianto debido a?

    * Visualizar o camiño crítico do pedido 1. ¿Está a tarefa de Xestión no camiño crítico? ___________

    * Ir a tarefa 2.1 do pedido (*Soldar unions do teito*) e aumentar a asignación de recursos existente a 2.5 recursos por día mantendo a estratexia de asignación empregada *Calcular data fin*

       * Data de fin con asignación 1 recurso por día: __________
       * Data de fin con asignación 2 recurso por día: __________
       * Adianto: __________

    * Co adianto que se consegue coa tarefa 2.1 (*Soldar unions do teito*), ¿ conséguese agora acabar o pedido do *grupo 1* antes ? ¿Cal é a causa? _______________


B) Consolidar o avance do 20% para o 1 de Outubro sobre a tarefa "Soldar unións do teito".

Coa consolidación realizada, ¿que sucedeu coa data de finalización da tarefa? ¿Imos con adianto ou atraso? ¿Somos capaces de safisfacer o deadline establecido?

C) Comprobar diferentes tipos de avances:

   * ¿Cal é o progreso global do proxecto?

   * ¿Cal é o progreso segundo o camiño crítico e en base a duración do proxecto?

   * ¿Cal é o progreso segundo o número de horas do camiño crítico do proxecto?

E) Método de Montecarlo:

   * Acceder a NavalPlan Configuración e habilitar o método de Montecarlo.

   * Acceder ó pedido do grupo 1.

   * Premer na perspectiva do método de Montecarlo.

   * Seleccionar 1000 iteracións e agrupación por semanas.

   * Premer en "Ir!".

   * ¿Cal é a semana máis probable de finalización do proxecto?


Exercicio 2 .- Subcontratación de tarefas (10m)
================================================

Para poder subcontratar unha tarefa, antes é necesario contar con unha empresa dada de alta con NavalPlan instalado. Crear dita empresa:

   * Nome: Empresa subcontratada grupo 1
   * NIF: COMPANY_CODE
   * Cliente: Si.
   * Subcontractor: Si.
   * Usuario: wswriter
   * Interactúa con aplicaciones: Si.
   * URI: http://localhost:8080/navalplanner-webapp/
   * Login: wswriter
   * Contraseña: wswriter

(Solo un grupo vai poder gravar esta empresa).

Exercicio 3 .- Subcontratación de tarefas (15m)
===============================================

Subcontratar a tarefa do pedido *Grupo 1*, *terceira do bloque 2*, é dicir, a tarefa con nome *Escotillas de camarote*.

Os datos da subcontratación serán:

   * Empresa externa: curso___(curso de destino)
   * Descrición do traballo: pedido do grupo 1 do curso ___(curso de orixe).
   * Prezo da subcontratación: 10000
   * Código da subcontratación: ped_gr1_cu1
   * Data de fin pedido: Unha semana antes da proposta pola aplicación.

Unha vez marcada a tarefa como subcontratada realizar o envío do pedido a empresa curso___.

Exercicio 4 .- Reporte de avances sobre tarefas subcontratadas (10m)
====================================================================

Ir ao pedido *pedido do grupo 1 do curso ___*, editar a tarefa do pedido e introducir un avance de tipo *Subcontractor* con valor de 30% con data 12 de Agosto.

Ir a área de notificación de avances e enviar o avance introducido a empresa curso2.

Comprobar que a tarefa subcontratada do pedido  *Grupo 1*, *terceira do bloque 2* recibe a notificación de avances da empresa curso___. ¿Que porcentaxe
de avance aparece na tarefa subcontratada?

Exercicio 5 .- Reporte de avances sobre tarefas subcontratadas (10m)
=====================================================================

Ir ao pedido *pedido do grupo 1 do curso ___*, editar a tarefa do pedido e introducir un avance de tipo *Subcontractor* con valor de 30% con data 12 de Agosto.

Ir a área de notificación de avances e enviar o avance introducido a empresa curso___.

Comprobar que a tarefa subcontratada do pedido  *Grupo 1*, *terceira do bloque 2* recibe a notificación de avances da empresa curso___. ¿Que porcentaxe
de avance aparece na tarefa subcontratada?

Exercicio 6 .- Planificación cara atrás
=======================================

Acceder á edición do "pedido do grupo 1 do curso". Engadir catro novas tarefas como fillas da única tarefa que ten o proxecto:

   * Tarefa 1: 40 horas.
   * Tarefa 2: 40 horas.
   * Tarefa 3: 60 horas.
   * Tarefa 4: 60 horas.

Acceder ós datos xerais do pedido e modificar o modo de planificación a "Atrás".

Acceder á planificación do proxecto:

   * Establecer dependencia entre tarefa 3 e tarefa 4 de tipo *FIN-INICIO*.
   * Establecer dependencia entre tarefa 2 e tarefa 3 de tipo *FIN-INICIO*.
   * Establecer dependencia entre tarefa 1 e tarefa 2 de tipo *FIN-INICIO*.

¿Como se van colocando as tarefas?

Acceder á asignación de recursos:

   * Asignar a Javier Pérez Campos a razón de 1 recurso por día coas estratexias por defecto á tarefa 4.
   * Asignar a Javier Pérez Campos a razón de 1 recurso por día coas estratexias por defecto á tarefa 3.
   * Asignar a Javier Pérez Campos a razón de 1 recurso por día coas estratexias por defecto á tarefa 2.
   * Asignar a Javier Pérez Campos a razón de 1 recurso por día coas estratexias por defecto á tarefa 1.


Exercicio 7 .- Realizacións de asignacións planas en asignación avanzada (10m) -
================================================================================

Este exercicio consiste en acceder a pantalla de asignación avanzada do pedido *Grupo 1* e facer os seguintes cambios:

   * Acceder ao nivel de semana de zoom.
   * Na tarefa "Coordinación", poñer na semana 8 do ano 25 horas en lugar de 40 e modificar na semana 9, con 55 horas en lugar de 40.
   * Pulsar no botón gardar.
   * Ir a perspectiva de planificación do pedido, baixar a nivel de zoom de día e localizar as semanas 8 e 9. ¿Existe unha asignación de recursos por debaixo da liña de capacidade nas semanas 8 e 9? ¿É menor que a das outras semanas contiguas? __________

Exercicio 8 .- Realización de asignación con interpolación lineal (15m)
=======================================================================

Esta tarefa consiste na realización dunha asignación utilizando interpolación lineal con tramos. A interpolación lineal vaise a facer sobre a tarefa do pedido *Grupo 1* co nome de *Coordinación* e os tramos que se van a utilizar son os seguintes:

   * Ao 50% de lonxitude do proxecto hai que estar ao 25% de completitude da tarefa.
   * Ao 75% de lonxitude do proxecto hai que estar ao 50% de completitude da tarefa.

¿Cántos recursos por día se adican segundo a función de interpolación lineal en cada un dos tres tramos? ¿ Canto se adica agora as distintas semanas do proxecto? _______________

Exercicio 9 .- Crear modelo de plantilla de traballo e aplicalo (15m)
======================================================================

Crear un modelo de pedido do grupo de líneas de pedido co nome *Bloque 1* dentro do *Grupo 1* e co nome *modelo bloque 1 - Grupo 1*

Aplicar o *modelo bloque 1 - Grupo 1*  ao pedido do *Grupo 1*. Renomear como "bloque 3" e gardar o pedido.

Consultar o modelo *modelo bloque 1 - Grupo 1* e consultar o histórico de asignacións e pestaña de histórico de estadísticas do modelo.


Exercicio 10.- Creación de tipo de parte de traballo (15m)
==========================================================

O alumno ten que crear un tipo de parte de traballo cos seguintes datos:

**Campos obrigatorios:**

   * Nome do parte: Tipo Grupo 1
   * Código: tg1
   * Data: A nivel de *liña* de parte de traballo.
   * Recurso: A nivel de *cabeceira* de parte de traballo.
   * Elemento de pedido: A nivel de *liña* de parte de traballo.
   * Administración de horas: Número de horas asignadas.

**Campos opcionais:**

   * Crear un campo de texto a nivel de liña que se denomine *Incidencias* e que teña un tamaño de 20 caracteres.
   * Crear un campo de tipo de etiqueta a nivel de cabeceira que inclúa o centro de custo. Incluír como etiqueta por defecto "CC Vigo".


Exercicio 11.- Introducir os seguintes tipos de horas en NavalPlan (10m)
========================================================================

   * **Tipo de hora:**

      * Nome: Normal convenio metal
      * Prezo por defecto: 15
      * Activado: Sí.

   * **Tipo de hora:**

      * Nome: Extra convenio metal
      * Prezo por defecto: 17
      * Activado: Sí.

Exercicio 12.- Imputación de horas de tipo de parte de traballo (15m)
======================================================================

Este exercicio consiste en introducir os seguintes partes de traballo do tipo *Tipo Grupo 1*:

   * Parte 1:

      * Cabeceira:

         * Recurso: Javier Perez Campos.
         * Observacions: Ningunha

      *  Liñas de partes de traballo:

        ====================  ======================  =============================  =============  ===========================
          Data                 Incid.                 Elemento de pedido             Num Horas      Tipo
        ====================  ======================  =============================  =============  ===========================
         1 de Febreiro        Ningunha                Coordinación Pedido Grupo 1            9      Hora normal convenio metal
         2 de Febreiro        Ningunha                Coordinación Pedido Grupo 1            8      Hora normal convenio metal
         3 de Febreiro        Ningunha                Coordinación Pedido Grupo 1            8      Hora normal convenio metal
         4 de Febreiro        Ningunha                Coordinación Pedido Grupo 1            4      Hora normal convenio metal
         5 de Febreiro        Ningunha                Coordinación Pedido Grupo 1            2      Hora extra convenio metal
         7 de Febreiro        Orden xefe              Coordinación Pedido Grupo 2            4      Hora normal convenio metal
        ====================  ======================  =============================  =============  ===========================

   * Parte 2:

       * Cabeceira:

          * Recurso: Javier Martinez Alvarez.
          * Observacions: Ningunha

       * Liñas de partes de traballo:

          ====================  =========================  ==========================================  =============  ===========================
            Data                 Incid.                    Elemento de pedido                          Num Horas      Tipo
          ====================  =========================  ==========================================  =============  ===========================
           2 de Febreiro        Ningunha                   Soldar unións do teito Pedido Grupo 1        9             Hora normal convenio metal
           3 de Febreiro        Ningunha                   Soldar unións do teito Pedido Grupo 1        9             Hora normal convenio metal
           4 de Febreiro        Ningunha                   Soldar unións do teito Pedido Grupo 1        4             Hora normal convenio metal
           5 de Febreiro        Ningunha                   Soldar unións do teito Pedido Grupo 1        4             Hora extra convenio metal
           7 de Febreiro        Orden xefe                 Soldar unións do teito Pedido Grupo 1        9             Hora normal convenio metal
          ====================  =========================  ==========================================  =============  ===========================

Unha vez introducios os partes de traballo, as preguntas son:

  * Visualizar na pantalla de planificación de pedidos canto é a porcentaxe de horas que se imputaron as dúas tarefas as cales se imputaron partes de traballo:

     * Porcentaxe de horas imputadas en elemento de pedido *Coordinación*: _____________
     * Porcentaxe de horas imputadas en elemento de pedido *Soldar unións do teito*: __________

   * Visualizar na pantalla dos elementos de pedido canto son o total de horas asignadas aos elementos de pedido:

      * Total de horas imputadas en elemento de pedido *Coordinación*: ____________
      * Total de horas imputadas en elemento de pedido *Soldar unións do teito*: ___________


Exercicio 13 .- Crear as seguintes categorias de custo (10m)
============================================================

   * **Nome da categoria:** "Operarios con menos de 5 anos de experiencia grupo 1". Ten as seguintes asignacións de costes de horas:

        * Asignación 1:

         * *Tipo de hora:* Hora normal convenio metal
         * *Prezo por hora:* 15
         * *Data de inicio:* Data actual
         * *Data de fin:* 31/12/2011

        * Asignación 2:

         * *Tipo de hora:* Hora normal convenio metal
         * *Prezo por hora:* 16
         * *Data de inicio:* 01/01/2012
         * *Data de fin:* - en branco -

        * Asignación 3:

         * *Tipo de hora:* Hora extra convenio metal
         * *Prezo por hora:* 17
         * *Data de inicio:* Data actual
         * *Data de fin:* 31/12/2011

        * Asignación 4:

         * *Tipo de hora:* Hora extra convenio metal
         * *Prezo por hora:* 18
         * *Data de inicio:* 01/01/2012
         * *Data de fin:* - branco -

   * **Nome da categoría:** "Operarios con máis de 5 anos de experiencia grupo 1". Ten as seguintes asignacións de costes de horas:

        * Asignación 1:

         * *Tipo de hora:* Hora normal convenio metal
         * *Prezo por hora:* 17
         * *Data de inicio:* 01/02/2011
         * *Data de fin:* 31/12/2011

        * Asignación 2:

         * *Tipo de hora:* Hora normal convenio metal
         * *Prezo por hora:* 18
         * *Data de inicio:*  01/01/2012
         * *Data de fin:* - en branco -

        * Asignación 3:

         * *Tipo de hora:* Hora extra convenio metal
         * *Prezo por hora:* 20
         * *Data de inicio:* 01/02/2011
         * *Data de fin:* 31/12/2011

        * Asignación 4:

         * *Tipo de hora:* Hora extra convenio metal
         * *Prezo por hora:* 21
         * *Data de inicio:*  01/01/2012
         * *Data de fin:* - branco -

Exercicio 14 .- Asignar os traballadores as categorías de coste (5m)
====================================================================

Asignar os traballadores seguintes as categorías de coste que se indican.

         * Maria Perez Mariño - Operario con menos de 5 anos de experiencia - Dende 01/02/2011
         * Javier Perez Campos - Operario con máis de 5 anos de experiencia - Dende 01/02/2011


Exercicio 15 .- Método do valor gañado
======================================

Acceder á vista de Gantt e seleccionar a pestana de "Valor Gañado".

Datos para interpretar indicadores básicos:

   * BCWS: Custo presupostado do traballo planificado. Calcúlase a partir das horas planificadas ata unha data.
   * ACWP: Custo real do traballo realizado. Calcúlase a partir das horas adicadas ata unha data.
   * BCWP: Custo presupostado do traballo realizado. Calcúlase a partir de multiplicar o progreso das tarefas pola cantidade estimada das tarefas.

Datos para interpretar indicadores derivados:

    * CV: desviación en coste CV = BCWP - ACWP
    * SV: desviación en planificación SV = BCWP - BCWS
    * BAC: total custo planificado BAC = max (BCWS)
    * EAC: estimación do custo total actual EAC = (ACWP/ BCWP) * BAC
    * VAC: desviacion ó custo final VAC= BAC - EAC
    * ETC: estimación do custo pendente = EAC - ACWP
    * CPI: eficiencia en custo CPI = BCWP / ACWP
    * SPI: eficiencia en programación SPI= BCWP / BCWS


Exercicio 16 .- Visualizar o coste das tarefas do pedido (10m)
=========================================================================================================

Hai que visualizar o coste das tarefas do pedido *Grupo 1* a través do informe **Costes por recurso**.  ¿Canto é o coste que se leva gastado na tarefa de Xestión? ___________


Exercicio 17 .- Administración de materiais (15m)
=================================================

Crear as seguintes categorías de materiais cos materiais que se indican en cada unha delas:

   1.- (Categoría) Tornillos grupo 1
      1.1.- (Categoría) Tornillos de bronce do grupo 1 (no autogenerado)
           -  (Material) Código: t1g1, Descrición: Tornillo grupo 1: 15 mm, Prezo: 0.5, Unidades: unidades.
           -  (Material) Código: t2g1, Descrición: Tornillo grupo 1: 20 mm, Prezo: 0.75, Unidades: unidades.
      1.2.- (Categoría) Tornillos de aceiro do grupo 1 (no autogenerado)
           -  (Material) Código: t3g1, Descrición: Tornillo grupo 1: 17 mm, Prezo: 0.5, Unidades: unidades.
           -  (Material) Código: t4g1, Descrición: Tornillo grupo 1: 19 mm, Prezo: 0.75, Unidades: unidades.


Exercicio 18 .- Asignación de materiais en elemento de pedido (10m)
===================================================================

Asignar os seguintes materiais os elementos de pedido *Grupo 1*:

   * Tarefa primeira do Bloque 2: Teito de madeira de camarote A

         * Tornillo grupo 1: 20mm, Data de recepción estimada: 25 de Abril, Unidades: 100, Prezo da unidade: 12, Estado: PENDING.

   * Tarefa segunda do Bloque 2: Cama e mesilla de camarote A

         * Tornillo grupo 2: 17mm, Data de recepción estimada: 29 de Abril, Unidades: 100, Prezo da unidade: 0,5, Estado: PENDING.

   * Calcular o informe de necesidades de materiais para o pedido *Grupo 1*.

Exercicio 19 .- Creacion de formulario de calidade (15m)
========================================================

Crear un novo formulario de calidade:

   * *Nome*: Formulario de Calidade Grupo 1
   * *Tipo de Formulario*: Porcentaxe
   * *Notificar Avance*: Marcado

Introducir os seguintes elementos do formulario de calidade:

   * Control de calidade 1 -  25%
   * Control de calidade 2 -  50%
   * Control de calidade 3 -  75%
   * Control de calidade 4 - 100%


Exercicio 20 .- Asignación de formulario de calidade (10m)
==========================================================

Asignar a pedido *Grupo 1* o formulario de Calidade Grupo1.

Marcar o control de calidade 1 como superado con data do 1 de Marzo de 2010.

Grabar o pedido.


Exercicio 21 .- Creación de formulario de calidade como avance (10m)
====================================================================

Ir a nivel de pedido *Grupo 1* a sección de Formularios de Calidade.

Marcar o formulario de Calidade Grupo1 que notifica Avance.

Marcar que o novo avance en base a calidade é o avance que propaga na sección de avances do pedido.


Exercicio 22 .- Crear usuarios e configurar os seus permisos (15m)
==================================================================

Crear un usuario cos seguintes datos:

   * Nome de usuario: grupo1_permisos
   * Contrasional: grupo1_permisos
   * Roles de usuario: Ningún.
   * Perfís de usuario: Ningún.

Acceder ao pedido con nome *Grupo 1* e dar permiso de lectura ao usuario *grupo1_permisos*.

Saír da aplicación do usuario co que se está conectado *grupo1* e entrar co novo usuario *grupo1_permisos*. Comprobar que ao entrar co usuario *grupo1_permisos* só se pode ver o pedido *Grupo 1* e que non se pode modificar.

Probar que se se configura no pedido *Grupo 1* o usuario *grupo1_permisos* con permiso de escritura ao entrar con él pódese modificar o pedido *Grupo 1*.

Exercicio 23 .- Outros informes (10m)
=====================================

Visualizar o informe *Progreso de traballo por tarefa* para o pedido do *Grupo 1*

Datos para interpretar o  informe:

   * Diferencia en planificación: (Avance Medido * Horas planificadas total) - Horas planificadas
   * Diferencia en coste: (Avance Medido * Horas planificadas total) - Horas imputadas
   * Ratio desfase en coste: Avance Medido / Avance imputado
   * Ratio desfase en planificación: Avance Medido / Avance planificado

Exercicio 24 .- Recursos limitantes (25m)
=========================================

Crear un tipo de criterios:
   * Nome: Tipo máquina grupo 1
   * Tipo de criterio: MAQUINA
   * Asignar criterios: Torno grupo 1

Crear un recurso de carácter limitante de tipo máquina:
   * Nome: Torno 20mm grupo 1
   * Descripción: Torno que utilizamos para ...
   * Recursos limitantes: Recurso Limitante.
   * Criterio: Torno grupo 1
   * Calendario: Galicia xornada completa.

Acceder ó pedido "Pedido grupo 1" e acceder ás propiedades da tarefa "Soldar unións do chan" do "bloque 3".

   * Seleccionar na pestana de "Propiedades da tarefa" e cambiar a "Recursos limitantes".
   * Seleccionar o recurso manualmente
   * Acceder a "Planificación -> Recursos limitantes".
   * Asignar tarefa a cola de Torno "Automáticamente".

