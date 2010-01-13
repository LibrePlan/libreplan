Partes de traballo
##################

.. contents::

Os partes de traballo permiten o seguimento das horas que dedican os recursos existentes ás tarefas nas que están planificados.

A aplicación permite configurar novos formularios de introdución de horas dedicadas, especificando os campos que se desexa que figuren nestes modelos, así como incorporar os partes das tarefas que son realizadas polos traballadores e facer un seguimento dos mesmos.

Antes de poder engadir entradas con dedicación dos recursos, é necesario especificar como mínimo un tipo de parte de traballo o cal define a estrutura que teñen todas as filas que se engadan no mesmo. Poden crearse tantos tipos de partes de traballo no sistema como sexa necesario.

Tipos de partes de traballo
===========================

Un parte de traballo consta dunha serie de campos comúns para todo o parte, e un conxunto de liñas de parte de traballo con valores específicos para os campos definidos en cada unha das filas. Por exemplo, o recurso e a tarefa son comúns para todos os partes, sen embargo, pode haber campos novos como "incidencias", que non se desexen en todos os tipos.

É posible configurar diferentes tipos de partes de traballo para que unha empresa diseñe os seus partes dependendo das necesidades para cada caso:

.. figure:: images/work-report-types.png
   :scale: 40

   Tipos de partes de traballo

A administración dos tipos de partes de traballo permite configurar este tipo de características, así como engadir novos campos de texto ou de etiquetas opcionais. Dentro da primeira das pestanas da edición dos tipos de partes de traballo pódese configurar o tipo para os atributos obrigatorios (se son globais para todo o parte, ou se especifican a nivel de liña), e engadir novos campos opcionais.

Os campos obrigatorios que deben figurar en todos os partes de traballo son os seguintes:

* Nome e código: Campos identificativos do nome do tipo de parte de traballo e código do mesmo.
* Data: Campo de data á que corresponde o parte
* Recurso: Traballador ou máquina que figura no parte ou liña de parte de traballo.
* Elemento de pedido: Código do elemento de pedido ó que imputar as horas do traballo realizado
* Xestión de horas: Determina a política de imputación de horas a levar a cabo, a cal pode ser:
   * Por número de horas asignadas
   * Por horas de comezo e fin
   * Por número de horas e rango de comezo e fin (permite diverxencia e ten prioridade o número de horas)

Existe a posibilidade de engadir novos campos ós partes:

* Tipo de etiqueta: É posible solicitar que se indique unha etiqueta do sistema á hora de encher o parte de traballo. Por exemplo, o tipo de etiqueta cliente se desexamos que en cada parte se introduza o cliente para o que se traballou.
* Campos libres: Campos de tipo entrada de texto libre que se poden introducir no parte de traballo.

.. figure:: images/work-report-type.png
   :scale: 50

   Creación de tipo de parte de traballo con campos personalizados


Para os campos de data, recurso e elemento de pedido, poden configurarse se figuran na cabeceira do parte e polo tanto son globais ó mesmo, ou se son engadidos en cada unha das filas.

Finalmente, poden engadirse novos campos de texto adicionais ou etiquetas ás existentes no sistema, tanto para a cabeceira dos partes de traballo como en cada unha das liñas, mediante os campos de Texto Complementario e Tipos de etiquetas, respectivamente. Na pestana de Xestión de campos adicionais e etiquetas, o usuario pode configurar a orde na que introducir ditos elementos nos partes de traballo.

Listado de partes de traballo
=============================

Unha vez configurados os formatos dos partes a incorporar ó sistema, pódense introducir os datos no formulario creado segundo a estrutura definida no tipo de parte de traballo correspondente. Para facelo, é necesario seguir os seguintes pasos:

* Premer no botón 'Novo parte de traballo' asociado o tipo de parte que se desexe do listado de tipos de partes de traballo.
* A aplicación amosa o parte construído a partir da configuración dada para o tipo. Ver seguinte imaxe.

.. figure:: images/work-report-type.png
   :scale: 50

   Estrutura do parte de traballo a partir do tipo

* Seleccionar cada un dos campos que se amosa para o parte:

   * Recurso: Se se elixiu a cabeceira, só se indica o recurso unha vez. En caso contrario, para cada liña do parte é necesario elixir un recurso.
   * Código da tarefa: Código da tarefa á que se está asignando o parte de traballo. Do mesmo xeito que o resto de campos, se o campo é de cabeceira introducirase o valor unha vez ou tantas veces como liñas do parte.
   * Data: Data do parte ou de cada liña dependendo de se a configuración é por cabeceira ou liña.
   * Número de horas. O número de horas de traballo do proxecto.
   * Horas de inicio e fin. Horas de comezo e fin de traballo para calcular as horas de traballo definitivas. Este campo só aparece nos casos de políticas de imputación de horas de "Por horas de comezo e fin" e "Por número de horas e rango de comezo e fin".
   * Tipo de horas: Permite elixir entre tipos de horas "Normais", "Extraordinarias", etc.

* Premer en "Gardar" ou "Gardar e Continuar".

