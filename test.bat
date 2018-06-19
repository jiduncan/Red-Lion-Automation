@echo off 
timeout /t 5
cd "C:\Program Files\NirCmd\"
start nircmd.exe cmdwait 5000 monitor off
rundll32.exe user32.dll, LockWorkStation
exit