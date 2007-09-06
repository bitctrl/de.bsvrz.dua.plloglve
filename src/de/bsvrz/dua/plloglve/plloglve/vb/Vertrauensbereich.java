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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

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

//	/**
//	 * Läuft die Applikation im Testbetrieb, dann ist ein Tag genau 144s lang!
//	 */
//	protected static final boolean TEST = false;
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Map von Systemobjekten auf Fahrstreifenobjekte mit Informationen
	 * zu den jeweiligen Vertrauensbereichsverletzungen
	 */
	private Map<SystemObject, VertrauensFahrStreifen> fahrStreifenMap = 
				new HashMap<SystemObject, VertrauensFahrStreifen>();
	
	/**
	 * speichert ob das letzte Ergebnis (pro Fahrstreifen) auf keine Daten stand
	 */
	private Map<SystemObject, Boolean> keineDaten = new HashMap<SystemObject, Boolean>();
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param stdAspekte Informationen zu den
	 * Standardpublikationsaspekten für dieses Modul
	 */
	public Vertrauensbereich(final IStandardAspekte stdAspekte){
		this.standardAspekte = stdAspekte;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		
		if(this.publizieren){
			this.publikationsAnmeldungen.modifiziereObjektAnmeldung(this.standardAspekte.
					getStandardAnmeldungen(this.verwaltung.getSystemObjekte()));
		}
				
		for(SystemObject fsObj:dieVerwaltung.getSystemObjekte()){
			this.fahrStreifenMap.put(fsObj, new VertrauensFahrStreifen(dieVerwaltung, fsObj));
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			
			List<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();
			
			for(ResultData resultat:resultate){
				if(resultat != null){
					Data datum = null;

					if(resultat.getData() != null){
						VertrauensFahrStreifen fs = this.fahrStreifenMap.get(resultat.getObject());
						
						if(fs != null){
							datum = fs.plausibilisiere(resultat);
						}else{
							LOGGER.warning("Datum für nicht identifizierbaren Fahrstreifen empfangen: " +  //$NON-NLS-1$
									resultat.getObject());
						}						
					}
					
					ResultData publikationsDatum = new ResultData(resultat.getObject(),
																	new DataDescription(resultat.getDataDescription().getAttributeGroup(), 
																	standardAspekte.getStandardAspekt(resultat), (short)0),
																	resultat.getDataTime(), datum);
					ResultData weiterzuleitendesDatum = new ResultData(resultat.getObject(),
																	resultat.getDataDescription(),
																	resultat.getDataTime(), datum);

					if(this.publizieren){
						boolean sende = true;
						if(publikationsDatum.getData() == null){
							Boolean letztesDatumKeineDaten = this.keineDaten.get(publikationsDatum.getObject());
							if(letztesDatumKeineDaten == null || letztesDatumKeineDaten){
								sende = false;
							}
						}	
						
						if(sende){
							this.publikationsAnmeldungen.sende(publikationsDatum);
							this.keineDaten.put(publikationsDatum.getObject(), publikationsDatum.getData() == null);
						}
					}
					
					weiterzuleitendeResultate.add(weiterzuleitendesDatum);
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
		// Datenflusssteuerung ist hier nicht dynamisch
	}
}