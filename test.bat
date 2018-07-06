@echo off 
timeout /t 5
set "DAY=%date:~0,3%" 
set "hour=%time:~0,2%"
echo %DAY%
pause
if %DAY% equ Tue (
	if %hour% equ 9 (
		goto shutdownnow
	)
)


exit

:shutdownnow
echo %time% turning off now
pause
taskkill /im chromedriver.exe
taskkill /im javaw.exe
timeout /t 5
::shutdown /s /f