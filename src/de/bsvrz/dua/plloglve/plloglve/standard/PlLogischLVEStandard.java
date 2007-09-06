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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Das Submodul PL-Prüfung logisch LVE standard führt zunächst eine Wertebereichsprüfung
 * für die empfangenen Datensätzen durch. Diese orientiert sich an den dazu vorgesehenen
 * Parametern. Folgende Attributarten werden untersucht:<br>
 * - Verkehrsstärken,<br>
 * - Mittlere Geschwindigkeiten,<br>
 * - Belegungen,<br>
 * - Standardabweichungen, etc..<br>
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class PlLogischLVEStandard
extends AbstraktBearbeitungsKnotenAdapter{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Mapt alle FS-Systemobjekte auf für die Standardplausbibilisierung für 
	 * LZD parametrierte Fahrstreifenobjekte 
	 */
	private Map<SystemObject, AbstraktPLFahrStreifen> lzdFahrStreifen = 
								new HashMap<SystemObject, AbstraktPLFahrStreifen>();

	/**
	 * Mapt alle FS-Systemobjekte auf für die Standardplausbibilisierung für 
	 * KZD parametrierte Fahrstreifenobjekte 
	 */
	private Map<SystemObject, AbstraktPLFahrStreifen> kzdFahrStreifen =
								new HashMap<SystemObject, AbstraktPLFahrStreifen>();
	
	/**
	 * Datenverteiler-ID der KZD-Attributgruppe
	 */
	private static long ATG_KZD_ID = -1;

	/**
	 * Datenverteiler-ID der LZD-Attributgruppe
	 */
	private static long ATG_LZD_ID = -1;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		
		ATG_KZD_ID = dieVerwaltung.getVerbindung().getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD).getId();
		ATG_LZD_ID = dieVerwaltung.getVerbindung().getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD).getId();
		
		IVerwaltungMitGuete verwaltungMitGuete = null;
		if(dieVerwaltung instanceof IVerwaltungMitGuete){
			verwaltungMitGuete = (IVerwaltungMitGuete)dieVerwaltung;
		}else{
			throw new RuntimeException("Dieses Modul benötigt Informationen" + //$NON-NLS-1$
					" zum Guetefaktor der angeschlossenen SWE"); //$NON-NLS-1$
		}
			
		for(SystemObject obj:dieVerwaltung.getSystemObjekte()){
			if(obj.getType().getPid().equals(DUAKonstanten.TYP_FAHRSTREIFEN_LZ)){
				lzdFahrStreifen.put(obj, new LzdPLFahrStreifen(verwaltungMitGuete, obj));
			}
			kzdFahrStreifen.put(obj, new KzdPLFahrStreifen(verwaltungMitGuete, obj));
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
					
					AbstraktPLFahrStreifen fahrStreifen = null;
					
					if(resultat.getDataDescription().getAttributeGroup().getId() == ATG_KZD_ID){
						fahrStreifen = this.kzdFahrStreifen.get(resultat.getObject());	
					}else
					if(resultat.getDataDescription().getAttributeGroup().getId() == ATG_LZD_ID){
						fahrStreifen = this.lzdFahrStreifen.get(resultat.getObject());	
					}
					
					Data pData = null;
					if(fahrStreifen != null){
						 pData = fahrStreifen.plausibilisiere(resultat);
					}else{
						LOGGER.warning("Fahrstreifen " + resultat.getObject() +  //$NON-NLS-1$
								" konnte nicht identifiziert werden"); //$NON-NLS-1$
					}

					if(pData != null){
						ResultData ersetztesResultat = new ResultData(
								resultat.getObject(),
								resultat.getDataDescription(),
								resultat.getDataTime(),
								pData);
						weiterzuleitendeResultate.add(ersetztesResultat);
					}else{
						weiterzuleitendeResultate.add(resultat);						
					}
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
		// hier findet keine Publikation statt
	}

}