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
import java.util.SortedSet;
import java.util.TreeSet;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.vew.TestParameter;
import de.bsvrz.dua.plloglve.vew.VerwaltungPlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;

/**
 * Repräsentiert einen Fahrstreifen mit allen Informationen, die zur Ermittlung
 * des Vertrauens in diesen Fahrstreifen notwendig sind.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public class VertrauensFahrStreifen implements ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Alle Attribute, die ggf. auf implausibel gesetzt werden müssen
	 */
	private static final String[] ATTRIBUTE = new String[] {
		"qKfz", "qLkw", "qPkw", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"vKfz", "vLkw", "vPkw", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"b", "tNetto", "sKfz", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	"vgKfz" }; //$NON-NLS-1$

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	protected static IVerwaltung dieVerwaltung = null;

	/**
	 * Datenbeschreibung für Parameterattributgruppe
	 * <code>atg.verkehrsDatenVertrauensBereichFs</code>.
	 */
	protected static DataDescription paraVertrauenDD = null;

	/**
	 * aktuelle Parameter der Attributgruppe
	 * <code>atg.verkehrsDatenVertrauensBereichFs</code> für diesen
	 * Fahrstreifen.
	 */
	private AtgVerkehrsDatenVertrauensBereichFs parameter = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums für
	 * <code>qKfz</code>.
	 */
	private BezugsZeitraum datenBezugsZeitraumQKfz = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums für
	 * <code>qLkw</code>.
	 */
	private BezugsZeitraum datenBezugsZeitraumQLkw = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums für
	 * <code>qPkw</code>.
	 */
	private BezugsZeitraum datenBezugsZeitraumQPkw = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums für
	 * <code>vKfz</code>.
	 */
	private BezugsZeitraum datenBezugsZeitraumVKfz = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums für
	 * <code>vLkw</code>.
	 */
	private BezugsZeitraum datenBezugsZeitraumVLkw = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums für
	 * <code>vPkw</code>.
	 */
	private BezugsZeitraum datenBezugsZeitraumVPkw = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums für
	 * <code>sKfz</code>.
	 */
	private BezugsZeitraum datenBezugsZeitraumSKfz = null;

	/**
	 * Datensätze mit Ausfallinformationen des aktuellen Bezugszeitraums für
	 * <code>b</code>.
	 */
	private BezugsZeitraum datenBezugsZeitraumB = null;

	/**
	 * zeigt an, ob der Vertrauensbereich für diesen Fahrstreifen durch
	 * irgendein Attribut verletzt ist.
	 */
	private boolean vertrauenVerletztAllgemein = false;

	/**
	 * das Objekt.
	 */
	private final SystemObject objekt;

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param objekt
	 *            das mit einem Fahrstreifen assoziierte Systemobjekt
	 */
	protected VertrauensFahrStreifen(final IVerwaltung verwaltung,
			final SystemObject objekt) {
		this.objekt = objekt;

		if (VertrauensFahrStreifen.dieVerwaltung == null) {
			VertrauensFahrStreifen.dieVerwaltung = verwaltung;
			VertrauensFahrStreifen.paraVertrauenDD = new DataDescription(
					VertrauensFahrStreifen.dieVerwaltung
							.getVerbindung()
							.getDataModel()
							.getAttributeGroup(
									"atg.verkehrsDatenVertrauensBereichFs"), //$NON-NLS-1$
					VertrauensFahrStreifen.dieVerwaltung.getVerbindung()
							.getDataModel()
					.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL));
		}

		datenBezugsZeitraumQKfz = new BezugsZeitraum(
				VertrauensFahrStreifen.dieVerwaltung, "qKfz"); //$NON-NLS-1$
		datenBezugsZeitraumQLkw = new BezugsZeitraum(
				VertrauensFahrStreifen.dieVerwaltung, "qLkw"); //$NON-NLS-1$
		datenBezugsZeitraumQPkw = new BezugsZeitraum(
				VertrauensFahrStreifen.dieVerwaltung, "qPkw"); //$NON-NLS-1$
		datenBezugsZeitraumVKfz = new BezugsZeitraum(
				VertrauensFahrStreifen.dieVerwaltung, "vKfz"); //$NON-NLS-1$
		datenBezugsZeitraumVLkw = new BezugsZeitraum(
				VertrauensFahrStreifen.dieVerwaltung, "vLkw"); //$NON-NLS-1$
		datenBezugsZeitraumVPkw = new BezugsZeitraum(
				VertrauensFahrStreifen.dieVerwaltung, "vPkw"); //$NON-NLS-1$
		datenBezugsZeitraumSKfz = new BezugsZeitraum(
				VertrauensFahrStreifen.dieVerwaltung, "sKfz"); //$NON-NLS-1$
		datenBezugsZeitraumB = new BezugsZeitraum(
				VertrauensFahrStreifen.dieVerwaltung, "b"); //$NON-NLS-1$

		VertrauensFahrStreifen.dieVerwaltung.getVerbindung().subscribeReceiver(
				this, objekt, VertrauensFahrStreifen.paraVertrauenDD,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	/**
	 * Führt eine Plausibilisierung des übergebenen Originaldatums durch und
	 * gibt ggf. eine Betriebsmeldung aus
	 *
	 * @param originalDatum
	 *            ein DAV-Originaldatum
	 * @return das plausibilisierte Datum oder unveränderte Originaldatum, wenn
	 *         das emfangene Originaldatum nicht verändert werden musste
	 */
	protected final Data plausibilisiere(final ResultData originalDatum) {
		Data copy = originalDatum.getData();

		synchronized (this) {
			if ((parameter != null) && parameter.isAuswertbar()) {

				final SortedSet<BezugsZeitraumAusfall> ausfallErgebnisse = new TreeSet<BezugsZeitraumAusfall>();

				ausfallErgebnisse.add(datenBezugsZeitraumQKfz.ermittleAusfall(
						originalDatum, parameter));
				ausfallErgebnisse.add(datenBezugsZeitraumQLkw.ermittleAusfall(
						originalDatum, parameter));
				ausfallErgebnisse.add(datenBezugsZeitraumQPkw.ermittleAusfall(
						originalDatum, parameter));
				ausfallErgebnisse.add(datenBezugsZeitraumVKfz.ermittleAusfall(
						originalDatum, parameter));
				ausfallErgebnisse.add(datenBezugsZeitraumVLkw.ermittleAusfall(
						originalDatum, parameter));
				ausfallErgebnisse.add(datenBezugsZeitraumVPkw.ermittleAusfall(
						originalDatum, parameter));
				ausfallErgebnisse.add(datenBezugsZeitraumSKfz.ermittleAusfall(
						originalDatum, parameter));
				ausfallErgebnisse.add(datenBezugsZeitraumB.ermittleAusfall(
						originalDatum, parameter));

				final boolean verletztAlt = vertrauenVerletztAllgemein;
				final boolean verletztAktuell = datenBezugsZeitraumQKfz
						.isVertrauensBereichVerletzt()
						|| datenBezugsZeitraumQLkw
						.isVertrauensBereichVerletzt()
						|| datenBezugsZeitraumQPkw
						.isVertrauensBereichVerletzt()
						|| datenBezugsZeitraumVKfz
						.isVertrauensBereichVerletzt()
						|| datenBezugsZeitraumVLkw
						.isVertrauensBereichVerletzt()
						|| datenBezugsZeitraumVPkw
						.isVertrauensBereichVerletzt()
						|| datenBezugsZeitraumSKfz
						.isVertrauensBereichVerletzt()
						|| datenBezugsZeitraumB.isVertrauensBereichVerletzt();

				if (verletztAktuell) {
					copy = copy.createModifiableCopy();

					/**
					 * Hier nur die Markierungs aller Attribute des Datensatzes.
					 * Eine Betriebsmeldung muss nicht ausgegeben werden, da
					 * dies ggf. schon für jeden einzelnen Wert getan wird
					 */
					for (final String attribut : VertrauensFahrStreifen.ATTRIBUTE) {
						copy.getItem(attribut)
						.getItem("Status")
						.getItem("MessWertErsetzung")
						.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
					}

					final long vertrauensBereich = TestParameter.getInstanz()

							.isTestVertrauen() ? parameter.getBezugsZeitraum()
									* TestParameter.INTERVALL_VB * 60L : parameter
									.getBezugsZeitraum() * Constants.MILLIS_PER_HOUR;

							final Date start = new Date(originalDatum.getDataTime()
									- vertrauensBereich);
							final Date ende = new Date(originalDatum.getDataTime());

							final String nachricht = "Setzte alles auf implausibel da Vertrauensbereich verletzt [" + //$NON-NLS-1$
									DUAKonstanten.BM_ZEIT_FORMAT.format(start)
									+ ", " + DUAKonstanten.BM_ZEIT_FORMAT.format(ende) + //$NON-NLS-1$
									"] ("
									+ parameter.getBezugsZeitraum()
									+ " Stunde(n))";

							LOGGER.fine(
									VerwaltungPlPruefungLogischLVE
									.getPlLogIdent(originalDatum)
									+ "\n"
									+ nachricht);
				} else {
					if (verletztAlt) {
						final long vertrauensBereich = TestParameter
								.getInstanz().isTestVertrauen() ? parameter
										.getBezugsZeitraum()
										* TestParameter.INTERVALL_VB * 60L : parameter
										.getBezugsZeitraum()
										* Constants.MILLIS_PER_HOUR;

										final Date start = new Date(originalDatum.getDataTime()
												- vertrauensBereich);
										final Date ende = new Date(originalDatum.getDataTime());

										final String nachricht = "Daten wieder innerhalb des Vertrauensbereichs. Im Zeitraum von " + //$NON-NLS-1$
												DUAKonstanten.BM_ZEIT_FORMAT.format(start)
												+ " Uhr bis " + DUAKonstanten.BM_ZEIT_FORMAT.format(ende) + //$NON-NLS-1$
												" ("
												+ parameter.getBezugsZeitraum()
												+ " Stunde(n)) implausible Fahrstreifenwerte am Fahrstreifen " + //$NON-NLS-1$
												originalDatum.getObject()
												+ " von " + ausfallErgebnisse.last() + //$NON-NLS-1$
												". Fahrstreifenwerte werden wieder verarbeitet."; //$NON-NLS-1$

										DUAUtensilien.sendeBetriebsmeldung(
												VertrauensFahrStreifen.dieVerwaltung
										.getVerbindung(),
												VertrauensFahrStreifen.dieVerwaltung
										.getBmvIdKonverter(),
												MessageGrade.WARNING, objekt, nachricht);
					}
				}

				vertrauenVerletztAllgemein = verletztAktuell;
			} else {
				LOGGER.config(
						"Datum kann nicht plausibilisiert werden, da keine" + //$NON-NLS-1$
								" (oder nicht verwertbare) Parameter vorliegen: " //$NON-NLS-1$
								+ this);
			}
		}

		return copy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					synchronized (this) {
						parameter = new AtgVerkehrsDatenVertrauensBereichFs(
								resultat.getData());
					}
				}
			}
		}
	}

}
