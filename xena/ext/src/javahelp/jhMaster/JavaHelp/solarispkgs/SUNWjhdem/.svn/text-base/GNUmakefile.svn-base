#
# @(#)GNUmakefile	1.11 06/10/30
# 
# Copyright (c) 2006 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
# 
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the LICENSE file that accompanied this code.
# 
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
# 
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
# 
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#

# Install for JavaHelp.
# This is a Solaris GNUmakefile

PKGNAME=SUNWjhdem
J2SE=jdk
#OPT=j2se/opt
OPT=$(J2SE)/packages
#DJH=j2se/opt/javahelp
DJH=$(OPT)/javax.help-2.0
INSTALL=$(PKGNAME)/install
RELOC=$(PKGNAME)/reloc
PWD=$(shell pwd)
OWNERGROUP=$(shell id | cut -f2  -d'(' | cut -f1 -d')') $(shell groups | cut -f1 -d' ')
PKGPATH=$(PWD)/$(PKGNAME)

Dbin=$(DJH)/bin

Ddemos=$(DJH)/demos $(DJH)/demos/bin $(DJH)/demos/hsjar \
       $(DJH)/demos/lib $(DJH)/demos/hs \
       $(DJH)/demos/hs/merge $(DJH)/demos/hs/newmerge\
       $(DJH)/demos/browser

Ddemosrc=$(DJH)/demos/src/sunw/demo/jhdemo \
	 $(DJH)/demos/src/sunw/demo/idedemo \
         $(DJH)/demos/src/sunw/demo/classviewer \
         $(DJH)/demos/src/sunw/demo/classviewer/plaf/basic \
         $(DJH)/demos/src/sunw/demo/classviewer/plaf/metal \
         $(DJH)/demos/src/sunw/demo/classviewer/plaf/motif \
         $(DJH)/demos/src/sunw/demo/classviewer/plaf/windows \
         $(DJH)/demos/src/sunw/demo/searchdemo \
         $(DJH)/demos/src/sunw/demo/merge \
         $(DJH)/demos/src/sunw/demo/newmerge \
         $(DJH)/demos/src/sunw/demo/browser

TOP=../..
DD=$(TOP)/demos
JD=$(TOP)/javahelp
PKGTOP=..

CP=/bin/cp -p -f
RM=/bin/rm -f
TAR=/usr/sbin/tar
MV=/bin/mv -f
JAR=jar

CPDIR=/bin/cp -pr -f

all: dirs helpset demo demosrc package
	pkgmk -o -d $(PWD) -r $(PWD) -f $(PWD)/prototype basedir=$(PWD)

dirs: $(J2SE) $(OPT) $(DJH)

package: $(PKGPATH) pkginfo proto

pkginfo:
	sed s/\<REV\>/\,REV\=`date +%Y.%m.%d`/ < pkginfo.base > pkginfo

proto:
	chmod -R g-s $(J2SE)
	chmod -R o+r $(J2SE)
	pkgproto $(J2SE) > prototype.tmp
	sed s/"$(OWNERGROUP)"/"root bin"/ prototype.tmp > prototype.build
	$(RM) prototype.tmp
	if [ -f prototype.base ]; then \
		cat prototype.base prototype.build > prototype; \
		$(RM) prototype.build; \
	else \
		$(MV) prototype.build prototype; \
	fi; \


#
# HelpSets
#

