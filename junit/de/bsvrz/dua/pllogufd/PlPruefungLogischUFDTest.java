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
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;

/**
 * Basisklasse der Tests der SWE Pl-Prüfung logisch UFD
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class PlPruefungLogischUFDTest
implements ClientSenderInterface{
	
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
	}
	
	
	/**
	 * Initialisiert alle Umfelddatensensoren als statische Objekte
	 */
	public static final void initialisiere()
	throws Exception {
		if(DAV == null){
			DAV = DAVTest.getDav();
			UmfeldDatenArt.initialisiere(DAV);
			
			SENSOREN.add(ni1 = DAV.getDataModel().getObject("AAA.pllogufd.NI.1")); //$NON-NLS-1$
			SENSOREN.add(ni2 = DAV.getDataModel().getObject("AAA.pllogufd.NI.2")); //$NON-NLS-1$
			SENSOREN.add(ni3 = DAV.getDataModel().getObject("AAA.pllogufd.NI.3")); //$NON-NLS-1$
			
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
