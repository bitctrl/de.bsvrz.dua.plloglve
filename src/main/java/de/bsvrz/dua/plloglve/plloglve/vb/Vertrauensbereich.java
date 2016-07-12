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

package de.bsvrz.dua.plloglve.plloglve.vb;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dieses Submodul ueberwacht die Einschalt- bzw. Ausschaltschwelle für den
 * Vertrauensbereich eines Fahrstreifens im Bezugszeitraum. Beim Betreten bzw.
 * Verlassen des Vertrauensbereichs wird eine entsprechende Meldung generiert.
 * Darueber hinaus findet innerhalb dieses Submoduls ggf. die Publikation aller
 * empfangenen Daten statt.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class Vertrauensbereich extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Map von Systemobjekten auf Fahrstreifenobjekte mit Informationen zu den
	 * jeweiligen Vertrauensbereichsverletzungen.
	 */
	private Map<SystemObject, VertrauensFahrStreifen> fahrStreifenMap = new HashMap<SystemObject, VertrauensFahrStreifen>();

	/**
	 * Standardkonstruktor.
	 * 
	 * @param stdAspekte
	 *            Informationen zu den Standardpublikationsaspekten für dieses
	 *            Modul
	 */
	public Vertrauensbereich(final IStandardAspekte stdAspekte) {
		this.standardAspekte = stdAspekte;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		if (this.publizieren) {
			this.publikationsAnmeldungen
					.modifiziereObjektAnmeldung(this.standardAspekte
							.getStandardAnmeldungen(this.verwaltung
									.getSystemObjekte()));
		}

		for (SystemObject fsObj : dieVerwaltung.getSystemObjekte()) {
			this.fahrStreifenMap.put(fsObj, new VertrauensFahrStreifen(
					dieVerwaltung, fsObj, PlPruefungLogischLVE.getMq(fsObj)));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if (resultate != null) {

			List<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();

			for (ResultData resultat : resultate) {
				if (resultat != null) {
					Data datum = null;

					if (resultat.getDataDescription().getAttributeGroup()
							.getId() == PlPruefungLogischLVE.atgKzdId) {
						if (resultat.getData() != null) {
							VertrauensFahrStreifen fs = this.fahrStreifenMap
									.get(resultat.getObject());

							if (fs != null) {
								datum = fs.plausibilisiere(resultat);
							} else {
								Debug.getLogger()
										.warning("Datum fuer nicht identifizierbaren Fahrstreifen empfangen: " + //$NON-NLS-1$
												resultat.getObject());
							}
						}
					} else {
						datum = resultat.getData();
					}

					ResultData publikationsDatum = new ResultData(resultat
							.getObject(), new DataDescription(resultat
							.getDataDescription().getAttributeGroup(),
							standardAspekte.getStandardAspekt(resultat)), resultat.getDataTime(), datum, resultat
							.isDelayedData());
					ResultData weiterzuleitendesDatum = new ResultData(resultat
							.getObject(), resultat.getDataDescription(),
							resultat.getDataTime(), datum, resultat
									.isDelayedData());

					if (this.publizieren) {
						this.publikationsAnmeldungen.sende(publikationsDatum);
					}

					weiterzuleitendeResultate.add(weiterzuleitendesDatum);
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
		// Datenflusssteuerung ist hier nicht dynamisch
	}

}
