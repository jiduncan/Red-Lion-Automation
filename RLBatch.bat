@echo off
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Batch file to run a java program redlionexec.jar on mon-fri from  :
:: 7am to 6pm. The program is set to lock the screen and turn the    : 
:: display off at 5pm daily. Computer will shutdown on Fri at 7pm.   :
::                                                                   :
:: Created By: Johnny Duncan										 :
:: Last Modified: 6/19/18											 :
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

set flag=0
set lockscreenflag=0
set shutdownflag=0
timeout /t 60

:: checks day of the week and time 
:begin
set "DAY=%date:~0,3%" 
set "hour=%time:~0,2%"
set "minute=%time:~3,2%"
set "second=%time:~6,2%"
echo %DAY% %hour% %minute% %second% 

:: if between 7am and 5pm and first time call runit
if %hour% geq 7 if %hour% leq 17 if %flag% equ 0 (goto runit)
:: 
if %hour%==17 if %lockscreenflag%==0 (goto lock)

if %hour% geq 18 if %hour% lss 7 if %flag% equ 1 (goto killit)
if %DAY%==Fri if %hour%==19 (goto shutDownNow)

goto cont

:runit
echo %time% entered runit
start javaw -jar redlionexec.jar
set flag=1
set lockscreenflag=0
timeout /t 60
goto begin

:lock
echo %time% entered lock 
lockscreenflag=1
cd "C:\Program Files\NirCmd\"
start nircmd.exe cmdwait 5000 monitor off
rundll32.exe user32.dll, LockWorkStation
timeout /t 60
goto begin

:killit
echo %time% entered killit
taskkill /im chromedriver.exe
taskkill /im javaw.exe
set flag=0
timout /t 60
goto begin

:cont
echo %time% entered cont 
timeout /t 60
goto begin

:shutDownNow
echo %time% turning off now 
shutdown /s /f

exit