Introdución
###########

.. contents::

O propósito deste documento é describir as características de LibrePlan e
proporcionar información ós usuarios sobre como configurar e utilizar a
aplicación.

LibrePlan é unha aplicación web de código aberto para a planificación de
proxectos. O seu obxectivo principal é proporcionar unha solución completa para a
xestión dos proxectos da empresa.
Para calquera información específica que necesites sobre este software, por
favor ponte en contacto co equipo de desenvolvemento en
http://www.libreplan.com/contact/


.. figure:: images/company_view.png
   :scale: 50

   Vista global de empresa

Vista global da empresa e xestión de perspectivas
=================================================

Tal e como se visualiza na anterior captura, a principal pantalla da aplicación e a vista global da empresa, unha vista na que o usuario poderá visualizar a lista de proxectos planificados para coñecer o estado global da empresa, tanto a nivel de pedidos, como de uso de recursos. A vista global de empresa está, asimesmo, formada por 3 perspectivas:

* Vista de planificación: Vista que conxuga dous puntos de vista:

   * Vista dos pedidos e a sua temporalidade: Cada proxecto é unha caixa de diagramas de Gantt indicando a data de comezo e de fin do proxecto. Ademais, combínase dita información co amosado da data acordada de finalización (*deadline*) e con un contraste entre porcentaxe de avance e horas adicadas realmente a cada proxecto. Esta información da unha visión clara de como se atopa a empresa nun momento dado. Esta vista é a portada da aplicación.
   * Gráfica de uso dos recursos da empresa: Gráfica que busca a información de asignacións dos recursos ós proxectos e que ofrece un resumo de como está o uso dos recursos de toda a empresa: a cor verde indica asignacións de recursos por debaixo do 100%, a liña negra indica a carga dispoñible de recursos e a cor amarela indica as asignacións a recursos que están por enriba do 100%. É posible dispor de menos asignacións que recursos dispoñibles e ó mesmo tempo contar con sobreasignacións en recursos concretos.

* Vista de carga de recursos: Pantalla que amosa o listado de traballadores da empresa e a carga debido a asignacións específicas a tarefas ou asignacións xenéricas debido a que o recurso satisfai unha lista de criterios. Ver a seguinte imaxe. Para acceder a esta vista é necesario premer en *Carga global de recursos*.
* Vista de administración de pedidos. Pantalla que amosa o listado de pedidos da empresa onde o usuario poderá realizar as seguintes operacións: filtrar, editar, borrar, visualizar en planificación ou crear novo pedido. Para acceder a esta vista é necesario premer en *Lista de pedidos*.

.. figure:: images/resources_global.png
   :scale: 50

   Vista global de recursos

.. figure:: images/order_list.png
   :scale: 50

   Listado de pedidos

A xestión de perspectivas que se comentou para a vista global de empresa é moi similar á prantexada para un só proxecto. O acceso a un proxecto pódese realizar de varias formas:

* Premendo no botón dereito sobre a caixa de diagrama de Gantt do pedido e seleccionando en *Planificar*.
* Accedendo ó listado de pedidos e premendo na icona simbolizando os diagramas de Gantt.
* Creando un novo pedido e cambiar de perspectiva sobre o pedido sendo visualizado.

Sobre un pedido, a aplicación amosa as seguintes perspectivas:

* Vista de planificación. Vista na que o usuario pode visualizar a planificación das tarefas, dependencias, fitos, etc. Ver sección de *Planificación* para máis información.
* Vista de carga de recursos. Vista na que o usuario pode comprobar a carga dos recursos asignados ó proxecto. O código de cores é o mesmo que na vista global de empresa: verde para carga menor ó 100%, amarelo para carga igual a 100% e vermello para carga maior a 100%. A carga pode vir dada por unha tarefa ou por unha lista de criterios (asignación xenérica).
* Vista de edición de pedido. Vista na que o usuario pode administrar os datos do pedido. Ver sección de *Pedidos* para máis información..
* Vista de asignación avanzada de recursos. Vista na que o usuario pode asignar os recursos de xeito avanzado, seleccionando as horas por día ou as funcións de asignación que desexa aplicar. Ver sección de *Asignación de recursos* para máis información.

¿Por que me é útil LibrePlan?
=============================

