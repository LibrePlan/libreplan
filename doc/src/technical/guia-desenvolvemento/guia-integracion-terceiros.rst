
LibrePlan: Guía de integración
##############################

A guía de integración de LibrePlan detalla as posibilidades existentes para a integración entre aplicacións co software LibrePlan.

As funcionalidades de integración da ferramenta para a Xestión da Producción permitirán a compartición de datos entre as distintas ferramentas existentes en cada empresa. LibrePlan define unha serie de formatos de intercambio de información empregando a sintaxe XML. A descrición das interfaces e formatos de intercambio son totalmente abertos e está dispoñible a súa implementación para incorporar novas posibilidades de integración se fose necesario.


Visión Global
=============

LibrePlan é unha aplicación que ten coma núcleo de funcionalidades a xestión da planificación e control dunha empresa. LibrePlan traballa cun conxunto de datos enfocado a xestión de recursos para a realización de tarefas permitindo a súa planificación e control. Aínda que LibrePlan permite xestionar o proceso de forma autónoma é necesario provelo de mecanismos de intercambio que permitan a incorporación de información recollida por outras aplicacións xa existentes nas empresas usuarias de LibrePlan.

As empresas que teñen unha certa implantación de sistemas de xestión deberían permitir incorporar a información dos mesmos para facer máis rico e sinxelo o traballo co planificador. As entidades que teñen unha utilidade fóra do planificador son:

   * Os *recursos* que soen ser utilizados nas aplicacións de xestión de persoal e nóminas.
   * Os *pedidos* que son empregados nas ferramentas de presupostación e xestión de ventas.
   * Os *partes de traballo* que recollen o traballo feito polos traballadores e que son empregados para o control dos mesmos e a elaboración das súas nóminas.

Estes elementos son empregados directamente no planificador, xa que os pedidos organízanse en tarefas, que son asignadas a recursos e logo a súa execución é controlada a través dos partes de traballo e a súa vez faise un análise de custos.

LibrePlan permite a integración en dous escenarios:

   * Integración con outras aplicacións: permitindo a incorporación de datos de programas desenvoltos por terceiros. Isto será posible empregando as Interfaces Públicas da Aplicación (API) dispoñibles. A API de LibrePlan permite a interacción coas seguintes entidades propias: recursos, pedidos e partes de traballo, aínda que incorporan outras entidades de interese coma materiais, etiquetas, criterios, categorías de custo e tipos de horas.
   * Integración con outras instancias de LibrePlan: permitindo operacións que deixen compartir información entre distintas empresas centrándose no proceso de subcontratación e reporte de avances do subcontratista.


Integración con outras aplicacións da propia empresa
----------------------------------------------------

LibrePlan permite a integración con outras aplicacións, LibrePlan poderá incorporar información procedente de outras aplicacións empregando un formato de intercambio común que será recibido por un servizo web que permitirá realizar actualizacións de datos ao longo do tempo.

Esta integración deberá ser configurada mediante a habilitación dun usuario que terá permiso de acceso para cada un dos servizos que é de interese integrar, as aplicacións poderán chamar aos servizos empregando o nome de usuario e contrasinal e LibrePlan procesará a incorporación dos datos enviados. En caso de que ocorran erros na incorporación de datos na resposta da petición incorporarase a información dispoñible sobre o erro.

A integración dentro da empresa permite unha integración forte con distintas aplicacións que poderán chamar en calquera momento aos servizos web para a actualización de datos.


Integración entre aplicacións LibrePlan de distintas empresas
-------------------------------------------------------------

LibrePlan ten sido concibido coma un software que permita a realación con distintas empresas que empreguen a aplicación. O escenario principal de colaboración entre empresas é a contratación entre as mesmas, dentro do proceso de planificación é normal a situación que varias empresas colaboren nun mesmo proxecto sendo unha a contratista principal e outras subcontratistas desta.

LibrePlan ten desenvolto un sistema de intercambio de información que permite remitir a información das tarefas a subcontratar a outras empresas e recibir os reportes de avance dos proxectos subcontratados.

Esta integración a nivel aplicación precisa da existencia de usuarios cruzados entre as empresas subcontratante e subcontratista. Esta configuración permitirá que as aplicacións se poidan comunicar a información en tempo real.

Nota: esta integración tamén será posible con outras aplicacións sempre e cando estas empreguen o mesmo formato de intercambio e as aplicacións implementen os servizos de intercambio necesarios.


Fundamentos técnicos
====================

Servizos WEB
------------

Un *servizo web* defínese como un mecanismo de comunicación que se establece entre dous programas a través dunha rede utilizando como protocolo de transporte das mensaxes *HTTP*. Na comunicación que se establece os dous programas que se comunican desempeñan ambos papeis:

   * Un dos programas leva a cabo o papel de *cliente*. É cliente o programa que inicia a comunicación e pide que o programa invocado execute unha determinada operación.
   * O outro desempeña o papel de *servidor*. É o programa que ofrece unha serie de operacións aos clientes. Espera a recepción de peticións de operacións e cando lle chegan execútaas proporcionándolle ao programa cliente unha resposta sobre a operación.

As razóns que levaron á elección de servizos web como vehículo de comunicación para levar a cabo a integración son as seguintes:

   * Por unha banda, os servizos web utilizan o protocolo HTTP como transporte, que é o que usa a WWW. Debido a que practicamente en todas as redes de ordenadores está permitido o acceso á WWW, os firewalls das corporacións non filtran o porto no que se serve a web, e de forma transparente a integración de LibrePlan con outra instalación do programa en outra empresa ou con outras aplicacións dentro da compañía funcionaría.
   * Permiten a definición de mensaxes estruturados en XML - non son protocolos binarios de comunicación - de forma que é sinxelo entender as mensaxes de intercambio se o XML se define de forma acorde ao *modelo de dominio*.
   * É unha tecnoloxía amplamente probada.
   * É independente da linguaxe de programación. Isto permite que aínda que LibrePlan está desenvolvido utilizando a plataforma Java póidanse realizar clientes en distintas tecnoloxías como .NET, C, C++ maximizando as capacidades de integración e facilitándoa.

Dentro dos servizos web existen dous grandes subtipos: Os servizos web baseados en *SOAP* e os servizos web *REST*.

A grandes trazos os servizos web baseados en *SOAP* utilizan como corpo das mensaxes que se intercambias mensaxes *XML* que seguen o estándar *SOAP*. Ademais de *SOAP* son servizos que usan outra serie de estándares relacionados coñecidos como a pila WS (WS-Security, WS-Notification, WSDL).

Os servizos web REST (REpresentational State Transfer) usan as operacións do protocolo HTTP (POST, PUT, DELETE e GET) para especificar parte das operacións e non poñen restricións acerca do corpo das mensaxes. Poden ser XML ou non e, se son XML, non teñen que cinguirse ao estándar *SOAP*.

En LibrePlan os servizos web nos que se baseará a integración serán servizos *REST*. As razóns da elección de servizos REST son as seguintes:

   * Son máis sinxelos de implementar que os servizos *SOAP*. Isto facilita aos usuarios que queiran integrarse con LibrePlan o proceso, xa que o desenvolvemento e a depuración é máis sinxela.
   * Unicamente son máis directos de implementar os servizos web *SOAP* se se utilizan ferramentas automáticas (que existen en linguaxes como Java ou .NET)  que a partir da descrición dos servizos (WSDL) son capaces de xerar os clientes. Agora ben, esta vantaxe descartouse para a elección do tipo de servizos web a implementar porque se quere que, no caso de que non exista integración automática e os XML de intercambio de datos se xeren a man ou ben a partir dunha base de datos pero sen ter que programarse o cliente *SOAP*, esta integración siga sendo posible.

Invocar un servizo web REST de LibrePlan será tan sinxelo como cun cliente HTTP (como pode ser simplemente o navegador para algunhas operacións) invocar unha URL.

Seguridade
----------

Os servizos web REST de que constará LibrePlan para a integración contemplan un soporte de seguridade. A seguridade trátase en 3 dimensións:

   * Seguridade da comunicación.
   * Autenticación do cliente.
   * Autorización do cliente con respecto á operación invocada.


Seguridade da comunicación
~~~~~~~~~~~~~~~~~~~~~~~~~~

A seguridade da comunicación refírese a garantir que as mensaxes que se intercambian entre unha instalación de LibrePlan e os seus clientes (que pode ser outra aplicación ou outra instalación de LibrePlan realizada noutra compañía) sexan confidencias entre os dous extremos da comunicación. Quere dicir isto que, como pode que atravesen redes públicas - integración a través de Internet -, están suxeitas a poder ser examinados por todas as persoas ou axentes que teñan acceso ao medio. Para evitar, por tanto, que ao examinar o medio se obteña información privada das empresas que manteñen a comunicación con LibrePlan este proporciona un mecanismo de seguridade.

