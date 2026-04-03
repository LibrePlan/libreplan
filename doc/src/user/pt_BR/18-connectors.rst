Conectores
##########

.. contents::

Os conectores são aplicações cliente do *LibrePlan* que podem ser utilizadas para comunicar com servidores (web) para obter dados, processá-los e armazená-los. Atualmente, existem três conectores: o conector JIRA, o conector Tim Enterprise e o conector de E-mail.

Configuração
============

Os conectores devem ser configurados corretamente antes de poderem ser utilizados. Podem ser configurados a partir da tela "Configurações Principais" na aba "Conectores".

A tela do conector inclui:

*   **Lista suspensa:** Uma lista de conectores disponíveis.
*   **Tela de edição de propriedades:** Um formulário de edição de propriedades para o conector selecionado.
*   **Botão de teste de conexão:** Um botão para testar a conexão com o conector.

Selecione o conector que deseja configurar na lista suspensa de conectores. Um formulário de editor de propriedades para o conector selecionado será exibido. No formulário de editor de propriedades, você pode alterar os valores das propriedades conforme necessário e testar suas configurações usando o botão "Testar conexão".

.. NOTE::

   As propriedades são configuradas com valores padrão. A propriedade mais importante é "Ativado". Por padrão, está definido como "N". Isso indica que o conector não será utilizado a menos que você altere o valor para "Y" e salve as alterações.

Conector JIRA
=============

O JIRA é um sistema de rastreamento de problemas e projetos.

O conector JIRA é uma aplicação que pode ser utilizada para solicitar dados do servidor web JIRA para problemas JIRA e processar a resposta. A solicitação é baseada em etiquetas JIRA. No JIRA, as etiquetas podem ser utilizadas para categorizar problemas. A solicitação é estruturada da seguinte forma: recuperar todos os problemas que são categorizados por este nome de etiqueta.

O conector recebe a resposta, que neste caso são os problemas, e os converte em "Elementos de Projeto" e "Folhas de Horas" do *LibrePlan*.

O *conector JIRA* deve ser configurado corretamente antes de poder ser utilizado.

Configuração
------------

Na tela "Configurações Principais", escolha a aba "Conectores". Na tela de conectores, selecione o conector JIRA na lista suspensa. Uma tela de editor de propriedades será então exibida.

Nesta tela, você pode configurar os seguintes valores de propriedades:

*   **Ativado:** Y/N, indicando se deseja utilizar o conector JIRA. O padrão é "N."
*   **URL do servidor:** O caminho absoluto para o servidor web JIRA.
*   **Nome de usuário e senha:** As credenciais do usuário para autorização.
*   **Etiquetas JIRA: lista separada por vírgulas de etiquetas ou URL:** Você pode inserir a URL da etiqueta ou uma lista de etiquetas separada por vírgulas.
*   **Tipo de horas:** O tipo de horas de trabalho. O padrão é "Padrão."

.. NOTE::

   **Etiquetas JIRA:** Atualmente, o servidor web JIRA não suporta fornecer uma lista de todas as etiquetas disponíveis. Como solução alternativa, desenvolvemos um script PHP simples que realiza uma consulta SQL simples no banco de dados JIRA para buscar todas as etiquetas distintas. Você pode utilizar este script PHP como "URL de etiquetas JIRA" ou inserir as etiquetas que deseja como texto separado por vírgulas no campo "Etiquetas JIRA".

Por fim, clique no botão "Testar conexão" para testar se você consegue conectar-se ao servidor web JIRA e que suas configurações estão corretas.

Sincronização
-------------

Na janela do projeto, em "Dados gerais", você pode iniciar a sincronização de elementos de projeto com problemas JIRA.

Clique no botão "Sincronizar com JIRA" para iniciar a sincronização.

*   Se for a primeira vez, uma janela pop-up (com uma lista de etiquetas com preenchimento automático) será exibida. Nesta janela, você pode selecionar uma etiqueta para sincronizar e clicar no botão "Iniciar sincronização" para começar o processo de sincronização, ou clicar no botão "Cancelar" para cancelá-lo.

*   Se uma etiqueta já estiver sincronizada, a última data de sincronização e a etiqueta serão exibidas na tela JIRA. Neste caso, nenhuma janela pop-up para selecionar uma etiqueta será exibida. Em vez disso, o processo de sincronização iniciará diretamente para essa etiqueta (já sincronizada) exibida.

.. NOTE::

   A relação entre "Projeto" e "etiqueta" é de um para um. Apenas uma etiqueta pode ser sincronizada com um "Projeto."

.. NOTE::

   Após uma (re)sincronização bem-sucedida, as informações serão gravadas no banco de dados e a tela JIRA será atualizada com a última data de sincronização e etiqueta.

A (re)sincronização é realizada em duas fases:

*   **Fase 1:** Sincronização de elementos de projeto, incluindo atribuição e medições de progresso.
*   **Fase 2:** Sincronização de folhas de horas.

.. NOTE::

   Se a Fase 1 falhar, a Fase 2 não será realizada e nenhuma informação será gravada no banco de dados.

