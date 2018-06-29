@echo off
:: Turns off display and locks the screen

timeout /t 60
cd "C:\Program Files\NirCmd\"
start nircmd.exe cmdwait 5000 monitor off
rundll32.exe user32.dll, LockWorkStation
