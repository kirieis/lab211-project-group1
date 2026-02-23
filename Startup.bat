@echo off
title Github Pharmacy - Startup
cd /d "%~dp0"

echo ============================================
echo   GITHUB PHARMACY - KHOI DONG HE THONG
echo ============================================
echo.

:: Buoc 1: Chay Tomcat Server (cua so rieng)
echo [1/2] Dang khoi dong Tomcat Server (port 8081)...
start "Tomcat Server" cmd /k "cd /d %~dp0 && mvn clean package cargo:run"

:: Cho 5 giay de Tomcat bat dau khoi dong
timeout /t 5 /nobreak >nul

:: Buoc 2: Cau hinh va chay Ngrok
echo [2/2] Dang khoi dong Ngrok Tunnel...
.\ngrok.exe config add-authtoken 39AA85xOmoZDldz9GTNwPuNu0rz_5Byfz4GfpsxFg3pzJADXX

echo.
echo ============================================
echo   TRUY CAP WEB:
echo   https://unplanked-inculpably-malorie.ngrok-free.dev/home.html
echo ============================================
echo.

.\ngrok.exe http 127.0.0.1:8081 --domain=unplanked-inculpably-malorie.ngrok-free.dev
pause