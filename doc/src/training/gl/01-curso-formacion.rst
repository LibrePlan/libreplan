-----------
Introdución
-----------

O obxetivo deste curso formativo é **capacitar aos futuros usuarios de NavalPlan** para que obteñan o máximo rendemento das características do software NavalPlan desenvolvido pola Fundación para o Fomento da Calidade Industrial e Desenvolvemento Tecnolóxico de Galicia para a xestión da producción no sector naval.

Os asistentes ao curso ao final da primeira sesión serán capaces de:

   * **Xestionar os recursos** humanos e máquina e as súas capacidades.
   * **Xestionar os proxectos** dos proxectos a planificar e organizar o traballo.
   * **Planificar os proxectos** no tempo incorporando dependencias e restricións temporais.
   * **Asignar recursos** á realización de tarefas para cumprir prazos e controlar a execución dos proxectos.
   * **Controlar o progreso** dos proxectos e a carga dos recursos.
   * **Organizar a produción** empregando calendarios, criterios e etiquetas.

Para isto:

   * Traballarán os conceptos necesarios para a realización de planificacións con NavalPlan.
   * Coñecerán as distintas pantallas de NavalPlan e aplicarán as súas funcionalidades en casos prácticos.

Estarán en disposición de utilizar NavalPlan para **realizar planificacións reais nas súas empresas**.

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

* Ir ao Listado de Criterios, opción Administración / Xestión / Xestión > Tipos de Datos > Criterios
* Premer o botón *Crear*
* Cubrir o campo do Nome do tipo de Criterio, por exemplo (Profesión, Permiso, Localización)
* Indicar o tipo de recurso ao que é aplicable:

   * TRABALLADOR: indica que é aplicable aos recursos humanos.
   * MÁQUINA: indica que é aplicable aos recursos máquina.

* Seleccionar se un tipo de criterio permite que un recurso cumpra máis de un criterio de este tipo. Se está activado para un tipo de criterio chamado profesión permitira que cada recurso pida ter simultaneamente máis dunha profesión activa no tempo, por exemplo Carpinteiro e Pintor.
* Seleccionase se o tipo de criterio é xerárquico. Isto permitirá definir unha estrutura xerárquica que pode ser útil para reflexar unha relación entre os criterios de especifidade. Un exemplo sería a localización onde se poden definir criterios con relación de pertenza Europa > España > Galicia > Santiago.
* Deixar marcado o campo de activado, xa que a súa desactivación suporía que non se poderían asignar este tipo de criterio aos recursos.
* Incorporar o campo descrición informativo sobre o tipo de criterio
* Premer na opción na parte inferior da venta de *gardar e continuar*. Procedendo a creación de criterios deste tipo de criterio.


Alta de criterios dun determinado tipo
--------------------------------------

No momento de creación dun tipo de criterio o premendo na operación de edición na vista de tipos de criterios pódense crear novos criterios.

* Asociar un novo criterio introducindo na sección de *Criterios do tipo seleccionado* o nome do novo criterio. Tomarase como exemplo a creación de criterios de Tipo Profesión, no que pódense crear criterios de Soldador, Caldereiro, Carpinteiro, Pintor, Califugador, etc.
* Escribir o nome do criterio e premer no botón *Criterio novo*. O novo criterio aparecerá na táboa de criterios.
* Pódese desmarcar o botón de activo se se precisa deixar de utilizar na aplicación un criterio.
* Se o tipo de criterio estivera definido como xerárquico poderase indentar coas frechas esquerda e dereita para incorporar criterios como fillos ou especializacións do inmediatamente superior.
* Pódese crear un criterio fillo doutro. Só e preciso seleccionar co rato o criterio a incorporar, e logo escribir o nome do novo criterio e pulsar no botón *Engadir*.
* Ao eliminar a opción de xerarquía do tipo de ficheiro o sistema avisará que se perderan as relacións pai fillo aplanando os criterios.
* Unha vez finalizada coa creación de criterios premer na opción de Gardar na parte inferior da páxina. É de reseñar que os cambios, por termo xeral na aplicación, so serán consolidados (gardados) cando se presione o Gardar. Se se pulsa sobre o botón Pechar perderase o traballo dende a última grabación
* Una vez gardado, pasarase ao listado de tipos de criterios onde aparecerá o novo tipo de criterio.

Outras operacións sobre criterios
---------------------------------

Sera posible a realización das seguintes operacións sobre criterios:

* Desactivación/Activación de tipos de criterios
* Desactivación/Activación de criterios
* Modificación dos datos dun tipo de criterio
* Reorganización da estrutura de criterios

Máis documentación na sección da axuda da documentación de NavalPlan

Administración de etiquetas
===========================

As etiquetas son entidades que se utilizan na aplicación para a organización conceptualmente de tarefas ou elementos de proxecto.

As etiquetas categorizanse segundo os tipos de etiquetas. Unha etiqueta só pertence a un tipo de etiqueta, sen embargo, nada impide crear tantas etiquetas similares que pertenzan a tipos de etiquetas diferentes.


Alta dun novo tipo de Etiqueta
------------------------------

Para crear un novo tipo de criterio debese seguir a seguinte serie de pasos:

* Ir ao Listado de Etiquetas, opción Administración / Xestión > Tipos de datos > Etiquetas
* Premer o botón *Crear*
* Cubrir o campo do Nome do tipo de Etiqueta, por exemplo Centro de Coste, Zona de Buque, Dificultade, etc...
* Pódese premer o botón *Gardar e Continuar*  para almacenar o novo tipo creado, logo proceder a asociar etiquetas a un tipo de etiquetas.

Alta dunha nova etiqueta dun tipo
---------------------------------

No momento de creación dun tipo de etiqueta ou premendo na operación de edición na vista de tipos de etiqueta pódese crear novas etiquetas para ese tipo.

* Na sección de lista de etiquetas introducir o nome da nova etiqueta no campo de texto de *Nova Etiqueta*.
* Premer o botón de Nova etiqueta e esta aparecerá na táboa de etiquetas asociada ao tipo que se estea editando.
* Para consolidar as modificacións e as novas altas simplemente premer no botón de Gardar que volta ao listado de tipos de etiquetas.

Administración de calendarios
=============================

Os calendarios son as entidades da aplicación que determinan as capacidade de carga dos distintos recursos. Un calendario está formado por unha serie de días anuais, onde cada día dispón de horas dispoñibles para traballar. Os calendarios indican cantas horas pode traballar un recurso ao longo do tempo.

Por exemplo, un festivo pode ter 0 horas dispoñibles e, se as horas de traballo dentro dun día laboral son 8, é este número que se asigna como tempo dispoñible para ese día.

Existen dous modos de indicarlle ó sistema cantas horas de traballo ten un día:

    * Por día da semana. Por exemplo, os luns trabállanse 8 horas xeralmente.
    * Por excepcións. Por exemplo, o luns 30 de Xaneiro trabállanse 10 horas.

O sistema de calendarios permite que uns calendarios deriven doutros, de forma que un calendario desa forma pódense ter calendarios de distintas localizacións da empresa seguindo unha organización como a seguinte España > Galicia > Ferrol e España > Galicia > Vigo de forma que a modificación de festivos a nivel estatal modifique automáticamente os festivos a nivel dos calendarios de Galicia, Ferrol e Vigo.

Para acceder a xestión dos calendarios da empresa e preciso situarse na sección de **Administración / Xestión** > **Calendarios**


Creación dun novo calendario
----------------------------

Para a creación dun novo calendario é necesario:

   * Premer no botón  "Crear" na sección de Calendarios.
   * Introducir o nome do calendario para poder identificalo.
   * O calendario creado será un calendario sen ningún dato. Veranse tódalas datas do calendario en vermello polo que ese días non teñen asignación de horas. E preciso introducir a información relativa a Semana Laboral e as Excepcións.
   * Premer na pestana de *Semana de Traballo*. Asignar a xornada de traballo por defecto por cada día da semana. Por exemplo, é posible marcar 8 horas laborais de luns a venres definindo unha xornada laboral de 40 horas. Na parte dereita da pantalla poderase ver o resultado díario da asignación de xornada. Ao longo do tempo pódese ir modificando a xornada por defecto dun calendario. Isto será posible na edición ao crear novas versións do calendario.
   * Situarse na pestana de Excepción e introducir aqueles días especiais que teñen unha influencia no calendario laboral da empresa o no calendario do grupo de traballadores que se estea creando. Por exemplo, deberíanse sinalar os días festivos.
   * Seleccionar unha data no calendario, por exemplo o 19 de Marzo. Sinalar o tipo de excepción como BANK_HOLIDAY (Día de Vacacións). Finalmente indicar o número de horas a traballar que nese caso será 0. E pulsar no botón *Crear Excepción*.
   * O listado de excepcións pódese ver a dereita do formulario de creación de excepcións.
   * **A aplicación só permite modificacións do calendario a futuro** para que non se teña influencia en planificacións pasadas.
   * É posible marcar un conxunto de datas coma excepcións, simplemente tense que marcar a data de inicio no calendario e seleccionar no campo data fin a data ata a que chegue a excepción.
   * Para borrar unha excepción no calendario premerase na operación de borrar no listado de excepcións.
   * Pulsar en *Gardar* e o novo calendario aparecerá no listado de calendarios.

Edición dun calendario
----------------------

Será posible modificar un calendario para incluir modificacións na xornada laboral semanal ou para modificar os días excepcionais, para iso debense seguir os seguintes pasos:

   * Pulsar no botón editar nas operacións dun calendario existente no listado da administración de calendarios.
   * Poderanse modificar ou crear novos días excepcionais a futuro segundo as instrucións previas de creación dun novo calendario.
   * Para modificar a semana laboral por defecto e preciso situarse na pestañá de *Semana de Traballo*:

       * Pulsar en *crear unha nova semana de traballo*.
       * Indicar a data a partires da que esa semana entra en vigor.
       * Pulsar na opción de *Crear*.
       * Editar o valor das horas dos días laborais por cada día da semana.
       * Unha vez pulsada na opción *Gardar* do calendario se consolidaran os cambios desta nova versión. A partires da data de aplicación da nova versión o calendario comportarase desa forma.

    * Para que as modificacións teñan efecto é preciso premer no botón *Gardar* do calendario, se se pulsa no botón *Cancelar* os cambios consolidados non se almacenarán.

Copiar un calendario
--------------------

Poderase realizar unha copia dun calendario, a realización dunha copia supón que se creará un novo calendario cunha copia de tódolos datos do calendario orixinal. Este calendario poderase editar coma calquera outro calendario existente. Únicamente é preciso cambiarlle o nome para que non coincida con ningún dos existentes. A copia dun calendario non mantén ningunha relación co calendario de orixe.

Para facer unha copia seguiranse os seguintes pasos:

* Premer no botón *Crear Copia* nas operacións do calendario que se quere copiar no listado de administración.
* Modificar o nome do calendario
* Modificar os datos do noso interese se fora preciso.
* Premer no botón *Gardar*.


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

