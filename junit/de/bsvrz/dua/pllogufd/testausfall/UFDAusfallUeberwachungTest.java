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

package de.bsvrz.dua.pllogufd.testausfall;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.DataNotSubscribedException;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.common.SendSubscriptionNotConfirmed;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.pllogufd.DAVTest;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.TestUtensilien;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Test des Moduls Ausfallüberwachung.<br>
 * Voraussetzungen:<br>
 * 1.) Alle Sensoren im Konfigurationsbereich <code>kb.duaTestObjekteUFD</code> werden überwacht<br>
 * 2.) Daten werden im Minutenintervall zur vollen Minute gesendet (Datenzeitstempel)<br>
 * 3.) Datenverzug wird auf 10s (für Sensoren xxx1), 15s (für Sensoren xxx2) und 20s (für Sensoren xxx3)
 * gesetzt<br> 
 * 4.) jedes zehnte Datum fällt komplett aus<br>
 * (Die Punkte 2.-4. werden durch den Test selbst realisiert)<br>
 * <br>
 * In diesem Test wird für alle Sensoren zunächst ein Datum mit dem Datenzeitstempel
 * der bereits vergangenen Minute gesendet (TS = Zeitpunkt Teststart, 0=Zeitstempel
 * der bereits vergangenen Minute) um die Applikation zu initialisieren. 
 * Dann (nach Zeitpunkt 2) werden für alle Sensoren im Sekundenintervall Daten gesendet,
 * die <b>nicht</b> als <code>nicht erfasst</code> markiert sind. Die Ausfall-Informationen
 * zu den Datensätzen werden beim Versand berechnet und gespeichert (jeweils Sensor und
 * erwarteter Zustand).<br><br>
 * 
 *  0(erstes Dat.)        1     TS              2                    3<br>
 *  |---------------------|---------------------|--------------------|<br><br>
 *  
 *  Sollten die empfangenen Daten von den je Sensor berechneten Informationen abweichen,
 *  gilt der Test als nicht bestanden. (Der Test läuft <code>TEST_AUSFALL_UEBERWACHUNG_LAEUFE</code> mal)
 *  <br><br>
 *  Alle Ergebnisse des Tests werden in die Konsole ausgegeben
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class UFDAusfallUeberwachungTest 
implements ClientSenderInterface, ClientReceiverInterface{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Parameterbeschreibung der Ausfallüberwachung
	 */
	private DataDescription paraAusfallUeberwachung = null;
	
	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	/**
	 * letzter Soll-Ergebnis-Wert <code>Ausgefallen</code> von einem Sensor   
	 */
	private Map<SystemObject, Boolean> ergebnisSoll = new HashMap<SystemObject, Boolean>();

	/**
	 * letzter Ist-Ergebnis-Wert <code>Ausgefallen</code> von einem Sensor 
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
		paraAusfallUeberwachung = new DataDescription(
				dav.getDataModel().getAttributeGroup("atg.ufdsAusfallÜberwachung"), //$NON-NLS-1$
				dav.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
				(short)0);
		dav.subscribeSender(this, PlPruefungLogischUFDTest.SENSOREN, paraAusfallUeberwachung, SenderRole.sender());
		
		/**
		 * Parameter setzen auf 10s (für Sensoren xxx1), 15s (für Sensoren xxx2) und 20s (für Sensoren xxx3)
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			if(sensor.getPid().endsWith("1")){ //$NON-NLS-1$
				this.setMaxAusfallFuerSensor(sensor, 10000L);
			}else
			if(sensor.getPid().endsWith("2")){ //$NON-NLS-1$
				this.setMaxAusfallFuerSensor(sensor, 15000L);
			}else
			if(sensor.getPid().endsWith("3")){ //$NON-NLS-1$
				this.setMaxAusfallFuerSensor(sensor, 20000L);
			}
		}
		
		/**
		 * Warte eine Sekunde bis die Parameter sicher da sind
		 */
		Pause.warte(1000L);
		
				
		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Prüfung logisch UFD kommen
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			DataDescription datenBeschreibung = new DataDescription(
					dav.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"), //$NON-NLS-1$
					(short)0);
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}
		
		/**
		 * Warte eine Sekunde bis Datenanmeldung durch ist
		 */
		Pause.warte(1000L);
		
		
		/**
		 * Sende initiale Daten für alle Sensoren mit dem Datenzeitstempel der vergangenen Minute
		 */
		long ersteDatenZeit = TestUtensilien.getBeginNaechsterMinute() - Konstante.MINUTE_IN_MS * 2;
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			ResultData resultat = TestUtensilien.getExterneErfassungDatum(sensor);
			resultat.setDataTime(ersteDatenZeit);
			PlPruefungLogischUFDTest.SENDER.sende(resultat);
		}
		
		Pause.warte(1000L);
	}

	
	/**
	 * Setzt den maximalen Zeitverzug eines Umfelddatensensors
	 * 
	 * @param obj Umfelddatensensor
	 * @param verzugInMillis maximalen Zeitverzug in ms
	 */
	private final void setMaxAusfallFuerSensor(final SystemObject obj, final long verzugInMillis){
		Data parameterData = dav.createData(dav.getDataModel().
				getAttributeGroup("atg.ufdsAusfallÜberwachung")); //$NON-NLS-1$
		parameterData.getTimeValue("maxZeitVerzug").setMillis(verzugInMillis); //$NON-NLS-1$
		ResultData parameter = new ResultData(obj, 
				this.paraAusfallUeberwachung, System.currentTimeMillis(), parameterData);
		
		try {
			this.dav.sendData(parameter);
		} catch (DataNotSubscribedException e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		} catch (SendSubscriptionNotConfirmed e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		}
	}
	
	
	/**
	 * Führt den Vergleich aller Ist-Werte mit allen Soll-Werten durch
	 * und zeigt die Ergebnisse an. Gleichzeitig werden die Ergebnisse
	 * über <code>JUnit</code> getestet<br><br>
	 * Nach dem Test werden die Mengen der Soll- und Ist-Werte wieder
	 * gelöscht
	 */
	private final void ergebnisUeberpruefen(){
		if(!this.ergebnisIst.isEmpty() && !this.ergebnisSoll.isEmpty()){				
			for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
				System.out.println("Vergleiche " + sensor.getPid() + ": Soll(" + (this.ergebnisSoll.get(sensor)?"timeout":"in time") +//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						"), Ist("  //$NON-NLS-1$
						+ (this.ergebnisIst.get(sensor)?"timeout":"in time") + ") --> " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
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
	 * Anzahl der Intervalle, die der Test der Ausfallüberwachung laufen soll
	 */
	private static final int TEST_AUSFALL_UEBERWACHUNG_LAEUFE = 100;
	
	
	/**
	 * der eigentliche Test
	 */
	@Test
	public void testUFDAusfallUeberwachung()
	throws Exception{
						
		/**
		 * Test-Schleife
		 */
		for(int testZaehler = 0; testZaehler < TEST_AUSFALL_UEBERWACHUNG_LAEUFE; testZaehler++){
	
			System.out.println("---\nTestlauf Nr." + (testZaehler+1) + "\n---"); //$NON-NLS-1$ //$NON-NLS-2$
			
			this.ergebnisUeberpruefen();
			
			/**
			 * Warte bis zum Anfang der nächsten Minute
			 */
			final long start = TestUtensilien.getBeginNaechsterMinute();
			Pause.warte(start - System.currentTimeMillis());

			/**
			 * In dieser Schleife wird für jeden Sensor im Takt von einer Sekunde jeweils 
			 * ein Datum gesendet. Die Reihenfolge der Sensoren wird dabei vor jedem Durchlauf
			 * neu "ausgewürfelt". Jeder zehnte Sensor wird ignoriert
			 */
			int[] indexFeld = DAVTest.getZufaelligeZahlen(PlPruefungLogischUFDTest.SENSOREN.size());
			for(int i = 0; i<indexFeld.length; i++){
				SystemObject sensor = PlPruefungLogischUFDTest.SENSOREN.get(indexFeld[i]);
				
				/**
				 * Dieser Wert fällt komplett aus
				 */
				if(DAVTest.R.nextInt(10) == 0){
					this.ergebnisSoll.put(sensor, true);
					System.out.println("Sende nicht: " + sensor.getName());  //$NON-NLS-1$
					continue;
				}
				
				ResultData resultat = TestUtensilien.getExterneErfassungDatum(sensor);
				resultat.setDataTime(start - Konstante.MINUTE_IN_MS);
				PlPruefungLogischUFDTest.SENDER.sende(resultat);
				
				if(sensor.getPid().endsWith("1")){ //$NON-NLS-1$
					this.ergebnisSoll.put(sensor, System.currentTimeMillis() - start > 10000);
					System.out.println("Sende: " + sensor.getName() +  //$NON-NLS-1$
							(System.currentTimeMillis() - start > 10000?" timeout":" in time"));  //$NON-NLS-1$//$NON-NLS-2$
				}
				if(sensor.getPid().endsWith("2")){ //$NON-NLS-1$
					this.ergebnisSoll.put(sensor, System.currentTimeMillis() - start > 15000);
					System.out.println("Sende: " + sensor.getName() +  //$NON-NLS-1$
							(System.currentTimeMillis() - start > 15000?" timeout":" in time"));  //$NON-NLS-1$//$NON-NLS-2$
				}
				if(sensor.getPid().endsWith("3")){ //$NON-NLS-1$
					this.ergebnisSoll.put(sensor, System.currentTimeMillis() - start > 20000);
					System.out.println("Sende: " + sensor.getName() +  //$NON-NLS-1$
							(System.currentTimeMillis() - start > 20000?" timeout":" in time"));  //$NON-NLS-1$//$NON-NLS-2$
				}
				
				Pause.warte(1000L);
			}			
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
					if(this.ergebnisIst.get(resultat.getObject()) == null){
						this.ergebnisIst.put(resultat.getObject(), 
								ufdDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA);
						System.out.println("Empfange: " + resultat.getObject().getName() +  //$NON-NLS-1$
								(ufdDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA?" timeout":" in time")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}
	}
}
