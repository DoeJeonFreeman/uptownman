#!/bin/csh
set PATH=/usr/local/bin:/usr/lib64/qt-3.3/bin:/usr/kerberos/sbin:/usr/kerberos/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/op/bin:/usr/local/bin:/usr/java/jdk1.6.0_33/bin
setenv WEB_HOME "/var/www/html"
set SHELL_HOME=/op/DFSM_GRPH/SHEL
source $SHELL_HOME/conf.csh

echo ' start shell for town forecast graphic....' 
set e_s=00
set tday=$1
set rdate=$tday$e_s

echo "start " >> /op/DFSM_GRPH/ncl_scr/kwrf_end.log
date >> /op/DFSM_GRPH/ncl_scr/kwrf_end.log
cd $SHELL_HOME
$SHELL_HOME/dfs_grph_job_gdps.csh $rdate
$SHELL_HOME/dfs_grph_job_kwrf.csh $rdate
$SHELL_HOME/dfs_grph_job_um12km.csh $rdate
echo $rdate 
echo $tday 

set cmd_opt="rundate="\"${rdate}\"" "model=\"KWRF\"
set cmd_rdp="rundate="\"${rdate}\"" "model=\"RDPS\"
echo $cmd_opt 
echo "testing home town"
echo $HOME_TOWN
cd $HOME_TOWN
pwd
echo "Sky ncl run" 
ncl grid_sky_kf.ncl $cmd_opt & 
echo "Pop ncl run" 
ncl grid_pop_kf.ncl $cmd_opt &
echo "Tmp ncl run" 
ncl grid_tmp_kf.ncl $cmd_opt &
echo "Rain 3hour ncl run" 
ncl grid_pop_rain_kf.ncl $cmd_opt  &
echo "Pty ncl run" 
ncl grid_pty_kf.ncl $cmd_opt  &
echo "RH ncl run" 
ncl grid_rh_kf.ncl $cmd_opt &
echo "Wind ncl run" 
ncl grid_wind_kf.ncl $cmd_opt &
echo "Snow ncl run" 
ncl grid_sn3_kf.ncl $cmd_opt &
rm /var/www/html/dateTime.dat
echo $rdate > $WEB_HOME\/dateTime.dat
date >> /op/DFSM_GRPH/ncl_scr/kwrf_end.log

wait