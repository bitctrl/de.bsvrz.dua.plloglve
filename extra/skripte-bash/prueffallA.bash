#!/bin/bash
source ../../../skripte-bash/einstellungen.sh

echo =================================================
echo =
echo =       Pruefungen SE4 - DUA, SWE 4.2
echo =
echo =================================================
echo 

index=0
declare -a tests
declare -a testTexts

#########################
# Name der Applikation #
#########################
appname=plloglve

########################
#     Testroutinen     #
########################

testTexts[$index]="Testet die Standandkontrollen inkl. Differenzialkontrolle"
tests[$index]="test.PlPruefungLogischLVETestStandardDiff"
index=$(($index+1))


########################
#      ClassPath       #
########################
cp="../../de.bsvrz.sys.funclib.bitctrl/de.bsvrz.sys.funclib.bitctrl-runtime.jar"
cp=$cp":../de.bsvrz.dua."$appname"-runtime.jar"
cp=$cp":../de.bsvrz.dua."$appname"-test.jar"
cp=$cp":../../junit-4.1.jar"

########################
#     Ausfuehrung      #
########################

for ((i=0; i < ${#tests[@]}; i++));
do
	echo "================================================="
	echo "="
	echo "= Test Nr. "$(($i+1))":"
	echo "="
	echo "= "${testTexts[$i]}
	echo "="
	echo "================================================="
	echo 
	java -cp $cp $jvmArgs org.junit.runner.JUnitCore "de.bsvrz.dua."$appname"."${tests[$i]}
	sleep 5
done

exit
