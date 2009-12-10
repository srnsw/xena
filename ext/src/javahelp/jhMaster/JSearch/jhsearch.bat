@echo off

REM
REM @(#)jhsearch.bat	1.5 06/10/30
REM 
REM Copyright (c) 2006 Sun Microsystems, Inc.  All Rights Reserved.
REM DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
REM 
REM This code is free software; you can redistribute it and/or modify it
REM under the terms of the GNU General Public License version 2 only, as
REM published by the Free Software Foundation.  Sun designates this
REM particular file as subject to the "Classpath" exception as provided
REM by Sun in the LICENSE file that accompanied this code.
REM 
REM This code is distributed in the hope that it will be useful, but WITHOUT
REM ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
REM FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
REM version 2 for more details (a copy is included in the LICENSE file that
REM accompanied this code).
REM 
REM You should have received a copy of the GNU General Public License version
REM 2 along with this work; if not, write to the Free Software Foundation,
REM Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
REM 
REM Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
REM CA 95054 USA or visit www.sun.com if you need additional information or
REM have any questions.
REM

rem Run the JHSearch
rem @(#)jhsearch.bat 1.3 07/14/98

if ."%JSHOME%".==."". goto nojhhome
goto runnit
:nojhhome
echo The environment variable JSHOME has not been set
goto done
echo 
:runnit
@echo on
set CLASSPATH=%JSHOME%\lib\jsearch.jar
java com.sun.java.help.search.QueryEngine %1
@echo off

:done

