@echo off
title Smart University Transport Management System
echo ================================================
echo  Smart University Transport Management System
echo  Premium Admin Dashboard
echo ================================================
java -cp "out;lib\mysql-connector-j-8.0.33.jar" ^
     -Dawt.useSystemAAFontSettings=on ^
     -Dswing.aatext=true ^
     Main
pause
