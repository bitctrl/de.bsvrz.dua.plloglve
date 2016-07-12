/* 
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Pl-Prüfung logisch LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.plloglve.
 * 
 * de.bsvrz.dua.plloglve is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.plloglve is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.plloglve.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.plloglve.plloglve.standard;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.DuaVerkehrsNetz;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Das Submodul PL-Prüfung logisch LVE standard führt zunächst eine
 * Wertebereichsprüfung für die empfangenen Datensätzen durch. Diese orientiert
 * sich an den dazu vorgesehenen Parametern. Folgende Attributarten werden
 * untersucht:<br>
 * - Verkehrsstärken,<br>
 * - Mittlere Geschwindigkeiten,<br>
 * - Belegungen,<br>
 * - Standardabweichungen, etc..<br>
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class PlLogischLVEStandard extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Mapt alle FS-Systemobjekte auf für die Standardplausbibilisierung für KZD
	 * parametrierte Fahrstreifenobjekte.
	 */
	private Map<SystemObject, PLFahrStreifen> _fahrStreifen = new HashMap<>();
	
	/**
	 * {@inheritDoc}
	 * 
	 * Es wird fuer alle Fahrstreifen (Systemobjekte vom Typ
	 * <code>typ.fahrStreifen</code>) eine Instanz der Klasse
	 * <code>PLFahrStreifen</code> erstellt.
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		/*
		 * DUA-Verkehrs-Netz initialisieren
		 */
		DuaVerkehrsNetz.initialisiere(dieVerwaltung.getVerbindung());

		for (SystemObject obj : dieVerwaltung.getSystemObjekte()) {
			_fahrStreifen.put(obj, new PLFahrStreifen(dieVerwaltung, obj, PlPruefungLogischLVE.getMq(obj)));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if (resultate != null) {
			Collection<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();
			for (ResultData resultat : resultate) {
				if (resultat != null) {

					PLFahrStreifen fahrStreifen = null;

					if(resultat.getDataDescription().getAttributeGroup()
							.getId() != PlPruefungLogischLVE.atgKzdId) {
						
						// Langzeitdaten nur weiterleiten
						weiterzuleitendeResultate.add(resultat);
						continue;
					}
					
					/**
					 * Es wurden KZD empfangen
					 */
					fahrStreifen = this._fahrStreifen.get(resultat.getObject());
					Data pData = null;
					if(fahrStreifen != null) {
						pData = fahrStreifen.plausibilisiere(resultat);
					}
					else {
						Debug.getLogger()
								.warning(
										"Fahrstreifen " + resultat.getObject() + //$NON-NLS-1$
												" konnte nicht identifiziert werden"); //$NON-NLS-1$
					}

					if(pData != null) {
						ResultData ersetztesResultat = new ResultData(
								resultat.getObject(),
								resultat.getDataDescription(),
								resultat.getDataTime(), pData,
								resultat.isDelayedData()
						);
						weiterzuleitendeResultate.add(ersetztesResultat);
					}
					else {
						weiterzuleitendeResultate.add(resultat);
					}
				}
			}

			if (this.knoten != null && !weiterzuleitendeResultate.isEmpty()) {
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate
						.toArray(new ResultData[0]));
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
