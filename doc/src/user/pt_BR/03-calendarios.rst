Calendários
###########

.. contents::

Os calendários são entidades no programa que definem a capacidade de trabalho dos recursos. Um calendário consiste em uma série de dias ao longo do ano, com cada dia dividido em horas de trabalho disponíveis.

Por exemplo, um feriado pode ter 0 horas de trabalho disponíveis. Por outro lado, um dia de trabalho típico pode ter 8 horas designadas como tempo de trabalho disponível.

Existem duas formas principais de definir o número de horas de trabalho em um dia:

*   **Por Dia da Semana:** Este método define um número padrão de horas de trabalho para cada dia da semana. Por exemplo, as segundas-feiras podem normalmente ter 8 horas de trabalho.
*   **Por Exceção:** Este método permite desvios específicos em relação ao horário padrão por dia da semana. Por exemplo, a segunda-feira, 30 de janeiro, pode ter 10 horas de trabalho, substituindo o horário padrão de segunda-feira.

Administração de Calendários
==============================

O sistema de calendários é hierárquico, permitindo criar calendários base e depois derivar novos calendários a partir deles, formando uma estrutura em árvore. Um calendário derivado de um calendário de nível superior herdará seus horários diários e exceções, a menos que sejam explicitamente modificados. Para gerenciar efetivamente os calendários, é importante compreender os seguintes conceitos:

*   **Independência dos Dias:** Cada dia é tratado de forma independente, e cada ano tem seu próprio conjunto de dias. Por exemplo, se 8 de dezembro de 2009 é feriado, isso não significa automaticamente que 8 de dezembro de 2010 também é feriado.
*   **Dias de Trabalho Baseados no Dia da Semana:** Os dias de trabalho padrão são baseados nos dias da semana. Por exemplo, se as segundas-feiras normalmente têm 8 horas de trabalho, então todas as segundas-feiras em todas as semanas de todos os anos terão 8 horas disponíveis, a menos que uma exceção seja definida.
*   **Exceções e Períodos de Exceção:** Você pode definir exceções ou períodos de exceção para se desviar do horário padrão por dia da semana. Por exemplo, pode-se especificar um único dia ou um intervalo de dias com um número diferente de horas de trabalho disponíveis em relação à regra geral para esses dias da semana.

.. figure:: images/calendar-administration.png
   :scale: 50

   Administração de Calendários

A administração de calendários é acessível através do menu "Administração". A partir daí, os usuários podem realizar as seguintes ações:

1.  Criar um novo calendário do zero.
2.  Criar um calendário derivado de um existente.
3.  Criar um calendário como cópia de um existente.
4.  Editar um calendário existente.

Criar um Novo Calendário
-------------------------

Para criar um novo calendário, clique no botão "Criar". O sistema exibirá um formulário onde você pode configurar o seguinte:

*   **Selecionar a Aba:** Escolha a aba com que deseja trabalhar:

    *   **Marcar Exceções:** Definir exceções ao horário padrão.
    *   **Horas de Trabalho por Dia:** Definir as horas de trabalho padrão para cada dia da semana.

*   **Marcar Exceções:** Se você selecionar a opção "Marcar Exceções", pode:

    *   Selecionar um dia específico no calendário.
    *   Selecionar o tipo de exceção. Os tipos disponíveis são: férias, doença, greve, feriado e feriado útil.
    *   Selecionar a data de fim do período de exceção. (Este campo não precisa ser alterado para exceções de um único dia.)
    *   Definir o número de horas de trabalho durante os dias do período de exceção.
    *   Excluir exceções previamente definidas.

*   **Horas de Trabalho por Dia:** Se você selecionar a opção "Horas de Trabalho por Dia", pode:

    *   Definir as horas de trabalho disponíveis para cada dia da semana (segunda-feira, terça-feira, quarta-feira, quinta-feira, sexta-feira, sábado e domingo).
    *   Definir diferentes distribuições de horas semanais para períodos futuros.
    *   Excluir distribuições de horas previamente definidas.

Essas opções permitem aos usuários personalizar completamente os calendários de acordo com suas necessidades específicas. Clique no botão "Salvar" para armazenar quaisquer alterações feitas no formulário.

.. figure:: images/calendar-edition.png
   :scale: 50

   Edição de Calendários

.. figure:: images/calendar-exceptions.png
   :scale: 50

   Adicionando uma Exceção a um Calendário

Criar Calendários Derivados
----------------------------

Um calendário derivado é criado com base em um calendário existente. Ele herda todas as características do calendário original, mas pode ser modificado para incluir opções diferentes.

Um caso de uso comum para calendários derivados é quando você tem um calendário geral para um país, como a Espanha, e precisa criar um calendário derivado para incluir feriados adicionais específicos de uma região, como a Galícia.

É importante notar que quaisquer alterações feitas no calendário original se propagarão automaticamente ao calendário derivado, a menos que uma exceção específica tenha sido definida no calendário derivado. Por exemplo, o calendário para a Espanha pode ter um dia de trabalho de 8 horas no dia 17 de maio. No entanto, o calendário para a Galícia (um calendário derivado) pode não ter horas de trabalho nesse mesmo dia porque é um feriado regional. Se o calendário espanhol for posteriormente alterado para ter 4 horas de trabalho disponíveis por dia durante a semana de 17 de maio, o calendário galego também mudará para ter 4 horas de trabalho disponíveis para cada dia dessa semana, exceto o dia 17 de maio, que permanecerá como dia não útil devido à exceção definida.

