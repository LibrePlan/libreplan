Exercicios Grupo 2 - Sesión 1
#############################

.. contents::


Exercicio  1. Criterios
=======================

Crear cada grupo o tipo de criterio cos criterios asociados  detallados:



      * Nome do tipo de criterio: Localización
      * Permite valores  simultáneos: Non.
      * Xerárquico: Sí.
      * Criterios:

         * España
         * España -> Galicia.
         * España ->  Galicia -> Ferrol.
         * España ->  Galicia -> Vigo.
         * España ->  Asturias -> Gijón.
         * España -> Pais  Vasco -> San Sebastian.
         * España ->  Andalucía -> Cádiz.

Exercicio 2. Calendarios
========================

Crear cada grupo un calendario global na aplicación cos seguintes datos:



      * Calendario Galicia media xornada.
      * Deriva de España.
      * Días excepcionais:

         * 17 de Maio - Tipo BankHoliday - Horas  laborais 0.
         * 24 de Decembro - Tipo Workable  BankHoliday - Horas laborais 4.
         * 31 de Decembro -  Tipo Workable BanHoliday - Horas laborais 4.

      *  Xornada semanal:

         * Luns - 9 h.
         * Martes - 9 h.
         * Mércores - 9 h.
         * Xoves - 9 h.
         * Venres - 4 h.

Exercicio 3. Etiquetas
======================

Crear os  tipos de etiqueta e etiquetas seguintes:


      * Tipo de  etiqueta: Centro de coste.
      * Valores:

         * Centro de custo 1.
         * Centro de custo 2.
         * Centro de custo 3.
         * Centro de custo 4.

Exercicio 4. Recursos
=====================

Crear os seguintes recursos cos datos seguintes:



      * Nome: Lois Amado  Montes. NIF:  33333333A Calendario derivado: Andalucía.
      * Nome: Raúl Cisneros Díaz. NIF:  44444444H Calendario derivado: Andalucía media  xornada.
      * Nome: Laura Menendez Gomez. NIF: 8976562L Calendario derivado: Andalucía.

Exercicio 5. Períodos Actividade
================================

Configurar os seguintes períodos de  actividade para os traballadores.



      * Nome: Lois Amado Montes.

         * Data contratación: 22/03/2010 - Indefinido.

      * Nome: Raúl  Cisneros Díaz.

         * Data contratación: 29/03/2010 - 29/09/2011

      * Nome: Laura Menendez Gomez.

         * Data contratación: 01/03/2010 - Indefinido.


Exercicio 6 - Configurar Excepcións de Calendario
=================================================

Configurar as vacacións como exception  days de intervalo e de tipo HOLIDAY.



      * Nome: Lois Amado Montes.

         * Vacacións: 07/06/2010 - 13/06/2010

      * Nome: Raúl Cisneros Díaz.

         * Vacacións: 14/06/2010 - 20/06/2010

Exercicio 7 -  Criterios en recursos
====================================

Configurar a satisfacción de criterios por parte dos recursos.



      * Nome: Lois Amado Montes.
      * Satisfaccións de criterio:

         * Grupo: Grupo 2 - Dende 01/04/2010 ata infinito.
         * Tipo de traballo: Pintor - Dende 01/04/2010 ata infinito.

       * Nome: Raúl Cisneros Díaz.
       * Satisfaccións de criterio:

         * Grupo: Grupo 2  - Dende 01/04/2010 ata infinito.
         * Tipo de  traballo: Califugador - Dende 01/04/2010 ata infinito.

       * Nome: Laura Menendez Gomez
       * Satisfaccións de criterio:
          * Grupo: Grupo 2 - Dende 01/03/2010 ata infinito.

Exercicio  8. Creación dun pedido
=================================

