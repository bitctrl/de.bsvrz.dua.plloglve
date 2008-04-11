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

package de.bsvrz.dua.plloglve.util.pruef;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.plloglve.util.PlPruefungInterface;
import de.bsvrz.sys.funclib.bitctrl.dua.bm.BmClient;
import de.bsvrz.sys.funclib.bitctrl.dua.bm.IBmListener;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * KZD Listener Liest Ergebnis-CSV-Datei Wartet auf gesendete und gepruefte
 * Daten und gibt diese an Vergleicher-Klasse weiter.
 * 
 * @author BitCtrl Systems GmbH, Görlitz
 * 
 * @version $Id$
 */
public class FilterMeldung implements IBmListener {

	/**
	 * Logger.
	 */
	protected static final Debug LOGGER = Debug.getLogger();

	/**
	 * Aufrufende Klasse.
	 */
	PlPruefungInterface caller;

	/**
	 * Datenverteilerverbindung von der aufrufenden Klasse.
	 */
	private ClientDavInterface dav;

	/**
	 * Meldungssystemobjekt.
	 */
	SystemObject meld;

	/**
	 * Gibt an, ob Ergebnisdatensätze auf Fehlerfreiheit geprüft werden.
	 */
	private String filter = null;

	/**
	 * Zählt die Anzahl der gefilterten Meldungen.
	 */
	private int meldAnzahl = 0;

	/**
	 * Erforderliche Anzahl an gefilterten Meldungen.
	 */
	private int erfAnz;

	/**
	 * Erlaubte Abweichung der Anzahl an gefilterten Meldungen.
	 */
	private int anzHyst;

	/**
	 * Empfange-Datenbeschreibung für KZD und LZD.
	 */
	public static DataDescription ddMeldEmpf = null;

	/**
	 * Gibt an, ob die geforderte Anzahl inklusive Hysterese eingehalten wurde.
	 */
	private boolean anzahlEingehalten = false;

	/**
	 * Initialisiert und Konfiguriert den Meldungsfilter
	 * 
	 * @param caller Die aufrufende Klasse
	 * @param dav Die Datenverteilerverbindung
	 * @param filter Der anzuwendende Filter
	 * @param erfAnz Die erwartete Anzahl erfasster Meldungen
	 * @param anzHyst Hystereseparameter
	 * @throws Exception wird weitergereicht
	 */
	public FilterMeldung(PlPruefungInterface caller, ClientDavInterface dav,
			String filter, int erfAnz, int anzHyst) throws Exception {
		this.dav = dav;
		this.filter = filter;
		this.erfAnz = erfAnz;
		this.anzHyst = anzHyst;
		this.caller = caller;

		LOGGER
				.info("Filtere Betriebsmeldungen nach \"" + filter + "\" - Erwarte " + erfAnz + " gefilterte Meldungen"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		/*
		 * Melde Empfänger für Betriebsmeldungen an
		 */
		ddMeldEmpf = new DataDescription(this.dav.getDataModel()
				.getAttributeGroup("atg.betriebsMeldung"), //$NON-NLS-1$
				this.dav.getDataModel().getAspect("asp.information"), //$NON-NLS-1$
				(short) 0);

		BmClient.getInstanz(dav).addListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereBetriebsMeldungen(SystemObject obj, long zeit,
			String text) {
		if (text.contains(filter)) {
			meldAnzahl++;
			LOGGER.info(meldAnzahl + ". Meldung empfangen\n\r" + text); //$NON-NLS-1$

			if (meldAnzahl == erfAnz) {
				LOGGER.info("Erforderliche Anzahl an Meldungen erhalten"); //$NON-NLS-1$
				anzahlEingehalten = true;
				caller.doNotify();
			} else if (meldAnzahl > (erfAnz + anzHyst)) {
				anzahlEingehalten = false;
				LOGGER.warning("Mehr Meldungen gefiltert als erwartet"); //$NON-NLS-1$	
			}
		}
	}

	/**
	 * Wurde die geforderte Anzahl inklusive Hysterese eingehalten.
	 * 
	 * @return <code>True</code>, wenn die Anzahl inklusive Hysterese
	 *         eingehalten wurde, sonst <code>False</code>
	 */
	public boolean wurdeAnzahlEingehalten() {
		return anzahlEingehalten;
	}

	/**
	 * Liefert die Anzahl erhaltener Meldungen.
	 * 
	 * @return Anzahl erhaltener Meldungen
	 */
	public int getAnzahlErhaltenerMeldungen() {
		return meldAnzahl;
	}

	/**
	 * Liefert die erwartete Anzahl an Meldungen.
	 * 
	 * @return Erwartete Anzahl an Meldungen
	 */
	public int getErwarteteAnzahlMeldungen() {
		return erfAnz;
	}
}
