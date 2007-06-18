package de.bsvrz.dua.plloglve.plloglve.daten;

import stauma.dav.clientside.Data;

/**
 * Repräsentiert die DAV-ATG
 * <code>atg.VerkehrsDatenDifferenzialKontrolleFs</code>
 * 
 * @author Thierfelder
 *
 */
public class AtgVerkehrsDatenDifferenzialKontrolleFs
extends AtgDatenObjekt{

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für qKfz.
	 */
	private int maxAnzKonstanzqKfz;
	
	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für qLkw.
	 */
	private int maxAnzKonstanzqLkw;
	
	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für qPkw.
	 */
	private int maxAnzKonstanzqPkw;
	
	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für vKfz.
	 */
	private int maxAnzKonstanzvKfz;
	
	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für vLkw.
	 */
	private int maxAnzKonstanzvLkw;
	
	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für vPkw.
	 */
	private int maxAnzKonstanzvPkw;
	
	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für die Streung S.
	 */
	private int maxAnzKonstanzStreung;
	
	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für die Belegung b.
	 */
	private int maxAnzKonstanzBelegung;  


	/**
	 * Standardkonstruktor
	 * 
	 * @param data initialisierendes DAV-Datum
	 */
	public AtgVerkehrsDatenDifferenzialKontrolleFs(final Data data){
		if(data == null){
			throw new NullPointerException("Uebergebenes Datum ist <<null>>"); //$NON-NLS-1$
		}
		this.maxAnzKonstanzqKfz = data.getUnscaledValue("maxAnzKonstanzqKfz").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzqLkw = data.getUnscaledValue("maxAnzKonstanzqLkw").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzqPkw = data.getUnscaledValue("maxAnzKonstanzqPkw").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvKfz = data.getUnscaledValue("maxAnzKonstanzvKfz").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvLkw = data.getUnscaledValue("maxAnzKonstanzvLkw").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvPkw = data.getUnscaledValue("maxAnzKonstanzvPkw").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzStreung = data.getUnscaledValue("maxAnzKonstanzStreung").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzBelegung = data.getUnscaledValue("maxAnzKonstanzBelegung").intValue(); //$NON-NLS-1$
	}


	/**
	 * Erfragt maxAnzKonstanzBelegung
	 * 
	 * @return maxAnzKonstanzBelegung
	 */
	public final int getMaxAnzKonstanzBelegung() {
		return maxAnzKonstanzBelegung;
	}


	/**
	 * Erfragt maxAnzKonstanzqKfz
	 * 
	 * @return maxAnzKonstanzqKfz
	 */
	public final int getMaxAnzKonstanzqKfz() {
		return maxAnzKonstanzqKfz;
	}


	/**
	 * Erfragt maxAnzKonstanzqLkw
	 * 
	 * @return maxAnzKonstanzqLkw
	 */
	public final int getMaxAnzKonstanzqLkw() {
		return maxAnzKonstanzqLkw;
	}


	/**
	 * Erfragt maxAnzKonstanzqPkw
	 * 
	 * @return maxAnzKonstanzqPkw
	 */
	public final int getMaxAnzKonstanzqPkw() {
		return maxAnzKonstanzqPkw;
	}


	/**
	 * Erfragt maxAnzKonstanzStreung
	 * 
	 * @return maxAnzKonstanzStreung
	 */
	public final int getMaxAnzKonstanzStreung() {
		return maxAnzKonstanzStreung;
	}


	/**
	 * Erfragt maxAnzKonstanzvKfz
	 * 
	 * @return maxAnzKonstanzvKfz
	 */
	public final int getMaxAnzKonstanzvKfz() {
		return maxAnzKonstanzvKfz;
	}


	/**
	 * Erfragt maxAnzKonstanzvLkw
	 * 
	 * @return maxAnzKonstanzvLkw
	 */
	public final int getMaxAnzKonstanzvLkw() {
		return maxAnzKonstanzvLkw;
	}


	/**
	 * Erfragt maxAnzKonstanzvPkw
	 * 
	 * @return maxAnzKonstanzvPkw
	 */
	public final int getMaxAnzKonstanzvPkw() {
		return maxAnzKonstanzvPkw;
	}

}
