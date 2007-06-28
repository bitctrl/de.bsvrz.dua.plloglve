/** 
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.2 Pl-Pr�fung logisch LVE
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.plloglve.plloglve.diff;

import de.bsvrz.sys.funclib.bitctrl.dua.AtgDatenObjekt;
import stauma.dav.clientside.Data;

/**
 * Repr�sentiert die DAV-ATG
 * <code>atg.verkehrsDatenDifferenzialKontrolleFs</code>
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AtgVerkehrsDatenDifferenzialKontrolleFs
extends AtgDatenObjekt{

	/**
	 * Maximal zul�ssige Anzahl von Intervallen mit Ergebniskonstanz f�r qKfz.
	 */
	private int maxAnzKonstanzqKfz;
	
	/**
	 * Maximal zul�ssige Anzahl von Intervallen mit Ergebniskonstanz f�r qLkw.
	 */
	private int maxAnzKonstanzqLkw;
	
	/**
	 * Maximal zul�ssige Anzahl von Intervallen mit Ergebniskonstanz f�r qPkw.
	 */
	private int maxAnzKonstanzqPkw;
	
	/**
	 * Maximal zul�ssige Anzahl von Intervallen mit Ergebniskonstanz f�r vKfz.
	 */
	private int maxAnzKonstanzvKfz;
	
	/**
	 * Maximal zul�ssige Anzahl von Intervallen mit Ergebniskonstanz f�r vLkw.
	 */
	private int maxAnzKonstanzvLkw;
	
	/**
	 * Maximal zul�ssige Anzahl von Intervallen mit Ergebniskonstanz f�r vPkw.
	 */
	private int maxAnzKonstanzvPkw;
	
	/**
	 * Maximal zul�ssige Anzahl von Intervallen mit Ergebniskonstanz f�r die Streung S.
	 */
	private int maxAnzKonstanzStreung;
	
	/**
	 * Maximal zul�ssige Anzahl von Intervallen mit Ergebniskonstanz f�r die Belegung b.
	 */
	private int maxAnzKonstanzBelegung;  


	/**
	 * Standardkonstruktor
	 * 
	 * @param data initialisierendes DAV-Datum
	 */
	public AtgVerkehrsDatenDifferenzialKontrolleFs(final Data data){
		if(data == null){
			throw new NullPointerException("Uebergebenes Datum ist <<null>>"); //$NON-NLS-1$
		}
		this.maxAnzKonstanzqKfz = data.getUnscaledValue("maxAnzKonstanzqKfz").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzqLkw = data.getUnscaledValue("maxAnzKonstanzqLkw").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzqPkw = data.getUnscaledValue("maxAnzKonstanzqPkw").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvKfz = data.getUnscaledValue("maxAnzKonstanzvKfz").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvLkw = data.getUnscaledValue("maxAnzKonstanzvLkw").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzvPkw = data.getUnscaledValue("maxAnzKonstanzvPkw").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzStreung = data.getUnscaledValue("maxAnzKonstanzStreung").intValue(); //$NON-NLS-1$
		this.maxAnzKonstanzBelegung = data.getUnscaledValue("maxAnzKonstanzBelegung").intValue(); //$NON-NLS-1$
	}


	/**
	 * Erfragt maxAnzKonstanzBelegung
	 * 
	 * @return maxAnzKonstanzBelegung
	 */
	public final int getMaxAnzKonstanzBelegung() {
		return maxAnzKonstanzBelegung;
	}


	/**
	 * Erfragt maxAnzKonstanzqKfz
	 * 
	 * @return maxAnzKonstanzqKfz
	 */
	public final int getMaxAnzKonstanzqKfz() {
		return maxAnzKonstanzqKfz;
	}


	/**
	 * Erfragt maxAnzKonstanzqLkw
	 * 
	 * @return maxAnzKonstanzqLkw
	 */
	public final int getMaxAnzKonstanzqLkw() {
		return maxAnzKonstanzqLkw;
	}


	/**
	 * Erfragt maxAnzKonstanzqPkw
	 * 
	 * @return maxAnzKonstanzqPkw
	 */
	public final int getMaxAnzKonstanzqPkw() {
		return maxAnzKonstanzqPkw;
	}


	/**
	 * Erfragt maxAnzKonstanzStreung
	 * 
	 * @return maxAnzKonstanzStreung
	 */
	public final int getMaxAnzKonstanzStreung() {
		return maxAnzKonstanzStreung;
	}


	/**
	 * Erfragt maxAnzKonstanzvKfz
	 * 
	 * @return maxAnzKonstanzvKfz
	 */
	public final int getMaxAnzKonstanzvKfz() {
		return maxAnzKonstanzvKfz;
	}


	/**
	 * Erfragt maxAnzKonstanzvLkw
	 * 
	 * @return maxAnzKonstanzvLkw
	 */
	public final int getMaxAnzKonstanzvLkw() {
		return maxAnzKonstanzvLkw;
	}


	/**
	 * Erfragt maxAnzKonstanzvPkw
	 * 
	 * @return maxAnzKonstanzvPkw
	 */
	public final int getMaxAnzKonstanzvPkw() {
		return maxAnzKonstanzvPkw;
	}

}
