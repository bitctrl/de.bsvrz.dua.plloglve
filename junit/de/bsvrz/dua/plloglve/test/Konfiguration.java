/** 
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.plloglve.test;


/**
 * Speichert die Verbindungsdaten.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public final class Konfiguration {

	/**
	 * Verbindungsdaten.
	 */
	private static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", //$NON-NLS-1$
			"-benutzer=Tester", //$NON-NLS-1$
			"-authentifizierung=c:\\passwd", //$NON-NLS-1$
			"-debugLevelStdErrText=ERROR", //$NON-NLS-1$
			"-debugLevelFileText=OFF" }; //$NON-NLS-1$

//	/**
//	 * Verbindungsdaten.
//	 */
//	private static final String[] CON_DATA = new String[] {
//			"-datenverteiler=localhost:8083", //$NON-NLS-1$
//			"-benutzer=Tester", //$NON-NLS-1$
//			"-authentifizierung=passwd", //$NON-NLS-1$
//			"-debugLevelStdErrText=ERROR", //$NON-NLS-1$
//			"-debugLevelFileText=OFF" }; //$NON-NLS-1$

	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden.
	 */
//	public static final String TEST_DATEN_VERZ = "../testDaten/V_2.9.2(20.05.08)/"; //$NON-NLS-1$
	public static final String TEST_DATEN_VERZ = ".\\extra\\testDaten\\V_2.9.2(20.05.08)\\"; //$NON-NLS-1$

	/**
	 * Die CSV-Datei mit Paramterdaten.
	 */
	public static final String DATENCSV_PARAMETER = "Parameter";
	
	
	/**
	 * Die CSV-Datei mit Daten des FS1.
	 */
	public static final String DATENCSV_FS1 = "Fahrstreifen1";
	
	/**
	 * Die CSV-Datei mit Daten des FS2.
	 */
	public static final String DATENCSV_FS2 = "Fahrstreifen2";
	
	/**
	 * Die CSV-Datei mit Daten des FS3.
	 */
	public static final String DATENCSV_FS3 = "Fahrstreifen3";
	
	
	/**
	 * Die CSV-Datei mit Daten der Standardprüfung KZD TLS.
	 */
	public static final String DATENCSV_LVE_TLS = "PL-Pruef_LVE_TLS";
	
	/**
	 * Die CSV-Datei mit Daten der Standardprüfung KZD Grenzwerte.
	 */
	public static final String DATENCSV_LVE_GRENZ = "PL-Pruef_LVE_Grenz";
	
	/**
	 * Die CSV-Datei mit Daten der Standardprüfung LZD Grenzwerte.
	 */
	public static final String DATENCSV_LZD = "PL-Pruefung_LZD";
	
	/**
	 * Die CSV-Datei mit Daten der Differenzialkontrolle.
	 */
	public static final String DATEN_CSV_DIFF = "Fahrstreifen_Diff";
	
	/**
	 * Die CSV-Datei mit Fehlerdaten eines FS.
	 */
	public static final String DATENCSV_FS_FEHLER = "Fahrstreifen_Fehler";
	
	/**
	 * Die CSV-Datei mit OK-Daten eines FS.
	 */
	public static final String DATENCSV_FS_OK = "Fahrstreifen_OK";
	
	
	/**
	 * Die PID des ersten KZD Testfahrstreifens.
	 */
	public static final String PID_TESTFS1_KZD = "AAA.Test.fs.kzd.1";
	
	/**
	 * Die PID des zweiten KZD Testfahrstreifens.
	 */
	public static final String PID_TESTFS2_KZD = "AAA.Test.fs.kzd.2";
	
	/**
	 * Die PID des dritten KZD Testfahrstreifens.
	 */
	public static final String PID_TESTFS3_KZD = "AAA.Test.fs.kzd.3";
	
	
	/**
	 * Die PID des ersten LZD Testfahrstreifens.
	 */
	public static final String PID_TESTFS1_LZD = "AAA.Test.fs.lzd.1";
	
	/**
	 * Die PID des zweiten LZD Testfahrstreifens.
	 */
	public static final String PID_TESTFS2_LZD = "AAA.Test.fs.lzd.2";
	
	/**
	 * Die PID des dritten LZD Testfahrstreifens.
	 */
	public static final String PID_TESTFS3_LZD = "AAA.Test.fs.lzd.3";
	
	
	/**
	 * Dummy-Konstruktor.
	 */
	private Konfiguration() {
		
	}
	
	/**
	 * Erfragt eine Kopie der Verbindungsdaten.
	 * 
	 * @return eine Kopie der Verbindungsdaten
	 */
	protected static String[] getConData() {
		String[] conDataKopie = new String[CON_DATA.length];

		for (int i = 0; i < conDataKopie.length; i++) {
			conDataKopie[i] = CON_DATA[i];
		}

		return conDataKopie;
	}

}
