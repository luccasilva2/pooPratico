@echo off
chcp 65001 > nul
cd /d "%~dp0"
if not exist out mkdir out
javac --release 11 -encoding UTF-8 -d out src\pagamentos\*.java src\pagamentos\modelo\*.java src\pagamentos\interfaces\*.java src\pagamentos\servico\*.java
if errorlevel 1 (
    echo Erro na compilacao.
    pause
    exit /b 1
)
java -Dstdout.encoding=UTF-8 -cp out pagamentos.Main
pause
