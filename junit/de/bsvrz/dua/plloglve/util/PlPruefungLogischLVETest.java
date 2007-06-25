package de.bsvrz.dua.plloglve.util;


import org.junit.Before;
import org.junit.Test;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Automatisierter Test nach Prüfspezifikation für SWE Pl-Prüfung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETest
implements ClientSenderInterface{

	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden
	 */
	protected static final String TEST_DATEN_VERZ =
		"C:\\Dokumente und Einstellungen\\Thierfelder\\Eigene Dateien\\workspace\\de.bsvrz.dua.plloglve\\testDaten\\"; //$NON-NLS-1$
	
	/**
	 * TestFS1
	 */
	public static SystemObject FS1 = null;
	
	/**
	 * TestFS2
	 */
	public static SystemObject FS2 = null;
	
	/**
	 * TestFS3
	 */
	public static SystemObject FS3 = null;
	
	/**
	 * Sende-Datenbeschreibung für LZD
	 */
	public static DataDescription DD_LZD_SEND = null;
	
	/**
	 * Sende-Datenbeschreibung für KZD
	 */
	public static DataDescription DD_KZD_SEND = null;
	
	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav();	
		
		FS1 = this.dav.getDataModel().getObject("fs.mq.a10.0000.1üfs"); //$NON-NLS-1$
		FS2 = this.dav.getDataModel().getObject("fs.mq.a10.0000.2üfs"); //$NON-NLS-1$
		FS3 = this.dav.getDataModel().getObject("fs.mq.a10.0000.hfs"); //$NON-NLS-1$
		
		DD_KZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
										  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
										  (short)0);
		
		DD_LZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
				  						  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				  						  (short)0);	

		
		this.dav.subscribeSender(this, new SystemObject[]{FS1, FS2, FS3}, 
				DD_KZD_SEND, SenderRole.source());
		
		this.dav.subscribeSender(this, new SystemObject[]{FS1, FS2, FS3}, 
				DD_LZD_SEND, SenderRole.source());
	}
	
	/**
	 * Gesamter Test nach Prüfspezifikation
	 */
	@Test
	public void testAlles()throws Exception{
		TestFahrstreifenImporter paraImp = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen2"); //$NON-NLS-1$
		
		Data zeile = null;
		while( (zeile = paraImp.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null ){
			ResultData resultat = new ResultData(FS1, DD_KZD_SEND, System.currentTimeMillis(), zeile);
			this.dav.sendData(resultat);
		}		
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		// TODO Automatisch erstellter Methoden-Stub		
	}

	
	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return false;
	}
	
}
