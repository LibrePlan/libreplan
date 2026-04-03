Gestão de Custos
#################

.. _costes:
.. contents::

Custos
=======

A gestão de custos permite aos usuários estimar os custos dos recursos utilizados em um projeto. Para gerenciar custos, as seguintes entidades devem ser definidas:

*   **Tipos de Horas:** Indicam os tipos de horas trabalhadas por um recurso. Os usuários podem definir tipos de horas tanto para máquinas como para trabalhadores. Exemplos de tipos de horas incluem: "Horas adicionais pagas a R$20 por hora." Os seguintes campos podem ser definidos para tipos de horas:

    *   **Código:** Código externo para o tipo de horas.
    *   **Nome:** Nome do tipo de horas. Por exemplo, "Adicional".
    *   **Taxa Padrão:** Taxa padrão básica para o tipo de horas.
    *   **Ativação:** Indica se o tipo de horas está ativo ou não.

*   **Categorias de Custo:** As categorias de custo definem os custos associados a diferentes tipos de horas durante períodos específicos (que podem ser indefinidos). Por exemplo, o custo das horas adicionais para trabalhadores qualificados de primeiro grau no ano seguinte é de R$24 por hora. As categorias de custo incluem:

    *   **Nome:** Nome da categoria de custo.
    *   **Ativação:** Indica se a categoria está ativa ou não.
    *   **Lista de Tipos de Horas:** Esta lista define os tipos de horas incluídos na categoria de custo. Especifica os períodos e as taxas para cada tipo de horas. Por exemplo, à medida que as taxas mudam, cada ano pode ser incluído nesta lista como um período de tipo de horas, com uma taxa horária específica para cada tipo de horas (que pode diferir da taxa horária padrão para esse tipo de horas).

Gerenciamento de Tipos de Horas
--------------------------------

Os usuários devem seguir estas etapas para registrar tipos de horas:

*   Selecione "Gerenciar tipos de horas trabalhadas" no menu "Administração".
*   O programa exibe uma lista dos tipos de horas existentes.

.. figure:: images/hour-type-list.png
   :scale: 35

   Lista de Tipos de Horas

*   Clique em "Editar" ou "Criar".
*   O programa exibe um formulário de edição de tipos de horas.

.. figure:: images/hour-type-edit.png
   :scale: 50

   Edição de Tipos de Horas

*   Os usuários podem inserir ou alterar:

    *   O nome do tipo de horas.
    *   O código do tipo de horas.
    *   A taxa padrão.
    *   Ativação/desativação do tipo de horas.

*   Clique em "Salvar" ou "Salvar e continuar".

Categorias de Custo
--------------------

Os usuários devem seguir estas etapas para registrar categorias de custo:

*   Selecione "Gerenciar categorias de custo" no menu "Administração".
*   O programa exibe uma lista das categorias existentes.

.. figure:: images/category-cost-list.png
   :scale: 50

   Lista de Categorias de Custo

*   Clique no botão "Editar" ou "Criar".
*   O programa exibe um formulário de edição de categorias de custo.

.. figure:: images/category-cost-edit.png
   :scale: 50

   Edição de Categorias de Custo

*   Os usuários inserem ou alteram:

    *   O nome da categoria de custo.
    *   A ativação/desativação da categoria de custo.
    *   A lista de tipos de horas incluídos na categoria. Todos os tipos de horas têm os seguintes campos:

        *   **Tipo de Horas:** Escolha um dos tipos de horas existentes no sistema. Se não existir nenhum, um tipo de horas deve ser criado (este processo é explicado na subseção anterior).
        *   **Data de Início e Término:** As datas de início e término (esta última é opcional) para o período que se aplica à categoria de custo.
        *   **Taxa Horária:** A taxa horária para esta categoria específica.

*   Clique em "Salvar" ou "Salvar e continuar".

A atribuição de categorias de custo a recursos está descrita no capítulo sobre recursos. Acesse a seção "Recursos".
