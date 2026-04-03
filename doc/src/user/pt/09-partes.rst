Relatórios de Trabalho
######################

.. contents::

Os relatórios de trabalho permitem a monitorização das horas que os recursos dedicam às tarefas às quais estão atribuídos.

O programa permite aos utilizadores configurar novos formulários para introdução de horas dedicadas, especificando os campos que pretendem que apareçam nesses formulários. Isto permite a incorporação de relatórios de tarefas realizadas por trabalhadores e a monitorização da actividade dos trabalhadores.

Antes de os utilizadores poderem adicionar entradas para recursos, devem definir pelo menos um tipo de relatório de trabalho. Este tipo define a estrutura do relatório, incluindo todas as linhas que lhe são adicionadas. Os utilizadores podem criar tantos tipos de relatórios de trabalho quantos necessários no sistema.

Tipos de Relatórios de Trabalho
=================================

Um relatório de trabalho consiste numa série de campos comuns a todo o relatório e num conjunto de linhas de relatório de trabalho com valores específicos para os campos definidos em cada linha. Por exemplo, recursos e tarefas são comuns a todos os relatórios. No entanto, podem existir outros novos campos, como "incidentes", que não são necessários em todos os tipos de relatórios.

Os utilizadores podem configurar diferentes tipos de relatórios de trabalho para que uma empresa possa conceber os seus relatórios de acordo com as suas necessidades específicas:

.. figure:: images/work-report-types.png
   :scale: 40

   Tipos de Relatórios de Trabalho

A administração de tipos de relatórios de trabalho permite aos utilizadores configurar estes tipos e adicionar novos campos de texto ou etiquetas opcionais. No primeiro separador para edição de tipos de relatórios de trabalho, é possível configurar o tipo para os atributos obrigatórios (se se aplicam a todo o relatório ou se são especificados ao nível da linha) e adicionar novos campos opcionais.

Os campos obrigatórios que devem aparecer em todos os relatórios de trabalho são os seguintes:

*   **Nome e Código:** Campos de identificação para o nome do tipo de relatório de trabalho e o seu código.
*   **Data:** Campo para a data do relatório.
*   **Recurso:** Trabalhador ou máquina que aparece no relatório ou linha de relatório de trabalho.
*   **Elemento de Projeto:** Código do elemento de projeto ao qual o trabalho efectuado é atribuído.
*   **Gestão de Horas:** Determina a política de atribuição de horas a usar, que pode ser:

    *   **De acordo com as Horas Atribuídas:** As horas são atribuídas com base nas horas atribuídas.
    *   **De acordo com as Horas de Início e Fim:** As horas são calculadas com base nos horários de início e fim.
    *   **De acordo com o Número de Horas e o Intervalo de Início e Fim:** São permitidas discrepâncias, e o número de horas tem prioridade.

Os utilizadores podem adicionar novos campos aos relatórios:

*   **Tipo de Etiqueta:** Os utilizadores podem solicitar ao sistema que apresente uma etiqueta ao preencher o relatório de trabalho. Por exemplo, o tipo de etiqueta cliente, se o utilizador desejar introduzir o cliente para quem o trabalho foi realizado em cada relatório.
*   **Campos Livres:** Campos onde o texto pode ser introduzido livremente no relatório de trabalho.

.. figure:: images/work-report-type.png
   :scale: 50

   Criar um Tipo de Relatório de Trabalho com Campos Personalizados

Os utilizadores podem configurar os campos de data, recurso e elemento de projeto para aparecerem no cabeçalho do relatório, o que significa que se aplicam a todo o relatório, ou podem ser adicionados a cada uma das linhas.

Por fim, novos campos de texto adicionais ou etiquetas podem ser adicionados aos existentes, no cabeçalho do relatório de trabalho ou em cada linha, utilizando os campos "Texto adicional" e "Tipo de etiqueta", respectivamente. Os utilizadores podem configurar a ordem em que estes elementos devem ser introduzidos no separador "Gestão de campos adicionais e etiquetas".

Lista de Relatórios de Trabalho
================================

Após a configuração do formato dos relatórios a incorporar no sistema, os utilizadores podem introduzir os detalhes no formulário criado de acordo com a estrutura definida no tipo de relatório de trabalho correspondente. Para tal, os utilizadores precisam de seguir estes passos:

*   Clique no botão "Novo relatório de trabalho" associado ao relatório desejado na lista de tipos de relatórios de trabalho.
*   O programa apresenta então o relatório com base nas configurações fornecidas para o tipo. Veja a imagem seguinte.

.. figure:: images/work-report-type.png
   :scale: 50

   Estrutura do Relatório de Trabalho Baseada no Tipo

*   Seleccione todos os campos mostrados para o relatório:

    *   **Recurso:** Se o cabeçalho tiver sido escolhido, o recurso é mostrado apenas uma vez. Alternativamente, para cada linha do relatório, é necessário escolher um recurso.
    *   **Código da Tarefa:** Código da tarefa à qual o relatório de trabalho está a ser atribuído. À semelhança dos restantes campos, se o campo estiver no cabeçalho, o valor é introduzido uma vez ou tantas vezes quantas necessárias nas linhas do relatório.
    *   **Data:** Data do relatório ou de cada linha, dependendo de o cabeçalho ou a linha estar configurado.
    *   **Número de Horas:** O número de horas de trabalho no projecto.
    *   **Horas de Início e Fim:** Horas de início e fim do trabalho para calcular as horas de trabalho definitivas. Este campo só aparece no caso das políticas de atribuição de horas "De acordo com as Horas de Início e Fim" e "De acordo com o Número de Horas e o Intervalo de Início e Fim".
    *   **Tipo de Horas:** Permite aos utilizadores escolher o tipo de hora, por exemplo, "Normal", "Extraordinária", etc.

*   Clique em "Guardar" ou "Guardar e continuar".