A seguridade consiste na posibilidade de servir cifrados os datos e a elección feita para realizar o cifrado é servir os servizos web por HTTPS (HTTP Secure) en lugar de por HTTP. HTTPS é a combinación de HTTP con SSL. Con SSL conséguese por una parte garantir a identidade do servidor LibrePlan e, por outra banda, cifrar a comunicación entre o servidor e o cliente.

Servir os servizos web con HTTPS pódese facer tanto dende o contedor de Servlets necesario para servir a aplicación (Apache Tomcat, Jetty) como se se serve detrás dun proxy que realice o cifrado por HTTPS (por exemplo detrás dun servidor web Apache). En calquera caso, será necesario que a empresa posúa un *certificado público* que permita servir por HTTPS os servizos web e/ou a aplicación.


Autenticación do cliente
~~~~~~~~~~~~~~~~~~~~~~~~

O proceso de autenticación consiste en determinar quen é a persoa ou entidade que quere efectuar unha operación ofrecida por un servizo web.

A aplicación LibrePlan conta con autenticación a través da súa interface web. Está desenvolvido un módulo de usuarios que permite a alta, baixa de usuarios e a configuración dos permisos que poden posuír. Existe un conxunto predefinido de roles e estes roles se poden outorgar/denegar aos diferentes usuarios. Un rol permite realizar un determinado conxunto de operacións.

Para a autenticación nos servizos web proponse reutilizar o sistema de usuarios de forma que para que o servidor vaia a proporcionar unha resposta haberá un paso de autenticar ao peticionario. Por tanto as aplicacións que se desexen integrar con LibrePlan terán que ter creadas na aplicación un usuario coas credencias adecuadas para invocar as operacións desexadas.

Para identificar o peticionario vaise usar a autenticación Basic Access Authentication HTTP. Con este método de autenticación pode pasarse un usuario/contrasinal ao servidor web. Pásase cunha cabeceira na mensaxe HTTP. O formato é o seguinte:

   * Authentication: Basic [usuario:contrasinal codificados en base64]

A codificación base64 unicamente é para ocultar o usuario:contrasinal da vista do usuario, pero non ten por obxecto evitar a súa lectura, xa que a súa conversión a formato lexible por humano é directa algoritmicamente. Á pregunta de como se garante, polo tanto, que o usuario:contrasinal sexan interceptados por un terceiro na rede de comunicación a resposta é a través da seguridade da comunicación. O protocolo HTTPS establece un medio cifrado entre o servidor e o cliente de forma que as mensaxes HTTP completas van cifradas (incluíndo as cabeceiras).


Autorización do cliente respecto á operación invocada
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Unha vez se é capaz de identificar o peticionario da operación do servizo web, é necesario autorizalo ou denegarlle o acceso. Na aplicación web que forma parte de LibrePlan a autorización faise utilizando o framework *Spring Security*. A través deste framework é posible de forma declarativa esixir a posesión de un rol para acceder a unha determinada operación.

Os servizos web se identifican por URL e método HTTP. Exemplos diso son:

   * */ws/rest/orderelements* Method GET
   * */ws/rest/criteriontypes* Method POST

Con Spring Security é posible esixir que para que un usuario teña acceso a unha URL ou outra estea autenticado e teña o rol requirido. Exemplos:

::

 <intercept-url pattern="/ws/rest/**" access="ROLE_WS_READER" method="GET"/>
 <intercept-url pattern="/ws/rest/**" access="ROLE_WS_WRITER" method="POST"/>
 <intercept-url pattern="/ws/rest/**" access="ROLE_WS_WRITER" method="PUT"/>

Desta maneira garántese que o usuario peticionario do servizo ten as credencias adecuadas para a súa invocación.


Conceptos xerais e políticas globais
====================================

Nesta sección detállase unha serie de asuncións e requisitos que se deberán cumprir para ter unha boa integración con LibrePlan. Nesta sección se tratarán as seguintes temáticas:

   * Codificación das entidades
   * Comportamentos das altas e actualizacións
   * Control de erros e recuperación


Codificación das entidades
--------------------------

En todos os fluxos de integración de entidades, cada un dos obxectos que se transmiten dunha aplicación a outra terán unha codificación. Desta forma un obxecto terá un identificador alfanumérico que o identifique tanto na aplicación de LibrePlan coma no resto das aplicacións que se integren.

As entidades que participan na integración terán un atributo *code*, no que se gardará a codificación. A existencia deste código e o seu mantemento é necesario para a correcta integración. O código debe ser único por entidade. Aínda que as entidades suxeitas a integración con outras aplicacións teñen este atributo *code* único dentro de LibrePlan para todas as entidades utilizarase un identificador subrogado autoxerado polo framework de persistencia.

LibrePlan á hora de comunicarse con outras aplicacións recibirá os obxectos e cotexará a existencia dentro do sistema de obxectos de LibrePlan. Se se recibe un obxecto cunha codificación existente entendese que é unha actualización do mesmo, se o obxecto que se recibe non existe no sistema LibrePlan darao de alta.

No caso de que se incorporen entidades con referencias a outras entidades entón na importación comprobarase se existe esta entidade referenciada e se non existe darase un erro. Un exemplo disto sería cando se realice a importación dun traballador e se fai referencia ao calendario que se lle quere asignar. Se non existe, indicarase que non se atopa a instancia.

A incorporación mediante ficheiro XML supón a introdución dunha secuencia de _ítems_ que se van a ir procesando secuencialmente. Esta secuencia de ítems estará formada polas entidades que se incorporan a aplicación. Para identificar as entidades que se transmiten LibrePlan empregará unha codificación baseada en dous parámetros:

   * A posición da instancia no XML. Chamarase *num_item*
   * A codificación baseada no código. Chamarase *code*
   * O tipo de entidade da instancia. Chamarase *entity-type*

No caso de que unha instancia referencie a unha terceira da cal non se dispón do código dado de alta no sistema, LibrePlan reportará un erro de importación indicando o num_item e a codificación da entidade que produciu o erro de importación.

Resumo da codificación da identificación das instancias:

   * *instance-id*: identificación da instancia, estará formada por: *num_item*, *code* e *entity-type*.
   * *num_item*: identifica cun número a posición da entidade dentro do ficheiro XML de importación. Ten a utilidade de permitir localizar a instancia que provocou un erro.
   * *code*: será unha codificación alfanumérica con características de unicidade dentro das instancias dunha mesma entidade (*entity-type*). Este código será común as aplicacións que se estean a integrar.
   * *entity-type*: será posible identificar que tipo de entidade representa unha instancia mediante o seu *entity-type*. Exemplo: resource, work-report, label.

Espazo de nomes e codificación na relación con terceiras empresas
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

No caso de integración de servizos dentro da mesma empresa partimos da existencia e control dunha unicidade de código dentro da organización. Iso non se pode presupoñer cando nos referimos á situación de relación entre dúas empresas. Nese caso LibrePlan manterá unha referencia dobre sobre as instancias de entidades que son compartidas entre dúas organizacións.

LibrePlan respectará a codificación das entidades da empresa subcontratante e será a empresa subcontratista a que manteña ao longo de todas as comunicacións a referencia ás entidades reportadas pola empresa subcontratante. Esta relación manterase no caso das entidades relacionadas cos pedidos como é o caso de Order e OrderElement. Nesas entidades incorporarase un novo atributo *external-code* que fará referencia ao atributo *code* da entidade contratante.

Internamente a empresa subcontratista traballará coa súa codificación propia no atributo *code* que será empregada na interacción coas outras aplicacións da propia empresa.

Comportamentos das altas e actualizacións
-----------------------------------------

A aplicación LibrePlan permitirá realizar unha alta a través de bloques de entidades. A semántica que se adoptará nestas incorporación de conxuntos de entidades será a seguinte. Realmente a operación non é unha alta senón que vai a ser unha alta ou modificación. Isto significa que cando se leva a cabo a incorporación se segue o seguinte algoritmo:

   1. Compróbase se existe a entidade do bloque a inserir.
   #. Se non existe a entidade, entón procédese a levar a cabo a alta.
   #. Se existe a entidade, entón procédese a levar a cabo unha modificación da mesma.

Unha vez que procede a levar a cabo a alta ou modificación, realízase outro proceso, detallado polos seguintes pasos:

   1. Inténtase construír a entidade a dar de alta ou modificar a través da procura das entidades referenciadas.
   #. Se non se pode construír a entidade por problemas en si mesma ou ben nas entidades referenciadas darase un erro indicando cal é o problema.
   #. Se se consegue construír a entidade, entón procédese a pasar as validacións - regras de negocio - que os datos das entidades deben verificar. Se se producen un ou varios non cumprimentos repórtanse.
   #. Se non se produce ningunha violación lévase a cabo a inserción ou modificación.

Para estas operacións de alta ou modificación vaise a utilizar un servizo web identificado a través dunha URL e o método de HTTP POST.

