-------
Grupo 1
-------

.. contents::


Exercicio  1. Criterios
=======================

Crear cada grupo o tipo de criterio cos criterios asociados  detallados:



      * Nome do tipo de criterio: Tipo de traballo.
      * Permite valores  simultáneos: Sí.
      * Xerárquico: Non.
      * Criterios:

         * Soldador.
         * Carpinteiro.
         * Pintor.
         * Califugador.
         * Soldador submarino.
         * Electricista.

Exercicio 2. Calendarios
========================

Crear cada grupo un calendario global na aplicación cos seguintes datos:



      * Calendario  Galicia xornada completa.
      * Deriva de España.
      * Días excepcionais:

         * 22 de Abril - Tipo BankHoliday - Horas laborais 0.
         * 17 de Maio - Tipo BankHoliday - Horas laborais 0.
         * 30 de Decembro - Tipo Workable BanHoliday - Horas laborais 4.

      * Xornada semanal:

         * Luns - 9 h.
         * Martes - 9 h.
         * Mércores - 9 h.
         * Xoves - 9 h.
         * Venres - 4 h.

Exercicio 3. Etiquetas
======================

Crear os  tipos de etiqueta e etiquetas seguintes:



      * Tipo de etiqueta: Prioridade.
      * Valores:

         * Moi urxente.
         * Urxente.
         * Media.
         * Baixa.

Exercicio 4. Recursos
=====================

Crear os seguintes recursos cos datos seguintes:



      * Nome: Javier Martínez Alvarez. NIF: 11111111F Calendario derivado: Galicia.
      * Nome: María Pérez  Mariño. NIF: 22222222D Calendario derivado: Galicia media xornada.
      * Nome: Javier Pérez Campos NIF: 5656565L Calendario derivado: Galicia.

Exercicio 5. Períodos Actividade
================================

Configurar os seguintes períodos de  actividade para os traballadores.



      * Nome: Javier Martínez Alvarez.

         * Data contratación: 08/02/2011 - Indefinido.

      * Nome: María Pérez Mariño.

         * Data contratación:  15/02/2011 - 15/09/2012.

     * Nome: Javier Pérez Campos

        * Data contratación: 01/02/2011 - Indefinido.

Exercicio 6 - Configurar Excepcións de Calendario
=================================================

Configurar as vacacións como exception  days de intervalo e de tipo HOLIDAY.



      * Nome: Javier Martínez Alvarez.

         * Vacacións: 05/02/2011 - 11/02/2011.

      * Nome: María Pérez Mariño.

         * Vacacións:  03/02/2011 - 09/02/2011.

Exercicio 7 -  Criterios en recursos
====================================

Configurar a satisfacción de criterios por parte dos recursos.



      * Nome: Javier Martínez Álvarez.
      * Satisfaccións de criterio:

         * Grupo: Grupo1 - Dende 01/03/2011 ata infinito.
         * Tipo de traballo: Soldador - Dende 01/03/2011 ata infinito.

      * Nome: María Pérez Mariño.
      * Satisfaccións de criterio:

         * Grupo: Grupo 1 - Dende 01/03/2011 ata infinito.
         * Tipo de traballo: Carpinteiro - Dende 01/03/2011 ata infinito.

      * Nome: Javier Pérez Campos.
      * Satisfaccións de criterio:

         * Grupo: Grupo 1 - Dende data actual ata infinito.

Exercicio  8. Creación dun pedido
=================================