Para a súa selección seguense os seguintes pasos:

   * Entrar na sección de **Administración / Xestión > NavalPlan: Configuración** do menú principal.
   * Seleccionar no campo *Calendario por defecto* o calendario desexado.
   * Premer no botón *Gardar*


------------------
Módulo de recursos
------------------

Conceptos teóricos
==================

Os recursos son as entidades que realizan os traballos necesarios para completar os proxectos. Os proxectos na planificación represéntanse mediante diagramas de Gantt que dispoñen no tempo as diferentes actividades.

En NavalPlan existen tres tipos de recursos capaces de realizar traballo. Estos tres tipos son:

   * Traballadores. Os traballadores son os recursos humanos da empresa.
   * Máquinas. As máquinas son capaces tamén de desenvolver tarefas e teñen existencia en NavalPlan.
   * Recursos virtuais. Os recursos virtuais son como grupos de traballadores que non teñen existencia real na empresa, é dicir, non se corresponden con traballadores reais, con nome e apelidos, da empresa.

Utilidade dos recursos virtuais
-------------------------------

Os recursos virtuais son, como se explicou, como grupos de traballadores pero que non se corresponden con traballadores concretos con nome e apelidos.

Dotouse a NavalPlan a posibilidade de usar recursos virtuais debido a dous escenario de uso:

   * Usar recursos virtuais para simular contratacións futuras por necesidades de proxectos. Pode ocorrer que para satisfacer proxectos futuros as empresas necesiten contratar traballadores nun momento futuro do tempo. Para prever e simular cantos traballadores poden necesitar os usuarios da aplicación poden usar os recursos virtuais.
   * Pode existir empresas que dexesen xestionar as aplicación sen ter que levar unha xestión dos recursos con respecto os datos dos traballadores reais da empresa. Para estes casos, os usuario poden usar tamén os recursos virtuais.

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

Como nota dicir que existe unha comprobación que impide a gravación de dous traballadores co mesmo nome, apelidos e NIF. Todos estos campos son, ademais, obrigatorios.

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

   * Accede a Lista de traballares, opción Recursos > Grupo de traballadores virtuais.
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

Os criterios satisfanse durante un determinado período de tempo ou ben a partir dunha determinada data e de forma indefinida.

Para asignar un determinado criterio a un traballador hai que dar os seguintes pasos:

  * Acceder á opción Recursos > Traballadores.
  * Premer sobre o botón de edición sobre a fila do listado correspondente a o recurso desexado.
  * Pulsar na pestana Criterios asignados.
  * Premer no botón *Engadir* ao lado da etiqueta *criterio novo*. Esto provoca que se engada unha fila con tres columnas de datos:

     * Columna Nome do criterio. Seleccionar o criterio que se quere configurar como satisfeito polo traballador. O usuario ten que desplegar ou buscar o criterio elixido.
     * Columna Data de inicio. Elixir a data dende a cal o traballador satisface o criterio. É obrigatoria e aparece por defecto cuberta coa data do día actual.
     * Columna Data de fin. Configura a data ata cal se satisface o criterio. Non é obrigatoria. Se non se enche o criterio é satisfeito sen data de caducidade.

Adicionalmente existe na pantalla un *checkbox* para seleccionar que criterios son visualizados, todos os satisfeitos durante toda a historia do traballador ou únicamente os vixentes na actualidade.

A asignación de criterios ríxese polas regras ditadas polo tipo de criterio do criterio que se está asignando. Así por exemplo cabe mencionar dous aspectos:

   * En criterios de calquera tipo, unha asignación de criterio non se pode solapar no tempo con outra asignacion do mesmo criterio nun mesmo traballador.
   * En criterios que non permiten múltiples valores por recurso, non pode haber dúas asignacións de criterio a un traballador de maneira que o seu intervalo de validez teña algún dia común.

Os criterios que son seleccionables para ser asignados aos traballadores son os criterios de tipo TRABALLADOR.

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

As regras de asignación de criterios son as mesmas que para os traballadores. A diferencia é que os criterios que son seleccionables para asignar as máquinas son os criterios de tipo MAQUINA.

Alta de criterios en grupo de traballadores virtuais
----------------------------------------------------

A asignación de criterios para os traballadores virtuais é similar a asignación de criterios para os traballadores reais. Os pasos a dar son os seguintes:

   * Acceder a opción Recursos > Grupos de traballadores virtuais.
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

Na creación dun traballador créase un calendario ao traballador que deriva, por defecto, do calendario configurado por defecto na aplicación.

A configuración da aplicación pódese consultar en *Administracion* > *NavalPlan: Configuracion*.

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

   * Acceder a Lista de traballadores, opción Recursos > Máquinas.
   * Premer o botón Crear
   * Cubrir os campos do  formulario: Nome da máquina, código e descrición.
   * Premer na pestana Calendario
   * Nesa pestana  seleccionar o no selector que aparece do cal se quere derivar.
   * Premer o botón "Gardar"  ou ben "Gardar e continuar".

Asignación de calendario pai a grupos de traballadores virtuais
---------------------------------------------------------------

Os grupos de traballadores virtuais tamén configuran o calendario pai do cal derivan de forma similar aos traballadores reais e as máquinas. Os pasos son:

   * Accede a Lista de grupos de recursos virtuais, opción Recursos > Grupo de traballadores virtuais.
   * Premer no botón Crear.
   * Cubrir os datos na pestana de Datos persoais.
   * Premer na pestana Calendario
   * Nesa pestana  seleccionar o no selector que  aparece do cal se quere derivar.
   * Premer o botón "Gardar" ou ben "Gardar e continuar".

Cambio de calendario pai a traballadores, máquinas ou grupos de traballadores virtuais
--------------------------------------------------------------------------------------

É posible cambiar o calendario pai do cal deriva un recurso calquera, xa sexa un traballador, máquina ou un grupo de traballador virtual.

Para elo hai que facer o seguinte:

   * Ir a sección correspondente: Recursos > Lista de máquinas, Recursos > Lista de traballadores ou Recursos > Grupo virtual de traballadores.
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

Os dous primeiros conceptos, é dicir, a xornada semanal de traballo e as excepcións de dedicación explícanse na sección de **Administración de calendario xeral**

Agora ben, os calendarios dos recursos teñen unha particularidade con respecto ao calendario da empresa. Esta peculariedade son os períodos de activación.

Os períodos de activación son intervalos nos cales os traballadores se encontran dispoñibles para a planificación. Conceptualmente correspóndense con aqueles períodos nos cales o traballador se atopa contratado pola empresa. Un traballador pode ser contratado por un tempo, despois abandonar a empresa e reincorporarse posteriormente. NavalPlan garda a historia de planificación de todos os recursos, utiliza a información de activación para impedir que se lle asigne traballo.

No momento da creación dun traballador configúrase con un período de activación que vai dende o momento da alta ata o infinito. Neste momento non é posible cambialo e esta operación ten que ser feita cunha edición posterior do recurso.

Configuración dos períodos de activación dun recurso
----------------------------------------------------

Os períodos de activación dun determinado recurso teñen que satisfacer non ter puntos de solapamento no tempo. Os pasos para configuralos son:

   * Ir a sección correspondente: Recursos > Máquinas, Recursos >  Traballadores ou Recursos > Grupo de traballadores virtuais.
   * Seleccionar a fila do recurso que se quere editar e premer no botón da fila asociada para editar.
   * Acceder a pestana de Calendario.
   * Dentro da pestana de Calendario premer na pestana interior Períodos de activación.
   * No interior da pestana sairán a lista de períodos de activación. Pulsar no botón Crear período de activación.
   * Neste momento engádese unha fila coas seguintes columnas:

      * Data de inicio: A encher obrigatoriamente. Introducir a data na cal se quixera activar o recurso.
      * Data de fin: Opcional. Introducir a data no cal o traballador deixa de estar activo na empresa.
   * Premer no botón "Gardar" ou "Gardar e continuar".


-------------------
Módulo de proxectos
-------------------

Conceptos teóricos
==================

Os proxectos son as contratacións de traballo que as empresas asinan cos seus clientes. No conxunto de empresas do naval os proxectos están constituídos por un número de elementos organizados en estruturas de datos xerárquicas (árbores), tamén chamadas EdT (estruturas de traballo).

Básicamente existen dous tipos de nodos:

   * Nodos contedores. Un nodo contedor é un agregador e actúa como clasificador de elementos. Non introduce traballo por el mesmo, senón que o traballo por el representado e a suma de todalas horas dos nodos descendentes do mesmo.
   * Nodos folla. Un nodo folla é un nodo que non ten fillos e que que está constituido por un ou máis conxuntos de horas.

En NavalPlan, por tanto, permítese o traballo con proxectos estruturados según os tipos de nodos precendentes.

Acceso a vista global da empresa
================================

A vista global da empresa e a pantalla inicial da empresa, a que se entra unha vez que o usuario entra na aplicación.

Nela o que se pode ver son tódolos proxectos que existen na empresa e estes son representados a través dun diagrama de gantt. Os datos que se poden observar de cada proxecto son:

   * A súa data de inicio e a súa data de fin.
   * Cal é o progreso na realización de cada proxecto.
   * O número de horas que se levan feito de cada un deles.
   * Cal é a súa **data límite** en caso de que o teñan.

Ademais do anterior mostrase na parte inferior da pantalla dúas gráficas:

   * Gráfica de carga de carga de recursos.
   * Gráfica de valor gañado.

Para acceder á vista de empresa chega con facer login na aplicación. En caso de xa atoparse traballando coa aplicación, o acceso á vista de empresa conséguese a través da operación de menú *Planificación > Planificación de proxectos*.

Creación dun proxecto
=====================