helpset: $(Ddemos)
	$(CP) $(DD)/hsjar/holidays.jar $(DJH)/demos/hsjar/holidays.jar
	chmod 644 $(DJH)/demos/hsjar/holidays.jar
	$(CP) $(DD)/hsjar/animals.jar $(DJH)/demos/hsjar/animals.jar
	chmod 644 $(DJH)/demos/hsjar/animals.jar
	$(CP) $(DD)/hsjar/invertebrates.jar $(DJH)/demos/hsjar/invertebrates.jar
	chmod 644 $(DJH)/demos/hsjar/invertebrates.jar
	$(CP) $(DD)/hsjar/vertebrates.jar $(DJH)/demos/hsjar/vertebrates.jar
	chmod 644 $(DJH)/demos/hsjar/vertebrates.jar
	$(CP) $(DD)/hsjar/apidoc.jar $(DJH)/demos/hsjar/apidoc.jar
	chmod 644 $(DJH)/demos/hsjar/apidoc.jar
	$(CP) $(DD)/hsjar/idehelp.jar $(DJH)/demos/hsjar/idehelp.jar
	chmod 644 $(DJH)/demos/hsjar/idehelp.jar
	$(CP) $(DD)/hsjar/object.jar $(DJH)/demos/hsjar/object.jar
	chmod 644 $(DJH)/demos/hsjar/object.jar
	$(CP) $(DD)/hsjar/idehelp_de.jar $(DJH)/demos/hsjar/idehelp_de.jar
	chmod 644 $(DJH)/demos/hsjar/idehelp_de.jar
	$(CP) $(DD)/hsjar/idehelp_en.jar $(DJH)/demos/hsjar/idehelp_en.jar
	chmod 644 $(DJH)/demos/hsjar/idehelp_en.jar
	$(CP) $(DD)/hsjar/idehelp_ja.jar $(DJH)/demos/hsjar/idehelp_ja.jar
	chmod 644 $(DJH)/demos/hsjar/idehelp_ja.jar
	mkdir -m 755 -p $(DJH)/demos/hs ; \
	( cd $(DD)/hs ; echo merge/GNUmakefile > exclude.tmp ) ; \
	( cd $(DD)/hs ; $(TAR) cFFXf exclude.tmp - merge ) | (cd $(DJH)/demos/hs ; $(TAR) xf - ) ; \
	chmod -R u+w $(DJH)/demos/hs/merge
	rm $(DD)/hs/exclude.tmp
	( cd $(DD)/hs ; echo newmerge/GNUmakefile > exclude.tmp ) ; \
	( cd $(DD)/hs ; $(TAR) cFFXf exclude.tmp - newmerge ) | (cd $(DJH)/demos/hs ; $(TAR) xf - ) ; \
	chmod -R u+w $(DJH)/demos/hs/newmerge
	rm $(DD)/hs/exclude.tmp
	chmod -R g-w $(DJH)/demos/hs

#
# Demos
#

