Configuração LDAP
#################

.. contents::

Esta tela permite estabelecer uma conexão com o LDAP para delegar
a autenticação e/ou autorização.

Está dividida em quatro áreas diferentes, que são explicadas abaixo:

Ativação
========

Esta área é usada para definir as propriedades que determinam como o *LibrePlan* usa
o LDAP.

Se o campo *Ativar autenticação LDAP* estiver marcado, o *LibrePlan* consultará
o LDAP sempre que um usuário tentar fazer login na aplicação.

O campo *Usar funções LDAP* marcado significa que é estabelecido um mapeamento entre as funções LDAP e as
funções do LibrePlan. Consequentemente, as permissões de um usuário no
LibrePlan dependerão das funções que o usuário possui no LDAP.

Configuração
============

Esta seção contém os valores dos parâmetros para acessar o LDAP. *Base*, *UserDN* e
*Password* são parâmetros usados para conectar ao LDAP e pesquisar usuários. Portanto,
o usuário especificado deve ter permissão para realizar esta operação no LDAP. Na
parte inferior desta seção, há um botão para verificar se é possível estabelecer uma conexão LDAP
com os parâmetros fornecidos. É aconselhável testar a conexão antes de
continuar a configuração.

.. NOTE::

   Se o seu LDAP estiver configurado para funcionar com autenticação anônima, você pode
   deixar os atributos *UserDN* e *Password* em branco.

.. TIP::

   Em relação à configuração do *Active Directory (AD)*, o campo *Base* deve ser a
   localização exata onde o usuário vinculado reside no AD.

   Exemplo: ``ou=organizational_unit,dc=example,dc=org``

Autenticação
============

Aqui, você pode configurar a propriedade nos nós LDAP onde o nome de usuário fornecido
deverá ser encontrado. A propriedade *UserId* deve ser preenchida com o nome da
propriedade onde o nome de usuário está armazenado no LDAP.

A caixa de seleção *Salvar senhas no banco de dados*, quando marcada, significa que a
senha também é armazenada no banco de dados do LibrePlan. Dessa forma, se o LDAP estiver
offline ou inacessível, os usuários LDAP podem se autenticar no banco de dados do LibrePlan.
Se não estiver marcada, os usuários LDAP só podem ser autenticados no LDAP.

Autorização
===========

Esta seção permite definir uma estratégia para combinar as funções LDAP com as
funções do LibrePlan. A primeira escolha é a estratégia a ser usada, dependendo da
implementação do LDAP.

Estratégia de Grupo
-------------------

Quando esta estratégia é usada, indica que o LDAP tem uma estratégia de grupos de funções.
Isso significa que os usuários no LDAP são nós que estão diretamente sob um ramo que
representa o grupo.

O exemplo a seguir representa uma estrutura LDAP válida para usar a estratégia de grupo.

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
com a lista de usuários pertencentes ao grupo:

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

E, por exemplo, se você quiser combinar algumas funções:

* Administração: ``cn=admins;cn=itpeople``
* Leitor de serviço web: ``cn=itpeople``
* Escritor de serviço web: ``cn=itpeople``
* Leitura de todos os projetos permitida: ``cn=admins``
* Edição de todos os projetos permitida: ``cn=admins``
* Criação de projetos permitida: ``cn=workers``

Estratégia de Propriedade
-------------------------

Quando um administrador decide usar esta estratégia, indica que cada usuário
é um nó LDAP e, dentro do nó, existe uma propriedade que representa
o(s) grupo(s) do usuário. Neste caso, a configuração não requer o
parâmetro *Caminho do grupo*.

O exemplo a seguir representa uma estrutura LDAP válida para usar a estratégia de propriedade.

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

Neste caso, cada usuário terá um atributo, por exemplo, chamado ``group``,
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

   Esta estratégia tem uma restrição: cada usuário só pode pertencer a um grupo.

A configuração para este caso é a seguinte:

* Estratégia de pesquisa de funções: ``Property strategy``
* Caminho do grupo:
* Propriedade da função: ``group``
* Consulta de pesquisa de funções: ``[USER_ID]``

E, por exemplo, se você quiser combinar algumas funções:

* Administração: ``admins;itpeople``
* Leitor de serviço web: ``itpeople``
* Escritor de serviço web: ``itpeople``
* Leitura de todos os projetos permitida: ``admins``
* Edição de todos os projetos permitida: ``admins``
* Criação de projetos permitida: ``workers``

**Por Identificador de Usuário**

Você pode até usar uma solução alternativa para especificar funções do LibrePlan diretamente aos usuários
sem ter um atributo em cada usuário LDAP.

Neste caso, você especificará quais usuários têm as diferentes funções do LibrePlan
por ``uid``.

A configuração para este caso é a seguinte:

* Estratégia de pesquisa de funções: ``Property strategy``
* Caminho do grupo:
* Propriedade da função: ``uid``
* Consulta de pesquisa de funções: ``[USER_ID]``

E, por exemplo, se você quiser combinar algumas funções:

* Administração: ``admin1;it1``
* Leitor de serviço web: ``it1;it2``
* Escritor de serviço web: ``it1;it2``
* Leitura de todos os projetos permitida: ``admin1``
* Edição de todos os projetos permitida: ``admin1``
* Criação de projetos permitida: ``worker1;worker2;worker3``

Correspondência de Funções
--------------------------

Na parte inferior desta seção, há uma tabela com todas as funções do LibrePlan
e um campo de texto ao lado de cada uma. Isso é para a correspondência de funções. Por exemplo,
se um administrador decidir que a função *Administração* do LibrePlan corresponde
às funções *admin* e *administrators* do LDAP, o campo de texto deve conter:
"``admin;administrators``". O caractere para separar as funções é "``;``".

.. NOTE::

   Se você quiser especificar que todos os usuários ou todos os grupos têm uma permissão, você
   pode usar um asterisco (``*``) como curinga para se referir a eles. Por exemplo, se
   você quiser que todos tenham a função *Criação de projetos permitida*, você configurará
   a correspondência de funções da seguinte forma:

   * Criação de projetos permitida: ``*``