.. NOTE::

   As informações de sucesso ou falha serão exibidas em uma janela pop-up.

Após a conclusão bem-sucedida da sincronização, o resultado será exibido na aba "Estrutura Analítica do Projeto (tarefas WBS)" da tela "Detalhes do Projeto". Nesta interface, há duas alterações em relação ao WBS padrão:

*   A coluna "Total de horas da tarefa" é não modificável (somente leitura) porque a sincronização é unidirecional. As horas das tarefas só podem ser atualizadas no servidor web JIRA.
*   A coluna "Código" exibe as chaves dos problemas JIRA e são também hiperlinks para os problemas JIRA. Clique na chave desejada se quiser ir para o documento dessa chave (problema JIRA).

Agendamento
-----------

A ressincronização de problemas JIRA também pode ser realizada por meio do agendador. Acesse a tela "Agendamento de tarefas". Nessa tela, você pode configurar uma tarefa JIRA para realizar a sincronização. A tarefa busca as últimas etiquetas sincronizadas no banco de dados e as ressincroniza adequadamente. Consulte também o Manual do Agendador.

Conector Tim Enterprise
=======================

O Tim Enterprise é um produto holandês da Aenova. É uma aplicação web para a administração do tempo gasto em projetos e tarefas.

O conector Tim é uma aplicação que pode ser utilizada para comunicar com o servidor Tim Enterprise para:

*   Exportar todas as horas gastas por um trabalhador (usuário) em um projeto que poderiam ser registradas no Tim Enterprise.
*   Importar todos os horários do trabalhador (usuário) para planejar o recurso de forma eficaz.

O *conector Tim* deve ser configurado corretamente antes de poder ser utilizado.

Configuração
------------

Na tela "Configurações Principais", escolha a aba "Conectores". Na tela de conectores, selecione o conector Tim na lista suspensa. Uma tela de editor de propriedades será então exibida.

Nesta tela, você pode configurar os seguintes valores de propriedades:

*   **Ativado:** Y/N, indicando se deseja utilizar o conector Tim. O padrão é "N."
*   **URL do servidor:** O caminho absoluto para o servidor Tim Enterprise.
*   **Nome de usuário e senha:** As credenciais do usuário para autorização.
*   **Número de dias de folha de horas para Tim:** O número de dias anteriores que deseja exportar as folhas de horas.
*   **Número de dias de horário do Tim:** O número de dias futuros que deseja importar os horários.
*   **Fator de produtividade:** Horas de trabalho efetivas em porcentagem. O padrão é "100%."
*   **IDs de departamento para importar horário:** IDs de departamentos separados por vírgulas.

Por fim, clique no botão "Testar conexão" para testar se você consegue conectar-se ao servidor Tim Enterprise e que suas configurações estão corretas.

Exportação
----------

Na janela do projeto, em "Dados gerais", você pode iniciar a exportação de folhas de horas para o servidor Tim Enterprise.

Insira o "Código de produto Tim" e clique no botão "Exportar para Tim" para iniciar a exportação.

O conector Tim adiciona os seguintes campos junto com o código de produto:

*   O nome completo do trabalhador/usuário.
*   A data em que o trabalhador trabalhou em uma tarefa.
*   O esforço, ou horas trabalhadas na tarefa.
*   Uma opção indicando se o Tim Enterprise deve atualizar o registro ou inserir um novo.

A resposta do Tim Enterprise contém apenas uma lista de IDs de registros (números inteiros). Isso dificulta a determinação do que deu errado, pois a lista de respostas contém apenas números não relacionados aos campos da solicitação. Pressupõe-se que a solicitação de exportação (registro no Tim) foi bem-sucedida se todas as entradas da lista não contiverem valores "0". Caso contrário, a solicitação de exportação falhou para as entradas que contêm valores "0". Portanto, não é possível ver qual solicitação falhou, pois as entradas da lista contêm apenas o valor "0." A única forma de determinar isso é examinar o arquivo de log no servidor Tim Enterprise.

.. NOTE::

   Após uma exportação bem-sucedida, as informações serão gravadas no banco de dados e a tela Tim será atualizada com a última data de exportação e código de produto.

.. NOTE::

   As informações de sucesso ou falha serão exibidas em uma janela pop-up.

Agendamento da Exportação
--------------------------

O processo de exportação também pode ser realizado por meio do agendador. Acesse a tela "Agendamento de Tarefas". Nessa tela, você pode configurar uma tarefa de Exportação Tim. A tarefa busca as últimas folhas de horas exportadas no banco de dados e as reexporta adequadamente. Consulte também o manual do Agendador.

Importação
----------

A importação de horários só funciona com a ajuda do agendador. Não existe uma interface de usuário projetada para isso, pois nenhuma entrada do usuário é necessária. Acesse a tela "Agendamento de tarefas" e configure uma tarefa de Importação Tim. A tarefa percorre todos os departamentos configurados nas propriedades do conector e importa todos os horários de cada departamento. Consulte também o Manual do Agendador.

Para a importação, o conector Tim adiciona os seguintes campos na solicitação:

