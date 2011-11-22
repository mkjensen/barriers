@echo off

REM md_d, for performing a molecular dynamics simulation using GROMACS in double precision.
REM Tested using GROMACS 4.5.4.

REM Directory of this script.
set DIR=%~dp0

REM Input files.
set INPUT_RUNFILE=%DIR%md.mdp
set INPUT_CONFORMATION=%1
set INPUT_TOPOLOGY=%2

REM Output files.
set OUTPUT_TRAJECTORY=output.xtc
set OUTPUT_ENERGY=output.edr

REM Temporary files.
set TEMP_MDP=temp.mdp
set TEMP_TPR=temp.tpr
set TEMP_CPT=temp.cpt
set TEMP_GRO=temp.gro
set TEMP_LOG=temp.log

REM Avoid GROMACS backup files.
set GMX_MAXBACKUP=-1

REM Create run file.
grompp_d -f %INPUT_RUNFILE% -po %TEMP_MDP% -c %INPUT_CONFORMATION% -p %INPUT_TOPOLOGY% -o %TEMP_TPR% > nul

REM Execute run file.
mdrun_d -s %TEMP_TPR% -x %OUTPUT_TRAJECTORY% -cpo %TEMP_CPT% -c %TEMP_GRO% -e %OUTPUT_ENERGY% -g %TEMP_LOG% > nul
