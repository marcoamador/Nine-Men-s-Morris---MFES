@echo off
rem Runs a collection of VDM++ test examples
rem Assumes specification is in Word RTF files

set S1=Player.rtf
set S2=TestPlayer.rtf
set S3=Board.rtf
set S4=TestBoard.rtf
set S5=Game.rtf
set S6=TestGame.rtf

"D:\VDM++Toolbox_v8.1.1b\bin\vppde" -p -R vdm.tc %S1% %S2% %S3% %S4% %S5% %S6%
for /R %%f in (*.arg) do call vdmtest "%%f"
