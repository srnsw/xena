#
# @(#)GNUmakefile	1.12 06/10/30
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

.SUFFIXES:
.SUFFIXES: .java .class

JHHOMEWS=../../JavaHelp
VPATH = $(JHHOMEWS)/src/impl:$(JHHOMEWS)/src/new

SEARCH_SRC=ConfigFile.java \
	IndexBuilder.java \
	IndexerKit.java \
	SearchEngine.java \
	SearchEvent.java \
	SearchItem.java \
	SearchListener.java \
	SearchQuery.java

SEARCH_CLASSES=$(SEARCH_SRC:%.java=classes/javax/help/search/%.class)

OTHER_SRC=javax/help/event/EventListenerList.java \
	javax/help/HelpUtilities.java

OTHER_CLASSES=$(OTHER_SRC:%.java=classes/%.class)

IMPL_SRC:=com/sun/java/help/impl/HeaderParser.java
IMPL_CLASSES=$(IMPL_SRC:%.java=classes/%.class)

ALL_CLASSES=$(SEARCH_CLASSES) $(IMPL_CLASSES) $(OTHER_CLASSES)


jar:	../lib/jsearch-misc.jar

../lib/jsearch-misc.jar:  classes $(ALL_CLASSES)
	cd classes ; \
	jar cf ../../lib/jsearch-misc.jar javax com

classes/%.class: %.java
	javac -d classes -classpath ../../JavaHelp/src/impl:../../JavaHelp/src/new $<

classes:
	mkdir classes

clean:
	$(RM) -rf classes
