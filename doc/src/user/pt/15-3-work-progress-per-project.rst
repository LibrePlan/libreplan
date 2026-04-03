Relatório de Trabalho e Progresso por Projecto
###############################################

.. contents::

Finalidade
==========

Este relatório fornece uma visão geral do estado dos projectos, considerando tanto o progresso como o custo.

Analisa o progresso actual de cada projecto, comparando-o com o progresso planeado e o trabalho concluído.

O relatório apresenta também vários rácios relacionados com o custo do projecto, comparando o desempenho actual com o desempenho planeado.

Parâmetros de Entrada e Filtros
================================

Existem vários parâmetros obrigatórios:

   *   **Data de Referência:** É a data utilizada como ponto de referência para comparar o estado planeado do projecto com o seu desempenho real. *O valor predefinido para este campo é a data actual*.

   *   **Tipo de Progresso:** É o tipo de progresso utilizado para medir o progresso do projecto. A aplicação permite que um projecto seja medido simultaneamente com diferentes tipos de progresso. O tipo seleccionado pelo utilizador no menu pendente é utilizado para calcular os dados do relatório. O valor predefinido para o *tipo de progresso* é *propagação*, que é um tipo de progresso especial que utiliza o método preferido de medição do progresso configurado para cada elemento da EAP.

Os parâmetros opcionais são:

   *   **Data de Início:** É a data de início mais antiga para os projectos a incluir no relatório. Se este campo ficar em branco, não existe uma data de início mínima para os projectos.

   *   **Data de Fim:** É a data de fim mais recente para os projectos a incluir no relatório. Todos os projectos que terminem depois da *Data de Fim* serão excluídos.

   *   **Filtrar por Projectos:** Este filtro permite ao utilizador seleccionar os projectos específicos a incluir no relatório. Se não forem adicionados projectos ao filtro, o relatório incluirá todos os projectos na base de dados. É fornecido um menu pendente pesquisável para encontrar o projecto pretendido. Os projectos são adicionados ao filtro clicando no botão *Adicionar*.

Resultado
=========

O formato de saída é o seguinte:

Cabeçalho
---------

O cabeçalho do relatório apresenta os seguintes campos:

   *   **Data de Início:** A data de início do filtro. Não é apresentada se o relatório não for filtrado por este campo.
   *   **Data de Fim:** A data de fim do filtro. Não é apresentada se o relatório não for filtrado por este campo.
   *   **Tipo de Progresso:** O tipo de progresso utilizado no relatório.
   *   **Projectos:** Indica os projectos filtrados para os quais o relatório é gerado. Apresentará a cadeia *Todos* quando o relatório incluir todos os projectos que satisfaçam os outros filtros.
   *   **Data de Referência:** A data de referência de entrada obrigatória seleccionada para o relatório.

Rodapé
------

O rodapé apresenta a data em que o relatório foi gerado.

Corpo
-----

O corpo do relatório é constituído por uma lista de projectos seleccionados com base nos filtros de entrada.

Os filtros funcionam adicionando condições, excepto para o conjunto formado pelos filtros de data (*Data de Início*, *Data de Fim*) e o *Filtrar por Projectos*. Neste caso, se um ou ambos os filtros de data estiverem preenchidos e o *Filtrar por Projectos* tiver uma lista de projectos seleccionados, este último filtro tem precedência. Isto significa que os projectos incluídos no relatório são os fornecidos pelo *Filtrar por Projectos*, independentemente dos filtros de data.

É importante ter em conta que o progresso no relatório é calculado como uma fracção da unidade, variando entre 0 e 1.

Para cada projecto seleccionado para inclusão no resultado do relatório, são apresentadas as seguintes informações:

   * *Nome do Projecto*.
   * *Total de Horas*. O total de horas do projecto é apresentado somando as horas de cada tarefa. São apresentados dois tipos de total de horas:
      *   *Estimadas (TE)*. É a soma de todas as horas estimadas na EAP do projecto. Representa o número total de horas estimadas para concluir o projecto.
      *   *Planeadas (TP)*. No *LibrePlan*, é possível ter duas quantidades diferentes: o número estimado de horas para uma tarefa (o número de horas inicialmente estimado para concluir a tarefa) e as horas planeadas (as horas alocadas no plano para concluir a tarefa). As horas planeadas podem ser iguais, inferiores ou superiores às horas estimadas e são determinadas numa fase posterior, a operação de atribuição. Por conseguinte, o total de horas planeadas para um projecto é a soma de todas as horas alocadas para as suas tarefas.
   * *Progresso*. São apresentadas três medições relacionadas com o progresso global do tipo especificado no filtro de entrada de progresso para cada projecto na data de referência:
      *   *Medido (PM)*. É o progresso global considerando as medições de progresso com uma data anterior à *Data de Referência* nos parâmetros de entrada do relatório. Todas as tarefas são tidas em conta e a soma é ponderada pelo número de horas de cada tarefa.
      *   *Imputado (PI)*. É o progresso assumindo que o trabalho continua ao mesmo ritmo que as horas concluídas para uma tarefa. Se X horas de Y horas de uma tarefa estiverem concluídas, o progresso imputado global é considerado X/Y.
      *   *Planeado (PP)*. É o progresso global do projecto de acordo com o calendário planeado na data de referência. Se tudo tivesse acontecido exactamente como planeado, o progresso medido deveria ser igual ao progresso planeado.
   * *Horas até à Data*. Existem dois campos que mostram o número de horas até à data de referência de duas perspectivas:
      *   *Planeadas (HP)*. Este número é a soma das horas alocadas a qualquer tarefa do projecto com uma data inferior ou igual à *Data de Referência*.
      *   *Reais (HR)*. Este número é a soma das horas comunicadas nos relatórios de trabalho para qualquer uma das tarefas do projecto com uma data inferior ou igual à *Data de Referência*.
   * *Diferença*. Sob este título, existem várias métricas relacionadas com o custo:
      *   *Custo*. É a diferença em horas entre o número de horas gastas, considerando o progresso medido, e as horas concluídas até à data de referência. A fórmula é: *PM*TP - HR*.
      *   *Planeado*. É a diferença entre as horas gastas de acordo com o progresso global medido do projecto e o número planeado até à *Data de Referência*. Mede a vantagem ou o atraso no tempo. A fórmula é: *PM*TP - HP*.
      *   *Rácio de Custo*. É calculado dividindo *PM* / *PI*. Se for superior a 1, significa que o projecto é rentável neste momento. Se for inferior a 1, significa que o projecto está a perder dinheiro.
      *   *Rácio Planeado*. É calculado dividindo *PM* / *PP*. Se for superior a 1, significa que o projecto está adiantado em relação ao calendário. Se for inferior a 1, significa que o projecto está atrasado em relação ao calendário.
