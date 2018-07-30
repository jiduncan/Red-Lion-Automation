@echo off
timeout /t 60
cd C:\Program Files (x86)\Red Lion Controls\Crimson 3.0\Utils
websync -path C:\Users\johnny\Dropbox\RL_Logs\PTV_Logs 192.168.1.180 -user johnny -pass Bf4elite123!
echo finished File Transfer
pause
