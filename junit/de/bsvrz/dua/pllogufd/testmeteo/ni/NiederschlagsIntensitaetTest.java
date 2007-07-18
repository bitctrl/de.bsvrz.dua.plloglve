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
package de.bsvrz.dua.pllogufd.testmeteo.ni;

import junit.framework.Assert;

import org.junit.Test;

import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.DAVTest;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoErgebnis;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolleTest;

/**
 * Überprüfung des Submoduls NiederschlagsIntensität aus der Komponente Meteorologische Kontrolle.
 * Diese Überprüfung richtet sich nach den Vorgaben von [QS-02.04.00.00.00-PrSpez-2.0 (DUA)],
 * S.27<br>
 * <b>Achtung:</b> Die ersten drei Zeilen der Tabelle werden hier nicht berücksichtigt, da
 * diese sich auf eine Regel beziehen, die nicht mehr zur Implementierung vorgesehen ist 
 * (<code><b>Wenn</b> (NS == Niederschlag) <b>und</b> (NI == 0) <b>und</b> (RLF > NIgrenzNassRLF)
 * <b>dann</b> (NI=implausibel)</code>)
 *  
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class NiederschlagsIntensitaetTest
extends MeteorologischeKontrolleTest {

	
	/**
	 * {@inheritDoc} 
	 */
	public NiederschlagsIntensitaetTest()
	throws Exception {
		super();
	}
	
	
	/**
	 * Testet implizit die Methode <code>regel1</code> aus {@link NiederschlagsIntensitaetsMessstelle}
	 */
	@Test
	public final void testRegel1(){
	
		/**
		 * 4. Zeile aus Tabelle auf Seite 27
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(niSensoren, 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NI)R1.4\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NI)R1.4\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		
		/**
		 * 5. Zeile aus Tabelle auf Seite 27
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(niSensoren, 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.5\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.5\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
	}

	
	/**
	 * Testet implizit die Methode <code>regel3</code> aus {@link NiederschlagsIntensitaetsMessstelle}
	 */
	@Test
	public final void testRegel3(){

		long rlfStart = MeteoKonst.NIgrenzTrockenRLF - 4;
		
		/**
		 * 6. Zeile aus Tabelle auf Seite 27
		 *
		 * RLF = NIgrenzTrockenRLF - 3, RLF < NIgrenzTrockenRLF (für = 1T)
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1.0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.1.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * RLF = NIgrenzTrockenRLF - 2, RLF < NIgrenzTrockenRLF (für = 2T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.1.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
		/**
		 * RLF = NIgrenzTrockenRLF - 1, RLF < NIgrenzTrockenRLF (für = 3T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.1.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		

		/**
		 * RLF = NIgrenzTrockenRLF, !(RLF < NIgrenzTrockenRLF (1T))
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, ++rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.1.4\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		
		/**
		 * Lasse jetzt RLF wieder abfallen bis für mehr als 3T gilt: RLF < NIgrenzTrockenRLF
		 *
		 * RLF = NIgrenzTrockenRLF - 1, RLF < NIgrenzTrockenRLF (für = 1T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.1.5\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 2, RLF < NIgrenzTrockenRLF (für = 2T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.1.6\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 3, RLF < NIgrenzTrockenRLF (für = 3T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.1.7\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 4, RLF < NIgrenzTrockenRLF (für = 4T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1.0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NI)R1.1.8\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 4, RLF < NIgrenzTrockenRLF (für = 5T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1.0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NI)R1.1.9\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 5, RLF > NIgrenzTrockenRLF (für = 6T)
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1.0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, --rlfStart, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NI)R1.1.10\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		/**
		 * RLF = NIgrenzTrockenRLF - 1
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1.0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NIgrenzTrockenRLF + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.1.11\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
			
		
		
		/**
		 * lasse jetzt die Relative Luftfeuchte wieder 4T unter NIgrenzTrockenRLF sein und änder dann auf WFD > 0
		 */
		
		for(int i=0; i<3; i++){
			zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
			DAVTest.warteBis(zeitStempel + 50);
		
			this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1.0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
			this.sendeDaten(rlfSensoren, MeteoKonst.NIgrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
			
			DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
			for(SystemObject niSensor:this.niSensoren){
				MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
				MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
				if(DEBUG)System.out.println("(NI)R1.2.1\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
				if(TEST_AN)Assert.assertEquals(soll, ist);
			}			
		}
		
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1.0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NIgrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NI)R1.2.2\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}

		/**
		 * Ändere jetzt WFD auf WFD > 0: Implausibilisierung wird aufgehoben
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T; 
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(wfdSensoren, 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(niSensoren, MeteoKonst.NIgrenzNassNI + 1.0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NIgrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject niSensor:this.niSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(niSensor);
			MeteoErgebnis soll = new MeteoErgebnis(niSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R1.2.3\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}	
	}

	
	/**
	 * Testet implizit die Methode <code>regel2</code> aus {@link NiederschlagsIntensitaetsMessstelle}
	 */
	@Test
	public final void testRegel2(){
		
		/**
		 * 7. Zeile aus Tabelle auf Seite 27
		 */
		long zeitStempel = this.getTestBeginnIntervall();
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(niSensoren, MeteoKonst.NIminNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NIgrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, true);
			if(DEBUG)System.out.println("(NI)R2.7\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}
		
//		/**
//		 * 8. Zeile aus Tabelle auf Seite 27
//		 */
//		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
//		DAVTest.warteBis(zeitStempel + 50);
//	
//		this.sendeDaten(niSensoren, MeteoKonst.NIminNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
//		this.sendeDaten(nsSensoren, 0, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
//		this.sendeDaten(rlfSensoren, MeteoKonst.NIgrenzTrockenRLF + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
//		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
//		for(SystemObject nsSensor:this.nsSensoren){
//			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
//			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
//			if(DEBUG)System.out.println("(NI)R2.8\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
//			if(TEST_AN)Assert.assertEquals(soll, ist);
//		}
		
		/**
		 * 9. Zeile aus Tabelle auf Seite 27
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(niSensoren, MeteoKonst.NIminNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NIgrenzTrockenRLF - 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R2.9\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}		

		/**
		 * 10. Zeile aus Tabelle auf Seite 27
		 */
		zeitStempel += PlPruefungLogischUFDTest.STANDARD_T;
		DAVTest.warteBis(zeitStempel + 50);
	
		this.sendeDaten(niSensoren, MeteoKonst.NIminNI + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(nsSensoren, 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		this.sendeDaten(rlfSensoren, MeteoKonst.NIgrenzTrockenRLF + 1, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T);
		DAVTest.warteBis(zeitStempel + PlPruefungLogischUFDTest.STANDARD_T / 20 * 18);
		for(SystemObject nsSensor:this.nsSensoren){
			MeteoErgebnis ist = this.ergebnisIst.get(nsSensor);
			MeteoErgebnis soll = new MeteoErgebnis(nsSensor, zeitStempel - PlPruefungLogischUFDTest.STANDARD_T, false);
			if(DEBUG)System.out.println("(NI)R2.10\nSoll: " + soll + "\nIst: " + ist); //$NON-NLS-1$ //$NON-NLS-2$
			if(TEST_AN)Assert.assertEquals(soll, ist);
		}		
	}

}
