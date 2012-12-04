@echo off
rem Runs a collection of VDM++ test examples
rem Assumes specification is in Word RTF files

set S1=Board.rtf
set S2=TestBoard.rtf
set S3=Game.rtf
set S4=TestGame.rtf
set S5=Player.rtf
set S6=TestPlayer.rtf


"D:\VDM++Toolbox_v8.1.1b\bin\vppde" -p -R vdm.tc %S1% %S2%
for /R %%f in (*.arg) do call vdmtest "%%f"
