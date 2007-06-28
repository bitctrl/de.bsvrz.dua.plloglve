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

package de.bsvrz.dua.plloglve.plloglve.standard;

import stauma.dav.clientside.Data;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;

/**
 * Repr�sentiert die DAV-ATG
 * <code>atg.verkehrsDatenKurzZeitIntervallPlausibilit�tsPr�fungLogisch</code>
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AtgVerkehrsDatenKZIPlPruefLogisch 
extends AbstraktAtgPLLogischLVEParameter{

	
	/**
	 * Standardkonstruktor
	 * 
	 * @param data initialisierendes DAV-Datum
	 */
	public AtgVerkehrsDatenKZIPlPruefLogisch(final Data data){
		if(data == null){
			throw new NullPointerException("Uebergebenes Datum ist <<null>>"); //$NON-NLS-1$
		}
		this.vKfzGrenz = data.getUnscaledValue("vKfzGrenz").intValue(); //$NON-NLS-1$
		this.bGrenz = data.getUnscaledValue("bGrenz").intValue(); //$NON-NLS-1$
		this.optionen = OptionenPlausibilitaetsPruefungLogischVerkehr.getZustand(data.getUnscaledValue("Optionen").intValue()); //$NON-NLS-1$
		this.qKfzBereichMin = data.getItem("qKfzBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qKfzBereichMax = data.getItem("qKfzBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qLkwBereichMin = data.getItem("qLkwBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qLkwBereichMax = data.getItem("qLkwBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qPkwBereichMin = data.getItem("qPkwBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.qPkwBereichMax = data.getItem("qPkwBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vKfzBereichMin = data.getItem("vKfzBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vKfzBereichMax = data.getItem("vKfzBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vLkwBereichMin = data.getItem("vLkwBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vLkwBereichMax = data.getItem("vLkwBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vPkwBereichMin = data.getItem("vPkwBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vPkwBereichMax = data.getItem("vPkwBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vgKfzBereichMin = data.getItem("vgKfzBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.vgKfzBereichMax = data.getItem("vgKfzBereich").getUnscaledValue("Max").intValue(); //$NON-NLS-1$//$NON-NLS-2$
		this.belegungBereichMin = data.getItem("BelegungBereich").getUnscaledValue("Min").intValue();  //$NON-NLS-1$//$NON-NLS-2$
		this.belegungBereichMax = data.getItem("BelegungBereich").getUnscaledValue("Max").intValue();  //$NON-NLS-1$//$NON-NLS-2$
	}

	
	/**
	 * Erfragt die PID dieser Attributgruppe
	 * 
	 * @return die PID dieser Attributgruppe
	 */
	public static final String getPid(){
		return "atg.verkehrsDatenKurzZeitIntervallPlausibilit�tsPr�fungLogisch"; //$NON-NLS-1$
	}
	
}
