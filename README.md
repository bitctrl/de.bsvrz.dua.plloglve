[![Build Status](https://travis-ci.org/bitctrl/de.bsvrz.dua.plloglve.svg?branch=master)](https://travis-ci.org/bitctrl/de.bsvrz.dua.plloglve)
[![Build Status](https://api.bintray.com/packages/bitctrl/maven/de.bsvrz.dua.plloglve/images/download.svg)](https://bintray.com/bitctrl/maven/de.bsvrz.dua.plloglve)

# Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE

Version: ${version}

## Übersicht

Aufgabe der SWE PL-Prüfung logisch LVE ist es, die logische PL-Prüfung der LVE für die
Kurzzeitdaten und Langzeitdaten durchzuführen. Diese erfolgt innerhalb der SWE für alle
vorgesehenen Objekte durch die Wertebereichsprüfung (PL-Prüfung formal) und einer nachfolgenden
logischen Plausibilitätsprüfung. Die logische Plausibilitätsprüfung untergliedert sich in

- PL-Prüfung logisch für Kurzzeitdaten Verkehr und Langzeitdaten Verkehr,
- Differenzialkontrolle von Fahrstreifendaten,
- Ermittlung der Ausfallhäufigkeit von Fahrstreifendaten und
- Ermittlung des Vertrauensbereichs von Fahrstreifendaten.

Eine genaue Beschreibung erfolgt in den [AFo]. Nach der Prüfung werden die Daten ggf. unter
einem parametrierbaren Aspekt publiziert.


## Versionsgeschichte

### 2.0.2

Releasedatum: 22.07.2016

- Umpacketierung gemäß NERZ-Konvention
  
### 2.0.1

Release-Datum: 25.06.2016

#### Fehlerkorrekturen

Folgende Fehler gegenüber vorhergehenden Versionen wurden korrigiert:

- Bei der Gutmeldung der Vertrauensbereichsprüfung (Betriebsmeldung
  DUA-PP-VB02) kam es zu einer Exception, wenn die neue Aufalldauer 0 war.

### 2.0.0

Release-Datum: 31.05.2016

#### Neue Abhängigkeiten

Die SWE benötigt nun das Distributionspaket de.bsvrz.sys.funclib.bitctrl.dua
in Mindestversion 1.5.0 und de.bsvrz.sys.funclib.bitctrl in Mindestversion 1.4.0,
sowie die Kernsoftware in Mindestversion 3.8.0.

#### Datenmodelländerungen

Folgende Änderungen an Konfigurationsbereichen wurden durchgeführt:
- kb.tmVerkehrGlobal Version 59
– Neue Parameterattributgruppe für die Differenzialkontrolle:
  atg.verkehrsDatenDifferenzialKontrolleFs2. Die bisherige Attributgruppe
 (ohne 2 am Ende) wird nicht mehr verwendet und bleibt aus
  Kompatibilitätsgründen bestehen.
– Neue Parameterattributgruppe für die anderen PL-Prüfungen:
  atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2.
  Die bisherige Attributgruppe (ohne 2 am Ende) wird nicht mehr verwendet,
  genauso wie die Attributgruppen atg.verkehrsDatenAusfallHäufigkeitFs
  und atg.verkehrsDatenVertrauensBereichFs.
– Die Parameterattributgruppe atg.verkehrsLageVerfahren1 wurde im die
  Attribute k3 und kT (analog zu Verfahren2) ergänzt.

#### Änderungen

Folgende Änderungen gegenüber vorhergehenden Versionen wurden durchgeführt:

- Überarbeitung der erzeugten Betriebsmeldungen gemäß neuen Anwenderforderungen.
- Verwendung der neuen Parameterdatensätze
  – atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2
    und
  – atg.verkehrsDatenDifferenzialKontrolleFs2 statt der bisherigen Parameter.
- Anpassung der Prüfungen Ausfallhäufigkeit und Vertrauensbereich, sodass diese
  das parametrierte Prüfintervall verwenden und nicht bei jedem eintreffenden Datensatz
  durchgeführt werden.
- Bei der Ausfallhäufigkeit werden nicht mehr die Attribute b und s geprüft.
- Komplette Überarbeitung der Grenzwertprüfung gemäß Anwenderforderungen.
- Die Differenzialkontrolle verwendet jetzt ein einheitliches Parameterattribut für
  Verkehrsmengen und Geschwindigkeiten und die Werte werden nur dann als konstant
  betrachtet wenn im betreffenden Zeitbereich alle Verkehrsmengenwerte oder
  alle Geschwindigkeiten konstant geblieben sind.
- Alle Prüfungen (KEx–15), die der SWE KEx-TLS zugeordnet sind, wurden aus
  der SWE entfernt
- Überarbeitung der Güte-Berechnungen.

### 1.4.0

- Umstellung auf Java 8 und UTF-8

### 1.3.0

- Umstellung auf Funclib-Bitctrl-Dua

### 1.2.2

- direkter Memberzugriff in Klassen der de.bsvrz.sys.funclib.bitctrl beseitigt

### 1.2.1

- BezugsZeitraum: Fehlerausgabe korrigiert für den Fall, dass die Einschaltschwelle überschritten war und 
  die Ausschaltschwelle noch nicht unterschritten wurde

### 1.2.0

- Umstellung auf Maven-Build

### 1.1.2

- Senden von reinen Betriebsmeldungen in DUA um die Umsetzung von Objekt-PID/ID nach
  Betriebsmeldungs-ID erweitert.  

### 1.1.1

- FIX 1588: Bezeichnung von Fahrstreifenobjekten in Betriebsmeldungen ueberarbeitet.

### 1.1.0

- Neuer Aufrufparameter -altAnlagen=[Ja/Nein] eingebaut. Achtung: Dieser Aufrufparameter 
  muss in der Regel beim Aufruf der SWE Messwertersetzung LVE angegeben werden, da diese die 
  SWE Pl-Prüfung logisch LVE logisch automatisch mitstartet.

### 1.0.2

- FIX: Sämtliche Konstruktoren DataDescription(atg, asp, sim) ersetzt durch
       DataDescription(atg, asp)

### 1.0.0

- Erste vollständige Auslieferung

### 1.0.0b

- Erste Auslieferung (beta, nur teilweise nach Prüfspezifikation getestet)


## Bemerkungen

Diese SWE ist eine eigenständige Datenverteiler-Applikation, welche über die Klasse
de.bsvrz.dua.plloglve.vew.VerwaltungPlPruefungLogischLVE mit folgenden Parametern gestartet werden kann
(zusaetzlich zu den normalen Parametern jeder Datenverteiler-Applikation):
	-KonfigurationsBereichsPid=pid(,pid)

Der Datenfluss durch diese Applikation ist wie folgt:
externe Erfassung 
	--> formale Prüfung (Publikation asp.plPrüfungFormal) 
		-->	logische Prüfung (Publikation unter asp.plPrüfungLogisch)
	


## Kontakt

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
