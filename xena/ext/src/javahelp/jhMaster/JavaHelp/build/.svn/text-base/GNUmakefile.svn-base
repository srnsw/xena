#
# @(#)GNUmakefile	1.92 06/10/30
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

#
# @(#)GNUmakefile 1.73 12/10/98
#
.SUFFIXES:
.SUFFIXES: .java .class $(SUFFIXES)

LIBDIR=../javahelp/lib
DEMOLIB=../demos/lib
CLASSDIR=classes
TMPCLASSDIR=tmpclasses
TMPJAR=tmpclasses.jar
SRCDIR=../src
BINDIR=../javahelp/bin
PKGDIR=javax/help
PKGDIRIMPL=com/sun/java/help

ifeq ($(JSEARCHHOME),)
JSEARCHHOME=../../JSearch
endif

ifeq ($(J2EEHOME),)
J2EEHOME=/java/re/j2sdkee/1.3/archive/fcs/latest/binaries/solsparc
endif

ifeq ($(JDICHOME),)
JDICHOME=/java/re/jdic/archive/binaries
endif

JAVAC=javac
CP=/bin/cp -f

JAVADOCFLAGS=-J-mx64m -J-ms64m

APIDOC=../doc/api
TARGETPKG= \
	javax.help javax.help.search javax.help.event \
	javax.help.plaf javax.help.plaf.basic javax.help.tagext\
	../src/impl/com/sun/java/help/impl/ViewAwareComponent.java \
	../src/impl/com/sun/java/help/impl/JHSecondaryViewer.java \
	../src/impl/com/sun/java/help/impl/JHSecondaryViewerBeanInfo.java

# the Java files
include share/javax_help.jmk

# Miscelaneous files - currently just images & cursors
include share/javax_help.mmk

all: prep $(CLASSDIR)/$(PKGDIR) $(CLASSDIR)/$(BASICPKGDIR)/images $(CLASSDIR)/$(GTKPKGDIR)/images jars demos

prep:
	cd $(BINDIR) ; \
	$(MAKE); \
	cd ../../.. 
	chmod a+x $(BINDIR)/jhindexer
	chmod a+x $(BINDIR)/jhsearch

quick: $(CLASSDIR)/$(PKGDIR) $(CLASSDIR)/$(BASICPKGDIR)/images $(CLASSDIR)/$(GTKPKGDIR)/images classes

jars: $(CLASSDIR)/$(BASICPKGDIR)/images $(CLASSDIR)/$(PKGDIR) \
	$(LIBDIR)/jhall.jar $(LIBDIR)/jh.jar \
	$(LIBDIR)/jhbasic.jar $(LIBDIR)/jh-client.jar \
	\
	$(LIBDIR)/jsearch-client.jar $(LIBDIR)/jsearch-indexer.jar \
	\
	$(LIBDIR)/jsearch.jar

demos: $(DEMOLIB)

classes: $(CLASSDIR)/$(BASICPKGDIR)/images $(CLASSDIR)/$(GTKPKGDIR)/images $(CLASSDIR)/$(PKGDIR) $(ALL_CLASSES) $(IMAGES) $(CURSORS) $(RESOURCES)

$(LIBDIR)/jhbasic.jar: $(CLASSDIR)/$(PKGDIR) $(ALL_CLASSES) $(IMAGES) $(CURSORS) $(RESOURCES) Manifest
	mkdir -p $(LIBDIR)
	mkdir -p $(TMPCLASSDIR)
	jar -cf $(TMPJAR) $(CLASSDIR)
	cd $(TMPCLASSDIR) ; jar -xf ../$(TMPJAR) ; \
	/bin/rm -f $(subst .,*.,$(NOT_CLIENT_NOSEARCH_CLASSES)) 
	cd $(TMPCLASSDIR)/$(CLASSDIR) ; \
	jar cfm ../../$(LIBDIR)/jhbasic.jar ../../Manifest *
	/bin/rm -rf $(TMPCLASSDIR) $(TMPJAR)