Para a creación dun proxecto hai que acometer os seguintes pasos:

   * Acceder ao opción Planificación > Proxectos.
   * Premer no botón situado na barra de botón co texto *Crear proxecto novo*.
   * NavalPlan amosa una ventá onde se solicitan os datos básicos do proxecto:

      * Nome. Cadena identificativa do proxecto. Obligatorio.
      * Código do proxecto. Código para identificar o proxecto. Deber ser único. Non cubrilo e manter marcado o checkbox Autoxeneración de código. Se éste está cuberto encárgase NavalPlan de crear o código correspondente. Obligatorio.
      * Data de inicio. Esta data é a data a partir da cal se comenzará a planificación do proxecto. Obligatorio.
      * Data límite. Este campo é opcional e indica cal é o deadline.
      * Cliente. Campo para seleccionar cales dos clientes da empresa é o contratista do proxecto.
      * Calendario asignado. Os proxectos teñen un calendario que dicta cando se traballa neles. Hai que seleccionar o calendario que se quere utilizar.

   * Aparecen unha serie de pestanas. A que aparece seleccionada por defecto, a primeira delas con título *EDT (tarefas)* (estrutura de traballo). Esta pestana explícase na sección *Introdución de tarefas do proxecto con horas e nome*
   * Os datos xerais poden ser editados premendo na pestana *Datos xerais*. Os datos que se poden introducir son:

      * Nome do proxecto. Cadena identificativa do proxecto. Obligatorio.
      * Código do proxecto. Código para identificar o proxecto. Deber ser único. Non cubrilo e manter marcado o checkbox Autoxeneración de código. Se éste está cuberto encárgase NavalPlan de crear o código correspondente. Obligatorio.
      * Código externo: Campo utilizado para integración con terceiras aplicacións.
      * Modo de planificación: Adiante ou atrás. A planificación cara adiante é aquela que as tarefas se van colocando dende a tarefa de inicio e se moven cara adiante segundo se establecen dependencias. A planificación cara atrás é aquela que as tarefas se colocan con fin na data de entrega e as dependencias entre elas se xestionan cara atrás.
      * Data de inicio. Esta data é a data a partir da cal se comenzará a planificación do proxecto. Obligatorio.
      * Data límite. Este campo é opcional e indica cal é o deadline.
      * Responsable. Campo de texto para indicar a persoa responsable. Informativo e opcional.
      * Cliente. Campo para seleccionar cales dos clientes da empresa é o contratista do proxecto.
      * Referencia do cliente. Identificador externo do cliente se o usuario o desexa utilizar.
      * Descrición. Campo para describir de que vai o proxecto ou poñer calquera nota.
      * As dependencias teñen prioridade. Campo relacionado coa planificación que indica quen manda se as restricións que teñen as tarefas ou os movementos ordenados polas dependencias.
      * Calendario asignado. Os proxectos teñen un calendario que dicta cando se traballa neles. Hai que seleccionar o calendario que se quere utilizar.
      * Presuposto. Desglose do que se presupostou o proxecto en dúas cantidades:

         * Traballo. Cantidade polo que se presupostou a man de obra do proxecto.
         * Materiais. Cantidade polo que se presupostaron os materiais do proxecto.
      * Estado. Un proxecto pode estar en varios estados ao longo da súa existencia. Os ofrecidos son:

         * Ofertado
         * Aceptado
         * Empezado
         * Finalizado
         * Cancelado
         * Subcontratado
         * Pasado a histórico.
   * Pulsa no botón Gardar representado por un disquete de ordenador na barra.

Se os datos introducidos son correctos o sistema proporciona nunha ventá emerxente o resultado da operación.

Edición dun proxecto
====================

Para a edición dun proxecto existen varios camiños posibles:

   * Opcion 1:
      * Ir a opción Planificación > Proxectos.
      * Premer sobre a icona de edición, lapiceiro sobre folla de papel, que se corresponda co proxecto desexado.
   * Opción 2:
      * Ir a Planificación > Vista da compañía.
      * Facer dobre click co botón esquerdo do rato sobre a tarefa que representa o proxecto na vista da empresa ou ben pulsar co botón dereito sobre a tarefa e despois escoller a opción Planificar.
      * Pulsar a icona da parte esquerda Detalles de proxecto.
   * Opción 3:
      * Ir a Planificación > Vista da compañía.
      * Premer na icona da parte esquerda Listado de proxectos.
      * Facer click sobre a icona que representa unha libreta en branco con un lapis verde ou facer dobre click sobre a fila desexada.

Introdución de tarefas a un proxecto con horas e nome
=====================================================

Para introducir as tarefas, contedores ou elementos de proxecto folla, hai que dar os seguintes pasos:

   * Ir a opción Planificación > Proxectos.
   * Premer sobre a icona de edicion, lapis sobre folla de papel, que se corresponda co proxecto desexado.
   * Seleccionar a pestana "EDT (tarefas)"
   * Unha vez aquí, introducir na linea de edición situada enriba da táboa de lista de tarefas os seguintes valores:

      * No campo de nome unha identificación da tarefa.
      * No campo horas un número enteiro que represente o número de horas de que se compón o traballo da tarefa.

   * Premer o botón "Nova tarefa"

Ao pulsar no botón anterior engádese unha tarefa de tipo folla e sitúase ao final das tarefas existentes na árbore de tarefas.

No caso de que se queira cambiar a posición da tarefa e situalo en outro lugar da árbore debe seleccionarse a fila concreta e despois premer nas iconas situadas na zona superior dereita da edición do proxecto:

   * Icona frecha arriba. Premendoo faise que a tarefa ascenda na árbore de tarefas.
   * Icona frecha abaixo. Pulsando nel faise que a tarefa descenda na árbore de tarefas.

A través do explicado ata agora o que se engaden son tarefas folla, pero tamén e posible engadir tarefas contedores. Para engadir tarefas contedores, o usuario pode realizar varios itinerarios:

Creando tarefas contedoras mediante arrastrar e soltar
------------------------------------------------------

Para poder levar a cabo esta operación é necesario dispor de alomenos dous elementos de proxecto folla creados segundo o procedemento explicado no punto anterior. Partindo do suposto que ter dous elementos de proxecto folla elemento E1 e elemento E2.

Os pasos a dar son os seguintes:

   * Colocarse co punteiro do rato encima do elemento E1.
   * Pulsar o botón esquerdo do rato e sen soltar arrastrar o elemento E1. Mentras se mantén pulsado aparecerá un texto sobre o fondo indicando que o elemento E1 está agarrado.
   * Desprazar o rato mantendo pulsado o botón esquerdo ata situarse encima do elemento E2. Nese momento liberar o botón do rato.
   * O que ocorre neste punto é que se creará unha tarefa contedor que terá o nome de E2 e posuirá dous fillos cos nomes E2 Copia e E1. O elemento E2 Copia terá a carga de traballo do anterior elemento E2 e, agora, o elemento E2 conterá a suma das horas de E1 e E2 Copia.

Creando tarefas contedoras mediante creación con tarefa folla seleccionada
--------------------------------------------------------------------------

Para levar a cabo esta operación é necesario dispor dunha tarefa folla creado, supóñase que chamado E1. A partir de aquí, os pasos para crear un contedor son:

   * Situar o punteiro do rato na fila do elemento E1 e pulsar o botón esquerdo do rato na área da fila que vai dende o comezo ata o primeira icona que sae na fila (icono de notificación de estado de planificación que se verá máis adiante). Tras realizar esta acción a fila aparecerá seleccionada.
   * Introducir na liña de edición, situada enriba da táboa da árbore de tarefas, o nova tarefa, con nome E2 e un numero de horas.
   * Premer no botón "Engadir" que está situado á dereita da etiqueta "Nova tarefa" e os campos de entrada de nome e horas.
   * O que ocorre neste punto é que se creará unha tarefa contedor que terá o nome de E2 e posuirá dous fillos cos nomes E2 Copia e E1. O elemento E2 Copia terá a carga de traballo do anterior elemento E2 e, agora, o elemento E2 conterá a suma das horas de E1 e E2 Copia.

Creando tarefas contedoras mediante a pulsación da icona de indentación
-----------------------------------------------------------------------

Para levar a cabo esta operación é necesario ter creadas as tarefas, E1 e E2, situado E1 antes que E2. A partir de aquí levar a cabo os seguintes pasos:

   * Seleccionar elemento E2 (debe saír en amarelo o fondo da tarefa).
   * Pulsar sobre o botón de identar cara a dereita, frecha apuntado a dereita na zona superior dereita de iconas.
   * O que ocorre neste punto é que se creará unha tarefa contedor que terá o nome de E2 e posuirá dous fillos cos nomes E2 Copia e E1. O elemento E2 Copia terá a carga de traballo do anterior elemento E2 e, agora, o elemento E2 conterá a suma das horas de E1 e E2 Copia.

Desprazamento de tarefas
------------------------

Unha vez se ten unha estrutura de tarefas contedor e tarefas folla tamén se poden realizar operacións de modificación da posición dos elementos nesta estrutura.

Para realizar estas operación disponse das iconas situadas na parte superior dereita da zona de edición, simplemente é necesario seleccionar a fila sobre a que se desexa aplicar unha operación. Os botóns de operación son:

   * Icono frecha arriba. Permite o desprazamento cara arriba dunha tarefa dentro de todos as súas tarefas irmáns, é dicir, que posúan o mesmo pai.
   * Icono frecha abaixo. Permite o desprazamento cara abaixo dunha tarefa dentro de todos as súas tarefas irmáns, é dicir, que posúan o mesmo pai.
   * Icono frecha esquerda. Permite desindentar unha tarefa. Isto supón subilo na xerarquía e poñelo ao mesmo nivel que o seu pai actual. Só está activado nas tarefas que teñen un pai, é dicir, que non son raíz.
   * Icono frecha dereita. Permite indentar unha tarefa. Esto supón baixalo na xerarquía e poñelo ao mesmo nivel que os fillos do seu irmán situado encima del. Só está permitida esta operación nas tarefas que teñen un irmán por enriba del.

Puntos de planificación
=======================

Conceptos teóricos
------------------

Unha vez os proxectos está introducidos cun conxunto de horas o seguinte paso e determinar como se planifican.

NavalPlan é flexible para determinar a granularidade do que se quere planificar e para elo introduce o concepto de puntos de planificación. Isto permite aos usuarios ter flexibilidade á hora de decidir se un proxecto interesa planificalo con moito detalle ou ben se interesa xestionalo máis globalmente.

Os puntos de planificación son marcas que se realizan sobre as árbores de tarefas dun proxecto para indicar a que nivel se desexa planificar. Se se marca unha tarefa como punto de planificación significa que se vai a crear unha tarea de planificación a ese nivel que agrupa o traballo de tódalas tarefas situados por debaixo del. Se este punto de planificación se corresponde cunha tarefa que non é raíz o que se fai é que as tarefas por enriba del se converten en tareas contedoras en planificación.

Unha tarefa pode estar en 3 estados de planificación tendo en conta os puntos de planificación:

   * **Totalmente planificado**. Significa que o traballo que él representa está totalmente incluído na planificación. Pode darse este estado en tres casos:

      * Que sexa punto de planificación.
      * Que se atope por debaixo dun punto de planificación. Neste caso o seu traballo xa se atopa integrado polo punto de planificación pai del.
      * Que non haxa ningún punto de planificación por encima del pero que para todo o traballo que representa haxa un punto de planificación por debaixo del que o cubra.

   * **Sen planificar**. Significa que para o traballo que representa non haxa ningún punto de planificación que recolla parte do seu traballo para ser planificado. Isto ocorre cando non é punto de planificación e non hai ningún punto de planificación por enriba ou por debaixo del na xerarquía.

   * **Parcialmente planificado**. Significa que parte do seu traballo está planificado e outra parte aínda non se incluíu na planificación. Este caso ocorre cando a tarefa non é punto de planificación, non hai ningunha tarefa por encima del na xerarquía que sexa punto de planificación e, ademais, existen descendentes do mismo que sí son puntos de planificación pero hai outros descendentes que están en estado sen planificar.

Así mesmo un proxecto terá un estado de planificación referido a tódolos seus elementos de proxecto e será o seguinte:

   * Un proxecto atópase en estado totalmente planificado se todos os seus elementos de proxecto se atopan en estado totalmente planificado.
   * Un proxecto atópase sen planificar se todos os seus elementos de proxecto se atopan en estado sen planificar.
   * Un proxecto atópase parcialmente planificado se hai algunha tarefa que está en estado sen planificar.

