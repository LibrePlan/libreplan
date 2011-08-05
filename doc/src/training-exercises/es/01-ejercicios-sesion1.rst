--------
Sesión 1
--------

.. contents:: Contenidos


Ejercicio  1. Criterios
=======================

Crear 2 tipos de criterios con los criterios  detallados:

**Tipo de criterio 1**

      * *Nombre del tipo de criterio*: Formación.
      * *Permite valores simultáneos*: Sí.
      * *Jerárquico*: No.
      * *Criterios*:

         * Gestor de proyecto.
         * Analista.
         * Programador.

**Tipo de criterio 2**

       * *Nombre del tipo de criterio*: Lugar.
       * *Permite valores simultáneos*: No.
       * *Jerárquico*: No.
       * *Criterios*:

          * Galicia
          * Comunidad Valenciana


Ejercicio 2. Calendarios
========================

2.1 - Cambiar el nombre del calendario por defecto con nombre *Default* y
llamarle **España**.

2.2 - Crear dos calendarios globales en la aplicación con los siguientes datos:

**Calendario 1**:

   * Nombre: Galicia.
   * Deriva de España.
   * Días excepcionales:

         * 22 de Abril - Tipo BankHoliday - Horas laborales 0.
         * 17 de Mayo - Tipo BankHoliday - Horas laborales 0.
         * 30 de Diciembre - Tipo Workable BankHoliday - Horas laborales 4.

   * Jornada semanal:

         * Lunes - 8 h.
         * Martes - 8 h.
         * Miércoles - 8 h.
         * Jueves - 8 h.
         * Viernes - 8 h.

**Calendario 2**


   * Nombre: Comunidad Valenciana.
   * Deriva de España.
   * Días excepcionales:

         * 12 de Abril - Tipo BankHoliday - Horas laborales 0.
         * 10 de Octubre - Tipo BankHoliday - Horas laborales 0.
         * 30 de Diciembre - Tipo Workable BankHoliday - Horas laborales 4.


   * Jornada semanal:

         * Lunes - 8 h
         * Martes - 8 h.
         * Miércoles - 8 h.
         * Jueves - 8 h.
         * Viernes - 8 h.



Ejercicio 3. Etiquetas
======================

Crear los  tipos de etiqueta y etiquetas siguientes:



      * Tipo de etiqueta: Prioridad.
      * Valores:

         * Muy urgente.
         * Urgente.
         * Promedio.
         * Baja.

Ejercicio 4. Recursos
=====================

Crear los siguientes recursos con los datos siguientes:

**Recurso 1**
      * Nombre: Raúl González Álvarez.
      * NIF: 11111111A
      * Calendario derivado: Galicia. Jornada laboral:

         *  Lunes: 8h
         *  Martes: 8h
         *  Miércoles: 8h
         *  Jueves: 8h
         *  Viernes: 4h

      * Nombre: Ana Pérez López. - NIF: 22222222B - Calendario derivado:
        Comunidad Valenciana
      * Nombre: Felipe Romero Gómez - NIF: 33333333C - Calendario derivado:
        Comunidad Valenciana
      * Nombre: Vicente Martínez Pino - NIF: 44444444C - Calendario derivado:
        Galicia
      * Nombre: Pablo Requejo Tilve - NIF: 5555555G - Calendario derivado:
        España

Ejercicio 5. Períodos Actividad
===============================

Configurar los siguientes períodos de actividad  para los trabajadores.


      * Nombre: Raúl González Álvarez.

         * Fecha de contratación: 01/03/2011 - Indefinido.

      * Nombre: Ana Pérez López.

         * Fecha de contratación:  01/03/2011 - 30/06/2011.

      * Nombre: Felipe Romero Gómez

        * Fecha de contratación: 01/03/2011 - Indefinido.

      * Nombre: Vicente Martínez Pino

        * Fecha de contratación: 01/03/2011 - Indefinido.

      * Nombre: Pablo Requejo Tilve

        * Fecha de contratación: 01/03/2011 - Indefinido.

Ejercicio 6 - Configurar Excepciones de Calendario
==================================================

