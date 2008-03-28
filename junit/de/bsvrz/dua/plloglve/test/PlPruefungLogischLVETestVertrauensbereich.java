package de.bsvrz.dua.plloglve.test;

import org.junit.Before;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dua.plloglve.util.PlPruefungVertrauensbereich;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Automatisierter Test nach Pr�fspezifikation f�r SWE Pl-Pr�fung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETestVertrauensbereich {

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
		this.dav = DAVTest.getDav(Verbindung.getConData());	
	}

	/**
	 * Vertrauensbereichstest nach Pr�fspezifikation
	 */
	@Test
	public void testVB()throws Exception{
		PlPruefungVertrauensbereich pruefVertrB = new PlPruefungVertrauensbereich(dav,Verbindung.TEST_DATEN_VERZ, alLogger);
		pruefVertrB.benutzeAssert(true);
		pruefVertrB.setMeldungHysterese(0);
		pruefVertrB.pruefe();
	}
}
