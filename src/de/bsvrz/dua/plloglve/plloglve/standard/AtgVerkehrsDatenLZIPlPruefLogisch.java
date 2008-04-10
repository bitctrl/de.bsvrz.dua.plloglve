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

package de.bsvrz.dua.plloglve.plloglve.standard;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;

/**
 * Repräsentiert die DAV-ATG
 * <code>atg.verkehrsDatenLangZeitIntervallPlausibilitätsPrüfungLogisch</code>.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class AtgVerkehrsDatenLZIPlPruefLogisch extends
		AbstraktAtgPLLogischLVEParameter {

	/**
	 * Standardkonstruktor.
	 * 
	 * @param data
	 *            initialisierendes DAV-Datum
	 */
	public AtgVerkehrsDatenLZIPlPruefLogisch(final Data data) {
		if (data == null) {
			throw new NullPointerException("Uebergebenes Datum ist <<null>>"); //$NON-NLS-1$
		}
		this.optionen = OptionenPlausibilitaetsPruefungLogischVerkehr
				.getZustand(data.getUnscaledValue("Optionen").intValue()); //$NON-NLS-1$
		this.qKfzBereichMin = data
				.getItem("qKfzBereich").getUnscaledValue("Min").longValue(); //$NON-NLS-1$//$NON-NLS-2$
		this.qKfzBereichMax = data
				.getItem("qKfzBereich").getUnscaledValue("Max").longValue(); //$NON-NLS-1$//$NON-NLS-2$
		this.qLkwBereichMin = data
				.getItem("qLkwBereich").getUnscaledValue("Min").longValue(); //$NON-NLS-1$//$NON-NLS-2$
		this.qLkwBereichMax = data
				.getItem("qLkwBereich").getUnscaledValue("Max").longValue(); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Erfragt die PID dieser Attributgruppe.
	 * 
	 * @return die PID dieser Attributgruppe
	 */
	public static final String getPid() {
		return "atg.verkehrsDatenLangZeitIntervallPlausibilitätsPrüfungLogisch"; //$NON-NLS-1$
	}

}
