#!/bin/csh
set PATH=/usr/local/bin:/usr/lib64/qt-3.3/bin:/usr/kerberos/sbin:/usr/kerberos/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/op/bin:/usr/local/bin:/usr/java/jdk1.6.0_33/bin
setenv DISPLAY 172.20.134.31:0.0
setenv WEB_HOME "/var/www/html"
set SHELL_HOME=/op/DFSM_GRPH/SHEL
source $SHELL_HOME/conf.csh

echo ' start shell for town forecast graphic....' 
#echo $1 > org.out
set e_s=00
set tday=$1
set rdate=$tday$e_s

echo "start " >> /op/DFSM_GRPH/ncl_scr/kwrf_end.log
date >> /op/DFSM_GRPH/ncl_scr/kwrf_end.log
#echo $rdate > check_date.out
#echo $tday > check_date2.out
set day = `echo $rdate |cut -c 7-8 `
cd $SHELL_HOME
echo $day >>  $rdate.check
#ls -al /s0031/DFSD/SHRT/MOSM/201305/$day >> $rdate.check
$SHELL_HOME/dfs_grph_job_gdps.csh $rdate
$SHELL_HOME/dfs_grph_job_kwrf.csh $rdate
$SHELL_HOME/dfs_grph_job_um12km.csh $rdate
echo $rdate 
echo $tday 

set cmd_opt="rundate="\"${rdate}\"" "model=\"KWRF\"
set cmd_rdp="rundate="\"${rdate}\"" "model=\"RDPS\"
echo $cmd_rdp 
echo "testing home town"
echo $HOME_TOWN
cd $HOME_TOWN
pwd
echo "Pop ncl run" 
ncl grid_pop.ncl $cmd_rdp &
rm /var/www/html/dateTime.dat
echo $rdate > $WEB_HOME\/dateTime.dat
date >> /op/DFSM_GRPH/ncl_scr/kwrf_end.log

wait

