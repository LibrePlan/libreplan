------------
Introducción
------------

El objetivo de este curso formativo es capacitar a los futuros usuarios de
NavalPlan** para que obtengan el máximo rendimiento de las características del
software NavalPlan desarrollado por la Fundación para el Fomento de la Calidad
Industrial y Desarrollo Tecnológico de Galicia para la gestión de la producción
en el sector naval.

Los asistentes al curso al final de la primera sesión serán capaces de:

   * **Gestionar los recursos** humanos y máquina y sus capacidades.
   * **Gestionar los proyectos** de los proyectos a planificar y organizar el
     trabajo.
   * **Planificar los proyectos** en el tiempo incorporando dependencias y restricciones temporales.
   * **Asignar recursos** a la realización de tareas para cumplir plazos y controlar la ejecución de los proyectos.
   * **Controlar el progreso** de los proyectos y la carga de los recursos.
   * **Asignar los proyectos** empleando calendarios, criterios y etiquetas.

Para esto:

   * Trabajarán los conceptos necesarios para la realización de planificaciones con NavalPlan.
   * Conocerán las distintas pantallas de NavalPlan y aplicarán sus funcionalidades en casos prácticos.

Estarán en disposición de utilizar NavalPlan para **realizar planificaciones reales en sus empresas**.

---------------------------------------------
Módulo de administración de entidades básicas
---------------------------------------------


Administración de criterios
===========================

Los criterios son las entidades que se utilizan en la aplicación para
categorizar los recursos y las tareas. Las tareas requieren criterios y los
recursos satisfacen criterios. Un ejemplo de utilización de criterios es la
siguiente secuencia: un recurso es asignado con el criterio "soldador" (es
decir, satisface el criterio "soldador") y una tarea requiere el criterio
"soldador" para ser realizada, en consecuencia ante una asignación de recursos a
tareas los trabajadores con criterio "soldador" son los que se utilicen a la
hora de asignar xenericamente (no aplica en las asignaciones específicas).

Los criterios se organizan en tipos de criterios, las cales reúnen los criterios de una misma categoría.

Alta de un nuevo tipo de Criterio
---------------------------------

Para crear un nuevo tipo de criterio debiera seguir la siguiente secuencia de pasos:

* Ir al Listado de Criterios, opción Administración / Gestión / Gestión > Tipos de Datos > Criterios
* Presionar el botón *Crear*
* Cubrir el campo del Nombre del tipo de Criterio, por ejemplo (Profesión, Permiso, Localización)
* Indicar el tipo de recurso a lo que es aplicable:

   * TRABAJADOR: indica que es aplicable a los recursos humanos.
   * MÁQUINA: indica que es aplicable a los recursos máquina.

* Seleccionar si un tipo de criterio permite que un recurso cumpla más de un criterio de este tipo. Se está activado para un tipo de criterio llamado profesión había permitido que cada recurso pida tener simultáneamente más de una profesión activa en el tiempo, por ejemplo Carpintero y Pintor.
* Seleccionara si el tipo de criterio es jerárquico. Esto permitirá definir una estructura jerárquica que puede ser útil para reflexar una relación entre los criterios de especifidade. Un ejemplo sería la localización donde se pueden definir criterios con relación de pertenencia Europa > España > Galicia > Santiago.
* Dejar marcado el campo de activado, ya que su desactivación supondría que no se podrían asignar este tipo de criterio a los recursos.
* Incorporar el campo descripción informativo sobre el tipo de criterio
* Presionar en la opción en la parte inferior de la venta de *guardar y continuar*. Procediendo la creación de criterios de este tipo de criterio.


Alta de criterios de un determinado tipo

Para crear criterios de un tipo determinado, hay que proceder a editar el tipo de criterio del que se quieren añadir criterios. Esto se realiza desde la lista de criterios pulsando en el botón *editar*. Tras ello, los pasos a dar son los siguientes:

* Escribir el nombre del criterio en la caja *Criterio nuevo* y pulsar en el botón *Añadir*. El nuevo criterio aparecerá en la tabla de criterios.
* Se puede desmarcar el botón de activo si se estima dejar de utilizar en la aplicación un criterio.
* Si el tipo de criterio es jerárquico se puede indentar con las flechas izquierda y derecha para incorporar criterios como hijos o bien como generalizaciones de criterios más específicos.
* Se puede crear un criterio hijo de otro. Sólo es preciso seleccionar con el ratón el criterio a incorporar, y luego escribir el nombre del nuevo criterio y pulsar en el botón *Añadir*.
* Al eliminar un criterio si la opción de jerarquía está activa el sistema avisará de que se perderán las relaciones padre-hijo mediante un proceso de aplanado.
* Una vez que se finalice con la creación de criterios, hay que presionar en la opción de *Guardar* en la parte inferior de la página. Es de reseñar que los cambios, por término general en la aplicación, solamente se consolidan (guardan) cuando se presione en *Guardar*. Si se pulsa sobre el botón *Cerrar* se pierde el trabajo desde la última grabación.
* Una vez guardado, se pasa al listado de tipos de criterios donde aparecerá el nuevo tipo de criterio.

Otras operaciones sobre criterios
---------------------------------

También es posible realizar las siguientes operaciones sobre criterios:

* Desactivación/Activación de tipos de criterios
* Desactivación/Activación de criterios
* Modificación de los datos de un tipo de criterio
* Reorganización de la estructura de criterios

Sobre estas operaciones se puede encontrar más documentación en la sección de la ayuda de *NavalPlan*

Administración de etiquetas
===========================

Las etiquetas son entidades que se utilizan en la aplicación para la organización conceptualmente de tareas o elementos de proyecto.

Las etiquetas categorizanse según los tipos de etiquetas. Una etiqueta sólo pertenece a un tipo de etiqueta, sin embargo, nada impide crear tantas etiquetas similares que pertenezcan a tipos de etiquetas diferentes.


Alta de un nuevo tipo de etiqueta
---------------------------------

Para crear un nuevo tipo de etiqueta hay que seguir los pasos siguientes:

* Ir al Listado de Etiquetas, opción *Administración / Gestión > Tipos de datos > Etiquetas*.
* Presionar el botón *Crear*
* Cubrir el campo *Nombre* del tipo de etiqueta. Posibles valores son, por ejemplo, Centro de Coste, Zona de Buque, Dificultad, etc...
* Se puede presionar en el botón *Guardar y Continuar*  para almacenar el nuevo tipo creado y, después, proceder a asociar etiquetas a un tipo de etiquetas.

Alta de una nueva etiqueta de un tipo
-------------------------------------

En el momento de creación de un tipo de etiqueta o bien presionando en la operación de edición en el listado de tipos de etiqueta, se pueden crear nuevas etiquetas para ese tipo. Los pasos son:

* En la sección de *Lista de etiquetas* introducir el nombre de la nueva etiqueta en el campo de texto de *Nueva Etiqueta*.
* Presionar el botón de *Nueva etiqueta* y, tras ello, aparecerá en la tabla de etiquetas asociada al tipo que se está editando.
* Para consolidar las modificaciones y las noticias altas, simplemente hay que presionar en el botón de *Guardar* que vuelta al listado de tipos de etiquetas.

Administración de calendarios
=============================

Los calendarios son las entidades de la aplicación que determinan las capacidad de carga de los distintos recursos. Un calendario está formado por una serie de días anuales, donde cada día dispone de horas disponibles para trabajar. Los calendarios indican cuantas horas puede trabajar un recurso a lo largo del tiempo.

Por ejemplo, un festivo puede tener 0 horas disponibles y, si las horas de trabajo dentro de un día laboral son 8, es este número que se asigna como tiempo disponible para ese día.

Existen dos modos de indicarle al sistema cuantas horas de trabajo tiene un día:

    * Por día de la semana. Por ejemplo, los lunes se trabajan 8 horas generalmente.
    * Por excepciones. Por ejemplo, el lunes 30 de Enero se trabajan 10 horas.

El sistema de calendarios permite que unos calendarios deriven de otros, de forma se pueden tener calendarios de distintas localizaciones de la empresa siguiendo una organización como la siguiente *España > Galicia > Ferrol* y *España > Galicia > Vigo*. Es esta situación de ejemplo, la modificación de festivos a nivel estatal modifica automáticamente los festivos a nivel de los calendarios de *Galicia*, *Ferrol* y *Vigo*.

Para acceder la gestión de los calendarios de la empresa es preciso situarse en la sección de *Administración / Gestión > Calendarios*.


Creación de un nuevo calendario
-------------------------------

Para la creación de un nuevo calendario es necesario:

   * Presionar en el botón  *Crear* en la sección de *Calendarios*.
   * Introducir el nombre del calendario para poder identificarlo.
   * El calendario creado será un calendario sin ningún dato. Se verán todas las fechas del calendario en rojo por lo que esos días no tienen asignación de horas. Es preciso introducir la información relativa a la *semana Laboral* y las *excepciones*.
   * Presionar en la pestaña de *Semana de Trabajo*. Asignar la jornada de trabajo por defecto para cada día de la semana. Por ejemplo, es posible marcar 8 horas de trabajo de lunes a viernes para una jornada laboral de 40 horas. En la parte derecha de la pantalla se podrá ver las horas determinadas para trabajar en una jornada concreta. A lo largo del tiempo se puede ir modificando la semanal laboral por defecto de un calendario. Esto se realiza a través de la creación de nuevas versiones del calendario.
   * Situarse en la pestaña de *Excepción* e introducir aquellos días especiales que tienen una influencia en el calendario laboral de la empresa o en el calendario del grupo de trabajadores que se esté creando. Por ejemplo, se deben señalar los días festivos.
   * Seleccionar una fecha en el calendario, por ejemplo el 19 de Marzo. Señalar el tipo de excepción como *BANK_HOLIDAY (Día de Vacaciones)*. Finalmente indicar el número de horas a trabajar que, en este caso, será 0. A continuacion, pulsar en el botón *Crear Excepción*.
   * El listado de excepciones se puede ver a la derecha del formulario de creación de excepciones.
   * **La aplicación sólo permite modificaciones del calendario a futuro** para que no se tenga influencia en planificaciones pasadas.
   * Es posible marcar un conjunto de fechas como excepciones, simplemente se tiene que marcar la fecha de inicio en el calendario y seleccionar en el campo data fin la fecha hasta la que llegua la excepción.
   * Para borrar una excepción en el calendario se presionará en el icono de *Borrar* en el listado de excepciones.
   * Finalmente pulsar en *Guardar* y el nuevo calendario aparecerá en el listado de calendarios.

Edición de un calendario
------------------------

Es posible modificar un calendario para incluir modificaciones en la jornada laboral semanal o para modificar los días excepcionales. Para eso se deben seguir los siguientes pasos:

   * Pulsar en el botón *Editar* en las operaciones de un calendario del listado de la administración de calendarios.
   * Es posible modificar o crear nuevos días excepcionales a futuro siguiendo las instrucciones previas de creación de un nuevo calendario.
   * Para modificar la semana laboral por defecto y es necesario situarse en la pestañá de *Semana de Trabajo* y hacer:

       * Pulsar en el botón *crear una nueva semana de trabajo*.
       * Indicar la fecha a partir de la que la semana entrará en vigor.
       * Pulsar en el botón de *Crear*.
       * Editar el valor de las horas de los días laborales por cada día de la semana.
       * Una vez que se pulse en la opción *Guardar* del calendario, los cambios de esta nueva versión se consolidarán. A partir de la fecha de aplicación de la nueva versión el calendario se comportará de la manera especificada.

    * Para que las modificaciones tengan efecto es necesario presionar en el botón *Guardar* del calendario. Si se pulsa en el botón *Cancelar* los cambios consolidados no se almacenarán.

Copiar un calendario
--------------------

Existe la opción de realizar copias de un calendario. La realización de una copia supone la creación de un nuevo calendario con una copia de todos los datos del calendario original. Este calendario se podrá editar como cualquiera otro calendario existente. Únicamente es necesario cambiarle el nombre para que no coincida con ninguno de los existentes. La copia de un calendario no mantiene ninguna relación con el calendario de origen.

Para hacer una copia se seguirán los siguientes pasos:

* Presionar en el botón *Crear Copia* en las operaciones del calendario que se quiere copiar en el listado de administración.
* Modificar el nombre del calendario.
* Modificar los datos de nuestro interés si es necesario.
* Presionar en el botón *Guardar*.


Creación de un calendario derivado
----------------------------------

Se pueden crear calendarios derivados de otros. Un calendario derivado es una especialización del calendario del que deriva. La aplicación típica de calendario derviado son situaciones en las que la empresa o entidad tiene varias localizaciones con múltiples calendarios laborales. Otro ejemplo de utilización es para definir el calendario de trabajadores con media jornada pero que tienen los mismos festivos que el resto de la empresa. La derivación es como crear una copia pero con la salvedad de que los cambios en el calendario origen siguen afectando a los calendarios derivados.

