package de.bsvrz.dua.plloglve.test;

import org.junit.Before;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dua.plloglve.util.PlPruefungDiff;
import de.bsvrz.dua.plloglve.util.PlPruefungLogisch;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Automatisierter Test nach Prüfspezifikation für SWE Pl-Prüfung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETestStandardDiff {

	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	/**
	 * Loggerargument
	 * 
	 * Pfadangabe mit Argument: -debugFilePath=[Pfad]
	 */
	private String[] argumente = new String[] {"-debugLevelFileText=ALL"};
	private ArgumentList alLogger = new ArgumentList(argumente);
	
	/**
	 * Vorbereitungen (DAV-Anmeldung)
	 **/
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav(Verbindung.getConData());	
	}

	/**
	 * KZD TLS Test nach Prüfspezifikation
	 */
	@Test
	public void testKZDTLS()throws Exception{
		PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,Verbindung.TEST_DATEN_VERZ, alLogger);
		pruefLogisch.benutzeAssert(true);
		int[][] bereiche = {{2, 101}};
		pruefLogisch.pruefeKZDTLS(bereiche);
	}
	
	/**
	 * KZD Grenz Test nach Prüfspezifikation
	 */
	@Test
	public void testKZDGrenz()throws Exception{
//		PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,Verbindung.TEST_DATEN_VERZ, alLogger);
//		pruefLogisch.benutzeAssert(true);
//		int[][] bereiche = {{2, 21}};
//		pruefLogisch.pruefeKZDGrenz(bereiche);
	}
	
	/**
	 * LZD Grenz Test nach Prüfspezifikation
	 */
	@Test
	public void testLZDTLS()throws Exception{
//		PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,Verbindung.TEST_DATEN_VERZ, alLogger);
//		pruefLogisch.benutzeAssert(true);
//		int[][] bereiche = {{2, 21}};
//		pruefLogisch.pruefeLZDGrenz(bereiche);
	}
	
	/**
	 * Differentialkontrolle nach Prüfspezifikation
	 */
	@Test
	public void testDiff()throws Exception{
//		PlPruefungDiff plPruefDiff = new PlPruefungDiff(dav,Verbindung.TEST_DATEN_VERZ, alLogger);
//		plPruefDiff.benutzeAssert(true);
//		plPruefDiff.setMeldungHysterese(0);
//		plPruefDiff.pruefe();
	}
}
