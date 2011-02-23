-------
Grupo 5
-------

.. contents::

Exercicio  1. Criterios
=======================

Crear cada grupo o tipo de criterio cos criterios asociados  detallados:


      * Nome do tipo de criterio: Documentación.
      * Permite valores simultáneos: Sí.
      * Xerárquico: Non.
      * Criterios:

         * Certificado medico 2011
         * Certificado medico 2011
         * Pase entrada estaleiro Navantia.
         * Pase entrada estaleiro Vulcano.
         * Pase entrada estaleiro Barreras.

Exercicio 2. Calendarios
========================

Crear cada grupo un calendario global na aplicación cos seguintes datos:


      * Calendario Asturias
      * Deriva de España.
      * Días excepcionais:

         * 22 de Abril - Tipo BankHoliday - Horas laborais 0.
         * 8 de Setembro - Tipo BankHoliday -  Horas laborais 0.
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

      * Tipo de etiqueta: Grupo
      * Valores:

         * Grupo 1
         * Grupo 2
         * Grupo 3
         * Grupo 4
         * Grupo 5


Exercicio 4. Recursos
=====================

Crear os seguintes recursos cos datos seguintes:


      * Nome: Manuel Rodríguez Fernández NIF: 99999999F Calendario derivado: Andalucía media  xornada.
      * Nome: Raquel Tilve Moreno  NIF: 12121212 B Calendario derivado: Asturias.
      * Nome: Damian Pintos Abogado NIF: 23232323B  Calendario derivado: Andalucía media xornada.


Exercicio 5. Períodos Actividade
================================

Configurar os seguintes períodos de  actividade para os traballadores.


      * Nome: Manuel Rodríguez Fernández

         * Data contratación:  22/02/2011 - Indefinido.

      * Nome: Raquel Tilve Moreno.

         * Data contratación: 28/02/2011 - 29/09/2011

      * Nome: Damian Pintos Abogado.

         * Data contratación: 01/02/2011 - Indefinido

Exercicio 6 - Configurar Excepcións de Calendario
=================================================

Configurar as vacacións como exception  days de intervalo e de tipo HOLIDAY.


      * Nome: Manuel Rodríguez Fernández

         * Vacacións:  19/07/2011 - 25/07/2011

      * Nome: Raquel Tilve Moreno.

         * Vacacións: 26/07/2011 - 01/08/2011

Exercicio 7 -  Criterios en recursos
====================================

Configurar a satisfacción de criterios por parte dos recursos.


      * Nome: Manuel Rodríguez Fernández
      * Satisfaccións  de criterio:

         * Grupo: Grupo 5 -  Dende 01/03/2011 ata infinito.
         * Tipo de  traballo: Pintor - Dende 01/03/2011 ata infinito.

      * Nome: Raquel Tilve Moreno.
      * Satisfaccións de criterio:

         * Grupo: Grupo 5  - Dende 01/03/2011 ata infinito.
         * Tipo de  traballo: Califugador - Dende 01/03/2011 ata infinito.

     * Nome. Damian Pintos Abogado.

      * Satisfaccións de criterio:

        * Grupo: Grupo 5 - Dende data actual ata infinito.



Exercicio  8. Creación dun pedido
=================================

Crear un pedido cada grupo cos seguintes datos e poñerlle, os puntos de planificación e os criterios indicados:


      * Datos de pedido

         * Nome:   Pedido Grupo 5.
         * Data inicio: 01/02/2011
         * Data limite:   01/06/2011.
         * Cliente: -
         * Calendario:   Galicia.

      * Elementos de  pedido:

         * 1.   Coordinación -  *Punto de planificación* - Grupo 5

            * 1.1 Reunións con  cliente - 100h
            * 1.2  Reunións  con traballadores - 100h

         * 2   Bloque 1   *Punto de planificación* - Grupo 5/Pintor

            * 2.1 Pintar  camarotes  A e B- 350 h
            * 2.2 Pintar sala de  máquinas - 200 h
            * 2.3 Pintar  cociña de buque - 100 h

         * 3 Bloque  2 - Grupo 5/Califugador

            * 3.1 Illar camarote A - 300 h *Punto de planificación*
            * 3.2 Illar camarote B - 250 h *Punto de planificación*
            * 3.3 Illar camarote C - 200 h *Punto de planificación*

      * Outros datos do pedido:
         * Responsable: Nome da persoa   do grupo.
         * Presuposto: Traballo:  200.000  Materiais: 0
         * Estado:  Ofertado.


