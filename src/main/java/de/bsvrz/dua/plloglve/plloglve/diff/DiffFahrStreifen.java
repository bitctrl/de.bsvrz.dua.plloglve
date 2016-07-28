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

package de.bsvrz.dua.plloglve.plloglve.diff;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.VariableMitKonstanzZaehler;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Speichert, wie lange einzelne KZD-Werte eines bestimmten Fahrstreifens in folge konstant sind.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class DiffFahrStreifen implements ClientReceiverInterface {

	/**
	 * Vorlage der Betriebsmeldung
	 */
	public static final MessageTemplate TEMPLATE_DIFF = new MessageTemplate(
			MessageGrade.INFORMATION,
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.fixed("Grenzwert für Messwertkonstanz bei Differenzialkontrolle für "),
			MessageTemplate.set("attr", " und ", "Attribut ", "Attribute "),
			MessageTemplate.fixed(" überschritten am Fahrstreifen "),
			MessageTemplate.object(),
			MessageTemplate.fixed("("),
			MessageTemplate.variable("mq"),
			MessageTemplate.fixed("), da "),
			MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". Wert wird auf fehlerhaft gesetzt. "),
			MessageTemplate.ids()
	).withIdFactory(new MessageIdFactory() {
		@Override
		public String generateMessageId(final OperatingMessage message) {
			return message.getObject().getPidOrId() + " [DUA-PP-VDK]";
		}
	});

	private static final Debug _debug = Debug.getLogger();
	/**
	 * Messquerschnitt-Objekt zu diesem FS
	 */
	private final SystemObject _mq;
	/**
	 * Fahrstreifen-Objekt
	 */
	private final SystemObject _objekt;
	/**
	 * Variable <code>qKfz</code> mit der Information wie lange diese Variable schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> qKfzZaehler = new VariableMitKonstanzZaehler<Long>(
			"qKfz"); //$NON-NLS-1$
	/**
	 * Variable <code>qLkw</code> mit der Information wie lange diese Variable schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> qLkwZaehler = new VariableMitKonstanzZaehler<Long>(
			"qLkw"); //$NON-NLS-1$
	/**
	 * Variable <code>qPkw</code> mit der Information wie lange diese Variable schon konstant ist..
	 */
	private final VariableMitKonstanzZaehler<Long> qPkwZaehler = new VariableMitKonstanzZaehler<Long>(
			"qPkw"); //$NON-NLS-1$
	/**
	 * Variable <code>vKfz</code> mit der Information wie lange diese Variable schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> vKfzZaehler = new VariableMitKonstanzZaehler<Long>(
			"vKfz"); //$NON-NLS-1$
	/**
	 * Variable <code>vLkw</code> mit der Information wie lange diese Variable schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> vLkwZaehler = new VariableMitKonstanzZaehler<Long>(
			"vLkw"); //$NON-NLS-1$
	/**
	 * Variable <code>vPkw</code> mit der Information wie lange diese Variable schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> vPkwZaehler = new VariableMitKonstanzZaehler<Long>(
			"vPkw"); //$NON-NLS-1$
	/**
	 * Variable <code>sKfz</code> mit der Information wie lange diese Variable schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> sKfzZaehler = new VariableMitKonstanzZaehler<Long>(
			"sKfz"); //$NON-NLS-1$
	/**
	 * Variable <code>b</code> mit der Information wie lange diese Variable schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> bZaehler = new VariableMitKonstanzZaehler<Long>(
			"b"); //$NON-NLS-1$
	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	private IVerwaltung dieVerwaltung = null;
	/**
	 * Datenbeschreibung für Parameter der Differenzialkontrolle.
	 */
	private DataDescription diffParaBeschreibung = null;
	/**
	 * Fahrstreifenbezogene Parameter der Differezialkontrolle.
	 */
	private AtgVerkehrsDatenDifferenzialKontrolleFs parameter = null;

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung Verbindung zum Verwaltungsmodul
	 * @param obj        Fahrstreifen-Systemobjekt
	 * @param mq         Messquerschnitt zu diesem Fahrstreifen (kann null sein in Ausnahmefällen)
	 */
	protected DiffFahrStreifen(final IVerwaltung verwaltung,
			final SystemObject obj, final SystemObject mq) {
		_objekt = obj;
		dieVerwaltung = verwaltung;
		_mq = mq;
		AttributeGroup attributeGroup = dieVerwaltung
				.getVerbindung()
				.getDataModel()
				.getAttributeGroup(
						"atg.verkehrsDatenDifferenzialKontrolleFs2");
		if(attributeGroup == null) {
			_debug.warning("Attributgruppe " + "atg.verkehrsDatenDifferenzialKontrolleFs2" + " ist nicht im Datenmodell vorhanden, die Differenzialkontrolle nicht durchgeführt");
			return;
		}
		diffParaBeschreibung = new DataDescription(
				attributeGroup, //$NON-NLS-1$
				dieVerwaltung.getVerbindung().getDataModel()
						.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL)
		);

		dieVerwaltung.getVerbindung().subscribeReceiver(this, obj,
		                                                diffParaBeschreibung, ReceiveOptions.normal(),
		                                                ReceiverRole.receiver()
		);

	}

	/**
	 * Für die empfangenen Daten wird geprüft, ob innerhalb eines zu definierenden Zeitraums (parametrierbare Anzahl der Erfassungsintervalle, parametrierbar je
	 * Fahrstreifen) eine Änderung des Messwerts vorliegt. Liegt eine Ergebniskonstanz für eine frei parametrierbare Anzahl von Erfassungsintervallen für
	 * einzelne (oder alle Werte) vor, so erfolgt eine Kennzeichnung der Werte als Implausibel und Fehlerhaft. Darüber hinaus wird eine entsprechende
	 * Betriebsmeldung versendet.
	 *
	 * @param resultat ein emfangenes FS-KZ-Datum
	 * @return eine gekennzeichnete Kopie des originalen Datensatzes oder <code>null</code>, wenn der Datensatz durch die Plausibilisierung nicht beanstandet
	 * wurde
	 */
	protected Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if(resultat != null && resultat.getData() != null) {
			if(resultat.getDataDescription().getAttributeGroup().getPid()
					.equals(DUAKonstanten.ATG_KZD)) {
				final Data data = resultat.getData();

				synchronized(this) {
					if(parameter != null) {
						long qKfz = data
								.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
						long qLkw = data
								.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
						long qPkw = data
								.getItem("qPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
						long vPkw = data
								.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
						long vLkw = data
								.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
						long vKfz = data
								.getItem("vKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
						long b = data
								.getItem("b").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
						long sKfz = data
								.getItem("sKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$

						qKfzZaehler.aktualisiere(qKfz < 1 ? null : qKfz);
						qLkwZaehler.aktualisiere(qLkw < 1 ? null : qLkw);
						qPkwZaehler.aktualisiere(qPkw < 1 ? null : qPkw);
						vKfzZaehler.aktualisiere(vKfz < 1 ? null : vKfz);
						vLkwZaehler.aktualisiere(vLkw < 1 ? null : vLkw);
						vPkwZaehler.aktualisiere(vPkw < 1 ? null : vPkw);
						sKfzZaehler.aktualisiere(sKfz < 1 ? null : sKfz);
						bZaehler.aktualisiere(b < 6 ? null : b);

						final Collection<VariableMitKonstanzZaehler<Long>> puffer = new ArrayList<VariableMitKonstanzZaehler<Long>>();

						OperatingMessage messageText = TEMPLATE_DIFF.newMessage(resultat.getObject());

						if(parameter.getMaxAnzKonstanzQ() > 0 
								&& qKfz > 0 && qKfzZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzQ()
								&& qLkw > 0 && qLkwZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzQ()
								&& qPkw > 0 && qPkwZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzQ()) {
							puffer.add(qKfzZaehler);
							puffer.add(qLkwZaehler);
							puffer.add(qPkwZaehler);
							messageText.addId("[DUA-PP-VDK01]");
							messageText.add("attr", "q", true);
							messageText.add("values", "q konstant " + qKfzZaehler.getWertIstKonstantSeit() + " Intervalle > " + parameter.getMaxAnzKonstanzQ() + " Intervalle maximal", true);
						}

						if(parameter.getMaxAnzKonstanzV() > 0 
								&& (qKfz == 0 || vKfzZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzV())
								&& (qLkw == 0 || vLkwZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzV())
								&& (qPkw == 0 || vPkwZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzV())
								&& (qKfz > 0 || qLkw > 0 || qPkw > 0)) {
							puffer.add(vKfzZaehler);
							puffer.add(vLkwZaehler);
							puffer.add(vPkwZaehler);
							messageText.addId("[DUA-PP-VDK02]");
							messageText.add("attr", "v", true);
							messageText.add("values", "v konstant " + vKfzZaehler.getWertIstKonstantSeit() + " Intervalle > " + parameter.getMaxAnzKonstanzV() + " Intervalle maximal", true);
						}

						if(parameter.getMaxAnzKonstanzStreung() > 0 
								&& sKfz > 0 && sKfzZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzStreung()) {
							puffer.add(sKfzZaehler);
							messageText.addId("[DUA-PP-VDK03]");
							messageText.add("attr", "s");
							messageText.add("values", "s konstant " + sKfzZaehler.getWertIstKonstantSeit() + " Intervalle > " + parameter.getMaxAnzKonstanzStreung() + " Intervalle maximal", true);
						}
						if(parameter.getMaxAnzKonstanzBelegung() > 0
								&& b > 0 && bZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzBelegung()) {
							puffer.add(bZaehler);
							messageText.addId("[DUA-PP-VDK04]");
							messageText.add("attr", "b");
							messageText.add("values", "b konstant " + bZaehler.getWertIstKonstantSeit() + " Intervalle > " + parameter.getMaxAnzKonstanzBelegung() + " Intervalle maximal", true);
						}

						if(!puffer.isEmpty()) {
							copy = data.createModifiableCopy();
							for(final VariableMitKonstanzZaehler<? extends Long> wert : puffer) {
								copy.getItem(wert.getName())
										.getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$			
								copy.getItem(wert.getName())
										.getItem("Status")
										.getItem("MessWertErsetzung")
										.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$

							}
							messageText.put("mq", _mq);
							messageText.send();
						}
					}
					else {
						Debug.getLogger()
								.warning("Fuer Fahrstreifen " + this + //$NON-NLS-1$
										         " wurden noch keine Parameter für die Differenzialkontrolle empfangen"); //$NON-NLS-1$
					}
				}
			}
		}

		return copy;
	}

	public void update(final ResultData[] davParameterFeld) {
		if(davParameterFeld != null) {
			for(final ResultData davParameter : davParameterFeld) {
				if(davParameter != null && davParameter.getData() != null) {
					synchronized(this) {
						parameter = new AtgVerkehrsDatenDifferenzialKontrolleFs(
								davParameter.getData());
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
