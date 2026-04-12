@echo off
cd /d "%~dp0"
title RoadSync Server
echo.
echo ========================================
echo   RoadSync - starting (http://localhost:8080)
echo   To STOP the server: press Ctrl+C
echo ========================================
echo.

REM Prefer Maven Wrapper (no need to install "mvn" on Windows)
if exist "%~dp0mvnw.cmd" (
  call "%~dp0mvnw.cmd" spring-boot:run -DskipTests
  goto :end
)

where mvn >nul 2>nul
if %errorlevel%==0 (
  mvn spring-boot:run -DskipTests
  goto :end
)

echo ERROR: Could not find mvnw.cmd or mvn.
echo Open this folder in File Explorer and check that mvnw.cmd exists:
echo   %~dp0
pause
exit /b 1

:end
if errorlevel 1 (
  echo.
  echo Build/run failed. If you use IntelliJ: open DrivingSchoolSystemApplication.java
  echo from src\main\java (NOT from target\classes), then click the green Run arrow.
  echo.
  pause
)
