Calendários
###########

.. contents::

Os calendários são entidades no programa que definem a capacidade de trabalho dos recursos. Um calendário consiste numa série de dias ao longo do ano, com cada dia dividido em horas de trabalho disponíveis.

Por exemplo, um feriado pode ter 0 horas de trabalho disponíveis. Por outro lado, um dia de trabalho típico pode ter 8 horas designadas como tempo de trabalho disponível.

Existem duas formas principais de definir o número de horas de trabalho num dia:

*   **Por Dia da Semana:** Este método define um número padrão de horas de trabalho para cada dia da semana. Por exemplo, as segundas-feiras podem normalmente ter 8 horas de trabalho.
*   **Por Excepção:** Este método permite desvios específicos em relação ao horário padrão por dia da semana. Por exemplo, a segunda-feira, 30 de Janeiro, pode ter 10 horas de trabalho, substituindo o horário padrão de segunda-feira.

Administração de Calendários
==============================

O sistema de calendários é hierárquico, permitindo criar calendários base e depois derivar novos calendários a partir deles, formando uma estrutura em árvore. Um calendário derivado de um calendário de nível superior herdará os seus horários diários e excepções, a menos que sejam explicitamente modificados. Para gerir eficazmente os calendários, é importante compreender os seguintes conceitos:

*   **Independência dos Dias:** Cada dia é tratado de forma independente, e cada ano tem o seu próprio conjunto de dias. Por exemplo, se 8 de Dezembro de 2009 é feriado, isso não significa automaticamente que 8 de Dezembro de 2010 também é feriado.
*   **Dias de Trabalho Baseados no Dia da Semana:** Os dias de trabalho padrão baseiam-se nos dias da semana. Por exemplo, se as segundas-feiras normalmente têm 8 horas de trabalho, então todas as segundas-feiras em todas as semanas de todos os anos terão 8 horas disponíveis, a menos que seja definida uma excepção.
*   **Excepções e Períodos de Excepção:** Pode definir excepções ou períodos de excepção para se desviar do horário padrão por dia da semana. Por exemplo, pode especificar um único dia ou um intervalo de dias com um número diferente de horas de trabalho disponíveis em relação à regra geral para esses dias da semana.

.. figure:: images/calendar-administration.png
   :scale: 50

   Administração de Calendários

A administração de calendários é acessível através do menu "Administração". A partir daí, os utilizadores podem efectuar as seguintes acções:

1.  Criar um novo calendário a partir do zero.
2.  Criar um calendário derivado de um existente.
3.  Criar um calendário como cópia de um existente.
4.  Editar um calendário existente.

Criar um Novo Calendário
-------------------------

Para criar um novo calendário, clique no botão "Criar". O sistema apresentará um formulário onde pode configurar o seguinte:

*   **Seleccionar o Separador:** Escolha o separador com que pretende trabalhar:

    *   **Marcar Excepções:** Definir excepções ao horário padrão.
    *   **Horas de Trabalho por Dia:** Definir as horas de trabalho padrão para cada dia da semana.

*   **Marcar Excepções:** Se seleccionar a opção "Marcar Excepções", pode:

    *   Seleccionar um dia específico no calendário.
    *   Seleccionar o tipo de excepção. Os tipos disponíveis são: férias, doença, greve, feriado e feriado útil.
    *   Seleccionar a data de fim do período de excepção. (Este campo não precisa de ser alterado para excepções de um único dia.)
    *   Definir o número de horas de trabalho durante os dias do período de excepção.
    *   Eliminar excepções previamente definidas.

*   **Horas de Trabalho por Dia:** Se seleccionar a opção "Horas de Trabalho por Dia", pode:

    *   Definir as horas de trabalho disponíveis para cada dia da semana (segunda-feira, terça-feira, quarta-feira, quinta-feira, sexta-feira, sábado e domingo).
    *   Definir diferentes distribuições de horas semanais para períodos futuros.
    *   Eliminar distribuições de horas previamente definidas.

Estas opções permitem aos utilizadores personalizar completamente os calendários de acordo com as suas necessidades específicas. Clique no botão "Guardar" para armazenar quaisquer alterações efectuadas ao formulário.

.. figure:: images/calendar-edition.png
   :scale: 50

   Edição de Calendários

.. figure:: images/calendar-exceptions.png
   :scale: 50

   Adicionar uma Excepção a um Calendário

Criar Calendários Derivados
----------------------------

Um calendário derivado é criado com base num calendário existente. Herda todas as características do calendário original, mas pode ser modificado para incluir opções diferentes.

Um caso de uso comum para calendários derivados é quando tem um calendário geral para um país, como a Espanha, e precisa de criar um calendário derivado para incluir feriados adicionais específicos de uma região, como a Galiza.

É importante notar que quaisquer alterações efectuadas ao calendário original propagar-se-ão automaticamente ao calendário derivado, a menos que uma excepção específica tenha sido definida no calendário derivado. Por exemplo, o calendário para a Espanha pode ter um dia de trabalho de 8 horas no dia 17 de Maio. No entanto, o calendário para a Galiza (um calendário derivado) pode não ter horas de trabalho nesse mesmo dia porque é um feriado regional. Se o calendário espanhol for posteriormente alterado para ter 4 horas de trabalho disponíveis por dia durante a semana de 17 de Maio, o calendário galego também mudará para ter 4 horas de trabalho disponíveis para cada dia dessa semana, excepto o dia 17 de Maio, que permanecerá como dia não útil devido à excepção definida.

