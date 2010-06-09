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
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;

/**
 * Speichert, wie lange einzelne KZD-Werte eines bestimmten Fahrstreifens in
 * folge konstant sind.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class DiffFahrStreifen extends AbstractSystemObjekt implements
		ClientReceiverInterface {

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
	private VariableMitKonstanzZaehler<Long> qKfzZaehler = new VariableMitKonstanzZaehler<Long>(
			"qKfz"); //$NON-NLS-1$

	/**
	 * Variable <code>qLkw</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private VariableMitKonstanzZaehler<Long> qLkwZaehler = new VariableMitKonstanzZaehler<Long>(
			"qLkw"); //$NON-NLS-1$

	/**
	 * Variable <code>qPkw</code> mit der Information wie lange diese Variable
	 * schon konstant ist..
	 */
	private VariableMitKonstanzZaehler<Long> qPkwZaehler = new VariableMitKonstanzZaehler<Long>(
			"qPkw"); //$NON-NLS-1$

	/**
	 * Variable <code>vKfz</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private VariableMitKonstanzZaehler<Long> vKfzZaehler = new VariableMitKonstanzZaehler<Long>(
			"vKfz"); //$NON-NLS-1$

	/**
	 * Variable <code>vLkw</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private VariableMitKonstanzZaehler<Long> vLkwZaehler = new VariableMitKonstanzZaehler<Long>(
			"vLkw"); //$NON-NLS-1$

	/**
	 * Variable <code>vPkw</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private VariableMitKonstanzZaehler<Long> vPkwZaehler = new VariableMitKonstanzZaehler<Long>(
			"vPkw"); //$NON-NLS-1$

	/**
	 * Variable <code>sKfz</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private VariableMitKonstanzZaehler<Long> sKfzZaehler = new VariableMitKonstanzZaehler<Long>(
			"sKfz"); //$NON-NLS-1$

	/**
	 * Variable <code>b</code> mit der Information wie lange diese Variable
	 * schon konstant ist.
	 */
	private VariableMitKonstanzZaehler<Long> bZaehler = new VariableMitKonstanzZaehler<Long>(
			"b"); //$NON-NLS-1$

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            ein Systemobjekt eines Fahrstreifens
	 */
	protected DiffFahrStreifen(final IVerwaltung verwaltung,
			final SystemObject obj) {
		super(obj);

		if (dieVerwaltung == null) {
			dieVerwaltung = verwaltung;
			diffParaBeschreibung = new DataDescription(dieVerwaltung
					.getVerbindung().getDataModel().getAttributeGroup(
							"atg.verkehrsDatenDifferenzialKontrolleFs"), //$NON-NLS-1$
					dieVerwaltung.getVerbindung().getDataModel().getAspect(
							DaVKonstanten.ASP_PARAMETER_SOLL));

		}

		dieVerwaltung.getVerbindung().subscribeReceiver(this, obj,
				diffParaBeschreibung, ReceiveOptions.normal(),
				ReceiverRole.receiver());
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
	 *         <code>null</code>, wenn der Datensatz durch die
	 *         Plausibilisierung nicht beanstandet wurde
	 */
	protected Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if (resultat != null && resultat.getData() != null) {
			if (resultat.getDataDescription().getAttributeGroup().getPid()
					.equals(DUAKonstanten.ATG_KZD)) {
				Data data = resultat.getData();

				if (this.parameter != null) {
					final long qKfz = data
							.getItem("qKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long qLkw = data
							.getItem("qLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long qPkw = data
							.getItem("qPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long vPkw = data
							.getItem("vPkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long vLkw = data
							.getItem("vLkw").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long vKfz = data
							.getItem("vKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long b = data
							.getItem("b").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
					final long sKfz = data
							.getItem("sKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$

					this.qKfzZaehler.aktualisiere(qKfz);
					this.qLkwZaehler.aktualisiere(qLkw);
					this.qPkwZaehler.aktualisiere(qPkw);
					this.vKfzZaehler.aktualisiere(vKfz);
					this.vLkwZaehler.aktualisiere(vLkw);
					this.vPkwZaehler.aktualisiere(vPkw);
					this.sKfzZaehler.aktualisiere(sKfz);
					this.bZaehler.aktualisiere(b);

					Collection<VariableMitKonstanzZaehler<Long>> puffer = new ArrayList<VariableMitKonstanzZaehler<Long>>();
					synchronized (this.parameter) {
						if (qKfz > 0
								&& this.qKfzZaehler.getWertIstKonstantSeit() > this.parameter
										.getMaxAnzKonstanzqKfz()) {
							puffer.add(this.qKfzZaehler);
						}
						if (qLkw > 0
								&& this.qLkwZaehler.getWertIstKonstantSeit() > this.parameter
										.getMaxAnzKonstanzqLkw()) {
							puffer.add(this.qLkwZaehler);
						}
						if (qPkw > 0
								&& this.qPkwZaehler.getWertIstKonstantSeit() > this.parameter
										.getMaxAnzKonstanzqPkw()) {
							puffer.add(this.qPkwZaehler);
						}

						if (vKfz > 0
								&& this.vKfzZaehler.getWertIstKonstantSeit() > this.parameter
										.getMaxAnzKonstanzvKfz()) {
							puffer.add(this.vKfzZaehler);
						}
						if (vLkw > 0
								&& this.vLkwZaehler.getWertIstKonstantSeit() > this.parameter
										.getMaxAnzKonstanzvLkw()) {
							puffer.add(this.vLkwZaehler);
						}
						if (vPkw > 0
								&& this.vPkwZaehler.getWertIstKonstantSeit() > this.parameter
										.getMaxAnzKonstanzvPkw()) {
							puffer.add(this.vPkwZaehler);
						}

						if (sKfz > 0
								&& this.sKfzZaehler.getWertIstKonstantSeit() > this.parameter
										.getMaxAnzKonstanzStreung()) {
							puffer.add(this.sKfzZaehler);
						}
						if (b > 0
								&& this.bZaehler.getWertIstKonstantSeit() > this.parameter
										.getMaxAnzKonstanzBelegung()) {
							puffer.add(this.bZaehler);
						}

						if (!puffer.isEmpty()) {
							copy = data.createModifiableCopy();
							for (VariableMitKonstanzZaehler<Long> wert : puffer) {
								copy.getItem(wert.getName()).getUnscaledValue(
										"Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$			
								copy
										.getItem(wert.getName())
										.getItem("Status")
										.getItem("MessWertErsetzung")
										.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
								DUAUtensilien.sendeBetriebsmeldung(
										dieVerwaltung.getVerbindung(),
										MessageGrade.WARNING, resultat.getObject(),
										"Fahrstreifen " + //$NON-NLS-1$
												this + ": " + wert); //$NON-NLS-1$
							}
						}
					}
				} else {
					Debug
							.getLogger()
							.warning("Fuer Fahrstreifen " + this + //$NON-NLS-1$
									" wurden noch keine Parameter für die Differenzialkontrolle empfangen"); //$NON-NLS-1$
				}
			}
		}

		return copy;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] davParameterFeld) {
		if (davParameterFeld != null) {
			for (ResultData davParameter : davParameterFeld) {
				if (davParameter != null && davParameter.getData() != null) {
					synchronized (this) {
						this.parameter = new AtgVerkehrsDatenDifferenzialKontrolleFs(
								davParameter.getData());
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemObjektTyp getTyp() {
		return new SystemObjektTyp() {

			public Class<? extends SystemObjekt> getKlasse() {
				return DiffFahrStreifen.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}

		};
	}

}
