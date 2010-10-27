-----------
Introdución
-----------

O obxetivo deste curso formativo é **capacitar aos futuros usuarios de NavalPlan** para que obteñan o máximo rendemento das características do software NavalPlan desenvolvido pola Fundación para o Fomento da Calidade Industrial e Desenvolvemento Tecnolóxico de Galicia para a xestión da producción no sector naval:

Os asistentes ao curso ao final da primeira sesión serán capaces de:

   * **Xestionar os recursos** humanos e máquina e as súas capacidades.
   * **Xestionar os pedidos** dos proxectos a planificar e organizar o traballo.
   * **Planificar os pedidos** no tempo incorporando dependencias e restricións temporais.
   * **Asignar recursos** á realización de tarefas para cumprir prazos e controlar a execución dos proxectos.
   * **Controlar o avance** dos proxectos e a carga dos recursos.
   * **Organizar a produción** empregando calendarios, criterios e etiquetas.

Para isto:

   * Traballarán os conceptos necesarios para a realización de planificacións con NavalPlan.
   * Coñecerán as distintas pantallas de NavalPlan e aplicarán as súas funcionalidades en casos prácticos.

Estarán en disposición de utilizar NavalPlan para **realizar planificacións reais nas súas empresas**.

Esta primeira sesión formativa ten continuación coa segunda sesión na que se tratarán outros aspectos avanzados de NavalPlan.

---------------------------------------------
Módulo de administración de entidades básicas
---------------------------------------------


Administración de criterios
===========================

Os criterios son as entidades que se utilizan na aplicación para categorizar os recursos e as tarefas. As tarefas requiren criterios e os recursos satisfán criterios. Un exemplo de utilización de criterios é a seguinte secuencia: un recurso é asignado co criterio "soldador" (é dicir, satisfai o criterio "soldador") e unha tarefa require o criterio "soldador" para ser realizada, en consecuencia ante unha asignación de recursos a tarefas os traballadores con criterio "soldador" son os que se utilicen á hora de asignar xenericamente (non aplica nas asignacións específicas).

Os criterios organízanse en tipos de criterios, os cales reúnen os criterios dunha mesma categoría.

Alta dun novo tipo de Criterio
------------------------------

Para crear un novo tipo de criterio debese seguir a seguinte secuencia de pasos:

* Ir ao Listado de Criterios, opción Administración > Tipos de datos > Xestionar Criterios
* Premer o botón *Crear*
* Cubrir o campo do Nome do tipo de Criterio, por exemplo (Profesión, Permiso, Localización)
* Indicar o tipo de recurso ao que é aplicable:

   * Resource: indica que é aplicable a calquera tipo de recurso
   * Worker: indica que é aplicable aos recursos humanos.
   * Machine: indica que é aplicable aos recursos máquina.

* Seleccionar se un tipo de criterio permite que un recurso cumpra máis de un criterio de este tipo. Se está activado para un tipo de criterio chamado profesión permitira que cada recurso pida ter simultaneamente máis dunha profesión activa no tempo, por exemplo Carpinteiro e Pintor.
* Seleccionase se o tipo de criterio é xerárquico. Isto permitirá definir unha estrutura xerárquica que pode ser útil para reflexar unha relación entre os criterios de especifidade. Un exemplo sería a localización onde se poden definir criterios con relación de pertenza Europa > España > Galicia > Santiago.
* Deixar marcado o campo de activado, xa que a súa desactivación suporía que non se poderían asignar este tipo de criterio aos recursos.
* Incorporar o campo descrición informativo sobre o tipo de criterio
* Premer na opción na parte inferior da venta de *gravar e continuar*. Procedendo a creación de criterios deste tipo de criterio.


Alta de criterios dun determinado tipo
--------------------------------------

No momento de creación dun tipo de criterio o premendo na operación de edición na vista de tipos de criterios pódense crear novos criterios.

* Asociar un novo criterio introducindo na sección de *Criterios asignados*  o nome do novo criterio. Tomarase como exemplo a creación de criterios de Tipo Profesión, no que pódense crear criterios de Soldador, Caldereiro, Carpinteiro, Pintor, Califugador, etc.
* Escribir o nome do criterio e premer no botón *Criterio novo*. O novo criterio aparecerá na táboa de criterios.
* Pódese desmarcar o botón de activo se se precisa deixar de utilizar na aplicación un criterio.
* Se o tipo de criterio estivera definido como xerárquico poderase indentar coas frechas esquerda e dereita para incorporar criterios como fillos ou especializacións do inmediatamente superior.
* Pódese crear un criterio fillo doutro só e preciso seleccionar co rato o criterio a incorporar e logo escribir o nome do novo criterio e pulsar no botón *Criterio novo*.
* Ao eliminar a opción de xerarquía do tipo de ficheiro o sistema avisará que se perderan as relacións pai fillo aplanando os criterios.
* Unha vez finalizada coa creación de criterios premer na opción de Gravar na parte inferior da páxina. É de reseñar que os cambios, por termo xeral na aplicación, so serán consolidados (gardados) cando se presione o Gardar. Se se pulsa sobre o botón Pechar perderase o traballo dende a última grabación
* Una vez gardado, pasarase ao listado de tipos de criterios onde aparecerá o novo tipo de criterio.

Outras operacións sobre criterios
---------------------------------

Será posible a realización das seguintes operacións sobre criterios:

* Desactivación/Activación de tipos de criterios
* Desactivación/Activación de criterios
* Modificación dos datos dun tipo de criterio
* Reorganización da estrutura de criterios

Máis documentación na sección da axuda da documentación de NavalPlan

Administración de etiquetas
===========================

As etiquetas son entidades que se utilizan na aplicación para a organización conceptualmente de tarefas ou elementos de pedido.

As etiquetas categorízanse segundo os tipos de etiquetas. Unha etiqueta só pertence a un tipo de etiqueta, sen embargo, nada impide crear tantas etiquetas similares que pertenzan a tipos de etiquetas diferentes.


Alta dun novo tipo de Etiqueta
------------------------------

Para crear un novo tipo de criterio debese seguir a seguinte serie de pasos:

* Ir ao Listado de Criterios, opción Administración > Tipos de datos > Tipos Etiquetas
* Premer o botón *Crear*
* Cubrir o campo do Nome do tipo de Etiqueta, por exemplo Centro de Custe, Zona de Buque, Dificultade, etc...
* Pódese premer o botón *Gardar e Continuar*  para almacenar o novo tipo creado, logo proceder a asociar etiquetas a un tipo de etiquetas.

Alta dunha nova etiqueta dun tipo
---------------------------------

No momento de creación dun tipo de etiqueta ou premendo na operación de edición na vista de tipos de etiqueta pódese crear novas etiquetas para ese tipo.

* Na sección de lista de etiquetas introducir o nome da nova etiqueta no campo de texto de *Nova Etiqueta*.
* Premer o botón de Nova etiqueta e esta aparecerá na táboa de etiquetas asociada ao tipo que se estea editando.
* Para consolidar as modificacións e as novas altas simplemente premer no botón de Gardar que volta ao listado de tipos de etiquetas.

Administración de calendarios
=============================

Os calendarios son as entidades da aplicación que determinan as capacidade de carga dos distintos recursos. Un calendario está formado  por unha serie de días anuais, onde cada día dispón de horas dispoñibles  para traballar. Os calendarios din cantas horas pode traballar un recurso ao longo do tempo.

Por exemplo, un festivo pode ter 0 horas dispoñibles e, se as horas de traballo dentro dun día laboral son 8, é este número que se asigna  como tempo dispoñible para ese día.

Existen dous modos de indicarlle ó sistema cantas horas de traballo ten un día:

    * Por día da semana. Por exemplo, os luns trabállanse 8 horas xeralmente.
    * Por excepcións. Por exemplo, o luns 30 de Xaneiro trabállanse 10  horas.

O sistema de calendarios permite que uns calendarios deriven doutros, de forma que un calendario desa forma pódense ter calendarios de distintas localizacións da empresa seguindo unha organización como a seguinte España > Galicia > Ferrol e España > Galicia > Vigo de forma que a modificación de festivos a nivel estatal modifique automaticamente os festivos a nivel dos calendarios de Galicia, Ferrol e Vigo.

Para acceder a xestión dos calendarios da empresa e preciso situarse na sección de **Administración** > **Calendarios**


Creación dun novo calendario
----------------------------

Para a creación dun novo calendario é necesario:

   * Premer no botón  "Crear" na sección de Administración de Calendarios.
   * Introducir o nome do calendario para poder identificalo.
   * O calendario creado será un calendario sen ningún dato. Veranse tódalas datas do calendario en vermello polo que ese días non teñen asignación de horas. E preciso introducir a información relativa a Semana Laboral e as Excepcións.
   * Premer na pestana de *Semana de Traballo*. Asignar a xornada de traballo por defecto por cada día da semana. Por exemplo é posible elixir 8 horas laborais de luns a venres definindo unha xornada laboral de 40 horas. Na parte dereita da pantalla poderase ver o resultado diario da asignación de xornada. Ao longo do tempo pódese ir modificando a xornada por defecto dun calendario. Isto será posible na edición ao crear novas versións do calendario.
   * Situarse na pestana de Excepción e introducir aqueles días especiais que teñen unha influencia no calendario laboral da empresa o no calendario do grupo de traballadores que se estea creando. Por exemplo, deberíanse sinalar os días festivos.
   * Seleccionar unha data no calendario, por exemplo o 19 de Marzo. Sinalar o tipo de excepción como BANK_HOLIDAY (Día de Vacacións). Finalmente indicar o número de horas a traballar que nese caso será 0. E pulsar no botón *Crear Excepción*.
   * O listado de excepcións pódese ver a dereita do formulario de creación de excepcións.
   * **A aplicacións só permite modificacións do calendario a futuro** para que non se teña influencia en planificacións pasadas.
   * É posible marcar un conxunto de datas coma excepcións, simplemente tense que marcar a data de inicio no calendario e seleccionar no campo data fin a data ata a que chegue a excepción.
   * Para borrar unha excepción no calendario pulsarase na operación de borrar no listado de excepcións.
   * Pulsar en *Gardar* e o novo calendario aparecerá no listado de calendarios.

Edición dun calendario
----------------------

Será posible modificar un calendario para incluir modificacións na xornada laboral semanal ou para modificar os días excepcionais, para iso debense seguir os seguintes pasos:

   * Pulsar no botón editar nas operacións dun calendario existente no listado da administración de calendarios.
   * Poderanse modificar ou crear novos días excepcionais a futuro segundo as instrucións previas de creación dun novo calendario.
   * Para modificar a semana laboral por defecto e preciso situarse na pestana de *Semana de Traballo*:

       * Pulsar en *crear unha nova semana de traballo*.
       * Indicar a data a partires da que esa semana entra en vigor.
       * Pulsar na opción de *Crear*.
       * Editar o valor das horas dos días laborais por cada día da semana.
       * Unha vez pulsada na opción *Gardar* do calendario se consolidaran os cambios desta nova versión. A partires da data de aplicación da nova versión o calendario comportarase desa forma.

    * Para que as modificacións teñan efecto é preciso premer no botón *Gardar* do calendario, se se pulsa no botón *Cancelar* os cambios consolidados non se almacenarán.

Copiar un calendario
--------------------

Poderase realizar unha copia dun calendario, a realización dunha copia supón que se creará un novo calendario cunha copia de tódolos datos do calendario orixinal. Este calendario poderase editar coma calquera outro calendario existente. Unicamente é preciso cambiarlle o nome para que non coincida con ningún dos existentes. A copia dun calendario non mantén ningunha relación co calendario de orixe.

