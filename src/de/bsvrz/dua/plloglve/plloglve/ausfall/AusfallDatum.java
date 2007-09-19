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
import de.bsvrz.dua.plloglve.plloglve.AbstraktDAVZeitEinzelDatum;
import de.bsvrz.dua.plloglve.plloglve.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Repräsentiert ein ausgefallenes KZ-Datum
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AusfallDatum 
extends AbstraktDAVZeitEinzelDatum{
		
	/**
	 * eine Zeichenkette, die den Ausfall illustriert (formatiert für Betriebsmeldungen)
	 */
	private String ausfallStr = null;
	
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

		this.datenZeit = resultat.getDataTime();
		this.intervallLaenge = data.getTimeValue("T").getMillis(); //$NON-NLS-1$
		
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
		
		this.ausfallStr = Konstante.LEERSTRING;
		if(qKfzWert == DUAKonstanten.FEHLERHAFT || qKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "qKfz, "; //$NON-NLS-1$
		}
		if(qLkwWert == DUAKonstanten.FEHLERHAFT || qLkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "qLkw, "; //$NON-NLS-1$
		}
		if(qPkwWert == DUAKonstanten.FEHLERHAFT || qPkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "qPkw, "; //$NON-NLS-1$
		}
		if(vKfzWert == DUAKonstanten.FEHLERHAFT || vKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "vKfz, "; //$NON-NLS-1$
		}
		if(vLkwWert == DUAKonstanten.FEHLERHAFT || vLkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "vLkw, "; //$NON-NLS-1$
		}
		if(vPkwWert == DUAKonstanten.FEHLERHAFT || vPkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "vPkw, "; //$NON-NLS-1$
		}
		if(sKfzWert == DUAKonstanten.FEHLERHAFT || sKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "sKfz, "; //$NON-NLS-1$
		}
		if(bWert == DUAKonstanten.FEHLERHAFT || bImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "b, "; //$NON-NLS-1$
		}
		
		if(this.ausgefallen){
			this.ausfallStr = this.ausfallStr.substring(0, this.ausfallStr.length() - 2);
		}
	}
	
	/**
	 * Gibt nur ein Datum zurück, wenn es sich um ein Datum handelt, dass
	 * auch im Sinne der Plausibilisierung ausgewertet werden kann
	 * Also z.B. nicht "Keine Quelle"
	 * 
	 * @param resultat ein Kz-Datum
	 * @return eine mit dem übergebenen Datum korrespondierende Instanz dieser Klasse
	 * oder <code>null</code> für keine Quelle usw.
	 */
	public static final AusfallDatum getAusfallDatumVon(final ResultData resultat){
		AusfallDatum datum = null;
		
		if(resultat != null && resultat.getData() != null){
			datum = new AusfallDatum(resultat);
		}
			
		return datum;
	}

	
	/**
	 * Erfragt, ob dieses Datum im Sinne des gleitenden Tages veraltet ist
	 * 
	 * @return ob dieses Datum im Sinne des gleitenden Tages veraltet ist
	 */
	public boolean isDatumVeraltet(){
		if(TestParameter.TEST_AUSFALL){
			return this.datenZeit + 144000l < System.currentTimeMillis();
		}
		return this.datenZeit + Konstante.TAG_24_IN_MS < System.currentTimeMillis();
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
		String s = "Datenzeit: " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(this.datenZeit)) +  //$NON-NLS-1$
								" (" + this.datenZeit + "ms)\n"; //$NON-NLS-1$ //$NON-NLS-2$
		
		if(this.ausgefallen){
			s += "Ausgefallen: " + this.ausfallStr; //$NON-NLS-1$
		}else{
			s += "OK"; //$NON-NLS-1$
		}
		
		return s;
	}

}