Configurar las vacaciones como exception  days de intervalo y de tipo HOLIDAY.

      * Nombre: Raúl González Álvarez.

         * Vacaciones: 01/06/2011 - 12/06/2011.

      * Nombre: Felipe Romero Gómez

         * Vacaciones: 01/04/2011 - 10/04/2011

Ejercicio 7 -  Criterios en recursos
====================================

Configurar la satisfacción de criterios por parte de los recursos.

      * **Nombre**: Raúl González Álvarez.
      * **Satisfacciones de criterio**:

         * Formación: Gestor de proyecto - Desde 01/03/2011 hasta infinito.

      * **Nombre**: Ana Pérez López
      * **Satisfacciones de criterio**:

         * Formación: Programador - Desde 01/03/2011 hasta infinito.
         * Localización: Comunidad Valenciana - Desde 01/03/2011 hasta infinito.

      * **Nombre**: Felipe Romero Gómez
      * **Satisfacciones de criterio**:

         * Formación: Programador - Desde 01/03/2011 hasta infinito.
         * Localización: Comunidad Valenciana - Desde 01/03/2011 hasta infinito.

      * **Nombre**: Vicente Martínez Pino
      * **Satisfacciones de criterio**:

         * Formación: Programador - Desde 01/03/2011 hasta infinito.
         * Localización Galicia - Desde 01/03/2011 hasta infinito.

      * **Nombre**: Pablo Requejo Tilve

         * Formación: Analista - Desde 01/03/2011 hasta infinito.


Ejercicio  8. Creación de un pedido
===================================

Crear un pedido con los siguientes datos y ponerle, los puntos de planificación
y los criterios indicados:

   * Datos de pedido:

      * Nombre: Proyecto 1 - Programa de contabilidad.
      * Fecha inicio: 01/03/2011
      * Fecha limite: 01/08/2011.
      * Cliente: -
      * Calendario: España.

   * Elementos de pedido:

      * 1  Coordinación - 350h - *Punto de planificación* - Gestor de proyecto

      * 2  Análisis  - 160h - *Punto de planificación* - Analista

      * 3 Módulo sistema de usuarios - Programador & Comunidad Valenciana

         * 3.1 CU1- Alta de usuarios - 80h - *Punto de planificación*
         * 3.2 CU2- Registro de usuarios - 80h - *Punto de planificación*
         * 3.3 CU3- Gestión de roles de usuario - 160h - *Punto de planificación*

      * 4 Módulo Facturas - *Punto de planificación* - Programador & Galicia

         * 4.1 CU4 - Alta de facturas - 100h
         * 4.2 CU5 - Listado de facturas - 40h
         * 4.3 CU6 - Búsqueda de facturas - 80h

      * 5 Módulo Pruebas - 160h - *Punto de planificación*

   * Otros datos del pedido:
      * Responsable: Nombre de la persona.
      * Presupuesto: Trabajo: 100.000  Materiales: 0
      * Estado: Ofertado.

Ejercicio  9 - Planificando dependencias
========================================

Poner las dependencias siguientes en la planificación del pedido:

Añadir las siguientes dependencias:

         * Análisis *FIN-INICIO* Módulo del sistema de usuarios
         * CU1 - Alta de usuario *INICIO-INICIO* CU2 - Registro de usuario
         * CU2 - Registro de usuario  *FIN-INICIO* CU3 - Gestión de roles
         * Módulo sistema de usuarios *FIN-INICIO* Módulo de facturas
         * Módulo de facturas *FIN-INICIO* Pruebas


Ejercicio 10. Asignación de recursos
====================================