Con respecto a operación de borrado, non se vai a contemplar a súa existencia de maneira xeral. A razón é que o borrado de datos de planificador dunha entidade en xeral ten efectos en cascada sobre múltiples datos das planificacións levadas a cabo nel. Por tanto, a estratexia xeral de nunca borrar fisicamente os datos é a axeitada. Isto non impide que para algunhas entidades teña sentido operacións como a súa desactivación. Isto faría que non se borrara fisicamente a entidade da base de datos senón que deixara de terse en conta a partir dese momento para as novas operacións de planificación nas que estea involucrada.

Control de erros
----------------

Uso dos códigos de estado HTTP nas respostas
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

As mensaxes de resposta HTTP conteñen unha liña que se coñece como liña de estado. O formato da liña de estado é a seguinte:

::

 Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF

Como se pode apreciar existe un campo que é o código de estado, *Status-Code*. O código de estado é un numero de 3 díxitos que se usa para indicar como foi satisfeita a petición por parte do servidor web. Existen un conxunto de estados predefinidos que indican causas comúns que poden acontecer cando se invoca unha URL por parte dun cliente.

LibrePlan vai a facer o seguinte uso dos códigos de estado das respostas HTTP:

   1. *200 OK*. Se a petición é servida correctamente. Os erros lóxicos froito dos datos de entrada do servizo tamén se reportarán mediante este código. Xa que o cliente poderá procesalos para analizar as causas dos erros.
   #. *404 Not Found*. Este código de estado non se vai a devolver por parte de ningún servizo web de LibrePlan. Será devolto unicamente polo contedor de servlets se o cliente invoca unha URL que non se corresponde con ningún dos servizos publicados.
   #. *403 Access is denied*. Este código de estado será devolto por LibrePlan cando a autenticación do usuario é correcta no sistema de usuarios da aplicación pero non ten permiso para executar o servizo que se está solicitando.
   #. *401 Bad credentials*. É utilizado na resposta por LibrePlan para indicar que a autenticación é incorrecta. Quere dicir o anterior que non existe un usuario/contrasinal válido.
   #. *500 Internal Server Error*. Devólvese este código de estado sempre que se produce algún erro provocado por unha excepción que provoque a finalización do fío de execución (thread) no servidor que atende a petición do servizo.
   #. *400 Bad Request*. Darase este erro cando a validación do corpo da petición XML por parte do servizo web de LibrePlan non sexa correcta por non axustarse ao esquema XML que describe o servizo.


Erros que provocan a finalización do fío de execución (thread)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Se existe un erro de programación que xorde froito da invocación dun servizo neste caso devólvese, como se dixo no apartado precedente, un código de erro HTTP 500 e no corpo da mensaxe HTTP darase o seguinte:

::

  <?xml version="1.0" encoding="utf-8" standalone="yes"?>
    <internal-error xmlns="http://rest.ws.libreplan.org"
     message="">
    <stack-trace>
    </stack-trace>
  </internal-error>

Dentro de *<stack-trace>* irá a pila de execución de chamadas do programa ata chegar a función que desencadeou o problema.

Cando se produce un erro deste tipo a entidade que estea realizando a integración podería crear unha incidencia no sistema de xestión de erros describindo a situación que levou a aparición do erro e incluíndo o stack-trace devolto polo servizo web para facilitar a solución do erro pola comunidade de LibrePlan.

Erros lóxicos
~~~~~~~~~~~~~

Os erros lóxicos son erros que non son debidos a un defecto na aplicación LibrePlan senón que son debidos a dúas posibles causas:

   * Os datos que se teñen na base de datos de LibrePlan non son compatibles cos datos de entrada da petición.
   * Os datos de entrada para a operación solicitada no servizo web non son correctos.

Cando se producen erros lóxicos van a ser catalogados polo equipo de desenvolvemento de LibrePlan en dous posibles tipos:

   1. Erros recuperables. Os erros recuperables son aqueles para os que os desenvolvedores da integración do cliente poden decidir intentar realizar unha recuperación automática do erro.
   #. Erros non recuperables. Os erros non recuperables son aqueles para os que non se pode implementar ningún mecanismo automático de solución do problema e o único camiño e a intervención humana para a solución dos problemas detectados.

No corpo da resposta HTTP cando se produce un erro ou varios erros lóxicos será a seguinte:

::

   <instance-constraint-violations instance-id="YY" code="XXX"
   entity-type="ZZZZZ">

     <recoverable-error error-code="1" message="XXX">
         <property name="type" value="dddddd"/>
         <property name="value" value="eeeee"/>
     </recoverable-error>

     <constraint-violation message="XXX" />
     <constraint-violation message="XXX" />

   </instance-constraint-violations/>


Cada erro recuperable indícase a través da etiqueta *<recoverable-error>* e cada erro non recuperable informarase coa etiqueta *<constraint-violation>*.

A descrición dun *<recoverable-error>* é a seguinte:

  * Atributo *error-code*. No atributo *error-code* irá un código de erro interno definido en LibrePlan. Será un número e existirá unha táboa de códigos de erros recuperables en LibrePlan que permitirán aos integradores implementar unha solución recuperable adecuada a cada código de erro.
  * Atributo *message*. Aquí indicarase unha descrición do erro.
  * Etiqueta *<property>*. Pode haber varias etiquetas deste tipo que son usadas para proporcionar datos que poden ser necesarios con dous atributos cada unha:

     * *name*. Nome da propiedade.
     * *value*.  Valor da propiedade.

Pola súa banda cada *<constraint-violation>* ten un atributo *message* no cal se indicará a causa do erro de validación.

Validacións contra esquema
~~~~~~~~~~~~~~~~~~~~~~~~~~

O framework que se utiliza para a implementación dos servizos web é Apache CXF. Con este framework xerase e pódese consultar o XML Schema para cada servizo. Cando aquí se menciona que se poden consultar significa que son servidos polo servlet utilizado por CXF para implementar os servizos web. Por exemplo, a URL para consultar o esquema XML dun servizo é a seguinte:

::

  <url-do-servizo>?_wadl&_type=xml


Os documentos XML Schema son un estándar da W3C que permite especificar a estrutura e formato dos documentos XML. Poden ser utilizados para validar se un determinado XML se axusta a un determinado esquema e así determinar se hai algún erro.

Xa que para os servizos web de LibrePlan van estar dispoñibles os XML Schema dos mesmos, estes poderán ser utilizados polos integradores de aplicacións con LibrePlan para validar que xeran os XML de intercambio correctos.

Tamén se implementará a través de CXF unha validación do XML entrante no corpo das mensaxes HTTP de invocación dos servizos web por parte dos clientes. Por tanto, se validará se o XML contra o esquema XML e se non é correcto mandarase unha mensaxe de resposta HTTP con código de estado 400 e corpo baleiro.


Fluxos de integración
=====================

Os fluxos de integración detallan a secuencia que ten que facer unha aplicación cliente para integrarse coa aplicación LibrePlan e en que secuencia poderá realizar as chamadas aos servizos web dispoñibles.

Os servizos web atópanse dispoñibles a partir da URL_BASE da aplicación en /ws/rest/:

   * URL Base de Servizos: URL_BASE_LIBREPLAN_WEBAPP/ws/rest
   * Exemplo: http://www.libreplan.org/libreplan-webapp/ws/rest

A partir deste intre denominase a esta URL de servizos coma *BASE_SERVICES_URL*.


Fluxos de importación con outras aplicacións
--------------------------------------------

Incorporación de Materiais
~~~~~~~~~~~~~~~~~~~~~~~~~~
Descrición
     * A incorporación de materiais permitirá a importación da información das categorías e materíais de interese na aplicación dende outras aplicacións.
     * A incorporación de materiais permitirá asociar materiais a necesidades para o inicio de tarefas no planificador.

Roles
     * Cliente: proporciona nova información sobre os materiais ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información dos materiais.

Precondicións
    * Tódolos materiais pertencerán a unha categoría.
    * Mantense un código único por material e por categoría.

Postcondicións
    *  Os novos materiais e categorías de materiais serán incorporadas ao sistema.
    *  As instancias que existían previamente no sistema:

       * os seus campos propios serán actualizados coa nova información.
       * se un material cambia de categoría modificarase a categoría a que pertence o material.