Borrar elementos de proxecto
----------------------------

Para borrar elementos de proxectos existe unha icona que representa unha papeleira sobre cada fila que representa unha tarefa. Por tanto, para borrar hai que:

   * Identificar a fila que se corresponde ca tarefa que se desexa eliminar.
   * Premer co botón de esquerdo do rato sobre a icona da papeleira. Neste momento o sistema procede a borrar tanto a tarefa como tódolos seus descendentes.
   * Pulsar na icona de Gardar, disquete na barra superior, para confirmar o borrado.

Creación de puntos de planificación
-----------------------------------

Para a creación de puntos de planificación hai que realizar os seguintes pasos:

   * Ir a opción Planificación > Proxectos.
   * Identificar a fila que se corresponde co proxecto que se quere editar e que ten que ter elementos de proxecto. Premer o botón Editar, lapis sobre folla de papel, e pulsalo.
   * Seleccionar a pestana "EDT (tarefas)".
   * Identificar sobre a árbore a que nivel se desexa planificar cada parte e, unha vez decidido, onde se desexa crear unha tarefa de planificación pulsar co rato sobre un icono que representa un diagrama de gantt de dúas tarefas. Isto converte a tarefa en punto de planificación, pon en verde tódolos elementos totalmente planificados e se marcará a fila do punto de planificación e as súas descendentes cunha cunha N.
   * Pulsar na icona de Gardar, disquete na barra superior, para confirmar o borrado.

Para desmarcar punto de planificación e planificar a outro nivel facer o seguinte:

   * Identificar sobre a árbore de tarefas aquel que estaba marcado como punto de planificación e que se desexa cambiar.
   * Premer sobre a icona que representa un diagrama de gantt cunha aspa X vermella. Tras elo, quítase como elemento de planificación e actualízase o estado de planificación do seus descendentes e antecesores.
   * Pulsar na icona de Gardar, disquete na barra superior, para confirmar o borrado.

Criterios en tarefas
====================

Conceptos teóricos
------------------

As tarefas representan o traballo que hai que planificar e tamén poden esixir o cumprimento de criterios. O feito de que unha tarefa esixa un criterio significa que se determina que para a realización do traballo que ten asociado a tarefa é apropiado que o recurso que se planifique satisfaga ese criterio.

Os criterios cando se aplican a unha determinada tarefa propáganse realmente a todos os seus descendentes. Isto significa que se un criterio e esixido a un determinado nivel na árbore de tarefas, pasa a ser a esixido tamén por tódalas tarefas fillas.

Por tanto, un criterio pode ser esixido de dúas formas nunha tarefa:

   * De forma directa. Neste caso o criterio é configurado como requirido na tarefa polo usuario.
   * De forma indirecta. O criterio é requirido na tarefa por herdanza debido a que ese criterio é requirido nunha tarefa pai.

Os criterios indirectos dunha tarefa poden ser invalidados, é dicir, configurados como non aplicados nun determinada tarefa descendente do primeiro. Se un criterio indirecto é invalidado nun determinada tarefa, entón invalídase en tódolos descendentes do elemento que se está configurando como invalidado.

Introdución de criterio nunha tarefa folla
-----------------------------------------------------

Para dar de alta un criterio nunha tarefa folla hai que dar os seguintes pasos:

   * Ir a opción Planificación > Proxectos
   * Identificar sobre a lista de proxectos o proxecto co cal se quere traballar.
   * Pulsar no botón editar do proxecto folla desexado.
   * Seleccionar a pestana **EDT (tarefas)**
   * Identificar a tarefa folla ao cal se desexa configurar os criterios.
   * Premer no botón editar da tarefa. Isto abre unha ventá emerxente.
   * Sobre a ventá emerxente seleccionar a pestana **Criterio requirido**.
   * Pulsa no botón **Engadir** na primeira sección denominada **Criterios asignados requiridos**.
   * Neste momento se engade unha fila na cal na primeira columna, **Nome do criterio**, se inclúe un compoñente de búsqueda de criterios. Pulsar co botón esquerdo do rato sobre este compoñente de búsqueda e comezar a teclear o nome do criterio ou tipo de criterio do cal se quere engadir o criterio.
   * Seleccionar sobre o conxunto de criterios que encaixan coa clave de búsqueda tecleada polo usuario aquel en concreto que se quere requirir á tarefa.
   * Pulsar en **Atrás**.
   * Premer sobre a icona de gardar representado por un disquete da barra de operación situada na parte superior.

Introdución de criterio nunha tarefa contedor
---------------------------------------------

Para dar de alta un criterio nunha tarefa contedor hai que dar os seguintes pasos:

   * Ir a opción Planificación > Proxectos
   * Identificar sobre a lista de proxectos o proxecto co cal se quere traballar.
   * Pulsar no botón editar do proxecto desexado.
   * Seleccionar a pestana **EDT (tarefas)**
   * Identificar a tarefa contedor ao cal se desexa configurar os criterios.
   * Premer no botón editar da tarefa. Esto abre unha ventá emerxente.
   * Sobre a ventá emerxente seleccionar a pestana **Criterio requirido**
   * Pulsa no botón **Engadir** na primeira sección denominada **Criterios asignados requiridos**
   * Neste momento se engade unha fila na cal na primeira columna, **Nome do criterio**, se inclúe un compoñente de búsqueda de criterios. Pulsar co botón esquerda do rato sobre este compoñente de búsqueda e comezar a teclear o nome do criterio ou tipo de criterio do cal se quere engadir o criterio.
   * Seleccionar sobre o conxunto de criterios que encaixan coa clave de búsqueda tecleada polo usuario aquel en concreto que se quere requirir á tarefa.
   * Pulsar en **Atrás**.
   * Premer sobre a icona de gardar representado por un disquete da barra de operación situada na parte superior.

Para comprobar como se engade o criterio sobre todos os elementos fillos descendentes da tarefa contedor ao cal se lle requiriu o criterio dar os seguintes pasos:

   * Identificar sobre a árbore de tarefas do proxecto sobre o que se está a traballar unha tarefa fillo da tarefa contedor que require un criterio.
   * Pulsar sobre o botón de edición da tarefa identificado no punto anterior.
   * Sobre a ventá emerxente seleccionar a pestana **Criterio requirido**
   * Na sección da parte superior da ventá titulada **Criterios asignados requiridos** observarase o criterio requirido buscar o nome do criterio requirido pola tarefa pai. Aparecerá amosado como **Indirecto** na columna de tipo.

Invalidar un requirimento de criterio nunha tarefa
--------------------------------------------------

Para levar a cabo a operación descrita neste epígrafe hai que ter unha situación ao menos dunha tarefa contedor E1 que teña dentro unha tarefa E2 e a tarefa E1 teña requirido un criterio C1.

Baixo esta premisa, para invalidar o criterio C1 no elemento E2 hai que efectuar os seguintes pasos:

   * Identificar sobre a árbore de tarefas o elemento E2.
   * Pulsar sobre a icona de edición da fila correspondente a E2.
   * Ir a pestana *Criterios requiridos*.
   * Identificar na táboa da sección **Criterios asignados requiridos** o criterio C1 que ten que aparece co tipo **Indirecto**
   * Premer no botón invalidar.
   * Pulsar en **Atrás**.
   * Premer sobre a icona de gardar representado por un disquete da barra de operacións situada na parte superior.

Borrar un requirimento de criterio nunha tarefa
-----------------------------------------------

Os requirimentos que se poden borrar son únicamente os criterios directos, xa que os criterios indirectos únicamente se poden invalidar. Os pasos que hai que dar para invalidar un criterio directos son os seguintes:

   * Ir a opción Planificación > Proxectos
   * Identificar sobre a lista de proxectos o proxecto co cal se quere traballar.
   * Pulsar no botón editar do proxecto desexado (ou facer dobre click sobre a fila desexada).
   * Seleccionar a pestana **EDT (tarefas)**
   * Identificar a tarefa que ten un criterio directo e ao cal se desexa borrar un criterio directo.
   * Premer no botón editar da tarefa. Isto abre unha ventá emerxente.
   * Sobre a ventá emerxente seleccionar a pestana **Criterio requirido**.
   * Identificar na táboa da sección **Criterios asignados requiridos** o criterio directo que se desexa borrar.
   * Premer na icona de borrar da fila correspondente.
   * Pulsar no botón **Atrás**
   * Premer sobre a icona de **gardar** representado por un disquete na barra de operacións situada na parte superior.

Xestión de requirimentos a nivel de proxecto
--------------------------------------------

A tódolos efectos un proxecto actúa como unha tarefa contedor que engloba tódalas tarefas raíces. Por tanto, no referente aos criterios tódolos criterios que se asignen ao proxecto serán herdados como criterios indirectos en todas as tarefas.

Como se deduce tamén, un proxecto non pode recibir criterios indirectos xa que é a raíz da árbore dos seus elementos de proxecto.

Os pasos para acceder a xestión dos criterios a nivel de proxecto son os seguintes:

   * Ir a opción Planificación > Proxectos.
   * Identificar sobre a lista de proxectos o proxecto sobre o cal se quere traballar.
   * Premer no botón editar do proxecto.
   * Seleccionar a pestana *Criterio requirido*
   * Acceder a sección **Criterios asignados requiridos** onde se poden xestionar a adición de criterio directos e o borrado dos existentes como o explicado nas tarefas.
   * Premer sobre a icona de gardar representado por un disquete na barra de operacións situada na parte superior.

-----------------------
Módulo de planificación
-----------------------

Para comprender as principais funcionalidades de planificación da aplicación é preciso acceder a sección **Planificación > Planificación de proxectos**. Navaplan permite consultar a informacións de planificación da empresa en dous niveis:

   * Nivel Empresa: pódese consultar a información de tódolos proxectos en curso.
   * Nivel Proxecto: pódese consultar a información de tódalas tarefas dun proxecto.

Dende a vista de empresa é posible navegar ao detalle dun proxecto facendo doble click na caixa do diagrama de gantt que representa o proxecto ou pulsando co botón dereito para abrir o menú contextual seleccionando "planificar".

Para volver a vista de empresa tense que pulsar no menú principal en **Planificación > Planificación de proxectos** ou en **INCIO** na ruta que mostra a información que se estea visualizando.

A vista de empresa xa detallada previamente é a pantalla principal da aplicación para o seguimento da situación dos proxectos da empresa.

Perspectivas: vista de recursos, proxectos e asignación avanzada
================================================================

Tanto a vista de empresa coma a de nivel proxectos permiten a visualización de diferentes perspectivas da información. As perspectivas permiten cambiar o punto de vista dende o que se consulta a información de planificación Recursos, Tarefas ou Temporal.

Dentro de cada nivel Empresa ou Proxecto é posible cambiar dunha perspectiva pulsando nas iconas que se mostran na parte esquerda da vista de planificación.

