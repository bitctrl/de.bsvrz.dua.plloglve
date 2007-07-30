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

package de.bsvrz.dua.plloglve.plloglve.vb;

import java.util.Date;

import stauma.dav.clientside.ResultData;
import de.bsvrz.dua.plloglve.plloglve.AbstraktDAVZeitEinzelDatum;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Speichert für ein finales DAV-Attribut des Datensatzes
 * <code>atg.verkehrsDatenKurzZeitIntervall</code>, ob dieses
 * (im Sinne der Vertrauensbereichsprüfung) ausgefallen ist
 * oder nicht
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AusfallEinzelDatum 
extends AbstraktDAVZeitEinzelDatum{
		
	/**
	 * Gibt an, ob dieses Datum (im Sinne der Vertrauensbereichsprüfung)
	 * ausgefallen ist
	 */
	private boolean ausgefallen = false;

	/**
	 * Standardkonstruktor
	 * 
	 * @param name der Name dieses Datums
	 */
	protected AusfallEinzelDatum(final String name, final ResultData originalDatum){
		this.datenZeit = originalDatum.getDataTime();
		this.intervallLaenge = originalDatum.getData().
									getUnscaledValue("T").longValue(); //$NON-NLS-1$		
		this.ausgefallen = 
			originalDatum.getData().getUnscaledValue(name).longValue() == DUAKonstanten.FEHLERHAFT ||
			originalDatum.getData().getItem(name).getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$
					getUnscaledValue("Implausibel").longValue() == DUAKonstanten.JA;   //$NON-NLS-1$
	}

	
	/**
	 * Erfragt, ob dieses Datum (im Sinne der Vertrauensbereichsprüfung)
	 * ausgefallen ist oder nicht
	 * 
	 * @return ob dieses Datum ausgefallen ist
	 */
	public final boolean isAusgefallen(){
		return this.ausgefallen;
	}
	
	
	/**
	 * Erfragt, ob das Datum noch aktuell ist
	 * 
	 * @param bezugsIntervall das Bezugsintervall in Stunden
	 * @return ob das Datum noch aktuell ist
	 */
	public final boolean isDatumVeraltet(final long bezugsIntervall) {
		long bezugsIntervallInMillis = Vertrauensbereich.TEST?bezugsIntervall*6000:bezugsIntervall * Konstante.STUNDE_IN_MS;
		return this.datenZeit + bezugsIntervallInMillis < System.currentTimeMillis();
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return (this.ausgefallen?"Ausgefallen":"OK") + //$NON-NLS-1$ //$NON-NLS-2$
				"\nDatenzeit: " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(this.datenZeit)) +  //$NON-NLS-1$
					" (" + this.datenZeit + "ms)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
