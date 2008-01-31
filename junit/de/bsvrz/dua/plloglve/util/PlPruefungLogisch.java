package de.bsvrz.dua.plloglve.util;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.util.para.ParaLZDLogImport;
import de.bsvrz.dua.plloglve.util.pruef.PruefeDatenLogisch;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

public class PlPruefungLogisch
implements ClientSenderInterface {
	
	/**
	 * Assert-Statements benutzen?
	 */
	private static final boolean USE_ASSERT = true; 

	/**
	 * Logger und Loggerargument
	 */
	private ArgumentList alLogger;
	
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden
	 */
	private String TEST_DATEN_VERZ = null;
	
	/**
	 * Die zur Prüfung zu verwendende CSV Datei mit Testdaten
	 */
	private String csvPruefDatei = null;
	
	/**
	 * Gibt an, ob es sich um die Prüfung von TLS Daten handelt
	 */
	private boolean tlsPruefung = false;
	
	/**
	 * Testfahrstreifen KZD FS1, FS2, FS3
	 */
	public static SystemObject FS1 = null;
	public static SystemObject FS2 = null;
	public static SystemObject FS3 = null;

	/**
	 * Testfahrstreifen LZD FS1_LZD, FS2_LZD, FS3_LZD
	 */
	public static SystemObject FS1_LZ = null;
	public static SystemObject FS2_LZ = null;
	public static SystemObject FS3_LZ = null;
	
	/**
	 * Sende-Datenbeschreibung für LZD
	 */
	public static DataDescription DD_LZD_SEND = null;
	
	/**
	 * Sende-Datenbeschreibung für KZD
	 */
	public static DataDescription DD_KZD_SEND = null;

	/**
	 * Importer für die Parameter der KZD-FS
	 */
	private ParaKZDLogImport kzdImport1;
	private ParaKZDLogImport kzdImport2;
	private ParaKZDLogImport kzdImport3;

	/**
	 * Importer für die Parameter der LZD-FS
	 */
	private ParaLZDLogImport lzdImport1;
	private ParaLZDLogImport lzdImport2;
	private ParaLZDLogImport lzdImport3;
	
	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	/**
	 * Sendet Testdaten und vergleicht plausibilisierte Daten auf SOLL-Werte
	 * @param dav Datenteilerverbindung
	 * @param TEST_DATEN_VERZ Verzeichnis mit Testdaten
	 */
	public PlPruefungLogisch(ClientDavInterface dav, String TEST_DATEN_VERZ, ArgumentList alLogger) {
		this.dav = dav;
		this.TEST_DATEN_VERZ = TEST_DATEN_VERZ;
		this.alLogger = alLogger;

		/*
		 * Meldet Sender für KZD und LZD unter dem Aspekt Externe Erfassung an
		 */
		FS1 = this.dav.getDataModel().getObject("fs.mq.a100.0000.hfs"); //$NON-NLS-1$
		FS2 = this.dav.getDataModel().getObject("fs.mq.a100.0000.1üfs"); //$NON-NLS-1$
		FS3 = this.dav.getDataModel().getObject("fs.mq.a100.0000.2üfs"); //$NON-NLS-1$

		FS1_LZ = this.dav.getDataModel().getObject("AAA.Test.fs.lzd.1"); //$NON-NLS-1$
		FS2_LZ = this.dav.getDataModel().getObject("AAA.Test.fs.lzd.2"); //$NON-NLS-1$
		FS3_LZ = this.dav.getDataModel().getObject("AAA.Test.fs.lzd.3"); //$NON-NLS-1$

		
		DD_KZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
										  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
										  (short)0);
		
		DD_LZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
				  						  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				  						  (short)0);	
	}
	
	/**
	 * Konfiguriert Klasse für die Prüfung von KZD TLS, sendet Testdaten
	 * und führt Prüfung durch
	 * @throws Exception
	 */
	public void pruefeKZDTLS() throws Exception {
		this.csvPruefDatei = "PL-Pruef_LVE_TLS"; //$NON-NLS-1$
		
		this.tlsPruefung = true;
		
		LOGGER.info("Prüfe KZD TLS..."); //$NON-NLS-1$
		
		/*
		 * Importiere KZD TLS Parameter
		 */
		kzdImport1 = new ParaKZDLogImport(dav, FS1, TEST_DATEN_VERZ + "Parameter"); //$NON-NLS-1$
		kzdImport2 = new ParaKZDLogImport(dav, FS2, TEST_DATEN_VERZ + "Parameter"); //$NON-NLS-1$
		kzdImport3 = new ParaKZDLogImport(dav, FS3, TEST_DATEN_VERZ + "Parameter"); //$NON-NLS-1$
		
		doPruefeKZD();
	}
	
	/**
	 * Konfiguriert Klasse für die Prüfung von KZD auf Grenzwerte, sendet Testdaten
	 * und führt Prüfung durch
	 * @throws Exception
	 */
	public void pruefeKZDGrenz() throws Exception {
		this.csvPruefDatei = "PL-Pruef_LVE_Grenz";

		/*
		 * Initialisiere Logger
		 */
		Debug.init("PlPruefeKZDGrenz", alLogger); //$NON-NLS-1$
		
		LOGGER.info("Prüfe KZD Grenzwerte..."); //$NON-NLS-1$
		
		/*
		 * Importiere KZD Grenzwert Parameter
		 */
		kzdImport1 = new ParaKZDLogImport(dav, FS1, TEST_DATEN_VERZ + "Parameter");
		kzdImport2 = new ParaKZDLogImport(dav, FS2, TEST_DATEN_VERZ + "Parameter");
		kzdImport3 = new ParaKZDLogImport(dav, FS3, TEST_DATEN_VERZ + "Parameter");
		
		importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
		doPruefeKZD();
	}
	
	/**
	 * Konfiguriert Klasse für die Prüfung von LZD auf Grenzwerte, sendet Testdaten
	 * und führt Prüfung durch
	 * @throws Exception
	 */
	public void pruefeLZDGrenz() throws Exception {
		this.csvPruefDatei = "PL-Pruefung_LZD";
		
		LOGGER.info("Prüfe LZD Grenzwerte...");
		
		/*
		 * Importiere LZD Grenzwert Parameter
		 */		
		lzdImport1 = new ParaLZDLogImport(dav, FS1_LZ, TEST_DATEN_VERZ + "Parameter");
		lzdImport2 = new ParaLZDLogImport(dav, FS2_LZ, TEST_DATEN_VERZ + "Parameter");
		lzdImport3 = new ParaLZDLogImport(dav, FS3_LZ, TEST_DATEN_VERZ + "Parameter");
		
		importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
		doPruefeLZD();
	}
	
	/**
	 * Sendet Testdaten und führt KZD Prüfung entsprechend der Konfiguration durch
	 * @throws Exception
	 */
	private void doPruefeKZD()
	throws Exception {
		/*
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, new SystemObject[]{FS1, FS2, FS3}, 
				DD_KZD_SEND, SenderRole.source());
	
		/*
		 * Initialisiere Testfahrstreifen-Datenimporter
		 */
		TestFahrstreifenImporter fsImpFS1 = null;
		TestFahrstreifenImporter fsImpFS2 = null;
		TestFahrstreifenImporter fsImpFS3 = null;
		
		fsImpFS1 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1"); //$NON-NLS-1$
		fsImpFS2 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen2"); //$NON-NLS-1$
		fsImpFS3 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen3"); //$NON-NLS-1$
		
		/*
		 * Initialisiert Testerobjekt für den SOLL-IST-Vergleich
		 */
		PruefeDatenLogisch fsPruefer = new PruefeDatenLogisch(this, dav, new SystemObject[]{FS1, FS2, FS3}, TEST_DATEN_VERZ + csvPruefDatei);
		
		int csvIndex = 0;
		
		/*
		 * Beinhalten den aktuellen Testdatensatz für FS1, FS2, FS3
		 */
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;
		
		/*
		 * Ergbnisdatensätze
		 */
		ResultData resultat1 = null;
		ResultData resultat2 = null;
		ResultData resultat3 = null;
		
		
		/*
		 * Gibt an, ob fuer den entsprechenden Fahrstriefen Testdaten vorhanden sind
		 */
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;
		
		/*
		 * Gibt an, ob für mind. 1 FS noch Testdaten vorliegen
		 */
		boolean csvDatenVorhanden = true;
				
		long aktZeit = System.currentTimeMillis();
				
		while(csvDatenVorhanden) {
			/*
			 * Übergebe CSV-Offset und zu prüfenden Zeitstempel an Testerobjekt
			 */
			LOGGER.info("Setze CSV-Zeile und Zeitstempel fuer Prüfer -> Zeile:"+(csvIndex+2)+" Zeit:"+aktZeit); //$NON-NLS-1$ //$NON-NLS-2$
			fsPruefer.listen(csvIndex,aktZeit);
			
			/*
			 * Sende Testdaten für FS1, FS2, FS3
			 */
			if((zeileFS1 = fsImpFS1.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS1 (CSV-Zeile "+(csvIndex+2)+")");		 //$NON-NLS-1$ //$NON-NLS-2$
				resultat1 = new ResultData(FS1, DD_KZD_SEND, aktZeit, zeileFS1);
			} else{				
				datenFS1Vorhanden = false;
			}

			if((zeileFS2 = fsImpFS2.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS2 (CSV-Zeile "+(csvIndex+2)+")");		 //$NON-NLS-1$ //$NON-NLS-2$
				resultat2 = new ResultData(FS2, DD_KZD_SEND, aktZeit, zeileFS2);
			} else datenFS2Vorhanden = false;
	
			if((zeileFS3 = fsImpFS3.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS3 (CSV-Zeile "+(csvIndex+2)+")");		 //$NON-NLS-1$ //$NON-NLS-2$
				resultat3 = new ResultData(FS3, DD_KZD_SEND, aktZeit, zeileFS3);
			} else datenFS3Vorhanden = false;

			/*
			 * Prüft, ob noch Testdaten für die Fahrstreifen vorliegen
			 */
			if(!datenFS1Vorhanden || !datenFS2Vorhanden || !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				LOGGER.info("Keine Daten mehr vorhanedn. Beende Prüfung..."); //$NON-NLS-1$
			} else {
				LOGGER.info("Warte auf SOLL-IST-Vergleich (CSV-Zeile "+(csvIndex+1)+")...");  //$NON-NLS-1$//$NON-NLS-2$
				this.dav.sendData(new ResultData[]{resultat1,resultat2,resultat3});
				doWait();  //Warte auf Ueberpruefung der FS1-FS3
			}
			
			csvIndex++;
			
			/*
			 * Wechselt die Reaktionsart bei Grenzwertüberschreitung des nächsten DS
			 */
			if(!tlsPruefung) wechselReaktionKZD(csvIndex);
			
			aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
		}		
		
		/*
		 * Sender abmelden
		 */
		this.dav.unsubscribeSender(this, new SystemObject[]{FS1, FS2, FS3},	DD_KZD_SEND);
	}
	
	/**
	 * Sendet Testdaten und führt LZD Prüfung entsprechend der Konfiguration durch
	 * @throws Exception
	 */
	private void doPruefeLZD() throws Exception {
		/*
		 * Sender anmelden
		 */
		this.dav.subscribeSender(this, new SystemObject[]{FS1_LZ, FS2_LZ, FS3_LZ}, 
				DD_LZD_SEND, SenderRole.source());
		
		/*
		 * Initialisiere Testfahrstreifen-Datenimporter
		 */
		TestFahrstreifenImporter fsImpFS1 = null;
		TestFahrstreifenImporter fsImpFS2 = null;
		TestFahrstreifenImporter fsImpFS3 = null;
		
		fsImpFS1 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1"); //$NON-NLS-1$
		fsImpFS2 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen2"); //$NON-NLS-1$
		fsImpFS3 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen3"); //$NON-NLS-1$
		
		/*
		 * Initialisiert Testerobjekt für den SOLL-IST-Vergleich
		 */
		PruefeDatenLogisch fsPruefer = new PruefeDatenLogisch(this, dav, new SystemObject[]{FS1_LZ, FS2_LZ, FS3_LZ}, TEST_DATEN_VERZ + csvPruefDatei);
		
		//Aktueller Index (Zeile) in CSV Datei
		int csvIndex = 0;
		
		/*
		 * Beinhalten den aktuellen Testdatensatz für FS1, FS2, FS3
		 */
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;
		
		/*
		 * Ergbnisdatensätze
		 */
		ResultData resultat1 = null;
		ResultData resultat2 = null;
		ResultData resultat3 = null;
		
		/*
		 * Gibt an, ob fuer den entsprechenden Fahrstriefen Testdaten vorhanden sind
		 */
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;
		
		/*
		 * Gibt an, ob für mind. 1 FS noch Testdaten vorliegen
		 */
		boolean csvDatenVorhanden = true;
		
		long aktZeit = System.currentTimeMillis();
				
		while(csvDatenVorhanden) {
			/*
			 * Übergebe CSV-Offset und zu prüfenden Zeitstempel an Testerobjekt
			 */
			LOGGER.info("Setze CSV-Zeile und Zeitstempel für Pruefer -> Zeile:"+(csvIndex+2)+" Zeit:"+aktZeit);
			fsPruefer.listen(csvIndex,aktZeit);
			
			/*
			 * Sende Testdaten für FS1, FS2, FS3
			 */
			if((zeileFS1 = fsImpFS1.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS1 (CSV-Zeile "+(csvIndex+2)+")");		
				resultat1 = new ResultData(FS1_LZ, DD_LZD_SEND, aktZeit, zeileFS1);
			} else datenFS1Vorhanden = false;

			if((zeileFS2 = fsImpFS2.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS2 (CSV-Zeile "+(csvIndex+2)+")");
				 resultat2 = new ResultData(FS2_LZ, DD_LZD_SEND, aktZeit, zeileFS2);
			} else datenFS2Vorhanden = false;
	
			if((zeileFS3 = fsImpFS3.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS3 (CSV-Zeile "+(csvIndex+2)+")");
				resultat3 = new ResultData(FS3_LZ, DD_LZD_SEND, aktZeit, zeileFS3);
			} else datenFS3Vorhanden = false;
			
			/*
			 * Prüft, ob noch Testdaten für die Fahrstreifen vorliegen
			 */
			if(!datenFS1Vorhanden && !datenFS2Vorhanden && !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				LOGGER.info("Keine Daten mehr vorhanedn. Beende Prüfung...");
			} else {
				LOGGER.info("Warte auf SOLL-IST-Vergleich (CSV-Zeile "+(csvIndex+1)+")...");
				this.dav.sendData(new ResultData[]{resultat1,resultat2,resultat3});
				doWait();  //Warte auf Ueberpruefung der FS1-FS3
			}
			
			csvIndex++;
			
			/*
			 * Wechselt die Reaktionsart bei Grenzwertüberschreitung des nächsten DS
			 */
			wechselReaktionLZD(csvIndex);
			
			aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
		}
		
		/*
		 * Sender abmelden
		 */
		this.dav.unsubscribeSender(this, new SystemObject[]{FS1_LZ, FS2_LZ, FS3_LZ}, DD_LZD_SEND);
	}
	
	/**
	 * Importiert die Optionen für die Datenprüfung von KZD
	 * @param option Die zu importierende Option
	 * @throws Exception
	 */
	private void importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr option) throws Exception {
		kzdImport1.setOptionen(option);
		kzdImport1.importiereParameter(1);
		
		kzdImport2.setOptionen(option);
		kzdImport2.importiereParameter(2);
		
		kzdImport3.setOptionen(option);
		kzdImport3.importiereParameter(3);
	}
	
	/**
	 * Importiert die Optionen für die Datenprüfung von LZD
	 * @param option Die zu importierende Option
	 * @throws Exception
	 */
	private void importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr option) throws Exception {
		lzdImport1.setOptionen(option);
		lzdImport1.importiereParameter(1);
		
		lzdImport2.setOptionen(option);
		lzdImport2.importiereParameter(2);
		
		lzdImport3.setOptionen(option);
		lzdImport3.importiereParameter(3);
	}
	
	/**
	 * Wechselt die Reaktionsart auf Grenzwertüberschreitung bei KZD
	 * Ausgegangen wird von 100 Datensätzen -> Wechsel alle 20 Datensätze
	 * @param csvIndex Der DS-Index
	 * @throws Exception
	 */
	private void wechselReaktionKZD(int csvIndex) throws Exception {
		switch(csvIndex) {
			case 20: {
				importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
				break;
			}
			case 40: {
				importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX);
				break;
			}
			case 60: {
				importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
				break;
			}
			case 80: {
				importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
				break;
			}
		}
	}
	
	/**
	 * Wechselt die Reaktionsart auf Grenzwertüberschreitung bei LZD
	 * Ausgegangen wird von 100 Datensätzen -> Wechsel alle 20 Datensätze
	 * @param csvIndex Der DS-Index
	 * @throws Exception
	 */
	private void wechselReaktionLZD(int csvIndex) throws Exception {
		switch(csvIndex) {
			case 20: {
				importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
				break;
			}
			case 40: {
				importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX);
				break;
			}
			case 60: {
				importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
				break;
			}
			case 80: {
				importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
				break;
			}
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
				this.wait();
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