Na **vista da empresa** existen tres perspectivas dispoñibles:

   * Planificación de proxectos: amosa a visión dos proxectos no tempo cunha representación dun diagrama de Gantt, nesta vista aparecen tódolos proxectos planificados coa súa date de inicio e fin. Graficamente se pode ver en cada caixa o grado de progreso, o número de horas traballadas no proxecto e as datas límites de entrega.
   * Uso de recursos: mostra a visión dos recursos da empresa no tempo, representando nun grafico de liñas do tempo a carga de traballo dos recursos co detalle das tarefas as que están asignados.
   * Lista de proxectos: amosa o listado dos proxectos existentes coa súa información de datas, presuposto, horas e estado e permite acceder a edición dos detalles do proxecto.
   * Planificación de recursos limitantes: Vista de planificación dos recursos que son limitantes, é dicir, actúan como colas, de xeito que tarefas de outros proxectos son xestionados nas colas dos recursos limitantes da empresa.

Na **vista de proxecto** existen catro perspectivas dispoñibles:

   * Planificación do proxecto: amosa a visión das tarefas do proxecto no tempo cunha representación de diagrama de Gantt, nesta vista pode consultarse a información das datas de inicio e fin, a estrutura xerarquica das tarefas, os progresos, as horas imputadas, as dependencias de tarefas, os fitos e as datas límite das tarefas.
   * Uso de recursos: amosa a visión dos recursos asignados ao proxecto no tempo coa súa carga de traballo tanto en tarefas deste proxecto coma as pertencentes a outros proxectos por asignacións xenéricas ou específicas.
   * Detalles de proxecto: permite acceder a toda a información do proxecto, organización do traballo, asignación de criterios, materiais, etc. Xa foi tratada dentro da edición de proxectos.
   * Asignación avanzada: amosa a asignación numérica con diversos niveles de granularidade (dia,semana,mes) dos recursos nas tarefas do proxecto. Permite modificar as asignacións de recursos no tempo as distintas tarefas do mesmo.
   * Se se habilitou o "método de Montecarlo": método que permite recorrer as planificacións baseándose nunha estimación, optimista, pesimista e realista das duracións das tarefas e unhas probabilidades de ocorrencia. A partir da anterior información, NavalPlan ofrece a probabilidade de finalización do proxecto nunha data ou unha semana concreta.

Vista de planificación de empresa
=================================

A vista de planificación de empresa mostra no tempo os proxectos en curso. Os proxectos represéntanse mediantes un diagrama de Gantt que indica as datas de inicio e fin dos proxectos mediante a visualización dunha caixa nun eixo temporal.

A vista de planificación dispón dunha barra de ferramentas na parte superior que permite realizar as seguintes operacións:

   * Impresión da planificación: Xera un ficheiro PDF ou unha imaxe en PNG co gráfico da planificación.
   * Nivel de zoom: permite modificar a escala temporal na que se mostra a información. Pódese seleccionar a granularidade a distintos niveis: día, semana, mes, trimestre, ano.
   * Amosar/Ocultar etiquetas: oculta ou amosa no diagrama de gantt as etiquetas asociadas a cada un dos proxectos.
   * Amosar/Ocultar progresos: oculta ou amosa no diagrama de gantt os progresos asociados a cada un dos proxectos.
   * Amosar/Ocultar horas asignadas: oculta ou amosa no diagrama de gantt as horas asignadas asociadas a cada un dos proxectos.
   * Amosar/Ocultar asignacións: oculta ou amosa no diagrama de gantt os recursos asignados a cada un dos proxectos.
   * Filtrado de etiquetas y criterios: permite seleccionar proxectos en base a que cumpran criterios ou teñan asociadas etiquetas.
   * Filtrado por intervalo de datas: permite seleccionar datas de inicio e fin para o filtrado.
   * Selector de filtrado en subelementos: realiza as búsquedas anteriores incluindo os elementos e tarefas que forman o proxecto. E non únicamente as etiquetas e criterios asociadas ao primeiro nivel do proxecto.
   * Acción de Filtrado: executa a búsquera en base aos parametros definidos anteriormente.

Na parte esquerda están os cambios de perspectivas a nivel de empresa que permitirá ir a sección de Carga global de recursos e Lista de proxectos. A perspectiva que se estea visualizando e a Planificación.

Na parte inferior amósase a información da carga dos recursos no tempo así como as gráficas referentes ao valor gañado que serán explicadas máis adiante.

Vista de planificación de proxecto
==================================

Para acceder a vista de planificación dun proxecto é preciso facer doble click na representación do do diagrama de Gantt nun proxecto, ou cambiar a perspectiva de planificación dende a perspectiva de detalle de proxectos.

Nesta vista poderase acceder as accións de definición de dependencias entre tarefas e asignación de recursos.

A vista de planificación de proxecto dispón dunha barra de ferramentas na parte superior que permite realizar as seguintes operacións:

   * Gardar planificación: consolida na base de datos tódolos cambios realizados sobre a planificación e a asignación de recursos. **É importante gardar sempre os cambios unha vez terminada a elaboración da planificación**. Se se cambia de perspectiva ou se entra noutra sección perderanse os cambios.
   * Operación de reasignar: esta operación permite recalcular as asignacións de recursos nas tarefas do proxecto.
   * Nivel de zoom: permite modificar a escala temporal na que se mostra a  información. Pódese seleccionar a granularidade a distintos niveis: día,  semana, mes, trimestre, ano.
   * Resaltar camiño crítico: mostra o camiño crítico do proxecto, realiza o cálculo daquelas tarefas que o seu atraso implicará un atraso do proxecto.
   * Amosar/Ocultar  etiquetas: oculta ou amosa no diagrama de gantt as etiquetas asociadas a cada unha das tarefas.
   * Amosar/Ocultar asignacións: oculta ou amosa  no diagrama de gantt os recursos asignados a cada unha das tarefas.
   * Amosar/Ocultar horas asignadas: oculta ou amosa no diagrama de gantt as horas asignadas asociadas a cada unha das tarefas.
   * Amosar/Ocultar asignacións: oculta ou amosa no diagrama de gantt os recursos asignados a cada unha das tarefas.
   * Expandir tarefas folla: mostra tódalas tarefas de último nivel expandindo tódolos niveis da arbore de tarefas.
   * Filtrado de  etiquetas y criterios: permite seleccionar proxectos en base a que cumpran  criterios ou teñan asociadas etiquetas.
   * Filtrado por intervalo  de datas: permite seleccionar datas de inicio e fin para o filtrado.
   * Filtrado por nome: permite indicar o nome da tarefa
   * Acción de  Filtrado: executa a procura en base aos parametros definidos  anteriormente.

Xusto enriba da barra de tarefas atopase o nome do proxecto que esta detrás do texto INICIO > Planificación > Planificación de proxectos > NOME DO PROXECTO.

Se o proxecto se atopa totalmente planificado aparecera a dereita do nome unha letra C (Completamente Planificado), pero se non están marcados tódolos puntos de planificación do proxecto amosarse unha letra P (Parcialmente Planificado). Só se amosará a letra C cando tódalas tarefas na edición do proxecto se atopen por debaixo dun punto de planificación.

Na vista de planificación de proxecto pódese observar que as tarefas organízanse de forma xerárquica, de forma que pódense expandir e comprimir as tarefas.

Na parte inferior amósase a información da carga dos recursos no tempo así como as gráficas referentes ao valor gañado que serán explicadas máis adiante.

Na vista de planificación dun proxecto pódese facer as seguintes operacións de interese:

   * Definición de dependencias entre tarefas.
   * Definición de retriccións de tarefas.
   * Asignación de recursos a tarefas

Asignación de dependencias
--------------------------

Unha dependencia é una relación entre dúas tarefas pola cal unha tarefa A non pode comezar ou terminar ata que unha tarefa B comece ou remate. Navalplan implementa as seguintes relacións de dependencias entre tarefas entre dúas tarefas chamadas A e B.

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
   * Recordar que é preciso pulsar na icona de gravar para consolidar os cambios na planificación, xa que a operación de dependencias non se

O comportamento do recálculo de asignacións de tarefas compórtase de diferente xeito dependendo do tipo de planificación elixida para o proxecto:

   * Planificación cara adiante: A tarefa na que entra a dependencia será colocada xusto despois da tarefa orixe. As asignacións de recursos faranse cara adiante e estableceráselle unha restrición TAN PRONTO COMO SEXA POSIBLE.
   * Planificación cara atrás: A tarefa da que sae a dependencia será colocada xusto antes da data de comezo da tarefa destino da dependencia. As asignacións de recursos faranse cara atrás temporalmente e estableceráselle unha restrición TAN TARDE COMO SEXA POSIBLE.

Asignación de recursos
======================

A asignación de recursos é unha das partes máis importantes da  aplicación. A asignación de recursos pode realizarse de dous xeitos diferentes:

   * Asignacións específicas.
   * Asignacións xenéricas.

Cada unha das asignacións é explicada nas seguintes seccións.

Para realizar calquera das dúas asignacións de recursos é necesario  dar os seguintes pasos:

   * Acceder á planificación dun proxecto.
   * Premer co botón dereito sobre a tarefa que se desexa asignar na opción de asignación de recursos.
   * A aplicación amosa unha pantalla na que se pode  visualizar a seguinte información.

      * Información da tarefa:

         * Listado de criterios que deben ser satisfeitos. Por cada grupo de horas, amósase un listado de grupos de horas e cada grupo  de horas esixe o seu listado de criterios.
         * Asignación recomendada: Opción que lle permite á  aplicación recoller os criterios que deben ser satisfeitos e as horas  totais de cada grupo de horas e fai unha proposta de asignación xenérica  recomendada. Se había unha asignación previa, o sistema elimina dita  asignación substituíndoa pola nova.

      * Configuración de asignación:

         * Data de inicio e data de fin  da tarefa.
         * Duración.
         * Tipo de cálculo: O sistema permite elixir a  estratexia que se desexa levar a cabo para calcular as asignacións:
         * Calcular número de horas: Calcula o número de horas que faría falla  que adicasen os recursos asignados dados unha data de fin e un número de  recursos por día.
         * Calcular data fin: Calcula a data de fin da tarefa a partir dos  número de recursos da tarefa e das horas totais adicar para rematar a  tarefa.
         * Calcular número de recursos: Calcula o número de recursos necesarios  para rematar a tarefa en unha data específica e adicando unha serie de  horas coñecidas.

      * Asignacións:
         * Asignacións: Listado de asignacións realizadas.  Neste listado poderanse ver as asignacións xenéricas (o nome sería a  lista de criterios satisfeita, horas e número de recursos por día). Cada  asignación realizada pode ser borrada explicitamente premendo no botón  de borrar.

   * Introducir o nome do recurso ou criterio desexado no campo "Seleccione criterios ou recursos". Tamén é posible premer en "Búsqueda avanzada" para realizar unha procura avanzada.
   * Se se elixiu a primeira opción: O sistema amosará un listado que cumpra coas condicións de procura. O usuario debe elixir o recurso ou criterio que desexa e premer en "Engadir".

      * Se o usuario elixe un recurso, NavalPlan realizará unha asignación específica. Ver sección "Asignación específica" para  coñecer que significa elixir esta opción.
      * Se o usuario elixe un criterio, NavalPlan realizará unha asignación xenérica. Ver sección "Asignación xenérica" para coñecer que significa elixir esta opción.

   * Se se elixiu a segunda opción: A aplicación amosa unha nova pantalla formada por unha árbore de  criterios e un listado á dereita dos traballadores que cumpren os criterios seleccionados:
      * Seleccionar o tipo de asignación a realizar:

         * Asignación específica. Ver sección "Asignación específica" para  coñecer que significa elixir esta opción.
         * Asignación xenérica. Ver sección "Asignación xenérica para coñecer  que significa elixir esta opción.

      * Seleccionar unha lista de criterios (asignación xenérica) ou unha lista de traballadores (asignación específica). A elección  múltiple realízase premendo no botón "Crtl" á hora de pulsar en cada traballador ou criterio.
      * Premer no botón "Seleccionar". É  importante ter en conta que, se non se marca asignación xenérica, é  necesario escoller un traballador ou máquina para poder realizar unha  asignación, en caso contrario chega con elixir un ou varios criterios.
   * A aplicación amosa no listado de asignacións da pantalla orixinal de asignación de recursos a lista de criterios ou  recursos seleccionados.
   * Cubrir as horas ou o número de recursos por día dependendo da estratexia de asignación que se solicitou levar a cabo á aplicación.
   * Premer no botón Aceptar para marca a asignación como feita. É importante reseñar que a operación non será consolidada ata que se pulse na icona de gravar da vista de planificación, se se sae da vista de planificación perderanse os cambios.
   * O planificador calculará a nova duración das tarefas en base a asignación realizada.

