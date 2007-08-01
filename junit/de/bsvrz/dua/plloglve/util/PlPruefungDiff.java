package de.bsvrz.dua.plloglve.util;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.ArgumentList;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.dua.plloglve.pruef.FilterMeldung;
import de.bsvrz.dua.plloglve.pruef.PruefeMarkierung;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

public class PlPruefungDiff
implements ClientSenderInterface, PlPruefungInterface {

	/**
	 * Logger
	 */
	protected Debug LOGGER;
	
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
	public PlPruefungDiff(ClientDavInterface dav, String TEST_DATEN_VERZ, ArgumentList alLogger) {
		this.dav = dav;
		this.TEST_DATEN_VERZ = TEST_DATEN_VERZ;

		/*
		 * Initialisiere Logger
		 */
		Debug.init("PlPruefungDiff", alLogger); //$NON-NLS-1$
		LOGGER = Debug.getLogger();
		
		/*
		 * Melde Sender für FS an
		 */
		FS = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		
		DD_KZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				  	  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				  	  (short)0);

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
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, FS, DD_KZD_SEND, SenderRole.source());
		
		/*
		 * Initialisiere FS-Daten-Importer 
		 */
		TestFahrstreifenImporter fsImpFSDiff = null;
		fsImpFSDiff = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen_Diff"); //$NON-NLS-1$
		
		Data zeileFSDiff;
		
		Long aktZeit = System.currentTimeMillis();
		
		/*
		 * Initialisiert Meldungsfilter
		 */
		new FilterMeldung(this, dav, "konstant", 63);
		LOGGER.info("Meldungsfilter initialisiert: Erwarte 63 Meldungen mit \"konstant\"");
		
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
				LOGGER.info("Durchlauf:"+(i+1)+" - CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit);
				
				/*
				 * Setzt Konfiguration des Markierungs-Prüfers
				 */
				markPruefer.listenOK(aktZeit);
				
				if(dsKumm == 4 || dsKumm == 13) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte qKfz als fehlerhaft und implausibel");
					markPruefer.listenFehlImpl("qKfz", aktZeit);
				}
				
				if((dsKumm == 6) || (dsKumm >= 13 && dsKumm <= 17)) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte qLkw als fehlerhaft und implausibel");
					markPruefer.listenFehlImpl("qLkw", aktZeit);
				}
				
				if(dsKumm >= 9 && dsKumm <= 13) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte qPkw als fehlerhaft und implausibel");
					markPruefer.listenFehlImpl("qPkw", aktZeit);
				}
				
				if(dsKumm >= 30 && dsKumm <= 31) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte vKfz als fehlerhaft und implausibel");
					markPruefer.listenFehlImpl("vKfz", aktZeit);
				}
				
				if(dsKumm >= 36 && dsKumm <= 38) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte vLkw als fehlerhaft und implausibel");
					markPruefer.listenFehlImpl("vLkw", aktZeit);
				}
				
				if(dsKumm == 32) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte vPkw als fehlerhaft und implausibel");
					markPruefer.listenFehlImpl("vPkw", aktZeit);
				}
				
				if(dsKumm == 31 || dsKumm == 35) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte b als fehlerhaft und implausibel");
					markPruefer.listenFehlImpl("b", aktZeit);
				}
				
				ResultData resultat1 = new ResultData(FS, DD_KZD_SEND, aktZeit, zeileFSDiff);
				this.dav.sendData(resultat1);	

				doWait(75);
				
				aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
			}
			fsImpFSDiff.reset();
			fsImpFSDiff.getNaechsteZeile();
		}
		
		LOGGER.info("Warte 30 Sekunden auf Meldungsfilter");
		doWait(30000);
		
		LOGGER.info("Prüfung erfolgreich abgeschlossen");
		
		/*
		 * Sender abmelden
		 */
		this.dav.unsubscribeSender(this, FS, DD_KZD_SEND);
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
	private void doWait(int zeit) {
		synchronized(this) {
			try {
				this.wait(zeit);
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
