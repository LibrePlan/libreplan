Relatório de Trabalho e Progresso por Projeto
##############################################

.. contents::

Finalidade
==========

Este relatório fornece uma visão geral do estado dos projetos, considerando tanto o progresso quanto o custo.

Ele analisa o progresso atual de cada projeto, comparando-o com o progresso planejado e o trabalho concluído.

O relatório também exibe várias proporções relacionadas ao custo do projeto, comparando o desempenho atual com o desempenho planejado.

Parâmetros de Entrada e Filtros
================================

Existem vários parâmetros obrigatórios:

   *   **Data de Referência:** É a data usada como ponto de referência para comparar o estado planejado do projeto com seu desempenho real. *O valor padrão para este campo é a data atual*.

   *   **Tipo de Progresso:** É o tipo de progresso usado para medir o progresso do projeto. A aplicação permite que um projeto seja medido simultaneamente com diferentes tipos de progresso. O tipo selecionado pelo usuário no menu suspenso é usado para calcular os dados do relatório. O valor padrão para o *tipo de progresso* é *propagação*, que é um tipo de progresso especial que usa o método preferido de medição do progresso configurado para cada elemento da EAP.

Os parâmetros opcionais são:

   *   **Data de Início:** É a data de início mais antiga para os projetos a serem incluídos no relatório. Se este campo for deixado em branco, não há uma data de início mínima para os projetos.

   *   **Data de Fim:** É a data de fim mais recente para os projetos a serem incluídos no relatório. Todos os projetos que terminarem após a *Data de Fim* serão excluídos.

   *   **Filtrar por Projetos:** Este filtro permite ao usuário selecionar os projetos específicos a serem incluídos no relatório. Se nenhum projeto for adicionado ao filtro, o relatório incluirá todos os projetos no banco de dados. Um menu suspenso pesquisável é fornecido para encontrar o projeto desejado. Os projetos são adicionados ao filtro clicando no botão *Adicionar*.

Resultado
=========

O formato de saída é o seguinte:

Cabeçalho
---------

O cabeçalho do relatório exibe os seguintes campos:

   *   **Data de Início:** A data de início do filtro. Não é exibida se o relatório não for filtrado por este campo.
   *   **Data de Fim:** A data de fim do filtro. Não é exibida se o relatório não for filtrado por este campo.
   *   **Tipo de Progresso:** O tipo de progresso usado no relatório.
   *   **Projetos:** Indica os projetos filtrados para os quais o relatório é gerado. Exibirá a string *Todos* quando o relatório incluir todos os projetos que satisfaçam os outros filtros.
   *   **Data de Referência:** A data de referência de entrada obrigatória selecionada para o relatório.

Rodapé
------

O rodapé exibe a data em que o relatório foi gerado.

Corpo
-----

O corpo do relatório consiste em uma lista de projetos selecionados com base nos filtros de entrada.

Os filtros funcionam adicionando condições, exceto para o conjunto formado pelos filtros de data (*Data de Início*, *Data de Fim*) e o *Filtrar por Projetos*. Neste caso, se um ou ambos os filtros de data estiverem preenchidos e o *Filtrar por Projetos* tiver uma lista de projetos selecionados, este último filtro tem precedência. Isso significa que os projetos incluídos no relatório são os fornecidos pelo *Filtrar por Projetos*, independentemente dos filtros de data.

É importante observar que o progresso no relatório é calculado como uma fração da unidade, variando entre 0 e 1.

Para cada projeto selecionado para inclusão no resultado do relatório, as seguintes informações são exibidas:

   * *Nome do Projeto*.
   * *Total de Horas*. O total de horas do projeto é mostrado somando as horas de cada tarefa. Dois tipos de total de horas são mostrados:
      *   *Estimadas (TE)*. É a soma de todas as horas estimadas na EAP do projeto. Representa o número total de horas estimadas para concluir o projeto.
      *   *Planejadas (TP)*. No *LibrePlan*, é possível ter duas quantidades diferentes: o número estimado de horas para uma tarefa (o número de horas inicialmente estimado para concluir a tarefa) e as horas planejadas (as horas alocadas no plano para concluir a tarefa). As horas planejadas podem ser iguais, menores ou maiores que as horas estimadas e são determinadas em uma fase posterior, a operação de atribuição. Portanto, o total de horas planejadas para um projeto é a soma de todas as horas alocadas para suas tarefas.
   * *Progresso*. Três medições relacionadas ao progresso global do tipo especificado no filtro de entrada de progresso para cada projeto na data de referência são mostradas:
      *   *Medido (PM)*. É o progresso global considerando as medições de progresso com uma data anterior à *Data de Referência* nos parâmetros de entrada do relatório. Todas as tarefas são levadas em conta e a soma é ponderada pelo número de horas de cada tarefa.
      *   *Imputado (PI)*. É o progresso assumindo que o trabalho continua no mesmo ritmo que as horas concluídas para uma tarefa. Se X horas de Y horas de uma tarefa estiverem concluídas, o progresso imputado global é considerado X/Y.
      *   *Planejado (PP)*. É o progresso global do projeto de acordo com o cronograma planejado na data de referência. Se tudo tivesse acontecido exatamente como planejado, o progresso medido deveria ser igual ao progresso planejado.
   * *Horas até a Data*. Há dois campos que mostram o número de horas até a data de referência de duas perspectivas:
      *   *Planejadas (HP)*. Este número é a soma das horas alocadas a qualquer tarefa do projeto com uma data menor ou igual à *Data de Referência*.
      *   *Reais (HR)*. Este número é a soma das horas reportadas nos relatórios de trabalho para qualquer uma das tarefas do projeto com uma data menor ou igual à *Data de Referência*.
   * *Diferença*. Sob este título, há várias métricas relacionadas ao custo:
      *   *Custo*. É a diferença em horas entre o número de horas gastas, considerando o progresso medido, e as horas concluídas até a data de referência. A fórmula é: *PM*TP - HR*.
      *   *Planejado*. É a diferença entre as horas gastas de acordo com o progresso global medido do projeto e o número planejado até a *Data de Referência*. Mede a vantagem ou o atraso no tempo. A fórmula é: *PM*TP - HP*.
      *   *Proporção de Custo*. É calculada dividindo *PM* / *PI*. Se for maior que 1, significa que o projeto é lucrativo neste momento. Se for menor que 1, significa que o projeto está perdendo dinheiro.
      *   *Proporção Planejada*. É calculada dividindo *PM* / *PP*. Se for maior que 1, significa que o projeto está adiantado em relação ao cronograma. Se for menor que 1, significa que o projeto está atrasado em relação ao cronograma.
