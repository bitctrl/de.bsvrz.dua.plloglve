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

package de.bsvrz.dua.plloglve.plloglve;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.plloglve.plloglve.ausfall.Ausfallhaeufigkeit;
import de.bsvrz.dua.plloglve.plloglve.diff.DifferenzialKontrolle;
import de.bsvrz.dua.plloglve.plloglve.standard.PlLogischLVEStandard;
import de.bsvrz.dua.plloglve.plloglve.vb.Vertrauensbereich;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;

/**
 * Implementierung des Moduls Pl-Prüfung logisch LVE der SWE Pl-Prüfung logisch
 * LVE. Dieses Modul leitet nur die empfangenen Daten an seine Submudole weiter,
 * welche die eigentliche Plausibilisierung durchführen
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class PlPruefungLogischLVE extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * Startzeit des Moduls Pl-Prüfung logisch LVE.
	 */
	public static final long START_ZEIT = System.currentTimeMillis();

	/**
	 * Submodul Pl-Prüfung logisch LVE standard.
	 */
	private PlLogischLVEStandard standard = new PlLogischLVEStandard();

	/**
	 * Submodul Differenzial-Kontrolle.
	 */
	private DifferenzialKontrolle diff = new DifferenzialKontrolle();

	/**
	 * Submodul Ausfallhaeufigkeit.
	 */
	private Ausfallhaeufigkeit ausfall = new Ausfallhaeufigkeit();

	/**
	 * Submodul Vertrauensbereich.
	 */
	private Vertrauensbereich vb = null;

	/**
	 * Datenverteiler-ID der KZD-Attributgruppe.
	 */
	public static long atgKzdId = -1;

	/**
	 * Datenverteiler-ID der LZD-Attributgruppe.
	 */
	public static long atgLzdId = -1;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param stdAspekte
	 *            Informationen zu den Standardpublikationsaspekten für diese
	 *            Instanz des Moduls Pl-Prüfung logisch LVE
	 */
	public PlPruefungLogischLVE(final IStandardAspekte stdAspekte) {
		this.standardAspekte = stdAspekte;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		MessageSender.getInstance().setApplicationLabel("PL-Logisch LVE");
		
		atgKzdId = dieVerwaltung.getVerbindung().getDataModel()
				.getAttributeGroup(DUAKonstanten.ATG_KZD).getId();
		atgLzdId = dieVerwaltung.getVerbindung().getDataModel()
				.getAttributeGroup(DUAKonstanten.ATG_LZD).getId();

		this.vb = new Vertrauensbereich(this.standardAspekte);

		this.standard.initialisiere(dieVerwaltung);
		this.standard.setNaechstenBearbeitungsKnoten(this.diff);

		this.diff.initialisiere(dieVerwaltung);
		this.diff.setNaechstenBearbeitungsKnoten(this.ausfall);

		this.ausfall.initialisiere(dieVerwaltung);
		this.ausfall.setNaechstenBearbeitungsKnoten(this.vb);

		this.vb.setPublikation(this.publizieren);
		this.vb.initialisiere(dieVerwaltung);
		this.vb.setNaechstenBearbeitungsKnoten(this.knoten);
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
	public void aktualisiereDaten(ResultData[] resultate) {
		this.standard.aktualisiereDaten(resultate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// wird hier nicht benötigt, da die Publikation erst im letzten Submodul
		// "Vertrauensbereich" stattfindet
	}

}