Para facer unha copia seguiranse os seguintes pasos:

* Premer no botón *Crear Copia* nas operacións do calendario que se quere copiar no listado de administración.
* Modificar o nome do calendario
* Modificar os datos do noso interese se fora preciso.
* Premer no botón *Gravar*.


Creación dun calendario derivado
--------------------------------

Poderanse crear calendarios derivados de outros, un calendario derivado é unha especialización do calendario do que deriva. A aplicación normal deste tipo de calendarios e para as situacións nas que as empresa teñen diversas localizacións con múltiples calendarios laborais. Tamén se poden empregar para definir o calendario de traballadores que traballan a media xornada pero teñen os mesmos festivos que o resto da empresa. A derivación e coma crear unha copia na que os cambios no calendario orixe seguen afectando aos calendarios derivados.

Os pasos para a creación dun calendario derivado son os seguintes:

   * Pulsar no botón de crear derivado nas operacións dun calendario existente no listado da administración de calendarios.
   * Poderase ver que se indica que este calendario é derivado do orixinario na información do calendario e disponse de toda a información do calendario preexistente.
   * Pódese realizar tódalas modificacións que se desexen sobre este calendario coas seguintes diferencias:

      * Para modificar a xornada laboral debese desmarcar o campo *Por defecto* que indica que as horas laborais por día son as mesmas que no calendario do que se deriva.
      * Poderase modificar o calendario do que se deriva nas edicións do calendario, entrando en vigor a partires da data de modificación.

   * Para que as modificacións teñan efecto é preciso premer no botón *Gardar* do calendario, se se pulsa no botón *Cancelar* os cambios consolidados non se almacenarán.
   * Verase que o novo calendario derivado aparece nunha estrutura xerarquica por debaixo do calendario de orixe.

Configuración do calendario por defecto da empresa
--------------------------------------------------

Para facilitar o emprego e configuración dos calendarios na aplicación é posible configurar o calendario por defecto da empresa. Este calendario será o que apareza seleccionado inicialmente cando se cree un recurso ou se asocie un calendario a unha tarefa.

Para a súa selección séguense os seguintes pasos:

   * Entrar na sección de **Administración > NavalPlan: Configuración** do menú principal.
   * Seleccionar no campo *Calendario por defecto* o calendario desexado.
   * Premer no botón *Gardar*


------------------
Módulo de recursos
------------------

Conceptos teóricos
==================

Os recursos son as entidades que realizan os traballos necesarios para completar os pedidos. Os pedidos na planificación se representan mediante diagramas de Gantt que dispoñen no tempo as diferentes actividades.

En NavalPlan existen tres tipos de recursos capaces de realizar traballo. Estes tres tipos son:

   * Traballadores. Os traballadores son os recursos humanos da empresa.
   * Máquinas. As máquinas son capaces tamén de desenvolver tarefas e teñen existencia en NavalPlan.
   * Recursos virtuais. Os recursos virtuais son como grupos de traballadores que non teñen existencia real na empresa, é dicir, non se corresponden con traballadores reais, con nome e apelidos, da empresa.

Utilidade dos recursos virtuais
-------------------------------

Os recursos virtuais son, como se explicou, como grupos de traballadores pero que non se corresponden con traballadores concretos con nome e apelidos.

Dotouse a NavalPlan a posibilidade de usar recursos virtuais debido a dous escenario de uso:

   * Usar recursos virtuais para simular contratacións futuras por necesidades de proxectos. Pode ocorrer que para satisfacer proxectos futuros as empresas necesiten contratar traballadores nun momento futuro do tempo. Para prever e simular cantos traballadores poden necesitar os usuarios da aplicación poden usar os recursos virtuais.
   * Pode existir empresas que desexen xestionar as aplicación sen ter que levar unha xestión dos recursos con respecto os datos dos traballadores reais da empresa. Para estes casos, os usuario poden usar tamén os recursos virtuais.

Alta de recursos
================

Alta de recursos traballador
----------------------------

Para crear un traballador hai que realizar os seguintes pasos:

   * Acceder a Lista de traballadores, opción Recursos > Traballadores.
   * Premer o botón Crear
   * Cubrir os campos do formulario: Nome, Apelidos.
   * Premer o botón "Gardar" ou ben "Gardar e continuar".

A partir dese momento existirá un novo traballador en NavalPlan.

Como nota dicir que existe unha comprobación que impide a gravación de dous traballadores co mesmo nome, apelidos e NIF. Todos estes campos son, ademais, obrigatorios.

Alta de máquinas
----------------

Para crear unha máquina dar os seguintes pasos:

   * Accede a Lista de traballadores, opción Recursos > Máquinas.
   * Premer o botón Crear.
   * Cubrir os datos na pestana de datos da máquina. Os datos a cubrir son:

      * Nome. Nome da máquina
      * Código da máquina. O código da máquina ten que ser único e se xera aínda que se pode editar.
      * Descrición da máquina.

Alta de recursos virtuais
-------------------------

Para crear un recurso virtual dar os seguintes pasos:

   * Accede a Lista de traballares, opción Recursos > Grupos virtuais de traballadores.
   * Premer no botón Crear.
   * Cubrir os datos na pestana de Datos persoais. Os campos a cubrir son:

      * Nome do grupo de recursos virtual.
      * Capacidade. A capacidade significa cantos recursos forman parte do grupo. Isto implica que un recurso virtual pode traballador por día a súa capacidade multiplicada polo número de horas que traballa por día segundo o calendario.
      * Observacións.

Alta de criterios
=================

Alta de criterios en traballador
--------------------------------

Os traballadores da empresa satisfacen criterios. O feito de que cumpra un criterio significa que ten unha determinada capacidade ou ten unha determinada condición que ten relevancia para a planificación.

Os criterios se satisfacen durante un determinado período de tempo ou ben a partir dunha determinada data e de forma indefinida.

Para asignar un determinado criterio a un traballador hai que dar os seguintes pasos:

  * Acceder á opción Recursos > Traballadores.
  * Premer sobre o botón de edición sobre a fila do listado correspondente a o recurso desexado.
  * Pulsar na pestana Criterios asignados.
  * Premer no botón Engadir criterio. Isto provoca que se engada unha fila con tres columnas de datos:

     * Columna Nome do criterio. Seleccionar o criterio que se quere configurar como satisfeito polo traballador. O usuario ten que despregar ou buscar o criterio elixido.
     * Columna Data de inicio. Elixir a data dende a cal o traballador satisface o criterio. É obrigatoria e aparece por defecto cuberta coa data do día actual.
     * Columna Data de fin. Configura a data ata cal se satisface o criterio. Non é obrigatoria. Se non se enche o criterio é satisfeito sen data de caducidade.

Adicionalmente existe na pantalla un checkbox para seleccionar que criterios son visualizados, todos os satisfeitos durante toda a historia do traballador ou unicamente os vixentes na actualidade.

A asignación de criterios ríxese polas regras ditadas polo tipo de criterio do criterio que se está asignando. Así por exemplo cabe mencionar dous aspectos:

   * En criterios de calquera tipo, unha asignación de criterio non se pode solapar no tempo con outra asignación do mesmo criterio nun mesmo traballador.
   * En criterios que non permiten múltiples valores por recurso, non pode haber dúas asignacións de criterio a un traballador de maneira que o seu intervalo de validez teña algún dia común.

Os criterios que son seleccionables para para ser asignados aos traballadores son os criterios de tipo RECURSO ou de tipo TRABALLADOR.

Alta de criterios en máquina
----------------------------

Para asignar un determinado criterio a unha máquina hai que dar os seguintes pasos:

   * Acceder a opción Recursos > Máquinas.
   * Premer sobre o botón de edición sobre a fila do listado correspondente a máquina que se desexa.
   * Pulsar na pestana Criterios asignados.
   * Premer no botón Engadir criterio. Isto provoca que se engada unha fila con tres columnas de datos:

      * Columna Nome do criterio. Seleccionar o criterio que se quere configurar  como satisfeito polo traballador. O usuario ten que despregar ou buscar  o criterio elixido.
      * Columna Data de  inicio. Elixir a data dende a cal o traballador satisface o criterio. É  obrigatoria e aparece por defecto cuberta coa data do día actual.
      * Columna Data de fin. Configura a data ata cal se satisface o criterio.  Non é obrigatoria. Se non se enche o criterio é satisfeito sen data de  caducidade.

As regras de asignación de criterios son as mesmas que para os traballadores. A diferencia é que os criterios que son seleccionables para asignar as máquinas son os criterios de tipo RECURSO ou de tipo MAQUINA.

Alta de criterios en grupo de traballadores virtuais
----------------------------------------------------

A asignación de criterios para os traballadores virtuais é similar a asignación de criterios para os traballadores reais. Os pasos a dar son os seguintes:

   * Acceder a opción Recursos > Grupos virtuais de traballadores.
   * Premer sobre o botón de edición da fila do listado que se corresponda co grupo virtual de traballadores ao que se queira engadir criterios.
   * Seleccionar a pestana Criterios asignados.
   *  Premer no botón Engadir criterio. Isto provoca que se engada unha fila  con tres columnas de datos:

      * Columna Nome do criterio. Seleccionar o  criterio que se quere configurar  como satisfeito polo traballador. O  usuario ten que despregar ou buscar  o criterio elixido.
      * Columna Data de  inicio. Elixir a data dende a cal o traballador  satisface o criterio. É  obrigatoria e aparece por defecto cuberta coa  data do día actual.
      * Columna Data de  fin. Configura a data ata cal se satisface o criterio.  Non é  obrigatoria. Se non se enche o criterio é satisfeito sen data de   caducidade.

As regras para a asignación de criterios aos grupos de traballadores virtuais son as mesmas que os traballadores reais.

Asignación de calendarios a recursos
====================================

Conceptos teóricos
------------------

Os traballadores teñen un calendario propio. Sen embargo, non é un calendario que haxa que definir completamente senón que é un calendario que deriva dun dos calendarios da empresa.

O feito de derivar dun calendario significa que, senón se configura, herda completamente a definicións do calendario do cal deriva: herda a definición da semana de traballo, os días festivos, etc.

NavalPlan, sen embargo, ademais de facer que os seus recursos deriven do calendario da empresa, permite a definición de particularidades do calendario. Isto implica que as vacacións do traballador, particularidades da súa xornada de traballo como o número de horas de que consta o seu contrato de traballo, sexa contemplado na planificación.

Asignación de calendario pai a traballadores en creación de traballador
-----------------------------------------------------------------------

Na creación dun traballador se crea un calendario ao traballador que deriva, por defecto, do calendario configurado por defecto na aplicación.

A configuración da aplicación pódese consultar en Administración > NavalPlan: Configuración.

Para cambiar o calendario do cal deriva un recurso no momento da creación hai que dar os seguintes pasos:

   * Acceder a Lista de traballadores, opción Recursos > Traballadores.
   * Premer o botón Crear
   * Cubrir os campos do  formulario: Nome, Apelidos.
   * Premer na pestana Calendario
   * Nesa pestana seleccionar o no selector que aparece do cal se quere derivar.
   * Premer o botón "Gardar"  ou ben "Gardar e continuar".


Asignación de calendario pai a máquinas en creación de máquinas
---------------------------------------------------------------

As máquinas configuran o calendario do cal derivan no momento da creación de forma similar aos traballadores. Os pasos serían:

   * Acceder a Lista de máquinas, opción Recursos > Máquinas.
   * Premer o botón Crear
   * Cubrir os campos do  formulario: Nome da máquina, código e descrición.
   * Premer na pestana Calendario
   * Nesa pestana  seleccionar o no selector que aparece do cal se quere derivar.
   * Premer o botón "Gardar"  ou ben "Gardar e continuar".

