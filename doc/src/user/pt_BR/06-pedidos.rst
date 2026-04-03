Projetos e Elementos de Projeto
################################

.. contents::

Os projetos representam o trabalho a ser executado pelos usuários do programa. Cada projeto corresponde a um projeto que a empresa oferecerá aos seus clientes.

Um projeto consiste em um ou mais elementos de projeto. Cada elemento de projeto representa uma parte específica do trabalho a ser realizado e define como o trabalho no projeto deve ser planejado e executado. Os elementos de projeto são organizados hierarquicamente, sem limitações na profundidade da hierarquia. Essa estrutura hierárquica permite a herança de determinadas características, como etiquetas.

As seções a seguir descrevem as operações que os usuários podem realizar com projetos e elementos de projeto.

Projetos
========

Um projeto representa um projeto ou trabalho solicitado por um cliente à empresa. O projeto identifica o projeto no planejamento da empresa. Ao contrário de programas de gerenciamento abrangentes, o LibrePlan apenas requer determinados detalhes chave para um projeto. Esses detalhes são:

*   **Nome do Projeto:** O nome do projeto.
*   **Código do Projeto:** Um código único para o projeto.
*   **Valor Total do Projeto:** O valor financeiro total do projeto.
*   **Data de Início Estimada:** A data de início planejada para o projeto.
*   **Data de Fim:** A data de conclusão planejada para o projeto.
*   **Responsável:** O indivíduo responsável pelo projeto.
*   **Descrição:** Uma descrição do projeto.
*   **Calendário Atribuído:** O calendário associado ao projeto.
*   **Geração Automática de Códigos:** Uma configuração para instruir o sistema a gerar automaticamente códigos para elementos de projeto e grupos de horas.
*   **Preferência entre Dependências e Restrições:** Os usuários podem escolher se as dependências ou restrições têm prioridade em caso de conflitos.

No entanto, um projeto completo também inclui outras entidades associadas:

*   **Horas Atribuídas ao Projeto:** O total de horas alocadas ao projeto.
*   **Progresso Atribuído ao Projeto:** O progresso feito no projeto.
*   **Etiquetas:** Etiquetas atribuídas ao projeto.
*   **Critérios Atribuídos ao Projeto:** Critérios associados ao projeto.
*   **Materiais:** Materiais necessários para o projeto.
*   **Formulários de Qualidade:** Formulários de qualidade associados ao projeto.

Criar ou editar um projeto pode ser feito a partir de vários locais no programa:

*   **A partir da "Lista de Projetos" na Visão Geral da Empresa:**

    *   **Editar:** Clique no botão de edição no projeto desejado.
    *   **Criar:** Clique em "Novo projeto".

*   **A partir de um Projeto no Diagrama de Gantt:** Mude para a visualização de detalhes do projeto.

Os usuários podem acessar as seguintes abas ao editar um projeto:

*   **Edição dos Detalhes do Projeto:** Esta tela permite aos usuários editar os detalhes básicos do projeto:

    *   Nome
    *   Código
    *   Data de Início Estimada
    *   Data de Fim
    *   Responsável
    *   Cliente
    *   Descrição

    .. figure:: images/order-edition.png
       :scale: 50

       Edição de Projetos

*   **Lista de Elementos de Projeto:** Esta tela permite aos usuários realizar várias operações em elementos de projeto:

    *   Criar novos elementos de projeto.
    *   Promover um elemento de projeto um nível acima na hierarquia.
    *   Rebaixar um elemento de projeto um nível abaixo na hierarquia.
    *   Recuar um elemento de projeto (movê-lo para baixo na hierarquia).
    *   Avançar um elemento de projeto (movê-lo para cima na hierarquia).
    *   Filtrar elementos de projeto.
    *   Excluir elementos de projeto.
    *   Mover um elemento dentro da hierarquia por arrastar e soltar.

    .. figure:: images/order-elements-list.png
       :scale: 40

       Lista de Elementos de Projeto

*   **Horas Atribuídas:** Esta tela exibe o total de horas atribuídas ao projeto, agrupando as horas inseridas nos elementos de projeto.

    .. figure:: images/order-assigned-hours.png
       :scale: 50

       Atribuição de Horas ao Projeto pelos Trabalhadores

*   **Progresso:** Esta tela permite aos usuários atribuir tipos de progresso e inserir medições de progresso para o projeto. Consulte a seção "Progresso" para mais detalhes.

