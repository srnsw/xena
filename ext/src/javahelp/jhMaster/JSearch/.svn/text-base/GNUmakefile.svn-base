#
# @(#)GNUmakefile	1.31 06/10/30
# 
# Copyright (c) 2006 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
# 
# This code is free software; you can redistribute it and or modify it
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

# This is a bare-bones Makefile just to build the engine from scratch.
#
# It should be replaced by a real Makefile.

TOP := $(shell pwd)

ifeq ($(JHHOMEWS),)
TOP=$(shell pwd)
JHHOMEWS=$(TOP)/../JavaHelp
endif

CP=/bin/cp -f

PKG = com/sun/java/help/search
JARS = 	lib/jsearch-misc.jar \
	lib/jsearch-client.jar \
	lib/jsearch-indexer.jar \
	lib/jsearch.jar

all: lib ${JARS} binaries demos

clean: 
	rm -rf lib
	cd client ; $(MAKE) clean
	cd indexer ; $(MAKE) clean
	cd misc ; $(MAKE) clean

# simple scripts

binaries: jhindexer jhsearch
	- chmod a+x jhindexer
	- chmod a+x jhsearch

# a test case

demos: demos/holidays

demos/holidays:
	mkdir -p demos/holidays
	cd demos/holidays ; export to ; to=`pwd` ; \
	cd ${JHHOMEWS}/demos/hs/holidays ; \
	tar cFFf - hol | ( cd $${to} ; tar xvf - )


## JAR files

lib/jsearch.jar: lib/jsearch-client.jar lib/jsearch-indexer.jar lib/jsearch-misc.jar
	/bin/csh -f jarinto FORCE lib/jsearch.jar \
		lib/jsearch-client.jar lib/jsearch-indexer.jar lib/jsearch-misc.jar

lib/jsearch-misc.jar: FORCE
	cd misc ; $(MAKE) JHHOMEWS=$(JHHOMEWS) jar

lib/jsearch-client.jar: lib/jsearch-misc.jar FORCE
	cd client ; $(MAKE) jar

lib/jsearch-indexer.jar: lib/jsearch-misc.jar FORCE
	cd indexer ; $(MAKE) jar

## Directories

lib:
	mkdir -p lib

FORCE:
