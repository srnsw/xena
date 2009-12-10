#*************************************************************************
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
# 
# Copyright 2008 by Sun Microsystems, Inc.
#
# OpenOffice.org - a multi-platform office productivity suite
#
# $RCSfile$
#
# $Revision$
#
# This file is part of OpenOffice.org.
#
# OpenOffice.org is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License version 3
# only, as published by the Free Software Foundation.
#
# OpenOffice.org is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License version 3 for more details
# (a copy is included in the LICENSE file that accompanied this code).
#
# You should have received a copy of the GNU Lesser General Public License
# version 3 along with OpenOffice.org.  If not, see
# <http://www.openoffice.org/license.html>
# for a copy of the LGPLv3 License.
#
#*************************************************************************
# Quick and dirty way to run all tests in sub-directories; probably only works
# on Unix.
# Can be removed once tests are included in regular builds.

all .PHONY:
    cd com/sun/star/comp/bridgefactory && dmake
    cd com/sun/star/comp/connections && dmake
    cd com/sun/star/lib/uno/bridges/java_remote && dmake
    cd com/sun/star/lib/uno/environments/java && dmake
    cd com/sun/star/lib/uno/environments/remote && dmake
    cd com/sun/star/lib/uno/protocols/urp && dmake
    cd com/sun/star/lib/util && dmake
    cd com/sun/star/uno && dmake
