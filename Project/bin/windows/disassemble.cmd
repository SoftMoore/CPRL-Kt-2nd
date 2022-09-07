@echo off

rem
rem Run CVM disassembler on a single ".obj" file
rem

rem set config environment variables locally
setlocal
call cprl_config.cmd

rem use either CLASSPATH or MODULEPATH
rem set CLASSPATH=%COMPILER_PROJECT_PATH%
rem java -ea -cp "%CLASSPATH%" edu.citadel.cvm.DisassemblerKt %*
set MODULEPATH=%COMPILER_PROJECT_PATH%
java -ea -p "%MODULEPATH%" -m edu.citadel.cvm/edu.citadel.cvm.DisassemblerKt %*

rem restore settings
endlocal
