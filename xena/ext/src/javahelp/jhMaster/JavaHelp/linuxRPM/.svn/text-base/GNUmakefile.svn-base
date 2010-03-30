#
# @(#)GNUmakefile	1.7 06/11/01
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

# @(#)GNUmakefile	1.7 06/11/01
# Install for JavaHelp.
# This is a linux RPM GNUmakefile

JH_NAME=javahelp
JH_VERSION=2.0
TEMP_DIR=/tmp/rpm-$(JH_NAME)
NATIVE_ARCH=i586
JH_VERSION=2.0
PKGNAME=SUNWjhrt
J2SE=j2se
OPT=j2se/opt
DJH=j2se/opt/javahelp
INSTALL=$(PKGNAME)/install
RELOC=$(PKGNAME)/reloc
PWD=$(shell pwd)
OWNERGROUP=$(shell id | cut -f2  -d'(' | cut -f1 -d')') $(shell groups | cut -f1 -d' ')
PKGPATH=$(PWD)/$(PKGNAME)

JD=../$(JH_NAME)
TOP=..
PKGTOP=.

CAT=/bin/cat
CD=cd
CHMOD=/bin/chmod
CP=/bin/cp -p -f
ECHO=echo
MKDIR=/bin/mkdir -p
RM=/bin/rm -f
RPM=/usr/bin/rpmbuild
TAR=/bin/tar
FIND=/usr/bin/find
MV=/bin/mv -f
JAR=jar

all: copy prep-RPM build-RPM 
	echo DONE with RPM!

dirs: $(J2SE) $(OPT) $(DJH)

package: $(PKGPATH) pkginfo proto

copy:
	$(ECHO) ### Removing old javahelp directories
	$(RM) -r $(TEMP_DIR)
	$(MKDIR) -p $(TEMP_DIR)
	pwd
	$(ECHO) ### Copying over the jh2.0 image for use with RPM
	$(CD) .. ;$(TAR) cf - jh$(JH_VERSION) | ( cd $(TEMP_DIR) ; $(TAR) xfBp -)
###	$(MV) $(TEMP_DIR)/jh$(JH_VERSION) $(TEMP_DIR)/$(JH_NAME)$(JH_VERSION) 
	$(MKDIR) $(TEMP_DIR)/packages
	$(MV) $(TEMP_DIR)/jh$(JH_VERSION) $(TEMP_DIR)/packages/javax.help-$(JH_VERSION) 
	$(FIND) $(TEMP_DIR) -name "*.bat" -print -exec $(RM) {} \;
###	$(CD) $(TEMP_DIR) ; $(TAR) cf $(JH_NAME)$(JH_VERSION).tar $(JH_NAME)$(JH_VERSION)
	$(CD) $(TEMP_DIR) ; $(TAR) cf $(JH_NAME)$(JH_VERSION).tar packages


clobber:
	$(RM) -rf j2se
	$(RM) -rf $(PKGNAME)
	$(RM) -rf prototype
	$(RM) -rf pkginfo



prep-RPM:

	@#subtle: override the sourcedir so that we don't have to copy
	@#        large binaries around.
	@# $(ECHO) "%_sourcedir	$(ABS_OUTPUTDIR)" >> $(RPM_MACROFILE)
	@# $(ECHO) "%_sourcedir	$(TEMP_DIR)" >> $(HOME)/.rpmmacros_jh
	@# $(ECHO) "%_builddir	$(TEMP_DIR)/BUILD" >> $(RPM_MACROFILE)
	@# $(ECHO) "%_rpmdir	$(TEMP_DIR)" >> $(RPM_MACROFILE)
	@#
	@# generate the gen-rpm-jh.spec file
	@#
	$(ECHO) milestone is $(MILESTONE)
	$(ECHO) ### generate the gen-rpm-jh.spec file
	$(ECHO) " "  >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define _builddir $(TEMP_DIR)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define _sourcedir $(TEMP_DIR)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define _rpmdir $(TEMP_DIR)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define _tmppath $(TEMP_DIR)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define jh_prefix 	/usr/java" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define jh_tarfile 	javahelp$(VERSION).tar" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define arch		i586" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define jh_dir		packages/javax.help-$(JH_VERSION)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define jh_name	$(JH_NAME)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%define jh_version	$(JH_VERSION)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	@#	$(ECHO) "%define packed_jars	\"$(JRE_PACKED_JARS)\"" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) " " >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Name:	sun-$(JH_NAME)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Summary: JavaHelp Runtime/Development Environment" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Version:	%{jh_version}" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Release:	fcs" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Copyright:	1994-2004 Sun Microsystems, Inc." >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Group:		Java Software Engineering" >> $(TEMP_DIR)/gen-rpm-jh.spec
	@#	$(ECHO) "Provides: jaxp_parser_impl, xml-commons-apis" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Requires:	glibc >= 2.1.2-11, sh-utils >= 2.0-1, fileutils >= 4.0-8, gawk >= 3.0.4-1, textutils >= 2.0-2" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "AutoReqProv:	no" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Url:	http://java.sun.com/products/javahelp" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Vendor:	Sun Microsystems, Inc." >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Source:	$(JH_NAME)$(JH_VERSION).tar" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Packager:	Java Software <http://java.sun.com/docs/forms/javahelp-sendusmail.html>" >> \
		$(TEMP_DIR)/gen-rpm-jh.spec
	@###$(ECHO) "Buildroot:	$(TEMP_DIR)/rpm-root" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Buildroot:	$(TEMP_DIR)" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "Prefix: %{jre_prefix}" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) " " >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) "%description" >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) -n "The JavaHelp Runtime/Development Environment consists of JavaHelp " >> $(TEMP_DIR)/gen-rpm-jh.spec
	@#	$(ECHO) -n "virtual machine, the Java platform core classes, " >> $(TEMP_DIR)/gen-rpm-jh.spec
	@#	$(ECHO) -n "and supporting files. It is the runtime part of " >> $(TEMP_DIR)/gen-rpm-jh.spec
	$(ECHO) " " >> $(TEMP_DIR)/gen-rpm-jh.spec
	@#
	@# add the rest of the template to the spec file...
	@#
	$(CAT) rpm-jh.spec >> $(TEMP_DIR)/gen-rpm-jh.spec