*   **Etiquetas:** Esta tela permite aos usuários atribuir etiquetas a um projeto e ver as etiquetas diretas e indiretas previamente atribuídas. Consulte a seção a seguir sobre edição de elementos de projeto para uma descrição detalhada do gerenciamento de etiquetas.

    .. figure:: images/order-labels.png
       :scale: 35

       Etiquetas de Projeto

*   **Critérios:** Esta tela permite aos usuários atribuir critérios que se aplicarão a todas as tarefas do projeto. Esses critérios serão automaticamente aplicados a todos os elementos de projeto, exceto os que foram explicitamente invalidados. Os grupos de horas dos elementos de projeto, que são agrupados por critérios, também podem ser visualizados, permitindo aos usuários identificar os critérios necessários para um projeto.

    .. figure:: images/order-criterions.png
       :scale: 50

       Critérios de Projeto

*   **Materiais:** Esta tela permite aos usuários atribuir materiais a projetos. Os materiais podem ser selecionados a partir das categorias de materiais disponíveis no programa. Os materiais são gerenciados da seguinte forma:

    *   Selecione a aba "Pesquisar materiais" na parte inferior da tela.
    *   Digite texto para pesquisar materiais ou selecione as categorias para as quais deseja encontrar materiais.
    *   O sistema filtra os resultados.
    *   Selecione os materiais desejados (múltiplos materiais podem ser selecionados pressionando a tecla "Ctrl").
    *   Clique em "Atribuir".
    *   O sistema exibe a lista de materiais já atribuídos ao projeto.
    *   Selecione as unidades e o status a atribuir ao projeto.
    *   Clique em "Salvar" ou "Salvar e continuar".
    *   Para gerenciar o recebimento de materiais, clique em "Dividir" para alterar o status de uma quantidade parcial de material.

    .. figure:: images/order-material.png
       :scale: 50

       Materiais Associados a um Projeto

*   **Qualidade:** Os usuários podem atribuir um formulário de qualidade ao projeto. Esse formulário é então preenchido para garantir que determinadas atividades associadas ao projeto sejam realizadas. Consulte a seção a seguir sobre edição de elementos de projeto para detalhes sobre o gerenciamento de formulários de qualidade.

    .. figure:: images/order-quality.png
       :scale: 50

       Formulário de Qualidade Associado ao Projeto

Edição de Elementos de Projeto
================================

Os elementos de projeto são editados a partir da aba "Lista de elementos de projeto" clicando no ícone de edição. Isso abre uma nova tela onde os usuários podem:

*   Editar informações sobre o elemento de projeto.
*   Ver horas atribuídas a elementos de projeto.
*   Gerenciar o progresso de elementos de projeto.
*   Gerenciar etiquetas de projeto.
*   Gerenciar critérios requeridos pelo elemento de projeto.
*   Gerenciar materiais.
*   Gerenciar formulários de qualidade.

As subseções a seguir descrevem cada uma dessas operações em detalhe.

Edição de Informações sobre o Elemento de Projeto
-------------------------------------------------

A edição de informações sobre o elemento de projeto inclui a modificação dos seguintes detalhes:

*   **Nome do Elemento de Projeto:** O nome do elemento de projeto.
*   **Código do Elemento de Projeto:** Um código único para o elemento de projeto.
*   **Data de Início:** A data de início planejada do elemento de projeto.
*   **Data de Fim Estimada:** A data de conclusão planejada do elemento de projeto.
*   **Total de Horas:** O total de horas alocadas ao elemento de projeto. Essas horas podem ser calculadas a partir dos grupos de horas adicionados ou inseridas diretamente. Se inseridas diretamente, as horas devem ser distribuídas entre os grupos de horas, e um novo grupo de horas criado se as porcentagens não corresponderem às porcentagens iniciais.
*   **Grupos de Horas:** Um ou mais grupos de horas podem ser adicionados ao elemento de projeto. **O objetivo desses grupos de horas** é definir os requisitos para os recursos que serão atribuídos para realizar o trabalho.
*   **Critérios:** Podem ser adicionados critérios que devem ser cumpridos para possibilitar a atribuição genérica para o elemento de projeto.

.. figure:: images/order-element-edition.png
   :scale: 50

   Edição de Elementos de Projeto

Visualização de Horas Atribuídas a Elementos de Projeto
--------------------------------------------------------

