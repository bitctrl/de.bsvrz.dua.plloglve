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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.dua.plloglve.vew.TestParameter;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IntervallPufferException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;

/**
 * Speichert die Ausfallhäufigkeit eine Fahrstreifens über einem gleitenden Tag.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public class AusfallFahrStreifen implements ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Format der Zeitangabe innerhalb der Betriebsmeldung.
	 */
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm"); //$NON-NLS-1$

	/**
	 * IP der ATG KZD.
	 */
	private static long atgKzdId = -1;

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	private static IVerwaltung dieVerwaltung = null;

	/**
	 * Datenbeschreibung der Parameterattributgruppe
	 * <code>atg.verkehrsDatenAusfallHäufigkeitFs</code>.
	 */
	private static DataDescription ausfallBeschreibung = null;

	/**
	 * Maximal zulässige Ausfallhäufigkeit eines Fahrstreifens pro Tag.
	 */
	private long maxAusfallProTag = -4;

	/**
	 * Datensaetze mit Ausfallinformationen der letzten 24h.
	 */
	private final AusfallPuffer gleitenderTag = new AusfallPuffer();

	/**
	 * das Objekt.
	 */
	private final SystemObject objekt;

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            das mit einem Fahrstreifen assoziierte Systemobjekt
	 */
	protected AusfallFahrStreifen(final IVerwaltung verwaltung,
			final SystemObject obj) {
		objekt = obj;

		if (AusfallFahrStreifen.dieVerwaltung == null) {
			AusfallFahrStreifen.dieVerwaltung = verwaltung;
			AusfallFahrStreifen.ausfallBeschreibung = new DataDescription(
					AusfallFahrStreifen.dieVerwaltung
							.getVerbindung()
							.getDataModel()
							.getAttributeGroup(
									"atg.verkehrsDatenAusfallHäufigkeitFs"), //$NON-NLS-1$
					AusfallFahrStreifen.dieVerwaltung.getVerbindung()
							.getDataModel()
					.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL));
			AusfallFahrStreifen.atgKzdId = AusfallFahrStreifen.dieVerwaltung
					.getVerbindung().getDataModel()
					.getAttributeGroup(DUAKonstanten.ATG_KZD).getId();
		}

		AusfallFahrStreifen.dieVerwaltung.getVerbindung().subscribeReceiver(
				this, obj, AusfallFahrStreifen.ausfallBeschreibung,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	/**
	 * Fuehrt die Plausibilisierung durch. (nur für KZD)
	 *
	 * @param resultat
	 *            ein Fahrstreifendatum (KZD)
	 */
	protected final void plausibilisiere(final ResultData resultat) {
		if (resultat.getDataDescription().getAttributeGroup().getId() == AusfallFahrStreifen.atgKzdId) {

			final AusfallDatumKomplett ausfallDatum = AusfallDatumKomplett
					.getAusfallDatumVon(resultat);
			if (ausfallDatum != null) {
				try {
					gleitenderTag.add(ausfallDatum);
				} catch (final IntervallPufferException e) {
					LOGGER.error(Constants.EMPTY_STRING, e);
					e.printStackTrace();
				}
			}
			testAufAusfall(ausfallDatum);
		}
	}

	/**
	 * Erreichnet den Ausfall dieses Fahrstreifens und gibt ggf. eine
	 * Betriebsmeldung aus.
	 *
	 * @param letztesAusfallDatum
	 *            das letzte ausgefallene Datum
	 */
	private void testAufAusfall(final AusfallDatumKomplett letztesAusfallDatum) {
		long ausfallZeit = 0;

		if (AusfallFahrStreifen.programmLaeuftSchonLaengerAlsEinTag()) {
			try {
				if (TestParameter.getInstanz().isTestAusfall()) {
					if (letztesAusfallDatum != null) {
						gleitenderTag
						.loescheAllesUnterhalbVon(letztesAusfallDatum
								.getIntervallEnde() - 144000L);
					}
				} else {
					if (letztesAusfallDatum != null) {
						gleitenderTag
						.loescheAllesUnterhalbVon(letztesAusfallDatum
								.getIntervallEnde()
								- Constants.MILLIS_PER_DAY);
					}
				}
				ausfallZeit = gleitenderTag.getAusfallZeit();
			} catch (final IntervallPufferException e) {
				LOGGER.error(Constants.EMPTY_STRING, e);
				e.printStackTrace();
			}

			if (maxAusfallProTag >= 0) {

				double ausfallInProzent;
				if (TestParameter.getInstanz().isTestAusfall()) {
					ausfallInProzent = (((double) ausfallZeit / (double) 144000L) * 100.0);
				} else {
					ausfallInProzent = (((double) ausfallZeit / (double) Constants.MILLIS_PER_DAY) * 100.0);
				}

				if (ausfallInProzent > maxAusfallProTag) {
					final long stunden = ausfallZeit
							/ Constants.MILLIS_PER_HOUR;
					final long minuten = (ausfallZeit - (stunden * Constants.MILLIS_PER_HOUR))
							/ Constants.MILLIS_PER_MINUTE;

					final String nachricht = "Ausfallhäufigkeit innerhalb der letzten 24 Stunden überschritten. Im Zeitraum von " + //$NON-NLS-1$
							AusfallFahrStreifen.FORMAT.format(new Date(System
									.currentTimeMillis()
									- Constants.MILLIS_PER_DAY))
									+ " Uhr bis " + //$NON-NLS-1$
									AusfallFahrStreifen.FORMAT.format(new Date(System
									.currentTimeMillis()))
									+ " Uhr (1 Tag) implausible Fahrstreifenwerte am Fahrstreifen " + //$NON-NLS-1$
									objekt
									+ " von "
									+ DUAUtensilien.runde(ausfallInProzent, 1)
									+ "% (> " + maxAusfallProTag + //$NON-NLS-1$
									"%) entspricht Ausfall von "
									+ stunden
									+ " Stunde(n) " + minuten + " Minute(n)."; //$NON-NLS-1$ //$NON-NLS-2$

					DUAUtensilien.sendeBetriebsmeldung(
							AusfallFahrStreifen.dieVerwaltung.getVerbindung(),
							AusfallFahrStreifen.dieVerwaltung
									.getBmvIdKonverter(), MessageGrade.WARNING,
							objekt, nachricht);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] davParameterFeld) {
		if (davParameterFeld != null) {
			for (final ResultData davParameter : davParameterFeld) {
				if ((davParameter != null) && (davParameter.getData() != null)) {
					maxAusfallProTag = davParameter.getData()
							.getUnscaledValue("maxAusfallProTag").longValue(); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Erfragt, ob diese Applikation (eigentlich das Modul Pl-Prüfung logisch
	 * LVE) schon länger als einen Tag läuft (erst dann solltes Objekt Daten
	 * markieren bzw. Betriebsmeldungen ausgeben)
	 *
	 * @return ob diese Applikation schon länger als einen Tag läuft
	 */
	private static boolean programmLaeuftSchonLaengerAlsEinTag() {
		if (TestParameter.getInstanz().isTestAusfall()) {
			return (PlPruefungLogischLVE.START_ZEIT + 144000L) < System
					.currentTimeMillis();
		}
		return (PlPruefungLogischLVE.START_ZEIT + Constants.MILLIS_PER_DAY) < System
				.currentTimeMillis();
	}

}
