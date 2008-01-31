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
 * Klasse mit allen Testflags
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class TestParameter {
	
	/**
	 * Ist dieses Flag gesetzt, so läuft die Applikation im Testbetrieb für das Modul
	 * Vertrauensbereich. Ein Tag ist dann für dieses Modul genau 144s lang!
	 */
	public static final boolean TEST_VERTRAUEN = true;
	
	/**
	 * Ist dieses Flag gesetzt, so läuft die Applikation im Testbetrieb für das Modul
	 * Ausfallhäufigkeit. Ein Tag ist dann für dieses Modul genau 144s lang!
	 */
	public static final boolean TEST_AUSFALL = false;
	
}
