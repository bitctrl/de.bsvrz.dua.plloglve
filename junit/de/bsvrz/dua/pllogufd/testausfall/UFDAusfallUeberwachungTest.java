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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
 * Voraussetzungen (Testbedingungen):<br>
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
	 * Die Daten werden im Abstand von <code>ABSTAND + Random.nextInt(ABSTAND)</code> versendet
	 */
	protected static final int ABSTAND = 500;
	
	/**
	 * Die Zeit (in ms) die die erwartete Eintreffzeit eines Datums von
	 * der tatsächlichen Eintreffzeit differieren darf
	 */
	protected static final long ERGEBNIS_TOLERANZ = 500;
	
	/**
	 * jeder sovielte Sensor-Wert wird nicht versandt
	 */
	protected static final int AUSFALL = 5;
	
	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx1
	 */
	private static final long MAX_VERZUG_1 = 3000L;
	
	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx2
	 */
	private static final long MAX_VERZUG_2 = 4000L;
	
	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx3
	 */
	private static final long MAX_VERZUG_3 = 6000L;
	
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
	 * letzter Soll-Ergebnis-Wert von einem Sensor   
	 */
	private Map<SystemObject, Ergebnis> ergebnisSoll = new HashMap<SystemObject, Ergebnis>();

	/**
	 * letzte Ist-Ergebnis-Werte von einem Sensor.
	 * Dies sind hier mehrere, da ggf. auch mehrere Datensätze pro Intervall empfangen werden können,
	 * wenn z.B. die Ausfallkontrolle <b>und</b> die Testapplikation einen Wert senden
	 */
	private Map<SystemObject, Collection<Ergebnis>> ergebnisIst = new HashMap<SystemObject, Collection<Ergebnis>>();


	
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
		 * Warte bis Anmeldung sicher durch ist
		 */
		Pause.warte(1000L);
		
		/**
		 * Parameter setzen auf 10s (für Sensoren xxx1), 15s (für Sensoren xxx2) und 20s (für Sensoren xxx3)
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			if(sensor.getPid().endsWith("1")){ //$NON-NLS-1$
				this.setMaxAusfallFuerSensor(sensor, MAX_VERZUG_1);
			}else
			if(sensor.getPid().endsWith("2")){ //$NON-NLS-1$
				this.setMaxAusfallFuerSensor(sensor, MAX_VERZUG_2);
			}else
			if(sensor.getPid().endsWith("3")){ //$NON-NLS-1$
				this.setMaxAusfallFuerSensor(sensor, MAX_VERZUG_3);
			}
		}
		
		/**
		 * Warte eine Sekunde bis die Parameter sicher da sind
		 */
		
				
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

				Collection<Ergebnis> istErgebnisse = this.ergebnisIst.get(sensor);
				Ergebnis erfolgsErgebnis = null;
				if(istErgebnisse == null){
					System.out.println("NULL: " + sensor); //$NON-NLS-1$
				}else{
					for(Ergebnis istErgebnis:istErgebnisse){
						if(istErgebnis.equals(this.ergebnisSoll.get(sensor))){
							erfolgsErgebnis = istErgebnis;
							break;
						}
					}
					
					if(erfolgsErgebnis != null){
						System.out.println("Vergleiche (AUSFALL)" + sensor.getPid() + ": Soll(" + this.ergebnisSoll.get(sensor) +//$NON-NLS-1$ //$NON-NLS-2$
									"), Ist(" + erfolgsErgebnis + ") --> Ok"); //$NON-NLS-1$ //$NON-NLS-2$
					}else{
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!Fehler!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //$NON-NLS-1$
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!Fehler!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //$NON-NLS-1$
						System.out.println("  Soll: " + this.ergebnisSoll.get(sensor)); //$NON-NLS-1$
						System.out.println("  Ist-Werte: "); //$NON-NLS-1$
						for(Ergebnis istErgebnis:istErgebnisse){
							System.out.println("    " + istErgebnis); //$NON-NLS-1$
						}
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!Fehler!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //$NON-NLS-1$
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!Fehler!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //$NON-NLS-1$
					}
				}
				
				/**
				 * JUnit-Test
				 */
				//Assert.assertTrue("Felher an Sensor: " + sensor.getPid(), erfolgsErgebnis != null); //$NON-NLS-1$
			}			
		}
		
		/**
		 * Lösche Soll- und Ist-Ergebnissmenge vor nächstem Durchlauf
		 */
		this.ergebnisIst.clear();
		this.ergebnisSoll.clear();		
	}
	
	
	/**
	 * Anzahl der Intervalle, die der Test der Ausfallüberwachung laufen soll
	 */
	private static final int TEST_AUSFALL_UEBERWACHUNG_LAEUFE = 10000;
	
	
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
	
			/**
			 * Warte bis zum Anfang der nächsten Minute
			 */
			final long start = TestUtensilien.getBeginNaechsterMinute();
			Pause.warte(start - System.currentTimeMillis() + 100);
			
			this.ergebnisUeberpruefen();
			
			System.out.println("---\nTestlauf Nr." + (testZaehler+1) + "\n---"); //$NON-NLS-1$ //$NON-NLS-2$
			

			/**
			 * In dieser Schleife wird für jeden Sensor im stochastischen Takt jeweils 
			 * ein Datum gesendet. Die Reihenfolge der Sensoren wird dabei vor jedem Durchlauf
			 * neu "ausgewürfelt". Jeder <code>AUSFALL</code>-te Sensor wird ignoriert
			 */
			int[] indexFeld = DAVTest.getZufaelligeZahlen(PlPruefungLogischUFDTest.SENSOREN.size());
			for(int i = 0; i<indexFeld.length; i++){
				SystemObject sensor = PlPruefungLogischUFDTest.SENSOREN.get(indexFeld[i]);
				
				/**
				 * Dieser Wert fällt komplett aus
				 */
				if(DAVTest.R.nextInt(AUSFALL) == 0){
					Ergebnis erwartetesErgebnis = new Ergebnis(sensor, start - Konstante.MINUTE_IN_MS, true);
					this.ergebnisSoll.put(sensor, erwartetesErgebnis);
					System.out.println("Sende nicht: " + erwartetesErgebnis);  //$NON-NLS-1$
					continue;
				}
				
				ResultData resultat = TestUtensilien.getExterneErfassungDatum(sensor);
				resultat.setDataTime(start - Konstante.MINUTE_IN_MS);
				PlPruefungLogischUFDTest.SENDER.sende(resultat);
				
				/**
				 * Berechne den Status (ob <code>nicht erfasst</code>)
				 * des gerade versendeten Datums
				 */
				Boolean nichtErfasst = null;	// == egal
				
				if(sensor.getPid().endsWith("1")){ //$NON-NLS-1$
					if(Math.abs(System.currentTimeMillis() - start - MAX_VERZUG_1) > ERGEBNIS_TOLERANZ){
						nichtErfasst = System.currentTimeMillis() - start > MAX_VERZUG_1;	
					}
				}
				if(sensor.getPid().endsWith("2")){ //$NON-NLS-1$
					if(Math.abs(System.currentTimeMillis() - start - MAX_VERZUG_2) > ERGEBNIS_TOLERANZ){
						nichtErfasst = System.currentTimeMillis() - start > MAX_VERZUG_2;	
					}
				}
				if(sensor.getPid().endsWith("3")){ //$NON-NLS-1$
					if(Math.abs(System.currentTimeMillis() - start - MAX_VERZUG_3) > ERGEBNIS_TOLERANZ){
						nichtErfasst = System.currentTimeMillis() - start > MAX_VERZUG_3;	
					}
				}

				Ergebnis erwartetesErgebnis = new Ergebnis(sensor,
								start - Konstante.MINUTE_IN_MS,
								nichtErfasst);
				
				this.ergebnisSoll.put(sensor, erwartetesErgebnis);
				System.out.println(jetzt() + "Sende: " + erwartetesErgebnis);  //$NON-NLS-1$
				
				Pause.warte(ABSTAND + DAVTest.R.nextInt(ABSTAND));
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
					
					Ergebnis ergebnisIstFuerSensor = new Ergebnis(resultat.getObject(), resultat.getDataTime(),
																  ufdDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA);					 

					Collection<Ergebnis> ergebnisseBisJetzt = this.ergebnisIst.get(resultat.getObject());
					if(ergebnisseBisJetzt == null){
						ergebnisseBisJetzt = new ArrayList<Ergebnis>();
						ergebnisseBisJetzt.add(ergebnisIstFuerSensor);
						this.ergebnisIst.put(resultat.getObject(), ergebnisseBisJetzt);
					}else{
						ergebnisseBisJetzt.add(ergebnisIstFuerSensor);
					}
					
					System.out.println(jetzt() + ", Empfange: " + ergebnisIstFuerSensor); //$NON-NLS-1$
				}
			}
		}
	}
	
	
	/**
	 * Erfragt die aktuelle Zeit als String
	 * 
	 * @return die aktuelle Zeit als String
	 */
	private final String jetzt(){
		return "(JETZT:" + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(System.currentTimeMillis())) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	/**
	 * Für den Test der Ausfallkontrolle relevanter Teil der Informationen
	 * eines Sensorwertes. Über Objekte dieser Klasse wird der Soll-Ist-Vergleich
	 * vorgenommen.
	 * 
	 * @author BitCtrl Systems GmbH, Thierfelder
	 *
	 */
	private class Ergebnis{
		
		/**
		 * Datenzeit
		 */
		protected long datenZeit = 1;
		
		/**
		 * Systemobjekt eines Umfelddatensensors
		 */
		protected SystemObject sensor = null;
		
		/**
		 * ob das Datum als <code>nicht erfasst</code> gekennzeichnet erwartet
		 * wird. <code>null</code> gildt als "egal"
		 */
		protected Boolean nichtErfasst = false;
		
		
		/**
		 * Standardkontruktor
		 * 
		 * @param sensor Systemobjekt eines Umfelddatensensors
		 * @param datenZeit Datenzeit
		 * @param nichtErfasst ob das Datum als <code>nicht erfasst</code> gekennzeichnet erwartet
		 * wird. <code>null</code> gilt als "egal"<br>
		 * <b>Achtung:</b> Die Markierung "egal" wird verwendet, wenn die Zeit, zu der das Datum
		 * verschickt wurde und die Zeit, zu der es die Ausfallkontrolle hätte ebenfalls verschicken
		 * sollen, innerhalb des Toleranzbereichs </code>ERGEBNIS_TOLERANZ</code> liegen 
		 */
		public Ergebnis(SystemObject sensor, long datenZeit, Boolean nichtErfasst){
			this.sensor = sensor;
			this.datenZeit = datenZeit;
			this.nichtErfasst = nichtErfasst;
		}


		/**
		 * Zwei Ergebnisdatensätze gelten als gleich, wenn:<br>
		 * - die Sensoren identisch sind,<br>
		 * - die erwarteten Datenzeiten identisch sind und<br>
		 * - die Markierungen als <code>nicht erfasst</code> identisch sind (so diese nicht als "egal" markiert sind) 
		 */
		@Override
		public boolean equals(Object obj) {
			boolean ergebnis = false;

			if(obj instanceof Ergebnis){
				Ergebnis that = (Ergebnis)obj;
				if(this.sensor.equals(that.sensor) && this.datenZeit == that.datenZeit){
					ergebnis = true;
					if(this.nichtErfasst != null && that.nichtErfasst != null){
						ergebnis &= this.nichtErfasst == that.nichtErfasst;
					}
				}
			}

			return ergebnis;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			String nichtErfasstStr = (this.nichtErfasst == null?"egal":(this.nichtErfasst?"ja":"nein")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			
			return "Sensor: " + this.sensor.getPid() + ", Daten: " +   //$NON-NLS-1$//$NON-NLS-2$
				DUAKonstanten.NUR_ZEIT_FORMAT_GENAU.format(new Date(this.datenZeit)) +
				", nicht Erfasst: " + nichtErfasstStr;  //$NON-NLS-1$
		}
	}
}
