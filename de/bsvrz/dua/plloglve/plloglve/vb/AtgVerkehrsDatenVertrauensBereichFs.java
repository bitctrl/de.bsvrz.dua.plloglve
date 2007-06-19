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

package de.bsvrz.dua.plloglve.plloglve.vb;

import de.bsvrz.sys.funclib.bitctrl.dua.AtgDatenObjekt;
import stauma.dav.clientside.Data;

/**
 * Repräsentiert die DAV-ATG
 * <code>atg.verkehrsDatenVertrauensBereichFs</code>
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AtgVerkehrsDatenVertrauensBereichFs 
extends AtgDatenObjekt{

	/**
	 * Bezugszeitraum für die Vertrauensbereichsüberprüfung.
	 */
	private int bezugsZeitraum;
	
	/**
	 * Einschaltschwelle für den Vertrauensbereich eines Fahrstreifens im
	 * Bezugszeitraum. Bei Überschreiten dieses Wertes wird eine entsprechende Fehlermeldung generiert.
	 */
	private int maxAusfallProBezugsZeitraumEin;
	
	/**
	 * Ausschaltschwelle für den Vertrauensbereich eines Fahrstreifens im Bezugszeitraum.
	 * Bei Unterschreiten dieses Wertes wird eine entsprechende Fehlermeldung zurückgenommen
	 */
	private int maxAusfallProBezugsZeitraumAus;  

	
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
	 * Erfragt BezugsZeitraum
	 * 
	 * @return bezugsZeitraum
	 */
	public final int getBezugsZeitraum() {
		return bezugsZeitraum;
	}


	/**
	 * Erfragt maxAusfallProBezugsZeitraumAus
	 * 
	 * @return maxAusfallProBezugsZeitraumAus
	 */
	public final int getMaxAusfallProBezugsZeitraumAus() {
		return maxAusfallProBezugsZeitraumAus;
	}


	/**
	 * Erfragt maxAusfallProBezugsZeitraumEin
	 * 
	 * @return maxAusfallProBezugsZeitraumEin
	 */
	public final int getMaxAusfallProBezugsZeitraumEin() {
		return maxAusfallProBezugsZeitraumEin;
	}
	
}
