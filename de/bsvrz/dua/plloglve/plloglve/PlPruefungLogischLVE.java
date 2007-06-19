/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Plausibilitätsprüfung logisch LVE
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

import stauma.dav.clientside.ResultData;
import de.bsvrz.dua.plloglve.plloglve.ausfall.Ausfallhaeufigkeit;
import de.bsvrz.dua.plloglve.plloglve.diff.DifferenzialKontrolle;
import de.bsvrz.dua.plloglve.plloglve.standard.PlLogischLVEStandard;
import de.bsvrz.dua.plloglve.plloglve.vb.Vertrauensbereich;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IStandardAspekte;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Implementierung des Moduls Pl-Prüfung logisch LVE der SWE Pl-Prüfung logisch LVE.
 * Dieses Modul leitet nur die empfangenen Daten an seine Submudole weiter, welche
 * die eigentliche Plausibilisierung durchführen 
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class PlPruefungLogischLVE
extends AbstraktBearbeitungsKnotenAdapter{

	/**
	 * Submodul Pl-Prüfung logisch LVE standard
	 */
	private PlLogischLVEStandard standard = new PlLogischLVEStandard();

	/**
	 * Submodul Differenzial-Kontrolle
	 */
	private DifferenzialKontrolle diff = new DifferenzialKontrolle();
	
	/**
	 * Submodul Ausfallhaeufigkeit
	 */
	private Ausfallhaeufigkeit ausfall = new Ausfallhaeufigkeit();
	
	/**
	 * Submodul Vertrauensbereich
	 */	
	private Vertrauensbereich vb = new Vertrauensbereich();
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param stdAspekte Informationen zu den
	 * Standardpublikationsaspekten für diese
	 * Instanz des Moduls Pl-Prüfung logisch LVE
	 */
	public PlPruefungLogischLVE(final IStandardAspekte stdAspekte){
		if(stdAspekte != null){
			this.standardAspekte = stdAspekte;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
	
		this.standard.initialisiere(dieVerwaltung);
		this.standard.setNaechstenBearbeitungsKnoten(this.diff);
		
		this.diff.initialisiere(dieVerwaltung);
		this.diff.setNaechstenBearbeitungsKnoten(this.ausfall);
				
		this.ausfall.initialisiere(dieVerwaltung);
		this.ausfall.setNaechstenBearbeitungsKnoten(this.vb);
		
		this.vb.initialisiere(dieVerwaltung);
		this.vb.setNaechstenBearbeitungsKnoten(this.knoten);
		this.vb.setPublikation(this.publizieren);		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return ModulTyp.PL_PRUEFUNG_LOGISCH_LVE;
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
