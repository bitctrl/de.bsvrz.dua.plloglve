package de.bsvrz.dua.plloglve.pruef;

import stauma.dav.configuration.interfaces.SystemObject;
import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import stauma.dav.clientside.ClientReceiverInterface;
import de.bsvrz.dua.plloglve.util.PlPruefungInterface;
import sys.funclib.debug.Debug;

/*
 * KZD Listener
 * Liest Ergebnis-CSV-Datei
 * Wartet auf gesendete und gepruefte Daten und gibt diese an Vergleicher-Klasse weiter
 */
public class PruefeMarkierung
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
	private PlPruefungInterface caller;
	
	/**
	 * Zeitstempel der zu pruefenden Daten
	 */
	private long pruefZeitstempel;
	
	/**
	 * Gibt an, ob Ergebnisdatensätze auf Fehlerfreiheit geprüft werden
	 */
	private boolean pruefeOK = false;
	
	/**
	 * Empfange-Datenbeschreibung für KZD und LZD
	 */
	public static DataDescription DD_KZD_EMPF = null;
	public static DataDescription DD_LZD_EMPF = null;
	
	/**
	 * Prüft einen Ergebnisdatensatz mit entsprechendem Zeitstempel auf Fehlerfreiheit
	 * bzw. Fehlerhaftigkeit 
	 * @param caller Die aufrufende Klasse
	 * @param dav Datenverteilerverbindung
	 * @param fs Das zu prüfende Fahrstreifenobjekt
	 * @throws Exception
	 */
	public PruefeMarkierung(PlPruefungInterface caller, ClientDavInterface dav, SystemObject fs)
	throws Exception {
		this.caller = caller;  //aufrufende Klasse uebernehmen
		this.dav = dav;
		
		/*
		 * Melde Empfänger für KZD und LZD unter dem Aspekt PlPrüfung Logisch an
		 */
		DD_KZD_EMPF = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
			      this.dav.getDataModel().getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
			      (short)0);
		
		DD_LZD_EMPF = new DataDescription(this.dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
			      this.dav.getDataModel().getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH),
			      (short)0);
		
		this.dav.subscribeReceiver(this, fs, 
				DD_KZD_EMPF, ReceiveOptions.normal(), ReceiverRole.receiver());
		
		this.dav.subscribeReceiver(this, fs, 
				DD_LZD_EMPF, ReceiveOptions.delayed(), ReceiverRole.receiver());
	}
	
	/**
	 * Konfiguriert dieses Objekt auf die Prüfung von fehlerfreien Daten
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenOK(long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeOK = true;
		LOGGER.info("Prüfe auf fehlerfreien DS...");
	}
	
	/**
	 * Konfiguriert dieses Objekt auf die Prüfung von fehlerhafte Daten
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenFehlerhaft(long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeOK = false;
		LOGGER.info("Prüfe auf fehlerhaften DS...");
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

				if(pruefeOK) {
					if(!result.toString().contains("fehlerhaft")) {
						LOGGER.info("OK: fehlerfrei (SOLL) == (IST) fehlerfrei");
					} else {
						LOGGER.warning("Fehlerfreien DS erwartet aber fehlerhaften DS erhalten:\n\r"+result.toString());
					}
				} else {
					if(result.toString().contains("fehlerhaft")) {
						LOGGER.info("OK: fehlerhaft (SOLL) == (IST) fehlerhaft");
					} else {
						LOGGER.warning("Fehlerhaften DS erwartet aber fehlerfreien DS erhalten:\n\r"+result.toString());
					}
				}
				caller.doNotify();
			}
		}
	}
}