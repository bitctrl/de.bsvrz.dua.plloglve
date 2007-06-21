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

package de.bsvrz.dua.plloglve.plloglve.vb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import stauma.dav.clientside.Data;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.av.DAVObjektAnmeldung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.DFSKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerungFuerModul;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Dieses Submodul überwacht die Einschalt- bzw. Ausschaltschwelle für
 * den Vertrauensbereich eines Fahrstreifens im Bezugszeitraum. Beim
 * Betreten bzw. Verlassen des Vertrauensbereichs wird eine entsprechende
 * Meldung generiert. Darüber hinaus findet innerhalb dieses Submoduls
 * ggf. die Publikation aller empfangenen Daten statt.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class Vertrauensbereich
extends AbstraktBearbeitungsKnotenAdapter{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Map von Systemobjekten auf Fahrstreifenobjekte
	 */
	private Map<SystemObject, VertrauensFahrStreifen> fahrStreifenMap = 
				new TreeMap<SystemObject, VertrauensFahrStreifen>();
	
	/**
	 * Parameter zur Datenflusssteuerung für diese
	 * SWE und dieses Modul
	 */
	private IDatenFlussSteuerungFuerModul iDfsMod
								= DFSKonstanten.STANDARD;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param stdAspekte Informationen zu den
	 * Standardpublikationsaspekten für dieses Modul
	 */
	public Vertrauensbereich(final IStandardAspekte stdAspekte){
		if(stdAspekte != null){
			this.standardAspekte = stdAspekte;
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
				
		for(SystemObject fsObj:dieVerwaltung.getSystemObjekte()){
			this.fahrStreifenMap.put(fsObj, new VertrauensFahrStreifen(dieVerwaltung, fsObj));
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat: resultate){
				if(resultat != null && resultat.getData() != null){
					VertrauensFahrStreifen fs = this.fahrStreifenMap.get(resultat.getObject());
					
					if(fs != null){
						Data data = null;
						data = fs.plausibilisiere(resultat);

						if(this.publizieren){
							if(data == null){
								data = resultat.getData();
							}
							
							ResultData publikationsDatum = 
									iDfsMod.getPublikationsDatum(resultat,
											data, standardAspekte.getStandardAspekt(resultat));
							if(publikationsDatum != null){
								this.publikationsAnmeldungen.sende(publikationsDatum);
							}
						}						
					}else{
						LOGGER.warning("Datum für nicht identifizierbaren Fahrstreifen empfangen: " +  //$NON-NLS-1$
								resultat.getObject().getPid());
					}
				}
			}
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return ModulTyp.PL_PRUEFUNG_LOGISCH_LVE;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		if(this.publizieren){
			SystemObject[] objektFilter = new SystemObject[0];
						
			Collection<DAVObjektAnmeldung> anmeldungenStd =
							new ArrayList<DAVObjektAnmeldung>();

			if(this.standardAspekte != null){
				anmeldungenStd = this.standardAspekte.
									getStandardAnmeldungen(
									objektFilter);
			}
			
			Collection<DAVObjektAnmeldung> anmeldungen = 
					this.iDfsMod.getDatenAnmeldungen(objektFilter, 
							anmeldungenStd);
			
			synchronized(this){
				this.publikationsAnmeldungen.modifiziereObjektAnmeldung(anmeldungen);
			}
		}
	}
}