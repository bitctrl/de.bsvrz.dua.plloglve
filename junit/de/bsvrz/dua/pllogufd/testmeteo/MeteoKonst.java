/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
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

package de.bsvrz.dua.pllogufd.testmeteo;

import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;

/**
 * Standardwerte für meteorologische Pl-Prüfung
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MeteoKonst {

	
	public static final long NSGrenzLT = -5;
	
	public static final long NSGrenzTrockenRLF = 70;
	
	public static final double NSminNI = 0.1;

	public static final long NSGrenzRLF = 78;

	public static final double NIgrenzNassNI = 0.5;

	public static final long NIgrenzNassRLF = 78;

	public static final double NIminNI = 0.1;

	public static final long NIgrenzTrockenRLF = 70;

	public static final long NIminTrockenRLF = PlPruefungLogischUFDTest.STANDARD_T * 3;

	public static final double WFDgrenzNassNI = 0.5;

	public static final long WFDgrenzNassRLF = 78;

	public static final long WDFminNassRLF = PlPruefungLogischUFDTest.STANDARD_T * 3;

	public static final long SWgrenzTrockenRLF = 70;

	public static final long SWgrenzSW = 500;
	
}
