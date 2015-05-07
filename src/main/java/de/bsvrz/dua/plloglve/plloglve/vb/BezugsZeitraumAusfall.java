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

import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;

/**
 * Speichert das Ergebnis einer Überprüfung des Vertrauensbereichs für ein
 * bestimmtes Attribut.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class BezugsZeitraumAusfall implements Comparable<BezugsZeitraumAusfall> {

	/**
	 * Ausschaltgrenze Grenze.
	 */
	private long grenze = -1;

	/**
	 * der prozentuale Ausfall.
	 */
	private double ausfallInProzent = -1;

	/**
	 * Ausfall in Minuten.
	 */
	private long ausfallInMinuten = -1;

	/**
	 * Ausfall in Stunden.
	 */
	private long ausfallInStunden = -1;

	/**
	 * Standardkonstruktor.
	 *
	 * @param grenze
	 *            Ausschaltgrenze
	 * @param ausfallInProzent
	 *            der prozentuale Ausfall des Attributs im Bezugszeitraum
	 * @param ausfallInStunden
	 *            Ausfall in Stunden
	 * @param ausfallInMinuten
	 *            Ausfall in Minuten
	 */
	public BezugsZeitraumAusfall(final long grenze,
			final double ausfallInProzent, final long ausfallInStunden,
			final long ausfallInMinuten) {
		this.grenze = grenze;
		this.ausfallInProzent = ausfallInProzent;
		this.ausfallInStunden = ausfallInStunden;
		this.ausfallInMinuten = ausfallInMinuten;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean result = false;

		if (obj instanceof BezugsZeitraumAusfall) {
			final BezugsZeitraumAusfall that = (BezugsZeitraumAusfall) obj;
			result = this.ausfallInProzent == that.ausfallInProzent;
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final BezugsZeitraumAusfall that) {
		return new Double(this.ausfallInProzent)
		.compareTo(that.ausfallInProzent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return DUAUtensilien.runde(this.ausfallInProzent, 1)
				+ "% (< " + this.grenze + "%) entspricht Ausfall von " + //$NON-NLS-1$//$NON-NLS-2$
				this.ausfallInStunden
				+ " Stunde(n) " + this.ausfallInMinuten + " Minute(n)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
