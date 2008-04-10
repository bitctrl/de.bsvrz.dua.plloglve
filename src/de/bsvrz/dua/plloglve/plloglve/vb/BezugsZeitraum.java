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

package de.bsvrz.dua.plloglve.plloglve.vb;

import java.util.Date;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.dua.plloglve.vew.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IntervallPufferException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageState;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;

/**
 * Speichert für einen Bezugszeitraum und ein finales DAV-Attribut des
 * Datensatzes <code>atg.verkehrsDatenKurzZeitIntervall</code> (z.B.
 * <code>qKfz</code>) die ausgefallenen Werte
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class BezugsZeitraum {

	/**
	 * Debug-Logger.
	 */
	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	private static IVerwaltung dieVerwaltung = null;

	/**
	 * alle ausgefallenen Datensaetze in diesem Bezugszeitraum.
	 */
	private VertrauensPuffer ausgefalleneDaten = new VertrauensPuffer();

	/**
	 * Name des finalen DAV-Attributs, für den Werte in diesem Bezugszeitraum
	 * gespeichert werden (z.B. <code>qKfz</code>).
	 */
	private String name = null;

	/**
	 * aktueller Zustand der Vertrauensbereichsverletzung für dieses Datum.
	 */
	private boolean vertrauensBereichVerletzt = false;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param name
	 *            name Name des finalen DAV-Attributs, für den Werte in diesem
	 *            Bezugszeitraum gespeichert werden sollen (z.B.
	 *            <code>qKfz</code>)
	 */
	protected BezugsZeitraum(final IVerwaltung verwaltung, final String name) {
		if (dieVerwaltung == null) {
			dieVerwaltung = verwaltung;
		}
		this.name = name;
	}

	/**
	 * Erfragt, ob im Moment der Vertrauensbereich verletzt ist. Dies ist der
	 * Fall, wenn die Einschaltschwelle von einem Datum überschritten, und von
	 * allen späteren Daten (bis jetzt) die Ausschaltschwelle nicht
	 * unterschritten wurde.
	 * 
	 * @return ob im Moment der Vertrauensbereich verletzt ist
	 */
	protected final boolean isVertrauensBereichVerletzt() {
		return this.vertrauensBereichVerletzt;
	}

	/**
	 * Aktualisiert diese Datenstruktur mit einem aktuellen Kurzzeitdatum und
	 * den aktuellen Parametern. Errechnet anhand der übergebenen Parameter, ob
	 * der Vertrauensbereich (immernoch, nicht mehr) verletzt ist und gibt ggf.
	 * eine Betriebsmeldung aus
	 * 
	 * @param originalDatum
	 *            ein KZD eines Fahrstreifens
	 * @param parameter
	 *            die aktuellen Parameter des Vertrauensbereichs für diesen
	 *            Fahrstreifen
	 * @return der aktuelle Ausfall dieses Attributs im Bezugszeitraum, oder
	 *         <code>null</code> wenn dieser nicht ermittelt werden konnte
	 */
	protected final BezugsZeitraumAusfall ermittleAusfall(
			final ResultData originalDatum,
			final AtgVerkehrsDatenVertrauensBereichFs parameter) {
		BezugsZeitraumAusfall ausfall = new BezugsZeitraumAusfall(0, 0, 0, 0);

		VertrauensEinzelDatum neuesAusfallEinzelDatum = new VertrauensEinzelDatum(
				this.name, originalDatum);

		try {
			ausgefalleneDaten.add(neuesAusfallEinzelDatum);
		} catch (IntervallPufferException e) {
			LOGGER.error("Fehler beim Erweitern des Ausfallpuffers", e); //$NON-NLS-1$
			e.printStackTrace();
		}

		long ausfallZeit = 0;
		try {
			if (TestParameter.getInstanz().isTestVertrauen()) {
				/**
				 * Ein Tag ist jetzt genau 144s lang
				 */
				this.ausgefalleneDaten
						.loescheAllesUnterhalbVon(neuesAusfallEinzelDatum
								.getIntervallEnde()
								- (parameter.getBezugsZeitraum()
										* TestParameter.INTERVALL_VB * 60L));
			} else {
				this.ausgefalleneDaten
						.loescheAllesUnterhalbVon(neuesAusfallEinzelDatum
								.getIntervallEnde()
								- (parameter.getBezugsZeitraum() * Constants.MILLIS_PER_HOUR));
			}
		} catch (IntervallPufferException e) {
			LOGGER.error("Fehler beim Verkleinern des Ausfallpuffers", e); //$NON-NLS-1$
			e.printStackTrace();
		}

		ausfallZeit += ausgefalleneDaten.getAusfall();

		final long bezugsZeitraumInMillis = TestParameter.getInstanz()
				.isTestVertrauen() ? parameter.getBezugsZeitraum()
				* TestParameter.INTERVALL_VB * 60L : parameter
				.getBezugsZeitraum()
				* Constants.MILLIS_PER_HOUR;
		double ausfallInProzent = 0;
		if (bezugsZeitraumInMillis > 0) {
			ausfallInProzent = (double) (((double) ausfallZeit / (double) bezugsZeitraumInMillis) * 100.0);
		}

		/**
		 * Lauft das Programm schon länger als der Bezugszeitraum groß ist? Nur
		 * dann ist eine Vertrauensbereichsprüfung sinnvoll
		 */
		if (PlPruefungLogischLVE.START_ZEIT + bezugsZeitraumInMillis < System
				.currentTimeMillis()) {
			boolean einschaltSchwelleUEBERschritten = false;
			boolean ausschaltSchwelleUNTERSchritten = false;

			if (ausfallInProzent > parameter
					.getMaxAusfallProBezugsZeitraumEin()) {
				einschaltSchwelleUEBERschritten = true;
			}
			if (ausfallInProzent < parameter
					.getMaxAusfallProBezugsZeitraumAus()) {
				ausschaltSchwelleUNTERSchritten = true;
			}

			if (einschaltSchwelleUEBERschritten) {
				if (!this.vertrauensBereichVerletzt) {
					this.vertrauensBereichVerletzt = true;
				}
			}

			if (ausschaltSchwelleUNTERSchritten) {
				if (this.vertrauensBereichVerletzt) {
					this.vertrauensBereichVerletzt = false;
					long stunden = ausfallZeit
							/ (TestParameter.getInstanz().isTestVertrauen() ? TestParameter.INTERVALL_VB * 60L
									: Constants.MILLIS_PER_HOUR);
					long minuten = (ausfallZeit - (stunden * (TestParameter
							.getInstanz().isTestVertrauen() ? TestParameter.INTERVALL_VB * 60L
							: Constants.MILLIS_PER_HOUR)))
							/ Constants.MILLIS_PER_MINUTE;
					ausfall = new BezugsZeitraumAusfall(parameter
							.getMaxAusfallProBezugsZeitraumAus(),
							ausfallInProzent, stunden, minuten);
				}
			}

			if (this.vertrauensBereichVerletzt) {
				Date start = new Date(originalDatum.getDataTime()
						- bezugsZeitraumInMillis);
				Date ende = new Date(originalDatum.getDataTime());
				long stunden = ausfallZeit
						/ (TestParameter.getInstanz().isTestVertrauen() ? TestParameter.INTERVALL_VB * 60L
								: Constants.MILLIS_PER_HOUR);
				long minuten = (ausfallZeit - (stunden * (TestParameter
						.getInstanz().isTestVertrauen() ? TestParameter.INTERVALL_VB * 60L
						: Constants.MILLIS_PER_HOUR)))
						/ Constants.MILLIS_PER_MINUTE;

				String nachricht = "Daten außerhalb des Vertrauensbereichs. Im Zeitraum von " + //$NON-NLS-1$
						DUAKonstanten.BM_ZEIT_FORMAT.format(start)
						+ " Uhr bis " + DUAKonstanten.BM_ZEIT_FORMAT.format(ende) + //$NON-NLS-1$ 
						" ("
						+ parameter.getBezugsZeitraum()
						+ " Stunde(n)) implausible Fahrstreifenwerte für den Wert " + //$NON-NLS-1$ //$NON-NLS-2$
						this.name
						+ " am Fahrstreifen " + originalDatum.getObject() + " von " + ausfallInProzent + "% (> " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						parameter.getMaxAusfallProBezugsZeitraumEin()
						+ "%) entspricht Ausfall von " + stunden + " Stunde(n) " + //$NON-NLS-1$ //$NON-NLS-2$
						minuten
						+ " Minute(n). Fahrstreifenwerte werden auf Implausibel gesetzt."; //$NON-NLS-1$

				dieVerwaltung.sendeBetriebsMeldung(
						"Vertrauensbereichsprüfung", //$NON-NLS-1$
						MessageType.APPLICATION_DOMAIN,
						"", //$NON-NLS-1$
						MessageGrade.WARNING, MessageState.NEW_MESSAGE,
						nachricht);
			}
		}

		return ausfall;
	}
}