Realizar las siguientes asignaciones


      * **Tarea: Coordinación**:

         * *Asignación específica*: Raúl González Álvarez
         * *Estrategia*: Calcular días laborables
         * *Numero de recursos*: 0,5

      * **Tarea: Análisis**:

         * *Asignación recomendada* Criterio Análisis
         * *Estrategia*: Calcular recursos por día
         * *Número de días laborales*: 20
         * *Horas a asignar*: 160h

      * **Tarea: CU1 - Alta de usuarios**:

         * *Asignación recomendada* Criterios Programador + Comunidad Valenciana
         * *Estrategia*: Calcular recursos por día
         * *Número de dias laborales*: 10
         * *Horas asignar*: 80h.

      * **Tarea: CU2 - Registro de usuario**:

         * *Asignación recomendada* Criterios Programador + Comunidad Valenciana
         * *Estrategia*: Calcular recursos por día
         * *Número de dias laborales*: 10
         * *Horas asignar*: 80h.

      * **Tarea: CU3 - Gestión de roles de usuario** Se asignan 2 recursos
        específicamente

         * *Asignación específica*: Felipe Romero Gómez y Ana Pérez López.
         * *Estrategia*: Calcular número de horas
         * *Recursos por día*: 0,25 para Felipe y 0,75 para Ana
         * *Número de días laborales:* 21

      * **Tarea: Módulo de facturas**

         * *Asignación recomendada*: Programador + Galicia
         * *Estrategia*: Calcular días laborales.
         * *Número de horas:* 220
         * *Número de recursos por día:* 1

      * **Tarea: Pruebas**

         * *Asignación:* una asignación genérica de 1 Gestor de proyecto, una
           asignación genérica de analista y una asignación específica de Ana
           Pérez López.
         * *Estrategia*: Calcular recursos por día.
         * *Numero de horas*: 80 horas de perfil gestor de proyecto, 80 horas de
           perfil analista y 40 horas para Ana.
         * *Días laborables:* 21


Ejercicio 11. Preguntas sobre asignación
========================================

**1.- ¿Por qué en la asignación específica de la tarea de coordinación se
asignan a Raúl González 2h el viernes y de lunes a jueves 4h?**

  Respuesta: La causa es que se asigna 0,5 recursos por día y el recurso tiene
  una jornada laboral en la cual los viernes se hace 4h (media jornada).

**2.- ¿Cuántas son las horas asignadas a Raúl González del 1 al 12 de Junio en
la tarea de coordinación?¿Por qué ocurre esto?**

  Respuesta: Se asignan 0 horas porque Raúl en ese período está de vacaciones.

**3.- ¿Cuántos son los recursos por día que se aplican en la tarea de análisis
fruto de la configuración realizada?**

  Respuesta: Se asigna 1 recurso por día.

**4.- ¿Cuantos recursos por día suman las tareas CU1- Alta de usuarios y CU2-
Registro de usuario que son planificadas simultáneamente? ¿Por que causan
sobreasignación si hay 2 programadores valencianos?**

  Respuesta: Suman 2 recursos por día y hay dos programadores en la Comunidad
  Valenciana. La razón de que causen sobreasignación es que Felipe Romero Gómez,
  uno de los programadores valencianos se encuentra de vacaciones del 1 al 10 de
  Abril. Por ello solo se puede asignar 4 días de los que dura la tarea (32h
  trabajables). Para cumplir con los 10 días por tanto hay que sobreasignar al
  otro programador valenciano, a Ana.

**5.- ¿Por qué en la tarea CU3- Gestión de roles de usuario para un número de
horas de 160h y un numero de recursos asignados de 1 (0,25+0,75) es necesario
que se configuraran 21 días laborales?**

  Respuesta: La razón es que la tarea dura del 12/04/2011 al 11/05/2011 y este
  primer día el 12 es festivo para ambas según el calendario de la Comunidad
  Valenciana del cual derivan.

**6.- ¿En la tarea de pruebas, por qué prácticamente da los mismos recursos por
día de análisis y de gestión de proyecto dedicadas a pruebas que a Ana cuando
Ana dedica la mitad de horas?**

  Respuesta: La razón de esto es que Ana solo puede trabajar hasta el 30 de
  Junio, porque después se le agota su contrato de trabajo.

Ejercicio 12. Progresos
=======================

Realizar las siguientes asignaciones de progreso


      * Tarea coordinación - Avance de tipo porcentaje - Valor  máximo 100 -
        Propaga

         * Valores: 25% a 1 Abril de 2011.

      * Tarea análisis - Avance de tipo unidades - Valor máximo 6 - Propaga

         * Valor: 1 unidad a 4 de Marzo de 2011
         * Valor: 2 unidades a 11 de Marzo de 2011
         * Valor: 3 unidades a 25 de Marzo de 2011

