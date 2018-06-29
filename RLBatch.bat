@echo off
::SetLocal EnableDelayedExpansion
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Batch file to run a java program redlionexec.jar on mon-fri from  :
:: 7am to 6pm. The program is set to lock the screen and turn the    : 
:: display off at 5pm daily. Computer will shutdown on Fri at 7pm.   :
::                                                                   :
:: Created By: Johnny Duncan										 :
:: Last Modified: 6/29/18											 :
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

timeout /t 60
:: checks day of the week and time 
:begin
set "DAY=%date:~0,3%" 
set "hour=%time:~0,2%"
set "minute=%time:~3,2%"
set "second=%time:~6,2%"
set "myDate=%date:~4,2%-%date:~7,2%-%date:~10,4%"
echo %time% entered beggin
echo %time% entered begin >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"

:: if between 7am and 5pm and first time call runit
if %hour% geq 6 (
	if %hour% leq 17 (goto runit)
)
if %hour% equ 17 (
	goto lock
)
if %hour% geq 18 (
	if %minute% geq 30 (
		goto killit
	)
)
if %DAY% == Fri (
	if %hour% equ 18 ( 
		if %minute% equ 45 (
			goto shutDownNow
		)
	)
)
if %DAY% neq "Fri" (
	if %hour% equ 19 (
		goto eodexit
	)
)
goto cont

:runit
echo %time% entered runit
echo %time% entered runit >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
tasklist /nh /fi "IMAGENAME eq javaw.exe" | find /i "javaw.exe" > nul || (start javaw -jar redlionexec.jar)
timeout /t 60
goto begin

:lock
echo %time% entered lock
echo %time% entered lock >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
cd "C:\Program Files\NirCmd\"
start nircmd.exe cmdwait 5000 monitor off
rundll32.exe user32.dll, LockWorkStation
timeout /t 60
goto begin

:killit
echo %time% entered killit
echo %time% entered killit >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
taskkill /im chromedriver.exe
taskkill /im javaw.exe
timeout /t 60
goto begin

:eodexit
echo %time% exiting program for the day
echo %time% exiting program for the day >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
exit

:cont
echo %time% entered cont
echo %time% entered cont >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
timeout /t 60
goto begin

:shutDownNow
echo %time% turning off now
echo %time% turning off now >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
shutdown /s /f

