--------
Sesión 2
--------

.. contents:: Contenidos

Ejercicio 1.- Familiarización con el camino crítico
===================================================

En este ejercicio hay que realizar varios puntos:

**1.- Visualizar el camino crítico para el proyecto que se está planificando.
¿Cuántas tareas forman parte de el?**

  Respuesta: _________________________


**2.- Se considera que realmente se ha sobreestimado el tiempo para
coordinación. Con una gestión eficiente del equipo se prevé que se puede pasar
de 350h a 300h de coordinación. Por tanto, realizar la reasignación de la tarea
de coordinación conservando la estrategia y cambiando únicamente el número de
horas.**

  Respuesta:

     * Fecha original de terminación del pedido: _____________________
     * Nueva fecha de terminación del pedido: _____________________
     * Adelanto: _____________________________

**3.- Debido al cambio en la asignación de la tarea de coordinación. ¿Se ha
conseguido acabar el proyecto antes? Si se ha conseguido acabar explicar por
qué.**

Respuesta: ______________________________________________________________


Ejercicio 2.- Consolidación de progresos
========================================

Tras pasar un cierto tiempo desde la medición de los progresos por parte del
analista, el gestor de proyecto mantiene una reunión de coordinación con el
analista y tras revisar su trabajo, deciden confirmar que se han analizado
correctamente 3 de los 6 casos de uso de los que consta el proyecto. Es decir,
se decide realizar una consolidación de las tres mediciones de progreso y, por
tanto, de la última que indica que se terminó de analizar el caso de uso 3 el 25
de Marzo.

Sobre estos supuestos, el usuario tiene que realizar los siguientes puntos:

**1.- Poniéndose en el papel del gestor de proyecto, el hecho de recibir un
avance del 50% (3 de 6 unidades) el 25 de Marzo, ¿qué conclusiones saca?. ¿Va
la tarea adelantada o atrasada? ¿Cuál es el progreso planificado para la tarea
de análisis en la fecha del 25 de Marzo?**

  Respuesta: 25/29*100= 86% es el progreso teórico.

**2.- Realizar la consolidación de progreso que el gestor de proyecto recibe
tras la reunión con el analista.**

**3.- Tras realizar esta consolidación comprobar cuál ha sido el resultado de la
misma y responder a los siguientes datos:**

  Respuesta:

    * Horas que se reasignan desde la fecha de consolidación hasta el final de
      la tarea: _____________________
    * Recursos por día que tienen que ser asignados en el tramo no consolidado y
      que la aplicación calcula: _____________________

**4.- No se pueden asumir los recursos por día que calcula la aplicación. Por
tanto, reasignar la tarea de análisis en el período no consolidado con los
siguientes datos:**

   * Estrategia de asignación: Calcular días laborables.
   * Recursos por día: 1.
   * Horas por asignar: 79h.

   * ¿Afecta la nueva duración de la tarea de análisis al fin del proyecto?  _____________________
   * ¿Qué margen existe ahora con la fecha de fin de proyecto (fecha de
     deadline)? _____________________

     ¿Se sigue cumpliendo? _____________________

Ejercicio 3.- Medición de progreso del proyecto
===============================================

   * ¿Cuál es el progreso global del proyecto? _____________________

   * ¿Cuál es el progreso segundo el camino crítico y en base a duración del
     proyecto? _____________________

   * ¿Cuál es el progreso segundo el número de horas del camino crítico del
     proyecto? _____________________

   * ¿Sabrías explicar por qué el progreso es distinto? _____________________

     ¿A qué se debe en este caso concreto?  ___________________________________________


Ejercicio 4.- Simulación de duración del proyecto siguiendo el método de Montecarlo
===================================================================================

   * Acceder la ventana de Configuración de LibrePlan y habilitar el método de
     Montecarlo.

   * Acceder al proyecto *Proyecto 1 - Programa de contabilidad*.

   * Presionar en la perspectiva del método de Montecarlo.

   * Seleccionar 10000 iteraciones y agrupación por semanas.

   * Presionar en *Ir!*.

   * ¿Cuál es la semana más probable de finalización del proyecto? _____________________