A aba "Horas atribuídas" permite aos usuários ver os relatórios de trabalho associados a um elemento de projeto e verificar quantas das horas estimadas já foram concluídas.

.. figure:: images/order-element-hours.png
   :scale: 50

   Horas Atribuídas a Elementos de Projeto

A tela está dividida em duas partes:

*   **Lista de Relatórios de Trabalho:** Os usuários podem ver a lista de relatórios de trabalho associados ao elemento de projeto, incluindo a data e hora, recurso e número de horas dedicadas à tarefa.
*   **Utilização das Horas Estimadas:** O sistema calcula o número total de horas dedicadas à tarefa e compara com as horas estimadas.

Gerenciamento do Progresso de Elementos de Projeto
---------------------------------------------------

A inserção de tipos de progresso e o gerenciamento do progresso de elementos de projeto estão descritos no capítulo "Progresso".

Gerenciamento de Etiquetas de Projeto
--------------------------------------

As etiquetas, conforme descrito no capítulo sobre etiquetas, permitem aos usuários categorizar elementos de projeto. Isso permite aos usuários agrupar informações de planejamento ou projeto com base nessas etiquetas.

Os usuários podem atribuir etiquetas diretamente a um elemento de projeto ou a um elemento de projeto de nível superior na hierarquia. Uma vez atribuída uma etiqueta por qualquer um dos métodos, o elemento de projeto e a tarefa de planejamento relacionada são associados à etiqueta e podem ser utilizados para filtragem subsequente.

.. figure:: images/order-element-tags.png
   :scale: 50

   Atribuição de Etiquetas para Elementos de Projeto

Conforme mostrado na imagem, os usuários podem realizar as seguintes ações a partir da aba **Etiquetas**:

*   **Ver Etiquetas Herdadas:** Ver etiquetas associadas ao elemento de projeto que foram herdadas de um elemento de projeto de nível superior. A tarefa de planejamento associada a cada elemento de projeto tem as mesmas etiquetas associadas.
*   **Ver Etiquetas Diretamente Atribuídas:** Ver etiquetas diretamente associadas ao elemento de projeto utilizando o formulário de atribuição para etiquetas de nível inferior.
*   **Atribuir Etiquetas Existentes:** Atribuir etiquetas pesquisando-as entre as etiquetas disponíveis no formulário abaixo da lista de etiquetas diretas. Para pesquisar uma etiqueta, clique no ícone da lupa ou digite as primeiras letras da etiqueta na caixa de texto para exibir as opções disponíveis.
*   **Criar e Atribuir Novas Etiquetas:** Criar novas etiquetas associadas a um tipo de etiqueta existente a partir deste formulário. Para isso, selecione um tipo de etiqueta e insira o valor da etiqueta para o tipo selecionado. O sistema cria automaticamente a etiqueta e a atribui ao elemento de projeto quando "Criar e atribuir" é clicado.

Gerenciamento de Critérios Requeridos pelo Elemento de Projeto e Grupos de Horas
--------------------------------------------------------------------------------

Tanto um projeto quanto um elemento de projeto podem ter critérios atribuídos que devem ser cumpridos para o trabalho ser realizado. Os critérios podem ser diretos ou indiretos:

*   **Critérios Diretos:** São atribuídos diretamente ao elemento de projeto. São critérios requeridos pelos grupos de horas no elemento de projeto.
*   **Critérios Indiretos:** São atribuídos a elementos de projeto de nível superior na hierarquia e são herdados pelo elemento que está sendo editado.

Além dos critérios requeridos, podem ser definidos um ou mais grupos de horas que fazem parte do elemento de projeto. Isso depende de o elemento de projeto conter outros elementos de projeto como nós filhos ou ser um nó folha. No primeiro caso, as informações sobre horas e grupos de horas só podem ser visualizadas. No entanto, os nós folha podem ser editados. Os nós folha funcionam da seguinte forma:

*   O sistema cria um grupo de horas padrão associado ao elemento de projeto. Os detalhes que podem ser modificados para um grupo de horas são:

    *   **Código:** O código para o grupo de horas (se não gerado automaticamente).
    *   **Tipo de Critério:** Os usuários podem escolher atribuir um critério de máquina ou trabalhador.
    *   **Número de Horas:** O número de horas no grupo de horas.
    *   **Lista de Critérios:** Os critérios a aplicar ao grupo de horas. Para adicionar novos critérios, clique em "Adicionar critério" e selecione um no mecanismo de pesquisa que aparece após clicar no botão.