Clases involucradas en LibrePlan
 .. image:: images/materials.png
    :width: 300
    :alt: Diagrama de Clases do dominio de Materiais en LibrePlan

Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de novos materiais e categorías e actualiza os datos dos materiais e categorías existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Exemplo de ficheiro de importación
 ::

  <material-units-list xmlns="http://rest.ws.libreplan.org">
     <material-unit code="10" name="Unidades"/>
     <material-unit code="20" name="M3"/>
     <material-unit code="30" name="Metros Lineales"/>
   </material-units>



  <material-category-list xmlns="http://rest.ws.libreplan.org">
    <material-category code="10" name="Tornillos" >
       <material-list>
         <material code="TOR12" description="Tornillos Serie-12" price="123.12" unit-type="10" disabled="false"/>
         <material code="TOR13" description="Tornillos Serie-13" price="123.12" unit-type="10" disabled="false"/>
         <material code="TOR15" description="Tornillos Serie-15" price="123.12" unit-type="10" disabled="false"/>
       </material-list>
       <children>
         <material-category code="20" name="Tornillos Planos" >
            <material-list>
              <material code="TORP12" description="Tornillos Serie-12" price="123.12" unit-type="10" disabled="false"/>
              <material code="TORP13" description="Tornillos Serie-13" price="123.12" unit-type="10" disabled="false"/>
              <material code="TORP15" description="Tornillos Serie-15" price="123.12" unit-type="10" disabled="false"/>
            </material-list>
         </material-category>
         <material-category code="23" name="Tornillos Estrella" >
            <material-list>
              <material code="TORE12" description="Tornillos Serie-12" price="123.12" unit-type="10" disabled="false"/>
              <material code="TORE13" description="Tornillos Serie-13" price="123.12" unit-type="10" disabled="false"/>
              <material code="TORE15" description="Tornillos Serie-15" price="123.12" unit-type="10" disabled="true"/>
            </material-list>
         </material-category>
        </children>
    </material-category>
   </material-category-list>

Incorporación de Etiquetas
~~~~~~~~~~~~~~~~~~~~~~~~~~
Descrición
     * A incorporación de etiquetas permitirá a importación da información dos tipos de etiquetas e etiquetas de interese na aplicación dende outras aplicacións.
     * As etiquetas permitirán a catalogación e filtrado dos elementos do pedido.
     * Exemplos de etiquetas: Zonas do Buque, Prioridades, Centros de Coste, etc...

Roles
     * Cliente: proporciona nova información sobre as etiquetas ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información das etiquetas.

Precondicións
    * Tódalas etiquetas pertencen a un tipo de etiqueta.
    * Mantense un código único por etiquetas e por tipo de etiqueta.
    * O nome das etiquetas é unico dentro dun tipo de étiqueta.
    * Unha etiqueta previamente existente non pode cambiar de tipo.

Postcondicións
    *  As novas etiquetas e tipos de etiqueta serán incorporadas ao sistema.
    *  As instancias que existían previamente no sistema:

       * os seus campos propios serán actualizados coa nova información.

Clases involucradas en LibrePlan
 .. image:: images/labels.png
    :width: 200
    :alt: Diagrama de Clases do dominio de Etiquetas en LibrePlan

Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de novas etiquetas e tipos de etiquetas e actualiza os datos das etiquetas e tipos de etiquetas existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Exemplo de ficheiro de importación
 ::

  <labels-type-list xmlns="http://rest.ws.libreplan.org">
    <label-type code="10" name="Prioridad" >
       <labels-list>
         <label code="1001" name="Baja" />
         <label code="1002" name="Media" />
         <label code="1003" name="Alta" />
       </labels-list>
    </label-type>
    <label-type code="20" name="Complexidade" >
       <labels-list>
         <label code="2001" name="Baja" />
         <label code="2002" name="Media" />
         <label code="2003" name="Alta" />
       </labels-list>
    </label-type>
   </labels-type-list>


Incorporación de Tipos de Criterios e Criterios
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de criterios permitirá incorporar nova información de criterios á aplicación co obxectivo de unificar a codificación con aplicacións externas.
     * Os criterios incorporaranse en base á relación de criterios que pertencen a un tipo de criterio.
     * Os criterios poderán ter unha estrutura xerárquica na súa importación.
     * Exemplos de criterios serían:

        * Tipo de Criterio: Gremio

	    * Criterio: Soldador
            * Criterio: Electricista
            * Criterio: Tubeiro

Roles
     * Cliente: proporciona nova información sobre os criterios e tipos de criterio ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información dos criterios e tipos de criterio.

Precondicións
    * O código de todos os criterios debe de ser único.
    * Os criterios que pertencen a un tipo de criterio non xerárquico non poderán ter nodos fillos.

Postcondicións
    *  As novas instancias serán incorporadas ao sistema unha vez se comprobe se non existían previamente.
    *  As instancias que existían previamente no sistema:

       * os seus campos propios serán actualizados coa nova información.
       * non se poderá cambiar unha entidade que estivera definida como xerárquica e tiña unha estrutura de criterios a non xerárquica.
       * se un criterio non aparece nunha nova importación non se realizará ningún cambio xa que non se realizan borrados. So se realizan actualizacións e marcados coma non activados.

Clases involucradas en LibrePlan
 .. image:: images/criterions.png
    :width: 350
    :alt: Diagrama de Clases do dominio de Criterios en LibrePlan


Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de novos criterios e tipos de criterios e actualiza os datos dos criterios existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Información de realización da chamada
   * *URL Servizo*: BASE_SERVICE_URL/criteriontypes
   * *Exemplo URL*:http://www.libreplan.org/libreplan-webapp/ws/rest/services/criteriontypes
   * *Método POST*

Descrición do formato do ficheiro XML::

   * Nodo criterion-type-list: raíz da importación de tipos de criterios. Pode conter un ou varios nodos do tipo criterion-type.

      * Nodo criterion-type: representa un tipo de criterio.

         * Atributo code (String): código único compartido entre LibrePlan e outras aplicacións que referencia ao tipo de criterio.
         * Atributo name (String): nome que identifica o tipo de criterio.
         * Atributo description (String): describe ao criterio.
         * Atributo allow-hierarchy (boolean): indica se os criterios deste tipo de criterio teñen unha xerarquía de criterios.
         * Atributo allow-simultaneous-criterions-per-resource (boolean): indica que os recursos poden cumprir simultaneamente no tempo máis de un criterio deste tipo.
         * Atributo enabled (boolean): indica que este tipo de criterio está activo. Se non estivera activo non serán asignables novos criterios a este tipo de criterio.
         * Atributo resource (Enumeration): indica para que tipo de recursos é aplicable este criterio, os posibles valores serán (RESOURCE, MACHINE e WORKER).
         * Nodo criterion-list: inclúe a lista de criterios que pertencen ao tipo de criterio. Pode conter un ou varios nodos do tipo criterion.

            * Nodo criterion: representa a unha instancia da entidade criterio.

               * Atributo code (String): código único compartido entre LibrePlan e outras aplicacións que referencia a un criterio.
               * Atributo name (String): nome descritivo do criterio.
               * Atributo active (boolean): indica se este criterio está activo. Se non estivera activo este criterio non sería aplicable a outras entidades no futuro.
               * Nodo children: indica que un criterio ten fillos na xerarquía, polo cal todos os fillos cumpren o criterio do nodo pai.

                  * Nodo criterion: mantén a mesma estrutura que o nodo criterion descrito previamente. E permite describir a estrutura dos fillos.

Exemplo de ficheiro de importación
::

  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>

  <criterion-type-list xmlns="http://rest.ws.libreplan.org">
    <criterion-type code="CRITYPE4" name="ct-4" description="ct-4 desc" allow-hierarchy="true"
        allow-simultaneous-criterions-per-resource="true" enabled="true"
        resource="RESOURCE">

        <criterion-list>
            <criterion code="CRI1" name="c1" active="true"/>
            <criterion code="CRI2" name="c2" active="true">
                <children>
                    <criterion code="CRI3" name="c2-1" active="true">
                        <children>
                            <criterion code="CRI4" name="c2-1-1" active="false"/>
                            <criterion code="CRI5" name="c2-1-2" active="true"/>
                        </children>
                    </criterion>
                    <criterion code="CRI6" name="c2-2" active="true"/>
                </children>
            </criterion>
        </criterion-list>
    </criterion-type>
  </criterion-type-list>

Incorporación de Tipos de Horas de Traballo
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de tipos de horas de traballo  permitirá a importación da información dos tipos de horas de traballo de interese na aplicación dende outras aplicacións.
     * Os tipos de horas terán coma atributos base o código, un nome do tipo de hora, se está habilitada e un precio por hora por defecto a aplicar.
     * Exemplos de tipos de hora son: ordinaria, extra, nocturna, extra-noctura, etc...
     * Estes tipos de horas empregaranse na incorporación dos partes de traballo.

Roles
     * Cliente: proporciona nova información sobre os tipos de horas ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información dos tipos de horas.

Precondicións
    * Mantense un código único por tipo de hora.
    * O nome do tipo de hora de traballo e único.

Postcondicións
    *  Os novos tipos de horas de traballo serán incorporadas ao sistema.
    *  As instancias que existían previamente no sistema verán actualizada a súa información.

Clases involucradas en LibrePlan
 .. image:: images/typeofworkhours.png
    :width: 150
    :alt: Diagrama de Clases do dominio de Tipos de Horas en LibrePlan


Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de novos tipos de horas de traballo e actualiza os datos dos xa existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Exemplo de ficheiro de importación
 ::

  <hours-type-list xmlns="http://rest.ws.libreplan.org">
    <hours-type code="12" name="Ordinaria" default-price="10.2" enabled="true" />
    <hours-type code="13" name="Extra" default-price="14.2" enabled="true" />
    <hours-type code="14" name="Nocturna" default-price="14.2" enabled="true" />
  </hours-type-list>