Ejercicio 5 .- Subcontratación de tareas
========================================

Para poder subcontratar una tarea, antes es necesario contar con una empresa
dada de alta con *LibrePlan* instalado. Crear dicha empresa:

   * Nombre: Empresa subcontratada
   * NIF: COMPANY_CODE
   * Cliente: Sí.
   * Subcontratista: Sí.
   * Usuario: wswriter
   * Interactúa con aplicaciones: Sí.
   * URI: http://localhost:8080/libreplan-webapp/
   * Login: wswriter
   * Contraseña: wswriter


Ejercicio 6 .- Subcontratación de tareas
========================================

Este ejercicio consiste en realizar una subcontratación de una nueva tarea del
proyecto con el que se está trabajando. Supóngase el caso de que se quiere
contar con una empresa especializada en *testing* para realizar pruebas
funcionales de la aplicación al final del desarrollo. Los pasos de los que
consta el ejercicio para realizar la subcontratación son los siguientes:

   * Crear una nueva tarea en el proyecto denominada *Subcontratación pruebas*.
     La tarea consistirá en 100h de pruebas.
   * Crear una dependencia *FIN-INICIO* desde la tarea de *Modulo de facturas*
     como origen hacia la tarea.
   * Realizar la subcontratación con los siguientes datos:
       * Empresa a la que se subcontrata: Empresa subcontratada.
       * Descripción del trabajo: Pruebas funcionales.
       * Precio de la subcontratacion: 6.000 euros.
       * Código de la subcontratación: SUB_PF_01
       * Hito de fecha de fin: 18 de Julio de 2011.
       * Marcar enviar grupo de horas.
   * Grabar el pedido y realizar el envío de la tarea subcontratada.


Ejercicio 7 .- Reporte de avances sobre tareas subcontratadas
=============================================================

El ejercicio consiste en los siguientes puntos:

* Ir al proyecto creado por el envío de la subcontratación, que se llamará
  *Pruebas funcionales* e introducir un nuevo tipo de progreso de tipo
  **Subcontractor** sobre la tarea *Subcontratación pruebas*.

* Introducir una medida de progreso del 30% en el progreso **Subcontractor** con
  fecha 14 de Julio de 2011.

* Ir al área de notificación de progresos y notificar el progreso a la empresa
  contratista.

* Comprobar en el pedido de la empresa contratista como se recibe correctamente
  dicho progreso. Esto se puede realizar yendo a la vista de planificación de
  proyecto y, una vez ahí, pulsando en el botón para visualizar los progresos.


Ejercicio 8 .- Planificación hacia atrás
========================================

Crear un nuevo proyecto de planificación con los siguientes datos:

    * Nombre: Proyecto 2 - Proyecto de planificación hacia atrás.
    * Fecha inicio: 01/03/2011.
    * Fecha fin: 01/11/2011.
    * En la pestaña de datos generales seleccionar como modo de planificación
      *Hacia atrás*.
    * Introducir 4 tareas con los siguientes nombre y número de horas:

        * Tarea 1: 40 horas.
        * Tarea 2: 40 horas.
        * Tarea 3: 60 horas.
        * Tarea 4: 60 horas.

    * Acceder a la planificación del proyecto:

        * Establecer dependencia entre tarea 3 y tarea 4 de tipo *FIN-INICIO*.
        * Establecer dependencia entre tarea 2 y tarea 3 de tipo *FIN-INICIO*.
        * Establecer dependencia entre tarea 1 y tarea 2 de tipo *FIN-INICIO*.

    * ¿Cómo se van colocando las tareas?  ___________________________________________
    * Realizar las siguientes asignaciones de recursos:

        * Asignar a Raúl González Alvárez a razón de 1 recurso por día con las
          estrategias por defecto a la tarea 4.
        * Asignar a Raúl González Alvárez a razón de 1 recurso por día con las
          estrategias por defecto a la tarea 3.
        * Asignar a Raúl González Alvárez a razón de 1 recurso por día con las
          estrategias por defecto a la tarea 2.
        * Asignar a Raúl González Alvárez a razón de 1 recurso por día con las
          estrategias por defecto a la tarea 1.


