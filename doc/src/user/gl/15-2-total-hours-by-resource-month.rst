Informe de horas totais traballadas por recurso nun mes
#######################################################

.. contents::

Propósito
=========

Este informe permite obter o número total de horas traballadas polos recursos nun mes. Isto pode ser útil para calcular as horas extras feitas ou, dependendiendo da organización, a cantidade de horas que ten que ser pagadas por recurso nun mes.

A aplicación permite rexistrar partes de traballo tanto para os traballadores como para as máquinas. E, de acordo con isto, o informe no caso das máquinas indica as horas totais que as máquinas estiveron funcionando nun determinado mes.

Parámetros de entrada e filtro
==============================

No informe debe ser especificado o ano e o mes para obter o total de horas por recurso que traballaron.

Saída
=====

O formato de saída do informe é o seguinte:

Cabeceira
---------

Na cabeceira do informe móstrase;

   * O *ano* ao que pertence o informe que se está extraendo.
   * O *mes* ao cal pertene os datos do informe mostrado.

Pé de páxina
------------

No pé de páxina móstrase a data na que cúal o informe sacouse.

Corpo
-----

A área de datos do informe consiste nunha única sección na que se inclúe unha tabal con dúas columnas:

   * Unha columna denominada **Nome** para o nome do recurso.
   * Unha columna chamada **Horas** coa suma de todas as horas dedicadas polo recurso ao que corresponde unha fila.

Hai unha final total agregadora do total de horas dedicadas  por calquera dos recursos no *mes*, *anos* ao que corresponde o informe.

