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

package de.bsvrz.dua.plloglve.plloglve.ausfall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
 * Dieses Submodul ueberprueft, ob die parametrierte maximal zulaessige
 * Ausfallhaeufigkeit eines Fahrstreifens pro Tag ueberschritten wurde.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class Ausfallhaeufigkeit extends AbstraktBearbeitungsKnotenAdapter {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Mapt FS-Systemobjekte auf Fahrstreifenobjekte mit den für dieses Submodul
	 * notwendigen Informationen.
	 */
	private final Map<SystemObject, AusfallFahrStreifen> fahrStreifen = new HashMap<>();

	@Override
	public void initialisiere(final IVerwaltung dieVerwaltung) throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);

		for (final SystemObject obj : dieVerwaltung.getSystemObjekte()) {
			this.fahrStreifen.put(obj, new AusfallFahrStreifen(dieVerwaltung, obj));
		}
	}

	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		if (resultate != null) {
			final Collection<ResultData> weiterzuleitendeResultate = new ArrayList<>();

			for (final ResultData resultat : resultate) {
				if (resultat != null) {

					if (!TestParameter.getInstanz().isTestVertrauen()) {
						if ((resultat.getDataDescription().getAttributeGroup().getId() == PlPruefungLogischLVE.atgKzdId)
								&& (resultat.getData() != null)) {
							final AusfallFahrStreifen fs = this.fahrStreifen.get(resultat.getObject());

							if (fs != null) {
								fs.plausibilisiere(resultat);
							} else {
								Ausfallhaeufigkeit.LOGGER
										.error("Konnte Fahrstreifen zu Datensatz nicht identifizieren:\n" //$NON-NLS-1$
												+ resultat);
							}
						}
					}

					weiterzuleitendeResultate.add(resultat);
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
