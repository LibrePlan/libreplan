Planejamento de Tarefas
########################

.. _planificacion:
.. contents::

Planejamento de Tarefas
========================

O planejamento no LibrePlan é um processo descrito ao longo do guia do usuário, sendo os capítulos sobre projetos e atribuição de recursos particularmente importantes. Este capítulo descreve os procedimentos básicos de planejamento após os diagramas de Gantt e projetos estarem corretamente configurados.

.. figure:: images/planning-view.png
   :scale: 35

   Visualização de Planejamento do Trabalho

Assim como na visão geral da empresa, a visualização de planejamento do projeto está dividida em várias visualizações com base nas informações que estão sendo analisadas. As visualizações disponíveis para um projeto específico são:

*   Visualização de Planejamento
*   Visualização de Carga de Recursos
*   Visualização de Lista de Projetos
*   Visualização de Atribuição Avançada

Visualização de Planejamento
------------------------------

A Visualização de Planejamento combina três perspectivas diferentes:

*   **Planejamento do Projeto:** O planejamento do projeto é exibido na parte superior direita do programa como um diagrama de Gantt. Esta visualização permite aos usuários mover temporariamente tarefas, atribuir dependências entre elas, definir marcos e estabelecer restrições.
*   **Carga de Recursos:** A visualização de Carga de Recursos, localizada na parte inferior direita da tela, mostra a disponibilidade de recursos com base nas atribuições, em oposição às atribuições feitas a tarefas. As informações exibidas nesta visualização são as seguintes:

    *   **Área Roxa:** Indica uma carga de recursos abaixo de 100% de sua capacidade.
    *   **Área Verde:** Indica uma carga de recursos abaixo de 100%, resultante do recurso estar planejado para outro projeto.
    *   **Área Laranja:** Indica uma carga de recursos superior a 100% como resultado do projeto atual.
    *   **Área Amarela:** Indica uma carga de recursos superior a 100% como resultado de outros projetos.

*   **Visualização de Gráfico e Indicadores de Valor Agregado:** Estes podem ser visualizados a partir da aba "Valor Agregado". O gráfico gerado baseia-se na técnica de valor agregado, e os indicadores são calculados para cada dia útil do projeto. Os indicadores calculados são:

    *   **BCWS (Custo Orçado do Trabalho Programado):** A função de tempo cumulativa para o número de horas planejadas até uma determinada data. Será 0 no início planejado da tarefa e igual ao número total de horas planejadas no final. Como em todos os gráficos cumulativos, sempre aumentará. A função para uma tarefa será a soma das atribuições diárias até a data de cálculo. Esta função tem valores para todos os momentos, desde que os recursos tenham sido atribuídos.
    *   **ACWP (Custo Real do Trabalho Realizado):** A função de tempo cumulativa para as horas registradas em relatórios de trabalho até uma determinada data. Esta função só terá um valor de 0 antes da data do primeiro relatório de trabalho da tarefa, e seu valor continuará a aumentar à medida que o tempo passa e as horas dos relatórios de trabalho são adicionadas. Não terá valor após a data do último relatório de trabalho.
    *   **BCWP (Custo Orçado do Trabalho Realizado):** A função de tempo cumulativa que inclui o valor resultante de multiplicar o progresso da tarefa pela quantidade de trabalho que se estimou ser necessária para sua conclusão. Os valores desta função aumentam à medida que o tempo passa, assim como os valores de progresso. O progresso é multiplicado pelo número total de horas estimadas para todas as tarefas. O valor BCWP é a soma dos valores para as tarefas que estão sendo calculadas. O progresso é totalizado quando está configurado.
    *   **CV (Variação de Custo):** CV = BCWP - ACWP
    *   **SV (Variação de Cronograma):** SV = BCWP - BCWS
    *   **BAC (Orçamento na Conclusão):** BAC = máx (BCWS)
    *   **EAC (Estimativa na Conclusão):** EAC = (ACWP / BCWP) * BAC
    *   **VAC (Variação na Conclusão):** VAC = BAC - EAC
    *   **ETC (Estimativa para Concluir):** ETC = EAC - ACWP
    *   **CPI (Índice de Desempenho de Custos):** CPI = BCWP / ACWP
    *   **SPI (Índice de Desempenho de Cronograma):** SPI = BCWP / BCWS

