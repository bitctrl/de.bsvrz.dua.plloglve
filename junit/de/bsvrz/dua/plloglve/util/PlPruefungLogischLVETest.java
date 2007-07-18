package de.bsvrz.dua.plloglve.util;


import java.io.*;

import org.junit.Before;
import org.junit.Test;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.SystemObject;
//import de.bsvrz.dua.plloglve.plloglve.standard.KzdPLFahrStreifen;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.pruef.PruefKZDLog;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Automatisierter Test nach Pr¸fspezifikation f¸r SWE Pl-Pr¸fung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETest
implements ClientSenderInterface {

	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden
	 */
	protected static final String TEST_DATEN_VERZ =
		"D:\\goerlitz\\eclipse32\\workspace\\de.bsvrz.dua.plloglve\\testDaten\\"; //$NON-NLS-1$
	
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
	 * Sende-Datenbeschreibung f¸r LZD
	 */
	public static DataDescription DD_LZD_SEND = null;
	
	/**
	 * Sende-Datenbeschreibung f¸r KZD
	 */
	public static DataDescription DD_KZD_SEND = null;

	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	/**
	 * Fahrstriefen Prameter Importer
	 */
	private TestFahrstreifenImporter paraImpFS1 = null;
	private TestFahrstreifenImporter paraImpFS2 = null;
	private TestFahrstreifenImporter paraImpFS3 = null;
	
	/**
	 * Gibt an, ob CSV Daten vorhanden sind
	 */
	private boolean csvDatenVorhanden = true;

	/**
	 * TODO: Log Writer
	 */
	public BufferedWriter br;
	
	/**
	 * {@inheritDoc}
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav();	

		FS1 = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		FS2 = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.2"); //$NON-NLS-1$
		FS3 = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.3"); //$NON-NLS-1$
		
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
		
		//TODO: Initialisiere Log Writer
		this.br = new BufferedWriter(new FileWriter("C:\\pr.log"));
	}

	/**
	 * Gesamter Test nach Pr¸fspezifikation
	 */
	@Test
	public void testAlles()throws Exception{
		
		ParaKZDLogImport kzdImport1 = new ParaKZDLogImport(dav, FS1, TEST_DATEN_VERZ + "parameter"); //$NON-NLS-1$
		ParaKZDLogImport kzdImport2 = new ParaKZDLogImport(dav, FS2, TEST_DATEN_VERZ + "parameter"); //$NON-NLS-1$
		ParaKZDLogImport kzdImport3 = new ParaKZDLogImport(dav, FS3, TEST_DATEN_VERZ + "parameter"); //$NON-NLS-1$

		kzdImport1.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
		kzdImport1.importiereParameter(1);
		
		kzdImport1.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
		kzdImport1.importiereParameter(1);
		
		kzdImport1.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
		kzdImport1.importiereParameter(1);
		
		kzdImport1.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
		kzdImport1.importiereParameter(1);
		
		kzdImport1.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX);
		kzdImport1.importiereParameter(1);
		
		kzdImport2.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
		kzdImport2.importiereParameter(2);
		
		kzdImport2.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
		kzdImport2.importiereParameter(2);
		
		kzdImport2.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
		kzdImport2.importiereParameter(2);
		
		kzdImport2.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
		kzdImport2.importiereParameter(2);
		
		kzdImport2.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX);
		kzdImport2.importiereParameter(2);
		
		kzdImport3.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
		kzdImport3.importiereParameter(3);
		
		kzdImport3.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
		kzdImport3.importiereParameter(3);
		
		kzdImport3.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
		kzdImport3.importiereParameter(3);
		
		kzdImport3.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
		kzdImport3.importiereParameter(3);
		
		kzdImport3.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX);
		kzdImport3.importiereParameter(3);
		
		//Initialisiere Parameter Importer
		paraImpFS1 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1"); //$NON-NLS-1$
		paraImpFS2 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen2kon"); //$NON-NLS-1$
		paraImpFS3 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen3"); //$NON-NLS-1$
		
		//TODO: Log
		System.out.println("MT:Init Pruefer");
		
		//Initialisiere Daten-Listener (ClientReceiver)
		PruefKZDLog fsPruefer = new PruefKZDLog(this, new SystemObject[]{FS1, FS2, FS3}, TEST_DATEN_VERZ + "PL-Pruef_LVE_TLS");	
		//PruefKZDLog fsPruefer = new PruefKZDLog(this, new SystemObject[]{FS1, FS2, FS3}, TEST_DATEN_VERZ + "Messwerters.LVE");
		
		//Aktueller Index (Zeile) in CSV Datei
		int csvIndex = 0;
		
		//Ergibnisdaten der jeweiligen Fahrstreifen
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;
		
		Data zeileFS1LZ;
		Data zeileFS2LZ;
		Data zeileFS3LZ;
		
		//Gibt an, ob fuer den entsprechenden Fahrstriefen CSV Daten vorhanden sind
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;
		
		//Aktueller Zeitstempel
		long aktZeit;
		
		//zeileFS2LZ = paraImpFS2.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup());
		//System.out.println(zeileFS2LZ.toString());
		//ResultData resultat2LZ = new ResultData(FS2, DD_LZD_SEND, System.currentTimeMillis(), zeileFS2LZ);
		//this.dav.sendData(resultat2LZ);
		//paraImpFS2.reset();

		paraImpFS2.setT(1L);
		paraImpFS3.setT(1L);
		
		while(csvDatenVorhanden) {
			aktZeit = System.currentTimeMillis();  //setze aktuelle Zeit
			//TODO: Log
			System.out.println("MT:Setze Offset und Zeitstempel fuer Pruefer");
			
			//uebergebe CSV Index und Zeitstempel an Listener
			fsPruefer.listen(csvIndex,aktZeit);
			
			//Lese CSV Parameter und Ergebnisdatensatz erstellen + senden
			//Es werden die Daten der Fahrstreifen FS1-FS3 gesendet bis keine
			//CSV Daten mehr zur Verfuegung stehen 
			/*
			if((zeileFS1 = paraImpFS1.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				//TODO: Log
				System.out.println("MT:Sende Daten fuer FS1 -> "+aktZeit);		
				ResultData resultat1 = new ResultData(FS1, DD_KZD_SEND, aktZeit, zeileFS1);
				this.dav.sendData(resultat1);
			} else datenFS1Vorhanden = false;
			*/
			
			if((zeileFS2 = paraImpFS2.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				//TODO: Log
				System.out.println("MT:Sende Daten fuer FS2 -> "+aktZeit);
				ResultData resultat2 = new ResultData(FS2, DD_KZD_SEND, aktZeit, zeileFS2);
				this.dav.sendData(resultat2);
			} else datenFS2Vorhanden = false;
	
			if((zeileFS3 = paraImpFS3.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				//TODO: Log
				System.out.println("MT:Sende Daten fuer FS3 -> "+aktZeit);
				ResultData resultat3 = new ResultData(FS3, DD_KZD_SEND, aktZeit, zeileFS3);
				this.dav.sendData(resultat3);
			} else datenFS3Vorhanden = false;

			//Erhoehe CSV Index
			csvIndex++;
			
			//Wenn keine CSV Daten mehr
			//if(!datenFS1Vorhanden && !datenFS2Vorhanden && !datenFS3Vorhanden)
			if(!datenFS2Vorhanden && !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				System.out.println("MT:Keine Daten");  //Breche Datensendung ab
			} else {
				System.out.println("MT:Warte");
				doWait();  //Warte auf Ueberpruefung der FS1-FS3
			}
		}
		
		//TODO: Schlieﬂe Log Writer
		this.br.write("\n\r");
		this.br.flush();
		this.br.close();
	}

	/*
	 * Weckt diesen Thread
	 */
	public void doNotify() {
		synchronized(this) {
			this.notify();
		}
	}
	
	/*
	 * Laessten diesen Thread warten
	 */
	private void doWait() {
		synchronized(this) {
			try {
				this.wait();
			}catch(Exception e){};
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
	
	public ClientDavInterface uebergebeDAV() {
		return this.dav;
	}

}