build-RPM:
	@#
	@# rpm wants to put the .rpm file the _rpmdir/{arch} directory.
	@#
	@######-$(MKDIR) $(TEMP_DIR)
	@# on RH8 rpm builds as user 'rpm'
	$(CHMOD) 777  $(TEMP_DIR)
	@# 
	@# generate the rpm file...
	@#
	$(RPM) -bb --buildroot $(TEMP_DIR) --rmsource --target $(NATIVE_ARCH)-Sun-Linux $(TEMP_DIR)/gen-rpm-jh.spec
	@#
	@# rename the generated .rpm file to the name we want 
	@#
	@# -$(MV) $(OUTPUTDIR)/$(NATIVE_ARCH)/$(TEMP_JRE_RPMFILE_NAME) \
	@#		$(OUTPUTDIR)/$(NATIVE_ARCH)/$(JRE_SHORT_BUNDLE_NAME).rpm
	@#

zip-RPM:
	@# not being zipped right now.
	@#
	@# zip it up for inclusion in self-extracting zip...
	@#
	$(RM) $(OUTPUTDIR)/$(NATIVE_ARCH)/temp.rpm $(TEMP_DIR)/temp.bin
	$(CD) $(OUTPUTDIR)/$(NATIVE_ARCH) ; \
		$(ZIPEXE) -qryn nothing temp.rpm $(JRE_SHORT_BUNDLE_NAME).rpm
	@# 
	@# combine the self-extractor and .rpm.zip into a single binary...
	@#
	$(CAT) $(UNZIPSFX) $(OUTPUTDIR)/$(NATIVE_ARCH)/temp.rpm > $(TEMP_DIR)/temp.bin
	$(SFX2SH) $(TEMP_DIR)/temp.bin \
	          $(JRE_IMAGE_DIR)/LICENSE \
		  $(OUTPUTDIR)/$(NATIVE_ARCH)/$(JRE_SHORT_BUNDLE_NAME)-rpm.sh \
		  $(JRE_NODOTVERSION) \
		  $(OUTPUTDIR)/$(JRE_TEMPORARY_DIR) \
		  "$(JRE_PACKED_JARS)" \
		  $(PLATFORM) \
		  " " \
		  "RPM"
	$(MV) $(OUTPUTDIR)/$(NATIVE_ARCH)/$(JRE_SHORT_BUNDLE_NAME).rpm $(BIN_BUNDLEDIR)/$(JRE_BUNDLE_NAME).rpm
	$(MV) $(OUTPUTDIR)/$(NATIVE_ARCH)/$(JRE_SHORT_BUNDLE_NAME)-rpm.sh $(BIN_BUNDLEDIR)/$(JRE_BUNDLE_NAME)-rpm.sh
	@# 
	@# remove the intermediate files...
	@#
	$(RM) $(TEMP_DIR)/temp.bin
	$(RM) -r $(OUTPUTDIR)/$(NATIVE_ARCH)
	$(RM) -r $(OUTPUTDIR)/$(JRE_TEMPORARY_DIR)
	$(RM) $(OUTPUTDIR)/$(JRE_BUNDLE_NAME).bin $(OUTPUTDIR)/$(JRE_BUNDLE_NAME).zip

jh-rpm-clobber:
	$(RM) $(TEMP_DIR)/gen-rpm-jh.spec $(RPM_MACROFILE) 
	$(RM) -r $(TEMP_DIR)/BUILD $(TEMP_DIR)/RPMS
	$(RM) $(OUTPUTDIR)/$(JRE_RPM_TARFILE) 
	$(RM) $(OUTPUTDIR)/$(ARCH)/$(JRE_SHORT_BUNDLE_NAME).rpm
	$(RM) $(OUTPUTDIR)/$(ARCH)/temp.rpm $(TEMP_DIR)/temp.bin
	$(RM) $(OUTPUTDIR)/$(ARCH)/$(JRE_SHORT_BUNDLE_NAME)-rpm.sh
	$(RM) $(BIN_BUNDLEDIR)/$(JRE_BUNDLE_NAME).rpm
	$(RM) $(BIN_BUNDLEDIR)/$(JRE_BUNDLE_NAME)-rpm.sh
	$(RM) -r $(OUTPUTDIR)/$(ARCH)

