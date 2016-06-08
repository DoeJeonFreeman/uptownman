#!/bin/csh
set PATH=/usr/local/bin:/usr/lib64/qt-3.3/bin:/usr/kerberos/sbin:/usr/kerberos/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/op/bin:/usr/local/bin:/usr/java/jdk1.6.0_33/bin
setenv WEB_HOME "/var/www/html"
set SHELL_HOME=/op/DFSM_GRPH/SHEL
source $SHELL_HOME/conf.csh

echo ' start shell for town forecast MRFCST table data...' 
set e_s=00
set tday=$1
set rdate=$tday$e_s

cd $SHELL_HOME
$SHELL_HOME/dfs_grph_job_bltn.csh $rdate
echo $tday 

