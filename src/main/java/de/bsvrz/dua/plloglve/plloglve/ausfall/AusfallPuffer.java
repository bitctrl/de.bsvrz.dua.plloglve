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

package de.bsvrz.dua.plloglve.plloglve.ausfall;

import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IntervallPuffer;

/**
 * Repraesentiert einen zeitlichen Puffer, dessen einzelne Elemente sich
 * inhaltlich nur in der Eigenschaft <code>isAusgefallen</code> unterscheiden.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class AusfallPuffer extends IntervallPuffer<AusfallDatum> {

	/**
	 * Erfragt die gesamte Ausfallzeit aller im Puffer gespeicherten Daten.
	 *
	 * @return die gesamte Ausfallzeit aller im Puffer gespeicherten Daten
	 */
	public final long getAusfallZeit() {
		long ausfall = 0;

		synchronized (this.puffer) {
			for (final Intervall<AusfallDatum> intervall : this.puffer.values()) {
				if (intervall.getInhalt().isAusgefallen()) {
					ausfall += intervall.getIntervallEnde()
							- intervall.getIntervallStart();
				}
			}
		}

		return ausfall;
	}

}
