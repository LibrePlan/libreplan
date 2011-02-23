-------
Grupo 4
-------

.. contents::

Exercicio  1. Criterios
=======================

Crear cada grupo o tipo de criterio cos criterios asociados  detallados:



      * Nome do tipo de criterio: Contrato laboral.
      * Permite valores simultáneos: Non.
      * Xerárquico: Non.
      * Criterios:

         * Indefinido.
         * En prácticas primeiro ano.
         * En prácticas  segundo ano.
         * Por obra.
         * Temporal

Exercicio 2. Calendarios
========================

Crear cada grupo un calendario global na aplicación cos seguintes datos:



      * Calendario  Andalucía media xornada.
      * Deriva de España.
      * Días excepcionais:

         * 1 de Marzo - Tipo BankHoliday - Horas laborais 0.
         * 22 de Abril - Tipo BankHoliday - Horas laborais 0.
         * 30 de Decembro  - Tipo Workable BanHoliday - Horas laborais 4.

      * Xornada semanal:

         * Luns - 4 h.
         * Martes - 4 h.
         * Mércores - 4 h.
         * Xoves - 4 h.
         * Venres - 4 h.

Exercicio 3. Etiquetas
======================

Crear os  tipos de etiqueta e etiquetas seguintes:


      * Tipo de  etiqueta: Nivel de Risco do traballo.
      * Valores:

         * Risco alto.
         * Risco medio.
         * Risco baixo.
         * Sen risco.

Exercicio 4. Recursos
=====================

Crear os seguintes recursos cos datos seguintes:



      * Nome: Félix  González López. NIF: 77777777G Calendario derviado: Galicia media xornada.
      * Nome: Óscar Iraola Sáez. NIF: 88888888B Calendario derivado: Andalucía.
      * Nome: Elena Boluda Ferrer NIF: 99999999B Calendario derivado: Galicia media xornada.

Exercicio 5. Períodos Actividade
================================

Configurar os seguintes períodos de  actividade para os traballadores.



      * Nome: Félix González López.

         * Data contratación: 22/02/2011 - Indefinido.

      * Nome: Óscar Iraola Sáez.

         * Data contratación:  28/02/2011 - 29/09/2011

      * Nome: Elena Boluda Ferrer.

         * Data contratación: 01/02/2011 - Indefinido


Exercicio 6 - Configurar Excepcións de Calendario
=================================================

Configurar as vacacións como exception  days de intervalo e de tipo HOLIDAY.



      * Nome: Félix González López.

         * Vacacións: 05/07/2011 - 11/07/2011

      * Nome: Óscar Iraola Sáez.

         * Vacacións: 12/07/2011 - 18/07/2011

Exercicio 7 -  Criterios en recursos
====================================

Configurar a satisfacción de criterios por parte dos recursos.



      * Nome: Félix González López.
      * Satisfaccións de criterio:

         * Grupo: Grupo4 - Dende 01/03/2011 ata infinito.
         * Tipo de traballo: Soldador - Dende 01/03/2011 ata infinito.

      * Nome: Óscar Iraola Sáez.
      * Satisfaccións  de criterio:

         * Grupo: Grupo 4 -  Dende 01/03/2011 ata infinito.
         * Tipo de  traballo: Carpinteiro - Dende 01/03/2011 ata infinito.

      * Nome: Elena Boluda Ferrer.
      * Satisfaccións de criterio:

         * Grupo: Grupo 4 - Dende data actual ata infinito.

Exercicio  8. Creación dun pedido
=================================