Asignación de calendario pai a grupos de traballadores virtuais
---------------------------------------------------------------

Os grupos de traballadores virtuais tamén configuran o calendario pai do cal derivan de forma similar aos traballadores reais e as máquinas. Os pasos son:

   * Accede a Lista de recursos virtuais, opción Recursos > Grupos virtuais de traballadores.
   * Premer no botón Crear.
   * Cubrir os datos na pestana de Datos persoais.
   * Premer na pestana Calendario
   * Nesa pestana  seleccionar o no selector que  aparece do cal se quere derivar.
   * Premer o botón "Gardar" ou ben "Gardar e continuar".

Cambio de calendario pai a traballadores, máquinas ou grupos de traballadores virtuais
--------------------------------------------------------------------------------------

É posible cambiar o calendario pai do cal deriva un recurso calquera, xa sexa un traballador, máquina ou un grupo de traballador virtual.

Para elo hai que facer o seguinte:

   * Ir a sección correspondente: Recursos > Máquinas, Recursos > Traballadores ou Recursos > Grupos virtuais de traballadores.
   * Acceder a pestana Calendario
   * Premer no botón Borrar calendario.
   * Seleccionar o novo calendario pai do cal se quere derivar.
   * Premer o botón "Gardar" ou ben "Gardar e continuar".

Personalización de calendario de recurso traballador, máquina ou grupo de traballador virtual
---------------------------------------------------------------------------------------------

Os recursos traballador, máquina ou grupo de traballador virtual poden configurar no seu propio calendario os seguintes elementos:

   * A súa xornada semanal de traballo.
   * Excepcións de dedicación en períodos de tempo.
   * Períodos de activación.

Os dous primeiros conceptos, é dicir, a xornada semanal de traballo e as excepción de dedicación se explican na sección de **Administración de calendario xeral**

Agora ben, os calendarios dos recursos teñen unha particularidade con respecto o calendario da empresa. Esta peculiaridade son os períodos de activación.

Os períodos de activación son intervalos nos cales os traballadores se encontran dispoñibles para a planificación. Conceptualmente se corresponden con aqueles períodos nos cales o traballador se atopa contratado pola empresa. Un traballador pode ser contratado por un tempo, despois abandonar a empresa e reincorporarse posteriormente. É o mesmo traballador e, como NavalPlan, garda a historia de planificación de todos os recursos, ten que impedir que se lle asigne traballo.

No momento da creación dun traballador se configura con un período de activación que vai dende o momento da alta ata o infinito. Neste momento non é posible cambialo e esta operación ten que ser feita cunha edición posterior do recurso.

Configuración dos períodos de activación dun recurso
----------------------------------------------------

Os períodos de activación dun determinado recurso teñen que satisfacer non ter puntos de solapamento no tempo. Os pasos para configuralos son:

   * Ir a sección correspondente: Recursos > Máquinas, Recursos >  Traballadores ou Recursos > Grupos virtuais de traballadores.
   * Seleccionar a fila do recurso que se quere editar e premer no botón da fila asociada para editar.
   * Acceder a pestana de Calendario.
   * Dentro da pestana de Calendario premer na pestana interior Períodos de activación.
   * No interior da pestana sairán a lista de períodos de activación. Pulsar no botón Crear período de activación
   * Neste momento se engade unha fila coas seguintes columnas:

      * Data de inicio: A encher obrigatoriamente. Introducir a data na cal se quixera activar o recurso.
      * Data de fin: Opcional. Introducir a data no cal o traballador deixa de estar activo na empresa.
   * Premer no botón "Gardar" ou "Gardar e continuar".


-----------------
Módulo de pedidos
-----------------

Conceptos teóricos
==================

Os pedidos son as contratacións de traballo que as empresas asinan cos seus clientes. No conxunto de empresas do naval os pedidos están constituídos por un número de elementos organizados en estruturas de datos xerárquicas (árbores).

Basicamente existen dous tipos de nodos:

   * Nodos contedores. Un nodo contedor é un agregador e actúa como clasificador de elementos. Non introduce traballo por el mesmo, senón que o traballo por el representado e a suma de tódalas horas dos nodos descendentes do mesmo.
   * Nodos folla. Un nodo folla é un nodo que non ten fillos e que que está constituido por un ou máis conxuntos de horas.

En NavalPlan, por tanto, se permite o traballo con pedidos estruturados segundo os tipos de nodos precedentes.

Acceso a vista global da empresa
================================

A vista global da empresa e a pantalla inicial da empresa, a que se entra unha vez que o usuario entra na aplicación.

Nela o que se pode ver son tódolos pedidos que existen na empresa e estes son representados a través dun diagrama de Gantt. Os datos que se poden observar de cada pedido son:

   * A súa data de inicio e a súa data de fin.
   * Cal é o progreso na realización de cada pedido.
   * O número de horas que se levan feito de cada un deles.
   * Cal é a súa **data límite** en caso de que o teñan.

Ademais do anterior mostrase  na parte inferior da pantalla dúas gráficas:

   * Gráfica de carga de carga de recursos.
   * Gráfica de valor gañado.

Para acceder á vista de empresa basta con entrar na aplicación dende a páxina de introdución de usuario e contrasinal.

Creación dun pedido
===================

Para a creación dun pedido hai que acometer os seguintes pasos:

   * Acceder ao opción Planificación > Proxectos.
   * Premer no botón situado na barra de botón co texto Crear pedido novo.
   * Aparecen unha serie de pestanas. A que aparece seleccionada por defecto, a primeira delas con título Datos xerais, é a que contén os datos necesarios. Cubrir os seguintes:

      * Nome do pedido. Cadea identificativa do pedido. Obrigatorio.
      * Código do pedido. Código para identificar o pedido. Deber ser único. Non cubrilo e manter marcado o checkbox Autoxeración de código. Se este está cuberto encárgase NavalPlan de crear o código correspondente. Obrigatorio.
      * Data de comezo estimada. Esta data é a data a partir da cal se comezará a planificación do pedido. Obrigatorio.
      * Data límite. Este campo é opcional e indica cal é a data límite (*deadline*).
      * Responsable. Campo de texto para indicar a persoa responsable. Informativo e opcional.
      * Cliente. Campo para seleccionar cales dos clientes da empresa é o contratista do pedido.
      * Descrición. Campo para describir de que vai o pedido ou poñer calquera nota.
      * As dependencias teñen prioridade. Campo relacionado coa planificación que indica quen manda se as restricións que teñen as tarefas ou os movementos ordenados polas dependencias.
      * Calendario asignado. Os pedidos teñen un calendario que dita cando se traballa neles. Hai que seleccionar o calendario que se quere utilizar.
      * Presuposto. Desglose do que se presupostou o pedido en dúas cantidades:

         * Traballo. Cantidade polo que se presupostou a man de obra do pedido.
         * Materiais. Cantidade polo que se presupostaron os materiais do pedido.
      * Estado. Un pedido pode estar en varios estados ao longo da súa existencia. Os ofrecidos son:

         * Ofertado
         * Aceptado
         * Empezado
         * Finalizado
         * Cancelado
         * Subcontratado
         * Pasado a histórico.
   * Pulsa no botón Gardar representado por un disco de ordenador na barra.

Se os datos introducidos son correctos o sistema proporciona nunha ventá emerxente o resultado da operación.

Edición dun pedido
==================

Para a edición dun pedido existen varios camiños posibles:

   * Opción 1:
      * Ir a opción Planificación > Proxectos
      * Premer sobre o icono de edición, lapis sobre folla de papel, que se corresponda co pedido desexado.
   * Opción 2:
      * Ir a Planificación > Planificación de proxectos.
      * Facer dobre click co botón esquerdo do rato sobre a tarefa que representa o pedido na vista da empresa ou ben pulsar co botón dereito sobre a tarefa e despois escoller a opción Planificar.
      * Pulsar o icono da parte esquerda Detalles de pedido.

Introdución de elementos de pedido con horas e nome
====================================================

Para introducir os elementos de pedido, contedores ou elementos de pedido folla, hai que dar os seguintes pasos:

   * Ir a opción Planificación > Proxectos.
   * Premer sobre o icono de edición, lapis  sobre folla de papel, que se corresponda co pedido desexado.
   * Seleccionar a pestana "Elementos de pedido"
   * Unha vez aquí, introducir na liña de edición situada encima da táboa de lista de elementos de pedido os seguinte valores:

      * No campo de nome unha identificación do elemento de pedido.
      * No campo horas un número enteiro que represente o número de horas de que se compón o traballo do elemento de pedido.

   * Premer o botón "Novo elemento de pedido"

Ao pulsar no botón anterior se engade un elemento de pedido de tipo folla e se sitúa ao final dos elementos de pedido existentes na árbores de elementos de pedido.

No caso de que se quería cambiar a posición do elemento de pedido e situalo en outro lugar da árbore deben premerse os iconos de cada fila de elemento de pedido seguintes:

   * Icono frecha arriba. Premendoo faise que o elemento de pedido ascenda na árbore de elementos de pedido.
   * Icono frecha abaixo. Pulsando nel faise que o elemento de pedido descenda na árbore de elementos de pedido.

A través do explicado ata agora o que se engaden son elementos de pedido folla, pero tamén e posible engadir elementos de pedido contedores. Para engadir elementos de pedido contedores, o usuario pode realizar varios itinerarios:

Creando elementos contedores mediante arrastrar e soltar
--------------------------------------------------------

Para poder levar a cabo esta operación e necesario dispor de ao menos dous elementos de pedido folla creados segundo o procedemento explicado no punto anterior. Partindo do suposto que ter dous elementos de pedido folla elemento E1 e elemento E2.

Os pasos a dar son os seguintes:

   * Colocarse co punteiro do rato encima do elemento E1.
   * Pulsar o botón esquerdo do rato e sen soltar arrastrar o elemento E1. Mentres se manten pulsado aparecerá un texto sobre o fondo indicando que o elemento E1 está agarrado.
   * Desprazar o rato mantendo pulsado o botón esquerdo ata situarse encima do elemento E2. Nese momento liberar o botón do rato.
   * O que ocorre neste punto é que se creará un elemento de pedido contedor que terá o nome de E2 e posuirá dous fillos cos nomes E2 Copia e E1. O elemento E2 Copia terá a carga de traballo do anterior elemento E2 e, agora, o elemento E2 conterá a suma das horas de E1 e E2 Copia.

Creando elementos contedores mediante creación de elemento con elemento folla seleccionado
------------------------------------------------------------------------------------------

Para levar a cabo esta operación é necesario dispor dun elemento de pedido folla creado, supóñase que chamado E1. A partir de aquí, os pasos para crear un contedor son:

   * Situar o punteiro do rato na fila do elemento E1 e pulsar o botón esquerdo do rato na área da fila que vai dende o comezo ata o primeiro icono que sae na fila (icono de notificación de estado de planificación que se verá máis adiante). Tras realizar esta acción a fila aparecerá seleccionada.
   * Introducir na liña de edición, situada encima da táboa da árbore de elementos de pedido, o novo elemento de pedido, con nome E2 e un numero de horas.
   * Premer no botón "Novo elemento de pedido".
   * O que ocorre neste punto é que se creará un elemento de pedido contedor que terá o nome de E2 e posuirá dous fillos  cos nomes E2 Copia e E1. O elemento E2 Copia terá a carga de traballo do  anterior elemento E2 e, agora, o elemento E2 conterá a suma das horas  de E1 e E2 Copia.