demo: $(Ddemos)
	$(CP) $(DD)/idedemo/idedemo.jar $(DJH)/demos/bin/idedemo.jar
	chmod 644 $(DJH)/demos/bin/idedemo.jar

	$(CP) $(DD)/merge/merge.jar $(DJH)/demos/bin/merge.jar
	chmod 644 $(DJH)/demos/bin/merge.jar

	$(CP) $(DD)/newmerge/newmerge.jar $(DJH)/demos/bin/newmerge.jar
	chmod 644 $(DJH)/demos/bin/newmerge.jar

	$(CP) $(DD)/bin/apiviewer.jar $(DJH)/demos/bin/apiviewer.jar
	chmod 644 $(DJH)/demos/bin/apiviewer.jar

	$(CP) $(DD)/bin/object.jar $(DJH)/demos/bin/object.jar
	chmod 644 $(DJH)/demos/bin/object.jar

	$(CP) $(DD)/bin/hsviewer.jar $(DJH)/demos/bin/hsviewer.jar
	chmod 644 $(DJH)/demos/bin/hsviewer.jar

	$(CP) $(DD)/bin/UserGuide.jar $(DJH)/demos/bin/UserGuide.jar
	chmod 644 $(DJH)/demos/bin/hsviewer.jar

	$(CP) $(DD)/lib/classviewer.jar $(DJH)/demos/lib/classviewer.jar
	chmod 644 $(DJH)/demos/lib/classviewer.jar
	$(CP) $(DD)/lib/searchdemo.jar $(DJH)/demos/lib/searchdemo.jar
	chmod 644 $(DJH)/demos/lib/searchdemo.jar
	$(CP) $(DD)/lib/browserdemo.jar $(DJH)/demos/browser/browserdemo.jar
	chmod 644 $(DJH)/demos/browser/browserdemo.jar
	$(CP) $(DD)/newbrowser/*.html $(DJH)/demos/browser/
	$(CP) $(DD)/newbrowser/demo_instructions $(DJH)/demos/browser/
	chmod -R u+w $(DJH)/demos/browser

#
# Demo sources
#

demosrc: $(Ddemosrc) 
	$(CP) $(DD)/jhdemo/sunw/demo/jhdemo/JHLauncher.java $(DJH)/demos/src/sunw/demo/jhdemo/
	$(CP) $(DD)/jhdemo/sunw/demo/jhdemo/Runner.java $(DJH)/demos/src/sunw/demo/jhdemo/
	$(CP) $(DD)/jhdemo/sunw/demo/jhdemo/ElementTreePanel.java $(DJH)/demos/src/sunw/demo/jhdemo/
	$(CP) $(DD)/README.install $(DJH)/demos/README
	chmod 644 $(DJH)/demos/README
	$(CP) $(DD)/idedemo/sunw/demo/idedemo/ApiDemo.java $(DJH)/demos/src/sunw/demo/idedemo/
	$(CP) $(DD)/idedemo/sunw/demo/idedemo/IdeDemo.properties $(DJH)/demos/src/sunw/demo/idedemo/
	here=`pwd` ; \
	  cd  $(DJH)/demos/src/sunw/demo/idedemo ; \
	  $(JAR) -xf $$here/$(DD)/idedemo/images.jar
	$(CP) $(DD)/searchdemo/sunw/demo/searchdemo/ClientSearchEngine.java $(DJH)/demos/src/sunw/demo/searchdemo/
	$(CP) $(DD)/searchdemo/sunw/demo/searchdemo/ClientSearchQuery.java $(DJH)/demos/src/sunw/demo/searchdemo/
	$(CP) $(DD)/apidoc/sunw/demo/classviewer/ClassViewerView.java $(DJH)/demos/src/sunw/demo/classviewer/
	$(CP) $(DD)/apidoc/sunw/demo/classviewer/ClassViewerNavigator.java $(DJH)/demos/src/sunw/demo/classviewer/
	$(CP) $(DD)/apidoc/sunw/demo/classviewer/plaf/basic/BasicClassViewerNavigatorUI.java $(DJH)/demos/src/sunw/demo/classviewer/plaf/basic/
	$(CP) $(DD)/apidoc/sunw/demo/classviewer/plaf/metal/MetalClassViewerNavigatorUI.java $(DJH)/demos/src/sunw/demo/classviewer/plaf/metal/
	$(CP) $(DD)/apidoc/sunw/demo/classviewer/plaf/motif/MotifClassViewerNavigatorUI.java $(DJH)/demos/src/sunw/demo/classviewer/plaf/motif/
	$(CP) $(DD)/apidoc/sunw/demo/classviewer/plaf/windows/WindowsClassViewerNavigatorUI.java $(DJH)/demos/src/sunw/demo/classviewer/plaf/windows/
	$(CP) $(DD)/merge/sunw/demo/merge/Merge.java $(DJH)/demos/src/sunw/demo/merge/
	$(CP) $(DD)/newmerge/sunw/demo/newmerge/Merge.java $(DJH)/demos/src/sunw/demo/newmerge/
	$(CP) $(DD)/newmerge/sunw/demo/newmerge/HSFilter.java $(DJH)/demos/src/sunw/demo/newmerge/
	$(CP) $(DD)/newmerge/sunw/demo/newmerge/JarAccessory.java $(DJH)/demos/src/sunw/demo/newmerge/
	$(CP) $(DD)/newbrowser/sunw/demo/browser/HelpButton.java $(DJH)/demos/src/sunw/demo/browser/
	chmod -R u+w $(DJH)/demos/src
	chmod -R g-w $(DJH)/demos/src

#
# Directory creation
#
$(J2SE) $(OPT) $(DJH) $(Ddemos) $(Ddemosrc) $(Dbin) $(PKGPATH):
	mkdir -m 755 -p $@
	chmod 755 $@

#
# Cleanup
#
clean: clobber

clobber:
	$(RM) -rf $(J2SE)
	$(RM) -rf $(PKGNAME)
	$(RM) -rf prototype
	$(RM) -rf pkginfo

