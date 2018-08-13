@echo off
timeout /t 60
set "myDate=%date:~4,2%-%date:~7,2%-%date:~10,4%"
echo %time% Daily FTP Routine Start >> "C:\Users\johnny\Dropbox\RL_Logs\daily_FTP_Logs\%myDate%.txt"

cd C:\Program Files (x86)\Red Lion Controls\Crimson 3.0\Utils
websync -path C:\Users\johnny\Dropbox\RL_Logs\PTV_Logs 192.168.1.180 -user **** -pass ****

echo %time% Daily FTP Routine End >> "C:\Users\johnny\Dropbox\RL_Logs\daily_FTP_Logs\%myDate%.txt"
pause