A vista expandida é amosada se se marca o *checkbox* que aparece ó lado do texto "vista expandida". Esta vista é útil para a visualización de datos con consolidación de progresos. Os campos amosados son:

   * Nome: Nome da asignación (criterio asignado ou recurso asignado).
   * Horas. Orixinal: Horas orixinalmente asignadas ó recurso ou criterio anterior.
   * Horas. Consolidado: Horas que se consolidaron nunha data concreta como horas que representan o progreso consolidado.
   * Horas. Non consolidado: Horas que quedarían por facer da tarefa, unha vez se consolidaron unha porcentaxe das horas nunha data concreta.
   * Horas. Total: Ratio de recursos por día total da tarefa.
   * Horas. Consolidado: Ratio de recursos por día das horas xa consolidadas da tarefa.
   * Horas. Non consolidado: Ratio de recursos das horas nonn consolidadas da tarefa.

Asignación de recursos específicos
==================================

A asignación específica é aquela asignación de un recurso de xeito concreto e específico a unha tarefa de un proxecto, é dicir, o usuario da aplicación está decidindo que "nome e apelidos" ou qué "máquina" concreta debe ser asignada a unha tarefa.

A aplicación, cando un recurso é asignado específicamente, crea  asignacións diarias en relación á porcentaxe de recurso diario que se  elixiu para asignación, contrastando previamente co calendario dispoñible do recurso. Exemplo: unha asignación de 0.5 recursos para  unha tarefa de 32 horas fai que se asignen ó recurso específico  (supoñendo un calendario laboral de 8 horas diarias) 4 horas diarias para realizar a tarefa.

Para realizar a asignación a un recurso específico é preciso centrarse nos seguintes pasos na pestana de asignación de recursos dunha tarefa.

   * Introducir un nome ou apelidos de recurso no campo de procura que sae á dereita do texto "Seleccione criterios ou recursos" e seleccionar o recurso de entre os que cumpren os criterios de filtrado. Premer en "Engadir".
   * Outra opción sería:
      * Pulsar na opción de *Búsqueda avanzada*
      * Marcar asignación específica coma tipo de asignación.
      * Filtrar os recursos empregando os criterios que cumpre.
      * Seleccionar un recurso ou varios (empregando Ctrl+Selección co rato).
      * Premer no botón Seleccionar.
   * Na vista xeral de asignación indicar a carga de traballo diaria de cada recurso ou o número de horas asignadas. Este campo dependerá do tipo de calculo seleccionado na asignación.
   * Premer Aplicar ou Aplicar cambios da pestana.
   * Una vez completada a asignación gravar a planificación do proxecto e consultar a carga dos recursos asignados.

Asignación de recursos xenérica
===============================

A asignación xenérica e unha das aportacións de máis interese da aplicación. Nunha parte importante dos traballos non é interesante coñecer a priori quen vai a realizar as tarefas dun proxecto. Nese caso ó unico que interesa para realizar unha asignación e identificar os criterios que teñen que cumprir os recursos que poden facer esa tarefa. O concepto de asignación xenérica repersenta a asignación por criterios en lugar de por persoas. O sistema será o encargado de realizar a asignación entre os recursos que cumpran os criterios necesarios. O sistema fará unha asignación totalmente arbitraria pero que será válida a efectos de coñecer a carga xeral dos recursos da empresa.

A asignación de recursos a unha tarefa segue o calendario definido para o proxecto tendo en conta o número de recursos asignados que cumpran os criterios definidos.

Para realizar a asignación a  un recurso xenérico so é preciso centrarse nos seguintes pasos na  pestana de asignación de recursos dunha tarefa.

   * Introducir un nome de criterio no campo de procura que sae á dereita do texto "Seleccione criterios ou recursos" e seleccionar o recurso de entre os que cumpren os criterios de filtrado. Premer en "Engadir".
   * Outra opción sería:
      * Pulsar na opción de  *Búsqueda avanzada*
      * Marcar asignación  xenérica coma tipo de asignación.
      * Seleccionar un ou varios criterios (empregando Ctrl+Selección co rato).
      * Premer no botón Seleccionar.
   * Na vista xeral de  asignación indicar a carga de traballo diaria para a asignación xenérica ou o  número de horas asignadas. Este campo dependerá do tipo de calculo  seleccionado na asignación.
   * Premer Aplicar ou  Aplicar cambios da pestana.
   * Una vez completada a  asignación gravar a planificación do proxecto e consultar a carga dos  recursos asignados.

Cando se fai unha asignación xenérica non de ten o control sobre que recursos se asigna a carga de traballo. O sistema fará un reparto sobrecargando equitativamente aos recursos se fora necesario se non existe capacidade suficiente nese momento do tempo dos recursos que cumpren os criterios da tarefa.

Asignación recomendada
----------------------

Na vista de asignación e posible marcar a **Asignación recomendada**. Esta opción permite á aplicación recoller os criterios que deben ser satisfeitos e as horas totais de cada grupo de horas e fai unha proposta de asignación xenérica recomendada. Isto garante que as horas a asignar coinciden coas horas orzamentadas así como o seu reparto por criterios.

Se había  unha asignación previa, o sistema elimina dita asignación substituíndoa pola nova. A asignación que se realiza será sempre unha asignación xenérica sobre os criterios existentes no proxecto.

Revisión de asignación na pantalla de carga de recursos
=======================================================

No momento de contar con  recursos asignados a tarefas dun proxecto ten sentido consultar a carga  que teñen os recursos asignados. Para iso contase coa segunda  perspectiva denominada carga de recursos.

Nesta vista vese a  información dos recursos específicos ou xenéricos asignados ao proxecto así coma a carga, coa información das  tarefas as que teñen sido asignados os mesmos.

Nun primeiro nivel mostrase  o nome do recurso e ao seu carón mostrase unha liña gráfica que indica a  carga do recurso no tempo. Se nun intervalo a barra está en vermello  o  recurso se atopa sobrecargado por riba do 100%, en laranxa se a carga  está ao 100% e en verde se a carga é inferior ao 100%.  Esta barra marca  con liñas verticais blancas os cambios de asignacións de tarefas.

Ao posicionarse co punteiro  rato por riba da barra e esperar uns segundos aparecerá o detalles da  carga do recurso en formato numérico.

Por cada liña de recurso pódese expandir a  información e consultar as tarefas e a carga que supón cada unha delas.  Pódense identificar as tarefas do proxecto xa que aparecen coa  nomenclatura Nome do proxecto: :Nome da tarefa. Tamén se mostran tarefas  doutros proxectos para poder analizar as causas das sobrecargas dos  proxectos. Cando a carga e debida nun recurso específico é debida a unha asignación xenérica amósase a tarefa cos nome dos criterios entre Corchetes. Tamén é posible coñecer qué tarefas de outros proxectos están cargando o recurso en cuestión.

Esta  perspectiva permite coñecer en detalle a situación dos recursos con  respecto as tarefas do proxecto.

Revisión de asignacións na pantalla  de asignación avanzada
===========================================================

Una vez se está consultando  a información dun proxecto se este proxecto ten asignacións pódese acceder  a perspectiva de vista de asignación avanzada. Nesta vista vese o  proxecto coma unha táboa que mostra tarefas e recursos asignados a mesma  ao longo do tempo. Sendo filas as tarefas e cada asignación a un recurso  recursos un subelemento da fila. E sendo as columnas as unidades de  tempo dependendo do nivel definido de Zoom.

Nesta vista pódese cotexar o  resultado da asignación diaria de cada unha das asignacións específicas  feitas previamente. Existen dous modos de acceder á asignación  avanzada:

   * Accedendo a un proxecto  concreto e cambiar de perspectiva para  asignación avanzada. Neste caso  amosaranse todas as tarefas do proxecto e  os recursos asignados (tanto  específicos como xenéricos).
   * Accedendo á asignación  de recursos e premendo no botón "Asignación  avanzada". Neste caso  amosaranse as asignacións da tarefa para a que se  está asignando  recursos (amósanse tanto as xenéricas como as específicas).

Pódese  acceder ó nivel de  zoom que desexe:

   * Se o  zoom elixido é un zoom superior a día. Se o usuario modifica o  valor  de horas asignado á semana, mes, cuadrimestre ou semestre, o  sistema  reparte as horas de xeito lineal durante todos os días do  período  elixido.
   * Se o zoom elixido é un zoom de día. Se o usuario  modifica o valor de  horas asignado ó día, estas horas só aplican ó  día. Deste xeito o  usuario pode decidir cantas horas se asignan  diariamente ós recursos da tarefa.

   Para  consolidar os cambios da asignación avanzada é preciso premer o botón de *Gardar*. É importante que o total de horas coincida co total de horas asignadas a un intervalo temporal.

Na pantalla de asignación avanzada é posible realizar asignacións en base a funcións:
   * Función lineal por tramos. Calcula tramos lineais en base a unha serie de puntos dadots polos pares: punto que marca un momento na tarefa, porcentaxe de avance esperado.
   * Función de interpolación polinómica. Función que en base a unha serie de puntos dados polos pares (punto que marca un momento na tarefa, porcentaxe de avance esperado) calcula o polinomio que satisfai a curva.

Creación de fitos
=================

Na planificación dun proxecto poden existir fitos, os fitos considéranse coma tarefas que non teñen traballo asociado, polo que non poden ter asignacións. A principal utilidade dos fitos como pode ser o de fin de proxecto, unha auditoría ou un punto de control e establecer dependencias entre tarefas dunha forma cómoda.

