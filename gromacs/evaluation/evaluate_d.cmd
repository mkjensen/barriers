@echo off

REM evaluate_d, for evaluating conformations using GROMACS in double precision.
REM Tested using GROMACS 4.5.4.

REM Directory of this script.
set DIR=%~dp0

REM Input files.
set INPUT_RUNFILE=%DIR%evaluation.tpr
set INPUT_CONFORMATION=%1

REM Output files.
set OUTPUT_ENERGY=output.edr

REM Temporary files.
set TEMP_LOG=temp.log

REM Avoid GROMACS backup files.
set GMX_MAXBACKUP=-1

REM Execute run file.
mdrun_d -s %INPUT_RUNFILE% -e %OUTPUT_ENERGY% -g %TEMP_LOG% -rerun %INPUT_CONFORMATION% > nul
