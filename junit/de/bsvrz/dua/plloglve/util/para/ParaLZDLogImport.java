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

package de.bsvrz.dua.plloglve.util.para;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.plloglve.standard.AtgVerkehrsDatenLZIPlPruefLogisch;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;

/**
 * Importiert die Parameter für die Pl-Prüfung logisch LZD.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class ParaLZDLogImport extends AbstraktParameterImport {

	/**
	 * Prüf-Optionen.
	 */
	private OptionenPlausibilitaetsPruefungLogischVerkehr optionen = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param dav
	 *            Datenverteier-Verbindung
	 * @param objekt
	 *            das Systemobjekt, für das die Parameter gesetzt werden sollen
	 * @param csvQuelle
	 *            Quelle der Daten (CSV-Datei)
	 * @throws Exception
	 *             falls dieses Objekt nicht vollständig initialisiert werden
	 *             konnte
	 */
	public ParaLZDLogImport(ClientDavInterface dav, SystemObject objekt,
			String csvQuelle) throws Exception {
		super(dav, objekt, csvQuelle);
	}

	/**
	 * Setzte die Prüf-Optionen.
	 * 
	 * @param optionen
	 *            aktuelle Prüf-Optionen
	 */
	public final void setOptionen(
			final OptionenPlausibilitaetsPruefungLogischVerkehr optionen) {
		this.optionen = optionen;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Data fuelleRestAttribute(Data datensatz) {
		DUAUtensilien
				.getAttributDatum("qPkwÄBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwÄBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKfzNkBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKfzNkBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwGBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwGBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		// DUAUtensilien.getAttributDatum("qPkwBereich.Min",
		// datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		// DUAUtensilien.getAttributDatum("qPkwBereich.Max",
		// datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKradBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKradBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLfwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLfwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwÄBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwÄBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwABereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwABereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qBusBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$		
		DUAUtensilien
				.getAttributDatum("qBusBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwKBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwKBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwABereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwABereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qSattelKfzBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qSattelKfzBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vKfzBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vKfzBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vPkwÄBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vPkwÄBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vKfzNkBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vKfzNkBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$		
		DUAUtensilien
				.getAttributDatum("vPkwGBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vPkwGBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vPkwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vPkwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vKradBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vKradBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLfwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLfwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLkwÄBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLkwÄBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vPkwABereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vPkwABereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLkwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLkwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vBusBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vBusBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLkwKBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLkwKBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLkwABereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vLkwABereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vSattelKfzBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("vSattelKfzBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sKfzBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sKfzBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sPkwÄBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sPkwÄBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sKfzNkBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sKfzNkBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sPkwGBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sPkwGBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sPkwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sPkwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sKradBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sKradBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLfwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLfwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLkwÄBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLkwÄBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sPkwABereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sPkwABereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLkwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLkwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sBusBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sBusBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLkwKBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLkwKBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLkwABereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sLkwABereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sSattelKfzBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("sSattelKfzBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85KfzBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85KfzBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85PkwÄBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85PkwÄBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85KfzNkBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85KfzNkBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85PkwGBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85PkwGBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85PkwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85PkwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85KradBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85KradBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LfwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LfwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LkwÄBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LkwÄBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85PkwABereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85PkwABereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LkwBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LkwBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85BusBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85BusBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LkwKBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LkwKBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LkwABereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85LkwABereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85SattelKfzBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("v85SattelKfzBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKfzGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKfzGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwÄGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwÄGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKfzNkGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKfzNkGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwGGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwGGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKradGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qKradGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLfwGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLfwGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwÄGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwÄGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwAGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qPkwAGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qBusGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qBusGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwKGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwKGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwAGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum("qLkwAGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum(
						"qSattelKfzGeschwKlasseBereich.Min", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
		DUAUtensilien
				.getAttributDatum(
						"qSattelKfzGeschwKlasseBereich.Max", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$

		DUAUtensilien
				.getAttributDatum("Optionen", datensatz).asUnscaledValue().set(this.optionen.getCode()); //$NON-NLS-1$
		datensatz.getUnscaledValue("Optionen").set(this.optionen.getCode()); //$NON-NLS-1$
		return datensatz;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getAttributPfadVon(String attributInCSVDatei, int index) {
		if (attributInCSVDatei.endsWith(")")) { //$NON-NLS-1$
			String nummerStr = attributInCSVDatei.substring(attributInCSVDatei
					.length() - 2, attributInCSVDatei.length() - 1);
			int nummer = -1;
			try {
				nummer = Integer.parseInt(nummerStr);
			} catch (Exception ex) {
				//
			}

			if (nummer == index) {
				if (attributInCSVDatei.startsWith("qKfzMin")) { //$NON-NLS-1$
					return "qKfzBereich.Min"; //$NON-NLS-1$
				}
				if (attributInCSVDatei.startsWith("qKfzMax")) { //$NON-NLS-1$
					return "qKfzBereich.Max"; //$NON-NLS-1$
				}
				if (attributInCSVDatei.startsWith("qLkwMax")) { //$NON-NLS-1$
					return "qLkwBereich.Max"; //$NON-NLS-1$
				}
				if (attributInCSVDatei.startsWith("qLkwMin")) { //$NON-NLS-1$
					return "qLkwBereich.Min"; //$NON-NLS-1$
				}
				if (attributInCSVDatei.startsWith("qPkwMax")) { //$NON-NLS-1$
					return "qPkwBereich.Max"; //$NON-NLS-1$
				}
				if (attributInCSVDatei.startsWith("qPkwMin")) { //$NON-NLS-1$
					return "qPkwBereich.Min"; //$NON-NLS-1$
				}
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributeGroup getParameterAtg() {
		return sDav.getDataModel().getAttributeGroup(
				AtgVerkehrsDatenLZIPlPruefLogisch.getPid());
	}

}
