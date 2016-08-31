#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set WEB_TOP  = /var/www/html 

set EXT_LIB  = /op/DFSM_GRPH/EXT_LIB

set PATH_XMLOutputter  = /op/DFSM_GRPH/EXEC

set SHEL_HOME = /op/DFSM_GRPH/SHEL


set SSPS = /data/DFSD/SHRT/SSPS
set JDOM_HOME  = /op/DFSM_GRPH/jdom



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
set SOURCEDir = $SSPS/$yy$mm/$dd
set stn_ssps = DFS_SHRT_STN_SSPS



#---file name
   set stn_ssps_t3h = $SOURCEDir/${stn_ssps}_T3H.$yymmddhhnn        # ECMWF
   set stn_ssps_reh = $SOURCEDir/${stn_ssps}_REH.$yymmddhhnn
   set stn_ssps_pty = $SOURCEDir/${stn_ssps}_PTY.$yymmddhhnn
   set stn_ssps_uuu = $SOURCEDir/${stn_ssps}_UUU.$yymmddhhnn
   set stn_ssps_vvv = $SOURCEDir/${stn_ssps}_VVV.$yymmddhhnn
   set stn_ssps_rn3 = $SOURCEDir/${stn_ssps}_RN3.$yymmddhhnn
   set stn_ssps_sn3 = $SOURCEDir/${stn_ssps}_SN3.$yymmddhhnn
   set stn_ssps_rn6 = $SOURCEDir/${stn_ssps}_RN6.$yymmddhhnn
   set stn_ssps_sn6 = $SOURCEDir/${stn_ssps}_SN6.$yymmddhhnn
   set stn_ssps_sky = $SOURCEDir/${stn_ssps}_SKY.$yymmddhhnn
   #me.2C.doeJeon 24Nov2015
   set stn_ssps_vis = $SOURCEDir/${stn_ssps}_VIS.$yymmddhhnn

#------------------------------------------------------------

    java -cp .:$JDOM_HOME/build/jdom.jar:$EXT_LIB/jaxen.jar:$PATH_XMLOutputter SSPS_ToXML $stn_ssps_t3h $stn_ssps_reh  $stn_ssps_rn3 $stn_ssps_sn3 $stn_ssps_sky $stn_ssps_pty $stn_ssps_rn6 $stn_ssps_sn6 $stn_ssps_vis
    java -cp .:$JDOM_HOME/build/jdom.jar:$EXT_LIB/jaxen.jar:$PATH_XMLOutputter SSPS_UVW $stn_ssps_uuu $stn_ssps_vvv

#echo  javac -cp .:/op/DFSM_GRPH/jdom/build/jdom.jar:/op/DFSM_GRPH/EXT_LIB/jaxen.jar:/op/DFSM_GRPH/EXEC SSPS_UVW.java
