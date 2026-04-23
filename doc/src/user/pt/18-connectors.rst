Conectores
##########

.. contents::

Os conectores são aplicações cliente do *LibrePlan* que podem ser utilizadas para comunicar com servidores (web) para obter dados, processá-los e armazená-los. Actualmente, existem três conectores: o conector JIRA, o conector Tim Enterprise e o conector de E-mail.

Configuração
============

Os conectores devem ser configurados correctamente antes de poderem ser utilizados. Podem ser configurados a partir do ecrã "Configurações Principais" no separador "Conectores".

O ecrã do conector inclui:

*   **Lista pendente:** Uma lista de conectores disponíveis.
*   **Ecrã de edição de propriedades:** Um formulário de edição de propriedades para o conector seleccionado.
*   **Botão de teste de ligação:** Um botão para testar a ligação com o conector.

Seleccione o conector que pretende configurar na lista pendente de conectores. Será apresentado um formulário de editor de propriedades para o conector seleccionado. No formulário de editor de propriedades, pode alterar os valores das propriedades conforme necessário e testar as suas configurações usando o botão "Testar ligação".

.. NOTE::

   As propriedades são configuradas com valores predefinidos. A propriedade mais importante é "Activado". Por predefinição, está definido como "N". Isto indica que o conector não será utilizado a menos que altere o valor para "Y" e guarde as alterações.

Conector JIRA
=============

O JIRA é um sistema de rastreamento de problemas e projectos.

O conector JIRA é uma aplicação que pode ser utilizada para solicitar dados do servidor web JIRA para problemas JIRA e processar a resposta. A solicitação é baseada em etiquetas JIRA. No JIRA, as etiquetas podem ser utilizadas para categorizar problemas. A solicitação é estruturada da seguinte forma: obter todos os problemas que são categorizados por este nome de etiqueta.

O conector recebe a resposta, que neste caso são os problemas, e converte-os em "Elementos de Projeto" e "Folhas de Horas" do *LibrePlan*.

O *conector JIRA* deve ser configurado correctamente antes de poder ser utilizado.

Configuração
------------

No ecrã "Configurações Principais", escolha o separador "Conectores". No ecrã de conectores, seleccione o conector JIRA na lista pendente. Será então apresentado um ecrã de editor de propriedades.

Neste ecrã, pode configurar os seguintes valores de propriedades:

*   **Activado:** Y/N, indicando se pretende utilizar o conector JIRA. O valor predefinido é "N."
*   **URL do servidor:** O caminho absoluto para o servidor web JIRA.
*   **Nome de utilizador e palavra-passe:** As credenciais do utilizador para autorização.
*   **Etiquetas JIRA: lista separada por vírgulas de etiquetas ou URL:** Pode introduzir o URL da etiqueta ou uma lista de etiquetas separada por vírgulas.
*   **Tipo de horas:** O tipo de horas de trabalho. O valor predefinido é "Predefinido."

.. NOTE::

   **Etiquetas JIRA:** Actualmente, o servidor web JIRA não suporta fornecer uma lista de todas as etiquetas disponíveis. Como solução alternativa, desenvolvemos um script PHP simples que realiza uma consulta SQL simples na base de dados JIRA para obter todas as etiquetas distintas. Pode utilizar este script PHP como "URL de etiquetas JIRA" ou introduzir as etiquetas que pretende como texto separado por vírgulas no campo "Etiquetas JIRA".

Por fim, clique no botão "Testar ligação" para testar se consegue ligar-se ao servidor web JIRA e que as suas configurações estão correctas.

Sincronização
-------------

Na janela do projecto, em "Dados gerais", pode iniciar a sincronização de elementos de projeto com problemas JIRA.

Clique no botão "Sincronizar com JIRA" para iniciar a sincronização.

*   Se for a primeira vez, será apresentada uma janela pop-up (com uma lista de etiquetas com preenchimento automático). Nesta janela, pode seleccionar uma etiqueta para sincronizar e clicar no botão "Iniciar sincronização" para começar o processo de sincronização, ou clicar no botão "Cancelar" para o cancelar.

*   Se uma etiqueta já estiver sincronizada, a última data de sincronização e a etiqueta serão apresentadas no ecrã JIRA. Neste caso, não será apresentada nenhuma janela pop-up para seleccionar uma etiqueta. Em vez disso, o processo de sincronização iniciará directamente para essa etiqueta (já sincronizada) apresentada.

.. NOTE::

   A relação entre "Projeto" e "etiqueta" é de um para um. Apenas uma etiqueta pode ser sincronizada com um "Projeto."

.. NOTE::

   Após uma (re)sincronização bem-sucedida, as informações serão escritas na base de dados e o ecrã JIRA será actualizado com a última data de sincronização e etiqueta.

A (re)sincronização é realizada em duas fases:

*   **Fase 1:** Sincronização de elementos de projeto, incluindo atribuição e medições de progresso.
*   **Fase 2:** Sincronização de folhas de horas.

.. NOTE::

   Se a Fase 1 falhar, a Fase 2 não será realizada e nenhuma informação será escrita na base de dados.