Ejercicio 9 .- Realizaciones de asignaciones planas en asignación avanzada
==========================================================================

El recurso Raúl González Alvárez está sobrecargado en la semana 27 del año
debido a la coincidencia de la tarea *Pruebas* con la tarea *Coordinación*. Lo
que se va a hacer para solucionar esta situación es operar a través de la
pantalla de asignación avanzada. Los cambios que se quieren acometer son los
siguientes:

    * Acceder al nivel de zoom de semana.
    * Se van a redistribuir las horas que hay en la semana 27 del año
      inicialmente 20 asignadas a Raúl y se pasará al siguiente esquema de
      asignación:

       * Semana 27 del año: 5 horas.
       * Semana 28 del año: 30 horas.
       * Semana 29 del año: 25 horas.

    * Comprobar a través de la ventana de carga de recursos si con el cambio
      introducido Raúl no está sobrecargado ahora en ese tramo (semana 27).

Ejercicio 10 .- Realización de asignación con interpolación lineal
==================================================================

Esta tarea consiste en la realización de una asignación utilizando interpolación
lineal con tramos. La interpolación lineal se va a hacer sobre la tarea del
proyecto *Modulo de facturas* y los tramos que se van a utilizar son los
siguientes:

   * Al 50% de longitud del proyecto hay que estar al 80% de completitud de la
     tarea.
   * Al 100% de longitud del proyecto hay que estar al 100% de completitud de la
     tarea.

Comprobar a través de la pantalla de asignación avanzada cuánto se dedica en las
semanas de cada tramo.

Ejercicio 11 .- Crear modelo de plantilla de trabajo y aplicarlo
================================================================

Ir al proyecto *Proyecto 1 - Programa de contabilidad* y crear una plantilla del
grupo de tareas *Módulo Sistema de usuarios*. El nombre de la plantilla será
*Plantilla módulo sistema de usuarios*.

A continuación ir al proyecto *Proyecto 2 - Proyecto de planificación hacia
atrás* y aplicar la plantilla a nivel raíz del proyecto. Renombrar la tarea como
*Sistema de usuarios* y grabar.

Por último ir a la pestaña de histórico y de estadísticas de la plantilla y
comprobar los datos que allí se muestran.


Ejercicio 12.- Creación de tipo de parte de trabajo
===================================================

El alumno tiene que crear un tipo de parte de trabajo con los siguientes datos:

**Campos obligatorios:**

   * Nombre del parte: Tipo 1
   * Código: Autogenerado.
   * Fecha: A nivel de *línea* de parte de trabajo.
   * Recurso: A nivel de *cabecera* de parte de trabajo.
   * Tarea: A nivel de *línea* de parte de trabajo.
   * Administración de horas: Número de horas asignadas.

**Campos opcionales:**

   * Crear un campo de texto a nivel de línea que se denomine *Incidencias* y
     que tenga un tamaño de 20 caracteres.


Ejercicio 13 .- Introducir los siguientes tipos de horas en LibrePlan
=====================================================================

   * **Tipo de hora:**

      * Nombre: Normal TIC
      * Precio por defecto: 15
      * Activado: Sí.

   * **Tipo de hora:**

      * Nombre: Extra TIC
      * Precio por defecto: 20
      * Activado: Sí.

Ejercicio 14 .- Imputación de horas de tipo de parte de trabajo
===============================================================

