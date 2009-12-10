#
# @(#)GNUmakefile	1.7 06/10/30
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

PKGNAME=SUNWjhdoc
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

DDOC=$(DJH)/doc

Ddocs=$(DDOC)/jhug $(DDOC)/jhug/overview $(DDOC)/jhug/author \
      $(DDOC)/jhug/images $(DDOC)/jhug/images/overview $(DDOC)/jhug/dev \
      $(DDOC)/jhug/merge $(DDOC)/jhug/locale $(DDOC)/jhug/release \
      $(DDOC)/jhug/JavaHelpSearch $(DDOC)/api $(DDOC)/css

Ddocimages=$(DDOC)/images

Dspecs=$(DDOC)/spec

TOP=../..
JD=$(TOP)/javahelp
DOCD=$(TOP)/doc
PKGTOP=..

ifeq ($(JHUGDIR),)
JHUGDIR=$(TOP)/doc/jhug
endif
JHUG=jhug.pdf

CP=/bin/cp -p -f
RM=/bin/rm -f
TAR=/usr/sbin/tar
MV=/bin/mv -f
JAR=jar

CPDIR=/bin/cp -pr -f

all: dirs spec docs package
	pkgmk -o -d $(PWD) -r $(PWD) -f $(PWD)/prototype basedir=$(PWD)

dirs: $(J2SE) $(OPT) $(DJH) $(DDOC)

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
# JavaHelp Specification 
#
spec: $(Dspecs) 
	mkdir -m 755 -p $(DDOC)/spec
	cp $(DOCD)/public-spec/V2.0/JavaHelp_V2_0_Specification.pdf $(DDOC)/spec
	chmod -R u+w $(DDOC)/spec	

#
# Generic Documentation
#
docs: $(Ddocs) docimages $(JHUGDIR)/$(JHUG)
	cd $(DOCD) ; \
	$(TAR) cFFf - jhug | (cd $(PWD)/$(DDOC) ; $(TAR) xf - )
	$(MV) $(DDOC)/jhug/$(JHUG) $(DDOC)/$(JHUG)
	chmod u+w $(DDOC)/$(JHUG) 
	chmod -R u+w $(DDOC)/jhug
	chmod g-w $(DDOC)/jhug/JavaHelpSearch/*
	$(RM) $(DDOC)/jhug/GNUmakefile $(DDOC)/jhug/createps.sh
	$(CPDIR) $(DOCD)/api/* $(DDOC)/api/
	chmod -R g-w $(DDOC)/api
	$(CP) $(DOCD)/css/default.css $(DDOC)/css/default.css
	chmod u+w $(DDOC)/css/default.css

docimages: $(Ddocimages)
	cd $(DOCD) ; \
	$(TAR) cFFf - images | (cd $(PWD)/$(DDOC) ; $(TAR) xf - )
	chmod -R u+w $(DDOC)/images

#
# Directory creation
#
$(J2SE) $(OPT) $(DJH) $(DDOC) $(Ddocs) $(Ddocimages) $(Dspecs) $(PKGPATH):
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

