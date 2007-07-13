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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoErgebnis;
import de.bsvrz.dua.pllogufd.testmeteo.MeteorologischeKontrolleTest;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;

/**
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
	 * Führt den Vergleich aller Ist-Werte mit allen Soll-Werten durch
	 * und zeigt die Ergebnisse an. Gleichzeitig werden die Ergebnisse
	 * über <code>JUnit</code> getestet<br><br>
	 * Nach dem Test werden die Mengen der Soll- und Ist-Werte wieder
	 * gelöscht
	 */
	protected void ergebnisUeberpruefen(){
		if(!this.ergebnisIst.isEmpty() && !this.ergebnisSoll.isEmpty()){
			//
		}
		this.ergebnisIst.clear();
		this.ergebnisSoll.clear();		
	}
	
	
	
	/**
	 * der eigentliche Test
	 */
	@Test
	public void testNiederschlagsArt()
	throws Exception{
		GregorianCalendar kal = new GregorianCalendar();
		kal.setTimeInMillis(System.currentTimeMillis());
		kal.set(Calendar.MILLISECOND, 0);
		long zeitStempel = kal.getTimeInMillis();
		
		Pause.warte(zeitStempel + STANDARD_T - System.currentTimeMillis());
		
		int i = 40;
		for(SystemObject nsSensor:this.nsSensoren){
			this.sendeDatum(nsSensor, i++, zeitStempel);
			this.ergebnisSoll.put(nsSensor, new MeteoErgebnis(nsSensor, zeitStempel, true));
			break;
		}
		for(SystemObject ltSensor:this.ltSensoren){
			this.sendeDatum(ltSensor, -6, zeitStempel);
			break;
		}
		
		/**
		 * nur zum Flush
		 */
		zeitStempel += STANDARD_T;
		System.out.println(zeitStempel + STANDARD_T - System.currentTimeMillis());
		//Pause.warte(zeitStempel + STANDARD_T - System.currentTimeMillis());
		for(SystemObject nsSensor:this.nsSensoren){
			this.sendeDatum(nsSensor, 60, zeitStempel);
			break;
		}
		
		zeitStempel += STANDARD_T;
		System.out.println(zeitStempel + STANDARD_T - System.currentTimeMillis());
		//Pause.warte(zeitStempel + STANDARD_T - System.currentTimeMillis());
		for(SystemObject nsSensor:this.nsSensoren){
			this.sendeDatum(nsSensor, 60, zeitStempel);
			break;
		}					

	}	
}
