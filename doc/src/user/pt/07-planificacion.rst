Planeamento de Tarefas
######################

.. _planificacion:
.. contents::

Planeamento de Tarefas
======================

O planeamento no LibrePlan é um processo descrito ao longo do guia do utilizador, sendo os capítulos sobre projetos e atribuição de recursos particularmente importantes. Este capítulo descreve os procedimentos básicos de planeamento após os diagramas de Gantt e projetos estarem correctamente configurados.

.. figure:: images/planning-view.png
   :scale: 35

   Vista de Planeamento do Trabalho

Tal como na visão geral da empresa, a vista de planeamento do projecto está dividida em várias vistas com base nas informações que estão a ser analisadas. As vistas disponíveis para um projecto específico são:

*   Vista de Planeamento
*   Vista de Carga de Recursos
*   Vista de Lista de Projetos
*   Vista de Atribuição Avançada

Vista de Planeamento
---------------------

A Vista de Planeamento combina três perspectivas diferentes:

*   **Planeamento do Projecto:** O planeamento do projecto é apresentado na parte superior direita do programa como um diagrama de Gantt. Esta vista permite aos utilizadores mover temporariamente tarefas, atribuir dependências entre elas, definir marcos e estabelecer restrições.
*   **Carga de Recursos:** A vista de Carga de Recursos, localizada na parte inferior direita do ecrã, mostra a disponibilidade de recursos com base nas atribuições, em oposição às atribuições feitas a tarefas. As informações apresentadas nesta vista são as seguintes:

    *   **Área Roxa:** Indica uma carga de recursos abaixo de 100% da sua capacidade.
    *   **Área Verde:** Indica uma carga de recursos abaixo de 100%, resultante do recurso estar planeado para outro projecto.
    *   **Área Laranja:** Indica uma carga de recursos superior a 100% como resultado do projecto actual.
    *   **Área Amarela:** Indica uma carga de recursos superior a 100% como resultado de outros projectos.

*   **Vista de Gráfico e Indicadores de Valor Ganho:** Estes podem ser visualizados a partir do separador "Valor Ganho". O gráfico gerado baseia-se na técnica de valor ganho, e os indicadores são calculados para cada dia útil do projecto. Os indicadores calculados são:

    *   **BCWS (Custo Orçamentado do Trabalho Agendado):** A função de tempo cumulativa para o número de horas planeadas até uma determinada data. Será 0 no início planeado da tarefa e igual ao número total de horas planeadas no final. Como em todos os gráficos cumulativos, sempre aumentará. A função para uma tarefa será a soma das atribuições diárias até à data de cálculo. Esta função tem valores para todos os momentos, desde que os recursos tenham sido atribuídos.
    *   **ACWP (Custo Real do Trabalho Efectuado):** A função de tempo cumulativa para as horas registadas em relatórios de trabalho até uma determinada data. Esta função só terá um valor de 0 antes da data do primeiro relatório de trabalho da tarefa, e o seu valor continuará a aumentar à medida que o tempo passa e as horas dos relatórios de trabalho são adicionadas. Não terá valor após a data do último relatório de trabalho.
    *   **BCWP (Custo Orçamentado do Trabalho Efectuado):** A função de tempo cumulativa que inclui o valor resultante de multiplicar o progresso da tarefa pela quantidade de trabalho que se estimou ser necessária para a sua conclusão. Os valores desta função aumentam à medida que o tempo passa, assim como os valores de progresso. O progresso é multiplicado pelo número total de horas estimadas para todas as tarefas. O valor BCWP é a soma dos valores para as tarefas que estão a ser calculadas. O progresso é totalizando quando está configurado.
    *   **CV (Variação de Custo):** CV = BCWP - ACWP
    *   **SV (Variação de Agenda):** SV = BCWP - BCWS
    *   **BAC (Orçamento na Conclusão):** BAC = máx (BCWS)
    *   **EAC (Estimativa na Conclusão):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Variação na Conclusão):** VAC = BAC - EAC
    *   **ETC (Estimativa para Concluir):** ETC = EAC - ACWP
    *   **CPI (Índice de Desempenho de Custos):** CPI = BCWP / ACWP
    *   **SPI (Índice de Desempenho de Agenda):** SPI = BCWP / BCWS