.. NOTE::

   As informações de sucesso ou falha serão apresentadas numa janela pop-up.

Após a conclusão bem-sucedida da sincronização, o resultado será apresentado no separador "Estrutura de Decomposição do Trabalho (tarefas WBS)" do ecrã "Detalhes do Projecto". Nesta interface, há duas alterações em relação ao WBS padrão:

*   A coluna "Total de horas da tarefa" é não modificável (apenas leitura) porque a sincronização é unidireccional. As horas das tarefas só podem ser actualizadas no servidor web JIRA.
*   A coluna "Código" apresenta as chaves dos problemas JIRA e são também hiperligações para os problemas JIRA. Clique na chave desejada se pretender ir para o documento dessa chave (problema JIRA).

Agendamento
-----------

A ressincronização de problemas JIRA também pode ser realizada através do agendador. Aceda ao ecrã "Agendamento de tarefas". Nesse ecrã, pode configurar uma tarefa JIRA para realizar a sincronização. A tarefa procura as últimas etiquetas sincronizadas na base de dados e ressincroniza-as em conformidade. Consulte também o Manual do Agendador.

Conector Tim Enterprise
=======================

O Tim Enterprise é um produto holandês da Aenova. É uma aplicação web para a administração do tempo gasto em projectos e tarefas.

O conector Tim é uma aplicação que pode ser utilizada para comunicar com o servidor Tim Enterprise para:

*   Exportar todas as horas gastas por um trabalhador (utilizador) num projecto que poderiam ser registadas no Tim Enterprise.
*   Importar todos os horários do trabalhador (utilizador) para planear o recurso de forma eficaz.

O *conector Tim* deve ser configurado correctamente antes de poder ser utilizado.

Configuração
------------

No ecrã "Configurações Principais", escolha o separador "Conectores". No ecrã de conectores, seleccione o conector Tim na lista pendente. Será então apresentado um ecrã de editor de propriedades.

Neste ecrã, pode configurar os seguintes valores de propriedades:

*   **Activado:** Y/N, indicando se pretende utilizar o conector Tim. O valor predefinido é "N."
*   **URL do servidor:** O caminho absoluto para o servidor Tim Enterprise.
*   **Nome de utilizador e palavra-passe:** As credenciais do utilizador para autorização.
*   **Número de dias de folha de horas para Tim:** O número de dias anteriores que pretende exportar as folhas de horas.
*   **Número de dias de horário do Tim:** O número de dias futuros que pretende importar os horários.
*   **Factor de produtividade:** Horas de trabalho efectivas em percentagem. O valor predefinido é "100%."
*   **IDs de departamento para importar horário:** IDs de departamentos separados por vírgulas.

Por fim, clique no botão "Testar ligação" para testar se consegue ligar-se ao servidor Tim Enterprise e que as suas configurações estão correctas.

Exportação
----------

Na janela do projecto, em "Dados gerais", pode iniciar a exportação de folhas de horas para o servidor Tim Enterprise.

Introduza o "Código de produto Tim" e clique no botão "Exportar para Tim" para iniciar a exportação.

O conector Tim adiciona os seguintes campos juntamente com o código de produto:

*   O nome completo do trabalhador/utilizador.
*   A data em que o trabalhador trabalhou numa tarefa.
*   O esforço, ou horas trabalhadas na tarefa.
*   Uma opção que indica se o Tim Enterprise deve actualizar o registo ou inserir um novo.

A resposta do Tim Enterprise contém apenas uma lista de IDs de registos (números inteiros). Isto dificulta a determinação do que correu mal, uma vez que a lista de respostas contém apenas números não relacionados com os campos da solicitação. Assume-se que a solicitação de exportação (registo no Tim) foi bem-sucedida se todas as entradas da lista não contiverem valores "0". Caso contrário, a solicitação de exportação falhou para as entradas que contêm valores "0". Por conseguinte, não é possível ver qual solicitação falhou, uma vez que as entradas da lista contêm apenas o valor "0." A única forma de determinar isto é examinar o ficheiro de registo no servidor Tim Enterprise.

.. NOTE::

   Após uma exportação bem-sucedida, as informações serão escritas na base de dados e o ecrã Tim será actualizado com a última data de exportação e código de produto.

.. NOTE::

   As informações de sucesso ou falha serão apresentadas numa janela pop-up.

Agendamento da Exportação
--------------------------

O processo de exportação também pode ser realizado através do agendador. Aceda ao ecrã "Agendamento de Tarefas". Nesse ecrã, pode configurar uma tarefa de Exportação Tim. A tarefa procura as últimas folhas de horas exportadas na base de dados e reexporta-as em conformidade. Consulte também o manual do Agendador.

Importação
----------

A importação de horários só funciona com a ajuda do agendador. Não existe uma interface de utilizador concebida para isto, uma vez que não é necessária nenhuma entrada do utilizador. Aceda ao ecrã "Agendamento de tarefas" e configure uma tarefa de Importação Tim. A tarefa percorre todos os departamentos configurados nas propriedades do conector e importa todos os horários de cada departamento. Consulte também o Manual do Agendador.

Para a importação, o conector Tim adiciona os seguintes campos na solicitação:

