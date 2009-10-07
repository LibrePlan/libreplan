Introdución
##############

.. contents::

A aplicación para xestión da produción do sector auxiliar do naval pretende resolver principalmente o problema da planificación nas empresas pertencentes ó sector. Para elo desenvolvéronse unha serie de funcionalidades que dan solución a certos problemas detectados durante a análise do proxecto.

A modo de resumo, poderíamos destacar os conceptos básicos cos que traballará a aplicación
   * Criterios: Os criterios son unha entidade do sistema que permitirán clasificar os recursos (tanto humanos como máquinas) e as tarefas. Os recursos satisfarán criterios e por outro lado as tarefas requiren criterios para ser realizadas.
   * Calendarios: Os calendarios determinarán as horas produtivas dispoñibles dos diferentes recursos. O usuario poderá crear calendarios xerais da empresa e derivar as características para calendarios máis concretos, chegando ata a nivel de calendario por recurso ou tarefa.
   * Avances: A aplicación permitirá xestionar diversos tipos de avances. Un proxecto pode ser medido en porcentaxe de avance, sen embargo, pode querer ser medido en unidades, presuposto acordado, etc. Será responsabilidade da persoa que xestiona a planificación decidir qué tipo de avance será utilizado para contrastar avances a niveis superiores de proxecto. 
   * Recursos: Serán de dous tipos diferentes: humanos e máquinas. Os recursos humanos serán os traballadores da empresa que se utilizarán para controlar a carga da empresa e de uso dos mesmos. Por outro lado, as máquinas, dependentes das persoas que as xestionan, serán outros recursos que tamén serán controlables na aplicación.
   * Pedido e elementos de pedido: Os traballos solicitados polos clientes terán un reflexo na aplicación en forma de pedido, que se estrutura en elementos de pedido. O pedido cos seus elementos conformarán unha estrutura xerárquina en n niveis. Esta árbore de elementos será sobre a que se traballe á hora de planificar traballos.
   * Tarefas: As tarefas son los elementos de planificación da aplicación. Serán utilizadas para temporalizar os traballos a realizar. As características máis importantes das tarefas serán: teñen dependencias entre si e poden requerir criterios a ser satisfeitos para asignar recursos.
   * Partes de traballo: Son os partes dos traballadores das empresas, indicando as horas traballadas e por outro lado as tarefas asignadas ás horas que un traballador realizou. Con esta información, o sistema é capaz de calcular cantas horas foron consumidas dunha tarefa con respecto ó total de horas presupostadas, permitindo contrastar os avances respecto do consumo de horas real.
   * Etiquetas: Serán elementos que se usarán para o etiquetado das tarefas dos proxectos. Con estas etiquetas o usuario da aplicación poderá realizar agrupacións conceptuais das tarefas para posteriormente poder consultar información das mesmas de xeito agrupado e filtrado.