Na visualização de planejamento do projeto, os usuários podem realizar as seguintes ações:

*   **Atribuir Dependências:** Clique com o botão direito em uma tarefa, escolha "Adicionar dependência" e arraste o ponteiro do mouse para a tarefa à qual a dependência deve ser atribuída.

    *   Para alterar o tipo de dependência, clique com o botão direito na dependência e escolha o tipo desejado.

*   **Criar um Novo Marco:** Clique na tarefa antes da qual o marco deve ser adicionado e selecione a opção "Adicionar marco". Os marcos podem ser movidos selecionando o marco com o ponteiro do mouse e arrastando-o para a posição desejada.
*   **Mover Tarefas sem Perturbar Dependências:** Clique com o botão direito no corpo da tarefa e arraste-a para a posição desejada. Se nenhuma restrição ou dependência for violada, o sistema atualizará a atribuição diária de recursos à tarefa e colocará a tarefa na data selecionada.
*   **Atribuir Restrições:** Clique na tarefa em questão e selecione a opção "Propriedades da tarefa". Uma janela pop-up aparecerá com um campo "Restrições" que pode ser modificado. As restrições podem conflitar com as dependências, razão pela qual cada projeto especifica se as dependências têm prioridade sobre as restrições. As restrições que podem ser estabelecidas são:

    *   **O mais cedo possível:** Indica que a tarefa deve começar o mais cedo possível.
    *   **Não antes de:** Indica que a tarefa não deve começar antes de uma determinada data.
    *   **Começar em uma data específica:** Indica que a tarefa deve começar em uma data específica.

A visualização de planejamento também oferece vários procedimentos que funcionam como opções de visualização:

*   **Nível de Zoom:** Os usuários podem escolher o nível de zoom desejado. Existem vários níveis de zoom: anual, quadrimestral, mensal, semanal e diário.
*   **Filtros de Pesquisa:** Os usuários podem filtrar tarefas com base em etiquetas ou critérios.
*   **Caminho Crítico:** Como resultado do uso do algoritmo *Dijkstra* para calcular caminhos em grafos, o caminho crítico foi implementado. Ele pode ser visualizado clicando no botão "Caminho crítico" nas opções de visualização.
*   **Mostrar Etiquetas:** Permite aos usuários ver as etiquetas atribuídas às tarefas em um projeto, que podem ser visualizadas na tela ou impressas.
*   **Mostrar Recursos:** Permite aos usuários ver os recursos atribuídos às tarefas em um projeto, que podem ser visualizados na tela ou impressos.
*   **Imprimir:** Permite aos usuários imprimir o diagrama de Gantt que está sendo visualizado.

Visualização de Carga de Recursos
-----------------------------------

A Visualização de Carga de Recursos fornece uma lista de recursos que contém uma lista de tarefas ou critérios que geram cargas de trabalho. Cada tarefa ou critério é mostrado como um diagrama de Gantt para que as datas de início e fim da carga possam ser vistas. Uma cor diferente é mostrada dependendo de a carga do recurso ser superior ou inferior a 100%:

*   **Verde:** Carga inferior a 100%
*   **Laranja:** Carga de 100%
*   **Vermelho:** Carga superior a 100%

.. figure:: images/resource-load.png
   :scale: 35

   Visualização de Carga de Recursos para um Projeto Específico

Se o ponteiro do mouse for colocado sobre o diagrama de Gantt do recurso, a porcentagem de carga para o trabalhador será mostrada.

Visualização de Lista de Projetos
---------------------------------

A Visualização de Lista de Projetos permite aos usuários acessar as opções de edição e exclusão de projetos. Consulte o capítulo "Projetos" para mais informações.

Visualização de Atribuição Avançada
-------------------------------------

A Visualização de Atribuição Avançada é explicada em profundidade no capítulo "Atribuição de Recursos".
