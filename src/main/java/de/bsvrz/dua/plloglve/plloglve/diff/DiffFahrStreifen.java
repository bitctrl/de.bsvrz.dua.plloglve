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

package de.bsvrz.dua.plloglve.plloglve.diff;

import java.util.ArrayList;
import java.util.Collection;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.VariableMitKonstanzZaehler;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;

/**
 * Speichert, wie lange einzelne KZD-Werte eines bestimmten Fahrstreifens in
 * folge konstant sind.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class DiffFahrStreifen implements ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Verbindung zum Verwaltungsmodul.
	 */
	private static IVerwaltung dieVerwaltung = null;

	/**
	 * Datenbeschreibung für Parameter der Differezialkontrolle.
	 */
	private static DataDescription diffParaBeschreibung = null;

	/**
	 * Fahrstreifenbezogene Parameter der Differezialkontrolle.
	 */
	private AtgVerkehrsDatenDifferenzialKontrolleFs parameter = null;

	/**
	 * Variable <code>qKfz</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> qKfzZaehler = new VariableMitKonstanzZaehler<Long>("qKfz"); //$NON-NLS-1$

	/**
	 * Variable <code>qLkw</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> qLkwZaehler = new VariableMitKonstanzZaehler<>("qLkw"); //$NON-NLS-1$

	/**
	 * Variable <code>qPkw</code> mit der Information wie lange diese Variable
	 * schon konstant ist..
	 */
	private final VariableMitKonstanzZaehler<Long> qPkwZaehler = new VariableMitKonstanzZaehler<>("qPkw"); //$NON-NLS-1$

	/**
	 * Variable <code>vKfz</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> vKfzZaehler = new VariableMitKonstanzZaehler<>("vKfz"); //$NON-NLS-1$

	/**
	 * Variable <code>vLkw</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> vLkwZaehler = new VariableMitKonstanzZaehler<>("vLkw"); //$NON-NLS-1$

	/**
	 * Variable <code>vPkw</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> vPkwZaehler = new VariableMitKonstanzZaehler<>("vPkw"); //$NON-NLS-1$

	/**
	 * Variable <code>sKfz</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> sKfzZaehler = new VariableMitKonstanzZaehler<>("sKfz"); //$NON-NLS-1$

	/**
	 * Variable <code>b</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private final VariableMitKonstanzZaehler<Long> bZaehler = new VariableMitKonstanzZaehler<>("b"); //$NON-NLS-1$

	/**
	 * Standardkonstruktor.
	 *
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            ein Systemobjekt eines Fahrstreifens
	 */
	protected DiffFahrStreifen(final IVerwaltung verwaltung, final SystemObject obj) {
		if (DiffFahrStreifen.dieVerwaltung == null) {
			DiffFahrStreifen.dieVerwaltung = verwaltung;
			DiffFahrStreifen.diffParaBeschreibung = new DataDescription(
					DiffFahrStreifen.dieVerwaltung.getVerbindung().getDataModel()
					.getAttributeGroup("atg.verkehrsDatenDifferenzialKontrolleFs"), //$NON-NLS-1$
					DiffFahrStreifen.dieVerwaltung.getVerbindung().getDataModel()
							.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL));

		}

		DiffFahrStreifen.dieVerwaltung.getVerbindung().subscribeReceiver(this, obj,
				DiffFahrStreifen.diffParaBeschreibung, ReceiveOptions.normal(), ReceiverRole.receiver());

	}

	/**
	 * Für die empfangenen Daten wird geprüft, ob innerhalb eines zu
	 * definierenden Zeitraums (parametrierbare Anzahl der Erfassungsintervalle,
	 * parametrierbar je Fahrstreifen) eine Änderung des Messwerts vorliegt.
	 * Liegt eine Ergebniskonstanz für eine frei parametrierbare Anzahl von
	 * Erfassungsintervallen für einzelne (oder alle Werte) vor, so erfolgt eine
	 * Kennzeichnung der Werte als Implausibel und Fehlerhaft. Darüber hinaus
	 * wird eine entsprechende Betriebsmeldung versendet.
	 *
	 * @param resultat
	 *            ein emfangenes FS-KZ-Datum
	 * @return eine gekennzeichnete Kopie des originalen Datensatzes oder
	 *         <code>null</code>, wenn der Datensatz durch die Plausibilisierung
	 *         nicht beanstandet wurde
	 */
	protected Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if ((resultat != null) && (resultat.getData() != null)) {
			if (resultat.getDataDescription().getAttributeGroup().getPid().equals(DUAKonstanten.ATG_KZD)) {
				final Data data = resultat.getData();

				if (parameter != null) {
					final long qKfz = data.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long qLkw = data.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long qPkw = data.getItem("qPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long vPkw = data.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long vLkw = data.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long vKfz = data.getItem("vKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long b = data.getItem("b").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long sKfz = data.getItem("sKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$

					qKfzZaehler.aktualisiere(qKfz);
					qLkwZaehler.aktualisiere(qLkw);
					qPkwZaehler.aktualisiere(qPkw);
					vKfzZaehler.aktualisiere(vKfz);
					vLkwZaehler.aktualisiere(vLkw);
					vPkwZaehler.aktualisiere(vPkw);
					sKfzZaehler.aktualisiere(sKfz);
					bZaehler.aktualisiere(b);

					final Collection<VariableMitKonstanzZaehler<Long>> puffer = new ArrayList<>();
					synchronized (parameter) {
						if ((qKfz > 0) && (qKfzZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzqKfz())) {
							puffer.add(qKfzZaehler);
						}
						if ((qLkw > 0) && (qLkwZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzqLkw())) {
							puffer.add(qLkwZaehler);
						}
						if ((qPkw > 0) && (qPkwZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzqPkw())) {
							puffer.add(qPkwZaehler);
						}

						if ((vKfz > 0) && (vKfzZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzvKfz())) {
							puffer.add(vKfzZaehler);
						}
						if ((vLkw > 0) && (vLkwZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzvLkw())) {
							puffer.add(vLkwZaehler);
						}
						if ((vPkw > 0) && (vPkwZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzvPkw())) {
							puffer.add(vPkwZaehler);
						}

						if ((sKfz > 0)
								&& (sKfzZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzStreung())) {
							puffer.add(sKfzZaehler);
						}
						if ((b > 0) && (bZaehler.getWertIstKonstantSeit() > parameter.getMaxAnzKonstanzBelegung())) {
							puffer.add(bZaehler);
						}

						if (!puffer.isEmpty()) {
							copy = data.createModifiableCopy();
							for (final VariableMitKonstanzZaehler<Long> wert : puffer) {
								copy.getItem(wert.getName()).getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$
								copy.getItem(wert.getName()).getItem("Status").getItem("MessWertErsetzung")
										.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$

								DUAUtensilien.sendeBetriebsmeldung(DiffFahrStreifen.dieVerwaltung.getVerbindung(),
										DiffFahrStreifen.dieVerwaltung.getBmvIdKonverter(), MessageGrade.WARNING,
										resultat.getObject(), "Fahrstreifen " + //$NON-NLS-1$
												resultat.getObject() + ": " + wert); //$NON-NLS-1$
							}
						}
					}
				} else {
					DiffFahrStreifen.LOGGER.warning("Fuer Fahrstreifen " + this + //$NON-NLS-1$
							" wurden noch keine Parameter für die Differenzialkontrolle empfangen"); //$NON-NLS-1$
				}
			}
		}

		return copy;
	}

	@Override
	public void update(final ResultData[] davParameterFeld) {
		if (davParameterFeld != null) {
			for (final ResultData davParameter : davParameterFeld) {
				if ((davParameter != null) && (davParameter.getData() != null)) {
					synchronized (this) {
						parameter = new AtgVerkehrsDatenDifferenzialKontrolleFs(davParameter.getData());
					}
				}
			}
		}
	}

}
