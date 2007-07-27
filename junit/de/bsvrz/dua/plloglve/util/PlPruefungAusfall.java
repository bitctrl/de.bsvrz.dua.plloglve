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
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.pruef.FilterMeldung;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

public class PlPruefungAusfall
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
	 * Sende-Datenbeschreibung f�r KZD
	 */
	public static DataDescription DD_KZD_SEND = null;
	
	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	/**
	 * Filter-Timout
	 */
	private boolean filterTimeout = true;
	
	/**
	 * Sendet Testdaten und pr�ft Ausfallkontrolle
	 * @param dav Datenverteilerverbindung
	 * @param TEST_DATEN_VERZ Testdatenverzeichnis
	 */
	public PlPruefungAusfall(ClientDavInterface dav, String TEST_DATEN_VERZ) {
		this.dav = dav;
		this.TEST_DATEN_VERZ = TEST_DATEN_VERZ;

		/*
		 * Melde Sender f�r FS an
		 */
		FS = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		
		DD_KZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				  	  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				  	  (short)0);

		try{
			this.dav.subscribeSender(this, FS, DD_KZD_SEND, SenderRole.source());
		}catch(Exception e) {
			LOGGER.error("Pr�fung kann nicht konfiguriert werden: "+e);
		}
		
		try{
			kzdImport = new ParaKZDLogImport(dav, FS, TEST_DATEN_VERZ + "Parameter_TLS");
			kzdImport.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
			kzdImport.importiereParameter(1);
			kzdImport.importParaAusfall();
		}catch(Exception e) {
			LOGGER.error("Kann Test nicht konfigurieren: "+e);
		}
	}
	
	/**
	 * Pr�fung der Ausfallkontrolle
	 * @throws Exception
	 */
	public void pruefe() throws Exception {
		/*
		 * Initialisiere Parameter Importer f�r fehlerfreie und
		 * fehlerhafte DS
		 */
		TestFahrstreifenImporter paraImpFSOK = null;
		TestFahrstreifenImporter paraImpFSFehler = null;
		
		paraImpFSOK = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1_OK"); //$NON-NLS-1$
		paraImpFSFehler = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1_Fehler"); //$NON-NLS-1$
		
		/*
		 * Setze Intervall auf 100MS
		 */
		paraImpFSOK.setT(100L);
		paraImpFSFehler.setT(100L);
		
		/*
		 * Aktuelle fehlerfreie und fehlerhafte Fahrstreifen-DS
		 */
		Data zeileFSOK;
		Data zeileFSFehler;
		
		Long pruefZeit = System.currentTimeMillis();
		Long aktZeit;
			
		/*
		 * Sendet fehlerfreie DS f�r einen Tag
		 */
//		LOGGER.info("Sende fehlerfreie DS f�r 1 Tag (1440");
//		for(int i=1;i<=1440;i++) {
//			
//			if((zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) == null) {
//				paraImpFSOK.reset();
//				paraImpFSOK.getNaechsteZeile();
//				zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
//			}
//			
//			ResultData resultat1 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zeileFSOK);
//			this.dav.sendData(resultat1);
//			
//			aktZeit = pruefZeit + Konstante.MINUTE_IN_MS;
//		}

		/*
		 * Pr�fung
		 */
		//FilterMeldung filterM = new FilterMeldung(this, dav,"Ausfallh�ufigkeit", 1457);
		//TODO:DEBUG
		FilterMeldung filterM = new FilterMeldung(this, dav,"Ausfallh�ufigkeit", 1600);

		//TODO:DEBUG
		//for(int i=1;i<=2429;i++) {
		for(int i=1;i<=2500;i++) {
			if((i > 928 && i <= 972) || (i > 989 && i <= 1032)) {
				//LOGGER.info("["+i+"] Sende fehlerhaftes Datum...");
				if((zeileFSFehler = paraImpFSFehler.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) == null) {
					paraImpFSFehler.reset();
					paraImpFSFehler.getNaechsteZeile();
					zeileFSFehler = paraImpFSFehler.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
				}
				ResultData resultat1 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zeileFSFehler);
				this.dav.sendData(resultat1);
				System.out.println("["+i+"]A");
			} else {
				//LOGGER.info("["+i+"] Sende fehlerfreies Datum...");
				if((zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) == null) {
					paraImpFSOK.reset();
					paraImpFSOK.getNaechsteZeile();
					zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
				}
				ResultData resultat1 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zeileFSOK);
				this.dav.sendData(resultat1);
				System.out.println("["+i+"]O");
			}
			//pruefZeit = pruefZeit + Konstante.MINUTE_IN_MS;
			pruefZeit = pruefZeit + 100L;
			if((aktZeit = System.currentTimeMillis()) < pruefZeit) {
				Pause.warte(pruefZeit - aktZeit);
			}
		}
		
		doWait();
		
		if(filterTimeout) {
			LOGGER.warning("Filter-Timeout");
		}
	}
	
	/* (Kein Javadoc)
	 * @see de.bsvrz.dua.plloglve.util.PlPruefungInterface#doNotify()
	 */
	public void doNotify() {
		filterTimeout = false;
		synchronized(this) {
			this.notify();
		}
	}
	
	/**
	 * L�ssten diesen Thread warten
	 */
	private void doWait() {
		synchronized(this) {
			try {
				this.wait(60000);
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
