#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set WEB_TOP  = /var/www/html
set HOME_TOP  = /op/DFSM_GRPH
set parserABSPath = /op/DFSM_GRPH/EXEC/MEDM

set JDOM_HOME  = /op/DFSM_GRPH/jdom

#set NPPM = /s0031/DFSD/MEDM/NPPM
#set MOSM = /s0031/DFSD/MEDM/MOSM
 set NPPM = /data/DFSD/MEDM/NPPM
 set MOSM = /data/DFSD/MEDM/MOSM

#--------------------------------------------------------------------

#set yymmddhhnn = `cut -c1-12 ${WEB_TOP}/dateTime.dat`

set yymmddhhnn = `echo $1`

#echo $yymmddhhnn > ${WEB_TOP}/dateTime.dat

set yy = `echo $1 | cut -c1-4`
set mm = `echo $1 | cut -c5-6`
set dd = `echo $1 | cut -c7-8`
set hh = `echo $1 | cut -c9-10`

#--------------------------------------------------------------------


# 2c.doeJeon
# May 14, 2013
# add ECMWF(excludes grd)
set ECMWF = $NPPM/$yy$mm/$dd
set stn_ecmwf = DFS_MEDM_STN_ECMW_NPPM


# ECMWF
   set stn_ecmwf_tmx = $ECMWF/${stn_ecmwf}_TMX.$yymmddhhnn
   set stn_ecmwf_tmn = $ECMWF/${stn_ecmwf}_TMN.$yymmddhhnn

   set stn_ecmwf_pty = $ECMWF/${stn_ecmwf}_PTY.$yymmddhhnn
   set stn_ecmwf_sky = $ECMWF/${stn_ecmwf}_SKY.$yymmddhhnn

   set stn_ecmwf_r12 = $ECMWF/${stn_ecmwf}_R12.$yymmddhhnn
   set stn_ecmwf_s12 = $ECMWF/${stn_ecmwf}_S12.$yymmddhhnn





#ECMWF
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath RawData_ToXML  $stn_ecmwf_sky $stn_ecmwf_pty $stn_ecmwf_r12 $stn_ecmwf_s12

    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath TMM_ToXML $stn_ecmwf_tmn $stn_ecmwf_tmx

