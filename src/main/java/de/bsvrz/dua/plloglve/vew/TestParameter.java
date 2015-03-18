/*
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.2 Pl-Pr�fung logisch LVE
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.plloglve.vew;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse mit allen Testflags.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class TestParameter {

	/**
	 * Erfassungsintervalldauer im Testbetrieb (bzw. Laenge einer Minute)
	 */
	public static final long INTERVALL_VB = 100L;

	/**
	 * einzige statische Instanz dieser Klasse.
	 */
	private static TestParameter instanz = null;

	/**
	 * Ist dieses Flag gesetzt, so laeuft die Applikation im Testbetrieb fuer
	 * das Modul Vertrauensbereich. Ein Tag ist dann f�r dieses Modul genau 144s
	 * lang!
	 */
	private boolean testVertrauen = false;

	/**
	 * Ist dieses Flag gesetzt, so laeuft die Applikation im Testbetrieb fuer
	 * das Modul Ausfallhaeufigkeit. Ein Tag ist dann f�r dieses Modul genau
	 * 144s lang!
	 */
	private boolean testAusfall = false;

	/**
	 * Dummy-Konstruktor.
	 */
	private TestParameter() {
		instanz = this;
	}

	/**
	 * Standardkonstruktor.
	 * 
	 * @param testParameter
	 *            der Kommandozeilenparameter <code>-test=</code>
	 */
	protected TestParameter(final String testParameter) {
		if (instanz == null) {
			if (testParameter.toLowerCase().equals("ausfall")) {
				testAusfall = true;
			} else if (testParameter.toLowerCase().equals("vertrauen")) {
				testVertrauen = true;
			}
			Debug.getLogger().info("!!! " + this + " !!!");
			instanz = this;
		} else {
			Debug.getLogger().warning(
					"Testparameter wurden bereits initialisiert mit:\n"
							+ instanz);
		}
	}

	/**
	 * Erfragt die statische Instanz dieser Klasse.
	 * 
	 * @return die statische Instanz dieser Klasse
	 */
	public static final TestParameter getInstanz() {
		if (instanz == null) {
			instanz = new TestParameter();
		}
		return instanz;
	}

	/**
	 * Erfragt, ob die Applikation im Testbetrieb fuer das Modul
	 * Vertrauensbereich laeuft. Ein Tag ist dann f�r dieses Modul genau 144s
	 * lang!
	 * 
	 * @return ob die Applikation im Testbetrieb fuer das Modul
	 *         Vertrauensbereich laeuft
	 */
	public final boolean isTestVertrauen() {
		return testVertrauen;
	}

	/**
	 * Erfragt, ob die Applikation im Testbetrieb fuer das Modul
	 * Ausfallhaeufigkeit laeuft. Ein Tag ist dann f�r dieses Modul genau 144s
	 * lang!
	 * 
	 * @return ob die Applikation im Testbetrieb fuer das Modul
	 *         Ausfallhaeufigkeit laeuft
	 */
	public final boolean isTestAusfall() {
		return testAusfall;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (testAusfall) {
			return "Test des Moduls Ausfallueberwachung";
		}
		if (testVertrauen) {
			return "Test des Moduls Vertrauensbereich";
		}
		return "Kein Test (Normalbetrieb)";
	}

}