Crear un pedido cada grupo cos seguintes datos e poñerlle, os puntos de planificación e os criterios indicados:



   * Datos de pedido:

      * Nome: Pedido Grupo 1.
      * Data inicio: 01/02/2011
      * Data limite: 01/06/2011.
      * Cliente: -
      * Calendario: Galicia.

   * Elementos de pedido:

      * 1  Coordinacion - *Punto de planificación* - Grupo 1

         * 1.1 Reunións con  cliente - 100h
         * 1.2  Reunións con traballadores - 100h

      * 2  Bloque 1  - Grupo 1/Soldador

         * 2.1  Soldar  unions do teito - 200 h  *Punto de planificación*
         * 2.2 Soldar  unions do  chan - 200 h *Punto de planificación*
         * 2.3 Repasar  soldaduras ocos - 100 h *Punto de planificación*

      * 3 Bloque  2 - Grupo 1/Carpinteiro

         * 3.1 Teito de  madeira de camarote A - 300 h *Punto de planificación*
         * 3.2 Cama e  mesilla de camarote A - 250 h *Punto de planificación*
         * 3.3 Poñer  escotillas  camarote A - 200 h *Punto de planificación*

   * Outros datos do pedido:
      * Responsable: Nome da persoa do grupo.
      * Presuposto: Traballo: 100.000  Materiais: 0
      * Estado: Ofertado.

Exercicio  9 - Planificando dependencias
========================================

Poñer as dependencias seguintes na planificación de cada pedido:

Poñer as seguintes dependencias:

         * Bloque 1 FIN-INICIO Bloque 2
         * Soldar unions do teito FIN-INICIO Soldar unions do chan
         * Soldar unions do chan FIN-INICIO Repasar soldaduras ocos
         * Teito de madeira de camarote INICIO-INICIO Cama e mesillas camartoe
         * Cama e mesillas camarote A FIN-INICIO Poñer escotillas camarote
         * Crear un fito chamado Entrega proxecto
         * Fito Escotillas camarote FIN-INICIO Recepción material

Exercicio 10. Asignación de recursos
====================================

Realizar as seguintes asignacións



      * Tarefa: Coordinación:

         * Asignación específica: Javier Pérez Campos
         * Estratexia: - Calcular días laborables
         * Numero de recursos por dia: 0.6

      * Tarefa: Soldar unions do teito

         * Asignación xenérica
         * Estratexia recomendada
         * Numero de recursos por dia: 1

      * Tarefa: Soldar unions do chan

         * Asignación xenérica
         * Estratexia recomendada
         * Numero de recursos por dia: 1

      * Tarefa: Repasar soldaduras ocos:

         * Asignación xenérica
         * Estratexia recomendada
         * Numero de recursos por dia: 1

      * Tarefa: Teito madeira camarote A.

         * Asignación xenérica con criterios [Grupo 1, Carpinteiro]
         * Estratexia: Calcular recursos por dia.
         * Duración: 21 días.
         * Horas: 300 horas.

      * Tarefa: Cama e mesilla camarote A.

         * Asignación xenérica con criterios [Grupo 1, Carpinteiro]
         * Estratexia: Calcular numero de horas
         * Numero de recursos por dia: 1
         * Duración: 20 días.
         * Horas: 250 horas.

      * Tarefa: Escotillas camarote

         * Asignación xenérica con criterios [Grupo 1, Carpinteiro]
         * Estratexia: Calcular días laborables
         * Recursos por dia: 0.5
         * Horas: 200

Exercicio 11. Avances
======================

Realizar as seguintes asignacións de avance



      * Elemento de pedido - Coordinación - Avance de tipo porcentaxe - Valor  máximo 100 - Propaga

         * Valores: 25% a 15 Marzo de 2011.

      * Elemento de pedido - Soldar unions no teito - Avance de tipo unidades - Valor máximo 5 - Propaga

         * Valores: 1 unidade ao 2 de Marzo de 2011
         * Valores: 2 unidades ao 30 de Marzo de 2011

      * Elemento de pedido - Soldar unions do chan - Avance de tipo unidades - Valor máximo 10 - Propaga

         * Valores:  3 unidades ao 2 de Abril de 2011.

      * Elemento de pedido - Repasar soldadoras ocos - Avance de tipo unidades - Valor máximo 15 - Propaga

         * Valores: 5 unidades a 31 de Marzo de 2011.

      * Elemento de pedido - Cama e mesilla de camarote A - Avance de tipo porcentaxe - Valor máximo 100 - Propaga

         * Valores: 25 a 16 de Marzo de 2011.

      * Configurar a nivel de pedido 1 que o  avance de tipo children é o que propaga.