Los pasos para la creación de un calendario derivado son los siguientes:

   * Pulsar en el botón de crear derivado en las operaciones de un calendario existente en el listado de la administración de calendarios.
   * Se puede comprobar que se indica que este calendario es derivado del originario en la información del calendario y como se hereda toda la información del calendario preexistente.
   * Está permitido realizar todas las modificaciones que se deseen sobre este calendario de la forma normal, con las siguientes salvedades:

      * Para modificar la jornada laboral es necesario desmarcar el campo *Por defecto*. Este campo indica que las horas laborales por día son las mismas que en el calendario del que se deriva.
      * También se puede modificar el calendario del que se deriva en las ediciones del calendario, entrando en vigor a partir de la fecha de modificación.

   * Para que las modificaciones tengan efecto es necesario presionar en el botón *Guardar* del calendario. Por el contrario, si se pulsa en el botón *Cancelar*, los cambios consolidados no se almacenarán.
   * A partir de este momento, se puede ver el nuevo calendario derivado y como aparece en una estructura jerárquica por debajo del calendario de origen.

Configuración del calendario por defecto de la empresa
------------------------------------------------------

Para facilitar el empleo y configuración de los calendarios en la aplicación, es posible configurar el calendario por defecto de la empresa. Este calendario será lo que aparezca seleccionado inicialmente cuando se cree un recurso o se asocie un calendario la una tarea.

Para su selección hay que seguir los siguientes pasos:

   * Entrar en la sección de **Administración / Gestión > NavalPlan: Configuración** del menú principal.
   * Seleccionar en el campo *Calendario por defecto*, el calendario deseado.
   * Presionar en el botón *Guardar*


------------------
Módulo de recursos
------------------

Conceptos teóricos
==================

Los recursos son las entidades que realizan los trabajos necesarios para completar los proyectos. Los proyectos en la planificación se representan mediante diagramas de Gantt que disponen en el tiempo las actividades.

En *NavalPlan* existen tres tipos de recursos capaces de realizar trabajo. Estos tres tipos son:

   * *Trabajadores*. Los trabajadores son los recursos humanos de la empresa.
   * *Máquinas*. Las máquinas son capaces también de desarrollar tareas y tienen existencia en *NavalPlan*.
   * *Recursos virtuales*. Los recursos virtuales son como grupos de trabajadores que no tienen existencia real en la empresa, es decir, no se corresponden con trabajadores reales, con nombre y apellidos, de la empresa.

Utilidad de los recursos virtuales
----------------------------------

Los recursos virtuales son, como se explicó, como grupos de trabajadores pero que no se corresponden con trabajadores concretos con nombre y apellidos.

Se dotó a *NavalPlan* la posibilidad de usar recursos virtuales debido a dos escenarios de uso:

   * Usar recursos virtuales para simular contrataciones futuras por necesidades de proyectos. Puede ocurrir que para satisfacer proyectos futuros las empresas necesiten contratar trabajadores en un momento futuro del tiempo. Para preveer y simular cuantos trabajadores pueden necesitar los usuarios de la aplicación pueden usar los recursos virtuales.
   * También pueden existir empresas que deseen gestionar las aplicación sin tener que llevar una gestión de los recursos con respeto los datos de los trabajadores reales de la empresa. Para estos casos, los usuario pueden usar también los recursos virtuales.

Alta de recursos
================

Alta de recursos trabajador
---------------------------

Para crear un trabajador hay que realizar los siguientes pasos:

   * Acceder la Lista de trabajadores, opción *Recursos > Trabajadores*.
   * Presionar el botón Crear
   * Cubrir los campos del formulario: *Nombre*, *Apellidos*.
   * Presionar el botón *Guardar* o bien *Guardar y continuar*.

A partir diera momento existirá un nuevo trabajador en *NavalPlan*.

Como nota decir que existe una comprobación que impide la grabación de dos trabajadores con el incluso nombre, apellidos y NIF. Todos estos campos son, además, obligatorios.

Alta de máquinas
----------------

Para crear una máquina dar los siguientes pasos:

   * Accede la Lista de trabajadores, opción *Recursos > Máquinas*.
   * Presionar el botón *Crear*.
   * Cubrir los datos en la pestaña de datos de la máquina. Los datos a cubrir son:

      * *Nombre*. Nombre de la máquina
      * *Código de la máquina*. El código de la máquina tiene que ser único. Se puede autogenerar.
      * *Descripción de la máquina*.

Alta de recursos virtuales
--------------------------

Para crear un recurso virtual dar los siguientes pasos:

   * Accede la *Lista de grupos de trabajadores virtuales*, opción *Recursos > Grupo de trabajadores virtuales*.
   * Presionar en el botón *Crear*.
   * Cubrir los datos en la pestaña de *Datos personales*. Los campos a cubrir son:

      * Nombre del grupo de recursos virtual.
      * *Capacidad*. La capacidad significa cuantos recursos forman parte del grupo. Esto implica que un recurso virtual puede trabajador por día su capacidad multiplicada por el número de horas que trabaja por día de acuerdo con su calendario.
      * *Observaciones*.

Alta de criterios
=================

Alta de criterios en trabajador
-------------------------------

Los trabajadores de la empresa satisfacen criterios. El hecho de que un trabajador cumpla un criterio significa que tiene una determinada capacidad o tiene una determinada condición que tiene relevancia para la planificación.

Los criterios se satisfacen durante un determinado período de tiempo o bien a partir de una determinada fecha y de forma indefinida.

Para asignar un criterio a un trabajador hay que dar los siguientes pasos:

  * Acceder a la opción *Recursos > Trabajadores*.
  * Presionar sobre el botón de edición sobre la fila del listado corresponsal la el recurso deseado.
  * Pulsar en la pestaña *Criterios asignados*.
  * Presionar en el botón *Añadir* del lado de la etiqueta *Criterio nuevo*. Esto provoca que se añada una fila con tres columnas de datos:

     * *Columna nombre del criterio*. Seleccionar el criterio que se quiere configurar cómo satisfecho por el trabajador. El usuario tiene que desplegar o buscar el criterio elegido.
     * *Columna fecha de inicio*. Elegir la fecha desde a cuál el trabajador satisface el criterio. Es obligatorio y aparece por defecto cubierta con la fecha del día actual.
     * *Columna fecha de fin*. Configurar la fecha hasta la cual se satisface el criterio. No es obligatoria. Si no se rellena el criterio es satisfecho sin fecha de caducidad.

Adicionalmente existe en la pantalla un *checkbox* para seleccionar que criterios son listados. Dos opciones: todos los satisfechos durante toda la historia del trabajador o únicamente los vigentes en la actualidad.

La asignación de criterios se rige por las reglas dictadas por el tipo de criterio del criterio que se está asignando. Así por ejemplo cabe mencionar dos aspectos:

   * En criterios de cualquier tipo, una asignación de criterio no se puede solapar en el tiempo con otra asignacion del mismo criterio en un el mismo trabajador.
   * En criterios que no permiten múltiples valores por recurso, no puede haber dos asignaciones de criterio del mismo tipo de criterio de manera que sus intervalos de validez tenga algún punto en común.

Los criterios que son seleccionables para ser asignados a los trabajadores son los criterios de tipo *TRABAJADOR*.

Alta de criterios en máquina
----------------------------

Para asignar un determinado criterio a una máquina hay que dar los siguientes pasos:

   * Acceder la opción *Recursos > Máquinas*.
   * Presionar sobre el botón de edición en la fila del listado correspondiente a la máquina que se desea.
   * Pulsar en la pestaña *Criterios asignados*.
   * Presionar en el botón *Añadir criterio*. Esto provoca que se añada una fila con tres columnas de datos:

      * *Columna Nombre del criterio*. Seleccionar el criterio que se quiere configurar  cómo satisfecho por el trabajador. El usuario tiene que desplegar o buscar  el criterio elegido.
      * *Columna Fecha de inicio*.  Elegir la fecha desde a cuál el trabajador satisface el criterio. Es  obligatoria y aparece por defecto cubierta con la fecha del día actual.
      * *Columna Fecha de fin*. Configurar la fecha hasta cual se satisface el criterio.  No es obligatoria. Si no se completa  el criterio es satisfecho de forma indefinida.

Las reglas de asignación de criterios son las mismas que para los trabajadores. La diferencia es que los criterios que son seleccionables para asignar a las máquinas son los criterios de tipo *MAQUINA*

Alta de criterios en grupo de trabajadores virtuales
----------------------------------------------------

La asignación de criterios para los trabajadores virtuales es similar a la asignación de criterios para los trabajadores reales. Los pasos a dar son los siguientes:

   * Acceder la opción *Recursos > Grupos de trabajadores virtuales*.
   * Presionar sobre el botón de edición de la fila del listado que se corresponda con el grupo virtual de trabajadores a lo que se quiera añadir criterios.
   * Seleccionar la pestaña *Criterios asignados*.
   *  Presionar en el botón *Añadir criterio*. Esto provoca que se añada una fila  con tres columnas de datos:

      * Columna *Nombre del criterio*. Seleccionar el  criterio que se quiere configurar  cómo satisfecho por el trabajador. El  usuario tiene que desplegar o buscar  el criterio elegido.
      * Columna *Fecha de inicio*.  Elegir la fecha desde a cuál el trabajador  satisface el criterio. Es obligatoria y aparece por defecto cubierta con la fecha  del día actual.
      * Columna *Fecha de fin*.  Configura la fecha hasta cual se satisface el criterio.  No es  obligatoria. Si no se llena el criterio es satisfecho sin fecha de caducidad..

Las reglas para la asignación de criterios a los grupos de trabajadores virtuales son las mismas que los trabajadores reales.

Asignación de calendarios a recursos
====================================

Conceptos teóricos
------------------

Los trabajadores tienen un calendario propio. Sin embargo, no es un calendario que haya que definir completamente sino que es un calendario que deriva de uno de los calendarios de la empresa.

El hecho de derivar de un calendario significa que, sino se configura, hereda completamente la definiciones del calendario del cual deriva: hereda la definición de la semana de trabajo, los días festivos, etc.

*NavalPlan*, sin embargo, además de hacer que sus recursos deriven del calendario de la empresa, permite la definición de particularidades del calendario. Esto implica que las vacaciones del trabajador o casos especicales de jornada de trabajo como el número de horas de que consta el contrato de trabajo, sea contemplado en la planificación.

Asignación de calendario padre a trabajadores en creación de trabajador
-----------------------------------------------------------------------

En la creación de un trabajador se crea un calendario al trabajador que deriva, por defecto, del calendario configurado por defecto en la aplicación.

La configuración de la aplicación se puede consultar en *Administracion* > *NavalPlan: Configuracion*.

Para cambiar el calendario del cual deriva un recurso en el momento de la creación hay que dar los siguientes pasos:

   * Acceder la Lista de trabajadores, opción *Recursos > Trabajadores*.
   * Presionar el botón *Crear*.
   * Cubrir los campos del  formulario: *Nombre*, *Apellidos*.
   * Presionar en la pestaña *Calendario*
   * En esa pestaña seleccionar lo en el selector que aparece del cual se quiere derivar.
   * Presionar el botón *Guardar*  o bien *Guardar y continuar*.


Asignación de calendario padre a máquinas en creación de máquinas
-----------------------------------------------------------------

Las máquinas configuran el calendario del cual derivan en el momento de la creación de forma similar a los trabajadores. Los pasos son:

   * Acceder la Lista de trabajadores, opción *Recursos > Máquinas*.
   * Presionar el botón *Crear*.
   * Cubrir los campos del  formulario: Nombre de la máquina, código y descripción.
   * Presionar en la pestaña *Calendario*.
   * En esa pestaña  seleccionar lo en el selector que aparece del cual se quiere derivar.
   * Presionar el botón *Guardar*  o bien *Guardar y continuar*.

Asignación de calendario padre a grupos de trabajadores virtuales
-----------------------------------------------------------------

Los grupos de trabajadores virtuales también configuran el calendario padre del cual derivan de forma similar a los trabajadores reales y a las máquinas. Los pasos son:

   * Accede la Lista de grupos de recursos virtuales, opción *Recursos > Grupo de trabajadores virtuales*.
   * Presionar en el botón *Crear*.
   * Cubrir los datos en la pestaña de *Datos personales*.
   * Presionar en la pestaña *Calendario*
   * En esa pestaña  seleccionar lo en el selector que  aparece del cual se quiere derivar.
   * Presionar el botón *Guardar* o bien *Guardar y continuar*.

Cambio de calendario padre a trabajadores, máquinas o grupos de trabajadores virtuales
--------------------------------------------------------------------------------------

Es posible cambiar el calendario padre del cual deriva un recurso cualquiera, ya sea un trabajador, máquina o un grupo de trabajadores virtual.

Para realizarlo hay que hacer lo siguiente:

   * Ir la sección correspondiente: *Recursos > Lista de máquinas, Recursos > Lista de trabajadores* o *Recursos > Grupo virtual de trabajadores*.
   * Acceder la pestaña *Calendario*.
   * Presionar en el botón *Borrar calendario*.
   * Seleccionar el nuevo calendario padre del cual se quiere derivar.
   * Presionar el botón *Guardar* o bien *Guardar y continuar*.

Personalización de calendario de recurso trabajador, máquina o grupo de trabajador virtual
------------------------------------------------------------------------------------------

Los recursos trabajador, máquina o grupo de trabajadores virtuales pueden configurar en su propio calendario los siguientes elementos:

   * Su jornada semanal de trabajo.
   * Excepciones de dedicación en períodos de tiempo.
   * Períodos de activación.

Los dos primeros conceptos, es decir, la jornada semanal de trabajo y las excepciones de dedicación, se explican en la sección de **Administración de calendario general**

