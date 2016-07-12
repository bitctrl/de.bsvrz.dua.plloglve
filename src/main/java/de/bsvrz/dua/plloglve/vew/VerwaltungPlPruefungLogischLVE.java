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

package de.bsvrz.dua.plloglve.vew;

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plformal.plformal.PlPruefungFormal;
import de.bsvrz.dua.plformal.vew.PPFStandardAspekteVersorger;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapterMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;

import java.util.Collection;

/**
 * Implementierung des Moduls Verwaltung der SWE Pl-Prüfung logisch LVE. Dieses
 * Modul erfragt die zu überprüfenden Daten aus der Parametrierung und
 * initialisiert damit die Module Pl-Prüfung formal und Pl-Prüfung logisch LVE,
 * die dann die eigentliche Prüfung durchführen.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class VerwaltungPlPruefungLogischLVE extends
		AbstraktVerwaltungsAdapterMitGuete {

	/**
	 * Instanz des Moduls PL-Prüfung formal.
	 */
	private PlPruefungFormal plPruefungFormal = null;

	/**
	 * Instanz des Moduls PL-Prüfung logisch LVE.
	 */
	private PlPruefungLogischLVE plPruefungLogischLVE = null;

	/**
	 * {@inheritDoc}
	 */
	public SWETyp getSWETyp() {
		return SWETyp.PL_PRUEFUNG_LOGISCH_LVE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere() throws DUAInitialisierungsException {
		super.initialisiere();
		
		MessageSender.getInstance().setApplicationLabel("PL-Logisch LVE");

		String infoStr = ""; //$NON-NLS-1$
		Collection<SystemObject> plLogLveObjekte = DUAUtensilien
				.getBasisInstanzen(this.verbindung.getDataModel().getType(
						DUAKonstanten.TYP_FAHRSTREIFEN), this.verbindung, this
						.getKonfigurationsBereiche());
		this.objekte = plLogLveObjekte.toArray(new SystemObject[0]);

		for (SystemObject obj : this.objekte) {
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		Debug.getLogger().config(
				"---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$

		this.plPruefungFormal = new PlPruefungFormal(
				new PPFStandardAspekteVersorger(this).getStandardPubInfos());
		this.plPruefungFormal.setPublikation(true);
		this.plPruefungFormal.initialisiere(this);

		boolean verarbeiteLangzeitdaten = isVerarbeiteLangzeitdaten();
		this.plPruefungLogischLVE = new PlPruefungLogischLVE(
				verarbeiteLangzeitdaten ?
						new PlLogLVEStandardAspekteVersorger(this).getStandardPubInfos()
						: new PlLogLVEStandardAspekteVersorgerKurzZeit(this).getStandardPubInfos());
		this.plPruefungLogischLVE.setPublikation(true);
		this.plPruefungLogischLVE.initialisiere(this);

		this.plPruefungFormal
				.setNaechstenBearbeitungsKnoten(this.plPruefungLogischLVE);

		DataDescription anmeldungsBeschreibungKZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(
						DUAKonstanten.ATG_KZD), this.verbindung.getDataModel()
						.getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG));
		DataDescription anmeldungsBeschreibungLZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(
						DUAKonstanten.ATG_LZD), this.verbindung.getDataModel()
						.getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG));

		this.verbindung.subscribeReceiver(this, this.objekte,
				anmeldungsBeschreibungKZD, ReceiveOptions.normal(),
				ReceiverRole.receiver());
		for (SystemObject fsObj : this.objekte) {
			if (verarbeiteLangzeitdaten && fsObj.isOfType(DUAKonstanten.TYP_FAHRSTREIFEN_LZ)) {
				this.verbindung.subscribeReceiver(this, fsObj,
						anmeldungsBeschreibungLZD, ReceiveOptions.delayed(),
						ReceiverRole.receiver());
			}
		}
	}


	private boolean isVerarbeiteLangzeitdaten() {
		final String arg = getArgument("ignoriereLangzeitdaten");
		if (arg == null) {
			return true;
		}

		final boolean ignoriereLangzeitdaten = Boolean.parseBoolean(arg);
		return !ignoriereLangzeitdaten;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		this.plPruefungFormal.aktualisiereDaten(resultate);
	}

	/**
	 * Startet diese Applikation.
	 * 
	 * @param argumente
	 *            Argumente der Kommandozeile
	 */
	public static void main(String[] argumente) {
		StandardApplicationRunner.run(new VerwaltungPlPruefungLogischLVE(),
				argumente);
	}

	/**
	 * {@inheritDoc}.<br>
	 * 
	 * Standard-Gütefaktor für Ersetzungen (90%)<br>
	 * Wenn das Modul Pl-Prüfung logisch LVE einen Messwert ersetzt (eigentlich
	 * nur bei Wertebereichsprüfung) so vermindert sich die Güte des
	 * Ausgangswertes um diesen Faktor (wenn kein anderer Wert über die
	 * Kommandozeile übergeben wurde)
	 */
	@Override
	public double getStandardGueteFaktor() {
		return 0.8;
	}

}