Creando elementos de pedido contedor mediante a pulsación do icono de indentación
---------------------------------------------------------------------------------

Para levar a cabo esta operación é necesario ter creados dos elementos de pedido, E1 e E2, situado E1 antes que E2. A partir de aquí levar a cabo os seguintes pasos:

   * Pulsar sobre o botón de identar cara a dereita, frecha apuntado a dereita, do elemento E2.
   * O que ocorre neste punto é que se creará un  elemento de pedido contedor que terá o nome de E2 e posuirá dous fillos   cos nomes E2 Copia e E1. O elemento E2 Copia terá a carga de traballo  do  anterior elemento E2 e, agora, o elemento E2 conterá a suma das  horas  de E1 e E2 Copia.

Desprazamento de elementos de pedido
------------------------------------

Unha vez se ten unha estrutura de elementos de pedido contedor e elementos de pedido folla tamén se poden realizar operacións de modificación da posición dos elementos nesta estrutura.

Para realizar estas operación se dispón dos iconos situados na parte dereita de cada fila correspondente a un elemento de pedido. Os botóns de operación son:

   * Icono frecha arriba. Permite o desprazamento cara arriba dun elemento de pedido dentro de todos os seus elementos de pedido irmáns, é dicir, que posúan o mesmo pai.
   * Icono frecha abaixo. Permite o desprazamento cara abaixo dun elemento de pedido dentro de todos os seus elementos de pedido irmáns, é dicir, que posúan o mesmo pai.
   * Icono frecha esquerda. Permite desindentar un elemento de pedido. Isto supón subilo na xerarquía e poñelo ao mesmo nivel que o seu pai actual. Só está activado nos elementos de pedido que teñen un pai, é dicir, que non son raíz.
   * Icono frecha dereita. Permite indentar un elemento de pedido. Isto supón baixalo na xerarquía e poñelo ao mesmo nivel que os fillos dos seu irmán situado encima del. Só está permitida esta operación nos elementos de pedido que teñen un irmán por enriba del.

Puntos de planificación
=======================

Conceptos teóricos
------------------

Unha vez os pedidos está introducidos cun conxunto de horas o seguinte paso e determinar como se planifican.

NavalPlan é flexible para determinar a granularidade do que se quere planificar e para elo introduce o concepto de puntos de planificación. Isto permite aos usuario ter flexibilidade á hora de decidir se un pedido interesa planificalo con moito detalle ou ben se interesa xestionalo máis globalmente.

Os puntos de planificación son marcas que se realizan sobre as árbores de elementos de pedido dun pedido para indicar a que nivel se desexa planificar. Se se marca un elemento de pedido como punto de planificación significa que se vai a crear unha tarefa de planificación a ese nivel que agrupa o traballo de tódolos elementos de pedido situados por debaixo del. Se este punto de planificación se corresponde cun elemento de pedido que non é raíz ademais o que se fai e que os elementos de pedido por encima del se converten en tarefas contedoras en planificación.

Un elemento de pedido pode estar en 3 estados de planificación tendo en conta os puntos de planificación:

   * **Totalmente planificado**. Significa que o traballo que él representa está totalmente incluído na planificación. Pode darse este estado en tres casos:

      * Que sexa punto de planificación.
      * Que se atope por debaixo dun punto de planificación. Neste caso o seu traballo xa se atopa integrado polo punto de planificación pai del.
      * Que non haxa ningún punto de planificación por encima del pero que para todo o traballo que representa haxa un punto de planificación por debaixo del que o cubra.

   * **Sen planificar**. Significa que para o traballo que él representa non hai ningún punto de planificación que recolla parte do seu traballo para ser planificado. Isto ocorre cando non é punto de planificación e non hai ningún punto de planificación por encima ou por debaixo del na xerarquía.

   * **Parcialmente planificado**. Significa que parte do seu traballo está planificado e outra parte aínda non se incluíu na planificación. Este caso ocorre cando o elemento de pedido non é punto de planificación, non hai ningún elemento de pedido por encima del na xerarquía que sexa punto de planificación e, ademais, existen descendentes do mesmo que se son puntos de planificación pero hai outros descendentes que están en estado sen planificar.

Así mesmo un pedido terá un estado de planificación referido a tódolos seus elementos de pedido e será o seguinte:

   * Un pedido atópase en estado totalmente planificado se todos os seus elementos de pedido se atopan en estado totalmente planificado.
   * Un pedido atópase sen planificar se todos os seus elementos de pedido se atopan en estado sen planificar.
   * Un pedido atópase parcialmente planificado se hai algún elemento de pedido que está en estado sen planificar.

Borrar elementos de pedido
--------------------------

Para borrar elementos de pedidos existe un icono que representa unha papeleira sobre cada fila que representa un elemento de pedido de pedido. Por tanto, para borrar hai que:

   * Identificar a fila que se corresponde co elemento de pedido que se desexa eliminar.
   * Premer co botón de esquerdo do rato sobre o icono da papeleira. Neste momento o sistema procede a borrar tanto o elemento de pedido como tódolos seus descendentes.
   * Pulsar no icono de Gardar, disquete na barra superior, para confirmar o borrado.

Creación de puntos de planificación
-----------------------------------

Para a creación de puntos de planificación hai que realizar os seguintes pasos:

   * Ir a opción Planificación > Proxectos.
   * Identificar a fila que se corresponde co pedido que se quere editar e que ten que ter elementos de pedido. Premer o botón Editar, lapis sobre folla de papel, e pulsalo.
   * Seleccionar a pestana Elementos de pedido.
   * Identificar sobre a árbore a que nivel se desexa planificar cada parte e, unha vez decidido, onde se desexa crear unha tarefa de planificación pulsar co rato sobre un icono que representa un diagrama de gantt de dúas tarefas. Isto converte o elemento de pedido en punto de planificación, pon en verde tódolos elementos totalmente planificados e se marcará a fila do punto de planificación e as súas descendentes cunha cunha N.
   * Pulsar no icono de Gardar, disquete na barra superior, para confirmar o borrado.

Para desmarcar punto de planificación e planificar a outro nivel facer o seguinte:

   * Identificar sobre a árbore de elementos de pedidos aquel que estaba marcado como punto de planificación e que se desexa cambiar.
   * Premer sobre o icono que representa un diagrama de gantt cunha aspa X vermella. Tras elo, se quita como elemento de planificación e se actualiza o estado de planificación do seus descendentes e antecesores.
   * Pulsar no icono de Gardar, disquete na barra superior, para confirmar o borrado.

Criterios en elementos de pedido
================================

Conceptos teóricos
------------------

Os elementos de pedido representa o traballo que hai que planificar e tamén poden esixir o cumprimento de criterios. O feito de que un elemento de pedido esixa un criterio significa que se determina que para a realización do traballo que ten asociado o elemento de pedido é apropiado que o recurso que se planifique satisfaga ese criterio.

Os criterios cando se aplican a un determinado elemento de pedido se propagan realmente a todos os seus descendentes. Isto significa que se un criterio e esixido a un determinado nivel na árbore de elementos de pedido, pasa a ser a esixido tamén por tódolos elementos de pedido fillos.

Por tanto, un criterio pode ser esixido de dúas formas nun elemento de pedido:

   * De forma directa. Neste caso o criterio é configurado como requirido no elemento de pedido polo usuario.
   * De forma indirecta. O criterio é requirido no elemento de pedido por herdanza debido a que ese criterio é requirido nun elemento de pedido pai.

Os criterio indirectos dun elemento de pedido poden ser invalidados, é dicir, configurados como non aplicados nun determinado elemento de pedido descendente do primeiro. Se un criterio indirecto é invalidado nun determinado elemento de pedido, entón invalídase en tódolos descendentes do elemento que se está configurando como invalidado.

Introdución de criterio nun elemento de pedido folla
-----------------------------------------------------

Para dar de alta un criterio nun elemento de pedido folla hai que dar os seguintes pasos:

   * Ir a opción Planificación > Proxectos
   * Identificar sobre a lista de pedidos o pedido co cal se quere traballar.
   * Pulsar no botón editar do pedido folla desexado.
   * Seleccionar a pestana **Elementos de pedido**
   * Identificar o elemento de pedido folla ao cal se desexa configurar os criterios.
   * Premer no botón editar do elemento de pedido. Isto abre unha ventá emerxente.
   * Sobre a ventá emerxente seleccionar a pestana **Criterio requirido**
   * Pulsa no botón **Engadir** na primeira sección denominada **Criterios asignados requiridos**
   * Neste momento se engade unha fila na cal na primeira columna, **Nome do criterio**, se inclúe un compoñente de procura de criterios. Pulsar co botón esquerda do rato sobre este compoñente de procura e comezar a teclear o nome do criterio ou tipo de criterio do cal se quere engadir o criterio.
   * Seleccionar sobre o conxunto de criterios que encaixan coa clave de procura tecleada polo usuario aquel en concreto que se quere requirir ao elemento de pedido.
   * Pulsar en **Atrás**.
   * Premer sobre o icono de gardar representado por un disquete da barra de operación situada na parte superior.

Introdución de criterio nun elemento de pedido contedor
--------------------------------------------------------

Para dar de alta un criterio nun elemento de pedido contedor hai que dar os seguintes pasos:

   * Ir a opción Planificación > Proxectos
   * Identificar sobre a lista de pedidos o pedido co cal se quere traballar.
   * Pulsar no botón editar do pedido desexado.
   * Seleccionar a pestana **Elementos de pedido**
   * Identificar o elemento de pedido contedor ao cal se desexa configurar os criterios.
   * Premer no botón editar do elemento de pedido. Isto abre unha ventá emerxente.
   * Sobre a ventá emerxente seleccionar a pestana **Criterio requirido**
   * Pulsa no botón **Engadir** na primeira sección denominada **Criterios asignados requiridos**
   * Neste momento se engade unha fila na cal na primeira columna, **Nome do criterio**, se inclúe un compoñente de procura de criterios. Pulsar co botón esquerda do rato sobre este compoñente de procura e comezar a teclear o nome do criterio ou tipo de criterio do cal se quere engadir o criterio.
   * Seleccionar sobre o conxunto de criterios que encaixan coa clave de procura tecleada polo usuario aquel en concreto que se quere requirir ao elemento de pedido.
   * Pulsar en **Atrás**.
   * Premer sobre o icono de gardar representado por un disquete da barra de operación situada na parte superior.

Para comprobar como se engade o criterio sobre todos os elementos fillos descendentes do elemento de pedido contedor ao cal se lle requiriu o criterio dar os seguintes pasos:

   * Identificar sobre a árbore de elementos de pedido do pedido sobre o que se está a traballar un elemento de pedido fillo do elemento de pedido contedor que require un criterio.
   * Pulsar sobre o botón de edición do elemento de pedido identificado no punto anterior.
   * Sobre a ventá emerxente seleccionar a pestana **Criterio requirido**
   * Na sección da parte superior da ventá titulada **Criterios asignados requiridos** observarase o criterio requirido buscar o nome do criterio requirido polo elemento de pedido pai. Aparecerá mostrado como **Indirecto** na columna de tipo.

Invalidar un requirimento de criterio nun elemento de pedido
------------------------------------------------------------

Para levar a cabo a operación descrita neste epígrafe hai que ter unha situación ao menos dun elemento de pedido contedor E1 que teña dentro un elemento de pedido E2 e o elemento de pedido E1 teña requirido un criterio C1.

