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

package de.bsvrz.dua.plloglve.plloglve.ausfall;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IIntervallDatum;

/**
 * Repräsentiert ein ausgefallenes KZ-Datum.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public final class AusfallDatum implements IIntervallDatum<AusfallDatum> {

	/**
	 * ist dieses Datum "ausgefallen".
	 */
	private boolean ausgefallen = false;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param resultat
	 *            ein KZD-Datum
	 */
	private AusfallDatum(ResultData resultat) {
		Data data = resultat.getData();

		final long qKfzWert = data
				.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long qLkwWert = data
				.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long qPkwWert = data
				.getItem("qPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long vPkwWert = data
				.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long vLkwWert = data
				.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long vKfzWert = data
				.getItem("vKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$

		final long qKfzImpl = data
				.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").
				getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long qLkwImpl = data
				.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").
				getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long qPkwImpl = data
				.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").
				getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long vKfzImpl = data
				.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").
				getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long vLkwImpl = data
				.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").
				getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long vPkwImpl = data
				.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").
				getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	

		if (qKfzWert == DUAKonstanten.FEHLERHAFT
				&& qKfzImpl == DUAKonstanten.JA) {
			this.ausgefallen = true;
		}
		if (qLkwWert == DUAKonstanten.FEHLERHAFT
				&& qLkwImpl == DUAKonstanten.JA) {
			this.ausgefallen = true;
		}
		if (qPkwWert == DUAKonstanten.FEHLERHAFT
				&& qPkwImpl == DUAKonstanten.JA) {
			this.ausgefallen = true;
		}
		if (vKfzWert == DUAKonstanten.FEHLERHAFT
				&& vKfzImpl == DUAKonstanten.JA) {
			this.ausgefallen = true;
		}
		if (vLkwWert == DUAKonstanten.FEHLERHAFT
				&& vLkwImpl == DUAKonstanten.JA) {
			this.ausgefallen = true;
		}
		if (vPkwWert == DUAKonstanten.FEHLERHAFT
				&& vPkwImpl == DUAKonstanten.JA) {
			this.ausgefallen = true;
		}
	}

	/**
	 * Gibt nur ein Datum zurueck, wenn es sich um ein Datum handelt, dass auch
	 * im Sinne der Plausibilisierung ausgewertet werden kann Also z.B. nicht
	 * <code>keine Quelle</code> oder <code>keine Daten</code>
	 * 
	 * @param resultat
	 *            ein Kz-Datum
	 * @return eine mit dem uebergebenen Datum korrespondierende Instanz dieser
	 *         Klasse oder <code>null</code> fuer <code>keine Quelle</code>
	 *         usw.
	 */
	public static AusfallDatum getAusfallDatumVon(
			final ResultData resultat) {
		AusfallDatum datum = null;

		if (resultat != null && resultat.getData() != null) {
			datum = new AusfallDatum(resultat);
		}

		return datum;
	}

	/**
	 * Erfragt ob dieses Datum ausgefallen ist.
	 * 
	 * @return ob dieses Datum ausgefallen ist
	 */
	public boolean isAusgefallen() {
		return this.ausgefallen;
	}

	@Override
	public String toString() {
		String s = null;

		if (this.ausgefallen) {
			s = "Ausgefallen"; //$NON-NLS-1$
		} else {
			s = "OK"; //$NON-NLS-1$
		}

		return s;
	}

	/**
	 * Zwei Ausfalldaten sind gleich, wenn deren Attribute
	 * <code>ausgefallen</code> den selben Wert haben.
	 * 
	 * @return das Vergleichsergebnis
	 */
	public boolean istGleich(AusfallDatum that) {
		return this.isAusgefallen() == that.isAusgefallen();
	}

}
