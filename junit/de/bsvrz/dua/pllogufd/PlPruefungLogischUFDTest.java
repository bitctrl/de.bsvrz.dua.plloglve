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

import java.util.ArrayList;
import java.util.List;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.DataNotSubscribedException;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.common.SendSubscriptionNotConfirmed;
import stauma.dav.configuration.interfaces.Aspect;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.pllogufd.testmeteo.MeteoKonst;
import de.bsvrz.dua.pllogufd.typen.UfdsVergleichsOperator;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Basisklasse der Tests der SWE Pl-Prüfung logisch UFD
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class PlPruefungLogischUFDTest
implements ClientSenderInterface{
	
	/**
	 * Standardintervalllänge für die meisten Tests (1s)
	 */
	public static final long STANDARD_T = 2000L;
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Sender-Instanz
	 */
	public static PlPruefungLogischUFDTest SENDER = null;
	
	/**
	 * alle betrachteten Umfelddatensensoren
	 */
	public static SystemObject ni1 = null;
	public static SystemObject ni2 = null; 
	public static SystemObject ni3 = null; 

	public static SystemObject ns1 = null;
	public static SystemObject ns2 = null; 
	public static SystemObject ns3 = null; 

	public static SystemObject fbz1 = null;
	public static SystemObject fbz2 = null; 
	public static SystemObject fbz3 = null; 
	
	public static SystemObject wfd1 = null;
	public static SystemObject wfd2 = null;
	public static SystemObject wfd3 = null;

	public static SystemObject lt1 = null;
	public static SystemObject lt2 = null;
	public static SystemObject lt3 = null;
	
	public static SystemObject rlf1 = null;
	public static SystemObject rlf2 = null;
	public static SystemObject rlf3 = null;
	
	public static SystemObject sw1 = null;
	public static SystemObject sw2 = null;
	public static SystemObject sw3 = null;
	
	public static SystemObject hk1 = null;
	public static SystemObject hk2 = null;
	public static SystemObject hk3 = null;
	
	public static SystemObject fbt1 = null;
	public static SystemObject fbt2 = null;
	public static SystemObject fbt3 = null;
	
	public static SystemObject tt11 = null;
	public static SystemObject tt12 = null;
	public static SystemObject tt13 = null;
	
	public static SystemObject tt31 = null;
	public static SystemObject tt32 = null;
	public static SystemObject tt33 = null;
	
	public static SystemObject rs1 = null;
	public static SystemObject rs2 = null;
	public static SystemObject rs3 = null;
	
	public static SystemObject gt1 = null;
	public static SystemObject gt2 = null;
	public static SystemObject gt3 = null;
	
	public static SystemObject tpt1 = null;
	public static SystemObject tpt2 = null;
	public static SystemObject tpt3 = null;
	
	public static SystemObject wgs1 = null;
	public static SystemObject wgs2 = null;
	public static SystemObject wgs3 = null;
	
	public static SystemObject wgm1 = null;
	public static SystemObject wgm2 = null;
	public static SystemObject wgm3 = null;
	
	public static SystemObject wr1 = null;
	public static SystemObject wr2 = null;
	public static SystemObject wr3 = null;	
	
	/**
	 * Menge aller im Test betrachteten Sensoren
	 */
	public static List<SystemObject> SENSOREN = new ArrayList<SystemObject>();
		
	/**
	 * Datenverteiler-Verbindung
	 */
	public static ClientDavInterface DAV = null;
	
	/**
	 * Parameterdatenbeschreibung für die Ausfallüberwachung
	 */
	public static DataDescription paraAusfallUeberwachung = null;
	
	
	/**
	 * Standardkonstruktor
	 */
	public PlPruefungLogischUFDTest()
	throws Exception{
		
		for(SystemObject sensor:SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			
			DataDescription datenBeschreibung = new DataDescription(
						DAV.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
						DAV.getDataModel().getAspect("asp.externeErfassung"), //$NON-NLS-1$
						(short)0);
			DAV.subscribeSender(this, sensor, datenBeschreibung, SenderRole.source());
		}
		
		/**
		 * Anmelden zum Senden von Parameter für die Meteorologische Kontrolle
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);

			if(datenArt.equals(UmfeldDatenArt.NS) || 
			   datenArt.equals(UmfeldDatenArt.NI) ||
			   datenArt.equals(UmfeldDatenArt.WFD) ||
			   datenArt.equals(UmfeldDatenArt.SW)){
				DataDescription parameterBeschreibung = new DataDescription(
						DAV.getDataModel().getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
								UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName()),
								DAV.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
					    (short)0);
				DAV.subscribeSender(this, sensor, parameterBeschreibung, SenderRole.sender());
			}
		}
				
		/**
		 * Anmeldung auf alle Parameter für die Ausfallkontrolle
		 */
		paraAusfallUeberwachung = new DataDescription(
				DAV.getDataModel().getAttributeGroup("atg.ufdsAusfallÜberwachung"), //$NON-NLS-1$
				DAV.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
				(short)0);
		DAV.subscribeSender(this, PlPruefungLogischUFDTest.SENSOREN, paraAusfallUeberwachung, SenderRole.sender());

		
		/**
		 * Anmeldung auf die Parameter der Differenzialkontrolle
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			DataDescription paraDifferenzialkontrolle = new DataDescription(
					DAV.getDataModel().getAttributeGroup("atg.ufdsDifferenzialKontrolle" + datenArt.getName()), //$NON-NLS-1$
					DAV.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
					(short)0);
			DAV.subscribeSender(this, sensor, paraDifferenzialkontrolle, SenderRole.sender());			
		}

		/**
		 * Warte bis Anmeldung sicher durch ist
		 */
		Pause.warte(1000L);
	}
	
	
	/**
	 * Initialisiert alle Umfelddatensensoren als statische Objekte
	 */
	public static final void initialisiere()
	throws Exception {
		if(DAV == null){
			DAV = DAVTest.getDav();
			UmfeldDatenArt.initialisiere(DAV);

			
//			SENSOREN.add(ni1 = DAV.getDataModel().getObject("AAA.pllogufd.NI.1")); //$NON-NLS-1$
//			SENSOREN.add(fbz1 = DAV.getDataModel().getObject("AAA.pllogufd.FBZ.1")); //$NON-NLS-1$
//			SENSOREN.add(ns1 = DAV.getDataModel().getObject("AAA.pllogufd.NS.1")); //$NON-NLS-1$
//			SENSOREN.add(wfd1 = DAV.getDataModel().getObject("AAA.pllogufd.WFD.1")); //$NON-NLS-1$
//			SENSOREN.add(lt1 = DAV.getDataModel().getObject("AAA.pllogufd.LT.1")); //$NON-NLS-1$
//			SENSOREN.add(rlf1 = DAV.getDataModel().getObject("AAA.pllogufd.RLF.1")); //$NON-NLS-1$
//			SENSOREN.add(sw1 = DAV.getDataModel().getObject("AAA.pllogufd.SW.1")); //$NON-NLS-1$
			
			SENSOREN.add(ni1 = DAV.getDataModel().getObject("AAA.pllogufd.NI.1")); //$NON-NLS-1$
			SENSOREN.add(ni2 = DAV.getDataModel().getObject("AAA.pllogufd.NI.2")); //$NON-NLS-1$
			SENSOREN.add(ni3 = DAV.getDataModel().getObject("AAA.pllogufd.NI.3")); //$NON-NLS-1$

			SENSOREN.add(fbz1 = DAV.getDataModel().getObject("AAA.pllogufd.FBZ.1")); //$NON-NLS-1$
			SENSOREN.add(fbz2 = DAV.getDataModel().getObject("AAA.pllogufd.FBZ.2")); //$NON-NLS-1$
			SENSOREN.add(fbz3 = DAV.getDataModel().getObject("AAA.pllogufd.FBZ.3")); //$NON-NLS-1$

			SENSOREN.add(ns1 = DAV.getDataModel().getObject("AAA.pllogufd.NS.1")); //$NON-NLS-1$
			SENSOREN.add(ns2 = DAV.getDataModel().getObject("AAA.pllogufd.NS.2")); //$NON-NLS-1$
			SENSOREN.add(ns3 = DAV.getDataModel().getObject("AAA.pllogufd.NS.3")); //$NON-NLS-1$
			
			SENSOREN.add(wfd1 = DAV.getDataModel().getObject("AAA.pllogufd.WFD.1")); //$NON-NLS-1$
			SENSOREN.add(wfd2 = DAV.getDataModel().getObject("AAA.pllogufd.WFD.2")); //$NON-NLS-1$
			SENSOREN.add(wfd3 = DAV.getDataModel().getObject("AAA.pllogufd.WFD.3")); //$NON-NLS-1$
			
			SENSOREN.add(lt1 = DAV.getDataModel().getObject("AAA.pllogufd.LT.1")); //$NON-NLS-1$
			SENSOREN.add(lt2 = DAV.getDataModel().getObject("AAA.pllogufd.LT.2")); //$NON-NLS-1$
			SENSOREN.add(lt3 = DAV.getDataModel().getObject("AAA.pllogufd.LT.3")); //$NON-NLS-1$
			
			SENSOREN.add(rlf1 = DAV.getDataModel().getObject("AAA.pllogufd.RLF.1")); //$NON-NLS-1$
			SENSOREN.add(rlf2 = DAV.getDataModel().getObject("AAA.pllogufd.RLF.2")); //$NON-NLS-1$
			SENSOREN.add(rlf3 = DAV.getDataModel().getObject("AAA.pllogufd.RLF.3")); //$NON-NLS-1$
			
			SENSOREN.add(sw1 = DAV.getDataModel().getObject("AAA.pllogufd.SW.1")); //$NON-NLS-1$
			SENSOREN.add(sw2 = DAV.getDataModel().getObject("AAA.pllogufd.SW.2")); //$NON-NLS-1$
			SENSOREN.add(sw3 = DAV.getDataModel().getObject("AAA.pllogufd.SW.3")); //$NON-NLS-1$
			
			SENSOREN.add(hk1 = DAV.getDataModel().getObject("AAA.pllogufd.HK.1")); //$NON-NLS-1$
			SENSOREN.add(hk2 = DAV.getDataModel().getObject("AAA.pllogufd.HK.2")); //$NON-NLS-1$
			SENSOREN.add(hk3 = DAV.getDataModel().getObject("AAA.pllogufd.HK.3")); //$NON-NLS-1$
					
			SENSOREN.add(fbt1 = DAV.getDataModel().getObject("AAA.pllogufd.FBT.1")); //$NON-NLS-1$
			SENSOREN.add(fbt2 = DAV.getDataModel().getObject("AAA.pllogufd.FBT.2")); //$NON-NLS-1$
			SENSOREN.add(fbt3 = DAV.getDataModel().getObject("AAA.pllogufd.FBT.3")); //$NON-NLS-1$
			
			SENSOREN.add(tt11 = DAV.getDataModel().getObject("AAA.pllogufd.TT1.1")); //$NON-NLS-1$
			SENSOREN.add(tt12 = DAV.getDataModel().getObject("AAA.pllogufd.TT1.2")); //$NON-NLS-1$
			SENSOREN.add(tt13 = DAV.getDataModel().getObject("AAA.pllogufd.TT1.3")); //$NON-NLS-1$
	
			SENSOREN.add(tt31 = DAV.getDataModel().getObject("AAA.pllogufd.TT3.1")); //$NON-NLS-1$
			SENSOREN.add(tt32 = DAV.getDataModel().getObject("AAA.pllogufd.TT3.2")); //$NON-NLS-1$
			SENSOREN.add(tt33 = DAV.getDataModel().getObject("AAA.pllogufd.TT3.3")); //$NON-NLS-1$
			
			SENSOREN.add(rs1 = DAV.getDataModel().getObject("AAA.pllogufd.RS.1")); //$NON-NLS-1$
			SENSOREN.add(rs2 = DAV.getDataModel().getObject("AAA.pllogufd.RS.2")); //$NON-NLS-1$
			SENSOREN.add(rs3 = DAV.getDataModel().getObject("AAA.pllogufd.RS.3")); //$NON-NLS-1$
			
			SENSOREN.add(gt1 = DAV.getDataModel().getObject("AAA.pllogufd.GT.1")); //$NON-NLS-1$
			SENSOREN.add(gt2 = DAV.getDataModel().getObject("AAA.pllogufd.GT.2")); //$NON-NLS-1$
			SENSOREN.add(gt3 = DAV.getDataModel().getObject("AAA.pllogufd.GT.3")); //$NON-NLS-1$
			
			SENSOREN.add(tpt1 = DAV.getDataModel().getObject("AAA.pllogufd.TPT.1")); //$NON-NLS-1$
			SENSOREN.add(tpt2 = DAV.getDataModel().getObject("AAA.pllogufd.TPT.2")); //$NON-NLS-1$
			SENSOREN.add(tpt3 = DAV.getDataModel().getObject("AAA.pllogufd.TPT.3")); //$NON-NLS-1$
			
			SENSOREN.add(wgs1 = DAV.getDataModel().getObject("AAA.pllogufd.WGS.1")); //$NON-NLS-1$
			SENSOREN.add(wgs2 = DAV.getDataModel().getObject("AAA.pllogufd.WGS.2")); //$NON-NLS-1$
			SENSOREN.add(wgs3 = DAV.getDataModel().getObject("AAA.pllogufd.WGS.3")); //$NON-NLS-1$
			
			SENSOREN.add(wgm1 = DAV.getDataModel().getObject("AAA.pllogufd.WGM.1")); //$NON-NLS-1$
			SENSOREN.add(wgm2 = DAV.getDataModel().getObject("AAA.pllogufd.WGM.2")); //$NON-NLS-1$
			SENSOREN.add(wgm3 = DAV.getDataModel().getObject("AAA.pllogufd.WGM.3")); //$NON-NLS-1$
			
			SENSOREN.add(wr1 = DAV.getDataModel().getObject("AAA.pllogufd.WR.1")); //$NON-NLS-1$
			SENSOREN.add(wr2 = DAV.getDataModel().getObject("AAA.pllogufd.WR.2")); //$NON-NLS-1$
			SENSOREN.add(wr3 = DAV.getDataModel().getObject("AAA.pllogufd.WR.3")); //$NON-NLS-1$
			
			SENDER = new PlPruefungLogischUFDTest();
		}
	}
	

	/**
	 * Versendet ein Umfelddatum als Quelle
	 * 
	 * @param resultat ein Umfelddatum
	 * @throws Exception wird weitergereicht
	 */
	public final void sende(final ResultData resultat)
	throws Exception{
		DAV.sendData(resultat);
	}
	
	
	/**
	 * Setzt alle Parameter der Meteorologischen Kontrolle an bzw. aus 
	 *
	 * @param an Standardparameter anschalten?
	 */
	public final void setMeteoKontrolle(boolean an){
		Aspect vorgabeAspekt = DAV.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE);	

		if(an){
			/**
			 * Standardparameter senden
			 */
			for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
				UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
				 
				if(datenArt.equals(UmfeldDatenArt.NS)){
					AttributeGroup atg = DAV.getDataModel().
							getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
							UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
					Data parameterDatum = DAV.createData(atg);
					
					parameterDatum.getUnscaledValue("NSGrenzLT").set(MeteoKonst.NSGrenzLT); //$NON-NLS-1$
					parameterDatum.getUnscaledValue("NSGrenzTrockenRLF").set(MeteoKonst.NIgrenzTrockenRLF); //$NON-NLS-1$
					parameterDatum.getScaledValue("NSminNI").set(MeteoKonst.NSminNI); //$NON-NLS-1$
					parameterDatum.getUnscaledValue("NSGrenzRLF").set(MeteoKonst.NSGrenzRLF); //$NON-NLS-1$
					ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
					try {
						DAV.sendData(parameterResultat);
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.error(Konstante.LEERSTRING, e);
					}
				}

				if(datenArt.equals(UmfeldDatenArt.NI)){
					AttributeGroup atg = DAV.getDataModel().
							getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
							UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
					Data parameterDatum = DAV.createData(atg);
					
					parameterDatum.getScaledValue("NIgrenzNassNI").set(MeteoKonst.NIgrenzNassNI); //$NON-NLS-1$
					parameterDatum.getUnscaledValue("NIgrenzNassRLF").set(MeteoKonst.NIgrenzNassRLF); //$NON-NLS-1$
					parameterDatum.getScaledValue("NIminNI").set(MeteoKonst.NIminNI); //$NON-NLS-1$
					parameterDatum.getUnscaledValue("NIgrenzTrockenRLF").set(MeteoKonst.NIgrenzTrockenRLF); //$NON-NLS-1$
					parameterDatum.getTimeValue("NIminTrockenRLF").setMillis(MeteoKonst.NIminTrockenRLF); //$NON-NLS-1$
					ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
					try {
						DAV.sendData(parameterResultat);
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.error(Konstante.LEERSTRING, e);
					}
				}

				if(datenArt.equals(UmfeldDatenArt.WFD)){
					AttributeGroup atg = DAV.getDataModel().
							getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
							UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
					Data parameterDatum = DAV.createData(atg);
					
					parameterDatum.getScaledValue("WFDgrenzNassNI").set(MeteoKonst.WFDgrenzNassNI); //$NON-NLS-1$
					parameterDatum.getUnscaledValue("WFDgrenzNassRLF").set(MeteoKonst.WFDgrenzNassRLF); //$NON-NLS-1$
					parameterDatum.getTimeValue("WDFminNassRLF").setMillis(MeteoKonst.WDFminNassRLF); //$NON-NLS-1$
					ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
					try {
						DAV.sendData(parameterResultat);
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.error(Konstante.LEERSTRING, e);
					}
				}

				if(datenArt.equals(UmfeldDatenArt.SW)){
					AttributeGroup atg = DAV.getDataModel().
							getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
							UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
					Data parameterDatum = DAV.createData(atg);

					parameterDatum.getUnscaledValue("SWgrenzTrockenRLF").set(MeteoKonst.SWgrenzTrockenRLF); //$NON-NLS-1$
					parameterDatum.getUnscaledValue("SWgrenzSW").set(MeteoKonst.SWgrenzSW); //$NON-NLS-1$

					ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
					try {
						DAV.sendData(parameterResultat);
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.error(Konstante.LEERSTRING, e);
					}
				}			
			}
		}else{
			/**
			 * Alle Vergleichswerte auf <code>fehlerhaft</code> setzen
			 */
			for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
				UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
				 
				if(datenArt.equals(UmfeldDatenArt.NS)){
					AttributeGroup atg = DAV.getDataModel().
							getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
							UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
					Data parameterDatum = DAV.createData(atg);
					
					UmfeldDatenSensorWert ltWert = new UmfeldDatenSensorWert(UmfeldDatenArt.LT);
					ltWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NSGrenzLT").set(ltWert.getWert()); //$NON-NLS-1$

					UmfeldDatenSensorWert rlfWert = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NSGrenzTrockenRLF").set(rlfWert.getWert()); //$NON-NLS-1$
					
					UmfeldDatenSensorWert niWert = new UmfeldDatenSensorWert(UmfeldDatenArt.NI);
					niWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NSminNI").set(niWert.getWert()); //$NON-NLS-1$

					UmfeldDatenSensorWert nsRlfWert = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
					nsRlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NSGrenzRLF").set(nsRlfWert.getWert()); //$NON-NLS-1$
					ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
					try {
						DAV.sendData(parameterResultat);
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.error(Konstante.LEERSTRING, e);
					}
				}

				if(datenArt.equals(UmfeldDatenArt.NI)){
					AttributeGroup atg = DAV.getDataModel().
							getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
							UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
					Data parameterDatum = DAV.createData(atg);
					
					UmfeldDatenSensorWert niWert = new UmfeldDatenSensorWert(UmfeldDatenArt.NI);
					niWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NIgrenzNassNI").set(niWert.getWert()); //$NON-NLS-1$
					
					UmfeldDatenSensorWert rlfWert = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NIgrenzNassRLF").set(rlfWert.getWert()); //$NON-NLS-1$
					
					niWert = new UmfeldDatenSensorWert(UmfeldDatenArt.NI);
					niWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NIminNI").set(niWert.getWert()); //$NON-NLS-1$

					rlfWert = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("NIgrenzTrockenRLF").set(rlfWert.getWert()); //$NON-NLS-1$
					
					parameterDatum.getTimeValue("NIminTrockenRLF").setMillis(STANDARD_T * 2); //$NON-NLS-1$
					ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
					try {
						DAV.sendData(parameterResultat);
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.error(Konstante.LEERSTRING, e);
					}
				}

				if(datenArt.equals(UmfeldDatenArt.WFD)){
					AttributeGroup atg = DAV.getDataModel().
							getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
							UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
					Data parameterDatum = DAV.createData(atg);
					
					UmfeldDatenSensorWert niWert = new UmfeldDatenSensorWert(UmfeldDatenArt.NI);
					niWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("WFDgrenzNassNI").set(niWert.getWert()); //$NON-NLS-1$
					
					UmfeldDatenSensorWert rlfWert = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("WFDgrenzNassRLF").set(rlfWert.getWert()); //$NON-NLS-1$
					
					parameterDatum.getTimeValue("WDFminNassRLF").setMillis(STANDARD_T * 2); //$NON-NLS-1$
					ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
					try {
						DAV.sendData(parameterResultat);
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.error(Konstante.LEERSTRING, e);
					}
				}

				if(datenArt.equals(UmfeldDatenArt.SW)){
					AttributeGroup atg = DAV.getDataModel().
							getAttributeGroup("atg.ufdsMeteorologischeKontrolle" + //$NON-NLS-1$
							UmfeldDatenArt.getUmfeldDatenArtVon(sensor).getName());
					Data parameterDatum = DAV.createData(atg);

					UmfeldDatenSensorWert rlfWert = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
					rlfWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("SWgrenzTrockenRLF").set(rlfWert.getWert()); //$NON-NLS-1$
					
					UmfeldDatenSensorWert swWert = new UmfeldDatenSensorWert(UmfeldDatenArt.RLF);
					swWert.setFehlerhaftAn();
					parameterDatum.getUnscaledValue("SWgrenzSW").set(swWert.getWert()); //$NON-NLS-1$

					ResultData parameterResultat = new ResultData(sensor,
							new DataDescription(atg, vorgabeAspekt, (short)0), System.currentTimeMillis(), parameterDatum);
					try {
						DAV.sendData(parameterResultat);
					} catch (Exception e) {
						e.printStackTrace();
						LOGGER.error(Konstante.LEERSTRING, e);
					}
				}			
			}
		}
	}
	
	
	/**
	 * Setzt den maximalen Zeitverzug eines Umfelddatensensors
	 * 
	 * @param obj Umfelddatensensor
	 * @param verzugInMillis maximalen Zeitverzug in ms
	 */
	public final void setMaxAusfallFuerSensor(final SystemObject obj, final long verzugInMillis){
		Data parameterData = DAV.createData(DAV.getDataModel().
				getAttributeGroup("atg.ufdsAusfallÜberwachung")); //$NON-NLS-1$
		parameterData.getTimeValue("maxZeitVerzug").setMillis(verzugInMillis); //$NON-NLS-1$
		ResultData parameter = new ResultData(obj, 
				paraAusfallUeberwachung, System.currentTimeMillis(), parameterData);
		
		try {
			DAV.sendData(parameter);
		} catch (DataNotSubscribedException e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		} catch (SendSubscriptionNotConfirmed e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		}
	}


	/**
	 * Setzt die Parameter eines Umfelddatensensors für de Differenzialkontrolle
	 * 
	 * @param sensor der Sensor
	 * @param wert der Vergleichswert, demgegenüber der Sensorwert kleiner sein muss, damit die
	 * Differenzialkontrolle durchgeführt werden kann
	 * @param zeit die Zeit, die ein Wert maximal gleich bleiben darf
	 */
	public final void setDiffPara(SystemObject sensor, int wert, long zeit){
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
		Data datum = DAV.createData(DAV.getDataModel().getAttributeGroup(
				"atg.ufdsDifferenzialKontrolle" + datenArt.getName())); //$NON-NLS-1$

		datum.getUnscaledValue("Operator").set(UfdsVergleichsOperator.KLEINER.getCode()); //$NON-NLS-1$
		datum.getUnscaledValue(datenArt.getAbkuerzung() + "Grenz").set(wert); //$NON-NLS-1$
		datum.getTimeValue(datenArt.getAbkuerzung() + "maxZeit").setMillis(zeit); //$NON-NLS-1$

		DataDescription paraDifferenzialkontrolle = new DataDescription(
				DAV.getDataModel().getAttributeGroup("atg.ufdsDifferenzialKontrolle" + datenArt.getName()), //$NON-NLS-1$
				DAV.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
				(short)0);
		ResultData parameterSatz = new ResultData(sensor, paraDifferenzialkontrolle, System.currentTimeMillis(), datum);
		try {
			DAV.sendData(parameterSatz);
		} catch (DataNotSubscribedException e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		} catch (SendSubscriptionNotConfirmed e) {
			e.printStackTrace();
			LOGGER.error(Konstante.LEERSTRING, e);
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		// mache nichts
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return false;
	}
	
}