Crear un pedido cada grupo cos seguintes datos e poñerlle, os puntos de planificación e os criterios indicados:



      * Datos de pedido:

         * Nome:  Pedido Grupo 2.
         * Data inicio: 08/03/2010
         * Data  limite: 01/06/2011.
         * Cliente: Barreras.
         * Responsable: Nome da persoa do grupo.
         * Presuposto: Traballo:  100.000  Materiais: 0
         * Calendario: Galicia.
         * Estado:  Ofertado.

      *  Elementos de pedido:

         * 1  Coordinacion - **Punto de planificación** - Criterio Grupo 2

            * 1.1 Reunións con cliente - 100h
            * 1.2 Reunións con traballadores - 100h

         * 2  Bloque 1 - Criterio Grupo 2

            * 2.1 Pintar camarotes A e B- 350 h  - **Punto de planificación** - Criterio Pintor
            * 2.2 Pintar sala de máquinas - 200 h - **Punto de planificación** - Criterio Pintor
            * 2.3 Pintas cociña de buque - 100 h - **Punto de planificación** - Criterio Pintor

         * 3 Bloque  2 - Grupo 2

            * 3.1 Illar camarote A - 300 h - **Punto de planificación** - Criterio Califugador
            * 3.2 Illar camarote B - 250 h - **Punto de planificación** - Criterio Califugador
            * 3.3 Illar camarote C - 200 h - **Punto de planificación** - Criterio Califugador

Exercicio  9 - Planificando dependencias
========================================

Poñer as dependencias seguintes na planificación de cada pedido:



Poñer as seguintes dependencias:

         * Bloque 1 FIN-INICIO Bloque 2
         * Pintar camarotes A e B FIN-INICIO Pintar sá de máquinas
         * Pintar sá de máquinas FIN-INICIO Pintar cociñas de buque.
         * Illar camarote A INICIO-INICIO Illar camarote B
         * Illar camarote A FIN-INICIO Illar camarote C
         * Crear un fito chamado Recepción de material que sexa o 14/10/2010
         * Fito recepción de material FIN-INICIO Illar camarote C

Exercicio 10. Asignación de recursos
====================================

Realizar as seguintes asignacións



      * Tarefa:  Coordinación:

         * Asignación específica: Laura Menendez Gomez
         * Estratexia: - Calcula data fin
         * Numero  de recursos por dia: 0.6

      * Tarefa: Pintar camarotes A e B

         * Asignación xenérica
         * Estratexia recomendada
         * Numero  de recursos por dia: 1

      * Tarefa: Pintar sá de máquinas

         * Asignación xenérica
         * Estratexia recomendada
         * Número  de recursos por dia: 1

      * Tarefa: Pintar cociñas de buque

         * Asignación xenérica
         * Estratexia recomendada
         * Número  de recursos por dia: 1

      * Tarefa: Illar camarote A

         * Asignación xenérica con criterios [Grupo 2, Califugador]
         * Estratexia: Calcular recursos por dia.
         * Data de fin: 15 Outubro 2010
         * Horas:  300 horas.

      * Tarefa: Illar camarote B

         * Asignación xenérica con criterios [Grupo 2, Califugador]
         * Estratexia: Calcular número de horas
         * Número de  recursos por dia: 0.5
         * Data de fin: 1 de Agosto 2010

      * Tarefa:  Illar camarote

         * Asignación xenérica con criterios [Grupo  2, Califugador]
         * Estratexia: Calcular data fin
         * Recursos por dia: 0.5
         * Horas: 200

Exercicio 11. Avances
======================

Realizar as seguintes asignacións de avance



      *  Elemento de pedido - Coordinación - Avance de tipo porcentaxe - Valor   máximo 100 - Propaga

         * Valores: 25% a 15 Marzo de 2010.

      * Elemento  de pedido - Pintar camarotes A e B - Avance de tipo unidades - Valor  máximo 5 - Propaga

         * Valores: 1 unidade ao 2 de Marzo de 2010
         *  Valores: 2 unidades ao 30 de Marzo de 2010

      * Elemento de pedido -  Pintar sa de maquinas - Avance de tipo unidades - Valor máximo 10 -  Propaga

         * Valores:  3 unidades ao 2 de Abril de  2010.

      * Elemento de pedido - Pintar cociñas buque - Avance de tipo unidades - Valor máximo 15 - Propaga

         *  Valores: 5 unidades a 31 de Marzo de 2010.

      * Elemento de pedido -  Bloque 2 - Avance de tipo porcentaxe - Valor máximo 100 - Propaga

         *  Valores: 5 a 16 de Marzo de 2010.

      * Configurar a nivel de pedido  que o  avance  de tipo children é o que propaga.

