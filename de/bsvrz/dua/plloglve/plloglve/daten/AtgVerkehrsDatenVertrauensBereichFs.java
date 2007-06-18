package de.bsvrz.dua.plloglve.plloglve.daten;

import stauma.dav.clientside.Data;

/**
 * Repräsentiert die DAV-ATG
 * <code>atg.verkehrsDatenVertrauensBereichFs</code>
 * 
 * @author Thierfelder
 *
 */
public class AtgVerkehrsDatenVertrauensBereichFs 
extends AtgDatenObjekt{

	/**
	 * Bezugszeitraum für die Vertrauensbereichsüberprüfung.
	 */
	private int bezugsZeitraum;
	
	/**
	 * Einschaltschwelle für den Vertrauensbereich eines Fahrstreifens im
	 * Bezugszeitraum. Bei Überschreiten dieses Wertes wird eine entsprechende Fehlermeldung generiert.
	 */
	private int maxAusfallProBezugsZeitraumEin;
	
	/**
	 * Ausschaltschwelle für den Vertrauensbereich eines Fahrstreifens im Bezugszeitraum.
	 * Bei Unterschreiten dieses Wertes wird eine entsprechende Fehlermeldung zurückgenommen
	 */
	private int maxAusfallProBezugsZeitraumAus;  

	
	/**
	 * Standardkonstruktor
	 * 
	 * @param data initialisierendes DAV-Datum
	 */
	public AtgVerkehrsDatenVertrauensBereichFs(final Data data){
		if(data == null){
			throw new NullPointerException("Uebergebenes Datum ist <<null>>"); //$NON-NLS-1$
		}
		this.bezugsZeitraum = data.getUnscaledValue("BezugsZeitraum").intValue(); //$NON-NLS-1$
		this.maxAusfallProBezugsZeitraumEin = data.getUnscaledValue("maxAusfallProBezugsZeitraumEin").intValue(); //$NON-NLS-1$
		this.maxAusfallProBezugsZeitraumAus = data.getUnscaledValue("maxAusfallProBezugsZeitraumAus").intValue(); //$NON-NLS-1$
	}


	/**
	 * Erfragt BezugsZeitraum
	 * 
	 * @return bezugsZeitraum
	 */
	public final int getBezugsZeitraum() {
		return bezugsZeitraum;
	}


	/**
	 * Erfragt maxAusfallProBezugsZeitraumAus
	 * 
	 * @return maxAusfallProBezugsZeitraumAus
	 */
	public final int getMaxAusfallProBezugsZeitraumAus() {
		return maxAusfallProBezugsZeitraumAus;
	}


	/**
	 * Erfragt maxAusfallProBezugsZeitraumEin
	 * 
	 * @return maxAusfallProBezugsZeitraumEin
	 */
	public final int getMaxAusfallProBezugsZeitraumEin() {
		return maxAusfallProBezugsZeitraumEin;
	}
	
}
