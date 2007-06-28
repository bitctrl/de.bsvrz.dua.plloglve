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

package de.bsvrz.dua.plloglve.plloglve.diff;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class WertMitKonstanzZaehler {
	
	/**
	 * 
	 */
	private String name = null;
	
	/**
	 * 
	 */
	private Integer wert = null;
	
	/**
	 * 
	 */
	private int wertIstKonstantSeit = 0;

	
	/**
	 * 
	 * @param name
	 */
	protected WertMitKonstanzZaehler(final String name){
		this.name = name;
	}
	
	
	/**
	 * 
	 * @param neuerWert
	 */
	protected final void update(final int neuerWert){
		if(this.wert == null || this.wert != neuerWert){
			this.wertIstKonstantSeit = 1;
		}else{
			this.wertIstKonstantSeit++;
		}
		
		this.wert = neuerWert;
	}
		
	
	/**
	 * @return wert
	 */
	protected final Integer getWert() {
		return this.wert;
	}


	/**
	 * @return wertIstKonstantSeit
	 */
	protected final int getWertIstKonstantSeit() {
		return this.wertIstKonstantSeit;
	}
	

	/**
	 * @return wertIstKonstantSeit
	 */
	protected final String getName() {
		return this.name;
	}	


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = this.name + " ist seit " + this.wertIstKonstantSeit + //$NON-NLS-1$
									" Intervallen konstant " + this.wert; //$NON-NLS-1$
	
		if(this.wert == null){
			s = this.name + " wurde noch nicht beschrieben"; //$NON-NLS-1$
		}
			
		return s;
	}

}
