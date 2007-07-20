package de.bsvrz.dua.plloglve.pruef;

import java.util.HashMap;
import java.util.ArrayList;

import de.bsvrz.dua.plloglve.util.CSVImporter;
import stauma.dav.configuration.interfaces.SystemObject;
import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.Data;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import stauma.dav.clientside.ClientReceiverInterface;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.dua.plloglve.util.PlPruefungLogischLVETest;
import sys.funclib.debug.Debug;

/*
 * KZD Listener
 * Liest Ergebnis-CSV-Datei
 * Wartet auf gesendete und gepruefte Daten und gibt diese an Vergleicher-Klasse weiter
 */
public class PruefeKZDLogisch
implements ClientReceiverInterface {

	/**
	 * Logger
	 */
	protected Debug LOGGER = Debug.getLogger();
	
	/**
	 * Datenverteilerverbindung von der aufrufenden Klasse
	 */
	private ClientDavInterface dav;
	
	/**
	 * Aufrunfende Klasse
	 */
	private PlPruefungLogischLVETest caller;
	
	/*
	 * CSV-Importer
	 */
	private CSVImporter csvImp;
	
	/**
	 * CSV Index
	 */
	private int csvOffset;
	
	/**
	 * Speichert die eingelesenen CSV Daten
	 */
	private ArrayList<HashMap<String,Integer>> csvZeilenFS1 = new ArrayList<HashMap<String,Integer>>();
	private ArrayList<HashMap<String,Integer>> csvZeilenFS2 = new ArrayList<HashMap<String,Integer>>();
	private ArrayList<HashMap<String,Integer>> csvZeilenFS3 = new ArrayList<HashMap<String,Integer>>();
	
	private Long csvWerttNetto;
	
	private ArrayList<Long> alCSVWerttNettoFS1 = new ArrayList<Long>();
	private ArrayList<Long> alCSVWerttNettoFS2 = new ArrayList<Long>();
	private ArrayList<Long> alCSVWerttNettoFS3 = new ArrayList<Long>();
	
	/**
	 * Zeitstempel der zu pruefenden Daten
	 */
	private long pruefZeitstempel;
	
	/**
	 * Gibt an, ob die jeweilige Fahrstreifenpruefung erfolgt ist
	 */
	private boolean pruefungFS1Fertig = false;
	private boolean pruefungFS2Fertig = false;
	private boolean pruefungFS3Fertig = false;
	
	/**
	 * Empfange-Datenbeschreibung für KZD und LZD
	 */
	public static DataDescription DD_KZD_EMPF = null;
	public static DataDescription DD_LZD_EMPF = null;
	
	/**
	 * Listener, der auf ein bestimmtes Ergenis wartet und dann eine Prüfung
	 * der Ergebnisdaten durchführt
	 * @param caller Die aufrufende Klasse
	 * @param fs Die zu überwachenden Fahrstreifenobjekte
	 * @param csvQuelle Die Quell CSV-Datei mit Soll-Werten
	 * @throws Exception
	 */
	public PruefeKZDLogisch(PlPruefungLogischLVETest caller, SystemObject[] fs, String csvQuelle)
	throws Exception {
		this.caller = caller;  //aufrufende Klasse uebernehmen
		this.dav = caller.uebergebeDAV();  //DAV uebernehmen
		
		//Empfangs-Datenbeschreibung
		DD_KZD_EMPF = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
			      this.dav.getDataModel().getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
			      (short)0);
		
		DD_LZD_EMPF = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
			      this.dav.getDataModel().getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
			      (short)0);
		
		//Empfänger anmelden
		this.dav.subscribeReceiver(this, fs, 
				DD_KZD_EMPF, ReceiveOptions.normal(), ReceiverRole.receiver());
		
		this.dav.subscribeReceiver(this, fs, 
				DD_LZD_EMPF, ReceiveOptions.normal(), ReceiverRole.receiver());
		
		try {
			//CSV Importer initialisieren
			this.csvImp = new CSVImporter(csvQuelle);
		} catch(Exception e) {
			LOGGER.error("Fehler beim importieren der CSV Datei: "+e);
		}
		csvImp.getNaechsteZeile();  //Tabellenkopf in CSV ueberspringen
		csvEinlesen();  //CSV einlesen
	}
	
	/**
	 * Setzt den zu verwendenden Offset in der CSV-Datei und den
	 * Zeitstempel des Ergebnisses, auf das gewartet werden soll
	 * @param csvOffset Zu verwendender Offset in CSV-Datei
	 * @param pruefZeitstempel Ergebniszeitstempel, auf den gewartet wird
	 */
	public void listen(int csvOffset, long pruefZeitstempel) {
		this.csvOffset = csvOffset;  //CSV-Index uebernehmen
		this.pruefZeitstempel = pruefZeitstempel;  //Zeitstempel uebernehmen
		
		//keine Pruefung abgeschlossen
		pruefungFS1Fertig = false;
		pruefungFS2Fertig = false;
		pruefungFS3Fertig = false;
	}
	
	/**
	 * CSV-Einlesen
	 */
	private void csvEinlesen() {
		String[] aktZeile;
		
		try {
			while((aktZeile = csvImp.getNaechsteZeile()) != null) {
				if(aktZeile.length > 0) {
					csvZeilenFS1.add(csvLeseZeile(aktZeile, 1));
					csvZeilenFS2.add(csvLeseZeile(aktZeile, 2));
					csvZeilenFS3.add(csvLeseZeile(aktZeile, 3));
				}
			}
		} catch (Exception e) {
			LOGGER.error("Fehler beim einlesen der CSV Datei: "+e);
		}
	}
	
	/**
	 * Gibt eine HashMap mit den Soll-Werten eines Fahrstreifens zurück
	 * @param aktZeile Der Prüf-CSV Offset
	 * @param fsIndex Der zu verwendende Fahrstreifen
	 * @return Fahrstreifen-Soll-Daten <AttributPfad,Wert> 
	 * @throws Exception
	 */
	private HashMap<String,Integer> csvLeseZeile(String[] aktZeile, int fsIndex) throws Exception {
		int verschiebung = (fsIndex-1)*2;

		HashMap<String,Integer> hmCSV = new HashMap<String,Integer>();
		int csvPosition = 0+verschiebung;

		hmCSV.put("qKfz.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"qKfz"));

		csvPosition = 6+verschiebung;
		hmCSV.put("qPkw.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"qPkw"));
		
		csvPosition = 12+verschiebung;
		hmCSV.put("qLkw.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"qLkw"));
		
		csvPosition = 18+verschiebung;
		hmCSV.put("vKfz.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"vKfz"));
		
		csvPosition = 24+verschiebung;
		hmCSV.put("vPkw.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"vPkw"));
		
		csvPosition = 30+verschiebung;
		hmCSV.put("vLkw.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"vLkw"));
		
		csvPosition = 36+verschiebung;
		hmCSV.put("vgKfz.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"vgKfz"));
		
		csvPosition = 42+verschiebung;
		hmCSV.put("b.Wert", Integer.parseInt(aktZeile[csvPosition]));
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"b"));

		csvPosition = 48+verschiebung;
		csvWerttNetto = Long.parseLong(aktZeile[csvPosition]);
		hmCSV.putAll(csvLeseStatus(aktZeile[csvPosition+1],"tNetto"));
		
		switch(fsIndex) {
			case 1: {
				alCSVWerttNettoFS1.add(csvWerttNetto);
				break;
			}
			case 2: {
				alCSVWerttNettoFS2.add(csvWerttNetto);
				break;
			}
			case 3: {
				alCSVWerttNettoFS3.add(csvWerttNetto);
				break;
			}
		}

		return hmCSV;
	}
	
	
	
	/**
	 * Liest ein Statusfeld, extrahiert Daten in eine HashMap und gibt diese zurück
	 * @param status Die Statuszeile. Parameter durch Leerzeichen getrennt
	 * @param praefix Das Attribut des auszulesenden Status
	 * @return Statusdaten <Attributpfad,Wert>
	 */
	private HashMap<String,Integer> csvLeseStatus(String status, String praefix) {
		
		HashMap<String,Integer> hmCSVStatus = new HashMap<String,Integer>();
		
		String[] statusGeteilt = status.split(" ");

		Float guete = null;
		
		Integer errCode = 0;
		
		for(int i = 0; i<statusGeteilt.length;i++) {
		
			if(statusGeteilt[i].equalsIgnoreCase("Fehl"))
				errCode = errCode-2;
			
			if(statusGeteilt[i].equalsIgnoreCase("nErm"))
				errCode = errCode-1;
			
			if(statusGeteilt[i].equalsIgnoreCase("Impl"))
				hmCSVStatus.put(praefix+".Status.MessWertErsetzung.Implausibel", DUAKonstanten.JA);
			else if(!hmCSVStatus.containsKey(praefix+".Status.MessWertErsetzung.Implausibel"))
				hmCSVStatus.put(praefix+".Status.MessWertErsetzung.Implausibel", DUAKonstanten.NEIN);
			
			if(statusGeteilt[i].equalsIgnoreCase("Intp"))
				hmCSVStatus.put(praefix+".Status.MessWertErsetzung.Interpoliert", DUAKonstanten.JA);
			else if(!hmCSVStatus.containsKey(praefix+".Status.MessWertErsetzung.Interpoliert"))
				hmCSVStatus.put(praefix+".Status.MessWertErsetzung.Interpoliert", DUAKonstanten.NEIN);

			if(statusGeteilt[i].equalsIgnoreCase("nErf"))
				hmCSVStatus.put(praefix+".Status.Erfassung.NichtErfasst", DUAKonstanten.JA);
			else if(!hmCSVStatus.containsKey(praefix+".Status.Erfassung.NichtErfasst"))
				hmCSVStatus.put(praefix+".Status.Erfassung.NichtErfasst", DUAKonstanten.NEIN);

			if(statusGeteilt[i].equalsIgnoreCase("wMaL"))
				hmCSVStatus.put(praefix+".Status.PlLogisch.WertMaxLogisch", DUAKonstanten.JA);
			else if(!hmCSVStatus.containsKey(praefix+".Status.PlLogisch.WertMaxLogisch"))
				hmCSVStatus.put(praefix+".Status.PlLogisch.WertMaxLogisch", DUAKonstanten.NEIN);
			
			if(statusGeteilt[i].equalsIgnoreCase("wMax"))
				hmCSVStatus.put(praefix+".Status.PlFormal.WertMax", DUAKonstanten.JA);
			else if(!hmCSVStatus.containsKey(praefix+".Status.PlFormal.WertMax"))
				hmCSVStatus.put(praefix+".Status.PlFormal.WertMax", DUAKonstanten.NEIN);

			if(statusGeteilt[i].equalsIgnoreCase("wMiL"))
				hmCSVStatus.put(praefix+".Status.PlLogisch.WertMinLogisch", DUAKonstanten.JA);
			else if(!hmCSVStatus.containsKey(praefix+".Status.PlLogisch.WertMinLogisch"))
				hmCSVStatus.put(praefix+".Status.PlLogisch.WertMinLogisch", DUAKonstanten.NEIN);

			if(statusGeteilt[i].equalsIgnoreCase("wMin"))
				hmCSVStatus.put(praefix+".Status.PlFormal.WertMin", DUAKonstanten.JA);
			else if(!hmCSVStatus.containsKey(praefix+".Status.PlFormal.WertMin"))
				hmCSVStatus.put(praefix+".Status.PlFormal.WertMin", DUAKonstanten.NEIN);
			
			try {
				guete = Float.parseFloat(statusGeteilt[i].replace(",", "."))*100;
				hmCSVStatus.put(praefix+".Güte.Index",guete.intValue());
			} catch (Exception e) {
				//kein float wert
			}
		}
		
		if(errCode < 0) {
			if(!praefix.equals("tNetto")) {
				hmCSVStatus.put(praefix+".Wert", errCode);
			} else {
				csvWerttNetto = errCode.longValue();
			}
		}
			
		//hmCSVStatus.put(praefix+".Güte.Index",100);
		
		return hmCSVStatus;
	}

	/**
	 * Uebergebe CSV-Werte (tNetto)
	 */
	public long getCSVWerttNettoFS1(int csvOffset) {
		return alCSVWerttNettoFS1.get(csvOffset);
	}
	
	public long getCSVWerttNettoFS2(int csvOffset) {
		return alCSVWerttNettoFS2.get(csvOffset);
	}
	
	public long getCSVWerttNettoFS3(int csvOffset) {
		return alCSVWerttNettoFS3.get(csvOffset);
	}
	
	/** {@inheritDoc}
	 * 
	 * @param results Ergebnisdatensätze vom DaV
	 * @throws InvalidArgumentException 
	 */
	public void update(ResultData[] results) {
		for (ResultData result : results) {
			//Pruefe Ergebnisdatensatz auf Zeitstempel
			if ((result.getDataDescription().equals(DD_KZD_EMPF) ||
				result.getDataDescription().equals(DD_LZD_EMPF)) &&
				result.getData() != null &&
				result.getDataTime() == pruefZeitstempel) {

				try {
					//Ermittle FS und pruefe Daten
					if(result.getObject().getName().endsWith(".1")) {
						new VergleicheKZD(this,result,csvZeilenFS1,1,csvOffset);
						pruefungFS1Fertig = true;
					} else if(result.getObject().getName().endsWith(".2")) {
						new VergleicheKZD(this,result,csvZeilenFS2,2,csvOffset);
						pruefungFS2Fertig = true;
					} else if(result.getObject().getName().endsWith(".3")) {
						new VergleicheKZD(this,result,csvZeilenFS3,3,csvOffset);
						pruefungFS3Fertig = true;
					}
				} catch(Exception e) {}

				LOGGER.info("Warte auf Pruefung des Fahrstreifendatums");
				doWait();  //Warte auf pruefung eines FS
				this.pruefungFertig();  //pruefe ob alle 3 FS geprueft worden
			}
		}
	}
	
	/**
	 * Prueft ob alle 3 FS geprueft worden
	 */
	private void pruefungFertig() {
		if(pruefungFS1Fertig && pruefungFS2Fertig && pruefungFS3Fertig) {
			LOGGER.info("Pruefung der Fahrstreifen abgeschlossen. Benachrichtige Hauptthread...");
			doWait();  //Warte 250ms
			caller.doNotify();  //Benachrichtige aufrufende Klasse
		}
	}
	
	/**
	 * Warten (Default: 250ms)
	 */
	private void doWait() {
		synchronized(this) {
			try {
				this.wait(250);
			}catch(Exception e) {}
		}
	}
	
	/**
	 * Diesen Thread wecken
	 */
	public void doNotify() {
		synchronized(this) {
			this.notify();
		}
	}
	
}

/**
 * Vergleicht CSV Daten mit Ergebnisdaten
 */
class VergleicheKZD extends Thread {

	/**
	 * Logger
	 */
	protected Debug LOGGER = Debug.getLogger();
	private String pruefLog;
	
	/**
	 * Aufrufende Klasse
	 */
	private PruefeKZDLogisch caller;
	
	/**
	 * Übergebene Ergebnisdaten
	 */
	private Data daten;
		
	/**
	 * tNetto werte der Ergebnisdaten
	 */
	private long resultWerttNettoFS1;
	private long resultWerttNettoFS2;
	private long resultWerttNettoFS3;

	/**
	 * Attribut-Praefixe
	 */
	private String[] attributNamenPraefix = null;

	/**
	 * Attributnamen
	 */
	private String[] attributNamen = {".Wert",
									  ".Status.Erfassung.NichtErfasst",
									  ".Status.PlFormal.WertMax",
									  ".Status.PlFormal.WertMin",
									  ".Status.PlLogisch.WertMaxLogisch",
									  ".Status.PlLogisch.WertMinLogisch",
									  ".Status.MessWertErsetzung.Implausibel",
	  								  ".Status.MessWertErsetzung.Interpoliert",
	  								  ".Güte.Index"};

	/**
	 * Uebergebene CSV Zeilen des jeweiligen FS
	 */
	ArrayList<HashMap<String,Integer>> csvZeilen;
	
	/**
	 * Uebergebener Fahrstreifenindex (1-3)
	 */
	int fsIndex;
	
	/**
	 * Uebergebener CSV Index
	 */
	int csvOffset;
	
	/**
	 * Erstellt einen Prüfthread welcher Soll-Werte und Ergebniswerte vergleicht und
	 * das Ergebnis ausgibt
	 * @param caller Die aufrufende Klasse
	 * @param result Das zu prüfende Ergebnis
	 * @param csvZeilen die Soll-CSV-Werte (aller Fahrstreifen)
	 * @param fsIndex Der zu prüfende Fahrstreifen
	 * @param csvOffset Der zu verwendende CSV-Offset
	 */
	public VergleicheKZD(PruefeKZDLogisch caller, ResultData result, ArrayList<HashMap<String,Integer>> csvZeilen, int fsIndex, int csvOffset) {
		this.caller = caller;  //uebernehme aufrufende Klasse
		this.daten = result.getData();  //uebernehme Ergebnisdaten
		this.csvZeilen = csvZeilen;  //uebernehme CSV Daten
		this.fsIndex = fsIndex;  //uebernehme FS-Index
		this.csvOffset = csvOffset;  //uebernehme CSV Index

		setzeAttributNamen(result.getDataDescription().getAttributeGroup().getPid());
		
		this.start();  //starte Thread
	}

	/**
	 * Setze Attributnamen entsprechend der Attributgruppe
	 * @param atg Zu verwendende Attributgruppe
	 */
	private void setzeAttributNamen(String atg) {
		if(atg.equals(DUAKonstanten.ATG_KZD)) {
			attributNamenPraefix = new String[]{"qKfz",
												"qPkw",
												"qLkw",
												"vKfz",
												"vPkw",
												"vLkw",
												"vgKfz",
												"b",
				 								"tNetto"};
		} else if(atg.equals(DUAKonstanten.ATG_LZD)){
			attributNamenPraefix = new String[]{"qKfz",
												"qPkw",
												"qLkw",
												"vKfz",
												"vPkw",
												"vLkw"};
		}
	}
	
	/**
	 * Startet Thread
	 */
	public void run() {
		try {
			pruefeDaten(csvZeilen, fsIndex, csvOffset);  //Pruefe Daten
		} catch(Exception e){}
	}
	
	/**
	 * Lese Ergebnisdaten
	 */
	private HashMap<String,Integer> ergebnisLesen(int fsIndex) {
		HashMap<String,Integer> hmResult = new HashMap<String,Integer>();
		String attribut;
		int aktuellerWert;
		for(int i=0;i<attributNamenPraefix.length;i++) {
			for(int j=0;j<attributNamen.length;j++) {
				attribut = attributNamenPraefix[i]+attributNamen[j];
				if(!attribut.equals("tNetto.Wert")) {
					aktuellerWert = DUAUtensilien.getAttributDatum(attribut, daten).asUnscaledValue().intValue();
					hmResult.put(attribut, aktuellerWert);
				} else {
					switch(fsIndex) {
						case 1: {
							resultWerttNettoFS1 = DUAUtensilien.getAttributDatum("tNetto.Wert", daten).asUnscaledValue().longValue();
							break;
						}
						case 2: {
							resultWerttNettoFS2 = DUAUtensilien.getAttributDatum("tNetto.Wert", daten).asUnscaledValue().longValue();
							break;
						}
						case 3: {
							resultWerttNettoFS3 = DUAUtensilien.getAttributDatum("tNetto.Wert", daten).asUnscaledValue().longValue();
							break;
						}
					}
				}
			}
		}
		return hmResult;
	}
	
	/**
	 * prüfe Daten
	 */
	private void pruefeDaten(ArrayList<HashMap<String,Integer>> csvZeilen, int fsIndex, int csvOffset) throws Exception {
		String ident = "[FS:"+fsIndex+"-DS:"+csvOffset+"] ";  //FS + CSV Index
		LOGGER.info("Pruefe Fahrstreifendatum "+ident);
		
		HashMap<String,Integer> hmCSV = csvZeilen.get(csvOffset);
		HashMap<String,Integer> hmResult = ergebnisLesen(fsIndex);
		
		long csvWerttNetto = -10;
		long resultWerttNetto = -20;
		
		switch(fsIndex) {
			case 1: {
				csvWerttNetto = caller.getCSVWerttNettoFS1(csvOffset);
				resultWerttNetto = resultWerttNettoFS1;
				break;
			}
			case 2: {
				csvWerttNetto = caller.getCSVWerttNettoFS2(csvOffset);
				resultWerttNetto = resultWerttNettoFS2;
				break;
			}
			case 3: {
				csvWerttNetto = caller.getCSVWerttNettoFS3(csvOffset);
				resultWerttNetto = resultWerttNettoFS3;
				break;
			}
		}
		
		String attribut;
		String sollWertErl = "";
		String istWertErl = "";
		
		LOGGER.info(ident+"HashMap Groesse: CSV("+hmCSV.size()+") <> Results("+hmResult.size()+")");
		
		pruefLog = "";
		for(int i=0;i<attributNamenPraefix.length;i++) {
			for(int j=0;j<attributNamen.length;j++) {
				attribut = attributNamenPraefix[i]+attributNamen[j];
				
				if(!attribut.equals("tNetto.Wert")) {

					if(attribut.endsWith(".Wert")) {
						sollWertErl = wertErl(hmCSV.get(attribut));
						istWertErl = wertErl(hmResult.get(attribut));
					}
					
					if(!hmCSV.get(attribut).equals(hmResult.get(attribut))) {
						LOGGER.warning(ident+"DIF ("+attribut+"):"+ hmCSV.get(attribut)+ sollWertErl +" (SOLL)<>(IST) "+hmResult.get(attribut) + istWertErl);
						pruefLog += ident+"DIF ("+attribut+"):"+ hmCSV.get(attribut) + sollWertErl +" (SOLL)<>(IST) "+hmResult.get(attribut) + istWertErl +"\n\r";
					} else {
						pruefLog += ident+" OK ("+attribut+"):"+ hmCSV.get(attribut) + sollWertErl +" (SOLL)==(IST) "+hmResult.get(attribut) + istWertErl +"\n\r";
					}
				} else {
					
					sollWertErl = wertErl((int)csvWerttNetto);
					istWertErl = wertErl((int)resultWerttNetto);
					
					if(csvWerttNetto != resultWerttNetto) {
						LOGGER.error(ident+"DIF ("+attribut+"):"+ csvWerttNetto + sollWertErl +" (SOLL)<>(IST) "+resultWerttNetto + istWertErl);
						pruefLog += ident+"DIF ("+attribut+"):"+ csvWerttNetto + sollWertErl +" (SOLL)<>(IST) "+resultWerttNetto + istWertErl +"\n\r";
					} else {
						pruefLog += ident+" OK ("+attribut+"):"+ csvWerttNetto + sollWertErl +" (SOLL)<>(IST) "+resultWerttNetto + istWertErl +"\n\r";
					}
				}
			}
		}
		
		LOGGER.info(pruefLog);
		LOGGER.info("Benachrichtige Listener");
		caller.doNotify();  //Benachrichtige aufrufende Klasse
	}

	private String wertErl(long wert) {
		if(wert == -1)
			return "nicht ermittelbar";
		else if(wert == -2)
			return "fehlerhaft";
		else if(wert == -3)
			return "nicht ermittelbar + fehlerhaft";
		else
			return "";
	}
	
}
