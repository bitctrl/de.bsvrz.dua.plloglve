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

package de.bsvrz.dua.pllogufd.testaufab;

import java.util.Calendar;
import java.util.GregorianCalendar;
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
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.DAVTest;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.TestUtensilien;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Test des Moduls Anstieg-Abfall-Kontrolle<br>
 * Der Test implementiert die Vorgaben aus dem Dokument [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class AnstiegAbfallKontrolleTest 
implements ClientSenderInterface, ClientReceiverInterface{
	
	/**
	 * Markierung der einzelnen Messwerte analog Graphik 5-4 
	 * [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25
	 */
	protected static enum MARKIERUNG{
		ok,
		nicht_ermittelbar,
		implausibel_o_fehlerfahft
	}
	
	/**
	 * Standardintervalllänge für Testdaten 5s
	 */
	private static final long STANDARD_T = Konstante.SEKUNDE_IN_MS * 2;
	
	/**
	 * Messwerte mit der Vorgänger-Nachfolger-Relation entsprechend der
	 * Graphik in [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25 (bei maximaler 
	 * Differenz von 5)
	 */
	private static MessWert[] MESSWERTE = null;
	
	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;

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
			DataDescription paraAnstiegAbfallKontrolle = new DataDescription(
					dav.getDataModel().getAttributeGroup("atg.ufdsAnstiegAbstiegKontrolle" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
					(short)0);
			dav.subscribeSender(this, sensor, paraAnstiegAbfallKontrolle, SenderRole.sender());			
		}
		
		/**
		 * maximal zulässige Differenz auf pauschal 5 setzen
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			Data datum = dav.createData(dav.getDataModel().getAttributeGroup(
					"atg.ufdsAnstiegAbstiegKontrolle" + datenArt.getName())); //$NON-NLS-1$
			
			datum.getUnscaledValue(datenArt.getAbkuerzung() + "maxDiff").set(5); //$NON-NLS-1$
			
			DataDescription paraAnstiegAbfallKontrolle = new DataDescription(
					dav.getDataModel().getAttributeGroup("atg.ufdsAnstiegAbstiegKontrolle" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
					(short)0);
			ResultData parameterSatz = new ResultData(sensor, paraAnstiegAbfallKontrolle, System.currentTimeMillis(), datum);
			dav.sendData(parameterSatz);
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
		 * Initialisierung der Messwerte mit der Vorgänger-Nachfolger-Relation entsprechend der
		 * Graphik 4-5 in [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25 (bei maximaler Differenz von 5)
		 */
		MESSWERTE = new MessWert[]{
				new MessWert(5,  MARKIERUNG.ok, null),
				new MessWert(5,  MARKIERUNG.ok, null),
				new MessWert(11, MARKIERUNG.ok, true),
				new MessWert(9,  MARKIERUNG.ok, null),
				new MessWert(9,  MARKIERUNG.ok, null),
				new MessWert(8,  MARKIERUNG.ok, null),
				new MessWert(4,  MARKIERUNG.ok, null),
				new MessWert(10, MARKIERUNG.ok, true),
				new MessWert(1,  MARKIERUNG.ok, true),
				new MessWert(2,  MARKIERUNG.ok, null),
				new MessWert(3,  MARKIERUNG.ok, null),
				new MessWert(3,  MARKIERUNG.ok, null),
				new MessWert(9,  MARKIERUNG.ok, true),
				new MessWert(9,  MARKIERUNG.ok, null),
				new MessWert(6,  MARKIERUNG.ok, null),
				new MessWert(6,  MARKIERUNG.nicht_ermittelbar, false),
				new MessWert(15, MARKIERUNG.ok, false),
				new MessWert(15, MARKIERUNG.ok, null),
				new MessWert(12, MARKIERUNG.implausibel_o_fehlerfahft, false),
				new MessWert(4,  MARKIERUNG.ok, false),
				new MessWert(4,  MARKIERUNG.ok, null),
				new MessWert(4,  MARKIERUNG.implausibel_o_fehlerfahft, false),
				new MessWert(12, MARKIERUNG.ok, false),
				new MessWert(12, MARKIERUNG.nicht_ermittelbar, false),
				new MessWert(8,  MARKIERUNG.ok, null),
				new MessWert(8,  MARKIERUNG.ok, null),
				new MessWert(8,  MARKIERUNG.ok, null),
				new MessWert(8,  MARKIERUNG.ok, null),
				new MessWert(4,  MARKIERUNG.ok, null),
				new MessWert(4,  MARKIERUNG.ok, null)				
			};
	}
	
	
	/**
	 * Führt den Vergleich aller Ist-Werte mit allen Soll-Werten durch
	 * und zeigt die Ergebnisse an. Gleichzeitig werden die Ergebnisse
	 * über <code>JUnit</code> getestet<br><br>
	 * Nach dem Test werden die Mengen der Soll- und Ist-Werte wieder
	 * gelöscht
	 */
	private final void ergebnisUeberpruefen(final int durchlauf){
		if(!this.ergebnisIst.isEmpty()){
			Boolean erwartung = MESSWERTE[durchlauf].wirdAlsFehlerhaftUndImplausibelErwartet();
			if(erwartung != null){
				boolean erwarteterStatusIstImplausibelUndFehlerHaft = erwartung; 
				for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){				
					System.out.println("Vergleiche (AAKONTR)[" + durchlauf + "] " + sensor.getPid() + ": Soll(" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
							(erwarteterStatusIstImplausibelUndFehlerHaft?"impl":"ok") +//$NON-NLS-1$ //$NON-NLS-2$
							"), Ist("  //$NON-NLS-1$
							+ (this.ergebnisIst.get(sensor)?"impl":"ok") + ") --> " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
							(erwarteterStatusIstImplausibelUndFehlerHaft == this.ergebnisIst.get(sensor)?"Ok":"!!!FEHLER!!!")); //$NON-NLS-1$ //$NON-NLS-2$
					Assert.assertEquals("Objekt: " + sensor.toString(), //$NON-NLS-1$
							erwarteterStatusIstImplausibelUndFehlerHaft, 
							this.ergebnisIst.get(sensor).booleanValue());
				}
			}
		}
		this.ergebnisIst.clear();	
	}
	
	
	/**
	 * der eigentliche Test
	 */
	@Test
	public void testUFDDifferenzialKontrolle()
	throws Exception{
		
		for(int durchlauf = 0; durchlauf<MESSWERTE.length; durchlauf++){
						
			GregorianCalendar kal = new GregorianCalendar();
			kal.setTimeInMillis(System.currentTimeMillis());
			kal.set(Calendar.MILLISECOND, 0);
			final long zeitStempel = kal.getTimeInMillis();
						
			/**
			 * Produziere Werte, die getestet werden und "unbeschädigt"
			 * durch die Diff-Prüfung kommen
			 */
			for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
				ResultData resultat = TestUtensilien.getExterneErfassungDatum(sensor);
				UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(resultat);
				datum.setT(STANDARD_T);
				/**
				 * Setzte Prüfwert
				 */
				datum.getWert().setWert(MESSWERTE[durchlauf].getWert());
				if(MESSWERTE[durchlauf].getMarkierung() == MARKIERUNG.nicht_ermittelbar){
					datum.getWert().setNichtErmittelbarAn();
				}else
				if(MESSWERTE[durchlauf].getMarkierung() == MARKIERUNG.implausibel_o_fehlerfahft){
					if(durchlauf%2 == 0){
						datum.getWert().setFehlerhaftAn();
					}else{
						datum.setStatusMessWertErsetzungImplausibel(DUAKonstanten.JA);
					}
				}
				
				ResultData sendeDatum = new ResultData(datum.getOriginalDatum().getObject(),
						    datum.getOriginalDatum().getDataDescription(), 
						    zeitStempel, datum.getDatum());
				
				PlPruefungLogischUFDTest.SENDER.sende(sendeDatum);
			}
						
			/**
			 * Warte bis zum nächsten Intervall
			 */
			Pause.warte(STANDARD_T);
			
			this.ergebnisUeberpruefen(durchlauf);
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
	
	
	/**
	 * Ein Objekt dieser Klasse entspricht einem Punkt innerhalb der Graphik
	 * aus [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25
	 * 
	 * @author Thierfelder
	 *
	 */
	protected class MessWert{
		
		/**
		 * Der Sensor-Wert
		 */
		private long wert = -1;
		
		/**
		 * Markierung des Wertes (analog [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25)
		 * (Eingangsrichtung)
		 */
		private MARKIERUNG markierung = null;
		
		/**
		 * gibt an, ob das Pl-Ergebnis der Prüfung dieses Wertes als
		 * <code>implausibel</code> und </code>fehlerhaft</code> gekennzeichnet erwartet wird
		 * <code>null</code> wird als don't care interpretiert
		 */
		private Boolean fehlerhaftUndImplausibel = null;
		

		/**
		 * Standardkontruktor
		 * 
		 * @param wert der Sensor-Wert
		 * @param markierung Markierung des Wertes (analog [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25)
		 * @param fehlerhaftUndImplausibel gibt an, ob das Pl-Ergebnis der Prüfung dieses Wertes als
		 * <code>implausibel</code> und <code>fehlerhaft</code> gekennzeichnet erwartet wird<br>
		 * <code>null</code> wird als don't care interpretiert
		 */
		public MessWert(final long wert, final MARKIERUNG markierung, final Boolean fehlerhaftUndImplausibel){
			this.wert = wert;
			this.markierung = markierung;
			this.fehlerhaftUndImplausibel = fehlerhaftUndImplausibel;
		}


		/**
		 * Erfragt die Markierung des Wertes (analog [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25)
		 * 
		 * @return markierung Markierung des Wertes (analog [QS-02.04.00.00.00-PrSpez-2.0 (DUA)], S. 25)
		 */
		public final MARKIERUNG getMarkierung() {
			return markierung;
		}


		/**
		 * Erfragt den Sensor-Wert
		 * 
		 * @return wert der Sensor-Wert
		 */
		public final long getWert() {
			return wert;
		}


		/**
		 * Erfragt, ob das Pl-Ergebnis der Prüfung dieses Wertes als
		 * <code>implausibel</code> und </code>fehlerhaft</code> gekennzeichnet
		 * erwartet wird
		 * 
		 * @return fehlerhaftUndImplausibel ob das Pl-Ergebnis der Prüfung dieses Wertes als
		 * <code>implausibel</code> und </code>fehlerhaft</code> gekennzeichnet erwartet wird<br>
		 * <code>null</code> wird als don't care interpretiert
		 */
		public final Boolean wirdAlsFehlerhaftUndImplausibelErwartet() {
			return fehlerhaftUndImplausibel;
		}
		
	}
}
