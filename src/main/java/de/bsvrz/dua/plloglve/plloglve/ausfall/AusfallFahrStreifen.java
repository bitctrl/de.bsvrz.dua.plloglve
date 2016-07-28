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

package de.bsvrz.dua.plloglve.plloglve.ausfall;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.standard.AtgVerkehrsDatenKurzZeitIntervallPlLogisch;
import de.bsvrz.dua.plloglve.plloglve.standard.PLFahrStreifen;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.intpuf.IntervallPufferException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Speichert die Ausfallhäufigkeit eine Fahrstreifens über einem gleitenden Tag.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class AusfallFahrStreifen implements ClientReceiverInterface {

	private static final Debug _debug = Debug.getLogger();

	/**
	 * Format der Zeitangabe innerhalb der Betriebsmeldung. 
	 */
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.GERMAN);

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	private IVerwaltung _verwaltung = null;

	/**
	 * Messquerschnitt-Objekt dieses FS
	 */
	private final SystemObject _mq;

	/**
	 * Datensaetze mit Ausfallinformationen der letzten 24h.
	 */
	private final AusfallPuffer gleitenderTag = new AusfallPuffer();

	/**
	 * das Fahrstreifen-Objekt.
	 */
	private final SystemObject _objekt;

	/**
	 * Aktueller Parameter-Datensatz oder null falls nicht vorhanden
	 */
	private AtgVerkehrsDatenKurzZeitIntervallPlLogisch _parameter;

	/**
	 * Zeitpunkt der letzen Prüfung (damit das Prüfintervall eingehalten wird)
	 */
	private Instant _lastCheckTime;


	/**
	 * Vorlage für die Betriebsmeldung
	 */
	private static final MessageTemplate TEMPLATE = new MessageTemplate(
			MessageGrade.WARNING,
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.fixed("Ausfallhäufigkeit innerhalb der letzten 24 Stunden überschritten. Im Zeitraum von "),
	        MessageTemplate.variable("from"),
	        MessageTemplate.fixed(" bis "),
			MessageTemplate.variable("to"),
			MessageTemplate.fixed(" (1 Tag) implausible Fahrstreifenwerte am Fahrstreifen "),
			MessageTemplate.object(),
			MessageTemplate.fixed("("),
			MessageTemplate.variable("mq"),
			MessageTemplate.fixed(") während "),
			MessageTemplate.variable("duration"),
			MessageTemplate.fixed(" (> "),
			MessageTemplate.variable("maxDuration"),
			MessageTemplate.fixed("). "),
	        MessageTemplate.ids()
	).withIdFactory(message -> message.getObject().getPidOrId() + " [DUA-PP-AH]");

	/**
	 * Standardkonstruktor.
	 *  @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 * @param mq
	 */
	protected AusfallFahrStreifen(final IVerwaltung verwaltung,
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
	protected final synchronized void plausibilisiere(final ResultData resultat) {
		final AusfallDatumKomplett ausfallDatum = AusfallDatumKomplett
				.getAusfallDatumVon(resultat);
		if(_parameter != null) {
			if(ausfallDatum != null) {
				try {
					gleitenderTag.add(ausfallDatum);
				}
				catch(final IntervallPufferException e) {
					_debug.error("Fehler beim Intervallpuffer", e);
				}

				long startIntervall = ausfallDatum.getIntervallEnde() - (long) (24 * 60 * 60 * 1000);
				try {
					gleitenderTag.loescheAllesUnterhalbVon(startIntervall);
				}
				catch(final IntervallPufferException e) {
					_debug.error("Fehler beim Intervallpuffer", e);
				}
				Instant now = Instant.ofEpochMilli(ausfallDatum.getIntervallEnde());
				long pruefIntervallAusfall = _parameter.getPruefIntervallAusfall();
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
		} else {
			Debug.getLogger()
					.warning("Fuer Fahrstreifen " + this + //$NON-NLS-1$
							         " wurden noch keine Parameter für die Ausfallhäufigkeit empfangen"); //$NON-NLS-1$
		}
	}

	/**
	 * Prüft den Fahrstreifen auf Ausfall und sendet eine Betriebsmeldung wenn ein Ausfall erkannt wurde
	 * @param fromTime Startzeitpunkt des Prüfintervalls
	 * @param checkTime Endzeitpunkt des Prüfintervalls
	 */
	private synchronized void pruefeAusfall(final Instant fromTime, final Instant checkTime){
		if(_parameter == null) return;
		
		long ausfallZeit = gleitenderTag.getAusfallZeit();
		long maxAusfallZeitProTag = _parameter.getMaxAusfallZeitProTag();
		if(maxAusfallZeitProTag > 0 && ausfallZeit > maxAusfallZeitProTag) {
			OperatingMessage operatingMessage = TEMPLATE.newMessage(_objekt);
			operatingMessage.put("mq", _mq);
			operatingMessage.put("from", formatDate(fromTime));
			operatingMessage.put("to", formatDate(checkTime));
			operatingMessage.put("duration", formatDuration(ausfallZeit));
			operatingMessage.put("maxDuration", formatDuration(maxAusfallZeitProTag));
			operatingMessage.addId("[DUA-PP-AH01]");
			operatingMessage.send();
		}
	}

	/**
	 * Formatiert ein Datum
	 * @param dateTime Zeitpunkt
	 * @return String-Wert
	 */
	public static String formatDate(final Instant dateTime) {
		return DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(dateTime, ZoneId.systemDefault()));
	}

	/**
	 * Formatiert ein Zeitbereich
	 * @param tmp Dauer in Millisekunden
	 * @return Ein Text wie "1 Stunde 13 Minuten"
	 */
	public static String formatDuration(long tmp) {
		long ms = tmp % 1000;
		tmp /= 1000;
		long sec = tmp % 60;
		tmp /= 60;
		long min = tmp % 60;
		tmp /= 60;
		long h = tmp;
		StringBuilder stringBuilder = new StringBuilder();
		if(h >= 1){
			if(h == 1){
				stringBuilder.append("1 Stunde ");
			}
			else {
				stringBuilder.append(h).append(" Stunden ");
			}
		}	
		if(min >= 1){
			if(min == 1){
				stringBuilder.append("1 Minute ");
			}
			else {
				stringBuilder.append(min).append(" Minuten ");
			}
		}	
		if(sec >= 1){
			if(sec == 1){
				stringBuilder.append("1 Sekunde ");
			}
			else {
				stringBuilder.append(sec).append(" Sekunden ");
			}
		}	
		if(ms >= 1){
			if(ms == 1){
				stringBuilder.append("1 Millisekunde ");
			}
			else {
				stringBuilder.append(ms).append(" Millisekunden ");
			}
		}
		
		if(stringBuilder.length() == 0) return "0 Sekunden";
		
		stringBuilder.setLength(stringBuilder.length()-1);
		return stringBuilder.toString();
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

	@Override
	public String toString() {
		return _objekt.toString();
	}
}
