Painel de Projecto
##################

.. contents::

O painel de projecto é uma perspectiva do *LibrePlan* que contém um conjunto de **KPIs (Indicadores-Chave de Desempenho)** para ajudar a avaliar o desempenho de um projecto em termos de:

   *   Progresso do trabalho
   *   Custo
   *   Estado dos recursos alocados
   *   Restrições de tempo

Indicadores de Desempenho do Progresso
=======================================

São calculados dois indicadores: a percentagem de progresso do projecto e o estado das tarefas.

Percentagem de Progresso do Projecto
-------------------------------------

Este gráfico apresenta o progresso global de um projecto, comparando-o com o progresso esperado com base no gráfico de *Gantt*.

O progresso é representado por duas barras:

   *   *Progresso Actual:* O progresso actual baseado nas medições efectuadas.
   *   *Progresso Esperado:* O progresso que o projecto deveria ter atingido neste momento, de acordo com o plano do projecto.

Para ver o valor real medido de cada barra, passe o cursor do rato sobre a barra.

O progresso global do projecto é estimado utilizando vários métodos diferentes, uma vez que não existe uma única abordagem universalmente correcta:

   *   **Progresso por Propagação:** É o tipo de progresso definido como progresso de propagação ao nível do projecto. Neste caso, não é possível calcular um valor esperado e apenas a barra actual é apresentada.
   *   **Por Todas as Horas de Tarefas:** O progresso de todas as tarefas do projecto é calculado como média para obter o valor global. Trata-se de uma média ponderada que considera o número de horas alocadas a cada tarefa.
   *   **Pelas Horas do Caminho Crítico:** O progresso das tarefas pertencentes a qualquer um dos caminhos críticos do projecto é calculado como média para obter o valor global. Trata-se de uma média ponderada que considera o total de horas alocadas a cada tarefa envolvida.
   *   **Pela Duração do Caminho Crítico:** O progresso das tarefas pertencentes a qualquer um dos caminhos críticos é calculado como média ponderada, mas desta vez considerando a duração de cada tarefa envolvida em vez das horas atribuídas.

Estado das Tarefas
------------------

Um gráfico circular apresenta a percentagem de tarefas do projecto em diferentes estados. Os estados definidos são:

   *   **Concluídas:** Tarefas completas, identificadas por um valor de progresso de 100%.
   *   **Em Curso:** Tarefas actualmente em execução. Estas tarefas têm um valor de progresso diferente de 0% ou 100%, ou foi registado algum tempo de trabalho.
   *   **Prontas para Iniciar:** Tarefas com 0% de progresso, sem tempo registado, todas as suas tarefas dependentes *FINISH_TO_START* estão *concluídas* e todas as suas tarefas dependentes *START_TO_START* estão *concluídas* ou *em curso*.
   *   **Bloqueadas:** Tarefas com 0% de progresso, sem tempo registado e com tarefas dependentes anteriores que não estão *em curso* nem no estado *pronto para iniciar*.

Indicadores de Custo
====================

São calculados vários indicadores de custo da *Gestão do Valor Ganho*:

   *   **CV (Variação de Custo):** A diferença entre a *curva do Valor Ganho* e a *curva do Custo Real* no momento actual. Valores positivos indicam um benefício e valores negativos indicam uma perda.
   *   **ACWP (Custo Real do Trabalho Realizado):** O número total de horas registadas no projecto no momento actual.
   *   **CPI (Índice de Desempenho de Custo):** O rácio *Valor Ganho / Custo Real*.

        *   > 100 é favorável, indicando que o projecto está abaixo do orçamento.
        *   = 100 é também favorável, indicando que o custo está exactamente conforme planeado.
        *   < 100 é desfavorável, indicando que o custo de conclusão do trabalho é superior ao planeado.
   *   **ETC (Estimativa para Concluir):** O tempo restante para concluir o projecto.
   *   **BAC (Orçamento na Conclusão):** O valor total de trabalho alocado no plano do projecto.
   *   **EAC (Estimativa na Conclusão):** A projecção do gestor do custo total na conclusão do projecto, baseada no *CPI*.
   *   **VAC (Variação na Conclusão):** A diferença entre o *BAC* e o *EAC*.

        *   < 0 indica que o projecto está acima do orçamento.
        *   > 0 indica que o projecto está abaixo do orçamento.

Recursos
========

Para analisar o projecto do ponto de vista dos recursos, são fornecidos dois rácios e um histograma.

Histograma do Desvio de Estimativa em Tarefas Concluídas
---------------------------------------------------------

Este histograma calcula o desvio entre o número de horas alocadas às tarefas do projecto e o número real de horas dedicadas às mesmas.

O desvio é calculado como uma percentagem para todas as tarefas concluídas, e os desvios calculados são representados num histograma. O eixo vertical mostra o número de tarefas dentro de cada intervalo de desvio. São calculados dinamicamente seis intervalos de desvio.

Rácio de Horas Extraordinárias
-------------------------------

Este rácio resume a sobrecarga dos recursos alocados às tarefas do projecto. É calculado usando a fórmula: **rácio de horas extraordinárias = sobrecarga / (carga + sobrecarga)**.

   *   = 0 é favorável, indicando que os recursos não estão sobrecarregados.
   *   > 0 é desfavorável, indicando que os recursos estão sobrecarregados.

Rácio de Disponibilidade
------------------------

Este rácio resume a capacidade livre dos recursos actualmente alocados ao projecto. Por conseguinte, mede a disponibilidade dos recursos para receber mais alocações sem ficarem sobrecarregados. É calculado como: **rácio de disponibilidade = (1 - carga/capacidade) * 100**

   *   Os valores possíveis estão entre 0% (totalmente atribuído) e 100% (não atribuído).

Tempo
=====

São incluídos dois gráficos: um histograma para o desvio de tempo no tempo de conclusão das tarefas do projecto e um gráfico circular para as violações de prazo.

Adiantamento ou Atraso na Conclusão de Tarefas
-----------------------------------------------

Este cálculo determina a diferença em dias entre o tempo de conclusão planeado para as tarefas do projecto e o seu tempo de conclusão real. A data de conclusão planeada é retirada do gráfico de *Gantt* e a data de conclusão real é retirada do último tempo registado para a tarefa.

O adiantamento ou atraso na conclusão das tarefas é representado num histograma. O eixo vertical mostra o número de tarefas com um valor de diferença em dias de adiantamento/atraso correspondente ao intervalo de dias da abcissa. São calculados seis intervalos dinâmicos de desvio de conclusão de tarefas.

   *   Valores negativos significam conclusão antes do prazo.
   *   Valores positivos significam conclusão após o prazo.

Violações de Prazo
------------------

Esta secção calcula a margem em relação ao prazo do projecto, se definido. Adicionalmente, um gráfico circular apresenta a percentagem de tarefas que cumprem o seu prazo. Três tipos de valores são incluídos no gráfico:

   *   Percentagem de tarefas sem prazo configurado.
   *   Percentagem de tarefas concluídas com uma data de conclusão real posterior ao seu prazo. A data de conclusão real é retirada do último tempo registado para a tarefa.
   *   Percentagem de tarefas concluídas com uma data de conclusão real anterior ao seu prazo.
