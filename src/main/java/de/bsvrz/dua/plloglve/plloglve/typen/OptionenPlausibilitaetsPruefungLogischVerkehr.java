/* 
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Pl-Prüfung logisch LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.plloglve.
 * 
 * de.bsvrz.dua.plloglve is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.plloglve is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.plloglve.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.plloglve.plloglve.typen;

import de.bsvrz.sys.funclib.bitctrl.daf.AbstractDavZustand;

import java.util.HashMap;
import java.util.Map;

/**
 * Repräsentiert den DAV-Enumerationstypen
 * <code>att.optionenPlausibilitätsPrüfungLogischVerkehr</code>.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public final class OptionenPlausibilitaetsPruefungLogischVerkehr extends
		AbstractDavZustand {

	/**
	 * Der Wertebereich dieses DAV-Enumerationstypen.
	 */
	private static Map<Integer, OptionenPlausibilitaetsPruefungLogischVerkehr> werteBereich = new HashMap<Integer, OptionenPlausibilitaetsPruefungLogischVerkehr>();

	/**
	 * Wertebereichsprüfung wird NICHT durchgeführt. Wert wird nicht verändert,
	 * es werden keine Statusflags gesetzt.
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr KEINE_PRUEFUNG = new OptionenPlausibilitaetsPruefungLogischVerkehr(
			"Keine Prüfung", 0); //$NON-NLS-1$

	/**
	 * Wertebereichsprüfung wird durchgeführt. Bei Messwertüberschreibung wird der Wert auf den maximalen Wert gesetzt und mit dem
	 * Flag MaxWertLogisch versehen.
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr WERT_REDUZIEREN = new OptionenPlausibilitaetsPruefungLogischVerkehr(
			"Wert reduzieren", 1); //$NON-NLS-1$

	/**
	 * Wertebereichsprüfung wird durchgeführt. Bei Messwertüberschreitung wird der wert als Implausibel und MaxWertLogisch
	 * gekennzeichnet und auf fehlerhaft gesetzt. Bei Geschwindigkeitswerten werden alle Geschwindigkeitswerte gleichzeitig
	 * betrachtet. Die Güte der Werte wird um 20% reduziert. Es wird eine Betriebsmeldugn erzeugt.
	 */
	public static final OptionenPlausibilitaetsPruefungLogischVerkehr AUF_FEHLERHAFT_SETZEN = new OptionenPlausibilitaetsPruefungLogischVerkehr(
			"Auf fehlerhaft setzen", 2); //$NON-NLS-1$

	/**
	 * Standardkonstruktor.
	 * 
	 * @param code
	 *            der Code
	 * @param name
	 *            die Bezeichnung
	 */
	private OptionenPlausibilitaetsPruefungLogischVerkehr(String name, int code) {
		super(code, name);
		werteBereich.put(code, this);
	}

	/**
	 * Erfragt den Wert dieses DAV-Enumerationstypen mit dem übergebenen Code.
	 * 
	 * @param code
	 *            der Kode des Zustands
	 * @return der Code des Enumerations-Wertes
	 */
	public static OptionenPlausibilitaetsPruefungLogischVerkehr getZustand(
			int code) {
		return werteBereich.get(code);
	}
}
