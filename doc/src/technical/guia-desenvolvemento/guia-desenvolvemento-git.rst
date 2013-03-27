Guia de uso de GIT no proxecto de Xestión da Producción
#######################################################

Para a utilización do sistema de xestión da configuración distribuida GIT e recomendable instalar os seguinte paquetes nas distribicións Debian GNU/Linux e Ubuntu.

::

 $sudo apt-get install git-core git-email giggle

Esta guía detalla o uso que deberá realizar o desenvolvedor que quere participar no proxecto a hora de remitir os parches e como organizarse para traballar, tamén se detalla a forma de traballar do integrador dos parches que será de utilidades para quen quera incorporar parches remitidos por outros desenvolvedores na súa versión do código fonte.

Perfil do desenvolvedor
=======================

O desenvolvedor traballará nun repositorio git local e non ten dereito de escritura sobre o repositorio git central do proxecto. No repositorio de referencia so poderá aplicar parches os integradores do proxecto. Unha vez que un desenvolvedor implemente unha funcionalidade no software deberá crear un parche e envialo a lista de desenvolvemento do proxecto. Para a súa revisión e subida a rama principal do proxecto (*master*).

Para comezar a traballar o primerio que terá que facer o desenvolvedor e descargar o repositorio GIT que se atopa accesible de forma anónima por http mediante o seguinte comando:

::

 $ git clone git://github.com/Igalia/libreplan.git


Esto creará unha copia local de trabajo, para empezar a trabajar se recomienda la creación de una rama para cada caso de uso a desarrollar o nombre de la rama é recomendable que teña realación coa funcionalidade que implemente ou o número de bug que trata de solucionar, e logo nos movemos a traballar na rama cun checkout.

::

 $ git branch NomeDaRama
 $ git checkout NomeDaRama

Unha vez dentro da rama, a medida que se vai avanzando coa implementación ou ao terminar coa implementación faise o commit do traballo feito. Debe terse en conta que as modificacións deben ter unha unidade de obxectivo e funcións.

::

 $ git add <ficheiros novos e modificados>
 $ git commit


E importate ter o habito de crear mensaxes de commit de calidad xa que permite facilitar o traballo aos intergradores. Como regla xleneral as mensaxes deben comezar cunha liña de non mais de 50 caracteres que describe o cambio de forma concisa, esta debe estar seguida dunha liña en blanco, seguida dunha explicación detallada. E recomendabe que a explicación detallada inclua a motivación do campo e contraste a implementación coa anterior comportamento. Outra recomedación e empregar o tempso verbais en presente. O emprego de puntos e secuencias é correcto.

A primeira liña da mensaxe dará lugar al nombre del fichero del parche.

Previamente a enviar o parche ao integrador, debemos comprobar o seu funcionamento coa última versión no upstream do master, polo que teremos que actualizar a última copia.

::

 $ git checkout master
 $ git pull

Neste momento coa última versión do master pasamos a facer un checkout a rama para facer un rebase.

::

 git checkout NomeDaRamaFEA
 git rebase master

Aquí temos un exemplo da mezcla e como aparece un conflicto:

::

 chema@rohan:~/git/xestion-producion$ git status
 # On branch master
 nothing to commit (working directory clean)
 chema@rohan:~/git/xestion-producion$ git co NomeDaRamaFEA
 Switched to branch "NomeDaRamaFEA"
 chema@rohan:~/git/xestion-producion$ git rebase master
 First, rewinding head to replay your work on top of it...
 Applying: Nova version sobre os conflictos
 error: patch failed: HOWTO.GIT:1
 error: HOWTO.GIT: patch does not apply
 Using index info to reconstruct a base tree...
 Falling back to patching base and 3-way merge...
 Auto-merged HOWTO.GIT
 CONFLICT (content): Merge conflict in HOWTO.GIT
 Failed to merge in the changes.
 Patch failed at 0001.

 When you have resolved this problem run "git rebase --continue".
 If you would prefer to skip this patch, instead run "git rebase --skip".
 To restore the original branch and stop rebasing run "git rebase --abort".

 chema@rohan:~/git/xestion-producion$ git rebase --continue
 You must edit all merge conflicts and then
 mark them as resolved using git add