Dicho lo anterior, los calendarios de los recursos tienen una particularidad con respeto al calendario de la empresa. Esta peculariedad son los *períodos de activación*.

Los *períodos de activación* son intervalos en los cuáles los trabajadores se encuentran disponibles para la planificación. Conceptualmente se corresponden con aquellos períodos en los cuáles el trabajador se encuentra contratado por la empresa. Un trabajador puede ser contratado por un tiempo, después abandonar la entidad a la que pertenece y reincorporarse posteriormente. *NavalPlan* guarda la historia de planificación de todos los recursos y utiliza la información de activación para impedir que se le asigne trabajo cuando no se encuentran contratados.

En el momento de la creación de un trabajador, por defecto, se configura con un período de activación que va desde el momento del alta hasta el infinito. Si se desea cambiar esta información no es posible realizarlo en el momento de la creación y esta operación tiene que ser hecha con una edición posterior del recurso.

Configuración de los períodos de activación de un recurso
---------------------------------------------------------

Los períodos de activación de un determinado recurso tienen que satisfacer no tener puntos de solapamento en el tiempo. Los pasos para configurarlos son:

   * Ir la sección correspondiente: *Recursos > Máquinas*, *Recursos >  Trabajadores* o *Recursos > Grupo de trabajadores virtuales*.
   * Seleccionar la fila del recurso que se quiere editar y presionar en el botón de la fila asociada para editar.
   * Acceder la pestaña de *Calendario*.
   * Dentro de la pestaña de *Calendario* presionar en la pestaña interior *Períodos de activación*.
   * En el interior de la pestaña saldrán la lista de períodos de activación. Pulsar en el botón *Crear período de activación*.
   * Hoy por hoy se añade una fila con las siguientes columnas:

      * *Fecha de inicio*: A llenar obligatoriamente. Introducir la fecha en la cual se había querido activar el recurso.
      * *Fecha de fin*: Opcional. Introducir la fecha en el cual el trabajador deja de estar activo en la empresa.

   * Presionar en el botón *Guardar* o *Guardar y continuar*.


-------------------
Módulo de proyectos
-------------------

Conceptos teóricos
==================

Los proyectos son las contrataciones de trabajo que las empresas firman con sus clientes. En el conjunto de empresas del naval los proyectos están constituidos por un número de elementos organizados en estructuras de datos jerárquicas (árboles), también llamadas *EdT* (estructuras de trabajo).

Básicamente existen dos tipos de nodos:

   * *Nodos contenedores*. Un nodo contenedor es un agregador y actúa como clasificador de elementos. No introduce trabajo por él mismo, sino que el trabajo por él representado es la suma de todas las horas de sus nodos descendientes.
   * *Nodos hoja*. Un nodo hoja es un nodo que no tiene hijos y que está constituido por uno o más conjuntos de horas.

En *NavalPlan*, por tanto, se permite el trabajo con proyectos estructurados según los tipos de nodos precendentes.

Acceso a vista global de la empresa
===================================

La vista global de la empresa es la pantalla inicial de la empresa, la que se entra una vez que el usuario entra en la aplicación.

En ella lo que se puede ver son todos los proyectos que existen en la empresa y se representan a través de un *diagrama de Gantt*. Los datos que seobservan de cada proyecto son:

   * Su fecha de inicio y su fecha de fin.
   * Cuál es el progreso en la realización de cada proyecto.
   * El número de horas que se llevan hecho de cada uno de ellos.
   * Cuál es su **fecha límite** en caso de que lo tengan.

Además de lo anterior se muestra en la parte inferior de la pantalla dos gráficas:

   * Gráfica de carga de carga de recursos.
   * Gráfica de valor ganado.

Para acceder a la vista de empresa llega con hacer login en la aplicación. En caso de encontrarse ya trabajando con la aplicación, el acceso a la vista de empresa se lleva acabo a través de la operación de menú *Planificación > Planificación de proyectos*.

Creación de un proyecto
=======================

Para la creación de un proyecto hay que acometer los siguientes pasos:

   * Acceder al opción *Planificación > Proyectos*.
   * Presionar en el botón situado en la barra de botón con el texto *Crear proyecto nuevo*.
   * *NavalPlan* muestra una ventana donde se solicitan los datos básicos del proyecto:

      * *Nombre*. Cadena identificativa del proyecto. Obligatorio.
      * *Código del proyecto*. Código para identificar el proyecto. Deber ser único. Se puede no cubrir si se tiene marcado el checkbox *Generar código*. En ese caso se encarga *NavalPlan* de crear el código correspondiente. Obligatorio.
      * *Fecha de inicio*. Esta fecha es la fecha a partir de la cual se comenzará la planificación del proyecto. Obligatorio.
      * *Fecha límite*. Este campo es opcional e indica cuál es el deadline.
      * *Cliente*. Campo para seleccionar cuales de los clientes de la empresa es el contratista del proyecto.
      * *Calendario asignado*. Los proyectos tienen un calendario que dicta cuando se trabaja en ellos. Hay que seleccionar el calendario que se quiere utilizar.

   * Aparecen una serie de pestañas. La que aparece seleccionada por defecto es la primera de ellas, que lleva por título *EDT (tareas)* (estructura de trabajo). Esta pestaña se explica en la sección *Introducción de tareas del proyecto con horas y nombre*
   * Los datos generales pueden ser editados presionando en la pestaña *Datos generales*. Los datos que se pueden introducir son:

      * *Nombre del proyecto*. Cadena identificativa del proyecto. Obligatorio.
      * *Código del proyecto*. Código para identificar el proyecto. Deber ser único. No cubrirlo y mantener marcado el checkbox *Generar código*. Si éste está cubierto se encarga NavalPlan de crear el código correspondiente. Obligatorio.
      * *Código externo*. Campo utilizado para la integración con terceras aplicaciones.
      * *Modo de planificación*: Adelante o atrás. La planificación hacia delante es aquella en la que las tareas se van colocando desde la fecha de inicio y se mueven hacia delante según se establecen dependencias. La planificación hacia atrás es aquella que las tareas se colocan con fin en la fecha de entrega y las dependencias entre ellas se gestionan hacia atrás.
      * *Fecha de inicio*. Esta fecha es la fecha a partir de la cual se comenzará la planificación del proyecto. Obligatorio.
      * *Fecha límite*. Este campo es opcional e indica cuál es el deadline.
      * *Responsable*. Campo de texto para indicar la persona responsable. Informativo y opcional.
      * *Cliente*. Campo para seleccionar cuales de los clientes de la empresa es el contratista del proyecto.
      * *Referencia del cliente*. Identificador externo del cliente si el usuario lo desea utilizar.
      * *Descripción*. Campo para describir el proyecto o poner cualquier nota.
      * *Las dependencias tienen prioridad*. Campo relacionado con la planificación que indica quien manda si las restricciones que tienen las tareas o los movimientos ordenados por las dependencias cuando existe un conflicto.
      * *Calendario asignado*. Los proyectos tienen un calendario que dicta cuando se trabaja en ellos. Hay que seleccionar el que se quiere utilizar.
      * *Presupuesto*. Desglose del dinero en que se presupuestó el proyecto en dos cantidades:

         * *Trabajo*. Cantidad por lo que se presupuestó a mano de obra del proyecto.
         * *Materiales*. Cantidad por lo que se presupuestaron los materiales del proyecto.
      * *Estado*. Un proyecto puede estar en varios estados a lo largo de su existencia. Los ofrecidos son:

         * Ofertado
         * Aceptado
         * Empezado
         * Finalizado
         * Cancelado
         * Subcontratado
         * Pasado a histórico.
   * Pulsa en el botón *Guardar* representado por un disquete de ordenador en la barra para consolidar los cambios.

Si los datos introducidos son correctos el sistema proporciona en una ventana emergente el resultado de la operación.

Edición de un proyecto
======================



Para la edición de un proyecto existen varios caminos posibles:

   * Opcion 1:
      * Ir a la entrada de menú  *Planificación > Proyectos*.
      * Presionar sobre el icono de edición, lápiz sobre hoja de papel, que se corresponda con el proyecto deseado.
   * Opción 2:
      * Ir a *Planificación > Vista de la compañía*.
      * Hacer doble click con el botón izquierdo del ratón sobre la tarea que representa el proyecto en la vista de la empresa o bien pulsar con el botón derecho sobre la tarea y después escoger la opción *Planificar*.
      * Pulsar el icono de la parte izquierda *Detalles de proyecto*.
   * Opción 3:
      * Ir a *Planificación > Vista de la compañía*.
      * Presionar en el icono de la parte izquierda *Listado de proyectos*.
      * Hacer click sobre el icono que representa una libreta en blanco con un lápiz verde o hacer doble click sobre la fila deseada.

Introducción de tareas a un proyecto con horas y nombre
=======================================================

Para introducir las tareas, contenedores o elementos de proyecto hoja, hay que dar los siguientes pasos:

   * Ir la opción *Planificación > Proyectos*.
   * Presionar sobre el icono de edición, lápiz sobre hoja de papel, que se corresponda con el proyecto deseado.
   * Seleccionar la pestaña *EDT (tareas)*
   * Una vez aquí, introducir en la línea de edición ubicada arriba de la tabla de lista de tareas los siguientes valores:

      * En el campo de nombre una identificación de la tarea.
      * En el campo horas un número entero que represente el número de horas de que se compone el trabajo de la tarea.

   * Presionar el botón *Nueva tarea*

Al pulsar en el botón anterior se añade una tarea de tipo hoja y se sitúa al final de las tareas existentes en el árbol de tareas.

En caso de que se quiera cambiar la posición de la tarea y situarla en otro lugar del árbol, debe seleccionarse la fila concreta y después presionar en los iconos ubicados en la zona superior derecha de la edición del proyecto:

   * *Icono flecha arriba*. Pulsándolo se hace que la tarea ascienda en el árbol de tareas.
   * *Icono flecha abajo*. Pulsando en él se hace que la tarea descienda en el árbol de tareas.

A través de lo explicado hasta ahora, lo que se hace es añadir tareas hoja. Ahora bien, también es posible añadir tareas contenedores. Para añadir tareas contedores, el usuario puede seguir varios itinerarios:

Creando tareas contedoras mediante arrastrar y soltar
-----------------------------------------------------

Para poder llevar a cabo esta operación es necesario disponer de al menos dos elementos de proyecto hoja creados según el procedimiento explicado en el punto anterior. Se va a partir del supuesto de que se dispone dos elementos de proyecto hoja elemento *Y1* y elemento *Y2*.

Los pasos a dar son los siguientes:

   * Colocarse con el puntero del ratón enzima del elemento *Y1*.
   * Pulsar el botón izquierdo del ratón y sin soltar arrastrar el elemento *Y1*. Mientras se mantiene pulsado aparecerá un texto sobre el fondo indicando que el elemento *Y1* está agarrado.
   * Desplazar el ratón manteniendo pulsado el botón izquierdo hasta situarse encima del elemento *Y2*. En ese momento liberar el botón del ratón.
   * Lo que ocurre en este punto es que se crea una tarea contenedor que tiene el nombre de *Y2* y poseerá dos hijos con los nombres *Y2 Copia* y *Y1*. El elemento *Y2 Copia* tendrá la carga de trabajo del anterior elemento *Y2* y, ahora, el elemento *Y2* contendrá la suma de las horas de *Y1* y *Y2 Copia*.

Creando tareas contedoras mediante creación con tarea hoja seleccionada
-----------------------------------------------------------------------

Para llevar a cabo esta operación es necesario disponer de una tarea hoja creada, llámemosla *Y1*. A partir de aquí, los pasos para crear un contenedor son:

   * Situar el puntero del ratón en la fila del elemento *Y1* y pulsar el botón izquierdo del ratón en la árela de la fila que va desde lo comienzo hasta el primer icono que sale en la fila (icono de notificación de estado de planificación que se verá más adelante). Tras realizar esta acción la fila aparecerá seleccionada.
   * Introducir en la línea de edición, situada encima de la tabla del árbol de tareas, la nueva tarea, con nombre *Y2* y un número de horas.
   * Presionar en el botón *Añadir* que está situado a la derecha de la etiqueta *Nueva tarea* y los campos de entrada de nombre y horas.
   * Lo que ocurre en este punto es que se crea una tarea contenedor con nombre de *Y2* y poseerá dos hijos con los nombres *Y2 Copia* y *Y1*. El elemento *Y2 Copia* tendrá la carga de trabajo del anterior elemento *Y2* y, ahora, el elemento *Y2* contendrá la suma de las horas de *Y1* y *Y2 Copia*.

Creando tareas contedoras mediante la pulsación del icono de indentación
------------------------------------------------------------------------

Para llevar a cabo esta operación es necesario tener creadas las tareas, *Y1* y *Y2*, situada Y1 antes que Y2. A partir de aquí, hay que llevar a cabo los siguientes pasos:

   * Seleccionar elemento *Y2* (debe salir en amarillo el fondo de la tarea).
   * Pulsar sobre el botón de indentar hacia la derecha, flecha apuntando a la derecha en la zona superior derecha de iconos.
   * Lo que ocurre en este punto es que se crea una tarea contenedora que tiene el nombre *Y2* y posee dos hijos con los nombres *Y2 Copia* y *Y1*. El elemento *Y2 Copia* tiene la carga de trabajo del anterior elemento *Y2* y, ahora, el elemento *Y2* contiene la suma de las horas de *Y1* y *Y2 Copia*.

