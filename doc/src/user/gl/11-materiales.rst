Materiais
#########
.. _materiales:
.. contents::


Administración de materiais
===========================

É posible xestionar unha base de datos de materiais básica organizados por categorías.

As categorías son contedores ós que se poden asignar materiais concretos e ó mesmo tempo máis categorías. Almacénanse en modo arbóreo de xeito que os materiais poden pertencer a categorías folla ou categorías intermedias.

Para administrar categorías:

* O usuario accede á operación de "Administración->Materiais".
* A aplicación amosa unha árbore de categorías.
* O usuario introduce un nome de categoría dentro da entrada de texto con un botón "Engadir" e preme no botón.
* A aplicación engade a categoría na árbore de categorías.

Se o usuario desexa posicionar unha categoría dentro da árbore de categorías debe seleccionar previamente a categoría pai en dita árbore para despois premer en "Engadir".

.. figure:: images/material.png
   :scale: 50

   Pantalla de administración de materiais

Para administrar materiais:

* O usuario selecciona a categoría para a que desexa incluír materiais e preme no botón "Engadir" na zona dereita de "Materiais".
* A aplicación engade unha nova fila baleira con campos para introducir os datos do material:

   * Código: Código do tipo de material (pode ser o código externo provinte dun ERP).
   * Descrición: Descrición do material.
   * Prezo da unidade: Prezo unitario de cada elemento de material.
   * Unidade: Unidade na que se desexa medir cada unidade de material.
   * Categoría: Categoría á que pertence.
   * Deshabilitado: Se o material está borrado ou non.

* O usuario enche os campos e preme no botón "Gardar".

A asignación de materiais a elementos de pedidos explícase no capítulo de "Pedidos".

