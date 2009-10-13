Calendarios
###########

.. contents::

Os calendarios serán os elementos do portal que determinen as dispoñibilidades para o traballo dos distintos recursos. Un calendario na aplicación estará formado por unha serie de días anuais, cada día disporá de unha serie de horas dispoñibles para o traballo.

Por exemplo, un festivo terá 0 horas dispoñibles e, se as horas de traballo dentro dun día laboral son 8, será este número o que se asignará de dispoñibilidade para ese día.

Administración de calendarios
=============================

A administración de calendarios actuará do seguinte xeito:
   * Cada día é independente entre sí e cada ano ten días diferentes, é dicir, se se marca o 8 de Decembro de 2009 como festivo eso non quere dicir que o ano 2010 xa teña o día 8 de Decembro como festivo.
   * Os días laborais márcanse en base a días da semana, é dicir, se se determina que o normal é traballar 8 horas os luns, quedarán todos os luns de todas as semanas dos diferentes anos marcados como 8 horas dispoñibles.
   * É posible marcar excepcións, é dicir, elixir un día concreto no que as horas dispoñibles sexan diferentes á regla xeral para dito día da semana.

A administración de calendarios está accesible dende as operacións de "Administración". Desde dito punto o usuario pode realizar o seguinte
   1 Crear un novo calendario dende cero.
   1 Crear un calendario derivado de outro calendario.
   1 Crear un calendario como copia de outro calendario.
   1 Editar un calendario existente.

Creación dun novo calendario
----------------------------

Para a creación dun novo calendario é necesario premer no botón "______". O sistema amosará un formulario no que o usuario poderá realizar as seguintes operacións:
   * Marcar as horas dispoñibles para cada día da semana (luns, martes, mércores, xoves, venres, sábados e domingos).
   * Seleccionar un día específico do calendario.
   * Asignar horas como excepción a un día específico do calendario.

Con estas operacións un usuario da aplicación ten a capacidade de personalizar os calendarios completamente ás súas necesidades. Para almacenar os cambios no formulario é necesario premer no botón "______".


Creación dun calendario derivado
--------------------------------

Un calendario derivado é un calendario que se crea como derivación dun existente, é dicir, herda todas as características do orixinal e ó mesmo tempo é posible modificalo para que conteña as súas particularidades.

Un exemplo de uso de calendarios derivados é a existencia dun calendario xeral para España e a creación dun derivado para só incluir os 2 festivos galegos sobre o xeral.

É importante destacar que ante calquera modificación realizada sobre o calendario orixinal o calendario derivado será directamente afectado, sempre e cando, non se definira unha actuación concreta sobre el mesmo. Por exemplo, no calendario de España inclúese un día laboral no 17 de Maio con 8 horas de traballo e no calendario galego, que se creou como derivación, o día 17 de Maio é considerado un día de 0 horas de traballo por ser festivo. Se sobre o calendario español se cambian os días da semana do 17 Maio para que as horas dispoñibles sexan 4 diarias, no galego o que sucederá é que todos os días da semana do 17 de Maio terán 4 horas dispoñibles excepto o mesmo día 17 que terá 0 horas, tal e como explícitamente se establecera antes.

Para crear un calendario derivado na aplicación, é necesario facer o seguinte:
   * Acceder ó menú de *Administración*.
   * Premer na operación de administración de calendarios.
   * Elixir un dos calendarios sobre o que se desexa realizar un derivado e premer no botón "______".
   * Unha vez realizada esta operación o sistema amosará un formulario de edición coas mesmas características que os formularios para crear calendarios dende cero, coa diferencia de que as excepcións e as horas por día da semana se propoñen en base ó calendario orixinal.
 

Creación dun calendario por copia
---------------------------------

Un calendario copiado é un calendario que se crea como copia exacta de outro existente, é dicir, que recibe todas as características do orixinal e ó mesmo tempo é posible modificalo para que conteña as súas particularidades. 

A diferencia entre copiar e derivar un calendario radica nos cambios no orixinal. No caso de copias, se o orixinal é modificado, non afectará á copia, sen embargo, cando se deriva, sí afecta ó fillo.

Un exemplo de uso de calendario por copia é o dispor de un calendario para Pontevedra e necesitar un calendario para A Coruña onde a maioría das características son as mesmas sen embargo, non se espera que os cambios nun afecten ó outro.

Para crear un calendario copiado na aplicación, é necesario facer o seguinte:
   * Acceder ó menú de *Administración*.
   * Premer na operación de administración de calendarios.
   * Elixir un dos calendarios sobre o que se desexa realizar un derivado e premer no botón "______".
   * Unha vez realizada esta operación o sistema amosará un formulario de edición coas mesmas características que os formularios para crear calendarios dende cero, coa diferencia de que as excepcións e as horas por día da semana se propoñen en base ó calendario orixinal.
 

Asignación de calendario a recursos
-----------------------------------

Un recurso poderá recibir como asignación un calendario existente ou un calendario creado específicamente para o recurso. En calquera dos casos, unha vez se asigna un calendario a un recurso, é posible realizar modificacións específicas para ese recurso.

Os casos posibles son:
   * *Creación de un calendario para o recurso a partir de cero*. Neste caso, o calendario será únicamente para o recurso e calqueira característica que se lle desexe asignar deberá ser modificada no propio calendario do recurso.
   * *Creación de un calendario para o recurso como copia de calendario existente*. O calendario recollerá dende un principio as especificidades do calendario orixinal, sen embargo, unha vez asignado, deberá ser modificado dende o propio recurso.
   * *Creación de un calendario para o recurso como derivado de calendario existente*. O calendario recollerá dende un principio as especificidades do calendario orixinal e, ó mesmo tempo, se o calendario orixinal é modificado para todos, o propio recurso recibirá esas modificacións de xeito indirecto, tal e como se comentou na sección de calendarios derivados.

Para asignar un calendario a un recurso é necesario dar os seguintes pasos:
   * Acceder á edición de recursos.
   * Seleccionar a pestana de "______".
   * A partir da pestana anterior aparecerá un formulario de edición de calendarios que permitirá:
      * Crear un calendario dende cero premendo no botón "______".
      * Crear un calendario derivado premendo no botón "______".
      * Crear un botón como copia premendo no botón "______".
   * O sistema amosará un formulario de edición de calendarios cos datos cargados en base á opción elixida no paso anterior.
   * ¿¿¿Para almacenar é necsario premer en SAve????
