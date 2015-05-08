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

package de.bsvrz.dua.plloglve.plloglve.vb;

import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IIntervallDatum;

/**
 * Ein Ausfalldatum eines Verkehrsattributs.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class VertrauensDatum implements IIntervallDatum<VertrauensDatum> {

	/**
	 * Ob dieses Datum als ausgefallen im Sinne der Vertrauensbereichskontrolle
	 * interpretiert wird.
	 */
	private boolean ausgefallen = false;

	/**
	 * Standardkonstruktor.
	 *
	 * @param ausgefallen
	 *            ob dieses Datum als ausgefallen im Sinne der
	 *            Vertrauensbereichskontrolle interpretiert wird
	 */
	public VertrauensDatum(final boolean ausgefallen) {
		this.ausgefallen = ausgefallen;
	}

	/**
	 * Erfragt, ob dieses Datum als ausgefallen im Sinne der
	 * Vertrauensbereichskontrolle interpretiert wird.
	 *
	 * @return ob dieses Datum als ausgefallen im Sinne der
	 *         Vertrauensbereichskontrolle interpretiert wird
	 */
	public final boolean isAusgefallen() {
		return this.ausgefallen;
	}

	@Override
	public boolean istGleich(final VertrauensDatum that) {
		return this.isAusgefallen() == that.isAusgefallen();
	}

}