Dende a vista de planificación du proxecto pódese crear un fito seguindo os seguintes pasos:

   * Seleccionar unha tarefa para marcar a posición gráfica onde se quere crear o fito.
   * Pulsar co botón dereito sobre a tarefa e seleccionar sobre o menú contextual *Engadir fito*
   * Crearase un fito xusto debaixo da tarefa seleccionada.
   * Pódese desplazar o fito no tempo adiantando ou atrasando a súa data, ou editar na columna da esquerda a súa data de inicio.
   * Pódense engadir dependencias dende ou cara ao fito.
   * Pódese borrar un fito existente.

Restricións das tarefas
=======================

As tarefas poden incorporar unha serie de restricións temporais as que indican que unha tarefa :

   * debe empezar o antes posible (TAN PRONTO COMO SEA POSIBLE)
   * non debe comezar antes dunha data (COMEZAR NON ANTES DE)
   * debe comezar nunha data fixa (COMEZAR EN DATA FIXA)
   * non debe acabar despois de  (ACABAR NON DESPOIS DE)
   * acabar o máis tarde posible (TAN TARDE COMO SEXA POSIBLE)

Para incorporar estas restricións debense seguir os seguintes pasos:

   * Pulsar co botón dereito sobre a tarefa a que se lle quere incorporar a restrición dende a vista de planificación.
   * Seleccionar no menú contextual *Propiedades da tarefa*
   * Na vista de propiedades seleccionar o tipo de restrición que interese. No casos das restricións que fan referencia a unha data debese cubrir a data da restrición neste punto.
   * Premer na opción de aceptar e gardar a planificación cando se termine coas modificacións.

A aplicación de restricións nas tarefas pode implicar que non se cumpran unha serie de dependencias, no caso de que exista algunha incompatibilidade terá preferencia por defecto as restricións sobre as dependencias, pero isto será configurable co parametro *As dependencias teñen prioridade* nas propiedades xerais do proxecto.

É posible definir na vista gráfica dependencias do tipo COMENZAR NON ANTES DE se se despraza co rato as tarefas directamente na vista de Gantt, e establecerase a data da restricións en base ao punto onde se deposite. Ainda que esta operación poida ser intuitiva e complexo axustar o día da restrición con niveis de zoom superiores ao día.

Asignación de calendarios a tarefas
===================================

Os proxectos teñen asociado un calendario que se tomará como referencia para o calendario das tarefas. Este calendario define os días que se traballan nunha tarefa así coma o número de horas por defecto por día nas asignacións xenéricas.

É posible asociar un calendario a unha tarefa da seguinte forma:

   * Pulsar co botón dereito sobre a tarefa a que se lle quere cambiar o calendario dende a vista de planificación.
   * Seleccionar no menú contextual *Asignación de Calendario*
   * Seleccionase o calendario de interese para a tarefa.
   * Premer na opción de asignar e gardar a planificación cando se termine coas modificacións.

Vista do gráfico global de carga de recursos da empresa
=======================================================

De forma paralela a vista de recursos dun proxecto, pódese consultar a vista xeral de recursos da empresa. Esta vista permite cotexar a planificación dos recursos dispoñibles. Pódese acceder dende a vista de planificación de empresa premendo na perspectiva de *Uso dos recursos*.

Nesta vista vese a  información de tódolos recursos específicos ou xenéricos que teñen algúnha asignación a algún proxecto. Mostrase a carga dos mesmos coa información das  tarefas as que teñen sido asignados. A diferencia da vista de carga a nivel proxecto aquí mostranse tódalas asignacións de tódolos recursos da empresa.

Nun primeiro nivel mostrase  o nome do recurso e ao seu carón mostrase unha liña gráfica que indica a  carga do recurso no tempo. Se nun intervalo a barra está en vermello  o  recurso se atopa sobrecargado por riba do 100%, en laranxa se a carga  está ao 100% e en verde se a carga é inferior ao 100%.  Esta barra marca  con liñas verticais blancas os cambios de asignacións de tarefas.

Ao situarse co punteiro do rato por riba da barra e esperar uns segundos aparecerá o detalles da  carga do recurso en formato numérico.

Por cada liña de recurso pódese expandir a  información e consultar as tarefas e a carga que supón cada unha delas.  Pódense identificar as tarefas do proxecto xa que aparecen coa  nomenclatura Nome do proxecto: :Nome da tarefa. Tamén se amosan tarefas  doutros proxectos para poder analizar as causas das sobrecargas dos  proxectos. Cando a carga e debida nun recurso específico é debida a unha asignación xenérica mostrase a tarefa cos nome dos criterios entre Corchetes.

Esta  perspectiva permite coñecer en detalle a situación dos recursos da empresa.

-------------------
Módulo de progresos
-------------------

Conceptos teóricos
==================

O progreso ou avance é unha medida que indica en que grao está feito un traballo. En NavalPlan os progresos se xestionan a dous niveis:

   * Tarefa. unha tarefa representa un traballo a ser realizado e, consecuentemente, é posible no programa medir o progreso dese traballo.
   * Proxecto, equivalencia de proxecto. Os proxectos de forma global tamén teñen un estado de progreso según o grao de completitude que teñen.

O progreso ten que ser medido manualmente polas persoas encargadas da planificación na empresa porque é un xuízo que se leva en base a unha valoración do estado dos traballos.

As características máis importantes do sistema de progresos en NavalPlan é o seguinte:

   * É posible ter varias maneiras de medir o progreso sobre unha determinada tarefa. Debido a elo, os progresos caracterízanse por ser medidos en diferentes unidades e son administrables os distintos tipos de progresos.
   * Programouse un sistema de propagación de progresos de maneira que se un progreso se mide a un determinado nivel da árbore de proxectos, entón calcúlase no nivel superior automáticamente cal debería ser o progreso en función das horas representadas polos fillos que teñan medido ese tipo de progreso.
   * Na vista de planificación, tanto a vista a nivel de empresa como a nivel de proxecto, sobre as tarefas que representan os puntos de planificación como os contedores das mesmas teñen a capacidade de representar graficamente un dos progresos da tarefa.

Administración de tipos de progreso
===================================

A administración de tipos de progreso permite ao usuario definir as distintas maneiras nas que desexa medir os progresos sobre as tarefas e proxectos. Para dar de alta un tipo de progreso hai que levar a cabo os seguintes pasos:

   * Ir a opción *Administración / Xestión* > *Tipos de datos* -> *Progreso*.
   * Premer no botón **Crear**.
   * Cubrir no formulario que se mostra os seguintes datos:

      * Nome da unidade. Nome do progreso polo que se vai a identificar. Normalmente será o nome da unidade. Non pode haber dous tipos de progreso co mesmo nome de unidade.
      * Activo. É necesario marcar esta opción se o usuario quere utilizar este tipo de progreso.
      * Valor máximo por defecto. Cando o usuario introduce un tipo de progreso nunha tarefa ten que seleccionar que valor representa a finalización do traballo. Pois ben, este valor máximo por defecto é o valor que primeiramente se asigna como valor que representa o 100% cando se realiza unha alta dun progreso deste tipo nunha tarefa.
      * Precisión. A precisión indica cal é a precisión decimal na cal se poden introducir as asignacións de progreso dun determinado tipo.
      * Porcentaxe. Se se indica que un tipo de progreso está marcado como porcentaxe significa que o valor máximo vai a estar predefinido ao valor 100 e non se ofrecerá ao usuario a posibilidade de cambialo cando se asigne a unha tarefa.

   * Premer no botón Gardar.

Borrado de tipo de progreso
---------------------------

O borrado dun tipo de progreso só ten sentido no caso de que non fora asignado nunca. Ademais, existen tipos de progreso predefinidos en NavalPlan necesarios para o seu funcionamento. Esto tipos de progreso predefinidos tampouco se poden borrar.

Se este é o caso hai que dar os seguintes pasos:

   * Ir a opción *Administración / Xestión* > *Tipos de datos* -> *Progreso*.
   * Identificar a fila correspondente o tipo de progreso que se desexa borrar.
   * Pulsar na icona da papeleira.
   * Se desprega unha ventá emerxente no cal se pide confirmación. Pulsar en Si.

Asignación de tipos de progresos a tarefas
==========================================

Esta operación consiste en configurar a medición do progreso dun determinada tarefa a través dun tipo de progreso. Para asignar un tipo de progreso a unha tarefa ten que cumprirse unha serie de regras:

   * Non debe existir ningunha asignación do tipo de progreso desexado nalgún dos seus descendentes.
   * Non debe existir ningunha asignación do tipo de progreso desexado nalgún dos seu antecesores.

O anterior quere dicir que o tipo de progreso so pode estar asignado en outra rama da árbore, non no recurrido que vai dende a tarefa ata a raíz e dende a tarefa cara tódolos seus descendentes.

Para dar de alta o tipo de progreso nunha tarefa hai dúas opcións:

Opción 1:
   * Ira a opción Planificacion > Planificación de proxectos.
   * Facer doble click sobre o proxecto que se desexa xestionar.
   * Premer sobre a tarefa que se desexa con botón dereito e elixir a operación "Asignacións de progreso".
   * Na pestana hai unha primeira área recadrada denominada **Asignación de progresos**. O usuario debe premer o botón **Engadir nova asignación de progreso**.
   * Nese momento se engade unha nova fila a táboa de tipos de progreso asignados. Na columna tipo aparece un selector no que hai que seleccionar o tipo de progreso.
   * Introducir o valor máximo para as medicións dese tipo de progreso sobre o order element.
   * Premer no botón da parte inferior **Atrás**
   * Facer clic co rato na icona de gardar, representado por un disquete, na barra de accións.

Opción 2:
   * Ira a opción Planificacion > Proxectos.
   * Seleccionar a fila que se corresponda co proxecto no cal se desexa configuración un tipo de progreso para medir o progreso.
   * Premer no botón editar do proxecto.
   * Seleccionar a pestana **EDT (Tarefas)**
   * Identificar a tarefa sobre o que se quere configurar o tipo de progreso.
   * Premer sobre o botón editar ta tarefa.
   * Sobre a ventá emerxente que aparece, seleccionar a pestana progresos.
   * Na pestana hai unha primeira área recadrada denominada **Asignación de progresos**. O usuario debe premer o botón **Engadir nova asignación de progreso**.
   * Nese momento se engade unha nova fila a táboa de tipos de progreso asignados. Na columna tipo aparece un selector no que hai que seleccionar o tipo de progreso.
   * Introducir o valor máximo para as medicións dese tipo de progreso sobre a tarefa.
   * Premer no botón da parte inferior **Atrás**
   * Facer clic co rato na icona de gardar, representado por un disquete, na barra de accións.

Engadir lectura de progreso sobre un tipo de progreso asignado nunha tarefa
===========================================================================

