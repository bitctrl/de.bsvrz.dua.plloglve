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

package de.bsvrz.dua.plloglve.plloglve;

import java.text.SimpleDateFormat;

import de.bsvrz.dua.plloglve.plloglve.ausfall.AusfallDatum;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AbstraktDAVZeitEinzelDatum 
implements Comparable<AbstraktDAVZeitEinzelDatum>{
	
	/**
	 * 
	 */
	protected static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");  //$NON-NLS-1$
	
	/**
	 * 
	 */
	protected long datenZeit = -1;
		
	/**
	 * die Intervalllänge des Datums
	 */
	protected long intervallLaenge = -1;

	
	/**
	 * 
	 * @return
	 */
	public final long getIntervallLaenge(){
		return this.intervallLaenge;
	}

	public long getDatenZeit(){
		return this.datenZeit;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AbstraktDAVZeitEinzelDatum that) {
		return new Long(this.getDatenZeit()).compareTo(that.getDatenZeit());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean resultat = false;
		
		if(obj instanceof AusfallDatum){
			AbstraktDAVZeitEinzelDatum that = (AbstraktDAVZeitEinzelDatum)obj;
			resultat = this.getDatenZeit() == that.getDatenZeit();
		}
		
		return resultat;
	}

}
