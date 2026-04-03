Gerenciamento de Recursos
##########################

.. _recursos:
.. contents::

O programa gerencia dois tipos distintos de recursos: pessoal e máquinas.

Recursos de Pessoal
-------------------

Os recursos de pessoal representam os trabalhadores da empresa. Suas características principais são:

*   Cumprem um ou mais critérios genéricos ou específicos de trabalhadores.
*   Podem ser especificamente atribuídos a uma tarefa.
*   Podem ser atribuídos genericamente a uma tarefa que requer um critério de recurso.
*   Podem ter um calendário padrão ou específico, conforme necessário.

Recursos Máquina
----------------

Os recursos máquina representam as máquinas da empresa. Suas características principais são:

*   Cumprem um ou mais critérios genéricos ou específicos de máquinas.
*   Podem ser especificamente atribuídos a uma tarefa.
*   Podem ser atribuídos genericamente a uma tarefa que requer um critério de máquina.
*   Podem ter um calendário padrão ou específico, conforme necessário.
*   O programa inclui uma tela de configuração onde um valor *alfa* pode ser definido para representar a relação máquina/trabalhador.

    *   O valor *alfa* indica a quantidade de tempo de trabalhador necessária para operar a máquina. Por exemplo, um valor alfa de 0,5 significa que cada 8 horas de operação da máquina requerem 4 horas do tempo de um trabalhador.
    *   Os usuários podem atribuir um valor *alfa* especificamente a um trabalhador, designando esse trabalhador para operar a máquina durante essa porcentagem de tempo.
    *   Os usuários também podem fazer uma atribuição genérica baseada em um critério, de modo que uma porcentagem de uso seja atribuída a todos os recursos que cumprem esse critério e têm tempo disponível. A atribuição genérica funciona de forma semelhante à atribuição genérica para tarefas, conforme descrito anteriormente.

Gerenciamento de Recursos
--------------------------

Os usuários podem criar, editar e desativar (mas não excluir permanentemente) trabalhadores e máquinas da empresa navegando até a seção "Recursos". Esta seção fornece as seguintes funcionalidades:

*   **Lista de Trabalhadores:** Exibe uma lista numerada de trabalhadores, permitindo aos usuários gerenciar seus detalhes.
*   **Lista de Máquinas:** Exibe uma lista numerada de máquinas, permitindo aos usuários gerenciar seus detalhes.

Gerenciamento de Trabalhadores
================================

O gerenciamento de trabalhadores é acessado indo à seção "Recursos" e selecionando "Lista de trabalhadores". Os usuários podem editar qualquer trabalhador da lista clicando no ícone de edição padrão.

Ao editar um trabalhador, os usuários podem acessar as seguintes abas:

1.  **Detalhes do Trabalhador:** Esta aba permite aos usuários editar os detalhes básicos de identificação do trabalhador:

    *   Nome
    *   Sobrenome(s)
    *   Documento de identificação nacional (CPF)
    *   Recurso baseado em fila (ver seção sobre Recursos Baseados em Fila)

    .. figure:: images/worker-personal-data.png
       :scale: 50

       Edição dos Dados Pessoais dos Trabalhadores

2.  **Critérios:** Esta aba é utilizada para configurar os critérios que um trabalhador cumpre. Os usuários podem atribuir quaisquer critérios de trabalhador ou genéricos que considerem adequados. É crucial que os trabalhadores cumpram critérios para maximizar a funcionalidade do programa. Para atribuir critérios:

    i.  Clique no botão "Adicionar critérios".
    ii. Pesquise o critério a ser adicionado e selecione o mais adequado.
    iii. Clique no botão "Adicionar".
    iv. Selecione a data de início a partir da qual o critério se torna aplicável.
    v.  Selecione a data de fim para aplicar o critério ao recurso. Esta data é opcional se o critério for considerado indefinido.

    .. figure:: images/worker-criterions.png
       :scale: 50

       Associação de Critérios a Trabalhadores

3.  **Calendário:** Esta aba permite aos usuários configurar um calendário específico para o trabalhador. Todos os trabalhadores têm um calendário padrão atribuído; no entanto, é possível atribuir um calendário específico a cada trabalhador com base em um calendário existente.

    .. figure:: images/worker-calendar.png
       :scale: 50

       Aba de Calendário para um Recurso

4.  **Categoria de Custo:** Esta aba permite aos usuários configurar a categoria de custo que um trabalhador cumpre durante um determinado período. Essa informação é utilizada para calcular os custos associados a um trabalhador em um projeto.

    .. figure:: images/worker-costcategory.png
       :scale: 50

       Aba de Categoria de Custo para um Recurso

A atribuição de recursos é explicada na seção "Atribuição de Recursos".

Gerenciamento de Máquinas
==========================

As máquinas são tratadas como recursos para todos os efeitos. Por isso, assim como os trabalhadores, as máquinas podem ser gerenciadas e atribuídas a tarefas. A atribuição de recursos é abordada na seção "Atribuição de Recursos", que explicará as características específicas das máquinas.

