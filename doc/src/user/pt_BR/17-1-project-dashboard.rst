Painel do Projeto
#################

.. contents::

O painel do projeto é uma perspectiva do *LibrePlan* que contém um conjunto de **KPIs (Indicadores-Chave de Desempenho)** para ajudar a avaliar o desempenho de um projeto em termos de:

   *   Progresso do trabalho
   *   Custo
   *   Estado dos recursos alocados
   *   Restrições de tempo

Indicadores de Desempenho do Progresso
=======================================

Dois indicadores são calculados: a porcentagem de progresso do projeto e o estado das tarefas.

Porcentagem de Progresso do Projeto
------------------------------------

Este gráfico exibe o progresso geral de um projeto, comparando-o com o progresso esperado com base no gráfico de *Gantt*.

O progresso é representado por duas barras:

   *   *Progresso Atual:* O progresso atual baseado nas medições realizadas.
   *   *Progresso Esperado:* O progresso que o projeto deveria ter alcançado neste momento, de acordo com o plano do projeto.

Para ver o valor real medido de cada barra, passe o cursor do mouse sobre a barra.

O progresso geral do projeto é estimado usando vários métodos diferentes, pois não há uma única abordagem universalmente correta:

   *   **Progresso por Propagação:** É o tipo de progresso definido como progresso de propagação no nível do projeto. Neste caso, não há como calcular um valor esperado e apenas a barra atual é exibida.
   *   **Por Todas as Horas de Tarefas:** O progresso de todas as tarefas do projeto é calculado como média para obter o valor geral. É uma média ponderada que considera o número de horas alocadas a cada tarefa.
   *   **Pelas Horas do Caminho Crítico:** O progresso das tarefas pertencentes a qualquer um dos caminhos críticos do projeto é calculado como média para obter o valor geral. É uma média ponderada que considera o total de horas alocadas a cada tarefa envolvida.
   *   **Pela Duração do Caminho Crítico:** O progresso das tarefas pertencentes a qualquer um dos caminhos críticos é calculado como média ponderada, mas desta vez considerando a duração de cada tarefa envolvida em vez das horas atribuídas.

Estado das Tarefas
------------------

Um gráfico de pizza mostra a porcentagem de tarefas do projeto em diferentes estados. Os estados definidos são:

   *   **Concluídas:** Tarefas completas, identificadas por um valor de progresso de 100%.
   *   **Em Andamento:** Tarefas que estão atualmente em execução. Essas tarefas têm um valor de progresso diferente de 0% ou 100%, ou algum tempo de trabalho foi registrado.
   *   **Prontas para Iniciar:** Tarefas com 0% de progresso, sem tempo registrado, todas as suas tarefas dependentes *FINISH_TO_START* estão *concluídas* e todas as suas tarefas dependentes *START_TO_START* estão *concluídas* ou *em andamento*.
   *   **Bloqueadas:** Tarefas com 0% de progresso, sem tempo registrado e com tarefas dependentes anteriores que não estão *em andamento* nem no estado *pronto para iniciar*.

Indicadores de Custo
====================

Vários indicadores de custo da *Gestão do Valor Agregado* são calculados:

   *   **CV (Variação de Custo):** A diferença entre a *curva do Valor Agregado* e a *curva do Custo Real* no momento atual. Valores positivos indicam um benefício e valores negativos indicam uma perda.
   *   **ACWP (Custo Real do Trabalho Realizado):** O número total de horas registradas no projeto no momento atual.
   *   **CPI (Índice de Desempenho de Custo):** A proporção *Valor Agregado / Custo Real*.

        *   > 100 é favorável, indicando que o projeto está abaixo do orçamento.
        *   = 100 também é favorável, indicando que o custo está exatamente conforme planejado.
        *   < 100 é desfavorável, indicando que o custo de conclusão do trabalho é maior que o planejado.
   *   **ETC (Estimativa para Concluir):** O tempo restante para concluir o projeto.
   *   **BAC (Orçamento na Conclusão):** O valor total de trabalho alocado no plano do projeto.
   *   **EAC (Estimativa na Conclusão):** A projeção do gerente do custo total na conclusão do projeto, baseada no *CPI*.
   *   **VAC (Variação na Conclusão):** A diferença entre o *BAC* e o *EAC*.

        *   < 0 indica que o projeto está acima do orçamento.
        *   > 0 indica que o projeto está abaixo do orçamento.

Recursos
========

Para analisar o projeto do ponto de vista dos recursos, dois índices e um histograma são fornecidos.

Histograma do Desvio de Estimativa em Tarefas Concluídas
---------------------------------------------------------

Este histograma calcula o desvio entre o número de horas alocadas às tarefas do projeto e o número real de horas dedicadas a elas.

O desvio é calculado como uma porcentagem para todas as tarefas concluídas, e os desvios calculados são representados em um histograma. O eixo vertical mostra o número de tarefas dentro de cada intervalo de desvio. Seis intervalos de desvio são calculados dinamicamente.

Índice de Horas Extras
----------------------

Este índice resume a sobrecarga dos recursos alocados às tarefas do projeto. É calculado usando a fórmula: **índice de horas extras = sobrecarga / (carga + sobrecarga)**.

   *   = 0 é favorável, indicando que os recursos não estão sobrecarregados.
   *   > 0 é desfavorável, indicando que os recursos estão sobrecarregados.

Índice de Disponibilidade
-------------------------

Este índice resume a capacidade livre dos recursos atualmente alocados ao projeto. Portanto, mede a disponibilidade dos recursos para receber mais alocações sem ficarem sobrecarregados. É calculado como: **índice de disponibilidade = (1 - carga/capacidade) * 100**

   *   Os valores possíveis estão entre 0% (totalmente atribuído) e 100% (não atribuído).

Tempo
=====

Dois gráficos são incluídos: um histograma para o desvio de tempo no tempo de conclusão das tarefas do projeto e um gráfico de pizza para as violações de prazo.

Adiantamento ou Atraso na Conclusão de Tarefas
-----------------------------------------------

Este cálculo determina a diferença em dias entre o tempo de conclusão planejado para as tarefas do projeto e seu tempo de conclusão real. A data de conclusão planejada é retirada do gráfico de *Gantt* e a data de conclusão real é retirada do último tempo registrado para a tarefa.

O adiantamento ou atraso na conclusão das tarefas é representado em um histograma. O eixo vertical mostra o número de tarefas com um valor de diferença em dias de adiantamento/atraso correspondente ao intervalo de dias da abscissa. Seis intervalos dinâmicos de desvio de conclusão de tarefas são calculados.

   *   Valores negativos significam conclusão antes do prazo.
   *   Valores positivos significam conclusão após o prazo.

Violações de Prazo
------------------

Esta seção calcula a margem em relação ao prazo do projeto, se definido. Adicionalmente, um gráfico de pizza mostra a porcentagem de tarefas que cumprem seu prazo. Três tipos de valores são incluídos no gráfico:

   *   Porcentagem de tarefas sem prazo configurado.
   *   Porcentagem de tarefas concluídas com uma data de conclusão real posterior ao seu prazo. A data de conclusão real é retirada do último tempo registrado para a tarefa.
   *   Porcentagem de tarefas concluídas com uma data de conclusão real anterior ao seu prazo.
