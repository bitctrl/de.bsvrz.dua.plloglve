package de.bsvrz.dua.plloglve.util.pruef;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.util.PlPruefungInterface;
import de.bsvrz.sys.funclib.bitctrl.dua.bm.BmClient;
import de.bsvrz.sys.funclib.bitctrl.dua.bm.IBmListener;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * KZD Listener
 * Liest Ergebnis-CSV-Datei
 * Wartet auf gesendete und gepruefte Daten und gibt diese an Vergleicher-Klasse weiter
 */
public class FilterMeldung
implements IBmListener {

	/**
	 * Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	
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
	 * Erlaubte Abweichung der Anzahl an gefilterten Meldungen
	 */
	private int anzHyst;
	
	/**
	 * Empfange-Datenbeschreibung für KZD und LZD
	 */
	public static DataDescription DD_MELD_EMPF = null;
	
	/**
	 * Gibt an, ob die geforderte Anzahl inklusive Hysterese eingehalten wurde
	 */
	private boolean anzahlEingehalten = false;
	
	
	/**
	 * Standardkonstruktor
	 * @param caller
	 * @param dav
	 * @param filter
	 * @param erfAnz
	 * @throws Exception
	 */
	public FilterMeldung(PlPruefungInterface caller, ClientDavInterface dav, String filter, int erfAnz, int anzHyst)
	throws Exception {
		this.dav = dav;
		this.filter = filter;
		this.erfAnz = erfAnz;
		this.anzHyst = anzHyst;
		this.caller = caller;
		
		LOGGER.info("Filtere Betriebsmeldungen nach \""+filter+"\" - Erwarte "+erfAnz+" gefilterte Meldungen"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		/*
		 * Melde Empfänger für Betriebsmeldungen an
		 */
		DD_MELD_EMPF = new DataDescription(this.dav.getDataModel().getAttributeGroup("atg.betriebsMeldung"), //$NON-NLS-1$
			      this.dav.getDataModel().getAspect("asp.information"), //$NON-NLS-1$
			      (short)0);
		
		BmClient.getInstanz(dav).addListener(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereBetriebsMeldungen(SystemObject obj, long zeit,
			String text) {
		if (text.contains(filter)) {
			meldAnzahl++;
			LOGGER.info(meldAnzahl + ". Meldung empfangen\n\r" + text); //$NON-NLS-1$
			
			if(meldAnzahl == erfAnz) {
				LOGGER.info("Erforderliche Anzahl an Meldungen erhalten"); //$NON-NLS-1$
				anzahlEingehalten = true;
				caller.doNotify();
			} else if (meldAnzahl > (erfAnz+anzHyst)) {
				anzahlEingehalten = false;
				LOGGER.warning("Mehr Meldungen gefiltert als erwartet"); //$NON-NLS-1$	
			}			
		}
	}
	
	/**
	 * Wurde die geforderte Anzahl inklusive Hysterese eingehalten 
	 * @return <code>True</code>, wenn die Anzahl inklusive Hysterese eingehalten
	 * wurde, sonst <code>False</code>
	 */
	public boolean wurdeAnzahlEingehalten() {
		return anzahlEingehalten;
	}
	
	/**
	 * Liefert die Anzahl erhaltener Meldungen
	 * @return Anzahl erhaltener Meldungen
	 */
	public int getAnzahlErhaltenerMeldungen() {
		return meldAnzahl;
	}
	
	/**
	 * Liefert die erwartete Anzahl an Meldungen
	 * @return Erwartete Anzahl an Meldungen
	 */
	public int getErwarteteAnzahlMeldungen() {
		return erfAnz;
	}
}