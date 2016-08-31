#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set MRFCST_HOME  = /op/www/html/MRFCST
set parserABSPath = /op/DFSM_GRPH/EXEC
set EXT_LIB  = /op/DFSM_GRPH/EXT_LIB
set BLTN = /data/DFSD/MEDM/BLTN
#--------------------------------------------------------------------

set yymmddhhnn = `echo $1`

set yy = `echo $1 | cut -c1-4`
set mm = `echo $1 | cut -c5-6`
set dd = `echo $1 | cut -c7-8`
set hh = `echo $1 | cut -c9-10`

#--------------------------------------------------------------------

set TBL = $BLTN/$yy$mm/$dd 
# 2c.doeJeon

#---file name
#- 1.SPY 2.TMXN
   set TBL_SPY = $TBL/DFS_MEDM_GRP_EPSG_BLTN_SPY.$yymmddhhnn     
   set TBL_TMX = $TBL/DFS_MEDM_STN_EPSG_BLTN_TMXN.$yymmddhhnn
#------------------------------------------------------------

echo $TBL_SPY
echo $TBL_TMX

#MediumRangeFCSTTable.
#javac -encoding UTF8 -cp path/to/some/stuff    

java -Dfile.encoding=UTF-8 -classpath .:$EXT_LIB/jdom.jar:$EXT_LIB/json-simple-1.1.1.jar:$parserABSPath MediumRangeFCSTDataJSONPopulator $TBL_SPY $TBL_TMX

set yyyymmddhh = `echo $1 | cut -c1-10`
echo $yyyymmddhh > ${MRFCST_HOME}/latestStuff.dat


