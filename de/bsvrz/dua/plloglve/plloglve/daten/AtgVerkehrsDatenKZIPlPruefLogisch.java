package de.bsvrz.dua.plloglve.plloglve.daten;

import stauma.dav.clientside.Data;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;

/**
 * Repräsentiert die DAV-ATG
 * <code>atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch</code>
 * 
 * @author Thierfelder
 *
 */
public class AtgVerkehrsDatenKZIPlPruefLogisch 
extends AtgDatenObjekt{

	/**
	 * Grenzgeschwindigkeit für PL-Prüfung. Ist dieser Wert überschritten, 
	 * muss b kleiner bGrenz sein, sonst ist b inplausibel. 
	 */
	private int vKfzGrenz;
	
	/**
	 * Ist vKfz größer als vKfzGrenz, so muss b kleiner bGrenz sein, sonst ist b inplausibel. 
	 */
	private	int bGrenz;
	
	/**
	 * Legt das Verhalten für den Umgang mit geprüften Werten nach der Wertebereichsprüfung fest. 
	 */
	private OptionenPlausibilitaetsPruefungLogischVerkehr optionen;

	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	private int qKfzBereichMin;
	
	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	private int qKfzBereichMax;
	
	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	private int qLkwBereichMin;  
	
	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	private int qLkwBereichMax;  
	
	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	private int qPkwBereichMin;
	
	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als normierter Stundenwert.
	 */
	private int qPkwBereichMax;  
	
	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	private int vKfzBereichMin;  
	
	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	private int vKfzBereichMax;  
	
	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	private int vLkwBereichMin;  
	
	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	private int vLkwBereichMax;  
	
	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	private int vPkwBereichMin;  
	
	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	private int vPkwBereichMax;
	
	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	private int vgKfzBereichMin;
	
	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	private int vgKfzBereichMax;  
	
	/**
	 * Minimum des erlaubten Prozentwertes.
	 */
	private int belegungBereichMin;

	/**
	 * Maximum des erlaubten Prozentwertes.
	 */
	private int belegungBereichMax; 

	
	/**
	 * Standardkonstruktor
	 * 
	 * @param data initialisierendes DAV-Datum
	 */
	public AtgVerkehrsDatenKZIPlPruefLogisch(final Data data){
		if(data == null){
			throw new NullPointerException("Uebergebenes Datum ist <<null>>"); //$NON-NLS-1$
		}
		this.vKfzGrenz = data.getUnscaledValue("vKfzGrenz").intValue(); //$NON-NLS-1$
		this.bGrenz = data.getUnscaledValue("bGrenz").intValue(); //$NON-NLS-1$
		this.optionen = OptionenPlausibilitaetsPruefungLogischVerkehr.getZustand(data.getUnscaledValue("Optionen").intValue()); //$NON-NLS-1$
		this.qKfzBereichMax = data.getItem("qKfzBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qLkwBereichMin = data.getItem("qLkwBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qLkwBereichMax = data.getItem("qLkwBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qPkwBereichMin = data.getItem("qPkwBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qPkwBereichMax = data.getItem("qPkwBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vKfzBereichMin = data.getItem("vKfzBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vKfzBereichMax = data.getItem("vKfzBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vLkwBereichMin = data.getItem("vLkwBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vLkwBereichMax = data.getItem("vLkwBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vPkwBereichMin = data.getItem("vPkwBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vPkwBereichMax = data.getItem("vPkwBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vgKfzBereichMin = data.getItem("vgKfzBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vgKfzBereichMax = data.getItem("vgKfzBereich").getUnscaledValue("Max").intValue(); //$NON-NLS-1$//$NON-NLS-2$
		this.belegungBereichMin = data.getItem("BelegungBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.belegungBereichMax = data.getItem("BelegungBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
	}


	/**
	 * Erfragt BelegungBereichMax
	 * 
	 * @return belegungBereichMax
	 */
	public final int getBelegungBereichMax() {
		return belegungBereichMax;
	}


	/**
	 * Erfragt BelegungBereichMin
	 * 
	 * @return belegungBereichMin
	 */
	public final int getBelegungBereichMin() {
		return belegungBereichMin;
	}


	/**
	 * Erfragt bGrenz
	 * 
	 * @return bGrenz
	 */
	public final int getBGrenz() {
		return bGrenz;
	}


	/**
	 * Erfragt die Optionen
	 * 
	 * @return optionen
	 */
	public final OptionenPlausibilitaetsPruefungLogischVerkehr getOptionen() {
		return optionen;
	}


	/**
	 * Erfragt qKfzBereichMax
	 * 
	 * @return qKfzBereichMax
	 */
	public final int getQKfzBereichMax() {
		return qKfzBereichMax;
	}


	/**
	 * Erfragt qKfzBereichMin
	 * 
	 * @return qKfzBereichMin
	 */
	public final int getQKfzBereichMin() {
		return qKfzBereichMin;
	}


	/**
	 * Erfragt qLkwBereichMax
	 * 
	 * @return qLkwBereichMax
	 */
	public final int getQLkwBereichMax() {
		return qLkwBereichMax;
	}


	/**
	 * Erfragt qLkwBereichMin
	 * 
	 * @return qLkwBereichMin
	 */
	public final int getQLkwBereichMin() {
		return qLkwBereichMin;
	}


	/**
	 * Erfragt qPkwBereichMax
	 * 
	 * @return qPkwBereichMax
	 */
	public final int getQPkwBereichMax() {
		return qPkwBereichMax;
	}


	/**
	 * Erfragt qPkwBereichMin
	 * 
	 * @return qPkwBereichMin
	 */
	public final int getQPkwBereichMin() {
		return qPkwBereichMin;
	}


	/**
	 * Erfragt vgKfzBereichMax
	 * 
	 * @return vgKfzBereichMax
	 */
	public final int getVgKfzBereichMax() {
		return vgKfzBereichMax;
	}


	/**
	 * Erfragt vgKfzBereichMin
	 * 
	 * @return vgKfzBereichMin
	 */
	public final int getVgKfzBereichMin() {
		return vgKfzBereichMin;
	}


	/**
	 * Erfragt vKfzBereichMax
	 * 
	 * @return vKfzBereichMax
	 */
	public final int getVKfzBereichMax() {
		return vKfzBereichMax;
	}


	/**
	 * Erfragt vKfzBereichMin
	 * 
	 * @return vKfzBereichMin
	 */
	public final int getVKfzBereichMin() {
		return vKfzBereichMin;
	}


	/**
	 * Erfragt vKfzGrenz
	 * 
	 * @return vKfzGrenz
	 */
	public final int getVKfzGrenz() {
		return vKfzGrenz;
	}


	/**
	 * Erfragt vLkwBereichMax
	 * 
	 * @return vLkwBereichMax
	 */
	public final int getVLkwBereichMax() {
		return vLkwBereichMax;
	}


	/**
	 * Erfragt vLkwBereichMin
	 * 
	 * @return vLkwBereichMin
	 */
	public final int getVLkwBereichMin() {
		return vLkwBereichMin;
	}


	/**
	 * Erfragt vPkwBereichMax
	 * 
	 * @return vPkwBereichMax
	 */
	public final int getVPkwBereichMax() {
		return vPkwBereichMax;
	}


	/**
	 * Erfragt vPkwBereichMin
	 * 
	 * @return vPkwBereichMin
	 */
	public final int getVPkwBereichMin() {
		return vPkwBereichMin;
	}
	
}
