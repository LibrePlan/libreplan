Name:           libreplan
Version:        1.3.1
Release:        1
Summary:        Web application for project planning, monitoring and control
License:        AGPLv3

Source0:        http://downloads.sourceforge.net/project/libreplan/LibrePlan/%{name}_%{version}.tar.gz
Source1:        http://downloads.sourceforge.net/project/libreplan/LibrePlan/%{name}_%{version}.war

BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch

#BuildRequires:  java-1.6.0-openjdk
#BuildRequires:  maven2
#BuildRequires:	maven2-common-poms
#BuildRequires:  python-docutils
#BuildRequires:	javamail
#BuildRequires:	javacc
#BuildRequires:	ant
#BuildRequires:	jpackage-utils
#BuildRequires:  postgresql-server

%if 0%{?fedora} || 0%{?centos}
Requires:       postgresql-jdbc
%endif
Requires:       java-1.6.0-openjdk
Requires:       postgresql
Requires:       postgresql-server
Requires:       tomcat6
%if 0%{?centos}
%else
Requires:       cutycapt
%endif

%if 0%{?suse_version}
%define distro openSUSE
%else
%define distro Fedora
%endif

%description
LibrePlan is a collaborative tool to plan, monitor and control projects and has
a rich web interface which provides a desktop alike user experience. All the
team members can take part in the planning and this makes possible to have a
real-time planning.

It was designed thinking on a scenario where multiple projects and resources
interact to carry out the work inside a company. Besides, it makes possible
the communication with other company tools providing a wide set of web
services to import and export data.

See README.%{distro} for more information.

%prep
#export CURDIR=${RPM_BUILD_DIR}/%{name}
%setup -q -n %{name}

%build
#export CURDIR=${RPM_BUILD_DIR}/%{name}
#mkdir -p ${CURDIR}/debian/maven-repo
#mvn2 -e -Pprod,postgresql,-liquibase-update -DdataSource.jndiName=java:comp/env/jdbc/libreplan-ds -Dmaven.test.skip=true -B -s ${CURDIR}/debian/maven-settings.xml install

%install
export CURDIR=${RPM_BUILD_DIR}/%{name}
rm -fr ${RPM_BUILD_ROOT}

# Commands to install arch-dependant stuff
mkdir -p ${RPM_BUILD_ROOT}%{_datadir}/%{name}/webapps/
#cp ${CURDIR}/libreplan-webapp/target/libreplan-webapp.war ${RPM_BUILD_ROOT}%{_datadir}/%{name}/webapps/libreplan.war
install -Dm0644 %{SOURCE1} ${RPM_BUILD_ROOT}%{_datadir}/%{name}/webapps/libreplan.war

# Copy SQL installation files
mkdir -p ${RPM_BUILD_ROOT}%{_datadir}/%{name}/pgsql/
cp ${CURDIR}/scripts/database/create_db.sql      ${RPM_BUILD_ROOT}%{_datadir}/%{name}/pgsql/
cp ${CURDIR}/scripts/database/create_user_postgresql.sql    ${RPM_BUILD_ROOT}%{_datadir}/%{name}/pgsql/
cp ${CURDIR}/scripts/database/install.sql       ${RPM_BUILD_ROOT}%{_datadir}/%{name}/pgsql/

# Copy SQL upgrade scripts
cp ${CURDIR}/scripts/database/upgrade_*.sql ${RPM_BUILD_ROOT}%{_datadir}/%{name}/pgsql/

# Install Policy file
#mkdir -p ${RPM_BUILD_ROOT}/etc/tomcat6/policy.d
#cp ${CURDIR}/debian/51libreplan.policy ${RPM_BUILD_ROOT}/etc/tomcat6/policy.d/

# Install Tomcat6 configuration file
mkdir -p ${RPM_BUILD_ROOT}%{_datadir}/%{name}/conf
cp ${CURDIR}/conf/libreplan.xml ${RPM_BUILD_ROOT}%{_datadir}/%{name}/conf/

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(-,root,root,-)
%doc AUTHORS COPYING NEWS README README.%{distro} TODO INSTALL HACKING
%dir %{_datadir}/%{name}
%dir %{_datadir}/%{name}/webapps
%dir %{_datadir}/%{name}/pgsql
%dir %{_datadir}/%{name}/conf
%{_datadir}/%{name}/webapps/*
%{_datadir}/%{name}/pgsql/*
%{_datadir}/%{name}/conf/*

%changelog
* Mon Oct 15 2012 Manuel Rego Casasnovas <rego@igalia.com> - 1.3.1-1
- Released LibrePlan 1.3.1
- Removed dependency with freefont
* Thu Jul 26 2012 Manuel Rego Casasnovas <rego@igalia.com> - 1.3.0-1
- Released LibrePlan 1.3.0
* Wed May 23 2012 Manuel Rego Casasnovas <rego@igalia.com> - 1.2.4-1
- Released LibrePlan 1.2.4
* Wed Apr 18 2012 Jacobo Aragunde Pérez <jaragunde@igalia.com> - 1.2.3-1
- Released LibrePlan 1.2.3
* Thu Mar 15 2012 Manuel Rego Casasnovas <rego@igalia.com> - 1.2.2-1
- Released LibrePlan 1.2.2
* Fri Feb 17 2012 Juan A. Suarez Romero <jasuarez@igalia.com> - 1.2.1-1
- Released LibrePlan 1.2.1
