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

import java.util.Date;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.dua.plloglve.vew.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IntervallPufferException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;

/**
 * Speichert für einen Bezugszeitraum und ein finales DAV-Attribut des
 * Datensatzes <code>atg.verkehrsDatenKurzZeitIntervall</code> (z.B.
 * <code>qKfz</code>) die ausgefallenen Werte
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class BezugsZeitraum {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	private static IVerwaltung dieVerwaltung = null;

	/**
	 * alle ausgefallenen Datensaetze in diesem Bezugszeitraum.
	 */
	private final VertrauensPuffer ausgefalleneDaten = new VertrauensPuffer();

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
		if (BezugsZeitraum.dieVerwaltung == null) {
			BezugsZeitraum.dieVerwaltung = verwaltung;
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

		/**
		 * Beginne erst mit der Arbeit, wenn ueberhaupt sinnvolle Ergebnisse
		 * errechnet werden koennen
		 */
		if (TestParameter.getInstanz().isTestVertrauen()
				|| ((parameter != null) && parameter.isAuswertbar() && ((System
						.currentTimeMillis() - parameter.getBezugsZeitraum()) > PlPruefungLogischLVE.START_ZEIT))) {
			final VertrauensEinzelDatum neuesAusfallEinzelDatum = new VertrauensEinzelDatum(
					this.name, originalDatum);

			try {
				ausgefalleneDaten.add(neuesAusfallEinzelDatum);
			} catch (final IntervallPufferException e) {
				LOGGER.error(
						"Fehler beim Erweitern des Ausfallpuffers", e); //$NON-NLS-1$
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
			} catch (final IntervallPufferException e) {
				LOGGER.error(
						"Fehler beim Verkleinern des Ausfallpuffers", e); //$NON-NLS-1$
				e.printStackTrace();
			}

			ausfallZeit += ausgefalleneDaten.getAusfall();

			final long bezugsZeitraumInMillis = TestParameter.getInstanz()
					.isTestVertrauen() ? parameter.getBezugsZeitraum()
							* TestParameter.INTERVALL_VB * 60L : parameter
							.getBezugsZeitraum() * Constants.MILLIS_PER_HOUR;
					double ausfallInProzent = 0;
					if (bezugsZeitraumInMillis > 0) {
						ausfallInProzent = ((double) ausfallZeit / (double) bezugsZeitraumInMillis) * 100.0;
					}

					/**
					 * Lauft das Programm schon länger als der Bezugszeitraum groß ist?
					 * Nur dann ist eine Vertrauensbereichsprüfung sinnvoll
					 */
					if ((PlPruefungLogischLVE.START_ZEIT + bezugsZeitraumInMillis) < System
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
								final long stunden = ausfallZeit
										/ (TestParameter.getInstanz().isTestVertrauen() ? TestParameter.INTERVALL_VB * 60L
												: Constants.MILLIS_PER_HOUR);
								final long minuten = (ausfallZeit - (stunden * (TestParameter
										.getInstanz().isTestVertrauen() ? TestParameter.INTERVALL_VB * 60L
												: Constants.MILLIS_PER_HOUR)))
												/ Constants.MILLIS_PER_MINUTE;
								ausfall = new BezugsZeitraumAusfall(
										parameter.getMaxAusfallProBezugsZeitraumAus(),
										ausfallInProzent, stunden, minuten);
							}
						}

						if (this.vertrauensBereichVerletzt) {
							final Date start = new Date(originalDatum.getDataTime()
									- bezugsZeitraumInMillis);
							final Date ende = new Date(originalDatum.getDataTime());
							final long stunden = ausfallZeit
									/ (TestParameter.getInstanz().isTestVertrauen() ? TestParameter.INTERVALL_VB * 60L
											: Constants.MILLIS_PER_HOUR);
							final long minuten = (ausfallZeit - (stunden * (TestParameter
									.getInstanz().isTestVertrauen() ? TestParameter.INTERVALL_VB * 60L
											: Constants.MILLIS_PER_HOUR)))
											/ Constants.MILLIS_PER_MINUTE;

							// Bilde den Text für den Vergleichswert
							String vwert = "";
							if (einschaltSchwelleUEBERschritten) {
								vwert = "" + parameter.getMaxAusfallProBezugsZeitraumEin() + "% (Einschaltschwelle)";
							} else {
								vwert = "" + parameter.getMaxAusfallProBezugsZeitraumAus() + "% (Ausschaltschwelle)";
							}
							
							final String nachricht = "Daten außerhalb des Vertrauensbereichs. Im Zeitraum von " + //$NON-NLS-1$
									DUAKonstanten.BM_ZEIT_FORMAT.format(start)
									+ " Uhr bis " + DUAKonstanten.BM_ZEIT_FORMAT.format(ende) + //$NON-NLS-1$
									" ("
									+ parameter.getBezugsZeitraum()
									+ " Stunde(n)) implausible Fahrstreifenwerte für den Wert " + //$NON-NLS-1$
									this.name
									+ " am Fahrstreifen "
									+ originalDatum.getObject()
									+ " von "
									+ DUAUtensilien.runde(ausfallInProzent, 1)
									+ "% (> " + //$NON-NLS-1$
									vwert
									+ ") entspricht Ausfall von " + stunden + " Stunde(n) " + //$NON-NLS-1$ //$NON-NLS-2$
									minuten
									+ " Minute(n). Fahrstreifenwerte werden auf Implausibel gesetzt."; //$NON-NLS-1$

							DUAUtensilien.sendeBetriebsmeldung(
									BezugsZeitraum.dieVerwaltung.getVerbindung(),
									BezugsZeitraum.dieVerwaltung.getBmvIdKonverter(),
									MessageGrade.WARNING, originalDatum.getObject(),
									nachricht);
						}
					}
		}

		return ausfall;
	}
}
