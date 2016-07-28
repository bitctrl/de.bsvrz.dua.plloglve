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

package de.bsvrz.dua.plloglve.plloglve.diff;

import java.util.Objects;

import de.bsvrz.dav.daf.main.Data;

/**
 * Repräsentiert aktuelle Daten der DAV-ATG
 * <code>atg.verkehrsDatenDifferenzialKontrolleFs</code>.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class AtgVerkehrsDatenDifferenzialKontrolleFs {

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für q.
	 */
	private final long maxAnzKonstanzVerkehrsmenge;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für v.
	 */
	private final long maxAnzKonstanzGeschwindigkeit;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für die
	 * Streung S.
	 */
	private final long maxAnzKonstanzStreung;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für die
	 * Belegung b.
	 */
	private final long maxAnzKonstanzBelegung;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param data
	 *            initialisierendes DAV-Datum
	 */
	public AtgVerkehrsDatenDifferenzialKontrolleFs(final Data data) {
		if (data == null) {
			throw new NullPointerException("Uebergebenes Datum ist <<null>>"); //$NON-NLS-1$
		}
		this.maxAnzKonstanzVerkehrsmenge = data.getUnscaledValue("maxAnzKonstanzVerkehrsmenge").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzGeschwindigkeit = data.getUnscaledValue("maxAnzKonstanzGeschwindigkeit").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzStreung = data.getUnscaledValue("maxAnzKonstanzStreung").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzBelegung = data.getUnscaledValue("maxAnzKonstanzBelegung").longValue(); //$NON-NLS-1$
	}

	/**
	 * Erfragt maxAnzKonstanzBelegung.
	 * 
	 * @return maxAnzKonstanzBelegung
	 */
	public final long getMaxAnzKonstanzBelegung() {
		return maxAnzKonstanzBelegung;
	}

	/**
	 * Erfragt maxAnzKonstanzVerkehrsmenge.
	 * 
	 * @return maxAnzKonstanzVerkehrsmenge
	 */
	public final long getMaxAnzKonstanzQ() {
		return maxAnzKonstanzVerkehrsmenge;
	}

	/**
	 * Erfragt maxAnzKonstanzStreung.
	 * 
	 * @return maxAnzKonstanzStreung
	 */
	public final long getMaxAnzKonstanzStreung() {
		return maxAnzKonstanzStreung;
	}

	/**
	 * Erfragt maxAnzKonstanzGeschwindigkeit.
	 * 
	 * @return maxAnzKonstanzGeschwindigkeit
	 */
	public final long getMaxAnzKonstanzV() {
		return maxAnzKonstanzGeschwindigkeit;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maxAnzKonstanzBelegung, maxAnzKonstanzGeschwindigkeit, maxAnzKonstanzStreung,
				maxAnzKonstanzVerkehrsmenge);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AtgVerkehrsDatenDifferenzialKontrolleFs other = (AtgVerkehrsDatenDifferenzialKontrolleFs) obj;
		if (maxAnzKonstanzBelegung != other.maxAnzKonstanzBelegung)
			return false;
		if (maxAnzKonstanzGeschwindigkeit != other.maxAnzKonstanzGeschwindigkeit)
			return false;
		if (maxAnzKonstanzStreung != other.maxAnzKonstanzStreung)
			return false;
		if (maxAnzKonstanzVerkehrsmenge != other.maxAnzKonstanzVerkehrsmenge)
			return false;
		return true;
	}
}