Exercicio  9 - Planificando dependencias
========================================

Poñer as dependencias seguintes na planificación de cada pedido:


Poñer  as seguintes dependencias:

         * Bloque 1   FIN-INICIO Bloque 2
         * Pintar camarotes A e B FIN-INICIO Pintar sá de máquinas
         * Pintar sá de maquinas   FIN-INICIO Pintar cociñas de buque
         * Illar camarote A INICIO-INICIO Illar camarote B
         * Illar camarote A FIN-INICIO Illar camarote C
         * Crear un fito   chamado Entrega proxecto
         * Fito Illar camarote C FIN-INICIO Entrega proxecto

Exercicio 10. Asignación de recursos
====================================

Realizar as seguintes asignacións


     *  Tarefa:   Coordinación:

         * Asignación  específica: Damian Pintos Abogado
         * Estratexia: -  Calcula data fin
         * Número  de  recursos por dia: 0.6

      * Tarefa: Pintar camarotes A e B

         *  Asignación   xenérica
         * Estratexia  recomendada
         *  Número   de recursos por dia: 1

      *  Tarefa: Pintar sa de maquinas

         *  Asignación  xenérica
         *  Estratexia  recomendada
         *  Número  de recursos por dia: 1

      *  Tarefa: Pintar cociñas de buque

         *  Asignación   xenérica
         * Estratexia  recomendada
         *  Número   de recursos por dia: 1

      *  Tarefa: Illar camarote A

         *  Asignación xenérica con criterios  [Grupo 4,  Carpinteiro]
         *  Estratexia: Calcular recursos por dia.
         *  Data  de fin: 15 Outubro 2011
         *  Horas:  300   horas.

      * Tarefa: Illar camarote B

         *   Asignación  xenérica con criterios [Grupo  4, Carpinteiro]
         *   Estratexia:  Calcular número de horas
         *  Número de    recursos por dia: 0.5
         *  Data  de fin: 1 de Setembro 2011

      * Tarefa:  Illar camarote C

         *  Asignación  xenérica con criterios [Grupo  4, Carpinteiro]
         *  Estratexia:  Calcular data fin
         *  Recursos por   dia: 0.5
         *  Horas: 200



Exercicio 11. Avances
======================

Realizar as seguintes asignacións de avance

      *    Elemento de pedido  - Coordinación - Avance de tipo porcentaxe -  Valor    máximo 100 -  Propaga

         * Valores: 25% a  15 Marzo de 2011.

      *  Elemento   de  pedido - Pintar camarotes A e B - Avance de tipo unidades -  Valor   máximo 5 -  Propaga

         * Valores: 1  unidade ao 2 de Marzo de 2011
         * Valores: 2  unidades ao 30 de Marzo de 2011

      *  Elemento de pedido   -  Pintar sá de máquinas - Avance de tipo unidades -  Valor máximo 10 -    Propaga

         * Valores:  3  unidades ao 2 de Abril de    2011.

      * Elemento de pedido -  Pintar cociñas de buque -   Avance de tipo unidades - Valor máximo 15 -  Propaga

          *   Valores: 5 unidades a 31 de Marzo de  2011.

      *  Elemento de pedido  -  Illar camarote A - Avance de  tipo porcentaxe - Valor  máximo 100 - Propaga

          *   Valores: 25 a 16  de Marzo de 2011.

