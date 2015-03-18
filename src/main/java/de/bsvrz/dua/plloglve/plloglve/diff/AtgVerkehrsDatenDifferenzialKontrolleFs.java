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

package de.bsvrz.dua.plloglve.plloglve.diff;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.sys.funclib.bitctrl.dua.AllgemeinerDatenContainer;

/**
 * Repräsentiert aktuelle Daten der DAV-ATG
 * <code>atg.verkehrsDatenDifferenzialKontrolleFs</code>.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class AtgVerkehrsDatenDifferenzialKontrolleFs extends
		AllgemeinerDatenContainer {

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für qKfz.
	 */
	private long maxAnzKonstanzqKfz;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für qLkw.
	 */
	private long maxAnzKonstanzqLkw;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für qPkw.
	 */
	private long maxAnzKonstanzqPkw;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für vKfz.
	 */
	private long maxAnzKonstanzvKfz;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für vLkw.
	 */
	private long maxAnzKonstanzvLkw;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für vPkw.
	 */
	private long maxAnzKonstanzvPkw;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für die
	 * Streung S.
	 */
	private long maxAnzKonstanzStreung;

	/**
	 * Maximal zulässige Anzahl von Intervallen mit Ergebniskonstanz für die
	 * Belegung b.
	 */
	private long maxAnzKonstanzBelegung;

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
		this.maxAnzKonstanzqKfz = data
				.getUnscaledValue("maxAnzKonstanzqKfz").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzqLkw = data
				.getUnscaledValue("maxAnzKonstanzqLkw").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzqPkw = data
				.getUnscaledValue("maxAnzKonstanzqPkw").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvKfz = data
				.getUnscaledValue("maxAnzKonstanzvKfz").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvLkw = data
				.getUnscaledValue("maxAnzKonstanzvLkw").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvPkw = data
				.getUnscaledValue("maxAnzKonstanzvPkw").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzStreung = data.getUnscaledValue(
				"maxAnzKonstanzStreung").longValue(); //$NON-NLS-1$
		this.maxAnzKonstanzBelegung = data.getUnscaledValue(
				"maxAnzKonstanzBelegung").longValue(); //$NON-NLS-1$
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
	 * Erfragt maxAnzKonstanzqKfz.
	 * 
	 * @return maxAnzKonstanzqKfz
	 */
	public final long getMaxAnzKonstanzqKfz() {
		return maxAnzKonstanzqKfz;
	}

	/**
	 * Erfragt maxAnzKonstanzqLkw.
	 * 
	 * @return maxAnzKonstanzqLkw
	 */
	public final long getMaxAnzKonstanzqLkw() {
		return maxAnzKonstanzqLkw;
	}

	/**
	 * Erfragt maxAnzKonstanzqPkw.
	 * 
	 * @return maxAnzKonstanzqPkw
	 */
	public final long getMaxAnzKonstanzqPkw() {
		return maxAnzKonstanzqPkw;
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
	 * Erfragt maxAnzKonstanzvKfz.
	 * 
	 * @return maxAnzKonstanzvKfz
	 */
	public final long getMaxAnzKonstanzvKfz() {
		return maxAnzKonstanzvKfz;
	}

	/**
	 * Erfragt maxAnzKonstanzvLkw.
	 * 
	 * @return maxAnzKonstanzvLkw
	 */
	public final long getMaxAnzKonstanzvLkw() {
		return maxAnzKonstanzvLkw;
	}

	/**
	 * Erfragt maxAnzKonstanzvPkw.
	 * 
	 * @return maxAnzKonstanzvPkw
	 */
	public final long getMaxAnzKonstanzvPkw() {
		return maxAnzKonstanzvPkw;
	}

}
