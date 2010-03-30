#
# @(#)GNUmakefile	1.2 06/10/30
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

# @(#)GNUmakefile	1.10 97/06/05
#
# Makefile for use on Solaris using gnumake.

# apiviewer.jar has launcher code. 
JHINDEXER_JAR = jhindexer.jar
JHSEARCH_JAR = jhsearch.jar

CP=/bin/cp -f

all: $(JHINDEXER_JAR) $(JHSEARCH_JAR)

clean:
	rm -f -r $(JHINDEXER_JAR) $(JHSEARCH_JAR)

$(JHINDEXER_JAR):
	@rm -f $(JHINDEXER_JAR)
	jar cmf manifest_jhindexer $(JHINDEXER_JAR)

$(JHSEARCH_JAR):
	@rm -f $(JHSEARCH_JAR)
	jar cmf manifest_jhsearch $(JHSEARCH_JAR)