Esta operación pode ser levada a cabo unha vez que se configurou previamente unha medición de tipo de progreso sobre unha tarefa. Partindo deste suposto, os pasos para engadir unha lectura de progreso sobre un tipo de progreso asignado a unha tarefa son os seguintes:


   * Opción 1: Ira a opción Planificacion > Planificación de proxectos.
      * Facer doble click sobre o proxecto que se desexa xestionar.
      * Premer sobre a tarefa que se desexa con botón dereito e elixir a operación "Asignacións de progreso".
   * Opción 2: Ir a opción Planificacion > Proxectos.
      * Seleccionar a fila que se corresponda co proxecto no cal se desexa configuración un tipo de progreso para medir o progreso.
      * Premer no botón editar do proxecto.
      * Seleccionar a pestana **EDT (Tarefas)**
      * Identificar a tarefa sobre o que se quere configurar o tipo de progreso.
      * Premer sobre o botón editar ta tarefa.
      * Sobre a ventá emerxente que aparece, seleccionar a pestana progresos.
   * Dentro da táboa incluida na área recadrada como **Asignación de progresos** elixir o tipo de progreso ó que se lle desexa asignar medida premendo en "Engadir medida"
   * Coa pulsación anterior engádese unha nova fila na sección inferiordenominada **Medidas de progreso** e se escribe ao lado do título **Medidas de progreso** o tipo de progreso que se acaba de seleccionar. Ademáis cárganse na táboa desa sección todas as lecturas de progreso que ata ese momento se teñen do tipo de progreso seleccionado. O usuario debe cubrir nela os datos:

      * Valor. Aquí debe introducir a medida de progreso nas unidades que define o tipo de progreso. O valor máximo ven determinado pola configuración da asignación do tipo de progreso á tarefa e a preción polo valor de precisión determinado polo tipo de progreso.
      * Data. A data indica cal é o día ao cal corresponde esta medición de progreso.
      * Porcentaxe. Esta columna é unha columna calculada e informa de que porcentaxe representa a medición de progreso considerando que a tarefa rematada é un 100%.

   * Premer no botón **Atrás**
   * Facer clic co rato na icona de gardar, representado por un disquete, na barra de accións.

É importante resaltar que asignando progreso sobre unha tarefa concreta ou sobre unha caixa de Gantt dunha tarefa correspondente coa anterior tarefa, a operación realizada é a mesma.

Amosado da evolución de lecturas de progreso graficamente
==========================================================

Sobre a pantalla de configuración de medidas de progreso é posible ver a evolución graficamente de un ou máis tipos de progreso configurados graficamente. Para elo o que hai que realizar é:

   * Na pantalla de "Asignación de progresos" (ver seccións anterires para acceder a esta ventá), seleccionar a columna *Mostrar* de cada un dos tipos de progreso que se queiran ver graficamente.
   * Observar na gráfica cal é a evolución das lecturas dos tipos de progreso seleccionados no tempo.

Configuración de propagación de tipo de progreso
================================================

Propagar é a operación que permite calcular o avance en nodos superiores en base ós nodos fillos, de modo que o tipo de avance que sexa propagado cara un pai, será o utilizado para calcular o avance de dito pai.

Existe unha columna na táboa de asignación de tipos de progreso a elementos de proxecto que é un botón radio que forma un conxunto con tódolos tipos de progreso asignados á tarefa que se está configurando. Isto significa que é unha columna que ten que estar marcada unha delas como que propaga e non pode haber máis con este atributo.

O tipo de progreso configurado sobre unha tarefa marcado como que propaga é o seleccionado para representar a tódolos tipos de progreso existentes na tarefa e será o utilizado para calcular cal é o progreso da tarefa pai - en caso de ter pai - en base aos progresos marcados como que propagan en cada un dos seus fillos. O cálculo consiste en ponderar o progreso de cada fillo en función da carga en horas de traballo que cada un aporta con respecto ao total do pai.

Para configurar o tipo de progreso que propaga nunha tarefa hai que seguir a secuencia seguinte de accións:

   * Ir a opción Planificacion > Lista de proxectos.
   * Seleccionar a fila que se corresponda co proxecto no cal se desexa configuración un tipo de progreso para medir o progreso.
   * Premer no botón editar do proxecto.
   * Seleccionar a pestana **Elementos de proxecto**
   * Identificar a tarefa sobre o que se quere configurar o tipo de progreso que propaga.
   * Premer sobre o botón editar da tarefa.
   * Sobre a ventá emerxente que aparece, seleccionar a pestana progresos.
   * Na sección **Asignación de progresos** seleccionar a fila do tipo de progreso desexado e marcar o botón de radio.
   * Premer no botón **Atrás**
   * Facer clic co rato na icona de gardar, representado por un disquete, na barra de accións.

Visualización de progresos xerais sobre vista de planificación de proxecto
==========================================================================

Na vista de planificación de proxecto amósanse as tarefas marcados como puntos de planificación e os seus ancestros, que aparecen como tarefas de planificación contedoras, a información dos tipos de progreso que propagan en cada nodo. Se non existen tipos de progreso configurados non se amosa ningunha información.

A información dun tipo de progreso de progreso sobre unha tarefa amósase graficamente a través dunha barra de cor verde que se pinta na metada inferior das tarefas e dos contedores. Esta información de progreso se mostra da seguinte maneira:

   * Represéntase a medición de progreso máis recente do tipo de progreso configurado como que propaga sobre a tarefa asociado a tarefa de planificación (tarefa contedora ou final).
   * Esta barra ten unha lonxitude que está relacionada coa lectura de progreso última e coa asignación de traballo que ten a tarefa ao longo do tempo. O algoritmo para o pintado é o seguinte:

      * Das horas planificadas da tarefa se calcula qué numero de horas representa a porcentaxe de progreso medida mais recente sobre o total de horas.
      * Vaise sumando as horas que se planifican cada dia dende o comezo da tarefa ata que se chega a igualar ou superar o numero de horas calculado no punto anterior.
      * Mirase que data é na que ocorre a igualación ou superación e se pinta a barra ata ese día.

Con este algoritmo a barra pintase de forma correcta cando o número de horas adicadas na tarefa non é constante ao longo de toda a duración da tarefa. Se o usuario se pon sobre a tarefa de planificación sae un texto emerxente que informa da porcentaxe de progreso que representa a barra.

Para ver a información de progreso dun proxecto é acceder a perspectiva de planificación dun proxecto.

Visualización de progresos xerais sobre vista de planificación de empresa
=========================================================================

Os proxectos son o nivel de agrupamento superior, como xa se dixo, das tarefas. A explicación da vista dos proxectos da empresa en forma de diagrama de gantt realízase dende a vista explicada no punto de proxectos, vista de empresa.

Pois ben, nesa vista de empresa se o proxecto ou as tarefas do seu interior teñen configurados tipos de progreso como que propagan e teñen lecturas de progreso, entón tamén se amosan na vista de empresa a nivel de proxecto.

A representación do progreso sobre o proxecto, é a misma que o explicado para as tarefas.

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


Consolidación progresos
=======================

Ainda que é posible introducir avances no sistema, ditos avances non se traducen en cambios nas tarefas e nas asignacións das mesmas. Sen embargo, consolidando os avances introducidos, sí se produce dito efecto. Consolidar unha tarefa significa asentar o avance para unha data dada definitivamente. Para consolidar un avance é necesario realizar os seguintes pasos:

   * Premer co botón dereito na tarefa elixida.
   * Elixir o primeiro dos avances a consolidar.
   * Premer en "Aceptar".
   * Gardar o proxecto.

Para entender a consolidación de progreso, é necesario analizar dous supostos. Supoñendo unha tarefa de 40 horas e un calendario de 8 horas diarias, e a tarefa conta con asignacións de 8 horas durante 5 días.

   * Exemplo 1: Introducíuse un progreso do 60% no segundo día e consolídase:

      * NavalPlan busca cantas horas se fixeron ata o día no que se introduxo o progreso. No exemplo, 16 horas correspondentes ás asignacións de 2 días de 8 horas cada día.
      * O sistema calcula canto quedaría por finalizar (no exemplo, quedaría un 40% da tarefa). En consecuencia quedan un 40% das 40 horas, o cal significa que quedan 16 horas.
      * O sistema entón marca como consolidadas as horas que calculou no primeiro punto (16h) e marca como que quedan 16 horas calculadas no segundo punto. En consecuencia, a tarefa ten agora unha duración de 32h.
      * NavalPlan, coas horas que quedan por facer, ás últimas 16h, non recalcula datas, senón que recalcula o ratio de recursos por día que se necesitan para poder finalizar na data inicial. É o usuario quen pode establecer o ratio orixinal de recursos por día para que se recalculen as asignacións e en consecuencia as datas de finalización.
      * Neste exemplo, se se establece 1 recurso por día ó que resta da tarefa contaríase con 1 día de adianto.

   * Exemplo 2: Introducíuse un progreso do 40% no cuarto día e consolídase:

      * NavalPlan busca cantas horas se fixeron ata o día no que se introduxo o progreso. No exemplo, 32 horas correspondentes ás asignacións de 4 días de 8 horas cada día.
      * O sistema calcula canto quedaría por finalizar (no exemplo, quedaría un 60% da tarefa). En consecuencia quedan un 60% das 40 horas, o cal significa que quedan 24 horas.
      * O sistema entón marca como consolidadas as horas que calculou no primeiro punto (32h) e marca como que quedan 24 horas calculadas no segundo punto. En consecuencia, a tarefa ten agora unha duración de 56h.
      * NavalPlan, coas horas que quedan por facer, ás últimas 24h, non recalcula datas, senón que recalcula o ratio de recursos por día que se necesitan para poder finalizar na data inicial. É o usuario quen pode establecer o ratio orixinal de recursos por día para que se recalculen as asignacións e en consecuencia as datas de finalización.
      * Neste exemplo, se se establece 1 recurso por día ó que resta da tarefa contaríase con 2 día de atraso.


Escenarios
=============

Os escenarios representan diferentes entornos de traballo. Os escenarios comparten certas tipos de datos que son comúns, outras poden pertencer a varios escenarios e outras son completamente diferentes:

   * Tipos de entidades comúns: criterios, etiquetas, etc.
   * Tipos de entidades que poden ser comúns: proxectos, tarefas e a asociación de datos ós mesmos.
   * Tipos de entidades independentes: asignacións de horas

Cando un usuario cambia de escenario, as asignacións de horas son diferentes entre proxectos porque as condicións poden ser diferentes, por exemplo, un novo proxecto que existe nun novo escenario.

As operacións básicas de operación entre escenarios son:

   * Creación de escenario
   * Cambio de escenario
   * Creación de proxecto en escenario
   * Envío de proxecto de un escenario a outro. Esta operación copia toda a información de un proxecto de un escenario a outro, excepto as asignacións de horas.

Os escenarios son xestionados dende a opción de menú "Escenarios" onde é posible administrar os escenarios existentes e crear novos. Por outro lado existe un botón de acceso rápido a escenario na zona dereita superior de NavalPlan.

As operacións de escenarios só se amosan se se configura na sección *Administración* > *NavalPlan: Configuración* que se amosen estas operacións.