Baixo esta premisa, para invalidar o criterio C1 no elemento E2 hai que efectuar os seguintes pasos:

   * Identificar sobre a árbore de elementos de pedido o elemento E2.
   * Pulsar sobre o icono de edición da fila correspondente a E2.
   * Ir a pestana *Criterios requirido*.
   * Identificar na táboa da sección **Criterios asignados requiridos** o criterio C1 que ten que aparece co tipo **Indirecto**
   * Premer no botón invalidar.
   * Pulsar en **Atrás**.
   * Premer sobre o icono de gardar representado por un disquete da barra de operacións situada na parte superior.

Borrar un requirimento de criterio nun elemento de pedido
---------------------------------------------------------

Os requirimentos que se poden borrar son unicamente os criterios directos, xa que os criterios indirectos unicamente se poden invalidar. Os pasos que hai que dar para invalidar un criterio directos son os seguintes:

   * Ir a opción Planificación > Proxectos
   * Identificar sobre a lista de pedidos o pedido co cal se quere traballar.
   * Pulsar no botón editar do pedido desexado.
   * Seleccionar a pestana **Elementos de pedido**
   * Identificar o elemento de pedido que ten un criterio directo e ao cal se desexa borrar un criterio directo.
   * Premer no botón editar do elemento de pedido. Isto abre unha ventá emerxente.
   * Sobre a ventá emerxente seleccionar a pestana **Criterio requirido**.
   * Identificar na táboa da sección **Criterios asignados requiridos** o criterio directo que se desexa borrar.
   * Premer no icono de borrar da fila correspondente.
   * Pulsar no botón **Atrás**
   * Premer sobre o icono de gardar representado por un disquete na barra de operacións situada na parte superior.

Xestión de requirimentos a nivel de pedido
------------------------------------------

A tódolos efectos un pedidos actúa como un elemento de pedido contedor que engloba tódolos elementos de pedido raíces. Por tanto, no referente aos criterios tódolos criterios que se asignen ao pedido serán herdados como criterios indirectos en todos os elementos de pedido.

Como se deduce tamén, un pedido non pode recibir criterios indirectos xa que é a raíz da árbore dos seus elementos de pedido.

Os pasos para acceder a xestión dos criterios a nivel de pedido son os seguintes:

   * Ir a opción Planificación > Proxectos.
   * Identificar sobre a lista de pedidos o pedido sobre o cal se quere traballar.
   * Premer no botón editar do pedido.
   * Seleccionar a pestana *Criterio requirido*
   * Acceder a sección **Criterios asignados requiridos** onde se poden xestionar a adición de criterio directos e o borrado dos existentes como o explicado nos elementos de pedido.
   * Premer sobre o icono de gardar representado por un disquete na barra de operacións situada na parte superior.

-----------------------
Módulo de planificación
-----------------------

Para comprender as principais funcionalidades de planificación da aplicación é preciso acceder a sección **Planificación > Planificación de proxectos**. Navaplan permite consultar a informacións de planificación da empresa en dous niveis:

   * Nivel Empresa: pódese consultar a información de tódolos pedidos en curso.
   * Nivel Pedido: pódese consultar a información de tódalas tarefas dun pedido.

Dende a vista de empresa é posible navegar ao detalle dun pedido facendo dobre click na caixa do diagrama de gantt que representa o pedido ou pulsando co botón dereito para abrir o menú contextual seleccionando planificar.

Para volver a vista de empresa tense que pulsar no menú principal en **Planificación > Planificación de proxectos** ou en **INCIO** na ruta que mostra a información que se estea visualizando.

A vista de empresa xa detallada previamente é a pantalla principal da aplicación para o seguimento da situación dos proxectos da empresa.

Perspectivas: vista de recursos, pedidos e asignación avanzada
==============================================================

Tanto a vista de empresa coma a de nivel pedidos permiten a visualización de diferentes perspectivas da información. As perspectivas permiten cambiar o punto de vista dende o que se consulta a información de planificación Recursos, Tarefas ou Temporal.

Dentro de cada nivel Empresa ou Pedido é posible cambiar dunha perspectiva pulsando nos iconos que se mostran na parte esquerda da vista de planificación.

Na **vista da empresa** existen tres perspectivas dispoñibles:

   * Planificación de proxectos: mostra a visión dos pedidos no tempo cunha representación dun diagrama de Gantt, nesta vista aparecen tódolos pedidos planificados coa súa date de inicio e fin. Graficamente se pode ver en cada caixa o grado de avance, o número de horas traballadas no pedido e as datas límites de entrega.
   * Carga de recursos: mostra a visión dos recursos da empresa no tempo, representando nun gráfico de liñas do tempo a carga de traballo dos recursos co detalle das tarefas as que están asignados.
   * Proxectos: mostra o listado dos pedidos existentes coa súa información de datas, presuposto, horas e estado e permite acceder a edición dos detalles do pedido.

Na **vista de pedido** existen catro perspectivas dispoñibles:

   * Planificación de proxectos: mostra a visión das tarefas do pedido no tempo cunha representación de diagrama de Gantt, nesta vista pode consultarse a información das datas de inicio e fin, a estrutura xerarquica das tarefas, os avances, as horas imputadas, as dependencias de tarefas, os fitos e as datas límite das tarefas.
   * Carga de recursos: mostra a visión dos recursos asignados ao pedido no tempo coa súa carga de traballo tanto en tarefas deste pedido coma as pertencentes a outros pedidos por asignacións xenéricas ou específicas.
   * Detalles do proxecto: permite acceder a toda a información do pedido, organización do traballo, asignación de criterios, materiais, etc. Xa foi tratada dentro da edición de pedidos.
   * Asignación avanzada: mostra a asignación numérica con diversos niveles de granularidade (dia,semana,mes) dos recursos nas tarefas do proxecto. Permite modificar as asignacións de recursos no tempo as distintas tarefas do mesmo.

Vista de planificación de empresa
=================================

A vista de planificación de empresa mostra no tempo os pedidos en curso. Os pedidos represéntanse mediantes un diagrama de Gantt que indica as datas de inicio e fin dos pedidos mediante a visualización dunha caixa nun eixo temporal.

A vista de planificación dispón dunha barra de ferramentas na parte superior que permite realizar as seguintes operacións:

   * Impresión da planificación: Xera un ficheiro PDF ou unha imaxe en PNG co gráfico da planificación.
   * Nivel de zoom: permite modificar a escala temporal na que se mostra a información. Pódese seleccionar a granularidade a distintos niveis: día, semana, mes, trimestre, ano.
   * Mostrar/Ocultar etiquetas: oculta ou mostra no diagrama de gantt as etiquetas asociadas a cada un dos pedidos.
   * Mostrar/Ocultar asignacións: oculta ou mostra no diagrama de gantt os recursos asignados a cada un dos pedidos.
   * Filtrado de etiquetas e criterios: permite seleccionar pedidos en base a que cumpran criterios ou teñan asociadas etiquetas.
   * Filtrado por intervalo de datas: permite seleccionar datas de inicio e fin para o filtrado.
   * Selector de filtrado en subelementos: realiza as procuras anteriores incluindo os elementos e tarefas que forman o pedido. E non unicamente as etiquetas e criterios asociadas ao primeiro nivel do pedido.
   * Acción de Filtrado: executa a procura en base aos parámetros definidos anteriormente.

Na parte esquerda están os cambios de perspectivas a nivel de empresa que permitirá ir a sección de Carga global de recursos e Lista de pedidos. A perspectiva que se estea visualizando e a Planificación.

Na parte inferior mostrase a información da carga dos recursos no tempo así como as gráficas referentes ao valor gañado que serán explicadas máis adiante.

Vista de planificación de pedido
================================

Para acceder a vista de planificación dun pedido é preciso facer dobre click na representación do do diagrama de Gantt nun pedido, ou cambiar a perspectiva de planificación dende a perspectiva de detalle de pedidos.

Nesta vista poderase acceder as accións de definición de dependencias entre tarefas e asignación de recursos.

A vista de planificación de pedido dispón dunha barra de ferramentas na parte superior que permite realizar as seguintes operacións:

   * Gravar planificación: consolida na base de datos tódolos cambios realizados sobre a planificación e a asignación de recursos. **É importante gravar sempre os cambios unha vez terminada a elaboración da planificación**. Se se cambia de perspectiva ou se entra noutra sección perderanse os cambios.
   * Operación de reasignar: esta operación permite recalcular as asignacións de recursos nas tarefas do pedido.
   * Nivel de zoom: permite modificar a escala temporal na que se mostra a  información. Pódese seleccionar a granularidade a distintos niveis: día,  semana, mes, trimestre, ano.
   * Resaltar camiño crítico: mostra o camiño crítico do pedido, realiza o calculo daquelas tarefas que a sua demora implicará unha entrega fora de tempo do pedido.
   * Mostrar/Ocultar  etiquetas: oculta ou mostra no diagrama de gantt as etiquetas asociadas a  cada un dos pedidos.
   * Mostrar/Ocultar asignacións: oculta ou mostra  no diagrama de gantt os recursos asignados a cada un dos pedidos.
   * Expandir tarefas folla: mostra tódalas tarefas de último nivel expandindo tódolos niveis da arbore de tarefas.
   * Filtrado de  etiquetas e criterios: permite seleccionar pedidos en base a que cumpran  criterios ou teñan asociadas etiquetas.
   * Filtrado por intervalo  de datas: permite seleccionar datas de inicio e fin para o filtrado.
   * Filtrado por nome: permite indicar o nome da tarefa
   * Acción de  Filtrado: executa a procura en base aos parámetros definidos  anteriormente.

Xusto enriba da barra de tarefas atopase o nome do pedido que esta detrás do texto INICIO > Planificación > Planificación de proxectos > NOME DO PEDIDO.

Se o pedido se atopa totalmente planificado aparecera a dereita do nome unha letra C (Completamente Planificado), pero se non están marcados tódolos puntos de planificación do pedido mostrarse unha letra P (Parcialmente Planificado). Só se mostrará a letra C cando tódolos elementos de pedido na edición do pedido se atopen por debaixo dun punto de planificación.

Na vista de planificación de pedido pódese observar que as tarefas organízanse de forma xerarquica, de forma que pódense expandir e comprimir as tarefas.

Na parte inferior mostrase a información da carga dos recursos no tempo así como as gráficas referentes ao valor gañado que serán explicadas máis adiante.

Na vista de planificación dun pedido pódese facer as seguintes operacións de interese:

   * Definición de dependencias entre tarefas.
   * Definición de retriccións de tarefas.
   * Asignación de recursos a tarefas

Asignación de dependencias
--------------------------

Unha dependencia é una relación entre dúas tarefas pola cal unha tarefa A non pode comezar ou terminar ata que unha tarefa B comece ou remate. Navalplan implementa as seguintes relación de dependencias entre tarefas entre dúas tarefas chamadas A e B.

   * Fin - Inicio: A tarefa B non pode comezar ata que a tarefa A remate. Esta e a relación de dependencia máis común.
   * Inicio - Inicio: A tarefa B non pode comezar ata que a tarefa A teña comezado.
   * Fin - Fin: A tarefa B non pode terminar ata que a tarefa A teña rematado.

