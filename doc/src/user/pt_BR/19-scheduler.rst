Agendador
#########

.. contents::

O agendador foi projetado para agendar tarefas de forma dinâmica. É desenvolvido utilizando o *Spring Framework Quartz scheduler*.

Para utilizar este agendador de forma eficaz, as tarefas (tarefas Quartz) a agendar devem ser criadas primeiro. Em seguida, essas tarefas podem ser adicionadas ao banco de dados, pois todas as tarefas a agendar são armazenadas no banco de dados.

Quando o agendador inicia, ele lê as tarefas a agendar ou desagendar do banco de dados e as agenda ou remove adequadamente. Posteriormente, as tarefas podem ser adicionadas, atualizadas ou removidas de forma dinâmica por meio da interface de usuário ``Agendamento de tarefas``.

.. NOTE::
   O agendador inicia quando a aplicação web LibrePlan inicia e para quando a aplicação para.

.. NOTE::
   Este agendador suporta apenas ``expressões cron`` para agendar tarefas.

Os critérios que o agendador utiliza para agendar ou remover tarefas quando inicia são os seguintes:

Para todas as tarefas:

* Agendar

  * A tarefa tem um *Conector*, e o *Conector* está ativado, e a tarefa tem permissão para ser agendada.
  * A tarefa não tem *Conector* e tem permissão para ser agendada.

* Remover

  * A tarefa tem um *Conector*, e o *Conector* não está ativado.
  * A tarefa tem um *Conector*, e o *Conector* está ativado, mas a tarefa não tem permissão para ser agendada.
  * A tarefa não tem *Conector* e não tem permissão para ser agendada.

.. NOTE::
   As tarefas não podem ser reagendadas ou desagendadas se estiverem atualmente em execução.

Vista de Lista de Agendamento de Tarefas
========================================

A vista ``Lista de agendamento de tarefas`` permite aos usuários:

*   Adicionar uma nova tarefa.
*   Editar uma tarefa existente.
*   Remover uma tarefa.
*   Iniciar um processo manualmente.

Adicionar ou Editar Tarefa
==========================

Na vista ``Lista de agendamento de tarefas``, clique em:

*   ``Criar`` para adicionar uma nova tarefa, ou
*   ``Editar`` para modificar a tarefa selecionada.

Ambas as ações abrirão um ``formulário de tarefa`` de criação/edição. O ``formulário`` exibe as seguintes propriedades:

*   Campos:

    *   **Grupo da tarefa:** O nome do grupo da tarefa.
    *   **Nome da tarefa:** O nome da tarefa.
    *   **Expressão cron:** Um campo somente leitura com um botão ``Editar`` para abrir a janela de inserção de ``expressão cron``.
    *   **Nome da classe da tarefa:** Uma ``lista suspensa`` para selecionar a tarefa (uma tarefa existente).
    *   **Conector:** Uma ``lista suspensa`` para selecionar um conector. Não é obrigatório.
    *   **Agendar:** Uma caixa de seleção para indicar se esta tarefa deve ser agendada.

*   Botões:

    *   **Salvar:** Para salvar ou atualizar uma tarefa tanto no banco de dados como no agendador. O usuário é então retornado à ``Vista de lista de agendamento de tarefas``.
    *   **Salvar e continuar:** O mesmo que "Salvar", mas o usuário não é retornado à ``Vista de lista de agendamento de tarefas``.
    *   **Cancelar:** Nada é salvo e o usuário é retornado à ``Vista de lista de agendamento de tarefas``.

*   E uma seção de dicas sobre a sintaxe da expressão cron.

Janela Pop-up de Expressão Cron
--------------------------------

Para inserir corretamente a ``expressão cron``, é utilizado um formulário pop-up de ``expressão cron``. Neste formulário, você pode inserir a ``expressão cron`` desejada. Consulte também a dica sobre a ``expressão cron``. Se você inserir uma ``expressão cron`` inválida, será notificado imediatamente.

Remover Tarefa
==============

Clique no botão ``Remover`` para excluir a tarefa tanto do banco de dados como do agendador. O sucesso ou falha desta ação será exibido.

Iniciar Tarefa Manualmente
==========================

Como alternativa a aguardar que a tarefa seja executada conforme agendado, você pode clicar neste botão para iniciar o processo diretamente. Posteriormente, as informações de sucesso ou falha serão exibidas em uma ``janela pop-up``.
