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

package de.bsvrz.dua.pllogufd.testmeteo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.Aspect;
import stauma.dav.configuration.interfaces.AttributeGroup;
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
 * Super-Klasse für alle Tests der Meteorologischen Kontrolle. Sendet Standardparameter
 * und meldet sich als Empfänger auf alle Umfelddaten unter dem Aspekt
 * <code>asp.plausibilitätsPrüfungLogisch</code> an
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public abstract class MeteorologischeKontrolleTest 
implements ClientSenderInterface, ClientReceiverInterface{

	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Standardintervalllänge für Testdaten 2s
	 */
	protected static final long STANDARD_T = Konstante.SEKUNDE_IN_MS * 2;
	
	/**
	 * Zum Ordnen der Systemobjekte nach ihrem Namen
	 */
	private static final Comparator<SystemObject> C = 
		new Comparator<SystemObject>(){
			public int compare(SystemObject so1, SystemObject so2) {
				return so1.getName().compareTo(so2.getName());
			}		
		};
	
	/**
	 * Datenverteiler-Verbindung
	 */
	protected ClientDavInterface dav = null;

	/**
	 * alle NS-Sensoren
	 */
	protected SortedSet<SystemObject> nsSensoren = new TreeSet<SystemObject>(C);

	/**
	 * alle NI-Sensoren
	 */
	protected SortedSet<SystemObject> niSensoren = new TreeSet<SystemObject>(C);

	/**
	 * alle LT-Sensoren
	 */
	protected SortedSet<SystemObject> ltSensoren = new TreeSet<SystemObject>(C);

	/**
	 * alle RLF-Sensoren
	 */
	protected SortedSet<SystemObject> rlfSensoren = new TreeSet<SystemObject>(C);

	/**
	 * alle WFD-Sensoren
	 */
	protected SortedSet<SystemObject> wfdSensoren = new TreeSet<SystemObject>(C);
	
	/**
	 * alle SW-Sensoren
	 */
	protected SortedSet<SystemObject> swSensoren = new TreeSet<SystemObject>(C);
	
	/**
	 * alle FBZ-Sensoren
	 */
	protected SortedSet<SystemObject> fbzSensoren = new TreeSet<SystemObject>(C);
	
	/**
	 * letzter Soll-Ergebnis-Wert von einem Sensor
	 */
	protected Map<SystemObject, MeteoErgebnis> ergebnisSoll = new HashMap<SystemObject, MeteoErgebnis>();

	/**
	 * letzter Ist-Ergebnis-Wert von einem Sensor
	 */
	protected Map<SystemObject, MeteoErgebnis> ergebnisIst = new HashMap<SystemObject, MeteoErgebnis>();
	

	/**
	 * Standardkonstruktor
	 * 
	 * @throws Exception leitet die Ausnahmen weiter
	 */
	public MeteorologischeKontrolleTest()
	throws Exception{
		this.dav = DAVTest.getDav();
		PlPruefungLogischUFDTest.initialisiere();
		final Aspect vorgabeAspekt = this.dav.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE);
				
		/**
		 * Ausfallkontrolle ausschalten
		 */
		DataDescription paraAusfallUeberwachung = new DataDescription(
				dav.getDataModel().getAttributeGroup("atg.ufdsAusfallÜberwachung"), //$NON-NLS-1$
				vorgabeAspekt,
				(short)0);
		dav.subscribeSender(this, PlPruefungLogischUFDTest.SENSOREN, paraAusfallUeberwachung, SenderRole.sender());

		/**
		 * Warte bis Anmeldung sicher durch ist
		 */
		Pause.warte(1000L);
		
		/**
		 * Parameter setzen -1 (Ausfallkontrolle aus)
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			Data parameterData = dav.createData(dav.getDataModel().
					getAttributeGroup("atg.ufdsAusfallÜberwachung")); //$NON-NLS-1$
			parameterData.getTimeValue("maxZeitVerzug").setMillis(-1); //$NON-NLS-1$
			ResultData parameter = new ResultData(sensor, 
					paraAusfallUeberwachung, System.currentTimeMillis(), parameterData);			
			this.dav.sendData(parameter);
		}
		
		
		/**
		 * Anmelden zum Senden von Standardparametern
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);

			if(datenArt.equals(UmfeldDatenArt.NS) || 
			   datenArt.equals(UmfeldDatenArt.NI) ||
			   datenArt.equals(UmfeldDatenArt.WFD) ||
			   datenArt.equals(UmfeldDatenArt.SW)){
				DataDescription parameterBeschreibung = new DataDescription(
						this.dav.getDataModel().getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
								UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName()),
					    vorgabeAspekt,
					    (short)0);
				this.dav.subscribeSender(this, sensor, parameterBeschreibung, SenderRole.sender());
			}
		}
		
		/**
		 * Warte bis Parameteranmeldung durch ist
		 */
		Pause.warte(1000L);
		
		/**
		 * Standardparameter senden
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			 
			if(datenArt.equals(UmfeldDatenArt.NS)){
				AttributeGroup atg = this.dav.getDataModel().
						getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
						UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
				Data parameterDatum = this.dav.createData(atg);
				
				parameterDatum.getUnscaledValue("NSGrenzLT").set(-5); //$NON-NLS-1$
				parameterDatum.getUnscaledValue("NSGrenzTrockenRLF").set(70); //$NON-NLS-1$
				parameterDatum.getScaledValue("NSminNI").set(0.1); //$NON-NLS-1$
				parameterDatum.getUnscaledValue("NSGrenzRLF").set(78); //$NON-NLS-1$
				ResultData parameterResultat = new ResultData(sensor,
						new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
				this.dav.sendData(parameterResultat);
			}

			if(datenArt.equals(UmfeldDatenArt.NI)){
				AttributeGroup atg = this.dav.getDataModel().
						getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
						UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
				Data parameterDatum = this.dav.createData(atg);
				
				parameterDatum.getScaledValue("NIgrenzNassNI").set(0.5); //$NON-NLS-1$
				parameterDatum.getUnscaledValue("NIgrenzNassRLF").set(78); //$NON-NLS-1$
				parameterDatum.getScaledValue("NIminNI").set(0.1); //$NON-NLS-1$
				parameterDatum.getUnscaledValue("NIgrenzTrockenRLF").set(70); //$NON-NLS-1$
				parameterDatum.getTimeValue("NIminTrockenRLF").setMillis(STANDARD_T * 2); //$NON-NLS-1$
				ResultData parameterResultat = new ResultData(sensor,
						new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
				this.dav.sendData(parameterResultat);				
			}

			if(datenArt.equals(UmfeldDatenArt.WFD)){
				AttributeGroup atg = this.dav.getDataModel().
						getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
						UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
				Data parameterDatum = this.dav.createData(atg);
				
				parameterDatum.getScaledValue("WFDgrenzNassNI").set(0.5); //$NON-NLS-1$
				parameterDatum.getUnscaledValue("WFDgrenzNassRLF").set(78); //$NON-NLS-1$
				parameterDatum.getTimeValue("WDFminNassRLF").setMillis(STANDARD_T * 2); //$NON-NLS-1$
				ResultData parameterResultat = new ResultData(sensor,
						new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
				this.dav.sendData(parameterResultat);
			}

			if(datenArt.equals(UmfeldDatenArt.SW)){
				AttributeGroup atg = this.dav.getDataModel().
						getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
						UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
				Data parameterDatum = this.dav.createData(atg);

				parameterDatum.getUnscaledValue("SWgrenzTrockenRLF").set(70); //$NON-NLS-1$
				parameterDatum.getUnscaledValue("SWgrenzSW").set(500); //$NON-NLS-1$

				ResultData parameterResultat = new ResultData(sensor,
						new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
				this.dav.sendData(parameterResultat);
			}
		}
		
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
		 * Initialisiere alle Objektmengen
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			if(datenArt.equals(UmfeldDatenArt.NI)){
				this.niSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.NS)){
				this.nsSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.WFD)){
				this.wfdSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.SW)){
				this.swSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.FBZ)){
				this.fbzSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.RLF)){
				this.rlfSensoren.add(sensor);
			}
			if(datenArt.equals(UmfeldDatenArt.LT)){
				this.ltSensoren.add(sensor);
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
	 * Sendet einen Sensorwert
	 * 
	 * @param sensor der Umfelddatensensor
	 * @param wert der zu sendende Wert
	 * @param datenZeitStempel der Datenzeitstempel
	 */
	public final void sendeDatum(final SystemObject sensor, long wert, long datenZeitStempel){
		UmfeldDatenSensorDatum datum = new UmfeldDatenSensorDatum(TestUtensilien.getExterneErfassungDatum(sensor));
		datum.setT(STANDARD_T);
		datum.getWert().setWert(wert);
		ResultData resultat = new ResultData(sensor, datum.getOriginalDatum().getDataDescription(), datenZeitStempel, datum.getDatum());
		try {
			PlPruefungLogischUFDTest.SENDER.sende(resultat);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		}			
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(resultat);
					boolean implausibel = ufdDatum.getStatusMessWertErsetzungImplausibel() == DUAKonstanten.JA;
					this.ergebnisIst.put(resultat.getObject(), 
							new MeteoErgebnis(resultat.getObject(), resultat.getDataTime(), implausibel));
				}
			}
		}
	}
}
