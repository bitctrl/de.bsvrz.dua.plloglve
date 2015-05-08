/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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

package de.bsvrz.dua.plloglve.plloglve.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.dua.plloglve.vew.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Submodul Differentialkontrolle. Dieses Submodul überprüft, ob die maximal
 * zulässige Anzahl von Intervallen mit Ergebniskonstanz überschritten wurde und
 * generiert ggf. eine Betriebsmeldung
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class DifferenzialKontrolle extends AbstraktBearbeitungsKnotenAdapter {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Map von Fahrtreifen-Systemobjekten auf Objekte mit Konstanzzählern.
	 */
	private final Map<SystemObject, DiffFahrStreifen> fahrStreifen = new HashMap<>();

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung) throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		for (final SystemObject obj : dieVerwaltung.getSystemObjekte()) {
			this.fahrStreifen.put(obj, new DiffFahrStreifen(dieVerwaltung, obj));
		}
	}

	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		if (resultate != null) {
			final Collection<ResultData> weiterzuleitendeResultate = new ArrayList<>();

			for (final ResultData resultat : resultate) {
				if (resultat != null) {

					if (!TestParameter.getInstanz().isTestVertrauen() && !TestParameter.getInstanz().isTestAusfall()) {

						if (resultat.getDataDescription().getAttributeGroup()
								.getId() == PlPruefungLogischLVE.atgKzdId) {
							if (resultat.getData() != null) {
								ResultData resultatNeu = resultat;

								final DiffFahrStreifen fs = this.fahrStreifen.get(resultat.getObject());

								Data data = null;
								if (fs != null) {
									data = fs.plausibilisiere(resultat);
								} else {
									DifferenzialKontrolle.LOGGER
											.error("Fahrstreifen zu Datensatz konnte nicht identifiziert werden:\n" + //$NON-NLS-1$
													resultat);
								}

								if (data != null) {
									resultatNeu = new ResultData(resultat.getObject(), resultat.getDataDescription(),
											resultat.getDataTime(), data, resultat.isDelayedData());
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
					} else {
						weiterzuleitendeResultate.add(resultat);
					}
				}
			}

			if ((this.knoten != null) && !weiterzuleitendeResultate.isEmpty()) {
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}

	@Override
	public ModulTyp getModulTyp() {
		return null;
	}

	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert
	}

}