Este ejercicio consiste en introducir los siguientes partes de trabajo del tipo
*Tipo 1*:

   * Parte 1:

      * *Cabecera*:

         * Recurso: Raúl González Álvarez.

      *  *Líneas de partes de trabajo*:

        ==============  ==================  =============================  =============  ===========================
          Fecha                 Incid.                 Tarea                   Num Horas      Tipo
        ==============  ==================  =============================  =============  ===========================
         1 Marzo            --                Coordinación                        4          Hora normal
         2 Marzo            --                Coordinación                        4          Hora normal
         3 Marzo            --                Coordinación                        4          Hora normal
         3 Marzo            --                Coordinación                        2          Hora extra
         4 Marzo            --                Coordinación                        4          Hora normal
         7 Marzo            --                Coordinación                        4          Hora normal
         7 Marzo            --                Coordinación                        3          Hora extra
        ==============  ==================  =============================  =============  ===========================

   * Parte 2:

       * Cabecera:

          * Recurso: Pablo Requejo Tilve.

       * Líneas de partes de trabajo:

        ==============  ==================  =============================  =============  ===========================
          Fecha                 Incid.                 Tarea                   Num Horas      Tipo
        ==============  ==================  =============================  =============  ===========================
         1 Marzo            --                Análisis                        8            Hora normal
         2 Marzo            --                Análisis                        8            Hora normal
         3 Marzo            --                Análisis                        8            Hora normal
         4 Marzo            --                Análisis                        8            Hora normal
         5 Marzo            --                Análisis                        4            Hora extra
         7 Marzo            --                Análisis                        8            Hora normal
        ==============  ==================  =============================  =============  ===========================



Una vez introducidos los partes de trabajo, las preguntas son:

  * Visualizar en la pantalla de planificación de proyectos cuanto es el
    porcentaje de horas que se imputaron a las dos tareas a las cuáles se
    imputaron partes de trabajo:

     * Porcentaje de horas imputadas en la tarea *Coordinación*: _____________________
     * Porcentaje de horas imputadas en la tarea *Análisis*: _____________________

  * Visualizar en la pantalla de los elementos de pedido cuanto son el total de
    horas asignadas a los elementos de pedido:

     * Total de horas imputadas en la tarea *Coordinación*: _____________________
     * Total de horas imputadas en la tarea *Análisis*: _____________________


Ejercicio 15 .- Crear las siguientes categorías de coste
========================================================

   * **Nombre de la categoría:** Programadores

        * Asignación 1:

         * *Tipo de hora:* Hora normal
         * *Precio por hora:* 15
         * *Fecha de inicio:* 01/03/2011
         * *Fecha de fin:* 31/05/2011

        * Asignación 2:

         * *Tipo de hora:* Hora normal
         * *Precio por hora:* 16
         * *Fecha de inicio:* 01/06/2011
         * *Fecha de fin:* - blanco -

        * Asignación 3:

         * *Tipo de hora:* Hora extra
         * *Precio por hora:* 17
         * *Fecha de inicio:* Fecha actual
         * *Fecha de fin:* 31/12/2011

        * Asignación 4:

         * *Tipo de hora:* Hora extra
         * *Precio por hora:* 18
         * *Fecha de inicio:* 01/01/2012
         * *Fecha de fin:* - blanco -

   * **Nombre de la categoría:** Gestor de proyecto y analista

        * Asignación 1:

         * *Tipo de hora:* Hora normal
         * *Precio por hora:* 20
         * *Fecha de inicio:* 01/03/2011
         * *Fecha de fin:* 31/05/2011

        * Asignación 2:

         * *Tipo de hora:* Hora normal
         * *Precio por hora:* 22
         * *Fecha de inicio:*  01/06/2012
         * *Fecha de fin:* - en blanco -

        * Asignación 3:

         * *Tipo de hora:* Hora extra
         * *Precio por hora:* 30
         * *Fecha de inicio:* Fecha actual
         * *Fecha de fin:* 31/12/2011

        * Asignación 4:

         * *Tipo de hora:* Hora extra
         * *Precio por hora:* 35
         * *Fecha de inicio:*  01/01/2012
         * *Fecha de fin:* - blanco -

Ejercicio 16 .- Asignar los trabajadores las categorías de coste
================================================================

Asignar los trabajadores siguientes las categorías de coste que se indican.

         * Raúl González Álvarez - Gestor de proyecto y analistas - Desde
           01/03/2011
         * Vicente Martínez Pino - Programadores - Desde 01/03/2011
         * Ana Pérez López - Programadores - Desde 01/03/2011
         * Pablo Requejo Tilve - Gestor de proyecto y analistas - Desde
           01/03/2011
         * Felipe Romero Gómez - Programadores - Desde 01/03/2011


