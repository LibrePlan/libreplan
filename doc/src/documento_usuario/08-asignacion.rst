Asignación de recursos
######################

.. asigacion_
.. contents::

A asignación de recursos é unha das partes máis importantes da aplicación. A asignación de recursos pode realizarse de dous xeitos diferentes:
   * Asignacións específicas.
   * Asignacións xenéricas.

Cada unha das asignacións é explicada nas seguintes seccións

Asignación específica
---------------------

A asignación específica é aquela asignación de un recurso de xeito concreto e específico á unha tarefa de un proxecto, é dicir, o usuario da aplicación está decidindo qué "nome e apelidos" ou qué "máquina" concreta debe ser asignada a unha tarefa.

A asignación específica é realizable dende a pantalla que se pode ver na imaxe:


.. figure:: images/asignacion-especifica.png
   :scale: 70

A aplicación, cando un recurso é asignado específicamente, crea asignacións diarias en relación á porcentaxe de recurso diario que o usuario elixiu para asignación e contrastando co calendario dispoñible do recurso. Exemplo: unha asignación de 0.5 recursos  para unha tarefa de 32 horas fará que se asignen ó recurso específico (supoñendo un calendario laboral de 8 horas diarias) 4 horas diarias para realizar a tarefa.

Asignación xenérica
-------------------

A asignación xenérica é aquela asignación onde o usuario no elixe os recursos concretamente e deixa á decisión da aplicación como reparte as cargas entre os recursos dispoñibles da empresa.

.. figure:: images/asignacion-xenerica.png
   :scale: 70

O sistema de asignación utiliza como base os seguintes supostos:
   * As tarefas contarán con criterios a ser requeridos ós recursos.
   * Os recursos estarán configurados para que satisfagan os criterios.

Sen embargo, o sistema non fallará naqueles casos nos que non se asignen criterios senón que non discernirá entre diferencias de criterios.