Desplazamiento de tareas
------------------------

Una vez se tiene una estructura de tareas contenedor y tareas hoja, también se pueden realizar operaciones de modificación de la posición de los elementos en esta estructura.

Para realizar estas operaciones, se disponen de los iconos ubicados en la parte superior derecha de la zona de edición. Simplemente es necesario seleccionar la fila sobre la que se desea aplicar una operación. Los botones de operación son:

   * *Icono flecha arriba*. Permite el desplazamiento hacia arriba de una tarea dentro de todos sus tareas hermanas, es decir, que posean el mismo padre.
   * *Icono flecha abajo*. Permite el desplazamiento hacia abajo de una tarea dentro de todos sus tareas hermanas, es decir, que posean el mismo padre.
   * *Icono flecha izquierda*. Permite desindentar una tarea. Esto supone subirla en la jerarquía y situarla al mismo nivel que su padre actual. Sólo está activado en las tareas que tienen un padre, es decir, que no son raíz.
   * *Icono flecha derecha*. Permite indentar una tarea. Esto supone bajarla en la jerarquía y ponerla al mismo nivel que los hijos de su hermana situado encima de ella. Sólo está permitida esta operación en las tareas que tienen un hermano por encima de él.

Puntos de planificación
=======================

Conceptos teóricos
------------------

Una vez los proyectos está introducidos con un conjunto de horas el siguiente paso es determinar cómo se planifican.

*NavalPlan* es flexible para determinar la granularidad de lo que se quiere planificar y para ello introduce el concepto de puntos de planificación. Esto permite a los usuarios tener flexibilidad a la hora de decidir si un proyecto interesa planificarlo con mucho detalle o bien se interesa gestionarlo más globalmente.

Los puntos de planificación son marcas que se realizan sobre los árboles de tareas de un proyecto para indicar a que nivel se desea planificar. Si se marca una tarea como punto de planificación significa que se va a crear una actividad de planificación a ese nivel que agrupa el trabajo de todas las tareas situados por debajo de él. Si este punto de planificación se corresponde con una tarea que no es raíz, lo que ocurre con las tareas por encima de éñ, es que estas se convierten en tareas contenedoras en planificación.

Una tarea puede estar en 3 estados de planificación teniendo en cuenta los puntos de planificación:

   * **Totalmente planificado**. Significa que el trabajo que ella representa está totalmente incluido en la planificación. Puede darse este estado en tres casos:

      * Que sea punto de planificación.
      * Que se encuentre por debajo de un punto de planificación. En este caso su trabajo ya se encuentra integrado por su punto de planificación ancestro.
      * Que no haya ningún punto de planificación por encima de ella pero que, para todo el trabajo que representa, haya un punto de planificación por debajo de ella que lo cubra.

   * **Sin planificar**. Significa que para el trabajo que representa no hay ningún punto de planificación que recoja parte de su trabajo para ser planificado. Esto ocurre cuando no es punto de planificación y no hay ningún punto de planificación por encima o por debajo de ella en la jerarquía.

   * **Parcialmente planificado**. Significa que parte de su trabajo está planificado y otra parte aun no se incluyó en la planificación. Este caso ocurre cuando la tarea no es punto de planificación, no hay ninguna tarea por encima de ella en la jerarquía que sea punto de planificación y, además, existen descendientes del mismo que sí son puntos de planificación pero hay otros de sus descendientes que están en estado sin planificar.

Asimismo un proyecto tendrá un estado de planificación referido a todos los sus elementos de proyecto y será el siguiente:

   * Un proyecto se encuentra en estado **totalmente planificado** si todos sus elementos de proyecto se encuentran en estado **totalmente planificado**.
   * Un proyecto se encuentra **sin planificar** si todos sus elementos de proyecto se encuentran en estado **sin planificar**.
   * Un proyecto se encuentra **parcialmente planificado** si hay alguna tarea que está en estado **sin planificar** y alguna en estado **planificado**.

Borrar elementos de proyecto
----------------------------

Para borrar elementos de proyectos existe un icono que representa una papelera sobre cada fila que representa una tarea. Por tanto, para borrar hay que:

   * Identificar la fila que se corresponde que tarea que se desea eliminar.
   * Presionar con el botón de izquierdo del ratón sobre el icono de la papelera. Hoy por hoy el sistema procede a borrar tanto la tarea como todos sus descendientes.
   * Pulsar en el icono de **Guardar**, disquete en la barra superior, para confirmar el borrado.

Creación de puntos de planificación
-----------------------------------

Para la creación de puntos de planificación hay que realizar los siguientes pasos:

   * Ir la opción **Planificación > Proyectos**.
   * Identificar la fila que se corresponde con el proyecto que se quiere editar y que tiene que tener elementos de proyecto. Presionar el botón *Editar,* lápiz sobre hoja de papel, y pulsarlo.
   * Seleccionar la pestaña **EDT (tareas)**.
   * Identificar sobre el árbol a que nivel se desea planificar cada parte y, una vez decidido, donde se desea crear una area de planificación pulsar con el ratón sobre un icono que represente un *diagrama de Gantt* de dos tareas. Esto convierte la tarea en punto de planificación, ponen en verde todos los elementos totalmente planificados y se marcará la fila del punto de planificación y sus descendientes con una con una N.
   * Pulsar en el icono de **Guardar**, disquete en la barra superior, para confirmar el borrado.

Para desmarcar el punto de planificación y planificar a otro nivel hacer lo siguiente:

   * Identificar sobre el árbol de tareas el punto que está marcado como punto de planificación y que se desea cambiar.
   * Presionar sobre el icono que representa un *diagrama de Gantt* con una aspa X roja. Tras eslabón, se quita como elemento de planificación y se actualiza el estado de planificación de sus descendientes y antecesores.
   * Pulsar en el icono de *Guardar*, disquete en la barra superior, para confirmar el borrado.

Criterios en tareas
===================

Conceptos teóricos
------------------

Las tareas representan el trabajo que hay que planificar y también pueden exigir el cumplimiento de criterios. El hecho de que una tarea exija un criterio significa que se determina que para la realización del trabajo que asociado a la tarea es necesario que el recurso que se planifique satisfaga ese criterio.

Los criterios cuando se aplican a una determinada tarea se propagan realmente a todos sus descendientes. Esto significa que se un criterio y exigido a uno determinado nivel en el árbol de tareas, pasa a ser la exigido también por todas las tareas hijas.

Por tanto, un criterio puede ser exigido de dos formas en una tarea:

   * *De forma directa*. En este caso el criterio es configurado cómo requerido en la tarea por el usuario.
   * *De forma indirecta*. El criterio es requerido en la tarea por herencia debido la que ese criterio es requerido en una tarea padre.

Los criterios indirectos de una tarea pueden ser invalidados, es decir, configurados como no aplicados en un determinada tarea descendiente del primero. Si un criterio indirecto es invalidado en un determinada tarea, entonces se invalida en todos los descendientes del elemento que se está configurando cómo invalidado.

Introducción de criterio en una tarea hoja
------------------------------------------

Para dar de alta un criterio en una tarea hoja hay que dar los siguientes pasos:

   * Ir a la opción *Planificación > Proyectos*
   * Identificar sobre la lista de proyectos el proyecto con el cual se quiere trabajar.
   * Pulsar en el botón editar del proyecto hoja deseado.
   * Seleccionar la pestaña *EDT (tareas)*
   * Identificar la tarea hoja al cual se desea configurar los criterios.
   * Presionar en el botón *Editar* de la tarea. Esto abre una ventana emergente.
   * Sobre la ventana emergente seleccionar la pestaña *Criterio requerido*.
   * Pulsa en el botón *Añadir* en la primera sección denominada *Criterios asignados requeridos*.
   * Hoy por hoy se añade una fila en la cual, en la primera columna, **Nombre del criterio**, se incluye un componente de búsqueda de criterios. Pulsar con el botón izquierdo del ratón sobre este componente de búsqueda y comenzar a teclear el nombre del criterio o tipo de criterio del cual se quiere añadir el criterio.
   * Seleccionar sobre el conjunto de criterios que encajan con la clave de búsqueda tecleada por el usuario aquel en concreto que se quiere requerir a la tarea.
   * Pulsar en *Atrás*.
   * Presionar sobre el icono de *Guardar* representado por un disquete de la barra de operación ubicada en la parte superior.

Introducción de criterio en una tarea contedor
----------------------------------------------

Para dar de alta un criterio en una tarea contenedora hay que dar los siguientes pasos:

   * Ir la opción *Planificación > Proyectos*
   * Identificar sobre la lista de proyectos el proyecto con el cual se quiere trabajar.
   * Pulsar en el botón editar del proyecto deseado.
   * Seleccionar la pestaña *EDT (tareas)*
   * Identificar la tarea contenedora a la cual se desea configurar los criterios.
   * Presionar en el botón *Editar* de la tarea. Esto abre una ventana emergente.
   * Sobre la ventana emergente seleccionar la pestaña *Criterio requerido*.
   * Pulsa en el botón *Añadir* en la primera sección denominada *Criterios asignados requeridos*
   * Hoy por hoy se añade una fila en la cual en la primera columna, *Nombre del criterio*, se incluye un componente de búsqueda de criterios. Pulsar con el botón izquierda del ratón sobre este componente de búsqueda y comenzar a teclear el nombre del criterio o tipo de criterio del cual se quiere añadir el criterio.
   * Seleccionar sobre el conjunto de criterios que encajan con la clave de búsqueda tecleada por el usuario aquel en concreto que se quiere requerir a la tarea.
   * Pulsar en *Atrás*.
   * Presionar sobre el icono de guardar representado por un disquete de la barra de operación ubicada en la parte superior.

Para comprobar cómo se añade el criterio sobre todos los elementos hijos descendentes de la tarea contedor al cual se le requirió el criterio dar los siguientes pasos:

   * Identificar sobre el árbol de tareas del proyecto sobre lo que se está trabajando una tarea hijo de la tarea contedor que requiere un criterio.
   * Pulsar sobre el botón de edición de la tarea identificado en el punto anterior.
   * Sobre la ventana emergente seleccionar la pestaña **Criterio requerido**
   * En la sección de la parte superior de la ventana titulada **Criterios asignados requeridos** se observará el criterio requerido buscar el nombre del criterio requerido por la tarea padre. Aparecerá mostrado cómo **Indirecto** en la columna de tipo.

Invalidar un requerimiento de criterio en una tarea
---------------------------------------------------

Para llevar a cabo a operación descrita en este epígrafe hay que tener una situación al menos de una tarea contedor Y1 que tenga dentro una tarea Y2 y la tarea Y1 haya requerido un criterio C1.

Bajo esta premisa, para invalidar el criterio C1 en el elemento Y2 hay que efectuar los siguientes pasos:

   * Identificar sobre el árbol de tareas el elemento Y2.
   * Pulsar sobre el icono de edición de la fila correspondiente la Y2.
   * Ir la pestaña *Criterios requeridos*.
   * Identificar en la tabla de la sección **Criterios asignados requeridos** el criterio C1 que tiene que aparece con el tipo **Indirecto**
   * Presionar en el botón invalidar.
   * Pulsar en *Atrás*.
   * Presionar sobre el icono de *Guardar* representado por un disquete de la barra de operaciones ubicada en la parte superior.

Borrar un requerimiento de criterio en una tarea
------------------------------------------------

Los requerimientos que se pueden borrar son únicamente los criterios directos, ya que los criterios indirectos únicamente se pueden invalidar. Los pasos que hay que dar para borrar un criterio directos son los siguientes:

   * Ir la opción *Planificación > Proyectos*.
   * Identificar sobre la lista de proyectos, el proyecto con el cual se quiere trabajar.
   * Pulsar en el botón editar del proyecto deseado (o hacer doble click sobre la fila deseada).
   * Seleccionar la pestaña *EDT (tareas)*.
   * Identificar la tarea que tiene un criterio directo que se desea eliminar.
   * Presionar en el botón editar de la tarea. Esto abre una ventana emergente.
   * Sobre la ventana emergente seleccionar la pestaña *Criterio requerido*.
   * Identificar en la tabla de la sección *Criterios asignados requeridos* el criterio directo que se desea borrar.
   * Presionar en el icono de *Borrar* de la fila correspondiente.
   * Pulsar en el botón *Atrás*
   * Presionar sobre el icono de *Guardar* representado por un disquete en la barra de operaciones ubicada en la parte superior.

Gestión de requerimientos a nivel de proyecto
---------------------------------------------

A todos los efectos un proyecto actúa como una tarea contenedora que engloba todas las tareas raíces. Por tanto, en referencia a los criterios, todos los que se asignen al proyecto son heredados como criterios indirectos en todas las tareas.

Como se puede deduce también, un proyecto no puede recibir criterios indirectos, ya que es la raíz del árbol de sus elementos de proyecto.

