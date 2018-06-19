@echo off
:: Created By: Johnny Duncan
:: Last Modified: 6/18/18

set flag=0
set lockscreenflag=0
set shutdownflag=0
timeout /t 60

:begin
set "DAY=%date:~0,3%" 
set "hour=%time:~0,2%"
set "minute=%time:~3,2%"
set "second=%time:~6,2%"
echo %DAY% %hour% %minute% %second% 

if %hour% geq 7 if %hour% leq 17 if %flag% equ 0 (goto runit)
if %hour%==17 if %lockscreenflag%==0 (goto lock)
if %hour% geq 18 if %hour% lss 7 if %flag% equ 1 (goto killit)
if %DAY%==Fri if %hour%==19 (goto shutDownNow)

goto cont

:runit
echo entered runit %time%
start javaw -jar redlionexec.jar
set flag=1
set lockscreenflag=0
timeout /t 60
goto begin

:lock
echo entered lock %time%
lockscreenflag=1
rundll32.exe user32.dll, LockWorkStation
timeout /t 60
goto begin

:killit
echo entered killit %time%
taskkill /im chromedriver.exe
taskkill /im javaw.exe
set flag=0
timout /t 60
goto begin

:cont
echo entered cont %time%
timeout /t 60
goto begin

:shutDownNow
echo turning off now
shutdown /s /f

exit