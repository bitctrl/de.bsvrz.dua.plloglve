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

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IntervallPufferElementAdapter;

/**
 * Speichert fuer ein finales DAV-Attribut des Datensatzes
 * <code>atg.verkehrsDatenKurzZeitIntervall</code>, ob dieses (im Sinne der
 * Vertrauensbereichspruefung) ausgefallen ist oder nicht. Ein Datum gilt als
 * ausgefallen, wenn es als <code>fehlerhaft</code> bzw.
 * <code>implausibel</code> gekennzeichnet ist.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class VertrauensEinzelDatum extends
		IntervallPufferElementAdapter<VertrauensDatum> {

	/**
	 * Standardkonstruktor.
	 * 
	 * @param name
	 *            der Name dieses Datums
	 * @param originalDatum das empfangene (unveraenderte) Originaldatum
	 */
	protected VertrauensEinzelDatum(final String name,
			final ResultData originalDatum) {
		super(originalDatum.getDataTime(), originalDatum.getDataTime()
				+ originalDatum.getData().getTimeValue("T").getMillis()); //$NON-NLS-1$

		/**
		 * ein Datum gilt als ausgefallen, wenn es als fehlerhaft bzw.
		 * implausibel gekennzeichnet ist
		 */
		this.inhalt = new VertrauensDatum(
				originalDatum.getData().getItem(name)
						.getUnscaledValue("Wert").longValue() == DUAKonstanten.FEHLERHAFT || //$NON-NLS-1$
						originalDatum
								.getData()
								.getItem(name)
								.getItem("Status").getItem("MessWertErsetzung").
								getUnscaledValue("Implausibel").longValue() == DUAKonstanten.JA); //$NON-NLS-1$
	}

}
