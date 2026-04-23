Relatório do Total de Horas Trabalhadas por Recurso em um Mês
##############################################################

.. contents::

Finalidade
==========

Este relatório fornece o número total de horas trabalhadas por cada recurso em um determinado mês. Essas informações podem ser úteis para determinar as horas extras dos trabalhadores ou, dependendo da organização, o número de horas pelas quais cada recurso deve ser remunerado.

A aplicação registra relatórios de trabalho tanto para trabalhadores quanto para máquinas. Para as máquinas, o relatório totaliza o número de horas em que estiveram em operação durante o mês.

Parâmetros de Entrada e Filtros
================================

Para gerar este relatório, os usuários devem especificar o ano e o mês para os quais desejam obter o número total de horas trabalhadas por cada recurso.

Resultado
=========

O formato de saída é o seguinte:

Cabeçalho
---------

O cabeçalho do relatório exibe:

   *   O *ano* ao qual os dados do relatório se referem.
   *   O *mês* ao qual os dados do relatório se referem.

Rodapé
------

O rodapé exibe a data em que o relatório foi gerado.

Corpo
-----

A seção de dados do relatório é composta por uma única tabela com duas colunas:

   *   Uma coluna com o rótulo **Nome** para o nome do recurso.
   *   Uma coluna com o rótulo **Horas** com o número total de horas trabalhadas pelo recurso nessa linha.

Há uma linha final que agrega o número total de horas trabalhadas por todos os recursos durante o *mês* e *ano* especificados.
