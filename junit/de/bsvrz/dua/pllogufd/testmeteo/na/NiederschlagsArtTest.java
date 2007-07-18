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

package de.bsvrz.dua.pllogufd.testmeteo.na;

import junit.framework.Assert;

import org.junit.Test;

import stauma.dav.configuration.interfaces.SystemObject;

import de.bsvrz.dua.pllogufd.DAVTest;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoErgebnis;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolleTest;

/**
 * Überprüfung des Submoduls NiederschlagsArt aus der Komponente Meteorologische Kontrolle.
 * Diese Überprüfung richtet sich nach den Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)],
 * S.26
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class NiederschlagsArtTest
extends MeteorologischeKontrolleTest{
	

	/**
	 * {@inheritDoc}
	 */
	public NiederschlagsArtTest()
	throws Exception {
		super();
	}
			
	
	/**
	 * Testet implizit die Methode <code>regel1</code> aus {@link NiederschlagsArtMessstelle}
	 */
	@Test
	public final void testRegel1(){

		/**
		 * Erste Zeile aus Tabelle auf Seite 26
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(nsSensoren, 40, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(ltSensoren, MeteoKonst.NSGrenzLT - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NS)R1.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * Zweite Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(nsSensoren, 41, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(ltSensoren, MeteoKonst.NSGrenzLT, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NS)R1.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * Dritte Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(nsSensoren, 42, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(ltSensoren, MeteoKonst.NSGrenzLT + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NS)R1.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
	}
	
	
	/**
	 * Testet implizit die Methode <code>regel3</code> aus {@link NiederschlagsArtMessstelle}
	 */
	@Test
	public final void testRegel3(){
		
		/**
		 * 4. Zeile aus Tabelle auf Seite 26
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(nsSensoren, 40, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NSGrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NS)R3.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * 5. Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(nsSensoren, 40, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NSGrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NS)R3.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * 6. Zeile
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(nsSensoren, 40, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NSGrenzTrockenRLF + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NS)R3.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
	}
	
	
	/**
	 * Testet implizit die Methode <code>regel2</code> aus {@link NiederschlagsArtMessstelle}<br>
	 * <br>
	 * <b>Achtung:</b> Der Überprüfung der letzten drei Tabellenspalten ([QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S.26)
	 * fällt weg, da diese die Funktionalität der NS-Regel Nr.4 aus den AFo testen (S. 104). Diese Regel wurde jedoch
	 * nicht implementiert.
	 */
	@Test
	public final void testRegel2(){
		
		/**
		 * 7. Zeile aus Tabelle auf Seite 26
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(nsSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NS)R2.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * 8. Zeile aus Tabelle auf Seite 26
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(nsSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NS)R2.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

	}
}
