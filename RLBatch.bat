@echo off
SetLocal EnableDelayedExpansion
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Batch file to run a java program redlionexec.jar on mon-fri from  :
:: 7am to 6pm. The program is set to lock the screen and turn the    : 
:: display off at 5pm daily. Computer will shutdown on Fri at 7pm.   :
::                                                                   :
:: Created By: Johnny Duncan										 :
:: Last Modified: 6/26/18											 :
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

set flag=0
set lockscreenflag=0
timeout /t 60

:: checks day of the week and time 
:begin
set "DAY=%date:~0,3%" 
set "hour=%time:~0,2%"
set "minute=%time:~3,2%"
set "second=%time:~6,2%"
::set "myTime=%DAY% %hour%:%minute%-%second%"
set "myDate=%date:~4,2%-%date:~7,2%-%date:~10,4%"
echo %time% entered begin >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"

:: if between 7am and 5pm and first time call runit
if %hour% geq 7 (
	if %hour% leq 17 (
		if %flag% equ 0 ( 
			goto runit
		)
	)
)
if %hour% equ 17 (
	if %lockscreenflag% equ 0 (
		goto lock
	)
)
if %hour% geq 18 (
	if %hour% lss 7 (
		if %flag% equ 1 (
			goto killit
		)
	)
)
if %DAY% == Fri (
	if %hour% equ 19 (
		goto shutDownNow
	)
)

goto cont

:runit
echo %time% entered runit >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
start javaw -jar redlionexec.jar
set flag=1
set lockscreenflag=0
echo flag = %flag% lockscreenflag = %lockscreenflag% >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
timeout /t 60
goto begin

:lock
echo %time% entered lock >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
set lockscreenflag=1
cd "C:\Program Files\NirCmd\"
start nircmd.exe cmdwait 5000 monitor off
rundll32.exe user32.dll, LockWorkStation
echo flag = %flag% lockscreenflag = %lockscreenflag% >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
timeout /t 60
goto begin

:killit
echo %time% entered killit >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
set flag=0
taskkill /im chromedriver.exe
taskkill /im javaw.exe
echo flag = %flag% lockscreenflag = %lockscreenflag% >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
timout /t 60
goto begin

:cont
echo %time% entered cont >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
echo flag = %flag% lockscreenflag = %lockscreenflag% >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
timeout /t 60
goto begin

:shutDownNow
echo %time% turning off now >> "C:\Users\johnny\Desktop\logs\batchlogs\%myDate%.txt"
shutdown /s /f

exit