Ejercicio 17 .- Método del valor ganado
=======================================

Acceder a la vista de Gantt y seleccionar la pestaña de "Valor Ganado".

Datos para interpretar indicadores básicos:

   * BCWS: Coste presupuestado del trabajo planificado. Se calcula a partir de
     las horas planificadas hasta una fecha.
   * ACWP: Coste real del trabajo realizado. Se calcula a partir de las horas
     dedicadas hasta una fecha.
   * BCWP: Coste presupuestado del trabajo realizado. Se calcula a partir de
     multiplicar el progreso de las tareas por la cantidad estimada de las
     tareas.

Datos para interpretar indicadores derivados:

    * CV: desviación en coste CV = BCWP - ACWP
    * SV: desviación en planificación SV = BCWP - BCWS
    * BAC: total coste planificado BAC = max (BCWS)
    * EAC: estimación del coste total actual EAC = (ACWP/ BCWP) * BAC
    * VAC: desviación al coste final VAC= BAC - EAC
    * ETC: estimación del coste pendiente = EAC - ACWP
    * CPI: eficiencia en coste CPI = BCWP / ACWP
    * SPI: eficiencia en programación SPI= BCWP / BCWS


Ejercicio 18 .- Visualizar el coste de las tareas del pedido
============================================================

Visualizar el coste de las tareas del proyecto *Proyecto 1 - Programa de
contabilidad* a través del informe **Costes por recurso**.

¿Cuánto es el coste que se lleva gastado en la tarea de Coordinación? _____________________

¿Y en la tarea de Análisis? _____________________


Ejercicio 19 .- Administración de materiales
============================================

Crear las siguientes categorías de materiales con los materiales que se indican
en cada una de ellas. Utilizar siempre autogeneración de código:

   1.- (Categoría) Equipos informáticos
      1.1.- (Categoría) Impresoras
           - (Material)  Descripción: Impresora Brother, Precio: 200, Unidades:
             unidades.
   2.- (Categoría) Material fungible
      1.2.- (Categoría) Material fungible (en el autogenerado)
           - (Material)  Descripción: Paquete folios de 500, Precio: 6,
             Unidades: unidades.


Ejercicio 18 .- Asignación de materiales en tareas
==================================================

Para la realización de la tarea *Pruebas* del *Proyecto 1 - Programa de
contabilidad* se quiere que termine con un informe impreso de todos las
pruebas que se hicieron y el resultado de las mismas. Por tanto, para llevar a
cabo la tarea se requieren dos materiales, que son una impresora nueva y un
paquete de folios.

En concreto la asignación tendrá los siguientes valores:

   * Impresora Brother.
        * Unidades: 1
        * Fecha de recepción estimada: 1 de Junio.
   * Paquete de folios:
        * Unidades: 2
        * Fecha de recepción estimada: 1 de Junio.

   * Calcular el informe de necesidades de materiales para el proyecto.

Ejercicio 19 .- Creación de formulario de calidad
=================================================

Crear un nuevo formulario de calidad:

   * *Nombre*: Formulario 1
   * *Tipo de Formulario*: Porcentaje
   * *Notificar Progreso*: Marcado

Introducir los siguientes elementos del formulario de calidad:

   * Control de calidad 1 (set pruebas 1) -  25%
   * Control de calidad 2 (set pruebas 2) -  50%
   * Control de calidad 3 (set pruebas 3) -  75%
   * Control de calidad 4 (set pruebas 4) - 100%


Ejercicio 20 .- Asignación de formulario de calidad
===================================================

Asignar en el proyecto *Proyecto 1 - Programa de contabilidad*  el formulario de
calidad *Formulario 1* en la tarea *Pruebas*.

Marcar el *control de calidad 1 (set pruebas 1)* cómo superado con fecha del *6
de Julio de 2011*.

Grabar el pedido.


