/** 
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */
 
package de.bsvrz.dua.plloglve.plloglve;


/**
 * Konstanten, die in Zusammenhang mit dem Modul
 * Pl-Prüfung logisch LVE benötigt werden
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class PLLOGKonstanten {
		
	/**
	 * Daten sind nicht ermittelbar (ist KEIN Fehler). Wird gesetzt, wenn
	 * der entsprechende Wert nicht ermittelbar ist und kein Interpolation
	 * sinnvoll möglich ist (z.B. ist die Geschwindigkeit nicht ermittelbar,
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
	 * anderen Gründen (z.B. Nenner = 0 in der Berechnungsformel) nicht möglich war.
	 */
	public static final int NICHT_ERMITTELBAR_BZW_FEHLERHAFT = -3;

	/**
	 * 24h in Millisekunden
	 */
	public static final long EIN_TAG_IN_MS = 24l * 60l * 60l * 1000l;
	
}
