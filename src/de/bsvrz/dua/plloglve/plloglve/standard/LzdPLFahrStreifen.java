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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;

/**
 * Klasse zum Durchführen der speziellen Standardplausibilisierung LVE
 * für KZD. Diese Klasse macht nichts weiter, als sich auf die Grenzwertparameter
 * anzumelden und einige Funktionen zur Plausibilisierung von KZD zur Verfügung zu stellen
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class LzdPLFahrStreifen 
extends KzdPLFahrStreifen{
	
	
	/**
	 * Standartdkonstruktor 
	 * 
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param obj das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	protected LzdPLFahrStreifen(final IVerwaltungMitGuete verwaltung, final SystemObject obj){
		super(verwaltung, obj);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void ueberpruefe(Data data, ResultData resultat) {
		if(this.parameterAtgLog != null){	
			synchronized (this.parameterAtgLog) {				
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheAufMaxVerletzung(data, resultat, "qKfz", this.parameterAtgLog.getQKfzBereichMax()); //$NON-NLS-1$

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				this.untersucheAufMaxVerletzung(data, resultat, "qLkw", this.parameterAtgLog.getQLkwBereichMax()); //$NON-NLS-1$
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getPlausibilisierungsParameterAtg(ClientDavInterface dav) {
		return dav.getDataModel().getAttributeGroup(AtgVerkehrsDatenLZIPlPruefLogisch.getPid());
	}	
	
}
