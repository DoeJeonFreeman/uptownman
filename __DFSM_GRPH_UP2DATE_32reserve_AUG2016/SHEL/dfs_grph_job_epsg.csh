#!/bin/csh -f
#--------------------------------------------------------------------
###DEFINE DIRECTORY
#--------------------------------------------------------------------
set WEB_ROOT  = /var/www/html

set PATH_COMBINER = /op/DFSM_GRPH/EXEC/MEDM

set EXT_LIB  = /op/DFSM_GRPH/EXT_LIB

set MOSM = /data/DFSD/MEDM/MOSM

set SHEL_HOME = /op/DFSM_GRPH/SHEL 
#--------------------------------------------------------------------

#set yymmddhhmm = `cut -c1-12 ${WEB_ROOT}/date.ensemble`

set yymmddhhmm = `echo $1`

#COMMENT OUB BY ME
#echo $yymmddhhmm > ${WEB_ROOT}/dateTime.dat

set yy = `echo $1 | cut -c1-4`
set mm = `echo $1 | cut -c5-6`
set dd = `echo $1 | cut -c7-8`
set hh = `echo $1 | cut -c9-10`

#--------------------------------------------------------------------

set sourceDir = $MOSM/$yy$mm/$dd

# 2c.doeJeon
# May 14, 2013
# add ECMWF(excludes grd)


#MOSM GDPS ENSEMBLE
    cd $SHEL_HOME

    java -cp .:$EXT_LIB/jdom2.jar:$EXT_LIB/jaxen.jar:$PATH_COMBINER SomeStuff2XML $sourceDir $yymmddhhmm CLD
    java -cp .:$EXT_LIB/jdom2.jar:$EXT_LIB/jaxen.jar:$PATH_COMBINER SomeStuff2XML $sourceDir $yymmddhhmm PTY
    java -cp .:$EXT_LIB/jdom2.jar:$EXT_LIB/jaxen.jar:$PATH_COMBINER SomeStuff2XML $sourceDir $yymmddhhmm R12
    java -cp .:$EXT_LIB/jdom2.jar:$EXT_LIB/jaxen.jar:$PATH_COMBINER MinMax2XML $sourceDir $yymmddhhmm MMX
	
    java -cp .:$EXT_LIB/jdom2.jar::$EXT_LIB/jaxen.jar:$PATH_COMBINER EnsembleMemberCombiner $yymmddhhmm
	
  # echo  java -cp .:$EXT_LIB/jdom2.jar:$EXT_LIB/jaxen.jar:$PATH_COMBINER MinMaxToXML $sourceDir $yymmddhhmm MMX 
 #  echo java -cp .:$EXT_LIB/jdom2.jar:$EXT_LIB/jaxen.jar:$PATH_COMBINER EnsembleMemberCombiner $yymmddhhmm
 
