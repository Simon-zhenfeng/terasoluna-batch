set TESTNO=36
call common.bat
set SOURCES=%sourcedir%\case%TESTNO%\*.java
set MAIN=case%TESTNO%.Main
call compile.bat
call run.bat