Agendador
#########

.. contents::

O agendador foi concebido para agendar tarefas de forma dinâmica. É desenvolvido utilizando o *Spring Framework Quartz scheduler*.

Para utilizar este agendador de forma eficaz, as tarefas (tarefas Quartz) a agendar devem ser criadas primeiro. De seguida, essas tarefas podem ser adicionadas à base de dados, uma vez que todas as tarefas a agendar são armazenadas na base de dados.

Quando o agendador arranca, lê as tarefas a agendar ou desagendar da base de dados e agenda-as ou remove-as em conformidade. Posteriormente, as tarefas podem ser adicionadas, actualizadas ou removidas de forma dinâmica através da interface de utilizador ``Agendamento de tarefas``.

.. NOTE::
   O agendador arranca quando a aplicação web LibrePlan arranca e pára quando a aplicação pára.

.. NOTE::
   Este agendador suporta apenas ``expressões cron`` para agendar tarefas.

Os critérios que o agendador utiliza para agendar ou remover tarefas quando arranca são os seguintes:

Para todas as tarefas:

* Agendar

  * A tarefa tem um *Conector*, e o *Conector* está activado, e a tarefa tem permissão para ser agendada.
  * A tarefa não tem *Conector* e tem permissão para ser agendada.

* Remover

  * A tarefa tem um *Conector*, e o *Conector* não está activado.
  * A tarefa tem um *Conector*, e o *Conector* está activado, mas a tarefa não tem permissão para ser agendada.
  * A tarefa não tem *Conector* e não tem permissão para ser agendada.

.. NOTE::
   As tarefas não podem ser reagendadas ou desagendadas se estiverem actualmente em execução.

Vista de Lista de Agendamento de Tarefas
========================================

A vista ``Lista de agendamento de tarefas`` permite aos utilizadores:

*   Adicionar uma nova tarefa.
*   Editar uma tarefa existente.
*   Remover uma tarefa.
*   Iniciar um processo manualmente.

Adicionar ou Editar Tarefa
==========================

Na vista ``Lista de agendamento de tarefas``, clique em:

*   ``Criar`` para adicionar uma nova tarefa, ou
*   ``Editar`` para modificar a tarefa seleccionada.

Ambas as acções abrirão um ``formulário de tarefa`` de criação/edição. O ``formulário`` apresenta as seguintes propriedades:

*   Campos:

    *   **Grupo da tarefa:** O nome do grupo da tarefa.
    *   **Nome da tarefa:** O nome da tarefa.
    *   **Expressão cron:** Um campo de leitura apenas com um botão ``Editar`` para abrir a janela de introdução de ``expressão cron``.
    *   **Nome da classe da tarefa:** Uma ``lista pendente`` para seleccionar a tarefa (uma tarefa existente).
    *   **Conector:** Uma ``lista pendente`` para seleccionar um conector. Não é obrigatório.
    *   **Agendar:** Uma caixa de verificação para indicar se esta tarefa deve ser agendada.

*   Botões:

    *   **Guardar:** Para guardar ou actualizar uma tarefa tanto na base de dados como no agendador. O utilizador é então devolvido à ``Vista de lista de agendamento de tarefas``.
    *   **Guardar e continuar:** O mesmo que "Guardar", mas o utilizador não é devolvido à ``Vista de lista de agendamento de tarefas``.
    *   **Cancelar:** Nada é guardado e o utilizador é devolvido à ``Vista de lista de agendamento de tarefas``.

*   E uma secção de sugestões sobre a sintaxe da expressão cron.

Janela Pop-up de Expressão Cron
--------------------------------

Para introduzir correctamente a ``expressão cron``, é utilizado um formulário pop-up de ``expressão cron``. Neste formulário, pode introduzir a ``expressão cron`` desejada. Consulte também a sugestão sobre a ``expressão cron``. Se introduzir uma ``expressão cron`` inválida, será notificado imediatamente.

Remover Tarefa
==============

Clique no botão ``Remover`` para eliminar a tarefa tanto da base de dados como do agendador. O sucesso ou falha desta acção será apresentado.

Iniciar Tarefa Manualmente
==========================

Como alternativa a aguardar que a tarefa seja executada conforme agendado, pode clicar neste botão para iniciar o processo directamente. Posteriormente, as informações de sucesso ou falha serão apresentadas numa ``janela pop-up``.
