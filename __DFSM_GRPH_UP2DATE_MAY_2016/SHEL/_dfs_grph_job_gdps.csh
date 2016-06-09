#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set WEB_TOP  = /var/www/html
set HOME_TOP  = /op/DFSM_GRPH
set parserABSPath = /op/DFSM_GRPH/EXEC

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

set GDPS = $NPPM/$yy$mm/$dd
set PMOS2 = $MOSM/$yy$mm/$dd

set stn_gdps = DFS_MEDM_STN_GDPS_NPPM
set grd_gdps = DFS_MEDM_GRD_GDPS_NPPM

set stn_pmos2 = DFS_MEDM_STN_GDPS_PMOS

#---file name
#-(1) GDPS
   set stn_gdps_tmx = $GDPS/${stn_gdps}_TMX.$yymmddhhnn                    
   set stn_gdps_tmn = $GDPS/${stn_gdps}_TMN.$yymmddhhnn
   set stn_gdps_pop = $GDPS/${stn_gdps}_POP.$yymmddhhnn
   set stn_gdps_pty = $GDPS/${stn_gdps}_PTY.$yymmddhhnn
   set stn_gdps_sky = $GDPS/${stn_gdps}_SKY.$yymmddhhnn
#2c.Doe Nov14, 2012
   set stn_gdps_r12 = $GDPS/${stn_gdps}_R12.$yymmddhhnn
   set stn_gdps_s12 = $GDPS/${stn_gdps}_S12.$yymmddhhnn
#---file name
#-(2) PMOS2 
   set stn_pmos2_tmx = $PMOS2/${stn_pmos2}_TMX.$yymmddhhnn                    
   set stn_pmos2_tmn = $PMOS2/${stn_pmos2}_TMN.$yymmddhhnn                    
#------------------------------------------------------------

#GDAPS

    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath RawData_ToXML $stn_gdps_pop $stn_gdps_sky $stn_gdps_pty $stn_gdps_r12 $stn_gdps_s12 
#    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath TMM_ToXML $stn_gdps_tmn $stn_gdps_tmx

#PMOS2 - MOSM weekly
    java -classpath .:$JDOM_HOME/build/jdom.jar:$parserABSPath TMM_ToXML $stn_pmos2_tmn $stn_pmos2_tmx
