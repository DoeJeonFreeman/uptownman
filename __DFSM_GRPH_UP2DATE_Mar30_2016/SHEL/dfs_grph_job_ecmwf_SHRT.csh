#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set WEB_TOP  = /var/www/html 
set HOME_TOP  = /op/DFSM_GRPH
set parserABSPath = /op/DFSM_GRPH/EXEC

set JDOM_HOME  = /op/DFSM_GRPH/jdom

#set NPPM = /s0031/DFSD/SHRT/NPPM
#set MOSM = /s0031/DFSD/SHRT/MOSM
 set NPPM = /data/DFSD/SHRT/NPPM
 set MOSM = /data/DFSD/SHRT/MOSM

#--------------------------------------------------------------------

#set yymmddhhnn = `cut -c1-12 ${WEB_TOP}/dateTime.dat`

set yymmddhhnn = `echo $1`

echo $yymmddhhnn > ${WEB_TOP}/dateTime.dat

set yy = `echo $1 | cut -c1-4`
set mm = `echo $1 | cut -c5-6`
set dd = `echo $1 | cut -c7-8`
set hh = `echo $1 | cut -c9-10`

#--------------------------------------------------------------------

# 2c.doeJeon
# May 14, 2013
# add ECMWF(excludes grd)
set ECMWF = $NPPM/$yy$mm/$dd
set stn_ecmwf = DFS_SHRT_STN_ECMW_NPPM



#---file name
   set stn_ecmwf_t3h = $ECMWF/${stn_ecmwf}_T3H.$yymmddhhnn        # ECMWF
   set stn_ecmwf_tmx = $ECMWF/${stn_ecmwf}_TMX.$yymmddhhnn
   set stn_ecmwf_tmn = $ECMWF/${stn_ecmwf}_TMN.$yymmddhhnn
   set stn_ecmwf_reh = $ECMWF/${stn_ecmwf}_REH.$yymmddhhnn
   set stn_ecmwf_pty = $ECMWF/${stn_ecmwf}_PTY.$yymmddhhnn
   set stn_ecmwf_uuu = $ECMWF/${stn_ecmwf}_UUU.$yymmddhhnn
   set stn_ecmwf_vvv = $ECMWF/${stn_ecmwf}_VVV.$yymmddhhnn
   set stn_ecmwf_rn3 = $ECMWF/${stn_ecmwf}_RN3.$yymmddhhnn
   set stn_ecmwf_sn3 = $ECMWF/${stn_ecmwf}_SN3.$yymmddhhnn
   set stn_ecmwf_rn6 = $ECMWF/${stn_ecmwf}_RN6.$yymmddhhnn
   set stn_ecmwf_sn6 = $ECMWF/${stn_ecmwf}_SN6.$yymmddhhnn
   set stn_ecmwf_sky = $ECMWF/${stn_ecmwf}_SKY.$yymmddhhnn

#------------------------------------------------------------



#ECMWF
#ECMWF
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath RawData_ToXML  $stn_ecmwf_reh $stn_ecmwf_rn3 $stn_ecmwf_sn3 $stn_ecmwf_sky $stn_ecmwf_pty $stn_ecmwf_rn6 $stn_ecmwf_sn6
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath T3H_ToXML $stn_ecmwf_t3h $stn_ecmwf_tmx $stn_ecmwf_tmn
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath WSD_ToXML $stn_ecmwf_uuu $stn_ecmwf_vvv

