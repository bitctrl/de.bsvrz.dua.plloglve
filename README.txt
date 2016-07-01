*************************************************************************************
*  Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE  *
*************************************************************************************

Version: ${version}

Übersicht
=========

Aufgabe der SWE PL-Prüfung logisch LVE ist es, die logische PL-Prüfung der LVE für die
Kurzzeitdaten und Langzeitdaten durchzuführen. Diese erfolgt innerhalb der SWE für alle
vorgesehenen Objekte durch die Wertebereichsprüfung (PL-Prüfung formal) und einer nachfolgenden
logischen Plausibilitätsprüfung. Die logische Plausibilitätsprüfung untergliedert sich in
-	PL-Prüfung logisch für Kurzzeitdaten Verkehr und Langzeitdaten Verkehr,
-	Differenzialkontrolle von Fahrstreifendaten,
-	Ermittlung der Ausfallhäufigkeit von Fahrstreifendaten und
-	Ermittlung des Vertrauensbereichs von Fahrstreifendaten.
Eine genaue Beschreibung erfolgt in den [AFo]. Nach der Prüfung werden die Daten ggf. unter
einem parametrierbaren Aspekt publiziert.



Versionsgeschichte
==================

1.4.0
=====
- Umstellung auf Java 8 und UTF-8

1.3.0
- Umstellung auf Funclib-Bitctrl-Dua

1.2.2
- direkter Memberzugriff in Klassen der de.bsvrz.sys.funclib.bitctrl beseitigt

1.2.1
- BezugsZeitraum: Fehlerausgabe korrigiert für den Fall, dass die Einschaltschwelle überschritten war und 
  die Ausschaltschwelle noch nicht unterschritten wurde


1.2.0
- Umstellung auf Maven-Build

1.1.2

  - Senden von reinen Betriebsmeldungen in DUA um die Umsetzung von Objekt-PID/ID nach
    Betriebsmeldungs-ID erweitert.  

1.1.1

  - FIX 1588: Bezeichnung von Fahrstreifenobjekten in Betriebsmeldungen ueberarbeitet.

1.1.0

  - Neuer Aufrufparameter -altAnlagen=[Ja/Nein] eingebaut. Achtung: Dieser Aufrufparameter 
    muss in der Regel beim Aufruf der SWE Messwertersetzung LVE angegeben werden, da diese die 
    SWE Pl-Prüfung logisch LVE logisch automatisch mitstartet.

1.0.2

  - FIX: Sämtliche Konstruktoren DataDescription(atg, asp, sim) ersetzt durch
         DataDescription(atg, asp)

1.0.0b

  - Erste Auslieferung (beta, nur teilweise nach Prüfspezifikation getestet)

1.0.0

  - Erste vollständige Auslieferung


Bemerkungen
===========

Diese SWE ist eine eigenständige Datenverteiler-Applikation, welche über die Klasse
de.bsvrz.dua.plloglve.vew.VerwaltungPlPruefungLogischLVE mit folgenden Parametern gestartet werden kann
(zusaetzlich zu den normalen Parametern jeder Datenverteiler-Applikation):
	-KonfigurationsBereichsPid=pid(,pid)

Der Datenfluss durch diese Applikation ist wie folgt:
externe Erfassung 
	--> formale Prüfung (Publikation asp.plPrüfungFormal) 
		-->	logische Prüfung (Publikation unter asp.plPrüfungLogisch)
	
- Tests:

	Die automatischen Tests, die in Zusammenhang mit der Prüfspezifikation durchgeführt
	werden, sind noch nicht endgültig implementiert.

- Logging-Hierarchie (Wann wird welche Art von Logging-Meldung produziert?):

	ERROR:
	- DUAInitialisierungsException --> Beendigung der Applikation
	- Fehler beim An- oder Abmelden von Daten beim Datenverteiler
	- Interne unerwartete Fehler
	
	WARNING:
	- Fehler, die die Funktionalität grundsätzlich nicht
	  beeinträchtigen, aber zum Datenverlust führen können
	- Nicht identifizierbare Konfigurationsbereiche
	- Probleme beim Explorieren von Attributpfaden 
	  (von Plausibilisierungsbeschreibungen)
	- Wenn mehrere Objekte eines Typs vorliegen, von dem
	  nur eine Instanz erwartet wird
	- Wenn Parameter nicht korrekt ausgelesen werden konnten
	  bzw. nicht interpretierbar sind
	- Wenn inkompatible Parameter übergeben wurden
	- Wenn Parameter unvollständig sind
	- Wenn ein Wert bzw. Status nicht gesetzt werden konnte
	
	INFO:
	- Wenn neue Parameter empfangen wurden
	
	CONFIG:
	- Allgemeine Ausgaben, welche die Konfiguration betreffen
	- Benutzte Konfigurationsbereiche der Applikation bzw.
	  einzelner Funktionen innerhalb der Applikation
	- Benutzte Objekte für Parametersteuerung von Applikationen
	  (z.B. die Instanz der Datenflusssteuerung, die verwendet wird)
	- An- und Abmeldungen von Daten beim Datenverteiler
	
	FINE:
	- Wenn Daten empfangen wurden, die nicht weiterverarbeitet 
	  (plausibilisiert) werden können (weil keine Parameter vorliegen)
	- Informationen, die nur zum Debugging interessant sind 
	

Disclaimer
==========

Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
Copyright (C) 2007 BitCtrl Systems GmbH 

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51
Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.


Kontakt
=======

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
