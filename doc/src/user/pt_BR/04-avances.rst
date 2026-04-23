Progresso
#########

.. contents::

O progresso do projeto indica o grau em que o tempo estimado de conclusão do projeto está sendo cumprido. O progresso da tarefa indica o grau em que a tarefa está sendo concluída de acordo com sua conclusão estimada.

Geralmente, o progresso não pode ser medido automaticamente. Um membro da equipe com experiência ou uma lista de verificação deve determinar o grau de conclusão de uma tarefa ou projeto.

É importante notar a distinção entre as horas atribuídas a uma tarefa ou projeto e o progresso dessa tarefa ou projeto. Embora o número de horas utilizadas possa ser maior ou menor do que o esperado, o projeto pode estar adiantado ou atrasado em relação à sua conclusão estimada no dia monitorado. Várias situações podem surgir dessas duas medições:

*   **Menos horas consumidas do que o esperado, mas o projeto está atrasado:** O progresso é inferior ao estimado para o dia monitorado.
*   **Menos horas consumidas do que o esperado, e o projeto está adiantado:** O progresso é superior ao estimado para o dia monitorado.
*   **Mais horas consumidas do que o esperado, e o projeto está atrasado:** O progresso é inferior ao estimado para o dia monitorado.
*   **Mais horas consumidas do que o esperado, mas o projeto está adiantado:** O progresso é superior ao estimado para o dia monitorado.

A visualização de planejamento permite comparar essas situações utilizando informações sobre o progresso feito e as horas utilizadas. Este capítulo explicará como inserir informações para monitorar o progresso.

A filosofia por trás do monitoramento de progresso é baseada no fato de os usuários definirem o nível no qual desejam monitorar seus projetos. Por exemplo, se os usuários desejam monitorar projetos, eles precisam apenas inserir informações para elementos de nível 1. Se desejam um monitoramento mais preciso no nível da tarefa, devem inserir informações de progresso em níveis inferiores. O sistema então agregará os dados para cima através da hierarquia.

Gerenciamento de Tipos de Progresso
=====================================

As empresas têm necessidades variadas ao monitorar o progresso do projeto, particularmente as tarefas envolvidas. Por isso, o sistema inclui "tipos de progresso". Os usuários podem definir diferentes tipos de progresso para medir o progresso de uma tarefa. Por exemplo, uma tarefa pode ser medida em porcentagem, mas essa porcentagem também pode ser traduzida em progresso em *Toneladas* com base no acordo com o cliente.

Um tipo de progresso tem um nome, um valor máximo e um valor de precisão:

*   **Nome:** Um nome descritivo que os usuários reconhecerão ao selecionar o tipo de progresso. Este nome deve indicar claramente que tipo de progresso está sendo medido.
*   **Valor Máximo:** O valor máximo que pode ser estabelecido para uma tarefa ou projeto como medição total do progresso. Por exemplo, se você estiver trabalhando com *Toneladas* e o máximo normal for 4000 toneladas, e nenhuma tarefa requerer mais de 4000 toneladas de qualquer material, então 4000 seria o valor máximo.
*   **Valor de Precisão:** O valor de incremento permitido para o tipo de progresso. Por exemplo, se o progresso em *Toneladas* deve ser medido em números inteiros, o valor de precisão seria 1. A partir daí, apenas números inteiros podem ser inseridos como medições de progresso (por exemplo, 1, 2, 300).

O sistema tem dois tipos de progresso padrão:

*   **Porcentagem:** Um tipo de progresso geral que mede o progresso de um projeto ou tarefa com base em uma porcentagem de conclusão estimada. Por exemplo, uma tarefa está 30% concluída dos 100% estimados para um dia específico.
*   **Unidades:** Um tipo de progresso geral que mede o progresso em unidades sem especificar o tipo de unidade. Por exemplo, uma tarefa envolve a criação de 3000 unidades, e o progresso é de 500 unidades do total de 3000.

.. figure:: images/tipos-avances.png
   :scale: 50

   Administração de Tipos de Progresso

Os usuários podem criar novos tipos de progresso da seguinte forma:

*   Vá à seção "Administração".
*   Clique na opção "Gerenciar tipos de progresso" no menu de segundo nível.
*   O sistema exibirá uma lista dos tipos de progresso existentes.
*   Para cada tipo de progresso, os usuários podem:

    *   Editar
    *   Excluir

*   Os usuários podem então criar um novo tipo de progresso.
*   Ao editar ou criar um tipo de progresso, o sistema exibe um formulário com as seguintes informações:

    *   Nome do tipo de progresso.
    *   Valor máximo permitido para o tipo de progresso.
    *   Valor de precisão para o tipo de progresso.

Inserção de Progresso por Tipo
================================

O progresso é inserido para elementos de projeto, mas também pode ser inserido usando um atalho a partir das tarefas de planejamento. Os usuários são responsáveis por decidir qual tipo de progresso associar a cada elemento de projeto.

Os usuários podem inserir um único tipo de progresso padrão para todo o projeto.

Antes de medir o progresso, os usuários devem associar o tipo de progresso escolhido ao projeto. Por exemplo, podem escolher o progresso em porcentagem para medir o progresso em toda a tarefa ou uma taxa de progresso acordada se as medições de progresso acordadas com o cliente forem inseridas no futuro.

.. figure:: images/avance.png
   :scale: 40

   Tela de Inserção de Progresso com Visualização Gráfica

Para inserir medições de progresso:

*   Selecione o tipo de progresso ao qual o progresso será adicionado.
    *   Se não existir nenhum tipo de progresso, um novo deve ser criado.
*   No formulário que aparece sob os campos "Valor" e "Data", insira o valor absoluto da medição e a data da medição.
*   O sistema armazena automaticamente os dados inseridos.

Comparação de Progresso para um Elemento de Projeto
====================================================

Os usuários podem comparar graficamente o progresso feito nos projetos com as medições realizadas. Todos os tipos de progresso têm uma coluna com um botão de verificação ("Mostrar"). Quando este botão é selecionado, é exibido o gráfico de progresso das medições realizadas para o elemento de projeto.

.. figure:: images/contraste-avance.png
   :scale: 40

   Comparação de Vários Tipos de Progresso
