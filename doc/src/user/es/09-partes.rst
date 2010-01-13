Partes de trabajo
#################

.. contents::

Los partes de trabajo permiten el seguimiento de las horas que dedican los recursos existentes a las tareas en las que están planificados.

La aplicación permite configurar nuevos formularios de introducción de horas dedicadas, especificando los campos que se desea que figuren en estos modelos, así como incorporar los partes de las tareas que son realizadas por los trabajadores y hacer un seguimiento de los mismos.

Antes de poder añadir entradas con dedicación de los recursos, es necesario especificar como mínimo un tipo de parte de trabajo que defina la estructura que tienen todas las filas que se añadan en él. Pueden crearse tantos tipos de partes de trabajo en el sistema como sea necesario.

Tipos de partes de trabajo
==========================

Un parte de trabajo consta de una serie de campos comunes para todo el parte, y un conjunto de líneas de parte de trabajo con valores específicos para los campos definidos en cada una de las filas. Por ejemplo, el recurso y la tarea son comunes para todos los partes, sin embargo, puede haber campos nuevos como "incidencias", que no se deseen en todos los tipos.

Es posible configurar diferentes tipos de partes de trabajo para que una empresa diseñe sus partes dependiendo de las necesidades para cada caso:

.. figure:: images/work-report-types.png
   :scale: 40

   Tipos de partes de trabajo

La administración de los tipos de partes de trabajo permite configurar este tipo de características, así como añadir nuevos campos de texto o de etiquetas opcionales. Dentro de la primera de las pestañas de edición de los tipos de partes de trabajo se puede configurar el tipo para los atributos obligatorios (si son globales para todo el parte, o se especifican a nivel de línea), y añadir nuevos campos opcionales.

Los campos obligatorios que deben figurar en todos los partes de trabajo son los siguientes:

* Nombre y código: Campos identificativos del nombre del tipo de parte de trabajo y código del mismo.
* Fecha: Campo de fecha a la que corresponde el parte.
* Recurso: Trabajador o máquina que figura en el parte o línea de parte de trabajo.
* Elemento de pedido: Código del elemento de pedido al que imputar las horas del trabajo realizado.
* Gestión de horas: Determina la política de imputación de horas a llevar a cabo, la cual puede ser:
   * Por número de horas asignadas.
   * Por horas de comienzo y fin.
   * Por número de horas y rango de comienzo y fin (permite divergencia y tiene prioridad el número de horas).

Existe la posibilidad de añadir nuevos campos a los partes:

* Tipo de etiqueta: Es posible solicitar que se indique una etiqueta del sistema a la hora de rellenar el parte de trabajo. Por ejemplo, el tipo de etiqueta cliente si deseamos que en cada parte se introduzca el cliente para el cual se trabajó.
* Campos libres: Campos de tipo entrada de texto libre que se pueden introducir en el parte de trabajo.

.. figure:: images/work-report-type.png
   :scale: 50

   Creación de tipo de parte de trabajo con campos personalizados


Para los campos de fecha, recurso y elemento de pedido, pueden configurarse se figuran en la cabecera del parte y por lo tanto son globales al mismo, o si son añadidos en cada una de las filas.

Finalmente, pueden añadirse nuevos campos de texto adicionales o etiquetas a las existentes en el sistema, tanto para la cabecera de los partes de trabajo como en cada una de las líneas, mediante los campos de "Texto Complementario" y "Tipos de Etiquetas", respectivamente. En la pestaña de "Gestión de campos adicionales y etiquetas", el usuario puede configurar el orden en la que introducir dichos elementos en los partes de trabajo.

Listado de partes de trabajo
============================

Una vez configurados los formatos de los partes a incorporar al sistema, se pueden introducir los datos en el formulario creado según la estructura definida en el tipo de parte de trabajo correspondiente. Para hacerlo, es necesario seguir los siguientes pasos:

* Presionar en el botón "Nuevo parte de trabajo" asociado el tipo de parte que se desee del listado de tipos de partes de trabajo.
* La aplicación muestra el parte construído a partir de la configuración dada para el tipo. Ver siguiente imagen.

.. figure:: images/work-report-type.png
   :scale: 50

   Estructura del parte de trabajo a partir del tipo

* Seleccionar cada uno de los campos que se muestra para el parte:

   * Recurso: Si se eligió la cabecera, sólo se indica el recurso una vez. En caso contrario, para cada línea del parte es necesario elegir un recurso.
   * Código de la tarea: Código de la tarea a la que se está asignando el parte de trabajo. Al igual que el resto de campos, si el campo es de cabecera se introducirá el valor una vez o tantas veces como líneas del parte.
   * Fecha: Fecha del parte o de cada línea dependiendo de se la configuración es por cabecera o línea.
   * Número de horas. El número de horas de trabajo del proyecto.
   * Horas de inicio y fin. Horas de comienzo y fin de trabajo para calcular las horas de trabajo definitivas. Este campo sólo aparece en los casos de políticas de imputación de horas de "Por horas de comienzo y fin" y "Por número de horas y rango de comienzo y fin".
   * Tipo de horas: Permite elegir entre tipos de horas "Normales", "Extraordinarias", etc.

* Presionar en "Guardar" o "Guardar y Continuar".
