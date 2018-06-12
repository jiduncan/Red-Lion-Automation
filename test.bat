@echo off 
timeout /t 10

:begin
set "hour=%time:~0,2%"
set "minute=%time:~3,2%"
set "second=%time:~6,2%"
set "DAY=%date:~0,3%"
echo %hour% %minute% %second% %DAY%
timeout /t 10

if %DAY%==Tue if %hour%==13 if %minute%==26 (goto :shut)
goto cont

:shut
echo turnoffnow
timeout /t 10
shutdown /s /f

:cont
echo cont
timeout /t 10
goto begin

exit