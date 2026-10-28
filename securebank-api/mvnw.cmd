@REM Maven Wrapper for SecureBank API
@REM Uses locally installed Maven at C:\lk\apache-maven-3.9.9

@echo off
setlocal

set JAVA_HOME=C:\lk\openjdk-25.0.2_windows-x64_bin\jdk-25.0.2
set MAVEN_HOME=C:\lk\apache-maven-3.9.9
set PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH%

call mvn %*
endlocal