Como podese ver para solucionar os conflictos  e preciso revisar os ficheiros que teñen conflicto e unha vez solucionado faise un add cos conflictos solucionados. E logo continuar co git rebase --continue , se é mellor abortar podese empregar o git rebase --abort.

Existe a opción de facer un git rebase -i que permite a realización do rebase de forma interactiva permitindo fusionar distintos commits en un ou otras operacións.

Para crear o parche unha vez rematado o rebase procedemos da seguinte forma, empregando o gomando git format-patch:

::

 $ git format-patch master


Este comando crea os ficheiros .patch un por cada un dos commits.

Está e unha sintaxe máis completa para a creación dos parches do commits da rama en curso. O parametro --cover-letter crea unha portada coma os dos faxes que pode ser logo utilizada no envío dos mails unha vez editada, este comando só funciona a partires da versión 1.5 de git. Este comando xerará un directorio patches no que creará os emails para formatear

::

 $ git format-patch -n --attach --thread --cover-letter -o patches/ master


Para facilitar o envío de parches por email e recomendable instalar o paquete git-email

::

 $ apt-get install git-email


Para configurar os comandos no arquivo .git/config coa información do seu proveedor a internet.

::

 [user]
	email = direccion@desenvolvedor.com
	name = Nome Desenvolvedor
 [sendemail]
	to = xestion-producion-patches@igalia.com
	smtpserver = <smtp-servidor>
	smtpencryption = tls
	smtpserverport = 587
	smtpuser = usuario
	smtppass = <a contraseña do smtp>


Para enviar os parches xerados que se atopan nun directorio previamente xerados co comando format-patch podemos utilizar:

::

 $ git send-email patches/


que enviará por email tódolos parches a lista definida que é o to definido por defecto.

Se queremos enviar un parche a outro desenvolvedor en particular empregaremos o parámetro --to:

::

 $ git send-email --to desenvolvedor2@email.com patches/


O desenvolvedor poderá borrar a rama cando o integrador lle confirme que o parche foi aplicado correctamente.

O perfil de integrador
======================

A diferencia do resto dos usuarios debería descargar o repositorio da seguinte forma, tendo os permisos adecuados no repositorio:

::

 $ git clone git@github.com:Igalia/libreplan.git


Esta forma de acceso permite un acceso de escritura ao repositorio:

Sobre o repositorio debería facer o seguinte para aplicar un parche, primeiro deberá descargar os parches a un ficheiro con formato mailbox co email completo enviado polo desenvolvedor.

O recomendable sería crear unha nova rama para probar o parche:

::

 $ git branch test-patch-xxxx
 $ git checkout test-patch-xxxx
 $ git am ficheiro.mbox

Se se quere aplicar un parche en formato .patch simplemente tense que executar o seguinte comando,

::

 $ git apply ficheiro.patch


A diferencia de git am, git apply non manten a identidade do commiter orixinal, polo que e recomendable empregar o git am para a importación dos commits.

Se o parche funciona debería procederse a aplicalo no master. E borraríase a rama temporal de aplicación. E se publicaría cun push.

::

 $ git rebase master
 $ git checkout master
 $ git merge test-patch-xxxx
 $ git branch -d test-patch-xxxx
 $ git push

Logo o integrador deberá responder a lista confirmando a aplicación do parche.

Trucos con GIT
==============

Cando estase resolvendo conflictos podense ver os distintos pais do merge. Empregar *git show :1:filename* para ver o ancestro comun, *git show :2:filename* para ver a version local, *git show :3:filename* para ver a version remota. Para escoller unha das versions empregar *git checkout --ours file* e *git checkout --theirs file*. (Solo en git 1.6.1 e posteriores)

Podese usar un *git mergetool* para resolver os conflictos empregando unha ferramenta visual.
