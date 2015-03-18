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

package de.bsvrz.dua.plloglve.plloglve;


/**
 * Abstrakter Container für Daten mit den Attributen Zeitstempel und
 * Intervalllänge. Die Objekte sind nach ihrer Datenzeit sortierbar.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id: AbstraktDAVZeitEinzelDatum.java 53825 2015-03-18 09:36:42Z
 *          peuker $
 */
public class AbstraktDAVZeitEinzelDatum implements
Comparable<AbstraktDAVZeitEinzelDatum> {

	/**
	 * die Datenzeit des Datums.
	 */
	protected long datenZeit = -1;

	/**
	 * die Intervalllänge des Datums.
	 */
	protected long intervallLaenge = -1;

	/**
	 * Erfragt die Intervalllänge des Datums.
	 *
	 * @return die Intervalllänge des Datums
	 */
	public final long getIntervallLaenge() {
		return this.intervallLaenge;
	}

	/**
	 * Erfragt die Datenzeit des Datums.
	 *
	 * @return die Datenzeit des Datums
	 */
	public long getDatenZeit() {
		return this.datenZeit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final AbstraktDAVZeitEinzelDatum that) {
		return new Long(this.getDatenZeit()).compareTo(that.getDatenZeit());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (datenZeit ^ (datenZeit >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		boolean resultat = false;

		if (obj instanceof AbstraktDAVZeitEinzelDatum) {
			final AbstraktDAVZeitEinzelDatum that = (AbstraktDAVZeitEinzelDatum) obj;
			resultat = this.getDatenZeit() == that.getDatenZeit();
		}

		return resultat;
	}
}
