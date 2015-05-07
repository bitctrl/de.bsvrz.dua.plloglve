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

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.dua.AllgemeinerDatenContainer;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Abstrakte Klasse die alle Parameter halten kann, die innerhalb der
 * Standardplausibilisierung LVE für sowohl LZD als auch KZD benötigt werden.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class AbstraktAtgPLLogischLVEParameter extends AllgemeinerDatenContainer {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Legt das Verhalten für den Umgang mit geprüften Werten nach der
	 * Wertebereichsprüfung fest.
	 */
	protected OptionenPlausibilitaetsPruefungLogischVerkehr optionen;

	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als
	 * normierter Stundenwert.
	 */
	protected long qKfzBereichMin;

	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als
	 * normierter Stundenwert.
	 */
	protected long qKfzBereichMax;

	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als
	 * normierter Stundenwert.
	 */
	protected long qLkwBereichMin;

	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als
	 * normierter Stundenwert.
	 */
	protected long qLkwBereichMax;

	/**
	 * Grenzgeschwindigkeit für PL-Prüfung. Ist dieser Wert überschritten, muss
	 * b kleiner bGrenz sein, sonst ist b inplausibel.
	 */
	protected long vKfzGrenz;

	/**
	 * Ist vKfz größer als vKfzGrenz, so muss b kleiner bGrenz sein, sonst ist b
	 * inplausibel.
	 */
	protected long bGrenz;

	/**
	 * Minimum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als
	 * normierter Stundenwert.
	 */
	protected long qPkwBereichMin;

	/**
	 * Maximum der erlaubten Verkehrsstärke (Anzahl der Fahrzeuge) als
	 * normierter Stundenwert.
	 */
	protected long qPkwBereichMax;

	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	protected long vKfzBereichMin;

	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	protected long vKfzBereichMax;

	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	protected long vLkwBereichMin;

	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	protected long vLkwBereichMax;

	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	protected long vPkwBereichMin;

	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	protected long vPkwBereichMax;

	/**
	 * Minimum der erlaubten Geschwindigkeit.
	 */
	protected long vgKfzBereichMin;

	/**
	 * Maximum der erlaubten Geschwindigkeit.
	 */
	protected long vgKfzBereichMax;

	/**
	 * Minimum des erlaubten Prozentwertes.
	 */
	protected long belegungBereichMin;

	/**
	 * Maximum des erlaubten Prozentwertes.
	 */
	protected long belegungBereichMax;

	/**
	 * Erfragt eine Schnittstelle zu den Parametern der logischen
	 * Plausibilisierung.
	 *
	 * @param resultat
	 *            ein Parameter-Resultat
	 * @return eine Schnittstelle zu den Parametern der logischen
	 *         Plausibilisierung oder <code>null</code>, wenn diese nicht
	 *         ausgelesen werden konnten
	 */
	public static final AbstraktAtgPLLogischLVEParameter getInstance(
			final ResultData resultat) {
		AbstraktAtgPLLogischLVEParameter dummy = null;

		if (resultat != null) {
			if (resultat.getData() != null) {
				if (resultat.getDataDescription().getAttributeGroup().getPid()
						.equals(AtgVerkehrsDatenKZIPlPruefLogisch.getPid())) {
					dummy = new AtgVerkehrsDatenKZIPlPruefLogisch(
							resultat.getData());
				} else if (resultat.getDataDescription().getAttributeGroup()
						.getPid()
						.equals(AtgVerkehrsDatenLZIPlPruefLogisch.getPid())) {
					dummy = new AtgVerkehrsDatenLZIPlPruefLogisch(
							resultat.getData());
				} else {
					LOGGER.warning(
							"Unbekannter Datensatz übergeben:\n" + //$NON-NLS-1$
									resultat.getDataDescription()
											.getAttributeGroup());
				}
			}
		}

		return dummy;
	}

	/**
	 * Erfragt qKfzBereichMax.
	 *
	 * @return qKfzBereichMax
	 */
	public final long getQKfzBereichMax() {
		return this.qKfzBereichMax;
	}

	/**
	 * Erfragt qKfzBereichMin.
	 *
	 * @return qKfzBereichMin
	 */
	public final long getQKfzBereichMin() {
		return this.qKfzBereichMin;
	}

	/**
	 * Erfragt qLkwBereichMax.
	 *
	 * @return qLkwBereichMax
	 */
	public final long getQLkwBereichMax() {
		return this.qLkwBereichMax;
	}

	/**
	 * Erfragt qLkwBereichMin.
	 *
	 * @return qLkwBereichMin
	 */
	public final long getQLkwBereichMin() {
		return this.qLkwBereichMin;
	}

	/**
	 * Erfragt die Optionen.
	 *
	 * @return optionen
	 */
	public final OptionenPlausibilitaetsPruefungLogischVerkehr getOptionen() {
		return this.optionen;
	}

	/**
	 * Erfragt BelegungBereichMax.
	 *
	 * @return belegungBereichMax
	 */
	public final long getBelegungBereichMax() {
		return belegungBereichMax;
	}

	/**
	 * Erfragt BelegungBereichMin.
	 *
	 * @return belegungBereichMin
	 */
	public final long getBelegungBereichMin() {
		return belegungBereichMin;
	}

	/**
	 * Erfragt bGrenz.
	 *
	 * @return bGrenz
	 */
	public final long getBGrenz() {
		return bGrenz;
	}

	/**
	 * Erfragt qPkwBereichMax.
	 *
	 * @return qPkwBereichMax
	 */
	public final long getQPkwBereichMax() {
		return qPkwBereichMax;
	}

	/**
	 * Erfragt qPkwBereichMin.
	 *
	 * @return qPkwBereichMin
	 */
	public final long getQPkwBereichMin() {
		return qPkwBereichMin;
	}

	/**
	 * Erfragt vgKfzBereichMax.
	 *
	 * @return vgKfzBereichMax
	 */
	public final long getVgKfzBereichMax() {
		return vgKfzBereichMax;
	}

	/**
	 * Erfragt vgKfzBereichMin.
	 *
	 * @return vgKfzBereichMin
	 */
	public final long getVgKfzBereichMin() {
		return vgKfzBereichMin;
	}

	/**
	 * Erfragt vKfzBereichMax.
	 *
	 * @return vKfzBereichMax
	 */
	public final long getVKfzBereichMax() {
		return vKfzBereichMax;
	}

	/**
	 * Erfragt vKfzBereichMin.
	 *
	 * @return vKfzBereichMin
	 */
	public final long getVKfzBereichMin() {
		return vKfzBereichMin;
	}

	/**
	 * Erfragt vKfzGrenz.
	 *
	 * @return vKfzGrenz
	 */
	public final long getVKfzGrenz() {
		return vKfzGrenz;
	}

	/**
	 * Erfragt vLkwBereichMax.
	 *
	 * @return vLkwBereichMax
	 */
	public final long getVLkwBereichMax() {
		return vLkwBereichMax;
	}

	/**
	 * Erfragt vLkwBereichMin.
	 *
	 * @return vLkwBereichMin
	 */
	public final long getVLkwBereichMin() {
		return vLkwBereichMin;
	}

	/**
	 * Erfragt vPkwBereichMax.
	 *
	 * @return vPkwBereichMax
	 */
	public final long getVPkwBereichMax() {
		return vPkwBereichMax;
	}

	/**
	 * Erfragt vPkwBereichMin.
	 *
	 * @return vPkwBereichMin
	 */
	public final long getVPkwBereichMin() {
		return vPkwBereichMin;
	}

}
