/** 
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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

import java.util.Date;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IIntervallPufferElement;

/**
 * Repraesentiert ein in einem temporaeren Puffer speicherbares Element mit der
 * Eigenschaft ausgefallen zu sein oder nicht.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public final class AusfallDatumKomplett implements
		IIntervallPufferElement<AusfallDatum> {

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
	private AusfallDatum inhalt = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param resultat
	 *            ein KZD-Datum
	 */
	private AusfallDatumKomplett(ResultData resultat) {
		Data data = resultat.getData();

		this.intervallAnfang = resultat.getDataTime();
		this.intervallEnde = resultat.getDataTime()
				+ data.getTimeValue("T").getMillis(); //$NON-NLS-1$
		this.inhalt = AusfallDatum.getAusfallDatumVon(resultat);
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
	public static AusfallDatumKomplett getAusfallDatumVon(
			final ResultData resultat) {
		AusfallDatumKomplett datum = null;

		if (resultat != null && resultat.getData() != null) {
			datum = new AusfallDatumKomplett(resultat);
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
	public AusfallDatum getInhalt() {
		return this.inhalt;
	}

}
