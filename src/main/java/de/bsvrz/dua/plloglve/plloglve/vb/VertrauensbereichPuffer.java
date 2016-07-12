/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Pl-Prüfung logisch LVE
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

package de.bsvrz.dua.plloglve.plloglve.vb;

import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IntervallPuffer;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Repraesentiert einen zeitlichen Puffer, dessen einzelne Elemente sich
 * inhaltlich nur in der Eigenschaft <code>isAusgefallen</code> unterscheiden.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class VertrauensbereichPuffer extends IntervallPuffer<VertrauensbereichDatum> {

	/**
	 * Erfragt die gesamte Ausfallzeit aller im Puffer gespeicherten Daten.
	 * 
	 * @return die gesamte Ausfallzeit aller im Puffer gespeicherten Daten
	 */
	public final long getAusfallZeit() {
		long ausfall = 0;

		synchronized (this.puffer) {
			for (Intervall<VertrauensbereichDatum> intervall : this.puffer.values()) {
				if (intervall.getInhalt().isAusgefallen()) {
					ausfall += intervall.getIntervallEnde()
							- intervall.getIntervallStart();
				}
			}
		}

		return ausfall;
	}
	
	/** 
	 * Gibt alle ausgefallenen Attribute im Puffer zurück
	 * @return alle ausgefallenen Attribute im Puffer
	 */
	public final Set<String> getAusfallAttribute() {
		final Set<String> ausfall = new LinkedHashSet<String>();

		synchronized (this.puffer) {
			for (Intervall<VertrauensbereichDatum> intervall : this.puffer.values()) {
				if (intervall.getInhalt().isAusgefallen()) {
					ausfall.addAll(intervall.getInhalt().getAusgefalleneAttribute());
				}
			}
		}

		return ausfall;
	}

}
