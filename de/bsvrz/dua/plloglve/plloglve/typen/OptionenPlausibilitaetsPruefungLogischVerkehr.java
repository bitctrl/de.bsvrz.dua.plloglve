package de.bsvrz.dua.plloglve.plloglve.typen;

import java.util.HashMap;
import java.util.Map;

import de.bsvrz.sys.funclib.bitctrl.daf.AbstractDavZustand;

/**
 * Repräsentiert den DAV-Enumerationstypen
 * <code>att.optionenPlausibilitätsPrüfungLogischVerkehr</code> 
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
	 * Wertebereichsprüfung wird NICHT durchgeführt. Wert wird nicht verändert,
	 * es werden keine Statusflags gesetzt
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr KEINE_PRUEFUNG = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("Keine Prüfung", 0); //$NON-NLS-1$
	
	/**
	 * Wertebereichsprüfung wird durchgeführt. Fehlerhafte Werte werden 
	 * nicht verändert, es werden nur die Statusflags gesetzt
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr NUR_PRUEFUNG = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("NurPrüfung", 1); //$NON-NLS-1$
	
	/**
	 * Wertebereichsprüfung wird durchgeführt. Bei Bereichsunter- bzw.
	 * überschreitung wird der Wert auf den parametrierten Min- bzw. /Max-Wert
	 * korrigiert und die Statusflags gesetzt 
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr SETZE_MIN_MAX = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("Setze MinMax", 2); //$NON-NLS-1$
	
	/**
	 * Wertebereichsprüfung wird durchgeführt. Bei Bereichsunterschreitung
	 * wird der Wert auf den parametrierten Min-Wert korrigiert und die
	 * Statusflags gesetzt, ansonsten Verhalten wie bei Option "NurPrüfen"
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr SETZE_MIN = 
		new OptionenPlausibilitaetsPruefungLogischVerkehr("Setze Min", 3); //$NON-NLS-1$

	/**
	 * Wertebereichsprüfung wird durchgeführt. Bei Bereichsüberschreitung wird
	 * der Wert auf den parametrierten Max-Wert korrigiert und die Statusflags
	 * gesetzt, ansonsten Verhalten wie bei Option "NurPrüfen"
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
	 * mit dem übergebenen Code
	 *
	 * @param der Code des Enumerations-Wertes
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr getZustand(int code){
		return WERTE_BEREICH.get(code);
	}
}

