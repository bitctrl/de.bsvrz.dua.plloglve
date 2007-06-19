/** 
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Plausibilitätsprüfung logisch LVE
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

import java.text.SimpleDateFormat;
import java.util.Date;

import de.bsvrz.dua.plloglve.plloglve.PLLOGKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

import stauma.dav.clientside.Data;
import stauma.dav.clientside.ResultData;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AusfallDatum 
implements Comparable<AusfallDatum>{
	
	/**
	 * 
	 */
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");  //$NON-NLS-1$
	
	/**
	 * 
	 */
	private String ausfallStr = null;
	
	/**
	 * 
	 */
	private boolean ausgefallen = false;
	
	/**
	 * 
	 */
	private long datenZeit = -1;
	
	/**
	 * 
	 */
	private long intervallLaenge = -1;

	
	/**
	 * 
	 * @param datum
	 */
	private AusfallDatum(ResultData resultat){
		Data data = resultat.getData();
		
		this.intervallLaenge = data.getTimeValue("T").getMillis(); //$NON-NLS-1$
		
		final int qKfzWert = data.getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qLkwWert = data.getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qPkwWert = data.getItem("qPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vPkwWert = data.getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vLkwWert = data.getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vKfzWert = data.getItem("vKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int bWert = data.getItem("b").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int sKfzWert = data.getItem("sKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$

		final int qKfzImpl = data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$	
		final int qLkwImpl = data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$	
		final int qPkwImpl = data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$	
		final int vKfzImpl = data.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$	
		final int vLkwImpl = data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$	
		final int vPkwImpl = data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$	
		final int sKfzImpl = data.getItem("sKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$	
		final int bImpl = data.getItem("b").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
								getUnscaledValue("Implausibel").intValue(); //$NON-NLS-1$
		
		this.ausfallStr = Konstante.LEERSTRING;
		if(qKfzWert == PLLOGKonstanten.FEHLERHAFT || qKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "qKfz, "; //$NON-NLS-1$
		}
		if(qLkwWert == PLLOGKonstanten.FEHLERHAFT || qLkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "qLkw, "; //$NON-NLS-1$
		}
		if(qPkwWert == PLLOGKonstanten.FEHLERHAFT || qPkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "qPkw, "; //$NON-NLS-1$
		}
		if(vKfzWert == PLLOGKonstanten.FEHLERHAFT || vKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "vKfz, "; //$NON-NLS-1$
		}
		if(vLkwWert == PLLOGKonstanten.FEHLERHAFT || vLkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "vLkw, "; //$NON-NLS-1$
		}
		if(vPkwWert == PLLOGKonstanten.FEHLERHAFT || vPkwImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "vPkw, "; //$NON-NLS-1$
		}
		if(sKfzWert == PLLOGKonstanten.FEHLERHAFT || sKfzImpl == DUAKonstanten.JA){
			this.ausgefallen = true;
			this.ausfallStr += "sKfz, "; //$NON-NLS-1$
		}
		if(bWert == PLLOGKonstanten.FEHLERHAFT || bImpl == DUAKonstanten.JA){
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
	 * @param resultat
	 * @return
	 */
	public static final AusfallDatum getAusfallDatumVon(final ResultData resultat){
		AusfallDatum datum = null;
		
		if(resultat != null && resultat.getData() != null){
			datum = new AusfallDatum(resultat);
		}
			
		return datum;
	}
	
	/**
	 * 
	 * @return
	 */
	public final long getIntervallLaenge(){
		return this.intervallLaenge;
	}
	
	/**
	 * 
	 * @return
	 */
	public final boolean isDatumVeraltet(){
		return this.datenZeit + PLLOGKonstanten.EIN_TAG_IN_MS < System.currentTimeMillis();
	}
	
	
	/**
	 * @return ausfall
	 */
	public final boolean isAusgefallen() {
		return this.ausgefallen;
	}

	/**
	 * 
	 * @return
	 */
	protected final long getDatenZeit(){
		return this.datenZeit;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AusfallDatum that) {
		return new Long(this.getDatenZeit()).compareTo(that.getDatenZeit());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean resultat = false;
		
		if(obj instanceof AusfallDatum){
			AusfallDatum that = (AusfallDatum)obj;
			resultat = this.getDatenZeit() == that.getDatenZeit();
		}
		
		return resultat;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = "Datenzeit: " + FORMAT.format(new Date(this.datenZeit)) +  //$NON-NLS-1$
								" (" + this.datenZeit + "ms)\n"; //$NON-NLS-1$ //$NON-NLS-2$
		
		if(this.ausgefallen){
			s += "Ausgefallen: " + this.ausfallStr; //$NON-NLS-1$
		}else{
			s += "OK"; //$NON-NLS-1$
		}
		
		return s;
	}

}
