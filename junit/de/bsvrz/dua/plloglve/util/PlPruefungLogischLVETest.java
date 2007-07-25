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
import sys.funclib.ArgumentList;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.dua.plloglve.pruef.PruefeKZDLogisch;
import de.bsvrz.dua.plloglve.util.para.ParaKZDLogImport;
import de.bsvrz.dua.plloglve.util.para.ParaLZDLogImport;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Automatisierter Test nach Prüfspezifikation für SWE Pl-Prüfung logisch LVE
 * 
 * @author Thierfelder
 *
 */
public class PlPruefungLogischLVETest
implements ClientSenderInterface {

	/**
	 * Verzeichnis, in dem sich die CSV-Dateien mit den Testdaten befinden
	 */
	protected static final String TEST_DATEN_VERZ = ".\\testDaten\\"; //$NON-NLS-1$
	
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
	 * TestFS1
	 */
	public static SystemObject FS1_LZ = null;
	
	/**
	 * TestFS2
	 */
	public static SystemObject FS2_LZ = null;
	
	/**
	 * TestFS3
	 */
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
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	/**
	 * Logger und Loggerattribute
	 * 
	 * Pfadangabe mit Argument: -debugFilePath=[Pfad]
	 */
	protected Debug LOGGER;
	private String[] argumente = new String[] {"-debugLevelFileText=ALL"};
	private ArgumentList alLogger = new ArgumentList(argumente);
	
	/**
	 * Definiert die verwendete Parameter CSV-Datei
	 * 
	 * Prameter.csv: Logische Prüfung mit Grenzwerten
	 * Prameter_TLS.csv: Logische Prüfung ohne Grenzwerte (TLS)
	 */
	//private String csvParameterDatei = "Parameter_TLS";
	private String csvParameterDatei = "Parameter";

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
	 * {@inheritDoc}
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav();	

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
		
		this.dav.subscribeSender(this, new SystemObject[]{FS1, FS2, FS3}, 
				DD_KZD_SEND, SenderRole.source());
		
		this.dav.subscribeSender(this, new SystemObject[]{FS1_LZ, FS2_LZ, FS3_LZ}, 
				DD_LZD_SEND, SenderRole.source());
		
		Debug.init("PlPruefungLogisch", alLogger); //$NON-NLS-1$
		LOGGER = Debug.getLogger();
	}

	/**
	 * Gesamter Test nach Prüfspezifikation
	 * 
	 * Definition der Grenzwertparameter
	 */
	@Test
	public void testAlles()throws Exception{

		kzdImport1 = new ParaKZDLogImport(dav, FS1, TEST_DATEN_VERZ + csvParameterDatei);
		kzdImport2 = new ParaKZDLogImport(dav, FS2, TEST_DATEN_VERZ + csvParameterDatei);
		kzdImport3 = new ParaKZDLogImport(dav, FS3, TEST_DATEN_VERZ + csvParameterDatei);

		lzdImport1 = new ParaLZDLogImport(dav, FS1_LZ, TEST_DATEN_VERZ + "Parameter"); //$NON-NLS-1$
		lzdImport2 = new ParaLZDLogImport(dav, FS2_LZ, TEST_DATEN_VERZ + "Parameter"); //$NON-NLS-1$
		lzdImport3 = new ParaLZDLogImport(dav, FS3_LZ, TEST_DATEN_VERZ + "Parameter"); //$NON-NLS-1$
		
		//importiert Optionen für ersten Teil der Prüfdaten
		importOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);

		
		/*
		 * Prüffälle
		 * 
		 * plPruefungKZDLogisch: Logische Prüfung
		 * plPruefungKZDDiff: Prüfung Differentialkontrolle
		 * plPruefungKZDAusfall: Prüfung Ausfallhäufigkeit
		 * 
		 * Weitere Einstellungen können in den entsprechenden Funktionen
		 * vorgenommen werden
		 */
		//plPruefungKZDLogisch();
		plPruefungLZDLogisch();
		//plPruefungKZDDiff();
		//plPruefungKZDAusfall();
	}
	
	/**
	 * Importiert die Optionen für die Datenprüfung
	 */
	private void importOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr option) throws Exception {
		kzdImport1.setOptionen(option);
		kzdImport1.importiereParameter(1);
		
		kzdImport2.setOptionen(option);
		kzdImport2.importiereParameter(2);
		
		kzdImport3.setOptionen(option);
		kzdImport3.importiereParameter(3);
		
		lzdImport1.setOptionen(option);
		lzdImport1.importiereParameter(1);
		
		lzdImport2.setOptionen(option);
		lzdImport2.importiereParameter(2);
		
		lzdImport3.setOptionen(option);
		lzdImport3.importiereParameter(3);
	}
	
	/**
	 * Prüfen der logisch Plausibilisierung
	 * 
	 * Für Grenzwertprüfung ist Parametrierung (Parameter.csv) entsprechend
	 * zu ändern
	 */
	private void plPruefungKZDLogisch() throws Exception {
		//Initialisiere Parameter Importer
		TestFahrstreifenImporter paraImpFS1 = null;
		TestFahrstreifenImporter paraImpFS2 = null;
		TestFahrstreifenImporter paraImpFS3 = null;
		
		/*
		 * Definition der Quell CSV-Dateien
		 * 
		 * Für Grenzwertprüfung ist auf Import der korrekten
		 * Parameter (Prameter.csv) zu achten 
		 */
		paraImpFS1 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1"); //$NON-NLS-1$
		paraImpFS2 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen2"); //$NON-NLS-1$
		paraImpFS3 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen3"); //$NON-NLS-1$
		
		//Initialisiere Daten-Listener (ClientReceiver)
		LOGGER.info("Initialisiere Prueferobjekt");

		/*
		 * Definition der Soll CSV-Datei
		 * 
		 * PL-Pruef_LVE_TLS.csv: Soll-Werte der logischen Prüfung ohne Grenzwerte
		 * PL-Pruef_LVE_Grenz.csv: Soll-Werte der logischen Prüfung mit Grenzwerten
		 * PL-Pruefung_LZD.csv: Soll-Werte der logischen Prüfung von LZD 
		 */
		//String csvPruefDatei = "PL-Pruef_LVE_TLS";		//Pruefdatei KZD TLS
		//String csvPruefDatei = "PL-Pruef_LVE_TLS_Korr";		//Pruefdatei KZD TLS
		//String csvPruefDatei = "PL-Pruef_LVE_Grenz";		//Pruefdatei KZD Grenzwerte
		String csvPruefDatei = "PL-Pruefung_LZD";		//Pruefdatei LZD
		
		PruefeKZDLogisch fsPruefer = new PruefeKZDLogisch(this, new SystemObject[]{FS1, FS2, FS3}, TEST_DATEN_VERZ + csvPruefDatei);
		
		//Aktueller Index (Zeile) in CSV Datei
		int csvIndex = 0;
		
		//Ergibnisdaten der jeweiligen Fahrstreifen
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;
		
		//Gibt an, ob fuer den entsprechenden Fahrstriefen CSV Daten vorhanden sind
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;
		
		//Aktueller Zeitstempel
		long aktZeit = System.currentTimeMillis();  //setze aktuelle Zeit
		
		boolean csvDatenVorhanden = true;
		
		while(csvDatenVorhanden) {
			//uebergebe CSV Index und Zeitstempel an Listener
			LOGGER.info("Setze CSV-Offset und Zeitstempel fuer Pruefer -> Offset:"+csvIndex+" Zeit:"+aktZeit);
			fsPruefer.listen(csvIndex,aktZeit);
			
			//Lese CSV Parameter und Ergebnisdatensatz erstellen + senden
			//Es werden die Daten der Fahrstreifen FS1-FS3 gesendet bis keine
			//CSV Daten mehr zur Verfuegung stehen 
			
			if((zeileFS1 = paraImpFS1.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS1");		
				ResultData resultat1 = new ResultData(FS1, DD_KZD_SEND, aktZeit, zeileFS1);
				this.dav.sendData(resultat1);
			} else datenFS1Vorhanden = false;

			if((zeileFS2 = paraImpFS2.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS2");
				ResultData resultat2 = new ResultData(FS2, DD_KZD_SEND, aktZeit, zeileFS2);
				this.dav.sendData(resultat2);
			} else datenFS2Vorhanden = false;
	
			if((zeileFS3 = paraImpFS3.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS3");
				ResultData resultat3 = new ResultData(FS3, DD_KZD_SEND, aktZeit, zeileFS3);
				this.dav.sendData(resultat3);
			} else datenFS3Vorhanden = false;
			
			//Erhoehe CSV Index
			csvIndex++;
			
			//wechsel der Reaktionsart auf Grenzwertüberschreitung
			wechselReaktion(csvIndex);
			
			//Erhöhe Zeitstempel
			aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
			
			//Wenn keine CSV Daten mehr
			if(!datenFS1Vorhanden && !datenFS2Vorhanden && !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				LOGGER.info("Keine Daten mehr vorhanedn. Beende Pruefung");
			} else {
				LOGGER.info("Warte auf Pruefer");
				doWait();  //Warte auf Ueberpruefung der FS1-FS3
			}
		}		
	}

	/**
	 * Prüfung der Differentialkontrolle
	 */
	private void plPruefungKZDDiff() throws Exception {
		//Initialisiere Parameter Importer
		TestFahrstreifenImporter paraImpFSDiff = null;
		/*
		 * Definition der Quell CSV-Datei entsprechend der
		 * Prüfspezifikation (Prüffall 20)
		 */
		paraImpFSDiff = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen2_Diff"); //$NON-NLS-1$
		
		//Ergibnisdaten der jeweiligen Fahrstreifen
		Data zeileFSDiff;
		
		//Aktuelle Zeit
		Long aktZeit = System.currentTimeMillis();
		
		//Sende Differentialkontrolldaten (3x)
		int dsCount = 0;
		for(int i=0;i<3;i++) {
			while((zeileFSDiff = paraImpFSDiff.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) != null) {
				ResultData resultat1 = new ResultData(FS1, DD_KZD_SEND, aktZeit, zeileFSDiff);
				this.dav.sendData(resultat1);	
				dsCount++;
				LOGGER.info("Durchlauf:"+i+" - Datensatz:"+dsCount+" - Zeit:"+aktZeit);
				
				aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
			}
			paraImpFSDiff.reset();
			paraImpFSDiff.getNaechsteZeile();
		}
	}
	
	/**
	 * Prüfung der Ausfallhäufigkeit
	 * 
	 * Simuliert werden kann das Senden von fehlerhaften/implausiblen
	 * Daten oder Datenausfall 
	 */
	private void plPruefungKZDAusfall() throws Exception {
		//Initialisiere Parameter Importer
		TestFahrstreifenImporter paraImpFSOK = null;
		TestFahrstreifenImporter paraImpFSFehler = null;
		
		/*
		 * Definition der Quell CSV-Dateien
		 * 
		 * paraImpFSOK: Quell-Daten welche durch vorherige Prüfungen
		 * 				(logisch + differential) nicht Implausibel
		 * 				und/oder Fehlerhaft markiert wurden
		 * 
		 * paraImpFSFehler: Quell-Daten welche durch vorherige Prüfungen
		 * 					Implausibel und/oder Fehlerhaft markiert werden
		 */
		paraImpFSOK = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1_OK"); //$NON-NLS-1$
		paraImpFSFehler = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1_Fehler"); //$NON-NLS-1$
		
		//Ergibnisdaten der jeweiligen Fahrstreifen
		Data zeileFSOK;
		Data zeileFSFehler;
		
		//Aktuelle Zeit
		Long aktZeit = System.currentTimeMillis()-(Konstante.MINUTE_IN_MS*1440*2);
		
		//Leere Daten (Vortag)
		
		for(int i=1;i<=1440;i++) {
			if((zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) == null) {
				paraImpFSOK.reset();
				paraImpFSOK.getNaechsteZeile();
				zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
			}
			ResultData resultat1 = new ResultData(FS1, DD_KZD_SEND, aktZeit, zeileFSOK);
			this.dav.sendData(resultat1);
			
			aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
		}
		
		
		//Ueberschreite Ausfallhaeufigkeit
		boolean simAusfall = false;
		
		int aAusfall = 0;
		int aOK = 0;
		int countAusfall = 0;
		int countOK = 0;
		
		for(int i=1;i<=1440;i++) {
			if(aAusfall == 0 && aOK == 0) {
				aOK = getaOK();
				aAusfall = getaAusfall();
				//LOGGER.info("Zufallszahlen: OK "+aOK+" - Ausfall "+aAusfall);
			}
			
			if(!simAusfall) {
				aOK--;
				countOK++;
				if(aOK == 0) {
					LOGGER.info("Wechsel zu Ausfall");
					simAusfall = true;
				}
				//Sende daten
				//LOGGER.info("OK - Interval "+i+" ("+aOK+") => OK gesamt: "+countOK);
				if((zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) == null) {
					paraImpFSOK.reset();
					paraImpFSOK.getNaechsteZeile();
					zeileFSOK = paraImpFSOK.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
				}
				ResultData resultat1 = new ResultData(FS1, DD_KZD_SEND, aktZeit, zeileFSOK);
				this.dav.sendData(resultat1);
				
			} else {
				aAusfall--;
				countAusfall++;
				if(aAusfall == 0) {
					LOGGER.info("Wechsel zu OK");
					simAusfall = false;
				}
				/*
				 * Sende Datum bei dem mind. 1 Attribut als Fehlerhaft
				 * und/oder Implausibel gekennzeichnet wird
				 *
				 * Durch nichtversenden dieser Daten kann ein Datenausfall
				 * simuliert werden 
				 */

				//LOGGER.info("Ausfall - Interval "+i+" ("+aAusfall+") => Ausfälle gesamt: "+countAusfall);
				if((zeileFSFehler = paraImpFSFehler.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup())) == null) {
					paraImpFSFehler.reset();
					paraImpFSFehler.getNaechsteZeile();
					zeileFSFehler = paraImpFSFehler.getNaechstenDatensatz(DD_KZD_SEND.getAttributeGroup());
				}
				ResultData resultat1 = new ResultData(FS1, DD_KZD_SEND, aktZeit, zeileFSFehler);
				this.dav.sendData(resultat1);
				
			}
			
			aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
		}
		
	}
	
	
	
	/**
	 * Standardprüfung für LZD-Sätze (nur Wertebereichsprüfung)
	 */
	private void plPruefungLZDLogisch()
	throws Exception {
		//Initialisiere Parameter Importer
		TestFahrstreifenImporter paraImpFS1 = null;
		TestFahrstreifenImporter paraImpFS2 = null;
		TestFahrstreifenImporter paraImpFS3 = null;
		
		/*
		 * Definition der Quell CSV-Dateien
		 * 
		 * Für Grenzwertprüfung ist auf Import der korrekten
		 * Parameter (Prameter.csv) zu achten 
		 */
		paraImpFS1 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen1"); //$NON-NLS-1$
		paraImpFS2 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen2"); //$NON-NLS-1$
		paraImpFS3 = new TestFahrstreifenImporter(this.dav, TEST_DATEN_VERZ + "fahrstreifen3"); //$NON-NLS-1$
		
		//Initialisiere Daten-Listener (ClientReceiver)
		LOGGER.info("Initialisiere Prueferobjekt"); //$NON-NLS-1$

		/*
		 * Definition der Soll CSV-Datei
		 * 
		 * PL-Pruef_LVE_TLS.csv: Soll-Werte der logischen Prüfung ohne Grenzwerte
		 * PL-Pruef_LVE_Grenz.csv: Soll-Werte der logischen Prüfung mit Grenzwerten
		 * PL-Pruefung_LZD.csv: Soll-Werte der logischen Prüfung von LZD 
		 */
		String csvPruefDatei = "PL-Pruefung_LZD";		//Pruefdatei LZD //$NON-NLS-1$
		
		PruefeKZDLogisch fsPruefer = new PruefeKZDLogisch(this, new SystemObject[]{FS1_LZ, FS2_LZ, FS3_LZ},
				TEST_DATEN_VERZ + csvPruefDatei);
		
		//Aktueller Index (Zeile) in CSV Datei
		int csvIndex = 0;
		
		//Ergibnisdaten der jeweiligen Fahrstreifen
		Data zeileFS1;
		Data zeileFS2;
		Data zeileFS3;
		
		//Gibt an, ob fuer den entsprechenden Fahrstriefen CSV Daten vorhanden sind
		boolean datenFS1Vorhanden = true;
		boolean datenFS2Vorhanden = true;
		boolean datenFS3Vorhanden = true;
		
		//Aktueller Zeitstempel
		long aktZeit = System.currentTimeMillis();  //setze aktuelle Zeit
		
		boolean csvDatenVorhanden = true;
		
		while(csvDatenVorhanden) {
			//uebergebe CSV Index und Zeitstempel an Listener
			LOGGER.info("Setze CSV-Offset und Zeitstempel fuer Pruefer -> Offset:"+csvIndex+" Zeit:"+aktZeit); //$NON-NLS-1$ //$NON-NLS-2$
			fsPruefer.listen(csvIndex,aktZeit);
			
			//Lese CSV Parameter und Ergebnisdatensatz erstellen + senden
			//Es werden die Daten der Fahrstreifen FS1-FS3 gesendet bis keine
			//CSV Daten mehr zur Verfuegung stehen 
			
			if((zeileFS1 = paraImpFS1.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS1_LZ");		 //$NON-NLS-1$
				ResultData resultat1 = new ResultData(FS1_LZ, DD_LZD_SEND, aktZeit, zeileFS1);
				this.dav.sendData(resultat1);
			} else datenFS1Vorhanden = false;

			if((zeileFS2 = paraImpFS2.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS2_LZ"); //$NON-NLS-1$
				ResultData resultat2 = new ResultData(FS2_LZ, DD_LZD_SEND, aktZeit, zeileFS2);
				this.dav.sendData(resultat2);
			} else datenFS2Vorhanden = false;
	
			if((zeileFS3 = paraImpFS3.getNaechstenDatensatz(DD_LZD_SEND.getAttributeGroup())) != null) {
				LOGGER.info("Sende Daten fuer FS3_LZ"); //$NON-NLS-1$
				ResultData resultat3 = new ResultData(FS3_LZ, DD_LZD_SEND, aktZeit, zeileFS3);
				this.dav.sendData(resultat3);
			} else datenFS3Vorhanden = false;
			
			//Erhoehe CSV Index
			csvIndex++;
			
			//wechsel der Reaktionsart auf Grenzwertüberschreitung
			wechselReaktion(csvIndex);
			
			//Erhöhe Zeitstempel
			aktZeit = aktZeit + Konstante.MINUTE_IN_MS;
			
			//Wenn keine CSV Daten mehr
			if(!datenFS1Vorhanden && !datenFS2Vorhanden && !datenFS3Vorhanden) {
				csvDatenVorhanden = false;
				LOGGER.info("Keine Daten mehr vorhanedn. Beende Pruefung"); //$NON-NLS-1$
			} else {
				LOGGER.info("Warte auf Pruefer"); //$NON-NLS-1$
				doWait();  //Warte auf Ueberpruefung der FS1-FS3
			}
		}		
	}

	
	
	/**
	 * Gibt eine Zufallszahl (1-100) zurück, welche die Anzahl der
	 * Daten repräsentiert, welche als nicht fehlerhaft und/oder
	 * implausibel markiert werden
	 * @return Anzahl der zu sendenden fehlerfreien Daten
	 */
	private int getaOK() {
		int iZufall = 0;
		while(iZufall == 0) {
			iZufall = zufallsZahl();
		}
		return iZufall*20;
	}
	
	/**
	 * Gibt eine Zufallszahl (1-5) zurück, welche die Anzahl der
	 * Daten repräsentiert, welche als fehlerhaft und/oder
	 * implausibel markiert werden
	 * @return Anzahl der zu sendenden fehlerhaften Daten
	 */
	private int getaAusfall() {
		int iZufall = 0;
		while(iZufall == 0) {
			iZufall = zufallsZahl();
		}
		return iZufall;
	}
	
	/**
	 * Gibt eine Zufallszahl (0-5) zurück
	 * @return Zufallszahl (0-5)
	 */
	private int zufallsZahl() {
		Double dZufall = Math.random()*5;
		return dZufall.intValue();
	}
	
	/**
	 * Wechselt die Reaktionsart auf Grenzwertüberschreitung
	 * 
	 * Ausgegangen wird von 100 Datensätzen -> Wechsel alle 20 Datensätze
	 */
	private void wechselReaktion(int csvIndex) throws Exception {
		switch(csvIndex) {
			case 20: {
				importOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN);
				break;
			}
			case 40: {
				importOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX);
				break;
			}
			case 60: {
				importOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG);
				break;
			}
			case 80: {
				importOptionen(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG);
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
