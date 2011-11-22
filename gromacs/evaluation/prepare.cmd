@echo off

REM prepare, for preparing a molecule for evaluation using GROMACS.
REM Tested using GROMACS 4.5.4.

REM Directory of this script.
set DIR=%~dp0

REM Input files.
set INPUT_RUNFILE=%DIR%evaluation.mdp
set INPUT_CONFORMATION=%1
set INPUT_TOPOLOGY=%2

REM Output files.
set OUTPUT_RUNFILE=evaluation.tpr

REM Temporary files.
set TEMP_MDP=temp.mdp

REM Avoid GROMACS backup files.
del /f %OUTPUT_RUNFILE% %TEMP_MDP%

REM Create run file.
grompp -f %INPUT_RUNFILE% -po %TEMP_MDP% -c %INPUT_CONFORMATION% -p %INPUT_TOPOLOGY% -o %OUTPUT_RUNFILE%