*   **Período:** O período (data de início - data de fim) para o qual pretende importar o horário. Pode ser fornecido como critério de filtro.
*   **Departamento:** O departamento para o qual pretende importar o horário. Os departamentos são configuráveis.
*   Os campos em que está interessado (como informações de pessoa, CategoriaHorário, etc.) que o servidor Tim deve incluir na sua resposta.

A resposta de importação contém os seguintes campos, que são suficientes para gerir os dias de excepção no *LibrePlan*:

*   **Informações de pessoa:** Nome e nome de rede.
*   **Departamento:** O departamento em que o trabalhador está a trabalhar.
*   **Categoria de horário:** Informações sobre a presença/ausência (Aanwzig/afwezig) do trabalhador e o motivo (tipo de excepção do *LibrePlan*) caso o trabalhador esteja ausente.
*   **Data:** A data em que o trabalhador está presente/ausente.
*   **Hora:** A hora de início da presença/ausência, por exemplo, 08:00.
*   **Duração:** O número de horas em que o trabalhador está presente/ausente.

Ao converter a resposta de importação para "Dia de excepção" do *LibrePlan*, as seguintes traduções são tidas em conta:

*   Se a categoria de horário contiver o nome "Vakantie", será traduzida para "RESOURCE HOLIDAY."
*   A categoria de horário "Feestdag" será traduzida para "BANK HOLIDAY."
*   Todas as restantes, como "Jus uren", "PLB uren", etc., devem ser adicionadas manualmente aos "Dias de Excepção do Calendário".

Além disso, na resposta de importação, o horário é dividido em duas ou três partes por dia: por exemplo, horário-manhã, horário-tarde e horário-noite. No entanto, o *LibrePlan* permite apenas um "Tipo de excepção" por dia. O conector Tim é então responsável por fundir estas partes como um único tipo de excepção. Ou seja, a categoria de horário com a duração mais longa é assumida como o tipo de excepção válido, mas a duração total é a soma de todas as durações destas partes de categoria.

Ao contrário do *LibrePlan*, no Tim Enterprise, a duração total em caso de o trabalhador estar de férias significa que o trabalhador não está disponível durante essa duração total. No entanto, no *LibrePlan*, se o trabalhador estiver de férias, a duração total deve ser zero. O conector Tim também trata desta tradução.

Conector de E-mail
==================

O e-mail é um método de troca de mensagens digitais de um autor para um ou mais destinatários.

O conector de e-mail pode ser utilizado para definir as propriedades de ligação do servidor SMTP (Simple Mail Transfer Protocol).

O *conector de e-mail* deve ser configurado correctamente antes de poder ser utilizado.

Configuração
------------

No ecrã "Configurações Principais", escolha o separador "Conectores". No ecrã de conectores, seleccione o conector de E-mail na lista pendente. Será então apresentado um ecrã de editor de propriedades.

Neste ecrã, pode configurar os seguintes valores de propriedades:

*   **Activado:** Y/N, indicando se pretende utilizar o conector de E-mail. O valor predefinido é "N."
*   **Protocolo:** O tipo de protocolo SMTP.
*   **Anfitrião:** O caminho absoluto para o servidor SMTP.
*   **Porta:** A porta do servidor SMTP.
*   **Endereço de origem:** O endereço de e-mail do remetente da mensagem.
*   **Nome de utilizador:** O nome de utilizador para o servidor SMTP.
*   **Palavra-passe:** A palavra-passe para o servidor SMTP.

Por fim, clique no botão "Testar ligação" para testar se consegue ligar-se ao servidor SMTP e que as suas configurações estão correctas.

Editar Modelo de E-mail
-----------------------

Na janela do projecto, em "Configuração" e depois "Editar Modelos de E-mail", pode modificar os modelos de e-mail para mensagens.

Pode escolher:

*   **Idioma do modelo:**
*   **Tipo de modelo:**
*   **Assunto do e-mail:**
*   **Conteúdo do modelo:**

É necessário especificar o idioma porque a aplicação web enviará e-mails aos utilizadores no idioma que escolheram nas suas preferências. É necessário escolher o tipo de modelo. O tipo é a função do utilizador, o que significa que este e-mail será enviado apenas aos utilizadores que estejam na função seleccionada (tipo). É necessário definir o assunto do e-mail. O assunto é um breve resumo do tópico da mensagem. É necessário definir o conteúdo do e-mail. Trata-se de qualquer informação que pretenda enviar ao utilizador. Existem também algumas palavras-chave que pode utilizar na mensagem; a aplicação web irá analisá-las e definir um novo valor em vez da palavra-chave.

Agendamento de E-mails
-----------------------

O envio de e-mails só pode ser realizado através do agendador. Aceda a "Configuração" e depois ao ecrã "Agendamento de Tarefas". Nesse ecrã, pode configurar uma tarefa de envio de e-mails. A tarefa obtém uma lista de notificações de e-mail, reúne dados e envia-os para o e-mail do utilizador. Consulte também o manual do Agendador.

.. NOTE::

   As informações de sucesso ou falha serão apresentadas numa janela pop-up.
