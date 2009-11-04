Asignación de recursos
######################

.. asigacion_
.. contents::

A asignación de recursos é unha das partes máis importantes da aplicación. A asignación de recursos pode realizarse de dous xeitos diferentes:

* Asignacións específicas.
* Asignacións xenéricas.

Cada unha das asignacións é explicada nas seguintes seccións

Asignación específica
=====================

A asignación específica é aquela asignación de un recurso de xeito concreto e específico á unha tarefa de un proxecto, é dicir, o usuario da aplicación está decidindo qué "nome e apelidos" ou qué "máquina" concreta debe ser asignada a unha tarefa.

A asignación específica é realizable dende a pantalla que se pode ver na imaxe:


.. figure:: images/asignacion-especifica.png
   :scale: 70

A aplicación, cando un recurso é asignado específicamente, crea asignacións diarias en relación á porcentaxe de recurso diario que o usuario elixiu para asignación e contrastando co calendario dispoñible do recurso. Exemplo: unha asignación de 0.5 recursos  para unha tarefa de 32 horas fará que se asignen ó recurso específico (supoñendo un calendario laboral de 8 horas diarias) 4 horas diarias para realizar a tarefa.

Asignación específica de máquinas
---------------------------------

A asignación específica de máquinas actuará do mesmo xeito que a de traballadores, é dicir, cando se asigna unha máquina a unha tarefa, o sistema almacena unha asignación de horas específica á máquina elixida. A diferencia principal será que no momento de asignar unha máquina, o sistema buscará o listado de traballadores ou criterios asignados á máquina:

* Se a máquina tiña un listado de traballadores asignados, elixirá entre o número deles que requira a máquina para o calendario asignado. Por exemplo, se o calendario da máquina é de 16 horas diarias e dos recursos de 8, asignará dous recursos da lista de recursos dispoñibles.
* Se a máquina tiña un criterio ou varios asignados, realizará asignacións xenéricas entre os recursos que satisfán os criterios asignados á máquina.

Asignación xenérica
===================

A asignación xenérica é aquela asignación onde o usuario no elixe os recursos concretamente e deixa á decisión da aplicación como reparte as cargas entre os recursos dispoñibles da empresa.

.. figure:: images/asignacion-xenerica.png
   :scale: 70

O sistema de asignación utiliza como base os seguintes supostos:

* As tarefas contarán con criterios a ser requeridos ós recursos.
* Os recursos estarán configurados para que satisfagan os criterios.

Sen embargo, o sistema non fallará naqueles casos nos que non se asignen criterios senón que non discernirá entre diferencias de criterios.

O algoritmo de asignación xenérica actuará do seguinte xeito:

* Cada recurso e día será tratado como un contedor onde caben asignacións diarias de horas, baseándose a capacidade máxima de asignación no calendario da tarefa.
* O sistema busca os recursos que satisfán o criterio.
* O sistema analiza qué asignacións teñen actualmente os diferentes recursos que cumpren os criterios.
* De entre os que satisfán os criterios escóllense os recursos que teñen dispoñibilidade suficiente.
* Se os recursos máis libres van sendo ocupados, seguirase realizando asignacións nos recursos que tiñan menor dispoñibilidade.
* Só cando todos os recursos que satisfán os criterios correspondentes están asignados ó 100% se comeza coa sobreasignación de recursos ata completar o total necesario para realizar a tarefa.

Asignación xenérica de máquinas
-------------------------------

A asignación xenérica de máquinas actuará do mesmo xeito que a de traballadores, é dicir, cando se asigna unha máquina a unha tarefa, o sistema almacena unha asignación de horas xenérica a cada unha das máquinas que satisfán os criterios, tal e como se describíu xenéricamente para os recursos en xeral. Sen embargo, tratándose de máquinas o sistema realiza a seguinte operación am maiores:

* Para cada máquina elixida para a asignación xenérica:
  * Recolle a información de configuración da máquina, é dicir, alfa, traballadores e criterios asignados.
  * Se a máquina tiña un listado de traballadores asignados, elixirá entre o número deles que requira a máquina dependendo do calendario asignado. Por exemplo, se o calendario da máquina é de 16 horas diarias e dos recursos de 8, asignará dous recursos da lista de recursos dispoñibles.
  * Se a máquina tiña un criterio ou varios asignados, realizará asignacións xenéricas entre os recursos que satisfán os criterios asignados á máquina.