Los pasos para acceder la gestión de los criterios a nivel de proyecto son los siguientes:

   * Ir la opción *Planificación > Proyectos*.
   * Identificar sobre la lista de proyectos el proyecto sobre lo cuál se quiere trabajar.
   * Presionar en el botón editar del proyecto.
   * Seleccionar la pestaña *Criterio requerido*
   * Acceder a la sección *Criterios asignados requeridos* donde se pueden gestionar la adición de criterio directos y el borrado de los existentes como el explicado en las tareas.
   * Presionar sobre el icono de *Guardar* representado por un disquete en la barra de operaciones ubicada en la parte superior.

-----------------------
Módulo de planificación
-----------------------

Para comprender las principales funcionalidades de planificación de la aplicación es preciso acceder la sección *Planificación > Planificación de proyectos*. *Navaplan* permite consultar información sobre planificación de la empresa en dos niveles:

   * **Nivel Empresa**: se puede consultar la información de todos los proyectos en curso.
   * **Nivel Proyecto**: se puede consultar la información de todas las tareas de un proyecto.

Desde la vista de empresa es posible navegar a la planificación de un proyecto haciendo doble click en la caja del *diagrama de Gantt* que representa el proyecto o pulsando con el botón derecho para abrir el menú contextual seleccionando *Planificar*.

Para volver a la vista de empresa se tiene que pulsar en el menú principal en *Planificación > Planificación de proyectos* o en **INICIO** en la ruta que muestra la información que se esté visualizando (migas de pan).

La vista de empresa ya detallada previamente es la pantalla principal de la aplicación para el seguimiento de la situación de los proyectos de la empresa.

Perspectivas: vista de recursos, proyectos y asignación avanzada
================================================================

Tanto la vista de empresa como la de nivel proyectos permiten la visualización de diferentes perspectivas de la información. Las perspectivas permiten cambiar el punto de vista desde lo que se consulta la información de planificación Recursos, Tareas o Temporal (diagrama de Gantt).

Dentro de cada nivel Empresa o Proyecto es posible cambiar de una perspectiva pulsando en los iconos que se muestran en la parte izquierda de la vista de planificación.

En la **vista de la empresa** existen tres perspectivas disponibles:

   * *Planificación de proyectos*: muestra la visión de los proyectos en el tiempo con una representación de *diagrama de Gantt*, en esta vista aparecen todos los proyectos planificados con su fecja de inicio y de fin. Graficamente se puede ver en cada caja el grado de progreso, el número de horas trabajadas en el proyecto y las fechas límites de entrega.
   * *Uso de recursos*: muestra la visión de los recursos de la empresa en el tiempo, representando en un grafico de líneas del tiempo a carga de trabajo de los recursos con el detalle de las tareas las que están asignados.
   * *Lista de proyectos*: muestra el listado de los proyectos existentes con su información de fechas, presupuesto, horas y estado y permite acceder la edición de los detalles del proyecto.
   * *Planificación de recursos limitantes*: Vista de planificación de los recursos que son limitantes, es decir, actúan como colas, de suerte que tareas de otros proyectos son gestionados en las colas de los recursos limitantes de la empresa.

En la **vista de proyecto** existen cuatro perspectivas disponibles:

   * *Planificación del proyecto*: muestra la visión de las tareas del proyecto en el tiempo con una representación de *diagrama de Gantt*. En esta vista puede consultarse la información de las fechas de inicio y fin, la estructura jerárquica de las tareas, los progresos, las horas imputadas, las dependencias de tareas, los hitos y las fechas límite de las tareas.
   * *Uso de recursos*: muestra la visión de los recursos asignados al proyecto en el tiempo con su carga de trabajo tanto en tareas de este proyecto como las pertenecientes a otros proyectos por asignaciones genéricas o específicas.
   * *Detalles de proyecto*: permite acceder a toda la información del proyecto, organización del trabajo, asignación de criterios, materiales, etc. Ya fue tratada dentro de la edición de proyectos.
   * *Asignación avanzada*: muestra la asignación numérica con diversos niveles de granularidad (día,semana,mes) de los recursos en las tareas del proyecto. Permite modificar las asignaciones de recursos en ellas.
   * *Si se habilitó el "método de Monte Carlo"*: Con este método se puede simular la duración de las planificaciones basándose en una estimación, optimista, pesimista y realista de las duraciones de las tareas del camino crítico y probabilidades de ocurrencia de cada duración. A partir de la anterior información, *NavalPlan* ofrece la probabilidad de finalización del proyecto en una fecha o una semana concreta.

Vista de planificación de empresa
=================================

La vista de planificación de empresa muestra en el tiempo los proyectos en curso. Los proyectos se representan mediante un *diagrama de Gantt* que indica las fechas de inicio y fin de los proyecto mediante la visualización de una caja en un eje temporal.

La vista de planificación dispone de una barra de herramientas en la parte superior que permite realizar las siguientes operaciones:

   * *Impresión de la planificación*: Genera un fichero PDF o una imagen en PNG con el gráfico de la planificación.
   * *Nivel de zoom*: permite modificar la escala temporal en la que se muestra la información. Se puede seleccionar la granularidad a distintos niveles: día, semana, mes, trimestre, año.
   * *Mostrar/Ocultar etiquetas*: oculta o muestra en el *diagrama de Gantt* las etiquetas asociadas a cada uno de los proyectos.
   * *Mostrar/Ocultar progresos*: oculta o muestra en el *diagrama de Gantt* los progresos asociados a cada uno de los proyectos.
   * *Mostrar/Ocultar horas asignadas*: oculta o muestra en el *diagrama de Gantt* las horas asignadas asociadas la cada uno de los proyectos.
   * *Mostrar/Ocultar asignaciones*: oculta o muestra en el *diagrama de Gantt* los recursos asignados a cada uno de los proyectos.
   * *Filtrado de etiquetas y criterios*: permite seleccionar proyectos en base a que cumplan criterios o tengan asociadas etiquetas.
   * *Filtrado por intervalo de fechas*: permite seleccionar fechas de inicio y fin para lo filtrado.
   * *Selector de filtrado en subelementos*: realiza las búsquedas anteriores incluyendo los elementos y tareas que forman el proyecto. Y no únicamente las etiquetas y criterios asociadas al primero nivel del proyecto.
   * *Acción de filtrado*: ejecuta la búsquera en base a los parametros definidos anteriormente.

En la parte izquierda están los cambios de perspectivas a nivel de empresa que permitirá ir a la sección de *Carga global de recursos* y *Lista de proyectos*. La perspectiva que se esté visualizando y la *Planificación*.

En la parte inferior se muestra la información de la carga de los recursos en el tiempo así como las gráficas referentes al valor ganado que serán explicadas más adelante.

Vista de planificación de proyecto
==================================

Para acceder la vista de planificación de un proyecto es preciso hacer doble
click en la representación del *diagrama de Gantt* en un proyecto, o cambiar la
perspectiva de planificación desde la perspectiva de detalle de proyectos.

En esta vista se puede acceder a las acciones de definición de dependencias entre tareas y asignación de recursos.

La vista de planificación de proyecto dispone de una barra de herramientas en la parte superior que permite realizar las siguientes operaciones:

   * *Guardar planificación*: consolida en la base de datos todos los cambios realizados sobre la planificación y la asignación de recursos. **ES importante guardar siempre los cambios una vez terminada la elaboración de la planificación**. Si se cambia de perspectiva o se entra en otra sección se perderán los cambios.
   * *Operación de reasignar*: esta operación permite recalcular las asignaciones de recursos en las tareas del proyecto.
   * *Nivel de zoom*: permite modificar la escala temporal en la que se muestra la información.  Se puede seleccionar la granularidade a distintos niveles: día,  semana, mes, trimestre, año.
   * *Resaltar camino crítico*: muestra el camino crítico del proyecto, es decir, realiza el cálculo de aquellas tareas que su retraso implica un retraso del proyecto.
   * *Mostrar/Ocultar  etiquetas*: oculta o muestra en el *diagrama de Gantt* las etiquetas asociadas la cada una de las tareas.
   * *Mostrar/Ocultar asignaciones*: oculta o muestra  en el *diagrama de Gantt* los recursos asignados la cada una de las tareas.
   * *Mostrar/Ocultar horas asignadas*: oculta o muestra en el *diagrama de
     Gantt* las horas asignadas asociadas la cada una de las tareas.
   * *Mostrar/Ocultar asignaciones*: oculta o muestra en el *diagrama de Gantt* los recursos asignados la cada una de las tareas.
   * *Expandir tareas hoja*: muestra todas las tareas de último nivel expandiendo todos los niveles de la arbore de tareas.
   * *Filtrado de etiquetas  y criterios*: permite seleccionar proyectos en base a que cumplan  criterios o tengan asociadas etiquetas.
   * *Filtrado por intervalo de fechas*: permite seleccionar fechas de inicio y
     fin para el filtrado.
   * *Filtrado por nombre*: permite indicar el nombre de la tarea.
   * *Acción de filtrado*:  ejecuta la búsqueda en base a los parametros definidos  anteriormente.

Justo arriba de la barra de tareas encontrara el nombre del proyecto que esta
detrás del texto *INICIO > Planificación > Planificación de proyectos > NOMBRE
DEL PROYECTO*.

Si el proyecto se encuentra totalmente planificado aparece con una letra C
(Completamente Planificado), pero si no están marcados todos los puntos de
planificación del proyecto mostrarse una letra P (Parcialmente Planificado).
Sólo se muestra la letra C cuando todas las tareas en la edición del proyecto se
encuentren por debajo de un punto de planificación.

En la vista de planificación de proyecto se puede observar que las tareas se organizan de forma jerárquica, de forma que se pueden expandir y comprimir las tareas.

En la parte inferior se muestra la información de la carga de los recursos en el tiempo así como las gráficas referentes al valor ganado que serán explicadas más adelante.

En la vista de planificación de un proyecto se puede hacer las siguientes operaciones de interés:

   * Definición de dependencias entre tareas.
   * Definición de retriccións de tareas.
   * Asignación de recursos a tareas

Asignación de dependencias
--------------------------

Una dependencia es una relación entre dos tareas por la cual una *tarea A* no
puede comenzar o terminar hasta que una *tarea B* comience o final. *Navalplan*
implementa las siguientes relaciones de dependencias entre tareas entre dos
tareas llamadas A y B.

   * *Fin - Inicio*: La tarea B no puede comenzar hasta que la tarea A final. Esta y la relación de dependencia más común.
   * *Inicio - Inicio*: La tarea B no puede comenzar hasta que la tarea A haya comenzado.
   * *Fin - Fin*: La tarea B no puede terminar hasta que la tarea La haya rematado.

Para añadir una dependencia proceda de la siguiente forma:

   * Marcar la tarea que se quiere que genere la dependencia. La tarea de la que se depende para que la dependencia sea cumplida.
   * Presionar el botón derecho del ratón sobre la tarea y en el menú contextual
     seleccionara la opción *Añadir Dependencia*.
   * Se muestra una flecha que seguirá el puntero del ratón.
   * Seleccionar haciendo click con el ratón la tarea dependiente, la que recibe
     la dependencia. Una vez seleccionada se creará una dependencia *Fin-Inicio* entre las dos tareas.
   * Para modificar el tipo de dependencia es preciso pulsar el botón derecho
   * del ratón en la flecha de la dependencia y seleccionar en el menú
   * contextual el tipo de dependencia como *Fin - Inicio*, *Fin-Fin* o
     *Inicio-Inicio*.
   * En el momento de crear la dependencia el planificador recalcula la posición temporal de las tareas segundo las dependencias. Alertará en caso de que se produzca un ciclo de dependencias indicando que su creación no es posible.
   * Recordar que es preciso pulsar en el icono de *grabar* para consolidar los
     cambios en la planificación.

El comportamiento del recálculo de asignaciones de tareas se comporta de diferente manera dependiendo del tipo de planificación elegida para el proyecto:

   * *Planificación hacia delante*: La tarea en la que entra la dependencia es colocada justo después de la tarea origen. Las asignaciones de recursos se harán hacia delante y se le establecerá una restricción *TAN PRONTO COMO SEA POSIBLE*.
   * *Planificación hacia atrás*: La tarea de la que sale la dependencia es
     colocada justo antes de la fecha de comienzo de la tarea destino de la
     dependencia. Las asignaciones de recursos se hace hacia atrás temporalmente
     y se le establecerá una restricción *TAN TARDE COMO SEA POSIBLE*.

Asignación de recursos
======================

La asignación de recursos es una de las partes más importantes de la  aplicación. La asignación de recursos puede realizarse de dos maneras diferentes:

   * Asignaciones específicas.
   * Asignaciones genéricas.

Cada una de las asignaciones se explica en las secciones posteriores.

