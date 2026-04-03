Relatório de Horas Trabalhadas por Recurso
##########################################

.. contents::

Finalidade
==========

Este relatório extrai uma lista de tarefas e o tempo que os recursos lhes dedicaram num período especificado. Vários filtros permitem ao utilizador refinar a consulta para obter apenas as informações pretendidas e excluir dados irrelevantes.

Parâmetros de Entrada e Filtros
================================

* **Datas**.
    * *Tipo*: Opcional.
    * *Dois campos de data*:
        * *Data de Início:* É a data mais antiga para os relatórios de trabalho a incluir. Os relatórios de trabalho com datas anteriores à *Data de Início* são excluídos. Se este parâmetro ficar em branco, os relatórios de trabalho não são filtrados pela *Data de Início*.
        * *Data de Fim:* É a data mais recente para os relatórios de trabalho a incluir. Os relatórios de trabalho com datas posteriores à *Data de Fim* são excluídos. Se este parâmetro ficar em branco, os relatórios de trabalho não são filtrados pela *Data de Fim*.

*   **Filtrar por Trabalhadores:**
    *   *Tipo:* Opcional.
    *   *Como funciona:* Pode seleccionar um ou mais trabalhadores para restringir os relatórios de trabalho ao tempo registado por esses trabalhadores específicos. Para adicionar um trabalhador como filtro, procure-o no selector e clique no botão *Adicionar*. Se este filtro ficar vazio, os relatórios de trabalho são obtidos independentemente do trabalhador.

*   **Filtrar por Etiquetas:**
    *   *Tipo:* Opcional.
    *   *Como funciona:* Pode adicionar uma ou mais etiquetas para utilizar como filtros, procurando-as no selector e clicando no botão *Adicionar*. Estas etiquetas são utilizadas para seleccionar as tarefas a incluir nos resultados ao calcular as horas que lhes foram dedicadas. Este filtro pode ser aplicado a folhas de horas, tarefas, ambos ou nenhum.

*   **Filtrar por Critérios:**
    *   *Tipo:* Opcional.
    *   *Como funciona:* Pode seleccionar um ou mais critérios, procurando-os no selector e clicando no botão *Adicionar*. Estes critérios são utilizados para seleccionar os recursos que satisfaçam pelo menos um deles. O relatório apresentará todo o tempo dedicado pelos recursos que cumpram um dos critérios seleccionados.

Resultado
=========

Cabeçalho
---------

O cabeçalho do relatório apresenta os filtros configurados e aplicados ao relatório actual.

Rodapé
------

A data em que o relatório foi gerado é apresentada no rodapé.

Corpo
-----

O corpo do relatório é constituído por vários grupos de informação.

*   O primeiro nível de agregação é por recurso. Todo o tempo dedicado por um recurso é apresentado em conjunto sob o cabeçalho. Cada recurso é identificado por:

    *   *Trabalhador:* Apelido, Nome Próprio.
    *   *Máquina:* Nome.

    Uma linha de resumo apresenta o número total de horas trabalhadas pelo recurso.

*   O segundo nível de agrupamento é por *data*. Todos os relatórios de um recurso específico na mesma data são apresentados em conjunto.

    Uma linha de resumo apresenta o número total de horas trabalhadas pelo recurso nessa data.

*   O nível final lista os relatórios de trabalho do trabalhador nesse dia. As informações apresentadas para cada linha de relatório de trabalho são:

    *   *Código da Tarefa:* O código da tarefa à qual as horas registadas são atribuídas.
    *   *Nome da Tarefa:* O nome da tarefa à qual as horas registadas são atribuídas.
    *   *Hora de Início:* Opcional. É a hora a que o recurso começou a trabalhar na tarefa.
    *   *Hora de Fim:* Opcional. É a hora a que o recurso terminou de trabalhar na tarefa na data especificada.
    *   *Campos de Texto:* Opcional. Se a linha do relatório de trabalho tiver campos de texto, os valores preenchidos são apresentados aqui. O formato é: <Nome do campo de texto>:<Valor>
    *   *Etiquetas:* Depende de o modelo de relatório de trabalho ter um campo de etiqueta na sua definição. Se houver várias etiquetas, são apresentadas na mesma coluna. O formato é: <Nome do tipo de etiqueta>:<Valor da etiqueta>
