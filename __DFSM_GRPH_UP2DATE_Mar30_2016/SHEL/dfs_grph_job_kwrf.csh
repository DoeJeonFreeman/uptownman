#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set WEB_TOP  = /var/www/html 
set HOME_TOP  = /op/DFSM_GRPH
set parserABSPath = /op/DFSM_GRPH/EXEC

set JDOM_HOME  = /op/DFSM_GRPH/jdom

#set NPPM = /s0031/DFSD/SHRT/NPPM
 set NPPM = /data/DFSD/SHRT/NPPM

#--------------------------------------------------------------------

#set yymmddhhnn = `cut -c1-12 ${WEB_TOP}/dateTime.dat`

set yymmddhhnn = `echo $1`

echo $yymmddhhnn > ${WEB_TOP}/dateTime.dat

set yy = `echo $1 | cut -c1-4`
set mm = `echo $1 | cut -c5-6`
set dd = `echo $1 | cut -c7-8`
set hh = `echo $1 | cut -c9-10`

#--------------------------------------------------------------------

set KWRF = $NPPM/$yy$mm/$dd 

set stn_kwrf = DFS_SHRT_STN_KWRF_NPPM
set grd_kwrf = DFS_SHRT_GRD_KWRF_NPPM


#---file name
#-(1) KWRF
   set stn_kwrf_t3h = $KWRF/${stn_kwrf}_T3H.$yymmddhhnn        # RDAPS(WRF 10km L40)
   set stn_kwrf_tmx = $KWRF/${stn_kwrf}_TMX.$yymmddhhnn                    
   set stn_kwrf_tmn = $KWRF/${stn_kwrf}_TMN.$yymmddhhnn
   set stn_kwrf_pop = $KWRF/${stn_kwrf}_POP.$yymmddhhnn
   set stn_kwrf_reh = $KWRF/${stn_kwrf}_REH.$yymmddhhnn
   set stn_kwrf_pty = $KWRF/${stn_kwrf}_PTY.$yymmddhhnn
   set stn_kwrf_uuu = $KWRF/${stn_kwrf}_UUU.$yymmddhhnn
   set stn_kwrf_vvv = $KWRF/${stn_kwrf}_VVV.$yymmddhhnn
   set stn_kwrf_rn3 = $KWRF/${stn_kwrf}_RN3.$yymmddhhnn
   set stn_kwrf_sn3 = $KWRF/${stn_kwrf}_SN3.$yymmddhhnn
   set stn_kwrf_r12 = $KWRF/${stn_kwrf}_R12.$yymmddhhnn
   set stn_kwrf_s12 = $KWRF/${stn_kwrf}_S12.$yymmddhhnn
   set stn_kwrf_sky = $KWRF/${stn_kwrf}_SKY.$yymmddhhnn
   #doeJeon May 21, 2013
   set stn_kwrf_rn6 = $KWRF/${stn_kwrf}_RN6.$yymmddhhnn
   set stn_kwrf_sn6 = $KWRF/${stn_kwrf}_SN6.$yymmddhhnn
   #doeJeon May 21, 2013


   set grn_kwrf_t3h = $KWRF/${grd_kwrf}_T3H.$yymmddhhnn       # RDAPS(WRF 10km L40)
   set grd_kwrf_tmx = $KWRF/${grd_kwrf}_TMX.$yymmddhhnn
   set grd_kwrf_tmn = $KWRF/${grd_kwrf}_TMN.$yymmddhhnn
   set grd_kwrf_pop = $KWRF/${grd_kwrf}_POP.$yymmddhhnn
   set grd_kwrf_reh = $KWRF/${grd_kwrf}_REH.$yymmddhhnn
   set grd_kwrf_pty = $KWRF/${grd_kwrf}_PTY.$yymmddhhnn
   set grd_kwrf_uuu = $KWRF/${grd_kwrf}_UUU.$yymmddhhnn
   set grd_kwrf_vvv = $KWRF/${grd_kwrf}_VVV.$yymmddhhnn
   set grd_kwrf_rn3 = $KWRF/${grd_kwrf}_RN3.$yymmddhhnn
   set grd_kwrf_sn3 = $KWRF/${grd_kwrf}_SN3.$yymmddhhnn
   set grd_kwrf_r12 = $KWRF/${grd_kwrf}_R12.$yymmddhhnn
   set grd_kwrf_s12 = $KWRF/${grd_kwrf}_S12.$yymmddhhnn
   set grd_kwrf_sky = $KWRF/${grd_kwrf}_SKY.$yymmddhhnn

#------------------------------------------------------------

#KWRF 
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath RawData_ToXML $stn_kwrf_pop $stn_kwrf_reh $stn_kwrf_rn3 $stn_kwrf_sn3 $stn_kwrf_sky $stn_kwrf_pty $stn_kwrf_r12 $stn_kwrf_s12 $stn_kwrf_rn6 $stn_kwrf_sn6
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath T3H_ToXML $stn_kwrf_t3h $stn_kwrf_tmx $stn_kwrf_tmn
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath WSD_ToXML $stn_kwrf_uuu $stn_kwrf_vvv

