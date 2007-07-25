/** 
 * Segment 4 Datenübernahme und Aufbereitung (DUA)
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

package de.bsvrz.dua.plloglve.util.para;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ResultData;
import stauma.dav.clientside.SenderRole;
import stauma.dav.configuration.interfaces.AttributeGroup;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.plloglve.util.CSVImporter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;


/**
 * Abstrakte Klasse zum Einlesen von Parametern aus der CSV-Datei 
 * innerhalb der Prüfspezifikation
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public abstract class AbstraktParameterImport
extends CSVImporter
implements ClientSenderInterface{
	
	/**
	 * Verbindung zum Datenverteiler
	 */
	protected static ClientDavInterface DAV = null;
	
	/**
	 * Systemobjekt, für das die Parameter gesetzt werden sollen
	 */
	protected SystemObject objekt = null;
		

	/**
	 * Standardkonstruktor
	 * 
	 * @param dav Datenverteier-Verbindung
	 * @param objekt das Systemobjekt, für das die Parameter gesetzt werden sollen
	 * @param csvQuelle Quelle der Daten (CSV-Datei)
	 * @throws Exception falls dieses Objekt nicht vollständig initialisiert werden konnte
	 */
	public AbstraktParameterImport(final ClientDavInterface dav, 
								   final SystemObject objekt,
						   		   final String csvQuelle)
	throws Exception{
		super(csvQuelle);
		if(DAV == null){
			DAV = dav;
		}
		
		this.objekt = objekt;
		DAV.subscribeSender(this, objekt, new DataDescription(
				this.getParameterAtg(), 
				DAV.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
				(short)0), SenderRole.sender());
		
//		System.out.println("Obj: " + objekt + "An: " + new DataDescription(
//				this.getParameterAtg(), 
//				DAV.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
//				(short)0));
		
		/**
		 * Tabellenkopf überspringen
		 */
		this.getNaechsteZeile();
	}
	
		
	/**
	 * Führt den Parameterimport aus
	 * 
	 * @param int index
	 * @throws Exception wenn die Parameter nicht vollständig
	 * importiert werden konnten
	 */
	public final void importiereParameter(int index)
	throws Exception{
		this.reset();
		this.getNaechsteZeile();
		String[] zeile = null;
		
		Data parameter = DAV.createData(this.getParameterAtg());
		
		while( (zeile = this.getNaechsteZeile()) != null ){
			String attributInCSVDatei = zeile[0];
			String wert = zeile[1];
			
			String attPfad = getAttributPfadVon(attributInCSVDatei, index);
			if(attPfad != null){
				try{
					long l = Long.parseLong(wert);
					DUAUtensilien.getAttributDatum(attPfad, parameter).asUnscaledValue().set(l);
				}catch(NumberFormatException ex){
					double d = Double.parseDouble(wert);
					DUAUtensilien.getAttributDatum(attPfad, parameter).asUnscaledValue().set(d);
				}
			}
		}
		
		
		ResultData resultat = new ResultData(this.objekt, new DataDescription(
				this.getParameterAtg(), 
				DAV.getDataModel().getAspect(Konstante.DAV_ASP_PARAMETER_VORGABE),
				(short)0), System.currentTimeMillis(), this.fuelleRestAttribute(parameter));
		//System.out.println(resultat);
		DAV.sendData(resultat);
	}
	

	/**
	 * Setzt alle restlichen Attribute innerhalb von diesem Datensatz
	 * abhängig von der tatsächlichen Attributgruppe
	 * 
	 * @param datensatz ein Datensatz
	 * @return der veränderte (vollständig ausgefüllte Datensatz)
	 */
	public Data fuelleRestAttribute(Data datensatz){
		return datensatz;
	}

	
	/**
	 * Erfragt den Attributpfad zu einem Attribut, das in der CSV-Datei 
	 * den übergebenen Namen hat
	 *  
	 * @param attributInCSVDatei Attributname innerhalb der CSV-Datei
	 * @param index index innerhalb von CVS-Datei
	 * @return den kompletten Attributpfad zum assoziierten DAV-Attribut
	 */
	protected abstract String getAttributPfadVon(final String attributInCSVDatei,
												 final int index);
	
	
	/**
	 * Erfragt die Parameter-Atg
	 * 
	 * @return die Parameter-Atg
	 */
	protected abstract AttributeGroup getParameterAtg();
	
	
	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		// keine Überprüfung
	}
	

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return false;
	}
	
}