LibrePlan é unha aplicación desenvolvida como unha ferramenta de
planificación de propósito xeral. Baséase nunha serie de conceptos
descubertos mediante o análisis de problemas na planificación de proxectos
industriais que non estaban completamente cubertos por ningunha ferramenta de
planificación existente. Outras das motivacións para o desenvolvemento de
LibrePlan se baseaban en proporcionar unha alternativa de software libre, e
completamente web, ás ferramentas de planficación privativas existentes.

Os conceptos esenciais que se utilizan para o programa son os seguintes:

* Vista global de empresa e multiproxecto: LibrePlan é unha aplicación orientada especificamente a dotar de información ós usuarios dos proxectos que se levan a cabo nunha empresa, polo que a base é multiproxecto. Non se determinou que o enfoque do proxecto sexa orientado individualmente a cada proxecto. Sen embargo, tamén será posible dispoñer de varias vistas específicas, entre elas a de proxectos individuais.
* Xestión de perspectivas: A vista global de empresa ou vista multiproxecto vese complementada coas perspectivas sobre a información que se almacena. Por exemplo, a vista global de empresa permite visualizar os pedidos e contrastar o estado dos mesmos, visualizar a carga xeral de recursos da empresa e administrar os pedidos. Por outro lado, na vista de proxecto, é posible visualizar a planificación, a carga de recursos, a vista de asignación de recursos avanzada e a edición do pedido relacionado.
* Criterios: Os criterios son unha entidade do sistema que permitirán clasificar os recursos (tanto humanos como máquinas) e as tarefas. Dende o punto de vista dos recursos, estes satisfarán criterios e, dende o punto de vista das tarefas, estas requirirán criterios a ser satisfeitos. Correspóndense con un dos aspectos máis importantes da aplicación, xa que os criterios formarán parte da base das asignacións xenéricas na aplicación, resolvendo un dos problemas máis importantes para o sector, a alta temporalidade dos recursos humanos e a dificultade para ter estimacións de carga da empresa a longo prazo.
* Recursos: Son de dous tipos diferentes: humanos e máquinas. Os recursos humanos son os traballadores da empresa que se utilizan para planificar, monitorizar e controlar a carga da empresa. E por outro lado, as máquinas, dependentes das persoas que as xestionan, son outros recursos que actúan de xeito similar ós recursos humanos.
* Asignación de recursos: Unha das claves é o feito de ofrecer a posibilidade de dous tipos diferentes de asignación: asignación específica e asignación xenérica. A xenérica é unha asignación baseada nos criterios que se requiren para realizar a unha tarefa, e que deben ser satisfeitos polos recursos que teñen a capacidade de realizala. Para entender a asignación xenérica, é necesario, imaxinarse o seguinte caso: Jonh Smith é soldador, xeralmente o propio Jonh Smith é asignado á tarefa planificada, pero "LibrePlan" ofrece a posibilidade de elixir un recurso en xeral entre os soldadores da empresa, sen preocuparse de se Jonh Smith é o asignado á tarefa.
* Control de carga da empresa: a aplicación da a posibilidade de ter un control sinxelo da carga dos recursos da empresa. Este control realízase a medio e longo prazo xa que se poden controlar tanto os proxectos presentes como os potenciais proxectos. "LibrePlan" ofrece gráficos de uso de recursos.
* Etiquetas: Son elementos que se usan para o etiquetado das tarefas dos proxectos. Con estas etiquetas o usuario da aplicación pode realizar agrupacións conceptuais das tarefas para posteriormente poder consultalas de xeito agrupado e filtrado.
* Filtrados: Dado que o sistema dispón de xeito natural de elementos que etiquetan ou caracterízan tarefas e recursos, é posible utilizar filtrado de criterios ou etiquetas, o cal dota de unha gran potencia para poder consultar información categorizada ou extraer informes específicos en base a criterios ou etiquetas.
* Calendarios: Os calendarios determinan as horas produtivas dispoñibles dos diferentes recursos. O usuario pode crear calendarios xerais da empresa e derivar as características para calendarios máis concretos, chegando ata a nivel de calendario por recurso ou tarefa.
* Pedido e elementos de pedido: Os traballos solicitados polos clientes teñen un reflexo na aplicación en forma de pedido, que se estrutura en elementos de pedido. O pedido cos seus elementos conforman unha estrutura xerárquica en *n* niveis. Esta árbore de elementos é sobre a que se traballe á hora de planificar traballos.
* Avances: A aplicación permite xestionar diversos tipos de avances. Un proxecto pode ser medido en porcentaxe de avance, sen embargo, pode querer ser medido en unidades, presuposto acordado, etc. É responsabilidade da persoa que xestiona a planificación decidir que tipo de avance é utilizado para contrastar avances a niveis superiores de proxecto.
* Tarefas: As tarefas son los elementos de planificación da aplicación. Son utilizadas para temporalizar os traballos a realizar. As características máis importantes das tarefas son: teñen dependencias entre si e poden requirir criterios a ser satisfeitos para asignar recursos.
* Partes de traballo: Son os partes dos traballadores das empresas, indicando as horas traballadas e por outro lado as tarefas asignadas ás horas que un traballador realizou. Con esta información, o sistema é capaz de calcular cantas horas foron consumidas dunha tarefa con respecto ó total de horas presupostadas, permitindo contrastar os avances respecto do consumo de horas real.

