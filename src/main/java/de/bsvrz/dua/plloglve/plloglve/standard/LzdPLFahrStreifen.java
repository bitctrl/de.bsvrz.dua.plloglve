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

package de.bsvrz.dua.plloglve.plloglve.standard;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.dua.guete.GueteVerfahren;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.dua.plloglve.vew.VerwaltungPlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.GanzZahl;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse zum Durchführen der speziellen Standardplausibilisierung LVE für KZD.
 * Diese Klasse macht nichts weiter, als sich auf die Grenzwertparameter
 * anzumelden und einige Funktionen zur Plausibilisierung von KZD zur Verfügung
 * zu stellen
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public class LzdPLFahrStreifen extends KzdPLFahrStreifen {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Alle Attribute, die innerhalb der PL-Prüfung logisch bzwl eines KZD
	 * veraendert werden koennen.
	 */
	private static final String[] ATTRIBUT_NAMEN = { "qKfz", //$NON-NLS-1$
		"qLkw", //$NON-NLS-1$
		"qPkw", //$NON-NLS-1$
		"vPkw", //$NON-NLS-1$
		"vLkw", //$NON-NLS-1$
		"vKfz", //$NON-NLS-1$
	"sKfz" }; //$NON-NLS-1$

	/**
	 * Standartdkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	protected LzdPLFahrStreifen(final IVerwaltungMitGuete verwaltung,
			final SystemObject obj) {
		super(verwaltung, obj);
	}

	/**
	 * Plausibilisiert ein übergebenes Datum.
	 *
	 * @param resultat
	 *            ein Originaldatum
	 * @return das veränderte Datum oder <code>null</code>, wenn keine
	 *         Veränderungen vorgenommen werden mussten
	 */
	@Override
	protected Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if (resultat.getData() != null) {
			copy = resultat.getData().createModifiableCopy();

			/**
			 * Aenderung laut Mailverkehr vom 2.4.08 - 4.4.08:
			 *
			 * ...mir ist beim Test der PL-Prüfung LVE für die Langzeitdaten
			 * weiterhin aufgefallen, dass (seit 2.7.5) immer wenn qLkw > qKfz
			 * ist, qKfz auf -2 gesetzt zu werden scheint. Dies ist aber durch
			 * keine Regel gedeckt.
			 *
			 * ...Sie haben recht, es gibt keine explizite Regel bzgl. der
			 * Reaktion auf die hier erkannten fehlerhaften Langzeitwerte (hier
			 * Lkw>Kfz), ich bin aber der Meinung, dass es nicht verkehrt ist,
			 * dann einen entsprechenden Status zu setzen, bevor das Programm
			 * ggf. an einer anderen Stelle aufgrund einer unzulässigen
			 * Operation abgebrochen wird oder eine unzulässigen Entscheidung
			 * trifft....
			 *
			 * ...Ich kann dies so ändern, es würde aber wieder auf eine
			 * explizite Änderung der Anforderungen hinauslaufen. Eigentlich
			 * heißt das doch, dass für LZD jetzt auch ein Teil der
			 * Standardtests gemacht werden soll, der bisher nur für KZD
			 * vorgesehen war. Die Frage ist nun, ob es dann wirklich nur diese
			 * eine Regel sein soll (R5) oder ob vielleicht noch andere Regeln
			 * mit dran hängen. Für Rückfragen stehe ich Ihnen gern zur
			 * Verfügung....
			 *
			 * ...ich wollte eigentlich keine neue Regel vorgeben, sondern nur
			 * bei der Berechnung von qPkw einen möglichen Fehler ausschließen,
			 * so wie man bei einer Division den Null-Fall ausschließt oder in
			 * anderen Fällen z.B. auf negative Werte abgefragt werden könnte,
			 * d..h. die generelle Anforderung, mögliche Fehler bei Berechnungen
			 * zu erkennen und zu melden (zu kennzeichnen), ist eine über alles
			 * stehende Regel. Zukünftig ist aber nicht auszuschließen, dass für
			 * LZD weitere explizite Regeln aufgestellt werden....
			 */
			final long qKfz = copy
					.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
			final long qLkw = copy
					.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$

			/**
			 * Regel Nr.5 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
			 */
			if ((qKfz >= 0) && (qLkw >= 0)) {
				if (qKfz < qLkw) {
					setAllesImplausibel(copy);

					letztesKZDatum = resultat;

					return copy;

					// copy.getItem("qKfz").getUnscaledValue("Wert").set(
					// DUAKonstanten.FEHLERHAFT);
					// copy.getItem("qKfz").getItem("Status").getItem(
					// "MessWertErsetzung")
					// .getUnscaledValue("Implausibel").set(
					// DUAKonstanten.JA);
					// copy.getItem("qKfz").getItem("Güte").getUnscaledValue(
					// "Index").set(0);
					//
					// copy.getItem("qLkw").getUnscaledValue("Wert").set(
					// DUAKonstanten.FEHLERHAFT);
					// copy.getItem("qLkw").getItem("Status").getItem(
					// "MessWertErsetzung")
					// .getUnscaledValue("Implausibel").set(
					// DUAKonstanten.JA);
					// copy.getItem("qLkw").getItem("Güte").getUnscaledValue(
					// "Index").set(0);
				}
			}

			/**
			 * Aenderung Ende:
			 */

			berechneQPkwUndVKfz(copy, resultat);
			ueberpruefe(copy, resultat);
			passeGueteAn(copy);
		}
		letztesKZDatum = resultat;

		return copy;
	}

	/**
	 * Setzt im uebergebenen Datensatz alle Werte auf implausibel und
	 * fehlerhaft.
	 *
	 * @param veraenderbaresDatum
	 *            ein veraenderbarer LVE-Datensatz (muss <code>!= null</code>
	 *            sein)
	 */
	@Override
	protected void setAllesImplausibel(final Data veraenderbaresDatum) {
		final int qKfz = veraenderbaresDatum
				.getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (qKfz == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
			.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
			.getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final int qLkw = veraenderbaresDatum
				.getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (qLkw == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
			.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
			.getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		veraenderbaresDatum
		.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

		veraenderbaresDatum
		.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

		final int vLkw = veraenderbaresDatum
				.getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (vLkw == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
			.getItem("vLkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
			.getItem("vLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final int vPkw = veraenderbaresDatum
				.getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (vPkw == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
			.getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
			.getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		veraenderbaresDatum.getItem("qKfz").getItem("Status")
				.getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qKfz").getItem("Güte")
				.getUnscaledValue("Index").set(0);
		veraenderbaresDatum.getItem("qLkw").getItem("Status")
				.getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qLkw").getItem("Güte")
				.getUnscaledValue("Index").set(0);
		veraenderbaresDatum.getItem("qPkw").getItem("Status")
				.getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qPkw").getItem("Güte")
				.getUnscaledValue("Index").set(0);
		veraenderbaresDatum.getItem("vKfz").getItem("Status")
				.getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vKfz").getItem("Güte")
				.getUnscaledValue("Index").set(0);
		veraenderbaresDatum.getItem("vLkw").getItem("Status")
				.getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vLkw").getItem("Güte")
				.getUnscaledValue("Index").set(0);
		veraenderbaresDatum.getItem("vPkw").getItem("Status")
				.getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vPkw").getItem("Güte")
				.getUnscaledValue("Index").set(0);

		final int sKfz = veraenderbaresDatum
				.getItem("sKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (sKfz == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
			.getItem("sKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
			.getItem("sKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		veraenderbaresDatum.getItem("sKfz").getItem("Status")
				.getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void ueberpruefe(final Data data, final ResultData resultat) {
		if (parameterAtgLog != null) {
			synchronized (parameterAtgLog) {
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				untersucheAufMaxVerletzung(data, resultat, "qKfz",
						parameterAtgLog.getQKfzBereichMax());

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				untersucheAufMaxVerletzung(data, resultat, "qLkw",
						parameterAtgLog.getQLkwBereichMax());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getPlausibilisierungsParameterAtg(
			final ClientDavInterface dav) {
		return dav.getDataModel().getAttributeGroup(
				AtgVerkehrsDatenLZIPlPruefLogisch.getPid());
	}

	// /**
	// * Setzt im uebergebenen Datensatz alle Werte auf implausibel und
	// fehlerhaft.
	// *
	// * @param veraenderbaresDatum
	// * ein veraenderbarer LVE-Datensatz (muss <code>!= null</code>
	// * sein)
	// */
	// protected void setAllesImplausibel(Data veraenderbaresDatum) {
	// int qKfz = veraenderbaresDatum
	// .getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
	// //$NON-NLS-2$
	// if(qKfz == DUAKonstanten.NICHT_ERMITTELBAR) {
	// veraenderbaresDatum
	// .getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// } else {
	// veraenderbaresDatum
	// .getItem("qKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// }
	//
	// int qLkw = veraenderbaresDatum
	// .getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
	// //$NON-NLS-2$
	// if(qLkw == DUAKonstanten.NICHT_ERMITTELBAR) {
	// veraenderbaresDatum
	// .getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// } else {
	// veraenderbaresDatum
	// .getItem("qLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// }
	//
	// veraenderbaresDatum
	// .getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	//
	//
	// veraenderbaresDatum
	// .getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	//
	// int vLkw = veraenderbaresDatum
	// .getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
	// //$NON-NLS-2$
	// if(vLkw == DUAKonstanten.NICHT_ERMITTELBAR) {
	// veraenderbaresDatum
	// .getItem("vLkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// } else {
	// veraenderbaresDatum
	// .getItem("vLkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// }
	//
	// int vPkw = veraenderbaresDatum
	// .getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
	// //$NON-NLS-2$
	// if(vPkw == DUAKonstanten.NICHT_ERMITTELBAR) {
	// veraenderbaresDatum
	// .getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// } else {
	// veraenderbaresDatum
	// .getItem("vPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// }
	//
	//
	// veraenderbaresDatum
	// .getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").
	// getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	// veraenderbaresDatum
	// .getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").
	// getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	// veraenderbaresDatum
	// .getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").
	// getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	//
	//
	// veraenderbaresDatum
	// .getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").
	// getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	// veraenderbaresDatum
	// .getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").
	// getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	// veraenderbaresDatum
	// .getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").
	// getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	//
	// int sKfz = veraenderbaresDatum
	// .getItem("sKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
	// //$NON-NLS-2$
	// if(sKfz == DUAKonstanten.NICHT_ERMITTELBAR) {
	// veraenderbaresDatum
	// .getItem("sKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// } else {
	// veraenderbaresDatum
	// .getItem("sKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);
	// //$NON-NLS-1$ //$NON-NLS-2$
	// }
	//
	// veraenderbaresDatum
	// .getItem("sKfz").getItem("Status").getItem("MessWertErsetzung").
	// getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	// }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean untersucheAufMaxVerletzung(final Data davDatum,
			final ResultData resultat, final String wertName, final long max) {
		if (parameterAtgLog != null) {

			final OptionenPlausibilitaetsPruefungLogischVerkehr optionen = parameterAtgLog
					.getOptionen();

			if (!optionen
					.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)) {
				final long wert = davDatum.getItem(wertName)
						.getUnscaledValue("Wert").longValue(); //$NON-NLS-1$

				final GanzZahl sweGueteWert = GanzZahl.getGueteIndex();
				sweGueteWert
						.setSkaliertenWert(AbstraktPLFahrStreifen.dieVerwaltung
								.getGueteFaktor());
				final GWert sweGuete = new GWert(sweGueteWert,
						GueteVerfahren.STANDARD, false);

				/**
				 * sonst handelt es sich nicht um einen Messwert
				 */
				if ((wert >= 0) && (max >= 0)) {
					final boolean maxVerletzt = wert > max;

					if (maxVerletzt) {
						if (optionen
								.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX)
								|| optionen
								.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX)) {
							davDatum.getItem(wertName)
							.getUnscaledValue("Wert").set(max); //$NON-NLS-1$
							davDatum.getItem(wertName)
							.getItem("Status")
							.getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
							LOGGER.fine(
									VerwaltungPlPruefungLogischLVE
									.getPlLogIdent(resultat)
									+ "\nSetze "
									+ wertName
									+ " auf "
									+ max + " und WertMaxLogisch");

							final GWert guete = new GWert(davDatum, wertName);
							GWert neueGuete = GWert
									.getNichtErmittelbareGuete(guete
											.getVerfahren());
							try {
								neueGuete = GueteVerfahren.produkt(guete,
										sweGuete);
							} catch (final GueteException e1) {
								LOGGER
								.error("Guete von " + wertName + " konnte nicht aktualisiert werden in " + resultat); //$NON-NLS-1$ //$NON-NLS-2$
								e1.printStackTrace();
							}
							davDatum.getItem(wertName)
							.getItem("Güte").//$NON-NLS-1$
							getUnscaledValue("Index").set(neueGuete.getIndexUnskaliert()); //$NON-NLS-1$
						} else if (optionen
								.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.NUR_PRUEFUNG)) {
							if (wertName.equals("vKfz") || wertName.startsWith("qKfz") || //$NON-NLS-1$ //$NON-NLS-2$
									wertName.equals("vLkw") || wertName.startsWith("qLkw") || //$NON-NLS-1$ //$NON-NLS-2$
									wertName.equals("vPkw") || wertName.startsWith("qPkw")) { //$NON-NLS-1$ //$NON-NLS-2$
								davDatum.getItem(wertName)
								.getItem("Status")
								.getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$
								LOGGER.fine(
										VerwaltungPlPruefungLogischLVE
										.getPlLogIdent(resultat)
										+ "\nSetze "
										+ wertName
										+ " auf WertMaxLogisch");
							}
						}
					}
				}
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.bsvrz.dua.plloglve.plloglve.standard.AbstraktPLFahrStreifen#
	 * berechneQPkwUndVKfz(de.bsvrz.dav.daf.main.Data,
	 * de.bsvrz.dav.daf.main.ResultData)
	 */
	@Override
	protected Data berechneQPkwUndVKfz(final Data data,
			final ResultData originalDatum) {
		final boolean qPkwEmpfangenIstBereitsBerechnet;
		final boolean vKfzEmpfangenIstBereitsBerechnet;
		if ((originalDatum != null) && (originalDatum.getData() != null)) {
			qPkwEmpfangenIstBereitsBerechnet = originalDatum.getData()
					.getItem("qPkw").getUnscaledValue("Wert").longValue() >= 0;
					vKfzEmpfangenIstBereitsBerechnet = originalDatum.getData()
					.getItem("vKfz").getUnscaledValue("Wert").longValue() >= 0;
		} else {
			qPkwEmpfangenIstBereitsBerechnet = false;
			vKfzEmpfangenIstBereitsBerechnet = false;
		}

		if (!qPkwEmpfangenIstBereitsBerechnet
				&& !vKfzEmpfangenIstBereitsBerechnet) {
			final long qKfz = data
					.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
			final boolean qKfzImplausibel = data.getItem("qKfz")
					.getItem("Status").getItem("MessWertErsetzung")
					.getUnscaledValue("Implausibel").longValue() == DUAKonstanten.JA; //$NON-NLS-1$
			final long qLkw = data
					.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
			final boolean qLkwImplausibel = data.getItem("qLkw")
					.getItem("Status").getItem("MessWertErsetzung")
					.getUnscaledValue("Implausibel").longValue() == DUAKonstanten.JA; //$NON-NLS-1$

			long qPkw = DUAKonstanten.NICHT_ERMITTELBAR;
			GWert qPkwGuete = GWert.getMaxGueteWert(GueteVerfahren.STANDARD);

			if ((qKfz >= 0) && !qKfzImplausibel) {
				if ((qLkw >= 0) && !qLkwImplausibel) {
					if (qLkw > qKfz) {
						LOGGER.fine(
								VerwaltungPlPruefungLogischLVE
								.getPlLogIdent(originalDatum)
								+ "\nSetze qPkw (eigentlich "
								+ VerwaltungPlPruefungLogischLVE
								.getWertIdent(qPkw)
								+ ") auf nicht ermittelbar da qLkw ("
								+ VerwaltungPlPruefungLogischLVE
								.getWertIdent(qLkw)
								+ ") > qKfz ("
								+ VerwaltungPlPruefungLogischLVE
								.getWertIdent(qLkw) + ").");
						qPkw = DUAKonstanten.NICHT_ERMITTELBAR;
					} else {
						qPkw = qKfz - qLkw;

						try {
							final GWert qKfzG = new GWert(data, "qKfz"); //$NON-NLS-1$
							final GWert qLkwG = new GWert(data, "qLkw"); //$NON-NLS-1$
							qPkwGuete = GueteVerfahren.differenz(qKfzG, qLkwG);
						} catch (final GueteException e) {
							e.printStackTrace();
							LOGGER
							.error("Berechnung der Guete von qPkw fehlgeschlagen", e); //$NON-NLS-1$
						}
					}
				} else {
					qPkw = qKfz;
					qPkwGuete = new GWert(data, "qKfz");
				}
			} else {
				LOGGER.fine(
						VerwaltungPlPruefungLogischLVE
						.getPlLogIdent(originalDatum)
						+ "\nSetze qPkw (eigentlich "
						+ VerwaltungPlPruefungLogischLVE
						.getWertIdent(qPkw)
						+ ") auf nicht ermittelbar da qKfz ("
						+ VerwaltungPlPruefungLogischLVE
						.getWertIdent(qKfz)
						+ ") < 0 bzw. implausibel.");
			}

			if (DUAUtensilien.isWertInWerteBereich(data
					.getItem("qPkw").getItem("Wert"), qPkw)) { //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("qPkw").getUnscaledValue("Wert").set(qPkw); //$NON-NLS-1$ //$NON-NLS-2$
				qPkwGuete.exportiere(data, "qPkw"); //$NON-NLS-1$
			} else {
				data.getItem("qPkw").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$//$NON-NLS-2$
				data.getItem("qPkw")
				.getItem("Status")
				.getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-2%
				LOGGER
				.fine(VerwaltungPlPruefungLogischLVE
								.getPlLogIdent(originalDatum)
								+ "\nSetze qPkw (eigentlich "
								+ VerwaltungPlPruefungLogischLVE
										.getWertIdent(qPkw)
								+ ") auf impl. und fehlerhaft da ausserhalb des Modell-Wertebereichs.");
			}

			final long vPkw = data
					.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
			final long vLkw = data
					.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
			long vKfz = DUAKonstanten.NICHT_ERMITTELBAR;
			GWert vKfzGuete = GWert.getMaxGueteWert(GueteVerfahren.STANDARD);

			if ((qKfz > 0) && (qPkw >= 0) && (vPkw >= 0) && (qLkw >= 0)
					&& (vLkw >= 0)) {
				final long qPkwDummy = qPkw >= 0 ? qPkw : 0;
				final long vPkwDummy = vPkw >= 0 ? vPkw : 0;
				final long qLkwDummy = qLkw >= 0 ? qLkw : 0;
				final long vLkwDummy = vLkw >= 0 ? vLkw : 0;
				vKfz = (long) (((double) ((qPkwDummy * vPkwDummy) + (qLkwDummy * vLkwDummy)) / (double) qKfz) + 0.5);

				try {
					final GWert qPkwG = new GWert(data, "qPkw"); //$NON-NLS-1$
					final GWert vPkwG = new GWert(data, "vPkw"); //$NON-NLS-1$
					final GWert qLkwG = new GWert(data, "qLkw"); //$NON-NLS-1$
					final GWert vLkwG = new GWert(data, "vLkw"); //$NON-NLS-1$
					final GWert qKfzG = new GWert(data, "qKfz"); //$NON-NLS-1$

					if ((qPkwDummy * vPkwDummy) == 0) {
						vKfzGuete = GueteVerfahren.quotient(
								GueteVerfahren.produkt(qLkwG, vLkwG), qKfzG);
					} else if ((qLkwDummy * vLkwDummy) == 0) {
						vKfzGuete = GueteVerfahren.quotient(
								GueteVerfahren.produkt(qPkwG, vPkwG), qKfzG);
					} else {
						vKfzGuete = GueteVerfahren.quotient(GueteVerfahren
								.summe(GueteVerfahren.produkt(qPkwG, vPkwG),
										GueteVerfahren.produkt(qLkwG, vLkwG)),
										qKfzG);
					}

				} catch (final GueteException e) {
					e.printStackTrace();
					LOGGER.error(
							"Berechnung der Guete von vKfz fehlgeschlagen", e); //$NON-NLS-1$
				}
			} else {
				LOGGER.fine(
						VerwaltungPlPruefungLogischLVE
						.getPlLogIdent(originalDatum)
						+ "\nSetze vKfz (eigentlich "
						+ VerwaltungPlPruefungLogischLVE
						.getWertIdent(vKfz)
						+ ") auf nicht ermittelbar da qKfz ("
						+ VerwaltungPlPruefungLogischLVE
						.getWertIdent(qKfz) + ") <= 0.");
			}

			if (DUAUtensilien.isWertInWerteBereich(data
					.getItem("vKfz").getItem("Wert"), vKfz)) { //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vKfz").getUnscaledValue("Wert").set(vKfz); //$NON-NLS-1$ //$NON-NLS-2$
				vKfzGuete.exportiere(data, "vKfz"); //$NON-NLS-1$
			} else {
				data.getItem("vKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$//$NON-NLS-2$
				data.getItem("vKfz")
				.getItem("Status")
				.getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-2%
				LOGGER
				.fine(VerwaltungPlPruefungLogischLVE
								.getPlLogIdent(originalDatum)
								+ "\nSetze vKfz (eigentlich "
								+ VerwaltungPlPruefungLogischLVE
										.getWertIdent(vKfz)
								+ ") auf impl. und fehlerhaft da ausserhalb des Wertebereichs.");
			}

			data.getItem("qPkw")
			.getItem("Status")
			.getItem("Erfassung").getUnscaledValue("NichtErfasst").set(DUAKonstanten.JA); //$NON-NLS-1$//$NON-NLS-2$
			data.getItem("vKfz")
			.getItem("Status")
			.getItem("Erfassung").getUnscaledValue("NichtErfasst").set(DUAKonstanten.JA); //$NON-NLS-1$//$NON-NLS-2$
		}

		return data;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getAttributNamen() {
		return LzdPLFahrStreifen.ATTRIBUT_NAMEN;
	}

}
