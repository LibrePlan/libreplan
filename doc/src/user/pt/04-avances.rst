Progresso
#########

.. contents::

O progresso do projecto indica o grau em que o tempo estimado de conclusão do projecto está a ser cumprido. O progresso da tarefa indica o grau em que a tarefa está a ser concluída de acordo com a sua conclusão estimada.

Geralmente, o progresso não pode ser medido automaticamente. Um membro da equipa com experiência ou uma lista de verificação deve determinar o grau de conclusão de uma tarefa ou projecto.

É importante notar a distinção entre as horas atribuídas a uma tarefa ou projecto e o progresso dessa tarefa ou projecto. Embora o número de horas utilizadas possa ser mais ou menos do que o esperado, o projecto pode estar adiantado ou atrasado em relação à sua conclusão estimada no dia monitorizado. Várias situações podem surgir destas duas medições:

*   **Menos horas consumidas do que o esperado, mas o projecto está atrasado:** O progresso é inferior ao estimado para o dia monitorizado.
*   **Menos horas consumidas do que o esperado, e o projecto está adiantado:** O progresso é superior ao estimado para o dia monitorizado.
*   **Mais horas consumidas do que o esperado, e o projecto está atrasado:** O progresso é inferior ao estimado para o dia monitorizado.
*   **Mais horas consumidas do que o esperado, mas o projecto está adiantado:** O progresso é superior ao estimado para o dia monitorizado.

A vista de planeamento permite comparar estas situações utilizando informações sobre o progresso efectuado e as horas utilizadas. Este capítulo explicará como introduzir informações para monitorizar o progresso.

A filosofia subjacente à monitorização do progresso baseia-se no facto de os utilizadores definirem o nível ao qual pretendem monitorizar os seus projectos. Por exemplo, se os utilizadores pretendem monitorizar projetos, apenas precisam de introduzir informações para elementos de nível 1. Se pretenderem uma monitorização mais precisa ao nível da tarefa, devem introduzir informações de progresso em níveis inferiores. O sistema irá então agregar os dados para cima através da hierarquia.

Gestão de Tipos de Progresso
==============================

As empresas têm necessidades variadas ao monitorizar o progresso do projecto, nomeadamente as tarefas envolvidas. Por isso, o sistema inclui "tipos de progresso". Os utilizadores podem definir diferentes tipos de progresso para medir o progresso de uma tarefa. Por exemplo, uma tarefa pode ser medida em percentagem, mas esta percentagem também pode ser traduzida em progresso em *Toneladas* com base no acordo com o cliente.

Um tipo de progresso tem um nome, um valor máximo e um valor de precisão:

*   **Nome:** Um nome descritivo que os utilizadores reconhecerão ao seleccionar o tipo de progresso. Este nome deve indicar claramente que tipo de progresso está a ser medido.
*   **Valor Máximo:** O valor máximo que pode ser estabelecido para uma tarefa ou projecto como medição total do progresso. Por exemplo, se estiver a trabalhar com *Toneladas* e o máximo normal for 4000 toneladas, e nenhuma tarefa requerer nunca mais de 4000 toneladas de qualquer material, então 4000 seria o valor máximo.
*   **Valor de Precisão:** O valor de incremento permitido para o tipo de progresso. Por exemplo, se o progresso em *Toneladas* deve ser medido em números inteiros, o valor de precisão seria 1. A partir daí, apenas números inteiros podem ser introduzidos como medições de progresso (por exemplo, 1, 2, 300).

O sistema tem dois tipos de progresso predefinidos:

*   **Percentagem:** Um tipo de progresso geral que mede o progresso de um projecto ou tarefa com base numa percentagem de conclusão estimada. Por exemplo, uma tarefa está 30% concluída dos 100% estimados para um dia específico.
*   **Unidades:** Um tipo de progresso geral que mede o progresso em unidades sem especificar o tipo de unidade. Por exemplo, uma tarefa envolve a criação de 3000 unidades, e o progresso é de 500 unidades do total de 3000.

.. figure:: images/tipos-avances.png
   :scale: 50

   Administração de Tipos de Progresso

Os utilizadores podem criar novos tipos de progresso da seguinte forma:

*   Vá à secção "Administração".
*   Clique na opção "Gerir tipos de progresso" no menu de segundo nível.
*   O sistema apresentará uma lista dos tipos de progresso existentes.
*   Para cada tipo de progresso, os utilizadores podem:

    *   Editar
    *   Eliminar

*   Os utilizadores podem então criar um novo tipo de progresso.
*   Ao editar ou criar um tipo de progresso, o sistema apresenta um formulário com as seguintes informações:

    *   Nome do tipo de progresso.
    *   Valor máximo permitido para o tipo de progresso.
    *   Valor de precisão para o tipo de progresso.

Introdução de Progresso por Tipo
==================================

O progresso é introduzido para elementos de projeto, mas também pode ser introduzido usando um atalho a partir das tarefas de planeamento. Os utilizadores são responsáveis por decidir qual tipo de progresso associar a cada elemento de projeto.

Os utilizadores podem introduzir um único tipo de progresso predefinido para toda a projeto.

Antes de medir o progresso, os utilizadores devem associar o tipo de progresso escolhido à projeto. Por exemplo, podem escolher o progresso em percentagem para medir o progresso em toda a tarefa ou uma taxa de progresso acordada se as medições de progresso acordadas com o cliente forem introduzidas no futuro.

.. figure:: images/avance.png
   :scale: 40

   Ecrã de Introdução de Progresso com Visualização Gráfica

Para introduzir medições de progresso:

*   Seleccione o tipo de progresso ao qual o progresso será adicionado.
    *   Se não existir nenhum tipo de progresso, deve ser criado um novo.
*   No formulário que aparece sob os campos "Valor" e "Data", introduza o valor absoluto da medição e a data da medição.
*   O sistema armazena automaticamente os dados introduzidos.

Comparação de Progresso para um Elemento de Projeto
=======================================================

Os utilizadores podem comparar graficamente o progresso efectuado nas projetos com as medições realizadas. Todos os tipos de progresso têm uma coluna com um botão de verificação ("Mostrar"). Quando este botão é seleccionado, é apresentado o gráfico de progresso das medições efectuadas para o elemento de projeto.

.. figure:: images/contraste-avance.png
   :scale: 40

   Comparação de Vários Tipos de Progresso