A maiores das funcionalidades que ofrece a aplicación caben destacar outras características que o distinguen de aplicacións similares:

* Integración con ERP: A aplicación importará información directamente dos ERP das empresas para os pedidos, recursos humanos, partes de traballo e certos criterios.
* Xestión de versións: A aplicación permitirá a xestión de diversas versións de planificacións e ó mesmo tempo a posibilidade de consultar a información de cada unha delas.
* Xestión de históricos: A aplicación non borra información, solo a invalida, polo que é posible consultar mediante filtrados por datas a información antiga.

Convencións de usabilidade
==========================

Comportamento dos formularios
-----------------------------
Antes de realizar unha exposición das distintas funcionalidades asociadas ós módulos máis importantes, é necesario facer unha explicación xeral da filosofía de navegación e formularios.

Existen fundamentalmente 3 tipos de formularios de edición:

* Formularios con botón de *Voltar*. Estes formularios forman parte de unha navegación máis completa, e os cambios que se van realizando vanse almacenando en memoria. Os cambios só se aplican cando o usuario almacena explicitamente toda pantalla dende a que chegou a dito formulario.
* Formularios con botón de *Gardar* e *Pechar*. Estes formularios permiten realizar 2 operacións. A primeira delas almacena e pecha a ventá actual e a segunda delas pecha sen almacenar os cambios.
* Formularios con botón de *Gardar e Continuar*, "Gardar" e "Pechar". Permiten realizar 3 operacións. A primeira delas almacena pero continúa no formulario actual. A segunda almacena e pecha o formulario. E a terceira pecha a ventá sen almacenar os cambios.

Iconas e botóns estándar
------------------------


* Edición: A edición dos rexistros da aplicación pode ser realizada xeralmente a través dunha icona formada por un lápis sobre unha libreta branca.
* Indentado esquerda: Xeralmente estas operacións son necesarias para elementos dunha árbore que se desexan mover cara niveis internos. Esta operación pode ser feita coa icona formada por unha frecha cara a dereita de cor verde.
* Indentado dereita: Xeralmente estas operacións son necesarias para elementos dunha árbore que se desexan mover desde niveis internos cara externos. Esta operación pode ser feita coa icona formada por unha frecha cara a esquerda de cor verde.
* Borrado: Os borrados poden ser realizados coa icona da papeleira.
* Procura: A lupa é unha icona que indica que a entrada de texto á esquerda da mesma está pensada para a procura de elementos.

Pestanas
--------
Existen formularios de edición e administración de contidos que se atopan representados mediante compoñentes gráficos baseados en pestanas. Dita presentación é un mecanismo para organizar a información de un formulario global en diferentes seccións que poden ser accedidas premendo nos títulos das diferentes pestanas, mantendo o estado no que se atopaban as demáis. En todos estes casos, as operacións de gardar ou cancelar que se executen afectan ó conxunto de subformularios das diferentes pestanas.

Accións explícitas e axuda contextual
-------------------------------------
Están implementados na aplicación compoñentes que proporcionan un texto descritivo adicional do elemento sobre que se atopa enfocado o transcorrer un segundo sobre os mesmos.
As accións que o usuario pode executar na aplicación están explicitadas tanto nas etiquetas dos botóns e nos textos de axuda que aparecen sobre os mesmos, nas opcións do menú de navegación ou nas opcións dos menús contextuais que se despregan o premer co botón dereito na área do planificador.
Asimesmo, tamén se proporcionan atallos ás operacións principais facendo dobre click nos elementos que se listan, ou asociando os eventos de teclado cos cursores e a tecla de retorno ó desprazamento polos formularios é á acción de engadir elementos, respectivamente.
