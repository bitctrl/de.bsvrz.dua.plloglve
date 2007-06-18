package de.bsvrz.dua.plloglve.plloglve.typen;

import java.util.HashMap;
import java.util.Map;

import de.bsvrz.sys.funclib.bitctrl.daf.AbstractDavZustand;

/**
 * Repr�sentiert den DAV-Enumerationstypen
 * <code>att.optionenPlausibilit�tsPr�fungLogischVerkehr</code> 
 * 
 * @author Thierfelder
 *
 */
public class OptionenPlausibilitaetsPruefungLogischVerkehr 
extends AbstractDavZustand{

	/**
	 * Der Wertebereich dieses DAV-Enumerationstypen
	 */
	private static Map<Integer, OptionenPlausibilitaetsPruefungLogischVerkehr> WERTE_BEREICH = 
						new HashMap<Integer, OptionenPlausibilitaetsPruefungLogischVerkehr>();
	
	/**
	 * Wertebereichspr�fung wird NICHT durchgef�hrt. Wert wird nicht ver�ndert,
	 * es werden keine Statusflags gesetzt
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr KEINE_PRUEFUNG = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("Keine Pr�fung", 0); //$NON-NLS-1$
	
	/**
	 * Wertebereichspr�fung wird durchgef�hrt. Fehlerhafte Werte werden 
	 * nicht ver�ndert, es werden nur die Statusflags gesetzt
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr NUR_PRUEFUNG = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("NurPr�fung", 1); //$NON-NLS-1$
	
	/**
	 * Wertebereichspr�fung wird durchgef�hrt. Bei Bereichsunter- bzw.
	 * �berschreitung wird der Wert auf den parametrierten Min- bzw. /Max-Wert
	 * korrigiert und die Statusflags gesetzt 
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr SETZE_MIN_MAX = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("Setze MinMax", 2); //$NON-NLS-1$
	
	/**
	 * Wertebereichspr�fung wird durchgef�hrt. Bei Bereichsunterschreitung
	 * wird der Wert auf den parametrierten Min-Wert korrigiert und die
	 * Statusflags gesetzt, ansonsten Verhalten wie bei Option "NurPr�fen"
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr SETZE_MIN = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("Setze Min", 3); //$NON-NLS-1$

	/**
	 * Wertebereichspr�fung wird durchgef�hrt. Bei Bereichs�berschreitung wird
	 * der Wert auf den parametrierten Max-Wert korrigiert und die Statusflags
	 * gesetzt, ansonsten Verhalten wie bei Option "NurPr�fen"
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr SETZE_MAX = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("Setze Max", 4); //$NON-NLS-1$
		
	
	/**
	 * {@inheritDoc}
	 */
	private OptionenPlausibilitaetsPruefungLogischVerkehr(String name, int code){
		super(code, name);
		WERTE_BEREICH.put(code, this);
	}
	
	/**
	 * Erfragt den Wert dieses DAV-Enumerationstypen 
	 * mit dem �bergebenen Code
	 *
	 * @param der Code des Enumerations-Wertes
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr getZustand(int code){
		return WERTE_BEREICH.get(code);
	}
}