*   Os usuários podem adicionar novos grupos de horas com características diferentes dos grupos de horas anteriores. Por exemplo, um elemento de projeto pode requerer um soldador (30 horas) e um pintor (40 horas).

.. figure:: images/order-element-criterion.png
   :scale: 50

   Atribuição de Critérios a Elementos de Projeto

Gerenciamento de Materiais
---------------------------

Os materiais são gerenciados em projetos como uma lista associada a cada elemento de projeto ou a um projeto em geral. A lista de materiais inclui os seguintes campos:

*   **Código:** O código do material.
*   **Data:** A data associada ao material.
*   **Unidades:** O número de unidades necessário.
*   **Tipo de Unidade:** O tipo de unidade utilizado para medir o material.
*   **Preço por Unidade:** O preço por unidade.
*   **Preço Total:** O preço total (calculado multiplicando o preço por unidade pelo número de unidades).
*   **Categoria:** A categoria à qual o material pertence.
*   **Status:** O status do material (por exemplo, Recebido, Solicitado, Pendente, Em processamento, Cancelado).

O trabalho com materiais é feito da seguinte forma:

*   Selecione a aba "Materiais" em um elemento de projeto.
*   O sistema exibe duas subabas: "Materiais" e "Pesquisar materiais".
*   Se o elemento de projeto não tiver materiais atribuídos, a primeira aba estará vazia.
*   Clique em "Pesquisar materiais" na parte inferior esquerda da janela.
*   O sistema exibe a lista de categorias disponíveis e materiais associados.

.. figure:: images/order-element-material-search.png
   :scale: 50

   Pesquisa de Materiais

*   Selecione categorias para refinar a pesquisa de materiais.
*   O sistema exibe os materiais que pertencem às categorias selecionadas.
*   Na lista de materiais, selecione os materiais a atribuir ao elemento de projeto.
*   Clique em "Atribuir".
*   O sistema exibe a lista selecionada de materiais na aba "Materiais" com novos campos a preencher.

.. figure:: images/order-element-material-assign.png
   :scale: 50

   Atribuição de Materiais a Elementos de Projeto

*   Selecione as unidades, status e data para os materiais atribuídos.

Para o acompanhamento subsequente de materiais, é possível alterar o status de um grupo de unidades do material recebido. Isso é feito da seguinte forma:

*   Clique no botão "Dividir" na lista de materiais à direita de cada linha.
*   Selecione o número de unidades para dividir a linha.
*   O programa exibe duas linhas com o material dividido.
*   Altere o status da linha que contém o material.

A vantagem de utilizar essa ferramenta de divisão é a capacidade de receber entregas parciais de material sem ter que esperar pela entrega total para marcá-la como recebida.

Gerenciamento de Formulários de Qualidade
------------------------------------------

Alguns elementos de projeto requerem a certificação de que determinadas tarefas foram concluídas antes de poderem ser marcados como completos. Por isso, o programa tem formulários de qualidade, que consistem em uma lista de questões que são consideradas importantes se respondidas positivamente.

É importante notar que um formulário de qualidade deve ser criado previamente para ser atribuído a um elemento de projeto.

Para gerenciar formulários de qualidade:

*   Vá à aba "Formulários de qualidade".

    .. figure:: images/order-element-quality.png
       :scale: 50

       Atribuição de Formulários de Qualidade a Elementos de Projeto

*   O programa tem um mecanismo de pesquisa para formulários de qualidade. Existem dois tipos de formulários de qualidade: por elemento ou por porcentagem.

    *   **Elemento:** Cada elemento é independente.
    *   **Porcentagem:** Cada questão aumenta o progresso do elemento de projeto por uma porcentagem. As porcentagens devem poder somar 100%.

*   Selecione um dos formulários criados na interface de administração e clique em "Atribuir".
*   O programa atribui o formulário escolhido da lista de formulários atribuídos ao elemento de projeto.
*   Clique no botão "Editar" no elemento de projeto.
*   O programa exibe as questões do formulário de qualidade na lista inferior.
*   Marque as questões que foram concluídas como alcançadas.

    *   Se o formulário de qualidade se basear em porcentagens, as questões são respondidas em ordem.
    *   Se o formulário de qualidade se basear em elementos, as questões podem ser respondidas em qualquer ordem.