Na vista de planeamento do projecto, os utilizadores podem efectuar as seguintes acções:

*   **Atribuir Dependências:** Clique com o botão direito numa tarefa, escolha "Adicionar dependência" e arraste o ponteiro do rato para a tarefa à qual a dependência deve ser atribuída.

    *   Para alterar o tipo de dependência, clique com o botão direito na dependência e escolha o tipo desejado.

*   **Criar um Novo Marco:** Clique na tarefa antes da qual o marco deve ser adicionado e seleccione a opção "Adicionar marco". Os marcos podem ser movidos seleccionando o marco com o ponteiro do rato e arrastando-o para a posição desejada.
*   **Mover Tarefas sem Perturbar Dependências:** Clique com o botão direito no corpo da tarefa e arraste-a para a posição desejada. Se não forem violadas restrições ou dependências, o sistema actualizará a atribuição diária de recursos à tarefa e colocará a tarefa na data seleccionada.
*   **Atribuir Restrições:** Clique na tarefa em questão e seleccione a opção "Propriedades da tarefa". Aparecerá uma janela pop-up com um campo "Restrições" que pode ser modificado. As restrições podem entrar em conflito com as dependências, razão pela qual cada projeto especifica se as dependências têm prioridade sobre as restrições. As restrições que podem ser estabelecidas são:

    *   **O mais cedo possível:** Indica que a tarefa deve começar o mais cedo possível.
    *   **Não antes de:** Indica que a tarefa não deve começar antes de uma determinada data.
    *   **Começar numa data específica:** Indica que a tarefa deve começar numa data específica.

A vista de planeamento também oferece vários procedimentos que funcionam como opções de visualização:

*   **Nível de Zoom:** Os utilizadores podem escolher o nível de zoom desejado. Existem vários níveis de zoom: anual, quadrimestral, mensal, semanal e diário.
*   **Filtros de Pesquisa:** Os utilizadores podem filtrar tarefas com base em etiquetas ou critérios.
*   **Caminho Crítico:** Como resultado de usar o algoritmo *Dijkstra* para calcular caminhos em grafos, o caminho crítico foi implementado. Pode ser visualizado clicando no botão "Caminho crítico" nas opções de visualização.
*   **Mostrar Etiquetas:** Permite aos utilizadores ver as etiquetas atribuídas às tarefas num projecto, que podem ser visualizadas no ecrã ou impressas.
*   **Mostrar Recursos:** Permite aos utilizadores ver os recursos atribuídos às tarefas num projecto, que podem ser visualizados no ecrã ou impressos.
*   **Imprimir:** Permite aos utilizadores imprimir o diagrama de Gantt que está a ser visualizado.

Vista de Carga de Recursos
---------------------------

A Vista de Carga de Recursos fornece uma lista de recursos que contém uma lista de tarefas ou critérios que geram cargas de trabalho. Cada tarefa ou critério é mostrado como um diagrama de Gantt para que as datas de início e fim da carga possam ser vistas. É mostrada uma cor diferente dependendo de a carga do recurso ser superior ou inferior a 100%:

*   **Verde:** Carga inferior a 100%
*   **Laranja:** Carga de 100%
*   **Vermelho:** Carga superior a 100%

.. figure:: images/resource-load.png
   :scale: 35

   Vista de Carga de Recursos para uma Projeto Específica

Se o ponteiro do rato for colocado sobre o diagrama de Gantt do recurso, será mostrada a percentagem de carga para o trabalhador.

Vista de Lista de Projetos
-----------------------------

A Vista de Lista de Projetos permite aos utilizadores aceder às opções de edição e eliminação de projetos. Consulte o capítulo "Projetos" para mais informações.

Vista de Atribuição Avançada
-----------------------------

A Vista de Atribuição Avançada é explicada em profundidade no capítulo "Atribuição de Recursos".
