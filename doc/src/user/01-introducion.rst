Introdución
##############

.. contents::

A aplicación para xestión da produción do sector auxiliar do naval pretende resolver principalmente o problema da planificación nas empresas pertencentes ó sector. Para elo desenvolvéronse unha serie de funcionalidades que dan solución a certos problemas detectados durante a análise do proxecto.


.. figure:: images/company_view.png
   :scale: 50

¿Por que me é útil "Navalpro"?
==============================

"Navalpro" é un proxecto desenvolvido co obxectivo de dotar ó usuario dunha ferramenta de planificación que se basea nunha serie de conceptos clave, os cales forman parte das características que distinguen a aplicación e a definen como unha ferramenta diseñada tendo en conta os problemas clave do sector:

* Vista global de empresa e multiproxecto: "Navalpro" é unha aplicación orientada específicamente a dotar de información ós usuarios dos proxectos que se levan a cabo nunha empresa, polo que a base é multiproxecto. Non se determinou que o enfoque do proxecto sexa orientado individualmente a cada proxecto. Sen embargo, tamén será posible dipoñer de varias vistas específicas, entre elas a de proxectos individuais.
* Criterios: Os criterios son unha entidade do sistema que permitirán clasificar os recursos (tanto humanos como máquinas) e as tarefas. Dende o punto de vista dos recursos, estes satisfarán criterios e, dende o punto de vista das tarefas, estas requerirán criterios a ser satisfeitos. Correspóndense con un dos aspectos máis importantes da aplicación, xa que os criterios formarán parte da base das asignacións xenéricas na aplicación, resolvendo un dos problemas máis importantes para o sector, a alta temporalidade dos recursos humanos e a dificultade para ter estimacións de carga da empresa a longo prazo.
* Recursos: Serán de dous tipos diferentes: humanos e máquinas. Os recursos humanos serán os traballadores da empresa que se utilizarán para controlar a carga da empresa e de uso dos mesmos. Por outro lado, as máquinas, dependentes das persoas que as xestionan, serán outros recursos que tamén serán controlables na aplicación.
* Asignación de recursos: Unha das claves é o feito de ofrecer a posibilidade de dous tipos diferentes de asignación: asignación específica e asignación xenérica. A xenérica é unha asignación baseada nos criterios que se lle establecen a unha tarefa para ser satisfeitos polos usuarios que teñen a capacidade de realizala.
* Control de carga da empresa: Baseado nos conceptos xa comentados, a aplicación dará a posibilidade de ter un control sinxelo da carga dos recursos da empresa a medio e longo prazo xa que se poderá controlar os proxectos presentes e os potenciais proxectos a futuro, visualizando as cargas en gráficos de uso de recursos.
* Etiquetas: Serán elementos que se usarán para o etiquetado das tarefas dos proxectos. Con estas etiquetas o usuario da aplicación poderá realizar agrupacións conceptuais das tarefas para posteriormente poder consultar información das mesmas de xeito agrupado e filtrado.
* Filtrados: Dado que o sistema disporá de xeito natural de elementos que etiquetan ou caracterízan tarefas e recursos, será posible utilizar filtrado de criterios ou etiquetas, o cal dotará de unha gran potencia para poder consultar información categorizada ou extraer informes específicos en base a criterios ou etiquetas.
* Calendarios: Os calendarios determinarán as horas produtivas dispoñibles dos diferentes recursos. O usuario poderá crear calendarios xerais da empresa e derivar as características para calendarios máis concretos, chegando ata a nivel de calendario por recurso ou tarefa.
* Pedido e elementos de pedido: Os traballos solicitados polos clientes terán un reflexo na aplicación en forma de pedido, que se estrutura en elementos de pedido. O pedido cos seus elementos conformarán unha estrutura xerárquina en n niveis. Esta árbore de elementos será sobre a que se traballe á hora de planificar traballos.
* Avances: A aplicación permitirá xestionar diversos tipos de avances. Un proxecto pode ser medido en porcentaxe de avance, sen embargo, pode querer ser medido en unidades, presuposto acordado, etc. Será responsabilidade da persoa que xestiona a planificación decidir qué tipo de avance será utilizado para contrastar avances a niveis superiores de proxecto.
* Tarefas: As tarefas son los elementos de planificación da aplicación. Serán utilizadas para temporalizar os traballos a realizar. As características máis importantes das tarefas serán: teñen dependencias entre si e poden requerir criterios a ser satisfeitos para asignar recursos.
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

* Formularios con botón de *Voltar*. Estes formularios forman parte de unha navegación máis completa, e os cambios que se van realizando vanse almacenando en memoria. Os cambios só se aplican cando o usuario almacena explícitamente toda pantalla dende a que chegou a dito formulario.
* Formularios con botón de *Gardar* e *Pechar*. Estes formularios permiten realizar 2 operacións. A primeira delas almacena e pecha a ventana actual e a segunda delas pecha sen almacenar os cambios.
* Formularios con botón de *Gardar*, "Gardar e Pechar" e "Pechar". Permiten realizar 3 operacións. A primeira delas almacena pero continúa no formulario actual. A segunda almacena e pecha o formulario. E a terceira pecha a ventana sen almacenar os cambios.

Iconas e botóns estándar
------------------------

* Edición: A edición dos rexistros da aplicación poderá ser realizada xeralmente a través dunha icona formada por un Lápiz sobre unha libreta brance.
* Indentado esquerda: Xeralmente estas operacións son necesarias para elementos dunha árbore que se desexan mover cara niveis internos. Esta operación poderá ser feita coa icona formada por unha frecha cara a dereita de cor verde.
* Indentado dereita: Xeralmente estas operacións son necesarias para elementos dunha árbore que se desexan mover desde niveis internoscara externos. Esta operación poderá ser feita coa icona formada por unha frecha cara a esqueda de cor verde.
* Borrado: Os borrados poderán ser realizados coa icona da papeleira.
* Procura: A lupa é unha icona que indicará que a entrada de texto á esquerda da mesma está pensada para a procura de elementos.

Pestanas
--------
Existirán formularios de edición e administración de contidos que se atopan representados mediante compoñentes gráficos baseados en pestanas. Dita presentación é un mecanismo para organizar a información de un formulario global en diferentes seccións que poden ser accedidas premedo nos
títulos das diferentes pestanas, mantendo o estado no que se atopaban as demáis. En todos estes casos, as operacións de gardar ou cancelar que se executen afectarán ó conxunto de subformularios das diferentes pestanas.

Accións explícitas e axuda contextual
-------------------------------------
Están implementados na aplicación compoñentes que proporcionan un texto descriptivo adicional do elemento sobre que se atopa enfocado o transcurrir un segundo sobre os mesmos.
As accións que o usuario pode executar na aplicación están explicitadas tanto nas etiquetas dos botóns e nos textos de axuda que aparecen sobre os mesmos, nas opcións do menú de navegación ou nas opcións dos menús contextuais que se desplegan o facer botón dereito na área do planificador.
Asimesmo, tamén se proporcionan atallos ás operacións principais facendo doble click nos elementos que se listan, ou asociando os eventos de teclado cos cursores e a tecla intro ó desplazamento polos formularios é á acción de engadir elementos, respectivamente.
