/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
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

package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.Date;

import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Ergebnisobjekt eines Tests der Komponente Meteorologische Kontrolle
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MeteoErgebnis{
	
	/**
	 * Datenzeitstempel des Ergebnisses
	 */
	private long zeitStempel = -1;
	
	/**
	 * Ist das Ergebnis als Implausibel gekennzeichnet
	 */
	private boolean implausibel = false;
	
	/**
	 * das Systemobjekt des Umfelddatensensors
	 */
	private SystemObject sensor = null;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param sensor das Systemobjekt des Umfelddatensensors
	 * @param zeitStempel Datenzeitstempel des Ergebnisses
	 * @param implausibel Ist das Ergebnis als Implausibel gekennzeichnet
	 */
	public MeteoErgebnis(final SystemObject sensor, final long zeitStempel, final boolean implausibel){
		this.sensor = sensor;
		this.zeitStempel = zeitStempel;
		this.implausibel = implausibel;
	}


	/**
	 * Erfragt ob das Ergebnis als Implausibel gekennzeichnet
	 * 
	 * @return implausibel Ist das Ergebnis als Implausibel gekennzeichnet
	 */
	public final boolean isImplausibel() {
		return implausibel;
	}


	/**
	 * Erfragt das Systemobjekt des Umfelddatensensors
	 * 
	 * @return sensor das Systemobjekt des Umfelddatensensors
	 */
	public final SystemObject getSensor() {
		return sensor;
	}

	
	/**
	 * Erfragt den Datenzeitstempel des Ergebnisses
	 * 
	 * @return zeitStempel Datenzeitstempel des Ergebnisses
	 */
	public final long getZeitStempel() {
		return zeitStempel;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ergebnis = false;
		
		if(obj instanceof MeteoErgebnis){
			MeteoErgebnis that = (MeteoErgebnis)obj;
			ergebnis = this.getSensor() == that.getSensor() &&
					   this.getZeitStempel() == that.getZeitStempel() &&
					   this.isImplausibel() == that.isImplausibel();						 
		}
		
		return ergebnis;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Sensor: " + this.sensor.getPid() + ", " + DUAKonstanten.  //$NON-NLS-1$//$NON-NLS-2$
				NUR_ZEIT_FORMAT_GENAU.format(new Date(this.zeitStempel)) +
				", impl.: " + (this.implausibel?"ja":"nein");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	}
	
}