As máquinas são gerenciadas a partir da entrada de menu "Recursos". Esta seção tem uma operação chamada "Lista de máquinas", que exibe as máquinas da empresa. Os usuários podem editar ou excluir uma máquina desta lista.

Ao editar máquinas, o sistema exibe uma série de abas para gerenciar diferentes detalhes:

1.  **Detalhes da Máquina:** Esta aba permite aos usuários editar os detalhes de identificação da máquina:

    i.  Nome
    ii. Código da máquina
    iii. Descrição da máquina

    .. figure:: images/machine-data.png
       :scale: 50

       Edição dos Detalhes da Máquina

2.  **Critérios:** Assim como nos recursos de trabalhadores, esta aba é utilizada para adicionar critérios que a máquina cumpre. Dois tipos de critérios podem ser atribuídos a máquinas: específicos de máquinas ou genéricos. Os critérios de trabalhadores não podem ser atribuídos a máquinas. Para atribuir critérios:

    i.  Clique no botão "Adicionar critérios".
    ii. Pesquise o critério a ser adicionado e selecione o mais adequado.
    iii. Selecione a data de início a partir da qual o critério se torna aplicável.
    iv. Selecione a data de fim para aplicar o critério ao recurso. Esta data é opcional se o critério for considerado indefinido.
    v.  Clique no botão "Salvar e continuar".

    .. figure:: images/machine-criterions.png
       :scale: 50

       Atribuição de Critérios a Máquinas

3.  **Calendário:** Esta aba permite aos usuários configurar um calendário específico para a máquina. Todas as máquinas têm um calendário padrão atribuído; no entanto, é possível atribuir um calendário específico a cada máquina com base em um calendário existente.

    .. figure:: images/machine-calendar.png
       :scale: 50

       Atribuição de Calendários a Máquinas

4.  **Configuração da Máquina:** Esta aba permite aos usuários configurar a relação entre máquinas e recursos de trabalhadores. Uma máquina tem um valor alfa que indica a relação máquina/trabalhador. Como mencionado anteriormente, um valor alfa de 0,5 indica que são necessárias 0,5 pessoas para cada dia completo de operação da máquina. Com base no valor alfa, o sistema atribui automaticamente trabalhadores associados à máquina assim que a máquina é atribuída a uma tarefa. Associar um trabalhador a uma máquina pode ser feito de duas formas:

    i.  **Atribuição Específica:** Atribuir um intervalo de datas durante o qual o trabalhador é atribuído à máquina. Esta é uma atribuição específica, pois o sistema atribui automaticamente horas ao trabalhador quando a máquina está agendada.
    ii. **Atribuição Genérica:** Atribuir critérios que devem ser cumpridos pelos trabalhadores atribuídos à máquina. Isso cria uma atribuição genérica de trabalhadores que cumprem os critérios.

    .. figure:: images/machine-configuration.png
       :scale: 50

       Configuração de Máquinas

5.  **Categoria de Custo:** Esta aba permite aos usuários configurar a categoria de custo que uma máquina cumpre durante um determinado período. Essa informação é utilizada para calcular os custos associados a uma máquina em um projeto.

    .. figure:: images/machine-costcategory.png
       :scale: 50

       Atribuição de Categorias de Custo a Máquinas

Grupos de Trabalhadores Virtuais
==================================

O programa permite aos usuários criar grupos de trabalhadores virtuais, que não são trabalhadores reais mas pessoal simulado. Esses grupos permitem aos usuários modelar o aumento da capacidade de produção em momentos específicos, com base nas configurações do calendário.

Os grupos de trabalhadores virtuais permitem aos usuários avaliar como o planejamento do projeto seria afetado pela contratação e atribuição de pessoal que cumpre critérios específicos, auxiliando assim o processo de tomada de decisão.

As abas para criar grupos de trabalhadores virtuais são as mesmas que para configurar trabalhadores:

*   Dados Gerais
*   Critérios Atribuídos
*   Calendários
*   Horas Associadas

A diferença entre grupos de trabalhadores virtuais e trabalhadores reais é que os grupos de trabalhadores virtuais têm um nome para o grupo e uma quantidade, que representa o número de pessoas reais no grupo. Existe também um campo para comentários, onde podem ser fornecidas informações adicionais, como qual projeto necessitaria de contratações equivalentes ao grupo de trabalhadores virtuais.

.. figure:: images/virtual-resources.png
   :scale: 50

   Recursos Virtuais

Recursos Baseados em Fila
==========================

Os recursos baseados em fila são um tipo específico de elemento produtivo que pode estar não atribuído ou ter 100% de dedicação. Em outras palavras, não podem ter mais de uma tarefa agendada ao mesmo tempo, nem podem ser superalocados.

Para cada recurso baseado em fila, uma fila é criada automaticamente. As tarefas agendadas para esses recursos podem ser gerenciadas especificamente utilizando os métodos de atribuição fornecidos, criando atribuições automáticas entre tarefas e filas que correspondem aos critérios necessários, ou movendo tarefas entre filas.