Para realizar cualquiera de las dos asignaciones de recursos es necesario  dar los siguientes pasos:

   * Acceder a la planificación de un proyecto.
   * Presionar con el botón derecho sobre la tarea que se desea asignar en la opción de asignación de recursos.
   * La aplicación muestra una pantalla en la que se puede  visualizar los
     siguientes datos.

      * **Información de la tarea**:

         * *Listado de criterios que deben ser satisfechos*. Se agrupan las horas según los criterios que requieren en grupos y se informa de los criterios que requiere cada grupo.
         * *Asignación recomendada*: Opción que le permite a la  aplicación recoger los criterios que deben ser satisfechos y las horas  totales de cada grupo de horas y hace una propuesta de asignación genérica recomendada. Si había una asignación previa, el sistema elimina dicha  asignación sustituyéndola por la nueva.

      * **Configuración de asignación**:

         * *Fecha de inicio* y *fecha de fin*  de la tarea.
         * *Duración*.
         * *Tipo de cálculo*: El sistema permite elegir la estrategia  que se desea llevar a cabo para calcular las asignaciones:
         * *Calcular número de horas*: Calcula el número de horas que haría falta  que dedicaran los recursos asignados dados una fecha de fin y un número de recursos  por día.
         * *Calcular fecha fin*: Calcula la fecha de fin de la tarea a partir del  número de recursos de la tarea y de las horas totales dedicar para finalizar la tarea.
         * *Calcular número de recursos*: Calcula el número de recursos necesarios  para finalizar la tarea en una fecha específica y dedicando una serie de horas  conocidas.

      * **Asignaciones**:
         * *Asignaciones*: Listado de asignaciones realizadas.  En este listado
           se podrán ver las asignaciones genéricas (el nombre sería la lista
           de criterios satisfecha, horas y número de recursos por día) y las
           asignaciones específicas. Cada  asignación realizada puede ser borrada explícitamente presionando en el botón  de borrar.

   * Introducir el nombre del recurso o criterio deseado en el campo *Seleccione criterios o recursos*. También es posible presionar en *Búsqueda avanzada* para realizar una búsqueda avanzada.
   * Si el usuario utiliza el selector de búsqueda simple: El sistema mostrará un listado que cumpla con las condiciones de búsqueda. El usuario debe elegir el recurso o criterio que desea y presionar en *Añadir*.

      * Si el usuario elige un recurso, *NavalPlan* realizará una asignación específica. Ver sección **Asignación específica** para  conocer que significa elegir esta opción.
      * Si el usuario elige un criterio, *NavalPlan* realizará una asignación
      * genérica. Ver sección **Asignación genérica** para conocer que significa elegir esta opción.

   * Si se eligió la opción de búsqueda avanzada para realizar la asignación: La aplicación muestra una nueva pantalla formada por un árbol de criterios  y un listado a la derecha de los trabajadores que cumplen los criterios seleccionados:
      * Seleccionar el tipo de asignación a realizar:

         * *Asignación específica*. Ver sección **Asignación específica** para  conocer que significa elegir esta opción.
         * *Asignación genérica*. Ver sección **Asignación genérica** para conocer que significa elegir esta opción.

      * Seleccionar una lista de criterios (asignación genérica) o una lista de trabajadores (asignación específica). La elección  múltiple se realiza presionando en el botón *Crtl* a la hora de pulsar en cada trabajador o criterio.
      * Presionar en el botón *Seleccionar*. Es  importante tener en cuenta que,
        si no se marca asignación genérica, es  necesario escoger un trabajador
        o máquina para poder realizar una  asignación. En caso contrario, llega con elegir uno o varios criterios.
   * La aplicación muestra en el listado de asignaciones de la pantalla original
     de asignación de recursos la lista de criterios o recursos  seleccionados.
   * Cubrir las horas o el número de recursos por día dependiendo de la
     estrategia de asignación que se le solicitó llevar a cabo a la aplicación.
   * Presionar en el botón *Aceptar* para marca la asignación como hecha. Es
     importante reseñar que la operación no será consolidada hasta que se pulse
     en el icono de grabar de la vista de planificación. Si se sale de la vista de planificación se perderán los cambios.
   * El planificador calculará la nueva duración de las tareas en base a asignación realizada.

La vista expandida es mostrada se se marca el *checkbox* que aparece al lado del
texto *vista expandida*. Esta vista es útil para la visualización de datos con consolidación de progresos. Los campos mostrados son:

   * *Nombre*: Nombre de la asignación (criterio asignado o recurso asignado).
   * *Horas. Original*: Horas originalmente asignadas al recurso o criterio anterior.
   * *Horas. Consolidado*: Horas que se consolidaron en una fecha concreta como horas que representan el progreso consolidado.
   * *Horas. No consolidado*: Horas que quedan por hacer de la tarea, una vez se consolidaron un porcentaje de las horas en una fecha concreta.
   * *Horas. Total*: Ratio de recursos por día total de la tarea.
   * *Horas. Consolidado*: Ratio de recursos por día de las horas ya consolidadas de la tarea.
   * *Horas. No consolidado*: Ratio de recursos de las horas nonn consolidadas de la tarea.

Asignación de recursos específicos
==================================

La asignación específica es aquella asignación de un recurso de manera concreta y específica a una tarea de un proyecto, es decir, el usuario de la aplicación está decidiendo que "nombre y apellidos" o qué "máquina" concreta debe ser asignada a una tarea.

La aplicación, cuando un recurso es asignado específicamente, crea  asignaciones diarias en relación al porcentaje de recurso diario que se  eligió para asignación, contrastando previamente con el calendario disponible del recurso. Ejemplo: una asignación de 0.5 recursos para  una tarea de 32 horas hace que se asignen al recurso específico  (suponiendo un calendario laboral de 8 horas diarias) 4 horas diarias para realizar la tarea.

Para realizar la asignación a un recurso específico es preciso centrarse en los siguientes pasos en la pestaña de asignación de recursos de una tarea.

   * Introducir un nombre o apellidos de recurso en el campo de búsqueda que
     sale a la derecha del texto "Seleccione criterios o recursos" y seleccionar
     el recurso de entre los que cumplen los criterios de filtrado. Presionar en
     *Añadir*.
   * Otra opción sería:
      * Pulsar en la opción de *Búsqueda avanzada*
      * Marcar asignación específica como tipo de asignación.
      * Filtrar los recursos empleando los criterios que cumple.
      * Seleccionar un recurso o varios (empleando *Ctrl+Selección* con el ratón).
      * Presionar en el botón *Seleccionar*.
   * En la vista general de asignación indicar la carga de trabajo diaria de cada recurso o el número de horas asignadas. Este campo dependerá del tipo de calculo seleccionado en la asignación.
   * Presionar *Aceptar* o *Aplicar cambios de la pestaña*.
   * Una vez completada la asignación grabar la planificación del proyecto y consultar la carga de los recursos asignados.

Asignación de recursos genérica
===============================

La asignación genérica es una de las aportacións de más interesantes de la
aplicación. En una parte importante de los trabajos no es interesante conocer a
priori quien va a realizar las tareas de un proyecto. En este caso lo único que
interesa para realizar la asignación es identificar los criterios que tienen que
cumplir los recursos que pueden hacer esa tarea. El concepto de asignación
genérica representa, por tanto, la asignación por criterios en lugar de por
personas. El sistema será el encargado de realizar la asignación entre los
recursos que cumplan los criterios necesarios. El sistema hace una asignación
totalmente arbitraria pero que es válida a efectos de conocer la carga general de los recursos de la empresa.

La asignación de recursos la una tarea sigue el calendario definido para el proyecto habida cuenta el número de recursos asignados que cumplan los criterios definidos.

Para realizar la asignación a un  recurso genérico so es preciso centrarse en los siguientes pasos en la pestaña  de asignación de recursos de una tarea.

   * Introducir un nombre de criterio en el campo de búsqueda que sale a la derecha del texto "Seleccione criterios o recursos" y seleccionar el recurso de entre los que cumplen los criterios de filtrado. Presionar en "Añadir".
   * Otra opción sería:
      * Pulsar en la opción de *Búsqueda  avanzadilla*
      * Marcar asignación  genérica como tipo de asignación.
      * Seleccionar uno o varios criterios (empleando *Ctrl+Selección* con el ratón).
      * Presionar en el botón *Seleccionar*.
   * En la vista general de asignación  indicar la carga de trabajo diaria para la asignación genérica o el  número de horas asignadas. Este campo dependerá del tipo de calculo  seleccionado en la asignación.
   * Presionar *Aceptar* o *Aplicar  cambios de la pestaña*.
   * Una vez completada la asignación  grabar la planificación del proyecto y consultar la carga de los  recursos asignados.

Cuando se hace una asignación genérica no se tiene el control sobre a qué
recursos se asigna la carga de trabajo. El sistema hace un reparto sobrecargando equitativamente a los recursos se había ido necesario si no existe capacidad suficiente en ese momento del tiempo de los recursos que cumplen los criterios de la tarea.

Asignación recomendada
----------------------

En la vista de asignación y posible marcar la **Asignación recomendada**. Esta
opción permite a la aplicación recoger los criterios que deben ser satisfechos y
las horas totales de cada grupo de horas y hacer una propuesta de asignación genérica recomendada. Esto garantiza que las horas a asignar coinciden con las horas orzamentadas así como el suyo reparto por criterios.

Si había  una asignación previa, el sistema elimina dicha asignación sustituyéndola por la nueva. La asignación que se realiza será siempre una asignación genérica sobre los criterios existentes en el proyecto.

Revisión de asignación en la pantalla de carga de recursos
==========================================================

En el momento de contar con los recursos posible para asignar a la tareas de un proyecto la
es conveniente consultar la carga que tienen. El usuario puede
consultar el estado de carga de los recursos a través de la  perspectiva
denominada *carga de recursos*.

En esta vista se ve la información de los recursos específicos o genéricos asignados al proyecto así como la carga, con la información de las  tareas las que han sido asignados los mismos.

En un primero nivel se muestra el nombre del recurso y, a su lado, muestra una
línea gráfica que indica la carga  del recurso en el tiempo. Si en un intervalo
la barra está en rojo, el recurso se encuentra sobrecargado por riba del 100%, en naranja si la carga  está al 100% y en verde si la carga es inferior al 100%.  Esta barra marca  con líneas verticales blancas los cambios de asignaciones de tareas.

Al posicionarse con el puntero ratón por encima de la barra y esperar unos segundos aparecerá el detalles de la  carga del recurso en formato numérico.

Por cada línea de recurso se puede expandir la información y consultar las
tareas y la carga que supone cada una de ellas.  Se pueden identificar las
tareas del proyecto ya que aparecen con la nomenclatura  *Nombre del
proyecto::Nombre de la tarea*. También se muestran tareas  de otros proyectos
para poder analizar las causas de las sobrecargas de los  proyectos. Cuando la
carga es debida en un recurso específico a una asignación genérica se muestra la
tarea con los nombre de los criterios entre corchetes. También es posible conocer qué tareas de otros proyectos están cargando el recurso en tela de juicio.

Esta  perspectiva permite conocer en detalle la situación de los recursos con
respecto a las tareas de los proyectos que tienen asignadas.

Revisión de asignaciones en la pantalla  de asignación avanzada
===============================================================

Si un proyecto tiene asignaciones se puede acceder a la perspectiva de
asignación avanzada para revisar en detalle las asignaciones. En esta vista se
ve el proyecto como una tabla que muestra las tareas con los recursos asignados
a la misma  a lo largo del tiempo. Hay una fila por cada par (tareas,asignación
a un recurso). Las columnas son, como se indico, las unidades de tiempo  y estas
son diferentes dependiendo del nivel definido de **Zoom**.

En esta vista se puede comprobar el resultado de la asignación diaria de cada una de las asignaciones  hechas previamente. Existen dos modos de acceder a la asignación  avanzada:

   * Accediendo a un proyecto  concreto y cambiar de perspectiva para
     asignación avanzada. En este caso  se mostrarán todas las tareas del
     proyecto y los  recursos asignados (tanto  específicas cómo genéricas).
   * Accediendo a la asignación  de recursos y presionando en el botón
   * *Asignación  avanzada*. En este caso  se mostrarán las asignaciones de la tarea para la que se  está asignando  recursos (se muestran tanto las genéricas como las específicas).

Se puede  acceder al nivel de zoom  que desee:

   * Si el  zoom elegido es un zoom superior a día. Si el usuario modifica el  valor  de horas asignado a la semana, mes, cuatrimestre o semestre, el  sistema  reparte las horas de manera lineal durante todos los días del  período  elegido.
   * Si el zoom elegido es un zoom de día. Si el usuario  modifica el valor de horas  asignado al día, estas horas sólo aplican al día. De este modo el usuario puede decidir cuantas horas se asignan diariamente a los recursos de la tarea.

   Para  consolidar los cambios de la asignación avanzada es preciso presionar el botón de *Guardar*. Es importante que el total de horas coincida con el total de horas asignadas a un intervalo temporal.

En la pantalla de asignación avanzada es posible realizar asignaciones en base a funciones:
   * *Función lineal por tramos*. Calcula tramos lineales en base a una serie de
     puntos dados por los pares: (punto que marca un porcentaje de la duración de la tarea,
     porcentaje de avance esperado).
   * *Función de interpolación polinómica*. Función que en base a una serie de
     puntos dados por los pares (punto que marca un porcentaje de la duración de
     la tarea, porcentaje de avance esperado) calcula el polinomio que satisface
     la curva.

Creación de hitos
=================

En la planificación de un proyecto pueden existir hitos. Los hitos se consideran
como tareas que no tienen trabajo asociado, por lo que no pueden tener
asignaciones. La principal utilidad de los hitos es marcar eventos
como puede ser el fin de proyecto, una auditoría o un punto de control. Así
mismo, se pueden establecer dependencias con ellos.

