package de.bsvrz.dua.plloglve.util;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.dua.plloglve.pruef.PruefeMarkierung;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

public class PlPruefungDiff
implements ClientSenderInterface, PlPruefungInterface {

	/**
	 * Logger
	 */
	protected Debug LOGGER = Debug.getLogger();
	
	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden
	 */
	private String TEST_DATEN_VERZ = null;
	
	/**
	 * Testfahrstreifen KZD
	 */
	public static SystemObject FS = null;
	
	/**
	 * KZD Importer
	 */
	private ParaKZDLogImport kzdImport;
	
	/**
	 * Sende-Datenbeschreibung für KZD
	 */
	public static DataDescription DD_KZD_SEND = null;
	
	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	/**
	 * Sendet Testdaten und prüft Differenzielkontrolle
	 * @param dav Datenteilerverbindung
	 * @param TEST_DATEN_VERZ Verzeichnis mit Testdaten
	 */
	public PlPruefungDiff(ClientDavInterface dav, String TEST_DATEN_VERZ) {
		this.dav = dav;
		this.TEST_DATEN_VERZ = TEST_DATEN_VERZ;

		/*
		 * Melde Sender für FS an
		 */
		FS = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		
		DD_KZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				  	  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				  	  (short)0);

		try{
			this.dav.subscribeSender(this, FS, DD_KZD_SEND, SenderRole.source());
		}catch(Exception e) {
			LOGGER.error("Kann Sender nicht anmelden: "+e);
		}
		
		try{
			kzdImport = new ParaKZDLogImport(dav, FS, TEST_DATEN_VERZ + "Parameter_TLS");
			kzdImport.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
			kzdImport.importiereParameter(1);
			kzdImport.importParaDiff();
		}catch(Exception e) {
			LOGGER.error("Kann Test nicht konfigurieren: "+e);
		}
	}
	
	/**
	 * Prüfung der Differentialkontrolle
	 * @throws Exception
	 */
	public void pruefe() throws Exception {
		/*
		 * Initialisiere FS-Daten-Importer 
		 */
		TestFahrstreifenImporter fsImpFSDiff = null;
		fsImpFSDiff = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen_Diff"); //$NON-NLS-1$
		
		Data zeileFSDiff;
		
		Long aktZeit = System.currentTimeMillis();
		
		/*
		 * Testerobjekt
		 */
		PruefeMarkierung markPruefer = new PruefeMarkierung(this, dav, FS);
		
		/*
		 * Sende Differentialkontrolldaten (3x)
		 */
		int dsCount = 0;
		int dsKumm;
		for(int i=0;i<3;i++) {
			while((zeileFSDiff = fsImpFSDiff.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				dsCount++;
				dsKumm = dsCount-(480 * i);
				LOGGER.info("Durchlauf:"+i+" - Datensatz:"+dsCount+" - Zeit:"+aktZeit);
				
				/*
				 * Setzt Konfiguration des Markierungs-Prüfers
				 */
				markPruefer.listenOK(aktZeit);
				
				if(dsKumm == 4 || dsKumm == 13) {
					markPruefer.listenFehlerhaft(aktZeit);
				}
				
				if((dsKumm == 6) || (dsKumm >= 13 && dsKumm <= 17)) {
					markPruefer.listenFehlerhaft(aktZeit);
				}
				
				if(dsKumm >= 9 && dsKumm <= 13) {
					markPruefer.listenFehlerhaft(aktZeit);
				}
				
				if(dsKumm >= 30 && dsKumm <= 31) {
					markPruefer.listenFehlerhaft(aktZeit);
				}
				
				if(dsKumm >= 36 && dsKumm <= 38) {
					markPruefer.listenFehlerhaft(aktZeit);
				}
				
				if(dsKumm == 32) {
					markPruefer.listenFehlerhaft(aktZeit);
				}
				
				if(dsKumm == 31 || dsKumm == 35) {
					markPruefer.listenFehlerhaft(aktZeit);
				}
				
				ResultData resultat1 = new ResultData(FS, DD_KZD_SEND, aktZeit, zeileFSDiff);
				this.dav.sendData(resultat1);	

				doWait();
				
				aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
			}
			fsImpFSDiff.reset();
			fsImpFSDiff.getNaechsteZeile();
		}
	}
	
	/**
	 * Weckt diesen Thread
	 */
	public void doNotify() {
		synchronized(this) {
			this.notify();
		}
	}
	
	/**
	 * Lässten diesen Thread warten
	 */
	private void doWait() {
		synchronized(this) {
			try {
				this.wait(250);
			}catch(Exception e){};
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		//VOID
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return false;
	}
}