*   **Período:** O período (data de início - data de término) para o qual deseja importar o horário. Pode ser fornecido como critério de filtro.
*   **Departamento:** O departamento para o qual deseja importar o horário. Os departamentos são configuráveis.
*   Os campos em que você está interessado (como informações de pessoa, CategoriaHorário, etc.) que o servidor Tim deve incluir em sua resposta.

A resposta de importação contém os seguintes campos, que são suficientes para gerenciar os dias de exceção no *LibrePlan*:

*   **Informações de pessoa:** Nome e nome de rede.
*   **Departamento:** O departamento em que o trabalhador está trabalhando.
*   **Categoria de horário:** Informações sobre a presença/ausência (Aanwzig/afwezig) do trabalhador e o motivo (tipo de exceção do *LibrePlan*) caso o trabalhador esteja ausente.
*   **Data:** A data em que o trabalhador está presente/ausente.
*   **Hora:** A hora de início da presença/ausência, por exemplo, 08:00.
*   **Duração:** O número de horas em que o trabalhador está presente/ausente.

Ao converter a resposta de importação para "Dia de exceção" do *LibrePlan*, as seguintes traduções são levadas em conta:

*   Se a categoria de horário contiver o nome "Vakantie", será traduzida para "RESOURCE HOLIDAY."
*   A categoria de horário "Feestdag" será traduzida para "BANK HOLIDAY."
*   Todas as demais, como "Jus uren", "PLB uren", etc., devem ser adicionadas manualmente aos "Dias de Exceção do Calendário".

Além disso, na resposta de importação, o horário é dividido em duas ou três partes por dia: por exemplo, horário-manhã, horário-tarde e horário-noite. No entanto, o *LibrePlan* permite apenas um "Tipo de exceção" por dia. O conector Tim é então responsável por mesclar essas partes como um único tipo de exceção. Ou seja, a categoria de horário com a maior duração é assumida como o tipo de exceção válido, mas a duração total é a soma de todas as durações dessas partes de categoria.

Ao contrário do *LibrePlan*, no Tim Enterprise, a duração total no caso de o trabalhador estar de férias significa que o trabalhador não está disponível durante essa duração total. No entanto, no *LibrePlan*, se o trabalhador estiver de férias, a duração total deve ser zero. O conector Tim também trata dessa conversão.

Conector de E-mail
==================

O e-mail é um método de troca de mensagens digitais de um autor para um ou mais destinatários.

O conector de e-mail pode ser utilizado para definir as propriedades de conexão do servidor SMTP (Simple Mail Transfer Protocol).

O *conector de e-mail* deve ser configurado corretamente antes de poder ser utilizado.

Configuração
------------

Na tela "Configurações Principais", escolha a aba "Conectores". Na tela de conectores, selecione o conector de E-mail na lista suspensa. Uma tela de editor de propriedades será então exibida.

Nesta tela, você pode configurar os seguintes valores de propriedades:

*   **Ativado:** Y/N, indicando se deseja utilizar o conector de E-mail. O padrão é "N."
*   **Protocolo:** O tipo de protocolo SMTP.
*   **Host:** O caminho absoluto para o servidor SMTP.
*   **Porta:** A porta do servidor SMTP.
*   **Endereço de origem:** O endereço de e-mail do remetente da mensagem.
*   **Nome de usuário:** O nome de usuário para o servidor SMTP.
*   **Senha:** A senha para o servidor SMTP.

Por fim, clique no botão "Testar conexão" para testar se você consegue conectar-se ao servidor SMTP e que suas configurações estão corretas.

Editar Modelo de E-mail
-----------------------

Na janela do projeto, em "Configuração" e depois "Editar Modelos de E-mail", você pode modificar os modelos de e-mail para mensagens.

Você pode escolher:

*   **Idioma do modelo:**
*   **Tipo de modelo:**
*   **Assunto do e-mail:**
*   **Conteúdo do modelo:**

Você precisa especificar o idioma porque a aplicação web enviará e-mails aos usuários no idioma que escolheram em suas preferências. Você precisa escolher o tipo de modelo. O tipo é a função do usuário, o que significa que este e-mail será enviado apenas aos usuários que estejam na função selecionada (tipo). Você precisa definir o assunto do e-mail. O assunto é um breve resumo do tópico da mensagem. Você precisa definir o conteúdo do e-mail. Trata-se de qualquer informação que deseja enviar ao usuário. Existem também algumas palavras-chave que você pode utilizar na mensagem; a aplicação web irá analisá-las e definir um novo valor em vez da palavra-chave.

Agendamento de E-mails
-----------------------

O envio de e-mails só pode ser realizado por meio do agendador. Acesse "Configuração" e depois a tela "Agendamento de Tarefas". Nessa tela, você pode configurar uma tarefa de envio de e-mails. A tarefa obtém uma lista de notificações de e-mail, reúne dados e os envia para o e-mail do usuário. Consulte também o manual do Agendador.

.. NOTE::

   As informações de sucesso ou falha serão exibidas em uma janela pop-up.