Desde la vista de planificación de proyectos se puede crear un hito siguiendo los siguientes pasos:

   * Seleccionar una tarea para marcar la posición gráfica donde se quiere crear el hito.
   * Pulsar con el botón derecho sobre la tarea y seleccionar sobre el menú contextual *Añadir hito*
   * Se creará un hito justo debajo de la tarea seleccionada.
   * Se puede desplazar el hito en el tiempo adelantando o retrasando su fecha, o editar en la columna de la izquierda su fecha de inicio.
   * Se pueden añadir dependencias desde o hacia el hito.
   * Se puede borrar un hito existente.

Restricciones de las tareas
===========================

Las tareas pueden incorporar una serie de restricciones temporales que indican que una tarea :

   * debe empezar el antes posible (**TAN PRONTO COMO SEA POSIBLE**)
   * no debe comenzar antes de una fecha (**COMENZAR NO ANTES DE**)
   * debe comenzar en una fecha fija (**COMENZAR EN FECHA FIJA**)
   * no debe acabar después  de (**ACABAR NO DESPUÉS DE**)
   * acabar lo más tarde posible (**TAN TARDE COMO SEA POSIBLE**)

Para incorporar estas restricciones se deben seguir los siguientes pasos:

   * Pulsar con el botón derecho sobre la tarea a que se le quiere incorporar la restricción desde la vista de planificación.
   * Seleccionar en el menú contextual *Propiedades de la tarea*
   * En la vista de propiedades seleccionar el tipo de restricción que interese.
   * En el casos de las restricciones que hacen referencia a una fecha, es
     necesario cubrir la fecha de la restricción en este punto.
   * Presionar en el icono de *Guardar* la planificación cuando se termine con las modificaciones.

La aplicación de restricciones en las tareas puede implicar que no se cumplan
una serie de dependencias. En caso de que exista alguna incompatibilidad tienen
preferencia por defecto las restricciones sobre las dependencias, pero esto es
configurable con el parametro *Las dependencias tienen prioridad*, en las propiedades generales del proyecto.

Es posible definir en la vista gráfica dependencias del tipo *COMENZAR NO ANTES
DE* si se desplaza con el ratón las tareas directamente en la vista de Gantt, y
se establecerá la fecha de la restricciones en base al punto donde se deposite.
Aún que esta operación pueda ser intuitiva y complejo ajustar el día de la restricción con niveles de zoom superiores al día.

Asignación de calendarios a tareas
==================================

Los proyectos han asociado un calendario que se tomará como referencia para el calendario de las tareas. Este calendario define los días que se trabajan en una tarea así como el número de horas por defecto por día en las asignaciones genéricas.

Es posible asociar un calendario a una tarea de la siguiente forma:

   * Pulsar con el botón derecho sobre la tarea a que se le quiere cambiar el calendario desde la vista de planificación.
   * Seleccionar en el menú contextual *Asignación de Calendario*
   * Seleccionara el calendario de interés para la tarea.
   * Presionar en la opción de asignar y guardar la planificación cuando se termine con las modificaciones.

Vista del gráfico global de carga de recursos de la empresa
===========================================================

De forma paralela la vista de recursos de un proyecto, se puede consultar la vista general de recursos de la empresa. Esta vista permite comparar la planificación de los recursos disponibles. Se puede acceder desde la vista de planificación de empresa presionando en la perspectiva de *Uso de los recursos*.

En esta vista se ve la información  de todos los recursos específicos o
genéricos que tienen alguna asignación la algún proyecto. Se muestra la carga de
todos los recursos con información de las  tareas en las que han sido asignados.
La diferencia con la vista de carga a nivel proyecto es que aquí se muestran las
asignaciones de todos los recursos de la empresa, no sólo las de los recursos
asignados al proyecto en el que se está trabajando.

En un primero nivel se muestra  el nombre del recurso y a su lado una línea
gráfica que indica la carga  del recurso en el tiempo. Si en un intervalo la
barra está en rojo, esto significa que  el  recurso se encuentra sobrecargado
por encima  del 100%, si está en naranja la carga es del 100% y si el color es
verde la carga es inferior al 100%.  Esta barra posee líneas verticales blancas
que indica los cambios de carga debido a intersecciones de tareas.

Al situarse con el puntero del ratón sobre la barra de carga, si se espera
durante unos instantes, entonces aparecen los de carga del recurso en formato numérico.

Por cada línea de recurso se puede expandir la información y consultar las
tareas y la carga que aporta cada una de ellas. Las tareas del proyecto aparecen
con la nomenclatura  *Nombre del proyecto::Nombre de la tarea*. También se
muestran tareas de otros proyectos para poder analizar las causas de las
sobrecargas debidas a ellos. Cuando la cargal la carga del recurso es debida a
una asignación genérica se indica con el nombre de los criterios usados en la
asignación entre Corchetes [].

Esta perspectiva permite conocer en detalle a situación de los recursos de la empresa.

-------------------
Módulo de progresos
-------------------

Conceptos teóricos
==================

El progreso o avance es una medida que indica en que grado está hecho un trabajo. En NavalPlan los progresos se gestionan a dos niveles:

   * *Tarea*. Una tarea representa un trabajo a ser realizado y, consecuentemente, es posible en el programa medir el progreso de ese trabajo.
   * *Proyecto*. Los proyectos de forma global también tienen un estado de progreso según el grado de completitud que tienen.

El progreso tiene que ser medido manualmente por las personas encargadas de la planificación en la empresa porque es un juicio que se lleva en base a una valoración del estado de los trabajos.

Las características más importantes del sistema de progresos en *NavalPlan* es el siguiente:

   * Es posible tener varias maneras de medir el progreso sobre una determinada tarea. Debido a ello, los progresos se pueden medir en diferentes unidades. Son administrables los distintos tipos de progresos.
   * Se programó un sistema de propagación de progresos. Según el mismo, cuando se mide a un determinado nivel del árbol de proyectos, a continuación, el sistema calcula en el nivel superior cual es el progreso en función de las horas representadas por los hijos que hayan medido ese tipo de progreso.
   * En la vista de planificación, tanto la vista a nivel de empresa como a nivel de proyecto, sobre las tareas se representan los avances como barras interiores.

Administración de tipos de progreso
===================================

La administración de tipos de progreso permite al usuario definir las distintas maneras en las que desea medir los progresos sobre las tareas y proyectos. Para dar de alta un tipo de progreso hay que llevar a cabo los siguientes pasos:

   * Ir la opción *Administración / Gestión* > *Tipos de datos* -> *Progreso*.
   * Presionar en el botón **Crear**.
   * Cubrir en el formulario que se muestra los siguientes datos:

      * *Nombre de la unidad*. Nombre del progreso por lo que se va a identificar. Normalmente será el nombre de la unidad. No puede haber dos tipos de progreso con el mismo nombre de unidad.
      * *Activo*. Es necesario marcar esta opción si el usuario quiere utilizar este tipo de progreso.
      * *Valor máximo por defecto*. Cuando el usuario introduce un tipo de progreso en una tarea tiene que seleccionar que valor representa la finalización del trabajo. Pues bien, este valor máximo por defecto es el valor que primeramente se asigna como valor que representa el 100% cuando se realiza una alta de un progreso de este tipo en una tarea.
      * *Precisión*. Indica cuál es la precisión decimal en la cual se pueden introducir las asignaciones de progreso de un determinado tipo.
      * *Porcentaje*. Si se indica que un tipo de progreso está marcado como porcentaje significa que el valor máximo va a estar predefinido al valor 100 y no se ofrecerá al usuario la posibilidad de cambiarlo cuando se asigne a una tarea.

   * Presionar en el botón *Guardar*.

Borrado de tipo de progreso
---------------------------

El borrado de un tipo de progreso sólo tiene sentido en caso de que no haya sido asignado nunca. Además, existen tipos de progreso predefinidos en *NavalPlan* necesarios para su funcionamiento. Estos tipos de progreso predefinidos tampoco se pueden borrar.

Si este es el caso hay que dar los siguientes pasos:

   * Ir la opción *Administración / Gestión* > *Tipos de datos* -> *Progreso*.
   * Identificar la fila correspondiente el tipo de progreso que se desea borrar.
   * Pulsar en el icono de la papelera.
   * Se despliega una ventana emergente en el cual se pide confirmación. Pulsar en sí.

Asignación de tipos de progresos a tareas
=========================================

Esta operación consiste en configurar la medición del progreso de un determinada tarea a través de un tipo de progreso. Para asignar un tipo de progreso a una tarea tiene que cumplirse una serie de reglas:

   * No debe existir ninguna asignación del tipo de progreso deseado en alguno de sus descendientes.
   * No debe existir ninguna asignación del tipo de progreso deseado en alguno de su ancestros.

Lo anterior quiere decir que el tipo de progreso soo puede estar asignado en otra rama del árbol, no en el recorrido que va desde la tarea hasta la raíz y desde la tarea hacia todos sus descendientes.

Para dar de alta el tipo de progreso en una tarea hay dos opciones:

**Opción 1**:
   * Ira la opción *Planificacion > Planificación de proyectos*.
   * Hacer doble click sobre el proyecto que se desea gestionar.
   * Presionar sobre la tarea que se desea con botón derecho y elegir la operación "Asignaciones de progreso".
   * En la pestaña hay una primera árela recadrada denominada **Asignación de progresos**. El usuario debe presionar el botón **Añadir nueva asignación de progreso**.
   * En ese momento se añade una nueva fila a la tabla de tipos de progreso asignados. En la columna tipo aparece un selector en el que hay que seleccionar el tipo de progreso.
   * Introducir el valor máximo para las mediciones del tipo de progreso sobre el elemento del proyecto.
   * Presionar en el botón de la parte inferior **Atrás**
   * Hacer clic con el ratón en el icono de *Guardar*, representado por un disquete, en la barra de acciones.

**Opción 2**:
   * Ira la opción *Planificacion > Proyectos*.
   * Seleccionar la fila que se corresponda con el proyecto en el cual se desea configuración un tipo de progreso para medir el progreso.
   * Presionar en el botón editar del proyecto.
   * Seleccionar la pestaña **EDT (Tareas)**
   * Identificar la tarea sobre la que se quiere configurar el tipo de progreso.
   * Presionar sobre el botón editar ta tarea.
   * Sobre la ventana emergente que aparece, seleccionar la pestaña progresos.
   * En la pestaña hay una primera área recuadrada denominada **Asignación de progresos**. El usuario debe presionar el botón **Añadir nueva asignación de progreso**.
   * En ese momento se añade una nueva fila a la tabla de tipos de progreso asignados. En la columna tipo aparece un selector en el que hay que seleccionar el tipo de progreso.
   * Introducir el valor máximo para las mediciones del tipo de progreso sobre la tarea.
   * Presionar en el botón de la parte inferior **Atrás**
   * Hacer clic con el ratón en el icono de *Guardar*, representado por un disquete, en la barra de acciones.

Añadir lectura de progreso sobre un tipo de progreso asignado en una tarea
==========================================================================

Esta operación puede ser llevada a cabo una vez que se configuró previamente una medición de tipo de progreso sobre una tarea. Partiendo de este supuesto, los pasos para añadir una lectura de progreso sobre un tipo de progreso asignado la una tarea son los siguientes:


   * **Opción 1**: Ira la opción *Planificacion > Planificación de proyectos*.
      * Hacer doble click sobre el proyecto que se desea gestionar.
      * Presionar sobre la tarea que se desea con botón derecho y elegir la operación *Asignaciones de progreso*.
   * **Opción 2**: Ir la opción *Planificacion > Proyectos*.
      * Seleccionar la fila que se corresponda con el proyecto en el cual se desea configurar un tipo de progreso para medir el progreso.
      * Presionar en el botón editar del proyecto.
      * Seleccionar la pestaña **EDT (Tareas)**
      * Identificar la tarea sobre la que se quiere configurar el tipo de progreso.
      * Presionar sobre el botón editar ta tarea.
      * Sobre la ventana emergente que aparece, seleccionar la pestaña progresos.
   * Dentro de la tabla incluida en la área recuadrada como **Asignación de progresos** hay que elegir el tipo de progreso al que se le desea asignar una medida presionando en el botón **Añadir medida**.
   * Con la pulsación anterior se añade una nueva fila en la sección inferior denominada **Medidas de progreso** y se añade al lado de la etiqueta **Medidas de progreso**, el tipo de progreso que se acaba de seleccionar. Además se cargan en la tabla de esa sección todas las lecturas de progreso que hasta ese momento se tienen del tipo de progreso seleccionado. El usuario debe cubrir en ella los datos:

      * *Valor*. Aquí debe introducir la medida de progreso en las unidades que define el tipo de progreso. El valor máximo viene determinado por la configuración de la asignación del tipo de progreso a la tarea y la preción por el valor de precisión determinado por el tipo de progreso.
      * *Fecha*. La fecha indica cuál es el día al cual corresponde esta medición de progreso.
      * *Porcentaje*. Esta columna es una columna calculada e informa de que porcentaje representa la medición de progreso considerando que la tarea rematada es un 100%.

   * Presionar en el botón **Atrás**
   * Hacer clic con el ratón en el icono de *Guardar*, representado por un disquete, en la barra de acciones.

Es importante resaltar que asignando progreso sobre una tarea concreta o sobre una caja de Gantt de una tarea correspondiente, la operación realizada es a misma.

Mostrado de la evolución de lecturas de progreso graficamente
=============================================================

