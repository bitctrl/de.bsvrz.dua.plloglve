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
import de.bsvrz.dua.plloglve.pruef.PruefeDatenLogisch;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.util.para.ParaLZDLogImport;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

public class PlPruefungLogisch
implements ClientSenderInterface {

	/**
	 * Logger
	 */
	protected Debug LOGGER = Debug.getLogger();
	
	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden
	 */
	private String TEST_DATEN_VERZ = null;
	
	/**
	 * Die zur Pr�fung zu verwendende CSV Datei mit Testdaten
	 */
	private String csvPruefDatei = null;
	
	/**
	 * Gibt an, ob es sich um die Pr�fung von TLS Daten handelt
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
	 * Sende-Datenbeschreibung f�r LZD
	 */
	public static DataDescription DD_LZD_SEND = null;
	
	/**
	 * Sende-Datenbeschreibung f�r KZD
	 */
	public static DataDescription DD_KZD_SEND = null;

	/**
	 * Importer f�r die Parameter der KZD-FS
	 */
	private ParaKZDLogImport kzdImport1;
	private ParaKZDLogImport kzdImport2;
	private ParaKZDLogImport kzdImport3;

	/**
	 * Importer f�r die Parameter der LZD-FS
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
	public PlPruefungLogisch(ClientDavInterface dav, String TEST_DATEN_VERZ) {
		this.dav = dav;
		this.TEST_DATEN_VERZ = TEST_DATEN_VERZ;

		/*
		 * Meldet Sender f�r KZD und LZD unter dem Aspekt Externe Erfassung an
		 */
		FS1 = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		FS2 = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.2"); //$NON-NLS-1$
		FS3 = this.dav.getDataModel().getObject("AAA.Test.fs.kzd.3"); //$NON-NLS-1$

		FS1_LZ = this.dav.getDataModel().getObject("AAA.Test.fs.lzd.1"); //$NON-NLS-1$
		FS2_LZ = this.dav.getDataModel().getObject("AAA.Test.fs.lzd.2"); //$NON-NLS-1$
		FS3_LZ = this.dav.getDataModel().getObject("AAA.Test.fs.lzd.3"); //$NON-NLS-1$

		
		DD_KZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
										  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
										  (short)0);
		
		DD_LZD_SEND = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
				  						  this.dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				  						  (short)0);	
		
		try{
			this.dav.subscribeSender(this, new SystemObject[]{FS1, FS2, FS3}, 
					DD_KZD_SEND, SenderRole.source());
		
			this.dav.subscribeSender(this, new SystemObject[]{FS1_LZ, FS2_LZ, FS3_LZ}, 
					DD_LZD_SEND, SenderRole.source());
		} catch (Exception e) {
			LOGGER.error("Empf�nger konnte nicht angemeldet werden: "+e);
		}
	}
	
	/**
	 * Konfiguriert Klasse f�r die Pr�fung von KZD TLS, sendet Testdaten
	 * und f�hrt Pr�fung durch
	 * @throws Exception
	 */
	public void pruefeKZDTLS() throws Exception {
		this.csvPruefDatei = "PL-Pruef_LVE_TLS_Korr";
		this.tlsPruefung = true;

		LOGGER.info("Pr�fe KZD TLS...");
		
		/*
		 * Importiere KZD TLS Parameter
		 */
		kzdImport1 = new ParaKZDLogImport(dav, FS1, TEST_DATEN_VERZ + "Parameter_TLS");
		kzdImport2 = new ParaKZDLogImport(dav, FS2, TEST_DATEN_VERZ + "Parameter_TLS");
		kzdImport3 = new ParaKZDLogImport(dav, FS3, TEST_DATEN_VERZ + "Parameter_TLS");
		
		importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
		importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
		importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
		importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
		importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
		
		doPruefeKZD();
	}
	
	/**
	 * Konfiguriert Klasse f�r die Pr�fung von KZD auf Grenzwerte, sendet Testdaten
	 * und f�hrt Pr�fung durch
	 * @throws Exception
	 */
	public void pruefeKZDGrenz() throws Exception {
		this.csvPruefDatei = "PL-Pruef_LVE_Grenz";

		LOGGER.info("Pr�fe KZD Grenzwerte...");
		
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
	 * Konfiguriert Klasse f�r die Pr�fung von LZD auf Grenzwerte, sendet Testdaten
	 * und f�hrt Pr�fung durch
	 * @throws Exception
	 */
	public void pruefeLZDGrenz() throws Exception {
		this.csvPruefDatei = "PL-Pruefung_LZD";
		
		LOGGER.info("Pr�fe LZD Grenzwerte...");
		
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
	 * Sendet Testdaten und f�hrt KZD Pr�fung entsprechend der Konfiguration durch
	 * @throws Exception
	 */
	private void doPruefeKZD() throws Exception {
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
		 * Initialisiert Testerobjekt f�r den SOLL-IST-Vergleich
		 */
		PruefeDatenLogisch fsPruefer = new PruefeDatenLogisch(this, dav, new SystemObject[]{FS1, FS2, FS3}, TEST_DATEN_VERZ + csvPruefDatei);
		
		int csvIndex = 0;
		
		/*
		 * Beinhalten den aktuellen Testdatensatz f�r FS1, FS2, FS3
		 */
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;
		
		/*
		 * Gibt an, ob fuer den entsprechenden Fahrstriefen Testdaten vorhanden sind
		 */
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;
		
		/*
		 * Gibt an, ob f�r mind. 1 FS noch Testdaten vorliegen
		 */
		boolean csvDatenVorhanden = true;
				
		long aktZeit = System.currentTimeMillis();
				
		while(csvDatenVorhanden) {
			/*
			 * �bergebe CSV-Offset und zu pr�fenden Zeitstempel an Testerobjekt
			 */
			LOGGER.info("Setze CSV-Offset und Zeitstempel fuer Pr�fer -> Offset:"+csvIndex+" Zeit:"+aktZeit);
			fsPruefer.listen(csvIndex,aktZeit);
			
			/*
			 * Sende Testdaten f�r FS1, FS2, FS3
			 */
			if((zeileFS1 = fsImpFS1.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS1");		
				ResultData resultat1 = new ResultData(FS1, DD_KZD_SEND, aktZeit, zeileFS1);
				this.dav.sendData(resultat1);
			} else datenFS1Vorhanden = false;

			if((zeileFS2 = fsImpFS2.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS2");
				ResultData resultat2 = new ResultData(FS2, DD_KZD_SEND, aktZeit, zeileFS2);
				this.dav.sendData(resultat2);
			} else datenFS2Vorhanden = false;
	
			if((zeileFS3 = fsImpFS3.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS3");
				ResultData resultat3 = new ResultData(FS3, DD_KZD_SEND, aktZeit, zeileFS3);
				this.dav.sendData(resultat3);
			} else datenFS3Vorhanden = false;
			
			csvIndex++;
			
			/*
			 * Wechselt die Reaktionsart bei Grenzwert�berschreitung des n�chsten DS
			 */
			if(!tlsPruefung) wechselReaktionKZD(csvIndex);
			
			aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
			
			/*
			 * Pr�ft, ob noch Testdaten f�r die Fahrstreifen vorliegen
			 */
			if(!datenFS1Vorhanden && !datenFS2Vorhanden && !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				LOGGER.info("Keine Daten mehr vorhanedn. Beende Pr�fung...");
			} else {
				LOGGER.info("Warte auf SOLL-IST-Vergleich...");
				doWait();  //Warte auf Ueberpruefung der FS1-FS3
			}
		}		
	}
	
	/**
	 * Sendet Testdaten und f�hrt LZD Pr�fung entsprechend der Konfiguration durch
	 * @throws Exception
	 */
	private void doPruefeLZD() throws Exception {
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
		 * Initialisiert Testerobjekt f�r den SOLL-IST-Vergleich
		 */
		PruefeDatenLogisch fsPruefer = new PruefeDatenLogisch(this, dav, new SystemObject[]{FS1, FS2, FS3}, TEST_DATEN_VERZ + csvPruefDatei);
		
		//Aktueller Index (Zeile) in CSV Datei
		int csvIndex = 0;
		
		/*
		 * Beinhalten den aktuellen Testdatensatz f�r FS1, FS2, FS3
		 */
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;
		
		/*
		 * Gibt an, ob fuer den entsprechenden Fahrstriefen Testdaten vorhanden sind
		 */
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;
		
		/*
		 * Gibt an, ob f�r mind. 1 FS noch Testdaten vorliegen
		 */
		boolean csvDatenVorhanden = true;
		
		long aktZeit = System.currentTimeMillis();
				
		while(csvDatenVorhanden) {
			/*
			 * �bergebe CSV-Offset und zu pr�fenden Zeitstempel an Testerobjekt
			 */
			LOGGER.info("Setze CSV-Offset und Zeitstempel f�r Pruefer -> Offset:"+csvIndex+" Zeit:"+aktZeit);
			fsPruefer.listen(csvIndex,aktZeit);
			
			/*
			 * Sende Testdaten f�r FS1, FS2, FS3
			 */
			if((zeileFS1 = fsImpFS1.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS1");		
				ResultData resultat1 = new ResultData(FS1_LZ, DD_LZD_SEND, aktZeit, zeileFS1);
				this.dav.sendData(resultat1);
			} else datenFS1Vorhanden = false;

			if((zeileFS2 = fsImpFS2.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS2");
				ResultData resultat2 = new ResultData(FS2_LZ, DD_LZD_SEND, aktZeit, zeileFS2);
				this.dav.sendData(resultat2);
			} else datenFS2Vorhanden = false;
	
			if((zeileFS3 = fsImpFS3.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS3");
				ResultData resultat3 = new ResultData(FS3_LZ, DD_LZD_SEND, aktZeit, zeileFS3);
				this.dav.sendData(resultat3);
			} else datenFS3Vorhanden = false;
			
			csvIndex++;
			
			/*
			 * Wechselt die Reaktionsart bei Grenzwert�berschreitung des n�chsten DS
			 */
			wechselReaktionLZD(csvIndex);
			
			aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
			
			/*
			 * Pr�ft, ob noch Testdaten f�r die Fahrstreifen vorliegen
			 */
			if(!datenFS1Vorhanden && !datenFS2Vorhanden && !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				LOGGER.info("Keine Daten mehr vorhanedn. Beende Pr�fung...");
			} else {
				LOGGER.info("Warte auf SOLL-IST-Vergleich...");
				doWait();  //Warte auf Ueberpruefung der FS1-FS3
			}
		}		
	}
	
	/**
	 * Importiert die Optionen f�r die Datenpr�fung von KZD
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
	 * Importiert die Optionen f�r die Datenpr�fung von LZD
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
	 * Wechselt die Reaktionsart auf Grenzwert�berschreitung bei KZD
	 * Ausgegangen wird von 100 Datens�tzen -> Wechsel alle 20 Datens�tze
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
				importOptionenKZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
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
	 * Wechselt die Reaktionsart auf Grenzwert�berschreitung bei LZD
	 * Ausgegangen wird von 100 Datens�tzen -> Wechsel alle 20 Datens�tze
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
				importOptionenLZD(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
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
	 * L�ssten diesen Thread warten
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