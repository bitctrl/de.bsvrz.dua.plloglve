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

package de.bsvrz.dua.plloglve.plloglve.vb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.dua.plloglve.vew.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Dieses Submodul ueberwacht die Einschalt- bzw. Ausschaltschwelle für den
 * Vertrauensbereich eines Fahrstreifens im Bezugszeitraum. Beim Betreten bzw.
 * Verlassen des Vertrauensbereichs wird eine entsprechende Meldung generiert.
 * Darueber hinaus findet innerhalb dieses Submoduls ggf. die Publikation aller
 * empfangenen Daten statt.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class Vertrauensbereich extends AbstraktBearbeitungsKnotenAdapter {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Map von Systemobjekten auf Fahrstreifenobjekte mit Informationen zu den
	 * jeweiligen Vertrauensbereichsverletzungen.
	 */
	private final Map<SystemObject, VertrauensFahrStreifen> fahrStreifenMap = new HashMap<>();

	/**
	 * Standardkonstruktor.
	 *
	 * @param stdAspekte
	 *            Informationen zu den Standardpublikationsaspekten für dieses
	 *            Modul
	 */
	public Vertrauensbereich(final IStandardAspekte stdAspekte) {
		setStandardAspekte(stdAspekte);
	}

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung) throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		if (isPublizieren()) {
			getPublikationsAnmeldungen().modifiziereObjektAnmeldung(
					getStandardAspekte().getStandardAnmeldungen(getVerwaltung().getSystemObjekte()));
		}

		for (final SystemObject fsObj : dieVerwaltung.getSystemObjekte()) {
			this.fahrStreifenMap.put(fsObj, new VertrauensFahrStreifen(dieVerwaltung, fsObj));
		}
	}

	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		if (resultate != null) {

			final List<ResultData> weiterzuleitendeResultate = new ArrayList<>();

			for (final ResultData resultat : resultate) {
				if (resultat != null) {
					Data datum = null;

					if (!TestParameter.getInstanz().isTestAusfall()) {
						if (resultat.getDataDescription().getAttributeGroup()
								.getId() == PlPruefungLogischLVE.atgKzdId) {
							if (resultat.getData() != null) {
								final VertrauensFahrStreifen fs = this.fahrStreifenMap.get(resultat.getObject());

								if (fs != null) {
									datum = fs.plausibilisiere(resultat);
								} else {
									Vertrauensbereich.LOGGER
									.warning("Datum fuer nicht identifizierbaren Fahrstreifen empfangen: " + //$NON-NLS-1$
											resultat.getObject());
								}
							}
						} else {
							datum = resultat.getData();
						}
					} else {
						datum = resultat.getData();
					}

					final ResultData publikationsDatum = new ResultData(resultat.getObject(),
							new DataDescription(resultat.getDataDescription().getAttributeGroup(),
									getStandardAspekte().getStandardAspekt(resultat)),
							resultat.getDataTime(), datum, resultat.isDelayedData());
					final ResultData weiterzuleitendesDatum = new ResultData(resultat.getObject(),
							resultat.getDataDescription(), resultat.getDataTime(), datum, resultat.isDelayedData());

					if (isPublizieren()) {
						getPublikationsAnmeldungen().sende(publikationsDatum);
					}

					weiterzuleitendeResultate.add(weiterzuleitendesDatum);
				}
			}

			if ((getKnoten() != null) && !weiterzuleitendeResultate.isEmpty()) {
				getKnoten().aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}

	@Override
	public ModulTyp getModulTyp() {
		return null;
	}

	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// Datenflusssteuerung ist hier nicht dynamisch
	}
}