Incorporación de Categorías de Custo
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de categorías de custo permitirá a importación da información das categorías de custo dende outras aplicacións.
     * As categorías de custo incorporan a información dos custos de precio por hora du tipo de recurso segundo o tipo de hora de traballo que realice.
     * As categorías de custo teñen un precio por hora distinto ao longo do tempo.
     * Exemplo: Categorías de custo: Oficial de primeira. Ten un precio asociado de hora extra de 20 euros á hora durante o ano 2010.

Roles
     * Cliente: proporciona nova información sobre as categorías de custo ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información das categorías de custo.

Precondicións
    * Os códigos das categorías de custo son únicos.
    * No existen dúas categorías de custo co mesmo nome.
    * Nun periodo de tempo unha categoría de custo so pode ter un custo/hora para un tipo de hora simultaneamente.
    * Por categoría de coste só ó último intervalo de tempo pode carecer de data de fin.

Postcondicións
    *  As novas categorías de custo serán incorporadas ao sistema.
    *  As instancias que xa existían previamente no sistema verán actualizada a súa información.

Clases involucradas en LibrePlan
 .. image:: images/costcategories.png
    :width: 150
    :alt: Diagrama de Clases do dominio de Categorías de Coste en LibrePlan


Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de novas categorías de custo e actualiza os datos das xa existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.


Exemplo de ficheiro de importación
 ::

  <?xml version="1.0" encoding="utf-8" standalone="yes"?>
  <cost-category-list xmlns="http://rest.ws.libreplan.org">
    <cost-category enabled="true" name="Categoria A"
    code="18d6ef79-5b45-4928-bfd5-ec80a374699c">
      <hour-cost-list>
        <hour-cost work-hours-type="t1"
        endDate="2010-04-27T12:26:47.010+01:00"
        initDate="2010-03-17T12:26:47.010+01:00" priceCost="5.00"
        code="31001efc-64f2-45be-acb0-045b1d9562ee" />
        <hour-cost work-hours-type="t2"
        endDate="2010-04-27T12:26:47.010+01:00"
        initDate="2010-03-11T12:26:47.014+01:00" priceCost="8.00"
        code="fa840393-2718-4cbd-ba8e-c7f6503a7e9b" />
      </hour-cost-list>
    </cost-category>
    <cost-category enabled="true" name="Categoria B"
    code="b1029095-6ec4-484b-a620-5f0562cef800">
     <hour-cost-list>
      <hour-cost work-hours-type="t3"
         endDate="2010-05-27T12:26:47.010+01:00"
         initDate="2010-05-17T12:26:47.010+01:00" priceCost="6.50"
         code="72974982374kjfkjsdjsjdfsjls" />
     </hour-cost-list>
    </cost-category>
  </cost-category-list>


Incorporación de Recursos
~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de recursos permitirá a importación da información dos recursos humanos de interese na aplicación dende outras aplicacións.
     * Os recursos que se incorporarán serán de tipo máquina ou traballador.
     * A importación permitirá incorporar a información referente aos criterios que cumpre o recurso e a información sobre a súa categoría de custo.
     * A incorporación dos recursos poderá facer referencia ao calendario laboral existente.

Roles
     * Cliente: proporciona nova información sobre os recursos ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información dos recursos.

Precondicións
    * É necesario que as referencias aos criterios que cumpren os recursos estean dispoñibles na aplicación.
    * Débense cumprir as restricións temporais da aplicación dos criterios nos casos dos criterios que só poden ter un único valor no mesmo instante do tempo.
    * É necesario que as categorías de custo ás que pertencen os recursos xa foran importadas previamente en LibrePlan.
    * Un recurso só pode pertencer a unha categoría de custo nun momento do tempo.
    * Se se incorpora un calendario de traballo para o recurso, este deberá estar dado de alta na aplicación no momento da importación.

Postcondicións
    *  As novas instancias serán incorporadas ao sistema coa información dos seus criterios e categorías de custo. Se se indicou un calendario do recurso crearase un calendario de recurso derivado do indicado no XML, noutro caso crearase un calendario derivado do calendario por defecto da empresa.
    *  As instancias que existían previamente no sistema:

       * os seus campos propios serán actualizados coa nova información.
       * as relacións con novos criterios e categorías de custo serán incorporadas. Nunca se borrarán categorías nin criterios se non son incorporados na aplicación xa que poden ter sido dados de alta a través da interface web de LibrePlan. En caso de incoherencia nunca se borrará a información de LibrePlan e reportarase a existencia de inconsistencias para que sexan emendadas.

Clases involucradas en LibrePlan
 .. image:: images/resources.png
    :width: 350
    :alt: Diagrama de Clases do dominio de Recursos en LibrePlan


Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de novos recursos e actualiza os datos dos recursos existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Información de realización da chamada
   * *URL Servizo*: BASE_SERVICE_URL/resources
   * *Exemplo URL*:http://www.libreplan.org/libreplan-webapp/ws/rest/services/resources
   * *Método POST*

Descrición do formato do ficheiro XML::

   * Nodo resource-list: raíz da importación de recursos. Poder conter un ou varios nodos de tipo machine ou worker.

      * Nodo machine: representa un recurso máquina.

         * Atributo code (String): código único compartido entre LibrePlan e outras aplicacións que referencia a unha máquina.
         * Atributo name (String): nome que identifica a máquina.
         * Atributo description (String): describe a máquina.
         * Nodo criterion-satisfaction-list: inclúe a lista de satisfacción de criterios. Pode conter un ou varios nodos de tipo criterion-satisfaction.

            * Nodo criterion-satisfaction: representa que un recurso cumpre un criterio nun momento do tempo.

               * Atributo code (String): código único compartido entre LibrePlan e outras aplicacións que referencia a un criterio.
               * Atributo criterion-type-name (String): nome descritivo do tipo de criterio ao que pertence o criterio en cuestión.
               * Atributo criterion-name (String): nome descritivo do criterio que se aplica neste nodo criterion-satisfaction.
               * Atributo start-date (String): data de inicio do cumprimento do criterio polo recurso en formato ISO 8601 (YYYY-MM-DD).
               * Atributo end-date (String): date de finalización do cumprimento do criterio polo recurso en formato ISO 8601 (YYYY-MM-DD).

         * Nodo resource-cost-category-assignment-list: inclúe a lista de categorías de custo ás que pertence o recurso ao longo do tempo. Pode conter un ou varios nodos de tipo resources-cost-category-assignment.

            * Nodo resources-cost-category-assignment: representa que un recurso pertence a unha categoría de custo nun momento do tempo.

               * Atributo code (String): código único compartido entre LibrePlan e outras aplicacións que referencia a unha categoría de custo.
               * Atributo cost-category-name (String): nome descritivo da categoría de custo a que pertence o recurso.
               * Atributo start-date (String): data de inicio da pertenza á categoría de custo en formato ISO 8601 (YYYY-MM-DD).
               * Atributo end-date (String): data de finalización da pertenza á categoría de custo en formato ISO 8601 (YYYY-MM-DD).

      * Nodo worker: representa un recurso humano.

         * Atributo code (String): código único compartido entre LibrePlan e outras aplicacións que referencia a un traballador.
         * Atributo first-name (String): nome do traballador.
         * Atributo surname (String): apelidos do traballador.
         * Atributo nif (String): nif do traballador.
         * Nodo criterion-satisfaction-list: seguindo o formato detallado para as máquinas.
         * Nodo resources-cost-category-assignment-list: seguindo o formato detallado para as máquinas.

Exemplo de ficheiro de importación
 ::

  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>

  <resource-list xmlns="http://rest.ws.libreplan.org">
    <!-- [It assumes existence of "TestLocationGroupCriterion" and
         "TestCostCategory"] OK. -->
    <machine code="machineA" name="name" description="desc">
        <criterion-satisfaction-list>
            <criterion-satisfaction
                code="CRI433"
                start-date="2009-01-01"
                end-date=""/>
        </criterion-satisfaction-list>
        <resources-cost-category-assignment-list>
            <resources-cost-category-assignment
                code="COST123"
                start-date="2001-01-01"/>
            <resources-cost-category-assignment
                code="COST11"
                start-date="2000-01-01"
                end-date="2000-04-01"/>
        </resources-cost-category-assignment-list>
    </machine>

    <!-- [It assumes existence of "TestCalendar" and "TestCostCategory"] OK -->
    <worker code="WK123" first-name="workerA" surname="surname" nif="nif"
        calendar-name="TestCalendar">
        <criterion-satisfaction-list>
            <criterion-satisfaction
                code="CRI121"
                start-date="2003-03-21"
                end-date=""/>
            <criterion-satisfaction
                code="CRI122"
                start-date="2009-12-24"
                end-date="2009-12-25"/>
        </criterion-satisfaction-list>
        <resources-cost-category-assignment-list>
            <resources-cost-category-assignment
                code="COST444"
                start-date="2001-01-01"/>
            <resources-cost-category-assignment
                code="COST321"
                start-date="2000-01-01"
                end-date="2000-04-01"/>
        </resources-cost-category-assignment-list>
    </worker>
  </resource-list>