Para engadir unha dependencia procedese da seguinte forma:

   * Marcar a tarefa que se quere que xere a dependencia. A tarefa da que se depende para que a dependencia sexa cumprida.
   * Premer o botón dereito do rato sobre a tarefa e no menú contextual seleccionase a opción Engadir Dependencia.
   * Mostrarase unha frecha que seguirá o punteiro do rato.
   * Seleccionar facendo click co rato a tarefa dependente, a que recibe a dependencia. Unha vez seleccionada crearase unha dependencia Fin-Inicio entre as dúas tarefas.
   * Para modificar o tipo de dependencia é preciso pulsar o botón dereito do rato na frecha da dependencia e seleccionar no menú contextual o tipo de dependencia como Fin - Inicio, Fin-Fin ou Inicio-Inicio.
   * No momento de crear a dependencia o planificador recalculará a posición temporal das tarefas segundo as dependencias. Alertará no caso de que se produza un ciclo de dependencias indicando que a súa creación non é posible.
   * Recordar que é preciso pulsar no icono de gravar para consolidar os cambios na planificación.

Asignación de recursos
======================

A asignación de recursos é unha das partes máis importantes da aplicación. A asignación de recursos pode realizarse de dous xeitos  diferentes:

   * Asignacións específicas.
   * Asignacións xenéricas.

Cada unha das asignacións é explicada nas seguintes seccións.

Para realizar calquera das dúas asignacións de recursos é necesario  dar os seguintes pasos:

   * Acceder á planificación dun pedido.
   * Premer co botón dereito sobre a tarefa que se desexa asignar na opción de asignación de recursos.
   * A aplicación amosa unha pantalla na que se pode  visualizar a seguinte información.

       * Listado de criterios que deben ser satisfeitos. Por cada grupo de horas, amósase un listado de grupos de horas e cada grupo  de horas esixe o seu listado de criterios.
       * Información da tarefa: data de inicio e data de fin  da tarefa.
       * Tipo de cálculo: O sistema permite elixir a  estratexia que se desexa levar a cabo para calcular as asignacións:
       * Calcular número de horas: Calcula o número de horas que faría falla  que adicasen os recursos asignados dados unha data de fin e un número de  recursos por día.
       * Calcular data fin: Calcula a data de fin da tarefa a partir dos  número de recursos da tarefa e das horas totais adicar para rematar a  tarefa.
       * Calcular número de recursos: Calcula o número de recursos necesarios  para rematar a tarefa en unha data específica e adicando unha serie de  horas coñecidas.
       * Asignación recomendada: Opción que lle permite á  aplicación recoller os criterios que deben ser satisfeitos e as horas  totais de cada grupo de horas e fai unha proposta de asignación xenérica  recomendada. Se había unha asignación previa, o sistema elimina dita  asignación substituíndoa pola nova.
       * Asignacións: Listado de asignacións realizadas.  Neste listado poderanse ver as asignacións xenéricas (o nome sería a  lista de criterios satisfeita, horas e número de recursos por día). Cada  asignación realizada pode ser borrada explicitamente premendo no botón  de borrar.

   * Seleccionar a opción de "Procura de recursos".
   * A aplicación amosa unha nova pantalla formada por unha árbore de criterios e un listado á dereita dos traballadores que cumpren os criterios seleccionados:
   * Seleccionar o tipo de asignación a realizar:

       * Asignación específica. Ver sección "Asignación específica" para  coñecer que significa elixir esta opción.
       * Asignación xenérica. Ver sección "Asignación xenérica para coñecer  que significa elixir esta opción.

   * Seleccionar unha lista de criterios (asignación xenérica) ou unha lista de traballadores (asignación específica). A elección múltiple realízase premendo no botón "Crtl" á hora de pulsar en cada traballador ou criterio.
   * Premer no botón "Seleccionar". É  importante ter en conta que, se non se marca asignación xenérica, é  necesario escoller un traballador ou máquina para poder realizar unha  asignación, en caso contrario chega con elixir un ou varios criterios.
   * A aplicación amosa no listado de asignacións da  pantalla orixinal de asignación de recursos a lista de criterios ou  recursos seleccionados.
   * Cubrir as horas ou o número de recursos por día dependendo da estratexia de asignación que se solicitou levar a cabo á aplicación.
   * Premer no botón Aceptar para marca a asignación como feita. É importante reseñar que a operación non será consolidada ata que se pulse no icono de gravar da vista de planificación, se se sae da vista de planificación perderanse os cambios.
   * O planificador calculará a nova duración das tarefas en base a asignación realizada.


Asignación de recursos específicos
==================================

A asignación específica é aquela asignación de un recurso de xeito concreto e específico a unha tarefa de un proxecto, é dicir, o usuario  da aplicación está decidindo que "nome e apelidos" ou qué "máquina" concreta debe ser asignada a unha tarefa.

A aplicación, cando un recurso é asignado especificamente, crea asignacións diarias en relación á porcentaxe de recurso diario que se elixiu para asignación, contrastando previamente co calendario dispoñible do recurso. Exemplo: unha asignación de 0.5 recursos para  unha tarefa de 32 horas fai que se asignen ó recurso específico  (supoñendo un calendario laboral de 8 horas diarias) 4 horas diarias para realizar a tarefa.

Para realizar a asignación a un recurso específico é preciso centrarse nos seguintes pasos na pestana de asignación de recursos dunha tarefa.

   * Pulsar na opción de *Busca de recursos*
   * Marcar asignación específica coma tipo de asignación.
   * Filtrar os recursos empregando os criterios que cumpre.
   * Seleccionar un recurso ou varios (empregando Ctrl+Selección co rato).
   * Premer no botón Seleccionar.
   * Na vista xeral de asignación indicar a carga de traballo diaria de cada recurso ou o número de horas asignadas. Este campo dependerá do tipo de calculo seleccionado na asignación.
   * Premer Aplicar ou Aplicar cambios da pestana.
   * Una vez completada a asignación gravar a planificación do pedido e consultar a carga dos recursos asignados.

Asignación de recursos xenérica
===============================

A  asignación xenérica e unha das aportacións de máis interese da aplicación. Nunha parte importante dos traballos non é interesante coñecer a priori quen vai a realizar as tarefas dun pedido. Nese caso ó único que interesa para realizar unha asignación e identificar os criterios que teñen que cumprir os recursos que poden facer esa tarefa. O concepto de asignación xenérica representa a asignación por criterios en lugar de por persoas. O sistema será o encargado de realizar a asignación entre os recursos que cumpran os criterios necesarios. O sistema fará unha asignación totalmente arbitraria pero que será válida a efectos de coñecer a carga xeral dos recursos da empresa.

A asignación de recursos a unha tarefa segue o calendario definido para o pedido tendo en conta o número de recursos asignados que cumpran os criterios definidos.

Para realizar a asignación a  un recurso xenérico so é preciso centrarse nos seguintes pasos na  pestana de asignación de recursos dunha tarefa.

   * Pulsar na opción de  *Busca de recursos*
   * Marcar asignación  xenérica coma tipo de asignación.
   * Seleccionar un ou varios criterios (empregando Ctrl+Selección co rato).
   * Premer no botón Seleccionar.
   * Na vista xeral de  asignación indicar a carga de traballo diaria para a asignación xenérica ou o  número de horas asignadas. Este campo dependerá do tipo de calculo  seleccionado na asignación.
   * Premer Aplicar ou  Aplicar cambios da pestana.
   * Una vez completada a  asignación gravar a planificación do pedido e consultar a carga dos  recursos asignados.

Cando se fai unha asignación xenérica non de ten o control sobre que recursos se asigna a carga de traballo. O sistema fará un reparto sobrecargando equitativamente aos recursos se fora necesario se non existe capacidade suficiente nese momento do tempo dos recursos que cumpren os criterios da tarefa.

Asignación recomendada
----------------------

Na vista de asignación e posible marcar a **Asignación recomendada**. Esta opción permite á aplicación recoller os criterios que deben ser satisfeitos e as horas totais de cada grupo de horas e fai unha proposta de asignación xenérica recomendada. Isto garante que as horas a asignar coinciden coas horas orzamentadas así como o seu reparto por criterios.

Se había  unha asignación previa, o sistema elimina dita asignación substituíndoa pola nova. A asignación que se realiza será sempre unha asignación xenérica sobre os criterios existentes no pedido.

Revisión de asignación na pantalla de carga de recursos
=======================================================

No momento de contar con recursos asignados a tarefas dun pedido ten sentido consultar a carga  que teñen os recursos asignados. Para iso contase coa segunda  perspectiva denominada carga de recursos.

Nesta vista vese a información dos recursos específicos ou xenéricos asignados ao proxecto así coma a carga, coa información das  tarefas as que teñen sido asignados os mesmos.

Nun primeiro nivel mostrase  o nome do recurso e ao seu carón mostrase unha liña gráfica que indica a  carga do recurso no tempo. Se nun intervalo a barra está en vermello  o  recurso se atopa sobrecargado por riba do 100%, en laranxa se a carga  está ao 100% e en verde se a carga é inferior ao 100%.  Esta barra marca  con liñas verticais brancas os cambios de asignacións de tarefas.

Ao posicionarse co punteiro  rato por riba da barra e esperar uns segundos aparecerán o detalles da  carga do recurso en formato numérico.

Por cada liña de recurso pódese expandir a  información e consultar as tarefas e a carga que supón cada unha delas.  Pódense identificar as tarefas do pedido xa que aparecen coa  nomenclatura Nome do Pedido: :Nome da tarefa. Tamén se mostran tarefas  doutros pedidos para poder analizar as causas das sobrecargas dos  pedidos. Cando a carga e debida nun recurso específico é debida a unha asignación xenérica mostrase a tarefa cos nome dos criterios entre Corchetes.

Esta  perspectiva permite coñecer en detalle a situación dos recursos con  respecto as tarefas do pedido.

Revisión de asignacións na pantalla de asignación avanzada
===========================================================

Una vez se está consultando  a información dun pedido se este pedido ten asignacións pódese acceder  a perspectiva de vista de asignación avanzada. Nesta vista vese o  pedido coma unha táboa que mostra tarefas e recursos asignados a mesma  ao longo do tempo. Sendo filas as tarefas e cada asignación a un recurso  recursos un subelemento da fila. E sendo as columnas as unidades de  tempo dependendo do nivel definido de Zoom.

Nesta vista pódese cotexar o  resultado da asignación diaria de cada unha das asignacións específicas  feitas previamente. Existen dous modos de acceder á asignación  avanzada:

   * Accedendo a un pedido  concreto e cambiar de perspectiva para  asignación avanzada. Neste caso  amosaranse todas as tarefas do pedido e  os recursos asignados (tanto  específicos como xenéricos).
   * Accedendo á asignación  de recursos e premendo no botón "Asignación  avanzada". Neste caso  amosaranse as asignacións da tarefa para a que se  está asignando  recursos (amósanse tanto as xenéricas como as específicas).

Pódese  acceder ó nivel de  zoom que desexe:

   * Se o  zoom elixido é un zoom superior a día. Se o usuario modifica o  valor  de horas asignado á semana, mes, cuadrimestre ou semestre, o  sistema  reparte as horas de xeito lineal durante todos os días do  período  elixido.
   * Se o zoom elixido é un zoom de día. Se o usuario  modifica o valor de  horas asignado ó día, estas horas só aplican ó  día. Deste xeito o  usuario pode decidir cantas horas se asignan  diariamente ós recursos da tarefa.

   Para  consolidar os cambios da asignación avanzada é preciso premer o botón de  gravar. É importante que o total de horas coincida co total de horas asignadas a un intervalo temporal.

Na pantalla de asignación avanzada é posible realizar asignacións en base a funcións:
   * Función lineal por tramos. Calcula tramos lineais en base a unha serie de puntos dadots polos pares: punto que marca un momento na tarefa, porcentaxe de avance esperado.
   * Función de interpolación polinómica. Función que en base a unha serie de puntos dados polos pares (punto que marca un momento na tarefa, porcentaxe de avance esperado) calcula o polinomio que satisfai a curva.

