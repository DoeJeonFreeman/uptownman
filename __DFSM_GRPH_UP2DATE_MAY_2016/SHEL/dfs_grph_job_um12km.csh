#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set WEB_TOP  = /var/www/html 
set HOME_TOP  = /op/DFSM_GRPH
set parserABSPath = /op/DFSM_GRPH/EXEC

set JDOM_HOME  = /op/DFSM_GRPH/jdom

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

set UM12 = $NPPM/$yy$mm/$dd 
set PMOS = $MOSM/$yy$mm/$dd 
# 2c.doeJeon
# May 14, 2013
# add ECMWF(excludes grd)

set stn_rdps = DFS_SHRT_STN_RDPS_NPPM
set stn_pmos = DFS_SHRT_STN_RDPS_PMOS



#---file name
#-(1) RDPS
   set stn_rdps_t3h = $UM12/${stn_rdps}_T3H.$yymmddhhnn      # RDAPS(UM 12km L70)
   set stn_rdps_tmx = $UM12/${stn_rdps}_TMX.$yymmddhhnn
   set stn_rdps_tmn = $UM12/${stn_rdps}_TMN.$yymmddhhnn
   set stn_rdps_pop = $UM12/${stn_rdps}_POP.$yymmddhhnn
   set stn_rdps_reh = $UM12/${stn_rdps}_REH.$yymmddhhnn
   set stn_rdps_pty = $UM12/${stn_rdps}_PTY.$yymmddhhnn
   set stn_rdps_uuu = $UM12/${stn_rdps}_UUU.$yymmddhhnn
   set stn_rdps_vvv = $UM12/${stn_rdps}_VVV.$yymmddhhnn
   set stn_rdps_rn3 = $UM12/${stn_rdps}_RN3.$yymmddhhnn
   set stn_rdps_sn3 = $UM12/${stn_rdps}_SN3.$yymmddhhnn
   set stn_rdps_r12 = $UM12/${stn_rdps}_R12.$yymmddhhnn
   set stn_rdps_s12 = $UM12/${stn_rdps}_S12.$yymmddhhnn
   set stn_rdps_sky = $UM12/${stn_rdps}_SKY.$yymmddhhnn
   #doeJeon May 21, 2013
   set stn_rdps_rn6 = $UM12/${stn_rdps}_RN6.$yymmddhhnn
   set stn_rdps_sn6 = $UM12/${stn_rdps}_SN6.$yymmddhhnn
   #doeJeon May 21, 2013



#-(2) PMOS
   set stn_pmos_t3h = $PMOS/${stn_pmos}_T3H.$yymmddhhnn        # MOS(UM 12km L70)
   set stn_pmos_tmx = $PMOS/${stn_pmos}_TMX.$yymmddhhnn
   set stn_pmos_tmn = $PMOS/${stn_pmos}_TMN.$yymmddhhnn
   set stn_pmos_pop = $PMOS/${stn_pmos}_POP.$yymmddhhnn
   set stn_pmos_reh = $PMOS/${stn_pmos}_REH.$yymmddhhnn
   set stn_pmos_pty = $PMOS/${stn_pmos}_PTY.$yymmddhhnn
   set stn_pmos_uuu = $PMOS/${stn_pmos}_UUU.$yymmddhhnn
   set stn_pmos_vvv = $PMOS/${stn_pmos}_VVV.$yymmddhhnn 
   set stn_pmos_sky = $PMOS/${stn_pmos}_SKY.$yymmddhhnn 
   set stn_pmos_s12 = $PMOS/${stn_pmos}_S12.$yymmddhhnn 
   set stn_pmos_sn6 = $PMOS/${stn_pmos}_SN6.$yymmddhhnn  #doeJeon May 21, 2013  



#------------------------------------------------------------






#RDAPS
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath RawData_ToXML $stn_rdps_pop $stn_rdps_reh $stn_rdps_rn3 $stn_rdps_sn3 $stn_rdps_sky $stn_rdps_pty $stn_rdps_r12 $stn_rdps_s12 $stn_rdps_rn6  $stn_rdps_sn6
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath T3H_ToXML $stn_rdps_t3h $stn_rdps_tmx $stn_rdps_tmn
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath WSD_ToXML $stn_rdps_uuu $stn_rdps_vvv

#PMOS
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath RawData_ToXML $stn_pmos_pop $stn_pmos_reh $stn_pmos_sky $stn_pmos_pty $stn_pmos_s12 $stn_pmos_sn6
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath WSD_ToXML $stn_pmos_uuu $stn_pmos_vvv
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath T3H_ToXML $stn_pmos_t3h $stn_pmos_tmx $stn_pmos_tmn

