Configuração LDAP
#################

.. contents::

Este ecrã permite estabelecer uma ligação com o LDAP para delegar
a autenticação e/ou autorização.

Está dividido em quatro áreas diferentes, que são explicadas a seguir:

Activação
=========

Esta área é utilizada para definir as propriedades que determinam como o *LibrePlan* utiliza
o LDAP.

Se o campo *Activar autenticação LDAP* estiver marcado, o *LibrePlan* consultará
o LDAP sempre que um utilizador tentar iniciar sessão na aplicação.

O campo *Utilizar funções LDAP* marcado significa que é estabelecido um mapeamento entre as funções LDAP e as
funções do LibrePlan. Consequentemente, as permissões de um utilizador no
LibrePlan dependerão das funções que o utilizador possui no LDAP.

Configuração
============

Esta secção contém os valores dos parâmetros para aceder ao LDAP. *Base*, *UserDN* e
*Password* são parâmetros utilizados para ligar ao LDAP e procurar utilizadores. Por conseguinte,
o utilizador especificado deve ter permissão para realizar esta operação no LDAP. Na
parte inferior desta secção, existe um botão para verificar se é possível estabelecer uma ligação LDAP
com os parâmetros fornecidos. É aconselhável testar a ligação antes de
continuar a configuração.

.. NOTE::

   Se o seu LDAP estiver configurado para funcionar com autenticação anónima, pode
   deixar os atributos *UserDN* e *Password* em branco.

.. TIP::

   Relativamente à configuração do *Active Directory (AD)*, o campo *Base* deve ser a
   localização exacta onde o utilizador vinculado reside no AD.

   Exemplo: ``ou=organizational_unit,dc=example,dc=org``

Autenticação
============

Aqui, pode configurar a propriedade nos nós LDAP onde o nome de utilizador fornecido
deverá ser encontrado. A propriedade *UserId* deve ser preenchida com o nome da
propriedade onde o nome de utilizador está armazenado no LDAP.

A caixa de verificação *Guardar palavras-passe na base de dados*, quando marcada, significa que a
palavra-passe também é armazenada na base de dados do LibrePlan. Desta forma, se o LDAP estiver
inactivo ou inacessível, os utilizadores LDAP podem autenticar-se na base de dados do LibrePlan.
Se não estiver marcada, os utilizadores LDAP só podem ser autenticados no LDAP.

Autorização
===========

Esta secção permite definir uma estratégia para fazer corresponder as funções LDAP com as
funções do LibrePlan. A primeira escolha é a estratégia a utilizar, dependendo da
implementação do LDAP.

Estratégia de Grupo
-------------------

Quando esta estratégia é utilizada, indica que o LDAP tem uma estratégia de grupos de funções.
Isto significa que os utilizadores no LDAP são nós que estão directamente sob um ramo que
representa o grupo.

O exemplo seguinte representa uma estrutura LDAP válida para utilizar a estratégia de grupo.

* Estrutura LDAP::

   dc=example,dc=org
   |- ou=groups
      |- cn=admins
      |- cn=itpeople
      |- cn=workers
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

Neste caso, cada grupo terá um atributo, por exemplo, chamado ``member``,
com a lista de utilizadores pertencentes ao grupo:

* ``cn=admins``:

  * ``member: uid=admin1,ou=people,dc=example,dc=org``
  * ``member: uid=it1,ou=people,dc=example,dc=org``

* ``cn=itpeople``:

  * ``member: uid=it1,ou=people,dc=example,dc=org``
  * ``member: uid=it2,ou=people,dc=example,dc=org``

* ``cn=workers``:

  * ``member: uid=worker1,ou=people,dc=example,dc=org``
  * ``member: uid=worker2,ou=people,dc=example,dc=org``
  * ``member: uid=worker3,ou=people,dc=example,dc=org``

A configuração para este caso é a seguinte:

* Estratégia de pesquisa de funções: ``Group strategy``
* Caminho do grupo: ``ou=groups``
* Propriedade da função: ``member``
* Consulta de pesquisa de funções: ``uid=[USER_ID],ou=people,dc=example,dc=org``

