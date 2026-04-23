Relatório de Horas Trabalhadas por Recurso
##########################################

.. contents::

Finalidade
==========

Este relatório extrai uma lista de tarefas e o tempo que os recursos dedicaram a elas dentro de um período especificado. Vários filtros permitem ao usuário refinar a consulta para obter apenas as informações desejadas e excluir dados irrelevantes.

Parâmetros de Entrada e Filtros
================================

* **Datas**.
    * *Tipo*: Opcional.
    * *Dois campos de data*:
        * *Data de Início:* É a data mais antiga para os relatórios de trabalho a serem incluídos. Relatórios de trabalho com datas anteriores à *Data de Início* são excluídos. Se este parâmetro for deixado em branco, os relatórios de trabalho não são filtrados pela *Data de Início*.
        * *Data de Fim:* É a data mais recente para os relatórios de trabalho a serem incluídos. Relatórios de trabalho com datas posteriores à *Data de Fim* são excluídos. Se este parâmetro for deixado em branco, os relatórios de trabalho não são filtrados pela *Data de Fim*.

*   **Filtrar por Trabalhadores:**
    *   *Tipo:* Opcional.
    *   *Como funciona:* Você pode selecionar um ou mais trabalhadores para restringir os relatórios de trabalho ao tempo registrado por esses trabalhadores específicos. Para adicionar um trabalhador como filtro, pesquise-o no seletor e clique no botão *Adicionar*. Se este filtro estiver vazio, os relatórios de trabalho serão recuperados independentemente do trabalhador.

*   **Filtrar por Etiquetas:**
    *   *Tipo:* Opcional.
    *   *Como funciona:* Você pode adicionar uma ou mais etiquetas para usar como filtros, pesquisando-as no seletor e clicando no botão *Adicionar*. Essas etiquetas são usadas para selecionar as tarefas a serem incluídas nos resultados ao calcular as horas dedicadas a elas. Este filtro pode ser aplicado a folhas de horas, tarefas, ambos ou nenhum.

*   **Filtrar por Critérios:**
    *   *Tipo:* Opcional.
    *   *Como funciona:* Você pode selecionar um ou mais critérios, pesquisando-os no seletor e clicando no botão *Adicionar*. Esses critérios são usados para selecionar os recursos que satisfaçam pelo menos um deles. O relatório mostrará todo o tempo dedicado pelos recursos que atendam a um dos critérios selecionados.

Resultado
=========

Cabeçalho
---------

O cabeçalho do relatório exibe os filtros que foram configurados e aplicados ao relatório atual.

Rodapé
------

A data em que o relatório foi gerado é listada no rodapé.

Corpo
-----

O corpo do relatório é composto por vários grupos de informações.

*   O primeiro nível de agregação é por recurso. Todo o tempo dedicado por um recurso é mostrado em conjunto abaixo do cabeçalho. Cada recurso é identificado por:

    *   *Trabalhador:* Sobrenome, Nome.
    *   *Máquina:* Nome.

    Uma linha de resumo mostra o número total de horas trabalhadas pelo recurso.

*   O segundo nível de agrupamento é por *data*. Todos os relatórios de um recurso específico na mesma data são mostrados em conjunto.

    Uma linha de resumo mostra o número total de horas trabalhadas pelo recurso nessa data.

*   O nível final lista os relatórios de trabalho do trabalhador naquele dia. As informações exibidas para cada linha de relatório de trabalho são:

    *   *Código da Tarefa:* O código da tarefa à qual as horas registradas são atribuídas.
    *   *Nome da Tarefa:* O nome da tarefa à qual as horas registradas são atribuídas.
    *   *Hora de Início:* Opcional. É a hora em que o recurso começou a trabalhar na tarefa.
    *   *Hora de Fim:* Opcional. É a hora em que o recurso terminou de trabalhar na tarefa na data especificada.
    *   *Campos de Texto:* Opcional. Se a linha do relatório de trabalho tiver campos de texto, os valores preenchidos são mostrados aqui. O formato é: <Nome do campo de texto>:<Valor>
    *   *Etiquetas:* Depende de o modelo de relatório de trabalho ter um campo de etiqueta em sua definição. Se houver várias etiquetas, elas são mostradas na mesma coluna. O formato é: <Nome do tipo de etiqueta>:<Valor da etiqueta>