Ejercicio 21 .- Creación de formulario de calidad como avance
=============================================================

Ir a la tarea *Pruebas* del proyecto *Proyecto 1 - Programa de contabilidad* y
editar dicha tarea. En el pop-up emergente ir a la pestaña *Formularios de
calidad de tarea* y marcar que el formulario de calidad asignado, *Formulario 1*
como que notifica progreso.

Ir ahora a la pestaña de *Progresos* y configurar el progreso asociado al
formulario de calidad como *que propaga*

Por último, ir a la perspectiva de planificación del proyecto y visualizar los
progresos de las tareas. Identificar gráficamente este progreso.


Ejercicio 22 .- Crear usuarios y configurar sus permisos
========================================================

Crear un usuario con los siguientes datos:

   * Nombre de usuario: proyecto1_lectura
   * Contraseña: proyecto1_lectura
   * Roles de usuario: Ninguno.
   * Perfiles de usuario: Ninguno.

Acceder al proyecto con nombre *Proyecto 1 - Programa de contabilidad* y dar
permiso de lectura al usuario *proyecto1_lectura*.

Salir de la aplicación con el usuario con el que se está conectado, *admin*, y
entrar con el usuario *proyecto 1_lectura*. Comprobar que sólo se ve el proyecto
*Proyecto 1 - Programa de contabilidad* y que no se puede modificar.

Configurar ahora el usuario *proyecto1_lectura* con permiso de escritura sobre
el proyecto *Proyecto 1 - Programa de contabilidad* y comprobar que, en esta
situación, si que se puede modificar y grabar el proyecto con el usuario
*proyecto1_lectura*

Ejercicio 23 .- Otros informes
==============================

Visualizar el informe *Trabajo y progreso por tarea* para el proyecto *Proyecto
1 - Programa de contabilidad*

Datos para interpretar el  informe:

   * Diferencia en planificación: (Avance Medido * Horas planificadas total) -
     Horas planificadas
   * Diferencia en coste: (Avance Medido * Horas planificadas total) - Horas
     imputadas
   * Ratio desfase en coste: Avance Medido / Avance imputado
   * Ratio desfase en planificación: Avance Medido / Avance planificado

Ejercicio 24 .- Recursos limitantes
===================================

1.- Crear un recurso de carácter limitante de tipo trabajador:
   * Nombre: Auditor
   * Apellidos: Interno
   * NIF: 66666666H
   * Recursos limitantes: Recurso Limitante.
   * Calendario: España.

2.- Acceder al proyecto *Proyecto 1 - Programa de contabilidad* y crear las
siguientes tareas:

**Tarea 1**
   * Nombre: Auditoría análisis
   * Horas: 30
   * Dependencia: Análisis FIN-START Auditoria análisis.
   * Asignación limitante de recurso *Auditor Interno*

**Tarea 2**
   * Nombre: Auditoría sistema de usuarios
   * Horas: 20
   * Dependencia: Sistema de usuario FIN-START Auditoría sistema de usuarios
   * Asignación limitantes de recurso *Auditor Interno*

**Tarea 3**
   * Nombre: Auditoría facturas
   * Horas: 20
   * Dependencia: Módulo de facturas FIN-START Auditoría facturas
   * Asignación limitantes de recurso *Auditor Interno*


3.- Acceder a la vista de gestión de recursos limitantes y realizar una
asignación automática de las 3 tareas de recursos limitantes: Auditoría
análisis, auditoría sistema de usuarios, auditoría facturas. Grabar.

4.- Ir al proyecto *Proyecto 2 - Proyecto de planificación hacia atrás* y crear
una tarea con las siguientes características:

   * Nombre: *Tarea auditar*
   * Horas: 30 horas.
   * Ponerle una restricción: COMENZAR_NO_ANTES_QUE 8 de Abril.
   * Asignación limitantes de recurso *Auditor Interno*

5.- Ir a la pantalla de gestión de recursos limitantes y realizar una asignación
limitantes de la tarea *Tarea auditar*. ¿Cómo se planifica en las colas?
Grabar.
