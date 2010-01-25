
Guía de Arquitectura
#####################

Arquitectura basada en capas
============================

Enumeracion das capas usadas na aplicación:

   * Capa de interfaz:
      * Modulo de autenticacion.
      * Interfaz WEB:
         * Model-View-Controller
         * Capa de servicios conversacionais: XXXModel
      * Interfaz de servicios WEB.
         * Servicios REST.
   * Capa de loxica de negocio: Entidades e clases auxiliares de comportamento
   * Capa de persistencia

Tecnoloxías utilizadas e relacion con cada capa
=========================================================

   * Framework Spring. Contedor de inyeccion de dependencias usado de forma transversal na aplicación para inyeccion de control
e para proporcion de varios servicios como:
      * Xestion das transaccions en Hibernate e a sesión.
      * Spring TestContext para traballar sobre JUnit.
   * Framework ZK. Framework para facer interfaz web.
   * Servicios web REST.
      * Apache CXF. Para




Relación das capas cos paquetes e módulos do proxecto
=====================================================

Proxecto organizados en módulos: Definicion de modulo.

   * Modulo navalplanner-business
     Concepto de servidor. Abarca as capas de loxica de negocio e persistencia.
   * Modulo navalplanner-webapp.
     Toda a interfaz e servicios conversacionais e servicios web.
   * Modulo ganttzk.
     Modulo para compoñentes ZK de




