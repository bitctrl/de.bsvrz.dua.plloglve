/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
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

package de.bsvrz.dua.pllogufd;

import java.util.Calendar;
import java.util.GregorianCalendar;

import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Einige Utensilien für die Tests der SWE 4.3
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class TestUtensilien {
	

	/**
	 * Erfragt den Zeitpunkt des Beginns der nächsten Minute
	 * 
	 * @return den Zeitpunkt des Beginns der nächsten Minute in ms
	 */
	public static final long getBeginNaechsterMinute(){
		GregorianCalendar kal = new GregorianCalendar();
		kal.setTimeInMillis(System.currentTimeMillis());
		kal.add(Calendar.MINUTE, 1);
		kal.set(Calendar.SECOND, 0);
		kal.set(Calendar.MILLISECOND, 0);
		return kal.getTimeInMillis();
	}
	
	
	/**
	 * Erzeugt einen Messwert mit der Datenbeschreibung <code>asp.externeErfassung</code>
	 * 
	 * @param sensor ein Umfelddatensensor, für den ein Messwert erzeugt werden soll
	 * @return ein (ausgefüllter) Umfelddaten-Messwert der zum übergebenen Systemobjekt passt.
	 * Alle Pl-Prüfungs-Flags sind auf <code>NEIN</code> gesetzt. Der Daten-Intervall beträgt
	 * 1 min.
	 */
	public static final ResultData getExterneErfassungDatum(SystemObject sensor) {
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);		
		DataDescription datenBeschreibung = new DataDescription(
				PlPruefungLogischUFDTest.DAV.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
				PlPruefungLogischUFDTest.DAV.getDataModel().getAspect("asp.externeErfassung"), //$NON-NLS-1$
				(short)0);
		Data datum = PlPruefungLogischUFDTest.DAV.createData(
				PlPruefungLogischUFDTest.DAV.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName())); //$NON-NLS-1$
		datum.getTimeValue("T").setMillis(60L * 1000L); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getUnscaledValue("Wert").set(0); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("Erfassung"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("NichtErfasst").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("PlFormal"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMax").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("PlFormal"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMin").set(DUAKonstanten.NEIN); //$NON-NLS-1$

		datum.getItem(datenArt.getName()).getItem("Status").getItem("MessWertErsetzung"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("Implausibel").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("MessWertErsetzung"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("Interpoliert").set(DUAKonstanten.NEIN); //$NON-NLS-1$

		datum.getItem(datenArt.getName()).getItem("Güte").getUnscaledValue("Index").set(10000); //$NON-NLS-1$ //$NON-NLS-2$
		datum.getItem(datenArt.getName()).getItem("Güte").getUnscaledValue("Verfahren").set(0); //$NON-NLS-1$ //$NON-NLS-2$
		
		return new ResultData(sensor, datenBeschreibung, System.currentTimeMillis(), datum);
	}	
	
}
