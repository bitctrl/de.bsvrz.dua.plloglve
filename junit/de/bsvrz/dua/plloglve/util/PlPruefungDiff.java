package de.bsvrz.dua.plloglve.util;

import junit.framework.Assert;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.util.pruef.FilterMeldung;
import de.bsvrz.dua.plloglve.util.pruef.PruefeMarkierung;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

public class PlPruefungDiff
implements ClientSenderInterface, PlPruefungInterface {
	
	/**
	 * Assert-Statements benutzen?
	 */
	private boolean useAssert = true; 
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
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
	 * Abweichung zur erwarteten Anzahl von Meldungen
	 */
	private int meldungHyst = 0;
	
	
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
		Debug.init("PlPruefungDifferenzialkontrolle", alLogger); //$NON-NLS-1$
		
		/*
		 * Melde Sender für FS an
		 */
		FS = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		
		DD_KZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				  	  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				  	  (short)0);

		try{
			kzdImport = new ParaKZDLogImport(dav, FS, TEST_DATEN_VERZ + "Parameter"); //$NON-NLS-1$
			kzdImport.importParaDiff();
		}catch(Exception e) {
			LOGGER.error("Kann Test nicht konfigurieren: "+e); //$NON-NLS-1$
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
		fsImpFSDiff = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "Fahrstreifen_Diff"); //$NON-NLS-1$
		
		Data zeileFSDiff;
		
		Long aktZeit = System.currentTimeMillis();
		
		/*
		 * Initialisiert Meldungsfilter
		 */
		FilterMeldung meldFilter = new FilterMeldung(this, dav, "konstant", 63, meldungHyst); //$NON-NLS-1$
		LOGGER.info("Meldungsfilter initialisiert: Erwarte 63 Meldungen mit \"konstant\""); //$NON-NLS-1$
		
		/*
		 * Testerobjekt
		 */
		PruefeMarkierung markPruefer = new PruefeMarkierung(this, dav, FS);
		markPruefer.benutzeAssert(false);//useAssert);
		
		/*
		 * Sende Differentialkontrolldaten (3x)
		 */
		int dsCount = 0;
		int dsKumm;
		for(int i=0;i<3;i++) {
			while((zeileFSDiff = fsImpFSDiff.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				dsCount++;
				dsKumm = dsCount-(480 * i);
				LOGGER.info("Durchlauf:"+(i+1)+" - CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte alle Attribute als fehlerfrei"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				
				/*
				 * Setzt Konfiguration des Markierungs-Prüfers
				 */
				markPruefer.listenOK(aktZeit);
				
				if(dsKumm == 4 || dsKumm == 13) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte qKfz als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("qKfz", aktZeit); //$NON-NLS-1$
				}
				
				if((dsKumm == 6) || (dsKumm >= 13 && dsKumm <= 17)) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte qLkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("qLkw", aktZeit); //$NON-NLS-1$
				}
				
				if(dsKumm >= 9 && dsKumm <= 13) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte qPkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("qPkw", aktZeit); //$NON-NLS-1$
				}
				
				if(dsKumm >= 30 && dsKumm <= 31) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte vKfz als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("vKfz", aktZeit); //$NON-NLS-1$
				}
				
				if(dsKumm >= 36 && dsKumm <= 38) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte vLkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("vLkw", aktZeit); //$NON-NLS-1$
				}
				
				if(dsKumm == 32) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte vPkw als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("vPkw", aktZeit); //$NON-NLS-1$
				}
				
				if(dsKumm == 31 || dsKumm == 35) {
					LOGGER.info("CSV-Zeile:"+(dsKumm+1)+" - Zeit:"+aktZeit+" -> Konfiguriere Prüfer: Erwarte b als fehlerhaft und implausibel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					markPruefer.listenFehlImpl("b", aktZeit); //$NON-NLS-1$
				}
				
				ResultData resultat1 = new ResultData(FS, DD_KZD_SEND, aktZeit, zeileFSDiff);
				this.dav.sendData(resultat1);	

				doWait(75);
				
				aktZeit = aktZeit + Constants.MILLIS_PER_MINUTE;
			}
			fsImpFSDiff.reset();
			fsImpFSDiff.getNaechsteZeile();
		}
		
		LOGGER.info("Warte 30 Sekunden auf Meldungsfilter"); //$NON-NLS-1$
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
		
		LOGGER.info("Prüfung erfolgreich abgeschlossen"); //$NON-NLS-1$
		
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
