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

package de.bsvrz.dua.plloglve.plloglve.vb;

import stauma.dav.clientside.Data;
import de.bsvrz.sys.funclib.bitctrl.dua.AtgDatenObjekt;

/**
 * Repr�sentiert die DAV-ATG
 * <code>atg.verkehrsDatenVertrauensBereichFs</code>
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AtgVerkehrsDatenVertrauensBereichFs 
extends AtgDatenObjekt{

	/**
	 * Bezugszeitraum f�r die Vertrauensbereichs�berpr�fung
	 */
	private long bezugsZeitraum = -1;
	
	/**
	 * Einschaltschwelle f�r den Vertrauensbereich eines Fahrstreifens im
	 * Bezugszeitraum. Bei �berschreiten dieses Wertes wird eine entsprechende Fehlermeldung generiert
	 */
	private long maxAusfallProBezugsZeitraumEin = -1;
	
	/**
	 * Ausschaltschwelle f�r den Vertrauensbereich eines Fahrstreifens im Bezugszeitraum.
	 * Bei Unterschreiten dieses Wertes wird eine entsprechende Fehlermeldung zur�ckgenommen
	 */
	private long maxAusfallProBezugsZeitraumAus = -1;  

	
	/**
	 * Standardkonstruktor
	 * 
	 * @param data initialisierendes DAV-Datum
	 */
	public AtgVerkehrsDatenVertrauensBereichFs(final Data data){
		if(data == null){
			throw new NullPointerException("Uebergebenes Datum ist <<null>>"); //$NON-NLS-1$
		}
		this.bezugsZeitraum = data.getUnscaledValue("BezugsZeitraum").intValue(); //$NON-NLS-1$
		this.maxAusfallProBezugsZeitraumEin = data.getUnscaledValue("maxAusfallProBezugsZeitraumEin").intValue(); //$NON-NLS-1$
		this.maxAusfallProBezugsZeitraumAus = data.getUnscaledValue("maxAusfallProBezugsZeitraumAus").intValue(); //$NON-NLS-1$
	}

	
	/**
	 * Erfragt, ob sich diese Parameter zur �berpr�fung eines Datensatzes eignen. Dies
	 * ist nur der Fall, wenn alle Parameter >= 0 sind. 
	 * 
	 * @return ob sich diese Parameter zur �berpr�fung eines Datensatzes eignen
	 */
	public final boolean isAuswertbar(){
		return this.bezugsZeitraum >= 0 && 
			   this.maxAusfallProBezugsZeitraumAus >= 0 &&
			   this.maxAusfallProBezugsZeitraumEin >= 0;
	}
	

	/**
	 * Erfragt Bezugszeitraum f�r die Vertrauensbereichs�berpr�fung
	 * 
	 * @return Bezugszeitraum in Stunden
	 */
	public final long getBezugsZeitraum() {
		return this.bezugsZeitraum;
	}


	/**
	 * Erfragt Ausschaltschwelle f�r den Vertrauensbereich eines Fahrstreifens im Bezugszeitraum
	 * Bei Unterschreiten dieses Wertes wird eine entsprechende Fehlermeldung zur�ckgenommen
	 * 
	 * @return Ausschaltschwelle f�r den Vertrauensbereich eines Fahrstreifens
	 */
	public final long getMaxAusfallProBezugsZeitraumAus() {
		return this.maxAusfallProBezugsZeitraumAus;
	}


	/**
	 * Erfragt Einschaltschwelle f�r den Vertrauensbereich eines Fahrstreifens im
	 * Bezugszeitraum. Bei �berschreiten dieses Wertes wird eine entsprechende
	 * Fehlermeldung generiert.
	 * 
	 * @return Einschaltschwelle f�r den Vertrauensbereich in %
	 */
	public final long getMaxAusfallProBezugsZeitraumEin() {
		return this.maxAusfallProBezugsZeitraumEin;
	}
	
}
