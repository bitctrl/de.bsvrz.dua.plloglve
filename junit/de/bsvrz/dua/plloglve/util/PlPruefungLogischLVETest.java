package de.bsvrz.dua.plloglve.util;

import org.junit.Before;
import org.junit.Test;

import stauma.dav.clientside.ClientDavInterface;
import sys.funclib.ArgumentList;
import sys.funclib.debug.Debug;

/**
 * Automatisierter Test nach Pr�fspezifikation f�r SWE Pl-Pr�fung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETest {

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
		this.dav = DAVTest.getDav();	
		Debug.init("PlPruefungLogisch", alLogger); //$NON-NLS-1$
		LOGGER = Debug.getLogger();
	}

	/**
	 * Gesamter Test nach Pr�fspezifikation
	 * 
	 * Definition der Grenzwertparameter
	 */
	@Test
	public void testAlles()throws Exception{

		/*
		 * Pr�fung: KZD TLS und KZD sowie LZD auf Grenzwert�berschreitung
		 */
//		PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,TEST_DATEN_VERZ);
//		pruefLogisch.pruefeKZDTLS();
//		pruefLogisch.pruefeKZDGrenz();
//		pruefLogisch.pruefeLZDGrenz();

		/*
		 * Differenzialpr�fung: KZD
		 */
//		PlPruefungDiff plPruefDiff = new PlPruefungDiff(dav,TEST_DATEN_VERZ); 
//		plPruefDiff.pruefe();

		/*
		 * Pr�fung: Ausfallh�ufigkeit KZD
		 */
		PlPruefungAusfall pruefAusfall = new PlPruefungAusfall(dav,TEST_DATEN_VERZ);
		pruefAusfall.pruefe();
	}
}
