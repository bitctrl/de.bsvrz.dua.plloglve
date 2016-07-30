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

package de.bsvrz.dua.plloglve.plloglve;

/**
 * Abstrakter Container für Daten mit den Attributen Zeitstempel und
 * Intervalllänge. Die Objekte sind nach ihrer Datenzeit sortierbar.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @deprecated Klasse wird nicht verwendet
 */
@Deprecated
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

	public int compareTo(AbstraktDAVZeitEinzelDatum that) {
		return new Long(this.getDatenZeit()).compareTo(that.getDatenZeit());
	}
}
