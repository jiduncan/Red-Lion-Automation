@echo off
::SetLocal EnableDelayedExpansion
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Batch file to run a java program redlionexec.jar on mon-fri from  :
:: 7am to 6pm. The program is set to lock the screen and turn the    : 
:: display off at 5pm daily. Computer will shutdown on Fri at 7pm.   :
::                                                                   :
:: Created By: Johnny Duncan										 :
:: Last Modified: 7/5/18											 :
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
timeout /t 60
:: checks day of the week and time 
:begin

set "DAY=%date:~0,3%" 
set "hour=%time:~0,2%"
set "minute=%time:~3,2%"
set "second=%time:~6,2%"
set "myDate=%date:~4,2%-%date:~7,2%-%date:~10,4%"

