#!/bin/bash

# In das Verzeichnis des Skripts wechseln, damit relative Pfade funktionieren
cd `dirname $0`

# Allgemeine Einstellungen
source ../../../skripte-bash/einstellungen.sh

################################################################################
# SWE-Spezifische Parameter	(ueberprüfen und anpassen)                       #
################################################################################

kb="kb.duaTestObjekte"

################################################################################
# Folgende Parameter müssen überprüft und evtl. angepasst werden               #
################################################################################

# Parameter für den Java-Interpreter, als Standard werden die Einstellungen aus # einstellungen.sh verwendet.
#jvmArgs="-Dfile.encoding=ISO-8859-1"

# Parameter für den Datenverteiler, als Standard werden die Einstellungen aus # einstellungen.sh verwendet.
#dav1="-datenverteiler=localhost:8083 -benutzer=Tester -authentifizierung=passwd -debugFilePath=.."

jconPort="10414"

if [ "$testlauf" ]; then
	jvmArgs=$jvmArgs" -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port="$jconPort" -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
fi

################################################################################
# Ab hier muss nichts mehr angepasst werden                                    #
################################################################################

# Applikation starten
java $jvmArgs -jar ../de.bsvrz.dua.plloglve-runtime.jar \
	$dav1 \
	-KonfigurationsBereichsPid=$kb \
	-test=vertrauen \
	-gueteFaktor=0.97 \
	-debugLevelStdErrText=ERROR \
	-debugLevelFileText=INFO \
	-debugLevelFileXML=OFF \
	-debugLevelFileExcel=OFF \
	-debugLevelFileHTML=OFF \
	&
