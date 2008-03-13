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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IIntervallDatum;

/**
 * Repräsentiert ein ausgefallenes KZ-Datum
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AusfallDatum 
implements IIntervallDatum<AusfallDatum>{
	
	/**
	 * ist dieses Datum "ausgefallen"
	 */
	private boolean ausgefallen = false;

	
	/**
	 * Standardkonstruktor
	 * 
	 * @param resultat ein KZD-Datum
	 */
	private AusfallDatum(ResultData resultat){
		Data data = resultat.getData();

		final long qKfzWert = data.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long qLkwWert = data.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long qPkwWert = data.getItem("qPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long vPkwWert = data.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long vLkwWert = data.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long vKfzWert = data.getItem("vKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long bWert = data.getItem("b").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long sKfzWert = data.getItem("sKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$

		final long qKfzImpl = data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long qLkwImpl = data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long qPkwImpl = data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long vKfzImpl = data.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long vLkwImpl = data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long vPkwImpl = data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long sKfzImpl = data.getItem("sKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$	
		final long bImpl = data.getItem("b").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").longValue(); //$NON-NLS-1$
		
		if(qKfzWert == DUAKonstanten.FEHLERHAFT || qKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
		}
		if(qLkwWert == DUAKonstanten.FEHLERHAFT || qLkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
		}
		if(qPkwWert == DUAKonstanten.FEHLERHAFT || qPkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
		}
		if(vKfzWert == DUAKonstanten.FEHLERHAFT || vKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
		}
		if(vLkwWert == DUAKonstanten.FEHLERHAFT || vLkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
		}
		if(vPkwWert == DUAKonstanten.FEHLERHAFT || vPkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
		}
		if(sKfzWert == DUAKonstanten.FEHLERHAFT || sKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
		}
		if(bWert == DUAKonstanten.FEHLERHAFT || bImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
		}
	}
	
	
	/**
	 * Gibt nur ein Datum zurueck, wenn es sich um ein Datum handelt, dass
	 * auch im Sinne der Plausibilisierung ausgewertet werden kann
	 * Also z.B. nicht <code>keine Quelle</code> oder <code>keine Daten</code>
	 * 
	 * @param resultat ein Kz-Datum
	 * @return eine mit dem uebergebenen Datum korrespondierende Instanz dieser Klasse
	 * oder <code>null</code> fuer <code>keine Quelle</code> usw.
	 */
	public static final AusfallDatum getAusfallDatumVon(final ResultData resultat){
		AusfallDatum datum = null;
		
		if(resultat != null && resultat.getData() != null){
			datum = new AusfallDatum(resultat);
		}
			
		return datum;
	}
	
	
	/**
	 * Erfragt ob dieses Datum ausgefallen ist
	 * 
	 * @return ob dieses Datum ausgefallen ist
	 */
	public final boolean isAusgefallen() {
		return this.ausgefallen;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = null;

		if(this.ausgefallen){
			s = "Ausgefallen"; //$NON-NLS-1$
		}else{
			s = "OK"; //$NON-NLS-1$
		}

		return s;
	}

	
	/**
	 * {@inheritDoc}<br>
	 * 
	 * Zwei Ausfalldaten sind gleich, wenn deren Attribute <code>ausgefallen</code>
	 * den selben Wert haben.
	 */
	public boolean istGleich(AusfallDatum that) {
		return this.isAusgefallen() == that.isAusgefallen();
	}

}