.. figure:: images/calendar-create-derived.png
   :scale: 50

   Criando um Calendário Derivado

Para criar um calendário derivado:

*   Vá ao menu *Administração*.
*   Clique na opção *Administração de calendários*.
*   Selecione o calendário que deseja usar como base para o calendário derivado e clique no botão "Criar".
*   O sistema exibirá um formulário de edição com as mesmas características do formulário utilizado para criar um calendário do zero, exceto que as exceções propostas e as horas de trabalho por dia da semana serão baseadas no calendário original.

Criar um Calendário por Cópia
------------------------------

Um calendário copiado é uma duplicação exata de um calendário existente. Ele herda todas as características do calendário original, mas pode ser modificado de forma independente.

A diferença principal entre um calendário copiado e um calendário derivado é a forma como são afetados pelas alterações no original. Se o calendário original for modificado, o calendário copiado permanece inalterado. No entanto, os calendários derivados são afetados pelas alterações feitas no original, a menos que uma exceção seja definida.

Um caso de uso comum para calendários copiados é quando você tem um calendário para uma localidade, como "Pontevedra", e precisa de um calendário semelhante para outra localidade, como "A Coruña", onde a maioria das características são iguais. No entanto, as alterações em um calendário não devem afetar o outro.

Para criar um calendário copiado:

*   Vá ao menu *Administração*.
*   Clique na opção *Administração de calendários*.
*   Selecione o calendário que deseja copiar e clique no botão "Criar".
*   O sistema exibirá um formulário de edição com as mesmas características do formulário utilizado para criar um calendário do zero, exceto que as exceções propostas e as horas de trabalho por dia da semana serão baseadas no calendário original.

Calendário Padrão
------------------

Um dos calendários existentes pode ser designado como calendário padrão. Este calendário será automaticamente atribuído a qualquer entidade no sistema que seja gerenciada com calendários, a menos que um calendário diferente seja especificado.

Para configurar um calendário padrão:

*   Vá ao menu *Administração*.
*   Clique na opção *Configuração*.
*   No campo *Calendário padrão*, selecione o calendário que deseja usar como calendário padrão do programa.
*   Clique em *Salvar*.

.. figure:: images/default-calendar.png
   :scale: 50

   Definindo um Calendário Padrão

Atribuir um Calendário a Recursos
-----------------------------------

Os recursos só podem ser ativados (ou seja, ter horas de trabalho disponíveis) se tiverem um calendário atribuído com um período de ativação válido. Se nenhum calendário for atribuído a um recurso, o calendário padrão é atribuído automaticamente, com um período de ativação que começa na data de início e não tem data de expiração.

.. figure:: images/resource-calendar.png
   :scale: 50

   Calendário de Recursos

No entanto, você pode excluir o calendário previamente atribuído a um recurso e criar um novo calendário baseado em um existente. Isso permite uma personalização completa dos calendários para recursos individuais.

Para atribuir um calendário a um recurso:

*   Vá à opção *Editar recursos*.
*   Selecione um recurso e clique em *Editar*.
*   Selecione a aba "Calendário".
*   Serão exibidos o calendário, juntamente com suas exceções, horas de trabalho por dia e períodos de ativação.
*   Cada aba terá as seguintes opções:

    *   **Exceções:** Definir exceções e o período ao qual se aplicam, como férias, feriados ou dias de trabalho diferentes.
    *   **Semana de Trabalho:** Modificar as horas de trabalho para cada dia da semana (segunda-feira, terça-feira, etc.).
    *   **Períodos de Ativação:** Criar novos períodos de ativação para refletir as datas de início e fim dos contratos associados ao recurso. Veja a imagem a seguir.

*   Clique em *Salvar* para armazenar as informações.
*   Clique em *Excluir* se quiser alterar o calendário atribuído a um recurso.

.. figure:: images/new-resource-calendar.png
   :scale: 50

   Atribuindo um Novo Calendário a um Recurso

Atribuir Calendários a Projetos
--------------------------------

Os projetos podem ter um calendário diferente do calendário padrão. Para alterar o calendário de um projeto:

*   Acesse a lista de projetos na visão geral da empresa.
*   Edite o projeto em questão.
*   Acesse a aba "Informações gerais".
*   Selecione o calendário a ser atribuído no menu suspenso.
*   Clique em "Salvar" ou "Salvar e continuar".

Atribuir Calendários a Tarefas
--------------------------------

De forma semelhante a recursos e projetos, você pode atribuir calendários específicos a tarefas individuais. Isso permite definir calendários diferentes para fases específicas de um projeto. Para atribuir um calendário a uma tarefa:

*   Acesse a visualização de planejamento de um projeto.
*   Clique com o botão direito na tarefa à qual deseja atribuir um calendário.
*   Selecione a opção "Atribuir calendário".
*   Selecione o calendário a ser atribuído à tarefa.
*   Clique em *Aceitar*.
