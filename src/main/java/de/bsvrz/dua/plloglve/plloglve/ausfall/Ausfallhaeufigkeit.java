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

package de.bsvrz.dua.plloglve.plloglve.ausfall;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.HashMap;
import java.util.Map;

/**
 * Dieses Submodul ueberprueft, ob die parametrierte maximal zulaessige
 * Ausfallhaeufigkeit eines Fahrstreifens pro Tag ueberschritten wurde.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class Ausfallhaeufigkeit extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Mapt FS-Systemobjekte auf Fahrstreifenobjekte mit den für dieses Submodul
	 * notwendigen Informationen.
	 */
	private Map<SystemObject, AusfallFahrStreifen> fahrStreifen = new HashMap<SystemObject, AusfallFahrStreifen>();

	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		for (SystemObject obj : dieVerwaltung.getSystemObjekte()) {
			this.fahrStreifen.put(obj, new AusfallFahrStreifen(dieVerwaltung,
					obj, PlPruefungLogischLVE.getMq(obj)));
		}
	}

	public void aktualisiereDaten(ResultData[] resultate) {
		if (resultate != null) {

			for (ResultData resultat : resultate) {
				if (resultat != null) {
					if (resultat.getDataDescription().getAttributeGroup().getId() == PlPruefungLogischLVE.atgKzdId 
							&& resultat.getData() != null) {
						AusfallFahrStreifen fs = this.fahrStreifen.get(resultat.getObject());

						if (fs != null) {
							fs.plausibilisiere(resultat);
						} else {
							Debug.getLogger()
									.error("Konnte Fahrstreifen zu Datensatz nicht identifizieren:\n" //$NON-NLS-1$
											+ resultat);
						}
					}
				}
			}

			if (this.knoten != null) {
				this.knoten.aktualisiereDaten(resultate);
			}
		}
	}

	public ModulTyp getModulTyp() {
		return null;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert
	}

}
