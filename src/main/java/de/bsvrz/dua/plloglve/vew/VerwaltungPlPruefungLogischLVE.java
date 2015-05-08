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

package de.bsvrz.dua.plloglve.vew;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

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

/**
 * Implementierung des Moduls Verwaltung der SWE Pl-Prüfung logisch LVE. Dieses
 * Modul erfragt die zu überprüfenden Daten aus der Parametrierung und
 * initialisiert damit die Module Pl-Prüfung formal und Pl-Prüfung logisch LVE,
 * die dann die eigentliche Prüfung durchführen.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class VerwaltungPlPruefungLogischLVE extends AbstraktVerwaltungsAdapterMitGuete {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Instanz des Moduls PL-Prüfung formal.
	 */
	private PlPruefungFormal plPruefungFormal = null;

	/**
	 * Instanz des Moduls PL-Prüfung logisch LVE.
	 */
	private PlPruefungLogischLVE plPruefungLogischLVE = null;

	@Override
	public SWETyp getSWETyp() {
		return SWETyp.PL_PRUEFUNG_LOGISCH_LVE;
	}

	@Override
	protected void initialisiere() throws DUAInitialisierungsException {
		super.initialisiere();

		MessageSender.getInstance().setApplicationLabel("PL-Logisch LVE");

		/**
		 * Fuer den Start der Applikation im Testmodus
		 */
		final String test = this.getArgument("test");
		if ((test != null) && (test.length() > 0)) {
			new TestParameter(test);
		}

		String infoStr = ""; //$NON-NLS-1$
		final Collection<SystemObject> plLogLveObjekte = DUAUtensilien.getBasisInstanzen(
				this.verbindung.getDataModel().getType(DUAKonstanten.TYP_FAHRSTREIFEN), this.verbindung,
				this.getKonfigurationsBereiche());
		this.objekte = plLogLveObjekte.toArray(new SystemObject[0]);

		for (final SystemObject obj : this.objekte) {
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		VerwaltungPlPruefungLogischLVE.LOGGER.config("---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$

		this.plPruefungFormal = new PlPruefungFormal(new PPFStandardAspekteVersorger(this).getStandardPubInfos());
		this.plPruefungFormal.setPublikation(true);
		this.plPruefungFormal.initialisiere(this);

		this.plPruefungLogischLVE = new PlPruefungLogischLVE(
				new PlLogLVEStandardAspekteVersorger(this).getStandardPubInfos());
		this.plPruefungLogischLVE.setPublikation(true);
		this.plPruefungLogischLVE.initialisiere(this);

		this.plPruefungFormal.setNaechstenBearbeitungsKnoten(this.plPruefungLogischLVE);

		final DataDescription anmeldungsBeschreibungKZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				this.verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG));
		final DataDescription anmeldungsBeschreibungLZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
				this.verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG));

		this.verbindung.subscribeReceiver(this, this.objekte, anmeldungsBeschreibungKZD, ReceiveOptions.normal(),
				ReceiverRole.receiver());
		for (final SystemObject fsObj : this.objekte) {
			if (fsObj.isOfType(DUAKonstanten.TYP_FAHRSTREIFEN_LZ)) {
				this.verbindung.subscribeReceiver(this, fsObj, anmeldungsBeschreibungLZD, ReceiveOptions.delayed(),
						ReceiverRole.receiver());
			}
		}
	}

	@Override
	public void update(final ResultData[] resultate) {
		this.plPruefungFormal.aktualisiereDaten(resultate);
	}

	/**
	 * Startet diese Applikation.
	 *
	 * @param argumente
	 *            Argumente der Kommandozeile
	 */
	public static void main(final String[] argumente) {
		StandardApplicationRunner.run(new VerwaltungPlPruefungLogischLVE(), argumente);
	}

	/**
	 * Standard-Gütefaktor für Ersetzungen (90%)<br>
	 * Wenn das Modul Pl-Prüfung logisch LVE einen Messwert ersetzt (eigentlich
	 * nur bei Wertebereichsprüfung) so vermindert sich die Güte des
	 * Ausgangswertes um diesen Faktor (wenn kein anderer Wert über die
	 * Kommandozeile übergeben wurde)
	 */
	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}

	/**
	 * Erfragt alle fuer das Logging der Plausibilitaetskontrolle notwendigen
	 * Informationen einenes Datensatzes als Zeichenkette.
	 *
	 * @param originalDatum
	 *            das Originaldatum
	 * @return alle fuer das Logging der Plausibilitaetskontrolle notwendigen
	 *         Informationen einenes Datensatzes als Zeichenkette
	 *
	 *         TODO: in funclib auslagern
	 */
	public static final String getPlLogIdent(final ResultData originalDatum) {
		if (originalDatum != null) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat(DUAKonstanten.ZEIT_FORMAT_GENAU_STR);
			String ident = originalDatum.getObject().getPid() + " (DZ: "
					+ dateFormat.format(new Date(originalDatum.getDataTime())) + "), ["
					+ originalDatum.getDataDescription().getAttributeGroup().getPid() + ", "
					+ originalDatum.getDataDescription().getAspect().getPid() + "]: ";
			if (originalDatum.getData() != null) {
				ident += "Nutzdaten. ";
			} else {
				ident += "!!! keine Nutzdaten !!! ";
			}

			return ident;
		} else {
			return "<<null>> ";
		}
	}

	/**
	 * Erfragt eine Datenkatalog-kompatible Version eines DUA-Wertes als
	 * Zeichenkette.
	 *
	 * @param wert
	 *            ein DUA-Wert
	 * @return eine Datenkatalog-kompatible Version eines DUA-Wertes als
	 *         Zeichenkette
	 *
	 *         TODO: in funclib auslagern
	 */
	public static final String getWertIdent(final long wert) {
		if (wert < 0) {
			if (wert == -1) {
				return "nicht ermittelbar";
			}
			if (wert == -1) {
				return "fehlerhaft";
			}
			if (wert == -1) {
				return "fehlerhaft bzw. nicht ermittelbar";
			}
			return "!!! FEHLER !!!";
		} else {
			return new Long(wert).toString();
		}
	}

}