Creación de fitos
=================

Na planificación dun proxecto poden existir fitos, os fitos considéranse coma tarefas que non teñen traballo asociado, polo que non poden ter asignacións. A principal utilidade dos fitos como pode ser o de fin de proxecto, unha auditoría ou un punto de control e establecer dependencias entre tarefas dunha forma cómoda.

Dende a vista de planificación dun pedido pódese crear un fito seguindo os seguintes pasos:

   * Seleccionar unha tarefa para marcar a posición gráfica onde se quere crear o fito.
   * Pulsar co botón dereito sobre a tarefa e seleccionar sobre o menú contextual *Engadir fito*
   * Crearase un fito xusto debaixo da tarefa seleccionada.
   * Pódese desprazar o fito no tempo adiantando ou demorando a súa data, ou editar na columna da esquerda a súa data de inicio.
   * Pódense engadir dependencias dende ou cara ao fito.
   * Pódese borrar un fito existente.

Restricións das tarefas
=======================

As tarefas poden incorporar unha serie de restricións temporais as que indican que unha tarefa :

   * debe empezar o antes posible (TAN PRONTO COMO SEA POSIBLE)
   * non debe comezar antes dunha data (COMEZAR NON ANTES DE)
   * debe comezar nunha data fixa (COMEZAR EN DATA FIXA)

Para incorporar estas restricións débense seguir os seguintes pasos:

   * Pulsar co botón dereito sobre a tarefa a que se lle quere incorporar a restrición dende a vista de planificación.
   * Seleccionar no menú contextual *Propiedades da tarefa*
   * Na vista de propiedades seleccionar o tipo de restrición que interese. No casos das restricións que fan referencia a unha data debese cubrir a data da restrición neste punto.
   * Premer na opción de aceptar e gardar a planificación cando se termine coas modificacións.

A aplicación de restricións nas tarefas pode implicar que non se cumpran unha serie de dependencias, no caso de que exista algunha incompatibilidade terá preferencia por defecto as restricións sobre as dependencias, pero isto será configurable co parámetro *As dependencias teñen prioridade* nas propiedades xerais do pedido.

É posible definir na vista gráfica dependencias do tipo COMENZAR NON ANTES DE se se despraza co rato as tarefas directamente na vista de Gantt, e establecerase a data da restricións en base ao punto onde se deposite. Aínda que esta operación poida ser intuitiva e complexo axustar o día da restrición con niveis de zoom superiores ao día.

Asignación de calendarios a tarefas
===================================

Os pedidos teñen asociado un calendario que se tomará como referencia para o calendario das tarefas. Este calendario define os días que se traballan nunha tarefa así coma o número de horas por defecto por día nas asignacións xenéricas.

É posible asociar un calendario a unha tarefa da seguinte forma:

   * Pulsar co botón dereito sobre a tarefa a que se lle quere cambiar o calendario dende a vista de planificación.
   * Seleccionar no menú contextual *Asignación de Calendario*
   * Seleccionase o calendario de interese para a tarefa.
   * Premer na opción de asignar e gardar a planificación cando se termine coas modificacións.

Vista do gráfico global de carga de recursos da empresa
=======================================================

De forma paralela a vista de recursos dun pedido, pódese consultar a vista xeral de recursos de tódala a empresa. Esta vista permite cotexar a planificación dos recursos dispoñibles. Pódese acceder dende a vista de planificación de empresa premendo na perspectiva de *Carga global de recursos*.

Nesta vista vese a  información de tódolos recursos específicos ou xenéricos que teñen algunha asignación a algún proxecto. Mostrase a carga dos mesmos coa información das  tarefas as que teñen sido asignados. A diferencia da vista de carga a nivel pedido aquí móstranse tódalas asignacións de tódolos recursos da empresa.

Nun primeiro nivel mostrase  o nome do recurso e ao seu carón mostrase unha liña gráfica que indica a  carga do recurso no tempo. Se nun intervalo a barra está en vermello  o  recurso se atopa sobrecargado por riba do 100%, en laranxa se a carga  está ao 100% e en verde se a carga é inferior ao 100%.  Esta barra marca  con liñas verticais brancas os cambios de asignacións de tarefas.

Ao situarse co punteiro rato por riba da barra e esperar uns segundos aparecerá o detalles da  carga do recurso en formato numérico.

Por cada liña de recurso pódese expandir a  información e consultar as tarefas e a carga que supón cada unha delas.  Pódense identificar as tarefas do pedido xa que aparecen coa  nomenclatura Nome do Pedido: :Nome da tarefa. Tamén se mostran tarefas  doutros pedidos para poder analizar as causas das sobrecargas dos  pedidos. Cando a carga e debida nun recurso específico é debida a unha asignación xenérica mostrase a tarefa cos nome dos criterios entre Corchetes.

Esta  perspectiva permite coñecer en detalle a situación dos recursos da empresa.

-----------------
Módulo de Avances
-----------------

Conceptos teóricos
==================

O avance ou progreso é unha medida que indica en que grao está feito un traballo. En NavalPlan os avances se xestionan a dous niveis:

   * Elemento de pedido. Un elemento de pedido representa un traballo a ser realizado e, consecuentemente, é posible no programa medir o progreso dese traballo.
   * Pedido, equivalencia de proxecto. Os pedidos de forma global tamén teñen un estado de progreso segundo o grao de finalización que teñen.

O progreso ten que ser medido manualmente polas persoas encargadas da planificación na empresa porque é un xuízo que se leva en base a unha valoración do estado dos traballos.

As características máis importantes do sistema de avances en NavalPlan é o seguinte:

   * É posible ter varias maneiras de medir o avance sobre unha determinada tarefa. Debido a elo, os avances se caracterizan por ser medidos en diferentes unidades e son administrables os distintos tipos de avances.
   * Programouse un sistema de propagación de avances de maneira que se un avance se mide a un determinado nivel da árbore de pedidos, entón se calcula no nivel superior automaticamente cal debería ser o avance en función das horas representadas polos fillos que teñan medido ese tipo de avance.
   * Na vista de planificación, tanto a vista a nivel de empresa como a nivel de pedido, sobre as tarefas que representan os puntos de planificación como os contedores das mesmas teñen a capacidade de representar graficamente un dos avances da tarefa.

Administración de tipos de avance
=================================

A administración de tipos de avance permite ao usuario definir as distintas maneiras nas que desexa medir os avances sobre os elementos de pedido e pedidos. Para dar de alta un tipo de avance hai que levar a cabo os seguintes pasos:

   * Ir a opción Administración > Tipos de datos > Avances.
   * Premer no botón **Crear**.
   * Cubrir no formulario que se mostra os seguintes datos:

      * Nome da unidade. Nome do avance polo que se vai a identificar. Normalmente será o nome da unidade. Non pode haber dous tipos de avance co mesmo nome de unidade.
      * Activo. É necesario marcar esta opción se o usuario quere utilizar este tipo de avance.
      * Valor máximo por defecto. Cando o usuario introduce un tipo de avance nun elemento de pedido ten que seleccionar que valor representa a finalización do traballo. Pois ben, este valor máximo por defecto é o valor que primeiramente se asigna como valor que representa o 100% cando se realiza unha alta dun avance deste tipo nun elemento de pedido.
      * Precisión. A precisión indica cal é a precisión decimal na cal se poden introducir as asignacións de avance dun determinado tipo.
      * Porcentaxe. Se se indica que un tipo de avance está marcado como porcentaxe significa que o valor máximo vai a estar predefinido ao valor 100 e non se ofrecerá ao usuario a posibilidade de cambialo cando se asigne a un elemento de pedido.

   * Premer no botón Gardar.

Borrado de tipo de avance
-------------------------

O borrado dun tipo de avance só ten sentido no caso de que non fora asignado nunca. Ademais, existen tipos de avance predefinidos en NavalPlan necesarios para o seu funcionamento. Estes tipos de avance predefinidos tampouco se poden borrar.

Se este é o caso hai que dar os seguintes pasos:

   * Ir a opción Administración > Tipos de datos > Avances.
   * Identificar a fila correspondente o tipo de avance que se desexa borrar.
   * Pulsar no icono da papeleira.
   * Se desprega unha ventá emerxente no cal se pide confirmación. Pulsar en Si.

Asignación de tipos de avances a elementos de pedido
====================================================

Esta operación consiste en configurar a medición do progreso dun determinado elemento de pedido a través dun tipo de avance. Para asignar un tipo de avance a un elemento de pedido ten que cumprirse unha serie de regras:

   * Non debe existir ningunha asignación do tipo de avance desexado nalgún dos seus descendentes.
   * Non debe existir ningunha asignación do tipo de avance desexado nalgún dos seu ancestros.

O anterior quere dicir que o tipo de avance so pode estar asignado en outra rama da árbore, non no recorrido que vai dende o elemento de pedido ata a raíz e dende o elemento de pedido cara tódolos seus descendentes.

Para dar de alta o tipo de avance nun elemento de pedido hai que dar os seguintes pasos:

   * Ira a opción Planificación > Proxectos.
   * Seleccionar a fila que se corresponda co pedido no cal se desexa configuración un tipo de avance para medir o progreso.
   * Premer no botón editar do pedido.
   * Seleccionar a pestana **Elementos de pedido**
   * Identificar o elemento de pedido sobre o que se quere configurar o tipo de avance.
   * Premer sobre o botón editar do elemento de pedido.
   * Sobre a ventá emerxente que aparece, seleccionar a pestana Avances.
   * Na pestana hai unha primeira área recadrada denominada **Asignación de avances**. O usuario debe premer o botón **Engadir nova asignación de avance**.
   * Nese momento se engade unha nova fila a táboa de tipos de avance asignados. Na columna tipo aparece un selector no que hai que seleccionar o tipo de avance.
   * Introducir o valor máximo para as medicións dese tipo de avance sobre o elemento do pedido.
   * Premer no botón da parte inferior **Atrás**
   * Facer clic co rato no icono de gardar, representado por un disquete, na barra de accións.

Engadir lectura de avance sobre un tipo de avance asignado nun elemento de pedido
=================================================================================

Esta operación pode ser levada a cabo unha vez que se configurou previamente unha medición de tipo de avance sobre un elemento de pedido. Existen dúas formas de engadir avance sobre unha tarefa ou elemento de pedido. A primeira opción é:

   * Ir a opción Planificación > Proxectos.
   * Seleccionar a fila que se corresponda co pedido no cal se desexa configuración un tipo de avance para medir o progreso.
   * Premer no botón editar do pedido.
   * Seleccionar a pestana **Elementos de pedido**
   * Identificar o elemento de pedido sobre o que se quere configurar o tipo de avance.
   * Premer sobre o botón editar do elemento de pedido.
   * Sobre a ventá emerxente que aparece, seleccionar a pestana Avances.
   * Dentro da táboa incluida na área recadrada como **Asignación de avances** seleccionar, facendo unha pulsación co botón esquerdo do rato, o tipo de avance do cal se quere facer unha medida.
   * Coa pulsación anterior se habilita a sección inferior denominada **Medidas de avance** e se escribe ao lado do título **Medidas de avance** o tipo de avance que se acaba de seleccionar. Ademais se cargan na táboa desa sección todas as lecturas de avance que ata ese momento se teñen do tipo de avance seleccionado.
   * Pulsar no botón *Engadir nova medición de avance*
   * Nese momento se engade unha nova fila na táboa inferior de medicións de avance. O usuario debe cubrir nela os datos:

      * Valor. Aquí debe introducir a medida de avance nas unidades que define o tipo de avance. O valor máximo ven determinado pola configuración da asignación do tipo de avance ao elemento de pedido e a precisión polo valor de precisión determinado polo tipo de avance.
      * Data. A data indica cal é o día ao cal corresponde esta medición de avance.
      * Porcentaxe. Esta columna é unha columna calculada e informa de que porcentaxe representa a medición de avance considerando que a tarefa rematada é un 100%.

   * Premer no botón **Atrás**
   * Facer clic co rato no icono de gardar, representado por un disquete, na barra de accións.