Incorporación de Partes de Traballo
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de partes de traballo permitirá a importación da información dos partes de traballo dende outras aplicacións.
     * Os partes de traballo reflexan que un recurso dedicou nunha data un número de horas dun tipo traballando nun elemento do pedido.
     * Os partes de traballo incorporan un código de parte, un código do recurso, o código do traballo, o código do tipo de horas, a data de realización, o número de horas e opcionalmente unha hora de inicio e unha hora de fin.
     * Exemplo: O operario Xavier dedicou 3 horas extras o 2 de xaneiro de 2010 na orde de traballo C5232.

Roles
     * Cliente: proporciona nova información sobre os partes de traballo ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información dos partes de traballo.

Precondicións
    * Os partes de traballo farán referencia a entidades recurso, código do elemento do pedido e código de tipo de horas existentes previamente na aplicación.
    * A codificación dos partes de traballo e única.

Postcondicións
    *  Os novos partes de traballo serán incorporados ao sistema.
    *  Os partes xa existentes verán actualizada a súa información.

Clases involucradas en LibrePlan
 .. image:: images/workreports.png
    :width: 400
    :alt: Diagrama de Clases do dominio de Partes de Traballo en LibrePlan

Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de partes de traballo e actualiza os datos dos xa existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.




Exemplo de ficheiro de importación:
 ::

  <work-report-list xmlns="http://rest.ws.libreplan.org">
   <work-report code="312321" work-report-type="30" @date="2009-10-21" @resource="121" work-order="4323">
    <label-list>
      <label code="adfsdf" value="urxente" type="prioridade"/>
    </label-list>
    <text-field-list>
      <text-field name="incidence"  value="no"/>
    </text-field-list>
    <work-report-line-list>
      <work-report-line code="312" @resource="121" @work-order="4323" @date="" hour-type="10" @start-hour="13:00" @finish-hour="18:00" hours="5" >
       <label-list>
        <label code="e1-01" value="urxente" type="prioridade"/>
       </label-list>
       <text-field-list>
        <text-field name="incidence"  value="none"/>
       </text-field-list>
      </workreportline>
      <work-report-line code="313" resource="122" work-order="4323" date="" hour-type="10" start-hour="13:00" finish-hour="18:00" hours="5" >
       <label-list>
        <label code="e1-02" value="normal" type="prioridade"/>
       </label-list>
       <text-field-list>
        <text-field name="incidence" value="none"/>
       </text-field-list>
      </workreportline>
    </work-report-line-list>
   </work-report>
   <work-report .....>
   ...
   </work-report>
  </work-report-list>


Incorporación de Pedidos
~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de pedidos permitirá a importación da información dos pedidos dende outras aplicacións.
     * Os pedidos reflexan unha estructura do traballo que e preciso realizar dunha forma xerarquica.
     * Os cada elemento do pedido está codificado, e estes códigos serán os empregados para referenciar aos partes de traballo.
     * Os elementos do pedido poderán incorporar información referente a criterios necesarios para a realización dos traballos.
     * Os elementos do pedido poderán incorporar etiquetas que poderán ser empregadas para a realización de filtrados.
     * Os elementos do pedido poderán incorporar necesidades de materiais que poderán ser empregadas no planificador.
     * A estructura de traballo pode incorporar a información do número de horas de traballo presupuestadas para cada elemento. Esta etimación será realizada nos nodos folla.

Roles
     * Cliente: proporciona nova información sobre os pedidos ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información dos pedidos.

Precondicións
    * Os pedidos e os elementos do pedido terán unha codificación unica dentro da empresa.
    * Os materiais, etiquetas e criterios referenciados deberán ter sido previamente importados a LibrePlan.
    * Un pedido previamente importado permitirá a incorporación de novos nodos sempre e cando non se modifique a estructura dos nodos existentes previamente.


Postcondicións
    *  Os novos pedidos serán incorporados ao sistema.
    *  Os pedidos xa existentes verán actualizada a súa información.

      * Non se eliminarán referencias a materiais, etiquetas ou criterios no proceso de actualización, xa que estas poideron ser creadas dentro de LibrePlan.
      * Actualizarase a información dos elementos do pedido.

Clases involucradas en LibrePlan
 .. image:: images/orders.png
    :width: 350
    :alt: Diagrama de Clases do dominio de Pedidos en LibrePlan


Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de pedidos e actualiza os datos dos xa existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Exemplo de ficheiro de importación
 ::

  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
  <order-lists xmlns="http://rest.ws.libreplan.org">
   <order code="ORDER-1" name="Order" init-date="2010-01-01" deadline="2010-05-30">
    <label-list>
     <label code="10" name="low" type="priority" />
    </label-list>
    <criterion-requirements>
      <direct-criterion-requirement code="10" />
      <direct-criterion-requirement code="20" />
    </criterion-requirements>
    <advance-measurements>
      <advance-measurement date="2009-11-01" value="12.35" />
      <advance-measurement date="2009-12-01" value="25.35" />
    </advance-measurements>
    <children>
     <order-line-group code="ORDER-1-OE-1" name="Order element 1">
      <advance-measurements>
       <advance-measurement date="2009-11-01" value="10.35" />
       <advance-measurement date="2009-12-01" value="20.35" />
      </advance-measurements>
      <criterion-requirements>
       <indirect-criterion-requirement code="10" is-valid="true"/>
       <indirect-criterion-requirement code="20" is-valid="true"/>
      </criterion-requirements>
      <children>
       <order-line code="ORDER-1-OE-1-1" name="Order element 1.1">
        <criterion-requirements>
         <indirect-criterion-requirement code="10" is-valid="true"/>
         <indirect-criterion-requirement code="20" is-valid="true"/>
        </criterion-requirements>
        <material-assignments>
         <material-assignment material-code="MATERIAL-1" units="100" unit-price="10.5" />
        </material-assignments>
        <hours-group-list>
         <hours-group code="HG-1" working-hours="1000" resource-type="WORKER">
          <criterion-requirements>
           <indirect-criterion-requirement code="10" is-valid="false"/>
           <indirect-criterion-requirement code="20" is-valid="true"/>
          </criterion-requirements>
         </hours-group>
        </hours-group-list>
       </order-line>
       <order-line code="ORDER-1-OE-1-2" name="Order element 1.2">
        <criterion-requirements>
         <indirect-criterion-requirement code="10" is-valid="false"/>
         <indirect-criterion-requirement code="20" is-valid="true"/>
        </criterion-requirements>
        <hours-group-list>
         <hours-group code="HG-2" working-hours="2000" resource-type="WORKER" >
          <criterion-requirements>
           <indirect-criterion-requirement code="10" is-valid="true"/>
           <indirect-criterion-requirement code="20" is-valid="true"/>
          </criterion-requirements>
         </hours-group>
        </hours-group-list>
       </order-line>
      </children>
     </order-line-group>
     <order-line code="ORDER-1-OE-2" name="Order element 2">
      <criterion-requirements>
       <indirect-criterion-requirement code="10" is-valid="true"/>
       <indirect-criterion-requirement code="20" is-valid="true"/>
      </criterion-requirements>
      <advance-measurements>
       <advance-measurement date="2009-11-01" value="9.35" />
       <advance-measurement date="2009-12-01" value="50.35" />
      </advance-measurements>
      <labels>
        <label name="medium" type="risk" />
      </labels>
      <hours-group-list>
       <hours-group code="HG-3" working-hours="1500" resource-type="WORKER">
        <criterion-requirements>
         <indirect-criterion-requirement code="10" is-valid="true"/>
         <indirect-criterion-requirement code="20" is-valid="true" />
        </criterion-requirements>
       </hours-group>
      </hours-group-list>
     </order-line>
    </children>
   </order>
   <order code="ORDER-2" name="Order2" init-date="2010-04-01" deadline="2010-09-30">
   .....
   </order>
  </order-list>


Incorporación de Calendarios
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de calendarios permitirá a importación da información dos calendarios dende outras aplicacións.
     * Os calendarios empréganse para determinar a dispoñinibilidade dos traballadores na aplicación.

Roles
     * Cliente: proporciona nova información sobre os calendarios ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información dos calendarios.

Precondicións
    * Os calendarios terán unha codificación unica dentro da empresa.

Postcondicións
    *  Os novos calendarios serán incorporados ao sistema.
    *  Os calendarios xa existentes verán actualizada a súa información.

