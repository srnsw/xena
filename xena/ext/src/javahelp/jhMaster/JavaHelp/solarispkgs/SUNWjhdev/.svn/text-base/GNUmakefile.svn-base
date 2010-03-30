#
# @(#)GNUmakefile	1.9 06/10/30
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

PKGNAME=SUNWjhdev
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

Dlib=$(DJH)/lib
Ddtd=$(Dlib)/dtd
Dbin=$(DJH)/bin

DTDS=\
	$(Ddtd)/helpset_1_0.dtd \
	$(Ddtd)/map_1_0.dtd \
	$(Ddtd)/toc_1_0.dtd \
	$(Ddtd)/index_1_0.dtd

Dimplsrc=com/sun/java/help/impl/JHSecondaryViewer.java \
	 com/sun/java/help/impl/JHSecondaryViewerBeanInfo.java \
	 com/sun/java/help/impl/ViewAwareComponent.java

DSRC=$(DJH)/src

JD=../../javahelp
TOP=../..
DOCD=$(TOP)/doc
SRCD=$(TOP)/src
PKGTOP=..

CP=/bin/cp -p -f
RM=/bin/rm -f
TAR=/usr/sbin/tar
MV=/bin/mv -f
JAR=jar

CPDIR=/bin/cp -pr -f

all: dirs libs bins source package
	pkgmk -o -d $(PWD) -r $(PWD) -f $(PWD)/prototype basedir=$(PWD)

dirs: $(J2SE) $(OPT) $(DJH)

package: $(PKGPATH) pkginfo proto

pkginfo:
	sed s/\<REV\>/\,REV\=`date +%Y.%m.%d`/ < pkginfo.base > pkginfo

proto:
	chmod -R g-s $(J2SE)
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
# the JavaHelp libraries
#

libs: $(Ddtd) $(DTDS)

$(Ddtd)/%.dtd: $(DOCD)/public-spec/dtd/%.dtd
	$(CP) $< $@
	chmod 644 $@

#
# predefined commands
#

bins: $(Dbin)
	$(CP) $(JD)/bin/jhindexer_solsparc $(Dbin)/jhindexer
	chmod 755 $(Dbin)/jhindexer
	$(CP) $(JD)/bin/jhsearch_solsparc $(Dbin)/jhsearch
	chmod 755 $(Dbin)/jhsearch

#
# source
#
source:
	mkdir -p $(DSRC)
	$(RM) -rf $(DSRC)/*
	cd $(SRCD)/new ; \
	$(TAR) cFFf - . | (cd $(PWD)/$(DSRC) ; $(TAR) xf - )
	cd $(SRCD)/impl; \
	$(TAR) cf - $(Dimplsrc) | (cd $(PWD)/$(DSRC) ; $(TAR) xf - )
	cd $(PWD)/$(DSRC); jar -cMvf $(PWD)/$(DJH)/src.jar .
	chmod g-w $(DJH)/src.jar
	$(RM) -rf $(DSRC);
	chmod 755 $(Dlib);

#
# Directory creation
#
$(J2SE) $(OPT) $(DJH) $(Dlib) $(Ddtd) $(Dbin) $(PKGPATH):
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

