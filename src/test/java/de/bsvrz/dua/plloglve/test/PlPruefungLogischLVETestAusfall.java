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

package de.bsvrz.dua.plloglve.test;

import org.junit.Before;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dua.plloglve.util.PlPruefungAusfall;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Automatisierter Test nach Pr�fspezifikation f�r SWE Pl-Pr�fung logisch LVE.
 *
 * ! Achtung: Die Applikation Pl-Pr�fung logisch LVE ist mit dem Parameter
 * test=ausfall ! zu starten.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id: PlPruefungLogischLVETestAusfall.java 53825 2015-03-18 09:36:42Z
 *          peuker $
 */
public class PlPruefungLogischLVETestAusfall {

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
	 * Ausfallh�ufigkeitstest nach Pr�fspezifikation.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@Test
	public void testAusfall() throws Exception {
		final PlPruefungAusfall pruefAusfall = new PlPruefungAusfall(dav,
				alLogger);
		pruefAusfall.benutzeAssert(true);
		pruefAusfall.setMeldungHysterese(5);
		pruefAusfall.pruefe();
	}
}