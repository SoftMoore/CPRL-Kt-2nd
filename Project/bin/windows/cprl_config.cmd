@echo off

rem
rem Configuration settings for the CPRL compiler project.
rem
rem These settings assume an IntelliJ IDEA project with four separate modules named
rem edu.citadel.assembler, edu.citadel.compiler, edu.citadel.cprl, and edu.citadel.cvm.
rem Class files are placed in the IDEA default "out\production" directory.  The
rem project directory hierarchy is as follows:
rem  PROJECT_HOME
rem     - edu.citadel.assembler
rem     - edu.citadel.compiler
rem     - edu.citadel.cprl
rem     - edu.citadel.cvm

rem set PROJECT_HOME to the directory for your compiler project
set PROJECT_HOME=C:\Compilers\CompilerProject

rem set CLASSES_HOME to the directory name used for compiled Java classes
set CLASSES_HOME=%PROJECT_HOME%\out\production

set ASSEMBLER_HOME=%CLASSES_HOME%\edu.citadel.assembler
set COMPILER_HOME=%CLASSES_HOME%\edu.citadel.compiler
set CPRL_HOME=%CLASSES_HOME%\edu.citadel.cprl
set CVM_HOME=%CLASSES_HOME%\edu.citadel.cvm

rem set KT_LIB_HOME to the directory for the Kotlin jar files
set KT_LIB_HOME=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.1.2\plugins\Kotlin\kotlinc\lib

rem Add all project-related class directories to COMPILER_PROJECT_PATH.
set COMPILER_PROJECT_PATH=%ASSEMBLER_HOME%;%COMPILER_HOME%;%CPRL_HOME%;%CVM_HOME%;%KT_LIB_HOME%\kotlin-stdlib.jar