E, por exemplo, se pretender fazer corresponder algumas funções:

* Administração: ``cn=admins;cn=itpeople``
* Leitor de serviço web: ``cn=itpeople``
* Escritor de serviço web: ``cn=itpeople``
* Leitura de todos os projectos permitida: ``cn=admins``
* Edição de todos os projectos permitida: ``cn=admins``
* Criação de projectos permitida: ``cn=workers``

Estratégia de Propriedade
-------------------------

Quando um administrador decide utilizar esta estratégia, indica que cada utilizador
é um nó LDAP e, dentro do nó, existe uma propriedade que representa
o(s) grupo(s) do utilizador. Neste caso, a configuração não requer o
parâmetro *Caminho do grupo*.

O exemplo seguinte representa uma estrutura LDAP válida para utilizar a estratégia de propriedade.

* Estrutura LDAP::

   dc=example,dc=org
   |- ou=people
      |- uid=admin1
      |- uid=it1
      |- uid=it2
      |- uid=worker1
      |- uid=worker2
      |- uid=worker3

**Com Atributo**

Neste caso, cada utilizador terá um atributo, por exemplo, chamado ``group``,
com o nome do grupo ao qual pertence:

* ``uid=admin1``:

  * ``group: admins``

* ``uid=it1``:

  * ``group: itpeople``

* ``uid=it2``:

  * ``group: itpeople``

* ``uid=worker1``:

  * ``group: workers``

* ``uid=worker2``:

  * ``group: workers``

* ``uid=worker3``:

  * ``group: workers``


.. WARNING::

   Esta estratégia tem uma restrição: cada utilizador só pode pertencer a um grupo.

A configuração para este caso é a seguinte:

* Estratégia de pesquisa de funções: ``Property strategy``
* Caminho do grupo:
* Propriedade da função: ``group``
* Consulta de pesquisa de funções: ``[USER_ID]``

E, por exemplo, se pretender fazer corresponder algumas funções:

* Administração: ``admins;itpeople``
* Leitor de serviço web: ``itpeople``
* Escritor de serviço web: ``itpeople``
* Leitura de todos os projectos permitida: ``admins``
* Edição de todos os projectos permitida: ``admins``
* Criação de projectos permitida: ``workers``

**Por Identificador de Utilizador**

Pode mesmo utilizar uma solução alternativa para especificar funções do LibrePlan directamente aos utilizadores
sem ter um atributo em cada utilizador LDAP.

Neste caso, especificará quais os utilizadores que têm as diferentes funções do LibrePlan
por ``uid``.

A configuração para este caso é a seguinte:

* Estratégia de pesquisa de funções: ``Property strategy``
* Caminho do grupo:
* Propriedade da função: ``uid``
* Consulta de pesquisa de funções: ``[USER_ID]``

E, por exemplo, se pretender fazer corresponder algumas funções:

* Administração: ``admin1;it1``
* Leitor de serviço web: ``it1;it2``
* Escritor de serviço web: ``it1;it2``
* Leitura de todos os projectos permitida: ``admin1``
* Edição de todos os projectos permitida: ``admin1``
* Criação de projectos permitida: ``worker1;worker2;worker3``

Correspondência de Funções
--------------------------

Na parte inferior desta secção, existe uma tabela com todas as funções do LibrePlan
e um campo de texto junto a cada uma. Destina-se à correspondência de funções. Por exemplo,
se um administrador decidir que a função *Administração* do LibrePlan corresponde
às funções *admin* e *administrators* do LDAP, o campo de texto deverá conter:
"``admin;administrators``". O carácter para separar as funções é "``;``".

.. NOTE::

   Se pretender especificar que todos os utilizadores ou todos os grupos têm uma permissão, pode
   utilizar um asterisco (``*``) como carácter universal para se referir a eles. Por exemplo, se
   pretender que todos tenham a função *Criação de projectos permitida*, configurará
   a correspondência de funções da seguinte forma:

   * Criação de projectos permitida: ``*``
