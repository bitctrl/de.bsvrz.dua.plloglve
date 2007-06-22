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

package de.bsvrz.dua.plloglve.plloglve.ausfall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class Ausfallhaeufigkeit
extends AbstraktBearbeitungsKnotenAdapter{

	/**
	 * 
	 */
	private Map<SystemObject, AusfallFahrStreifen> fahrStreifen =
									new TreeMap<SystemObject, AusfallFahrStreifen>();
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		
		for(SystemObject obj:dieVerwaltung.getSystemObjekte()){
			this.fahrStreifen.put(obj, new AusfallFahrStreifen(dieVerwaltung, obj));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			Collection<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();
			
			for(ResultData resultat:resultate){				
				if(resultat != null){
					if(resultat.getData() != null){					
						AusfallFahrStreifen fs = this.fahrStreifen.get(resultat.getObject());
						
						if(fs != null){
							fs.plausibilisiere(resultat);
						}						
					}
					
					weiterzuleitendeResultate.add(resultat);				
				}
			}
			
			if(this.knoten != null && !weiterzuleitendeResultate.isEmpty()){
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert		
	}
}