A segunda das opcións é:

   * Ir á opción de Planificación > Planificación de pedidos.
   * Acceder ó proxecto desexado.
   * Elexir a tarefa á que se lle desexa engadir avance.
   * Premer botón dereito sobre a elixida e seleccionar a operación "Asignación de avance".
   * Continuar co noveno paso da primeira opción.

É importante resaltar que asignando avance sobre un elemento de pedido concreto ou sobre unha caixa de Gantt dunha tarefa correspondente co anterior elemento de pedido, a operación realizara é a mesma.

Mostrado da evolución de lecturas de avance graficamente
========================================================

Sobre a pantalla de configuración de medidas de avance é posible ver a evolución graficamente de un ou máis tipos de avance configurados graficamente. Para elo o que hai que realizar é:

   * Ir a opción Planificación > Proxectos.
   * Seleccionar a fila que se corresponda co pedido no cal se desexa configuración un tipo de avance para medir o progreso.
   * Premer no botón editar do pedido.
   * Seleccionar a pestana **Elementos de pedido**
   * Identificar o elemento de pedido sobre o que se quere configurar o tipo de avance.
   * Premer sobre o botón editar do elemento de pedido.
   * Sobre a ventá emerxente que aparece, seleccionar a pestana Avances.
   * Na sección **Asignación de avances** seleccionar a columna *Mostrar* de cada un dos tipos de avance que se queiran ver graficamente.
   * Observar na gráfica cal é a evolución das lecturas dos tipos de avance seleccionados no tempo.

Configuración de propagación de tipo de avance
==============================================

Propagar é a operación que permite calcular o avance en nodos superiores en base ós nodos fillos, de modo que o tipo de avance que sexa propagado cara un pai, será o utilizado para calcular o avance de dito pai.

Existe unha columna na táboa de asignación de tipos de avance a elementos de pedido que é un botón radio que forma un conxunto con tódolos tipos de avance asignados ao elemento de pedido que se está configurando. Isto significa que é unha columna que ten que estar marcada unha delas como que propaga e non pode haber máis con este atributo.

O tipo de avance configurado sobre un elemento de pedido marcado como que propaga é o seleccionado para representar a tódolos tipos de avance existentes no elemento de pedido e será o utilizado para calcular cal é o avance do elemento de pedido pai - en caso de ter pai - en base os avances marcados como que propagan en cada un dos seus fillos. O cálculo consiste en ponderar o avance de cada fillo en función da carga en horas de traballo que cada un aporta con respecto ao total do pai.

Para configurar o tipo de avance que propaga nun elemento de pedido hai que seguir a secuencia seguinte de accións:

   * Ir a opción Planificación > Proxectos.
   * Seleccionar a fila que se corresponda co pedido no cal se desexa configuración un tipo de avance para medir o progreso.
   * Premer no botón editar do pedido.
   * Seleccionar a pestana **Elementos de pedido**
   * Identificar o elemento de pedido sobre o que se quere configurar o tipo de avance que propaga.
   * Premer sobre o botón editar do elemento de pedido.
   * Sobre a ventá emerxente que aparece, seleccionar a pestana Avances.
   * Na sección **Asignación de avances** seleccionar a fila do tipo de avance desexado e marcar o botón de radio.
   * Premer no botón **Atrás**
   * Facer clic co rato no icono de gardar, representado por un disquete, na barra de accións.

Visualización de avances xerais sobre vista de planificación de pedido
======================================================================

Na vista de planificación de pedido se mostran dos elementos de pedido marcados como puntos de planificación e dos seus antecesores, que aparecen como tarefas de planificación contedoras, a información dos tipos de avance que propagan en cada nodo. Se non existen tipos de avance configurados non se mostra ningunha información.

A información dun tipo de avance de avance sobre unha tarefa se mostra graficamente a través dunha barra de cor verde que se pinta na metade inferior das tarefas e dos contedores. Esta información de avance se mostra da seguinte maneira:

   * Represéntase a medición de avance máis recente do tipo de avance configurado como que propaga sobre o elemento de pedido asociado a tarefa de planificación (tarefa contedora ou final).
   * Esta barra ten unha lonxitude que está relacionada coa lectura de avance última e coa asignación de traballo que ten a tarefa ao longo do tempo. O algoritmo para o pintado é o seguinte:

      * Das horas planificadas da tarefa se calcula que numero de horas representa a porcentaxe de avance medida mais recente sobre o total de horas.
      * Vaise sumando as horas que se planifican cada dia dende o comezo da tarefa ata que se chega a igualar ou superar o numero de horas calculado no punto anterior.
      * Mirase que data é na que ocorre a igualación ou superación e se pinta a barra ata ese día.

Con este algoritmo a barra pintase de forma correcta cando o número de horas adicadas na tarefa non é constante ao longo de toda a duración da tarefa. Se o usuario se pon sobre a tarefa de planificación sae un texto emerxente que informa da porcentaxe de avance que representa a barra.

Para ver a información de avance dun pedido é acceder a perspectiva de planificación dun pedido.

Visualización de avances xerais sobre vista de planificación de empresa
=======================================================================

Os pedidos son o nivel de agrupamento superior, como xa se dixo, dos elementos de pedido. A vista dos pedidos da empresa en forma de diagrama de gantt se realiza na vista explicada no punto de pedidos, vista de empresa.

Pois ben, nesa vista de empresa se o pedido ou os elementos de pedido do seu interior teñen configurados tipos de avance como que propagan e teñen lecturas de avance, entón tamén se mostran na vista de empresa a nivel de pedido.

A representación do avance sobre o pedido, é a mesma que o explicado para os elementos de pedido.

--------------------------
Outros conceptos avanzados
--------------------------

Recursos limitantes
===================

Os recursos limitantes son recursos que limitan a planificación, de xeito que só aceptan tarefas de xeito secuencial. Por esta razón, o modo de funcionamento é en modo de colas. Un recurso declarado como limitante aceptará planificación de tarefas encoladas. NavalPlan permite a xestión de ditas colas.

Para xestionar ditas colas é necesario contar con tarefas de tipo "Asignación de recursos limitantes". Para conseguir tarefas deste tipo accederase ás propiedades da tarefa (dende Planificación > Planificación de proxectos) e no combo de selección "Asignación de tipos de recursos" seleccionar "Asignación de recursos limitantes".

Unha vez unha tarefa é do tipo anterior, NavalPlan ofrece a posibilidade de asignar:

   * Un recurso limitante específico.
   * Un criterio xenérico. É importante asignar un criterio que se saiba satisfarán os recursos limitantes.

Agora ben, esta asignación non fai unha asignación de horas a dito recurso, está facendo unha asignación á cola do recurso. Faría falla agora xestionar dita cola. Para realizala, é necesario seguir os seguintes pasos:

   * Premer en "Planificación > Asignación de recursos limitantes".
   * O sistema amosará a lista de colas dispoñibles.
   * O sistema amosará tamén a lista de tarefas pendentes de introducir nas colas, por exemplo, as que se asignan a recursos específicos no paso anterior ou a criterios.
   * O usuario pode elixir introducir a tarefa automáticamente nunha cola:

      * O sistema buscará o mellor oco que satisfaga as restriccións da tarefa.

   * O usuario pode elixir introducir a tarefa manualmente nun punto da cola:

      * Apropiativamente: Movendo a tarefa que interfira coa introducida, movéndoa para un punto posterior.
      * Non apropiativamente: Permitindo engadir a tarefa só onde hai un oco do tamaño necesario.

   * As tarefas asignadas a recursos só se poden asignar a colas de ditos recursos.
   * As tarefas asignadas a criterios poderán ser asignadas a colas de recursos que satisfán os criterios.
   * Para afianzar os datos das colas é necesario premer na incoa de "Gravar", en caso contrario pérdense os datos das colas asignadas.


Consolidación de avances
========================

Ainda que é posible introducir avances no sistema, ditos avances non se traducen en cambios nas tarefas e nas asignacións das mesmas. Sen embargo, consolidando os avances introducidos, sí se produce dito efecto. Consolidar unha tarefa significa asentar o avance para unha data dada definitivamente. Para consolidar un avance é necesario realizar os seguintes pasos:

   * Premer co botón dereito na tarefa elixida.
   * Elixir o primeiro dos avances a consolidar.
   * Premer en "Aceptar".
   * Gardar o proxecto.

Para interpretar o que sucede cando se consolida un avance, é necesario ver os diferentes casos con un exemplo.

   * Suposto: unha tarefa que comeza o luns dunha semana concreta e remata no venres da mesma semana, con unha duración de 40 horas de 1 recurso:
      * Consolidar avance do 20% na data da metade da tarefa.

         * O sistema busca cantas horas se asignaron ata dito día, suposto, 20 horas por tratarse da metade da tarefa
         * Se se consolida o 20% quere dicir que queda o 80% por facer, en consecuencia de 40 estimadas quedarían 32 horas por facer, pero como xa se levaban feitas 20 horas, o novo total da tarefa son 56 horas, das que 20 están consolidadas e 36 quedan por facer.
         * En consecuencia, o proxecto leva retraso e para acabalo en tempo é necesario ampliar a asignación de recursos, se é posible.

      * Consolidar avance do 80% na data da metade da tarefa.

         * O sistema busca cantas horas se asignaron ata dito día, suposto, 20 horas por tratarse da metade da tarefa
         * Se se consolida o 80% quere dicir que queda o 20% por facer, en consecuencia de 40 estimadas quedarían 8 horas por facer, pero como xa se levaban feitas 20 horas, o novo total da tarefa son 28 horas, das que 20 están consolidadas e 8 quedan por facer.
         * En consecuencia, o proxecto leva adianto pero o final de tarefa mantense onde estaba, é responsabilidade do que planifica decidir se quere adiantar o tempo de finalización da tarefa aumentando asignacións.

      * Consolidar avance do 50% na data da metade da tarefa.

         * A tarefa continúa no mesmo estado porque se consolidan 20 e quedan 20 por facer tal como se estimaba no inicio da mesma.


Escenarios
=============

Os escenarios representan diferentes entornos de traballo. Os escenarios comparten certas tipos de datos que son comúns, outras poden pertencer a varios escenarios e outras son completamente diferentes:

   * Tipos de entidades comúns: criterios, etiquetas, etc.
   * Tipos de entidades que poden ser comúns: pedidos, elementos de pedido e a asociación de datos ós mesmos.
   * Tipos de entidades independentes: asignacións de horas

Cando un usuario cambia de escenario, as asignacións de horas son diferentes entre pedidos porque as condicións poden ser diferentes, por exemplo, un novo pedido que existe nun novo escenario.

As operacións básicas de operación entre escenarios son:

   * Creación de escenario
   * Cambio de escenario
   * Creación de pedido en escenario
   * Envío de pedido de un escenario a outro. Esta operación copia toda a información de un pedido de un escenario a outro, excepto as saignacións de horas.

Os escenarios son xestionados dende a opción de menú "Escenarios" onde é posible administrar os escenarios existentes e crear novos. Por outro lado existe un botón de acceso rápido a escenario na zona dereita superior de NavalPlan.
