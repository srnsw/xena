@echo off
rem
rem ==========================================================================
rem = Copyright 2004 The Apache Software Foundation.
rem =
rem = Licensed under the Apache License, Version 2.0 (the "License");
rem = you may not use this file except in compliance with the License.
rem = You may obtain a copy of the License at
rem =
rem =     http://www.apache.org/licenses/LICENSE-2.0
rem =
rem = Unless required by applicable law or agreed to in writing, software
rem = distributed under the License is distributed on an "AS IS" BASIS,
rem = WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem = See the License for the specific language governing permissions and
rem = limitations under the License.
rem ==========================================================================
rem
rem Change drive and directory to %1 (Win9X only for NT/2K use "cd /d")
cd %1
%1\
set ANT_RUN_CMD=%2
shift
shift

set PARAMS=
:loop
if ""%1 == "" goto runCommand
set PARAMS=%PARAMS% %1
shift
goto loop

:runCommand
rem echo %ANT_RUN_CMD% %PARAMS%
%ANT_RUN_CMD% %PARAMS%

