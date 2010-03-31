#--------------------------------------------------------------------
# RPM Spec file for JSpeex
# $Id$
#--------------------------------------------------------------------

%define name     jspeex
%define version  @VERSION@
%define release  1

Name:           %{name}
Version:        %{version}
Release:        %{release}
Epoch:          0
Summary:        100% Java Speex encoder/decoder/converter library
License:        BSD
Url:            http://jspeex.sourceforge.net/
Group:          Development/Libraries/Java
Vendor:         JSpeex
Source0:        http://switch.dl.sourceforge.net/sourceforge/jspeex/%{name}-%{version}.zip
BuildRequires:  ant
BuildRequires:  jpackage-utils >= 0:1.5
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-buildroot

%package javadoc
Summary:  Javadoc for %{name}
Group:    Development/Documentation

#--------------------------------------------------------------------
# Description
#--------------------------------------------------------------------
%description
JSpeex is a Java port of the Speex speech codec
(Open Source/Free Software patent-free audio compression format designed for speech).
It provides both the decoder and the encoder in pure Java, as well as a JavaSound SPI.

%description javadoc
Javadoc for %{name}.

#--------------------------------------------------------------------
# Build Preparations
#--------------------------------------------------------------------
%prep
rm -rf $RPM_BUILD_ROOT
%setup -q -c
find . -name "jspeex*.jar" -exec rm -f {} \;
find . -name "*.class" -exec rm -f {} \;

#--------------------------------------------------------------------
# Source Build
#--------------------------------------------------------------------
%build
cd jspeex
ant package
ant javadoc
cd ..

#--------------------------------------------------------------------
# Source Install
#--------------------------------------------------------------------
%install
# jar
install -d -m 755 $RPM_BUILD_ROOT%{_javadir}
install -m 644 %{name}/dist/%{name}.jar \
$RPM_BUILD_ROOT%{_javadir}/%{name}-%{version}.jar
(cd $RPM_BUILD_ROOT%{_javadir} && for jar in *-%{version}*; do \
ln -sf ${jar} ${jar/-%{version}/}; done)

# javadoc
install -d -m 755 $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}
cp -pr jspeex/doc/* $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}

#--------------------------------------------------------------------
# Post Build Cleaning
#--------------------------------------------------------------------
%clean
rm -rf $RPM_BUILD_ROOT

#--------------------------------------------------------------------
# Post-install Scripts
#--------------------------------------------------------------------
%post javadoc
rm -f %{_javadocdir}/%{name}
ln -s %{name}-%{version} %{_javadocdir}/%{name}

#--------------------------------------------------------------------
# Post-un-install Scripts
#--------------------------------------------------------------------
%postun javadoc
if [ "$1" = "0" ]; then
    rm -f %{_javadocdir}/%{name}
fi

#--------------------------------------------------------------------
# Package File List
#--------------------------------------------------------------------
%files
%defattr(0644,root,root,0755)
%doc jspeex/TODO jspeex/License.txt jspeex/README
%{_javadir}/*

%files javadoc
%defattr(0644,root,root,0755)
%{_javadocdir}/%{name}-%{version}

#--------------------------------------------------------------------
# Changelogs
#--------------------------------------------------------------------
%changelog
* Tue Aug 5 2003 Marc Gimpel <mgimpel@horizonwimba.com> 0.9.5-1
- bug fixes
* Tue Aug 5 2003 Marc Gimpel <marc.gimpel@wimba.com> 0.8.1-1
- release
