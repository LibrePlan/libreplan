Informe de horas traballadas por recurso
########################################

.. contents::

Propósito
=========

Este informe permite extraer unha lista de tarefas e tempo dedicado por parte dos recursos da organización nun período de tempo. Hai varios filtros que permiten configurar a consulta para obter a información desexada e evitar datos superfluos.

Datos de entrada e filtros
==========================

   * **Datas**.
      * *Tipo*: Opcional.
      * *Dous campos de data*:
         * *Data de inicio.* É a data mínima dos partes de traballo que se desexan. Os partes de traballo con data inferior á *data de inicio* ignóranse. Se non se especifica este parámetro, non hai data inferior de filtrado.

         * *Data de fin.* É a data máxima de partir de traballo que se incorporarán nos resultados do informe. Os partes de traballo cunha data posterior que a *data de fin* óbvianse. Se non se cobre o parámetro, non existe data tope para os partes de traballo a seleccionar.

   * **Filtrado por traballadores**
      * *Tipo*: Opcional.
      * *Como funciona:* Pódese seleccionar un traballador para restrinxir o conxunto de partes de traballo a aqueles correspondentes ao traballador seleccionado. Se se deixa en branco, selecciónanse os partes de traballo de forma independente ao traballador ao que pertencen.

   * **Filtrado por etiquetas**
      * *Tipo:* Opcional.
      * *Como funciona:* Pódese seleccionar unha ou varias etiquetas a través do compoñente de interfaz para a súa procura e pulsando no botón *Engadir* para incorporalas ao filtro. Úsanse para seleccionar as tarefas que serán incluídas nos resultados do informe.

   * **Filtrado por criterio**
      * *Tipo:* Opcional.
      * *Como funciona:* Pódese seleccionar un ou varios criterios a través do compoñente de procura e, despois, mediante o pulsado do botón de *Engadir*. Estes criterios úsanse para seleccionar os recursos que satisfagan polo menos un deles. O informe terá en conta o tempo dedicado dos recursos que satisfagan polo menos un dos criterios engadidos en leste filtro.

Saída
=====

Cabeceira
---------

Na cabeceira do informe indícase que filtros foi configurados e aplicados para a extracción do informe á que corresponde unha cabeceira concreta.

Pé de páxina
-------------

Include a data na que o reporte sacouse.

Corpo
-----

O corpo do informe contén os seguintes grupos de información:

* Hai un primeiro nivel de agregación de información por recurso. Todo o tempo dedicado por un recurso móstrase xunto debaixo da cabeceira. Cada recurso identifícase por:

   * *Traballador*: Apelidos, Nomee
   * *Máquina*: Nome.

Móstrase unha liña de resumo co total das  horas traballadas por un recurso.

* Hai un segundo nivel de agrupamiento consistente na *data*. Todos os partes de traballo dun recurso concreto no mesmo día móstrase de forma conxunta.

Hai unha liña de resumo co total das horas traballadas por recurso.

* Hai un terceiro e último nivel no cal se listan os partes de traballo do mesmo día dun traballador. A información que se mostra para cada liña de parte de traballo desta agrupación é:

   * *Código de tarefa* ao que as horas reportadas imputan tempo.
   * *Nome da tarefa* ao que as horas reportadas imputan.
   * *Hora de inicio*. Non é obrigatorio. É a hora de inicio á que o recurso empezou a realizar o traballo da tarefa.
   * *Hora de fin*- É opcional. É a hora de fin até a cal o recurso traballou na tarefa na data especificada.
   * *Campos de texto*. É opcional. Se o tipo de parte de traballo ten campos de texto enchidos con valores, estes valores se muestrann nesta columna segundo o formato: <Nome do campo de texto>:<Valor>
   * *Etiquetas*. Contén valor dependendo de se o tipo de parte de traballo contén polo menos un campo de etiquetas na súa definición. Se hai varias etiquetas móstranse na mesma columna. O formato é: <Nome do tipo de etiqueta>.<Valor da etiqueta asociada>.