Clases involucradas en LibrePlan
 .. image:: images/calendars.png
    :width: 450
    :alt: Diagrama de Clases do dominio de Calendarios en LibrePlan

Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de calendarios e actualiza os datos dos xa existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Exemplo de ficheiro de importación
 ::

  <?xml version="1.0" encoding="utf-8" standalone="yes"?>
  <base-calendar-list xmlns="http://rest.ws.libreplan.org">
  <base-calendar name="Spanish Calendar" code="000-001">
      <calendar-exception-list>
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-01-01"
          code="001-001" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-01-06"
          code="001-002" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-04-22"
          code="001-003" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-05-01"
          code="001-004" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-08-15"
          code="001-005" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-10-12"
          code="001-006" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-11-01"
          code="001-007" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-12-06"
          code="001-008" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-12-08"
          code="001-009" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-12-25"
          code="001-010" />
      </calendar-exception-list>
      <calendar-data-list>
        <calendar-data code="001-001">
          <hours-per-day-list>
            <hours-per-day hours="8" day="MONDAY" />
            <hours-per-day hours="8" day="TUESDAY" />
            <hours-per-day hours="8" day="WEDNESDAY" />
            <hours-per-day hours="8" day="THURSDAY" />
            <hours-per-day hours="8" day="FRIDAY" />
          </hours-per-day-list>
        </calendar-data>
      </calendar-data-list>
    </base-calendar>
    <base-calendar code="000-002" name="Galician Calendar"
      parent="000-001">
      <calendar-exception-list>
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-04-21"
          code="002-001" />
        <calendar-exception calendar-exception-type-code="BANK_HOLIDAY"
          hours="0" date="2011-05-17"
          code="002-002" />
      </calendar-exception-list>
      <calendar-data-list>
        <calendar-data code="001-002">
          <hours-per-day-list>
            <hours-per-day hours="8" day="MONDAY" />
            <hours-per-day hours="8" day="TUESDAY" />
            <hours-per-day hours="8" day="WEDNESDAY" />
            <hours-per-day hours="8" day="THURSDAY" />
            <hours-per-day hours="8" day="FRIDAY" />
          </hours-per-day-list>
        </calendar-data>
      </calendar-data-list>
    </base-calendar>
  </base-calendar-list>


Incorporación de Tipos de Horas de Traballo
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de tipos de horas de traballo permitirá a importación da información dos tipos de horas dende outras aplicacións.
     * Os tipos de horas de traballo empréganse para determinar as categorías de coste na aplicación.

Roles
     * Cliente: proporciona nova información sobre os tipos de horas de traballo ao servidor LibrePlan.
     * Servidor: procesa a petición do cliente incorporando a nova información dos tipos de horas de traballo.

Precondicións
    * Os tipos de horas de traballo terán unha codificación unica dentro da empresa.

Postcondicións
    *  Os novos tipos de horas de traballo serán incorporados ao sistema.
    *  Os tipos de horas xa existentes verán actualizada a súa información.

Clases involucradas en LibrePlan
 .. image:: images/costcategories.png
    :width: 150
    :alt: Diagrama de Clases do dominio de Tipos de Horas de Traballo en LibrePlan

Descrición do fluxo
  1. A aplicación cliente que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a alta de tipos de horas de traballo e actualiza os datos dos xa existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Exemplo de ficheiro de importación
 ::

  <?xml version="1.0" encoding="utf-8" standalone="yes"?>
  <type-work-hours-list xmlns="http://rest.ws.libreplan.org">
  <!-- Ok-->
    <type-work-hours enabled="true" defaultPrice="8.00" name="Hora Extra" code="t1" />

  <!-- Ok-->
    <type-work-hours enabled="false" defaultPrice="5.00" name="Normal" code="t2" />

  <!-- Ok-->
    <type-work-hours enabled="true" defaultPrice="9.50" name="Plus Nocturnidad" code="t3" />

  <!-- [ without enabled property ] Ok -->
    <type-work-hours defaultPrice="9.50" name="t4-name" code="t4" />

  <!-- [ without defaultPrice property ] Ok -->
  <type-work-hours enabled="true" name="t5-name" code="t5" />

  <!-- [ without name property ] -->
    <type-work-hours enabled="true" defaultPrice="9.50" code="t6" />

  <!-- [ without code property ] -->
  <type-work-hours enabled="true" defaultPrice="9.50" name="t7-name"  />

  <!-- [ with a repeated name ] -->
    <type-work-hours enabled="true" defaultPrice="9.50" name="Normal" code="t8" />

  <!-- [ with a repeated code ] OK updated -->
  <type-work-hours enabled="true" defaultPrice="9.50" name="t9-name" code="t1" />

  </type-work-hours-list>


Exportación de Horas Traballadas por Recursos
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de partes de traballo ou a introducción dos mesmos a través da aplicación permite obter a dedicación dos recursos.
     * O servizo de exportación por horas permitiranos consultar o total de  horas de traballo desenvoltos polos recursos nun periodo de tempo.
     * O servizo permitirá unha consulta global que mostrará a información de horas traballadas desglosada en tódolos recursos que traballaron no periodo de tempo.
     * O servizo permitirá unha consulta particular que mostrará a información do recursos particular nun periodo de tempo.

Roles
     * Cliente: pide a aplicación LibrePlan indicando un periodo de tempo, e opcionalmente o código dun recurso, o dato de horas traballadas.
     * Servidor: procesa a petición do cliente xerando un ficheiro XML coa información das horas traballadas por cada un dos recursos da empresa ou dun recurso particular.

Precondicións
    * No caso de consultas particulares o código do recurso consultado existe na aplicación.

Postcondicións
    *  Obtense o sumatorio para cada recurso do número de horas traballadas nun periodo de tempo.
    *  Se a consulta é particular, obtense únicamente o número de horas traballadas por ese recurso.

Clases involucradas en LibrePlan
 .. image:: images/workedhours.png
    :width: 150
    :alt: Diagrama de Clases do dominio de Recursos en LibrePlan

Descrición do fluxo
  1. A aplicación cliente que se integra fara unha petición ao servizo indicando o periodo de tempo e opcionalmente o código do recurso.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a petición, e xera un ficheiro XML coa información de horas traballadas polos recursos no periodo.
  #. O servizo web devolve o XML ou a saída de erros se a execución do servizo non foi correcta.
  #. A aplicación cliente procesa a saída XML do servizo e incorpora os datos sobre horas traballadas ou procesa os erros detectados polo servizo.


Exemplo de ficheiro de exportación:
 ::

  <resource-worked-hours-list xmlns="http://rest.ws.libreplan.org" start-date="2009-10-01" end-date="2009-10-31">
   <resource-worked-hours resource="321" hours="160" >
   <resource-worked-hours resource="322" hours="165" >
   <resource-worked-hours resource="323" hours="142" >
   <resource-worked-hours resource="324" hours="124" >
  </resource-worked-hours-list>

Exportación de Tipos de Excepcións do Calendario
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * Os diferentes días dos calendarios poden marcarse con diferentes tipos de excepcións do calendario.
     * O servizo de exportación dos tipos de excepción permite consultar os diferentes tipos de excepcións do calendario definidos na aplicación.

Roles
     * Cliente: pide a aplicación LibrePlan sen necesidade de pasar ningún argumento.
     * Servidor: procesa a petición do cliente xerando un ficheiro XML coa información dos tipos de excepcións do calendario.

Postcondicións
    *  Obtense a lista de tipos de excepcións do calendario definidos na aplicación.

Clases involucradas en LibrePlan
 .. image:: images/calendars.png
    :width: 450
    :alt: Diagrama de Clases do dominio de Calendarios en LibrePlan

Descrición do fluxo
  1. A aplicación cliente que se integra fara unha petición ao servizo.
  #. A aplicación cliente realiza a chamada ao servizo web cos datos de autorización.
  #. O servizo web procesa a petición, e xera un ficheiro XML coa información de tipos de excepcións do calendario.
  #. O servizo web devolve o XML ou a saída de erros se a execución do servizo non foi correcta.
  #. A aplicación cliente procesa a saída XML do servizo e incorpora os datos sobre tipos de excepcións ou procesa os erros detectados polo servizo.


Exemplo de ficheiro de exportación:
 ::

  <calendar-exception-type-list xmlns="http://rest.ws.libreplan.org">
    <calendar-exception-type over-assignable="false" color="red"
      name="BANK_HOLIDAY" code="BANK_HOLIDAY" />
    <calendar-exception-type over-assignable="false" color="red"
      name="HOLIDAY" code="HOLIDAY" />
    <calendar-exception-type over-assignable="false" color="red"
      name="LEAVE" code="LEAVE" />
    <calendar-exception-type over-assignable="false" color="red"
      name="SICK_LEAVE" code="SICK_LEAVE" />
    <calendar-exception-type over-assignable="false" color="red"
      name="STRIKE" code="STRIKE" />
    <calendar-exception-type over-assignable="true" color="orange"
      name="WORKABLE_BANK_HOLIDAY" code="WORKABLE_BANK_HOLIDAY" />
  </calendar-exception-type-list>


Fluxos con outras instancias de LibrePlan
-----------------------------------------

