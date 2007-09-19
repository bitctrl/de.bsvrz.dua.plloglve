package de.bsvrz.dua.plloglve.test;

import org.junit.Before;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dua.plloglve.util.PlPruefungVertrauensbereich;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Automatisierter Test nach Prüfspezifikation für SWE Pl-Prüfung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETestVertrauensbereich {
	
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
	 * Loggerargument
	 * 
	 * Pfadangabe mit Argument: -debugFilePath=[Pfad]
	 */
	private String[] argumente = new String[] {"-debugLevelFileText=ALL"};
	private ArgumentList alLogger = new ArgumentList(argumente);
	
	/**
	 * Vorbereitungen (DAV-Anmeldung)
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav(CON_DATA);	
	}

	/**
	 * Vertrauensbereichstest nach Prüfspezifikation
	 */
	@Test
	public void testVB()throws Exception{
		PlPruefungVertrauensbereich pruefVertrB = new PlPruefungVertrauensbereich(dav,TEST_DATEN_VERZ, alLogger);
		pruefVertrB.pruefe();
	}
}
