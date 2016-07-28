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

package de.bsvrz.dua.plloglve.plloglve.vb;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.ausfall.AusfallFahrStreifen;
import de.bsvrz.dua.plloglve.plloglve.standard.AtgVerkehrsDatenKurzZeitIntervallPlLogisch;
import de.bsvrz.dua.plloglve.plloglve.standard.PLFahrStreifen;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IntervallPufferException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Set;

/**
 * Repräsentiert einen Fahrstreifen mit allen Informationen, die zur Ermittlung
 * des Vertrauens in diesen Fahrstreifen notwendig sind.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class VertrauensFahrStreifen implements ClientReceiverInterface {

	/**
	 * Alle Attribute, die ggf. auf implausibel gesetzt werden müssen
	 */
	private static final String[] ATTRIBUTE = new String[] {
			"qKfz", "qLkw", "qPkw", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"vKfz", "vLkw", "vPkw", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"b", "tNetto", "sKfz", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			"vgKfz" }; //$NON-NLS-1$

	private static final Debug _debug = Debug.getLogger();
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.GERMAN);

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	private IVerwaltung _verwaltung = null;
	private final SystemObject _mq;

	/**
	 * Datensaetze mit Ausfallinformationen im Bezugszeitraum.
	 */
	private final VertrauensbereichPuffer gleitenderTag = new VertrauensbereichPuffer();

	/**
	 * das Objekt.
	 */
	private final SystemObject _objekt;

	/**
	 * Aktueller Parameter-Datensatz oder null falls nicht vorhanden
	 */
	private AtgVerkehrsDatenKurzZeitIntervallPlLogisch _parameter;

	private static final MessageTemplate TEMPLATE_EIN = new MessageTemplate(
			MessageGrade.INFORMATION,
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.fixed("Daten außerhalb des Vertrauensbereichs: im Zeitraum von "),
			MessageTemplate.variable("from"),
			MessageTemplate.fixed(" bis "),
			MessageTemplate.variable("to"),
			MessageTemplate.fixed(" ("),
			MessageTemplate.variable("bezug"),
			MessageTemplate.fixed(") implausible Fahrstreifenwerte für"),
			MessageTemplate.set("values", " und ", " den Wert ", " die Werte "),
			MessageTemplate.fixed(" am Fahrstreifen "),
			MessageTemplate.object(),
			MessageTemplate.fixed("("),
			MessageTemplate.variable("mq"),
			MessageTemplate.fixed(") während "),
			MessageTemplate.variable("duration"),
			MessageTemplate.fixed(" (> "),
			MessageTemplate.variable("maxDuration"),
			MessageTemplate.fixed("). Fahrstreifenwerte werden auf fehlerhaft gesetzt und als implausibel gekennzeichnet. "),
			MessageTemplate.ids()
	).withIdFactory(message -> message.getObject().getPidOrId() + " [DUA-PP-VB]");
	
	private static final MessageTemplate TEMPLATE_AUS = new MessageTemplate(
			MessageGrade.INFORMATION,
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.fixed("Daten wieder innerhalb des Vertrauensbereichs: im Zeitraum von "),
			MessageTemplate.variable("from"),
			MessageTemplate.fixed(" bis "),
			MessageTemplate.variable("to"),
			MessageTemplate.fixed(" ("),
			MessageTemplate.variable("bezug"),
			MessageTemplate.fixed(") implausible Fahrstreifenwerte am Fahrstreifen "),
			MessageTemplate.object(),
			MessageTemplate.fixed("("),
			MessageTemplate.variable("mq"),
			MessageTemplate.fixed(") während "),
			MessageTemplate.variable("duration"),
			MessageTemplate.fixed(" (< "),
			MessageTemplate.variable("maxDuration"),
			MessageTemplate.fixed("). Fahrstreifenwerte werden wieder verarbeitet. "),
			MessageTemplate.ids()
	).withIdFactory(message -> message.getObject().getPidOrId() + " [DUA-PP-VB]");

	private Instant _lastCheckTime;
	
	private PersistentOperatingMessage _meldung;

	/**
	 * Standardkonstruktor.
	 *  @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 * @param mq
	 */
	protected VertrauensFahrStreifen(final IVerwaltung verwaltung,
			final SystemObject obj, final SystemObject mq) {
		_objekt = obj;
		_verwaltung = verwaltung;
		_mq = mq;

		ClientDavInterface connection = verwaltung.getVerbindung();

		AttributeGroup parameterAtg = PLFahrStreifen.getParameterAtg(connection);
		if(parameterAtg != null) {
			connection.subscribeReceiver(
					this,
					obj,
					new DataDescription(parameterAtg, connection.getDataModel()
							.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL)), ReceiveOptions.normal(),
					ReceiverRole.receiver()
			);
		}
		else {
			_debug.warning("Attributgruppe " + AtgVerkehrsDatenKurzZeitIntervallPlLogisch.getPid() + " ist nicht im Datenmodell vorhanden, die Grenzwerte werden nicht geprüft");
		}
	}

	/**
	 * Fuehrt die Plausibilisierung durch. (nur für KZD)
	 *
	 * @param resultat
	 *            ein Fahrstreifendatum (KZD)
	 */
	protected final synchronized Data plausibilisiere(final ResultData resultat) {
		Data copy = resultat.getData();

		final VertrauensbereichDatumKomplett ausfallDatum = VertrauensbereichDatumKomplett
				.getAusfallDatumVon(resultat);
		if(_parameter != null) {
			if(ausfallDatum != null) {
				try {
					gleitenderTag.add(ausfallDatum);
				}
				catch(final IntervallPufferException e) {
					_debug.error("Fehler beim Intervallpuffer", e);
				}

				long startIntervall = ausfallDatum.getIntervallEnde() - _parameter.getVertrauensbereichBezugsZeitraum();
				try {
					gleitenderTag.loescheAllesUnterhalbVon(startIntervall);
				}
				catch(final IntervallPufferException e) {
					_debug.error("Fehler beim Intervallpuffer", e);
				}
				Instant now = Instant.ofEpochMilli(ausfallDatum.getIntervallEnde());
				long pruefIntervallAusfall = _parameter.getPruefIntervallVertrauensbereich();
				Instant nextCheckTime;
				if(_lastCheckTime == null) {
					nextCheckTime = now;
				}
				else {
					nextCheckTime = _lastCheckTime.plusMillis(pruefIntervallAusfall);
				}
				if(!now.isBefore(nextCheckTime)) {
					pruefeAusfall(Instant.ofEpochMilli(startIntervall), now);
					_lastCheckTime = nextCheckTime;
				}
			}
			final boolean verletztAktuell = _meldung != null;

			if(verletztAktuell && copy != null) {
				copy = copy.createModifiableCopy();

				for(final String attribut : ATTRIBUTE) {
					copy.getItem(attribut)
							.getItem("Status")
							.getItem("MessWertErsetzung")
							.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
					copy.getItem(attribut).getItem("Wert").asUnscaledValue().set(DUAKonstanten.FEHLERHAFT);
				}
			}
		} else {
			Debug.getLogger()
					.warning("Fuer Fahrstreifen " + this + //$NON-NLS-1$
							         " wurden noch keine Parameter für die Prüfung des Vertrauensbereichs empfangen"); //$NON-NLS-1$
		}

		return copy;
	}

	@Override
	public String toString() {
		return _objekt.toString();
	}

	private synchronized void pruefeAusfall(final Instant fromTime, final Instant checkTime){
		if(_parameter == null) return;

		long ausfallZeit = gleitenderTag.getAusfallZeit();
		long maxAusfallZeitEin = _parameter.getMaxAusfallProBezugszeitraumEin();
		long maxAusfallZeitAus = _parameter.getMaxAusfallProBezugszeitraumAus();
		if(maxAusfallZeitEin <= 0 && maxAusfallZeitAus <= 0) return;
		if(maxAusfallZeitAus > maxAusfallZeitEin){
			_debug.warning("Ungültige Vertrauensbereich-Parameter am Fahrstreifen " + _objekt + ": MaxAusfallProBezugszeitraumAus > MaxAusfallProBezugszeitraumEin");
			return;
		}
		if(_meldung != null && ausfallZeit < maxAusfallZeitAus) {
			sendeGutMeldung(fromTime, checkTime, ausfallZeit);
		}
		else if(_meldung != null || ausfallZeit > maxAusfallZeitEin) {
			erzeugeMeldung(fromTime, checkTime, ausfallZeit, gleitenderTag.getAusfallAttribute());
		}
	}

	private void erzeugeMeldung(final Instant fromTime, final Instant checkTime, final long ausfallZeit, final Set<String> values) {
		OperatingMessage operatingMessage = TEMPLATE_EIN.newMessage(_objekt);
		operatingMessage.put("mq", _mq);
		operatingMessage.put("from", AusfallFahrStreifen.formatDate(fromTime));
		operatingMessage.put("to", AusfallFahrStreifen.formatDate(checkTime));
		operatingMessage.put("duration", AusfallFahrStreifen.formatDuration(ausfallZeit));
		operatingMessage.put("maxDuration", AusfallFahrStreifen.formatDuration(_parameter.getMaxAusfallProBezugszeitraumEin()));
		operatingMessage.put("bezug", AusfallFahrStreifen.formatDuration(_parameter.getVertrauensbereichBezugsZeitraum()));
		operatingMessage.put("values", values);
		operatingMessage.addId("[DUA-PP-VB01]");
		if(_meldung == null){
			_meldung = operatingMessage.newPersistentMessage();
		}
		else {
			_meldung.update(operatingMessage);
		}
	}

	private void sendeGutMeldung(final Instant fromTime, final Instant checkTime, final long ausfallZeit) {
		OperatingMessage operatingMessage = TEMPLATE_AUS.newMessage(_objekt);
		operatingMessage.put("mq", _mq);
		operatingMessage.put("from", AusfallFahrStreifen.formatDate(fromTime));
		operatingMessage.put("to", AusfallFahrStreifen.formatDate(checkTime));
		operatingMessage.put("duration", AusfallFahrStreifen.formatDuration(ausfallZeit));
		operatingMessage.put("maxDuration", AusfallFahrStreifen.formatDuration(_parameter.getMaxAusfallProBezugszeitraumAus()));
		operatingMessage.put("bezug", AusfallFahrStreifen.formatDuration(_parameter.getVertrauensbereichBezugsZeitraum()));
		operatingMessage.addId("[DUA-PP-VB02]");
		_meldung.resolve(operatingMessage);
		_meldung = null;
	}

	public void update(final ResultData[] parameterFeld) {
		if(parameterFeld != null) {
			for(final ResultData parameter : parameterFeld) {
				if(parameter != null && parameter.getData() != null) {
					if(parameter
							.getDataDescription()
							.getAttributeGroup()
							.equals(PLFahrStreifen.getParameterAtg(_verwaltung.getVerbindung()))) {
						synchronized(this) {
							_parameter = AtgVerkehrsDatenKurzZeitIntervallPlLogisch.getInstance(parameter);
						}
					}
				}
			}
		}
	}

}