.. figure:: images/calendar-create-derived.png
   :scale: 50

   Criar um Calendário Derivado

Para criar um calendário derivado:

*   Vá ao menu *Administração*.
*   Clique na opção *Administração de calendários*.
*   Seleccione o calendário que pretende usar como base para o calendário derivado e clique no botão "Criar".
*   O sistema apresentará um formulário de edição com as mesmas características do formulário utilizado para criar um calendário a partir do zero, excepto que as excepções propostas e as horas de trabalho por dia da semana serão baseadas no calendário original.

Criar um Calendário por Cópia
------------------------------

Um calendário copiado é uma duplicação exacta de um calendário existente. Herda todas as características do calendário original, mas pode ser modificado de forma independente.

A diferença principal entre um calendário copiado e um calendário derivado é a forma como são afectados pelas alterações ao original. Se o calendário original for modificado, o calendário copiado permanece inalterado. No entanto, os calendários derivados são afectados pelas alterações efectuadas ao original, a menos que seja definida uma excepção.

Um caso de uso comum para calendários copiados é quando tem um calendário para uma localização, como "Pontevedra", e precisa de um calendário semelhante para outra localização, como "A Coruña", onde a maioria das características são iguais. No entanto, as alterações a um calendário não devem afectar o outro.

Para criar um calendário copiado:

*   Vá ao menu *Administração*.
*   Clique na opção *Administração de calendários*.
*   Seleccione o calendário que pretende copiar e clique no botão "Criar".
*   O sistema apresentará um formulário de edição com as mesmas características do formulário utilizado para criar um calendário a partir do zero, excepto que as excepções propostas e as horas de trabalho por dia da semana serão baseadas no calendário original.

Calendário Predefinido
-----------------------

Um dos calendários existentes pode ser designado como calendário predefinido. Este calendário será automaticamente atribuído a qualquer entidade no sistema que seja gerida com calendários, a menos que seja especificado um calendário diferente.

Para configurar um calendário predefinido:

*   Vá ao menu *Administração*.
*   Clique na opção *Configuração*.
*   No campo *Calendário predefinido*, seleccione o calendário que pretende usar como calendário predefinido do programa.
*   Clique em *Guardar*.

.. figure:: images/default-calendar.png
   :scale: 50

   Definir um Calendário Predefinido

Atribuir um Calendário a Recursos
-----------------------------------

Os recursos só podem ser activados (ou seja, ter horas de trabalho disponíveis) se tiverem um calendário atribuído com um período de activação válido. Se não for atribuído nenhum calendário a um recurso, o calendário predefinido é atribuído automaticamente, com um período de activação que começa na data de início e não tem data de expiração.

.. figure:: images/resource-calendar.png
   :scale: 50

   Calendário de Recursos

No entanto, pode eliminar o calendário previamente atribuído a um recurso e criar um novo calendário baseado num existente. Isto permite uma personalização completa dos calendários para recursos individuais.

Para atribuir um calendário a um recurso:

*   Vá à opção *Editar recursos*.
*   Seleccione um recurso e clique em *Editar*.
*   Seleccione o separador "Calendário".
*   Serão apresentados o calendário, juntamente com as suas excepções, horas de trabalho por dia e períodos de activação.
*   Cada separador terá as seguintes opções:

    *   **Excepções:** Definir excepções e o período ao qual se aplicam, como férias, feriados ou dias de trabalho diferentes.
    *   **Semana de Trabalho:** Modificar as horas de trabalho para cada dia da semana (segunda-feira, terça-feira, etc.).
    *   **Períodos de Activação:** Criar novos períodos de activação para reflectir as datas de início e fim dos contratos associados ao recurso. Veja a imagem seguinte.

*   Clique em *Guardar* para armazenar as informações.
*   Clique em *Eliminar* se pretender alterar o calendário atribuído a um recurso.

.. figure:: images/new-resource-calendar.png
   :scale: 50

   Atribuir um Novo Calendário a um Recurso

Atribuir Calendários a Projetos
-----------------------------------

Os projectos podem ter um calendário diferente do calendário predefinido. Para alterar o calendário de uma projeto:

*   Aceda à lista de projetos na visão geral da empresa.
*   Edite a projeto em questão.
*   Aceda ao separador "Informação geral".
*   Seleccione o calendário a atribuir a partir do menu suspenso.
*   Clique em "Guardar" ou "Guardar e continuar".

Atribuir Calendários a Tarefas
--------------------------------

De forma semelhante a recursos e projetos, pode atribuir calendários específicos a tarefas individuais. Isto permite definir calendários diferentes para fases específicas de um projecto. Para atribuir um calendário a uma tarefa:

*   Aceda à vista de planeamento de um projecto.
*   Clique com o botão direito na tarefa à qual pretende atribuir um calendário.
*   Seleccione a opção "Atribuir calendário".
*   Seleccione o calendário a atribuir à tarefa.
*   Clique em *Aceitar*.
