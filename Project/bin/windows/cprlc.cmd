@echo off

rem
rem Run CPRL compiler on a single ".cprl" file
rem

rem set config environment variables locally
setlocal
call cprl_config.cmd

rem use either CLASSPATH or MODULEPATH
rem set CLASSPATH=%COMPILER_PROJECT_PATH%
rem java -ea -cp "%CLASSPATH%" edu.citadel.cprl.CompilerKt %*
set MODULEPATH=%COMPILER_PROJECT_PATH%
java -ea -p "%MODULEPATH%" -m edu.citadel.cprl/edu.citadel.cprl.CompilerKt %*

rem restore settings
endlocal
