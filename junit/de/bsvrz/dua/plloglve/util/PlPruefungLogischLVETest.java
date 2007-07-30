package de.bsvrz.dua.plloglve.util;

import org.junit.Before;
import org.junit.Test;

import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;

import stauma.dav.clientside.ClientDavInterface;
import sys.funclib.ArgumentList;
import sys.funclib.debug.Debug;

/**
 * Automatisierter Test nach Prüfspezifikation für SWE Pl-Prüfung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETest {
	
//	/**
//	 * Verbindungsdaten
//	 */
//	public static final String[] CON_DATA = new String[] {
//			"-datenverteiler=192.168.1.191:8083", //$NON-NLS-1$ 
//			"-benutzer=Tester", //$NON-NLS-1$
//			"-authentifizierung=c:\\passwd1" }; //$NON-NLS-1$

	/**
	 * Verbindungsdaten
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", //$NON-NLS-1$ 
			"-benutzer=Tester", //$NON-NLS-1$
			"-authentifizierung=c:\\passwd" }; //$NON-NLS-1$
	
	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden
	 */
	protected static final String TEST_DATEN_VERZ = ".\\testDaten\\"; //$NON-NLS-1$

	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	/**
	 * Logger und Loggerattribute
	 * 
	 * Pfadangabe mit Argument: -debugFilePath=[Pfad]
	 */
	protected Debug LOGGER;
	private String[] argumente = new String[] {"-debugLevelFileText=ALL"};
	private ArgumentList alLogger = new ArgumentList(argumente);
	
	/**
	 * {@inheritDoc}
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav(CON_DATA);	
		Debug.init("PlPruefungLogisch", alLogger); //$NON-NLS-1$
		LOGGER = Debug.getLogger();
	}

	/**
	 * Gesamter Test nach Prüfspezifikation
	 * 
	 * Definition der Grenzwertparameter
	 */
	@Test
	public void testAlles()throws Exception{

		/*
		 * Prüfung: KZD TLS und KZD sowie LZD auf Grenzwertüberschreitung
		 */
//		PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,TEST_DATEN_VERZ);
//		pruefLogisch.pruefeKZDTLS();
//		pruefLogisch.pruefeKZDGrenz();
//		pruefLogisch.pruefeLZDGrenz();

		/*
		 * Differenzialprüfung: KZD
		 */
		PlPruefungDiff plPruefDiff = new PlPruefungDiff(dav,TEST_DATEN_VERZ); 
		plPruefDiff.pruefe();

		/*
		 * Prüfung: Ausfallhäufigkeit KZD
		 */
//		PlPruefungAusfall pruefAusfall = new PlPruefungAusfall(dav,TEST_DATEN_VERZ);
//		pruefAusfall.pruefe();
		
		/*
		 * Prüfung: Vertrauensbereich KZD
		 */
//		PlPruefungVertrauensbereich pruefVertrB = new PlPruefungVertrauensbereich(dav,TEST_DATEN_VERZ);
//		pruefVertrB.pruefe();
	}
}
