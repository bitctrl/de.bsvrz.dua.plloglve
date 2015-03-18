/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
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
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.plloglve.test;

import org.junit.Before;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dua.plloglve.util.PlPruefungDiff;
import de.bsvrz.dua.plloglve.util.PlPruefungLogisch;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Automatisierter Test nach Prüfspezifikation für SWE Pl-Prüfung logisch LVE.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id: PlPruefungLogischLVETestStandardDiff.java 53825 2015-03-18
 *          09:36:42Z peuker $
 */
public class PlPruefungLogischLVETestStandardDiff {

	/**
	 * Datenverteiler-Verbindung.
	 */
	private ClientDavInterface dav = null;

	/**
	 * Loggerargument.
	 *
	 * Pfadangabe mit Argument: -debugFilePath=[Pfad]
	 */
	private final String[] argumente = new String[] { "-debugLevelFileText=ALL" };

	/**
	 * der Logger.
	 */
	private final ArgumentList alLogger = new ArgumentList(argumente);

	/**
	 * Vorbereitungen (DAV-Anmeldung).
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav(Konfiguration.getConData());
	}

	/**
	 * KZD TLS Test nach Prüfspezifikation.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Test
	public void testKZDTLS() throws Exception {
		final PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,
				alLogger);
		pruefLogisch.benutzeAssert(true);
		final int[][] bereiche = { { 2, 101 } };
		System.out.println("\n\n*** KZD TLS Test nach Prüfspezifikation ***\n");
		pruefLogisch.pruefeKZDTLS(bereiche);
	}

	/**
	 * KZD Grenz Test nach Prüfspezifikation.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Test
	public void testKZDGrenz() throws Exception {
		final PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,
				alLogger);
		pruefLogisch.benutzeAssert(true);
		final int[][] bereiche = { { 2, 21 } };
		System.out
				.println("\n\n*** KZD Grenz Test nach Prüfspezifikation ***\n");
		pruefLogisch.pruefeKZDGrenz(bereiche);
	}

	/**
	 * LZD Grenz Test nach Prüfspezifikation.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Test
	public void testLZDTLS() throws Exception {
		final PlPruefungLogisch pruefLogisch = new PlPruefungLogisch(dav,
				alLogger);
		pruefLogisch.benutzeAssert(true);
		final int[][] bereiche = { { 2, 21 } };
		System.out
				.println("\n\n*** LZD Grenz Test nach Prüfspezifikation ***\n");
		pruefLogisch.pruefeLZDGrenz(bereiche);
	}

	/**
	 * Differentialkontrolle nach Prüfspezifikation.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Test
	public void testDiff() throws Exception {
		final PlPruefungDiff plPruefDiff = new PlPruefungDiff(dav, alLogger);
		plPruefDiff.benutzeAssert(true);
		plPruefDiff.setMeldungHysterese(5);
		System.out
				.println("\n\n*** Differentialkontrolle nach Prüfspezifikation ***\n");
		plPruefDiff.pruefe();
	}
}
