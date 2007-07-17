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
package de.bsvrz.dua.pllogufd.testmeteo.sw;

import junit.framework.Assert;

import org.junit.Test;

import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.DAVTest;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoErgebnis;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolleTest;

/**
 * Überprüfung des Submoduls Sichtweite aus der Komponente Meteorologische Kontrolle.
 * Diese Überprüfung richtet sich nach den Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)],
 * S.28<br>
 * <b>Achtung:</b> Bei den Test-Vorgaben werden die Zeilen innerhalb der Tabelle auf
 * S. 28 ignoriert, in denen NI nicht auf "don't care" steht, da diese die SW-Regel
 * Nr.2 überprüfen sollten. Diese Regel ist hier allerdings nicht implementiert.
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class SichtweiteTest
extends MeteorologischeKontrolleTest {

	
	/**
	 * {@inheritDoc}
	 */
	public SichtweiteTest()
	throws Exception {
		super();
	}

	
	/**
	 * Testet implizit die Methode <code>regel1</code> aus {@link SichtweitenMessstelle}
	 */
	@Test
	public final void testRegel1(){

		/**
		 * Erste Zeile aus Tabelle auf Seite 28
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(swSensoren, MeteoKonst.SWgrenzSW - DAVTest.R.nextInt(2), zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.SWgrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject swSensor:this.swSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(swSensor);
			MeteoErgebnis soll = new MeteoErgebnis(swSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(SW)R1.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * Zweite Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(swSensoren, MeteoKonst.SWgrenzSW - DAVTest.R.nextInt(2), zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.SWgrenzTrockenRLF + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject swSensor:this.swSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(swSensor);
			MeteoErgebnis soll = new MeteoErgebnis(swSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(SW)R1.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * Dritte Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(swSensoren, MeteoKonst.SWgrenzSW - DAVTest.R.nextInt(2), zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 40, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.SWgrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject swSensor:this.swSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(swSensor);
			MeteoErgebnis soll = new MeteoErgebnis(swSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(SW)R1.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * 7. Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(swSensoren, MeteoKonst.SWgrenzSW + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.SWgrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject swSensor:this.swSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(swSensor);
			MeteoErgebnis soll = new MeteoErgebnis(swSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(SW)R1.4\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
	}	
}
