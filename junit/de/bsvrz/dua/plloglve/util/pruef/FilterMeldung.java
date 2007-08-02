package de.bsvrz.dua.plloglve.util.pruef;

import de.bsvrz.dua.plloglve.util.PlPruefungInterface;
import stauma.dav.configuration.interfaces.SystemObject;
import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.ClientReceiverInterface;
import sys.funclib.debug.Debug;

/*
 * KZD Listener
 * Liest Ergebnis-CSV-Datei
 * Wartet auf gesendete und gepruefte Daten und gibt diese an Vergleicher-Klasse weiter
 */
public class FilterMeldung
implements ClientReceiverInterface {

	/**
	 * Logger
	 */
	protected Debug LOGGER = Debug.getLogger();
	
	/**
	 * Aufrufende Klasse
	 */
	PlPruefungInterface caller;
	
	/**
	 * Datenverteilerverbindung von der aufrufenden Klasse
	 */
	private ClientDavInterface dav;
	
	/**
	 * Meldungssystemobjekt
	 */
	SystemObject MELD;
	
	/**
	 * Gibt an, ob Ergebnisdatensätze auf Fehlerfreiheit geprüft werden
	 */
	private String filter = null;
	
	/**
	 * Zählt die Anzahl der gefilterten Meldungen
	 */
	private int meldAnzahl = 0;
	
	/**
	 * Erforderliche Anzahl an gefilterten Meldungen
	 */
	private int erfAnz;
	
	/**
	 * Empfange-Datenbeschreibung für KZD und LZD
	 */
	public static DataDescription DD_MELD_EMPF = null;
	
	/**
	 * Filtert Betriebsmeldungen nach einem bestimmten Kriterium 
	 * @param caller Die aufrufende Klasse
	 * @param dav Datenverteilerverbindung
	 * @param fs Das zu prüfende Fahrstreifenobjekt
	 * @throws Exception
	 */
	public FilterMeldung(PlPruefungInterface caller, ClientDavInterface dav, String filter, int erfAnz)
	throws Exception {
		this.dav = dav;
		this.filter = filter;
		this.erfAnz = erfAnz;
		this.caller = caller;
		
		//MELD = this.dav.getDataModel().getObject("kv.aoe.bitctrl.tester");
		MELD = this.dav.getDataModel().getObject("kv.bitctrl.thierfelder");
		
		LOGGER.info("Filtere Betriebsmeldungen nach \""+filter+"\" - Erwarte "+erfAnz+" gefilterte Meldungen");
		
		/*
		 * Melde Empfänger für Betriebsmeldungen an
		 */
		DD_MELD_EMPF = new DataDescription(this.dav.getDataModel().getAttributeGroup("atg.betriebsMeldung"),
			      this.dav.getDataModel().getAspect("asp.information"),
			      (short)0);
		
		this.dav.subscribeReceiver(this, MELD, 
				DD_MELD_EMPF, ReceiveOptions.normal(), ReceiverRole.receiver());
	}
	
	/** {@inheritDoc}
	 * 
	 * @param results Ergebnisdatensätze vom DaV
	 * @throws InvalidArgumentException 
	 */
	public void update(ResultData[] results) {
		for (ResultData result : results) {
			if (result.getDataDescription().equals(DD_MELD_EMPF) &&
				result.getData() != null &&
				result.toString().contains(filter)) {
				String meldung = result.getData().getItem("MeldungsText").asTextValue().toString();
				meldAnzahl++;
				LOGGER.info(meldAnzahl+". Meldung empfangen\n\r"+meldung);
			}
		}
		if(meldAnzahl == erfAnz) {
			LOGGER.info("Erforderliche Anzahl an Meldungen erhalten");
			caller.doNotify();
		} else if (meldAnzahl > erfAnz) {
			LOGGER.warning("Mehr Meldungen gefiltert als erwartet");
		}
	}
}