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

package de.bsvrz.dua.plloglve.plloglve.diff;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Submodul Differentialkontrolle. Dieses Submodul überprüft, ob die maximal
 * zulässige Anzahl von Intervallen mit Ergebniskonstanz überschritten wurde und
 * generiert ggf. eine Betriebsmeldung
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class DifferenzialKontrolle extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Map von Fahrtreifen-Systemobjekten auf Objekte mit Konstanzzählern.
	 */
	private Map<SystemObject, DiffFahrStreifen> fahrStreifen = new HashMap<SystemObject, DiffFahrStreifen>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		for (SystemObject obj : dieVerwaltung.getSystemObjekte()) {
			this.fahrStreifen
					.put(obj, new DiffFahrStreifen(dieVerwaltung, obj, PlPruefungLogischLVE.getMq(obj)));
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

					if (resultat.getDataDescription().getAttributeGroup()
							.getId() == PlPruefungLogischLVE.atgKzdId) {
						if (resultat.getData() != null) {
							ResultData resultatNeu = resultat;

							DiffFahrStreifen fs = this.fahrStreifen
									.get(resultat.getObject());

							Data data = null;
							if (fs != null) {
								data = fs.plausibilisiere(resultat);
							} else {
								Debug.getLogger()
										.error("Fahrstreifen zu Datensatz konnte nicht identifiziert werden:\n" + //$NON-NLS-1$
												resultat);
							}

							if (data != null) {
								resultatNeu = new ResultData(resultat
										.getObject(), resultat
										.getDataDescription(), resultat
										.getDataTime(), data, resultat
										.isDelayedData());
							}

							weiterzuleitendeResultate.add(resultatNeu);
						} else {
							weiterzuleitendeResultate.add(resultat);
						}
					} else {
						/**
						 * LZ-Datum empfangen. Dieses wird nicht einer
						 * Differentialkontrolle unterzogen
						 */
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
		// hier wird nicht publiziert
	}

}
