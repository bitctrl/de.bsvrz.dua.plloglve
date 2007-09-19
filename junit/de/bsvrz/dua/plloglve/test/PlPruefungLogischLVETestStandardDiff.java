package de.bsvrz.dua.plloglve.test;

import org.junit.Before;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dua.plloglve.util.PlPruefungDiff;
import de.bsvrz.dua.plloglve.util.PlPruefungLogisch;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Automatisierter Test nach Pr�fspezifikation f�r SWE Pl-Pr�fung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETestStandardDiff {
	
	/**
	 * Verbindungsdaten
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", //$NON-NLS-1$ 
			"-benutzer=Tester", //$NON-NLS-1$
			"-authentifizierung=c:\\passwd1" }; //$NON-NLS-1$

//	/**
//	 * Verbindungsdaten
//	 */
//	public static final String[] CON_DATA = new String[] {
//			"-datenverteiler=localhost:8083", //$NON-NLS-1$ 
//			"-benutzer=Tester", //$NON-NLS-1$
//			"-authentifizierung=c:\\passwd" }; //$NON-NLS-1$
	
	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden
	 */
	protected static final String TEST_DATEN_VERZ = ".\\testDaten\\"; //$NON-NLS-1$

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
		this.dav = DAVTest.getDav(CON_DATA);	
	}

	/**
	 * KZD TLS Test nach Pr�fspezifikation
	 */
	@Test
	public void testKZDTLS()throws Exception{
		PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,TEST_DATEN_VERZ, alLogger);
		pruefLogisch.pruefeKZDTLS();
	}
	
	/**
	 * KZD Grenz Test nach Pr�fspezifikation
	 */
	@Test
	public void testKZDGrenz()throws Exception{
//		PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,TEST_DATEN_VERZ);
//		pruefLogisch.pruefeKZDGrenz();
	}
	
	/**
	 * LZD Grenz Test nach Pr�fspezifikation
	 */
	@Test
	public void testLZDTLS()throws Exception{
//		PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,TEST_DATEN_VERZ);
//		pruefLogisch.pruefeLZDGrenz();
	}
	
	/**
	 * Differentialkontrolle nach Pr�fspezifikation
	 */
	@Test
	public void testDiff()throws Exception{
		PlPruefungDiff plPruefDiff = new PlPruefungDiff(dav,TEST_DATEN_VERZ, alLogger); 
		plPruefDiff.pruefe();
	}
}
