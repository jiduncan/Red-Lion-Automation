@echo off 
timeout /t 5
echo %date%
set myDate=%date:~4,2%-%date:~7,2%-%date:~10,4%
echo %myDate%
echo hello > "C:\Users\johnny\Desktop\testlogs\%myDate%.txt"

pause
