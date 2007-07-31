package de.bsvrz.dua.plloglve.pruef;

import stauma.dav.configuration.interfaces.SystemObject;
import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.Data;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
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
	 * Gibt an, welches Attribut getestet werden soll
	 * "alle" = pr�fe alle Attribute
	 */
	private String pruefeAttr;
	
	/**
	 * Gibt an, auf was f�r Markierungen getestet werden soll
	 */
	private long sollWert;
	private int sollImplausibel;
	
	/**
	 * Empfange-Datenbeschreibung f�r KZD und LZD
	 */
	public static DataDescription DD_KZD_EMPF = null;
	public static DataDescription DD_LZD_EMPF = null;
	
	/**
	 * Pr�ft einen Ergebnisdatensatz mit entsprechendem Zeitstempel auf Fehlerfreiheit
	 * bzw. Fehlerhaftigkeit 
	 * @param caller Die aufrufende Klasse
	 * @param dav Datenverteilerverbindung
	 * @param fs Das zu pr�fende Fahrstreifenobjekt
	 * @throws Exception
	 */
	public PruefeMarkierung(PlPruefungInterface caller, ClientDavInterface dav, SystemObject fs)
	throws Exception {
		this.caller = caller;  //aufrufende Klasse uebernehmen
		this.dav = dav;
		
		/*
		 * Melde Empf�nger f�r KZD und LZD unter dem Aspekt PlPr�fung Logisch an
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
	 * Konfiguriert dieses Objekt auf die Pr�fung von fehlerfreien Daten
	 * @param pruefeAttr Zu pr�fende Attribut(e)
	 * @param pruefZeitstempel Der zu pr�fende Zeitstempel
	 */
	public void listenOK(String pruefeAttr, long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = 0;
		this.sollImplausibel = DUAKonstanten.NEIN;
		LOGGER.info("Pr�fe DS auf fehlerfreien Attribut: "+pruefeAttr);
	}
	
	public void listenOK(long pruefZeitstempel) {
		listenOK("alle", pruefZeitstempel);
	}
	
	/**
	 * Konfiguriert dieses Objekt auf die Pr�fung von fehlerhaften Daten
	 * @param pruefeAttr Zu pr�fende Attribut(e)
	 * @param pruefZeitstempel Der zu pr�fende Zeitstempel
	 */
	public void listenFehl(String pruefeAttr, long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = DUAKonstanten.FEHLERHAFT;
		this.sollImplausibel = DUAKonstanten.NEIN;
		LOGGER.info("Pr�fe DS auf fehlerhaftes Attribut: "+pruefeAttr);
	}
	
	public void listenFehl(long pruefZeitstempel) {
		listenFehl("alle", pruefZeitstempel);
	}
	
	/**
	 * Konfiguriert dieses Objekt auf die Pr�fung von implausiblen Daten
	 * @param pruefeAttr Zu pr�fende Attribut(e)
	 * @param pruefZeitstempel Der zu pr�fende Zeitstempel
	 */
	public void listenImpl(String pruefeAttr, long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = 0;
		this.sollImplausibel = DUAKonstanten.JA;
		LOGGER.info("Pr�fe DS auf implausibles Attribut: "+pruefeAttr);
	}
	
	public void listenImpl(long pruefZeitstempel) {
		listenImpl("alle", pruefZeitstempel);
	}
	
	/**
	 * Konfiguriert dieses Objekt auf die Pr�fung von fehlerhaften und implausiblen Daten
	 * @param pruefeAttr Zu pr�fende Attribut(e)
	 * @param pruefZeitstempel Der zu pr�fende Zeitstempel
	 */
	public void listenFehlImpl(String pruefeAttr, long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = DUAKonstanten.FEHLERHAFT;
		this.sollImplausibel = DUAKonstanten.JA;
		LOGGER.info("Pr�fe DS auf fehlerhaftes und implausibles Attribut: "+pruefeAttr);
	}
	
	public void listenFehlImpl(long pruefZeitstempel) {
		listenFehlImpl("alle", pruefZeitstempel);
	}
	
	private void pruefeDS(Data data) {
		if(pruefeAttr.equals("alle")) {
			final long qKfz = data.getItem("qKfz").getUnscaledValue("Wert").longValue();
			final int qKfzImpl = data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			final long qLkw = data.getItem("qLkw").getUnscaledValue("Wert").longValue();
			final int qLkwImpl = data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			final long qPkw = data.getItem("qPkw").getUnscaledValue("Wert").longValue();
			final int qPkwImpl = data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			final long vPkw = data.getItem("vPkw").getUnscaledValue("Wert").longValue();
			final int vPkwImpl = data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			final long vLkw = data.getItem("vLkw").getUnscaledValue("Wert").longValue();
			final int vLkwImpl = data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			final long vKfz = data.getItem("vKfz").getUnscaledValue("Wert").longValue();
			final int vKfzImpl = data.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			final long b = data.getItem("b").getUnscaledValue("Wert").longValue();
			final int bImpl = data.getItem("b").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			final long sKfz = data.getItem("sKfz").getUnscaledValue("Wert").longValue();
			final int sKfzImpl = data.getItem("sKfz").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			
			pruefeAttr(qKfz, qKfzImpl);
			pruefeAttr(qLkw, qLkwImpl);
			pruefeAttr(qPkw, qPkwImpl);
			pruefeAttr(vPkw, vPkwImpl);
			pruefeAttr(vLkw, vLkwImpl);
			pruefeAttr(vKfz, vKfzImpl);
			pruefeAttr(b, bImpl);
			pruefeAttr(sKfz, sKfzImpl);
		} else {
			final long pruefWert = data.getItem(pruefeAttr).getUnscaledValue("Wert").longValue();
			final int pruefImpl = data.getItem(pruefeAttr).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			pruefeAttr(pruefWert, pruefImpl);
		}

	}
	
	private void pruefeAttr(long wert, int impl) {
		if(sollWert < 0) {
			if(wert != sollWert) {
				LOGGER.warning("Fehlerhafter Attributwert ("+pruefeAttr+"): "+sollWert+" (SOLL)<>(IST) "+wert);
			} else {
				LOGGER.info("Attributwert OK ("+pruefeAttr+"): "+sollWert+" (SOLL)==(IST) "+wert);
			}
		} else {
			if(wert < 0) {
				LOGGER.warning("Fehlerhafter Attributwert ("+pruefeAttr+"): Wert >= 0 (SOLL)<>(IST) "+wert);
			} else {
				LOGGER.info("Attributwert OK ("+pruefeAttr+"): Wert >= 0 (SOLL)==(IST) "+wert);
			}
		}
		
		if(sollImplausibel != impl) {
			LOGGER.warning("Fehlerhafte Implausibel-Markierung ("+pruefeAttr+"): "+sollImplausibel+" (SOLL)<>(IST) "+impl);
		} else {
			LOGGER.info("Implausibel-Markierung OK ("+pruefeAttr+"): "+sollImplausibel+" (SOLL)==(IST) "+impl);
		}
	}
	
	/** {@inheritDoc}
	 * 
	 * @param results Ergebnisdatens�tze vom DaV
	 * @throws InvalidArgumentException 
	 */
	public void update(ResultData[] results) {
		for (ResultData result : results) {
			//Pruefe Ergebnisdatensatz auf Zeitstempel
			if ((result.getDataDescription().equals(DD_KZD_EMPF) ||
				result.getDataDescription().equals(DD_LZD_EMPF)) &&
				result.getData() != null &&
				result.getDataTime() == pruefZeitstempel) {
				pruefeDS(result.getData());
				caller.doNotify();
			}
		}
	}
}