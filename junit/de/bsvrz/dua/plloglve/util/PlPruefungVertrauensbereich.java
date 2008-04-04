package de.bsvrz.dua.plloglve.util;

import junit.framework.Assert;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.util.pruef.FilterMeldung;
import de.bsvrz.dua.plloglve.util.pruef.PruefeMarkierung;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

public class PlPruefungVertrauensbereich
implements ClientSenderInterface, PlPruefungInterface {

	/**
	 * Assert-Statements benutzen?
	 */
	private boolean useAssert = true; 
	
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
	 * Intervalllänge in Millisekunden
	 */
	//static long INTERVALL = Konstante.MINUTE_IN_MS;
	static long INTERVALL = 100L;
	
	/**
	 * Fehlerdatensätze
	 */
	private Data zFSFehlQKfzQPkwQLkwVPkw1;
	private Data zFSFehlQKfzQPkwQLkwVPkw2;
	private Data zFSFehlB1;
	private Data zFSFehlB2;
	
	/**
	 * Gibt an, welcher fehlerhafte DS jeweils gesendet werden soll
	 */
	private boolean sendeFehler1_2 = false;
	private boolean sendeFehler2_2 = false;
	
	/**
	 * Abweichung zur erwarteten Anzahl von Meldungen
	 */
	private int meldungHyst = 0;
	
	
	/**
	 * Sendet Testdaten und prüft Ausfallkontrolle
	 * @param dav Datenverteilerverbindung
	 * @param TEST_DATEN_VERZ Testdatenverzeichnis
	 */
	public PlPruefungVertrauensbereich(ClientDavInterface dav, String TEST_DATEN_VERZ, ArgumentList alLogger) {
		this.dav = dav;
		this.TEST_DATEN_VERZ = TEST_DATEN_VERZ;

		/*
		 * Initialisiere Logger
		 */
		Debug.init("PlPruefungVertrauensbereich", alLogger); //$NON-NLS-1$
		LOGGER = Debug.getLogger();
		
		/*
		 * Melde Sender für FS an
		 */
		FS = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		
		DD_KZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				  	  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				  	  (short)0);

		try{
			kzdImport = new ParaKZDLogImport(dav, FS, TEST_DATEN_VERZ + "Parameter");
			kzdImport.setOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
			kzdImport.importiereParameter(1);
			kzdImport.importParaVertrauensbereich();
		}catch(Exception e) {
			LOGGER.error("Kann Test nicht konfigurieren: "+e);
		}
		
		try {
			TestFahrstreifenImporter paraImpFSFehler = null;
			paraImpFSFehler = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen_Fehler"); //$NON-NLS-1$
			paraImpFSFehler.setT(100L);
			zFSFehlQKfzQPkwQLkwVPkw1 = paraImpFSFehler.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
			zFSFehlQKfzQPkwQLkwVPkw2 = paraImpFSFehler.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
			zFSFehlB1 = paraImpFSFehler.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
			zFSFehlB2 = paraImpFSFehler.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
		}catch(Exception e) {
			LOGGER.error("Kann Fehlerdatensätze nicht importieren: "+e);
		}

	}
	
	/**
	 * Prüfung der Ausfallkontrolle
	 * @throws Exception
	 */
	public void pruefe() throws Exception {
		/*
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, FS, DD_KZD_SEND, SenderRole.source());
		
		/*
		 * Initialisiere Parameter Importer für fehlerfreie DS
		 */
		TestFahrstreifenImporter paraImpFSOK = null;
		paraImpFSOK = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen_OK"); //$NON-NLS-1$
		
		/*
		 * Setze Intervall auf 100MS
		 */
		paraImpFSOK.setT(100L);
		
		/*
		 * Aktueller fehlerfreie Fahrstreifen-DS
		 */
		Data zeileFSOK;

		Long pruefZeit = System.currentTimeMillis();
		Long aktZeit;

		/*
		 * Prüfung
		 */
		LOGGER.info("Beginne Prüfung");
		
		/*
		 * Warte auf 371 Meldungen
		 * 36 x 4 (qKfz, qPkw, qLkw, vPkw)
		 * 37 (b)
		 * 39 x 4 (qKfz, qPkw, qLkw, vPkw)
		 * 32 (b)
		 * 2 (Gutmeldung)
		 */
		FilterMeldung meldFilter = new FilterMeldung(this, dav,"Vertrauensbereichs", 371, meldungHyst);
		LOGGER.info("Meldungsfilter initialisiert: Erwarte 371 Meldungen mit \"Vertrauensbereichs\"");
		
		/*
		 * Sendet fehlerfreie DS für eine Stunde
		 */
		LOGGER.info("Sende fehlerfreie DS für 1 Stunde (60)");
		for(int i=1;i<=60;i++) {
			
			if((zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) == null) {
				paraImpFSOK.reset();
				paraImpFSOK.getNaechsteZeile();
				zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
			}
			
			ResultData resultat1 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zeileFSOK);
			this.dav.sendData(resultat1);
			
			//Sende Datensatz entsprechend Intervall
			pruefZeit = pruefZeit + INTERVALL;
			
			//Warte bis Intervallende
			if((aktZeit = System.currentTimeMillis()) < pruefZeit) {
				Pause.warte(pruefZeit - aktZeit);
			}
		}

		/*
		 * Testerobjekt
		 */
		PruefeMarkierung markPruefer = new PruefeMarkierung(this, dav, FS);
		markPruefer.benutzeAssert(useAssert);
		
		for(int i=1;i<=600;i++) {
			
			/*
			 * Konfiguriert Testerobjekt
			 * Prüfe Markierung der DS als Implausibel
			 */
			if ((i >= 29 && i < 72) || (i >= 511 && i < 552)) {
				markPruefer.listenImpl(pruefZeit);
				LOGGER.info("Intervall "+i+": Erwarte alle Attribute als Implausiebel");
			}
			
			/*
			 * Verlassen des VB vom 29. - 71. DS und vom 511. - 551. DS
			 * Innerhalb dieser Bereiche werden alle Werte des FS als Implausibel erwartet
			 */
			//if((i >= 4 && i <= 9) || (i >= 23 && i <= 29) || (i >= 490 && i <= 494) || (i >= 501 && i <= 506) || (i >= 512 && i <= 514)) {
			if((i >= 0 && i <= 13)) {
				/*
				 * Es wird ein fehlerhafter DS (qKfz, qPkw, qLkw, vPkw) gesendet
				 * Dabei wird der VB entsprechend Afo beim 29. DS verlassen
				 * 
				 * Der prozentuale Ausfall der Attribute liegt ab dem 65. DS unter 20% wobei der VB jedoch
				 * aufgrund der anderen Fehlerdaten weiterhin verlassen bleibt
				 * 
				 * Für den zweiten Testbereich liegt der prozentuale Ausfall ab dem 513. DS über 20% wobei
				 * der VB bereits früher durch die anderen Fehlerdaten verlassen wird
				 * 
				 * Ab dem 552. DS liegt der prozentuale Ausfall entsprechend Afo wieder im VB 
				 */
				LOGGER.info("Intervall "+i+": Sende fehlerhaftes Datum (qKfz, qPkw, qLkw, vPkw)");
				sendeFehler2(pruefZeit);
//			} else if ((i >= 10 && i <= 16) || (i >= 30 && i <= 36) || (i >= 482 && i <= 489) || (i >= 507 && i <= 511)) {
//			} else if((i >= 10 && i <= 16) || (i >= 30 && i <= 36)) {
//				LOGGER.info("Intervall "+i+": Sende fehlerhaftes Datum (b)");
//				sendeFehler1(pruefZeit);
			} else {
				if((zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) == null) {
					paraImpFSOK.reset();
					paraImpFSOK.getNaechsteZeile();
					zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
				}
				ResultData resultat1 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zeileFSOK);
				LOGGER.info("Intervall "+i+": Sende fehlerfreies Datum");
				this.dav.sendData(resultat1);
			}
			
			//Warte auf Markierungsprüfung
			doWait(75);
			
			//Sende Datensatz entsprechend Intervall
			pruefZeit = pruefZeit + INTERVALL;
			
			//Warte bis Intervallende
			if((aktZeit = System.currentTimeMillis()) < pruefZeit) {
				Pause.warte(pruefZeit - aktZeit);
			}
		}

		LOGGER.info("Warte auf Meldungsfilter");
		
		//Warte 30s auf Filterung der Betriebsmeldungen
		doWait(30000);

		String warnung = meldFilter.getAnzahlErhaltenerMeldungen() + " von " + meldFilter.getErwarteteAnzahlMeldungen() + " (Hysterese:" + meldungHyst + ") Betriebsmeldungen erhalten";
		if(!meldFilter.wurdeAnzahlEingehalten()) {
			if(useAssert) {
				Assert.assertTrue(warnung, false);
			} else {
				LOGGER.warning(warnung);
			}
		} else {
			LOGGER.info(warnung);
		}
		
		LOGGER.info("Prüfung erfolgreich abgeschlossen");
		
		/*
		 * Sender abmelden
		 */
		this.dav.unsubscribeSender(this, FS, DD_KZD_SEND);
	}
	
	/**
	 * Sendet einen fehlerhaften DS
	 * Fehlerhafte Attribute: qKfz, qPkw, pLkw, vPkw
	 * @param pruefZeit Zeitstempel des DS
	 * @throws Exception
	 */
	private void sendeFehler1(long pruefZeit) throws Exception {
		ResultData resultat1;
		if(!sendeFehler1_2) {
			resultat1 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zFSFehlQKfzQPkwQLkwVPkw1);
			this.dav.sendData(resultat1);
			sendeFehler1_2 = true;
		} else {
			resultat1 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zFSFehlQKfzQPkwQLkwVPkw2);
			this.dav.sendData(resultat1);
			sendeFehler1_2 = false;
		}
	}
	
	/**
	 * Sendet einen fehlerhaften DS
	 * Fehlerhaftes Attribut: b
	 * @param pruefZeit Zeitstempel des DS
	 * @throws Exception
	 */
	private void sendeFehler2(long pruefZeit) throws Exception {
		ResultData resultat2;
		if(!sendeFehler2_2) {
			resultat2 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zFSFehlB1);
			this.dav.sendData(resultat2);
			sendeFehler2_2 = true;
		} else {
			resultat2 = new ResultData(FS, DD_KZD_SEND, pruefZeit, zFSFehlB2);
			this.dav.sendData(resultat2);
			sendeFehler2_2 = false;
		}
	}
	
	/* (Kein Javadoc)
	 * @see de.bsvrz.dua.plloglve.util.PlPruefungInterface#doNotify()
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
	
	/**
	 * Soll Assert zur Fehlermeldung genutzt werden?
	 * @param useAssert
	 */
	public void benutzeAssert(final boolean useAssert) {
		this.useAssert = useAssert;
	}
	
	/**
	 * Setzt die erlaubte Abweichung zur erwarteten Anzahl an Betriebsmeldungen
	 * @param meldungHyst
	 */
	public void setMeldungHysterese(final int meldungHyst) {
		this.meldungHyst = meldungHyst;
	}
}
