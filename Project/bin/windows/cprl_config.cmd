@echo off

rem
rem Configuration settings for the CPRL compiler project.
rem
rem These settings assume an IntelliJ IDEA project with three separate modules named
rem edu.citadel.compiler, edu.citadel.cprl, and edu.citadel.cvm.  Class files are placed
rem in an "out\production" directory.  The project directory hierarchy is as follows:
rem  PROJECT_HOME
rem     - edu.citadel.compiler
rem     - edu.citadel.cprl
rem     - edu.citadel.cvm

rem set PROJECT_HOME to the directory for your IntelliJ projects
set PROJECT_HOME=C:\JMooreMACS\Teaching\Compiler\Project

rem set CLASSES_HOME to the directory name used for compiled Java classes
set CLASSES_HOME=%PROJECT_HOME%\out\production

set COMPILER_HOME=%CLASSES_HOME%\edu.citadel.compiler
set CPRL_HOME=%CLASSES_HOME%\edu.citadel.cprl
set CVM_HOME=%CLASSES_HOME%\edu.citadel.cvm

rem set KT_LIB_HOME to the directory for the Kotlin jar files
set KT_LIB_HOME=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2022.2\plugins\Kotlin\kotlinc\lib

rem Add all project-related class directories to COMPILER_PROJECT_PATH.
set COMPILER_PROJECT_PATH=%COMPILER_HOME%;%CPRL_HOME%;%CVM_HOME%;%KT_LIB_HOME%\kotlin-stdlib.jar
