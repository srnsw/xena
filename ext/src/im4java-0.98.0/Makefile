# -----------------------------------------------------------------------------
# Makefile for im4java
#
# $Author$
# $Revision$
#
# License: GPL2 (see COPYING)
# -----------------------------------------------------------------------------

.PHONY: all src test test-prepare jar clean distclean \
        forrest api-doc \
        srcdist bindist predist srcarch binarch postdist \
        inc-dist-major inc-dist-minor inc-dist-pl

export 
include version.inc
VERSION  = $(DIST_MAJOR).$(DIST_MINOR).$(DIST_PL)

DIST       := im4java
DIST_NAME  := $(DIST)-$(VERSION)
DIST_STUFF := $(wildcard README* NEWS TODO INSTALL ChangeLog COPYING*) Makefile \
	      version.inc bin input images.src src

JAVA_PACKAGE=org.im4java.core
JAVA_TEST_CLASS=org.im4java.test.Test

PREFIX    = /usr/local
BINDIR    = $(PREFIX)/bin
ETCDIR    = /etc
SBINDIR   = $(PREFIX)/sbin
MANDIR    = $(PREFIX)/share/man
MAN1DIR   = $(MANDIR)/man1
MAN5DIR   = $(MANDIR)/man5
SHAREDIR  = $(PREFIX)/share/$(DIST)

# targets ---------------------------------------------------------------------

default:
	@echo -e "\nmain targets:\n"
	@echo -e "\tall:       recreates source and jar from scratch"
	@echo -e "\tsrc:       create java-sources from interface-definitions"
	@echo -e "\tcompile:   compile source-code"
	@echo -e "\tjar:       create im4java-library file $(DIST_NAME).jar"
	@echo -e "\ttest:      run test-suite\n"
	@echo -e "\tdoc:       create documentation\n"
	@echo -e "\tsrcdist:   create source-distribution"
	@echo -e "\tbindist:   create binary-distribution (also includes source)\n"
	@echo -e "\tclean:     cleanup after compile and test"
	@echo -e "\tdistclean: complete cleanup"
	@echo -e ""

all: clean src jar

src:
	bin/mk-im4java -p $(JAVA_PACKAGE)

compile:
	rm -fr build/*
	mkdir -p build
	javac -Xlint:unchecked -d build/ -cp src `find src -name "*.java"`

jar: compile
	jar cmf input/manifest.mf $(DIST_NAME).jar -C build/ .

TESTS=all
test-prepare:
	rm -fr images
	cp -a images.src images
	cp -a "images/tulip2.jpg" "images/tulip 2.jpg"
	chmod +w images/*.jpg

test: test-prepare
	java $(JAVA_OPTS) -cp build $(JAVA_TEST_CLASS) $(TESTS)

# targets (documentation) -----------------------------------------------------

DOC_SRC_DIR=./doc-src
FORREST_API_ROOT="$(DOC_SRC_DIR)/xdocs/ref"

NAME      = The im4java Library
HOMEPAGE  = http://im4java.sourceforge.net/
COPYRIGHT = Released under the LGPL, (c) Bernhard Bablok 2008-2009
WTITLE    = "$(NAME)"
DTITLE    = "$(NAME), Version $(VERSION)"
DBOTTOM   = "$(COPYRIGHT)<br>Homepage: <a href="$(HOMEPAGE)">$(HOMEPAGE)</a>"
DHEADER   = "<strong>$(NAME), Version $(VERSION)</strong>"
DFOOTER   = "<strong>$(NAME), Version $(VERSION)</strong>"

doc: api-doc forrest

forrest:
	cd $(DOC_SRC_DIR); \
	forrest; \
	cp -avu build/site/* ../doc

api-doc:
	javadoc -sourcepath src -d $(FORREST_API_ROOT) -windowtitle $(WTITLE) \
                -doctitle $(DTITLE) -footer $(DFOOTER) -header $(DHEADER) \
                -bottom $(DBOTTOM) \
                -version -author -subpackages org.im4java

# targets (cleanup and distribution) ------------------------------------------

clean:
	rm -fr $(DIST)-*.tar.bz2 build/* doc/* doc-src/build/* \
                 $(FORREST_API_ROOT)/* images $(DIST_NAME).jar

distclean: clean
	rm -fr src/$(subst .,/,$(JAVA_PACKAGE))/*Ops.java

update-changelog:
	cvs2cl.pl --group-within-date --no-times --accum
	rm -f ChangeLog.bak
	cvs commit -m"upated ChangeLog for version $(VERSION)" ChangeLog

srcdist: predist srcarch postdist

bindist: predist binarch postdist

predist:
	rm -f $(DIST_NAME)-*.tar.bz2
	tar -cpzf $(DIST_NAME).tgz --exclude "CVS" \
           --exclude ".cvsignore" --exclude ".project" \
           --exclude ".classpath" $(DIST_STUFF)
	rm -fr $(DIST_NAME)
	mkdir $(DIST_NAME)

srcarch:
	(cd $(DIST_NAME); tar -xpzf ../$(DIST_NAME).tgz; \
         $(MAKE) distclean src \
        )
	tar -cpjf $(DIST_NAME)-src.tar.bz2 $(DIST_NAME)

binarch:
	(cd $(DIST_NAME); tar -xpzf ../$(DIST_NAME).tgz; \
         $(MAKE) distclean src jar; \
         rm -fr build/* \
        )
	tar -cpjf $(DIST_NAME)-bin.tar.bz2 $(DIST_NAME)

postdist:
	rm -fr $(DIST_NAME) $(DIST_NAME).tgz


# targets (version management)  -----------------------------------------------

commit-version:
	cvs commit -m"upgraded version to $(VERSION)" version.inc

inc-dist-major:
	@echo DIST_MAJOR=$$[$(DIST_MAJOR)+1] > version.inc
	@echo DIST_MINOR=0 >> version.inc
	@echo DIST_PL=0 >> version.inc
	$(MAKE) commit-version
	@cat version.inc

inc-dist-minor:
	@echo DIST_MAJOR=$(DIST_MAJOR) > version.inc
	@echo DIST_MINOR=$$[$(DIST_MINOR)+1] >> version.inc
	@echo DIST_PL=0 >> version.inc
	$(MAKE) commit-version
	@cat version.inc

inc-dist-pl:
	@echo DIST_MAJOR=$(DIST_MAJOR) > version.inc
	@echo DIST_MINOR=$(DIST_MINOR) >> version.inc
	@echo DIST_PL=$$[$(DIST_PL)+1] >> version.inc
	$(MAKE) commit-version
	@cat version.inc
