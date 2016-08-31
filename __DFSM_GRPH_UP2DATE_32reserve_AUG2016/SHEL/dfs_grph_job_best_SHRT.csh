#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set WEB_TOP  = /var/www/html 
set HOME_TOP  = /op/DFSM_GRPH
set parserABSPath = /op/DFSM_GRPH/EXEC
set JDOM_HOME  = /op/DFSM_GRPH/jdom
set MERG = /data/DFSD/SHRT/BEST
#--------------------------------------------------------------------

set yymmddhhnn = `echo $1`
#echo $yymmddhhnn > ${WEB_TOP}/dateTime.dat

set yy = `echo $1 | cut -c1-4`
set mm = `echo $1 | cut -c5-6`
set dd = `echo $1 | cut -c7-8`
set hh = `echo $1 | cut -c9-10`

#--------------------------------------------------------------------

set BEST = $MERG/$yy$mm/$dd 
# 2c.doeJeon
set stn_best = DFS_SHRT_STN_BEST_MERG

#---file name
#-(1) BEST
   set stn_best_t3h = $BEST/${stn_best}_T3H.$yymmddhhnn     
   set stn_best_tmx = $BEST/${stn_best}_TMX.$yymmddhhnn
   set stn_best_tmn = $BEST/${stn_best}_TMN.$yymmddhhnn
#   set stn_best_pop = $BEST/${stn_best}_POP.$yymmddhhnn
   set stn_best_reh = $BEST/${stn_best}_REH.$yymmddhhnn
   set stn_best_pty = $BEST/${stn_best}_PTY.$yymmddhhnn
   set stn_best_uuu = $BEST/${stn_best}_UUU.$yymmddhhnn
   set stn_best_vvv = $BEST/${stn_best}_VVV.$yymmddhhnn
   set stn_best_rn3 = $BEST/${stn_best}_RN3.$yymmddhhnn
   set stn_best_sn3 = $BEST/${stn_best}_SN3.$yymmddhhnn
   #set stn_best_r12 = $BEST/${stn_best}_R12.$yymmddhhnn
   #set stn_best_s12 = $BEST/${stn_best}_S12.$yymmddhhnn
   set stn_best_sky = $BEST/${stn_best}_SKY.$yymmddhhnn
   set stn_best_rn6 = $BEST/${stn_best}_RN6.$yymmddhhnn
   set stn_best_sn6 = $BEST/${stn_best}_SN6.$yymmddhhnn
#------------------------------------------------------------



#BEsT SHRT
    
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath Best_UVW2XML $stn_best_uuu $stn_best_vvv
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath Best_SingleVar2XML $stn_best_rn3 $stn_best_sn3 $stn_best_sky $stn_best_pty $stn_best_rn6  $stn_best_sn6 $stn_best_reh
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath Best_T3H2XML $stn_best_t3h $stn_best_tmx $stn_best_tmn

#echo  javac -encoding UTF8 -cp .:/op/DFSM_GRPH/jdom/build/jdom.jar:/op/DFSM_GRPH/EXEC Best_SingleVar2XML.java
#echo  javac -encoding UTF8 -cp .:/op/DFSM_GRPH/jdom/build/jdom.jar:/op/DFSM_GRPH/EXEC Best_T3H2XML.java
#echo  javac -encoding UTF8 -cp .:/op/DFSM_GRPH/jdom/build/jdom.jar:/op/DFSM_GRPH/EXEC Best_UVW2XML.java

#java -Dfile.encoding=UTF-8 -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath Best_UVW2XML $stn_best_uuu $stn_best_vvv