Crear un pedido cada grupo cos seguintes datos e poñerlle, os puntos de planificación e os criterios indicados:



      * Datos de pedido:

         * Nome:  Pedido Grupo 4.
         * Data inicio: 01/02/2011
         * Data  limite:   01/06/2011.
         * Cliente: -
         * Calendario:   Galicia.

      * Elementos de pedido:

         * 1. Coordinacion - Grupo 4 *Punto de planificación*

            * 1.1 Reunións con cliente - 100h
            * 1.2  Reunións con traballadores - 100h

         * 2  Bloque 1 - Grupo 4/Soldador

            * 2.1 Soldar cuberta A - 350 h *Punto de planificación*
            * 2.2 Soldar cuberta B - 200 h *Punto de planificación*
            * 2.3 Soldar cuberta C - 100 h *Punto de planificación*

         * 3 Bloque  2 - Grupo 4/Carpinteiro

            * 3.1 Teito de madeira de camarote A - 300 h *Punto de planificación*
            * 3.2 Cama e mesilla de camarote A - 250 h *Punto de planificación*
            * 3.3 Poñer escotillas  camarote A - 200 h *Punto de planificación*

      * Outros datos do pedido:
         * Responsable: Nome da persoa   do grupo.
         * Presuposto: Traballo:  200.000  Materiais: 0
         * Estado:  Ofertado.

Exercicio  9 - Planificando dependencias
========================================

Poñer as dependencias seguintes na planificación de cada pedido:



Poñer  as seguintes dependencias:

         * Bloque 1  FIN-INICIO Bloque 2
         * Soldar cuberta A FIN-INICIO Soldar cuberta B
         * Soldar cuberta B FIN-INICIO Soldar cuberta C
         * Teito de  madeira de camarote A INICIO-INICIO Cama e mesillas de camarote A
         * Teito de madeira de camarote A FIN-INICIO Poñer  escotillas camarote A
         * Crear un fito  chamado Entrega proxecto
         * Fito Poñer escotillas camarote A FIN-INICIO Entrega proxecto

Exercicio 10. Asignación de recursos
====================================

Realizar as seguintes asignacións



      *  Tarefa:   Coordinación:

         * Asignación  específica: Elena Boluda Ferrer
         * Estratexia: -  Calcula data fin
         * Número  de  recursos por dia: 0.6

      * Tarefa: Soldar cuberta A

         *  Asignación  xenérica
         * Estratexia  recomendada
         * Número   de recursos por dia: 1

      *  Tarefa: Soldar cuberta B

         *  Asignación xenérica
         *  Estratexia  recomendada
         * Número  de recursos por dia: 1

      *  Tarefa: Soldar cuberta C

         *  Asignación  xenérica
         * Estratexia  recomendada
         * Número   de recursos por dia: 1

      *  Tarefa: Teito  de madeira de camarote A

         * Asignación xenérica con criterios  [Grupo 4,  Carpinteiro]
         * Estratexia: Calcular recursos por dia.
         * Duración: 21 días.
         * Horas: 300 horas.

      * Tarefa: Cama e mesillas de camarote A

         * Asignación xenérica con criterios [Grupo  4, Carpinteiro]
         * Estratexia: Calcular número de horas
         * Número de recursos por dia: 0.5
         * Duración: 20 días.

      * Tarefa:  Poñer escotillas camarote A

         *  Asignación  xenérica con criterios [Grupo  4, Carpinteiro]
         *  Estratexia:  Calcular data fin
         *  Recursos por  dia: 0.5
         *  Horas: 200

Exercicio 11. Avances
======================

Realizar as seguintes asignacións de avance



      *   Elemento de pedido  - Coordinación - Avance de tipo porcentaxe - Valor    máximo 100 -  Propaga

         * Valores: 25% a 15 Marzo de 2011.

      *  Elemento   de pedido - Soldar cuberta A - Avance de tipo unidades -  Valor   máximo 5 - Propaga

         * Valores: 1  unidade ao 2 de Marzo de 2011
         *   Valores: 2  unidades ao 30 de Marzo de 2011

      * Elemento de pedido   -  Soldar cuberta B - Avance de tipo unidades - Valor máximo 10 -    Propaga

         * Valores:  3 unidades ao 2 de Abril de    2011.

      * Elemento de pedido - Soldar cuberta C -   Avance de tipo unidades - Valor máximo 15 - Propaga

          *   Valores: 5 unidades a 31 de Marzo de 2011.

      *  Elemento de pedido - Teito de madeira de camarote A - Avance de tipo porcentaxe - Valor  máximo 100 - Propaga

         *   Valores: 25 a 16  de Marzo de 2011.


