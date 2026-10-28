@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script for Windows
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "MVN_CMD=mvn.cmd") ELSE (SET "MVN_CMD=%~f0")
@SET "MAVEN_PROJECTBASEDIR=%~dp0"
@IF NOT "%MAVEN_BASEDIR%"=="" (SET "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%")

@SET "MVNW_REPOURL=https://repo.maven.apache.org/maven2"
@SET "MVNW_USERNAME="
@SET "MVNW_PASSWORD="
@SET "MVNW_VERBOSE=false"

@SET "distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"
@SET "MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

@IF EXIST "%MAVEN_WRAPPER_PROPERTIES%" (
  FOR /F "usebackq tokens=1,2 delims==" %%a IN ("%MAVEN_WRAPPER_PROPERTIES%") DO (
    IF "%%a"=="distributionUrl" SET "distributionUrl=%%b"
  )
)

@SET "MAVEN_HOME=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven"
@IF NOT EXIST "%MAVEN_HOME%" (
  ECHO Downloading Maven 3.9.9...
  PowerShell -Command "& { Invoke-WebRequest -Uri '%distributionUrl%' -OutFile '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven.zip' }"
  PowerShell -Command "& { Expand-Archive -Path '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven.zip' -DestinationPath '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\' }"
  MOVE "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\apache-maven-3.9.9" "%MAVEN_HOME%"
  DEL "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven.zip"
)

@"%MAVEN_HOME%\bin\mvn.cmd" %*
