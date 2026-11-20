@echo off
REM Compile and run the PG Accommodation Management System (Windows console).
REM Paths are relative, so this works even if the folder name has spaces.
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
javac -nowarn -encoding UTF-8 -d out src\com\pgms\ds\*.java src\com\pgms\util\*.java src\com\pgms\model\*.java src\com\pgms\db\*.java src\com\pgms\dao\*.java src\com\pgms\service\*.java src\com\pgms\ui\*.java src\com\pgms\gui\*.java
if errorlevel 1 goto end
java -cp "out;lib\mysql-connector-j-9.1.0.jar" com.pgms.ui.Main
:end
pause