Sobre la pantalla de configuración de medidas de progreso es posible ver la evolución graficamente de uno o más tipos de progreso de los que estén configurados. Para ver esta evolución lo que hay que realizar es:

   * En la pantalla de **Asignación de progresos** (ver secciones anteriores para acceder a esta ventana), seleccionar la columna *Mostrar* de cada uno de los tipos de progreso que se quieran ver graficamente.
   * Observar en la gráfica cuál es la evolución de las lecturas de los tipos de progreso seleccionados en el tiempo.

Configuración de propagación de tipo de progreso
================================================

Propagar es la operación que permite calcular el progreso en nodos superiores en base a los nodos hijos. El progreso que está marcado como *propagado* en un determinado nodo, es el que se utiliza para calcular el avance de su nodo padre, en caso de existir.


Existe una columna en la tabla de asignación de tipos de progreso a elementos de proyecto que es un botón radio. Este botón indica cual de los tipos de progreso asignados a la tarea es el que se configura como que propaga. Uno y sólo uno de los tipos de progreso asignados a un elemento de proyecto puede estar marcado como *propagado*.

El tipo de progreso configurado sobre una tarea marcada cómo que propaga va a representar a todos los tipos de progreso existentes en la tarea porque  es el utilizado para calcular cuál es el progreso de la tarea padre - en caso de tener padre -. El cálculo consiste en ponderar el progreso de cada hijo en función de la carga en horas de trabajo que cada uno aporta con respeto al total del padre.

Para configurar el tipo de progreso que propaga en una tarea hay que seguir la secuencia siguiente de acciones:

   * Ir la opción *Planificacion > Lista de proyectos*.
   * Seleccionar la fila que se corresponda con el proyecto en el cual se desea medir el progreso.
   * Presionar en el botón *Editar* del proyecto.
   * Seleccionar la pestaña **Elementos de proyecto**
   * Identificar la tarea sobre la que se quiere configurar el tipo de progreso que propaga.
   * Presionar sobre el botón *Editar* de la tarea.
   * Sobre la ventana emergente que aparece, seleccionar la pestaña progresos.
   * En la sección **Asignación de progresos** seleccionar la fila del tipo de progreso deseado y marcar el botón de radio.
   * Presionar en el botón **Atrás**
   * Hacer clic con el ratón en el icono de *Guardar*, representado por un disquete, en la barra de acciones.

Visualización de progresos generales sobre vista de planificación de proyecto
=============================================================================

En la vista de planificación de proyecto se muestran las tareas marcadas como puntos de planificación y sus ancestros, que aparecen como tareas de planificación contenedoras. Estando en esta vista, para visualizar las informaciones de progreso de las tareas hai que tener pulsado el botón de la barra de herramientas cuya imagen son dos tareas con una barra azul en su interior. Para estar seguros de la correcta identificación de este botón es el que, cuando se sitúa el puntero de ratón sobre el mismo, se muestra como texto emergente *Mostrar/Ocultar progreso*.

La información de un tipo de progreso de progreso sobre una tarea se muestra graficamente a través de una barra de color azul que se pinta en el interior de las tareas y de los contenedores. Si no existen tipos de progreso configurados no se muestra ninguna información. Esta información de progreso se muestra de la siguiente manera:

   * Se representa la medición de progreso más reciente del tipo de progreso configurado cómo *que propaga* sobre la tarea asociada a la tarea de planificación (tarea contenedora o tarea final).
   * Esta barra tiene una longitud que está relacionada con la lectura de progreso última y con la asignación de trabajo que tiene la tarea a lo largo del tiempo. El algoritmo para lo pintado es el siguiente:

      * De las horas planificadas de la tarea se calcula qué numero de horas representa el porcentaje de progreso medida más reciente sobre el total de horas.
      * Se va sumando las horas que se planifican cada día desde el comienzo de la tarea hasta que se llega a igualar o superar el número de horas calculado en el punto anterior.
      * Calcular que fecha es en la que ocurre la igualación o superación. Esa fecha hasta donde se pinta la barra de progreso.

Con este algoritmo la barra representa de forma correcta la situación en la que el número de horas dedicadas en la tarea no es constante a lo largo de toda la duración de la tarea. Si el usuario se pone sobre la tarea de planificación, sale un texto emergente que informa del porcentaje de progreso que representa la barra.

Para ver la información de progreso de un proyecto hay que acceder a la perspectiva de planificación de un proyecto.

Visualización de progresos generales sobre vista de planificación de empresa
============================================================================

Los proyectos son el nivel de agrupamiento superior, como ya se dijo, de las tareas. Se visualizan como *diagrama de Gantt* en la vista de empresa.

En esa vista de empresa si el proyecto, o bien, alguna de las tareas de su interior, tienen configurados tipos de progreso y tienen lecturas de progreso, entonces también se muestran en la vista de empresa a nivel de proyecto.

Para que se visualice la información de progreso a nivel de empresa es necesario que se tenga pulsado el botón de *Mostrar/Ocultar progreso* en la barra de botones superior. Este icono es el que tiene por representación dos tareas con una barra azul en su interior.

La representación del progreso sobre el proyecto, es la misma que lo explicado para las tareas.

-------------------------
Otros conceptos avanzados
-------------------------

Recursos limitantes
===================

Los recursos limitantes son recursos no sobreasignables que admiten la asignación de tareas únicamente a jornada completa. Ello conlleva que sólo aceptan tareas de manera secuencial. Por esta razón, el modo de funcionamiento es como si fueran colas. *NavalPlan* permite la gestión de dichas colas.

Para gestionarlas es necesario disponer de tareas asignadas con recursos limitantes, es decir, tareas de tipo *Asignación de recursos limitantes*. Para realizar una asignación a recursos limitantes para una tarea hay que seguir los siguientes pasos:

   * Acceder a la perpectiva de planificación del proyecto deseado (aquel del cual se quiere asignar una tarea como de recursos limitantes).
   * Identificar sobre el *diagrama de Gantt* la tarea que se quiere asignar a recursos limitantes.
   * Pulsar con el botón derecho del ratón sobre la tarea para desplegar su menú contextual.
   * Sobre el menú contextual seleccionar la opción *Propiedades de la tarea*.
   * Se abre una ventana emergente. En ella localizar la etiqueta *Tipo de asignación de recursos*.
   * Elegir en el selector asociado a la etiqueta anterior la opción *Asignación de recursos limitantes*.
   * Pulsar tras ello en la pestaña denominada *Asignación de recursos limitantes*.

Una vez en dicha pestaña, *NavalPlan* ofrece la posibilidad de asignar:

   * Un recurso limitante específico.
   * Uno o más criterios. En este caso sería una asignación a recursos limitantes genérica. Como advertencia, destacar que es importante asignar un criterio que se sepa satisfarán los recursos limitantes.


Lo hecho hasta aquí, no obstante, no supone la asignación de la tarea de forma definitiva. Lo que se ha hecho, es depositar la tarea en lo que se denomina en la aplicación la *cola de entrada a los recursos limitantes*. Es un espacio previo donde se sitúan las tareas limitantes para gestionar, a partir de ahí, su gestión y asignación definitiva. Para la gestión de la cola de entrada y la planificación definitiva de las tareas limitantes hay que realizar los siguientes pasos:

   * Acceder a la opción del menú *Planificación > Asignación de recursos limitantes*.
   * Aparece una pantalla en la que se muestran los recursos limitantes y su asignación temporal de tareas.
   * En la parte inferior de la pantalla se listan, también, las tareas pendientes de introducir en las colas.
   * El usuario puede elegir introducir la tarea en una cola de dos maneras. De forma automática, o bien, de forma manual.

      * Si el usuario desea realizar una asignación automática debe pulsar sobre el botón *Automático* de la columna de operaciones. El sistema, entonces, busca el mejor hueco que satisfaga las restriccións de la tarea y la asigna en ese punto.
      * Si el usuario, por el contrario, determina que requiere hacer una asignación manual debe pulsar sobre el botón *Manual*. Se abre una ventana emergente donde puede realizar la asignación de dos maneras:

         * **Apropiativamente**: Moviendo la tarea que interfiera con la introducida, moviéndola para un punto posterior.
         * **No apropiativamente**: Permitiendo añadir la tarea sólo donde hay un hueco del tamaño necesario.

Otras consideraciones a tener en cuenta son:

  * Las tareas asignadas a recursos específicos sólo se pueden asignar a la cola correspondiente al recurso configurado.
  * Las tareas asignadas a criterios podrán ser asignadas a colas de recursos que satisfacen los criterios.
  * Para afianzar los datos de las colas es necesario presionar en el icono con el disquete de *Grabar* antes de abandonar la pantalla. En caso contrario se pierden los datos de las colas asignadas.


Consolidación de progresos
==========================

A pesar de que es posible introducir progresos en el sistema, dichos progresos no se traducen en cambios en las tareas y en las asignaciones de las mismas. Sin embargo, si se consolidan los progresos introducidos, sí se produce dicho efecto. Consolidar una tarea significa asentar el progreso para una fecha dada definitivamente. Para consolidar un progreso es necesario realizar los siguientes pasos:

  * Ir a la vista de planificación del proyecto del cual se quieren consolidar progresos.
  * Presionar con el botón derecho en la tarea elegida para desplegar el menú contextual. La tarea elegida, debe tener configurado un tipo de progreso al menos y debe poseer valores de progreso medidos.
  * Elegir la opción *Consolidación de progreso*.
  * Elegir el primero de los avances a consolidar.
  * Presionar en *Aceptar*.
  * Guardar el proyecto.

Para entender la consolidación de progreso, es necesario analizar dos supuestos. Para ambos se va a partir de que se dispone de una tarea de 40 horas y un calendario de 8 horas diarias, y que la tarea cuenta con asignaciones de 8 horas durante 5 días.

   * **Ejemplo 1**: En este supuesto está introducido un progreso del 60% en el segundo día y se solicita la consolidación de esta medida:

      * *NavalPlan* busca cuantas horas se hicieron hasta el día en el que se introdujo el progreso. En el ejemplo, 16 horas correspondientes a las asignaciones de 2 días de 8 horas cada día.
      * A continuación calcula cuanto quedaría para finalizar (en el ejemplo, un 40% de la tarea). En este caso, un 40% de las 40 horas, esto es, 16 horas.
      * El sistema entonces marca cómo consolidadas las horas que calculó en el primero punto (16h) y marca cómo que quedan 16 horas calculadas en el segundo punto. En consecuencia, la tarea tiene ahora una duración de 32h.
      * *NavalPlan*, con las horas que quedan por hacer, las últimas 16h, recalcula el ratio de recursos por día que se necesitan para poder finalizar en la fecha inicialmente planificada para el fin de la tarea.
      * Posteriormente, el usuario puede establecer un ratio de recursos por día diferente para el tramo no consolidado. Con ello sí que se recalculan las fechas de finalización. En este ejemplo, si se establece 1 recurso por día lo que resta de la tarea se acabaría con 1 día de adelanto.

   * **Ejemplo 2**: Introducíuse un progreso del 40% en el cuarto día y se consolida:

      * *NavalPlan* busca cuantas horas se hicieron hasta el día en el que se introdujo el progreso. En el ejemplo, 32 horas correspondientes a las asignaciones de 4 días de 8 horas cada día.
      * El sistema, a continuación, calcula cuanto quedaría por finalizar (en el ejemplo, quedaría un 60% de la tarea). En consecuencia quedan un 60% de las 40 horas, lo cual se traduce en 24 horas.
      * Acto seguido, se marca cómo consolidadas las horas que calcularon en el primero punto (32h) y retiene que hay que reasignar 24 horas. La tarea queda, por tanto, con un número total de horas de 56h.
      * Por último, *NavalPlan*, con las últimas 24 horas recalcula el ratio de recursos por día que se necesitan para poder finalizar en la fecha inicial.
      * En este ejemplo, si se establece 1 recurso por día al que resta de la tarea se contaría con 2 día de retraso.


Escenarios
==========

Los escenarios representan diferentes entornos de trabajo. Los escenarios comparten ciertos tipos de datos que son comunes, otros son compartidos entre varios escenarios y otras son completamente diferentes en cada uno de ellos:

   * Tipos de entidades comunes: criterios, etiquetas, etc.
   * Tipos de entidades que pueden ser comunes: proyectos, tareas y la asociación de datos a los mismos.
   * Tipos de entidades independientes: asignaciones de horas

Cuando un usuario cambia de escenario, las asignaciones de horas son diferentes en los proyectos porque las condiciones pueden ser diferentes, por ejemplo, un nuevo proyecto que existe en uno nuevo escenario.

Las operaciones básicas de operación entre escenarios son:

   * Creación de escenario
   * Cambio de escenario
   * Creación de proyecto en escenario
   * Envío de proyecto de un escenario a otro. Esta operación copia toda la información de un proyecto de un escenario a otro, excepto las asignaciones de horas.

Los escenarios son gestionados desde la opción de menú *Escenarios* donde es posible administrar los escenarios existentes y crear nuevos. Por otro lado, existe un botón de acceso rápido a escenario en la zona derecha superior de *NavalPlan* en el caso de que el módulo se encuentre habilitado.

Las operaciones de escenarios sólo se muestran se se configura en la sección *Administración* > *NavalPlan: Configuración* que se muestren estas operaciones.
