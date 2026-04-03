Critérios
#########

.. contents::

Os critérios são elementos utilizados no programa para categorizar tanto recursos quanto tarefas. As tarefas requerem critérios específicos, e os recursos devem atender a esses critérios.

Aqui está um exemplo de como os critérios são utilizados: Um recurso recebe o critério "soldador" (o que significa que o recurso cumpre a categoria "soldador"), e uma tarefa requer o critério "soldador" para ser concluída. Consequentemente, quando os recursos são alocados a tarefas usando a alocação genérica (em oposição à alocação específica), os trabalhadores com o critério "soldador" serão considerados. Para mais informações sobre os diferentes tipos de alocação, consulte o capítulo sobre alocação de recursos.

O programa permite várias operações envolvendo critérios:

*   Administração de critérios
*   Atribuição de critérios a recursos
*   Atribuição de critérios a tarefas
*   Filtragem de entidades com base em critérios. As tarefas e os elementos de projeto podem ser filtrados por critérios para realizar várias operações no programa.

Esta seção explicará apenas a primeira função, administração de critérios. Os dois tipos de alocação serão abordados posteriormente: alocação de recursos no capítulo "Gerenciamento de Recursos", e filtragem no capítulo "Planejamento de Tarefas".

Administração de Critérios
============================

A administração de critérios pode ser acessada através do menu de administração:

.. figure:: images/menu.png
   :scale: 50

   Abas do Menu de Primeiro Nível

A operação específica para gerenciar critérios é *Gerenciar critérios*. Essa operação permite listar os critérios disponíveis no sistema.

.. figure:: images/lista-criterios.png
   :scale: 50

   Lista de Critérios

Você pode acessar o formulário de criação/edição de critério clicando no botão *Criar*. Para editar um critério existente, clique no ícone de edição.

.. figure:: images/edicion-criterio.png
   :scale: 50

   Edição de Critérios

O formulário de edição de critérios, conforme mostrado na imagem anterior, permite realizar as seguintes operações:

*   **Editar o nome do critério.**
*   **Especificar se vários valores podem ser atribuídos simultaneamente ou apenas um valor para o tipo de critério selecionado.** Por exemplo, um recurso poderia cumprir dois critérios, "soldador" e "operador de torno".
*   **Especificar o tipo de critério:**

    *   **Genérico:** Um critério que pode ser utilizado tanto para máquinas quanto para trabalhadores.
    *   **Trabalhador:** Um critério que só pode ser utilizado para trabalhadores.
    *   **Máquina:** Um critério que só pode ser utilizado para máquinas.

*   **Indicar se o critério é hierárquico.** Às vezes, os critérios precisam ser tratados hierarquicamente. Por exemplo, atribuir um critério a um elemento não atribui automaticamente esse critério a elementos dele derivados. Um exemplo claro de critério hierárquico é "localização". Por exemplo, uma pessoa designada com a localização "Galícia" também pertencerá à "Espanha".
*   **Indicar se o critério está autorizado.** É assim que os usuários desativam critérios. Depois que um critério foi criado e utilizado em dados históricos, ele não pode ser alterado. Em vez disso, pode ser desativado para evitar que apareça nas listas de seleção.
*   **Descrever o critério.**
*   **Adicionar novos valores.** Um campo de entrada de texto com o botão *Novo critério* está localizado na segunda parte do formulário.
*   **Editar os nomes dos valores de critérios existentes.**
*   **Mover valores de critérios para cima ou para baixo na lista de valores de critérios atuais.**
*   **Remover um valor de critério da lista.**

O formulário de administração de critérios segue o comportamento de formulário descrito na introdução, oferecendo três ações: *Salvar*, *Salvar e Fechar* e *Fechar*.
