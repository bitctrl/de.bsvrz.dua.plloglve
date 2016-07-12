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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IIntervallPufferElement;

import java.util.Date;

/**
 * Repraesentiert ein in einem temporaeren Puffer speicherbares Element mit der
 * Eigenschaft ausgefallen zu sein oder nicht.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public final class VertrauensbereichDatumKomplett implements
		IIntervallPufferElement<VertrauensbereichDatum> {

	/**
	 * Intervallanfang.
	 */
	private long intervallAnfang = 0;

	/**
	 * Intervallende.
	 */
	private long intervallEnde = 0;

	/**
	 * der Inhalt.
	 */
	private VertrauensbereichDatum inhalt = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param resultat
	 *            ein KZD-Datum
	 */
	private VertrauensbereichDatumKomplett(ResultData resultat) {
		Data data = resultat.getData();

		this.intervallAnfang = resultat.getDataTime();
		this.intervallEnde = resultat.getDataTime()
				+ data.getTimeValue("T").getMillis(); //$NON-NLS-1$
		this.inhalt = VertrauensbereichDatum.getAusfallDatumVon(resultat);
	}

	/**
	 * Gibt nur ein Datum zurueck, wenn es sich um ein Datum handelt, dass auch
	 * im Sinne der Plausibilisierung ausgewertet werden kann. Also ein Datum
	 * mit Nutzdaten.
	 * 
	 * @param resultat
	 *            ein Kz-Datum
	 * @return eine mit dem uebergebenen Datum korrespondierende Instanz dieser
	 *         Klasse oder <code>null</code> fuer keine Quelle usw.
	 */
	public static VertrauensbereichDatumKomplett getAusfallDatumVon(
			final ResultData resultat) {
		VertrauensbereichDatumKomplett datum = null;

		if (resultat != null && resultat.getData() != null) {
			datum = new VertrauensbereichDatumKomplett(resultat);
		}

		return datum;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = "Datenzeit: " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(this.intervallAnfang)) + //$NON-NLS-1$
				" (" + (this.intervallEnde - this.intervallAnfang)
				+ "ms): " + this.inhalt; //$NON-NLS-1$ //$NON-NLS-2$
		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getIntervallEnde() {
		return this.intervallEnde;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getIntervallStart() {
		return this.intervallAnfang;
	}

	/**
	 * {@inheritDoc}
	 */
	public VertrauensbereichDatum getInhalt() {
		return this.inhalt;
	}

}
