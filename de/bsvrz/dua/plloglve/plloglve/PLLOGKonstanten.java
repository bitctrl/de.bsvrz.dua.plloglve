package de.bsvrz.dua.plloglve.plloglve;


/**
 * Konstanten, die in Zusammenhang mit dem Modul
 * Pl-Pr�fung logisch LVE ben�tigt werden
 * 
 * @author Thierfelder
 *
 */
public class PLLOGKonstanten {
	
	/**
	 * Daten sind nicht ermittelbar (ist KEIN Fehler). Wird gesetzt, wenn
	 * der entsprechende Wert nicht ermittelbar ist und kein Interpolation
	 * sinnvoll m�glich ist (z.B. ist die Geschwindigkeit nicht ermittelbar,
	 * wenn kein Fahrzeug erfasst wurde)
	 */
	public static final int NICHT_ERMITTELBAR = -1;
	
	/**
	 * Daten sind fehlerhaft. 
	 * Wird gesetzt, wenn die Daten als fehlerhaft erkannt wurden.
	 */
	public static final int FEHLERHAFT = -2;
	
	/**
	 * Daten nicht ermittelbar, da bereits Basiswerte fehlerhaft. 
	 * Wird gesetzt, wenn Daten, die zur Berechnung dieses Werts notwendig sind,
	 * bereits als fehlerhaft gekennzeichnet sind, oder wenn die Berechnung aus
	 * anderen Gr�nden (z.B. Nenner = 0 in der Berechnungsformel) nicht m�glich war.
	 */
	public static final int NICHT_ERMITTELBAR_BZW_FEHLERHAFT = -3;
	
	/**
	 * Atg <code>atg.verkehrsDatenKurzZeitIntervall</code> 
	 */
	public static final String ATG_KZD = "atg.verkehrsDatenKurzZeitIntervall"; //$NON-NLS-1$
	
	/**
	 * Atg <code>atgverkehrsDatenLangZeitIntervall</code>
	 */
	public static final String ATG_LZD = "atg.verkehrsDatenLangZeitIntervall"; //$NON-NLS-1$

}
