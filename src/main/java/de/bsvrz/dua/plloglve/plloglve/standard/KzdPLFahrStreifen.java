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

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.vew.VerwaltungPlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse zum Durchführen der speziellen Standardplausibilisierung LVE für LZD.
 * Diese Klasse macht nichts weiter, als sich auf die Grenzwertparameter
 * anzumelden und einige Funktionen zur Plausibilisierung von LZD zur Verfügung
 * zu stellen
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class KzdPLFahrStreifen extends AbstraktPLFahrStreifen {

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
			"vgKfz", //$NON-NLS-1$
			"tNetto", //$NON-NLS-1$
			"b" }; //$NON-NLS-1$

	/**
	 * Standartdkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	public KzdPLFahrStreifen(final IVerwaltungMitGuete verwaltung,
			final SystemObject obj) {
		super(verwaltung, obj);
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

		veraenderbaresDatum.getItem("qKfz").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qLkw").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("qPkw").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$

		veraenderbaresDatum.getItem("vKfz").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vLkw").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vPkw").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$

		final int b = veraenderbaresDatum
				.getItem("b").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (b == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
					.getItem("b").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
					.getItem("b").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final long tNetto = veraenderbaresDatum
				.getItem("tNetto").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (tNetto == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
					.getItem("tNetto").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
					.getItem("tNetto").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final int sKfz = veraenderbaresDatum
				.getItem("sKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (sKfz == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
					.getItem("sKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
					.getItem("sKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final int vgKfz = veraenderbaresDatum
				.getItem("vgKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		if (vgKfz == DUAKonstanten.NICHT_ERMITTELBAR) {
			veraenderbaresDatum
					.getItem("vgKfz").getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			veraenderbaresDatum
					.getItem("vgKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
		}

		veraenderbaresDatum.getItem("b").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("tNetto").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("sKfz").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		veraenderbaresDatum.getItem("vgKfz").getItem("Status").getItem(
				"MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void ueberpruefe(final Data data, final ResultData resultat) {

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
		final long vgKfz = data
				.getItem("vgKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long tNetto = data
				.getItem("tNetto").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long t = data.getTimeValue("T").getMillis();
		final long b = data.getItem("b").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$

		long vgKfzLetztesIntervall = -4;
		if (letztesKZDatum != null) {
			if (letztesKZDatum.getData() != null) {
				if (t == resultat.getDataTime() - letztesKZDatum.getDataTime()) {
					vgKfzLetztesIntervall = letztesKZDatum.getData().getItem(
							"vgKfz").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$
				}
			}
		}

		/**
		 * Regel Nr.1 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if (qKfz == 0) {
			if (!(qLkw == 0 && qPkw == 0)) {
				Debug
						.getLogger()
						.fine(
								VerwaltungPlPruefungLogischLVE
										.getPlLogIdent(resultat)
										+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(1) verletzt:\n"
										+ "Wenn qKfz == 0 dann muss qLkw == 0 und qPkw == 0 sein. Aber:\nqKfz = "
										+ VerwaltungPlPruefungLogischLVE
												.getWertIdent(qKfz)
										+ "\nqLkw = "
										+ VerwaltungPlPruefungLogischLVE
												.getWertIdent(qLkw)
										+ "\nqPkw = "
										+ VerwaltungPlPruefungLogischLVE
												.getWertIdent(qPkw));
				setAllesImplausibel(data);
				return;
			}
		}

		if (data.getUnscaledValue("ArtMittelwertbildung").longValue() == DUAKonstanten.MWB_ARITHMETISCH) { //$NON-NLS-1$

			/**
			 * Regel Nr.2 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
			 */
			if(PlLogischLVEStandard.isBaWuePatchAktiv()) {
				if (qKfz >= 0 && qLkw >= 0) {
					if (qKfz - qLkw == 0) {
						if (!(qPkw == 0 && (vPkw == DUAKonstanten.NICHT_ERMITTELBAR || vPkw == 0))) {
							Debug
									.getLogger()
									.fine(
											VerwaltungPlPruefungLogischLVE
													.getPlLogIdent(resultat)
													+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(2) verletzt:\n"
													+ "Wenn (qKfz >= 0 und qLkw >= 0) und (qKfz - qLkw == 0), dann muss qPkw == 0 und vPkw == nicht ermittelbar sein. Aber:\nqKfz = "
													+ VerwaltungPlPruefungLogischLVE
															.getWertIdent(qKfz)
													+ "\nqLkw = "
													+ VerwaltungPlPruefungLogischLVE
															.getWertIdent(qLkw)
													+ "\nqPkw = "
													+ VerwaltungPlPruefungLogischLVE
															.getWertIdent(qPkw)
													+ "\nvPkw = "
													+ VerwaltungPlPruefungLogischLVE
															.getWertIdent(vPkw));
							setAllesImplausibel(data);						
							return;
						}
					}
				}
			} else {
				if (qKfz >= 0 && qLkw >= 0) {
					if (qKfz - qLkw == 0) {
						if (!(qPkw == 0 && vPkw == DUAKonstanten.NICHT_ERMITTELBAR)) {
							Debug
									.getLogger()
									.fine(
											VerwaltungPlPruefungLogischLVE
													.getPlLogIdent(resultat)
													+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(2) verletzt:\n"
													+ "Wenn (qKfz >= 0 und qLkw >= 0) und (qKfz - qLkw == 0), dann muss qPkw == 0 und vPkw == nicht ermittelbar sein. Aber:\nqKfz = "
													+ VerwaltungPlPruefungLogischLVE
															.getWertIdent(qKfz)
													+ "\nqLkw = "
													+ VerwaltungPlPruefungLogischLVE
															.getWertIdent(qLkw)
													+ "\nqPkw = "
													+ VerwaltungPlPruefungLogischLVE
															.getWertIdent(qPkw)
													+ "\nvPkw = "
													+ VerwaltungPlPruefungLogischLVE
															.getWertIdent(vPkw));
							setAllesImplausibel(data);						
							return;
						}
					}
				}
			}

			/**
			 * Regel Nr.3 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
			 */
			if(PlLogischLVEStandard.isBaWuePatchAktiv()) {
				if (qLkw == 0) {
					if (!(vLkw == DUAKonstanten.NICHT_ERMITTELBAR)) {
						data.getItem("vLkw").getUnscaledValue("Wert").set(0);
					}
				}
			} else {
				if (qLkw == 0) {
					if (!(vLkw == DUAKonstanten.NICHT_ERMITTELBAR)) {
						Debug.getLogger()
								.fine(VerwaltungPlPruefungLogischLVE
										.getPlLogIdent(resultat)
										+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(3) verletzt:\n"
										+ "Wenn qLkw == 0, dann muss vLkw auf nicht ermittelbar stehen. Aber:\nvLkw = "
										+ VerwaltungPlPruefungLogischLVE
												.getWertIdent(vLkw));
						setAllesImplausibel(data);
						return;
					}
				}
			}

			/**
			 * Regel Nr.4 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
			 */
			if (PlLogischLVEStandard.isBaWuePatchAktiv()) {
				if (qPkw == 0) {
					if (!(vPkw == DUAKonstanten.NICHT_ERMITTELBAR)) {
						data.getItem("vPkw").getUnscaledValue("Wert").set(0);
					}
				}
			} else {
				if (qPkw == 0) {
					if (!(vPkw == DUAKonstanten.NICHT_ERMITTELBAR)) {
						Debug.getLogger()
								.fine(VerwaltungPlPruefungLogischLVE
										.getPlLogIdent(resultat)
										+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(4) verletzt:\n"
										+ "Wenn qPkw == 0, dann muss vPkw auf nicht ermittelbar stehen. Aber:\nvPkw = "
										+ VerwaltungPlPruefungLogischLVE
												.getWertIdent(vPkw));
						setAllesImplausibel(data);
						return;
					}
				}
			}
		}

		/**
		 * Regel Nr.5 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if (qKfz >= 0 && qLkw >= 0) {
			if (qKfz < qLkw) {
				Debug
						.getLogger()
						.fine(
								VerwaltungPlPruefungLogischLVE
										.getPlLogIdent(resultat)
										+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(5) verletzt:\n"
										+ "Wenn qKfz >= 0 und qLkw >= 0, dann muss qKfz >= qLkw sein. Aber:\nqKfz = "
										+ VerwaltungPlPruefungLogischLVE
												.getWertIdent(qKfz)
										+ "\nqLkw = "
										+ VerwaltungPlPruefungLogischLVE
												.getWertIdent(qLkw));
				setAllesImplausibel(data);
				return;
			}
		}

		// /**
		// * Regel Nr.6 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		// */
		// if (qKfz >= 0 && qLkw >= 0) {
		// if (qKfz - qLkw > 0) {
		// if (vPkw >= 0) {
		// if (!(0 < vPkw)) {
		// Debug
		// .getLogger()
		// .fine(
		// VerwaltungPlPruefungLogischLVE
		// .getPlLogIdent(resultat)
		// + "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(6)
		// verletzt:\n"
		// + "Wenn qKfz >= 0 und qLkw >= 0 und qKfz - qLkw > 0, dann muss vPkw >
		// 0 sein. Aber:\nqKfz = "
		// + VerwaltungPlPruefungLogischLVE
		// .getWertIdent(qKfz)
		// + "\nqLkw = "
		// + VerwaltungPlPruefungLogischLVE
		// .getWertIdent(qLkw)
		// + "\nvPkw = "
		// + VerwaltungPlPruefungLogischLVE
		// .getWertIdent(vPkw));
		// setAllesImplausibel(data);
		// return;
		// }
		// }
		// }
		// }
		//
		// /**
		// * Regel Nr.7 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		// */
		// if (qKfz > 0) {
		// if (vKfz >= 0) {
		// if (!(0 < vKfz)) {
		// Debug
		// .getLogger()
		// .fine(
		// VerwaltungPlPruefungLogischLVE
		// .getPlLogIdent(resultat)
		// + "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(7)
		// verletzt:\n"
		// + "Wenn qKfz > 0 und vKfz >= 0, dann muss vKfz > 0 sein. Aber:\nqKfz
		// = "
		// + VerwaltungPlPruefungLogischLVE
		// .getWertIdent(qKfz)
		// + "\nvKfz = "
		// + VerwaltungPlPruefungLogischLVE
		// .getWertIdent(vKfz));
		// setAllesImplausibel(data);
		// return;
		// }
		// }
		// }
		//
		// /**
		// * Regel Nr.8 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		// */
		// if (qLkw > 0) {
		// if (vLkw >= 0) {
		// if (!(0 < vLkw)) {
		// Debug
		// .getLogger()
		// .fine(
		// VerwaltungPlPruefungLogischLVE
		// .getPlLogIdent(resultat)
		// + "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(8)
		// verletzt:\n"
		// + "Wenn qLkw > 0, dann muss vLkw > 0 sein. Aber:\nqLkw = "
		// + VerwaltungPlPruefungLogischLVE
		// .getWertIdent(qLkw)
		// + "\nvLkw = "
		// + VerwaltungPlPruefungLogischLVE
		// .getWertIdent(vLkw));
		// setAllesImplausibel(data);
		// return;
		// }
		// }
		// }

		/**
		 * Regel Nr.6 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if (qKfz >= 0 && qLkw >= 0) {
			if (qKfz - qLkw > 0) {
				if (vPkw >= 0 || vPkw == DUAKonstanten.NICHT_ERMITTELBAR) {
					if (!(0 < vPkw || vPkw == DUAKonstanten.NICHT_ERMITTELBAR)) {
						Debug
								.getLogger()
								.fine(
										VerwaltungPlPruefungLogischLVE
												.getPlLogIdent(resultat)
												+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(6) verletzt:\n"
												+ "Wenn qKfz >= 0 und qLkw >= 0 und qKfz - qLkw > 0, dann muss vPkw > 0 sein. Aber:\nqKfz = "
												+ VerwaltungPlPruefungLogischLVE
														.getWertIdent(qKfz)
												+ "\nqLkw = "
												+ VerwaltungPlPruefungLogischLVE
														.getWertIdent(qLkw)
												+ "\nvPkw = "
												+ VerwaltungPlPruefungLogischLVE
														.getWertIdent(vPkw));
						setAllesImplausibel(data);
						return;
					}
				}
			}
		}

		/**
		 * Regel Nr.7 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if (qKfz > 0) {
			if (vKfz >= 0 || vKfz == DUAKonstanten.NICHT_ERMITTELBAR) {
				if (!(0 < vKfz || vKfz == DUAKonstanten.NICHT_ERMITTELBAR)) {
					Debug
							.getLogger()
							.fine(
									VerwaltungPlPruefungLogischLVE
											.getPlLogIdent(resultat)
											+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(7) verletzt:\n"
											+ "Wenn qKfz > 0 und vKfz >= 0, dann muss vKfz > 0 sein. Aber:\nqKfz = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(qKfz)
											+ "\nvKfz = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(vKfz));
					setAllesImplausibel(data);
					return;
				}
			}
		}

		/**
		 * Regel Nr.8 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if (qLkw > 0) {
			if (vLkw >= 0 || vLkw == DUAKonstanten.NICHT_ERMITTELBAR) {
				if (!(0 < vLkw || vLkw == DUAKonstanten.NICHT_ERMITTELBAR)) {
					Debug
							.getLogger()
							.fine(
									VerwaltungPlPruefungLogischLVE
											.getPlLogIdent(resultat)
											+ "\nSetze ALLES auf implausibel und fehlerhaft weil Regel(8) verletzt:\n"
											+ "Wenn qLkw > 0, dann muss vLkw > 0 sein. Aber:\nqLkw = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(qLkw)
											+ "\nvLkw = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(vLkw));
					setAllesImplausibel(data);
					return;
				}
			}
		}

		/**
		 * Regel Nr.9 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if(PlLogischLVEStandard.isBaWuePatchAktiv()) {
			if (tNetto >= 0) {
				if (!(0 < tNetto && tNetto <= t)) {
					if(qKfz > 0) {
						Debug
								.getLogger()
								.fine(
										VerwaltungPlPruefungLogischLVE
												.getPlLogIdent(resultat)
												+ "\nSetze tNetto auf implausibel und fehlerhaft weil Regel(9) verletzt:\n"
												+ "Wenn tNetto muss im Intervall (0, T] liegen. Aber:\ntNetto = "
												+ VerwaltungPlPruefungLogischLVE
														.getWertIdent(tNetto)
												+ "\nT = "
												+ VerwaltungPlPruefungLogischLVE
														.getWertIdent(t));
						data
								.getItem("tNetto").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
						data.getItem("tNetto").getItem("Status").getItem(
								"MessWertErsetzung")
								.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
					}
				}
			}			
		} else {
			if (tNetto >= 0) {
				if (!(0 < tNetto && tNetto <= t)) {
					Debug
							.getLogger()
							.fine(
									VerwaltungPlPruefungLogischLVE
											.getPlLogIdent(resultat)
											+ "\nSetze tNetto auf implausibel und fehlerhaft weil Regel(9) verletzt:\n"
											+ "Wenn tNetto muss im Intervall (0, T] liegen. Aber:\ntNetto = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(tNetto)
											+ "\nT = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(t));
					data
							.getItem("tNetto").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
					data.getItem("tNetto").getItem("Status").getItem(
							"MessWertErsetzung")
							.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				}
			}			
		}

		/**
		 * Regel Nr.10 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if (qKfz == 0) {
			if (vgKfz >= 0 && vgKfzLetztesIntervall >= 0) {
				if (!(vgKfz == vgKfzLetztesIntervall)) {
					Debug
							.getLogger()
							.fine(
									VerwaltungPlPruefungLogischLVE
											.getPlLogIdent(resultat)
											+ "\nSetze vgKfz auf implausibel und fehlerhaft weil Regel(10) verletzt:\n"
											+ "Wenn qKfz == 0 und vgKfz(t) >= 0 und vgKfz(T-1) >= 0, dann muss vgKfz(t) >= 0 == vgKfz(T-1) sein. Aber:\nqKfz = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(qKfz)
											+ "\nvgKfz(t) = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(vgKfz)
											+ "\nvgKfz(t-T) = "
											+ VerwaltungPlPruefungLogischLVE
													.getWertIdent(vgKfzLetztesIntervall));
					data
							.getItem("vgKfz").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
					data
							.getItem("vgKfz")
							.getItem("Status")
							.getItem("MessWertErsetzung")
							.getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				}
			}
		}

		/**
		 * Regel Nr.11 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
		 */
		if (parameterAtgLog != null) {
			synchronized (parameterAtgLog) {
				if (vKfz >= 0 && parameterAtgLog.getVKfzGrenz() >= 0) {
					if (vKfz > parameterAtgLog.getVKfzGrenz()) {
						if (b >= 0 && parameterAtgLog.getBGrenz() >= 0) {
							if (!(b < parameterAtgLog.getBGrenz())) {
								Debug
										.getLogger()
										.fine(
												VerwaltungPlPruefungLogischLVE
														.getPlLogIdent(resultat)
														+ "\nSetze b auf implausibel und fehlerhaft weil Regel(11) verletzt:\n"
														+ "Wenn vKfz > vKfzGrenz dann muss b >= bGrenz sein. Aber:\nvKfz = "
														+ VerwaltungPlPruefungLogischLVE
																.getWertIdent(vKfz)
														+ "\nvKfzGrenz = "
														+ VerwaltungPlPruefungLogischLVE
																.getWertIdent(parameterAtgLog
																		.getVKfzGrenz())
														+ "\nb = "
														+ VerwaltungPlPruefungLogischLVE
																.getWertIdent(b)
														+ "\nbGrenz = "
														+ VerwaltungPlPruefungLogischLVE
																.getWertIdent(parameterAtgLog
																		.getBGrenz()));
								data
										.getItem("b").getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
								data.getItem("b").getItem("Status").getItem(
										"MessWertErsetzung").getUnscaledValue(
										"Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
							}
						}
					}
				}
			}
		}

		if (parameterAtgLog != null) {
			synchronized (parameterAtgLog) {
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				if (untersucheAufMaxVerletzung(data, resultat, "qKfz",
						parameterAtgLog.getQKfzBereichMax() * t
								/ Constants.MILLIS_PER_HOUR)) {
					return;
				}

				/**
				 * Regel Nr.13 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				if (untersucheAufMaxVerletzung(data, resultat, "qPkw",
						parameterAtgLog.getQPkwBereichMax() * t
								/ Constants.MILLIS_PER_HOUR)) {
					return;
				}

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				if (untersucheAufMaxVerletzung(data, resultat, "qLkw",
						parameterAtgLog.getQLkwBereichMax() * t
								/ Constants.MILLIS_PER_HOUR)) {
					return;
				}
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				// if (untersucheAufMaxVerletzung(data, resultat, "qKfz",
				// //$NON-NLS-1$
				// parameterAtgLog.getQKfzBereichMax())) {
				// return;
				// }
				//
				// /**
				// * Regel Nr.13 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				// */
				// if (untersucheAufMaxVerletzung(data, resultat, "qPkw",
				// parameterAtgLog.getQPkwBereichMax())) {
				// return;
				// }
				//
				// /**
				// * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				// */
				// if (untersucheAufMaxVerletzung(data, resultat, "qLkw",
				// parameterAtgLog.getQLkwBereichMax())) {
				// return;
				// }
				/**
				 * Regel Nr.15 (aus SE-02.00.00.00.00-AFo-5.2, S.95)
				 */
				if (untersucheAufMaxVerletzung(data, resultat, "vKfz",
						parameterAtgLog.getVKfzBereichMax())) {
					return;
				}

				/**
				 * Regel Nr.16 (aus SE-02.00.00.00.00-AFo-5.2, S.96)
				 */
				if (untersucheAufMaxVerletzung(data, resultat, "vLkw",
						parameterAtgLog.getVLkwBereichMax())) {
					return;
				}

				/**
				 * Regel Nr.17 (aus SE-02.00.00.00.00-AFo-5.2, S.96)
				 */
				if (untersucheAufMaxVerletzung(data, resultat, "vPkw",
						parameterAtgLog.getVPkwBereichMax())) {
					return;
				}

				/**
				 * Regel Nr.18 (aus SE-02.00.00.00.00-AFo-5.2, S.96)
				 */
				if (untersucheAufMaxVerletzung(data, resultat, "vgKfz",
						parameterAtgLog.getVgKfzBereichMax())) {
					return;
				}

				/**
				 * Regel Nr.19 (aus SE-02.00.00.00.00-AFo-5.2, S.96)
				 */
				if (untersucheAufMaxVerletzung(data, resultat, "b",
						parameterAtgLog.getBelegungBereichMax())) {
					return;
				}
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
				AtgVerkehrsDatenKZIPlPruefLogisch.getPid());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getAttributNamen() {
		return ATTRIBUT_NAMEN;
	}

}
