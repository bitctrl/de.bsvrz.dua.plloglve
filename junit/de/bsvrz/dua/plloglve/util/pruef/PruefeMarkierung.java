package de.bsvrz.dua.plloglve.util.pruef;


import junit.framework.Assert;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.InvalidArgumentException;
import de.bsvrz.dua.plloglve.util.PlPruefungInterface;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.debug.Debug;

/*
 * KZD Listener
 * Liest Ergebnis-CSV-Datei
 * Wartet auf gesendete und gepruefte Daten und gibt diese an Vergleicher-Klasse weiter
 */
public class PruefeMarkierung
implements ClientReceiverInterface {
	
	/**
	 * Ob <code>Assert...</code> benutzt werden soll oder blos Warnungen ausgegeben werden sollen
	 */
	private boolean useAssert = true;

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
	 * "alle" = prüfe alle Attribute
	 */
	private boolean pruefeAlleAttr = false;
	private String pruefeAttr;
	
	/**
	 * Gibt an, auf was für Markierungen getestet werden soll
	 */
	private long sollWert;
	private int sollImplausibel;
	private static int SOLL_WERT_KEINE_PRUEFUNG = 0;
	private static int SOLL_WERT_KEIN_FEHLER = 1;
	private static int SOLL_IMPLAUSIBEL_KEINE_PRUEFUNG = -1;
	
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
	 * Konfiguriert dieses Objekt auf die Prüfung von fehlerfreien Daten eines Attribut
	 * @param pruefeAttr Zu prüfende Attribut
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenOK(String pruefeAttr, long pruefZeitstempel) {
		pruefeAlleAttr = false;
		paramOK(pruefeAttr, pruefZeitstempel);
		LOGGER.info("Prüfe Datum auf fehlerfreies Attribut: "+pruefeAttr); //$NON-NLS-1$
	}
	
	/**
	 * Konfiguriert dieses Objekt auf die Prüfung von fehlerfreien Daten aller Attribute
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenOK(long pruefZeitstempel) {
		pruefeAlleAttr = true;
		paramOK("alle", pruefZeitstempel); //$NON-NLS-1$
		LOGGER.info("Prüfe alle Attribute des Datums auf fehlerfreiheit"); //$NON-NLS-1$
	}
	
	/**
	 * Abschließende Konfiguration des Objektes auf die Prüfung von fehlerfreien Daten 
	 * @param pruefeAttr Zu prüfende(s) Attribut(e)
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	private void paramOK(String pruefeAttr, long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = SOLL_WERT_KEIN_FEHLER;
		this.sollImplausibel = DUAKonstanten.NEIN;
	}
	
	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Fehlerhaft-Markierung eines Attributes
	 * @param pruefeAttr Zu prüfendes Attribut
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenFehl(String pruefeAttr, long pruefZeitstempel) {
		pruefeAlleAttr = false;
		paramFehl(pruefeAttr, pruefZeitstempel);
		LOGGER.info("Prüfe Datum auf Fehlerhaft-Markierung des Attributes: "+pruefeAttr); //$NON-NLS-1$
	}
	
	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Fehlerhaft-Markierung aller Attribute
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenFehl(long pruefZeitstempel) {
		pruefeAlleAttr = true;
		paramFehl("alle", pruefZeitstempel); //$NON-NLS-1$
		LOGGER.info("Prüfe Datum auf Fehlerhaft-Markierung aller Attribute"); //$NON-NLS-1$
	}
	
	/**
	 * Abschließende Konfiguration des Objektes für die Prüfung auf Fehlerhaft-Markierung
	 * @param pruefeAttr Zu prüfende(s) Attribut(e)
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	private void paramFehl(String pruefeAttr, long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = DUAKonstanten.FEHLERHAFT;
		this.sollImplausibel = SOLL_IMPLAUSIBEL_KEINE_PRUEFUNG;	
	}
	
	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Implausibel-Markierung eines Attributes
	 * @param pruefeAttr Zu prüfendes Attribut
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenImpl(String pruefeAttr, long pruefZeitstempel) {
		pruefeAlleAttr = false;
		paramImpl(pruefeAttr, pruefZeitstempel);
		LOGGER.info("Prüfe Datum auf Implausibel-Markierung des Attributes: "+pruefeAttr); //$NON-NLS-1$
	}
	
	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Implausibel-Markierung aller Attribute
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenImpl(long pruefZeitstempel) {
		pruefeAlleAttr = true;
		paramImpl("alle", pruefZeitstempel); //$NON-NLS-1$
		LOGGER.info("Prüfe Datum auf Implausibel-Markierung aller Attribute"); //$NON-NLS-1$
	}
	
	/**
	 * Abschließende Konfiguration des Objektes für die Prüfung auf Implausibel-Markierung
	 * @param pruefeAttr Zu prüfende(s) Attribut(e)
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	private void paramImpl(String pruefeAttr, long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = SOLL_WERT_KEINE_PRUEFUNG;
		this.sollImplausibel = DUAKonstanten.JA;
	}
	
	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Fehlerhaft- und Implausibel-Markierung eines Attributes
	 * @param pruefeAttr Zu prüfendes Attribut
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenFehlImpl(String pruefeAttr, long pruefZeitstempel) {
		pruefeAlleAttr = false;
		paramFehlImpl(pruefeAttr, pruefZeitstempel);
		LOGGER.info("Prüfe Datum auf Fehlerhaft- und Implausibel-Markierung des Attributes: "+pruefeAttr); //$NON-NLS-1$
	}
	
	/**
	 * Konfiguriert dieses Objekt für die Prüfung auf Fehlerhaft- und Implausibel-Markierung aller Attribute
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	public void listenFehlImpl(long pruefZeitstempel) {
		pruefeAlleAttr = true;
		paramFehlImpl("alle", pruefZeitstempel); //$NON-NLS-1$
		LOGGER.info("Prüfe Datum auf Fehlerhaft- und Implausibel-Markierung aller Attribute"); //$NON-NLS-1$
	}
	
	/**
	 * Abschließende Konfiguration des Objektes für die Prüfung auf Fehlerhaft- und Implausibel-Markierung
	 * @param pruefeAttr Zu prüfende(s) Attribut(e)
	 * @param pruefZeitstempel Der zu prüfende Zeitstempel
	 */
	private void paramFehlImpl(String pruefeAttr, long pruefZeitstempel) {
		this.pruefZeitstempel = pruefZeitstempel;
		this.pruefeAttr = pruefeAttr;
		this.sollWert = DUAKonstanten.FEHLERHAFT;
		this.sollImplausibel = DUAKonstanten.JA;	
	}
	
	/**
	 * Prüft Daten entsprechend der Konfiguration 
	 * @param data Ergebnisdatensatz
	 */
	private void pruefeDS(Data data) {
		if(pruefeAlleAttr) {
			pruefeAttr("qKfz", data); //$NON-NLS-1$
			pruefeAttr("qLkw", data); //$NON-NLS-1$
			pruefeAttr("qPkw", data); //$NON-NLS-1$
			pruefeAttr("vPkw", data); //$NON-NLS-1$
			pruefeAttr("vLkw", data); //$NON-NLS-1$
			pruefeAttr("vKfz", data); //$NON-NLS-1$
			pruefeAttr("b", data); //$NON-NLS-1$
			pruefeAttr("sKfz", data); //$NON-NLS-1$
		} else {
			pruefeAttr(pruefeAttr, data);
		}

	}
	
	/**
	 * Prueft Attribut entsprechend der Konfiguration
	 * @param pruefeAttr Das zu prüfende Attribut
	 * @param data Ergebnisdatensatz
	 */
	private void pruefeAttr(String pruefeAttr, Data data) {
		final long wert = data.getItem(pruefeAttr).getUnscaledValue("Wert").longValue(); //$NON-NLS-1$
		final int impl = data.getItem(pruefeAttr).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		if(sollWert < 0) {
			if(wert != sollWert) {
				String fehler = "Fehlerhafter Attributwert (" + pruefeAttr + "): " + sollWert + " (SOLL)<>(IST) " + wert; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if(useAssert){
					Assert.assertTrue(fehler, false);
				}else{
					LOGGER.warning(fehler);
				}
			} else {
				LOGGER.info("Attributwert OK (" + pruefeAttr+"): " + sollWert + " (SOLL)==(IST) " + wert);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			}
		} else if (sollWert == SOLL_WERT_KEIN_FEHLER) {
			if(wert < 0) {
				String fehler = "Fehlerhafter Attributwert ("+pruefeAttr+"): Wert >= 0 (SOLL)<>(IST) "+wert; //$NON-NLS-1$ //$NON-NLS-2$ 
				if(useAssert){
					Assert.assertTrue(fehler, false);
				}else{
					LOGGER.warning(fehler);
				}
			} else {
				LOGGER.info("Attributwert OK ("+pruefeAttr+"): Wert >= 0 (SOLL)==(IST) "+wert);  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		
		if(sollImplausibel != SOLL_IMPLAUSIBEL_KEINE_PRUEFUNG) {
			if(sollImplausibel != impl) {
				String fehler = "Fehlerhafte Implausibel-Markierung ("+pruefeAttr+"): "+sollImplausibel+" (SOLL)<>(IST) "+impl+ ",\n" + data; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
				if(useAssert){
					Assert.assertTrue(fehler, false);
				}else{
					LOGGER.warning(fehler);
				}
			} else {
				LOGGER.info("Implausibel-Markierung OK ("+pruefeAttr+"): "+sollImplausibel+" (SOLL)==(IST) "+impl);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			}
		}
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
				pruefeDS(result.getData());
				caller.doNotify();
			}
		}
	}
	
	/**
	 * Soll Assert zur Fehlermeldung genutzt werden?
	 * @param useAssert
	 */
	public void benutzeAssert(final boolean useAssert) {
		this.useAssert = useAssert;
	}
}