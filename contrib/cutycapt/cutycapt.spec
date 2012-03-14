Name:           cutycapt
Version:        20110107
Release:        2
Summary:        Captures web pages to files in different formats
License:        GPLv2+
URL:            http://cutycapt.sourceforge.net/
Source0:        http://cutycapt.svn.sourceforge.net/viewvc/cutycapt/CutyCapt.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root

BuildRequires:  qt-devel
%if 0%{?suse_version}
BuildRequires:  libQtWebKit-devel
%else
%if 0%{?fedora} == 15
BuildRequires:  qt-webkit-devel
%else
BuildRequires:  qtwebkit-devel
%endif
%endif
BuildRequires:  gcc-c++

%description
CutyCapt is a small cross-platform command-line utility to capture
WebKit's rendering of a web page into a variety of vector and bitmap
formats, including SVG, PDF, PS, PNG, JPEG, TIFF, GIF, and BMP. See
IECapt for a similar tool based on Internet Explorer.

%prep
%setup -q -n CutyCapt

%build
%if 0%{?suse_version}
qmake
%else
qmake-qt4
%endif
make

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT
install -m 755 -d       $RPM_BUILD_ROOT/usr/bin
install -m 755 CutyCapt $RPM_BUILD_ROOT/usr/bin/cutycapt

%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,root,root,-)
%{_bindir}/cutycapt


%changelog
* Fri Feb 17 2012 Juan A. Suarez Romero <jasuarez@gladiator> - 20110107-1
- Initial build.