Exportación-importación de Pedidos entre empresas Cliente-Proveedor
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de pedidos permitirá a importación da información dos pedidos dende a aplicación libreplan dunha empresa subcontratante.
     * Os pedidos reflexan unha estructura do traballo que e preciso realizar dunha forma xerarquica.
     * Os cada elemento do pedido está codificado, e estes códigos serán os empregados para reportar os avances a empresa subcontratante.
     * Os elementos do pedido poderán incorporar etiquetas que poderán ser empregadas para a realización de filtrados.
     * Os elementos do pedido poderán incorporar necesidades de materiais que poderán ser empregadas no planificador.
     * A estructura de traballo pode incorporar a información do número de horas de traballo presupuestadas para cada elemento. Esta estimación será realizada nos nodos folla.

Roles
     * Cliente: aplicación LibrePlan que remite un novo pedido a outra empresa cunha instalación de LibrePlan.
     * Servidor: procesa a petición do cliente incorporando o novo pedido

Precondicións
    * Os pedidos e os elementos do pedido terán unha codificación única dentro da empresa que xera a subcontratación. Non se poden mezclar codificacións dunha mesma empresa.
    * Os materiais, etiquetas e criterios referenciados deberán ter un código na incorporación que será referenciado coma código externo.
    * Un pedido previamente importado permitirá a incorporación de novos nodos sempre e cando non se modifique a estructura dos nodos existentes previamente.

Postcondicións
    * O novo pedido será incorporados ao sistema.
    * Se o pedido xa estaba no sistema actualizaránse os datos.
      * Non se eliminarán referencias a materiais ou etiquetas no proceso de actualización, xa que estas poideron ser creadas dentro de LibrePlan.
      * Actualizarase a información dos elementos do pedido.

Descrición do fluxo
  1. A aplicación LibrePlan cliente (a empresa subcontratante)  que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web (da empresa subcontratista) cos datos de autorización.
  #. O servizo web procesa a alta de pedidos e actualiza os datos dos xa existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Exemplo de ficheiro de importación
 ::

  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   <subcontracted-task-data xmlns="http://rest.ws.libreplan.org" work-description="Pedido de 100 puertas"
    subcontracted-code="REFERENCE-CODE-1" subcontracted-price="152200.03" external-company-nif="B15323232">
    <materials>
     <material code="MAT-1" material-reference="MATERIAL-1" name="Tuercas 2x20" descripcion="Tuercas moi resistentes"  unit-price="10.5"/>
    </materials>
   <labels>
   </labels>
    <order-line-group code="ORDER-1-OE-23" name="Order line group" init-date="2010-01-01" deadline="2011-02-01">
   <labels>
    <label code="label-10" />
   </labels>
   <children>
    <order-line-group code="ORDER-1-OE-1" name="Order element 1" description="Descripcion">
      <children>
        <order-line code="ORDER-1-OE-1-1" name="Order element 1.1">
          <material-assignments>
            <material-assignment  code="MAT-1" units="100" unit-price="11.21" />
          </material-assignments>
          <hours-groups>
            <hours-group code="HG-1" working-hours="1000" resource-type="WORKER" />
          </hours-groups>
        </order-line>
        <order-line code="ORDER-1-OE-1-2" name="Order element 1.2">
          <hours-groups>
            <hours-group code="HG-1" working-hours="2000" resource-type="WORKER" />
          </hours-groups>
        </order-line>
      </children>
    </order-line-group>
    <order-line code="ORDER-1-OE-2" name="Order element 2">
      <labels>
        <label name="medium" type="risk" />
      </labels>
      <hours-groups>
        <hours-group code="HG-1" working-hours="1500" resource-type="WORKER" />
      </hours-groups>
     </order-line>
    </children>
   </order-line-group>
  </subcontracted-task-data>


Exportación-importación de Avances entre empresas Proveedor-Cliente
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Descrición
     * A incorporación de avances de pedidos permitirá a comunicación dos avances dos pedidos incorporados mediante o módulo de xestión de subcontratas.
     * A incorporación de avances respetará a estructura xerarquica do pedido. Ao estar codificados os elementos do pedido, as medidas de avances estarán asociadas aos nodos do pedido.
     * O avance remitido será o que teña a empresa subcontratista de tipo *subcontractor*. Este avance sempre terá tipo porcentual.

Roles
     * Cliente: aplicación LibrePlan que remite as medicións de avances dun pedido comunicado previamente por outra empresa usuaria de LibrePlan.
     * Servidor: procesa a petición do cliente incorporando as novas medicións de avance.

Precondicións
    * O pedido fora comunicado mediante o módulo de xestión de subcontratas.
    * Os pedidos e os elementos do pedido terán unha codificación única dentro da empresa que xera a subcontratación e están sincronizados nas dúas empresas.
    * Non se poden mezclar codificacións dunha mesma empresa.
    * A empresa que xera a subcontratación non pode modificar a organización nen codificación dos elementos codificados.

Postcondicións
    * A empresa subcontratante incorpora as medicións de avance proporciondas pola emrpesa subcontratista.

Descrición do fluxo
  1. A aplicación LibrePlan cliente (a empresa subcontratista)  que se integra xerará un ficheiro seguindo o formato detallado.
  #. A aplicación cliente realiza a chamada ao servizo web (da empresa subcontratante) cos datos de autorización.
  #. O servizo web procesa as medicións de avances e actualiza os datos dos xa existentes.
  #. O servizo web devolve nun XML a saída de erros ou a correcta execución do servizo.
  #. A aplicación cliente procesa a saída XML do servizo e reporta o éxito ou os erros detectados polo servizo.

Exemplo de ficheiro de importación de avances.
 ::

  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   <subcontracted-task-data xmlns="http://rest.ws.libreplan.org" work-description="Pedido de 100 puertas"
    subcontracted-code="REFERENCE-CODE-1" subcontracted-price="152200.03" external-company-nif="B15323232">
   <children>
    <order-line-group code="ORDER-1-OE-1" name="Order element 1" description="Descripcion">
     <advance-meassurement value="75%">
      <children>
        <order-line code="ORDER-1-OE-1-1" name="Order element 1.1">
          <hours-groups>
            <hours-group code="HG-1" working-hours="1000" resource-type="WORKER" />
          </hours-groups>
        </order-line>
        <order-line code="ORDER-1-OE-1-2" name="Order element 1.2">
          <hours-groups>
            <hours-group code="HG-1" working-hours="2000" resource-type="WORKER" />
          </hours-groups>
        </order-line>
      </children>
    </order-line-group>
    <order-line code="ORDER-1-OE-2" name="Order element 2">
      <advance-meassurement value="90%">
      <hours-groups>
        <hours-group code="HG-1" working-hours="1500" resource-type="WORKER" />
      </hours-groups>
     </order-line>
    </children>
   </order-line-group>
  </subcontracted-task-data>


Desenvolvemento dun cliente
===========================

Nesta sección mostrase o código de exemplo de dous scripts shell que permiten unha operación de importación de recursos e outra de exportación de criterios.

Código de exemplo
-----------------

O seguinte script permite interactuar co servizo de importación de recursos empregando unha sinxela petición HTTP e enviando o XML mediante POST.

::

 #!/bin/sh

 DEVELOPMENT_BASE_SERVICE_URL=http://localhost:8080/libreplan-webapp/ws/rest
 PRODUCTION_BASE_SERVICE_URL=http://www.libreplan.org/libreplan-webapp/ws/rest

 DEVELOPMENT_CERTIFICATE=""
 PRODUCTION_CERTIFICATE=-k

 printf "Login name: "
 read loginName
 printf "Password: "
 read password

 baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
 certificate=$DEVELOPMENT_CERTIFICATE

 for i in "$@"
 do
    if [ "$i" = "--prod" ]; then
        baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
        certificate=$PRODUCTION_CERTIFICATE
    else
       file=$i
    fi
 done

 if [ "$file" = "" ]; then
     printf "Missing file\n" 1>&2
     exit 1
 fi

 authorization=`./base64.sh $loginName:$password`

 curl -sv -X POST $certificate -d @$file \
    --header "Content-type: application/xml" \
    --header "Authorization: Basic $authorization" \
    $baseServiceURL/resources | tidy -xml -i -q -utf8


O seguinte código de exemplo fai unha petición por GET que nos devolve o listado de tipos de criterios e criterios que están dados de alta na aplicación nun XML que segue o formato definido.

::

 #!/bin/sh

 DEVELOPMENT_BASE_SERVICE_URL=http://localhost:8080/libreplan-webapp/ws/rest
 PRODUCTION_BASE_SERVICE_URL=http://www.libreplan.org/libreplan-webapp/ws/rest

 DEVELOPMENT_CERTIFICATE=""
 PRODUCTION_CERTIFICATE=-k


 printf "Login name: "
 read loginName
 printf "Password: "
 read password

 if [ "$1" = "--prod" ]; then
     baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
     certificate=$PRODUCTION_CERTIFICATE
 else
    baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
    certificate=$DEVELOPMENT_CERTIFICATE
 fi

 authorization=`./base64.sh $loginName:$password`

 curl -sv -X GET $certificate --header "Authorization: Basic $authorization" \
     $baseServiceURL/criteriontypes | tidy -xml -i -q -utf8
