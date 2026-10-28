@echo off
REM ================================================================
REM  SecureBank API — Spring Boot Startup Script
REM  Starts the backend on http://localhost:8080
REM  JDK: Java 25 (OpenJDK 25.0.2), compile target: Java 21
REM  Spring Boot 3.3.5 requires Java 21 class files
REM ================================================================

set JAVA_HOME=C:\lk\openjdk-25.0.2_windows-x64_bin\jdk-25.0.2
set MAVEN_HOME=C:\lk\apache-maven-3.9.9
set PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH%

echo.
echo  ================================================================
echo   SecureBank API — Starting Spring Boot on http://localhost:8080
echo   H2 Console: http://localhost:8080/h2-console
echo  ================================================================
echo.

cd /d "%~dp0securebank-api"
call mvn spring-boot:run
