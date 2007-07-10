/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.3 Pl-Pr�fung logisch UFD
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.pllogufd.testdiff;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.DAVTest;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.TestUtensilien;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.dua.pllogufd.typen.UfdsVergleichsOperator;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Test des Moduls Differenzialkontrolle<br>
 * Der Test implementiert die Vorgaben aus dem Dokument [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 24
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class UFDDifferenzialKontrolleTest 
implements ClientSenderInterface, ClientReceiverInterface{
	
	/**
	 * Standardintervalll�nge f�r Testdaten 5s
	 */
	private static final long STANDARD_T = Konstante.SEKUNDE_IN_MS * 5;
	
	/**
	 * standardm��ige maximal zul�ssige Ergebniskonstanz in Intervallen 
	 */
	private static final long STANDARD_MAX_INTERVALLE = 3;

	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;

	/**
	 * letzter Soll-Ergebnis-Wert von einem Sensor<br>
	 * (<code>Implausibel</code> und <code>fehlerhaft</code> == <code>true</code>)
	 */
	private Map<SystemObject, Boolean> ergebnisSoll = new HashMap<SystemObject, Boolean>();

	/**
	 * letzter Ist-Ergebnis-Wert von einem Sensor<br>
	 * (<code>Implausibel</code> und <code>fehlerhaft</code> == <code>true</code>)
	 */
	private Map<SystemObject, Boolean> ergebnisIst = new HashMap<SystemObject, Boolean>();
	
	
	/**
	 * {@inheritDoc}
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav();
		PlPruefungLogischUFDTest.initialisiere();


		/**
		 * Anmeldung auf alle Parameter
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			DataDescription paraDifferenzialkontrolle = new DataDescription(
					dav.getDataModel().getAttributeGroup("atg.ufdsDifferenzialKontrolle" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
					(short)0);
			dav.subscribeSender(this, sensor, paraDifferenzialkontrolle, SenderRole.sender());			
		}
		
		/**
		 * maximal zul�ssige Zeitdauer der Ergebniskonstanz auf <code>STANDARD_T * STANDARD_MAX_INTERVALLE</code> stellen
		 * Eine �berpr�fung findet nur statt, wenn ein eingetroffener Wert "<" als der Grenzwert von 5 ist
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			Data datum = dav.createData(dav.getDataModel().getAttributeGroup(
					"atg.ufdsDifferenzialKontrolle" + datenArt.getName())); //$NON-NLS-1$
			
			datum.getUnscaledValue("Operator").set(UfdsVergleichsOperator.KLEINER.getCode()); //$NON-NLS-1$
			datum.getUnscaledValue(datenArt.getAbkuerzung() + "Grenz").set(5); //$NON-NLS-1$
			datum.getTimeValue(datenArt.getAbkuerzung() + "maxZeit").setMillis(STANDARD_T * STANDARD_MAX_INTERVALLE); //$NON-NLS-1$
			
			DataDescription paraDifferenzialkontrolle = new DataDescription(
					dav.getDataModel().getAttributeGroup("atg.ufdsDifferenzialKontrolle" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
					(short)0);
			ResultData parameterSatz = new ResultData(sensor, paraDifferenzialkontrolle, System.currentTimeMillis(), datum);
			dav.sendData(parameterSatz);
		}
		
		/**
		 * Warte eine Sekunde bis die Parameter sicher da sind
		 */
		Pause.warte(1000L);
		
				
		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Pr�fung logisch UFD kommen
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			DataDescription datenBeschreibung = new DataDescription(
					dav.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect("asp.plausibilit�tsPr�fungLogisch"), //$NON-NLS-1$
					(short)0);
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}
		
		/**
		 * Warte eine Sekunde bis Datenanmeldung durch ist
		 */
		Pause.warte(1000L);
	}
	
	
	/**
	 * F�hrt den Vergleich aller Ist-Werte mit allen Soll-Werten durch
	 * und zeigt die Ergebnisse an. Gleichzeitig werden die Ergebnisse
	 * �ber <code>JUnit</code> getestet<br><br>
	 * Nach dem Test werden die Mengen der Soll- und Ist-Werte wieder
	 * gel�scht
	 */
	private final void ergebnisUeberpruefen(){
		if(!this.ergebnisIst.isEmpty() && !this.ergebnisSoll.isEmpty()){				
			for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
				System.out.println("Vergleiche (DIFF)" + sensor.getPid() + ": Soll(" + (this.ergebnisSoll.get(sensor)?"impl":"ok") +//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						"), Ist("  //$NON-NLS-1$
						+ (this.ergebnisIst.get(sensor)?"impl":"ok") + ") --> " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
						(this.ergebnisSoll.get(sensor) == this.ergebnisIst.get(sensor)?"Ok":"!!!FEHLER!!!")); //$NON-NLS-1$ //$NON-NLS-2$
				Assert.assertEquals("Objekt: " + sensor.toString(), //$NON-NLS-1$
						this.ergebnisSoll.get(sensor), 
						this.ergebnisIst.get(sensor));
			}				
		}
		this.ergebnisIst.clear();
		this.ergebnisSoll.clear();		
	}
	
	
	
	
	/**
	 * Anzahl der Intervalle, die der Test der Differenzialkontrolle laufen soll
	 */
	private static final int TEST_DIFF_KONTROLLE_LAEUFE = 10;
	
	/**
	 * der eigentliche Test
	 */
	@Test
	public void testUFDDifferenzialKontrolle()
	throws Exception{
		
		/**
		 * Konstanzz�hler f�r Objekte, die als Implausibel zu markieren sind
		 */
		int konstanzZaehler_OK = 0;
		
		/**
		 * Konstanzz�hler f�r Objekte, die als nicht als Implausbiel zu markieren sind
		 */
		int konstanzZaehler_Impl = 0;
				
		/**
		 * Zeile 1 in Tabelle auf Seite 24 (QS-02.04.00.00.00-PrSpez-2.0 [DUA])
		 * Objekt f�r Folge von Messwerten, die sich (gerade) so h�ufig �ndert,
		 * dass die maximale Zeitdauer der Ergebniskonstanz nicht erreichen
		 */
		SystemObject objMaxGleichUndKontrolle = PlPruefungLogischUFDTest.fbt1;

		/**
		 * Zeile 2 in Tabelle auf Seite 24 (QS-02.04.00.00.00-PrSpez-2.0 [DUA])
		 * Objekt f�r Folge von Messwerten, die sich innerhalb der maximalen
		 * Zeitdauer nicht �ndert, dabei aber die Bedingung f�r die Differentialkontrolle
		 * nicht erf�llt
		 */
		SystemObject objImmerGleichUndKeineKontrolle = PlPruefungLogischUFDTest.lt1;

		/**
		 * Zeile 3=5 in Tabelle auf Seite 24 (QS-02.04.00.00.00-PrSpez-2.0 [DUA])
		 * Objekt f�r Folge von Messwerten, die sich innerhalb der maximalen Zeitdauer
		 * nicht �ndert und die eventuell vorhandene Bedingung f�r die Differentialkontrolle
		 * erf�llt
		 */
		SystemObject objImmerGleichUndKontrolle = PlPruefungLogischUFDTest.ni1;
		
		/**
		 * Zeile 4 in Tabelle auf Seite 24 (QS-02.04.00.00.00-PrSpez-2.0 [DUA])
		 * Messwert, der sich vom Vorg�ngerwert unterscheidet<br>
		 * Dies sind alle anderen Sensoren
		 */
				
		
		for(int durchlauf = 0; durchlauf<TEST_DIFF_KONTROLLE_LAEUFE; durchlauf++){
			
			this.ergebnisUeberpruefen();
			
			GregorianCalendar kal = new GregorianCalendar();
			kal.setTimeInMillis(System.currentTimeMillis());
			kal.set(Calendar.MILLISECOND, 0);
			final long zeitStempel = kal.getTimeInMillis();
			
			konstanzZaehler_Impl++;
			konstanzZaehler_OK++;
			
			/**
			 * Produziere Werte, die getestet werden und "unbesch�digt"
			 * durch die Diff-Pr�fung kommen
			 */
			for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
				ResultData resultat = TestUtensilien.getExterneErfassungDatum(sensor);
				UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
				datum.setT(STANDARD_T);
				/**
				 * Setzte Wert erst mal immer auf alternierend 1 und 2
				 */
				datum.getWert().setWert(1 + durchlauf%2);
				
				ResultData sendeDatum = null;
				
				if(sensor.equals(objImmerGleichUndKeineKontrolle)){
					datum.getWert().setWert(5);
					this.ergebnisSoll.put(resultat.getObject(), false);
				}else
				if(sensor.equals(objImmerGleichUndKontrolle)){
					datum.getWert().setWert(3);
					if(konstanzZaehler_Impl > STANDARD_MAX_INTERVALLE){
						this.ergebnisSoll.put(resultat.getObject(), true);
					}else{
						this.ergebnisSoll.put(resultat.getObject(), false);
					}
				}else
				if(sensor.equals(objMaxGleichUndKontrolle)){
					if(konstanzZaehler_OK > STANDARD_MAX_INTERVALLE){
						datum.getWert().setWert(3);
						konstanzZaehler_OK = 0;
					}else{
						datum.getWert().setWert(4);
					}
					this.ergebnisSoll.put(resultat.getObject(), false);
				}else{
					/**
					 * Andere Werte einfach senden
					 */
					this.ergebnisSoll.put(resultat.getObject(), false);
				}
				sendeDatum = new ResultData(datum.getOriginalDatum().getObject(),
						    datum.getOriginalDatum().getDataDescription(), 
						zeitStempel, datum.getDatum());
				
				PlPruefungLogischUFDTest.SENDER.sende(sendeDatum);
			}
						
			/**
			 * Warte bis zum n�chsten Intervall
			 */
			Pause.warte(STANDARD_T);
		}
		
	}


	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		// 		
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(resultat);
					boolean implausibelUndFehlerhaft = ufdDatum.getWert().isFehlerhaft() &&
													   ufdDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA;
					this.ergebnisIst.put(resultat.getObject(), implausibelUndFehlerhaft);
				}
			}
		}
	}
}