$(LIBDIR)/jh-client.jar: $(CLASSDIR)/$(PKGDIR) $(ALL_CLASSES) $(IMAGES) $(CURSORS) $(RESOUCES) Manifest
	mkdir -p $(LIBDIR)
	mkdir -p $(TMPCLASSDIR)
	jar -cf $(TMPJAR) $(CLASSDIR)
	cd $(TMPCLASSDIR) ; jar -xf ../$(TMPJAR)
	cd $(TMPCLASSDIR)/$(CLASSDIR) ; \
	jar cfm ../../$(LIBDIR)/jh-client.jar ../../Manifest *
	/bin/rm -rf $(TMPCLASSDIR) $(TMPJAR)

$(LIBDIR)/jh.jar: $(LIBDIR)/jh-client.jar $(LIBDIR)/jsearch-client.jar Manifest
	/bin/csh -f tools/jarinto FORCE MANIFEST Manifest \
		$(LIBDIR)/jh.jar \
		$(LIBDIR)/jh-client.jar $(LIBDIR)/jsearch-client.jar

$(LIBDIR)/jhall.jar: $(LIBDIR)/jh.jar $(LIBDIR)/jsearch-indexer.jar Manifest
	/bin/csh -f tools/jarinto FORCE MANIFEST Manifest \
		$(LIBDIR)/jhall.jar \
		$(LIBDIR)/jh.jar $(LIBDIR)/jsearch-indexer.jar

$(LIBDIR)/%.jar: $(JSEARCHHOME)/lib/%.jar
	$(CP) $< $@

$(BINDIR) \
$(DEMOLIB) \
$(CLASSDIR)/$(BASICPKGDIR)/images \
$(CLASSDIR)/$(GTKPKGDIR)/images \
$(CLASSDIR)/$(PKGDIR):
	mkdir -p $@

$(TMPCLASSDIR)/$(PKGDIR):
	mkdir -p $(TMPCLASSDIR)/$(PKGDIR)


docs: $(APIDOC)

$(APIDOC): $(CLASSDIR)/$(PKGDIR) $(ALL_CLASSES) 
	rm -rf $(APIDOC)
	mkdir -p $(APIDOC)
	export CLASSPATH; CLASSPATH=$(SRCDIR)/new:$(SRCDIR)/impl:$(LIBDIR)/jh.jar:$(J2EEHOME)/lib/j2ee.jar:$(JDICHOME)/jdic.jar ; \
	javadoc -public $(JAVADOCFLAGS) -d $(APIDOC) $(TARGETPKG)

clean: 
	/bin/rm -rf $(CLASSDIR)/*
	/bin/rm -rf $(LIBDIR)/*
	cd $(BINDIR) ; \
	$(MAKE) clean

$(CLASSDIR)/%.class: $(SRCDIR)/new/%.java
	export CLASSPATH; CLASSPATH=$(SRCDIR)/new:$(SRCDIR)/impl:$(CLASSPATH):$(J2EEHOME)/lib/j2ee.jar:$(JDICHOME)/jdic.jar ; \
	$(JAVAC) -d $(CLASSDIR) $<

$(CLASSDIR)/%.class: $(SRCDIR)/impl/%.java
	export CLASSPATH; CLASSPATH=$(SRCDIR)/new:$(SRCDIR)/impl:$(CLASSPATH):$(J2EEHOME)/lib/j2ee.jar:$(JDICHOME)/jdic.jar ; \
	$(JAVAC) -d $(CLASSDIR) $<

$(CLASSDIR)/$(BASICPKGDIR)/images/%.gif: $(SRCDIR)/new/$(BASICPKGDIR)/images/%.gif
	$(CP) $< $@

$(CLASSDIR)/$(BASICPKGDIR)/images/%.properties: $(SRCDIR)/new/$(BASICPKGDIR)/images/%.properties
	$(CP) $< $@

$(CLASSDIR)/$(GTKPKGDIR)/images/%.gif: $(SRCDIR)/new/$(GTKPKGDIR)/images/%.gif
	$(CP) $< $@

$(CLASSDIR)/$(GTKPKGDIR)/images/%.png: $(SRCDIR)/new/$(GTKPKGDIR)/images/%.png
	$(CP) $< $@

$(CLASSDIR)/$(GTKPKGDIR)/images/%.properties: $(SRCDIR)/new/$(GTKPKGDIR)/images/%.properties
	$(CP) $< $@

